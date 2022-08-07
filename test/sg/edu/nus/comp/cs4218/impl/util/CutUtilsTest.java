package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_DEC_RANGE;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_FLAG;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_LIST;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_RANGE;
import static sg.edu.nus.comp.cs4218.testutils.CutAssertUtils.*;

class CutUtilsTest {  //NOPMD - suppressed GodClass - This is a test class testing only CutUtils class. Many constants required as same values are reused, and many methods are needed for testing.

    private final static String NUM_NO_DASH = "123456";
    private final static String NUM_MID_DASH = "123-245";
    private final static String NUM_FRONT_DASH = "-123";
    private final static String NUM_BACK_DASH = "123-";
    private final static String NUM_MULTI_DASH = "-123-12-3--";
    private final static String CHAR_NO_DASH = "1a2b3";
    private final static String CHAR_ONE_DASH = "123-ab";
    private final static String CHAR_MULTI_DASH = "-1b-2-1-";
    private final static String MAX_MID_DASH = "2147483647999-21474836479999";
    private final static String MAX_FRONT_DASH = "-2147483647999";
    private final static String MAX_BACK_DASH = "2147483647999-";
    private final static String DASH = "-";
    private final static String DEC_RANGE = "5-1";
    private final static int[] MID_DASH = new int[]{123, 245};
    private final static int[] FRONT_DASH = new int[]{1, 123};
    private final static int[] BACK_DASH = new int[]{123, Integer.MAX_VALUE};
    private final static List<int[]> SINGLE_RANGE = Arrays.asList(new int[]{2, 10});
    private final static List<int[]> NO_MERGE = Arrays.asList(new int[]{1, 5}, new int[]{7, 10}, new int[]{11, 11}, new int[]{12, 20}, new int[]{21, 30});
    private final static List<int[]> UNSORT_NO_MERGE = Arrays.asList(new int[]{11, 11}, new int[]{21, 30}, new int[]{12, 20}, new int[]{1, 5}, new int[]{7, 10});
    private final static List<int[]> SORT_MERGE_IN = Arrays.asList(new int[]{1, 5}, new int[]{3, 10}, new int[]{11, 30}, new int[]{21, 25}, new int[]{31, 40}, new int[]{40, 50}, new int[]{51, 55});
    private final static List<int[]> MERGE_OUT = Arrays.asList(new int[]{1, 10}, new int[]{11, 30}, new int[]{31, 50}, new int[]{51, 55});
    private final static List<int[]> UNSORT_MERGE_IN = Arrays.asList(new int[]{3, 10}, new int[]{21, 25}, new int[]{51, 55}, new int[]{1, 5}, new int[]{11, 30}, new int[]{40, 50}, new int[]{31, 40});
    private final static byte[] EDGE_CHAR_0 = {"ñ".getBytes()[0]};
    private final static byte[] EDGE_CHAR_1 = {"ñ".getBytes()[1]};
    private final static String LINE_1 = "mul";
    private final static String LINE_2 = "evenwpfnroe4353fedds2";
    private final static String LINE_3 = "FR%$#FFR# F#@DEFRF F$#@!!@#";
    private final static String LINE_4 = "ñcas eñ4c2 on3tent";
    private final static List<int[]> LIST_SINGLE = Arrays.asList(new int[]{8, 8}); // 8
    private final static List<int[]> LIST_RANGE = Arrays.asList(new int[]{7, 10}); // 7-10
    private final static List<int[]> LIST_START_RANGE = Arrays.asList(new int[]{7, Integer.MAX_VALUE}); // 7-
    private final static List<int[]> LIST_END_RANGE = Arrays.asList(new int[]{1, 5}); // -5
    private final static List<int[]> LIST_LIST = Arrays.asList(new int[]{2, 2}, new int[]{5, 5}, new int[]{10, 10}); // 2,5,10
    private final static List<int[]> LIST_MIX = Arrays.asList(new int[]{1, 3}, new int[]{5, 8}, new int[]{10, 10}, new int[]{14, Integer.MAX_VALUE}); // -3,5-8,10,14-
    private final static String EMPTY = "";
    private final static String LINE_2_SINGLE = "n";
    private final static String LINE_3_SINGLE = "R";
    private final static String LINE_4_B_SINGLE = new String(EDGE_CHAR_0);
    private final static String LINE_4_C_SINGLE = "4";
    private final static String LINE_2_RANGE = "fnro";
    private final static String LINE_3_RANGE = "FR# ";
    private final static String LINE_4_B_RANGE = "eñ4";
    private final static String LINE_4_C_RANGE = "ñ4c2";
    private final static String LINE_2_START = "fnroe4353fedds2";
    private final static String LINE_3_START = "FR# F#@DEFRF F$#@!!@#";
    private final static String LINE_4_B_START = "eñ4c2 on3tent";
    private final static String LINE_4_C_START = "ñ4c2 on3tent";
    private final static String LINE_2_END = "evenw";
    private final static String LINE_3_END = "FR%$#";
    private final static String LINE_4_B_END = "ñcas";
    private final static String LINE_4_C_END = "ñcas ";
    private final static String LINE_1_LIST = "u";
    private final static String LINE_2_LIST = "vwo";
    private final static String LINE_3_LIST = "R# ";
    private final static String LINE_4_B_LIST = new String(EDGE_CHAR_1) + "s4";
    private final static String LINE_4_C_LIST = "c 2";
    private final static String LINE_2_MIX = "evewpfno53fedds2";
    private final static String LINE_3_MIX = "FR%#FFR DEFRF F$#@!!@#";
    private final static String LINE_4_B_MIX = "ñcs e" + new String(EDGE_CHAR_0) + "4on3tent";
    private final static String LINE_4_C_MIX = "ñca eñ423tent";
    private final static List<String> MULTI_LIST = Arrays.asList(LINE_1, LINE_2, LINE_3, LINE_4);


    @Test
    public void countDashAndNumCheck_onlyNumNoDash_noDash() throws Exception {
        int actualCount = CutUtils.countDashAndNumCheck(NUM_NO_DASH);
        assertEquals(0, actualCount);
    }

    @Test
    public void countDashAndNumCheck_onlyNumOneDashMid_oneDash() throws Exception {
        int actualCount = CutUtils.countDashAndNumCheck(NUM_MID_DASH);
        assertEquals(1, actualCount);
    }

    @Test
    public void countDashAndNumCheck_onlyNumOneDashFront_oneDash() throws Exception {
        int actualCount = CutUtils.countDashAndNumCheck(NUM_FRONT_DASH);
        assertEquals(1, actualCount);
    }

    @Test
    public void countDashAndNumCheck_onlyNumOneDashBack_oneDash() throws Exception {
        int actualCount = CutUtils.countDashAndNumCheck(NUM_BACK_DASH);
        assertEquals(1, actualCount);
    }

    @Test
    public void countDashAndNumCheck_onlyNumMultiDash_multiDash() throws Exception {
        int actualCount = CutUtils.countDashAndNumCheck(NUM_MULTI_DASH);
        assertEquals(5, actualCount);
    }

    @Test
    public void countDashAndNumCheck_mixNoDash_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> CutUtils.countDashAndNumCheck(CHAR_NO_DASH));
        assertEquals(INVALID_LIST, exception.getMessage());
    }

    @Test
    public void countDashAndNumCheck_mixOneDash_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> CutUtils.countDashAndNumCheck(CHAR_ONE_DASH));
        assertEquals(INVALID_LIST, exception.getMessage());
    }

    @Test
    public void countDashAndNumCheck_mixMultiDash_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> CutUtils.countDashAndNumCheck(CHAR_MULTI_DASH));
        assertEquals(INVALID_LIST, exception.getMessage());
    }

    @Test
    public void getNumRange_onlyNumDashMid_range() throws Exception {
        int[] actualRange = CutUtils.getNumRange(NUM_MID_DASH);
        assertTrue(Arrays.equals(MID_DASH, actualRange));
    }

    @Test
    public void getNumRange_onlyNumDashFront_range() throws Exception {
        int[] actualRange = CutUtils.getNumRange(NUM_FRONT_DASH);
        assertTrue(Arrays.equals(FRONT_DASH, actualRange));
    }

    @Test
    public void getNumRange_onlyNumDashBack_range() throws Exception {
        int[] actualRange = CutUtils.getNumRange(NUM_BACK_DASH);
        assertTrue(Arrays.equals(BACK_DASH, actualRange));
    }

    @Test
    public void getNumRange_onlyNumOnlyDash_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> CutUtils.getNumRange(DASH));
        assertEquals(INVALID_LIST, exception.getMessage());
    }

    @Test
    public void getNumRange_onlyNumNoDash_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> CutUtils.getNumRange(NUM_NO_DASH));
        assertEquals(INVALID_RANGE, exception.getMessage());
    }

    @Test
    public void getNumRange_onlyNumMultiDash_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> CutUtils.getNumRange(NUM_MULTI_DASH));
        assertEquals(INVALID_LIST, exception.getMessage());
    }

    @Test
    public void getNumRange_mix_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> CutUtils.getNumRange(CHAR_ONE_DASH));
        assertEquals(INVALID_RANGE, exception.getMessage());
    }

    @Test
    public void getNumRange_moreThanMaxIntDashMid_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> CutUtils.getNumRange(MAX_MID_DASH));
        assertEquals(INVALID_RANGE, exception.getMessage());
    }

    @Test
    public void getNumRange_moreThanMaxIntDashFront_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> CutUtils.getNumRange(MAX_FRONT_DASH));
        assertEquals(INVALID_RANGE, exception.getMessage());
    }

    @Test
    public void getNumRange_moreThanMaxIntDashBack_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> CutUtils.getNumRange(MAX_BACK_DASH));
        assertEquals(INVALID_RANGE, exception.getMessage());
    }

    @Test
    public void getNumRange_decreasingRange_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> CutUtils.getNumRange(DEC_RANGE));
        assertEquals(INVALID_DEC_RANGE, exception.getMessage());
    }

    @Test
    public void mergeNumRanges_singleRange_mergedRanges() {
        List<int[]> actualRanges = CutUtils.mergeNumRanges(SINGLE_RANGE);
        assertEquals(SINGLE_RANGE, actualRanges);
    }

    @Test
    public void mergeNumRanges_unsortNoMerge_mergedRanges() {
        List<int[]> actualRanges = CutUtils.mergeNumRanges(UNSORT_NO_MERGE);
        assertListEquals(NO_MERGE, actualRanges);
    }

    @Test
    public void mergeNumRanges_sortNoMerge_mergedRanges() {
        List<int[]> actualRanges = CutUtils.mergeNumRanges(NO_MERGE);
        assertListEquals(NO_MERGE, actualRanges);
    }

    @Test
    public void mergeNumRanges_unsortMerge_mergedRanges() {
        List<int[]> actualRanges = CutUtils.mergeNumRanges(UNSORT_MERGE_IN);
        assertListEquals(MERGE_OUT, actualRanges);
    }

    @Test
    public void mergeNumRanges_sortMerge_mergedRanges() {
        List<int[]> actualRanges = CutUtils.mergeNumRanges(SORT_MERGE_IN);
        assertListEquals(MERGE_OUT, actualRanges);
    }

    @Test
    public void cutInputString_charShortSingle_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_1, LIST_SINGLE);
        assertEquals(EMPTY, actualString);
    }

    @Test
    public void cutInputString_byteShortSingle_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_1, LIST_SINGLE);
        assertEquals(EMPTY, actualString);
    }

    @Test
    public void cutInputString_charNoSpaceSingle_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_2, LIST_SINGLE);
        assertEquals(LINE_2_SINGLE, actualString);
    }

    @Test
    public void cutInputString_byteNoSpaceSingle_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_2, LIST_SINGLE);
        assertEquals(LINE_2_SINGLE, actualString);
    }

    @Test
    public void cutInputString_charSpaceSingle_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_3, LIST_SINGLE);
        assertEquals(LINE_3_SINGLE, actualString);
    }

    @Test
    public void cutInputString_byteSpaceSingle_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_3, LIST_SINGLE);
        assertEquals(LINE_3_SINGLE, actualString);
    }

    @Test
    public void cutInputString_charEdgeSingle_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_4, LIST_SINGLE);
        assertEquals(LINE_4_C_SINGLE, actualString);
    }

    @Test
    public void cutInputString_byteEdgeSingle_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_4, LIST_SINGLE);
        assertEquals(LINE_4_B_SINGLE, actualString);
    }

    @Test
    public void cutInputString_charShortStartRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_1, LIST_START_RANGE);
        assertEquals(EMPTY, actualString);
    }

    @Test
    public void cutInputString_byteShortStartRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_1, LIST_START_RANGE);
        assertEquals(EMPTY, actualString);
    }

    @Test
    public void cutInputString_charNoSpaceStartRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_2, LIST_START_RANGE);
        assertEquals(LINE_2_START, actualString);
    }

    @Test
    public void cutInputString_byteNoSpaceStartRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_2, LIST_START_RANGE);
        assertEquals(LINE_2_START, actualString);
    }

    @Test
    public void cutInputString_charSpaceStartRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_3, LIST_START_RANGE);
        assertEquals(LINE_3_START, actualString);
    }

    @Test
    public void cutInputString_byteSpaceStartRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_3, LIST_START_RANGE);
        assertEquals(LINE_3_START, actualString);
    }

    @Test
    public void cutInputString_charEdgeStartRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_4, LIST_START_RANGE);
        assertEquals(LINE_4_C_START, actualString);
    }

    @Test
    public void cutInputString_byteEdgeStartRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_4, LIST_START_RANGE);
        assertEquals(LINE_4_B_START, actualString);
    }

    @Test
    public void cutInputString_charShortEndRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_1, LIST_END_RANGE);
        assertEquals(LINE_1, actualString);
    }

    @Test
    public void cutInputString_byteShortEndRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_1, LIST_END_RANGE);
        assertEquals(LINE_1, actualString);
    }

    @Test
    public void cutInputString_charNoSpaceEndRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_2, LIST_END_RANGE);
        assertEquals(LINE_2_END, actualString);
    }

    @Test
    public void cutInputString_byteNoSpaceEndRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_2, LIST_END_RANGE);
        assertEquals(LINE_2_END, actualString);
    }

    @Test
    public void cutInputString_charSpaceEndRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_3, LIST_END_RANGE);
        assertEquals(LINE_3_END, actualString);
    }

    @Test
    public void cutInputString_byteSpaceEndRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_3, LIST_END_RANGE);
        assertEquals(LINE_3_END, actualString);
    }

    @Test
    public void cutInputString_charEdgeEndRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_4, LIST_END_RANGE);
        assertEquals(LINE_4_C_END, actualString);
    }

    @Test
    public void cutInputString_byteEdgeEndRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_4, LIST_END_RANGE);
        assertEquals(LINE_4_B_END, actualString);
    }

    @Test
    public void cutInputString_charShortRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_1, LIST_RANGE);
        assertEquals(EMPTY, actualString);
    }

    @Test
    public void cutInputString_byteShortRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_1, LIST_RANGE);
        assertEquals(EMPTY, actualString);
    }

    @Test
    public void cutInputString_charNoSpaceRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_2, LIST_RANGE);
        assertEquals(LINE_2_RANGE, actualString);
    }

    @Test
    public void cutInputString_byteNoSpaceRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_2, LIST_RANGE);
        assertEquals(LINE_2_RANGE, actualString);
    }

    @Test
    public void cutInputString_charSpaceRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_3, LIST_RANGE);
        assertEquals(LINE_3_RANGE, actualString);
    }

    @Test
    public void cutInputString_byteSpaceRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_3, LIST_RANGE);
        assertEquals(LINE_3_RANGE, actualString);
    }

    @Test
    public void cutInputString_charEdgeRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_4, LIST_RANGE);
        assertEquals(LINE_4_C_RANGE, actualString);
    }

    @Test
    public void cutInputString_byteEdgeRange_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_4, LIST_RANGE);
        assertEquals(LINE_4_B_RANGE, actualString);
    }

    @Test
    public void cutInputString_charShortList_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_1, LIST_LIST);
        assertEquals(LINE_1_LIST, actualString);
    }

    @Test
    public void cutInputString_byteShortList_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_1, LIST_LIST);
        assertEquals(LINE_1_LIST, actualString);
    }

    @Test
    public void cutInputString_charNoSpaceList_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_2, LIST_LIST);
        assertEquals(LINE_2_LIST, actualString);
    }

    @Test
    public void cutInputString_byteNoSpaceList_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_2, LIST_LIST);
        assertEquals(LINE_2_LIST, actualString);
    }

    @Test
    public void cutInputString_charSpaceList_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_3, LIST_LIST);
        assertEquals(LINE_3_LIST, actualString);
    }

    @Test
    public void cutInputString_byteSpaceList_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_3, LIST_LIST);
        assertEquals(LINE_3_LIST, actualString);
    }

    @Test
    public void cutInputString_charEdgeList_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_4, LIST_LIST);
        assertEquals(LINE_4_C_LIST, actualString);
    }

    @Test
    public void cutInputString_byteEdgeList_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_4, LIST_LIST);
        assertEquals(LINE_4_B_LIST, actualString);
    }

    @Test
    public void cutInputString_charShortMix_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_1, LIST_MIX);
        assertEquals(LINE_1, actualString);
    }

    @Test
    public void cutInputString_byteShortMix_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_1, LIST_MIX);
        assertEquals(LINE_1, actualString);
    }

    @Test
    public void cutInputString_charNoSpaceMix_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_2, LIST_MIX);
        assertEquals(LINE_2_MIX, actualString);
    }

    @Test
    public void cutInputString_byteNoSpaceMix_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_2, LIST_MIX);
        assertEquals(LINE_2_MIX, actualString);
    }

    @Test
    public void cutInputString_charSpaceMix_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_3, LIST_MIX);
        assertEquals(LINE_3_MIX, actualString);
    }

    @Test
    public void cutInputString_byteSpaceMix_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_3, LIST_MIX);
        assertEquals(LINE_3_MIX, actualString);
    }

    @Test
    public void cutInputString_charEdgeMix_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, LINE_4, LIST_MIX);
        assertEquals(LINE_4_C_MIX, actualString);
    }

    @Test
    public void cutInputString_byteEdgeMix_cutString() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, LINE_4, LIST_MIX);
        assertEquals(LINE_4_B_MIX, actualString);
    }

    @Test
    public void cutInputString_charEmptyMix_empty() throws Exception {
        String actualString = CutUtils.cutInputString(true, false, EMPTY, LIST_MIX);
        assertEquals(EMPTY, actualString);
    }

    @Test
    public void cutInputString_byteEmptyMix_empty() throws Exception {
        String actualString = CutUtils.cutInputString(false, true, EMPTY, LIST_MIX);
        assertEquals(EMPTY, actualString);
    }

    // character and byte options
    @Test
    public void cutInputString_charByteShortMix_empty() {
        Exception exception = assertThrows(Exception.class, () -> CutUtils.cutInputString(true, true, LINE_1, LIST_MIX));
        assertEquals(INVALID_FLAG, exception.getMessage());
    }

    // no options
    @Test
    public void cutInputString_noOpEmptyMix_empty() {
        Exception exception = assertThrows(Exception.class, () -> CutUtils.cutInputString(false, false, LINE_1, LIST_MIX));
        assertEquals(INVALID_FLAG, exception.getMessage());
    }

    @Test
    public void cutInputString_charSpaceNull_empty() {
        Exception exception = assertThrows(Exception.class, () -> CutUtils.cutInputString(true, false, LINE_3, null));
        assertEquals(INVALID_LIST, exception.getMessage());
    }

    @Test
    public void cutInputStringList_charSingleSingle_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(true, false, Arrays.asList(LINE_2), LIST_SINGLE);
        assertEquals(Arrays.asList(LINE_2_SINGLE), actualStringList);
    }

    @Test
    public void cutInputStringList_byteSingleSingle_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(false, true, Arrays.asList(LINE_2), LIST_SINGLE);
        assertEquals(Arrays.asList(LINE_2_SINGLE), actualStringList);
    }

    @Test
    public void cutInputStringList_charSingleStartRange_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(true, false, Arrays.asList(LINE_3), LIST_START_RANGE);
        assertEquals(Arrays.asList(LINE_3_START), actualStringList);
    }

    @Test
    public void cutInputStringList_byteSingleStartRange_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(false, true, Arrays.asList(LINE_3), LIST_START_RANGE);
        assertEquals(Arrays.asList(LINE_3_START), actualStringList);
    }

    @Test
    public void cutInputStringList_charSingleEndRange_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(true, false, Arrays.asList(LINE_4), LIST_END_RANGE);
        assertEquals(Arrays.asList(LINE_4_C_END), actualStringList);
    }

    @Test
    public void cutInputStringList_byteSingleEndRange_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(false, true, Arrays.asList(LINE_4), LIST_END_RANGE);
        assertEquals(Arrays.asList(LINE_4_B_END), actualStringList);
    }

    @Test
    public void cutInputStringList_charSingleRange_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(true, false, Arrays.asList(LINE_2), LIST_RANGE);
        assertEquals(Arrays.asList(LINE_2_RANGE), actualStringList);
    }

    @Test
    public void cutInputStringList_byteSingleRange_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(false, true, Arrays.asList(LINE_2), LIST_RANGE);
        assertEquals(Arrays.asList(LINE_2_RANGE), actualStringList);
    }

    @Test
    public void cutInputStringList_charSingleList_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(true, false, Arrays.asList(LINE_3), LIST_LIST);
        assertEquals(Arrays.asList(LINE_3_LIST), actualStringList);
    }

    @Test
    public void cutInputStringList_byteSingleList_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(false, true, Arrays.asList(LINE_3), LIST_LIST);
        assertEquals(Arrays.asList(LINE_3_LIST), actualStringList);
    }

    @Test
    public void cutInputStringList_charSingleMix_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(true, false, Arrays.asList(LINE_4), LIST_MIX);
        assertEquals(Arrays.asList(LINE_4_C_MIX), actualStringList);
    }

    @Test
    public void cutInputStringList_byteSingleMix_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(false, true, Arrays.asList(LINE_4), LIST_MIX);
        assertEquals(Arrays.asList(LINE_4_B_MIX), actualStringList);
    }

    @Test
    public void cutInputStringList_charMultiSingle_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(true, false, MULTI_LIST, LIST_SINGLE);
        assertEquals(Arrays.asList(EMPTY, LINE_2_SINGLE, LINE_3_SINGLE, LINE_4_C_SINGLE), actualStringList);
    }

    @Test
    public void cutInputStringList_byteMultiSingle_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(false, true, MULTI_LIST, LIST_SINGLE);
        assertEquals(Arrays.asList(EMPTY, LINE_2_SINGLE, LINE_3_SINGLE, LINE_4_B_SINGLE), actualStringList);
    }

    @Test
    public void cutInputStringList_charMultiStartRange_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(true, false, MULTI_LIST, LIST_START_RANGE);
        assertEquals(Arrays.asList(EMPTY, LINE_2_START, LINE_3_START, LINE_4_C_START), actualStringList);
    }

    @Test
    public void cutInputStringList_byteMultiStartRange_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(false, true, MULTI_LIST, LIST_START_RANGE);
        assertEquals(Arrays.asList(EMPTY, LINE_2_START, LINE_3_START, LINE_4_B_START), actualStringList);
    }

    @Test
    public void cutInputStringList_charMultiEndRange_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(true, false, MULTI_LIST, LIST_END_RANGE);
        assertEquals(Arrays.asList(LINE_1, LINE_2_END, LINE_3_END, LINE_4_C_END), actualStringList);
    }

    @Test
    public void cutInputStringList_byteMultiEndRange_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(false, true, MULTI_LIST, LIST_END_RANGE);
        assertEquals(Arrays.asList(LINE_1, LINE_2_END, LINE_3_END, LINE_4_B_END), actualStringList);
    }

    @Test
    public void cutInputStringList_charMultiRange_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(true, false, MULTI_LIST, LIST_RANGE);
        assertEquals(Arrays.asList(EMPTY, LINE_2_RANGE, LINE_3_RANGE, LINE_4_C_RANGE), actualStringList);
    }

    @Test
    public void cutInputStringList_byteMultiRange_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(false, true, MULTI_LIST, LIST_RANGE);
        assertEquals(Arrays.asList(EMPTY, LINE_2_RANGE, LINE_3_RANGE, LINE_4_B_RANGE), actualStringList);
    }

    @Test
    public void cutInputStringList_charMultiList_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(true, false, MULTI_LIST, LIST_LIST);
        assertEquals(Arrays.asList(LINE_1_LIST, LINE_2_LIST, LINE_3_LIST, LINE_4_C_LIST), actualStringList);
    }

    @Test
    public void cutInputStringList_byteMultiList_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(false, true, MULTI_LIST, LIST_LIST);
        assertEquals(Arrays.asList(LINE_1_LIST, LINE_2_LIST, LINE_3_LIST, LINE_4_B_LIST), actualStringList);
    }

    @Test
    public void cutInputStringList_charMultiMix_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(true, false, MULTI_LIST, LIST_MIX);
        assertEquals(Arrays.asList(LINE_1, LINE_2_MIX, LINE_3_MIX, LINE_4_C_MIX), actualStringList);
    }

    @Test
    public void cutInputStringList_byteMultiMix_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(false, true, MULTI_LIST, LIST_MIX);
        assertEquals(Arrays.asList(LINE_1, LINE_2_MIX, LINE_3_MIX, LINE_4_B_MIX), actualStringList);
    }

    @Test
    public void cutInputStringList_charEmptyMix_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(true, false, Arrays.asList(), LIST_MIX);
        assertEquals(Arrays.asList(), actualStringList);
    }

    @Test
    public void cutInputStringList_byteEmptyMix_cutStringList() throws Exception {
        List<String> actualStringList = CutUtils.cutInputStringList(false, true, Arrays.asList(), LIST_MIX);
        assertEquals(Arrays.asList(), actualStringList);
    }

    @Test
    public void cutInputStringList_charMultiNull_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> CutUtils.cutInputStringList(true, false, MULTI_LIST, null));
        assertEquals(INVALID_LIST, exception.getMessage());
    }

    @Test
    public void cutInputStringList_charByteMultiMix_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> CutUtils.cutInputStringList(true, true, MULTI_LIST, LIST_MIX));
        assertEquals(INVALID_FLAG, exception.getMessage());
    }

    @Test
    public void cutInputStringList_noOpMultiMix_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> CutUtils.cutInputStringList(false, false, MULTI_LIST, LIST_MIX));
        assertEquals(INVALID_FLAG, exception.getMessage());
    }
}