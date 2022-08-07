package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.SortException;
import sg.edu.nus.comp.cs4218.impl.parser.SortArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

class SortApplicationTest {
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
    private final static String SORT_MULTI = "\n\n  \n  spaced\n!!!\n@@\n1\n1123\n54321\n55\nAAA 123random\nBBB\na123!@#random\nb";
    private final static String SORT_N_MULTI = "\n\n  \n  spaced\n!!!\n@@\n1\n55\n1123\n54321\nAAA 123random\nBBB\na123!@#random\nb";
    private final static String SORT_F_MULTI = "\n\n  \n  spaced\n!!!\n@@\n1\n1123\n54321\n55\na123!@#random\nAAA 123random\nb\nBBB";
    private final static String SORT_R_MULTI = "b\na123!@#random\nBBB\nAAA 123random\n55\n54321\n1123\n1\n@@\n!!!\n  spaced\n  \n\n";
    private final static String SORT_NF_MULTI = "\n\n  \n  spaced\n!!!\n@@\n1\n55\n1123\n54321\na123!@#random\nAAA 123random\nb\nBBB";
    private final static String SORT_NR_MULTI = "b\na123!@#random\nBBB\nAAA 123random\n54321\n1123\n55\n1\n@@\n!!!\n  spaced\n  \n\n";
    private final static String SORT_FR_MULTI = "BBB\nb\nAAA 123random\na123!@#random\n55\n54321\n1123\n1\n@@\n!!!\n  spaced\n  \n\n";
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
    private static SortArgsParser sortParser;
    private static ByteArrayOutputStream testOutputStream;

    @BeforeAll
    public static void setUp() throws IOException {
        sortApp = new SortApplication();
        sortParser = mock(SortArgsParser.class);
        sortApp.setSortParser(sortParser);

        testOutputStream = new ByteArrayOutputStream();

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

    @AfterEach
    public void reset() throws IOException {
        sortApp = new SortApplication();
        sortParser = mock(SortArgsParser.class);
        sortApp.setSortParser(sortParser);

        testOutputStream.reset();
        if (testStdin != null) {
            testStdin.close();
        }
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: False, single existing file multiple lines
    @Test
    public void sortFromFiles_FFFSingleExistFileMultiLine_sortedFFFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, false, false, fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_MULTI, sortedString);
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: False, single existing file no ext multiple lines
    @Test
    public void sortFromFiles_FFFSingleExistFileNoExtMultiLine_sortedFFFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, false, false, fileNoExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_MULTI, sortedString);
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: False, single existing file symbol multiple lines
    @Test
    public void sortFromFiles_FFFSingleExistFileSymbolMultiLine_sortedFFFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, false, false, fileSymbolPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_MULTI, sortedString);
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: False, single existing file spaced multiple lines
    @Test
    public void sortFromFiles_FFFSingleExistFileSpacedMultiLine_sortedFFFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, false, false, fileSpacedPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_MULTI, sortedString);
    }


    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: False, single non-existing file
    @Test
    public void sortFromFiles_FFFSingleNonExistFile_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> sortApp.sortFromFiles(false, false, false, folderPath.resolve(NON_EXIST_FILE).toString()));
        assertEquals(folderPath.resolve(NON_EXIST_FILE) + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: False, directory
    @Test
    public void sortFromFiles_FFFDirectory_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> sortApp.sortFromFiles(false, false, false, folderPath.resolve(NEST_DIR_1).toString()));
        assertEquals(folderPath.resolve(NEST_DIR_1) + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: False, non-existing directory
    @Test
    public void sortFromFiles_FFFNonExistDirectory_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> sortApp.sortFromFiles(false, false, false, folderPath.resolve(NON_EXIST_DIR).resolve(FILE_EXT).toString()));
        assertEquals(folderPath.resolve(NON_EXIST_DIR).resolve(FILE_EXT) + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: False, single existing file in directory
    @Test
    public void sortFromFiles_FFFSingleExistFileInDir_sortedFFFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, false, false, fileExtDirPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_MULTI, sortedString);
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: False, single existing file in nested directory
    @Test
    public void sortFromFiles_FFFSingleExistFileInNestDir_sortedFFFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, false, false, fileExtDirDirPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_MULTI, sortedString);
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: False, single existing file empty
    @Test
    public void sortFromFiles_FFFSingleExistFileEmpty_emptyString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, false, false, fileEmptyPath.toString());
        assertEquals(EMPTY_CONTENT, sortedString);
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: False, single existing file single line
    @Test
    public void sortFromFiles_FFFSingleExistFileSingleLine_singleLineString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, false, false, fileSinglePath.toString());
        assertEquals(SINGLE_LINE_CONT, sortedString);
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: False, multiple same existing files multiple lines
    @Test
    public void sortFromFiles_FFFMultiSameExistFilesMultiMultiLine_sortedFFFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, false, false, fileExtPath.toString(), fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "\n\n\n\n  \n  \n  spaced\n  spaced\n!!!\n!!!\n@@\n@@\n1\n1\n1123\n1123\n54321\n54321\n55\n55\nAAA 123random\nAAA 123random\nBBB\nBBB\na123!@#random\na123!@#random\nb\nb";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: False, multiple different existing files multiple lines
    @Test
    public void sortFromFiles_FFFMultiDiffExistFilesMultiMultiLine_sortedFFFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, false, false, fileExtPath.toString(), fileExt2Path.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "\n\n  \n  spaced\n!!!\n?\n@@\n1\n1123\n54321\n55\n765\nAAA 123random\nBBB\nOR no more\na123!@#random\nand more\nb\neven more";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: False, multiple existing files single and multiple lines
    @Test
    public void sortFromFiles_FFFMultiExistFilesSingleMultiLine_sortedFFFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, false, false, fileSinglePath.toString(), fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "\n\n  \n  spaced\n!!!\n@@\n1\n1123\n54321\n55\nAAA 123random\nBBB\na123!@#random\nb\nsingle line";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: True, single existing file multiple lines
    @Test
    public void sortFromFiles_FFTSingleExistFileMultiLine_sortedFFTString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, false, true, fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_F_MULTI, sortedString);
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: True, multiple same existing files multiple lines
    @Test
    public void sortFromFiles_FFTMultiSameExistFilesMultiLine_sortedFFTString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, false, true, fileExtPath.toString(), fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "\n\n\n\n  \n  \n  spaced\n  spaced\n!!!\n!!!\n@@\n@@\n1\n1\n1123\n1123\n54321\n54321\n55\n55\na123!@#random\na123!@#random\nAAA 123random\nAAA 123random\nb\nb\nBBB\nBBB";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: True, multiple diff existing files multiple lines
    @Test
    public void sortFromFiles_FFTMultiDiffExistFilesMultiLine_sortedFFTString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, false, true, fileExtPath.toString(), fileExt2Path.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "\n\n  \n  spaced\n!!!\n?\n@@\n1\n1123\n54321\n55\n765\na123!@#random\nAAA 123random\nand more\nb\nBBB\neven more\nOR no more";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: True, multiple existing files single and multiple lines
    @Test
    public void sortFromFiles_FFTMultiExistFilesSingleMultiLine_sortedFFTString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, false, true, fileSinglePath.toString(), fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "\n\n  \n  spaced\n!!!\n@@\n1\n1123\n54321\n55\na123!@#random\nAAA 123random\nb\nBBB\nsingle line";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: False, isReverse: True, isCaseIndependent: False, single existing file multiple lines
    @Test
    public void sortFromFiles_FTFSingleExistFileMultiLine_sortedFTFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, true, false, fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_R_MULTI, sortedString);
    }

    // isFirstWordNumber: False, isReverse: True, isCaseIndependent: False, multiple same existing files multiple lines
    @Test
    public void sortFromFiles_FTFMultiSameExistFilesMultiLine_sortedFTFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, true, false, fileExtPath.toString(), fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "b\nb\na123!@#random\na123!@#random\nBBB\nBBB\nAAA 123random\nAAA 123random\n55\n55\n54321\n54321\n1123\n1123\n1\n1\n@@\n@@\n!!!\n!!!\n  spaced\n  spaced\n  \n  \n\n\n\n";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: False, isReverse: True, isCaseIndependent: False, multiple diff existing files multiple lines
    @Test
    public void sortFromFiles_FTFMultiDiffExistFilesMultiLine_sortedFTFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, true, false, fileExtPath.toString(), fileExt2Path.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "even more\nb\nand more\na123!@#random\nOR no more\nBBB\nAAA 123random\n765\n55\n54321\n1123\n1\n@@\n?\n!!!\n  spaced\n  \n\n";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: False, isReverse: True, isCaseIndependent: False, multiple existing files single and multiple lines
    @Test
    public void sortFromFiles_FTFMultiExistFilesSingleMultiLine_sortedFTFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, true, false, fileSinglePath.toString(), fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "single line\nb\na123!@#random\nBBB\nAAA 123random\n55\n54321\n1123\n1\n@@\n!!!\n  spaced\n  \n\n";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: False, isReverse: True, isCaseIndependent: True, single existing file multiple lines
    @Test
    public void sortFromFiles_FTTSingleExistFileMultiLine_sortedFTTString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, true, true, fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_FR_MULTI, sortedString);
    }

    // isFirstWordNumber: False, isReverse: True, isCaseIndependent: True, multiple same existing files multiple lines
    @Test
    public void sortFromFiles_FTTMultiSameExistFilesMultiLine_sortedFTTString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, true, true, fileExtPath.toString(), fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "BBB\nBBB\nb\nb\nAAA 123random\nAAA 123random\na123!@#random\na123!@#random\n55\n55\n54321\n54321\n1123\n1123\n1\n1\n@@\n@@\n!!!\n!!!\n  spaced\n  spaced\n  \n  \n\n\n\n";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: False, isReverse: True, isCaseIndependent: True, multiple diff existing files multiple lines
    @Test
    public void sortFromFiles_FTTMultiDiffExistFilesMultiLine_sortedFTTString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, true, true, fileExtPath.toString(), fileExt2Path.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "OR no more\neven more\nBBB\nb\nand more\nAAA 123random\na123!@#random\n765\n55\n54321\n1123\n1\n@@\n?\n!!!\n  spaced\n  \n\n";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: False, isReverse: True, isCaseIndependent: True, multiple existing files single and multiple lines
    @Test
    public void sortFromFiles_FTTMultiExistFilesSingleMultiLine_sortedFTTString() throws Exception {
        String sortedString = sortApp.sortFromFiles(false, true, true, fileSinglePath.toString(), fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "single line\nBBB\nb\nAAA 123random\na123!@#random\n55\n54321\n1123\n1\n@@\n!!!\n  spaced\n  \n\n";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: True, isReverse: False, isCaseIndependent: False, single existing file multiple lines
    @Test
    public void sortFromFiles_TFFSingleExistFileMultiLine_sortedTFFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(true, false, false, fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_N_MULTI, sortedString);
    }

    // isFirstWordNumber: True, isReverse: False, isCaseIndependent: False, multiple same existing files multiple lines
    @Test
    public void sortFromFiles_TFFMultiSameExistFilesMultiLine_sortedTFFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(true, false, false, fileExtPath.toString(), fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "\n\n\n\n  \n  \n  spaced\n  spaced\n!!!\n!!!\n@@\n@@\n1\n1\n55\n55\n1123\n1123\n54321\n54321\nAAA 123random\nAAA 123random\nBBB\nBBB\na123!@#random\na123!@#random\nb\nb";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: True, isReverse: False, isCaseIndependent: False, multiple diff existing files multiple lines
    @Test
    public void sortFromFiles_TFFMultiDiffExistFilesMultiLine_sortedTFFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(true, false, false, fileExtPath.toString(), fileExt2Path.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "\n\n  \n  spaced\n!!!\n?\n@@\n1\n55\n765\n1123\n54321\nAAA 123random\nBBB\nOR no more\na123!@#random\nand more\nb\neven more";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: True, isReverse: False, isCaseIndependent: False, multiple existing files single and multiple lines
    @Test
    public void sortFromFiles_TFFMultiExistFilesSingleMultiLine_sortedTFFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(true, false, false, fileSinglePath.toString(), fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "\n\n  \n  spaced\n!!!\n@@\n1\n55\n1123\n54321\nAAA 123random\nBBB\na123!@#random\nb\nsingle line";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: True, isReverse: False, isCaseIndependent: True, single existing file multiple lines
    @Test
    public void sortFromFiles_TFTSingleExistFileMultiLine_sortedTFTString() throws Exception {
        String sortedString = sortApp.sortFromFiles(true, false, true, fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_NF_MULTI, sortedString);
    }

    // isFirstWordNumber: True, isReverse: False, isCaseIndependent: True, multiple same existing files multiple lines
    @Test
    public void sortFromFiles_TFTMultiSameExistFilesMultiLine_sortedTFTString() throws Exception {
        String sortedString = sortApp.sortFromFiles(true, false, true, fileExtPath.toString(), fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "\n\n\n\n  \n  \n  spaced\n  spaced\n!!!\n!!!\n@@\n@@\n1\n1\n55\n55\n1123\n1123\n54321\n54321\na123!@#random\na123!@#random\nAAA 123random\nAAA 123random\nb\nb\nBBB\nBBB";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: True, isReverse: False, isCaseIndependent: True, multiple diff existing files multiple lines
    @Test
    public void sortFromFiles_TFTMultiDiffExistFilesMultiLine_sortedTFTString() throws Exception {
        String sortedString = sortApp.sortFromFiles(true, false, true, fileExtPath.toString(), fileExt2Path.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "\n\n  \n  spaced\n!!!\n?\n@@\n1\n55\n765\n1123\n54321\na123!@#random\nAAA 123random\nand more\nb\nBBB\neven more\nOR no more";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: True, isReverse: False, isCaseIndependent: True, multiple existing files single and multiple lines
    @Test
    public void sortFromFiles_TFTMultiExistFilesSingleMultiLine_sortedTFTString() throws Exception {
        String sortedString = sortApp.sortFromFiles(true, false, true, fileSinglePath.toString(), fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "\n\n  \n  spaced\n!!!\n@@\n1\n55\n1123\n54321\na123!@#random\nAAA 123random\nb\nBBB\nsingle line";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: True, isReverse: True, isCaseIndependent: False, single existing file multiple lines
    @Test
    public void sortFromFiles_TTFSingleExistFileMultiLine_sortedTTFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(true, true, false, fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_NR_MULTI, sortedString);
    }

    // isFirstWordNumber: True, isReverse: True, isCaseIndependent: False, multiple same existing files multiple lines
    @Test
    public void sortFromFiles_TTFMultiSameExistFilesMultiLine_sortedTTFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(true, true, false, fileExtPath.toString(), fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "b\nb\na123!@#random\na123!@#random\nBBB\nBBB\nAAA 123random\nAAA 123random\n54321\n54321\n1123\n1123\n55\n55\n1\n1\n@@\n@@\n!!!\n!!!\n  spaced\n  spaced\n  \n  \n\n\n\n";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: True, isReverse: True, isCaseIndependent: False, multiple diff existing files multiple lines
    @Test
    public void sortFromFiles_TTFMultiDiffExistFilesMultiLine_sortedTTFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(true, true, false, fileExtPath.toString(), fileExt2Path.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "even more\nb\nand more\na123!@#random\nOR no more\nBBB\nAAA 123random\n54321\n1123\n765\n55\n1\n@@\n?\n!!!\n  spaced\n  \n\n";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: True, isReverse: True, isCaseIndependent: False, multiple existing files single and multiple lines
    @Test
    public void sortFromFiles_TTFMultiExistFilesSingleMultiLine_sortedTTFString() throws Exception {
        String sortedString = sortApp.sortFromFiles(true, true, false, fileSinglePath.toString(), fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "single line\nb\na123!@#random\nBBB\nAAA 123random\n54321\n1123\n55\n1\n@@\n!!!\n  spaced\n  \n\n";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: True, isReverse: True, isCaseIndependent: True, single existing file multiple lines
    @Test
    public void sortFromFiles_TTTSingleExistFileMultiLine_sortedTTTString() throws Exception {
        String sortedString = sortApp.sortFromFiles(true, true, true, fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_NFR_MULTI, sortedString);
    }

    // isFirstWordNumber: True, isReverse: True, isCaseIndependent: True, multiple same existing files multiple lines
    @Test
    public void sortFromFiles_TTTMultiSameExistFilesMultiLine_sortedTTTString() throws Exception {
        String sortedString = sortApp.sortFromFiles(true, true, true, fileExtPath.toString(), fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "BBB\nBBB\nb\nb\nAAA 123random\nAAA 123random\na123!@#random\na123!@#random\n54321\n54321\n1123\n1123\n55\n55\n1\n1\n@@\n@@\n!!!\n!!!\n  spaced\n  spaced\n  \n  \n\n\n\n";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: True, isReverse: True, isCaseIndependent: True, multiple diff existing files multiple lines
    @Test
    public void sortFromFiles_TTTMultiDiffExistFilesMultiLine_sortedTTTString() throws Exception {
        String sortedString = sortApp.sortFromFiles(true, true, true, fileExtPath.toString(), fileExt2Path.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "OR no more\neven more\nBBB\nb\nand more\nAAA 123random\na123!@#random\n54321\n1123\n765\n55\n1\n@@\n?\n!!!\n  spaced\n  \n\n";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: True, isReverse: True, isCaseIndependent: True, multiple existing files single and multiple lines
    @Test
    public void sortFromFiles_TTTMultiExistFilesSingleMultiLine_sortedTTTString() throws Exception {
        String sortedString = sortApp.sortFromFiles(true, true, true, fileSinglePath.toString(), fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        String expectedString = "single line\nBBB\nb\nAAA 123random\na123!@#random\n54321\n1123\n55\n1\n@@\n!!!\n  spaced\n  \n\n";
        assertEquals(expectedString, sortedString);
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: False, single line
    @Test
    public void sortFromStdin_FFFSingleLine_singleLineString() throws Exception {
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdin(false, false, false, testStdin).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SINGLE_LINE_CONT, sortedString);
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: False, empty string
    @Test
    public void sortFromStdin_FFFEmpty_emptyString() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());
        String sortedString = sortApp.sortFromStdin(false, false, false, testStdin).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(EMPTY_CONTENT, sortedString);
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: False, new line
    @Test
    public void sortFromStdin_FFFOnlyNewLine_emptyString() throws Exception {
        testStdin = new ByteArrayInputStream("\n".getBytes());
        String sortedString = sortApp.sortFromStdin(false, false, false, testStdin).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(EMPTY_CONTENT, sortedString);
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: False, multiple lines
    @Test
    public void sortFromStdin_FFFMultiLine_sortedFFFString() throws Exception {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdin(false, false, false, testStdin).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_MULTI, sortedString);
    }

    // null stdin stream
    @Test
    public void sortFromStdin_nullStream_throwsException() {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> sortApp.sortFromStdin(false, false, false, null));
        assertEquals(ERR_NULL_STREAMS, exception.getMessage());
    }

    // isFirstWordNumber: False, isReverse: False, isCaseIndependent: True, multiple lines
    @Test
    public void sortFromStdin_FFTMultiLine_sortedFFTString() throws Exception {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdin(false, false, true, testStdin).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_F_MULTI, sortedString);
    }

    // isFirstWordNumber: False, isReverse: True, isCaseIndependent: False, multiple lines
    @Test
    public void sortFromStdin_FTFMultiLine_sortedFTFString() throws Exception {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdin(false, true, false, testStdin).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_R_MULTI, sortedString);
    }

    // isFirstWordNumber: False, isReverse: True, isCaseIndependent: True, multiple lines
    @Test
    public void sortFromStdin_FTTMultiLine_sortedFTTString() throws Exception {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdin(false, true, true, testStdin).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_FR_MULTI, sortedString);
    }

    // isFirstWordNumber: True, isReverse: False, isCaseIndependent: False, multiple lines
    @Test
    public void sortFromStdin_TFFMultiLine_sortedTFFString() throws Exception {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdin(true, false, false, testStdin).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_N_MULTI, sortedString);
    }

    // isFirstWordNumber: True, isReverse: False, isCaseIndependent: True, multiple lines
    @Test
    public void sortFromStdin_TFTMultiLine_sortedTFTString() throws Exception {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdin(true, false, true, testStdin).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_NF_MULTI, sortedString);
    }

    // isFirstWordNumber: True, isReverse: True, isCaseIndependent: False, multiple lines
    @Test
    public void sortFromStdin_TTFMultiLine_sortedTTFString() throws Exception {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdin(true, true, false, testStdin).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_NR_MULTI, sortedString);
    }

    // isFirstWordNumber: True, isReverse: True, isCaseIndependent: True, multiple lines
    @Test
    public void sortFromStdin_TTTMultiLine_sortedTTTString() throws Exception {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdin(true, true, true, testStdin).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SORT_NFR_MULTI, sortedString);
    }

    //Concentrate more on the variation of stdin and files supplied instead of the flags supplied since it has been tested as above.
    // Stdin: empty, File 1: empty, File 2: empty
    @Test
    public void sortFromStdinandFiles_emptyEmptyEmpty_emptyString() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());
        String sortedString = sortApp.sortFromStdinAndFiles(false, false, false, testStdin, fileEmptyPath.toString(), fileEmptyPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(EMPTY_CONTENT, sortedString);
    }

    // Stdin: empty, File 1: single line, File 2: singe line
    @Test
    public void sortFromStdinandFiles_emptySingleSingle_sortedString() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());
        String sortedString = sortApp.sortFromStdinAndFiles(false, false, false, testStdin, fileSinglePath.toString(), fileSinglePath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SINGLE_LINE_CONT + "\n" + SINGLE_LINE_CONT, sortedString);
    }

    // Stdin: empty, File 1: multiple lines, File 2: multiple lines
    @Test
    public void sortFromStdinandFiles_emptyMultiMulti_sortedString() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());
        String sortedString = sortApp.sortFromStdinAndFiles(true, true, true, testStdin, fileExtPath.toString(), fileExt2Path.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals("OR no more\neven more\nBBB\nb\nand more\nAAA 123random\na123!@#random\n54321\n1123\n765\n55\n1\n@@\n?\n!!!\n  spaced\n  \n\n", sortedString);
    }

    // Stdin: empty, File 1: not given, File 2: not given
    @Test
    public void sortFromStdinandFiles_emptyNilNil_emptyString() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());
        String sortedString = sortApp.sortFromStdinAndFiles(true, true, true, testStdin).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(EMPTY_CONTENT, sortedString);
    }

    // Stdin: single line, File 1: single line, File 2: multiple lines
    @Test
    public void sortFromStdinandFiles_singleSingleMulti_sortedString() throws Exception {
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdinAndFiles(true, true, false, testStdin, fileSinglePath.toString(), fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals("single line\nsingle line\nb\na123!@#random\nBBB\nAAA 123random\n54321\n1123\n55\n1\n@@\n!!!\n  spaced\n  \n\n", sortedString);
    }

    // Stdin: single line, File 1: multiple lines, File 2: not given
    @Test
    public void sortFromStdinandFiles_singleMultiNil_sortedString() throws Exception {
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdinAndFiles(true, true, false, testStdin, fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals("single line\nb\na123!@#random\nBBB\nAAA 123random\n54321\n1123\n55\n1\n@@\n!!!\n  spaced\n  \n\n", sortedString);
    }

    // Stdin: single line, File 1: not given, File 2: empty
    @Test
    public void sortFromStdinandFiles_singleNilEmpty_singleLineString() throws Exception {
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdinAndFiles(true, true, false, testStdin, fileEmptyPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SINGLE_LINE_CONT, sortedString);
    }

    // Stdin: single line, File 1: empty, File 2: single line
    @Test
    public void sortFromStdinandFiles_singleEmptySingle_sortedString() throws Exception {
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdinAndFiles(true, false, false, testStdin, fileEmptyPath.toString(), fileSinglePath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals(SINGLE_LINE_CONT + "\n" + SINGLE_LINE_CONT, sortedString);
    }

    // Stdin: multiple lines, File 1: multiple lines, File 2: empty
    @Test
    public void sortFromStdinandFiles_multiMultiEmpty_sortedString() throws Exception {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdinAndFiles(false, true, true, testStdin, fileExtPath.toString(), fileEmptyPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals("BBB\nBBB\nb\nb\nAAA 123random\nAAA 123random\na123!@#random\na123!@#random\n55\n55\n54321\n54321\n1123\n1123\n1\n1\n@@\n@@\n!!!\n!!!\n  spaced\n  spaced\n  \n  \n\n\n\n", sortedString);
    }

    // Stdin: multiple lines, File 1: not given, File 2: single line
    @Test
    public void sortFromStdinandFiles_multiNilSingle_sortedString() throws Exception {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdinAndFiles(true, false, true, testStdin, fileSinglePath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals("\n\n  \n  spaced\n!!!\n@@\n1\n55\n1123\n54321\na123!@#random\nAAA 123random\nb\nBBB\nsingle line", sortedString);
    }

    // Stdin: multiple lines, File 1: not given, File 2: multiple lines
    @Test
    public void sortFromStdinandFiles_multiNilMulti_sortedString() throws Exception {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdinAndFiles(false, true, true, testStdin, fileExtPath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals("BBB\nBBB\nb\nb\nAAA 123random\nAAA 123random\na123!@#random\na123!@#random\n55\n55\n54321\n54321\n1123\n1123\n1\n1\n@@\n@@\n!!!\n!!!\n  spaced\n  spaced\n  \n  \n\n\n\n", sortedString);
    }

    // Stdin: multiple lines, File 1: single line, File 2: not given
    @Test
    public void sortFromStdinandFiles_multiSingleNil_sortedString() throws Exception {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdinAndFiles(true, false, false, testStdin, fileSinglePath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals("\n\n  \n  spaced\n!!!\n@@\n1\n55\n1123\n54321\nAAA 123random\nBBB\na123!@#random\nb\nsingle line", sortedString);
    }

    // Stdin: single line, File 1: file in directory, File 2: single line
    @Test
    public void sortFromStdinandFiles_multiFileDirSingle_sortedString() throws Exception {
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdinAndFiles(true, false, false, testStdin, folderPath.resolve(NEST_DIR_1).resolve(FILE_EXT).toString(), fileSinglePath.toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals("\n\n  \n  spaced\n!!!\n@@\n1\n55\n1123\n54321\nAAA 123random\nBBB\na123!@#random\nb\nsingle line\nsingle line", sortedString);
    }

    // Stdin: single line, File 1: single line, File 2: file in directory
    @Test
    public void sortFromStdinandFiles_multiSingleFileDir_sortedString() throws Exception {
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());
        String sortedString = sortApp.sortFromStdinAndFiles(true, false, false, testStdin, fileSinglePath.toString(), folderPath.resolve(NEST_DIR_1).resolve(FILE_EXT).toString()).replaceAll(STRING_NEWLINE, NORM_NEWLINE);
        assertEquals("\n\n  \n  spaced\n!!!\n@@\n1\n55\n1123\n54321\nAAA 123random\nBBB\na123!@#random\nb\nsingle line\nsingle line", sortedString);
    }

    // Stdin: null, File 1: multiple lines, File 2: single lines
    @Test
    public void sortFromStdinandFiles_nullMultiSingle_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> sortApp.sortFromStdinAndFiles(true, false, false, null, fileExtPath.toString(), fileSinglePath.toString()));
        assertEquals(ERR_NULL_STREAMS, exception.getMessage());
    }

    // Stdin: multiple lines, File 1: null, File 2: nil
    @Test
    public void sortFromStdinandFiles_multiNullMulti_throwsException() {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> sortApp.sortFromStdinAndFiles(true, false, false, testStdin, null));
        assertEquals(ERR_NULL_ARGS, exception.getMessage());
    }

    // Stdin: multiple lines, File 1: multiple lines, File 2: directory
    @Test
    public void sortFromStdinandFiles_multiMultiDir_throwsException() {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> sortApp.sortFromStdinAndFiles(true, false, false, testStdin, fileExtPath.toString(), folderPath.resolve(NEST_DIR_1).toString()));
        assertEquals(folderPath.resolve(NEST_DIR_1) + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // Stdin: multiple lines, File 1: directory, File 2: multiple lines
    @Test
    public void sortFromStdinandFiles_multiDirMulti_throwsException() {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> sortApp.sortFromStdinAndFiles(true, false, false, testStdin, folderPath.resolve(NEST_DIR_1).toString(), fileExtPath.toString()));
        assertEquals(folderPath.resolve(NEST_DIR_1) + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // Stdin: multiple lines, File 1: multiple lines, File 2: non-existent directory
    @Test
    public void sortFromStdinandFiles_multiMultiNonExistDir_throwsException() {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> sortApp.sortFromStdinAndFiles(true, false, false, testStdin, fileExtPath.toString(), folderPath.resolve(NON_EXIST_DIR).resolve(FILE_EXT).toString()));
        assertEquals(folderPath.resolve(NON_EXIST_DIR).resolve(FILE_EXT) + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // Stdin: multiple lines, File 1: non-existent directory, File 2: multiple lines
    @Test
    public void sortFromStdinandFiles_multiNonExistDirMulti_throwsException() {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> sortApp.sortFromStdinAndFiles(true, false, false, testStdin, folderPath.resolve(NON_EXIST_DIR).resolve(FILE_EXT).toString(), fileExtPath.toString()));
        assertEquals(folderPath.resolve(NON_EXIST_DIR).resolve(FILE_EXT) + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // Stdin: multiple lines, File 1: multiple lines, File 2: non-existent file
    @Test
    public void sortFromStdinandFiles_multiMultiNonExistFile_throwsException() {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> sortApp.sortFromStdinAndFiles(true, false, false, testStdin, fileExtPath.toString(), folderPath.resolve(NON_EXIST_FILE).toString()));
        assertEquals(folderPath.resolve(NON_EXIST_FILE) + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // Stdin: multiple lines, File 1: non-existent file, File 2: multiple lines
    @Test
    public void sortFromStdinandFiles_multiNonExistFileMulti_throwsException() {
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> sortApp.sortFromStdinAndFiles(true, false, false, testStdin, folderPath.resolve(NON_EXIST_FILE).toString(), fileExtPath.toString()));
        assertEquals(folderPath.resolve(NON_EXIST_FILE) + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // Stdin: empty, File 1: not given, File 2: not given
    @Test
    public void run_emptyNilNil_emptyOutput() throws InvalidArgsException, SortException {
        String[] args = {};
        List<String> files = Arrays.asList();
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(false);
        when(sortParser.isReverseOrder()).thenReturn(false);
        when(sortParser.isCaseIndependent()).thenReturn(false);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(EMPTY_CONTENT, testOutputStream.toString());
    }

    // Stdin: empty, File 1: empty, File 2: empty
    @Test
    public void run_emptyEmptyEmpty_emptyOutput() throws InvalidArgsException, SortException {
        String[] args = {"-", fileEmptyPath.toString(), fileEmptyPath.toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", fileEmptyPath.toString(), fileEmptyPath.toString());
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(false);
        when(sortParser.isReverseOrder()).thenReturn(false);
        when(sortParser.isCaseIndependent()).thenReturn(false);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(EMPTY_CONTENT, testOutputStream.toString());
    }

    // Stdin: empty, File 1: single line, File 2: single line
    @Test
    public void run_emptySingleSingle_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {"-", fileSinglePath.toString(), fileSinglePath.toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", fileSinglePath.toString(), fileSinglePath.toString());
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(false);
        when(sortParser.isReverseOrder()).thenReturn(false);
        when(sortParser.isCaseIndependent()).thenReturn(false);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(SINGLE_LINE_CONT + "\n" + SINGLE_LINE_CONT + "\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: empty, File 1: multiple lines, File 2: multiple lines
    @Test
    public void run_emptyMultiMulti_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {"-", fileExtPath.toString(), fileExtPath.toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", fileExtPath.toString(), fileExtPath.toString());
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(false);
        when(sortParser.isReverseOrder()).thenReturn(false);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("\n\n\n\n  \n  \n  spaced\n  spaced\n!!!\n!!!\n@@\n@@\n1\n1\n1123\n1123\n54321\n54321\n55\n55\na123!@#random\na123!@#random\nAAA 123random\nAAA 123random\nb\nb\nBBB\nBBB\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: single line, File 1: empty, File 2: single line
    @Test
    public void run_singleEmptySingle_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {fileEmptyPath.toString(), fileSinglePath.toString(), "-"}; // does nothing but for visual of args
        List<String> files = Arrays.asList(fileEmptyPath.toString(), fileSinglePath.toString(), "-");
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(false);
        when(sortParser.isReverseOrder()).thenReturn(false);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(SINGLE_LINE_CONT + "\n" + SINGLE_LINE_CONT + "\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: single line, File 1: single line, File 2: multiple line
    @Test
    public void run_singleSingleMulti_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {fileSinglePath.toString(), "-", fileExtPath.toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList(fileSinglePath.toString(), "-", fileExtPath.toString());
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(false);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(false);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("single line\nsingle line\nb\na123!@#random\nBBB\nAAA 123random\n55\n54321\n1123\n1\n@@\n!!!\n  spaced\n  \n\n\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: single line, File 1: single line, File 2: multiple line, multiple "-"
    @Test
    public void run_singleSingleMultiManyDash_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {"-", fileSinglePath.toString(), "-", "-", fileExtPath.toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", fileSinglePath.toString(), "-", "-", fileExtPath.toString());
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(false);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(false);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("single line\nsingle line\nb\na123!@#random\nBBB\nAAA 123random\n55\n54321\n1123\n1\n@@\n!!!\n  spaced\n  \n\n\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: single line, File 1: multiple lines, File 2: not given
    @Test
    public void run_singleMultiNil_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {fileExtPath.toString(), "-"}; // does nothing but for visual of args
        List<String> files = Arrays.asList(fileExtPath.toString(), "-");
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(false);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(false);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("single line\nb\na123!@#random\nBBB\nAAA 123random\n55\n54321\n1123\n1\n@@\n!!!\n  spaced\n  \n\n\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: single line, File 1: not given, File 2: empty
    @Test
    public void run_singleNilEmpty_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {fileEmptyPath.toString(), "-"}; // does nothing but for visual of args
        List<String> files = Arrays.asList(fileEmptyPath.toString(), "-");
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(false);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(false);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(SINGLE_LINE_CONT + "\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: multiple lines, File 1: single line, File 2: not given
    @Test
    public void run_multiSingleNil_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {fileSinglePath.toString(), "-"}; // does nothing but for visual of args
        List<String> files = Arrays.asList(fileSinglePath.toString(), "-");
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(true);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(false);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("single line\nb\na123!@#random\nBBB\nAAA 123random\n54321\n1123\n55\n1\n@@\n!!!\n  spaced\n  \n\n\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: multiple lines, File 1: multiple lines, File 2: empty
    @Test
    public void run_multiMultiEmpty_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {fileExt2Path.toString(), fileEmptyPath.toString(), "-"}; // does nothing but for visual of args
        List<String> files = Arrays.asList(fileExt2Path.toString(), fileEmptyPath.toString(), "-");
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(true);
        when(sortParser.isReverseOrder()).thenReturn(false);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("\n\n  \n  spaced\n!!!\n?\n@@\n1\n55\n765\n1123\n54321\na123!@#random\nAAA 123random\nand more\nb\nBBB\neven more\nOR no more\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: multiple lines, File 1: not given, File 2: single
    @Test
    public void run_multiNilSingle_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {"-", fileSinglePath.toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", fileSinglePath.toString());
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(true);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(false);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("single line\nb\na123!@#random\nBBB\nAAA 123random\n54321\n1123\n55\n1\n@@\n!!!\n  spaced\n  \n\n\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: multiple lines, File 1: empty, File 2: multiple lines
    @Test
    public void run_multiEmptyMulti_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {"-", fileEmptyPath.toString(), fileExtPath.toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", fileEmptyPath.toString(), fileExtPath.toString());
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(false);
        when(sortParser.isReverseOrder()).thenReturn(false);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("\n\n\n\n  \n  \n  spaced\n  spaced\n!!!\n!!!\n@@\n@@\n1\n1\n1123\n1123\n54321\n54321\n55\n55\na123!@#random\na123!@#random\nAAA 123random\nAAA 123random\nb\nb\nBBB\nBBB\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: empty, File 1: multiple lines, File 2: single line
    @Test
    public void run_emptyMultiSingle_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {"-", fileExtPath.toString(), fileSinglePath.toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", fileExtPath.toString(), fileSinglePath.toString());
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(false);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("single line\nBBB\nb\nAAA 123random\na123!@#random\n55\n54321\n1123\n1\n@@\n!!!\n  spaced\n  \n\n\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: empty, File 1: not given, File 2: multiple lines
    @Test
    public void run_emptyNilMulti_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {"-", fileExtPath.toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", fileExtPath.toString());
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(true);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(SORT_NFR_MULTI + "\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: empty, File 1: empty, File 2: not given
    @Test
    public void run_emptyEmptyNil_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {"-", fileEmptyPath.toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", fileEmptyPath.toString());
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(true);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(EMPTY_CONTENT, testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: empty, File 1: single line, File 2: empty
    @Test
    public void run_emptySingleEmpty_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {"-", fileSinglePath.toString(), fileEmptyPath.toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", fileSinglePath.toString(), fileEmptyPath.toString());
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(true);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(SINGLE_LINE_CONT + "\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: empty, File 1: new line, File 2: empty
    @Test
    public void run_emptyNewLineEmpty_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {"-", fileNewLinePath.toString(), fileEmptyPath.toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", fileNewLinePath.toString(), fileEmptyPath.toString());
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(true);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
    }

    // Stdin: empty, File 1: new line, File 2: not given
    @Test
    public void run_emptyNewLineNil_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {"-", fileNewLinePath.toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", fileNewLinePath.toString());
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(true);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
    }

    // Stdin: new line, File 1: not given, File 2: not given
    @Test
    public void run_newLineNilNil_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {}; // does nothing but for visual of args
        List<String> files = new ArrayList<>();
        testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(true);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
    }

    // Stdin: single line, File 1: file in directory, File 2: multiple lines
    @Test
    public void run_singleFileDirMulti_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {"-", folderPath.resolve(NEST_DIR_1).resolve(NEST_DIR_2).resolve(FILE_EXT).toString(), fileExtPath.toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", folderPath.resolve(NEST_DIR_1).resolve(NEST_DIR_2).resolve(FILE_EXT).toString(), fileExtPath.toString());
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(true);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("single line\nBBB\nBBB\nb\nb\nAAA 123random\nAAA 123random\na123!@#random\na123!@#random\n54321\n54321\n1123\n1123\n55\n55\n1\n1\n@@\n@@\n!!!\n!!!\n  spaced\n  spaced\n  \n  \n\n\n\n\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: single line, File 1: multiple lines, File 2: file in directory
    @Test
    public void run_singleMultiFileDir_sortedOutput() throws InvalidArgsException, SortException {
        String[] args = {"-", fileExtPath.toString(), folderPath.resolve(NEST_DIR_1).resolve(NEST_DIR_2).resolve(FILE_EXT).toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", fileExtPath.toString(), folderPath.resolve(NEST_DIR_1).resolve(NEST_DIR_2).resolve(FILE_EXT).toString());
        testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(true);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        sortApp.run(args, testStdin, testOutputStream);
        assertEquals("single line\nBBB\nBBB\nb\nb\nAAA 123random\nAAA 123random\na123!@#random\na123!@#random\n54321\n54321\n1123\n1123\n55\n55\n1\n1\n@@\n@@\n!!!\n!!!\n  spaced\n  spaced\n  \n  \n\n\n\n\n", testOutputStream.toString().replaceAll(STRING_NEWLINE, NORM_NEWLINE));
    }

    // Stdin: null, File 1: single line, File 2: empty
    @Test
    public void run_nullSingleEmpty_throwsSortException() throws InvalidArgsException {
        String[] args = {"-", fileSinglePath.toString(), fileEmptyPath.toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", fileSinglePath.toString(), fileEmptyPath.toString());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(true);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        Exception exception = assertThrows(SortException.class, () -> sortApp.run(args, null, testOutputStream));
        assertEquals(SORT_EXCEP + ERR_NULL_STREAMS, exception.getMessage());
    }

    // Stdin: empty, File 1: directory, File 2: multiple lines
    @Test
    public void run_emptyDirMulti_throwsSortException() throws InvalidArgsException {
        String[] args = {"-", folderPath.resolve(NEST_DIR_1).toString(), fileExtPath.toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", folderPath.resolve(NEST_DIR_1).toString(), fileExtPath.toString());
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(true);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        Exception exception = assertThrows(SortException.class, () -> sortApp.run(args, testStdin, testOutputStream));
        assertEquals(SORT_EXCEP + folderPath.resolve(NEST_DIR_1) + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // Stdin: multiple lines, File 1: single, File 2: directory
    @Test
    public void run_emptySingleDir_throwsSortException() throws InvalidArgsException {
        String[] args = {"-", fileSinglePath.toString(), folderPath.resolve(NEST_DIR_1).toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", fileSinglePath.toString(), folderPath.resolve(NEST_DIR_1).toString());
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(true);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        Exception exception = assertThrows(SortException.class, () -> sortApp.run(args, testStdin, testOutputStream));
        assertEquals(SORT_EXCEP + folderPath.resolve(NEST_DIR_1) + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // Stdin: empty, File 1: non-existent directory, File 2: multiple lines
    @Test
    public void run_emptyNonExistDirMulti_throwsSortException() throws InvalidArgsException {
        String[] args = {"-", folderPath.resolve(NON_EXIST_DIR).resolve(FILE_EXT).toString(), fileExtPath.toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", folderPath.resolve(NON_EXIST_DIR).resolve(FILE_EXT).toString(), fileExtPath.toString());
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(true);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        Exception exception = assertThrows(SortException.class, () -> sortApp.run(args, testStdin, testOutputStream));
        assertEquals(SORT_EXCEP + folderPath.resolve(NON_EXIST_DIR).resolve(FILE_EXT) + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // Stdin: multiple lines, File 1: single, File 2: non-existent directory
    @Test
    public void run_emptySingleNonExistDir_throwsSortException() throws InvalidArgsException {
        String[] args = {"-", fileSinglePath.toString(), folderPath.resolve(NON_EXIST_DIR).resolve(FILE_EXT).toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", fileSinglePath.toString(), folderPath.resolve(NON_EXIST_DIR).resolve(FILE_EXT).toString());
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(true);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        Exception exception = assertThrows(SortException.class, () -> sortApp.run(args, testStdin, testOutputStream));
        assertEquals(SORT_EXCEP + folderPath.resolve(NON_EXIST_DIR).resolve(FILE_EXT) + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // Stdin: empty, File 1: non-existent file, File 2: multiple lines
    @Test
    public void run_emptyNonExistFileMulti_throwsSortException() throws InvalidArgsException {
        String[] args = {"-", folderPath.resolve(NON_EXIST_FILE).toString(), fileExtPath.toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", folderPath.resolve(NON_EXIST_FILE).toString(), fileExtPath.toString());
        testStdin = new ByteArrayInputStream(EMPTY_CONTENT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(true);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        Exception exception = assertThrows(SortException.class, () -> sortApp.run(args, testStdin, testOutputStream));
        assertEquals(SORT_EXCEP + folderPath.resolve(NON_EXIST_FILE) + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // Stdin: multiple lines, File 1: single, File 2: non-existent file
    @Test
    public void run_emptySingleNonExistFile_throwsSortException() throws InvalidArgsException {
        String[] args = {"-", fileSinglePath.toString(), folderPath.resolve(NON_EXIST_FILE).toString()}; // does nothing but for visual of args
        List<String> files = Arrays.asList("-", fileSinglePath.toString(), folderPath.resolve(NON_EXIST_FILE).toString());
        testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());

        doNothing().when(sortParser).parse(args);
        when(sortParser.getFiles()).thenReturn(files);
        when(sortParser.isFirstWordNumber()).thenReturn(true);
        when(sortParser.isReverseOrder()).thenReturn(true);
        when(sortParser.isCaseIndependent()).thenReturn(true);

        Exception exception = assertThrows(SortException.class, () -> sortApp.run(args, testStdin, testOutputStream));
        assertEquals(SORT_EXCEP + folderPath.resolve(NON_EXIST_FILE) + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }
}