package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_DIR_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.IOAssertUtils.*;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.*;
import static org.mockito.Mockito.*;

@SuppressWarnings({"PMD.CloseResource"}) // Reason - Have already tried closing all opened resources.
class IOUtilsTest {
    private final static String NORM_NEWLINE = "\n";
    private final static String FILE_EXT = "file123.txt";
    private final static String FILE_NO_EXT = "file123";
    private final static String FILE_SPACE = "file 123.txt";
    private final static String FILE_NE = "ne.txt";
    private final static String FILE_OUT = "o";
    private final static String FILE_EXT_OUT = "o.txt";
    private final static String FILE_SPACE_OUT = "out put.txt";
    private final static String FILE_NE_OUT = "neo.txt";
    private final static String FILE_EMPTY = "empty.txt";
    private final static String FILE_SINGLE = "single.txt";
    private final static String FILE_NO_PERM = "noperm.txt";
    private final static String NEST_DIR = "nest";
    private final static String NEST_NE_DIR = "ne";
    private final static String EMPTY = "";
    private final static String SINGLE_LINE = "testing single li123ne cont!@#ent";
    private final static String MULTI_LINE = "testing\nmultiple l123%!line co\ncontent!@#31 21dsa\n\ntesting";
    private final static List<String> MULTI_LINE_LIST = Arrays.asList("testing", "multiple l123%!line co", "content!@#31 21dsa", "", "testing");
    private final static String SHELL_EXCEP = "shell: ";
    @TempDir
    public static Path folderPath;
    private static Path fileExtPath;
    private static Path fileNoExtPath;
    private static Path fileSpacePath;
    private static Path fileNePath;
    private static Path fileOutPath;
    private static Path fileOutExtPath;
    private static Path fileSpaceOutPath;
    private static Path fileNeOutPath;
    private static Path fileEmptyPath;
    private static Path fileSinglePath;
    private static Path fileDirPath;
    private static Path fileInDirPath;
    private static Path fileOutDirPath;
    private static Path fileNoPermPath;
    private static Path fileNeDirPath;

    @BeforeAll
    public static void setUp() throws IOException {
        fileExtPath = folderPath.resolve(FILE_EXT);
        fileNoExtPath = folderPath.resolve(FILE_NO_EXT);
        fileSpacePath = folderPath.resolve(FILE_SPACE);
        fileNePath = folderPath.resolve(FILE_NE);
        fileOutPath = folderPath.resolve(FILE_OUT);
        fileOutExtPath = folderPath.resolve(FILE_EXT_OUT);
        fileSpaceOutPath = folderPath.resolve(FILE_SPACE_OUT);
        fileNeOutPath = folderPath.resolve(FILE_NE_OUT);
        fileEmptyPath = folderPath.resolve(FILE_EMPTY);
        fileSinglePath = folderPath.resolve(FILE_SINGLE);
        fileDirPath = folderPath.resolve(NEST_DIR);
        fileInDirPath = folderPath.resolve(NEST_DIR).resolve(FILE_EXT);
        fileOutDirPath = folderPath.resolve(NEST_DIR).resolve(FILE_OUT);
        fileNoPermPath = folderPath.resolve(FILE_NO_PERM);
        fileNeDirPath = folderPath.resolve(NEST_NE_DIR).resolve(FILE_EXT);

        Files.createDirectories(fileDirPath);
        // ./file123.txt
        Files.write(fileExtPath, MULTI_LINE.getBytes());
        // ./file123
        Files.write(fileNoExtPath, MULTI_LINE.getBytes());
        // ./file 123.txt
        Files.write(fileSpacePath, MULTI_LINE.getBytes());
        // ./o
        Files.write(fileOutPath, MULTI_LINE.getBytes());
        // ./o
        Files.write(fileOutExtPath, MULTI_LINE.getBytes());
        // ./out put.txt
        Files.write(fileSpaceOutPath, MULTI_LINE.getBytes());
        // ./empty.txt
        Files.createFile(fileEmptyPath);
        // ./single.txt
        Files.write(fileSinglePath, SINGLE_LINE.getBytes());
        // ./nest/file123.txt
        Files.write(fileInDirPath, MULTI_LINE.getBytes());
        // ./nest/o.txt
        Files.write(fileOutPath, MULTI_LINE.getBytes());

        // ./noperm.txt
        Files.write(fileNoPermPath, MULTI_LINE.getBytes());
        removeFilePermissions(fileNoPermPath);
    }

    @AfterAll
    public static void tearDown() {
        resetFilePermissions(fileNoPermPath);
    }

    // opens inputstream of filename with ext
    @Test
    public void openInputStream_fileExt_openStream() throws ShellException, IOException {
        InputStream actualStream = IOUtils.openInputStream(fileExtPath.toString());
        assertFileInputStream(fileExtPath, actualStream);
        actualStream.close();
    }

    // opens inputstream of filename without ext
    @Test
    public void openInputStream_fileNoExt_openStream() throws ShellException, IOException {
        InputStream actualStream = IOUtils.openInputStream(fileNoExtPath.toString());
        assertFileInputStream(fileNoExtPath, actualStream);
        actualStream.close();
    }

    // opens inputstream of filename with space
    @Test
    public void openInputStream_fileSpace_openStream() throws ShellException, IOException {
        InputStream actualStream = IOUtils.openInputStream(fileSpacePath.toString());
        assertFileInputStream(fileSpacePath, actualStream);
        actualStream.close();
    }

    // opens inputstream of file with single line
    @Test
    public void openInputStream_fileSingleLine_openStream() throws ShellException, IOException {
        InputStream actualStream = IOUtils.openInputStream(fileSpacePath.toString());
        assertFileInputStream(fileSpacePath, actualStream);
        actualStream.close();
    }

    // opens inputstream of empty file
    @Test
    public void openInputStream_emptyFile_openStream() throws ShellException, IOException {
        InputStream actualStream = IOUtils.openInputStream(fileEmptyPath.toString());
        assertFileInputStream(fileEmptyPath, actualStream);
        actualStream.close();
    }

    // opens inputstream of file in directory
    @Test
    public void openInputStream_fileDir_openStream() throws ShellException, IOException {
        InputStream actualStream = IOUtils.openInputStream(fileInDirPath.toString());
        assertFileInputStream(fileInDirPath, actualStream);
        actualStream.close();
    }

    // opens inputstream of non-existent file
    @Test
    public void openInputStream_nonExistFile_throwsShellException() {
        Exception exception = assertThrows(ShellException.class, () -> IOUtils.openInputStream(fileNePath.toString()));
        assertEquals(SHELL_EXCEP + fileNePath + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // opens inputstream of directory
    @Test
    public void openInputStream_dir_throwsShellException() {
        Exception exception = assertThrows(ShellException.class, () -> IOUtils.openInputStream(fileDirPath.toString()));
        assertEquals(SHELL_EXCEP + fileDirPath + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // opens inputstream of non-existent directory
    @Test
    public void openInputStream_nonExistDir_throwsShellException() {
        Exception exception = assertThrows(ShellException.class, () -> IOUtils.openInputStream(fileNeDirPath.toString()));
        assertEquals(SHELL_EXCEP + fileNeDirPath + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // opens output stream of filename with ext
    @Test
    public void openOutputStream_fileExt_openStream() throws IOException, ShellException {
        OutputStream actualStream = IOUtils.openOutputStream(fileOutExtPath.toString());
        assertFileOutputStream(fileOutExtPath, actualStream);
        actualStream.close();
    }

    // opens output stream of filename without ext
    @Test
    public void openOutputStream_fileNoExt_openStream() throws IOException, ShellException {
        OutputStream actualStream = IOUtils.openOutputStream(fileOutPath.toString());
        assertFileOutputStream(fileOutPath, actualStream);
        actualStream.close();
    }

    // opens output stream of filename with space
    @Test
    public void openOutputStream_fileSpace_openStream() throws IOException, ShellException {
        OutputStream actualStream = IOUtils.openOutputStream(fileSpaceOutPath.toString());
        assertFileOutputStream(fileSpaceOutPath, actualStream);
        actualStream.close();
    }

    // opens output stream of non-existent file
    @Test
    public void openOutputStream_nonExistFile_openStream() throws IOException, ShellException {
        OutputStream actualStream = IOUtils.openOutputStream(fileNeOutPath.toString());
        assertFileOutputStream(fileNeOutPath, actualStream);
        deleteFileIfExists(fileNeOutPath);
        actualStream.close();
    }

    // opens output stream of file in directory
    @Test
    public void openOutputStream_fileDir_openStream() throws IOException, ShellException {
        OutputStream actualStream = IOUtils.openOutputStream(fileOutDirPath.toString());
        assertFileOutputStream(fileOutDirPath, actualStream);
        actualStream.close();
    }

    // opens output stream of directory
    @Test
    public void openOutputStream_dir_throwsShellException() {
        Exception exception = assertThrows(Exception.class, () -> IOUtils.openOutputStream(fileDirPath.toString()));
        assertEquals(SHELL_EXCEP + fileDirPath + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // opens output stream of non-exist directory
    @Test
    public void openOutputStream_nonExistDir_throwsShellException() {
        Exception exception = assertThrows(Exception.class, () -> IOUtils.openOutputStream(fileNeDirPath.toString()));
        assertEquals(SHELL_EXCEP + fileNeDirPath + ": " + ERR_DIR_NOT_FOUND, exception.getMessage());
    }

    @Test
    public void getLinesFromInputStream_multiLineFile_multiStringList() throws IOException {
        FileInputStream testInputStream = new FileInputStream(fileExtPath.toFile());
        List<String> actualList = IOUtils.getLinesFromInputStream(testInputStream);
        assertEquals(MULTI_LINE_LIST, actualList);
        testInputStream.close();
    }

    @Test
    public void getLinesFromInputStream_singleLineFile_singleStringList() throws IOException {
        FileInputStream testInputStream = new FileInputStream(fileSinglePath.toFile());
        List<String> actualList = IOUtils.getLinesFromInputStream(testInputStream);
        assertEquals(Arrays.asList(SINGLE_LINE), actualList);
        testInputStream.close();
    }

    @Test
    public void getLinesFromInputStream_emptyFile_emptyList() throws IOException {
        FileInputStream testInputStream = new FileInputStream(fileEmptyPath.toFile());
        List<String> actualList = IOUtils.getLinesFromInputStream(testInputStream);
        assertEquals(Arrays.asList(), actualList);
        testInputStream.close();
    }

    @Test
    public void closeInputStream_mockInputStream_inputStreamClose() throws ShellException, IOException {
        InputStream testInputStream = spy(InputStream.class);
        IOUtils.closeInputStream(testInputStream);
        verify(testInputStream).close();
        testInputStream.close();
    }

    @Test
    public void closeOutputStream_mockOutputStream_outputStreamClose() throws ShellException, IOException {
        OutputStream testOStream = spy(OutputStream.class);
        IOUtils.closeOutputStream(testOStream);
        verify(testOStream).close();
        testOStream.close();
    }

    @Test
    public void outputCurrentResults_multiLine_outputMultiLine() throws Exception {
        ByteArrayOutputStream testOutputStream = new ByteArrayOutputStream();
        IOUtils.outputCurrentResults(MULTI_LINE, testOutputStream);
        assertEquals(MULTI_LINE + NORM_NEWLINE, testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
        testOutputStream.close();
    }

    @Test
    public void outputCurrentResults_singleLine_outputSingleLine() throws Exception {
        ByteArrayOutputStream testOutputStream = new ByteArrayOutputStream();
        IOUtils.outputCurrentResults(SINGLE_LINE, testOutputStream);
        assertEquals(SINGLE_LINE + NORM_NEWLINE, testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
        testOutputStream.close();
    }

    @Test
    public void outputCurrentResults_empty_outputNewLine() throws Exception {
        ByteArrayOutputStream testOutputStream = new ByteArrayOutputStream();
        IOUtils.outputCurrentResults(EMPTY, testOutputStream);
        assertEquals(NORM_NEWLINE, testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
        testOutputStream.close();
    }
}