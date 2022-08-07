package sg.edu.nus.comp.cs4218.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.SortException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_DIR_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_MULTIPLE_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.*;

public class IORedirectionIT {
    private final static String STDIN_DASH = "-";
    private final static String NORM_NEWLINE = "\n";
    private final static String NUMBER_FORMAT = "\t%7d";
    private final static String ECHO_CMD = "echo";
    private final static String LS_CMD = "ls";
    private final static String WC_CMD = "wc";
    private final static String CAT_CMD = "cat";
    private final static String GREP_CMD = "grep";
    private final static String CUT_CMD = "cut";
    private final static String SORT_CMD = "sort";
    private final static String CP_CMD = "cp";
    private final static String RM_CMD = "rm";
    private final static String TEE_CMD = "tee";
    private final static String EXIT_CMD = "exit";
    private final static String CD_CMD = "cd";
    private final static String PASTE_CMD = "paste";
    private final static String UNIQ_CMD = "uniq";
    private final static String MV_CMD = "mv";
    private final static String REDIR_INPUT = "<";
    private final static String REDIR_OUTPUT = ">";
    private final static String FILE_IN = "1.txt";
    private final static String FILE_OUT = "o.txt";
    private final static String NON_EXIST_FILE = "ne.txt";
    private final static String NEST_DIR = "nest";
    private final static String NON_EXIST_DIR = "nedir";
    private final static String NO_PERM_FILE = "np.txt";
    private final static String FILE_TEMP = "temp.txt";
    private final static String FILE_A = "a.txt";
    private final static String FILE_B = "b.txt";
    private final static String FILE_DUP = "dup.txt";
    private final static String MULTI_LINE_CONT = "\n54321\n\n  \n  spaced\na123!@#random\n!!!\nb\nAAA 123random\nBBB\n@@\n1123\n1\n55\n";
    private final static String OUTPUT_CONT = "output";
    private final static String ECHO_IN = "testing\necho";
    private final static String ECHO_OUT = "testing\necho".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String LS_OUT = "1.txt\na.txt\nb.txt\ndup.txt\nnest\nnp.txt\no.txt".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String WC_OUT = String.format(NUMBER_FORMAT, 14) + String.format(NUMBER_FORMAT, 12) + String.format(NUMBER_FORMAT, 71);
    private final static String TEST_CAT = "\n54321\n\n  \n  spaced\na123!@#random\n!!!\nb\nAAA 123random\nBBB\n@@\n1123\n1\n55".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String GREP_IN = "space";
    private final static String GREP_OUT = "  spaced";
    private final static String CUT_FLAG = "-c";
    private final static String CUT_POS = "2";
    private final static String CUT_OUT = "\n4\n\n \n \n1\n!\n\nA\nB\n@\n1\n\n5".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String SORT_OUT = "\n\n  \n  spaced\n!!!\n@@\n1\n1123\n54321\n55\nAAA 123random\nBBB\na123!@#random\nb".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String TEST_IN_STR = "test input string";
    private final static String A_CONT = "a\nb\nc\n";
    private final static String B_CONT = "1\n2\n3\n";
    private final static String PASTE_OUT = "a\t1\nb\t2\nc\t3".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String DUP_CONT = "dup\ndup\nno dup\n";
    private final static String DUP_OUT = "dup\nno dup".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String SHELL_EXCEP = "shell: ";
    private final static String SORT_EXCEP = "sort: ";
    private final static String EXIT_EXCEP = "exit: Process completed";
    @TempDir
    public static Path folderPath;
    private static Path fileInPath;
    private static Path fileOutPath;
    private static Path fileNePath;
    private static Path fileInDirPath;
    private static Path fileOutDirPath;
    private static Path fileNeDirPath;
    private static Path fileDirPath;
    private static Path fileTempPath;
    private static Path fileNoPermPath;
    private static Path fileAPath;
    private static Path fileBPath;
    private static Path fileDupPath;
    private static Path fileTempMvPath;
    private static ByteArrayOutputStream testOutputStream;
    private static CallCommand callCommand;
    private static ApplicationRunner applicationRunner;
    private static ArgumentResolver argumentResolver;

    @BeforeAll
    public static void setUp() throws IOException {
        testOutputStream = new ByteArrayOutputStream();
        applicationRunner = new ApplicationRunner();
        argumentResolver = new ArgumentResolver();

        fileInPath = folderPath.resolve(FILE_IN);
        fileOutPath = folderPath.resolve(FILE_OUT);
        fileNePath = folderPath.resolve(NON_EXIST_FILE);
        fileInDirPath = folderPath.resolve(NEST_DIR).resolve(FILE_IN);
        fileOutDirPath = folderPath.resolve(NEST_DIR).resolve(FILE_OUT);
        fileNeDirPath = folderPath.resolve(NON_EXIST_DIR).resolve(FILE_IN);
        fileDirPath = folderPath.resolve(NEST_DIR);
        fileTempPath = folderPath.resolve(FILE_TEMP);
        fileNoPermPath = folderPath.resolve(NO_PERM_FILE);
        fileAPath = folderPath.resolve(FILE_A);
        fileBPath = folderPath.resolve(FILE_B);
        fileDupPath = folderPath.resolve(FILE_DUP);
        fileTempMvPath = folderPath.resolve(NEST_DIR).resolve(FILE_TEMP);
        // ./nest
        Files.createDirectories(folderPath.resolve(NEST_DIR));
        // ./1.txt
        Files.write(fileInPath, MULTI_LINE_CONT.getBytes());
        // ./o.txt
        Files.write(fileOutPath, OUTPUT_CONT.getBytes());
        // ./a.txt
        Files.write(fileAPath, A_CONT.getBytes());
        // ./b.txt
        Files.write(fileBPath, B_CONT.getBytes());
        // ./dup.txt
        Files.write(fileDupPath, DUP_CONT.getBytes());
        // ./nest/1.txt
        Files.write(fileInDirPath, MULTI_LINE_CONT.getBytes());
        // ./nest/o.txt
        Files.write(fileOutDirPath, OUTPUT_CONT.getBytes());
        // ./np.txt no read/write permissions file
        Files.write(fileNoPermPath, "no permissions".getBytes());
        removeFilePermissions(fileNoPermPath);
    }

    @AfterAll
    public static void tearDown() {
        resetFilePermissions(fileNoPermPath);
    }

    @BeforeEach
    public void reset() throws IOException {
        Files.write(fileOutPath, OUTPUT_CONT.getBytes());
        Files.write(fileOutDirPath, OUTPUT_CONT.getBytes());
        testOutputStream.reset();
        deleteFileIfExists(fileTempPath);
        deleteFileIfExists(fileTempMvPath);
        Environment.currentDirectory = folderPath.toString();
    }

    // echo cmd, input: given, output: not given, echo do not take in input stream
    @Test
    public void echoIOIntegration_inputNoOutput_output() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String[] args = new String[]{ECHO_CMD, ECHO_IN, REDIR_INPUT, fileInPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(ECHO_IN + STRING_NEWLINE, testOutputStream.toString());
    }

    // echo cmd, input: not given, output: given, echo do not take in input stream
    @Test
    public void echoIOIntegration_noInputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{ECHO_CMD, ECHO_IN, REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(ECHO_OUT, actualFileOutput);
    }

    // echo cmd, input: given, output: given, echo do not take in input stream
    @Test
    public void echoIOIntegration_inputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{ECHO_CMD, ECHO_IN, REDIR_INPUT, fileInPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(ECHO_OUT, actualFileOutput);
    }

    // ls cmd, input: given, output: not given, ls do not take in input stream
    @Test
    public void lsIOIntegration_inputNoOutput_output() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String[] args = new String[]{LS_CMD, REDIR_INPUT, fileInPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(LS_OUT + STRING_NEWLINE, testOutputStream.toString());
    }

    // ls cmd, input: not given, output: given, ls do not take in input stream
    @Test
    public void lsIOIntegration_noInputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{LS_CMD, REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(LS_OUT, actualFileOutput);
    }

    // ls cmd, input: given, output: given, ls do not take in input stream
    @Test
    public void lsIOIntegration_inputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{LS_CMD, REDIR_INPUT, fileInPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(LS_OUT, actualFileOutput);
    }

    // wc cmd, input: given, output: not given
    @Test
    public void wcIOIntegration_inputNoOutput_output() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String[] args = new String[]{WC_CMD, REDIR_INPUT, fileInPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(WC_OUT + STRING_NEWLINE, testOutputStream.toString());
    }

    // wc cmd, input: not given, output: given
    @Test
    public void wcIOIntegration_noInputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{WC_CMD, FILE_IN, REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(WC_OUT + "\t" + FILE_IN, actualFileOutput);
    }

    // wc cmd, input: given, output: given
    @Test
    public void wcIOIntegration_inputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{WC_CMD, REDIR_INPUT, fileInPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(WC_OUT, actualFileOutput);
    }

    // wc cmd, input: file in directory, output: not given
    @Test
    public void wcIOIntegration_inputDirNoOutput_output() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String[] args = new String[]{WC_CMD, REDIR_INPUT, fileInDirPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(WC_OUT + STRING_NEWLINE, testOutputStream.toString());
    }

    // wc cmd, input: not given, output: file in directory
    @Test
    public void wcIOIntegration_noInputOutputDir_outputFileDir() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{WC_CMD, FILE_IN, REDIR_OUTPUT, fileOutDirPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutDirPath);
        assertEquals(WC_OUT + "\t" + FILE_IN, actualFileOutput);
    }

    // wc cmd, input: not given, output: non-existent file
    @Test
    public void wcIOIntegration_noInputOutputNe_outputFileNe() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{WC_CMD, FILE_IN, REDIR_OUTPUT, fileNePath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileNePath);
        assertEquals(WC_OUT + "\t" + FILE_IN, actualFileOutput);
        deleteFileIfExists(fileNePath);
    }

    // cat cmd, input: given, output: not given
    @Test
    public void catIOIntegration_inputNoOutput_output() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String[] args = new String[]{CAT_CMD, REDIR_INPUT, fileInPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(TEST_CAT + STRING_NEWLINE, testOutputStream.toString());
    }

    // cat cmd, input: not given, output: given
    @Test
    public void catIOIntegration_noInputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{CAT_CMD, FILE_IN, REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(TEST_CAT, actualFileOutput);
    }

    // cat cmd, input: given, output: given
    @Test
    public void catIOIntegration_inputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{CAT_CMD, REDIR_INPUT, fileInPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(TEST_CAT, actualFileOutput);
    }

    // grep cmd, input: given, output: not given
    @Test
    public void grepIOIntegration_inputNoOutput_output() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String[] args = new String[]{GREP_CMD, GREP_IN, REDIR_INPUT, fileInPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(GREP_OUT + STRING_NEWLINE, testOutputStream.toString());
    }

    // grep cmd, input: not given, output: given
    @Test
    public void grepIOIntegration_noInputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{GREP_CMD, GREP_IN, FILE_IN, REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(GREP_OUT, actualFileOutput);
    }

    // grep cmd, input: given, output: given
    @Test
    public void grepIOIntegration_inputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{GREP_CMD, GREP_IN, REDIR_INPUT, fileInPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(GREP_OUT, actualFileOutput);
    }

    // cut cmd, input: given, output: not given
    @Test
    public void cutIOIntegration_inputNoOutput_output() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String[] args = new String[]{CUT_CMD, CUT_FLAG, CUT_POS, REDIR_INPUT, fileInPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(CUT_OUT + STRING_NEWLINE, testOutputStream.toString());
    }

    // cut cmd, input: not given, output: given
    @Test
    public void cutIOIntegration_noInputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{CUT_CMD, CUT_FLAG, CUT_POS, FILE_IN, REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(CUT_OUT, actualFileOutput);
    }

    // cut cmd, input: given, output: given
    @Test
    public void cutIOIntegration_inputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{CUT_CMD, CUT_FLAG, CUT_POS, REDIR_INPUT, fileInPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(CUT_OUT, actualFileOutput);
    }

    // sort cmd, input: given, output: not given
    @Test
    public void sortIOIntegration_inputNoOutput_output() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String[] args = new String[]{SORT_CMD, REDIR_INPUT, fileInPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(SORT_OUT + STRING_NEWLINE, testOutputStream.toString());
    }

    // sort cmd, input: not given, output: given
    @Test
    public void sortIOIntegration_noInputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{SORT_CMD, FILE_IN, REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(SORT_OUT, actualFileOutput);
    }

    // sort cmd, input: given, output: given
    @Test
    public void sortIOIntegration_inputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{SORT_CMD, REDIR_INPUT, fileInPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(SORT_OUT, actualFileOutput);
    }

    // cp cmd, input: given, output: not given, input output stream does not affect cp
    @Test
    public void cpIOIntegration_inputNoOutput_output() throws IOException, AbstractApplicationException, ShellException {
        Files.write(fileTempPath, TEST_IN_STR.getBytes());
        String[] args = new String[]{CP_CMD, fileTempPath.toString(), fileOutPath.toString(), REDIR_INPUT, fileInPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOut = getFileContent(fileOutPath);
        assertEquals(TEST_IN_STR, actualFileOut);
    }

    // cp cmd, input: not given, output: given, input output stream does not affect cp
    @Test
    public void cpIOIntegration_noInputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        Files.write(fileTempPath, TEST_IN_STR.getBytes());
        String[] args = new String[]{CP_CMD, fileTempPath.toString(), fileOutPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(TEST_IN_STR, actualFileOutput);
    }

    // cp cmd, input: given, output: given, input output stream does not affect cp
    @Test
    public void cpIOIntegration_inputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        Files.write(fileTempPath, TEST_IN_STR.getBytes());
        String[] args = new String[]{CP_CMD, fileTempPath.toString(), fileOutPath.toString(), REDIR_INPUT, fileInPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(TEST_IN_STR, actualFileOutput);
    }

    // rm cmd, input: given, output: not given, input output stream does not affect rm
    @Test
    public void rmIOIntegration_inputNoOutput_removeFile() throws IOException, AbstractApplicationException, ShellException {
        Files.createFile(fileTempPath);
        assertTrue(fileTempPath.toFile().exists());

        String[] args = new String[]{RM_CMD, fileTempPath.toString(), REDIR_INPUT, fileInPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertFalse(fileTempPath.toFile().exists());
    }

    // rm cmd, input: not given, output: given, input output stream does not affect rm
    @Test
    public void rmIOIntegration_noInputOutput_removeFile() throws IOException, AbstractApplicationException, ShellException {
        Files.createFile(fileTempPath);
        assertTrue(fileTempPath.toFile().exists());

        String[] args = new String[]{RM_CMD, fileTempPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertFalse(fileTempPath.toFile().exists());
    }

    // rm cmd, input: given, output: given, input output stream does not affect rm
    @Test
    public void rmIOIntegration_inputOutput_removeFile() throws IOException, AbstractApplicationException, ShellException {
        Files.createFile(fileTempPath);
        assertTrue(fileTempPath.toFile().exists());

        String[] args = new String[]{RM_CMD, fileTempPath.toString(), REDIR_INPUT, fileInPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertFalse(fileTempPath.toFile().exists());
    }

    // tee cmd, input: given, output: not given
    @Test
    public void teeIOIntegration_inputNoOutput_output() throws IOException, AbstractApplicationException, ShellException {
        Files.createFile(fileTempPath);
        String[] args = new String[]{TEE_CMD, fileTempPath.toString(), REDIR_INPUT, fileInPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOut = getFileContent(fileTempPath);
        assertEquals(TEST_CAT, actualFileOut);
        assertEquals(TEST_CAT + STRING_NEWLINE, testOutputStream.toString());
    }

    // tee cmd, input: not given, output: given
    @Test
    public void teeIOIntegration_noInputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        Files.createFile(fileTempPath);
        ByteArrayInputStream testInputStream = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = new String[]{TEE_CMD, fileTempPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(testInputStream, testOutputStream);

        String actualFileOutput1 = getFileContent(fileTempPath);
        String actualFileOutput2 = getFileContent(fileOutPath);
        assertEquals(TEST_CAT, actualFileOutput1);
        assertEquals(TEST_CAT, actualFileOutput2);
        testInputStream.close();
    }

    // tee cmd, input: given, output: given
    @Test
    public void teeIOIntegration_inputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        Files.createFile(fileTempPath);
        String[] args = new String[]{TEE_CMD, fileTempPath.toString(), REDIR_INPUT, fileInPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput1 = getFileContent(fileTempPath);
        String actualFileOutput2 = getFileContent(fileOutPath);
        assertEquals(TEST_CAT, actualFileOutput1);
        assertEquals(TEST_CAT, actualFileOutput2);
    }

    // exit cmd, input: given, output: not given, input output stream does not affect exit
    @Test
    public void exitIOIntegration_inputNoOutput_exit() {
        String[] args = new String[]{EXIT_CMD, REDIR_INPUT, fileInPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Exception exception = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));

        assertEquals(EXIT_EXCEP, exception.getMessage());
    }

    // exit cmd, input: not given, output: given, input output stream does not affect exit
    @Test
    public void exitIOIntegration_noInputOutput_exit() {
        String[] args = new String[]{EXIT_CMD, REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Exception exception = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));

        assertEquals(EXIT_EXCEP, exception.getMessage());
    }

    // exit cmd, input: given, output: given, input output stream does not affect exit
    @Test
    public void exitIOIntegration_inputOutput_exit() {
        String[] args = new String[]{EXIT_CMD, REDIR_INPUT, fileInPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Exception exception = assertThrows(ExitException.class, () -> callCommand.evaluate(System.in, testOutputStream));

        assertEquals(EXIT_EXCEP, exception.getMessage());
    }

    // wc cmd, input: more than one, output: not given
    @Test
    public void wcIOIntegration_multiInputNoOutput_throwsException() {
        String[] args = new String[]{WC_CMD, REDIR_INPUT, fileInPath.toString(), REDIR_INPUT, fileInPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Exception exception = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));

        assertEquals(SHELL_EXCEP + ERR_MULTIPLE_STREAMS, exception.getMessage());
    }

    // wc cmd, input: not given, output: more than one
    @Test
    public void wcIOIntegration_noInputMultiOutput_throwsException() {
        String[] args = new String[]{WC_CMD, FILE_IN, REDIR_OUTPUT, fileOutPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Exception exception = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));

        assertEquals(SHELL_EXCEP + ERR_MULTIPLE_STREAMS, exception.getMessage());
    }

    // wc cmd, input: non-existent file, output: not given
    @Test
    public void wcIOIntegration_nonExistFileInputNoOutput_throwsException() {
        String[] args = new String[]{WC_CMD, REDIR_INPUT, fileNePath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Exception exception = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));

        assertEquals(SHELL_EXCEP + fileNePath + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // wc cmd, input: non-existent directory, output: not given
    @Test
    public void wcIOIntegration_nonExistDirInputNoOutput_throwsException() {
        String[] args = new String[]{WC_CMD, REDIR_INPUT, fileNeDirPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Exception exception = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));

        assertEquals(SHELL_EXCEP + fileNeDirPath + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // wc cmd, input: not given, output: non-existent directory
    @Test
    public void wcIOIntegration_noInputNonExistDirOutput_throwsException() {
        String[] args = new String[]{WC_CMD, FILE_IN, REDIR_OUTPUT, fileNeDirPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Exception exception = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));

        assertEquals(SHELL_EXCEP + fileNeDirPath + ": " + ERR_DIR_NOT_FOUND, exception.getMessage());
    }

    // wc cmd, input: directory, output: not given
    @Test
    public void wcIOIntegration_dirInputNoOutput_throwsException() {
        String[] args = new String[]{WC_CMD, REDIR_INPUT, fileDirPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Exception exception = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));

        assertEquals(SHELL_EXCEP + fileDirPath + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // wc cmd, input: not given, output: directory
    @Test
    public void wcIOIntegration_noInputDirOutput_throwsException() {
        String[] args = new String[]{WC_CMD, FILE_IN, REDIR_OUTPUT, fileDirPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Exception exception = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));

        assertEquals(SHELL_EXCEP + fileDirPath + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // wc cmd, input: not given, output: no permission
    @Test
    public void wcIOIntegration_noInputNoPerm_throwsException() {
        String[] args = new String[]{WC_CMD, FILE_IN, REDIR_OUTPUT, fileNoPermPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Exception exception = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));

        assertEquals(SHELL_EXCEP + fileNoPermPath + ": " + ERR_NO_PERM, exception.getMessage());
    }

    // sort cmd, input: not given, output: given
    @Test
    public void sortIOIntegration_noInputOutputNonExistStdin_throwsException() {
        String[] args = new String[]{SORT_CMD, NON_EXIST_FILE, REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Exception exception = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));

        assertEquals(SORT_EXCEP + NON_EXIST_FILE + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // cd cmd, input: given, output: not given, input output stream does not affect cd
    @Test
    public void cdIOIntegration_inputNoOutput_changeDir() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String[] args = new String[]{CD_CMD, fileDirPath.toString(), REDIR_INPUT, fileInPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(Environment.currentDirectory, fileDirPath.toString());
    }

    // cd cmd, input: not given, output: given, input output stream does not affect cd
    @Test
    public void cdIOIntegration_noInputOutput_changeDir() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{CD_CMD, fileDirPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(Environment.currentDirectory, fileDirPath.toString());
    }

    // cd cmd, input: given, output: given, input output stream does not affect cd
    @Test
    public void cdIOIntegration_inputOutput_changeDir() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{CD_CMD, fileDirPath.toString(), REDIR_INPUT, fileInPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(Environment.currentDirectory, fileDirPath.toString());
    }

    // paste cmd, input: given, output: not given
    @Test
    public void pasteIOIntegration_inputNoOutput_output() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String[] args = new String[]{PASTE_CMD, STDIN_DASH, fileBPath.toString(), REDIR_INPUT, fileAPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(PASTE_OUT + STRING_NEWLINE, testOutputStream.toString());
    }

    // paste cmd, input: not given, output: given
    @Test
    public void pasteIOIntegration_noInputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{PASTE_CMD, fileAPath.toString(), fileBPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(PASTE_OUT, actualFileOutput);
    }

    // paste cmd, input: given, output: given
    @Test
    public void pasteIOIntegration_inputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{PASTE_CMD, STDIN_DASH, fileBPath.toString(), REDIR_INPUT, fileAPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(PASTE_OUT, actualFileOutput);
    }

    // uniq cmd, input: given, output: not given
    @Test
    public void uniqIOIntegration_inputNoOutput_output() throws FileNotFoundException, AbstractApplicationException, ShellException {
        String[] args = new String[]{UNIQ_CMD, REDIR_INPUT, fileDupPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertEquals(DUP_OUT + STRING_NEWLINE, testOutputStream.toString());
    }

    // uniq cmd, input: not given, output: given
    @Test
    public void uniqIOIntegration_noInputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{UNIQ_CMD, fileDupPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(DUP_OUT, actualFileOutput);
    }

    // uniq cmd, input: given, output: given
    @Test
    public void uniqIOIntegration_inputOutput_outputFile() throws IOException, AbstractApplicationException, ShellException {
        String[] args = new String[]{UNIQ_CMD, STDIN_DASH, REDIR_INPUT, fileDupPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        String actualFileOutput = getFileContent(fileOutPath);
        assertEquals(DUP_OUT, actualFileOutput);
    }

    // mv cmd, input: given, output: not given, input output stream does not affect mv
    @Test
    public void mvIOIntegration_inputNoOutput_moveFile() throws IOException, AbstractApplicationException, ShellException {
        Files.createFile(fileTempPath);
        String[] args = new String[]{MV_CMD, fileTempPath.toString(), fileTempMvPath.toString(), REDIR_INPUT, fileInPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertTrue(fileTempMvPath.toFile().exists());
    }

    // mv cmd, input: not given, output: given, input output stream does not affect mv
    @Test
    public void mvIOIntegration_noInputOutput_moveFile() throws IOException, AbstractApplicationException, ShellException {
        Files.createFile(fileTempPath);
        String[] args = new String[]{MV_CMD, fileTempPath.toString(), fileTempMvPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertTrue(fileTempMvPath.toFile().exists());
    }

    // mv cmd, input: given, output: given, input output stream does not affect mv
    @Test
    public void mvIOIntegration_inputOutput_moveFile() throws IOException, AbstractApplicationException, ShellException {
        Files.createFile(fileTempPath);
        String[] args = new String[]{MV_CMD, fileTempPath.toString(), fileTempMvPath.toString(), REDIR_INPUT, fileInPath.toString(), REDIR_OUTPUT, fileOutPath.toString()};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);

        assertTrue(fileTempMvPath.toFile().exists());
    }
}
