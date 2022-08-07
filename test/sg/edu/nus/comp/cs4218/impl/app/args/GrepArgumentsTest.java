package sg.edu.nus.comp.cs4218.impl.app.args;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.GrepException;
import sg.edu.nus.comp.cs4218.impl.app.args.GrepArguments;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class GrepArgumentsTest {

    private static final String FILENAME = "file";
    private static final String DASH = "-";
    private static final String VALID_PATTERN = "cs4218";
    private static final String INVALID_PATTERN = "Test\\";
    private GrepArguments grepArg;

    @BeforeEach
    void setupGrepArgumentsTest() {
        grepArg = new GrepArguments();
    }

    @Test
    public void parse_IsCaseInsensitiveFlagWithoutFile_ShouldReturnTrue() throws Exception {
        String[] args = {"-i", VALID_PATTERN};
        grepArg.parse(args);
        assertTrue(grepArg.isCaseInsensitive());
        assertFalse(grepArg.isCountOfLinesOnly());
        assertFalse(grepArg.isPrefixFileName());
        assertEquals(VALID_PATTERN, grepArg.getPattern());
        assertEquals(0, grepArg.getFiles().size());
    }

    @Test
    public void parse_IsPrefixFileNameWithFiles_ShouldReturnTrue() throws Exception {
        String[] args = {"-H", VALID_PATTERN, FILENAME};
        grepArg.parse(args);
        assertFalse(grepArg.isCaseInsensitive());
        assertFalse(grepArg.isCountOfLinesOnly());
        assertTrue(grepArg.isPrefixFileName());
        assertEquals(1, grepArg.getFiles().size());
        assertEquals(FILENAME, grepArg.getFiles().get(0));
    }

    @Test
    public void parse_IsCountLinesAndFilePrefixFlagsWithoutFile_ShouldReturnTrue() throws Exception {
        String[] args = {"-c", "-H", VALID_PATTERN};
        grepArg.parse(args);
        assertFalse(grepArg.isCaseInsensitive());
        assertTrue(grepArg.isCountOfLinesOnly());
        assertTrue(grepArg.isPrefixFileName());
        assertEquals(0, grepArg.getFiles().size());
    }

    @Test
    public void parse_IsCountLinesWithFiles_ShouldReturnTrue() throws Exception {
        String[] args = {"-c", VALID_PATTERN, FILENAME, FILENAME};
        grepArg.parse(args);
        assertFalse(grepArg.isCaseInsensitive());
        assertTrue(grepArg.isCountOfLinesOnly());
        assertFalse(grepArg.isPrefixFileName());
        assertEquals(2, grepArg.getFiles().size());
        assertEquals(FILENAME, grepArg.getFiles().get(0));
        assertEquals(FILENAME, grepArg.getFiles().get(1));
    }

    @Test
    public void parse_IsCaseInsensitiveAndCountLinesFlagWthFiles_ShouldReturnTrue() throws Exception {
        String[] args = {"-i", "-c", VALID_PATTERN, FILENAME, FILENAME, DASH, FILENAME, FILENAME};
        grepArg.parse(args);
        assertTrue(grepArg.isCaseInsensitive());
        assertTrue(grepArg.isCountOfLinesOnly());
        assertFalse(grepArg.isPrefixFileName());
        assertEquals(5, grepArg.getFiles().size());
        assertEquals(FILENAME, grepArg.getFiles().get(0));
        assertEquals(FILENAME, grepArg.getFiles().get(1));
        assertEquals(DASH, grepArg.getFiles().get(2));
        assertEquals(FILENAME, grepArg.getFiles().get(3));
        assertEquals(FILENAME, grepArg.getFiles().get(4));
    }

    @Test
    public void parse_AllFlagsWithoutFile_ShouldReturnTrue() throws Exception {
        String[] args = {"-icH", VALID_PATTERN};
        grepArg.parse(args);
        assertTrue(grepArg.isCaseInsensitive());
        assertTrue(grepArg.isCountOfLinesOnly());
        assertTrue(grepArg.isPrefixFileName());
        assertEquals(0, grepArg.getFiles().size());
    }

    @Test
    public void parse_IsCaseInsensitiveAndFilePrefixWithFiles_ShouldReturnTrue() throws Exception {
        String[] args = {"-i", "-H", VALID_PATTERN, FILENAME, DASH};
        grepArg.parse(args);
        assertTrue(grepArg.isCaseInsensitive());
        assertFalse(grepArg.isCountOfLinesOnly());
        assertTrue(grepArg.isPrefixFileName());
        assertEquals(2, grepArg.getFiles().size());
        assertEquals(FILENAME, grepArg.getFiles().get(0));
        assertEquals(DASH, grepArg.getFiles().get(1));
    }

    @Test
    public void parse_AllFlagsRandomOrder_ShouldReturnTrue() throws Exception {
        String[] args = {"-Hci", VALID_PATTERN};
        grepArg.parse(args);
        assertTrue(grepArg.isCaseInsensitive());
        assertTrue(grepArg.isCountOfLinesOnly());
        assertTrue(grepArg.isPrefixFileName());
    }

    @Test
    public void parse_AllFlagsSplitOut_ShouldReturnTrue() throws Exception {
        String[] args = {"-i", "-c", "-H", VALID_PATTERN};
        grepArg.parse(args);
        assertTrue(grepArg.isCaseInsensitive());
        assertTrue(grepArg.isCountOfLinesOnly());
        assertTrue(grepArg.isPrefixFileName());
    }

    @Test
    public void parse_PatternOnly_ShouldReturnTrue() throws Exception {
        String[] args = {VALID_PATTERN};
        grepArg.parse(args);
        assertFalse(grepArg.isCaseInsensitive());
        assertFalse(grepArg.isCountOfLinesOnly());
        assertFalse(grepArg.isPrefixFileName());
    }

    @Test
    public void parse_DuplicateFlags_ShouldReturnTrue() throws Exception {
        String[] args = {"-c", "-i", "-ci", "-i", VALID_PATTERN};
        grepArg.parse(args);
        assertTrue(grepArg.isCaseInsensitive());
        assertTrue(grepArg.isCountOfLinesOnly());
    }

    @Test
    public void parse_NoPatternFlagsAndFiles_ShouldThrowNoRegexException() {
        String[] args = {};
        Exception exception = assertThrows(Exception.class, () -> grepArg.parse(args));
        assertTrue(exception.toString().contains(ERR_NO_REGEX));
    }

    @Test
    public void parse_FlagsWithNoPattern_ShouldThrowNullArgsException() {
        String[] args = {"-i", "-c", "-H"};
        Exception exception = assertThrows(Exception.class, () -> grepArg.parse(args));
        assertTrue(exception.toString().contains(ERR_NULL_ARGS));
    }

    @Test
    public void parse_InvalidPattern_ShouldThrowNoRegexException() {
        String[] args = {INVALID_PATTERN};
        Exception exception = assertThrows(Exception.class, () -> grepArg.parse(args));
        assertTrue(exception.toString().contains(ERR_INVALID_REGEX));
    }

    @Test
    public void parse_InvalidFlag_ShouldThrowInvalidFlagsException() {
        String[] args = {"-g"};
        Throwable thrown = assertThrows(GrepException.class, () -> grepArg.parse(args));
        assertTrue(thrown.toString().contains(ERR_INVALID_FLAG));
    }

    @Test
    public void parse_InvalidFlags_ShouldThrowInvalidFlagsException() {
        String[] args = {"-qwe"};
        Throwable thrown = assertThrows(GrepException.class, () -> grepArg.parse(args));
        assertTrue(thrown.toString().contains(ERR_INVALID_FLAG));
    }

    @Test
    public void parse_NullPattern_ShouldThrowInvalidFlagsException() {
        Throwable thrown = assertThrows(Exception.class, () -> grepArg.parse((String[]) null));
        assertTrue(thrown.toString().contains(ERR_NULL_ARGS));
    }

    @Test
    public void parse_EmptyPattern_ShouldThrowInvalidFlagsException() {
        String[] args = {" "};
        Throwable thrown = assertThrows(Exception.class, () -> grepArg.parse(args));
        assertTrue(thrown.toString().contains(ERR_EMPTY_REGEX));
    }

    @Test
    public void validatePattern_ValidPattern_ShouldReturnTrue() {
        assertDoesNotThrow(() -> GrepArguments.validate(VALID_PATTERN));
    }

    @Test
    public void validatePattern_InvalidPattern_ShouldThrowInvalidFlagsException() {
        Throwable thrown = assertThrows(Exception.class, () -> GrepArguments.validate(INVALID_PATTERN));
        assertTrue(thrown.toString().contains(ERR_INVALID_REGEX));
    }

    @Test
    public void validatePattern_NullPattern_ShouldThrowInvalidFlagsException() {
        Throwable thrown = assertThrows(Exception.class, () -> GrepArguments.validate(null));
        assertTrue(thrown.toString().contains(ERR_NULL_ARGS));
    }

    @Test
    public void validatePattern_EmptyPattern_ShouldThrowInvalidFlagsException() {
        Throwable thrown = assertThrows(Exception.class, () -> GrepArguments.validate(""));
        assertTrue(thrown.toString().contains(ERR_EMPTY_REGEX));
    }
}
