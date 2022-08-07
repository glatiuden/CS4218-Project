package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PasteArgsParserTest {
    private final static String FILE_A = "a.txt";
    private final static String FILE_B = "b.txt";
    private final static String STD_IN = "-";
    private final static String SERIAL_FLAG = "-s";
    private final static String INVALID_FLAG = "-i";
    private final static String TEXT_EMPTY = "";

    @TempDir
    public static Path folderPath;

    private static Path fileAPath;
    private static Path fileBPath;

    private static PasteArgsParser parser;

    @BeforeAll
    public static void setup() {
        fileAPath = folderPath.resolve(FILE_A);
        fileBPath = folderPath.resolve(FILE_B);
    }

    @BeforeEach
    public void start() throws IOException {
        parser = new PasteArgsParser();
        Files.createDirectories(folderPath);
        Files.write(fileAPath, TEXT_EMPTY.getBytes());
        Files.write(fileBPath, TEXT_EMPTY.getBytes());
    }

    @Test
    public void isSerial_serialFlagNonFlag_true() throws InvalidArgsException {
        parser.parse(SERIAL_FLAG, fileAPath.toString());
        assertTrue(parser.isSerial());
    }

    @Test
    public void isSerial_serialFlagSerialFlag_true() throws InvalidArgsException {
        parser.parse(SERIAL_FLAG, SERIAL_FLAG);
        assertTrue(parser.isSerial());
    }

    @Test
    public void isSerial_serialFlagInvalidFlag_throwException() {
        assertThrows(InvalidArgsException.class, () -> parser.parse(SERIAL_FLAG, INVALID_FLAG));
    }

    @Test
    public void isSerial_nonFlagNonFlag_false() throws InvalidArgsException {
        parser.parse(fileAPath.toString(), fileBPath.toString());
        assertFalse(parser.isSerial());
    }

    @Test
    public void isFilesOnly_fileFile_true() throws InvalidArgsException {
        parser.parse(fileAPath.toString(), fileBPath.toString());
        assertTrue(parser.isFilesOnly());
    }

    @Test
    public void isFilesOnly_fileStdin_false() throws InvalidArgsException {
        parser.parse(fileAPath.toString(), STD_IN);
        assertFalse(parser.isFilesOnly());
    }

    @Test
    public void isFilesOnly_fileFlag_true() throws InvalidArgsException {
        parser.parse(fileAPath.toString(), SERIAL_FLAG);
        assertTrue(parser.isFilesOnly());
    }

    @Test
    public void isFilesOnly_stdinStdin_false() throws InvalidArgsException {
        parser.parse(STD_IN, STD_IN);
        assertFalse(parser.isFilesOnly());
    }

    @Test
    public void isFilesOnly_stdinFlag_false() throws InvalidArgsException {
        parser.parse(STD_IN, SERIAL_FLAG);
        assertFalse(parser.isFilesOnly());
    }

    @Test
    public void isFilesOnly_flagFlag_false() throws InvalidArgsException {
        parser.parse(SERIAL_FLAG, SERIAL_FLAG);
        assertFalse(parser.isFilesOnly());
    }

    @Test
    public void isStdinOnly_fileFile_false() throws InvalidArgsException {
        parser.parse(fileAPath.toString(), fileBPath.toString());
        assertFalse(parser.isStdinOnly());
    }

    @Test
    public void isStdinOnly_fileStdin_false() throws InvalidArgsException {
        parser.parse(fileAPath.toString(), STD_IN);
        assertFalse(parser.isStdinOnly());
    }

    @Test
    public void isStdinOnly_fileFlag_false() throws InvalidArgsException {
        parser.parse(fileAPath.toString(), SERIAL_FLAG);
        assertFalse(parser.isStdinOnly());
    }

    @Test
    public void isStdinOnly_stdinStdin_true() throws InvalidArgsException {
        parser.parse(STD_IN, STD_IN);
        assertTrue(parser.isStdinOnly());
    }

    @Test
    public void isStdinOnly_stdinFlag_true() throws InvalidArgsException {
        parser.parse(STD_IN, SERIAL_FLAG);
        assertTrue(parser.isStdinOnly());
    }

    @Test
    public void isStdinOnly_flagFlag_false() throws InvalidArgsException {
        parser.parse(SERIAL_FLAG, SERIAL_FLAG);
        assertFalse(parser.isStdinOnly());
    }

    @Test
    public void getInputs_fileFile_listWithBothFileNames() throws InvalidArgsException {
        List<String> expected = new ArrayList<>();
        expected.add(fileAPath.toString());
        expected.add(fileBPath.toString());
        parser.parse(fileAPath.toString(), fileBPath.toString());
        assertEquals(expected, parser.getInputs());
    }

    @Test
    public void getInputs_fileStdin_listWithFileNameAndStdin() throws InvalidArgsException {
        List<String> expected = new ArrayList<>();
        expected.add(fileAPath.toString());
        expected.add(STD_IN);
        parser.parse(fileAPath.toString(), STD_IN);
        assertEquals(expected, parser.getInputs());
    }

    @Test
    public void getInputs_fileFlag_listWithFileName() throws InvalidArgsException {
        List<String> expected = new ArrayList<>();
        expected.add(fileAPath.toString());
        expected.add(fileBPath.toString());
        parser.parse(fileAPath.toString(), fileBPath.toString());
        assertEquals(expected, parser.getInputs());
    }

    @Test
    public void getInputs_stdinStdin_listWithStdins() throws InvalidArgsException {
        List<String> expected = new ArrayList<>();
        expected.add(STD_IN);
        expected.add(STD_IN);
        parser.parse(STD_IN, STD_IN);
        assertEquals(expected, parser.getInputs());
    }

    @Test
    public void getInputs_stdinFlag_ListWithStdin() throws InvalidArgsException {
        List<String> expected = new ArrayList<>();
        expected.add(STD_IN);
        parser.parse(STD_IN, SERIAL_FLAG);
        assertEquals(expected, parser.getInputs());
    }

    @Test
    public void getInputs_flagFlag_emptyList() throws InvalidArgsException {
        List<String> expected = new ArrayList<>();
        parser.parse(SERIAL_FLAG, SERIAL_FLAG);
        assertEquals(expected, parser.getInputs());
    }
}
