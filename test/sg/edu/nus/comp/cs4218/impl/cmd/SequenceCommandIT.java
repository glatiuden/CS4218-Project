package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_APP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.*;

public class SequenceCommandIT {
    final static String RIGHT_ANSWER = "A B *C";
    final static List<String> VALID_ECHO = List.of("echo", RIGHT_ANSWER);
    final static List<String> VALID_LS = List.of("ls", "-X");
    final static List<String> VALID_CD = List.of("cd", "deep-folder");
    final static List<String> VALID_WC = List.of("wc", "-");
    final static List<String> VALID_EXIT = List.of("exit");
    final static List<String> CD_GO_BACK = List.of("cd", "..");
    final static List<String> INVALID_ARG = List.of(";ech;");
    final static String RANDOM_STRING = "This is a random thing that is \n use for the wc command.";
    static final String FILENAME = "test.txt";
    static final String FILENAME2 = "test2.txt";
    static final String FILENAME3 = "test3.a";
    static final String FOLDER_NAME = "deep-folder";
    static final String SEC_FOLDER = "second-folder";
    // Test Files
    static File rootTestFolder = new File("test-seq");
    static File[] dirs = new File[]{
            new File(Paths.get(rootTestFolder.toString(), FOLDER_NAME).toUri()),
            new File(Paths.get(rootTestFolder.toString(), FOLDER_NAME, SEC_FOLDER).toUri()),
    };
    static File[] files = new File[]{
            new File("test-seq/test.txt"),
            new File(Paths.get(dirs[0].getPath(), FILENAME2).toUri()),
            new File(Paths.get(dirs[0].getPath(), FILENAME3).toUri()),
    };
    List<Command> commands;
    ArgumentResolver argumentResolver = new ArgumentResolver();
    ApplicationRunner appRunner = new ApplicationRunner();
    ByteArrayOutputStream stdOutResult;


    /* TEST FOLDER STRUCTURE:
        test-seq/
        ├─ deep-folder/
        │  ├─ second-folder/
        │  ├─ test2.txt
        │  ├─ test3.a
        ├─ test.txt
    */
    InputStream emptyInputStream = new ByteArrayInputStream("".getBytes());
    CallCommand echoCmd = new CallCommand(VALID_ECHO, appRunner, argumentResolver);
    CallCommand lsCmd = new CallCommand(VALID_LS, appRunner, argumentResolver);
    CallCommand cdCmd = new CallCommand(VALID_CD, appRunner, argumentResolver);
    CallCommand cdGoBackCmd = new CallCommand(CD_GO_BACK, appRunner, argumentResolver);
    CallCommand exitCmd = new CallCommand(VALID_EXIT, appRunner, argumentResolver);
    CallCommand wcCmd = new CallCommand(VALID_WC, appRunner, argumentResolver);
    CallCommand invalidCallCmd = new CallCommand(INVALID_ARG, appRunner, argumentResolver);

    @BeforeAll
    static void setupAll() throws Exception {
        createAllFileNFolder(rootTestFolder, dirs, files);
        Environment.currentDirectory = rootTestFolder.getAbsolutePath();
        writeToFile(files[0].toString(), RANDOM_STRING);
    }

    @AfterAll
    static void tearDown() {
        deleteAll(rootTestFolder);
        Environment.resetCurrentDirectory();
    }

    @BeforeEach
    void setup() {
        commands = new LinkedList<>();
        stdOutResult = new ByteArrayOutputStream();
    }

    @Test
    public void evaluate_1Command_ReturnsTrue() throws FileNotFoundException, AbstractApplicationException, ShellException {
        commands.add(echoCmd);
        SequenceCommand seqCommand = new SequenceCommand(commands);
        seqCommand.evaluate(emptyInputStream, stdOutResult);
        assertEquals(RIGHT_ANSWER + STRING_NEWLINE, stdOutResult.toString());
    }

    // ECHO + CD + LS + CD
    @Test
    public void evaluate_MultiCommand_ReturnsTrue() throws FileNotFoundException, AbstractApplicationException, ShellException {
        commands.add(echoCmd);
        commands.add(cdCmd);
        commands.add(lsCmd);
        commands.add(cdGoBackCmd);
        SequenceCommand seqCommand = new SequenceCommand(commands);
        seqCommand.evaluate(emptyInputStream, stdOutResult);
        String expectAns = RIGHT_ANSWER + STRING_NEWLINE +
                SEC_FOLDER + STRING_NEWLINE +
                FILENAME3 + STRING_NEWLINE +
                FILENAME2 + STRING_NEWLINE;
        assertEquals(expectAns, stdOutResult.toString());
    }

    // Invalid command, at the center, but the rest of the commands still runs
    @Test
    public void evaluate_HasInvalidCommand_ReturnsTrue() throws FileNotFoundException, AbstractApplicationException, ShellException {
        commands.add(echoCmd);
        commands.add(invalidCallCmd);
        commands.add(cdCmd);
        commands.add(lsCmd);
        commands.add(cdGoBackCmd);
        SequenceCommand seqCommand = new SequenceCommand(commands);
        seqCommand.evaluate(emptyInputStream, stdOutResult);
        String expectAns = RIGHT_ANSWER + STRING_NEWLINE +
                "shell: ;ech;: " + ERR_INVALID_APP + STRING_NEWLINE +
                SEC_FOLDER + STRING_NEWLINE +
                FILENAME3 + STRING_NEWLINE +
                FILENAME2 + STRING_NEWLINE;
        assertEquals(expectAns, stdOutResult.toString());
    }

    // ECHO + CD + WC (Takes in standard input result) + LS
    @Test
    public void evaluate_RequiresStdIn_ReturnsTrue() throws FileNotFoundException, AbstractApplicationException, ShellException {
        commands.add(echoCmd);
        commands.add(cdCmd);
        commands.add(wcCmd);
        commands.add(lsCmd);
        commands.add(cdGoBackCmd);
        ByteArrayInputStream inputStreamStdIn = new ByteArrayInputStream(RANDOM_STRING.getBytes());
        SequenceCommand seqCommand = new SequenceCommand(commands);
        seqCommand.evaluate(inputStreamStdIn, stdOutResult);
        String expectAns = RIGHT_ANSWER + STRING_NEWLINE +
                expectedFileLine(56, 1, 12, "-") + STRING_NEWLINE +
                SEC_FOLDER + STRING_NEWLINE +
                FILENAME3 + STRING_NEWLINE +
                FILENAME2 + STRING_NEWLINE;
        assertEquals(expectAns, stdOutResult.toString());
    }

    // Even with exit command, the other commands should still run finish before the application exits (Also help to test absolute path handling)
    @Test
    public void evaluate_ExitCommand_ExitsAtTheEnd() throws FileNotFoundException, AbstractApplicationException, ShellException {
        CallCommand absoluteWcCmd = new CallCommand(List.of("wc", files[0].getAbsolutePath()), appRunner, argumentResolver);
        commands.add(exitCmd);
        commands.add(cdCmd);
        commands.add(cdGoBackCmd);
        commands.add(absoluteWcCmd);
        commands.add(lsCmd);

        ByteArrayInputStream inputStreamStdIn = new ByteArrayInputStream("".getBytes());
        SequenceCommand seqCommand = new SequenceCommand(commands);
        assertThrows(ExitException.class, () -> {
            seqCommand.evaluate(inputStreamStdIn, stdOutResult);
        });
        String expectAns = expectedFileLine(56, 1, 12, files[0].getAbsolutePath()) + STRING_NEWLINE +
                FOLDER_NAME + STRING_NEWLINE +
                FILENAME + STRING_NEWLINE;
        assertEquals(expectAns, stdOutResult.toString());
    }

    // Ensure that although ls commands might also need std input, but because it doesn't have -, it doesn't
    // take in that value, but instead wc takes it
    @Test
    public void evaluate_UseStdInput_WcUseStdInSuccessfully() throws FileNotFoundException, AbstractApplicationException, ShellException {
        CallCommand absoluteWcCmd = new CallCommand(List.of("wc", files[0].getAbsolutePath()), appRunner, argumentResolver);
        commands.add(lsCmd);
        commands.add(absoluteWcCmd);

        ByteArrayInputStream inputStreamStdIn = new ByteArrayInputStream(RANDOM_STRING.getBytes());
        SequenceCommand seqCommand = new SequenceCommand(commands);
        seqCommand.evaluate(inputStreamStdIn, stdOutResult);
        String expectAns = FOLDER_NAME + STRING_NEWLINE +
                FILENAME + STRING_NEWLINE +
                expectedFileLine(56, 1, 12, files[0].getAbsolutePath()) + STRING_NEWLINE;
        assertEquals(expectAns, stdOutResult.toString());
    }
}
