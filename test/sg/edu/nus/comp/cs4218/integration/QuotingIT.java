package sg.edu.nus.comp.cs4218.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.exception.CpException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.SortException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.testutils.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_PATH;

public class QuotingIT {

    private final static String LINE_BREAK = System.lineSeparator();
    private final static String WORD_1 = "Apple";
    private final static String WORD_2 = "Pear";
    private final static String WORD_3 = "Banana";
    private final static String TEXT =
            WORD_1 + LINE_BREAK + WORD_2 + LINE_BREAK + WORD_3 + LINE_BREAK + WORD_1 + LINE_BREAK + WORD_2;
    private final static int TEXT_LINE_COUNT = (int) TEXT.lines().count();
    private final static int TEXT_WORD_COUNT = TEXT.split(LINE_BREAK).length;
    private final static int TEXT_BYTE_COUNT = (TEXT + LINE_BREAK).getBytes().length;
    private final static String SORTED_TEXT =
            WORD_1 + LINE_BREAK + WORD_1 + LINE_BREAK + WORD_3 + LINE_BREAK + WORD_2 + LINE_BREAK + WORD_2;
    private final static String SORTED_FUSED_TEXT =
            WORD_1 + LINE_BREAK + WORD_1 + LINE_BREAK + WORD_1 + LINE_BREAK + WORD_1 + LINE_BREAK + WORD_3 +
                    LINE_BREAK + WORD_3 + LINE_BREAK + WORD_2 + LINE_BREAK + WORD_2 + LINE_BREAK + WORD_2 + LINE_BREAK + WORD_2;
    private final static String ECHO_KEYWORD = "echo";
    private final static String LS_KEYWORD = "ls";
    private final static String WC_KEYWORD = "wc";
    private final static String CAT_KEYWORD = "cat";
    private final static String GREP_KEYWORD = "grep";
    private final static String GREP_PATTERN = WORD_1.substring(0, 3);
    private final static String CUT_KEYWORD = "cut";
    private final static String CUT_FLAG = "-b";
    private final static String CUT_POS = "1";
    private final static String SORT_KEYWORD = "sort";
    private final static String RM_KEYWORD = "rm";
    private final static String TEE_KEYWORD = "tee";
    private final static String TEE_INPUT = "a" + LINE_BREAK + "b" + LINE_BREAK + "c";
    private final static String CP_KEYWORD = "cp";
    private final static String CP_FLAG = "-r";
    private final static String CD_KEYWORD = "cd";
    private final static String PASTE_KEYWORD = "paste";
    private final static String UNIQ_KEYWORD = "uniq";
    private final static String MV_KEYWORD = "mv";
    private final static String FILE = "a.txt";
    private final static String OTHER_FILE = "b.txt";
    private final static String INVALID_FILE = "DONT_EXIST.txt";
    private final static String FOLDER = "DIRECTORY";
    private final static String OTHER_FOLDER = "OTHER_DIRECTORY";
    private final static String INVALID_FOLDER = "DONT_EXIST";
    private final static String FORMAT_SINGLE = "'%s'";
    private final static String FORMAT_DOUBLE = "\"%s\"";
    private final static String FORMAT_BACKQUOTE = "`%s %s`";
    private final static String FORMAT_SINGDOUBLE = "'\"%s\"'";
    private final static String FORMAT_BACKSINGLE = "`%s '%s'`";
    private final static String FORMAT_BACKDOUBLE = "`%s \"%s\"`";
    private final static String FORMAT_WC = "\t%7d\t%7d\t%7d\t%s";
    private final static String FORMAT_GREP = "%s: %s";
    private final static String FORMAT_PASTE = "%s\t%s";
    private final static String ENV_DIR_REF = Environment.currentDirectory;
    @TempDir
    public static Path testPath;
    private static ByteArrayInputStream inputCapture;
    private static ByteArrayOutputStream outputCapture;
    private static CallCommand callCommand;
    private static ApplicationRunner applicationRunner;
    private static ArgumentResolver argumentResolver;
    private static Path filePath;
    private static Path otherFilePath;
    private static Path invalidFilePath;
    private static Path folderPath;
    private static Path otherFolderPath;
    private static Path invalidFolderPath;

    @BeforeAll
    public static void setUp() throws IOException {
        outputCapture = new ByteArrayOutputStream();
        applicationRunner = new ApplicationRunner();
        argumentResolver = new ArgumentResolver();

        Files.createDirectories(testPath);

        filePath = testPath.resolve(FILE);
        otherFilePath = testPath.resolve(OTHER_FILE);
        invalidFilePath = testPath.resolve(INVALID_FILE);
        folderPath = testPath.resolve(FOLDER);
        otherFolderPath = testPath.resolve(OTHER_FOLDER);
        invalidFolderPath = testPath.resolve(INVALID_FOLDER);

        Files.write(filePath, TEXT.getBytes());
        Files.write(otherFilePath, TEXT.getBytes());
        Files.createDirectories(folderPath);
        Files.createDirectories(otherFolderPath);
    }

    @BeforeEach
    public void start() {
        System.setOut(new PrintStream(outputCapture));
    }

    @AfterEach
    public void reset() throws IOException {
        outputCapture.reset();

        FileUtils.deleteFileIfExists(filePath);
        FileUtils.deleteFileIfExists(otherFilePath);
        Files.write(filePath, TEXT.getBytes());
        Files.write(otherFilePath, TEXT.getBytes());

        FileUtils.deleteFolder(folderPath);
        FileUtils.deleteFolder(otherFolderPath);
        Files.createDirectories(folderPath);
        Files.createDirectories(otherFolderPath);

        if (!Environment.currentDirectory.equals(ENV_DIR_REF)) {
            Environment.resetCurrentDirectory();
        }
    }

    @Test
    public void catQuoting_backQuoteDoubleQuoteNesting_printFileContent() throws IOException, ShellException,
            AbstractApplicationException {
        // cat `echo "FILE_PATH"`
        String[] args = new String[]{CAT_KEYWORD, String.format(FORMAT_BACKDOUBLE, ECHO_KEYWORD, filePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);
        assertEquals(TEXT + LINE_BREAK, outputCapture.toString());
    }

    @Test
    public void catQuoting_noQuoteBackQuoteInvalidArg_throwException() {
        // cat FILE_PATH `echo INVALID_FILE_PATH`
        Environment.setCurrentDirectory(testPath.toString());
        String[] args = new String[]{CAT_KEYWORD, filePath.toString(), String.format(FORMAT_BACKQUOTE, ECHO_KEYWORD,
                invalidFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(CatException.class, () -> callCommand.evaluate(System.in, outputCapture));
    }

    @Test
    public void catQuoting_doubleQuoteSingleQuoteInvalidArg_throwException() {
        // cat "FILE_PATH" 'INVALID_FILE_PATH'
        Environment.setCurrentDirectory(testPath.toString());
        String[] args = new String[]{CAT_KEYWORD, String.format(FORMAT_DOUBLE, filePath.toString()), String.format(FORMAT_SINGLE,
                invalidFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(CatException.class, () -> callCommand.evaluate(System.in, outputCapture));
    }

    @Test
    public void catQuoting_singleQuoteDoubleQuoteNesting_throwException() {
        // cat '"FILE_PATH"'
        String[] args = new String[]{CAT_KEYWORD, String.format(FORMAT_SINGDOUBLE, filePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(CatException.class, () -> callCommand.evaluate(System.in, outputCapture));
    }

    @Test
    public void cdQuoting_singleQuoteBackQuoteNesting_throwException() {
        // cd '`echo FOLDER_PATH`'
        String[] args = new String[]{CD_KEYWORD, String.format("'`%s %s`'", ECHO_KEYWORD, folderPath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(CdException.class, () -> callCommand.evaluate(System.in, outputCapture));
    }

    @Test
    public void cdQuoting_backQuoteSingleQuoteNesting_changeDirectory() throws IOException, ShellException,
            AbstractApplicationException {
        // cd `echo 'FOLDER_PATH'`
        String[] args = new String[]{CD_KEYWORD, String.format(FORMAT_BACKSINGLE, ECHO_KEYWORD, folderPath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);
        assertEquals(folderPath.toString(), Environment.currentDirectory);
    }

    @Test
    public void cdQuoting_noQuoteDoubleQuoteInvalidArg_throwException() {
        // cd INVALID_FOLDER_PATH "OTHER_FOLDER_PATH"
        String[] args = new String[]{CD_KEYWORD, invalidFolderPath.toString(), String.format(FORMAT_DOUBLE,
                otherFolderPath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(CdException.class, () -> callCommand.evaluate(System.in, outputCapture));
    }

    @Test
    public void cdQuoting_doubleQuoteNoQuote_XXX() {
        // cd "FOLDER_PATH" OTHER_FOLDER_PATH
        String[] args = new String[]{CD_KEYWORD, String.format(FORMAT_DOUBLE, folderPath.toString()),
                otherFolderPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(CdException.class, () -> callCommand.evaluate(System.in, outputCapture));
    }

    @Test
    public void cpQuoting_backQuoteDoubleQuoteNesting_fileCopyOver() throws IOException, ShellException,
            AbstractApplicationException {
        // cp `echo "FILE_PATH"` FOLDER_PATH
        String[] args = new String[]{CP_KEYWORD, String.format(FORMAT_BACKDOUBLE, ECHO_KEYWORD, filePath.toString()),
                folderPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        Path destFilePath = folderPath.resolve(FILE);
        assertEquals(FileUtils.getFileContent(destFilePath), TEXT);
    }

    @Test
    public void cpQuoting_noQuoteSingleQuoteInvalidArg_throwError() {
        // cp -r FOLDER_PATH 'INVALID_FOLDER_PATH'
        String[] args = new String[]{CP_KEYWORD, CP_FLAG, folderPath.toString(), String.format(FORMAT_SINGLE,
                invalidFolderPath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(CpException.class, () -> callCommand.evaluate(System.in, outputCapture));
    }

    @Test
    public void cpQuoting_singleQuoteDoubleQuoteNesting_throwError() {
        // cp '"FILE_PATH"' FOLDER_PATH
        String[] args = new String[]{CP_KEYWORD, String.format(FORMAT_SINGDOUBLE, filePath.toString()), folderPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(CpException.class, () -> callCommand.evaluate(System.in, outputCapture));
    }

    @Test
    public void cpQuoting_doubleQuoteBackQuote_fileCopyOver() throws IOException, ShellException,
            AbstractApplicationException {
        // cp "FILE_PATH" `echo FOLDER_PATH`
        String[] args = new String[]{CP_KEYWORD, String.format(FORMAT_DOUBLE, filePath.toString()),
                String.format(FORMAT_BACKQUOTE, ECHO_KEYWORD, folderPath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        Path destFilePath = folderPath.resolve(FILE);
        assertEquals(FileUtils.getFileContent(destFilePath), TEXT);
    }

    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    public void cutQuoting_singleQuoteDoubleQuoteNesting_printOnDemandError() throws ShellException,
            AbstractApplicationException, FileNotFoundException {
        // cut -b 1 '"FILE_PATH"'
        String[] args = new String[]{CUT_KEYWORD, CUT_FLAG, CUT_POS, String.format(FORMAT_SINGDOUBLE, filePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        String expected = String.format("%s: \"%s\": %s", CUT_KEYWORD, filePath.toString(), ERR_INVALID_PATH) + LINE_BREAK;
        assertEquals(expected, outputCapture.toString());
    }

    @Test
    public void cutQuoting_doubleQuoteSingleQuote_printFirstCharsOfBothFiles() throws IOException, ShellException,
            AbstractApplicationException {
        // cut -b 1 "FILE_PATH" 'OTHER_FILE_PATH'
        String[] args = new String[]{CUT_KEYWORD, CUT_FLAG, CUT_POS, String.format(FORMAT_DOUBLE, filePath.toString()),
                String.format(FORMAT_SINGLE, otherFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        String target = WORD_1.charAt(0) + LINE_BREAK + WORD_2.charAt(0) + LINE_BREAK + WORD_3.charAt(0) + LINE_BREAK +
                WORD_1.charAt(0) + LINE_BREAK + WORD_2.charAt(0);
        assertEquals(target + LINE_BREAK + target + LINE_BREAK, outputCapture.toString());
    }

    @Test
    public void cutQuoting_backQuoteDoubleQuoteNesting_printFirstCharsOfFile() throws IOException, ShellException,
            AbstractApplicationException {
        // cut -b 1 `echo "FILE_PATH"`
        String[] args = new String[]{CUT_KEYWORD, CUT_FLAG, CUT_POS, String.format(FORMAT_BACKDOUBLE, ECHO_KEYWORD,
                filePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        String target = WORD_1.charAt(0) + LINE_BREAK + WORD_2.charAt(0) + LINE_BREAK + WORD_3.charAt(0) + LINE_BREAK +
                WORD_1.charAt(0) + LINE_BREAK + WORD_2.charAt(0);
        assertEquals(target + LINE_BREAK, outputCapture.toString());
    }

    @Test
    public void cutQuoting_noQuoteBackQuoteInvalidArg_printFirstCharsOfFirstFile() throws IOException, ShellException,
            AbstractApplicationException {
        // cut -b 1 FILE_PATH `echo INVALID_FILE_PATH`
        String[] args = new String[]{CUT_KEYWORD, CUT_FLAG, CUT_POS, filePath.toString(), String.format(FORMAT_BACKQUOTE,
                ECHO_KEYWORD, invalidFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        String target = WORD_1.charAt(0) + LINE_BREAK +
                WORD_2.charAt(0) + LINE_BREAK +
                WORD_3.charAt(0) + LINE_BREAK +
                WORD_1.charAt(0) + LINE_BREAK +
                WORD_2.charAt(0) + LINE_BREAK +
                String.format("%s: %s: %s", CUT_KEYWORD, invalidFilePath.toString(), ERR_FILE_NOT_FOUND);
        assertEquals(target + LINE_BREAK, outputCapture.toString());
    }

    @Test
    public void grepQuoting_backQuoteSingleQuoteNesting_printResults() throws IOException, ShellException,
            AbstractApplicationException {
        // grep PATTERN `echo 'FILE_PATH'`
        Environment.setCurrentDirectory(testPath.toString());
        String[] args = new String[]{GREP_KEYWORD, GREP_PATTERN, String.format(FORMAT_BACKSINGLE, ECHO_KEYWORD, FILE)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        String target = WORD_1 + LINE_BREAK + WORD_1;
        assertEquals(target + LINE_BREAK, outputCapture.toString());
    }

    @Test
    public void grepQuoting_singleQuoteBackQuote_printResultsFromTwoFiles() throws IOException, ShellException,
            AbstractApplicationException {
        // grep PATTERN 'FILE_PATH' `echo OTHER_FILE_PATH`
        Environment.setCurrentDirectory(testPath.toString());
        String[] args = new String[]{GREP_KEYWORD, GREP_PATTERN, String.format(FORMAT_SINGLE, FILE),
                String.format(FORMAT_BACKQUOTE, ECHO_KEYWORD, OTHER_FILE)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        String target = String.format(FORMAT_GREP, FILE, WORD_1) + LINE_BREAK +
                String.format(FORMAT_GREP, FILE, WORD_1) + LINE_BREAK +
                String.format(FORMAT_GREP, OTHER_FILE, WORD_1) + LINE_BREAK +
                String.format(FORMAT_GREP, OTHER_FILE, WORD_1);
        assertEquals(target + LINE_BREAK, outputCapture.toString());
    }

    @Test
    public void grepQuoting_noQuoteDoubleQuoteInvalidArgs_throwException() {
        // grep PATTERN FILE_PATH `INVALID_FILE_PATH`
        Environment.setCurrentDirectory(testPath.toString());
        String[] args = new String[]{GREP_KEYWORD, GREP_PATTERN, FILE, String.format("`%s`", INVALID_FILE)};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, outputCapture));
    }

    @Test
    public void grepQuoting_doubleQuoteNoQuote_printResultsFromTwoFiles() throws IOException, ShellException,
            AbstractApplicationException {
        // grep PATTERN "FILE_PATH" OTHER FILE_PATH
        Environment.setCurrentDirectory(testPath.toString());
        String[] args = new String[]{GREP_KEYWORD, GREP_PATTERN, String.format(FORMAT_DOUBLE, FILE), OTHER_FILE};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        String target = String.format(FORMAT_GREP, FILE, WORD_1) + LINE_BREAK +
                String.format(FORMAT_GREP, FILE, WORD_1) + LINE_BREAK +
                String.format(FORMAT_GREP, OTHER_FILE, WORD_1) + LINE_BREAK +
                String.format(FORMAT_GREP, OTHER_FILE, WORD_1);
        assertEquals(target + LINE_BREAK, outputCapture.toString());
    }

    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    public void lsQuoting_singleQuoteDoubleQuoteNesting_throwException() {
        // ls '"FOLDER_PATH"'
        String[] args = new String[]{LS_KEYWORD, String.format("\'\"%s\"\'", folderPath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(LsException.class, () -> callCommand.evaluate(System.in, outputCapture));
    }

    @Test
    public void lsQuoting_doubleQuoteBackQuote_printOutputForBothPaths() throws IOException, ShellException,
            AbstractApplicationException {
        // ls "FOLDER_PATH" `echo OTHER_FOLDER_PATH`
        Environment.setCurrentDirectory(testPath.toString());
        FileUtils.createNewFile(folderPath.resolve(FILE));
        FileUtils.overwriteFileContent(folderPath.resolve(FILE), TEXT);

        String[] args = new String[]{LS_KEYWORD, String.format(FORMAT_DOUBLE, folderPath.toString()),
                String.format("`echo %s`", otherFolderPath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        String target = FOLDER + ":" + LINE_BREAK + FILE + LINE_BREAK + LINE_BREAK + OTHER_FOLDER + ":" + LINE_BREAK;
        assertEquals(target, outputCapture.toString());
    }

    @Test
    public void lsQuoting_noQuoteSingleQuoteInvalidArg_printOutputForFolder() throws IOException, ShellException,
            AbstractApplicationException {
        // ls FOLDER_PATH 'INVALID_FOLDER_PATH'
        Environment.setCurrentDirectory(testPath.toString());
        String[] args = new String[]{LS_KEYWORD, folderPath.toString(), String.format(FORMAT_SINGLE,
                invalidFolderPath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        //ls: cannot access 'DONT_EXIST': No such file or directory
        String expected = String.format("%s: cannot access '%s': %s", LS_KEYWORD, INVALID_FOLDER, ERR_FILE_NOT_FOUND) +
                LINE_BREAK + FOLDER + ":" + LINE_BREAK;
        assertEquals(expected, outputCapture.toString());
    }

    @Test
    public void lsQuoting_backQuoteNoQuote_printOutputForBothFolders() throws IOException, ShellException,
            AbstractApplicationException {
        // ls `echo FOLDER_PATH` OTHER_FOLDER_PATH
        Environment.setCurrentDirectory(testPath.toString());
        FileUtils.createNewFile(folderPath.resolve(FILE));
        FileUtils.overwriteFileContent(folderPath.resolve(FILE), TEXT);

        String[] args = new String[]{LS_KEYWORD, String.format(FORMAT_BACKQUOTE, ECHO_KEYWORD, folderPath.toString()),
                otherFolderPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        String target = FOLDER + ":" + LINE_BREAK + FILE + LINE_BREAK + LINE_BREAK + OTHER_FOLDER + ":" + LINE_BREAK;
        assertEquals(target, outputCapture.toString());
    }

    @Test
    public void mvQuoting_noQuoteSingleQuoteInvalidArg_throwException() throws IOException, ShellException,
            AbstractApplicationException {
        // mv INVALID_FILE_PATH 'FOLDER_PATH'
        String[] args = new String[]{MV_KEYWORD, invalidFilePath.toString(),
                String.format(FORMAT_SINGLE, folderPath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);
        String expected = String.format("%s: %s", MV_KEYWORD, ERR_FILE_NOT_FOUND) + LINE_BREAK;
        assertEquals(expected, outputCapture.toString());
    }

    @Test
    public void mvQuoting_doubleQuoteSingleQuoteInvalidArg_throwException() throws IOException, ShellException,
            AbstractApplicationException {
        // mv "INVALID_FILE_PATH" 'FOLDER_PATH'
        String[] args = new String[]{MV_KEYWORD, String.format(FORMAT_DOUBLE, invalidFilePath.toString()),
                String.format(FORMAT_SINGLE, folderPath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);
        String expected = String.format("%s: %s", MV_KEYWORD, ERR_FILE_NOT_FOUND) + LINE_BREAK;
        assertEquals(expected, outputCapture.toString());
    }

    @Test
    public void mvQuoting_backQuoteDoubleQuoteNesting_fileMoveToFolder() throws IOException, ShellException,
            AbstractApplicationException {
        // mv `echo "FILE_PATH"` FOLDER_PATH
        String[] args = new String[]{MV_KEYWORD, String.format(FORMAT_BACKDOUBLE, ECHO_KEYWORD, filePath.toString()),
                folderPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        Path destFilePath = folderPath.resolve(FILE);
        assertTrue(Files.exists(destFilePath));
        assertEquals(FileUtils.getFileContent(destFilePath), TEXT);
    }

    @Test
    public void mvQuoting_noQuoteBackQuote_fileMoveToFolder() throws IOException, ShellException,
            AbstractApplicationException {
        // mv FILE_PATH `echo FOLDER_PATH`
        String[] args = new String[]{MV_KEYWORD, filePath.toString(), String.format(FORMAT_BACKQUOTE, ECHO_KEYWORD,
                folderPath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        Path destFilePath = folderPath.resolve(FILE);
        assertTrue(Files.exists(destFilePath));
        assertEquals(FileUtils.getFileContent(destFilePath), TEXT);
    }

    @Test
    public void pasteQuoting_singleQuoteDoubleQuoteNesting_throwException() {
        // paste '"FILE_PATH"'
        String[] args = new String[]{PASTE_KEYWORD, String.format(FORMAT_SINGDOUBLE, filePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, outputCapture));
    }

    @Test
    public void pasteQuoting_noQuoteBackQuoteInvalidArg_throwException() {
        // paste FILE_PATH `echo INVALID_FILE_PATH`
        String[] args = new String[]{PASTE_KEYWORD, filePath.toString(), String.format(FORMAT_BACKQUOTE, ECHO_KEYWORD,
                invalidFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, outputCapture));
    }

    @Test
    public void pasteQuoting_doubleQuoteSingleQuoteInvalidArg_throwException() {
        // paste "INVALID_FILE_PATH" 'FILE_PATH'
        String[] args = new String[]{PASTE_KEYWORD, String.format(FORMAT_DOUBLE, invalidFilePath.toString()),
                String.format(FORMAT_SINGLE, filePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, outputCapture));
    }

    @Test
    public void pasteQuoting_doubleQuoteBackQuote_printTwoFilesLines() throws IOException, ShellException,
            AbstractApplicationException {
        // paste "FILE_PATH" `echo OTHER_FILE_PATH`
        String[] args = new String[]{PASTE_KEYWORD, String.format(FORMAT_DOUBLE, filePath.toString()),
                String.format(FORMAT_BACKQUOTE, ECHO_KEYWORD, otherFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        String target = String.format(FORMAT_PASTE, WORD_1, WORD_1) + LINE_BREAK +
                String.format(FORMAT_PASTE, WORD_2, WORD_2) + LINE_BREAK +
                String.format(FORMAT_PASTE, WORD_3, WORD_3) + LINE_BREAK +
                String.format(FORMAT_PASTE, WORD_1, WORD_1) + LINE_BREAK +
                String.format(FORMAT_PASTE, WORD_2, WORD_2);
        assertEquals(target + LINE_BREAK, outputCapture.toString());
    }

    @Test
    public void rmQuoting_singleQuoteDoubleQuoteNesting_throwException() {
        // rm '"FILE_PATH"'
        String[] args = new String[]{RM_KEYWORD, String.format(FORMAT_SINGDOUBLE, filePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(RmException.class, () -> callCommand.evaluate(System.in, outputCapture));
    }

    @Test
    public void rmQuoting_noQuoteBackQuoteInvalidArg_firstFileDeleteSecondFileError() {
        // rm FILE_PATH `echo INVALID_FILE_PATH`
        String[] args = new String[]{RM_KEYWORD, filePath.toString(), String.format(FORMAT_BACKQUOTE, ECHO_KEYWORD,
                invalidFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(RmException.class, () -> callCommand.evaluate(System.in, outputCapture));
        assertFalse(Files.exists(filePath));
    }

    @Test
    public void rmQuoting_doubleQuoteSingleQuoteInvalidArg_firstFileDeleteSecondFileError() {
        // rm "FILE_PATH" 'INVALID_FILE_PATH'
        String[] args = new String[]{RM_KEYWORD, String.format(FORMAT_DOUBLE, filePath.toString()), String.format(FORMAT_SINGLE,
                invalidFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(RmException.class, () -> callCommand.evaluate(System.in, outputCapture));
        assertFalse(Files.exists(filePath));
    }

    @Test
    public void rmQuoting_backQuoteDoubleQuoteNesting_throwException() throws IOException, ShellException,
            AbstractApplicationException {
        // rm `echo "FILE_PATH"`
        String[] args = new String[]{RM_KEYWORD, String.format(FORMAT_BACKDOUBLE, ECHO_KEYWORD, filePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);
        assertFalse(Files.exists(filePath));
    }

    @Test
    public void sortQuoting_backQuoteSingleQuoteNesting_sortedOutput() throws IOException, ShellException,
            AbstractApplicationException {
        // sort `echo 'FILE_PATH'`
        String[] args = new String[]{SORT_KEYWORD, String.format(FORMAT_BACKSINGLE, ECHO_KEYWORD, filePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);
        assertEquals(SORTED_TEXT + LINE_BREAK, outputCapture.toString());
    }

    @Test
    public void sortQuoting_singleQuoteBackQuote_printCombinedSortedEntries() throws IOException, ShellException,
            AbstractApplicationException {
        // sort 'FILE_PATH' `echo OTHER_FILE_PATH`
        String[] args = new String[]{SORT_KEYWORD, String.format(FORMAT_SINGLE, filePath.toString()),
                String.format(FORMAT_BACKQUOTE, ECHO_KEYWORD, otherFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);
        assertEquals(SORTED_FUSED_TEXT + LINE_BREAK, outputCapture.toString());
    }

    @Test
    public void sortQuoting_doubleQuoteNoQuoteInvalidArg_throwException() {
        // sort "INVALID_FILE_PATH" FILE_PATH
        String[] args = new String[]{SORT_KEYWORD, String.format(FORMAT_DOUBLE, invalidFilePath.toString()),
                filePath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(SortException.class, () -> callCommand.evaluate(System.in, outputCapture));
    }

    @Test
    public void sortQuoting_noQuoteDoubleQuote_printCombinedSortedEntries() throws IOException, ShellException,
            AbstractApplicationException {
        // sort FILE_PATH "OTHER_FILE_PATH"
        String[] args = new String[]{SORT_KEYWORD, filePath.toString(),
                String.format(FORMAT_DOUBLE, otherFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);
        assertEquals(SORTED_FUSED_TEXT + LINE_BREAK, outputCapture.toString());
    }

    @Test
    public void teeQuoting_singleQuoteBackQuote_inputWrittenToTerminalAndFiles() throws IOException, ShellException,
            AbstractApplicationException {
        // tee 'FILE_PATH' `echo OTHER_FILE_PATH`
        inputCapture = new ByteArrayInputStream(TEE_INPUT.getBytes());

        String[] args = new String[]{TEE_KEYWORD, String.format(FORMAT_SINGLE, filePath.toString()),
                String.format(FORMAT_BACKQUOTE, ECHO_KEYWORD, otherFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(inputCapture, outputCapture);
        assertEquals(TEE_INPUT + LINE_BREAK, outputCapture.toString());
        assertEquals(TEE_INPUT, FileUtils.getFileContent(filePath));
        assertEquals(TEE_INPUT, FileUtils.getFileContent(otherFilePath));
    }

    @Test
    public void teeQuoting_backQuoteSingleQuoteNesting_inputWrittenToTerminalAndFile() throws IOException,
            ShellException, AbstractApplicationException {
        // tee `echo 'FILE_PATH'`
        inputCapture = new ByteArrayInputStream(TEE_INPUT.getBytes());

        String[] args = new String[]{TEE_KEYWORD, String.format(FORMAT_BACKSINGLE, ECHO_KEYWORD, filePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(inputCapture, outputCapture);
        assertEquals(TEE_INPUT + LINE_BREAK, outputCapture.toString());
        assertEquals(TEE_INPUT, FileUtils.getFileContent(filePath));
    }

    @Test
    public void teeQuoting_noQuoteDoubleQuoteInvalidArg_throwException() {
        // tee FILE_PATH "FOLDER_PATH"
        inputCapture = new ByteArrayInputStream(TEE_INPUT.getBytes());

        String[] args = new String[]{TEE_KEYWORD, filePath.toString(), String.format(FORMAT_DOUBLE, folderPath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(TeeException.class, () -> callCommand.evaluate(inputCapture, outputCapture));
    }

    @Test
    public void teeQuoting_doubleQuoteNoQuote_throwException() throws IOException, ShellException,
            AbstractApplicationException {
        // tee "FILE_PATH" OTHER_FILE_PATH
        inputCapture = new ByteArrayInputStream(TEE_INPUT.getBytes());

        String[] args = new String[]{TEE_KEYWORD, String.format(FORMAT_DOUBLE, filePath.toString()), otherFilePath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(inputCapture, outputCapture);
        assertEquals(TEE_INPUT + LINE_BREAK, outputCapture.toString());
        assertEquals(TEE_INPUT, FileUtils.getFileContent(filePath));
        assertEquals(TEE_INPUT, FileUtils.getFileContent(otherFilePath));
    }

    @Test
    public void uniqQuoting_singleQuoteBackQuote_writeUniquesToSecondFile() throws IOException, ShellException,
            AbstractApplicationException {
        // uniq 'FILE_PATH' `echo OTHER_FILE_PATH`
        FileUtils.overwriteFileContent(otherFilePath, SORTED_TEXT);

        String[] args = new String[]{UNIQ_KEYWORD, String.format(FORMAT_SINGLE, filePath.toString()),
                String.format(FORMAT_BACKQUOTE, ECHO_KEYWORD, otherFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);
        assertEquals(TEXT, FileUtils.getFileContent(otherFilePath));
    }

    @Test
    public void uniqQuoting_backQuoteSingleQuoteNesting_writeUniquesToTerminal() throws IOException, ShellException,
            AbstractApplicationException {
        // uniq `echo 'FILE_PATH'`
        String[] args = new String[]{UNIQ_KEYWORD, String.format(FORMAT_BACKSINGLE, ECHO_KEYWORD, filePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);
        assertEquals(TEXT + LINE_BREAK, outputCapture.toString());
    }

    @Test
    public void uniqQuoting_noQuoteDoubleQuoteInvalidArg_throwException() {
        // uniq INVALID_FILE_PATH "FILE_PATH"
        String[] args = new String[]{UNIQ_KEYWORD, invalidFilePath.toString(), String.format(FORMAT_DOUBLE,
                filePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, outputCapture));
    }

    @Test
    public void uniqQuoting_doubleQuoteNoQuote_writeUniquesToSecondFile() throws IOException, ShellException,
            AbstractApplicationException {
        // uniq "FILE_PATH" OTHER_FILE_PATH
        FileUtils.overwriteFileContent(otherFilePath, SORTED_TEXT);

        String[] args = new String[]{UNIQ_KEYWORD, String.format(FORMAT_DOUBLE, filePath.toString()),
                otherFilePath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);
        assertEquals(TEXT, FileUtils.getFileContent(otherFilePath));
    }

    @Test
    public void wcQuoting_backQuoteSingleQuoteNesting_printCorrectInfo() throws IOException, ShellException,
            AbstractApplicationException {
        // wc `echo 'FILE_PATH'`
        FileUtils.overwriteFileContent(filePath, TEXT);

        String[] args = new String[]{WC_KEYWORD, String.format(FORMAT_BACKSINGLE, ECHO_KEYWORD, filePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        String line = String.format(FORMAT_WC, TEXT_LINE_COUNT, TEXT_WORD_COUNT, TEXT_BYTE_COUNT, filePath.toString());
        assertEquals(line + LINE_BREAK, outputCapture.toString());
    }

    @Test
    public void wcQuoting_singleQuoteBackQuote_printCorrectInfo() throws IOException, ShellException,
            AbstractApplicationException {
        // wc 'FILE_PATH' `echo OTHER_FILE_PATH`
        FileUtils.overwriteFileContent(filePath, TEXT);
        FileUtils.overwriteFileContent(otherFilePath, TEXT);

        String[] args = new String[]{WC_KEYWORD, String.format(FORMAT_SINGLE, filePath.toString()),
                String.format(FORMAT_BACKQUOTE, ECHO_KEYWORD, otherFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        String line1 = String.format(FORMAT_WC, TEXT_LINE_COUNT, TEXT_WORD_COUNT, TEXT_BYTE_COUNT, filePath.toString());
        String line2 = String.format(FORMAT_WC, TEXT_LINE_COUNT, TEXT_WORD_COUNT, TEXT_BYTE_COUNT, otherFilePath.toString());
        String total = String.format(FORMAT_WC, TEXT_LINE_COUNT * 2, TEXT_WORD_COUNT * 2, TEXT_BYTE_COUNT * 2, "total");
        assertEquals(line1 + LINE_BREAK + line2 + LINE_BREAK + total + LINE_BREAK, outputCapture.toString());
    }

    @Test
    public void wcQuoting_doubleQuoteNoQuoteInvalidArg_printInfoForValidFile() throws IOException, ShellException,
            AbstractApplicationException {
        // wc "INVALID_FILE_PATH" FILE_PATH
        FileUtils.overwriteFileContent(filePath, TEXT);

        String[] args = new String[]{WC_KEYWORD, String.format(FORMAT_DOUBLE, invalidFilePath.toString()), filePath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        // wc: C:\Users\tomas\AppData\Local\Temp\junit9796740785703352526\DONT_EXIST.txt: No such file or directory
        String error = String.format("%s: %s: %s", WC_KEYWORD, invalidFilePath.toString(), ERR_FILE_NOT_FOUND);
        String line = String.format(FORMAT_WC, TEXT_LINE_COUNT, TEXT_WORD_COUNT, TEXT_BYTE_COUNT, filePath.toString());
        String total = String.format(FORMAT_WC, TEXT_LINE_COUNT, TEXT_WORD_COUNT, TEXT_BYTE_COUNT, "total");
        assertEquals(error + LINE_BREAK + line + LINE_BREAK + total + LINE_BREAK, outputCapture.toString());
    }

    @Test
    public void wcQuoting_noQuoteDoubleQuote_printInfoForBothFiles() throws IOException, ShellException,
            AbstractApplicationException {
        // wc FILE_PATH "OTHER_FILE_PATH"
        FileUtils.overwriteFileContent(filePath, TEXT);
        FileUtils.overwriteFileContent(otherFilePath, TEXT);

        String[] args = new String[]{WC_KEYWORD, filePath.toString(), String.format(FORMAT_DOUBLE, otherFilePath.toString())};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, outputCapture);

        String line1 = String.format(FORMAT_WC, TEXT_LINE_COUNT, TEXT_WORD_COUNT, TEXT_BYTE_COUNT, filePath.toString());
        String line2 = String.format(FORMAT_WC, TEXT_LINE_COUNT, TEXT_WORD_COUNT, TEXT_BYTE_COUNT, otherFilePath.toString());
        String total = String.format(FORMAT_WC, TEXT_LINE_COUNT * 2, TEXT_WORD_COUNT * 2, TEXT_BYTE_COUNT * 2, "total");
        assertEquals(line1 + LINE_BREAK + line2 + LINE_BREAK + total + LINE_BREAK, outputCapture.toString());
    }
}
