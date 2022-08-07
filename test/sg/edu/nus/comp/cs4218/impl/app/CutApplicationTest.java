package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.impl.app.args.CutArguments;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_FLAG;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_LIST;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

class CutApplicationTest {
    private final static String STDIN_DASH = "-";
    private final static String CHAR_FLAG = "-c";
    private final static String BYTE_FLAG = "-b";
    private final static String FILE_1 = "1.txt";
    private final static String FILE_2 = "2.txt";
    private final static String FILE_SINGLE = "single.txt";
    private final static String FILE_EMPTY = "empty.txt";
    private final static String FILE_EDGE = "edge.txt";
    private final static String FILE_NE = "nef";
    private final static String NE_DIR = "ned";
    private final static String NEST_DIR = "nest";
    private final static String EMPTY_CONT = "";
    private final static String SINGLE_LINE_CONT = "single line content";
    private final static String MULTI_LINE_CONT = "mul\nlinef3242fdvse4tvd\ncontent\nevenwpfnroe4353fedds2\nmorecontentbutnospace\ncontentnospace";
    private final static String MULTI_LINE_CONT_2 = "FR%$#FFR# F#@DEFRF F$#@!!@#\n!EDEFR RET$#F!D #$FFG^ffmo3f\n!@3frenf4f f3i4r0f 34r43f$#Fgre43\n\n    \nspace\nnospace\n$%#RFRVw32f f2123f";
    private final static byte[] EDGE_CHAR_1 = {"ñ".getBytes()[1]};
    private final static String EDGE_CONT = "edg1e ñcaseñ4c2on3tent\nev ñmoreed gñe\nñand moñre";
    private final static String LIST_SINGLE_STR = "8";
    private final static String LIST_RANGE_STR = "7-10";
    private final static String LIST_START_STR = "7-";
    private final static String LIST_END_STR = "-5";
    private final static String LIST_LIST_STR = "2,5,10";
    private final static String LIST_MIX_STR = "-3,5-8,10,14-";
    private final static List<int[]> LIST_SINGLE = Arrays.asList(new int[]{8, 8}); // 8
    private final static List<int[]> LIST_RANGE = Arrays.asList(new int[]{7, 10}); // 7-10
    private final static List<int[]> LIST_START_RANGE = Arrays.asList(new int[]{7, Integer.MAX_VALUE}); // 7-
    private final static List<int[]> LIST_END_RANGE = Arrays.asList(new int[]{1, 5}); // -5
    private final static List<int[]> LIST_LIST = Arrays.asList(new int[]{2, 2}, new int[]{5, 5}, new int[]{10, 10}); // 2,5,10
    private final static List<int[]> LIST_MIX = Arrays.asList(new int[]{1, 3}, new int[]{5, 8}, new int[]{10, 10}, new int[]{14, Integer.MAX_VALUE}); // -3,5-8,10,14-
    private final static String EDGE_CHAR_SINGLE = "c" + STRING_NEWLINE + "e" + STRING_NEWLINE + "ñ";
    private final static String EDGE_BYTE_SINGLE = new String(EDGE_CHAR_1) + STRING_NEWLINE + "r" + STRING_NEWLINE + "o";
    private final static String EDGE_CHAR_RANGE = "ñcas" + STRING_NEWLINE + "reed" + STRING_NEWLINE + "oñre";
    private final static String EDGE_BYTE_RANGE = "ñca" + STRING_NEWLINE + "oree" + STRING_NEWLINE + "moñ";
    private final static String EDGE_CHAR_START = "ñcaseñ4c2on3tent" + STRING_NEWLINE + "reed gñe" + STRING_NEWLINE + "oñre";
    private final static String EDGE_BYTE_START = "ñcaseñ4c2on3tent" + STRING_NEWLINE + "oreed gñe" + STRING_NEWLINE + "moñre";
    private final static String EDGE_CHAR_END = "edg1e" + STRING_NEWLINE + "ev ñm" + STRING_NEWLINE + "ñand ";
    private final static String EDGE_BYTE_END = "edg1e" + STRING_NEWLINE + "ev ñ" + STRING_NEWLINE + "ñand";
    private final static String EDGE_CHAR_LIST = "des" + STRING_NEWLINE + "vmd" + STRING_NEWLINE + "a e";
    private final static String EDGE_BYTE_LIST = "dea" + STRING_NEWLINE + "v" + new String(EDGE_CHAR_1) + "e" + STRING_NEWLINE + new String(EDGE_CHAR_1) + "d" + new String(EDGE_CHAR_1);
    private final static String EDGE_CHAR_MIX = "edge ñcsc2on3tent" + STRING_NEWLINE + "ev morede" + STRING_NEWLINE + "ñan moñe";
    private final static String EDGE_BYTE_MIX = "edge ña" + new String(EDGE_CHAR_1) + "4c2on3tent" + STRING_NEWLINE + "ev " + new String(EDGE_CHAR_1) + "moreñe" + STRING_NEWLINE + "ñad mo" + new String(EDGE_CHAR_1);
    private final static String SINGLE_MIX = "sinle lnontent";
    private final static String MULTI_SINGLE = STRING_NEWLINE + "4" + STRING_NEWLINE + STRING_NEWLINE + "n" + STRING_NEWLINE + "t" + STRING_NEWLINE + "n";
    private final static String MULTI_RANGE = STRING_NEWLINE + "242f" + STRING_NEWLINE + "t" + STRING_NEWLINE + "fnro" + STRING_NEWLINE + "nten" + STRING_NEWLINE + "tnos";
    private final static String MULTI_START = STRING_NEWLINE + "242fdvse4tvd" + STRING_NEWLINE + "t" + STRING_NEWLINE + "fnroe4353fedds2" + STRING_NEWLINE + "ntentbutnospace" + STRING_NEWLINE + "tnospace";
    private final static String MULTI_END = "mul" + STRING_NEWLINE + "linef" + STRING_NEWLINE + "conte" + STRING_NEWLINE + "evenw" + STRING_NEWLINE + "morec" + STRING_NEWLINE + "conte";
    private final static String MULTI_LIST = "u" + STRING_NEWLINE + "iff" + STRING_NEWLINE + "oe" + STRING_NEWLINE + "vwo" + STRING_NEWLINE + "ocn" + STRING_NEWLINE + "oes";
    private final static String MULTI_MIX = "mul" + STRING_NEWLINE + "linf324fe4tvd" + STRING_NEWLINE + "conent" + STRING_NEWLINE + "evewpfno53fedds2" + STRING_NEWLINE + "morcontntnospace" + STRING_NEWLINE + "conentnse";
    private final static String MULTI_2_SINGLE = "R" + STRING_NEWLINE + "R" + STRING_NEWLINE + "f" + STRING_NEWLINE + STRING_NEWLINE + STRING_NEWLINE + STRING_NEWLINE + STRING_NEWLINE + "w";
    private final static String MULTI_2_RANGE = "FR# " + STRING_NEWLINE + " RET" + STRING_NEWLINE + "nf4f" + STRING_NEWLINE + STRING_NEWLINE + STRING_NEWLINE + STRING_NEWLINE + "e" + STRING_NEWLINE + "Vw32";
    private final static String MULTI_2_START = "FR# F#@DEFRF F$#@!!@#" + STRING_NEWLINE + " RET$#F!D #$FFG^ffmo3f" + STRING_NEWLINE + "nf4f f3i4r0f 34r43f$#Fgre43" + STRING_NEWLINE + STRING_NEWLINE + STRING_NEWLINE + STRING_NEWLINE + "e" + STRING_NEWLINE + "Vw32f f2123f";
    private final static String MULTI_2_END = "FR%$#" + STRING_NEWLINE + "!EDEF" + STRING_NEWLINE + "!@3fr" + STRING_NEWLINE + STRING_NEWLINE + "    " + STRING_NEWLINE + "space" + STRING_NEWLINE + "nospa" + STRING_NEWLINE + "$%#RF";
    private final static String MULTI_2_LIST = "R# " + STRING_NEWLINE + "EFT" + STRING_NEWLINE + "@rf" + STRING_NEWLINE + STRING_NEWLINE + " " + STRING_NEWLINE + "pe" + STRING_NEWLINE + "oa" + STRING_NEWLINE + "%F2";
    private final static String MULTI_2_MIX = "FR%#FFR DEFRF F$#@!!@#" + STRING_NEWLINE + "!EDFR RT!D #$FFG^ffmo3f" + STRING_NEWLINE + "!@3renffi4r0f 34r43f$#Fgre43" + STRING_NEWLINE + STRING_NEWLINE + "   " + STRING_NEWLINE + "spae" + STRING_NEWLINE + "nosace" + STRING_NEWLINE + "$%#FRVw22123f";
    private final static String CUT_EXCEP = "cut: ";
    @TempDir
    public static Path folderPath;
    private static CutApplication cutApp;
    private static CutArguments cutArg;
    private static ByteArrayOutputStream testOutputStream;
    private static ByteArrayOutputStream testErrorStream;
    private static Path file1Path;
    private static Path file2Path;
    private static Path fileSinglePath;
    private static Path fileEmptyPath;
    private static Path fileEdgePath;
    private static Path file1DirPath;

    @BeforeAll
    public static void setUp() throws IOException {
        cutApp = new CutApplication();
        cutArg = mock(CutArguments.class);
        cutApp.setCutArgs(cutArg);

        testOutputStream = new ByteArrayOutputStream();
        testErrorStream = new ByteArrayOutputStream();
        cutApp.setStdout(testOutputStream);
        System.setOut(new PrintStream(testErrorStream));

        file1Path = folderPath.resolve(FILE_1);
        file2Path = folderPath.resolve(FILE_2);
        fileSinglePath = folderPath.resolve(FILE_SINGLE);
        fileEmptyPath = folderPath.resolve(FILE_EMPTY);
        fileEdgePath = folderPath.resolve(FILE_EDGE);
        file1DirPath = folderPath.resolve(NEST_DIR).resolve(FILE_1);

        // ./nest
        Files.createDirectories(folderPath.resolve(NEST_DIR));
        // ./1.txt
        Files.write(file1Path, MULTI_LINE_CONT.getBytes());
        // ./2.txt
        Files.write(file2Path, MULTI_LINE_CONT_2.getBytes());
        // ./empty.txt
        Files.createFile(fileEmptyPath);
        // ./single.txt
        Files.write(fileSinglePath, SINGLE_LINE_CONT.getBytes());
        // ./edge.txt
        Files.write(fileEdgePath, EDGE_CONT.getBytes());
        // ./nest/1.txt
        Files.write(file1DirPath, MULTI_LINE_CONT.getBytes());
    }

    @AfterEach
    public void resetStreams() {
        testOutputStream.reset();
        testErrorStream.reset();
    }

    // All files multiple lines unless stated otherwise
    // character, single number, single file
    @Test
    public void cutFromFiles_charSingleSingle_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_SINGLE, file1Path.toString());
        assertEquals(MULTI_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_SINGLE, testResult);
    }

    // character, single number, multiple files
    @Test
    public void cutFromFiles_charSingleMulti_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_SINGLE, file1Path.toString(), file2Path.toString());
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_2_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_2_SINGLE, testResult);
    }

    // character, start range, single file
    @Test
    public void cutFromFiles_charStartRangeSingle_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_START_RANGE, file1Path.toString());
        assertEquals(MULTI_START + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_START, testResult);
    }

    // character, start range, multiple files
    @Test
    public void cutFromFiles_charStartRangeMulti_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_START_RANGE, file1Path.toString(), file2Path.toString());
        assertEquals(MULTI_START + STRING_NEWLINE + MULTI_2_START + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_START + STRING_NEWLINE + MULTI_2_START, testResult);
    }

    // character, end range, single file
    @Test
    public void cutFromFiles_charEndRangeSingle_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_END_RANGE, file1Path.toString());
        assertEquals(MULTI_END + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_END, testResult);
    }

    // character, end range, multiple files
    @Test
    public void cutFromFiles_charEndRangeMulti_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_END_RANGE, file2Path.toString(), file1Path.toString());
        assertEquals(MULTI_2_END + STRING_NEWLINE + MULTI_END + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_2_END + STRING_NEWLINE + MULTI_END, testResult);
    }

    // character, range, single file
    @Test
    public void cutFromFiles_charRangeSingle_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_RANGE, file2Path.toString());
        assertEquals(MULTI_2_RANGE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_2_RANGE, testResult);
    }

    // character, range, multiple files
    @Test
    public void cutFromFiles_charRangeMulti_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_RANGE, file2Path.toString(), file1Path.toString());
        assertEquals(MULTI_2_RANGE + STRING_NEWLINE + MULTI_RANGE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_2_RANGE + STRING_NEWLINE + MULTI_RANGE, testResult);
    }

    // character, list, single file
    @Test
    public void cutFromFiles_charListSingle_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_LIST, file2Path.toString());
        assertEquals(MULTI_2_LIST + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_2_LIST, testResult);
    }

    // character, list, multiple files
    @Test
    public void cutFromFiles_charListMulti_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_LIST, file2Path.toString(), file1Path.toString());
        assertEquals(MULTI_2_LIST + STRING_NEWLINE + MULTI_LIST + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_2_LIST + STRING_NEWLINE + MULTI_LIST, testResult);
    }

    // character, mixed, single file
    @Test
    public void cutFromFiles_charMixSingle_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_MIX, file1Path.toString());
        assertEquals(MULTI_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_MIX, testResult);
    }

    // character, mixed, multiple files
    @Test
    public void cutFromFiles_charMixMulti_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_MIX, file2Path.toString(), file1Path.toString());
        assertEquals(MULTI_2_MIX + STRING_NEWLINE + MULTI_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_2_MIX + STRING_NEWLINE + MULTI_MIX, testResult);
    }

    // byte, single, single file
    @Test
    public void cutFromFiles_byteSingleSingle_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_SINGLE, file1Path.toString());
        assertEquals(MULTI_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_SINGLE, testResult);
    }

    // byte, single, multiple files
    @Test
    public void cutFromFiles_byteSingleMulti_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_SINGLE, file1Path.toString(), file2Path.toString());
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_2_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_2_SINGLE, testResult);
    }

    // byte, start range, single file
    @Test
    public void cutFromFiles_byteStartRangeSingle_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_START_RANGE, file2Path.toString());
        assertEquals(MULTI_2_START + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_2_START, testResult);
    }

    // byte, start range, multiple files
    @Test
    public void cutFromFiles_byteStartRangeMulti_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_START_RANGE, file1Path.toString(), file2Path.toString());
        assertEquals(MULTI_START + STRING_NEWLINE + MULTI_2_START + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_START + STRING_NEWLINE + MULTI_2_START, testResult);
    }

    // byte, end range, single file
    @Test
    public void cutFromFiles_byteEndRangeSingle_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_END_RANGE, file2Path.toString());
        assertEquals(MULTI_2_END + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_2_END, testResult);
    }

    // byte, end range, multiple files
    @Test
    public void cutFromFiles_byteEndRangeMulti_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_END_RANGE, file2Path.toString(), file1Path.toString());
        assertEquals(MULTI_2_END + STRING_NEWLINE + MULTI_END + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_2_END + STRING_NEWLINE + MULTI_END, testResult);
    }

    // byte, range, single file
    @Test
    public void cutFromFiles_byteRangeSingle_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_RANGE, file1Path.toString());
        assertEquals(MULTI_RANGE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_RANGE, testResult);
    }

    // byte, range, multiple files
    @Test
    public void cutFromFiles_byteRangeMulti_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_RANGE, file2Path.toString(), file1Path.toString());
        assertEquals(MULTI_2_RANGE + STRING_NEWLINE + MULTI_RANGE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_2_RANGE + STRING_NEWLINE + MULTI_RANGE, testResult);
    }

    // byte, list, single file
    @Test
    public void cutFromFiles_byteListSingle_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_LIST, file1Path.toString());
        assertEquals(MULTI_LIST + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_LIST, testResult);
    }

    // byte, list, multiple files
    @Test
    public void cutFromFiles_byteListMulti_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_LIST, file2Path.toString(), file1Path.toString());
        assertEquals(MULTI_2_LIST + STRING_NEWLINE + MULTI_LIST + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_2_LIST + STRING_NEWLINE + MULTI_LIST, testResult);
    }

    // byte, mix, single file
    @Test
    public void cutFromFiles_byteMixSingle_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_MIX, file2Path.toString());
        assertEquals(MULTI_2_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_2_MIX, testResult);
    }

    // byte, mix, multiple files
    @Test
    public void cutFromFiles_byteMixMulti_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_MIX, file1Path.toString(), file2Path.toString());
        assertEquals(MULTI_MIX + STRING_NEWLINE + MULTI_2_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_MIX + STRING_NEWLINE + MULTI_2_MIX, testResult);
    }

    // character, mix, file in directory
    @Test
    public void cutFromFiles_charMixSingleDir_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_MIX, file1DirPath.toString());
        assertEquals(MULTI_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_MIX, testResult);
    }

    // byte, mix, file in directory multi file
    @Test
    public void cutFromFiles_byteMixMultiDir_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_MIX, file1DirPath.toString(), file2Path.toString());
        assertEquals(MULTI_MIX + STRING_NEWLINE + MULTI_2_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_MIX + STRING_NEWLINE + MULTI_2_MIX, testResult);
    }

    // character, mix, empty file
    @Test
    public void cutFromFiles_charMixEmpty_empty() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_MIX, fileEmptyPath.toString());
        assertEquals(EMPTY_CONT, testOutputStream.toString());
        assertEquals(EMPTY_CONT, testResult);
    }

    // byte, mix, empty file
    @Test
    public void cutFromFiles_byteMixEmpty_empty() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_MIX, fileEmptyPath.toString());
        assertEquals(EMPTY_CONT, testOutputStream.toString());
        assertEquals(EMPTY_CONT, testResult);
    }

    // character, mix, single line file
    @Test
    public void cutFromFiles_charMixSingleLine_cutLine() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_MIX, fileSinglePath.toString());
        assertEquals(SINGLE_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(SINGLE_MIX, testResult);
    }

    // byte, mix, single line file
    @Test
    public void cutFromFiles_byteMixSingleLine_cutLine() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_MIX, fileSinglePath.toString());
        assertEquals(SINGLE_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(SINGLE_MIX, testResult);
    }

    // character, single, edge case file
    @Test
    public void cutFromFiles_charSingleEdge_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_SINGLE, fileEdgePath.toString());
        assertEquals(EDGE_CHAR_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_CHAR_SINGLE, testResult);
    }

    // byte, single, edge case file
    @Test
    public void cutFromFiles_byteSingleEdge_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_SINGLE, fileEdgePath.toString());
        assertEquals(EDGE_BYTE_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_BYTE_SINGLE, testResult);
    }

    // character, start range, edge case file
    @Test
    public void cutFromFiles_charStartRangeEdge_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_START_RANGE, fileEdgePath.toString());
        assertEquals(EDGE_CHAR_START + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_CHAR_START, testResult);
    }

    // byte, start range, edge case file
    @Test
    public void cutFromFiles_byteStartRangeEdge_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_START_RANGE, fileEdgePath.toString());
        assertEquals(EDGE_BYTE_START + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_BYTE_START, testResult);
    }

    // character, end range, edge case file
    @Test
    public void cutFromFiles_charEndRangeEdge_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_END_RANGE, fileEdgePath.toString());
        assertEquals(EDGE_CHAR_END + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_CHAR_END, testResult);
    }

    // byte, end range, edge case file
    @Test
    public void cutFromFiles_byteEndRangeEdge_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_END_RANGE, fileEdgePath.toString());
        assertEquals(EDGE_BYTE_END + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_BYTE_END, testResult);
    }

    // character, range, edge case file
    @Test
    public void cutFromFiles_charRangeEdge_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_RANGE, fileEdgePath.toString());
        assertEquals(EDGE_CHAR_RANGE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_CHAR_RANGE, testResult);
    }

    // byte, range, edge case file
    @Test
    public void cutFromFiles_byteRangeEdge_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_RANGE, fileEdgePath.toString());
        assertEquals(EDGE_BYTE_RANGE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_BYTE_RANGE, testResult);
    }

    // character, list, edge case file
    @Test
    public void cutFromFiles_charListEdge_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_LIST, fileEdgePath.toString());
        assertEquals(EDGE_CHAR_LIST + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_CHAR_LIST, testResult);
    }

    // byte, list, edge case file
    @Test
    public void cutFromFiles_byteListEdge_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_LIST, fileEdgePath.toString());
        assertEquals(EDGE_BYTE_LIST + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_BYTE_LIST, testResult);
    }

    // character, mix, edge case file
    @Test
    public void cutFromFiles_charMixEdge_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_MIX, fileEdgePath.toString());
        assertEquals(EDGE_CHAR_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_CHAR_MIX, testResult);
    }

    // byte, mix, edge case file
    @Test
    public void cutFromFiles_byteMixEdge_cutLines() throws Exception {
        String testResult = cutApp.cutFromFiles(false, true, LIST_MIX, fileEdgePath.toString());
        assertEquals(EDGE_BYTE_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_BYTE_MIX, testResult);
    }

    // character, single number, directory
    @Test
    public void cutFromFiles_charSingleDir_printError() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_SINGLE, folderPath.resolve(NEST_DIR).toString());
        assertEquals(EMPTY_CONT, testOutputStream.toString());
        assertEquals(EMPTY_CONT, testResult);
        assertEquals(CUT_EXCEP + folderPath.resolve(NEST_DIR) + ": " + ERR_IS_DIR + STRING_NEWLINE, testErrorStream.toString());
    }

    // character, single number, directory with other valid files
    @Test
    public void cutFromFiles_charSingleDirWithValid_printError() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_SINGLE, file1Path.toString(), folderPath.resolve(NEST_DIR).toString(), file2Path.toString());
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_2_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_2_SINGLE, testResult);
        assertEquals(CUT_EXCEP + folderPath.resolve(NEST_DIR) + ": " + ERR_IS_DIR + STRING_NEWLINE, testErrorStream.toString());
    }

    // character, single number, non-existent file
    @Test
    public void cutFromFiles_charSingleNonExistFile_printError() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_SINGLE, folderPath.resolve(FILE_NE).toString());
        assertEquals(EMPTY_CONT, testOutputStream.toString());
        assertEquals(EMPTY_CONT, testResult);
        assertEquals(CUT_EXCEP + folderPath.resolve(FILE_NE) + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE, testErrorStream.toString());
    }

    // character, single number, non-existent file with other valid files
    @Test
    public void cutFromFiles_charSingleNonExistFileWithValid_cutLinesPrintError() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_SINGLE, file1Path.toString(), folderPath.resolve(FILE_NE).toString(), file2Path.toString());
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_2_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_2_SINGLE, testResult);
        assertEquals(CUT_EXCEP + folderPath.resolve(FILE_NE) + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE, testErrorStream.toString());
    }

    // character, single number, non-existent directory
    @Test
    public void cutFromFiles_charSingleNonExistDir_printError() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_SINGLE, folderPath.resolve(NE_DIR).resolve(FILE_1).toString());
        assertEquals(EMPTY_CONT, testOutputStream.toString());
        assertEquals(EMPTY_CONT, testResult);
        assertEquals(CUT_EXCEP + folderPath.resolve(NE_DIR).resolve(FILE_1) + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE, testErrorStream.toString());
    }

    // character, single number, non-existent directory with other valid files
    @Test
    public void cutFromFiles_charSingleNonExistDirWithValid_cutLinesPrintError() throws Exception {
        String testResult = cutApp.cutFromFiles(true, false, LIST_SINGLE, file1Path.toString(), folderPath.resolve(NE_DIR).resolve(FILE_1).toString(), file2Path.toString());
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_2_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_2_SINGLE, testResult);
        assertEquals(CUT_EXCEP + folderPath.resolve(NE_DIR).resolve(FILE_1) + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE, testErrorStream.toString());
    }

    // character, single number, null
    @Test
    public void cutFromFiles_charSingleNull_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> cutApp.cutFromFiles(true, false, LIST_SINGLE, null));
        assertEquals(ERR_NULL_ARGS, exception.getMessage());
    }

    // character, single number, null
    @Test
    public void cutFromFiles_charNullSingle_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> cutApp.cutFromFiles(true, false, null, file1Path.toString()));
        assertEquals(INVALID_LIST, exception.getMessage());
    }

    // character and byte, single number, single file
    @Test
    public void cutFromFiles_charByteSingleSingle_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> cutApp.cutFromFiles(true, true, LIST_SINGLE, file1Path.toString()));
        assertEquals(INVALID_FLAG, exception.getMessage());
    }

    // no options, single number, single file
    @Test
    public void cutFromFiles_noOPSingleSingle_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> cutApp.cutFromFiles(false, false, LIST_SINGLE, file1Path.toString()));
        assertEquals(INVALID_FLAG, exception.getMessage());
    }

    // character, single number
    @Test
    public void cutFromStdin_charSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(true, false, LIST_SINGLE, testStdin);
        assertEquals(MULTI_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_SINGLE, testResult);
        testStdin.close();
    }

    // byte, single number
    @Test
    public void cutFromStdin_byteSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(false, true, LIST_SINGLE, testStdin);
        assertEquals(MULTI_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_SINGLE, testResult);
        testStdin.close();
    }

    // character, start range
    @Test
    public void cutFromStdin_charStartRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(true, false, LIST_START_RANGE, testStdin);
        assertEquals(MULTI_START + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_START, testResult);
        testStdin.close();
    }

    // byte, start range
    @Test
    public void cutFromStdin_byteStartRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(false, true, LIST_START_RANGE, testStdin);
        assertEquals(MULTI_START + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_START, testResult);
        testStdin.close();
    }

    // character, end range
    @Test
    public void cutFromStdin_charEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String testResult = cutApp.cutFromStdin(true, false, LIST_END_RANGE, testStdin);
        assertEquals(MULTI_2_END + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_2_END, testResult);
        testStdin.close();
    }

    // byte, end range
    @Test
    public void cutFromStdin_byteEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String testResult = cutApp.cutFromStdin(false, true, LIST_END_RANGE, testStdin);
        assertEquals(MULTI_2_END + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_2_END, testResult);
        testStdin.close();
    }

    // character, range
    @Test
    public void cutFromStdin_charRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String testResult = cutApp.cutFromStdin(true, false, LIST_RANGE, testStdin);
        assertEquals(MULTI_2_RANGE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_2_RANGE, testResult);
        testStdin.close();
    }

    // byte, range
    @Test
    public void cutFromStdin_byteRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String testResult = cutApp.cutFromStdin(false, true, LIST_RANGE, testStdin);
        assertEquals(MULTI_2_RANGE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_2_RANGE, testResult);
        testStdin.close();
    }

    // character, list
    @Test
    public void cutFromStdin_charList_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String testResult = cutApp.cutFromStdin(true, false, LIST_LIST, testStdin);
        assertEquals(MULTI_2_LIST + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_2_LIST, testResult);
        testStdin.close();
    }

    // byte, list
    @Test
    public void cutFromStdin_byteList_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String testResult = cutApp.cutFromStdin(false, true, LIST_LIST, testStdin);
        assertEquals(MULTI_2_LIST + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_2_LIST, testResult);
        testStdin.close();
    }

    // character, mix
    @Test
    public void cutFromStdin_charMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(true, false, LIST_MIX, testStdin);
        assertEquals(MULTI_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_MIX, testResult);
        testStdin.close();
    }

    // byte, mix
    @Test
    public void cutFromStdin_byteMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(false, true, LIST_MIX, testStdin);
        assertEquals(MULTI_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(MULTI_MIX, testResult);
        testStdin.close();
    }

    // character, mix, single line
    @Test
    public void cutFromStdin_charMixSingleLine_cutLine() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(SINGLE_LINE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(true, false, LIST_MIX, testStdin);
        assertEquals(SINGLE_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(SINGLE_MIX, testResult);
        testStdin.close();
    }

    // character, mix, empty line
    @Test
    public void cutFromStdin_charMixEmpty_empty() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String testResult = cutApp.cutFromStdin(true, false, LIST_MIX, testStdin);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EMPTY_CONT, testResult);
        testStdin.close();
    }

    // character, null
    @Test
    public void cutFromStdin_charNull_throwsException() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> cutApp.cutFromStdin(true, false, null, testStdin));
        assertEquals(INVALID_LIST, exception.getMessage());
        testStdin.close();
    }

    // character and byte, single number
    @Test
    public void cutFromStdin_charByteSingle_throwsException() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> cutApp.cutFromStdin(true, true, LIST_SINGLE, testStdin));
        assertEquals(INVALID_FLAG, exception.getMessage());
        testStdin.close();
    }

    // no options, single number
    @Test
    public void cutFromStdin_noOpSingle_throwsException() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> cutApp.cutFromStdin(false, false, LIST_SINGLE, testStdin));
        assertEquals(INVALID_FLAG, exception.getMessage());
        testStdin.close();
    }

    // character, single number, edge case
    @Test
    public void cutFromStdin_charSingleEdge_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(true, false, LIST_SINGLE, testStdin);
        assertEquals(EDGE_CHAR_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_CHAR_SINGLE, testResult);
        testStdin.close();
    }

    // byte, single number, edge case
    @Test
    public void cutFromStdin_byteSingleEdge_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(false, true, LIST_SINGLE, testStdin);
        assertEquals(EDGE_BYTE_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_BYTE_SINGLE, testResult);
        testStdin.close();
    }

    // character, start range, edge case
    @Test
    public void cutFromStdin_charStartRangeEdge_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(true, false, LIST_START_RANGE, testStdin);
        assertEquals(EDGE_CHAR_START + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_CHAR_START, testResult);
        testStdin.close();
    }

    // byte, start range, edge case
    @Test
    public void cutFromStdin_byteStartRangeEdge_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(false, true, LIST_START_RANGE, testStdin);
        assertEquals(EDGE_BYTE_START + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_BYTE_START, testResult);
        testStdin.close();
    }

    // character, end range, edge case
    @Test
    public void cutFromStdin_charEndRangeEdge_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(true, false, LIST_END_RANGE, testStdin);
        assertEquals(EDGE_CHAR_END + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_CHAR_END, testResult);
        testStdin.close();
    }

    // byte, end range, edge case
    @Test
    public void cutFromStdin_byteEndRangeEdge_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(false, true, LIST_END_RANGE, testStdin);
        assertEquals(EDGE_BYTE_END + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_BYTE_END, testResult);
        testStdin.close();
    }

    // character, range, edge case
    @Test
    public void cutFromStdin_charRangeEdge_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(true, false, LIST_RANGE, testStdin);
        assertEquals(EDGE_CHAR_RANGE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_CHAR_RANGE, testResult);
        testStdin.close();
    }

    // byte, range, edge case
    @Test
    public void cutFromStdin_byteRangeEdge_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(false, true, LIST_RANGE, testStdin);
        assertEquals(EDGE_BYTE_RANGE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_BYTE_RANGE, testResult);
        testStdin.close();
    }

    // character, list, edge case
    @Test
    public void cutFromStdin_charListEdge_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(true, false, LIST_LIST, testStdin);
        assertEquals(EDGE_CHAR_LIST + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_CHAR_LIST, testResult);
        testStdin.close();
    }

    // byte, list, edge case
    @Test
    public void cutFromStdin_byteListEdge_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(false, true, LIST_LIST, testStdin);
        assertEquals(EDGE_BYTE_LIST + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_BYTE_LIST, testResult);
        testStdin.close();
    }

    // character, mix, edge case
    @Test
    public void cutFromStdin_charMixEdge_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(true, false, LIST_MIX, testStdin);
        assertEquals(EDGE_CHAR_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_CHAR_MIX, testResult);
        testStdin.close();
    }

    // byte, mix, edge case
    @Test
    public void cutFromStdin_byteMixEdge_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        String testResult = cutApp.cutFromStdin(false, true, LIST_MIX, testStdin);
        assertEquals(EDGE_BYTE_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(EDGE_BYTE_MIX, testResult);
        testStdin.close();
    }

    // character, multi lines, empty, multi lines, mix
    @Test
    public void cutFromStdinAndFiles_charMultiEmptyMultiMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_MIX, testStdin, fileEmptyPath.toString(), STDIN_DASH, file2Path.toString());
        assertEquals(MULTI_MIX + STRING_NEWLINE + MULTI_2_MIX + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi lines, empty, multi lines, mix
    @Test
    public void cutFromStdinAndFiles_byteMultiEmptyMultiMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_MIX, testStdin, STDIN_DASH, fileEmptyPath.toString(), file2Path.toString());
        assertEquals(MULTI_MIX + STRING_NEWLINE + MULTI_2_MIX + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi lines, empty, multi lines, end range
    @Test
    public void cutFromStdinAndFiles_charMultiEmptyMultiEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_END_RANGE, testStdin, fileEmptyPath.toString(), file2Path.toString(), STDIN_DASH);
        assertEquals(MULTI_2_END + STRING_NEWLINE + MULTI_END + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi lines, empty, multi lines, end range
    @Test
    public void cutFromStdinAndFiles_byteMultiEmptyMultiEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_END_RANGE, testStdin, STDIN_DASH, fileEmptyPath.toString(), file2Path.toString());
        assertEquals(MULTI_END + STRING_NEWLINE + MULTI_2_END + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, not given, multi lines, single number
    @Test
    public void cutFromStdinAndFiles_charEmptyNilMultiSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_SINGLE, testStdin, file2Path.toString(), STDIN_DASH);
        assertEquals(MULTI_2_SINGLE + STRING_NEWLINE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, not given, multi lines, single number
    @Test
    public void cutFromStdinAndFiles_byteEmptyNilMultiSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_SINGLE, testStdin, STDIN_DASH, file2Path.toString());
        assertEquals(STRING_NEWLINE + MULTI_2_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, empty, not given, start range
    @Test
    public void cutFromStdinAndFiles_charEmptyEmptyNilStartRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_START_RANGE, testStdin, fileEmptyPath.toString(), STDIN_DASH);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, empty, empty, not given, start range
    @Test
    public void cutFromStdinAndFiles_byteEmptyEmptyNilStartRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_START_RANGE, testStdin, STDIN_DASH, fileEmptyPath.toString());
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, multi lines, empty, end range
    @Test
    public void cutFromStdinAndFiles_charEmptyMultiEmptyEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_END_RANGE, testStdin, fileEmptyPath.toString(), STDIN_DASH, file1Path.toString());
        assertEquals(STRING_NEWLINE + MULTI_END + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, multi lines, empty, end range
    @Test
    public void cutFromStdinAndFiles_byteEmptyMultiEmptyEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_END_RANGE, testStdin, STDIN_DASH, fileEmptyPath.toString(), file1Path.toString());
        assertEquals(STRING_NEWLINE + MULTI_END + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, not given, multi lines, range
    @Test
    public void cutFromStdinAndFiles_charEmptyNilMultiRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_RANGE, testStdin, STDIN_DASH, file1Path.toString());
        assertEquals(STRING_NEWLINE + MULTI_RANGE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, not given, multi lines, range
    @Test
    public void cutFromStdinAndFiles_byteEmptyNilMultiRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_RANGE, testStdin, file1Path.toString(), STDIN_DASH);
        assertEquals(MULTI_RANGE + STRING_NEWLINE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, empty, not given, list
    @Test
    public void cutFromStdinAndFiles_charEmptyEmptyNilList_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_LIST, testStdin, STDIN_DASH, fileEmptyPath.toString());
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, empty, not given, list
    @Test
    public void cutFromStdinAndFiles_byteEmptyEmptyNilList_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_LIST, testStdin, fileEmptyPath.toString(), STDIN_DASH);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, multi lines, empty, mix
    @Test
    public void cutFromStdinAndFiles_charEmptyMultiEmptyMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_MIX, testStdin, STDIN_DASH, file2Path.toString(), fileEmptyPath.toString());
        assertEquals(STRING_NEWLINE + MULTI_2_MIX + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, multi lines, empty, mix
    @Test
    public void cutFromStdinAndFiles_byteEmptyMultiEmptyMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_MIX, testStdin, file2Path.toString(), fileEmptyPath.toString(), STDIN_DASH);
        assertEquals(MULTI_2_MIX + STRING_NEWLINE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi lines, empty, empty, range
    @Test
    public void cutFromStdinAndFiles_charMultiEmptyEmptyRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_RANGE, testStdin, STDIN_DASH, fileEmptyPath.toString(), fileEmptyPath.toString());
        assertEquals(MULTI_RANGE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi lines, empty, empty, range
    @Test
    public void cutFromStdinAndFiles_byteMultiEmptyEmptyRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_RANGE, testStdin, fileEmptyPath.toString(), fileEmptyPath.toString(), STDIN_DASH);
        assertEquals(MULTI_RANGE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi lines, multi lines, multi lines, list
    @Test
    public void cutFromStdinAndFiles_charMultiMultiMultiList_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_LIST, testStdin, file1Path.toString(), STDIN_DASH, file2Path.toString());
        assertEquals(MULTI_LIST + STRING_NEWLINE + MULTI_LIST + STRING_NEWLINE + MULTI_2_LIST + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi lines, multi lines, multi lines, list
    @Test
    public void cutFromStdinAndFiles_byteMultiMultiMultiList_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_LIST, testStdin, file1Path.toString(), file2Path.toString(), STDIN_DASH);
        assertEquals(MULTI_LIST + STRING_NEWLINE + MULTI_2_LIST + STRING_NEWLINE + MULTI_LIST + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi lines, not given, not given, mix
    @Test
    public void cutFromStdinAndFiles_charMultiNilNilMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_MIX, testStdin, STDIN_DASH);
        assertEquals(MULTI_MIX + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi lines, not given, not given, mix
    @Test
    public void cutFromStdinAndFiles_byteMultiNilNilMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_MIX, testStdin, STDIN_DASH);
        assertEquals(MULTI_MIX + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi lines, empty, empty, single number
    @Test
    public void cutFromStdinAndFiles_charMultiEmptyEmptySingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_SINGLE, testStdin, fileEmptyPath.toString(), STDIN_DASH, fileEmptyPath.toString());
        assertEquals(MULTI_2_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi lines, empty, empty, single number
    @Test
    public void cutFromStdinAndFiles_byteMultiEmptyEmptySingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_SINGLE, testStdin, fileEmptyPath.toString(), fileEmptyPath.toString(), STDIN_DASH);
        assertEquals(MULTI_2_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi lines, multi lines, multi lines, start range
    @Test
    public void cutFromStdinAndFiles_charMultiMultiMultiStartRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_START_RANGE, testStdin, file1Path.toString(), STDIN_DASH, file2Path.toString());
        assertEquals(MULTI_START + STRING_NEWLINE + EDGE_CHAR_START + STRING_NEWLINE + MULTI_2_START + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi lines, multi lines, multi lines, start range
    @Test
    public void cutFromStdinAndFiles_byteMultiMultiMultiStartRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_START_RANGE, testStdin, file1Path.toString(), file2Path.toString(), STDIN_DASH);
        assertEquals(MULTI_START + STRING_NEWLINE + MULTI_2_START + STRING_NEWLINE + EDGE_BYTE_START + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi lines, not given, not given, end range
    @Test
    public void cutFromStdinAndFiles_charMultiNilNilEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_END_RANGE, testStdin, STDIN_DASH);
        assertEquals(MULTI_END + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi lines, not given, not given, end range
    @Test
    public void cutFromStdinAndFiles_byteMultiNilNilEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_END_RANGE, testStdin, STDIN_DASH);
        assertEquals(MULTI_END + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, multi lines, not given, single number
    @Test
    public void cutFromStdinAndFiles_charEmptyMultiNilSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_SINGLE, testStdin, STDIN_DASH, fileEdgePath.toString());
        assertEquals(STRING_NEWLINE + EDGE_CHAR_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, multi lines, not given, single number
    @Test
    public void cutFromStdinAndFiles_byteEmptyMultiNilSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_SINGLE, testStdin, fileEdgePath.toString(), STDIN_DASH);
        assertEquals(EDGE_BYTE_SINGLE + STRING_NEWLINE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, not given, empty, start range
    @Test
    public void cutFromStdinAndFiles_charEmptyNilEmptyStartRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_START_RANGE, testStdin, STDIN_DASH, fileEmptyPath.toString());
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, not given, empty, start range
    @Test
    public void cutFromStdinAndFiles_byteEmptyNilEmptyStartRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_START_RANGE, testStdin, fileEmptyPath.toString(), STDIN_DASH);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, empty, multi lines, end range
    @Test
    public void cutFromStdinAndFiles_charEmptyEmptyMultiEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_END_RANGE, testStdin, fileEmptyPath.toString(), STDIN_DASH, file2Path.toString());
        assertEquals(STRING_NEWLINE + MULTI_2_END + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, empty, multi lines, end range
    @Test
    public void cutFromStdinAndFiles_byteEmptyEmptyMultiEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_END_RANGE, testStdin, file2Path.toString(), STDIN_DASH, fileEmptyPath.toString());
        assertEquals(MULTI_2_END + STRING_NEWLINE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, multi line, not given, range
    @Test
    public void cutFromStdinAndFiles_charEmptyMultiNilRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_RANGE, testStdin, STDIN_DASH, file2Path.toString());
        assertEquals(STRING_NEWLINE + MULTI_2_RANGE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, multi line, not given, range
    @Test
    public void cutFromStdinAndFiles_byteEmptyMultiNilRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_RANGE, testStdin, file2Path.toString(), STDIN_DASH);
        assertEquals(MULTI_2_RANGE + STRING_NEWLINE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, not given, empty, list
    @Test
    public void cutFromStdinAndFiles_charEmptyNilEmptyList_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_LIST, testStdin, STDIN_DASH, fileEmptyPath.toString());
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, not given, empty, list
    @Test
    public void cutFromStdinAndFiles_byteEmptyNilEmptyList_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_LIST, testStdin, fileEmptyPath.toString(), STDIN_DASH);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, empty, multi lines, mix
    @Test
    public void cutFromStdinAndFiles_charEmptyEmptyMultiMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_MIX, testStdin, STDIN_DASH, fileEmptyPath.toString(), file1Path.toString());
        assertEquals(STRING_NEWLINE + MULTI_MIX + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, empty, multi lines, mix
    @Test
    public void cutFromStdinAndFiles_byteEmptyEmptyMultiMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_MIX, testStdin, fileEmptyPath.toString(), file1Path.toString(), STDIN_DASH);
        assertEquals(MULTI_MIX + STRING_NEWLINE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi lines, not given, multi lines, range
    @Test
    public void cutFromStdinAndFiles_charMultiNilMultiRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_RANGE, testStdin, STDIN_DASH, fileEdgePath.toString());
        assertEquals(MULTI_RANGE + STRING_NEWLINE + EDGE_CHAR_RANGE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi lines, not given, multi lines, range
    @Test
    public void cutFromStdinAndFiles_byteMultiNilMultiRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_RANGE, testStdin, fileEdgePath.toString(), STDIN_DASH);
        assertEquals(EDGE_BYTE_RANGE + STRING_NEWLINE + MULTI_RANGE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi lines, empty, not given, list
    @Test
    public void cutFromStdinAndFiles_charMultiEmptyNilList_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_LIST, testStdin, STDIN_DASH, fileEmptyPath.toString());
        assertEquals(EDGE_CHAR_LIST + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi lines, empty, not given, list
    @Test
    public void cutFromStdinAndFiles_byteMultiEmptyNilList_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_LIST, testStdin, fileEmptyPath.toString(), STDIN_DASH);
        assertEquals(EDGE_BYTE_LIST + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi lines, multi lines, empty, mix
    @Test
    public void cutFromStdinAndFiles_charMultiMultiEmptyMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_MIX, testStdin, file1Path.toString(), STDIN_DASH, fileEmptyPath.toString());
        assertEquals(MULTI_MIX + STRING_NEWLINE + MULTI_MIX + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi lines, multi lines, empty, mix
    @Test
    public void cutFromStdinAndFiles_byteMultiMultiEmptyMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_MIX, testStdin, file1Path.toString(), fileEmptyPath.toString(), STDIN_DASH);
        assertEquals(MULTI_MIX + STRING_NEWLINE + MULTI_MIX + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi lines, not given, multi lines, single number
    @Test
    public void cutFromStdinAndFiles_charMultiNilMultiSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_SINGLE, testStdin, STDIN_DASH, fileEdgePath.toString());
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + EDGE_CHAR_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi lines, not given, multi lines, single number
    @Test
    public void cutFromStdinAndFiles_byteMultiNilMultiSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_SINGLE, testStdin, fileEdgePath.toString(), STDIN_DASH);
        assertEquals(EDGE_BYTE_SINGLE + STRING_NEWLINE + MULTI_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi lines, empty, not given, start range
    @Test
    public void cutFromStdinAndFiles_charMultiEmptyNilStartRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_START_RANGE, testStdin, STDIN_DASH, fileEmptyPath.toString());
        assertEquals(MULTI_START + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi lines, empty, not given, start range
    @Test
    public void cutFromStdinAndFiles_byteMultiEmptyNilStartRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_START_RANGE, testStdin, fileEmptyPath.toString(), STDIN_DASH);
        assertEquals(MULTI_START + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi lines, multi lines, empty, end range
    @Test
    public void cutFromStdinAndFiles_charMultiMultiEmptyEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_END_RANGE, testStdin, STDIN_DASH, fileEdgePath.toString(), fileEmptyPath.toString());
        assertEquals(MULTI_END + STRING_NEWLINE + EDGE_CHAR_END + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi lines, multi lines, empty, end range
    @Test
    public void cutFromStdinAndFiles_byteMultiMultiEmptyEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_END_RANGE, testStdin, fileEdgePath.toString(), STDIN_DASH, fileEmptyPath.toString());
        assertEquals(EDGE_BYTE_END + STRING_NEWLINE + MULTI_END + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, null, multi lines, multi lines, list
    @Test
    public void cutFromStdinAndFiles_charNullMultiMultiList_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> cutApp.cutFromStdinAndFiles(true, false, LIST_LIST, null, STDIN_DASH, fileEdgePath.toString(), file1Path.toString()));
        assertEquals(ERR_NULL_STREAMS, exception.getMessage());
    }

    // byte, null, multi lines, multi lines, list
    @Test
    public void cutFromStdinAndFiles_byteNullMultiMultiList_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> cutApp.cutFromStdinAndFiles(false, true, LIST_LIST, null, fileEdgePath.toString(), STDIN_DASH, file1Path.toString()));
        assertEquals(ERR_NULL_STREAMS, exception.getMessage());
    }

    // character, multi lines, null, list
    @Test
    public void cutFromStdinAndFiles_charMultiNullList_throwsException() throws IOException {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> cutApp.cutFromStdinAndFiles(true, false, LIST_LIST, testStdin, null));
        assertEquals(ERR_NULL_ARGS, exception.getMessage());
        testStdin.close();
    }

    // byte, multi lines, null, list
    @Test
    public void cutFromStdinAndFiles_byteMultiNullList_throwsException() throws IOException {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> cutApp.cutFromStdinAndFiles(false, true, LIST_LIST, testStdin, null));
        assertEquals(ERR_NULL_ARGS, exception.getMessage());
        testStdin.close();
    }

    // character, multi lines, multi lines, multi lines, null
    @Test
    public void cutFromStdinAndFiles_charMultiMultiMultiNull_throwsException() throws IOException {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> cutApp.cutFromStdinAndFiles(true, false, null, testStdin, STDIN_DASH, file1Path.toString(), file2Path.toString()));
        assertEquals(INVALID_LIST, exception.getMessage());
        testStdin.close();
    }

    // byte, multi lines, multi lines, multi lines, null
    @Test
    public void cutFromStdinAndFiles_byteMultiMultiMultiNull_throwsException() throws IOException {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> cutApp.cutFromStdinAndFiles(false, true, null, testStdin, STDIN_DASH, file1Path.toString(), file2Path.toString()));
        assertEquals(INVALID_LIST, exception.getMessage());
        testStdin.close();
    }

    // character and byte, multi lines, multi lines, multi lines, list
    @Test
    public void cutFromStdinAndFiles_charByteMultiMultiMultiList_throwsException() throws IOException {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> cutApp.cutFromStdinAndFiles(true, true, LIST_LIST, testStdin, STDIN_DASH, file1Path.toString(), file2Path.toString()));
        assertEquals(INVALID_FLAG, exception.getMessage());
        testStdin.close();
    }

    // no options, multi lines, multi lines, multi lines, list
    @Test
    public void cutFromStdinAndFiles_noOpMultiMultiMultiList_throwsException() throws IOException {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> cutApp.cutFromStdinAndFiles(false, false, LIST_LIST, testStdin, STDIN_DASH, file1Path.toString(), file2Path.toString()));
        assertEquals(INVALID_FLAG, exception.getMessage());
        testStdin.close();
    }

    // character, multi lines, multi lines, directory, mix
    @Test
    public void cutFromStdinAndFiles_charMultiMultiDirMix_cutLinesPrintError() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_MIX, testStdin, STDIN_DASH, fileEdgePath.toString(), folderPath.resolve(NEST_DIR).toString());
        assertEquals(MULTI_MIX + STRING_NEWLINE + EDGE_CHAR_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CUT_EXCEP + folderPath.resolve(NEST_DIR) + ": " + ERR_IS_DIR + STRING_NEWLINE, testErrorStream.toString());
        testStdin.close();
    }

    // byte, multi lines, multi lines, directory, mix
    @Test
    public void cutFromStdinAndFiles_byteMultiMultiDirMix_cutLinesPrintError() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_MIX, testStdin, fileEdgePath.toString(), STDIN_DASH, fileEmptyPath.toString(), folderPath.resolve(NEST_DIR).toString());
        assertEquals(EDGE_BYTE_MIX + STRING_NEWLINE + MULTI_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CUT_EXCEP + folderPath.resolve(NEST_DIR) + ": " + ERR_IS_DIR + STRING_NEWLINE, testErrorStream.toString());
        testStdin.close();
    }

    // character, multi lines, multi lines, non-exist file, mix
    @Test
    public void cutFromStdinAndFiles_charMultiMultiNonExistFileMix_cutLinesPrintError() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_MIX, testStdin, STDIN_DASH, fileEdgePath.toString(), folderPath.resolve(FILE_NE).toString());
        assertEquals(MULTI_MIX + STRING_NEWLINE + EDGE_CHAR_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CUT_EXCEP + folderPath.resolve(FILE_NE) + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE, testErrorStream.toString());
        testStdin.close();
    }

    // byte, multi lines, multi lines, non-exist file, mix
    @Test
    public void cutFromStdinAndFiles_byteMultiMultiNonExistFileMix_cutLinesPrintError() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_MIX, testStdin, fileEdgePath.toString(), STDIN_DASH, fileEmptyPath.toString(), folderPath.resolve(FILE_NE).toString());
        assertEquals(EDGE_BYTE_MIX + STRING_NEWLINE + MULTI_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CUT_EXCEP + folderPath.resolve(FILE_NE) + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE, testErrorStream.toString());
        testStdin.close();
    }

    // character, multi lines, multi lines, non-exist directory, mix
    @Test
    public void cutFromStdinAndFiles_charMultiMultiNonExistDirMix_cutLinesPrintError() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(true, false, LIST_MIX, testStdin, STDIN_DASH, fileEdgePath.toString(), folderPath.resolve(NE_DIR).resolve(FILE_1).toString());
        assertEquals(MULTI_MIX + STRING_NEWLINE + EDGE_CHAR_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CUT_EXCEP + folderPath.resolve(NE_DIR).resolve(FILE_1) + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE, testErrorStream.toString());
        testStdin.close();
    }

    // byte, multi lines, multi lines, non-exist directory, mix
    @Test
    public void cutFromStdinAndFiles_byteMultiMultiNonExistDirMix_cutLinesPrintError() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        cutApp.cutFromStdinAndFiles(false, true, LIST_MIX, testStdin, fileEdgePath.toString(), STDIN_DASH, fileEmptyPath.toString(), folderPath.resolve(NE_DIR).resolve(FILE_1).toString());
        assertEquals(EDGE_BYTE_MIX + STRING_NEWLINE + MULTI_MIX + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CUT_EXCEP + folderPath.resolve(NE_DIR).resolve(FILE_1) + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE, testErrorStream.toString());
        testStdin.close();
    }

    // character, empty, single file, single number
    @Test
    public void run_charEmptySingleSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_SINGLE_STR, file1Path.toString(), STDIN_DASH};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(file1Path.toString(), STDIN_DASH));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_SINGLE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, single file, single number
    @Test
    public void run_byteEmptySingleSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_SINGLE_STR, file1Path.toString(), STDIN_DASH};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(file1Path.toString(), STDIN_DASH));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_SINGLE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, multi files, start range
    @Test
    public void run_charEmptyMultiStartRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_START_STR, fileEdgePath.toString(), STDIN_DASH, file2Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(fileEdgePath.toString(), STDIN_DASH, file2Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_START_RANGE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(EDGE_CHAR_START + STRING_NEWLINE + STRING_NEWLINE + MULTI_2_START + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, multi files, start range
    @Test
    public void run_byteEmptyMultiStartRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_START_STR, fileEdgePath.toString(), STDIN_DASH, file2Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(fileEdgePath.toString(), STDIN_DASH, file2Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_START_RANGE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(EDGE_BYTE_START + STRING_NEWLINE + STRING_NEWLINE + MULTI_2_START + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, not given, end range
    @Test
    public void run_charEmptyNilEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_END_STR};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList());
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_END_RANGE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, not given, end range
    @Test
    public void run_byteEmptyNilEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_END_STR};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList());
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_END_RANGE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, single file, range
    @Test
    public void run_charEmptySingleRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_RANGE_STR, STDIN_DASH, fileEdgePath.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(STDIN_DASH, fileEdgePath.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_RANGE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(STRING_NEWLINE + EDGE_CHAR_RANGE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, single file, range
    @Test
    public void run_byteEmptySingleRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_RANGE_STR, STDIN_DASH, fileEdgePath.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(STDIN_DASH, fileEdgePath.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_RANGE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(STRING_NEWLINE + EDGE_BYTE_RANGE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, multi files, list
    @Test
    public void run_charEmptyMultiList_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_LIST_STR, fileEdgePath.toString(), file2Path.toString(), STDIN_DASH};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(fileEdgePath.toString(), file2Path.toString(), STDIN_DASH));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_LIST);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(EDGE_CHAR_LIST + STRING_NEWLINE + MULTI_2_LIST + STRING_NEWLINE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, multi files, list
    @Test
    public void run_byteEmptyMultiList_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_LIST_STR, fileEdgePath.toString(), file2Path.toString(), STDIN_DASH};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(fileEdgePath.toString(), file2Path.toString(), STDIN_DASH));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_LIST);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(EDGE_BYTE_LIST + STRING_NEWLINE + MULTI_2_LIST + STRING_NEWLINE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, not given, mix
    @Test
    public void run_charEmptyNilMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_MIX_STR};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList());
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_MIX);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, not given, mix
    @Test
    public void run_byteEmptyNilMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_MIX_STR};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList());
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_MIX);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi line, multi files, end range
    @Test
    public void run_charMultiMultiEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        String[] args = {CHAR_FLAG, LIST_END_STR, file1Path.toString(), file2Path.toString(), STDIN_DASH};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(file1Path.toString(), file2Path.toString(), STDIN_DASH));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_END_RANGE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_END + STRING_NEWLINE + MULTI_2_END + STRING_NEWLINE + EDGE_CHAR_END + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi line, multi files, end range
    @Test
    public void run_byteMultiMultiEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        String[] args = {BYTE_FLAG, LIST_END_STR, file1Path.toString(), file2Path.toString(), STDIN_DASH};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(file1Path.toString(), file2Path.toString(), STDIN_DASH));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_END_RANGE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_END + STRING_NEWLINE + MULTI_2_END + STRING_NEWLINE + EDGE_BYTE_END + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi line, not given, range
    @Test
    public void run_charMultiNilRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String[] args = {CHAR_FLAG, LIST_RANGE_STR, STDIN_DASH};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(STDIN_DASH));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_RANGE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_RANGE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi line, not given, range
    @Test
    public void run_byteMultiNilRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String[] args = {BYTE_FLAG, LIST_RANGE_STR, STDIN_DASH};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(STDIN_DASH));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_RANGE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_RANGE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi line, single file, list
    @Test
    public void run_charMultiSingleList_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String[] args = {CHAR_FLAG, LIST_LIST_STR, STDIN_DASH, file1Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(STDIN_DASH, file1Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_LIST);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_LIST + STRING_NEWLINE + MULTI_LIST + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi line, single file, list
    @Test
    public void run_byteMultiSingleList_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String[] args = {BYTE_FLAG, LIST_LIST_STR, STDIN_DASH, file1Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(STDIN_DASH, file1Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_LIST);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_LIST + STRING_NEWLINE + MULTI_LIST + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi line, multi files, mix
    @Test
    public void run_charMultiMultiMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String[] args = {CHAR_FLAG, LIST_MIX_STR, fileEdgePath.toString(), STDIN_DASH, file1Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(fileEdgePath.toString(), STDIN_DASH, file1Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_MIX);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(EDGE_CHAR_MIX + STRING_NEWLINE + MULTI_2_MIX + STRING_NEWLINE + MULTI_MIX + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi line, multi files, mix
    @Test
    public void run_byteMultiMultiMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String[] args = {BYTE_FLAG, LIST_MIX_STR, fileEdgePath.toString(), STDIN_DASH, file1Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(fileEdgePath.toString(), STDIN_DASH, file1Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_MIX);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(EDGE_BYTE_MIX + STRING_NEWLINE + MULTI_2_MIX + STRING_NEWLINE + MULTI_MIX + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi line, not given, single number
    @Test
    public void run_charMultiNilSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String[] args = {CHAR_FLAG, LIST_SINGLE_STR};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList());
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_SINGLE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi line, not given, single number
    @Test
    public void run_byteMultiNilSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String[] args = {BYTE_FLAG, LIST_SINGLE_STR};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList());
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_SINGLE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi line, single file, start range
    @Test
    public void run_charMultiSingleStartRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {CHAR_FLAG, LIST_START_STR, STDIN_DASH, fileEdgePath.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(STDIN_DASH, fileEdgePath.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_START_RANGE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_START + STRING_NEWLINE + EDGE_CHAR_START + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi line, not given, single number
    @Test
    public void run_byteMultiSingleStartRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {BYTE_FLAG, LIST_START_STR, STDIN_DASH, fileEdgePath.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(STDIN_DASH, fileEdgePath.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_START_RANGE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_START + STRING_NEWLINE + EDGE_BYTE_START + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, not given, single file, mix
    @Test
    public void run_charNilSingleMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_MIX_STR, fileEdgePath.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(fileEdgePath.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_MIX);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(EDGE_CHAR_MIX + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, not given, single file, mix
    @Test
    public void run_byteNilSingleMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_MIX_STR, fileEdgePath.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(fileEdgePath.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_MIX);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(EDGE_BYTE_MIX + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, not given, multiple files, single number
    @Test
    public void run_charNilMultiSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_SINGLE_STR, file1Path.toString(), file2Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(file1Path.toString(), file2Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_SINGLE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_2_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, not given, multiple files, single number
    @Test
    public void run_byteNilMultiSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_SINGLE_STR, file1Path.toString(), file2Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(file1Path.toString(), file2Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_SINGLE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_2_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, not given, single file, end range
    @Test
    public void run_charNilSingleEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_END_STR, file2Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(file2Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_END_RANGE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_END + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, not given, single file, end range
    @Test
    public void run_byteNilSingleEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_END_STR, file2Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(file2Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_END_RANGE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_END + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, not given, multiple files, range
    @Test
    public void run_charNilMultiRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_RANGE_STR, file2Path.toString(), file1Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(file2Path.toString(), file1Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_RANGE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_RANGE + STRING_NEWLINE + MULTI_RANGE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, not given, multiple files, range
    @Test
    public void run_byteNilMultiRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_RANGE_STR, file2Path.toString(), file1Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(file2Path.toString(), file1Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_RANGE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_RANGE + STRING_NEWLINE + MULTI_RANGE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, null, single file, single number
    @Test
    public void run_charNullSingleSingle_throwsCutException() throws Exception {
        String[] args = {CHAR_FLAG, LIST_SINGLE_STR, STDIN_DASH, file2Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(STDIN_DASH, file2Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_SINGLE);

        Exception exception = assertThrows(CutException.class, () -> cutApp.run(args, null, testOutputStream));
        assertEquals(CUT_EXCEP + ERR_NULL_STREAMS, exception.getMessage());
    }

    // byte, null, single file, single number
    @Test
    public void run_byteNullSingleSingle_throwsCutException() throws Exception {
        String[] args = {BYTE_FLAG, LIST_SINGLE_STR, STDIN_DASH, file2Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(STDIN_DASH, file2Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_SINGLE);

        Exception exception = assertThrows(CutException.class, () -> cutApp.run(args, null, testOutputStream));
        assertEquals(CUT_EXCEP + ERR_NULL_STREAMS, exception.getMessage());
    }

    // character, multi lines, single file, null
    @Test
    public void run_charMultiSingleNull_throwsCutException() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {CHAR_FLAG, STDIN_DASH, file1Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(STDIN_DASH, file1Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(null);

        Exception exception = assertThrows(CutException.class, () -> cutApp.run(args, testStdin, testOutputStream));
        assertEquals(CUT_EXCEP + INVALID_LIST, exception.getMessage());
        testStdin.close();
    }

    // byte, multi lines, single file, null
    @Test
    public void run_byteMultiSingleNull_throwsCutException() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {BYTE_FLAG, STDIN_DASH, file1Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(STDIN_DASH, file1Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(null);

        Exception exception = assertThrows(CutException.class, () -> cutApp.run(args, testStdin, testOutputStream));
        assertEquals(CUT_EXCEP + INVALID_LIST, exception.getMessage());
        testStdin.close();
    }

    // character, multi lines, single file, single number, null stdout
    @Test
    public void run_charMultiSingleSingleNull_throwsCutException() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {CHAR_FLAG, LIST_SINGLE_STR, STDIN_DASH, file1Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(STDIN_DASH, file1Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_SINGLE);

        Exception exception = assertThrows(CutException.class, () -> cutApp.run(args, testStdin, null));
        assertEquals(CUT_EXCEP + ERR_NULL_STREAMS, exception.getMessage());
        testStdin.close();
    }

    // byte, multi lines, single file, single number, null stdout
    @Test
    public void run_byteMultiSingleSingleNull_throwsCutException() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {BYTE_FLAG, LIST_SINGLE_STR, STDIN_DASH, file1Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(STDIN_DASH, file1Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_SINGLE);

        Exception exception = assertThrows(CutException.class, () -> cutApp.run(args, testStdin, null));
        assertEquals(CUT_EXCEP + ERR_NULL_STREAMS, exception.getMessage());
        testStdin.close();
    }

    // character and byte, multi lines, single file, single number
    @Test
    public void run_charByteMultiSingleSingle_throwsCutException() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {CHAR_FLAG, BYTE_FLAG, LIST_SINGLE_STR, STDIN_DASH, file1Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(STDIN_DASH, file1Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_SINGLE);

        Exception exception = assertThrows(CutException.class, () -> cutApp.run(args, testStdin, testOutputStream));
        assertEquals(CUT_EXCEP + INVALID_FLAG, exception.getMessage());
        testStdin.close();
    }

    // no options, multi lines, single file, single number
    @Test
    public void run_noOpMultiSingleSingle_throwsCutException() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {LIST_SINGLE_STR, STDIN_DASH, file1Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(STDIN_DASH, file1Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_SINGLE);

        Exception exception = assertThrows(CutException.class, () -> cutApp.run(args, testStdin, testOutputStream));
        assertEquals(CUT_EXCEP + INVALID_FLAG, exception.getMessage());
        testStdin.close();
    }

    // character, multi lines, directory, single number
    @Test
    public void run_charMultiDirSingle_cutLinesPrintsError() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {CHAR_FLAG, LIST_SINGLE_STR, folderPath.resolve(NEST_DIR).toString(), STDIN_DASH, folderPath.resolve(NEST_DIR).toString(), file1Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(folderPath.resolve(NEST_DIR).toString(), STDIN_DASH, folderPath.resolve(NEST_DIR).toString(), file1Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_SINGLE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CUT_EXCEP + folderPath.resolve(NEST_DIR) + ": " + ERR_IS_DIR + STRING_NEWLINE + CUT_EXCEP + folderPath.resolve(NEST_DIR) + ": " + ERR_IS_DIR + STRING_NEWLINE, testErrorStream.toString());
        testStdin.close();
    }

    // byte, multi lines, directory, single number
    @Test
    public void run_byteMultiDirSingle_cutLinesPrintsError() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {BYTE_FLAG, LIST_SINGLE_STR, folderPath.resolve(NEST_DIR).toString(), STDIN_DASH, folderPath.resolve(NEST_DIR).toString(), file1Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(folderPath.resolve(NEST_DIR).toString(), STDIN_DASH, folderPath.resolve(NEST_DIR).toString(), file1Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_SINGLE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CUT_EXCEP + folderPath.resolve(NEST_DIR) + ": " + ERR_IS_DIR + STRING_NEWLINE + CUT_EXCEP + folderPath.resolve(NEST_DIR) + ": " + ERR_IS_DIR + STRING_NEWLINE, testErrorStream.toString());
        testStdin.close();
    }

    // character, multi lines, non-existent file, single number
    @Test
    public void run_charMultiNonExistFileSingle_cutLinesPrintsError() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {CHAR_FLAG, LIST_SINGLE_STR, folderPath.resolve(FILE_NE).toString(), STDIN_DASH, folderPath.resolve(FILE_NE).toString(), file1Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(folderPath.resolve(FILE_NE).toString(), STDIN_DASH, folderPath.resolve(FILE_NE).toString(), file1Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_SINGLE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CUT_EXCEP + folderPath.resolve(FILE_NE) + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE + CUT_EXCEP + folderPath.resolve(FILE_NE) + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE, testErrorStream.toString());
        testStdin.close();
    }

    // byte, multi lines, non-existent file, single number
    @Test
    public void run_byteMultiNonExistFileSingle_cutLinesPrintsError() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {BYTE_FLAG, LIST_SINGLE_STR, folderPath.resolve(FILE_NE).toString(), STDIN_DASH, folderPath.resolve(FILE_NE).toString(), file1Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(folderPath.resolve(FILE_NE).toString(), STDIN_DASH, folderPath.resolve(FILE_NE).toString(), file1Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_SINGLE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CUT_EXCEP + folderPath.resolve(FILE_NE) + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE + CUT_EXCEP + folderPath.resolve(FILE_NE) + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE, testErrorStream.toString());
        testStdin.close();
    }

    // character, multi lines, non-existent directory, single number
    @Test
    public void run_charMultiNonExistDirSingle_cutLinesPrintsError() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {CHAR_FLAG, LIST_SINGLE_STR, STDIN_DASH, folderPath.resolve(NE_DIR).resolve(FILE_1).toString(), STDIN_DASH, folderPath.resolve(NE_DIR).resolve(FILE_1).toString(), file1Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(STDIN_DASH, folderPath.resolve(NE_DIR).resolve(FILE_1).toString(), STDIN_DASH, folderPath.resolve(NE_DIR).resolve(FILE_1).toString(), file1Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(true);
        when(cutArg.isBytePosition()).thenReturn(false);
        when(cutArg.getNumList()).thenReturn(LIST_SINGLE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CUT_EXCEP + folderPath.resolve(NE_DIR).resolve(FILE_1) + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE + CUT_EXCEP + folderPath.resolve(NE_DIR).resolve(FILE_1) + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE, testErrorStream.toString());
        testStdin.close();
    }

    // byte, multi lines, non-existent directory, single number
    @Test
    public void run_byteMultiNonExistDirSingle_cutLinesPrintsError() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {BYTE_FLAG, LIST_SINGLE_STR, folderPath.resolve(NE_DIR).resolve(FILE_1).toString(), STDIN_DASH, folderPath.resolve(NE_DIR).resolve(FILE_1).toString(), file1Path.toString()};

        doNothing().when(cutArg).parse(args);
        when(cutArg.getFiles()).thenReturn(Arrays.asList(folderPath.resolve(NE_DIR).resolve(FILE_1).toString(), STDIN_DASH, folderPath.resolve(NE_DIR).resolve(FILE_1).toString(), file1Path.toString()));
        when(cutArg.isCharacterPosition()).thenReturn(false);
        when(cutArg.isBytePosition()).thenReturn(true);
        when(cutArg.getNumList()).thenReturn(LIST_SINGLE);

        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CUT_EXCEP + folderPath.resolve(NE_DIR).resolve(FILE_1) + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE + CUT_EXCEP + folderPath.resolve(NE_DIR).resolve(FILE_1) + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE, testErrorStream.toString());
        testStdin.close();
    }
}