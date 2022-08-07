package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_ARG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FLAG;

public class LsArgsParserTest {
    static final String FILENAME = "file";
    LsArgsParser lsArgs;

    @BeforeEach
    void setup() {
        lsArgs = new LsArgsParser();
    }

    // Test Naming: Recursive (T/F) / Sort (T/F),  Files (Empty / 1 / Multiple)
    // Do Combination for boolean, and then within each test case, alternate for the number of files
    @Test
    public void parse_FFEmptyFile_ReturnsTrue() throws InvalidArgsException {
        String[] args = {};
        lsArgs.parse(args);
        assertFalse(lsArgs.isRecursive());
        assertFalse(lsArgs.isSortByExt());
        assertEquals(lsArgs.getDirectories().size(), 0);
    }

    @Test
    public void parse_FT1File_ReturnsTrue() throws InvalidArgsException {
        String[] args = {"-R", FILENAME};
        lsArgs.parse(args);
        assertTrue(lsArgs.isRecursive());
        assertFalse(lsArgs.isSortByExt());
        assertEquals(lsArgs.getDirectories().size(), 1);
    }

    @Test
    public void parse_TFMultiFile_ReturnsTrue() throws InvalidArgsException {
        String[] args = {"-X", "-", FILENAME, FILENAME, FILENAME};
        lsArgs.parse(args);
        assertFalse(lsArgs.isRecursive());
        assertTrue(lsArgs.isSortByExt());
        assertEquals(lsArgs.getDirectories().size(), 4);
    }

    @Test
    public void parse_TTMultiFile_ReturnsTrue() throws InvalidArgsException {
        String[] args = {"-XR", FILENAME, FILENAME};
        lsArgs.parse(args);
        assertTrue(lsArgs.isRecursive());
        assertTrue(lsArgs.isSortByExt());
        assertEquals(lsArgs.getDirectories().size(), 2);
    }

    // Test for out of order options
    @Test
    public void parse_TTSplitUp_ReturnsTrue() throws InvalidArgsException {
        String[] args = {"-X", "-R"};
        lsArgs.parse(args);
        assertTrue(lsArgs.isRecursive());
        assertTrue(lsArgs.isSortByExt());
    }

    @Test
    public void parse_DuplicatedOption_ReturnsTrue() throws InvalidArgsException {
        String[] args = {"-R", "-XR"};
        lsArgs.parse(args);
        assertTrue(lsArgs.isRecursive());
        assertTrue(lsArgs.isSortByExt());
    }

    // Note: LS allows that the options are listed anywhere
    @Test
    public void parse_OptionsAfterFile_ReturnsTrue() throws InvalidArgsException {
        String[] args = {FILENAME, "-X", "-R"};
        lsArgs.parse(args);
        assertTrue(lsArgs.isRecursive());
        assertTrue(lsArgs.isSortByExt());
    }

    @Test
    public void parse_UnknownOption_ThrowsError() {
        String[] args = {"-c", "-d"};
        InvalidArgsException exception = assertThrows(InvalidArgsException.class, () -> lsArgs.parse(args));
        assertTrue(exception.toString().contains(ERR_INVALID_FLAG));
    }

    @Test
    public void parse_UnknownOptions_ThrowsError() {
        String[] args = {"-cX"};
        InvalidArgsException exception = assertThrows(InvalidArgsException.class, () -> lsArgs.parse(args));
        assertTrue(exception.toString().contains(ERR_INVALID_FLAG));
    }

    @Test
    public void parse_underCaseOption_ThrowsError() {
        String[] args = {"-x"};
        InvalidArgsException exception = assertThrows(InvalidArgsException.class, () -> lsArgs.parse(args));
        assertTrue(exception.toString().contains(ERR_INVALID_FLAG));
    }

    // Fix for bug 2
    @Test
    public void parse_emptyArgs_ThrowsError() {
        String[] args = {""};
        InvalidArgsException exception = assertThrows(InvalidArgsException.class, () -> lsArgs.parse(args));
        assertTrue(exception.toString().contains(ERR_INVALID_ARG));
    }
}
