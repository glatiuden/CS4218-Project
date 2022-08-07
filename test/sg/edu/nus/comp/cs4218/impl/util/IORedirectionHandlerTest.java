package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_DIR_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_MULTIPLE_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.testutils.IOAssertUtils.*;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.*;

class IORedirectionHandlerTest {
    private final static String REDIR_INPUT = "<";
    private final static String REDIR_OUTPUT = ">";
    private final static String FILE_IN_1_EXT = "1.txt";
    private final static String FILE_IN_2 = "2";
    private final static String FILE_IN_SYMBOL = "-";
    private final static String FILE_IN_SPACED = "spa ced in file.txt";
    private final static String NON_EXIST_FILE = "ne.txt";
    private final static String NO_PERM_FILE = "np.txt";
    private final static String FILE_OUT_EXIST = "exist.txt";
    private final static String FILE_OUT_1_EXT = "o.txt";
    private final static String FILE_OUT_2 = "o";
    private final static String FILE_OUT_SPACE = "spa ced out file.txt";
    private final static String NEST_DIR_1 = "nest";
    private final static String NEST_DIR_2 = "nested";
    private final static String NON_EXIST_DIR = "nedir";
    private final static String SINGLE_LINE_CONT = "testing file 123 $#@!#@D12";
    private final static String MULTI_LINE_CONT = "testing and more\ntesting D@!#D 2 more lines\nand @#@!D#&((&F! even more lines";
    private final static String TEST_STRING_1 = "test";
    private final static String TEST_STRING_2 = "testing";
    private final static String TEST_STRING_3 = "evenmoretesting";
    private final static String OUTPUT_CONT = "output";
    private final static String SHELL_EXCEP = "shell: ";
    @TempDir
    public static Path folderPath;
    private static ArgumentResolver argRes;

    @BeforeAll
    public static void setup() throws IOException {
        argRes = mock(ArgumentResolver.class);

        // ./nest/nested
        Files.createDirectories(folderPath.resolve(NEST_DIR_1).resolve(NEST_DIR_2));
        // ./1.txt
        Files.write(folderPath.resolve(FILE_IN_1_EXT), SINGLE_LINE_CONT.getBytes());
        // ./2
        Files.write(folderPath.resolve(FILE_IN_2), MULTI_LINE_CONT.getBytes());
        // ./-
        Files.write(folderPath.resolve(FILE_IN_SYMBOL), SINGLE_LINE_CONT.getBytes());
        // ./'spa ced in file.txt'
        Files.write(folderPath.resolve(FILE_IN_SPACED), MULTI_LINE_CONT.getBytes());
        // ./exist.txt
        Files.write(folderPath.resolve(FILE_OUT_EXIST), OUTPUT_CONT.getBytes());
        // ./nest/2
        Files.write(folderPath.resolve(NEST_DIR_1).resolve(FILE_IN_2), SINGLE_LINE_CONT.getBytes());
        // ./nest/exist.txt
        Files.write(folderPath.resolve(NEST_DIR_1).resolve(FILE_OUT_EXIST), MULTI_LINE_CONT.getBytes());
        // ./nest/nested/1.txt
        Files.write(folderPath.resolve(NEST_DIR_1).resolve(NEST_DIR_2).resolve(FILE_IN_1_EXT), MULTI_LINE_CONT.getBytes());
        // ./nest/nested/exist.txt
        Files.write(folderPath.resolve(NEST_DIR_1).resolve(NEST_DIR_2).resolve(FILE_OUT_EXIST), OUTPUT_CONT.getBytes());

        // ./np.txt no read/write permissions file
        Files.write(folderPath.resolve(NO_PERM_FILE), SINGLE_LINE_CONT.getBytes());
        removeFilePermissions(folderPath.resolve(NO_PERM_FILE));
    }

    @AfterAll
    public static void tearDown() {
        resetFilePermissions(folderPath.resolve(NO_PERM_FILE));
    }

    @AfterEach
    public void resetOutputFile() throws IOException {
        Files.write(folderPath.resolve(FILE_OUT_EXIST), OUTPUT_CONT.getBytes());
        Files.write(folderPath.resolve(NEST_DIR_1).resolve(NEST_DIR_2).resolve(FILE_OUT_EXIST), OUTPUT_CONT.getBytes());
    }

    // non-empty args, no input/output redirection
    @Test
    public void extractRedirOptions_nonEmptyArgsNoIO_nonEmptyArgs() throws FileNotFoundException, AbstractApplicationException, ShellException {
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(TEST_STRING_1), System.in, System.out, argRes);
        ioRedirHandler.extractRedirOptions();
        assertEquals(System.in, ioRedirHandler.getInputStream());
        assertEquals(System.out, ioRedirHandler.getOutputStream());
        assertEquals(Arrays.asList(TEST_STRING_1), ioRedirHandler.getNoRedirArgsList());
    }

    // empty args, no input/output redirection
    @Test
    public void extractRedirOptions_emptyArgsNoIO_throwsShellException() {
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(), System.in, System.out, argRes);
        Exception exception = assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());
        assertEquals(SHELL_EXCEP + ERR_SYNTAX, exception.getMessage());
    }

    // only io args, only new file output redirection
    @Test
    public void extractRedirOptions_onlyIOArgsOnlyOutputNewFile_emptyArgs() throws IOException, AbstractApplicationException, ShellException {
        Path testPath = folderPath.resolve(FILE_OUT_1_EXT);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_OUTPUT, testPath.toString()), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(testPath.toString())).thenReturn(Arrays.asList(testPath.toString()));
        ioRedirHandler.extractRedirOptions();
        assertEquals(System.in, ioRedirHandler.getInputStream());
        assertEquals(Arrays.asList(), ioRedirHandler.getNoRedirArgsList());
        assertFileOutputStream(testPath, ioRedirHandler.getOutputStream());
        deleteFileIfExists(testPath);
    }

    // only io args, only new file spaced name output redirection
    @Test
    public void extractRedirOptions_onlyIOArgsOnlyOutputNewFileSpaced_emptyArgs() throws IOException, AbstractApplicationException, ShellException {
        Path testPath = folderPath.resolve(FILE_OUT_SPACE);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_OUTPUT, testPath.toString()), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(testPath.toString())).thenReturn(Arrays.asList(testPath.toString()));
        ioRedirHandler.extractRedirOptions();
        assertEquals(System.in, ioRedirHandler.getInputStream());
        assertEquals(Arrays.asList(), ioRedirHandler.getNoRedirArgsList());
        assertFileOutputStream(testPath, ioRedirHandler.getOutputStream());
        deleteFileIfExists(testPath);
    }

    // only io args, only existing file output redirection
    @Test
    public void extractRedirOptions_onlyIOArgsOnlyOutputExistFile_emptyArgs() throws IOException, AbstractApplicationException, ShellException {
        Path testPath = folderPath.resolve(FILE_OUT_EXIST);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_OUTPUT, testPath.toString()), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(testPath.toString())).thenReturn(Arrays.asList(testPath.toString()));
        ioRedirHandler.extractRedirOptions();
        assertEquals(System.in, ioRedirHandler.getInputStream());
        assertEquals(Arrays.asList(), ioRedirHandler.getNoRedirArgsList());
        assertFileOutputStream(testPath, ioRedirHandler.getOutputStream());
    }

    // io + other args, only new file output redirection
    @Test
    public void extractRedirOptions_ioArgsOnlyOutputNewFile_nonEmptyArgs() throws IOException, AbstractApplicationException, ShellException {
        Path testPath = folderPath.resolve(FILE_OUT_2);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(TEST_STRING_1, REDIR_OUTPUT, testPath.toString(), TEST_STRING_2), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(testPath.toString())).thenReturn(Arrays.asList(testPath.toString()));
        ioRedirHandler.extractRedirOptions();
        assertEquals(System.in, ioRedirHandler.getInputStream());
        assertEquals(Arrays.asList(TEST_STRING_1, TEST_STRING_2), ioRedirHandler.getNoRedirArgsList());
        assertFileOutputStream(testPath, ioRedirHandler.getOutputStream());
        deleteFileIfExists(testPath);
    }

    // only io args, only directory output redirection
    @Test
    public void extractRedirOptions_onlyIOArgsOnlyOutputDir_throwsShellException() throws FileNotFoundException, AbstractApplicationException, ShellException {
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_OUTPUT, folderPath.resolve(NEST_DIR_1).toString()), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(folderPath.resolve(NEST_DIR_1).toString())).thenReturn(Arrays.asList(folderPath.resolve(NEST_DIR_1).toString()));
        Exception exception = assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());
        assertEquals(SHELL_EXCEP + folderPath.resolve(NEST_DIR_1) + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // io + other args, only new file in directory output redirection
    @Test
    public void extractRedirOptions_ioArgsOnlyOutputNewFileInDir_nonEmptyArgs() throws IOException, AbstractApplicationException, ShellException {
        Path testPath = folderPath.resolve(NEST_DIR_1).resolve(FILE_OUT_1_EXT);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(TEST_STRING_1, TEST_STRING_2, REDIR_OUTPUT, testPath.toString()), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(testPath.toString())).thenReturn(Arrays.asList(testPath.toString()));
        ioRedirHandler.extractRedirOptions();
        assertEquals(System.in, ioRedirHandler.getInputStream());
        assertEquals(Arrays.asList(TEST_STRING_1, TEST_STRING_2), ioRedirHandler.getNoRedirArgsList());
        assertFileOutputStream(testPath, ioRedirHandler.getOutputStream());
        deleteFileIfExists(testPath);
    }

    // only io args, only file in non-existent directory output redirection
    @Test
    public void extractRedirOptions_onlyIOArgsOnlyOutputFileInNonExistDir_throwsShellException() throws FileNotFoundException, AbstractApplicationException, ShellException {
        Path testPath = folderPath.resolve(NON_EXIST_DIR).resolve(FILE_OUT_2);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_OUTPUT, testPath.toString()), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(testPath.toString())).thenReturn(Arrays.asList(testPath.toString()));
        Exception exception = assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());
        assertEquals(SHELL_EXCEP + testPath + ": " + ERR_DIR_NOT_FOUND, exception.getMessage());
    }

    // only io args, multiple output redirection. "> o.txt > o"
    @Test
    public void extractRedirOptions_onlyIOArgsMultiOutputRedirMultiFiles_throwsShellException() throws FileNotFoundException, AbstractApplicationException, ShellException {
        Path testPath1 = folderPath.resolve(FILE_OUT_1_EXT), testPath2 = folderPath.resolve(FILE_OUT_2);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_OUTPUT, testPath1.toString(), REDIR_OUTPUT, testPath2.toString()), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(testPath1.toString())).thenReturn(Arrays.asList(testPath1.toString()));
        when(argRes.resolveOneArgument(testPath2.toString())).thenReturn(Arrays.asList(testPath2.toString()));
        Exception exception = assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());
        assertEquals(SHELL_EXCEP + ERR_MULTIPLE_STREAMS, exception.getMessage());
    }

    // only io args, multiple output redirection. "> > o.txt"
    @Test
    public void extractRedirOptions_onlyIOArgsMultiOutputRedirSingleFileSecond_throwsShellException() {
        Path testPath = folderPath.resolve(FILE_OUT_1_EXT);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_OUTPUT, REDIR_OUTPUT, testPath.toString()), System.in, System.out, argRes);
        Exception exception = assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());
        assertEquals(SHELL_EXCEP + ERR_SYNTAX, exception.getMessage());
    }

    // only io args, multiple output redirection. "> o.txt >"
    @Test
    public void extractRedirOptions_onlyIOArgsMultiOutputRedirSingleFileFirst_throwsShellException() throws FileNotFoundException, AbstractApplicationException, ShellException {
        Path testPath = folderPath.resolve(FILE_OUT_1_EXT);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_OUTPUT, testPath.toString(), REDIR_OUTPUT), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(testPath.toString())).thenReturn(Arrays.asList(testPath.toString()));
        Exception exception = assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());
        assertEquals(SHELL_EXCEP + ERR_SYNTAX, exception.getMessage());
    }

    // only io args, only no perm file output redirection
    @Test
    public void extractRedirOptions_onlyIOArgsOnlyOutputNoPermFile_throwsShellException() throws FileNotFoundException, AbstractApplicationException, ShellException {
        Path testPath = folderPath.resolve(NO_PERM_FILE);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_OUTPUT, testPath.toString()), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(testPath.toString())).thenReturn(Arrays.asList(testPath.toString()));
        Exception exception = assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());
        assertEquals(SHELL_EXCEP + testPath + ": " + ERR_NO_PERM, exception.getMessage());
    }

    // only io args, existing input and output files redirection
    @Test
    public void extractRedirOptions_onlyIOArgsIOExistFiles_emptyArgs() throws IOException, AbstractApplicationException, ShellException {
        Path inputPath = folderPath.resolve(FILE_IN_1_EXT), outputPath = folderPath.resolve(FILE_OUT_EXIST);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_INPUT, inputPath.toString(), REDIR_OUTPUT, outputPath.toString()), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(inputPath.toString())).thenReturn(Arrays.asList(inputPath.toString()));
        when(argRes.resolveOneArgument(outputPath.toString())).thenReturn(Arrays.asList(outputPath.toString()));
        ioRedirHandler.extractRedirOptions();
        assertEquals(Arrays.asList(), ioRedirHandler.getNoRedirArgsList());
        assertFileInputStream(inputPath, ioRedirHandler.getInputStream());
        assertFileOutputStream(outputPath, ioRedirHandler.getOutputStream());
    }

    // io + other args, existing input and output files redirection
    @Test
    public void extractRedirOptions_ioArgsIOExistFiles_nonEmptyArgs() throws IOException, AbstractApplicationException, ShellException {
        Path inputPath = folderPath.resolve(FILE_IN_1_EXT), outputPath = folderPath.resolve(FILE_OUT_EXIST);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(TEST_STRING_1, REDIR_OUTPUT, outputPath.toString(), TEST_STRING_2, TEST_STRING_3, REDIR_INPUT, inputPath.toString()), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(inputPath.toString())).thenReturn(Arrays.asList(inputPath.toString()));
        when(argRes.resolveOneArgument(outputPath.toString())).thenReturn(Arrays.asList(outputPath.toString()));
        ioRedirHandler.extractRedirOptions();
        assertEquals(Arrays.asList(TEST_STRING_1, TEST_STRING_2, TEST_STRING_3), ioRedirHandler.getNoRedirArgsList());
        assertFileInputStream(inputPath, ioRedirHandler.getInputStream());
        assertFileOutputStream(outputPath, ioRedirHandler.getOutputStream());
    }

    // only io args, existing input file and output file in directory redirection
    @Test
    public void extractRedirOptions_onlyIOArgsIOExistFileExistFileDir_emptyArgs() throws IOException, AbstractApplicationException, ShellException {
        Path inputPath = folderPath.resolve(FILE_IN_2), outputPath = folderPath.resolve(NEST_DIR_1).resolve(FILE_OUT_EXIST);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_OUTPUT, outputPath.toString(), REDIR_INPUT, inputPath.toString()), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(inputPath.toString())).thenReturn(Arrays.asList(inputPath.toString()));
        when(argRes.resolveOneArgument(outputPath.toString())).thenReturn(Arrays.asList(outputPath.toString()));
        ioRedirHandler.extractRedirOptions();
        assertEquals(Arrays.asList(), ioRedirHandler.getNoRedirArgsList());
        assertFileInputStream(inputPath, ioRedirHandler.getInputStream());
        assertFileOutputStream(outputPath, ioRedirHandler.getOutputStream());
    }

    // io + other args, existing input file and output file in directory redirection
    @Test
    public void extractRedirOptions_ioArgsIOExistFileExistFileDir_nonEmptyArgs() throws IOException, AbstractApplicationException, ShellException {
        Path inputPath = folderPath.resolve(FILE_IN_2), outputPath = folderPath.resolve(NEST_DIR_1).resolve(FILE_OUT_EXIST);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_OUTPUT, outputPath.toString(), TEST_STRING_1, TEST_STRING_3, TEST_STRING_2, REDIR_INPUT, inputPath.toString()), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(inputPath.toString())).thenReturn(Arrays.asList(inputPath.toString()));
        when(argRes.resolveOneArgument(outputPath.toString())).thenReturn(Arrays.asList(outputPath.toString()));
        ioRedirHandler.extractRedirOptions();
        assertEquals(Arrays.asList(TEST_STRING_1, TEST_STRING_3, TEST_STRING_2), ioRedirHandler.getNoRedirArgsList());
        assertFileInputStream(inputPath, ioRedirHandler.getInputStream());
        assertFileOutputStream(outputPath, ioRedirHandler.getOutputStream());
    }

    // only io args, existing input file and non-existing output file in directory redirection
    @Test
    public void extractRedirOptions_onlyIOArgsIOExistFileNonExistFileDir_emptyArgs() throws IOException, AbstractApplicationException, ShellException {
        Path inputPath = folderPath.resolve(FILE_IN_SYMBOL), outputPath = folderPath.resolve(NEST_DIR_1).resolve(FILE_OUT_1_EXT);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_INPUT, inputPath.toString(), REDIR_OUTPUT, outputPath.toString()), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(inputPath.toString())).thenReturn(Arrays.asList(inputPath.toString()));
        when(argRes.resolveOneArgument(outputPath.toString())).thenReturn(Arrays.asList(outputPath.toString()));
        ioRedirHandler.extractRedirOptions();
        assertEquals(Arrays.asList(), ioRedirHandler.getNoRedirArgsList());
        assertFileInputStream(inputPath, ioRedirHandler.getInputStream());
        assertFileOutputStream(outputPath, ioRedirHandler.getOutputStream());
        deleteFileIfExists(outputPath);
    }

    // io + other args, only existing file input redirection
    @Test
    public void extractRedirOptions_ioArgsOnlyInputExistFile_nonEmptyArgs() throws IOException, AbstractApplicationException, ShellException {
        Path testPath = folderPath.resolve(FILE_IN_SYMBOL);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(TEST_STRING_1, TEST_STRING_2, REDIR_INPUT, testPath.toString(), TEST_STRING_3), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(testPath.toString())).thenReturn(Arrays.asList(testPath.toString()));
        ioRedirHandler.extractRedirOptions();
        assertEquals(Arrays.asList(TEST_STRING_1, TEST_STRING_2, TEST_STRING_3), ioRedirHandler.getNoRedirArgsList());
        assertEquals(System.out, ioRedirHandler.getOutputStream());
        assertFileInputStream(testPath, ioRedirHandler.getInputStream());
    }

    // io + other args, only existing file spaced name input redirection
    @Test
    public void extractRedirOptions_ioArgsOnlyInputExistFileSpaced_nonEmptyArgs() throws IOException, AbstractApplicationException, ShellException {
        Path testPath = folderPath.resolve(FILE_IN_SPACED);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(TEST_STRING_1, TEST_STRING_2, REDIR_INPUT, testPath.toString(), TEST_STRING_3), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(testPath.toString())).thenReturn(Arrays.asList(testPath.toString()));
        ioRedirHandler.extractRedirOptions();
        assertEquals(Arrays.asList(TEST_STRING_1, TEST_STRING_2, TEST_STRING_3), ioRedirHandler.getNoRedirArgsList());
        assertEquals(System.out, ioRedirHandler.getOutputStream());
        assertFileInputStream(testPath, ioRedirHandler.getInputStream());
    }

    // only io args, only directory input redirection
    @Test
    public void extractRedirOptions_onlyIOArgsOnlyInputDir_throwShellException() throws FileNotFoundException, AbstractApplicationException, ShellException {
        Path testPath = folderPath.resolve(NEST_DIR_1);
        when(argRes.resolveOneArgument(testPath.toString())).thenReturn(Arrays.asList(testPath.toString()));
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_INPUT, testPath.toString()), System.in, System.out, argRes);
        Exception exception = assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());
        assertEquals(SHELL_EXCEP + testPath + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // io + other args, existing input and output file in directory redirection
    @Test
    public void extractRedirOptions_ioArgsIOExistFilesInDir_nonEmptyArgs() throws IOException, AbstractApplicationException, ShellException {
        Path inputPath = folderPath.resolve(NEST_DIR_1).resolve(NEST_DIR_2).resolve(FILE_IN_1_EXT), outputPath = folderPath.resolve(NEST_DIR_1).resolve(NEST_DIR_2).resolve(FILE_OUT_EXIST);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_OUTPUT, outputPath.toString(), TEST_STRING_3, REDIR_INPUT, inputPath.toString(), TEST_STRING_1, TEST_STRING_2), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(inputPath.toString())).thenReturn(Arrays.asList(inputPath.toString()));
        when(argRes.resolveOneArgument(outputPath.toString())).thenReturn(Arrays.asList(outputPath.toString()));
        ioRedirHandler.extractRedirOptions();
        assertEquals(Arrays.asList(TEST_STRING_3, TEST_STRING_1, TEST_STRING_2), ioRedirHandler.getNoRedirArgsList());
        assertFileInputStream(inputPath, ioRedirHandler.getInputStream());
        assertFileOutputStream(outputPath, ioRedirHandler.getOutputStream());
    }

    // only io args, existing input and non-existing output file in directory redirection
    @Test
    public void extractRedirOptions_onlyIOArgsIOExistFileInDirNonExistFileInDir_emptyArgs() throws IOException, AbstractApplicationException, ShellException {
        Path inputPath = folderPath.resolve(NEST_DIR_1).resolve(NEST_DIR_2).resolve(FILE_IN_1_EXT), outputPath = folderPath.resolve(NEST_DIR_1).resolve(NEST_DIR_2).resolve(FILE_OUT_2);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_OUTPUT, outputPath.toString(), REDIR_INPUT, inputPath.toString()), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(inputPath.toString())).thenReturn(Arrays.asList(inputPath.toString()));
        when(argRes.resolveOneArgument(outputPath.toString())).thenReturn(Arrays.asList(outputPath.toString()));
        ioRedirHandler.extractRedirOptions();
        assertEquals(Arrays.asList(), ioRedirHandler.getNoRedirArgsList());
        assertFileInputStream(inputPath, ioRedirHandler.getInputStream());
        assertFileOutputStream(outputPath, ioRedirHandler.getOutputStream());
        deleteFileIfExists(outputPath);
    }

    // only io args, only existing input file in directory redirection
    @Test
    public void extractRedirOptions_onlyIOArgsOnlyInputExistFilesInDir_emptyArgs() throws IOException, AbstractApplicationException, ShellException {
        Path testPath = folderPath.resolve(NEST_DIR_1).resolve(NEST_DIR_2).resolve(FILE_IN_1_EXT);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_INPUT, testPath.toString()), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(testPath.toString())).thenReturn(Arrays.asList(testPath.toString()));
        ioRedirHandler.extractRedirOptions();
        assertEquals(Arrays.asList(), ioRedirHandler.getNoRedirArgsList());
        assertEquals(System.out, ioRedirHandler.getOutputStream());
        assertFileInputStream(testPath, ioRedirHandler.getInputStream());
    }

    // io + other args, existing input file in directory and output file redirection
    @Test
    public void extractRedirOptions_ioArgsIOExistFileInDirExistFile_nonEmptyArgs() throws IOException, AbstractApplicationException, ShellException {
        Path inputPath = folderPath.resolve(NEST_DIR_1).resolve(FILE_IN_2), outputPath = folderPath.resolve(FILE_OUT_EXIST);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(TEST_STRING_3, REDIR_INPUT, inputPath.toString(), TEST_STRING_1, REDIR_OUTPUT, outputPath.toString(), TEST_STRING_2), System.in, System.out, argRes);
        when(argRes.resolveOneArgument(inputPath.toString())).thenReturn(Arrays.asList(inputPath.toString()));
        when(argRes.resolveOneArgument(outputPath.toString())).thenReturn(Arrays.asList(outputPath.toString()));
        ioRedirHandler.extractRedirOptions();
        assertEquals(Arrays.asList(TEST_STRING_3, TEST_STRING_1, TEST_STRING_2), ioRedirHandler.getNoRedirArgsList());
        assertFileInputStream(inputPath, ioRedirHandler.getInputStream());
        assertFileOutputStream(outputPath, ioRedirHandler.getOutputStream());
    }

    // only io args, only file in non-existent directory input redirection
    @Test
    public void extractRedirOptions_onlyIOArgsOnlyInputFileInNonExistDir_throwsShellException() throws FileNotFoundException, AbstractApplicationException, ShellException {
        Path testPath = folderPath.resolve(NON_EXIST_DIR).resolve(FILE_IN_1_EXT);
        when(argRes.resolveOneArgument(testPath.toString())).thenReturn(Arrays.asList(testPath.toString()));
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_INPUT, testPath.toString()), System.in, System.out, argRes);
        Exception exception = assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());
        assertEquals(SHELL_EXCEP + testPath + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // only io args, only non-existent file input redirection
    @Test
    public void extractRedirOptions_onlyIOArgsOnlyInputNonExistFile_throwsShellException() throws FileNotFoundException, AbstractApplicationException, ShellException {
        Path testPath = folderPath.resolve(NON_EXIST_FILE);
        when(argRes.resolveOneArgument(testPath.toString())).thenReturn(Arrays.asList(testPath.toString()));
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_INPUT, testPath.toString()), System.in, System.out, argRes);
        Exception exception = assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());
        assertEquals(SHELL_EXCEP + testPath + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // only io args, multiple input redirection. "< 1.txt < 2"
    @Test
    public void extractRedirOptions_onlyIOArgsMultiInputRedirMultiFiles_throwsShellException() throws FileNotFoundException, AbstractApplicationException, ShellException {
        Path testPath1 = folderPath.resolve(FILE_IN_1_EXT), testPath2 = folderPath.resolve(FILE_IN_2);
        when(argRes.resolveOneArgument(testPath1.toString())).thenReturn(Arrays.asList(testPath1.toString()));
        when(argRes.resolveOneArgument(testPath2.toString())).thenReturn(Arrays.asList(testPath2.toString()));
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_INPUT, testPath1.toString(), REDIR_INPUT, testPath2.toString()), System.in, System.out, argRes);
        Exception exception = assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());
        assertEquals(SHELL_EXCEP + ERR_MULTIPLE_STREAMS, exception.getMessage());
    }

    // only io args, multiple input redirection. "< < 1.txt"
    @Test
    public void extractRedirOptions_onlyIOArgsMultiInputRedirSingleFileSecond_throwsShellException() {
        Path testPath = folderPath.resolve(FILE_IN_1_EXT);
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_INPUT, REDIR_INPUT, testPath.toString()), System.in, System.out, argRes);
        Exception exception = assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());
        assertEquals(SHELL_EXCEP + ERR_SYNTAX, exception.getMessage());
    }

    // only io args, multiple input redirection. "< o.txt <"
    @Test
    public void extractRedirOptions_onlyIOArgsMultiInputRedirSingleFileFirst_throwsShellException() throws FileNotFoundException, AbstractApplicationException, ShellException {
        Path testPath = folderPath.resolve(FILE_IN_SYMBOL);
        when(argRes.resolveOneArgument(testPath.toString())).thenReturn(Arrays.asList(testPath.toString()));
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_INPUT, testPath.toString(), REDIR_INPUT), System.in, System.out, argRes);
        Exception exception = assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());
        assertEquals(SHELL_EXCEP + ERR_SYNTAX, exception.getMessage());
    }

    // valid io args, null inputstream
    @Test
    public void extractRedirOptions_validIOArgsNullInputStream_throwsShellException() throws FileNotFoundException, AbstractApplicationException, ShellException {
        Path inputPath = folderPath.resolve(FILE_IN_1_EXT), outputPath = folderPath.resolve(FILE_OUT_EXIST);
        when(argRes.resolveOneArgument(inputPath.toString())).thenReturn(Arrays.asList(inputPath.toString()));
        when(argRes.resolveOneArgument(outputPath.toString())).thenReturn(Arrays.asList(outputPath.toString()));
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_INPUT, inputPath.toString(), REDIR_OUTPUT, outputPath.toString()), null, System.out, argRes);
        Exception exception = assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());
        assertEquals(SHELL_EXCEP + ERR_NULL_STREAMS, exception.getMessage());
    }

    // valid io args, null outputstream
    @Test
    public void extractRedirOptions_validIOArgsNullOutputStream_throwsShellException() throws FileNotFoundException, AbstractApplicationException, ShellException {
        Path inputPath = folderPath.resolve(FILE_IN_1_EXT), outputPath = folderPath.resolve(FILE_OUT_EXIST);
        when(argRes.resolveOneArgument(inputPath.toString())).thenReturn(Arrays.asList(inputPath.toString()));
        when(argRes.resolveOneArgument(outputPath.toString())).thenReturn(Arrays.asList(outputPath.toString()));
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_INPUT, inputPath.toString(), REDIR_OUTPUT, outputPath.toString()), System.in, null, argRes);
        Exception exception = assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());
        assertEquals(SHELL_EXCEP + ERR_NULL_STREAMS, exception.getMessage());
    }

    // valid io args, valid streams, argument resolve multiple files (input)
    @Test
    public void extractRedirOptions_validIOValidStreamMultiFileInput_throwsShellException() throws FileNotFoundException, AbstractApplicationException, ShellException {
        Path inputPath = folderPath.resolve(FILE_IN_1_EXT);
        when(argRes.resolveOneArgument(inputPath.toString())).thenReturn(Arrays.asList(inputPath.toString(), inputPath.toString())); // argument resolution probably will not produce something like this with the provided input but just for testing.
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_INPUT, inputPath.toString()), System.in, System.out, argRes);
        Exception exception = assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());
        assertEquals(SHELL_EXCEP + ERR_SYNTAX, exception.getMessage());
    }

    // valid io args, valid streams, argument resolve multiple files (output)
    @Test
    public void extractRedirOptions_validIOValidStreamMultiFileOutput_throwsShellException() throws FileNotFoundException, AbstractApplicationException, ShellException {
        Path outputPath = folderPath.resolve(FILE_OUT_EXIST);
        when(argRes.resolveOneArgument(outputPath.toString())).thenReturn(Arrays.asList(outputPath.toString(), outputPath.toString())); // argument resolution probably will not produce something like this with the provided input but just for testing.
        IORedirectionHandler ioRedirHandler = new IORedirectionHandler(Arrays.asList(REDIR_OUTPUT, outputPath.toString()), System.in, System.out, argRes);
        Exception exception = assertThrows(ShellException.class, () -> ioRedirHandler.extractRedirOptions());
        assertEquals(SHELL_EXCEP + ERR_SYNTAX, exception.getMessage());
    }
}