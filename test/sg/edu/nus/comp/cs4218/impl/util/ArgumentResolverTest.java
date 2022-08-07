package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ARGS;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.deleteFileIfExists;

public class ArgumentResolverTest {
    private final static String WORD_1 = "Hello";
    private final static String WORD_2 = "World";
    private final static String SRC_FILE = "123.txt";
    private final static String DEST_FILE = "321.txt";
    private final static String GLOBED_TXT_FILE = "*.txt";
    private final static String GLOBED_ABS_FILE = "*.ABSENT";
    private final static String ECHO_COMMAND = "echo World";
    private final static String STRSTR_FORMAT = "%s %s";
    private final static String ECHO_STR_FORMAT = "echo %s";
    @TempDir
    public static Path folderPath;
    private static ArgumentResolver resolver;

    @BeforeAll
    public static void setup() throws IOException {
        resolver = new ArgumentResolver();
        Files.createDirectories(folderPath);
    }

    @AfterAll
    public static void teardown() throws IOException {
        deleteFileIfExists(Path.of(Environment.currentDirectory).resolve(DEST_FILE));
    }

    private String processArg(String rawArg) throws AbstractApplicationException, ShellException,
            FileNotFoundException, InvalidArgsException {
        List<String> parsedArgs = resolver.resolveOneArgument(rawArg);
        if (parsedArgs.isEmpty()) {
            throw new InvalidArgsException(ERR_NO_ARGS);
        }
        return String.join(" ", parsedArgs);
    }

    private String parseArgs(List<String> rawArgs) throws AbstractApplicationException, ShellException,
            FileNotFoundException, InvalidArgsException {
        List<String> parsedArgs = resolver.parseArguments(rawArgs);
        if (parsedArgs.isEmpty()) {
            throw new InvalidArgsException(ERR_NO_ARGS);
        }
        return String.join(" ", parsedArgs);
    }

    @Test
    public void resolveOneArgument_singleQuotes_contentPreserved() throws Exception {
        String rawArg = String.format("'%s %s'", WORD_1, WORD_2);
        String expected = String.format(STRSTR_FORMAT, WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_singleQuotesTab_contentAndTabPreserved() throws Exception {
        String rawArg = String.format("'%s %s\t'", WORD_1, WORD_2);
        String expected = String.format("%s %s\\t", WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_singleQuotesAsterisk_fileNamePreserved() throws Exception {
        String rawArg = String.format("'%s'", GLOBED_TXT_FILE);
        assertEquals(GLOBED_TXT_FILE, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_singleQuotesSingleQuotes_singleQuotesRemoved() throws Exception {
        // Quotes cannot nest in the same type of quotes
        // '%s '%s'' will just be a concatenation of '%s ' and '%s'
        String rawArg = String.format("'%s '%s''", WORD_1, WORD_2);
        String expected = String.format(STRSTR_FORMAT, WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_singleQuotesDoubleQuotes_contentAndDoubleQuotesPreserved() throws Exception {
        String rawArg = String.format("'%s \"%s\"'", WORD_1, WORD_2);
        String expected = String.format("%s \"%s\"", WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_singleQuotesBackQuotes_contentAndCommandPreserved() throws Exception {
        String rawArg = String.format("'%s `%s`'", WORD_1, ECHO_COMMAND);
        String expected = String.format("%s `%s`", WORD_1, ECHO_COMMAND);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_singleQuotesPipe_contentAndSymbolPreserved() throws Exception {
        String rawArg = String.format("'%s | %s'", WORD_1, WORD_2);
        String expected = String.format("%s | %s", WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_singleQuotesInputRedirect_contentAndSymbolPreserved() throws Exception {
        String rawArg = String.format("'%s < %s'", WORD_1, WORD_2);
        String expected = String.format("%s < %s", WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_singleQuotesOutputRedirect_contentAndSymbolPreserved() throws Exception {
        String rawArg = String.format("'%s > %s'", WORD_1, WORD_2);
        String expected = String.format("%s > %s", WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_singleQuotesSemiColon_contentAndSymbolPreserved() throws Exception {
        String rawArg = String.format("'%s ; %s'", WORD_1, WORD_2);
        String expected = String.format("%s ; %s", WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_singleQuotesSpace_contentAndSpacesPreserved() throws Exception {
        String rawArg = String.format("'%s    %s'", WORD_1, WORD_2);
        String expected = String.format("%s    %s", WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_doubleQuotes_contentPreserved() throws Exception {
        String rawArg = String.format("\"%s %s\"", WORD_1, WORD_2);
        String expected = String.format(STRSTR_FORMAT, WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_doubleQuotesTab_contentAndTabPreserved() throws Exception {
        String rawArg = String.format("\"%s %s\t\"", WORD_1, WORD_2);
        String expected = String.format("%s %s\\t", WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_doubleQuotesAsterisk_fileNamePreserved() throws Exception {
        String rawArg = String.format("\"%s\"", GLOBED_TXT_FILE);
        assertEquals(GLOBED_TXT_FILE, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_doubleQuotesSingleQuotes_contentAndSingleQuotesPreserved() throws Exception {
        String rawArg = String.format("\"%s '%s'\"", WORD_1, WORD_2);
        String expected = String.format("%s '%s'", WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_doubleQuotesDoubleQuotes_doubleQuotesRemoved() throws Exception {
        // Quotes cannot nest in the same type of quotes
        // "%s "%s"" will just be a concatenation of "%s " and "%s"
        String rawArg = String.format("\"%s \"%s\"\"", WORD_1, WORD_2);
        String expected = String.format("%s %s", WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_doubleQuotesBackQuotes_contentAndEchoOutputPreserved() throws Exception {
        String rawArg = String.format("\"%s `%s`\"", WORD_1, ECHO_COMMAND);
        String expected = String.format(STRSTR_FORMAT, WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_doubleQuotesPipe_contentAndSymbolPreserved() throws Exception {
        String rawArg = String.format("\"%s | %s\"", WORD_1, WORD_2);
        String expected = String.format("%s | %s", WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_doubleQuotesInputRedirect_contentAndSymbolPreserved() throws Exception {
        String rawArg = String.format("\"%s < %s\"", WORD_1, WORD_2);
        String expected = String.format("%s < %s", WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_doubleQuotesOutputRedirect_contentAndSymbolPreserved() throws Exception {
        String rawArg = String.format("\"%s > %s\"", WORD_1, WORD_2);
        String expected = String.format("%s > %s", WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_doubleQuotesSemiColon_contentAndSymbolPreserved() throws Exception {
        String rawArg = String.format("\"%s; %s\"", WORD_1, WORD_2);
        String expected = String.format("%s; %s", WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_doubleQuotesSpace_contentAndSpacesPreserved() throws Exception {
        String rawArg = String.format("\"%s    %s\"", WORD_1, WORD_2);
        String expected = String.format("%s    %s", WORD_1, WORD_2);
        assertEquals(expected, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_backQuotes_echoOutputReturned() throws Exception {
        String rawArg = String.format("`echo %s`", WORD_2);
        assertEquals(WORD_2, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_backQuotesTab_contentPreserved() throws Exception {
        String rawArg = String.format("`echo %s\t`", WORD_2);
        assertEquals(WORD_2, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_backQuotesAsterisk_throwException() {
        String rawArg = String.format("`ls %s`", GLOBED_ABS_FILE);
        if (System.getProperty("os.name").startsWith("Window")) {
            assertThrows(LsException.class, () -> processArg(rawArg));
        } else {
            assertThrows(InvalidArgsException.class, () -> processArg(rawArg));
        }
    }

    @Test
    public void resolveOneArgument_backQuotesSingleQuotes_echoOutputPreserved() throws Exception {
        String rawArg = String.format("`echo '%s'`", WORD_2);
        assertEquals(WORD_2, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_backQuotesDoubleQuotes_echoOutputPreserved() throws Exception {
        String rawArg = String.format("`echo \"%s\"`", WORD_2);
        assertEquals(WORD_2, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_backQuotesBackQuotes_contentPreserved() throws Exception {
        String rawArg = String.format("`echo `%s``", WORD_2);
        assertEquals(WORD_2, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_backQuotesPipe_contentPreserved() throws Exception {
        String rawArg = String.format("`echo %s | cat`", WORD_2);
        assertEquals(WORD_2, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_backQuotesInputRedirect_contentPreserved() {
        String rawArg = String.format("`cat < %s", SRC_FILE);
        assertThrows(InvalidArgsException.class, () -> processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_backQuotesOutputRedirect_contentPreserved() {
        String rawArg = String.format("`echo %s > %s`", WORD_2, folderPath.resolve(DEST_FILE).toString());
        assertThrows(InvalidArgsException.class, () -> processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_backQuotesSemiColon_echoOutputPreserved() throws Exception {
        String rawArg = String.format("`echo %s; echo %s`", WORD_1, WORD_2);
        assertEquals(WORD_1 + " " + WORD_2, processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_backQuotesSpace_throwException() {
        String rawArg = String.format("`echo \" \"`");
        assertThrows(InvalidArgsException.class, () -> processArg(rawArg));
    }

    // Command Substitution TCs
    @Test
    public void resolveOneArgument_singleQuotes_commandSubstitutionNotExecuted() throws Exception {
        String rawArg = String.format("'%s:`echo %s\" \"`.'", WORD_1, WORD_2);
        assertEquals(String.format("%s:`echo %s\" \"`.", WORD_1, WORD_2), processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_doubleQuotes_commandSubstitutionExecuted() throws Exception {
        String rawArg = String.format("\"%s:`echo %s\" \"`.\"", WORD_2, WORD_1);
        assertEquals(String.format("%s:%s .", WORD_2, WORD_1), processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_doubleQuotesWrappedInSingleQuotes_commandSubstitutionNotExecuted() throws Exception {
        String rawArg = String.format("'\"%s:`echo \" \"`.\"'", WORD_1);
        assertEquals(String.format("\"%s:`echo \" \"`.\"", WORD_1), processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_singleQuotesWrappedInDoubleQuotes_commandSubstitutionExecuted() throws Exception {
        String rawArg = String.format("\"'%s:`echo \" \"`.'\"", WORD_2);
        assertEquals(String.format("'%s: .'", WORD_2), processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_doubleBackQuotes_commandSubstitutionNotExecuted() throws Exception {
        String rawArg = String.format("`echo `echo %s``", WORD_1);
        assertEquals(String.format(ECHO_STR_FORMAT, WORD_1, WORD_2), processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_multipleBackQuotes_lastCommandSubstitutionNotExecuted() throws Exception {
        String rawArg = String.format("`echo `echo `echo %s```", WORD_1);
        assertEquals(String.format(ECHO_STR_FORMAT, WORD_1, WORD_2), processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_singleQuotesWithMultipleBackQuotes_commandSubstitutionNotExecuted() throws Exception {
        // The single quote disabled all the special characters from first back quotes
        String rawArg = String.format("'%s:`echo `echo \" \"``.'", WORD_2);
        assertEquals(String.format("%s:`echo `echo \" \"``.", WORD_2), processArg(rawArg));
    }

    @Test
    public void resolveOneArgument_doubleQuotesWithMultipleBackQuotes_lastCommandSubstitutionNotExecuted() throws Exception {
        // Second echo is treated as string as disabled by the double quotes
        String rawArg = String.format("\"%s:`echo `echo %s\" \"``.\"", WORD_2, WORD_1);
        assertEquals(String.format("%s:echo %s .", WORD_2, WORD_1), processArg(rawArg));
    }

    @Test
    public void parseArguments_singleQuotes_commandSubstitutionNotExecuted() throws Exception {
        String rawArg = String.format("'%s:`echo %s\" \"`.'", WORD_1, WORD_2);
        List<String> rawArgs = Arrays.asList(rawArg);
        assertEquals(String.format("%s:`echo %s\" \"`.", WORD_1, WORD_2), parseArgs(rawArgs));
    }

    @Test
    public void parseArguments_doubleQuotes_commandSubstitutionExecuted() throws Exception {
        String rawArg = String.format("\"%s:`echo %s\" \"`.\"", WORD_2, WORD_1);
        List<String> rawArgs = Arrays.asList(rawArg);
        assertEquals(String.format("%s:%s .", WORD_2, WORD_1), parseArgs(rawArgs));
    }

    @Test
    public void parseArguments_doubleQuotesWrappedInSingleQuotes_commandSubstitutionNotExecuted() throws Exception {
        String rawArg = String.format("'\"%s:`echo \" \"`.\"'", WORD_1);
        List<String> rawArgs = Arrays.asList(rawArg);
        assertEquals(String.format("\"%s:`echo \" \"`.\"", WORD_1), parseArgs(rawArgs));
    }

    @Test
    public void parseArguments_singleQuotesWrappedInDoubleQuotes_commandSubstitutionExecuted() throws Exception {
        String rawArg = String.format("\"'%s:`echo \" \"`.'\"", WORD_2);
        List<String> rawArgs = Arrays.asList(rawArg);
        assertEquals(String.format("'%s: .'", WORD_2), parseArgs(rawArgs));
    }

    @Test
    public void parseArguments_doubleBackQuotes_commandSubstitutionNotExecuted() throws Exception {
        String rawArg = String.format("`echo `echo %s``", WORD_1);
        List<String> rawArgs = Arrays.asList(rawArg);
        assertEquals(String.format(ECHO_STR_FORMAT, WORD_1, WORD_2), parseArgs(rawArgs));
    }

    @Test
    public void parseArguments_multipleBackQuotes_lastCommandSubstitutionNotExecuted() throws Exception {
        String rawArg = String.format("`echo `echo `echo %s```", WORD_1);
        List<String> rawArgs = Arrays.asList(rawArg);
        assertEquals(String.format(ECHO_STR_FORMAT, WORD_1, WORD_2), parseArgs(rawArgs));
    }

    @Test
    public void parseArguments_singleQuotesWithMultipleBackQuotes_commandSubstitutionNotExecuted() throws Exception {
        // The single quote disabled all the special characters from first back quotes
        String rawArg = String.format("'%s:`echo `echo \" \"``.'", WORD_2);
        List<String> rawArgs = Arrays.asList(rawArg);
        assertEquals(String.format("%s:`echo `echo \" \"``.", WORD_2), parseArgs(rawArgs));
    }

    @Test
    public void parseArguments_doubleQuotesWithMultipleBackQuotes_lastCommandSubstitutionNotExecuted() throws Exception {
        // Second echo is treated as string as disabled by the double quotes
        String rawArg = String.format("\"%s:`echo `echo %s\" \"``.\"", WORD_2, WORD_1);
        List<String> rawArgs = Arrays.asList(rawArg);
        assertEquals(String.format("%s:echo %s .", WORD_2, WORD_1), parseArgs(rawArgs));
    }
}
