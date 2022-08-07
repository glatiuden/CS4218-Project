package sg.edu.nus.comp.cs4218.impl.app.public_test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.impl.app.UniqApplication;
import sg.edu.nus.comp.cs4218.testutils.TestStringUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

@SuppressWarnings("PMD") // Not required to check for given test from the prof
public class UniqApplicationPublicTest {
    private static final File TEMP = new File("temp-uniq");
    private static final File NONEXISTENT = new File("uniq_nonexistent.txt");
    private static final File FILE_EMPTY = new File("uniq_empty.txt");
    private static final File OUTPUT = new File("output.txt");

    private static final File FILE_NO_ADJ_DUP = new File("uniq_no_duplicates.txt");
    private static final String TEST_NO_ADJ_DUP = "Hello World" + STRING_NEWLINE + "Alice" + STRING_NEWLINE + "Bob" + STRING_NEWLINE + "Hello World" + STRING_NEWLINE + "Bob" + STRING_NEWLINE + "Alice" + STRING_NEWLINE + "CS4218";

    private static final File FILE_ALL_DUP = new File("uniq_all_duplicates.txt");
    private static final String TEST_CS4218 = "CS4218" + STRING_NEWLINE;
    private static final String TEST_ALL_DUP = TEST_CS4218.repeat(50); // NOPMD

    private static final File FILE_MIXED_DUP = new File("uniq_interleaved_duplicates.txt");
    private static final String TEST_CS1101S = "CS1101S" + STRING_NEWLINE;
    private static final String TEST_MIXED_DUP = TEST_CS4218.repeat(10) + TEST_CS1101S + // NOPMD
            TEST_CS4218.repeat(3) + TEST_CS4218.repeat(3) + TEST_CS1101S.repeat(20) + TEST_CS4218.repeat(2);

    private static UniqApplication uniqApplication;

    @BeforeAll
    static void setUpBeforeAll() throws IOException {
        writeToFileWithText(FILE_EMPTY, null);
        writeToFileWithText(FILE_NO_ADJ_DUP, TEST_NO_ADJ_DUP);
        writeToFileWithText(FILE_ALL_DUP, TEST_ALL_DUP);
        writeToFileWithText(FILE_MIXED_DUP, TEST_MIXED_DUP);

        TEMP.mkdirs();
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
        FILE_NO_ADJ_DUP.delete();
        FILE_ALL_DUP.delete();
        FILE_MIXED_DUP.delete();

        TEMP.delete();
        OUTPUT.delete();
    }

    @BeforeEach
    void setUp() {
        uniqApplication = new UniqApplication();
        uniqApplication.setStdout(System.out);
    }

    @Test
    void uniqFromFile_EmptyFile_ReturnsEmpty() {
        assertDoesNotThrow(() -> {
            String result = uniqApplication.uniqFromFile(false, false, false, FILE_EMPTY.toString(), "");
            assertTrue(result.isBlank());
        });
    }

    @Test
    void uniqFromFile_FileNoDuplicatesNoArguments_EqualToItself() {
        assertDoesNotThrow(() -> {
            String result = uniqApplication.uniqFromFile(false, false, false, FILE_NO_ADJ_DUP.toString(), "");
            assertEquals(TEST_NO_ADJ_DUP, result);
        });
    }

    @Test
    void uniqFromFile_FileNoDuplicatesCountOnly_AllOneCounts() {
        assertDoesNotThrow(() -> {
            String expected = "\t1 Hello World" + STRING_NEWLINE + "\t1 Alice" + STRING_NEWLINE + "\t1 Bob" + STRING_NEWLINE + "\t1 Hello World" + STRING_NEWLINE + "\t1 Bob" + STRING_NEWLINE + "\t1 Alice" + STRING_NEWLINE + "\t1 CS4218";
            String result = uniqApplication.uniqFromFile(true, false, false, FILE_NO_ADJ_DUP.toString(), "");
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_FileNoDuplicatesRepeatedOnly_ReturnsEmpty() {
        assertDoesNotThrow(() -> {
            String result = uniqApplication.uniqFromFile(false, true, false, FILE_NO_ADJ_DUP.toString(), "");
            assertTrue(result.isBlank());
        });
    }

    @Test
    void uniqFromFile_FileNoDuplicatesAllRepeatedOnly_ReturnsEmpty() {
        assertDoesNotThrow(() -> {
            String result = uniqApplication.uniqFromFile(false, false, true, FILE_NO_ADJ_DUP.toString(), "");
            assertTrue(result.isBlank());
        });
    }

    @Test
    void uniqFromFile_FileAllDuplicatesNoArguments_OnlyOneResult() {
        assertDoesNotThrow(() -> {
            String expected = "CS4218";
            String result = uniqApplication.uniqFromFile(false, false, false, FILE_ALL_DUP.toString(), "");
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_FileAllDuplicatesCountOnly_ReturnsCount() {
        assertDoesNotThrow(() -> {
            String expected = "\t50 CS4218"; // NOPMD
            String result = uniqApplication.uniqFromFile(true, false, false, FILE_ALL_DUP.toString(), "");
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_FileAllDuplicatesRepeatedOnly_OnlyOneResult() {
        assertDoesNotThrow(() -> {
            String expected = "CS4218";
            String result = uniqApplication.uniqFromFile(false, true, false, FILE_ALL_DUP.toString(), "");
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_FileAllDuplicatesAllRepeatedOnly_ReturnsItself() {
        assertDoesNotThrow(() -> {
            String result = uniqApplication.uniqFromFile(false, false, true, FILE_ALL_DUP.toString(), "");
            // Edit expected result to eliminate last "\r\n"
            assertEquals(TEST_CS4218.repeat(49) + "CS4218", result);
        });
    }

    @Test
    void uniqFromFile_FileAllDuplicatesCountAndRepeatedOnly_ReturnsCount() {
        assertDoesNotThrow(() -> {
            String expected = "\t50 CS4218";
            String result = uniqApplication.uniqFromFile(true, true, false, FILE_ALL_DUP.toString(), "");
            assertEquals(expected, result);
        });
    }

    // Deleted test case since -cD flag was supplied but gave results. My assumption will throw exception when -cD flag supplied.

    @Test
    void uniqFromFile_FileAllDuplicatesRepeatedAndAllRepeatedOnly_ReturnsItself() {
        assertDoesNotThrow(() -> {
            String result = uniqApplication.uniqFromFile(false, true, true, FILE_ALL_DUP.toString(), "");
            // Edit expected result to eliminate last "\r\n"
            assertEquals(TEST_CS4218.repeat(49) + "CS4218", result);
        });
    }

    // Deleted test case since -cD flag was supplied but gave results. My assumption will throw exception when -cD flag supplied.

    @Test
    void uniqFromFile_FileInterleavedDuplicatesNoArguments_Success() {
        assertDoesNotThrow(() -> {
            String expected = "CS4218" + STRING_NEWLINE + "CS1101S" + STRING_NEWLINE + "CS4218" + STRING_NEWLINE + "CS1101S" + STRING_NEWLINE + "CS4218";
            String result = uniqApplication.uniqFromFile(false, false, false, FILE_MIXED_DUP.toString(), "");
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_FileInterleavedDuplicatesCountOnly_Success() {
        assertDoesNotThrow(() -> {
            String expected = "\t10 CS4218" + STRING_NEWLINE + "\t1 CS1101S" + STRING_NEWLINE + "\t6 CS4218" + STRING_NEWLINE + "\t20 CS1101S" + STRING_NEWLINE + "\t2 CS4218";
            String result = uniqApplication.uniqFromFile(true, false, false, FILE_MIXED_DUP.toString(), "");
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_FileInterleavedDuplicatesRepeatedOnly_Success() {
        assertDoesNotThrow(() -> {
            String expected = "CS4218" + STRING_NEWLINE + "CS4218" + STRING_NEWLINE + "CS1101S" + STRING_NEWLINE + "CS4218";
            String result = uniqApplication.uniqFromFile(false, true, false, FILE_MIXED_DUP.toString(), "");
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_FileInterleavedDuplicatesAllRepeatedOnly_Success() {
        assertDoesNotThrow(() -> {
            String expected = TEST_CS4218.repeat(16) + TEST_CS1101S.repeat(20) + TEST_CS4218 + "CS4218";
            String result = uniqApplication.uniqFromFile(false, false, true, FILE_MIXED_DUP.toString(), "");
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromFile_NonExistentFile_Throws() {
        Exception exception = assertThrows(Exception.class, () -> uniqApplication.uniqFromFile(true, true, true,
                NONEXISTENT.toString(), ""));
    }

    @Test
    void uniqFromFile_Directory_Throws() {
        Exception exception = assertThrows(Exception.class, () -> uniqApplication.uniqFromFile(true, true, true,
                TEMP.toString(), ""));
    }

    @Test
    void uniqFromStdIn_NullStream_ThrowsException() {
        Exception exception = assertThrows(Exception.class, () ->
                uniqApplication.uniqFromStdin(false, false, false, null, "")
        );

    }

    @Test
    void uniqFromStdIn_EmptyFile_ReturnsEmpty() {
        assertDoesNotThrow(() -> {
            InputStream stream = new ByteArrayInputStream("".getBytes());

            String result = uniqApplication.uniqFromStdin(false, false, false, stream, "");
            assertTrue(result.isBlank());
        });
    }

    @Test
    void uniqFromStdIn_NoAdjacentDuplicates_Success() {
        assertDoesNotThrow(() -> {
            InputStream stream = new ByteArrayInputStream(TEST_NO_ADJ_DUP.getBytes());

            String result = uniqApplication.uniqFromStdin(false, false, false, stream, "");
            assertEquals(TEST_NO_ADJ_DUP, result);
        });
    }

    @Test
    void uniqFromStdIn_AllDuplicates_Success() {
        assertDoesNotThrow(() -> {
            String expected = "CS4218";
            InputStream stream = new ByteArrayInputStream(TEST_ALL_DUP.getBytes());

            String result = uniqApplication.uniqFromStdin(false, false, false, stream, "");
            assertEquals(expected, result);
        });
    }

    @Test
    void uniqFromStdIn_InterleavedDuplicates_Success() {
        assertDoesNotThrow(() -> {
            String expected = "CS4218" + STRING_NEWLINE + "CS1101S" + STRING_NEWLINE + "CS4218" + STRING_NEWLINE + "CS1101S" + STRING_NEWLINE + "CS4218";
            InputStream stream = new ByteArrayInputStream(TEST_MIXED_DUP.getBytes());

            String result = uniqApplication.uniqFromStdin(false, false, false, stream, "");
            assertEquals(expected, result);
        });
    }

}
