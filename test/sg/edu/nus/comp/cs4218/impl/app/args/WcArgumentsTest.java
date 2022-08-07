package sg.edu.nus.comp.cs4218.impl.app.args;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.WcException;
import sg.edu.nus.comp.cs4218.impl.app.args.WcArguments;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FLAG;

public class WcArgumentsTest {

    static final String FILENAME = "file";
    static final String DASH = "-";

    WcArguments wcArg;

    @BeforeEach
    void setup() {
        wcArg = new WcArguments();
    }

    // Test Naming: Lines (T/F) / Words (T/F) / Byte (T/F) Files (Empty / 1 / Multiple)
    // Pairwise testing = 7 test cases
    // Category partition for out of order files + queries that gives similar values (e.g., -wlc = no option given)
    @Test
    public void parse_TFF1File_ReturnsTrue() throws WcException {
        String[] args = {"-l", FILENAME};
        wcArg.parse(args);
        assertTrue(wcArg.isLines());
        assertFalse(wcArg.isWords());
        assertFalse(wcArg.isBytes());
        assertEquals(wcArg.getFiles().size(), 1);
        assertEquals(wcArg.getFiles().get(0), FILENAME);
    }

    @Test
    public void parse_FFTNoFile_ReturnsTrue() throws WcException {
        String[] args = {"-c"};
        wcArg.parse(args);
        assertFalse(wcArg.isLines());
        assertFalse(wcArg.isWords());
        assertTrue(wcArg.isBytes());
        assertEquals(wcArg.getFiles().size(), 0);
    }

    @Test
    public void parse_FTT1File_ReturnsTrue() throws WcException {
        String[] args = {"-wc", FILENAME};
        wcArg.parse(args);
        assertFalse(wcArg.isLines());
        assertTrue(wcArg.isWords());
        assertTrue(wcArg.isBytes());
        assertEquals(wcArg.getFiles().size(), 1);
        assertEquals(wcArg.getFiles().get(0), FILENAME);
    }

    @Test
    public void parse_FTFMultiFile_ReturnsTrue() throws WcException {
        String[] args = {"-w", FILENAME, FILENAME};
        wcArg.parse(args);
        assertFalse(wcArg.isLines());
        assertTrue(wcArg.isWords());
        assertFalse(wcArg.isBytes());
        assertEquals(wcArg.getFiles().size(), 2);
        assertEquals(wcArg.getFiles().get(0), FILENAME);
        assertEquals(wcArg.getFiles().get(1), FILENAME);
    }

    @Test
    public void parse_TTFEmpty_ReturnsTrue() throws WcException {
        String[] args = {"-w", "-l"};
        wcArg.parse(args);
        assertTrue(wcArg.isLines());
        assertTrue(wcArg.isWords());
        assertFalse(wcArg.isBytes());
        assertEquals(wcArg.getFiles().size(), 0);
    }

    @Test
    public void parse_TFTMulti_ReturnsTrue() throws WcException {
        String[] args = {"-l", "-c", FILENAME, FILENAME, DASH, FILENAME, FILENAME};
        wcArg.parse(args);
        assertTrue(wcArg.isLines());
        assertFalse(wcArg.isWords());
        assertTrue(wcArg.isBytes());
        assertEquals(wcArg.getFiles().size(), 5);
        assertEquals(wcArg.getFiles().get(0), FILENAME);
        assertEquals(wcArg.getFiles().get(1), FILENAME);
        assertEquals(wcArg.getFiles().get(2), DASH);
        assertEquals(wcArg.getFiles().get(3), FILENAME);
        assertEquals(wcArg.getFiles().get(4), FILENAME);
    }

    @Test
    public void parse_TTTMultiFile_ReturnsTrue() throws WcException {
        String[] args = {"-lcw", FILENAME, FILENAME, FILENAME};
        wcArg.parse(args);
        assertTrue(wcArg.isLines());
        assertTrue(wcArg.isWords());
        assertTrue(wcArg.isBytes());
        assertEquals(wcArg.getFiles().size(), 3);
        assertEquals(wcArg.getFiles().get(0), FILENAME);
        assertEquals(wcArg.getFiles().get(1), FILENAME);
        assertEquals(wcArg.getFiles().get(2), FILENAME);
    }

    // Check for ordering of values doesn't affect correctness
    @Test
    public void parse_TTTOtherOrder_ReturnsTrue() throws WcException {
        String[] args = {"-cwl"};
        wcArg.parse(args);
        assertTrue(wcArg.isLines());
        assertTrue(wcArg.isWords());
        assertTrue(wcArg.isBytes());
    }

    @Test
    public void parse_TTTOptionSplitOut_ReturnsTrue() throws WcException {
        String[] args = {"-c", "-l", "-w"};
        wcArg.parse(args);
        assertTrue(wcArg.isLines());
        assertTrue(wcArg.isWords());
        assertTrue(wcArg.isBytes());
    }

    @Test
    public void parse_TTTNoOption_ReturnsTrue() throws WcException {
        String[] args = {};
        wcArg.parse(args);
        assertTrue(wcArg.isLines());
        assertTrue(wcArg.isWords());
        assertTrue(wcArg.isBytes());
    }

    @Test
    public void parse_DuplicateOption_ReturnsTrue() throws WcException {
        String[] args = {"-c", "-l", "-cl", "-l"};
        wcArg.parse(args);
        assertTrue(wcArg.isLines());
        assertFalse(wcArg.isWords());
        assertTrue(wcArg.isBytes());
    }

    @Test
    public void parse_UnknownOption_ThrowsError() {
        String[] args = {"-c", "-d"};
        WcException exception = assertThrows(WcException.class, () -> wcArg.parse(args));
        assertTrue(exception.toString().contains(ERR_INVALID_FLAG));
    }

    @Test
    public void parse_UnknownOptions_ThrowsError() {
        String[] args = {"-cld"};
        WcException exception = assertThrows(WcException.class, () -> wcArg.parse(args));
        assertTrue(exception.toString().contains(ERR_INVALID_FLAG));
    }
}
