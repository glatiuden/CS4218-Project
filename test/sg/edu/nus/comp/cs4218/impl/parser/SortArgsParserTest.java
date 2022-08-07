package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.SortArgsParser;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_ARG;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FLAG;

class SortArgsParserTest {
    private final static String FILE_NO_SPACE = "possibleFileName";
    private final static String FILE_SPACE = "possible file name";
    private final static String FILE_EXT = "file.txt";
    private final static String INVALID_FILE_DASH = "-file";
    private final static String EMPTY = "";
    private final static String SPACE = " ";
    private final static String N_FLAG = "-n";
    private final static String F_FLAG = "-f";
    private final static String R_FLAG = "-r";
    private final static String NF_FLAG = "-fn";
    private final static String NR_FLAG = "-nr";
    private final static String FR_FLAG = "-rf";
    private final static String NFR_FLAG = "-frn";
    private final static String REPEATED_FLAG = "-rfrfn";
    private final static String INVALID_FLAG_1 = "-d";
    private final static String INVALID_FLAG_2 = "-ndf";
    private final static String INVALID_FLAG_3 = "-nnfdfdn";
    private static SortArgsParser sortParser;

    @BeforeEach
    public void setUp() {
        sortParser = new SortArgsParser();
    }

    // empty arguments
    @Test
    public void parse_emptyArgs_emptyAllFalse() throws InvalidArgsException {
        String[] args = {};
        sortParser.parse(args);
        assertEquals(Arrays.asList(), sortParser.getFiles());
        assertFalse(sortParser.isCaseIndependent());
        assertFalse(sortParser.isFirstWordNumber());
        assertFalse(sortParser.isReverseOrder());
    }

    // no flags with multiple files
    @Test
    public void parse_noFlagMultiFiles_allFalseMultiFiles() throws InvalidArgsException {
        String[] args = {FILE_NO_SPACE, FILE_SPACE};
        sortParser.parse(args);
        assertEquals(Arrays.asList(FILE_NO_SPACE, FILE_SPACE), sortParser.getFiles());
        assertFalse(sortParser.isCaseIndependent());
        assertFalse(sortParser.isFirstWordNumber());
        assertFalse(sortParser.isReverseOrder());
    }

    // n flag with multiple files
    @Test
    public void parse_nFlagMultiFiles_nTrueMultiFiles() throws InvalidArgsException {
        String[] args = {FILE_NO_SPACE, N_FLAG, FILE_SPACE};
        sortParser.parse(args);
        assertEquals(Arrays.asList(FILE_NO_SPACE, FILE_SPACE), sortParser.getFiles());
        assertFalse(sortParser.isCaseIndependent());
        assertTrue(sortParser.isFirstWordNumber());
        assertFalse(sortParser.isReverseOrder());
    }

    // f flag with single file
    @Test
    public void parse_fFlagSingleFile_fTrueSingleFile() throws InvalidArgsException {
        String[] args = {F_FLAG, FILE_EXT};
        sortParser.parse(args);
        assertEquals(Arrays.asList(FILE_EXT), sortParser.getFiles());
        assertTrue(sortParser.isCaseIndependent());
        assertFalse(sortParser.isFirstWordNumber());
        assertFalse(sortParser.isReverseOrder());
    }

    // r flag with no file
    @Test
    public void parse_rFlagNoFile_rTrueEmpty() throws InvalidArgsException {
        String[] args = {R_FLAG};
        sortParser.parse(args);
        assertEquals(Arrays.asList(), sortParser.getFiles());
        assertFalse(sortParser.isCaseIndependent());
        assertFalse(sortParser.isFirstWordNumber());
        assertTrue(sortParser.isReverseOrder());
    }

    // nf flag separate with multiple files
    @Test
    public void parse_nfFlagSepMultiFiles_nfTrueMultiFiles() throws InvalidArgsException {
        String[] args = {N_FLAG, F_FLAG, FILE_SPACE, FILE_EXT, FILE_NO_SPACE};
        sortParser.parse(args);
        assertEquals(Arrays.asList(FILE_SPACE, FILE_EXT, FILE_NO_SPACE), sortParser.getFiles());
        assertTrue(sortParser.isCaseIndependent());
        assertTrue(sortParser.isFirstWordNumber());
        assertFalse(sortParser.isReverseOrder());
    }

    // nf flag together with single file
    @Test
    public void parse_nfFlagTogthSingleFile_nfTrueSingleFile() throws InvalidArgsException {
        String[] args = {NF_FLAG, FILE_EXT};
        sortParser.parse(args);
        assertEquals(Arrays.asList(FILE_EXT), sortParser.getFiles());
        assertTrue(sortParser.isCaseIndependent());
        assertTrue(sortParser.isFirstWordNumber());
        assertFalse(sortParser.isReverseOrder());
    }

    // nr flag separate with multiple files
    @Test
    public void parse_nrFlagSepMultiFile_nrTrueMultiFiles() throws InvalidArgsException {
        String[] args = {FILE_NO_SPACE, R_FLAG, N_FLAG, SPACE};
        sortParser.parse(args);
        assertEquals(Arrays.asList(FILE_NO_SPACE, SPACE), sortParser.getFiles());
        assertFalse(sortParser.isCaseIndependent());
        assertTrue(sortParser.isFirstWordNumber());
        assertTrue(sortParser.isReverseOrder());
    }

    // nr flag together with no file
    @Test
    public void parse_nrFlagTogthNoFile_nrTrueEmpty() throws InvalidArgsException {
        String[] args = {NR_FLAG};
        sortParser.parse(args);
        assertEquals(Arrays.asList(), sortParser.getFiles());
        assertFalse(sortParser.isCaseIndependent());
        assertTrue(sortParser.isFirstWordNumber());
        assertTrue(sortParser.isReverseOrder());
    }

    // fr flag separate with multiple files
    @Test
    public void parse_frFlagSepMultiFile_frTrueMultiFiles() throws InvalidArgsException {
        String[] args = {F_FLAG, FILE_EXT, R_FLAG, FILE_SPACE};
        sortParser.parse(args);
        assertEquals(Arrays.asList(FILE_EXT, FILE_SPACE), sortParser.getFiles());
        assertTrue(sortParser.isCaseIndependent());
        assertFalse(sortParser.isFirstWordNumber());
        assertTrue(sortParser.isReverseOrder());
    }

    // fr flag together with single file
    @Test
    public void parse_frFlagTogthSingleFile_frTrueSingleFile() throws InvalidArgsException {
        String[] args = {FILE_SPACE, FR_FLAG};
        sortParser.parse(args);
        assertEquals(Arrays.asList(FILE_SPACE), sortParser.getFiles());
        assertTrue(sortParser.isCaseIndependent());
        assertFalse(sortParser.isFirstWordNumber());
        assertTrue(sortParser.isReverseOrder());
    }

    // nfr flag separate with no file
    @Test
    public void parse_nfrFlagSepNoFile_nfrTrueEmpty() throws InvalidArgsException {
        String[] args = {N_FLAG, FILE_NO_SPACE, F_FLAG, FILE_EXT, R_FLAG, FILE_SPACE};
        sortParser.parse(args);
        assertEquals(Arrays.asList(FILE_NO_SPACE, FILE_EXT, FILE_SPACE), sortParser.getFiles());
        assertTrue(sortParser.isCaseIndependent());
        assertTrue(sortParser.isFirstWordNumber());
        assertTrue(sortParser.isReverseOrder());
    }

    // nfr flag together with multiple files
    @Test
    public void parse_nfrFlagTogthMultiFiles_nfrTrueMultiFiles() throws InvalidArgsException {
        String[] args = {NFR_FLAG, FILE_SPACE, FILE_EXT};
        sortParser.parse(args);
        assertEquals(Arrays.asList(FILE_SPACE, FILE_EXT), sortParser.getFiles());
        assertTrue(sortParser.isCaseIndependent());
        assertTrue(sortParser.isFirstWordNumber());
        assertTrue(sortParser.isReverseOrder());
    }

    // repeated flag separate with single file
    @Test
    public void parse_repeatFlagSepSingleFile_nrTrueSingleFile() throws InvalidArgsException {
        String[] args = {N_FLAG, FILE_NO_SPACE, N_FLAG, R_FLAG};
        sortParser.parse(args);
        assertEquals(Arrays.asList(FILE_NO_SPACE), sortParser.getFiles());
        assertFalse(sortParser.isCaseIndependent());
        assertTrue(sortParser.isFirstWordNumber());
        assertTrue(sortParser.isReverseOrder());
    }

    // repeated flag together with no file
    @Test
    public void parse_repeatFlagTogthNoFiles_nfrTrueEmpty() throws InvalidArgsException {
        String[] args = {REPEATED_FLAG};
        sortParser.parse(args);
        assertEquals(Arrays.asList(), sortParser.getFiles());
        assertTrue(sortParser.isCaseIndependent());
        assertTrue(sortParser.isFirstWordNumber());
        assertTrue(sortParser.isReverseOrder());
    }

    // invalid single flag with multiple files
    @Test
    public void parse_invalidFlagMultiFiles_throwsInvalidArgsException() {
        String[] args = {INVALID_FLAG_1, EMPTY, FILE_EXT, FILE_SPACE};
        Exception exception = assertThrows(InvalidArgsException.class, () -> sortParser.parse(args));
        assertTrue(exception.getMessage().contains(ERR_INVALID_FLAG));
    }

    // invalid multiple flag separate with single file
    @Test
    public void parse_invalidMultiFlagSepSingleFile_throwsInvalidArgsException() {
        String[] args = {N_FLAG, INVALID_FLAG_1, EMPTY, FILE_EXT};
        Exception exception = assertThrows(InvalidArgsException.class, () -> sortParser.parse(args));
        assertTrue(exception.getMessage().contains(ERR_INVALID_FLAG));
    }

    // invalid multiple flag together with no file
    @Test
    public void parse_invalidMultiFlagTogthNoFile_throwsInvalidArgsException() {
        String[] args = {INVALID_FLAG_2};
        Exception exception = assertThrows(InvalidArgsException.class, () -> sortParser.parse(args));
        assertTrue(exception.getMessage().contains(ERR_INVALID_FLAG));
    }

    // invalid multiple flag together with multiple files
    @Test
    public void parse_invalidRepeatMultiFlagTogthMultiFiles_throwsInvalidArgsException() {
        String[] args = {FILE_EXT, INVALID_FLAG_3, FILE_EXT, FILE_NO_SPACE};
        Exception exception = assertThrows(InvalidArgsException.class, () -> sortParser.parse(args));
        assertTrue(exception.getMessage().contains(ERR_INVALID_FLAG));
    }

    // nr flag together with invalid file
    @Test
    public void parse_nrFlagTogthInvalidFile_throwsInvalidArgsException() {
        String[] args = {FILE_EXT, NR_FLAG, FILE_EXT, INVALID_FILE_DASH};
        Exception exception = assertThrows(InvalidArgsException.class, () -> sortParser.parse(args));
        assertTrue(exception.getMessage().contains(ERR_INVALID_FLAG));
    }

    // Fix for bug 19
    @Test
    public void parse_emptyArgs_ThrowsError() {
        String[] args = {""};
        InvalidArgsException exception = assertThrows(InvalidArgsException.class, () -> sortParser.parse(args));
        assertTrue(exception.getMessage().contains(ERR_INVALID_ARG));
    }
}