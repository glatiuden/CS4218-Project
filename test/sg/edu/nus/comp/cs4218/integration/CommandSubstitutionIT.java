package sg.edu.nus.comp.cs4218.integration;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.*;
import sg.edu.nus.comp.cs4218.impl.app.CatApplication;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.testutils.FileUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.app.RmApplication.IS_DIRECTORY;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_FLAG;
import static sg.edu.nus.comp.cs4218.impl.app.args.CutArguments.INVALID_LIST;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.testutils.CommandSubUtils.*;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.deleteAll;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.getFileContent;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

public class CommandSubstitutionIT {
    private static final String SPACE = " ";
    private static final String OVERWRITTEN_TEXT = "Overwritten...";
    private static final String CUT_FLAG = "-b";
    private static final String CUT_POS = "1-13";
    private static final String FILE_SORT_1 = "sort1.txt";
    private static final String FILE_SORT_2 = "sort2.txt";
    private static final String MULTI_LINE_CONT = "\n54321\n\n  \n  spaced\na123!@#random\n!!!\nb\nAAA 123random\nBBB\n@@\n1123\n1\n55";
    private static final String MULTI_LINE_CONT2 = "\nhello\n55\nworld\nCS4218\nCS4218";
    private static final String MULTI_LINE_CONT3 = "B.txt\nA.txt\nA.txt";
    private static final String SORT_MULTI = "\n\n  \n  spaced\n!!!\n@@\n1\n1123\n54321\n55\nAAA 123random\nBBB\na123!@#random\nb";
    private static final String FILE_UNIQ1_NAME = "uniq1.txt";
    private static final String FILE_UNIQ2_NAME = "uniq2.txt";
    private static final String FILE_UNIQF_NAME = "uniqf.txt";
    private static final String UNIQ_CONT_1 = "CS4218\nCS4218\nHello\nWorld\nhello";
    private static final String UNIQ_CONT_2 = "I love CS4218\nI love CS4218\nLabs\nLabs";
    private static final String UNIQ_CONT_F = "A.txt\nA.txt\nA.txt";
    private static final String TOTAL = "total";
    @TempDir
    public static Path folderPath;
    private static Path file1Path, file2Path, filePath, teeFilePath;
    private static ByteArrayOutputStream outputCapture, testOutputStream;
    private static CallCommand callCommand;
    private static ApplicationRunner applicationRunner;
    private static ArgumentResolver argumentResolver;
    private static Path sortPath1;
    private static Path sortPath;
    private static Path uniqPathF;

    @BeforeAll
    public static void setUp() throws IOException {
        testOutputStream = new ByteArrayOutputStream();
        applicationRunner = new ApplicationRunner();
        argumentResolver = new ArgumentResolver();

        FileUtils.createNewDirs(folderPath);
        Environment.setCurrentDirectory(folderPath.toString());
        outputCapture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputCapture));

        // Uniq Test Files
        Path uniqPath1 = folderPath.resolve(FILE_UNIQ1_NAME);
        Path uniqPath2 = folderPath.resolve(FILE_UNIQ2_NAME);
        uniqPathF = folderPath.resolve(FILE_UNIQF_NAME);
        Files.writeString(uniqPath1, UNIQ_CONT_1);
        Files.writeString(uniqPath2, UNIQ_CONT_2);
        Files.writeString(uniqPathF, UNIQ_CONT_F);

        // File: A.txt, File Content: "I love CS4218"
        file1Path = folderPath.resolve(FILE_ONE_NAME);
        Files.writeString(file1Path, FILE_ONE_CONTENT);

        // File: B.txt, File Content: "CS4218"
        file2Path = folderPath.resolve(FILE_TWO_NAME);
        Files.writeString(file2Path, FILE_TWO_CONTENT);

        // File: file.txt, File Content: "A.txt"
        filePath = folderPath.resolve(FILE_FILE_NAME);
        Files.writeString(filePath, FILE_ONE_NAME);

        // ./nest
        Path nestDir = folderPath.resolve(NEST_DIR);
        Files.createDirectories(nestDir);

        // File: nest/file.txt, File Content: "I love CS4218"
        Path dirPath = nestDir.resolve(FILE_ONE_NAME);
        Files.writeString(dirPath, FILE_ONE_CONTENT);

        // File: sort.txt, File Content: Multiline text
        sortPath = folderPath.resolve(FILE_SORT_NAME);
        Files.writeString(sortPath, MULTI_LINE_CONT3);

        sortPath1 = folderPath.resolve(FILE_SORT_1);
        Path sortPath2 = folderPath.resolve(FILE_SORT_2);
        Files.writeString(sortPath1, MULTI_LINE_CONT);
        Files.writeString(sortPath2, MULTI_LINE_CONT2);

        teeFilePath = folderPath.resolve(FILE_TEE_NAME);
    }

    @AfterAll
    public static void tearDown() {
        deleteAll(folderPath.toFile());
        Environment.resetCurrentDirectory();
    }

    private String generateWcOutputString(int num1, int num2, int num3, String fileName) {
        return String.format(NUMBER_FORMAT, num1) + String.format(NUMBER_FORMAT, num2) + String.format(NUMBER_FORMAT, num3) + TAB + fileName + STRING_NEWLINE;
    }

    @AfterEach
    public void reset() throws IOException {
        deleteMvTestFiles();
        testOutputStream.reset();
        outputCapture.reset();
    }

    // Echo
    @Test
    void echoGrepGrep_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(TRIPLE_STRING, GREP_CMD, GREP_PATTERN, FILE_ONE_NAME);
        String subCmd2 = String.format(TRIPLE_STRING, GREP_CMD, GREP_PATTERN, FILE_TWO_NAME);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = FILE_ONE_CONTENT + SPACE + FILE_TWO_CONTENT + System.lineSeparator();
        assertEquals(expectedResult, testOutputStream.toString());
    }

    @Test
    void echoCutCut_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_ONE_NAME);
        String subCmd2 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_TWO_NAME);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = FILE_ONE_CONTENT + SPACE + FILE_TWO_CONTENT + System.lineSeparator();
        assertEquals(expectedResult, testOutputStream.toString());
    }

    @Test
    void echoSortSort_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_1);
        String subCmd2 = String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_2);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = "spaced !!! @@ 1 1123 54321 55 AAA 123random BBB a123!@#random b 55 CS4218 CS4218 hello world" + System.lineSeparator();
        assertEquals(expectedResult, testOutputStream.toString());
    }

    @Test
    void echoPastePaste_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, PASTE_CMD, folderPath.resolve(FILE_TWO_NAME));
        String subCmd2 = String.format(DOUBLE_STRING, PASTE_CMD, folderPath.resolve(FILE_ONE_NAME));
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        String expectedResult = FILE_TWO_CONTENT + SPACE + FILE_ONE_CONTENT + System.lineSeparator();
        assertEquals(expectedResult, testOutputStream.toString());
    }

    @Test
    void echoTeeTee_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_TWO_CONTENT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate
        String subCmd1 = String.format(SINGLE_STRING, TEE_CMD);
        String subCmd2 = String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_TWO_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    void echoUniqUniq_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQ1_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQ2_NAME);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals("CS4218 Hello World hello I love CS4218 Labs" + STRING_NEWLINE, testOutputStream.toString());
    }

    // Echo + 2 Other Apps
    @Test
    void echoCatEcho_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, CAT_CMD, FILE_ONE_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, ECHO_CMD, FILE_TWO_CONTENT);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT + SPACE + FILE_TWO_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    void echoGrepLs_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(TRIPLE_STRING, GREP_CMD, GREP_PATTERN, FILE_TWO_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, LS_CMD, NEST_DIR);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_TWO_CONTENT + SPACE + NEST_DIR + ": " + FILE_ONE_NAME + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    void echoCutWc_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_ONE_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, WC_CMD, FILE_ONE_NAME);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT + SPACE + "0 3 13 " + FILE_ONE_NAME + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    void echoSortCat_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_1);
        String subCmd2 = String.format(DOUBLE_STRING, CAT_CMD, FILE_TWO_NAME);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(SORT_MULTI.replaceAll(BREAK_LINE, SPACE).trim() + SPACE + FILE_TWO_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    void echoTeeGrep_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(OVERWRITTEN_TEXT.getBytes());
        System.setIn(inputCapture);

        String subCmd1 = String.format(SINGLE_STRING, TEE_CMD);
        String subCmd2 = String.format(TRIPLE_STRING, GREP_CMD, GREP_PATTERN, FILE_ONE_NAME);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(OVERWRITTEN_TEXT + SPACE + FILE_ONE_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    void echoPasteCut_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, PASTE_CMD, file1Path.toString());
        String subCmd2 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_ONE_NAME);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT + SPACE + FILE_ONE_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    void echoUniqSort_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQ2_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_2);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals("I love CS4218 Labs 55 CS4218 CS4218 hello world" + STRING_NEWLINE, testOutputStream.toString());
    }

    // LS
    @Test
    void lsCutUniq_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_FILE_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQF_NAME);
        String[] args = {LS_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_NAME + STRING_NEWLINE + FILE_ONE_NAME + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    void lsGrepCut_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(TRIPLE_STRING, GREP_CMD, FILE_ONE_NAME, FILE_FILE_NAME);
        String subCmd2 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_FILE_NAME);
        String[] args = {LS_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_NAME + STRING_NEWLINE + FILE_ONE_NAME + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    void lsCatSort_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, CAT_CMD, FILE_FILE_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, SORT_CMD, FILE_UNIQF_NAME);
        String[] args = {LS_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_NAME + STRING_NEWLINE + FILE_ONE_NAME + STRING_NEWLINE
                + FILE_ONE_NAME + STRING_NEWLINE + FILE_ONE_NAME + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    void lsEchoPaste_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, ECHO_CMD, NEST_DIR);
        String subCmd2 = String.format(DOUBLE_STRING, PASTE_CMD, uniqPathF.toString());
        String[] args = {LS_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_NAME + STRING_NEWLINE + FILE_ONE_NAME + STRING_NEWLINE
                + FILE_ONE_NAME + STRING_NEWLINE + STRING_NEWLINE
                + NEST_DIR + ":" + STRING_NEWLINE + FILE_ONE_NAME + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    void sortPasteEcho_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, PASTE_CMD, filePath.toString());
        String subCmd2 = String.format(DOUBLE_STRING, ECHO_CMD, FILE_SORT_2);
        String[] args = {SORT_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals("\n55\nCS4218\nCS4218\nI love CS4218\nhello\nworld".replaceAll(BREAK_LINE, STRING_NEWLINE) + STRING_NEWLINE, testOutputStream.toString());
    }

    // Grep
    @Test
    void grepPasteGrep_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, PASTE_CMD, folderPath.resolve(FILE_TWO_NAME));
        String subCmd2 = String.format(TRIPLE_STRING, GREP_CMD, FILE_ONE_NAME, FILE_FILE_NAME);
        String[] args = {GREP_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    void grepCutUniq_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_TWO_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQF_NAME);
        String[] args = {GREP_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    void grepEchoSort_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, ECHO_CMD, FILE_TWO_CONTENT);
        String subCmd2 = String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_NAME);
        String[] args = {GREP_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_ONE_NAME + ": " + FILE_ONE_CONTENT + STRING_NEWLINE +
                FILE_ONE_NAME + ": " + FILE_ONE_CONTENT + STRING_NEWLINE +
                FILE_TWO_NAME + ": " + FILE_TWO_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    @Test
    void grepTeeCat_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_TWO_CONTENT.getBytes());
        System.setIn(inputCapture);

        String subCmd1 = String.format(SINGLE_STRING, TEE_CMD);
        String subCmd2 = String.format(DOUBLE_STRING, CAT_CMD, FILE_SORT_NAME);
        String[] args = {GREP_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(FILE_TWO_NAME + ": " + FILE_TWO_CONTENT + STRING_NEWLINE +
                FILE_ONE_NAME + ": " + FILE_ONE_CONTENT + STRING_NEWLINE +
                FILE_ONE_NAME + ": " + FILE_ONE_CONTENT + STRING_NEWLINE, testOutputStream.toString());
    }

    // WC
    @Test
    void wcSortEcho_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, ECHO_CMD, FILE_TWO_NAME);
        String[] args = {WC_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(generateWcOutputString(0, 3, 13, FILE_ONE_NAME) +
                generateWcOutputString(0, 3, 13, FILE_ONE_NAME) +
                generateWcOutputString(0, 1, 6, FILE_TWO_NAME) +
                generateWcOutputString(0, 1, 6, FILE_TWO_NAME) +
                generateWcOutputString(0, 8, 38, TOTAL), testOutputStream.toString());
    }

    @Test
    void wcCutTee_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_TWO_NAME.getBytes());
        System.setIn(inputCapture);

        String subCmd1 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_FILE_NAME);
        String subCmd2 = String.format(SINGLE_STRING, TEE_CMD);
        String[] args = {WC_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(generateWcOutputString(0, 3, 13, FILE_ONE_NAME) +
                generateWcOutputString(0, 1, 6, FILE_TWO_NAME) +
                generateWcOutputString(0, 4, 19, TOTAL), testOutputStream.toString());
    }

    @Test
    void wcPasteUniq_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, PASTE_CMD, filePath.toString());
        String subCmd2 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQF_NAME);
        String[] args = {WC_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(generateWcOutputString(0, 3, 13, FILE_ONE_NAME) +
                generateWcOutputString(0, 3, 13, FILE_ONE_NAME) +
                generateWcOutputString(0, 6, 26, TOTAL), testOutputStream.toString());
    }

    @DisabledOnOs(OS.WINDOWS)
    @Test
    void wcLsGrep_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, LS_CMD, NEST_DIR);
        String subCmd2 = String.format(TRIPLE_STRING, GREP_CMD, FILE_ONE_NAME, FILE_FILE_NAME);
        String[] args = {WC_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(generateWcOutputString(0, 3, 13, FILE_ONE_NAME) +
                generateWcOutputString(0, 3, 13, FILE_ONE_NAME) +
                generateWcOutputString(0, 6, 26, TOTAL), testOutputStream.toString());
    }

    // RM
    @Test
    void rmCutGrep_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        Files.writeString(mv2Path, FILE_MV2_NAME); // For testing purpose
        String subCmd1 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_MV1_NAME);
        String subCmd2 = String.format(TRIPLE_STRING, GREP_CMD, FILE_MV2_NAME, FILE_MV2_NAME);
        String[] args = {RM_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertFalse(Files.exists(mv2Path));
    }

    @Test
    void rmUniqPaste_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv2Path, FILE_MV2_NAME); // For testing purpose
        String subCmd1 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQM_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, PASTE_CMD, mv2Path.toString());
        String[] args = {RM_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertFalse(Files.exists(mv2Path));
    }

    @Test
    void rmSortEcho_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        Files.writeString(mv2Path, FILE_MV2_NAME); // For testing purpose
        String subCmd1 = String.format(DOUBLE_STRING, SORT_CMD, FILE_MV1_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, ECHO_CMD, FILE_MV2_NAME);
        String[] args = {RM_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertFalse(Files.exists(mv2Path));
    }

    @Test
    void rmTeeCat_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_MV2_NAME.getBytes());
        System.setIn(inputCapture);
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        String subCmd1 = String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, CAT_CMD, FILE_MV1_NAME);
        String[] args = {RM_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertFalse(Files.exists(mv2Path));
        assertEquals(FILE_MV2_NAME, getFileContent(teeFilePath));
    }

    // MV
    @Test
    void mvPasteTee_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_MV2_NAME.getBytes());
        System.setIn(inputCapture);
        createMvTestFiles(folderPath);
        Files.writeString(mv2Path, FILE_MV1_NAME); // For testing purpose
        String subCmd1 = String.format(DOUBLE_STRING, PASTE_CMD, mv2Path.toString());
        String subCmd2 = String.format(SINGLE_STRING, TEE_CMD);
        String[] args = {MV_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertEquals(FILE_ONE_CONTENT, getFileContent(mv2Path));
    }

    @Test
    void mvCutEcho_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        String subCmd1 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_MV1_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, ECHO_CMD, DIR_MV_NAME);
        String[] args = {MV_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertTrue(Files.exists(mvFolderPath.resolve(FILE_MV1_NAME)));
    }

    @Test
    void mvGrepPaste_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        Files.writeString(mv2Path, FILE_MV2_NAME); // For testing purpose
        String subCmd1 = String.format(TRIPLE_STRING, GREP_CMD, FILE_MV1_NAME, FILE_MV1_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, PASTE_CMD, mv2Path.toString());
        String[] args = {MV_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv1Path));
        assertEquals(FILE_MV1_NAME, getFileContent(mv2Path));
    }

    @Test
    void mvSortUniq_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv2Path, FILE_MV2_NAME); // For testing purpose
        String subCmd1 = String.format(DOUBLE_STRING, SORT_CMD, FILE_MV2_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQM_NAME);
        String[] args = {MV_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertFalse(Files.exists(mv2Path));
        assertEquals(FILE_MV2_NAME, getFileContent(mv1Path));
    }

    // CP
    @Test
    void cpTeeSort_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_MV2_NAME.getBytes());
        System.setIn(inputCapture);
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        String subCmd1 = String.format(SINGLE_STRING, TEE_CMD);
        String subCmd2 = String.format(DOUBLE_STRING, SORT_CMD, FILE_MV1_NAME);
        String[] args = {CP_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(getFileContent(mv2Path), getFileContent(mv1Path));
    }

    @Test
    void cpEchoUniq_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        String subCmd1 = String.format(DOUBLE_STRING, ECHO_CMD, FILE_TWO_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQM_NAME);
        String[] args = {CP_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(getFileContent(mv2Path), getFileContent(mv1Path));
    }

    @Test
    void cpCutGrep_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        Files.writeString(mv2Path, FILE_MV2_NAME); // For testing purpose
        String subCmd1 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_MV1_NAME);
        String subCmd2 = String.format(TRIPLE_STRING, GREP_CMD, FILE_MV2_NAME, FILE_MV2_NAME);
        String[] args = {CP_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(getFileContent(mv1Path), getFileContent(mv2Path));
    }

    @Test
    void cpPasteCat_validSubCommand_shouldEvaluateSuccessfully() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        Files.writeString(mv2Path, FILE_MV2_NAME); // For testing purpose
        String subCmd1 = String.format(DOUBLE_STRING, PASTE_CMD, mv2Path.toString());
        String subCmd2 = String.format(DOUBLE_STRING, CAT_CMD, FILE_MV1_NAME);
        String[] args = {CP_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(getFileContent(mv2Path), getFileContent(mv1Path));
    }

    // Echo
    @Test
    void echoGrepGrep_invalid1stSubCommand_shouldThrowGrepException() {
        String subCmd1 = String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_ONE_NAME);
        String subCmd2 = String.format(TRIPLE_STRING, GREP_CMD, GREP_PATTERN, FILE_TWO_NAME);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    @Test
    void echoCutCut_invalid1stSubCommand_shouldThrowCutException() {
        String subCmd1 = String.format(TRIPLE_STRING, CUT_CMD, CUT_POS, FILE_ONE_NAME);
        String subCmd2 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_TWO_NAME);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    @Test
    void echoSortSort_invalid1stSubCommand_shouldThrowSortException() {
        String subCmd1 = String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID);
        String subCmd2 = String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_2);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    @Test
    void echoPastePaste_invalid1stSubCommand_shouldThrowPasteException() {
        String subCmd1 = String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID);
        String subCmd2 = String.format(DOUBLE_STRING, PASTE_CMD, folderPath.resolve(FILE_ONE_NAME));
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    @Test
    void echoUniqUniq_invalid1stSubCommand_shouldThrowUniqException() {
        String subCmd1 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_INVALID);
        String subCmd2 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQ2_NAME);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    @Test
    void echoInvalidUniq_invalid1stSubCommand_shouldEvaluateSuccessfully() {
        String subCmd1 = String.format(SINGLE_STRING, INVALID_CMD);
        String subCmd2 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQ2_NAME);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }

    @Test
    void echoCatEcho_invalid1stSubCommand_shouldThrowCatException() {
        String subCmd1 = String.format(DOUBLE_STRING, CAT_CMD, FILE_INVALID);
        String subCmd2 = String.format(DOUBLE_STRING, ECHO_CMD, FILE_TWO_CONTENT);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    @Test
    void echoGrepLs_invalid1stSubCommand_shouldThrowGrepException() {
        String subCmd1 = String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_TWO_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, LS_CMD, NEST_DIR);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    @Test
    void echoCutWc_invalid2ndSubCommand_shouldThrowWcException() {
        String subCmd1 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_ONE_NAME);
        String subCmd2 = String.format(TRIPLE_STRING, WC_CMD, "-x", FILE_INVALID);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(WcException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(WC_EXCEP_DIR, ERR_INVALID_FLAG), thrown.getMessage());
    }

    @Test
    void echoSortCat_invalid1stSubCommand_shouldThrowSortException() {
        String subCmd1 = String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID);
        String subCmd2 = String.format(DOUBLE_STRING, CAT_CMD, FILE_TWO_NAME);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    @Test
    void echoTeeGrep_invalid2ndSubCommand_shouldThrowGrepException() {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_TWO_CONTENT.getBytes());
        System.setIn(inputCapture); // Divert the input to System.in for Sub Command to evaluate
        String subCmd1 = String.format(SINGLE_STRING, TEE_CMD);
        String subCmd2 = String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_ONE_NAME);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    @Test
    void echoPasteCut_invalid1stSubCommand_shouldThrowPasteException() {
        String subCmd1 = String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID);
        String subCmd2 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_ONE_NAME);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    @Test
    void echoUniqSort_invalid1stSubCommand_shouldThrowUniqException() {
        String subCmd1 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_INVALID);
        String subCmd2 = String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_2);
        String[] args = {ECHO_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    // LS
    @Test
    void lsCutUniq_invalid1stSubCommand_shouldThrowCutException() {
        String subCmd1 = String.format(TRIPLE_STRING, CUT_CMD, CUT_POS, FILE_FILE_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQF_NAME);
        String[] args = {LS_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    @Test
    void lsCutUniq_invalid2ndSubCommand_shouldThrowUniqException() {
        String subCmd1 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_FILE_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_INVALID);
        String[] args = {LS_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    @Test
    void lsGrepCut_invalid1stSubCommand_shouldThrowGrepException() {
        String subCmd1 = String.format(QUAD_STRING, GREP_CMD, "-x", GREP_PATTERN, FILE_FILE_NAME);
        String subCmd2 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_FILE_NAME);
        String[] args = {LS_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage());
    }

    @Test
    void lsGrepCut_invalid2ndSubCommand_shouldThrowCutException() {
        String subCmd1 = String.format(TRIPLE_STRING, GREP_CMD, FILE_ONE_NAME, FILE_FILE_NAME);
        String subCmd2 = String.format(QUAD_STRING, CUT_CMD, "-x", CUT_POS, FILE_FILE_NAME);
        String[] args = {LS_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    @Test
    void lsGrepCut_invalidOuterSubCommand_shouldThrowLsException() {
        String subCmd1 = String.format(TRIPLE_STRING, GREP_CMD, GREP_PATTERN, FILE_FILE_NAME);
        String subCmd2 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_FILE_NAME);
        String[] args = {LS_CMD, "-x", subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(LsException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(LS_EXCEP, ERR_INVALID_FLAG + ": x:"), thrown.getMessage().trim());
    }

    @Test
    void lsCatSort_invalid1stSubCommand_shouldThrowCatException() {
        String subCmd1 = String.format(TRIPLE_STRING, CAT_CMD, "-x", FILE_FILE_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, SORT_CMD, FILE_UNIQF_NAME);
        String[] args = {LS_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, ERR_INVALID_FLAG, "x"), thrown.getMessage());
    }

    @Test
    void lsCatSort_invalid2ndSubCommand_shouldThrowSortException() {
        String subCmd1 = String.format(DOUBLE_STRING, CAT_CMD, FILE_FILE_NAME);
        String subCmd2 = String.format(TRIPLE_STRING, SORT_CMD, "-x", FILE_UNIQF_NAME);
        String[] args = {LS_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, ERR_INVALID_FLAG, "x"), thrown.getMessage());
    }

    @Test
    void lsCatSort_invalidOuterCommand_shouldThrowLsException() {
        String subCmd1 = String.format(DOUBLE_STRING, CAT_CMD, FILE_FILE_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, SORT_CMD, FILE_UNIQF_NAME);
        String[] args = {LS_CMD, "-x", subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(LsException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(LS_EXCEP, ERR_INVALID_FLAG + ": x:"), thrown.getMessage().trim());
    }

    // CP
    @Test
    void cpTeeSort_invalidOuterCommand_shouldThrowCpException() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_MV2_NAME.getBytes());
        System.setIn(inputCapture);
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        String subCmd1 = String.format(SINGLE_STRING, TEE_CMD);
        String subCmd2 = String.format(DOUBLE_STRING, SORT_CMD, FILE_MV1_NAME);
        String[] args = {CP_CMD, "-x", subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CpException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CP_EXCEP, ERR_INVALID_FLAG, "x"), thrown.getMessage().trim());
    }

    @Test
    void cpInvalidSort_invalid1stSubCommand_shouldThrowShellException() {
        String subCmd1 = String.format(SINGLE_STRING, INVALID_CMD);
        String subCmd2 = String.format(DOUBLE_STRING, SORT_CMD, FILE_ONE_NAME);
        String[] args = {CP_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage().trim());
    }

    @Test
    void cpTeeSort_invalid2ndSubCommand_shouldThrowSortException() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_MV2_NAME.getBytes());
        System.setIn(inputCapture);
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        String subCmd1 = String.format(SINGLE_STRING, TEE_CMD);
        String subCmd2 = String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID);
        String[] args = {CP_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage().trim());
    }


    @Test
    void cpEchoUniq_invalidOuterCommand_shouldThrowCpException() throws Exception {
        createMvTestFiles(folderPath);
        String subCmd1 = String.format(DOUBLE_STRING, ECHO_CMD, FILE_TWO_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQM_NAME);
        String[] args = {CP_CMD, subCmd1, subCmd2, FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CpException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CP_EXCEP_DIR, ERR_IS_NOT_DIR), thrown.getMessage().trim());
    }

    @Test
    void cpEchoUniq_invalid2ndCommand_shouldThrowUniqException() throws Exception {
        createMvTestFiles(folderPath);
        String subCmd1 = String.format(DOUBLE_STRING, ECHO_CMD, FILE_TWO_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, UNIQ_CMD, "-g", FILE_UNIQM_NAME);
        String[] args = {CP_CMD, subCmd1, subCmd2, FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, ERR_INVALID_FLAG, "g"), thrown.getMessage());
    }

    @Test
    void cpInvalidUniq_invalid1stCommand_shouldThrowShellException() throws Exception {
        createMvTestFiles(folderPath);
        String subCmd1 = String.format(SINGLE_STRING, INVALID_CMD);
        String subCmd2 = String.format(DOUBLE_STRING, UNIQ_CMD, "-g", FILE_UNIQM_NAME);
        String[] args = {CP_CMD, subCmd1, subCmd2, FILE_ONE_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }

    @Test
    void cpCutGrep_invalidOuterCommand_shouldThrowCpException() throws Exception {
        createMvTestFiles(folderPath);
        String subCmd1 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_MV1_NAME);
        String subCmd2 = String.format(TRIPLE_STRING, GREP_CMD, FILE_MV2_NAME, FILE_MV2_NAME);
        String[] args = {CP_CMD, "-g", subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CpException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CP_EXCEP, ERR_INVALID_FLAG, "g"), thrown.getMessage().trim());
    }

    @Test
    void cpCutGrep_invalid1stSubCommand_shouldThrowCutException() throws Exception {
        createMvTestFiles(folderPath);
        String subCmd1 = String.format(TRIPLE_STRING, CUT_CMD, CUT_POS, FILE_MV1_NAME);
        String subCmd2 = String.format(TRIPLE_STRING, GREP_CMD, FILE_MV2_NAME, FILE_MV2_NAME);
        String[] args = {CP_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage().trim());
    }

    @Test
    void cpCutGrep_invalid2ndSubCommand_shouldThrowGrepException() throws Exception {
        createMvTestFiles(folderPath);
        String subCmd1 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_MV1_NAME);
        String subCmd2 = String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_MV2_NAME);
        String[] args = {CP_CMD, "-g", subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage().trim());
    }

    @Test
    void cpPasteCat_invalidOuterCommand_shouldThrowCpException() throws Exception {
        createMvTestFiles(folderPath);
        String subCmd1 = String.format(DOUBLE_STRING, PASTE_CMD, mv2Path.toString());
        String subCmd2 = String.format(DOUBLE_STRING, CAT_CMD, FILE_MV1_NAME);
        String[] args = {CP_CMD, subCmd1, subCmd2, DIR_INVALID};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CpException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CP_EXCEP, folderPath.resolve(DIR_INVALID), ERR_DIR_NOT_FOUND), thrown.getMessage().trim());
    }

    @Test
    void cpPasteCat_invalid1stCommand_shouldThrowPasteException() throws Exception {
        createMvTestFiles(folderPath);
        String subCmd1 = String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID);
        String subCmd2 = String.format(DOUBLE_STRING, CAT_CMD, FILE_MV1_NAME);
        String[] args = {CP_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage().trim());
    }

    @Test
    void cpPasteCat_invalid2ndSubCommand_shouldThrowCatException() throws Exception {
        createMvTestFiles(folderPath);
        String subCmd1 = String.format(DOUBLE_STRING, PASTE_CMD, mv2Path.toString());
        String subCmd2 = String.format(DOUBLE_STRING, CAT_CMD, DIR_MV_NAME);
        String[] args = {CP_CMD, subCmd1, subCmd2, DIR_INVALID};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, DIR_MV_NAME, CatApplication.ERR_IS_DIR), thrown.getMessage().trim());
    }


    // RM
    @Test
    void rmCutGrep_invalidOuterCommand_shouldThrowRmException() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        Files.writeString(mv2Path, FILE_MV2_NAME); // For testing purpose
        String subCmd1 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_MV1_NAME);
        String subCmd2 = String.format(TRIPLE_STRING, GREP_CMD, FILE_MV2_NAME, FILE_MV2_NAME);
        String[] args = {RM_CMD, subCmd1, subCmd2, DIR_INVALID};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage().trim());
    }

    @Test
    void rmCutGrep_invalid1stSubCommand_shouldThrowCutException() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        Files.writeString(mv2Path, FILE_MV2_NAME); // For testing purpose
        String subCmd1 = String.format(TRIPLE_STRING, CUT_CMD, CUT_FLAG, FILE_MV1_NAME);
        String subCmd2 = String.format(TRIPLE_STRING, GREP_CMD, FILE_MV2_NAME, FILE_MV2_NAME);
        String[] args = {RM_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_LIST), thrown.getMessage().trim());
    }

    @Test
    void rmCutGrep_invalid2ndSubCommand_shouldThrowGrepException() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        Files.writeString(mv2Path, FILE_MV2_NAME); // For testing purpose
        String subCmd1 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_MV1_NAME);
        String subCmd2 = String.format(TRIPLE_STRING, GREP_CMD, "-g", FILE_MV2_NAME, FILE_MV2_NAME);
        String[] args = {RM_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage().trim());
    }

    @Test
    void rmUniqPaste_invalidOuterCommand_shouldThrowRmException() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv2Path, FILE_MV2_NAME); // For testing purpose
        String subCmd1 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQM_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, PASTE_CMD, mv2Path.toString());
        String[] args = {RM_CMD, subCmd1, subCmd2, DIR_MV_NAME};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, DIR_MV_NAME, IS_DIRECTORY), thrown.getMessage().trim());
    }

    @Test
    void rmUniqPaste_invalid1stSubCommand_shouldThrowUniqException() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv2Path, FILE_MV2_NAME); // For testing purpose
        String subCmd1 = String.format(DOUBLE_STRING, UNIQ_CMD, "-g", FILE_UNIQM_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, PASTE_CMD, mv2Path.toString());
        String[] args = {RM_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, ERR_INVALID_FLAG, "g"), thrown.getMessage().trim());
    }

    @Test
    void rmUniqPaste_invalid2ndSubCommand_shouldThrowPasteException() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv2Path, FILE_MV2_NAME); // For testing purpose
        String subCmd1 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQM_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, PASTE_CMD, "-z", mv2Path.toString());
        String[] args = {RM_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_INVALID_FLAG + ": z"), thrown.getMessage().trim());
    }

    @Test
    void rmSortEcho_invalidOuterCommand_shouldThrowRmException() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        Files.writeString(mv2Path, FILE_MV2_NAME); // For testing purpose
        String subCmd1 = String.format(DOUBLE_STRING, SORT_CMD, FILE_MV1_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, ECHO_CMD, FILE_MV2_NAME);
        String[] args = {RM_CMD, subCmd1, subCmd2, FILE_INVALID};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(RM_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage().trim());
    }

    @Test
    void rmSortEcho_invalid1stCommand_shouldThrowSortException() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        Files.writeString(mv2Path, FILE_MV2_NAME); // For testing purpose
        String subCmd1 = String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID);
        String subCmd2 = String.format(DOUBLE_STRING, ECHO_CMD, FILE_MV2_NAME);
        String[] args = {RM_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage().trim());
    }

    @Test
    void rmEchoInvalid_invalid1stCommand_shouldThrowShellException() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        Files.writeString(mv2Path, FILE_MV2_NAME); // For testing purpose
        String subCmd1 = String.format(DOUBLE_STRING, ECHO_CMD, FILE_MV2_NAME);
        String subCmd2 = String.format(SINGLE_STRING, INVALID_CMD);
        String[] args = {RM_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }

    @Test
    void rmTeeCat_invalidOuterCommand_shouldThrowRmException() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_MV2_NAME.getBytes());
        System.setIn(inputCapture);
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        String subCmd1 = String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, CAT_CMD, FILE_MV1_NAME);
        String[] args = {RM_CMD, "-g", subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        assertThrows(RmException.class, () -> callCommand.evaluate(System.in, testOutputStream));
    }

    @Test
    void rmTeeCat_invalid1stSubCommand_shouldThrowCatException() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_MV2_NAME.getBytes());
        System.setIn(inputCapture);
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        String subCmd1 = String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME);
        String subCmd2 = String.format(TRIPLE_STRING, CAT_CMD, "-g", FILE_MV1_NAME);
        String[] args = {RM_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CatException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CAT_EXCEP, ERR_INVALID_FLAG, "g"), thrown.getMessage().trim());
    }

    @Test
    void rmTeeInvalid_invalid2ndSubCommand_shouldThrowShellException() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_MV2_NAME.getBytes());
        System.setIn(inputCapture);
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        String subCmd1 = String.format(DOUBLE_STRING, TEE_CMD, FILE_TEE_NAME);
        String subCmd2 = String.format(SINGLE_STRING, INVALID_CMD);
        String[] args = {RM_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage().trim());
    }

    // MV
    @Test
    void mvPasteTee_invalidOuterCommand_shouldThrowMvException() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_MV2_NAME.getBytes());
        System.setIn(inputCapture);
        createMvTestFiles(folderPath);
        Files.writeString(mv2Path, FILE_MV1_NAME); // For testing purpose
        String subCmd1 = String.format(DOUBLE_STRING, PASTE_CMD, mv2Path.toString());
        String subCmd2 = String.format(SINGLE_STRING, TEE_CMD);
        String[] args = {MV_CMD, "-h", subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(MvException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(MV_EXCEP, ERR_INVALID_FLAG), thrown.getMessage().trim());
    }

    @Test
    void mvCutEcho_invalidOuterCommand_shouldOutputMvErrorMessage() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        String subCmd1 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_MV1_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, ECHO_CMD, DIR_MV_NAME);
        String[] args = {MV_CMD, FILE_INVALID, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(MV_EXCEP, ERR_FILE_NOT_FOUND) + STRING_NEWLINE, outputCapture.toString());
    }

    @Test
    void mvGrepPaste_invalidOuterCommand_shouldThrowMvException() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv1Path, FILE_MV1_NAME); // For testing purpose
        Files.writeString(mv2Path, FILE_MV2_NAME); // For testing purpose
        String subCmd1 = String.format(TRIPLE_STRING, GREP_CMD, FILE_MV1_NAME, FILE_MV1_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, PASTE_CMD, mv2Path.toString());
        String[] args = {MV_CMD, "-g", subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(MvException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(MV_EXCEP, ERR_INVALID_FLAG), thrown.getMessage().trim());
    }

    @Test
    void mvSortUniq_invalidOuterCommand_shouldOutputMvErrorMessage() throws Exception {
        createMvTestFiles(folderPath);
        Files.writeString(mv2Path, FILE_MV2_NAME); // For testing purpose
        String subCmd1 = String.format(DOUBLE_STRING, SORT_CMD, FILE_MV2_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQM_NAME);
        String[] args = {MV_CMD, subCmd1, subCmd2, FILE_INVALID};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(MV_EXCEP, ERR_IS_NOT_DIR) + STRING_NEWLINE, outputCapture.toString());
    }

    @Test
    void sortPasteEcho_invalidOuterCommand_shouldThrowSortException() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, PASTE_CMD, filePath.toString());
        String subCmd2 = String.format(DOUBLE_STRING, ECHO_CMD, FILE_SORT_2);
        String[] args = {SORT_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals("\n55\nCS4218\nCS4218\nI love CS4218\nhello\nworld".replaceAll(BREAK_LINE, STRING_NEWLINE) + STRING_NEWLINE, testOutputStream.toString());
    }

    // WC
    @Test
    void wcSortEcho_invalidOuterCommand_shouldThrowWcException() {
        String subCmd1 = String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_NAME);
        String subCmd2 = String.format(DOUBLE_STRING, ECHO_CMD, FILE_TWO_NAME);
        String[] args = {WC_CMD, "-r", subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(WcException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(WC_EXCEP_DIR, ERR_INVALID_FLAG), thrown.getMessage().trim());
    }

    @Test
    void wcSortEcho_invalid1stSubCommand_shouldThrowSortException() {
        String subCmd1 = String.format(DOUBLE_STRING, SORT_CMD, FILE_INVALID);
        String subCmd2 = String.format(DOUBLE_STRING, ECHO_CMD, FILE_TWO_NAME);
        String[] args = {WC_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(SortException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(SORT_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    @Test
    void wcSortInvalid_invalid1stSubCommand_shouldThrowShellException() {
        String subCmd1 = String.format(DOUBLE_STRING, SORT_CMD, FILE_SORT_NAME);
        String subCmd2 = String.format(SINGLE_STRING, INVALID_CMD);
        String[] args = {WC_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }

    @Test
    void wcCutTee_invalidOuterCommand_shouldOutputWcErrorMessage() throws Exception {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_TWO_NAME.getBytes());
        System.setIn(inputCapture);

        String subCmd1 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_FILE_NAME);
        String subCmd2 = String.format(SINGLE_STRING, TEE_CMD);
        String[] args = {WC_CMD, subCmd1, subCmd2, FILE_INVALID};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_EXCEP, FILE_INVALID, ERR_FILE_NOT_FOUND) + STRING_NEWLINE, outputCapture.toString());
    }

    @Test
    void wcCutTee_invalid1stSubCommand_shouldThrowCutException() {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_TWO_NAME.getBytes());
        System.setIn(inputCapture);

        String subCmd1 = String.format(TRIPLE_STRING, CUT_CMD, CUT_POS, FILE_FILE_NAME);
        String subCmd2 = String.format(SINGLE_STRING, TEE_CMD);
        String[] args = {WC_CMD, subCmd1, subCmd2, FILE_INVALID};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(CutException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(CUT_EXCEP, INVALID_FLAG), thrown.getMessage());
    }

    @Test
    void wcCutInvalid_invalid2ndSubCommand_shouldThrowShellException() {
        ByteArrayInputStream inputCapture = new ByteArrayInputStream(FILE_TWO_NAME.getBytes());
        System.setIn(inputCapture);

        String subCmd1 = String.format(QUAD_STRING, CUT_CMD, CUT_FLAG, CUT_POS, FILE_FILE_NAME);
        String subCmd2 = String.format(SINGLE_STRING, INVALID_CMD);
        String[] args = {WC_CMD, subCmd1, subCmd2, FILE_INVALID};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(ShellException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(SHELL_EXCEP, thrown.getMessage());
    }

    @Test
    void wcPasteUniq_invalidOuterCommand_shouldOutputWcErrorMessage() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, PASTE_CMD, filePath.toString());
        String subCmd2 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQF_NAME);
        String[] args = {WC_CMD, DIR_INVALID, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        callCommand.evaluate(System.in, testOutputStream);
        assertEquals(String.format(WC_EXCEP, DIR_INVALID, ERR_FILE_NOT_FOUND) + STRING_NEWLINE, outputCapture.toString());
    }

    @Test
    void wcPasteUniq_invalid1stSubCommand_shouldThrowPasteException() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, PASTE_CMD, FILE_INVALID);
        String subCmd2 = String.format(DOUBLE_STRING, UNIQ_CMD, FILE_UNIQF_NAME);
        String[] args = {WC_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(PasteException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(PASTE_EXCEP, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    @Test
    void wcPasteUniq_invalid2ndSubCommand_shouldThrowUniqException() throws Exception {
        String subCmd1 = String.format(DOUBLE_STRING, PASTE_CMD, filePath.toString());
        String subCmd2 = String.format(DOUBLE_STRING, UNIQ_CMD, "-g", FILE_INVALID);
        String[] args = {WC_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(UniqException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(UNIQ_EXCEP, ERR_INVALID_FLAG, "g"), thrown.getMessage());
    }

    @Test
    void wcLsGrep_invalidOuterCommand_shouldThrowWcException() {
        String subCmd1 = String.format(DOUBLE_STRING, LS_CMD, NEST_DIR);
        String subCmd2 = String.format(TRIPLE_STRING, GREP_CMD, FILE_ONE_NAME, FILE_FILE_NAME);
        String[] args = {WC_CMD, "-cs4218", subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(WcException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(WC_EXCEP_DIR, ERR_INVALID_FLAG), thrown.getMessage().trim());
    }

    @Test
    void wcLsGrep_invalid1stSubCommand_shouldThrowLsException() {
        String subCmd1 = String.format(DOUBLE_STRING, LS_CMD, "-g", NEST_DIR);
        String subCmd2 = String.format(TRIPLE_STRING, GREP_CMD, FILE_ONE_NAME, FILE_FILE_NAME);
        String[] args = {WC_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(LsException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(LS_EXCEP, ERR_INVALID_FLAG + ": g:"), thrown.getMessage().trim());
    }

    @Test
    void wcLsGrep_invalid2ndSubCommand_shouldThrowGrepException() {
        String subCmd1 = String.format(DOUBLE_STRING, LS_CMD, NEST_DIR);
        String subCmd2 = String.format(TRIPLE_STRING, GREP_CMD, INVALID_PATTERN, FILE_FILE_NAME);
        String[] args = {WC_CMD, subCmd1, subCmd2};
        callCommand = new CallCommand(Arrays.asList(args), applicationRunner, argumentResolver);
        Throwable thrown = assertThrows(GrepException.class, () -> callCommand.evaluate(System.in, testOutputStream));
        assertEquals(String.format(GREP_EXCEP, ERR_SYNTAX), thrown.getMessage().trim());
    }
}
