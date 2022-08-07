package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.app.ExitApplication.EXIT_MESSAGE;
import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_ISTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createNewFile;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.writeToFile;

public class CallCommandIT {

    private static final String FILE_1_CONTENT = "This is CS4218 test file!";
    private static final String FILE_2_CONTENT = "This is file 2!";
    private static final String FILE_3_CONTENT = "This is file 3! I might be modified or deleted, or manipulated?";
    private static final String README_CONTENT = "# CS4218codebase" + System.lineSeparator() +
            "codebase for CS4218, 21/22 Sem2" + System.lineSeparator();
    private static final String DUPLICATE_1 = "THIS IS DUPLICATED";
    private static final String DUPLICATE_2 = "49548359308KLFJGDFLGJD%$$%^";
    private static final String UNIQ_CONTENT = DUPLICATE_1 + System.lineSeparator()
            + DUPLICATE_1 + System.lineSeparator()
            + "THIS IS NOT" + System.lineSeparator()
            + DUPLICATE_2 + System.lineSeparator()
            + DUPLICATE_2 + System.lineSeparator()
            + "END";
    private static final String ECHO_CMD = "echo";
    private static final String CAT_CMD = "cat";
    private static final String TEST_MSG = "test";
    private static final String WC_FORMAT = "\t%7d\t%7d\t%7d\t%s";
    private static final String WC_FORMAT_ONE = "\t%7d\t%s";
    private static final String LS_RESULT = "file1.txt" + System.lineSeparator()
            + "file2.txt" + System.lineSeparator()
            + "file3.txt";
    @TempDir
    File testDir;
    File file1;
    File file2;
    File file3;
    private CallCommand callCommand;
    private ApplicationRunner applicationRunner;
    private ArgumentResolver argumentResolver;
    private InputStream stdin;
    private OutputStream stdout;

    @BeforeEach
    void setup() throws Exception {
        applicationRunner = new ApplicationRunner();
        argumentResolver = new ArgumentResolver();
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        stdout = new ByteArrayOutputStream();

        // setup files
        file1 = new File(testDir, "file1.txt");
        createNewFile(file1.toPath());
        writeToFile(file1.getPath(), FILE_1_CONTENT);

        file2 = new File(testDir, "file2.txt");
        createNewFile(file2.toPath());
        writeToFile(file2.getPath(), FILE_2_CONTENT);

        file3 = new File(testDir, "file3.txt");
        createNewFile(file3.toPath());
        writeToFile(file3.getPath(), FILE_3_CONTENT);

        Environment.currentDirectory = testDir.getPath();
    }

    @AfterEach
    void teardown() throws IOException {
        delete(testDir);

        stdout.flush();
        stdout.close();
    }

    // integrated tests

    // echo
    @Test
    void evaluate_EchoMessage_CorrectEval() throws Exception {
        String[] args = new String[]{ECHO_CMD, TEST_MSG};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);

        assertEquals(TEST_MSG + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_Echo3Args_CorrectEval() throws Exception {
        String[] args = new String[]{ECHO_CMD, "A", "B", "C"};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);

        assertEquals("A B C" + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_EchoQuotes_CorrectEval() throws Exception {
        String[] args = new String[]{ECHO_CMD, "\"A*B*C\""};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        assertEquals("A*B*C" + System.lineSeparator(), stdout.toString());
    }

    // ls
    @Test
    void evaluate_Ls_CorrectEval() throws Exception {
        String[] args = new String[]{APP_LS};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);

        assertEquals(LS_RESULT + System.lineSeparator(), stdout.toString());
    }

    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_LsR_CorrectEval() throws Exception {
        String[] args = new String[]{APP_LS, "-R"};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);

        assertEquals(".\\:" + System.lineSeparator()
                + LS_RESULT + System.lineSeparator(), stdout.toString());
    }

    @Test
    @DisabledOnOs({OS.WINDOWS})
    void evaluate_LsRUnix_CorrectEval() throws Exception {
        String[] args = new String[]{APP_LS, "-R"};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);

        assertEquals("./:" + System.lineSeparator()
                + LS_RESULT + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_LsX_CorrectEval() throws Exception {
        String[] args = new String[]{APP_LS, "-X"};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);

        assertEquals(LS_RESULT + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_LsStar_CorrectEval() throws Exception {
        String[] args = new String[]{APP_LS, "*"};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);

        assertEquals(LS_RESULT + System.lineSeparator(), stdout.toString());
    }

    // wc
    @Test
    void evaluate_WcFile1_CorrectEval() throws Exception {
        String[] args = new String[]{APP_WC, file1.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        assertEquals(String.format(WC_FORMAT, 0, 5, 25, file1.getPath())
                + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_WcFile1C_CorrectEval() throws Exception {
        String[] args = new String[]{APP_WC, "-c", file1.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        assertEquals(String.format(WC_FORMAT_ONE, 25, file1.getPath())
                + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_WcFile1L_CorrectEval() throws Exception {
        String[] args = new String[]{APP_WC, "-l", file1.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        assertEquals(String.format(WC_FORMAT_ONE, 0, file1.getPath())
                + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_WcFile1W_CorrectEval() throws Exception {
        String[] args = new String[]{APP_WC, "-w", file1.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        assertEquals(String.format(WC_FORMAT_ONE, 5, file1.getPath())
                + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_WcFile1File2_CorrectEval() throws Exception {
        String[] args = new String[]{APP_WC, file1.getPath(), file2.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        assertEquals(
                String.format(WC_FORMAT, 0, 5, 25, file1.getPath())
                        + System.lineSeparator()
                        + String.format(WC_FORMAT, 0, 4, 15, file2.getPath())
                        + System.lineSeparator()
                        + String.format(WC_FORMAT, 0, 9, 40, "total")
                        + System.lineSeparator(), stdout.toString());
    }

    // cat
    @Test
    void evaluate_CatOneFile_CorrectEval() throws Exception {
        String[] args = new String[]{APP_CAT, file1.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_CatTwoFile_CorrectEval() throws Exception {
        String[] args = new String[]{APP_CAT, file1.getPath(), file2.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT + System.lineSeparator()
                + FILE_2_CONTENT + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_CatOneFileN_CorrectEval() throws Exception {
        String[] args = new String[]{APP_CAT, "-n", file1.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        assertEquals("1 " + FILE_1_CONTENT + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_CatTwoFileN_CorrectEval() throws Exception {
        String[] args = new String[]{APP_CAT, "-n", file1.getPath(), file2.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        assertEquals("1 " + FILE_1_CONTENT + System.lineSeparator()
                + "1 " + FILE_2_CONTENT + System.lineSeparator(), stdout.toString());
    }

    // grep
    @Test
    void evaluate_GrepAll_CorrectEval() throws Exception {
        String[] args = new String[]{APP_GREP, "'.*'", file1.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_GrepRegex_CorrectEval() throws Exception {
        String[] args = new String[]{APP_GREP, "21/22"};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream(README_CONTENT.getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        assertEquals("codebase for CS4218, 21/22 Sem2" + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_GrepRegexI_CorrectEval() throws Exception {
        String[] args = new String[]{APP_GREP, "-i", "sem2"};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream(README_CONTENT.getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        assertEquals("codebase for CS4218, 21/22 Sem2" + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_GrepRegexC_CorrectEval() throws Exception {
        String[] args = new String[]{APP_GREP, "-c", "Sem2"};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream(README_CONTENT.getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        assertEquals("1" + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_GrepRegexH_CorrectEval() throws Exception {
        String[] args = new String[]{APP_GREP, "-H", "'.*", file1.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream(README_CONTENT.getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        assertEquals(file1.getPath() + ": " + FILE_1_CONTENT + System.lineSeparator(), stdout.toString());
    }

    // exit
    @Test
    void evaluate_Exit_CorrectEval() throws Exception {
        String[] args = new String[]{APP_EXIT};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream(README_CONTENT.getBytes(StandardCharsets.UTF_8));

        Throwable thrown = assertThrows(ExitException.class, () -> callCommand.evaluate(stdin, stdout));
        assertEquals(String.format("exit: %s", EXIT_MESSAGE), thrown.getMessage());
    }

    // cut
    @Test
    void evaluate_CutC_CorrectEval() throws Exception {
        String[] args = new String[]{APP_CUT, "-c", "1,10,12", file1.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);
        assertEquals("TS2" + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_CutB_CorrectEval() throws Exception {
        String[] args = new String[]{APP_CUT, "-b", "1-10", file1.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);
        assertEquals("This is CS" + System.lineSeparator(), stdout.toString());
    }

    // sort
    @Test
    void evaluate_Sort_CorrectEval() throws Exception {
        String[] args = new String[]{APP_SORT, file1.getPath(), file2.getPath(), file3.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);
        assertEquals(FILE_1_CONTENT + System.lineSeparator()
                + FILE_2_CONTENT + System.lineSeparator()
                + FILE_3_CONTENT + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_SortR_CorrectEval() throws Exception {
        String[] args = new String[]{APP_SORT, "-r", file1.getPath(), file2.getPath(), file3.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);
        assertEquals(FILE_3_CONTENT + System.lineSeparator()
                + FILE_2_CONTENT + System.lineSeparator()
                + FILE_1_CONTENT + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_SortF_CorrectEval() throws Exception {
        String[] args = new String[]{APP_SORT, "-f", file1.getPath(), file2.getPath(), file3.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);
        assertEquals(FILE_1_CONTENT + System.lineSeparator()
                + FILE_2_CONTENT + System.lineSeparator()
                + FILE_3_CONTENT + System.lineSeparator(), stdout.toString());
    }

    // rm
    @Test
    void evaluate_Rm_CorrectEval() throws Exception {
        String[] args = new String[]{APP_RM, file1.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        assertTrue(file1.exists());
        callCommand.evaluate(stdin, stdout);
        assertFalse(file1.exists());
    }

    @Test
    void evaluate_RmR_CorrectEval() throws Exception {
        String[] args = new String[]{APP_RM, "-r", testDir.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        assertTrue(file1.exists());
        assertTrue(file2.exists());
        assertTrue(file3.exists());
        callCommand.evaluate(stdin, stdout);
        assertFalse(file1.exists());
        assertFalse(file2.exists());
        assertFalse(file3.exists());
    }

    @Test
    void evaluate_RmD_CorrectEval() throws Exception {
        String[] args = new String[]{APP_RM, "-d", testDir.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        file1.delete();
        file2.delete();
        file3.delete();

        assertTrue(testDir.exists());
        callCommand.evaluate(stdin, stdout);
        assertFalse(testDir.exists());
    }

    // tee
    @Test
    void evaluate_Tee_CorrectEval() throws Exception {
        String[] args = new String[]{APP_TEE, file1.getPath()};
        stdin = new ByteArrayInputStream(FILE_2_CONTENT.getBytes(StandardCharsets.UTF_8));
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);

        assertEquals(FILE_2_CONTENT + System.lineSeparator(), stdout.toString());
        assertEquals(FILE_2_CONTENT + System.lineSeparator(), Files.readString(file1.toPath()));
    }

    @Test
    void evaluate_TeeA_CorrectEval() throws Exception {
        String[] args = new String[]{APP_TEE, "-a", file1.getPath()};
        stdin = new ByteArrayInputStream(FILE_2_CONTENT.getBytes(StandardCharsets.UTF_8));
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);

        assertEquals(FILE_2_CONTENT + System.lineSeparator(), stdout.toString());
        assertEquals(FILE_1_CONTENT + FILE_2_CONTENT + System.lineSeparator(), Files.readString(file1.toPath()));
    }

    // cp
    @Test
    void evaluate_Cp_CorrectEval() throws Exception {
        Path file4Path = Path.of(testDir.getPath(), "file4.txt");
        String[] args = new String[]{APP_CP, file1.getPath(), file4Path.toString()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT, Files.readString(file4Path));
    }

    // cd
    @Test
    void evaluate_Cd_CorrectEval() throws Exception {
        String[] args = new String[]{APP_CD, ".."};
        String expectedDir = Path.of(Environment.currentDirectory).getParent().toString();
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);

        assertEquals(expectedDir, Environment.currentDirectory);
    }

    @Test
    void evaluate_CdDownTwo_CorrectEval() throws Exception {
        String[] args = new String[]{APP_CD, "../.."};
        String expectedDir = Path.of(Environment.currentDirectory).getParent().getParent().toString();
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);

        assertEquals(expectedDir, Environment.currentDirectory);
    }

    // paste
    @Test
    void evaluate_Paste_CorrectEval() throws Exception {
        String[] args = new String[]{APP_PASTE, file1.getPath(), file2.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);
        assertEquals("This is CS4218 test file!\tThis is file 2!" + System.lineSeparator(), stdout.toString());
    }

    // uniq
    @Test
    void evaluate_Uniq_CorrectEval() throws Exception {
        String[] args = new String[]{APP_UNIQ};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream(UNIQ_CONTENT.getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);
        assertEquals(DUPLICATE_1 + System.lineSeparator()
                + "THIS IS NOT" + System.lineSeparator()
                + DUPLICATE_2 + System.lineSeparator()
                + "END" + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_UniqC_CorrectEval() throws Exception {
        String[] args = new String[]{APP_UNIQ, "-c"};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream(UNIQ_CONTENT.getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);
        assertEquals("\t2 " + DUPLICATE_1 + System.lineSeparator()
                + "\t1 THIS IS NOT" + System.lineSeparator()
                + "\t2 " + DUPLICATE_2 + System.lineSeparator()
                + "\t1 END" + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_Uniqd_CorrectEval() throws Exception {
        String[] args = new String[]{APP_UNIQ, "-d"};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream(UNIQ_CONTENT.getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);
        assertEquals(DUPLICATE_1 + System.lineSeparator()
                + DUPLICATE_2 + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_UniqD_CorrectEval() throws Exception {
        String[] args = new String[]{APP_UNIQ, "-D"};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream(UNIQ_CONTENT.getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);
        assertEquals(DUPLICATE_1 + System.lineSeparator()
                + DUPLICATE_1 + System.lineSeparator()
                + DUPLICATE_2 + System.lineSeparator()
                + DUPLICATE_2 + System.lineSeparator(), stdout.toString());
    }

    // mv
    @Test
    void evaluate_Mv_CorrectEval() throws Exception {
        Path file4Path = Path.of(testDir.getParent(), "file4.txt");
        String[] args = new String[]{APP_MV, file1.getPath(), file4Path.toString()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        callCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT, Files.readString(file4Path));
        assertFalse(file1.exists());
    }

    // redirection tests
    @Test
    void evaluate_WithStdinUsingCatWithRedirectOutCommandTwoFiles_CorrectEval() throws Exception {
        String[] args = new String[]{CAT_CMD, file1.getPath(), ">", file2.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        List<String> fileContent = Files.readAllLines(file2.toPath());
        assertEquals("This is CS4218 test file!", fileContent.get(0));
        assertEquals("", stdout.toString());
    }

    @Test
    void evaluate_WithStdinUsingCatWithRedirectOutCommandThreeFiles_CorrectEval() throws Exception {
        String[] args = new String[]{CAT_CMD, file1.getPath(), file2.getPath(), ">", file3.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        List<String> fileContent = Files.readAllLines(file3.toPath());

        assertEquals(FILE_1_CONTENT, fileContent.get(0));
        assertEquals(FILE_2_CONTENT, fileContent.get(1));
        assertEquals("", stdout.toString());
    }

    @Test
    void evaluate_WithStdinUsingCatWithRedirectInCommand_CorrectEval() throws Exception {
        String[] args = new String[]{CAT_CMD, "<", file3.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        assertEquals(FILE_3_CONTENT + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_WithStdinUsingWcWithRedirectInCommand_CorrectEval() throws Exception {
        String[] args = new String[]{"wc", "<", file1.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        assertEquals(
                String.format("\t%7d\t%7d\t%7d", 0, 5, 25)
                        + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_WithStdinUsingTeeWithRedirectInCommand_CorrectEval() throws Exception {
        String[] args = new String[]{"tee", file1.getPath(), "<", file2.getPath()};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        callCommand.evaluate(stdin, stdout);

        List<String> fileContent = Files.readAllLines(file1.toPath());
        assertEquals(FILE_2_CONTENT, fileContent.get(0));
    }

    // failed test cases
    @Test
    void evaluate_NoStdin_ThrowsException() {
        String[] args = new String[]{"echo", "test"};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        ShellException shellException = assertThrows(ShellException.class, () -> {
            callCommand.evaluate(null, stdout);
        });
        assertTrue(shellException.getMessage().contains(ERR_NO_ISTREAM));
    }

    @Test
    void evaluate_NoStdout_ThrowsException() {
        String[] args = new String[]{"echo", "test"};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        ShellException shellException = assertThrows(ShellException.class, () -> {
            callCommand.evaluate(stdin, null);
        });
        assertTrue(shellException.getMessage().contains(ERR_NO_OSTREAM));
    }

    @Test
    void evaluate_InvalidApp_ThrowsException() throws Exception {
        String[] args = new String[]{"thisappdonotexist"};
        callCommand = new CallCommand(List.of(args), applicationRunner, argumentResolver);
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        ShellException shellException = assertThrows(ShellException.class, () -> {
            callCommand.evaluate(stdin, stdout);
        });
        assertTrue(shellException.getMessage().equals("shell: thisappdonotexist: Invalid app"));
    }

    // helper function
    void delete(File file) throws IOException {
        if (file.isDirectory()) {
            for (File fileN : file.listFiles()) {
                delete(fileN);
            }
        }
        if (file.exists()) {
            file.delete(); // ignore results since it could've just been deleted before
        }
    }
}
