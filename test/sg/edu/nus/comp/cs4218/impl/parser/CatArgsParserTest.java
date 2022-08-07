package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FLAG;

public class CatArgsParserTest {

    private static final String N_FLAG = "-n";
    private static final String INVALID_FLAG_1 = "-a";
    private static final String INVALID_FLAG_2 = "-rn";
    private static final String INVALID_FLAG_3 = "-ncd";
    private static final String FILE_1 = "thisisarandomfile.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String FILE_3 = "cs4218weloveyou.txt";
    CatArgsParser catArgsParser;

    @BeforeEach
    void init() {
        catArgsParser = new CatArgsParser();
    }

    // 1: null flag, no files
    @Test
    void parse_NullFlagNoFile_NoFlagNoFile() throws Exception {
        catArgsParser.parse();
        assertFalse(catArgsParser.isPrefixWithLineNumber());
        assertEquals(0, catArgsParser.getFiles().size());
    }

    // 2: null flag, single file
    @Test
    void parse_NullFlagSingleFile_NoFlagOneFile() throws Exception {
        String[] args = new String[]{FILE_1};
        catArgsParser.parse(args);
        assertFalse(catArgsParser.isPrefixWithLineNumber());
        assertEquals(args.length, catArgsParser.getFiles().size());
        assertEquals(Arrays.asList(FILE_1), catArgsParser.getFiles());
    }

    // 3: null flag, multiple files
    @Test
    void parse_NullFlagMultipleFile_NoFlagTwoFile() throws Exception {
        String[] args = new String[]{FILE_1, FILE_2};
        catArgsParser.parse(args);
        assertFalse(catArgsParser.isPrefixWithLineNumber());
        assertEquals(args.length, catArgsParser.getFiles().size());
        assertEquals(Arrays.asList(FILE_1, FILE_2), catArgsParser.getFiles());
    }

    // 4: empty flag (-), single file
    @Test
    void parse_EmptyStringArgs_NoFlagOneFile() throws Exception {
        String[] args = new String[]{"-", FILE_1};
        catArgsParser.parse(args);
        assertFalse(catArgsParser.isPrefixWithLineNumber());
        assertEquals(args.length, catArgsParser.getFiles().size());
        assertEquals(Arrays.asList("-", FILE_1), catArgsParser.getFiles());
    }

    // 5: empty flag (-), multiple files
    @Test
    void parse_EmptyStringArgsWithMultipleFile_NoFlagFourFile() throws Exception {
        String[] args = new String[]{"-", FILE_1, FILE_2, FILE_3};
        catArgsParser.parse(args);
        assertFalse(catArgsParser.isPrefixWithLineNumber());
        assertEquals(args.length, catArgsParser.getFiles().size());
        assertEquals(Arrays.asList("-", FILE_1, FILE_2, FILE_3), catArgsParser.getFiles());
    }

    // 6: empty flag (-), no files
    @Test
    void parse_EmptyStringArgsWithNoFile_NoFlagOneFile() throws Exception {
        String[] args = new String[]{"-"};
        catArgsParser.parse(args);
        assertFalse(catArgsParser.isPrefixWithLineNumber());
        assertEquals(args.length, catArgsParser.getFiles().size());
        assertEquals(Arrays.asList("-"), catArgsParser.getFiles());
    }

    // 7: n flag, multiple files
    @Test
    void parse_NArgsMultipleFile_NFlagTwoFile() throws Exception {
        String[] args = new String[]{FILE_1, N_FLAG, FILE_2};
        catArgsParser.parse(args);
        assertTrue(catArgsParser.isPrefixWithLineNumber());
        assertEquals(2, catArgsParser.getFiles().size());
        assertEquals(Arrays.asList(FILE_1, FILE_2), catArgsParser.getFiles());
    }

    // 8: n flag, no files
    @Test
    void parse_NArgsNoFile_NFlagNoFile() throws Exception {
        String[] args = new String[]{N_FLAG};
        catArgsParser.parse(args);
        assertTrue(catArgsParser.isPrefixWithLineNumber());
        assertEquals(0, catArgsParser.getFiles().size());
    }

    // 9: n flag, single files
    @Test
    void parse_NArgsSingleFile_NFlagOneFile() throws Exception {
        String[] args = new String[]{N_FLAG, FILE_1};
        catArgsParser.parse(args);
        assertTrue(catArgsParser.isPrefixWithLineNumber());
        assertEquals(1, catArgsParser.getFiles().size());
        assertEquals(Arrays.asList(FILE_1), catArgsParser.getFiles());
    }

    @Test
    void parse_InvalidArgsA_ThrowsException() {
        String[] args = new String[]{INVALID_FLAG_1};
        InvalidArgsException argsException = assertThrows(InvalidArgsException.class, () -> {
            catArgsParser.parse(args);
        });
        assertEquals(ERR_INVALID_FLAG + ": " + "a", argsException.getMessage());
    }

    @Test
    void parse_InvalidArgsRN_ThrowsException() {
        String[] args = new String[]{INVALID_FLAG_2};
        InvalidArgsException argsException = assertThrows(InvalidArgsException.class, () -> {
            catArgsParser.parse(args);
        });
        assertEquals(ERR_INVALID_FLAG + ": " + "r", argsException.getMessage());
    }

    @Test
    void parse_InvalidArgsNCD_ThrowsException() {
        String[] args = new String[]{INVALID_FLAG_3};
        InvalidArgsException argsException = assertThrows(InvalidArgsException.class, () -> {
            catArgsParser.parse(args);
        });
        assertEquals(ERR_INVALID_FLAG + ": " + "c", argsException.getMessage());
    }
}
