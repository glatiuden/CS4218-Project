package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.testutils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RegexArgumentTest {

    private static final String FILE_ONE_NAME = "test-a-file.txt";
    private static final String FILE_TWO_NAME = "test-b-file.txt";
    private static final String FILE_PREFIX = "test";
    private static final String FOLDER_SUFFIX = "folder";
    private static final String FILE_ONE_CONTENT = "Hello from CS4218";
    private static final String FILE_TWO_CONTENT = "CS4218 says hello";
    private static final String FOLDER_ONE_NAME = "subdir1-test-folder";
    private static final String FOLDER_TWO_NAME = "subdir2-test-folder";
    private static final String SUBDIRECTORY_NAME = "test-glob";
    private static final String SUBDIRECTORY_FILE = "glob.txt";
    private static final String ERR_CREATE_DIR = "Unable to create directory";

    @TempDir
    public static Path folderPath;
    private RegexArgument regexArgument;

    @BeforeAll
    static void setupRegexArgument() throws Exception {
        FileUtils.createNewDirs(folderPath);
        Environment.setCurrentDirectory(folderPath.toString());
        Path path1 = folderPath.resolve(FILE_ONE_NAME);
        Path path2 = folderPath.resolve(FILE_TWO_NAME);
        Files.writeString(path1, FILE_ONE_CONTENT);
        Files.writeString(path2, FILE_TWO_CONTENT);

        Path folder1 = folderPath.resolve(FOLDER_ONE_NAME);
        Path folder2 = folderPath.resolve(FOLDER_TWO_NAME);
        Path folder3 = folderPath.resolve(SUBDIRECTORY_NAME);
        boolean foldersCreated = folder1.toFile().mkdir() && folder2.toFile().mkdir() && folder3.toFile().mkdir();
        if (!foldersCreated) {
            throw new Exception(ERR_CREATE_DIR);
        }

        Path path3 = folder3.resolve(SUBDIRECTORY_FILE);
        Files.writeString(path3, FILE_TWO_CONTENT);
    }

    @AfterAll
    static void tearDownRegexArgument() {
        FileUtils.deleteFolder(folderPath);
        Environment.resetCurrentDirectory();
    }

    @BeforeEach
    void setupEach() {
        regexArgument = new RegexArgument();
    }

    @Test
    void append_ValidChar_ShouldAppendChar() {
        regexArgument.append('c');
        assertEquals("c", regexArgument.toString());
        assertFalse(regexArgument.isRegex());
        assertFalse(regexArgument.isEmpty());
    }

    @Test
    void appendAsterisk_Nil_ShouldAppendAsterisk() {
        regexArgument.appendAsterisk();
        assertEquals("*", regexArgument.toString());
        assertTrue(regexArgument.isRegex());
        assertFalse(regexArgument.isEmpty());
    }

    @Test
    void merge_RegexArgument_ShouldCopyOverAndIsRegexTrue() {
        RegexArgument newRegexArgument = new RegexArgument();
        newRegexArgument.appendAsterisk();
        newRegexArgument.merge("test");
        regexArgument.merge(newRegexArgument);
        assertEquals(newRegexArgument.toString(), regexArgument.toString());
        assertTrue(regexArgument.isRegex());
    }

    @Test
    void merge_RegexArgument_ShouldCopyOverAndIsRegexFalse() {
        RegexArgument newRegexArgument = new RegexArgument();
        regexArgument.merge(newRegexArgument);
        assertEquals(newRegexArgument.toString(), regexArgument.toString());
        assertFalse(regexArgument.isRegex());
    }

    @Test
    void merge_ValidString_ShouldAppendToPlaintextAndIsRegexTrue() {
        regexArgument.merge(FILE_PREFIX);
        assertEquals(FILE_PREFIX, regexArgument.toString());
        assertFalse(regexArgument.isRegex());
        assertFalse(regexArgument.isEmpty());
    }

    @Test
    void merge_EmptyString_IsEmpty() {
        regexArgument.merge("");
        assertFalse(regexArgument.isRegex());
        assertTrue(regexArgument.isEmpty());
    }

    @Test
    void globFiles_AsteriskOnly_ShouldReturnFilesAndFoldersContainingExpression() {
        List<String> expected = Arrays.asList(FOLDER_ONE_NAME, FOLDER_TWO_NAME, FILE_ONE_NAME, FILE_TWO_NAME, SUBDIRECTORY_NAME);
        regexArgument.appendAsterisk();
        assertTrue(regexArgument.isRegex());
        List<String> results = regexArgument.globFiles();
        assertEquals(expected, results);
    }

    @Test
    void globFiles_AsteriskAfterExpression_ShouldReturnFilesAndFoldersContainingExpression() {
        List<String> expected = Arrays.asList(FILE_ONE_NAME, FILE_TWO_NAME, SUBDIRECTORY_NAME);
        regexArgument.merge(FILE_PREFIX);
        regexArgument.appendAsterisk();
        assertTrue(regexArgument.isRegex());
        List<String> results = regexArgument.globFiles();
        assertEquals(expected, results);
    }

    @Test
    void globFiles_AsteriskBeforeExpression_ShouldReturnFilesAndFoldersContainingExpression() {
        List<String> expected = Arrays.asList(FOLDER_ONE_NAME, FOLDER_TWO_NAME);
        regexArgument.appendAsterisk();
        regexArgument.merge(FOLDER_SUFFIX);
        assertTrue(regexArgument.isRegex());
        List<String> results = regexArgument.globFiles();
        assertEquals(expected, results);
    }

    @Test
    void globFiles_AsteriskBetweenExpression_ShouldReturnFilesAndFoldersContainingExpression() {
        List<String> expected = Arrays.asList(FILE_ONE_NAME, FILE_TWO_NAME);
        regexArgument.append('t');
        regexArgument.appendAsterisk();
        regexArgument.merge("txt");
        assertTrue(regexArgument.isRegex());
        List<String> results = regexArgument.globFiles();
        assertEquals(expected, results);
    }

    @Test
    void globFiles_ExpressionBetweenAsterisk_ShouldReturnFilesAndFoldersContainingExpression() {
        List<String> expected = Arrays.asList(FOLDER_ONE_NAME, FOLDER_TWO_NAME, FILE_ONE_NAME, FILE_TWO_NAME, SUBDIRECTORY_NAME);
        regexArgument.appendAsterisk();
        regexArgument.merge(FILE_PREFIX);
        regexArgument.appendAsterisk();
        assertTrue(regexArgument.isRegex());
        List<String> results = regexArgument.globFiles();
        assertEquals(expected, results);
    }

    @Test
    void globFiles_ExpressionOnly_ShouldReturnListWithPlaintext() {
        List<String> expected = Arrays.asList(FILE_PREFIX);
        regexArgument.merge(FILE_PREFIX);
        List<String> results = regexArgument.globFiles();
        assertEquals(expected, results);
    }

    @Test
    void globFiles_SubdirectoryWithAsterisk_ShouldReturnFilesAndFoldersContainingExpression() {
        List<String> expected = Arrays.asList(SUBDIRECTORY_NAME + "/" + SUBDIRECTORY_FILE);
        regexArgument.merge(SUBDIRECTORY_NAME + "/");
        regexArgument.appendAsterisk();
        List<String> results = regexArgument.globFiles();
        assertEquals(expected, results);
    }

    @Test
    void globFiles_NoExpression_ShouldReturnEmptyList() {
        List<String> expected = new LinkedList<>();
        expected.add("");
        List<String> results = regexArgument.globFiles();
        assertFalse(regexArgument.isRegex());
        assertArrayEquals(expected.toArray(), results.toArray());
    }
}