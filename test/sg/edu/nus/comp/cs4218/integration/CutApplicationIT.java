package sg.edu.nus.comp.cs4218.integration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.CutException;
import sg.edu.nus.comp.cs4218.impl.app.CutApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_FLAG;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_LIST;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class CutApplicationIT {
    private final static String STDIN_DASH = "-";
    private final static String CHAR_FLAG = "-c";
    private final static String BYTE_FLAG = "-b";
    private final static String CHAR_BYTE_FLAG = "-cb";
    private final static String FILE_1 = "1.txt";
    private final static String FILE_2 = "2.txt";
    private final static String FILE_SINGLE = "single.txt";
    private final static String FILE_EMPTY = "empty.txt";
    private final static String FILE_EDGE = "edge.txt";
    private final static String FILE_NE = "nef";
    private final static String NE_DIR = "ned";
    private final static String NEST_DIR = "nest";
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
    private static ByteArrayOutputStream testOutputStream;
    private static ByteArrayOutputStream testErrorStream;
    private static Path file1Path;
    private static Path file2Path;
    private static Path fileEmptyPath;
    private static Path fileEdgePath;
    private static Path file1DirPath;

    @BeforeAll
    public static void setUpAll() throws IOException {
        file1Path = folderPath.resolve(FILE_1);
        file2Path = folderPath.resolve(FILE_2);
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
        // ./edge.txt
        Files.write(fileEdgePath, EDGE_CONT.getBytes());
        // ./nest/1.txt
        Files.write(file1DirPath, MULTI_LINE_CONT.getBytes());
    }

    @BeforeEach
    public void setUpEach() {
        cutApp = new CutApplication();

        testOutputStream = new ByteArrayOutputStream();
        testErrorStream = new ByteArrayOutputStream();
        cutApp.setStdout(testOutputStream);
        System.setOut(new PrintStream(testErrorStream));
    }

    // character, empty, single file, single number
    @Test
    public void run_charEmptySingleSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_SINGLE_STR, file1Path.toString(), STDIN_DASH};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, single file, single number
    @Test
    public void run_byteEmptySingleSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_SINGLE_STR, file1Path.toString(), STDIN_DASH};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, multi files, start range
    @Test
    public void run_charEmptyMultiStartRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_START_STR, fileEdgePath.toString(), STDIN_DASH, file2Path.toString()};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(EDGE_CHAR_START + STRING_NEWLINE + STRING_NEWLINE + MULTI_2_START + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, multi files, start range
    @Test
    public void run_byteEmptyMultiStartRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_START_STR, fileEdgePath.toString(), STDIN_DASH, file2Path.toString()};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(EDGE_BYTE_START + STRING_NEWLINE + STRING_NEWLINE + MULTI_2_START + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, not given, end range
    @Test
    public void run_charEmptyNilEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_END_STR};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, not given, end range
    @Test
    public void run_byteEmptyNilEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_END_STR};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, single file, range
    @Test
    public void run_charEmptySingleRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_RANGE_STR, STDIN_DASH, fileEdgePath.toString()};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(STRING_NEWLINE + EDGE_CHAR_RANGE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, single file, range
    @Test
    public void run_byteEmptySingleRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_RANGE_STR, STDIN_DASH, fileEdgePath.toString()};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(STRING_NEWLINE + EDGE_BYTE_RANGE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, multi files, list
    @Test
    public void run_charEmptyMultiList_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_LIST_STR, fileEdgePath.toString(), file2Path.toString(), STDIN_DASH};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(EDGE_CHAR_LIST + STRING_NEWLINE + MULTI_2_LIST + STRING_NEWLINE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, multi files, list
    @Test
    public void run_byteEmptyMultiList_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_LIST_STR, fileEdgePath.toString(), file2Path.toString(), STDIN_DASH};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(EDGE_BYTE_LIST + STRING_NEWLINE + MULTI_2_LIST + STRING_NEWLINE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, empty, not given, mix
    @Test
    public void run_charEmptyNilMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_MIX_STR};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, empty, not given, mix
    @Test
    public void run_byteEmptyNilMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_MIX_STR};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi line, multi files, end range
    @Test
    public void run_charMultiMultiEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        String[] args = {CHAR_FLAG, LIST_END_STR, file1Path.toString(), file2Path.toString(), STDIN_DASH};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_END + STRING_NEWLINE + MULTI_2_END + STRING_NEWLINE + EDGE_CHAR_END + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi line, multi files, end range
    @Test
    public void run_byteMultiMultiEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(EDGE_CONT.getBytes());
        String[] args = {BYTE_FLAG, LIST_END_STR, file1Path.toString(), file2Path.toString(), STDIN_DASH};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_END + STRING_NEWLINE + MULTI_2_END + STRING_NEWLINE + EDGE_BYTE_END + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi line, not given, range
    @Test
    public void run_charMultiNilRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String[] args = {CHAR_FLAG, LIST_RANGE_STR, STDIN_DASH};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_RANGE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi line, not given, range
    @Test
    public void run_byteMultiNilRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String[] args = {BYTE_FLAG, LIST_RANGE_STR, STDIN_DASH};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_RANGE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi line, single file, list
    @Test
    public void run_charMultiSingleList_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String[] args = {CHAR_FLAG, LIST_LIST_STR, STDIN_DASH, file1Path.toString()};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_LIST + STRING_NEWLINE + MULTI_LIST + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi line, single file, list
    @Test
    public void run_byteMultiSingleList_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String[] args = {BYTE_FLAG, LIST_LIST_STR, STDIN_DASH, file1Path.toString()};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_LIST + STRING_NEWLINE + MULTI_LIST + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi line, multi files, mix
    @Test
    public void run_charMultiMultiMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String[] args = {CHAR_FLAG, LIST_MIX_STR, fileEdgePath.toString(), STDIN_DASH, file1Path.toString()};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(EDGE_CHAR_MIX + STRING_NEWLINE + MULTI_2_MIX + STRING_NEWLINE + MULTI_MIX + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi line, multi files, mix
    @Test
    public void run_byteMultiMultiMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String[] args = {BYTE_FLAG, LIST_MIX_STR, fileEdgePath.toString(), STDIN_DASH, file1Path.toString()};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(EDGE_BYTE_MIX + STRING_NEWLINE + MULTI_2_MIX + STRING_NEWLINE + MULTI_MIX + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi line, not given, single number
    @Test
    public void run_charMultiNilSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String[] args = {CHAR_FLAG, LIST_SINGLE_STR};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi line, not given, single number
    @Test
    public void run_byteMultiNilSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT_2.getBytes());
        String[] args = {BYTE_FLAG, LIST_SINGLE_STR};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, multi line, single file, start range
    @Test
    public void run_charMultiSingleStartRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {CHAR_FLAG, LIST_START_STR, STDIN_DASH, fileEdgePath.toString()};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_START + STRING_NEWLINE + EDGE_CHAR_START + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, multi line, not given, single number
    @Test
    public void run_byteMultiSingleStartRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {BYTE_FLAG, LIST_START_STR, STDIN_DASH, fileEdgePath.toString()};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_START + STRING_NEWLINE + EDGE_BYTE_START + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, not given, single file, mix
    @Test
    public void run_charNilSingleMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_MIX_STR, fileEdgePath.toString()};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(EDGE_CHAR_MIX + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, not given, single file, mix
    @Test
    public void run_byteNilSingleMix_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_MIX_STR, fileEdgePath.toString()};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(EDGE_BYTE_MIX + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, not given, multiple files, single number
    @Test
    public void run_charNilMultiSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_SINGLE_STR, file1Path.toString(), file2Path.toString()};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_2_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, not given, multiple files, single number
    @Test
    public void run_byteNilMultiSingle_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_SINGLE_STR, file1Path.toString(), file2Path.toString()};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_2_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, not given, single file, end range
    @Test
    public void run_charNilSingleEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_END_STR, file2Path.toString()};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_END + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, not given, single file, end range
    @Test
    public void run_byteNilSingleEndRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_END_STR, file2Path.toString()};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_END + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, not given, multiple files, range
    @Test
    public void run_charNilMultiRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {CHAR_FLAG, LIST_RANGE_STR, file2Path.toString(), file1Path.toString()};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_RANGE + STRING_NEWLINE + MULTI_RANGE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // byte, not given, multiple files, range
    @Test
    public void run_byteNilMultiRange_cutLines() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String[] args = {BYTE_FLAG, LIST_RANGE_STR, file2Path.toString(), file1Path.toString()};
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_2_RANGE + STRING_NEWLINE + MULTI_RANGE + STRING_NEWLINE, testOutputStream.toString());
        testStdin.close();
    }

    // character, null, single file, single number
    @Test
    public void run_charNullSingleSingle_throwsCutException() throws Exception {
        String[] args = {CHAR_FLAG, LIST_SINGLE_STR, STDIN_DASH, file2Path.toString()};
        Exception exception = assertThrows(CutException.class, () -> cutApp.run(args, null, testOutputStream));
        assertEquals(CUT_EXCEP + ERR_NULL_STREAMS, exception.getMessage());
    }

    // byte, null, single file, single number
    @Test
    public void run_byteNullSingleSingle_throwsCutException() throws Exception {
        String[] args = {BYTE_FLAG, LIST_SINGLE_STR, STDIN_DASH, file2Path.toString()};
        Exception exception = assertThrows(CutException.class, () -> cutApp.run(args, null, testOutputStream));
        assertEquals(CUT_EXCEP + ERR_NULL_STREAMS, exception.getMessage());
    }

    // character, multi lines, single file, null
    @Test
    public void run_charMultiSingleNull_throwsCutException() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {CHAR_FLAG, STDIN_DASH, file1Path.toString()};
        Exception exception = assertThrows(CutException.class, () -> cutApp.run(args, testStdin, testOutputStream));
        assertEquals(CUT_EXCEP + INVALID_LIST, exception.getMessage());
        testStdin.close();
    }

    // byte, multi lines, single file, null
    @Test
    public void run_byteMultiSingleNull_throwsCutException() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {BYTE_FLAG, STDIN_DASH, file1Path.toString()};
        Exception exception = assertThrows(CutException.class, () -> cutApp.run(args, testStdin, testOutputStream));
        assertEquals(CUT_EXCEP + INVALID_LIST, exception.getMessage());
        testStdin.close();
    }

    // character, multi lines, single file, single number, null stdout
    @Test
    public void run_charMultiSingleSingleNull_throwsCutException() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {CHAR_FLAG, LIST_SINGLE_STR, STDIN_DASH, file1Path.toString()};
        Exception exception = assertThrows(CutException.class, () -> cutApp.run(args, testStdin, null));
        assertEquals(CUT_EXCEP + ERR_NULL_STREAMS, exception.getMessage());
        testStdin.close();
    }

    // byte, multi lines, single file, single number, null stdout
    @Test
    public void run_byteMultiSingleSingleNull_throwsCutException() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {BYTE_FLAG, LIST_SINGLE_STR, STDIN_DASH, file1Path.toString()};
        Exception exception = assertThrows(CutException.class, () -> cutApp.run(args, testStdin, null));
        assertEquals(CUT_EXCEP + ERR_NULL_STREAMS, exception.getMessage());
        testStdin.close();
    }

    // character and byte seperate, multi lines, single file, single number
    @Test
    public void run_charByteSepMultiSingleSingle_throwsCutException() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {CHAR_FLAG, BYTE_FLAG, LIST_SINGLE_STR, STDIN_DASH, file1Path.toString()};
        Exception exception = assertThrows(CutException.class, () -> cutApp.run(args, testStdin, testOutputStream));
        assertEquals(CUT_EXCEP + INVALID_LIST, exception.getMessage());
        testStdin.close();
    }

    // character and byte together, multi lines, single file, single number
    @Test
    public void run_charByteTogthMultiSingleSingle_throwsCutException() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {CHAR_BYTE_FLAG, LIST_SINGLE_STR, STDIN_DASH, file1Path.toString()};
        Exception exception = assertThrows(CutException.class, () -> cutApp.run(args, testStdin, testOutputStream));
        assertEquals(CUT_EXCEP + INVALID_FLAG, exception.getMessage());
        testStdin.close();
    }

    // no options, multi lines, single file, single number
    @Test
    public void run_noOpMultiSingleSingle_throwsCutException() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {LIST_SINGLE_STR, STDIN_DASH, file1Path.toString()};
        Exception exception = assertThrows(CutException.class, () -> cutApp.run(args, testStdin, testOutputStream));
        assertEquals(CUT_EXCEP + INVALID_FLAG, exception.getMessage());
        testStdin.close();
    }

    // character, multi lines, directory, single number
    @Test
    public void run_charMultiDirSingle_cutLinesPrintsError() throws Exception {
        ByteArrayInputStream testStdin = new ByteArrayInputStream(MULTI_LINE_CONT.getBytes());
        String[] args = {CHAR_FLAG, LIST_SINGLE_STR, folderPath.resolve(NEST_DIR).toString(), STDIN_DASH, folderPath.resolve(NEST_DIR).toString(), file1Path.toString()};
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
        cutApp.run(args, testStdin, testOutputStream);
        assertEquals(MULTI_SINGLE + STRING_NEWLINE + MULTI_SINGLE + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CUT_EXCEP + folderPath.resolve(NE_DIR).resolve(FILE_1) + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE + CUT_EXCEP + folderPath.resolve(NE_DIR).resolve(FILE_1) + ": " + ERR_FILE_NOT_FOUND + STRING_NEWLINE, testErrorStream.toString());
        testStdin.close();
    }
}
