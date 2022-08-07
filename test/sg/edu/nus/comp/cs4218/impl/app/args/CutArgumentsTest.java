package sg.edu.nus.comp.cs4218.impl.app.args;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_FLAG;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_LIST;
import static sg.edu.nus.comp.cs4218.testutils.CutAssertUtils.*;

class CutArgumentsTest {
    private final static String STDIN_DASH = "-";
    private final static String CHAR_FLAG = "-c";
    private final static String BYTE_FLAG = "-b";
    private final static String BOTH_FLAG = "-cb";
    private final static String INVAL_FLAG = "-d";
    private final static String FILE_1 = "1.txt";
    private final static String FILE_2 = "2";
    private final static String FILE_3 = "some other file.txt";
    private final static String EMPTY = "";
    private final static String SPACE = " ";
    private final static String LIST_SINGLE_STR = "8";
    private final static String LIST_RANGE_STR = "7-10";
    private final static String LIST_START_STR = "7-";
    private final static String LIST_END_STR = "-5";
    private final static String LIST_LIST_STR = "2,5,10";
    private final static String LIST_MIX_STR = "-3,5-8,10,14-";
    private final static String LIST_MIX_STR_2 = "5-8,-3,14-,10";
    private final static String LIST_MID_EMP = "3,,10";
    private final static String LIST_FRONT_EMP = ",2,5";
    private final static String LIST_BACK_EMP = "2,5,";
    private final static String LIST_MULTI_DASH = "-3-";
    private final static List<int[]> LIST_SINGLE = Arrays.asList(new int[]{8, 8}); // 8
    private final static List<int[]> LIST_RANGE = Arrays.asList(new int[]{7, 10}); // 7-10
    private final static List<int[]> LIST_START_RANGE = Arrays.asList(new int[]{7, Integer.MAX_VALUE}); // 7-
    private final static List<int[]> LIST_END_RANGE = Arrays.asList(new int[]{1, 5}); // -5
    private final static List<int[]> LIST_LIST = Arrays.asList(new int[]{2, 2}, new int[]{5, 5}, new int[]{10, 10}); // 2,5,10
    private final static List<int[]> LIST_MIX = Arrays.asList(new int[]{1, 3}, new int[]{5, 8}, new int[]{10, 10}, new int[]{14, Integer.MAX_VALUE}); // -3,5-8,10,14-
    private static CutArguments cutArg;

    @BeforeEach
    public void setUp() {
        cutArg = new CutArguments();
    }

    // character, single number, multiple files
    @Test
    public void parse_charSingleMulti_charSingleMulti() throws Exception {
        String[] args = {CHAR_FLAG, LIST_SINGLE_STR, FILE_1, FILE_2, FILE_3};
        cutArg.parse(args);
        assertEquals(Arrays.asList(FILE_1, FILE_2, FILE_3), cutArg.getFiles());
        assertListEquals(LIST_SINGLE, cutArg.getNumList());
        assertTrue(cutArg.isCharacterPosition());
        assertFalse(cutArg.isBytePosition());
    }

    // character, single number, not given
    @Test
    public void parse_charSingleNil_charSingleNil() throws Exception {
        String[] args = {CHAR_FLAG, LIST_SINGLE_STR};
        cutArg.parse(args);
        assertEquals(Arrays.asList(), cutArg.getFiles());
        assertListEquals(LIST_SINGLE, cutArg.getNumList());
        assertTrue(cutArg.isCharacterPosition());
        assertFalse(cutArg.isBytePosition());
    }

    // character, start range, multiple files
    @Test
    public void parse_charStartRangeMulti_charStartRangeMulti() throws Exception {
        String[] args = {CHAR_FLAG, LIST_START_STR, FILE_1, STDIN_DASH, FILE_2, FILE_3};
        cutArg.parse(args);
        assertEquals(Arrays.asList(FILE_1, STDIN_DASH, FILE_2, FILE_3), cutArg.getFiles());
        assertListEquals(LIST_START_RANGE, cutArg.getNumList());
        assertTrue(cutArg.isCharacterPosition());
        assertFalse(cutArg.isBytePosition());
    }

    // character, start range, not given
    @Test
    public void parse_charStartRangeNil_charStartRangeNil() throws Exception {
        String[] args = {CHAR_FLAG, LIST_START_STR};
        cutArg.parse(args);
        assertEquals(Arrays.asList(), cutArg.getFiles());
        assertListEquals(LIST_START_RANGE, cutArg.getNumList());
        assertTrue(cutArg.isCharacterPosition());
        assertFalse(cutArg.isBytePosition());
    }

    // character, end range, multiple files
    @Test
    public void parse_charEndRangeMulti_charEndRangeMulti() throws Exception {
        String[] args = {CHAR_FLAG, LIST_END_STR, EMPTY, FILE_2, FILE_3};
        cutArg.parse(args);
        assertEquals(Arrays.asList(FILE_2, FILE_3), cutArg.getFiles());
        assertListEquals(LIST_END_RANGE, cutArg.getNumList());
        assertTrue(cutArg.isCharacterPosition());
        assertFalse(cutArg.isBytePosition());
    }

    // character, end number, not given
    @Test
    public void parse_charEndRangeNil_charEndRangeNil() throws Exception {
        String[] args = {CHAR_FLAG, LIST_END_STR};
        cutArg.parse(args);
        assertEquals(Arrays.asList(), cutArg.getFiles());
        assertListEquals(LIST_END_RANGE, cutArg.getNumList());
        assertTrue(cutArg.isCharacterPosition());
        assertFalse(cutArg.isBytePosition());
    }

    // character, range, multiple files
    @Test
    public void parse_charRangeMulti_charRangeMulti() throws Exception {
        String[] args = {CHAR_FLAG, LIST_RANGE_STR, SPACE, FILE_2, STDIN_DASH};
        cutArg.parse(args);
        assertEquals(Arrays.asList(FILE_2, STDIN_DASH), cutArg.getFiles());
        assertListEquals(LIST_RANGE, cutArg.getNumList());
        assertTrue(cutArg.isCharacterPosition());
        assertFalse(cutArg.isBytePosition());
    }

    // character, number, not given
    @Test
    public void parse_charRangeNil_charRangeNil() throws Exception {
        String[] args = {CHAR_FLAG, LIST_RANGE_STR};
        cutArg.parse(args);
        assertEquals(Arrays.asList(), cutArg.getFiles());
        assertListEquals(LIST_RANGE, cutArg.getNumList());
        assertTrue(cutArg.isCharacterPosition());
        assertFalse(cutArg.isBytePosition());
    }

    // character, list, multiple files
    @Test
    public void parse_charListMulti_charListMulti() throws Exception {
        String[] args = {CHAR_FLAG, LIST_LIST_STR, STDIN_DASH, SPACE, EMPTY};
        cutArg.parse(args);
        assertEquals(Arrays.asList(STDIN_DASH), cutArg.getFiles());
        assertListEquals(LIST_LIST, cutArg.getNumList());
        assertTrue(cutArg.isCharacterPosition());
        assertFalse(cutArg.isBytePosition());
    }

    // character, list, not given
    @Test
    public void parse_charListNil_charListNil() throws Exception {
        String[] args = {CHAR_FLAG, LIST_LIST_STR};
        cutArg.parse(args);
        assertEquals(Arrays.asList(), cutArg.getFiles());
        assertListEquals(LIST_LIST, cutArg.getNumList());
        assertTrue(cutArg.isCharacterPosition());
        assertFalse(cutArg.isBytePosition());
    }

    // character, mix, multiple files
    @Test
    public void parse_charMixMulti_charMixMulti() throws Exception {
        String[] args = {CHAR_FLAG, LIST_MIX_STR, FILE_1, FILE_3};
        cutArg.parse(args);
        assertEquals(Arrays.asList(FILE_1, FILE_3), cutArg.getFiles());
        assertListEquals(LIST_MIX, cutArg.getNumList());
        assertTrue(cutArg.isCharacterPosition());
        assertFalse(cutArg.isBytePosition());
    }

    // character, mix, not given
    @Test
    public void parse_charMixNil_charMixNil() throws Exception {
        String[] args = {CHAR_FLAG, LIST_MIX_STR};
        cutArg.parse(args);
        assertEquals(Arrays.asList(), cutArg.getFiles());
        assertListEquals(LIST_MIX, cutArg.getNumList());
        assertTrue(cutArg.isCharacterPosition());
        assertFalse(cutArg.isBytePosition());
    }

    // character, mix unsorted, multiple files
    @Test
    public void parse_charMixUnsortMulti_charMixMulti() throws Exception {
        String[] args = {CHAR_FLAG, LIST_MIX_STR_2, FILE_1, FILE_3};
        cutArg.parse(args);
        assertEquals(Arrays.asList(FILE_1, FILE_3), cutArg.getFiles());
        assertListEquals(LIST_MIX, cutArg.getNumList());
        assertTrue(cutArg.isCharacterPosition());
        assertFalse(cutArg.isBytePosition());
    }

    // character, mix unsorted, not given
    @Test
    public void parse_charMixUnsortNil_charMixNil() throws Exception {
        String[] args = {CHAR_FLAG, LIST_MIX_STR_2};
        cutArg.parse(args);
        assertEquals(Arrays.asList(), cutArg.getFiles());
        assertListEquals(LIST_MIX, cutArg.getNumList());
        assertTrue(cutArg.isCharacterPosition());
        assertFalse(cutArg.isBytePosition());
    }

    // byte, single number, multiple files
    @Test
    public void parse_byteSingleMulti_byteSingleMulti() throws Exception {
        String[] args = {BYTE_FLAG, LIST_SINGLE_STR, FILE_1, SPACE, FILE_3};
        cutArg.parse(args);
        assertEquals(Arrays.asList(FILE_1, FILE_3), cutArg.getFiles());
        assertListEquals(LIST_SINGLE, cutArg.getNumList());
        assertFalse(cutArg.isCharacterPosition());
        assertTrue(cutArg.isBytePosition());
    }

    // byte, single number, not given
    @Test
    public void parse_byteSingleNil_byteSingleNil() throws Exception {
        String[] args = {BYTE_FLAG, LIST_SINGLE_STR};
        cutArg.parse(args);
        assertEquals(Arrays.asList(), cutArg.getFiles());
        assertListEquals(LIST_SINGLE, cutArg.getNumList());
        assertFalse(cutArg.isCharacterPosition());
        assertTrue(cutArg.isBytePosition());
    }

    // byte, start range, multiple files
    @Test
    public void parse_byteStartRangeMulti_byteStartRangeMulti() throws Exception {
        String[] args = {BYTE_FLAG, LIST_START_STR, FILE_3, FILE_1, FILE_3, EMPTY};
        cutArg.parse(args);
        assertEquals(Arrays.asList(FILE_3, FILE_1, FILE_3), cutArg.getFiles());
        assertListEquals(LIST_START_RANGE, cutArg.getNumList());
        assertFalse(cutArg.isCharacterPosition());
        assertTrue(cutArg.isBytePosition());
    }

    // byte, start range, not given
    @Test
    public void parse_byteStartRangeNil_byteStartRangeNil() throws Exception {
        String[] args = {BYTE_FLAG, LIST_START_STR};
        cutArg.parse(args);
        assertEquals(Arrays.asList(), cutArg.getFiles());
        assertListEquals(LIST_START_RANGE, cutArg.getNumList());
        assertFalse(cutArg.isCharacterPosition());
        assertTrue(cutArg.isBytePosition());
    }

    // byte, end range, multiple files
    @Test
    public void parse_byteEndRangeMulti_byteEndRangeMulti() throws Exception {
        String[] args = {BYTE_FLAG, LIST_END_STR, FILE_1, FILE_1, SPACE, FILE_1};
        cutArg.parse(args);
        assertEquals(Arrays.asList(FILE_1, FILE_1, FILE_1), cutArg.getFiles());
        assertListEquals(LIST_END_RANGE, cutArg.getNumList());
        assertFalse(cutArg.isCharacterPosition());
        assertTrue(cutArg.isBytePosition());
    }

    // byte, end range, not given
    @Test
    public void parse_byteEndRangeNil_byteEndRangeNil() throws Exception {
        String[] args = {BYTE_FLAG, LIST_END_STR};
        cutArg.parse(args);
        assertEquals(Arrays.asList(), cutArg.getFiles());
        assertListEquals(LIST_END_RANGE, cutArg.getNumList());
        assertFalse(cutArg.isCharacterPosition());
        assertTrue(cutArg.isBytePosition());
    }

    // byte, range, multiple files
    @Test
    public void parse_byteRangeMulti_byteRangeMulti() throws Exception {
        String[] args = {BYTE_FLAG, LIST_RANGE_STR, FILE_2, FILE_1};
        cutArg.parse(args);
        assertEquals(Arrays.asList(FILE_2, FILE_1), cutArg.getFiles());
        assertListEquals(LIST_RANGE, cutArg.getNumList());
        assertFalse(cutArg.isCharacterPosition());
        assertTrue(cutArg.isBytePosition());
    }

    // byte, range, not given
    @Test
    public void parse_byteRangeNil_byteRangeNil() throws Exception {
        String[] args = {BYTE_FLAG, LIST_RANGE_STR};
        cutArg.parse(args);
        assertEquals(Arrays.asList(), cutArg.getFiles());
        assertListEquals(LIST_RANGE, cutArg.getNumList());
        assertFalse(cutArg.isCharacterPosition());
        assertTrue(cutArg.isBytePosition());
    }

    // byte, list, multiple files
    @Test
    public void parse_byteListMulti_byteListMulti() throws Exception {
        String[] args = {BYTE_FLAG, LIST_LIST_STR, SPACE, FILE_1, EMPTY, FILE_2};
        cutArg.parse(args);
        assertEquals(Arrays.asList(FILE_1, FILE_2), cutArg.getFiles());
        assertListEquals(LIST_LIST, cutArg.getNumList());
        assertFalse(cutArg.isCharacterPosition());
        assertTrue(cutArg.isBytePosition());
    }

    // byte, list, not given
    @Test
    public void parse_byteListNil_byteListNil() throws Exception {
        String[] args = {BYTE_FLAG, LIST_LIST_STR};
        cutArg.parse(args);
        assertEquals(Arrays.asList(), cutArg.getFiles());
        assertListEquals(LIST_LIST, cutArg.getNumList());
        assertFalse(cutArg.isCharacterPosition());
        assertTrue(cutArg.isBytePosition());
    }

    // byte, mix, multiple files
    @Test
    public void parse_byteMixMulti_byteMixMulti() throws Exception {
        String[] args = {BYTE_FLAG, LIST_MIX_STR, EMPTY, SPACE, FILE_1, EMPTY, FILE_2};
        cutArg.parse(args);
        assertEquals(Arrays.asList(FILE_1, FILE_2), cutArg.getFiles());
        assertListEquals(LIST_MIX, cutArg.getNumList());
        assertFalse(cutArg.isCharacterPosition());
        assertTrue(cutArg.isBytePosition());
    }

    // byte, mix, not given
    @Test
    public void parse_byteMixNil_byteMixNil() throws Exception {
        String[] args = {BYTE_FLAG, LIST_MIX_STR};
        cutArg.parse(args);
        assertEquals(Arrays.asList(), cutArg.getFiles());
        assertListEquals(LIST_MIX, cutArg.getNumList());
        assertFalse(cutArg.isCharacterPosition());
        assertTrue(cutArg.isBytePosition());
    }

    // byte, mix unsorted, multiple files
    @Test
    public void parse_byteMixUnsortMulti_byteMixMulti() throws Exception {
        String[] args = {BYTE_FLAG, LIST_MIX_STR_2, EMPTY, SPACE, FILE_1, EMPTY, FILE_2};
        cutArg.parse(args);
        assertEquals(Arrays.asList(FILE_1, FILE_2), cutArg.getFiles());
        assertListEquals(LIST_MIX, cutArg.getNumList());
        assertFalse(cutArg.isCharacterPosition());
        assertTrue(cutArg.isBytePosition());
    }

    // byte, mix unsorted, not given
    @Test
    public void parse_byteMixUnsortNil_byteMixNil() throws Exception {
        String[] args = {BYTE_FLAG, LIST_MIX_STR_2};
        cutArg.parse(args);
        assertEquals(Arrays.asList(), cutArg.getFiles());
        assertListEquals(LIST_MIX, cutArg.getNumList());
        assertFalse(cutArg.isCharacterPosition());
        assertTrue(cutArg.isBytePosition());
    }

    // character, byte, mix, multiple files
    @Test
    public void parse_charByteMixMulti_throwsException() {
        String[] args = {CHAR_FLAG, BYTE_FLAG, LIST_MIX_STR, FILE_1, FILE_2, FILE_3};
        Exception exception = assertThrows(Exception.class, () -> cutArg.parse(args));
        assertEquals(INVALID_LIST, exception.getMessage());
    }

    // character and byte together, mix, multiple files
    @Test
    public void parse_charByteTogthMixMulti_throwsException() {
        String[] args = {BOTH_FLAG, LIST_MIX_STR, FILE_1, FILE_2, FILE_3};
        Exception exception = assertThrows(Exception.class, () -> cutArg.parse(args));
        assertEquals(INVALID_FLAG, exception.getMessage());
    }

    // no option, mix, multiple files
    @Test
    public void parse_noOpMixMulti_throwsException() {
        String[] args = {LIST_MIX_STR, FILE_1, FILE_2, FILE_3};
        Exception exception = assertThrows(Exception.class, () -> cutArg.parse(args));
        assertEquals(INVALID_FLAG, exception.getMessage());
    }

    // invalid flag, mix, multiple files
    @Test
    public void parse_invalidMixMulti_throwsException() {
        String[] args = {INVAL_FLAG, LIST_MIX_STR, FILE_1, FILE_2, FILE_3};
        Exception exception = assertThrows(Exception.class, () -> cutArg.parse(args));
        assertEquals(INVALID_FLAG, exception.getMessage());
    }

    // empty, mix, multiple files
    @Test
    public void parse_emptyMixMulti_throwsException() {
        String[] args = {EMPTY, LIST_MIX_STR, FILE_1, FILE_2, FILE_3};
        Exception exception = assertThrows(Exception.class, () -> cutArg.parse(args));
        assertEquals(INVALID_FLAG, exception.getMessage());
    }

    // space, mix, multiple files
    @Test
    public void parse_spaceMixMulti_throwsException() {
        String[] args = {SPACE, LIST_MIX_STR, FILE_1, FILE_2, FILE_3};
        Exception exception = assertThrows(Exception.class, () -> cutArg.parse(args));
        assertEquals(INVALID_FLAG, exception.getMessage());
    }

    // character, not given, multiple files
    @Test
    public void parse_charNilMulti_throwsException() {
        String[] args = {CHAR_FLAG, FILE_1, FILE_2, FILE_3};
        Exception exception = assertThrows(Exception.class, () -> cutArg.parse(args));
        assertEquals(INVALID_LIST, exception.getMessage());
    }

    // character, empty, multiple files
    @Test
    public void parse_charEmptyMulti_throwsException() {
        String[] args = {CHAR_FLAG, EMPTY, FILE_1, FILE_2, FILE_3};
        Exception exception = assertThrows(Exception.class, () -> cutArg.parse(args));
        assertEquals(INVALID_LIST, exception.getMessage());
    }

    // character, space, multiple files
    @Test
    public void parse_charSpaceMulti_throwsException() {
        String[] args = {CHAR_FLAG, SPACE, FILE_1, FILE_2, FILE_3};
        Exception exception = assertThrows(Exception.class, () -> cutArg.parse(args));
        assertEquals(INVALID_LIST, exception.getMessage());
    }

    // character, mid empty, multiple files
    @Test
    public void parse_charMidEmptyMulti_throwsException() {
        String[] args = {CHAR_FLAG, LIST_MID_EMP, FILE_1, FILE_2, FILE_3};
        Exception exception = assertThrows(Exception.class, () -> cutArg.parse(args));
        assertEquals(INVALID_LIST, exception.getMessage());
    }

    // character, front empty, multiple files
    @Test
    public void parse_charMidFrontEmptyMulti_throwsException() {
        String[] args = {CHAR_FLAG, LIST_FRONT_EMP, FILE_1, FILE_2, FILE_3};
        Exception exception = assertThrows(Exception.class, () -> cutArg.parse(args));
        assertEquals(INVALID_LIST, exception.getMessage());
    }

    // character, back empty, multiple files
    @Test
    public void parse_charMidBackEmptyMulti_throwsException() {
        String[] args = {CHAR_FLAG, LIST_BACK_EMP, FILE_1, FILE_2, FILE_3};
        Exception exception = assertThrows(Exception.class, () -> cutArg.parse(args));
        assertEquals(INVALID_LIST, exception.getMessage());
    }

    // character, multiple dashes, multiple files
    @Test
    public void parse_charMultiDashMulti_throwsException() {
        String[] args = {CHAR_FLAG, LIST_MULTI_DASH, FILE_1, FILE_2, FILE_3};
        Exception exception = assertThrows(Exception.class, () -> cutArg.parse(args));
        assertEquals(INVALID_LIST, exception.getMessage());
    }
}