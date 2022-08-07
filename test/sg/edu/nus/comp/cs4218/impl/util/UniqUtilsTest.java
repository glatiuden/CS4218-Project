package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.app.UniqApplication.ERR_C_CAP_D;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

class UniqUtilsTest {
    private final static String NORM_NEWLINE = "\n";
    private final static String EMPTY = "";
    private final static List<String> SINGLE_EMPTY = Arrays.asList(EMPTY);
    private final static List<String> MULTI_EMPTY = Arrays.asList(EMPTY, EMPTY, EMPTY);
    private final static String C_SINGLE_EMP_O = "\t1 " + STRING_NEWLINE;
    private final static String C_MULTI_EMP_O = "\t3 " + STRING_NEWLINE;
    private final static String CAP_D_MULTI_EMP_O = STRING_NEWLINE + STRING_NEWLINE + STRING_NEWLINE;
    private final static String SINGLE_CONT = "single";
    private final static String DUP_CONT = "duplicate\nduplicate\nno duplicate\nmore dup\nmore dup\nmoredup\nno more\n123 123\n123 123\n123 123\nevenmore\nevenmore\nevenmore\nevenmore";
    private final static String LAST_UNIQ_CONT = "dup !icate\ndup !icate\ndup licate\ndup licate\ndup\ndup\ndup\nno dup";
    private final static String ALL_UNIQ_CONT = "all uniq\nuniq\nall uniq\n uniq\ndup?\nno dup";
    private final static String SINGLE_O = "single" + STRING_NEWLINE;
    private final static String C_SINGLE_O = "\t1 single" + STRING_NEWLINE;
    private final static String DUP_O = "duplicate\nno duplicate\nmore dup\nmoredup\nno more\n123 123\nevenmore\n".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String C_DUP_O = "\t2 duplicate\n\t1 no duplicate\n\t2 more dup\n\t1 moredup\n\t1 no more\n\t3 123 123\n\t4 evenmore\n".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String D_DUP_O = "duplicate\nmore dup\n123 123\nevenmore\n".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String CAP_D_DUP_O = "duplicate\nduplicate\nmore dup\nmore dup\n123 123\n123 123\n123 123\nevenmore\nevenmore\nevenmore\nevenmore\n".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String CD_DUP_O = "\t2 duplicate\n\t2 more dup\n\t3 123 123\n\t4 evenmore\n".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String LAST_UNIQ_O = "dup !icate\ndup licate\ndup\nno dup\n".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String C_LAST_UNIQ_O = "\t2 dup !icate\n\t2 dup licate\n\t3 dup\n\t1 no dup\n".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String D_LAST_UNIQ_O = "dup !icate\ndup licate\ndup\n".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String CAP_D_LAST_UNIQ_O = "dup !icate\ndup !icate\ndup licate\ndup licate\ndup\ndup\ndup\n".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String CD_LAST_UNIQ_O = "\t2 dup !icate\n\t2 dup licate\n\t3 dup\n".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String ALL_UNIQ_O = "all uniq\nuniq\nall uniq\n uniq\ndup?\nno dup\n".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String C_ALL_UNIQ_O = "\t1 all uniq\n\t1 uniq\n\t1 all uniq\n\t1  uniq\n\t1 dup?\n\t1 no dup\n".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private static ByteArrayOutputStream testOutputStream;

    @BeforeAll
    public static void setUp() {
        testOutputStream = new ByteArrayOutputStream();
    }

    @AfterEach
    public void reset() {
        testOutputStream.reset();
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input list: empty
    @Test
    public void uniqInputList_TTFEmpty_TTFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(true, true, false, new ArrayList<>(), testOutputStream);
        assertEquals(new ArrayList<>(), actualList);
        assertEquals(EMPTY, testOutputStream.toString());
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input list: single empty content
    @Test
    public void uniqInputList_TTFSingleEmpty_TTFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(true, true, false, SINGLE_EMPTY, testOutputStream);
        assertEquals(new ArrayList<>(), actualList);
        assertEquals(EMPTY, testOutputStream.toString());
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input list: multi empty content
    @Test
    public void uniqInputList_TTFMultiEmpty_TTFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(true, true, false, MULTI_EMPTY, testOutputStream);
        assertEquals(Arrays.asList(C_MULTI_EMP_O.split(STRING_NEWLINE)), actualList);
        assertEquals(C_MULTI_EMP_O, testOutputStream.toString());
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input list: single
    @Test
    public void uniqInputList_TTFSingle_TTFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(true, true, false, Arrays.asList(SINGLE_CONT), testOutputStream);
        assertEquals(new ArrayList<>(), actualList);
        assertEquals(EMPTY, testOutputStream.toString());
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input list: duplicate
    @Test
    public void uniqInputList_TTFDup_TTFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(true, true, false, Arrays.asList(DUP_CONT.split(NORM_NEWLINE)), testOutputStream);
        assertEquals(Arrays.asList(CD_DUP_O.split(STRING_NEWLINE)), actualList);
        assertEquals(CD_DUP_O, testOutputStream.toString());
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input list: all unique
    @Test
    public void uniqInputList_TTFAllUniq_TTFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(true, true, false, Arrays.asList(ALL_UNIQ_CONT.split(NORM_NEWLINE)), testOutputStream);
        assertEquals(new ArrayList<>(), actualList);
        assertEquals(EMPTY, testOutputStream.toString());
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input list: last unique
    @Test
    public void uniqInputList_TTFLastUniq_TTFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(true, true, false, Arrays.asList(LAST_UNIQ_CONT.split(NORM_NEWLINE)), testOutputStream);
        assertEquals(Arrays.asList(CD_LAST_UNIQ_O.split(STRING_NEWLINE)), actualList);
        assertEquals(CD_LAST_UNIQ_O, testOutputStream.toString());
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, input list: empty
    @Test
    public void uniqInputList_TFFEmpty_TFFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(true, false, false, new ArrayList<>(), testOutputStream);
        assertEquals(new ArrayList<>(), actualList);
        assertEquals(EMPTY, testOutputStream.toString());
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, input list: single empty content
    @Test
    public void uniqInputList_TFFSingleEmpty_TFFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(true, false, false, SINGLE_EMPTY, testOutputStream);
        assertEquals(Arrays.asList(C_SINGLE_EMP_O.split(STRING_NEWLINE)), actualList);
        assertEquals(C_SINGLE_EMP_O, testOutputStream.toString());
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, input list: multi empty content
    @Test
    public void uniqInputList_TFFMultiEmpty_TFFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(true, false, false, MULTI_EMPTY, testOutputStream);
        assertEquals(Arrays.asList(C_MULTI_EMP_O.split(STRING_NEWLINE)), actualList);
        assertEquals(C_MULTI_EMP_O, testOutputStream.toString());
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, input list: single
    @Test
    public void uniqInputList_TFFSingle_TFFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(true, false, false, Arrays.asList(SINGLE_CONT), testOutputStream);
        assertEquals(Arrays.asList(C_SINGLE_O.split(STRING_NEWLINE)), actualList);
        assertEquals(C_SINGLE_O, testOutputStream.toString());
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, input list: duplicate
    @Test
    public void uniqInputList_TFFDup_TFFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(true, false, false, Arrays.asList(DUP_CONT.split(NORM_NEWLINE)), testOutputStream);
        assertEquals(Arrays.asList(C_DUP_O.split(STRING_NEWLINE)), actualList);
        assertEquals(C_DUP_O, testOutputStream.toString());
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, input list: all unique
    @Test
    public void uniqInputList_TFFAllUniq_TFFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(true, false, false, Arrays.asList(ALL_UNIQ_CONT.split(NORM_NEWLINE)), testOutputStream);
        assertEquals(Arrays.asList(C_ALL_UNIQ_O.split(STRING_NEWLINE)), actualList);
        assertEquals(C_ALL_UNIQ_O, testOutputStream.toString());
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, input list: last unique
    @Test
    public void uniqInputList_TFFLastUniq_TFFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(true, false, false, Arrays.asList(LAST_UNIQ_CONT.split(NORM_NEWLINE)), testOutputStream);
        assertEquals(Arrays.asList(C_LAST_UNIQ_O.split(STRING_NEWLINE)), actualList);
        assertEquals(C_LAST_UNIQ_O, testOutputStream.toString());
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, input list: empty
    @Test
    public void uniqInputList_FTTEmpty_FTTList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, true, true, new ArrayList<>(), testOutputStream);
        assertEquals(new ArrayList<>(), actualList);
        assertEquals(EMPTY, testOutputStream.toString());
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, input list: single empty content
    @Test
    public void uniqInputList_FTTSingleEmpty_FTTList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, true, true, SINGLE_EMPTY, testOutputStream);
        assertEquals(new ArrayList<>(), actualList);
        assertEquals(EMPTY, testOutputStream.toString());
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, input list: multi empty content
    @Test
    public void uniqInputList_FTTMultiEmpty_FTTList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, true, true, MULTI_EMPTY, testOutputStream);
        assertEquals(MULTI_EMPTY, actualList);
        assertEquals(CAP_D_MULTI_EMP_O, testOutputStream.toString());
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, input list: single
    @Test
    public void uniqInputList_FTTSingle_FTTList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, true, true, Arrays.asList(SINGLE_CONT), testOutputStream);
        assertEquals(new ArrayList<>(), actualList);
        assertEquals(EMPTY, testOutputStream.toString());
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, input list: duplicate
    @Test
    public void uniqInputList_FTTDup_FTTList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, true, true, Arrays.asList(DUP_CONT.split(NORM_NEWLINE)), testOutputStream);
        assertEquals(Arrays.asList(CAP_D_DUP_O.split(STRING_NEWLINE)), actualList);
        assertEquals(CAP_D_DUP_O, testOutputStream.toString());
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, input list: all unique
    @Test
    public void uniqInputList_FTTAllUniq_FTTList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, true, true, Arrays.asList(ALL_UNIQ_CONT.split(NORM_NEWLINE)), testOutputStream);
        assertEquals(new ArrayList<>(), actualList);
        assertEquals(EMPTY, testOutputStream.toString());
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, input list: last unique
    @Test
    public void uniqInputList_FTTLastUniq_FTTList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, true, true, Arrays.asList(LAST_UNIQ_CONT.split(NORM_NEWLINE)), testOutputStream);
        assertEquals(Arrays.asList(CAP_D_LAST_UNIQ_O.split(STRING_NEWLINE)), actualList);
        assertEquals(CAP_D_LAST_UNIQ_O, testOutputStream.toString());
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, input list: empty
    @Test
    public void uniqInputList_FTFEmpty_FTFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, true, false, new ArrayList<>(), testOutputStream);
        assertEquals(new ArrayList<>(), actualList);
        assertEquals(EMPTY, testOutputStream.toString());
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, input list: single empty content
    @Test
    public void uniqInputList_FTFSingleEmpty_FTFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, true, false, SINGLE_EMPTY, testOutputStream);
        assertEquals(new ArrayList<>(), actualList);
        assertEquals(EMPTY, testOutputStream.toString());
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, input list: multi empty content
    @Test
    public void uniqInputList_FTFMultiEmpty_FTFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, true, false, MULTI_EMPTY, testOutputStream);
        assertEquals(SINGLE_EMPTY, actualList);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, input list: single
    @Test
    public void uniqInputList_FTFSingle_FTFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, true, false, Arrays.asList(SINGLE_CONT), testOutputStream);
        assertEquals(new ArrayList<>(), actualList);
        assertEquals(EMPTY, testOutputStream.toString());
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, input list: duplicate
    @Test
    public void uniqInputList_FTFDup_FTFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, true, false, Arrays.asList(DUP_CONT.split(NORM_NEWLINE)), testOutputStream);
        assertEquals(Arrays.asList(D_DUP_O.split(STRING_NEWLINE)), actualList);
        assertEquals(D_DUP_O, testOutputStream.toString());
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, input list: all unique
    @Test
    public void uniqInputList_FTFAllUniq_FTFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, true, false, Arrays.asList(ALL_UNIQ_CONT.split(NORM_NEWLINE)), testOutputStream);
        assertEquals(new ArrayList<>(), actualList);
        assertEquals(EMPTY, testOutputStream.toString());
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, input list: last unique
    @Test
    public void uniqInputList_FTFLastUniq_FTFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, true, false, Arrays.asList(LAST_UNIQ_CONT.split(NORM_NEWLINE)), testOutputStream);
        assertEquals(Arrays.asList(D_LAST_UNIQ_O.split(STRING_NEWLINE)), actualList);
        assertEquals(D_LAST_UNIQ_O, testOutputStream.toString());
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, input list: empty
    @Test
    public void uniqInputList_FFTEmpty_FFTList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, false, true, new ArrayList<>(), testOutputStream);
        assertEquals(new ArrayList<>(), actualList);
        assertEquals(EMPTY, testOutputStream.toString());
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, input list: single empty content
    @Test
    public void uniqInputList_FFTSingleEmpty_FFTList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, false, true, SINGLE_EMPTY, testOutputStream);
        assertEquals(new ArrayList<>(), actualList);
        assertEquals(EMPTY, testOutputStream.toString());
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, input list: multi empty content
    @Test
    public void uniqInputList_FFTMultiEmpty_FFTList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, false, true, MULTI_EMPTY, testOutputStream);
        assertEquals(MULTI_EMPTY, actualList);
        assertEquals(CAP_D_MULTI_EMP_O, testOutputStream.toString());
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, input list: single
    @Test
    public void uniqInputList_FFTSingle_FFTList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, false, true, Arrays.asList(SINGLE_CONT), testOutputStream);
        assertEquals(new ArrayList<>(), actualList);
        assertEquals(EMPTY, testOutputStream.toString());
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, input list: duplicate
    @Test
    public void uniqInputList_FFTDup_FFTList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, false, true, Arrays.asList(DUP_CONT.split(NORM_NEWLINE)), testOutputStream);
        assertEquals(Arrays.asList(CAP_D_DUP_O.split(STRING_NEWLINE)), actualList);
        assertEquals(CAP_D_DUP_O, testOutputStream.toString());
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, input list: all unique
    @Test
    public void uniqInputList_FFTAllUniq_FFTList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, false, true, Arrays.asList(ALL_UNIQ_CONT.split(NORM_NEWLINE)), testOutputStream);
        assertEquals(new ArrayList<>(), actualList);
        assertEquals(EMPTY, testOutputStream.toString());
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, input list: last unique
    @Test
    public void uniqInputList_FFTLastUniq_FFTList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, false, true, Arrays.asList(LAST_UNIQ_CONT.split(NORM_NEWLINE)), testOutputStream);
        assertEquals(Arrays.asList(CAP_D_LAST_UNIQ_O.split(STRING_NEWLINE)), actualList);
        assertEquals(CAP_D_LAST_UNIQ_O, testOutputStream.toString());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input list: empty
    @Test
    public void uniqInputList_FFFEmpty_FFFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, false, false, new ArrayList<>(), testOutputStream);
        assertEquals(new ArrayList<>(), actualList);
        assertEquals(EMPTY, testOutputStream.toString());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input list: single empty content
    @Test
    public void uniqInputList_FFFSingleEmpty_FFFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, false, false, SINGLE_EMPTY, testOutputStream);
        assertEquals(SINGLE_EMPTY, actualList);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input list: multi empty content
    @Test
    public void uniqInputList_FFFMultiEmpty_FFFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, false, false, MULTI_EMPTY, testOutputStream);
        assertEquals(SINGLE_EMPTY, actualList);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input list: single
    @Test
    public void uniqInputList_FFFSingle_FFFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, false, false, Arrays.asList(SINGLE_CONT), testOutputStream);
        assertEquals(Arrays.asList(SINGLE_CONT), actualList);
        assertEquals(SINGLE_O, testOutputStream.toString());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input list: duplicate
    @Test
    public void uniqInputList_FFFDup_FFFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, false, false, Arrays.asList(DUP_CONT.split(NORM_NEWLINE)), testOutputStream);
        assertEquals(Arrays.asList(DUP_O.split(STRING_NEWLINE)), actualList);
        assertEquals(DUP_O, testOutputStream.toString());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input list: all unique
    @Test
    public void uniqInputList_FFFAllUniq_FFFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, false, false, Arrays.asList(ALL_UNIQ_CONT.split(NORM_NEWLINE)), testOutputStream);
        assertEquals(Arrays.asList(ALL_UNIQ_O.split(STRING_NEWLINE)), actualList);
        assertEquals(ALL_UNIQ_O, testOutputStream.toString());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input list: last unique
    @Test
    public void uniqInputList_FFFLastUniq_FFFList() throws Exception {
        List<String> actualList = UniqUtils.uniqInputList(false, false, false, Arrays.asList(LAST_UNIQ_CONT.split(NORM_NEWLINE)), testOutputStream);
        assertEquals(Arrays.asList(LAST_UNIQ_O.split(STRING_NEWLINE)), actualList);
        assertEquals(LAST_UNIQ_O, testOutputStream.toString());
    }

    // isCount: true, isRepeated: false, isAllRepeated: true, input list: last unique
    @Test
    public void uniqInputList_TFTLastUniq_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> UniqUtils.uniqInputList(true, false, true, Arrays.asList(LAST_UNIQ_CONT.split(NORM_NEWLINE)), testOutputStream));
        assertEquals(ERR_C_CAP_D, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input list: null
    @Test
    public void uniqInputList_FFFNullInput_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> UniqUtils.uniqInputList(false, false, false, null, testOutputStream));
        assertEquals(ERR_NULL_ARGS, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input list: last unique, null output
    @Test
    public void uniqInputList_FFFNullOutput_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> UniqUtils.uniqInputList(false, false, false, Arrays.asList(LAST_UNIQ_CONT.split(NORM_NEWLINE)), null));
        assertEquals(ERR_NULL_STREAMS, exception.getMessage());
    }
}