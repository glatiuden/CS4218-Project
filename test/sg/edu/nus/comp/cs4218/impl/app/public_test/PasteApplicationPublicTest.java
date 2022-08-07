package sg.edu.nus.comp.cs4218.impl.app.public_test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.impl.app.PasteApplication;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PasteApplicationPublicTest {
    private static final File DIRECTORY = new File("pasteTestDirectory");
    private static final File NONEXISTENT = new File("paste_nonexistent.txt");
    private static final File FILE_EMPTY = new File("paste_empty.txt");

    private static final String LINE_BREAK = System.lineSeparator();

    private static final File FILE_1 = new File("paste_1.txt");
    private static final String TEXT_FILE_1 = "A\nB\nC\nD\nE".replace("\n", LINE_BREAK);

    private static final File FILE_2 = new File("paste_2.txt");
    private static final String TEXT_FILE_2 = "1\n2\n3\n4\n5".replace("\n", LINE_BREAK);

    private static final String INTERLEAVE = "1\t2\t3\t4\t5\nA\tB\tC\tD\tE".replace("\n", LINE_BREAK);

    private static PasteApplication pasteApplication;
    private static InputStream inputStream;

    @BeforeAll
    static void setUpBeforeAll() throws IOException {
        writeToFileWithText(FILE_EMPTY, null);
        writeToFileWithText(FILE_1, TEXT_FILE_1);
        writeToFileWithText(FILE_2, TEXT_FILE_2);

        DIRECTORY.mkdirs();
    }

    public static void writeToFileWithText(File file, String text) throws IOException {
        FileWriter writer = new FileWriter(file); //NOPMD

        if (text == null || text.isBlank()) {
            writer.close();
            return;
        }

        writer.write(text);
        writer.close();
    }

    @AfterAll
    static void tearDownAfterAll() {
        FILE_EMPTY.delete();
        FILE_1.delete();
        FILE_2.delete();

        DIRECTORY.delete();
    }

    private void assertEqualsReplacingNewlines(String expected, String actual) {
        assertEquals(expected.replaceAll("\r\n", "\n"), actual.replaceAll("\r\n", "\n"));
    }

    @BeforeEach
    void setUp() {
        pasteApplication = new PasteApplication();
    }

    @Test
    void mergeFile_FileNotFound_ThrowsException() {
        assertThrows(Exception.class, () -> pasteApplication.mergeFile(true, NONEXISTENT.toString()));
    }

    @Test
    void mergeFile_FileIsDirectory_ThrowsException() {
        assertThrows(Exception.class, () -> pasteApplication.mergeFile(true, DIRECTORY.toString()));
    }

    @Test
    void mergeFileAndStdin_NullStream_ThrowsException() {
        assertThrows(Exception.class, () -> pasteApplication.mergeFileAndStdin(true, null));
    }

    @Test
    void mergeFileAndStdin_NullFilename_ThrowsException() {
        assertThrows(Exception.class, () -> pasteApplication.mergeFileAndStdin(true, inputStream, null));
    }

    @Test
    void mergeStdin_NullStream_ThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> pasteApplication.mergeStdin(true, null));
    }

    @Test
    void mergeStdin_NoSerial_ReturnsItself() throws Exception {
        InputStream stream = new ByteArrayInputStream(TEXT_FILE_1.getBytes());

        String result = pasteApplication.mergeStdin(false, stream);
        assertEquals(TEXT_FILE_1, result);
    }

    @Test
    void mergeStdin_Serial_ReturnsNewlinesReplacedByTabs() throws Exception {
        InputStream stream = new ByteArrayInputStream(TEXT_FILE_1.getBytes());

        String result = pasteApplication.mergeStdin(true, stream);
        assertEquals(TEXT_FILE_1.replaceAll(System.lineSeparator(), String.valueOf(TestStringUtils.CHAR_TAB)), result);
    }

    @Test
    void mergeFile_NullFilename_ThrowsException() {
        Exception exception = assertThrows(Exception.class, () -> pasteApplication.mergeFile(true, null));
    }

    @Test
    void mergeFile_NoSerialOneFile_ReturnsItself() throws Exception {
        String result = pasteApplication.mergeFile(false, FILE_1.toString());
        assertEqualsReplacingNewlines(TEXT_FILE_1, result);
    }


    @Test
    void mergeFile_NoSerialTwoFiles_ReturnsInterleaving() throws Exception {
        String expected = "A\t1\nB\t2\nC\t3\nD\t4\nE\t5";
        String result = pasteApplication.mergeFile(false, FILE_1.toString(), FILE_2.toString());
        assertEqualsReplacingNewlines(expected, result);
    }

    @Test
    void mergeFile_SerialTwoFiles_ReturnsParallel() throws Exception {
        String expected = "A\tB\tC\tD\tE\n1\t2\t3\t4\t5";
        String result = pasteApplication.mergeFile(true, FILE_1.toString(), FILE_2.toString());
        assertEqualsReplacingNewlines(expected, result);
    }
}
