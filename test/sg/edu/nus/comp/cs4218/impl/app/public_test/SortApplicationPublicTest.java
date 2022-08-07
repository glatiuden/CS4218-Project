package sg.edu.nus.comp.cs4218.impl.app.public_test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.impl.app.SortApplication;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("PMD") // Not required to check for given test from the prof
class SortApplicationPublicTest {
    private static final String TEMP = "temp-sort";
    private static final String TEST_FILE = "file.txt";
    @TempDir
    public static Path TEMP_PATH;
    private static SortApplication sortApplication;

    @BeforeAll
    static void setUpAll() throws IOException {
        Files.createDirectories(TEMP_PATH);
        Environment.setCurrentDirectory(TEMP_PATH.toString());
    }

    private String joinStringsByLineSeparator(String... strs) {
        return String.join(TestStringUtils.STRING_NEWLINE, strs);
    }

    private InputStream generateInputStreamFromStrings(String... strs) {
        return new ByteArrayInputStream(joinStringsByLineSeparator(strs).getBytes(StandardCharsets.UTF_8));
    }

    @BeforeEach
    void setUp() {
        sortApplication = new SortApplication();
    }

    private Path createFile(String name, String content) throws IOException {
        Path path = TEMP_PATH.resolve(name);
        Files.write(path, content.getBytes());
        return path;
    }

    @Test
    void sortFromStdin_NoFlags_ReturnsSortedList() throws Exception {
        InputStream stdin = generateInputStreamFromStrings("a", "c", "b");
        String expected = joinStringsByLineSeparator("a", "b", "c");
        assertEquals(expected, sortApplication.sortFromStdin(false, false, false, stdin));
    }

    @Test
    void sortFromStdin_IsFirstWordNumber_ReturnsSortedList() throws Exception {
        InputStream stdin = generateInputStreamFromStrings("10 b", "5 c", "1 a");
        String expected = joinStringsByLineSeparator("1 a", "5 c", "10 b");
        assertEquals(expected, sortApplication.sortFromStdin(true, false, false, stdin));
    }

    @Test
    void sortFromStdin_ReverseOrder_ReverseSortedList() throws Exception {
        InputStream stdin = generateInputStreamFromStrings("a", "c", "b");
        String expected = joinStringsByLineSeparator("c", "b", "a");
        assertEquals(expected, sortApplication.sortFromStdin(false, true, false, stdin));
    }

    @Test
    void sortFromStdin_CaseIndependent_CaseIndependentSortedList() throws Exception {
        InputStream stdin = generateInputStreamFromStrings("A", "C", "b");
        String expected = joinStringsByLineSeparator("A", "b", "C");
        assertEquals(expected, sortApplication.sortFromStdin(false, false, true, stdin));
    }

    // File

    @Test
    void sortFromFiles_NoFlags_ReturnsSortedList() throws Exception {
        createFile(TEST_FILE, joinStringsByLineSeparator("a", "c", "b"));
        String expected = joinStringsByLineSeparator("a", "b", "c");
        assertEquals(expected, sortApplication.sortFromFiles(false, false,
                false, TEST_FILE));
    }

    @Test
    void sortFromFiles_IsFirstWordNumber_ReturnsSortedList() throws Exception {
        createFile(TEST_FILE, joinStringsByLineSeparator("10 b", "5 c", "1 a"));
        String expected = joinStringsByLineSeparator("1 a", "5 c", "10 b");
        assertEquals(expected, sortApplication.sortFromFiles(true, false,
                false, TEST_FILE));
    }

    @Test
    void sortFromFiles_ReverseOrder_ReverseSortedList() throws Exception {
        createFile(TEST_FILE, joinStringsByLineSeparator("a", "c", "b"));
        String expected = joinStringsByLineSeparator("c", "b", "a");
        assertEquals(expected, sortApplication.sortFromFiles(false, true,
                false, TEST_FILE));
    }

    @Test
    void sortFromFiles_CaseIndependent_CaseIndependentSortedList() throws Exception {
        createFile(TEST_FILE, joinStringsByLineSeparator("A", "C", "b"));
        String expected = joinStringsByLineSeparator("A", "b", "C");
        assertEquals(expected, sortApplication.sortFromFiles(false, false,
                true, TEST_FILE));
    }
}