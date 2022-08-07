package sg.edu.nus.comp.cs4218.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.SortException;
import sg.edu.nus.comp.cs4218.impl.app.SortApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class SortApplicationIT {
    private final static String N_FLAG = "-n";
    private final static String R_FLAG = "-r";
    private final static String F_FLAG = "-f";
    private final static String FILE_EXT = "1.txt";
    private final static String FILE_EXT_2 = "2.txt";
    private final static String FILE_NO_EXT = "2";
    private final static String FILE_SYMBOL = "%";
    private final static String FILE_SPACED = "spa ced in file.txt";
    private final static String FILE_SINGLE = "single.txt";
    private final static String FILE_EMPTY = "empty.txt";
    private final static String FILE_NEWLINE = "newline.txt";
    private final static String NON_EXIST_FILE = "ne.txt";
    private final static String NEST_DIR_1 = "nest";
    private final static String NEST_DIR_2 = "nested";
    private final static String NON_EXIST_DIR = "nedir";
    private final static String NORM_NEWLINE = "\n";
    private final static String EMPTY_CONTENT = "";
    private final static String SINGLE_LINE_CONT = "single line";
    private final static String MULTI_LINE_CONT = "\n54321\n\n  \n  spaced\na123!@#random\n!!!\nb\nAAA 123random\nBBB\n@@\n1123\n1\n55";
    private final static String MULTI_LINE_CONT_2 = "even more\nand more\nOR no more\n?\n765";
    private final static String SORT_NFR_MULTI = "BBB\nb\nAAA 123random\na123!@#random\n54321\n1123\n55\n1\n@@\n!!!\n  spaced\n  \n\n";
    private final static String SORT_EXCEP = "sort: ";
    @TempDir
    public static Path folderPath;
    private static Path fileExtPath;
    private static Path fileExt2Path;
    private static Path fileNoExtPath;
    private static Path fileSymbolPath;
    private static Path fileSpacedPath;
    private static Path fileSinglePath;
    private static Path fileEmptyPath;
    private static Path fileNewLinePath;
    private static Path fileExtDirPath;
    private static Path fileExtDirDirPath;
    private static InputStream testStdin;
    private static SortApplication sortApp;
    private static ByteArrayOutputStream testOutputStream;

    @BeforeAll
    public static void setUpAll() throws IOException {
        fileExtPath = folderPath.resolve(FILE_EXT);
        fileExt2Path = folderPath.resolve(FILE_EXT_2);
        fileNoExtPath = folderPath.resolve(FILE_NO_EXT);
        fileSymbolPath = folderPath.resolve(FILE_SYMBOL);
        fileSpacedPath = folderPath.resolve(FILE_SPACED);
        fileSinglePath = folderPath.resolve(FILE_SINGLE);
        fileEmptyPath = folderPath.resolve(FILE_EMPTY);
        fileNewLinePath = folderPath.resolve(FILE_NEWLINE);
        fileExtDirPath = folderPath.resolve(NEST_DIR_1).resolve(FILE_EXT);
        fileExtDirDirPath = folderPath.resolve(NEST_DIR_1).resolve(NEST_DIR_2).resolve(FILE_EXT);
        // ./nest/nested
        Files.createDirectories(folderPath.resolve(NEST_DIR_1).resolve(NEST_DIR_2));
        // ./1.txt
        Files.write(fileExtPath, MULTI_LINE_CONT.getBytes());
        // ./2.txt
        Files.write(fileExt2Path, MULTI_LINE_CONT_2.getBytes());
        // ./2
        Files.write(fileNoExtPath, MULTI_LINE_CONT.getBytes());
        // ./%
        Files.write(fileSymbolPath, MULTI_LINE_CONT.getBytes());
        // ./'spa ced in file.txt'
        Files.write(fileSpacedPath, MULTI_LINE_CONT.getBytes());
        // ./single.txt
        Files.write(fileSinglePath, SINGLE_LINE_CONT.getBytes());
        // ./empty.txt
        Files.createFile(fileEmptyPath);
        // ./newline.txt
        Files.write(fileNewLinePath, STRING_NEWLINE.getBytes());
        // ./nest/1.txt
        Files.write(fileExtDirPath, MULTI_LINE_CONT.getBytes());
        // ./nest/nested/1.txt
        Files.write(fileExtDirDirPath, MULTI_LINE_CONT.getBytes());
    }

    @BeforeEach
    public void setUpEach() {
        sortApp = new SortApplication();
        testOutputStream = new ByteArrayOutputStream();
    }

    @AfterEach
    public void reset() throws IOException {
        if (testStdin != null) {
            testStdin.close();
        }
    }

    // Stdin: empty, File 1: not given, File 2: not given
    @Test
    public void run_FFFEmptyNilNil_FFFEmptyOutput() throws SortException {
        String[] args = {};
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(EMPTY_CONTENT, testOutputStream.toString());
    }

    // Stdin: empty, File 1: empty, File 2: empty
    @Test
    public void run_FFFEmptyEmptyEmpty_FFFEmptyOutput() throws SortException {
        String[] args = {"-", fileEmptyPath.toString(), fileEmptyPath.toString()};
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(EMPTY_CONTENT, testOutputStream.toString());
    }

    // Stdin: empty, File 1: single line, File 2: single line
    @Test
    public void run_FFFEmptySingleSingle_FFFSortedOutput() throws SortException {
        String[] args = {"-", fileSinglePath.toString(), fileSinglePath.toString()};
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(SINGLE_LINE_CONT + "\n" + SINGLE_LINE_CONT + "\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: empty, File 1: multiple lines, File 2: multiple lines
    @Test
    public void run_FFTEmptyMultiMulti_FFTSortedOutput() throws SortException {
        String[] args = {F_FLAG, "-", fileExtPath.toString(), fileExtPath.toString()};
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("\n\n\n\n  \n  \n  spaced\n  spaced\n!!!\n!!!\n@@\n@@\n1\n1\n1123\n1123\n54321\n54321\n55\n55\na123!@#random\na123!@#random\nAAA 123random\nAAA 123random\nb\nb\nBBB\nBBB\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: single line, File 1: empty, File 2: single line
    @Test
    public void run_FFTSingleEmptySingle_FFTSortedOutput() throws SortException {
        String[] args = {F_FLAG, fileEmptyPath.toString(), fileSinglePath.toString(), "-"};
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(SINGLE_LINE_CONT + "\n" + SINGLE_LINE_CONT + "\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: single line, File 1: single line, File 2: multiple line
    @Test
    public void run_FTFSingleSingleMulti_FTFSortedOutput() throws SortException {
        String[] args = {R_FLAG, fileSinglePath.toString(), "-", fileExtPath.toString()};
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("single line\nsingle line\nb\na123!@#random\nBBB\nAAA 123random\n55\n54321\n1123\n1\n@@\n!!!\n  spaced\n  \n\n\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: single line, File 1: single line, File 2: multiple line, multiple "-"
    @Test
    public void run_FTFSingleSingleMultiManyDash_FTFSortedOutput() throws SortException {
        String[] args = {R_FLAG, "-", fileSinglePath.toString(), "-", "-", fileExtPath.toString()};
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("single line\nsingle line\nb\na123!@#random\nBBB\nAAA 123random\n55\n54321\n1123\n1\n@@\n!!!\n  spaced\n  \n\n\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: single line, File 1: multiple lines, File 2: not given
    @Test
    public void run_FTFSingleMultiNil_FTFSortedOutput() throws SortException {
        String[] args = {R_FLAG, fileExtPath.toString(), "-"};
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("single line\nb\na123!@#random\nBBB\nAAA 123random\n55\n54321\n1123\n1\n@@\n!!!\n  spaced\n  \n\n\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: single line, File 1: not given, File 2: empty
    @Test
    public void run_FTFSingleNilEmpty_FTFSortedOutput() throws SortException {
        String[] args = {fileEmptyPath.toString(), "-", R_FLAG};
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(SINGLE_LINE_CONT + "\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: multiple lines, File 1: single line, File 2: not given
    @Test
    public void run_TTFMultiSingleNil_TTFSortedOutput() throws SortException {
        String[] args = {R_FLAG, fileSinglePath.toString(), "-", N_FLAG};
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("single line\nb\na123!@#random\nBBB\nAAA 123random\n54321\n1123\n55\n1\n@@\n!!!\n  spaced\n  \n\n\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: multiple lines, File 1: multiple lines, File 2: empty
    @Test
    public void run_TFTMultiMultiEmpty_TFTSortedOutput() throws SortException {
        String[] args = {N_FLAG, F_FLAG, fileExt2Path.toString(), fileEmptyPath.toString(), "-"};
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("\n\n  \n  spaced\n!!!\n?\n@@\n1\n55\n765\n1123\n54321\na123!@#random\nAAA 123random\nand more\nb\nBBB\neven more\nOR no more\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: multiple lines, File 1: not given, File 2: single
    @Test
    public void run_TTFMultiNilSingle_TTFSortedOutput() throws SortException {
        String[] args = {"-", R_FLAG, N_FLAG, fileSinglePath.toString()};
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("single line\nb\na123!@#random\nBBB\nAAA 123random\n54321\n1123\n55\n1\n@@\n!!!\n  spaced\n  \n\n\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: multiple lines, File 1: empty, File 2: multiple lines
    @Test
    public void run_FFTMultiEmptyMulti_FFTSortedOutput() throws SortException {
        String[] args = {"-", fileEmptyPath.toString(), F_FLAG, fileExtPath.toString()};
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("\n\n\n\n  \n  \n  spaced\n  spaced\n!!!\n!!!\n@@\n@@\n1\n1\n1123\n1123\n54321\n54321\n55\n55\na123!@#random\na123!@#random\nAAA 123random\nAAA 123random\nb\nb\nBBB\nBBB\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: empty, File 1: multiple lines, File 2: single line
    @Test
    public void run_FTTEmptyMultiSingle_FTTSortedOutput() throws SortException {
        String[] args = {fileExtPath.toString(), R_FLAG, fileSinglePath.toString(), F_FLAG};
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("single line\nBBB\nb\nAAA 123random\na123!@#random\n55\n54321\n1123\n1\n@@\n!!!\n  spaced\n  \n\n\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: empty, File 1: not given, File 2: multiple lines
    @Test
    public void run_TTTEmptyNilMulti_TTTSortedOutput() throws SortException {
        String[] args = {F_FLAG, R_FLAG, fileExtPath.toString(), N_FLAG};
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(SORT_NFR_MULTI + "\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: empty, File 1: empty, File 2: not given
    @Test
    public void run_TTTEmptyEmptyNil_TTTSortedOutput() throws SortException {
        String[] args = {N_FLAG, R_FLAG, F_FLAG, fileEmptyPath.toString()};
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(EMPTY_CONTENT, testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: empty, File 1: single line, File 2: empty
    @Test
    public void run_TTTEmptySingleEmpty_TTTSortedOutput() throws SortException {
        String[] args = {N_FLAG, R_FLAG, F_FLAG, fileSinglePath.toString(), fileEmptyPath.toString()};
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(SINGLE_LINE_CONT + "\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: empty, File 1: new line, File 2: empty
    @Test
    public void run_TTTEmptyNewLineEmpty_TTTSortedOutput() throws SortException {
        String[] args = {N_FLAG, R_FLAG, F_FLAG, "-", fileNewLinePath.toString(), fileEmptyPath.toString()};
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
    }

    // Stdin: empty, File 1: new line, File 2: not given
    @Test
    public void run_TTTEmptyNewLineNil_TTTSortedOutput() throws SortException {
        String[] args = {N_FLAG, F_FLAG, R_FLAG, "-", fileNewLinePath.toString()};
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
    }

    // Stdin: new line, File 1: not given, File 2: not given
    @Test
    public void run_TTTNewLineNilNil_TTTSortedOutput() throws SortException {
        String[] args = {N_FLAG, R_FLAG, F_FLAG};
        testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
    }

    // Stdin: single line, File 1: file in directory, File 2: multiple lines
    @Test
    public void run_TTTSingleFileDirMulti_TTTSortedOutput() throws SortException {
        String[] args = {"-", folderPath.resolve(NEST_DIR_1).resolve(NEST_DIR_2).resolve(FILE_EXT).toString(), N_FLAG, R_FLAG, F_FLAG, fileExtPath.toString()};
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("single line\nBBB\nBBB\nb\nb\nAAA 123random\nAAA 123random\na123!@#random\na123!@#random\n54321\n54321\n1123\n1123\n55\n55\n1\n1\n@@\n@@\n!!!\n!!!\n  spaced\n  spaced\n  \n  \n\n\n\n\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: single line, File 1: multiple lines, File 2: file in directory
    @Test
    public void run_TTTSingleMultiFileDir_TTTSortedOutput() throws SortException {
        String[] args = {F_FLAG, R_FLAG, N_FLAG, "-", fileExtPath.toString(), folderPath.resolve(NEST_DIR_1).resolve(NEST_DIR_2).resolve(FILE_EXT).toString()};
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());
        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("single line\nBBB\nBBB\nb\nb\nAAA 123random\nAAA 123random\na123!@#random\na123!@#random\n54321\n54321\n1123\n1123\n55\n55\n1\n1\n@@\n@@\n!!!\n!!!\n  spaced\n  spaced\n  \n  \n\n\n\n\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: null, File 1: single line, File 2: empty
    @Test
    public void run_TTTNullSingleEmpty_throwsSortException() {
        String[] args = {N_FLAG, R_FLAG, F_FLAG, "-", fileSinglePath.toString(), fileEmptyPath.toString()};
        Exception exception = assertThrows(SortException.class, () -> sortApp.run(args, null, testOutputStream));
        assertEquals(SORT_EXCEP + ERR_NULL_STREAMS, exception.getMessage());
    }

    // Stdin: empty, File 1: directory, File 2: multiple lines
    @Test
    public void run_TTTEmptyDirMulti_throwsSortException() {
        String[] args = {N_FLAG, R_FLAG, F_FLAG, "-", folderPath.resolve(NEST_DIR_1).toString(), fileExtPath.toString()};
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());
        Exception exception = assertThrows(SortException.class, () -> sortApp.run(args, testStdin, testOutputStream));
        assertEquals(SORT_EXCEP + folderPath.resolve(NEST_DIR_1) + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // Stdin: multiple lines, File 1: single, File 2: directory
    @Test
    public void run_TTTEmptySingleDir_throwsSortException() {
        String[] args = {N_FLAG, R_FLAG, F_FLAG, "-", fileSinglePath.toString(), folderPath.resolve(NEST_DIR_1).toString()};
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(SortException.class, () -> sortApp.run(args, testStdin, testOutputStream));
        assertEquals(SORT_EXCEP + folderPath.resolve(NEST_DIR_1) + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // Stdin: empty, File 1: non-existent directory, File 2: multiple lines
    @Test
    public void run_TTTEmptyNonExistDirMulti_throwsSortException() {
        String[] args = {N_FLAG, R_FLAG, F_FLAG, "-", folderPath.resolve(NON_EXIST_DIR).resolve(FILE_EXT).toString(), fileExtPath.toString()};
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());
        Exception exception = assertThrows(SortException.class, () -> sortApp.run(args, testStdin, testOutputStream));
        assertEquals(SORT_EXCEP + folderPath.resolve(NON_EXIST_DIR).resolve(FILE_EXT) + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // Stdin: multiple lines, File 1: single, File 2: non-existent directory
    @Test
    public void run_TTTEmptySingleNonExistDir_throwsSortException() {
        String[] args = {N_FLAG, R_FLAG, F_FLAG, "-", fileSinglePath.toString(), folderPath.resolve(NON_EXIST_DIR).resolve(FILE_EXT).toString()};
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(SortException.class, () -> sortApp.run(args, testStdin, testOutputStream));
        assertEquals(SORT_EXCEP + folderPath.resolve(NON_EXIST_DIR).resolve(FILE_EXT) + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // Stdin: empty, File 1: non-existent file, File 2: multiple lines
    @Test
    public void run_TTTEmptyNonExistFileMulti_throwsSortException() {
        String[] args = {N_FLAG, R_FLAG, F_FLAG, "-", folderPath.resolve(NON_EXIST_FILE).toString(), fileExtPath.toString()};
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());
        Exception exception = assertThrows(SortException.class, () -> sortApp.run(args, testStdin, testOutputStream));
        assertEquals(SORT_EXCEP + folderPath.resolve(NON_EXIST_FILE) + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // Stdin: multiple lines, File 1: single, File 2: non-existent file
    @Test
    public void run_TTTEmptySingleNonExistFile_throwsSortException() {
        String[] args = {N_FLAG, R_FLAG, F_FLAG, "-", fileSinglePath.toString(), folderPath.resolve(NON_EXIST_FILE).toString()};
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(SortException.class, () -> sortApp.run(args, testStdin, testOutputStream));
        assertEquals(SORT_EXCEP + folderPath.resolve(NON_EXIST_FILE) + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }
}
