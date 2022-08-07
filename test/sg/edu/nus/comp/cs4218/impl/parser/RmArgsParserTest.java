package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FLAG;

public class RmArgsParserTest {

    private static final String R_FLAG = "-r";
    private static final String D_FLAG = "-d";
    private static final String RD_FLAG = "-rd";
    private static final String REPEATED_FLAG = "-rr";
    private static final String INVALID_FLAG_1 = "-a";
    private static final String INVALID_FLAG_2 = "-rb";
    private static final String INVALID_FLAG_3 = "-rdc";
    private static final String FILE_1 = "thisisarandomfile.txt";
    private static final String FILE_2 = "file2.txt";
    private static final String FILE_3 = "cs4218weloveyou.txt";
    RmArgsParser rmArgsParser;

    @BeforeEach
    void init() {
        rmArgsParser = new RmArgsParser();
    }

    @Test
    void parse_EmptyArgs_NoFlags() throws Exception {
        rmArgsParser.parse();
        assertFalse(rmArgsParser.isEmptyDir());
        assertFalse(rmArgsParser.isRecursive());
        assertEquals(0, rmArgsParser.getFiles().size());
    }

    @Test
    void parse_EmptyStringArgs_NoFlags() throws Exception {
        String[] args = new String[]{};
        rmArgsParser.parse(args);
        assertFalse(rmArgsParser.isEmptyDir());
        assertFalse(rmArgsParser.isRecursive());
        assertEquals(args.length, rmArgsParser.getFiles().size());
    }

    @Test
    void parse_RArgs_Flag() throws Exception {
        String[] args = new String[]{R_FLAG};
        rmArgsParser.parse(args);
        assertFalse(rmArgsParser.isEmptyDir());
        assertTrue(rmArgsParser.isRecursive());
        assertEquals(0, rmArgsParser.getFiles().size());
    }

    @Test
    void parse_DArgs_Flag() throws Exception {
        String[] args = new String[]{D_FLAG};
        rmArgsParser.parse(args);
        assertTrue(rmArgsParser.isEmptyDir());
        assertFalse(rmArgsParser.isRecursive());
        assertEquals(0, rmArgsParser.getFiles().size());
    }

    @Test
    void parse_RDArgs_Flag() throws Exception {
        String[] args = new String[]{R_FLAG, D_FLAG};
        rmArgsParser.parse(args);
        assertTrue(rmArgsParser.isEmptyDir());
        assertTrue(rmArgsParser.isRecursive());
        assertEquals(0, rmArgsParser.getFiles().size());
    }

    @Test
    void parse_RspaceDArgs_Flag() throws Exception {
        String[] args = new String[]{RD_FLAG};
        rmArgsParser.parse(args);
        assertTrue(rmArgsParser.isEmptyDir());
        assertTrue(rmArgsParser.isRecursive());
        assertEquals(0, rmArgsParser.getFiles().size());
    }

    @Test
    void parse_RRArgs_Flag() throws Exception {
        String[] args = new String[]{REPEATED_FLAG};
        rmArgsParser.parse(args);
        assertFalse(rmArgsParser.isEmptyDir());
        assertTrue(rmArgsParser.isRecursive());
        assertEquals(0, rmArgsParser.getFiles().size());
    }

    @Test
    void parse_InvalidArgsA_ThrowsException() throws Exception {
        String[] args = new String[]{INVALID_FLAG_1};
        InvalidArgsException argsException = assertThrows(InvalidArgsException.class, () -> {
            rmArgsParser.parse(args);
        });
        assertEquals(ERR_INVALID_FLAG + ": " + "a", argsException.getMessage());
    }

    @Test
    void parse_InvalidArgsRB_ThrowsException() throws Exception {
        String[] args = new String[]{INVALID_FLAG_2};
        InvalidArgsException argsException = assertThrows(InvalidArgsException.class, () -> {
            rmArgsParser.parse(args);
        });
        assertEquals(ERR_INVALID_FLAG + ": " + "b", argsException.getMessage());
    }

    @Test
    void parse_InvalidArgsRDC_ThrowsException() throws Exception {
        String[] args = new String[]{INVALID_FLAG_3};
        InvalidArgsException argsException = assertThrows(InvalidArgsException.class, () -> {
            rmArgsParser.parse(args);
        });
        assertEquals(ERR_INVALID_FLAG + ": " + "c", argsException.getMessage());
    }

    @Test
    void parse_RArgsWithSingleFile_CorrectFileAndFlag() throws Exception {
        String[] args = new String[]{R_FLAG, FILE_1};
        rmArgsParser.parse(args);
        assertTrue(rmArgsParser.isRecursive());
        assertFalse(rmArgsParser.isEmptyDir());
        assertEquals(1, rmArgsParser.getFiles().size());
        assertEquals(FILE_1, rmArgsParser.getFiles().get(0));
    }

    @Test
    void parse_RArgsWithMultipleFile_CorrectFileAndFlag() throws Exception {
        String[] args = new String[]{R_FLAG, FILE_1, FILE_2};
        rmArgsParser.parse(args);
        assertTrue(rmArgsParser.isRecursive());
        assertFalse(rmArgsParser.isEmptyDir());
        assertEquals(2, rmArgsParser.getFiles().size());
        assertEquals(FILE_1, rmArgsParser.getFiles().get(0));
        assertEquals(FILE_2, rmArgsParser.getFiles().get(1));
    }

    @Test
    void parse_DArgsWithSingleFile_CorrectFileAndFlag() throws Exception {
        String[] args = new String[]{D_FLAG, FILE_1};
        rmArgsParser.parse(args);
        assertFalse(rmArgsParser.isRecursive());
        assertTrue(rmArgsParser.isEmptyDir());
        assertEquals(1, rmArgsParser.getFiles().size());
        assertEquals(FILE_1, rmArgsParser.getFiles().get(0));
    }

    @Test
    void parse_DArgsWithMultipleFile_CorrectFileAndFlag() throws Exception {
        String[] args = new String[]{D_FLAG, FILE_1, FILE_2};
        rmArgsParser.parse(args);
        assertFalse(rmArgsParser.isRecursive());
        assertTrue(rmArgsParser.isEmptyDir());
        assertEquals(2, rmArgsParser.getFiles().size());
        assertEquals(FILE_1, rmArgsParser.getFiles().get(0));
        assertEquals(FILE_2, rmArgsParser.getFiles().get(1));
    }

    @Test
    void parse_RDArgsWithSingleFile_CorrectFileAndFlag() throws Exception {
        String[] args = new String[]{RD_FLAG, FILE_1};
        rmArgsParser.parse(args);
        assertTrue(rmArgsParser.isRecursive());
        assertTrue(rmArgsParser.isEmptyDir());
        assertEquals(1, rmArgsParser.getFiles().size());
        assertEquals(FILE_1, rmArgsParser.getFiles().get(0));
    }

    @Test
    void parse_RDArgsWithMultipleFile_CorrectFileAndFlag() throws Exception {
        String[] args = new String[]{RD_FLAG, FILE_1, FILE_2, FILE_3};
        rmArgsParser.parse(args);
        assertTrue(rmArgsParser.isRecursive());
        assertTrue(rmArgsParser.isEmptyDir());
        assertEquals(3, rmArgsParser.getFiles().size());
        assertEquals(FILE_1, rmArgsParser.getFiles().get(0));
        assertEquals(FILE_2, rmArgsParser.getFiles().get(1));
        assertEquals(FILE_3, rmArgsParser.getFiles().get(2));
    }

    @Test
    void parse_FileBeforeArgs_CorrectFileAndFlag() throws Exception {
        String[] args = new String[]{FILE_1, R_FLAG};
        rmArgsParser.parse(args);
        assertTrue(rmArgsParser.isRecursive());
        assertFalse(rmArgsParser.isEmptyDir());
        assertEquals(1, rmArgsParser.getFiles().size());
        assertEquals(FILE_1, rmArgsParser.getFiles().get(0));
    }

}
