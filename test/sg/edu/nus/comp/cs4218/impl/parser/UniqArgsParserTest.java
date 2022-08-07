package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FLAG;

class UniqArgsParserTest {
    private final static String FILE_NO_SPACE = "possibleFileName";
    private final static String FILE_SPACE = "possible file name";
    private final static String FILE_EXT = "file.txt";
    private final static String INVALID_FILE_DASH = "-file";
    private final static String EMPTY = "";
    private final static String SPACE = " ";
    private final static String C_FLAG = "-c";
    private final static String D_FLAG = "-d";
    private final static String CAP_D_FLAG = "-D";
    private final static String CD_FLAG = "-cd";
    private final static String C_CAPD_FLAG = "-Dc";
    private final static String D_CAPD_FLAG = "-dD";
    private final static String C_D_CAPD_FLAG = "-dDc";
    private final static String REPEATED_FLAG = "-cddc";
    private final static String INVALID_FLAG_1 = "-g";
    private final static String INVALID_FLAG_2 = "-cgd";
    private final static String INVALID_FLAG_3 = "-crgd";
    private static UniqArgsParser uniqParser;

    @BeforeEach
    public void setUp() {
        uniqParser = new UniqArgsParser();
    }

    // empty arguments
    @Test
    public void parse_emptyArgs_emptyAllFalse() throws InvalidArgsException {
        String[] args = {};
        uniqParser.parse(args);
        assertEquals(Arrays.asList(), uniqParser.getFiles());
        assertFalse(uniqParser.isCount());
        assertFalse(uniqParser.isRepeated());
        assertFalse(uniqParser.isAllRepeated());
    }

    // no flags with multiple files
    @Test
    public void parse_noFlagMultiFiles_allFalseMultiFiles() throws InvalidArgsException {
        String[] args = {FILE_NO_SPACE, FILE_SPACE};
        uniqParser.parse(args);
        assertEquals(Arrays.asList(FILE_NO_SPACE, FILE_SPACE), uniqParser.getFiles());
        assertFalse(uniqParser.isCount());
        assertFalse(uniqParser.isRepeated());
        assertFalse(uniqParser.isAllRepeated());
    }

    // c flag with multiple files
    @Test
    public void parse_cFlagMultiFiles_cTrueMultiFiles() throws InvalidArgsException {
        String[] args = {FILE_NO_SPACE, C_FLAG, FILE_SPACE};
        uniqParser.parse(args);
        assertEquals(Arrays.asList(FILE_NO_SPACE, FILE_SPACE), uniqParser.getFiles());
        assertTrue(uniqParser.isCount());
        assertFalse(uniqParser.isRepeated());
        assertFalse(uniqParser.isAllRepeated());
    }

    // d flag with single file
    @Test
    public void parse_dFlagSingleFile_dTrueSingleFile() throws InvalidArgsException {
        String[] args = {D_FLAG, FILE_EXT};
        uniqParser.parse(args);
        assertEquals(Arrays.asList(FILE_EXT), uniqParser.getFiles());
        assertFalse(uniqParser.isCount());
        assertTrue(uniqParser.isRepeated());
        assertFalse(uniqParser.isAllRepeated());
    }

    // D flag with no file
    @Test
    public void parse_DFlagNoFile_DTrueEmpty() throws InvalidArgsException {
        String[] args = {CAP_D_FLAG};
        uniqParser.parse(args);
        assertEquals(Arrays.asList(), uniqParser.getFiles());
        assertFalse(uniqParser.isCount());
        assertFalse(uniqParser.isRepeated());
        assertTrue(uniqParser.isAllRepeated());
    }

    // cd flag separate with multiple files
    @Test
    public void parse_cdFlagSepMultiFiles_cdTrueMultiFiles() throws InvalidArgsException {
        String[] args = {D_FLAG, C_FLAG, FILE_SPACE, FILE_EXT, FILE_NO_SPACE};
        uniqParser.parse(args);
        assertEquals(Arrays.asList(FILE_SPACE, FILE_EXT, FILE_NO_SPACE), uniqParser.getFiles());
        assertTrue(uniqParser.isCount());
        assertTrue(uniqParser.isRepeated());
        assertFalse(uniqParser.isAllRepeated());
    }

    // cd flag together with single file
    @Test
    public void parse_cdFlagTogthSingleFile_cdTrueSingleFile() throws InvalidArgsException {
        String[] args = {CD_FLAG, FILE_EXT};
        uniqParser.parse(args);
        assertEquals(Arrays.asList(FILE_EXT), uniqParser.getFiles());
        assertTrue(uniqParser.isCount());
        assertTrue(uniqParser.isRepeated());
        assertFalse(uniqParser.isAllRepeated());
    }

    // cD flag separate with multiple files
    @Test
    public void parse_cDFlagSepMultiFile_cDTrueMultiFiles() throws InvalidArgsException {
        String[] args = {FILE_NO_SPACE, CAP_D_FLAG, C_FLAG, SPACE};
        uniqParser.parse(args);
        assertEquals(Arrays.asList(FILE_NO_SPACE, SPACE), uniqParser.getFiles());
        assertTrue(uniqParser.isCount());
        assertFalse(uniqParser.isRepeated());
        assertTrue(uniqParser.isAllRepeated());
    }

    // cD flag together with no file
    @Test
    public void parse_cDFlagTogthNoFile_cDTrueEmpty() throws InvalidArgsException {
        String[] args = {C_CAPD_FLAG};
        uniqParser.parse(args);
        assertEquals(Arrays.asList(), uniqParser.getFiles());
        assertTrue(uniqParser.isCount());
        assertFalse(uniqParser.isRepeated());
        assertTrue(uniqParser.isAllRepeated());
    }

    // dD flag separate with multiple files
    @Test
    public void parse_dDFlagSepMultiFile_dDTrueMultiFiles() throws InvalidArgsException {
        String[] args = {CAP_D_FLAG, FILE_EXT, D_FLAG, FILE_SPACE};
        uniqParser.parse(args);
        assertEquals(Arrays.asList(FILE_EXT, FILE_SPACE), uniqParser.getFiles());
        assertFalse(uniqParser.isCount());
        assertTrue(uniqParser.isRepeated());
        assertTrue(uniqParser.isAllRepeated());
    }

    // dD flag together with single file
    @Test
    public void parse_dDFlagTogthSingleFile_dDTrueSingleFile() throws InvalidArgsException {
        String[] args = {FILE_SPACE, D_CAPD_FLAG};
        uniqParser.parse(args);
        assertEquals(Arrays.asList(FILE_SPACE), uniqParser.getFiles());
        assertFalse(uniqParser.isCount());
        assertTrue(uniqParser.isRepeated());
        assertTrue(uniqParser.isAllRepeated());
    }

    // cdD flag separate with no file
    @Test
    public void parse_cdDFlagSepNoFile_cdDTrueEmpty() throws InvalidArgsException {
        String[] args = {CAP_D_FLAG, FILE_NO_SPACE, C_FLAG, FILE_EXT, D_FLAG, FILE_SPACE};
        uniqParser.parse(args);
        assertEquals(Arrays.asList(FILE_NO_SPACE, FILE_EXT, FILE_SPACE), uniqParser.getFiles());
        assertTrue(uniqParser.isCount());
        assertTrue(uniqParser.isCount());
        assertTrue(uniqParser.isCount());
    }

    // cdD flag together with multiple files
    @Test
    public void parse_cdDFlagTogthMultiFiles_cdDTrueMultiFiles() throws InvalidArgsException {
        String[] args = {C_D_CAPD_FLAG, FILE_SPACE, FILE_EXT};
        uniqParser.parse(args);
        assertEquals(Arrays.asList(FILE_SPACE, FILE_EXT), uniqParser.getFiles());
        assertTrue(uniqParser.isCount());
        assertTrue(uniqParser.isRepeated());
        assertTrue(uniqParser.isAllRepeated());
    }

    // repeated flag separate with single file
    @Test
    public void parse_repeatFlagSepSingleFile_nrTrueSingleFile() throws InvalidArgsException {
        String[] args = {C_FLAG, FILE_NO_SPACE, D_FLAG, C_FLAG};
        uniqParser.parse(args);
        assertEquals(Arrays.asList(FILE_NO_SPACE), uniqParser.getFiles());
        assertTrue(uniqParser.isCount());
        assertTrue(uniqParser.isRepeated());
        assertFalse(uniqParser.isAllRepeated());
    }

    // repeated flag together with no file
    @Test
    public void parse_repeatFlagTogthNoFiles_nfrTrueEmpty() throws InvalidArgsException {
        String[] args = {REPEATED_FLAG};
        uniqParser.parse(args);
        assertEquals(Arrays.asList(), uniqParser.getFiles());
        assertTrue(uniqParser.isCount());
        assertTrue(uniqParser.isRepeated());
        assertFalse(uniqParser.isAllRepeated());
    }

    // invalid single flag with multiple files
    @Test
    public void parse_invalidFlagMultiFiles_throwsInvalidArgsException() {
        String[] args = {INVALID_FLAG_1, EMPTY, FILE_EXT, FILE_SPACE};
        Exception exception = assertThrows(InvalidArgsException.class, () -> uniqParser.parse(args));
        assertTrue(exception.getMessage().contains(ERR_INVALID_FLAG));
    }

    // invalid multiple flag separate with single file
    @Test
    public void parse_invalidMultiFlagSepSingleFile_throwsInvalidArgsException() {
        String[] args = {CAP_D_FLAG, INVALID_FLAG_1, EMPTY, FILE_EXT};
        Exception exception = assertThrows(InvalidArgsException.class, () -> uniqParser.parse(args));
        assertTrue(exception.getMessage().contains(ERR_INVALID_FLAG));
    }

    // invalid multiple flag together with no file
    @Test
    public void parse_invalidMultiFlagTogthNoFile_throwsInvalidArgsException() {
        String[] args = {INVALID_FLAG_2};
        Exception exception = assertThrows(InvalidArgsException.class, () -> uniqParser.parse(args));
        assertTrue(exception.getMessage().contains(ERR_INVALID_FLAG));
    }

    // invalid multiple flag together with multiple files
    @Test
    public void parse_invalidRepeatMultiFlagTogthMultiFiles_throwsInvalidArgsException() {
        String[] args = {FILE_EXT, INVALID_FLAG_3, FILE_EXT, FILE_NO_SPACE};
        Exception exception = assertThrows(InvalidArgsException.class, () -> uniqParser.parse(args));
        assertTrue(exception.getMessage().contains(ERR_INVALID_FLAG));
    }

    // cd flag together with invalid file
    @Test
    public void parse_cdFlagTogthInvalidFile_throwsInvalidArgsException() {
        String[] args = {FILE_EXT, CD_FLAG, FILE_EXT, INVALID_FILE_DASH};
        Exception exception = assertThrows(InvalidArgsException.class, () -> uniqParser.parse(args));
        assertTrue(exception.getMessage().contains(ERR_INVALID_FLAG));
    }
}