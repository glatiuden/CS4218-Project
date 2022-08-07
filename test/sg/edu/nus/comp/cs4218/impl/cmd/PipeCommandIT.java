package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.PasteException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.expectedFileLine;

public class PipeCommandIT {
    private static final String KEYWORD_APPLE = "Apple";
    private static final String KEYWORD_BOY = "Boy";
    private static final String KEYWORD_CAR = "Car";
    private static final String KEYWORD_ZEALOUS = "Zealous";
    private static final String KEYWORD_DONKEY = "Donkey";
    private static final String KEYWORD_FAST = "Fast";
    private static final String KEYWORD_GOOGLE = "Google";
    private static final String KEYWORD_CS3203 = "CS3203 - Software Engineering Project";
    private static final String KEYWORD_CS4218 = "CS4218 - Software Testing";
    private static final String KEYWORD_CS4218_C = "CS4218";
    private static final String KEYWORD_CS3235 = "CS3235 - Computer Security";
    private static final String KEYWORD_GES1035 = "GES1035 - Singapore: Imagining the Next 50 Years";
    private static final String KEYWORD_UNIQUE = "I will make this a unique content";
    private static final String KEYWORD_N_UNIQUE = "This is not unique";
    private static final String KEYWORD_10_A = "AAAAAAAAAA";
    private static final String KEYWORD_10_B = "BBBBBBBBBB";
    private static final String KEYWORD_HEH = "heh";
    private static final String KEYWORD_TOTAL = "total";
    private static final String GREP_ALL = "'.*'";
    private static final String CUT_12 = "1,2";
    private static final String CUT_1000 = "1-1000";
    private static final String CUT_10000 = "1-10000";
    private static final String FILE_1_CONTENT = KEYWORD_APPLE + System.lineSeparator()
            + KEYWORD_BOY + System.lineSeparator()
            + KEYWORD_CAR + System.lineSeparator()
            + KEYWORD_ZEALOUS + System.lineSeparator()
            + KEYWORD_DONKEY + System.lineSeparator()
            + KEYWORD_FAST + System.lineSeparator()
            + KEYWORD_GOOGLE;
    private static final String FILE_2_CONTENT = KEYWORD_CS3203 + System.lineSeparator()
            + KEYWORD_CS4218 + System.lineSeparator()
            + KEYWORD_CS3235 + System.lineSeparator()
            + KEYWORD_GES1035;
    private static final String FILE_3_CONTENT = "This is file 3! I might be modified or deleted, or manipulated?";
    private static final String FILE_4_CONTENT = KEYWORD_UNIQUE + System.lineSeparator()
            + KEYWORD_UNIQUE + System.lineSeparator()
            + KEYWORD_N_UNIQUE + System.lineSeparator()
            + KEYWORD_10_A + System.lineSeparator()
            + KEYWORD_10_B + System.lineSeparator()
            + KEYWORD_10_A + System.lineSeparator()
            + KEYWORD_10_A;
    private static final String FILE_1_CONTENT_S = KEYWORD_APPLE + System.lineSeparator()
            + KEYWORD_BOY + System.lineSeparator()
            + KEYWORD_CAR + System.lineSeparator()
            + KEYWORD_DONKEY + System.lineSeparator()
            + KEYWORD_FAST + System.lineSeparator()
            + KEYWORD_GOOGLE + System.lineSeparator()
            + KEYWORD_ZEALOUS;
    private static final String FILE_4_UNIQ = KEYWORD_UNIQUE + System.lineSeparator()
            + KEYWORD_N_UNIQUE + System.lineSeparator()
            + KEYWORD_10_A + System.lineSeparator()
            + KEYWORD_10_B + System.lineSeparator()
            + KEYWORD_10_A;
    private static final String FILE_4_UNIQ_D = KEYWORD_UNIQUE + System.lineSeparator()
            + KEYWORD_10_A;
    private static final String FILE_4_UNIQ_WC = expectedFileLine(91, 5, 14, null);
    private static final String FILE_4_SORT_UNIQ = KEYWORD_10_A + System.lineSeparator()
            + KEYWORD_10_B + System.lineSeparator()
            + KEYWORD_UNIQUE + System.lineSeparator()
            + KEYWORD_N_UNIQUE;
    private static final String FILE_4_UNIQ_SORT = KEYWORD_10_A + System.lineSeparator()
            + KEYWORD_10_A + System.lineSeparator()
            + KEYWORD_10_B + System.lineSeparator()
            + KEYWORD_UNIQUE + System.lineSeparator()
            + KEYWORD_N_UNIQUE;
    private static final String FILE_4_UNIQD_SORT = KEYWORD_10_A + System.lineSeparator()
            + KEYWORD_UNIQUE;
    private static final String FILE1_WC = expectedFileLine(48, 7, 7, null);
    private static final String FILE2_WC = expectedFileLine(144, 4, 21, null);
    private static final String FILE1_PASTE_FILE2 = "Apple\tCS3203 - Software Engineering Project" + System.lineSeparator()
            + "Boy\tCS4218 - Software Testing" + System.lineSeparator()
            + "Car\tCS3235 - Computer Security" + System.lineSeparator()
            + "Zealous\tGES1035 - Singapore: Imagining the Next 50 Years" + System.lineSeparator()
            + "Donkey\t" + System.lineSeparator()
            + "Fast\t" + System.lineSeparator()
            + "Google\t";
    private static String tempDirLs = "";
    private static String tempDirLsWc = "";
    private static String file1WcName = "";
    private static String file2WcName = "";
    @TempDir
    File testDir;
    File file1;
    File file2;
    File file3;
    File file4;
    private PipeCommand pipeCommand;
    private ApplicationRunner applicationRunner;
    private ArgumentResolver argumentResolver;
    private InputStream stdin;
    private OutputStream stdout;

    @BeforeEach
    void init() throws Exception {
        applicationRunner = new ApplicationRunner();
        argumentResolver = new ArgumentResolver();
        stdin = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        stdout = new ByteArrayOutputStream();

        // setup files
        file1 = new File(testDir, "file1.txt");
        Files.write(file1.toPath(), Collections.singleton(FILE_1_CONTENT));

        file2 = new File(testDir, "file2.txt");
        Files.write(file2.toPath(), Collections.singleton(FILE_2_CONTENT));

        file3 = new File(testDir, "file3.txt");
        Files.write(file3.toPath(), Collections.singleton(FILE_3_CONTENT));

        file4 = new File(testDir, "file4.txt");
        Files.write(file4.toPath(), Collections.singleton(FILE_4_CONTENT));

        tempDirLs = file1.getName() + System.lineSeparator()
                + file2.getName() + System.lineSeparator()
                + file3.getName() + System.lineSeparator()
                + file4.getName();

        tempDirLsWc = expectedFileLine(44, 4, 4, null);

        file1WcName = expectedFileLine(48, 7, 7, file1.getPath());
        file2WcName = expectedFileLine(144, 4, 21, file2.getPath());

        Environment.currentDirectory = testDir.getPath();
    }

    @AfterEach
    void teardown() {
        Environment.resetCurrentDirectory();
    }

    @SuppressWarnings("PMD.UseVarargs")
        // Reason: Cannot be used as varargs as array of arguments has to be passed in
    List<CallCommand> generate2PipeCommand(String[] arg1, String[] arg2) {
        CallCommand command1 = new CallCommand(List.of(arg1), applicationRunner, argumentResolver);
        CallCommand command2 = new CallCommand(List.of(arg2), applicationRunner, argumentResolver);

        return Arrays.asList(command1, command2);
    }

    @SuppressWarnings("PMD.UseVarargs")
        // Reason: Cannot be used as varargs as array of arguments has to be passed in
    List<CallCommand> generate3PipeCommand(String[] arg1, String[] arg2, String[] arg3) {
        CallCommand command1 = new CallCommand(List.of(arg1), applicationRunner, argumentResolver);
        CallCommand command2 = new CallCommand(List.of(arg2), applicationRunner, argumentResolver);
        CallCommand command3 = new CallCommand(List.of(arg3), applicationRunner, argumentResolver);

        return Arrays.asList(command1, command2, command3);
    }

    @Test
    void evaluate_WithStdinUsingEchoCommandPipeCutCommand_CorrectEval() throws Exception {
        String[] echoArgs = new String[]{APP_ECHO, "\"baz\""};
        CallCommand echoCommand = new CallCommand(List.of(echoArgs), applicationRunner, argumentResolver);

        String[] cutArgs = new String[]{"cut", "-b", "2"};
        CallCommand cutCommand = new CallCommand(List.of(cutArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(echoCommand, cutCommand);

        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("a" + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_WithStdinUsingCatCommandPipeSortCommand_CorrectEval() throws Exception {
        String[] catArgs = new String[]{APP_CAT, file1.getPath()};
        CallCommand catCommand = new CallCommand(List.of(catArgs), applicationRunner, argumentResolver);

        String[] sortArgs = new String[]{"sort"};
        CallCommand sortCommand = new CallCommand(List.of(sortArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(catCommand, sortCommand);

        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT_S + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_WithStdinUsingCatCommandPipeGrepCommand_CorrectEval() throws Exception {
        String[] catArgs = new String[]{APP_CAT, file2.getPath()};
        CallCommand catCommand = new CallCommand(List.of(catArgs), applicationRunner, argumentResolver);

        String[] grepArgs = new String[]{"grep", KEYWORD_CS4218_C};
        CallCommand grepCommand = new CallCommand(List.of(grepArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(catCommand, grepCommand);

        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_CS4218 + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_WithStdinUsingCatCommandPipeGrepCommandCSKeyword_CorrectEval() throws Exception {
        String[] catArgs = new String[]{APP_CAT, file2.getPath()};
        CallCommand catCommand = new CallCommand(List.of(catArgs), applicationRunner, argumentResolver);

        String[] grepArgs = new String[]{"grep", "CS"};
        CallCommand grepCommand = new CallCommand(List.of(grepArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(catCommand, grepCommand);

        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_CS3203 + System.lineSeparator()
                + KEYWORD_CS4218 + System.lineSeparator()
                + KEYWORD_CS3235 + System.lineSeparator(), stdout.toString());
    }

    @Test
    void evaluate_WithStdinUsingEchoCommandPipeTeeCommand_CorrectEval() throws Exception {
        String[] echoArgs = new String[]{APP_ECHO, "hello"};
        CallCommand echoCommand = new CallCommand(List.of(echoArgs), applicationRunner, argumentResolver);

        String[] teeArgs = new String[]{"tee", file1.getPath()};
        CallCommand teeCommand = new CallCommand(List.of(teeArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(echoCommand, teeCommand);

        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        List<String> fileContent = Files.readAllLines(file1.toPath());
        assertEquals("hello", fileContent.get(0));
    }

    // 2 apps (1 pipe)
    // ls ls
    @Test
    void evaluate_LsLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_LS};
        String[] arg2 = new String[]{APP_LS};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // ls wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_LsWc_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_LS};
        String[] arg2 = new String[]{APP_WC};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLsWc + System.lineSeparator(), stdout.toString());
    }

    // ls cat
    @Test
    void evaluate_LsCat_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_LS};
        String[] arg2 = new String[]{APP_CAT};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // ls grep
    @Test
    void evaluate_LsGrep_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_LS};
        String[] arg2 = new String[]{APP_GREP, "file1"};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(file1.getName() + System.lineSeparator(), stdout.toString());
    }

    // ls cut
    @Test
    void evaluate_LsCut_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_LS};
        String[] arg2 = new String[]{APP_CUT, "-c", "5"};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("1" + System.lineSeparator()
                + "2" + System.lineSeparator()
                + "3" + System.lineSeparator()
                + "4" + System.lineSeparator(), stdout.toString());
    }

    // 6: ls sort
    @Test
    void evaluate_LsSort_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_LS};
        String[] arg2 = new String[]{APP_SORT};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 7: ls tee
    @Test
    void evaluate_LsTee_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_LS};
        String[] arg2 = new String[]{APP_TEE};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 8: ls paste
    @Test
    void evaluate_LsPaste_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_LS};
        String[] arg2 = new String[]{APP_PASTE, "-"};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 9: ls uniq
    @Test
    void evaluate_LsUniq_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_LS};
        String[] arg2 = new String[]{APP_UNIQ};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 10: wc wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_WcWc_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_WC, file1.getPath()};
        String[] arg2 = new String[]{APP_WC, file1.getPath()};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(file1WcName + System.lineSeparator(), stdout.toString());
    }

    // 11: wc cat
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_WcCat_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_WC, file1.getPath()};
        String[] arg2 = new String[]{APP_CAT};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(expectedFileLine(48, 7, 7, file1.getPath()) + System.lineSeparator(), stdout.toString());
    }

    // 12: wc grep
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_WcGrep_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_WC, file1.getPath()};
        String[] arg2 = new String[]{APP_GREP, GREP_ALL};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(expectedFileLine(48, 7, 7, file1.getPath()) + System.lineSeparator(), stdout.toString());
    }

    // 13: wc cut
    @Test
    void evaluate_WcCut_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_WC, file1.getPath()};
        String[] arg2 = new String[]{APP_CUT, "-c", "8"};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("7" + System.lineSeparator(), stdout.toString());
    }

    // 14: wc sort
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_WcSort_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_WC, file1.getPath(), file2.getPath()};
        String[] arg2 = new String[]{APP_SORT};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(file2WcName + System.lineSeparator()
                + file1WcName + System.lineSeparator()
                + expectedFileLine(192, 11, 28, KEYWORD_TOTAL) + System.lineSeparator(), stdout.toString());
    }

    // 15: wc tee
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_WcTee_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_WC, file1.getPath()};
        String[] arg2 = new String[]{APP_TEE};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(file1WcName + System.lineSeparator(), stdout.toString());
    }

    // 16: wc paste
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_WcPaste_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_WC, file1.getPath()};
        String[] arg2 = new String[]{APP_PASTE, "-"};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(file1WcName + System.lineSeparator(), stdout.toString());
    }

    // 17: wc uniq
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_WcUniq_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_WC, file1.getPath()};
        String[] arg2 = new String[]{APP_UNIQ};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(file1WcName + System.lineSeparator(), stdout.toString());
    }

    // 18: wc ls
    @Test
    void evaluate_WcLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_WC, file1.getPath()};
        String[] arg2 = new String[]{APP_LS};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 19: cat cat
    @Test
    void evaluate_CatCat_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CAT, file1.getPath()};
        String[] arg2 = new String[]{APP_CAT, "-", file2.getPath()};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT + System.lineSeparator()
                + FILE_2_CONTENT + System.lineSeparator(), stdout.toString());
    }

    // 20: cat grep
    @Test
    void evaluate_CatGrep_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CAT, file1.getPath(), file2.getPath()};
        String[] arg2 = new String[]{APP_GREP, GREP_ALL};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT + System.lineSeparator()
                + FILE_2_CONTENT + System.lineSeparator(), stdout.toString());
    }

    // 21: cat cut
    @Test
    void evaluate_CatCut_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CAT, file2.getPath()};
        String[] arg2 = new String[]{APP_CUT, "-c", "1-6"};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("CS3203" + System.lineSeparator()
                + KEYWORD_CS4218_C + System.lineSeparator()
                + "CS3235" + System.lineSeparator()
                + "GES103" + System.lineSeparator(), stdout.toString());
    }

    // 22: cat sort
    @Test
    void evaluate_CatSort_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CAT, file1.getPath()};
        String[] arg2 = new String[]{APP_SORT};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT_S + System.lineSeparator(), stdout.toString());
    }

    // 23: cat tee
    @Test
    void evaluate_CatTee_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CAT, file1.getPath()};
        String[] arg2 = new String[]{APP_TEE, file2.getPath()};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT + System.lineSeparator(), stdout.toString());
        assertEquals(FILE_1_CONTENT + System.lineSeparator(), Files.readString(file2.toPath()));
    }

    // 24: cat paste
    @Test
    void evaluate_CatPaste_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CAT, file1.getPath()};
        String[] arg2 = new String[]{APP_PASTE, "-", file2.getPath()};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_PASTE_FILE2 + System.lineSeparator(), stdout.toString());
    }

    // 25: cat uniq
    @Test
    void evaluate_CatUniq_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CAT, file4.getPath()};
        String[] arg2 = new String[]{APP_UNIQ};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQ + System.lineSeparator(), stdout.toString());
    }

    // 26: cat ls
    @Test
    void evaluate_CatLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CAT, file4.getPath()};
        String[] arg2 = new String[]{APP_LS};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 27: cat wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_CatWc_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CAT, file1.getPath()};
        String[] arg2 = new String[]{APP_WC};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_WC + System.lineSeparator(), stdout.toString());
    }

    // 28: grep grep
    @Test
    void evaluate_GrepGrep_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_GREP, "CS", file2.getPath()};
        String[] arg2 = new String[]{APP_GREP, GREP_ALL};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_CS3203 + System.lineSeparator()
                + KEYWORD_CS4218 + System.lineSeparator()
                + KEYWORD_CS3235 + System.lineSeparator(), stdout.toString());
    }

    // 29: grep cut
    @Test
    void evaluate_GrepCut_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_GREP, "CS", file2.getPath()};
        String[] arg2 = new String[]{APP_CUT, "-c", "3-6"};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("3203" + System.lineSeparator()
                + "4218" + System.lineSeparator()
                + "3235" + System.lineSeparator(), stdout.toString());
    }

    // 30: grep sort
    @Test
    void evaluate_GrepSort_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_GREP, "CS", file2.getPath()};
        String[] arg2 = new String[]{APP_SORT};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_CS3203 + System.lineSeparator()
                + KEYWORD_CS3235 + System.lineSeparator()
                + KEYWORD_CS4218 + System.lineSeparator(), stdout.toString());
    }

    // 31: grep tee
    @Test
    void evaluate_GrepTee_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_GREP, "CS", file2.getPath()};
        String[] arg2 = new String[]{APP_TEE, "-", file3.getPath()};

        String expectedMessage = KEYWORD_CS3203 + System.lineSeparator()
                + KEYWORD_CS4218 + System.lineSeparator()
                + KEYWORD_CS3235 + System.lineSeparator();

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(expectedMessage, stdout.toString());
        assertEquals(expectedMessage, Files.readString(file3.toPath()));
    }

    // 32: grep paste
    @Test
    void evaluate_GrepPaste_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_GREP, GREP_ALL, file1.getPath()};
        String[] arg2 = new String[]{APP_PASTE, "-", file2.getPath()};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_PASTE_FILE2 + System.lineSeparator(), stdout.toString());
    }

    // 33: grep uniq
    @Test
    void evaluate_GrepUniq_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_GREP, GREP_ALL, file4.getPath()};
        String[] arg2 = new String[]{APP_UNIQ};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQ + System.lineSeparator(), stdout.toString());
    }

    // 34: grep ls
    @Test
    void evaluate_GrepLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_GREP, GREP_ALL, file4.getPath()};
        String[] arg2 = new String[]{APP_LS};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 35: grep wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_GrepWc_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_GREP, GREP_ALL, file2.getPath()};
        String[] arg2 = new String[]{APP_WC};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE2_WC + System.lineSeparator(), stdout.toString());
    }

    // 36: grep cat
    @Test
    void evaluate_GrepCat_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_GREP, "AAAAAAA", file4.getPath()};
        String[] arg2 = new String[]{APP_CAT};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_10_A + System.lineSeparator()
                + KEYWORD_10_A + System.lineSeparator()
                + KEYWORD_10_A + System.lineSeparator(), stdout.toString());
    }

    // 37: cut cut
    @Test
    void evaluate_CutCut_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", "1-2", file2.getPath()};
        String[] arg2 = new String[]{APP_CUT, "-c", "1", "-", file1.getPath()};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("C" + System.lineSeparator()
                + "C" + System.lineSeparator()
                + "C" + System.lineSeparator()
                + "G" + System.lineSeparator()
                + "A" + System.lineSeparator()
                + "B" + System.lineSeparator()
                + "C" + System.lineSeparator()
                + "Z" + System.lineSeparator()
                + "D" + System.lineSeparator()
                + "F" + System.lineSeparator()
                + "G" + System.lineSeparator(), stdout.toString());
    }

    // 38: cut sort
    @Test
    void evaluate_CutSort_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", "1", file1.getPath()};
        String[] arg2 = new String[]{APP_SORT};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("A" + System.lineSeparator()
                + "B" + System.lineSeparator()
                + "C" + System.lineSeparator()
                + "D" + System.lineSeparator()
                + "F" + System.lineSeparator()
                + "G" + System.lineSeparator()
                + "Z" + System.lineSeparator(), stdout.toString());
    }

    // 39: cut tee
    @Test
    void evaluate_CutTee_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", "1", file1.getPath()};
        String[] arg2 = new String[]{APP_TEE, file2.getPath()};

        String expectedMessage = "A" + System.lineSeparator()
                + "B" + System.lineSeparator()
                + "C" + System.lineSeparator()
                + "Z" + System.lineSeparator()
                + "D" + System.lineSeparator()
                + "F" + System.lineSeparator()
                + "G" + System.lineSeparator();

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(expectedMessage, stdout.toString());
        assertEquals(expectedMessage, Files.readString(file2.toPath()));
    }

    // 40: cut paste
    @Test
    void evaluate_CutPaste_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", CUT_10000, file1.getPath()};
        String[] arg2 = new String[]{APP_PASTE, "-", file2.getPath()};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_PASTE_FILE2 + System.lineSeparator(), stdout.toString());
    }

    // 41: cut uniq
    @Test
    void evaluate_CutUniq_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", "1", file2.getPath()};
        String[] arg2 = new String[]{APP_UNIQ, "-d"};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("C" + System.lineSeparator(), stdout.toString());
    }

    // 42: cut ls
    @Test
    void evaluate_CutLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", "1024", file2.getPath()};
        String[] arg2 = new String[]{APP_LS};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 43: cut wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_CutWc_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", "1,5", file1.getPath()};
        String[] arg2 = new String[]{APP_WC};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(expectedFileLine(25, 7, 7, null) + System.lineSeparator(), stdout.toString());
    }

    // 44: cut cat
    @Test
    void evaluate_CutCat_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", CUT_12, file2.getPath()};
        String[] arg2 = new String[]{APP_CAT};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("CS" + System.lineSeparator()
                + "CS" + System.lineSeparator()
                + "CS" + System.lineSeparator()
                + "GE" + System.lineSeparator(), stdout.toString());
    }

    // 45: cut grep
    @Test
    void evaluate_CutGrep_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", "3", file2.getPath()};
        String[] arg2 = new String[]{APP_GREP, "[A-Z]"};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("S" + System.lineSeparator(), stdout.toString());
    }

    // 46: sort sort
    @Test
    void evaluate_SortSort_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file1.getPath()};
        String[] arg2 = new String[]{APP_SORT, "-", file1.getPath()};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_APPLE + System.lineSeparator()
                + KEYWORD_APPLE + System.lineSeparator()
                + KEYWORD_BOY + System.lineSeparator()
                + KEYWORD_BOY + System.lineSeparator()
                + KEYWORD_CAR + System.lineSeparator()
                + KEYWORD_CAR + System.lineSeparator()
                + KEYWORD_DONKEY + System.lineSeparator()
                + KEYWORD_DONKEY + System.lineSeparator()
                + KEYWORD_FAST + System.lineSeparator()
                + KEYWORD_FAST + System.lineSeparator()
                + KEYWORD_GOOGLE + System.lineSeparator()
                + KEYWORD_GOOGLE + System.lineSeparator()
                + KEYWORD_ZEALOUS + System.lineSeparator()
                + KEYWORD_ZEALOUS + System.lineSeparator(), stdout.toString());
    }

    // 47: sort tee
    @Test
    void evaluate_SortTee_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file1.getPath()};
        String[] arg2 = new String[]{APP_TEE, "-", file2.getPath()};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT_S + System.lineSeparator(), stdout.toString());
        assertEquals(FILE_1_CONTENT_S + System.lineSeparator(), Files.readString(file2.toPath()));
    }

    // 48: sort paste
    @Test
    void evaluate_SortPaste_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file1.getPath()};
        String[] arg2 = new String[]{APP_PASTE, "-", file2.getPath()};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("Apple\tCS3203 - Software Engineering Project" + System.lineSeparator()
                + "Boy\tCS4218 - Software Testing" + System.lineSeparator()
                + "Car\tCS3235 - Computer Security" + System.lineSeparator()
                + "Donkey\tGES1035 - Singapore: Imagining the Next 50 Years" + System.lineSeparator()
                + "Fast\t" + System.lineSeparator()
                + "Google\t" + System.lineSeparator()
                + "Zealous\t" + System.lineSeparator(), stdout.toString());
    }

    // 49: sort uniq
    @Test
    void evaluate_SortUniq_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file4.getPath()};
        String[] arg2 = new String[]{APP_UNIQ};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_SORT_UNIQ + System.lineSeparator(), stdout.toString());
    }

    // 50: sort ls
    @Test
    void evaluate_SortLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file4.getPath()};
        String[] arg2 = new String[]{APP_LS};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 51: sort wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_SortWc_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file1.getPath()};
        String[] arg2 = new String[]{APP_WC};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_WC + System.lineSeparator(), stdout.toString());
    }

    // 52: sort cat
    @Test
    void evaluate_SortCat_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file1.getPath()};
        String[] arg2 = new String[]{APP_CAT};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT_S + System.lineSeparator(), stdout.toString());
    }

    // 53: sort grep
    @Test
    void evaluate_SortGrep_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file1.getPath()};
        String[] arg2 = new String[]{APP_GREP, GREP_ALL};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT_S + System.lineSeparator(), stdout.toString());
    }

    // 54: sort cut
    @Test
    void evaluate_SortCut_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file1.getPath()};
        String[] arg2 = new String[]{APP_CUT, "-c", "1"};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("A" + System.lineSeparator()
                + "B" + System.lineSeparator()
                + "C" + System.lineSeparator()
                + "D" + System.lineSeparator()
                + "F" + System.lineSeparator()
                + "G" + System.lineSeparator()
                + "Z" + System.lineSeparator(), stdout.toString());
    }

    // 55: tee tee
    @Test
    void evaluate_TeeTee_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_TEE, file1.getPath()};
        String[] arg2 = new String[]{APP_TEE, file2.getPath()};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        stdin = new ByteArrayInputStream(FILE_1_CONTENT.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT + System.lineSeparator(), stdout.toString());
        assertEquals(FILE_1_CONTENT + System.lineSeparator(), Files.readString(file1.toPath()));
        assertEquals(FILE_1_CONTENT + System.lineSeparator(), Files.readString(file2.toPath()));
    }

    // 56: tee paste
    @Test
    void evaluate_TeePaste_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_TEE, file1.getPath()};
        String[] arg2 = new String[]{APP_PASTE, "-", file2.getPath()};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        stdin = new ByteArrayInputStream(FILE_1_CONTENT.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_PASTE_FILE2 + System.lineSeparator(), stdout.toString());
        assertEquals(FILE_1_CONTENT + System.lineSeparator(), Files.readString(file1.toPath()));
    }

    // 57: tee uniq
    @Test
    void evaluate_TeeUniq_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_TEE, file4.getPath()};
        String[] arg2 = new String[]{APP_UNIQ};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        stdin = new ByteArrayInputStream(FILE_4_CONTENT.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQ + System.lineSeparator(), stdout.toString());
        assertEquals(FILE_4_CONTENT + System.lineSeparator(), Files.readString(file4.toPath()));
    }

    // 58: tee ls
    @Test
    void evaluate_TeeLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_TEE, file4.getPath()};
        String[] arg2 = new String[]{APP_LS};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        stdin = new ByteArrayInputStream(FILE_4_CONTENT.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
        assertEquals(FILE_4_CONTENT + System.lineSeparator(), Files.readString(file4.toPath()));
    }

    // 59: tee wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_TeeWc_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_TEE, file1.getPath()};
        String[] arg2 = new String[]{APP_WC};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        stdin = new ByteArrayInputStream(FILE_1_CONTENT.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_WC + System.lineSeparator(), stdout.toString());
        assertEquals(FILE_1_CONTENT + System.lineSeparator(), Files.readString(file1.toPath()));
    }

    // 60: tee cat
    @Test
    void evaluate_TeeCat_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_TEE, file2.getPath()};
        String[] arg2 = new String[]{APP_CAT};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        stdin = new ByteArrayInputStream(FILE_2_CONTENT.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_2_CONTENT + System.lineSeparator(), stdout.toString());
        assertEquals(FILE_2_CONTENT + System.lineSeparator(), Files.readString(file2.toPath()));
    }

    // 61: tee grep
    @Test
    void evaluate_TeeGrep_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_TEE, file2.getPath()};
        String[] arg2 = new String[]{APP_GREP, "CS"};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        stdin = new ByteArrayInputStream(FILE_2_CONTENT.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_CS3203 + System.lineSeparator()
                + KEYWORD_CS4218 + System.lineSeparator()
                + KEYWORD_CS3235 + System.lineSeparator(), stdout.toString());
        assertEquals(FILE_2_CONTENT + System.lineSeparator(), Files.readString(file2.toPath()));
    }

    // 62: tee cut
    @Test
    void evaluate_TeeCut_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_TEE, file2.getPath()};
        String[] arg2 = new String[]{APP_CUT, "-c", "1-2"};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        stdin = new ByteArrayInputStream(FILE_2_CONTENT.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("CS" + System.lineSeparator()
                + "CS" + System.lineSeparator()
                + "CS" + System.lineSeparator()
                + "GE" + System.lineSeparator(), stdout.toString());
        assertEquals(FILE_2_CONTENT + System.lineSeparator(), Files.readString(file2.toPath()));
    }

    // 63: tee sort
    @Test
    void evaluate_TeeSort_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_TEE, file1.getPath()};
        String[] arg2 = new String[]{APP_SORT};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        stdin = new ByteArrayInputStream(FILE_1_CONTENT.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT_S + System.lineSeparator(), stdout.toString());
        assertEquals(FILE_1_CONTENT + System.lineSeparator(), Files.readString(file1.toPath()));
    }

    // 64: paste paste
    @Test
    void evaluate_PastePaste_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE, file1.getPath()};
        String[] arg2 = new String[]{APP_PASTE, "-", file2.getPath()};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_PASTE_FILE2 + System.lineSeparator(), stdout.toString());
    }

    // 65: paste uniq
    @Test
    void evaluate_PasteUniq_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE, file4.getPath()};
        String[] arg2 = new String[]{APP_UNIQ};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQ + System.lineSeparator(), stdout.toString());
    }

    // 66: paste ls
    @Test
    void evaluate_PasteLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE, file4.getPath()};
        String[] arg2 = new String[]{APP_LS};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 67: paste wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_PasteWc_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE, file2.getPath()};
        String[] arg2 = new String[]{APP_WC};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE2_WC + System.lineSeparator(), stdout.toString());
    }

    // 68: paste cat
    @Test
    void evaluate_PasteCat_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE, file3.getPath()};
        String[] arg2 = new String[]{APP_CAT};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_3_CONTENT + System.lineSeparator(), stdout.toString());
    }

    // 69: paste grep
    @Test
    void evaluate_PasteGrep_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE, file1.getPath()};
        String[] arg2 = new String[]{APP_GREP, KEYWORD_APPLE};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_APPLE + System.lineSeparator(), stdout.toString());
    }

    // 70: paste cut
    @Test
    void evaluate_PasteCut_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE, file1.getPath()};
        String[] arg2 = new String[]{APP_CUT, "-c", CUT_10000};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT + System.lineSeparator(), stdout.toString());
    }

    // 71: paste sort
    @Test
    void evaluate_PasteSort_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE, file1.getPath()};
        String[] arg2 = new String[]{APP_SORT};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT_S + System.lineSeparator(), stdout.toString());
    }

    // 72: paste tee
    @Test
    void evaluate_PasteTee_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE, file1.getPath()};
        String[] arg2 = new String[]{APP_TEE, file2.getPath()};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT + System.lineSeparator(), stdout.toString());
        assertEquals(FILE_1_CONTENT + System.lineSeparator(), Files.readString(file2.toPath()));
    }

    // 73: uniq uniq
    @Test
    void evaluate_UniqUniq_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, file4.getPath()};
        String[] arg2 = new String[]{APP_UNIQ};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQ + System.lineSeparator(), stdout.toString());
    }

    // 74: uniq ls
    @Test
    void evaluate_UniqLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, file4.getPath()};
        String[] arg2 = new String[]{APP_LS};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 75: uniq wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_UniqWc_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, file4.getPath()};
        String[] arg2 = new String[]{APP_WC};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQ_WC + System.lineSeparator(), stdout.toString());
    }

    // 76: uniq cat
    @Test
    void evaluate_UniqCat_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, file4.getPath()};
        String[] arg2 = new String[]{APP_CAT};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQ + System.lineSeparator(), stdout.toString());
    }

    // 77: uniq grep
    @Test
    void evaluate_UniqGrep_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, file4.getPath()};
        String[] arg2 = new String[]{APP_GREP, "BBB"};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_10_B + System.lineSeparator(), stdout.toString());
    }

    // 78: uniq cut
    @Test
    void evaluate_UniqCut_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, file4.getPath()};
        String[] arg2 = new String[]{APP_CUT, "-c", "100"};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("" + System.lineSeparator()
                + "" + System.lineSeparator()
                + "" + System.lineSeparator()
                + "" + System.lineSeparator()
                + "" + System.lineSeparator(), stdout.toString());
    }

    // 79: uniq sort
    @Test
    void evaluate_UniqSort_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, file4.getPath()};
        String[] arg2 = new String[]{APP_SORT};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQ_SORT + System.lineSeparator(), stdout.toString());
    }

    // 80: uniq tee
    @Test
    void evaluate_UniqTee_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, file4.getPath()};
        String[] arg2 = new String[]{APP_TEE, file2.getPath()};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQ + System.lineSeparator(), stdout.toString());
        assertEquals(FILE_4_UNIQ + System.lineSeparator(), Files.readString(file2.toPath()));
    }

    // 81: uniq paste
    @Test
    void evaluate_UniqPaste_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, file4.getPath()};
        String[] arg2 = new String[]{APP_PASTE, file2.getPath()};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_2_CONTENT + System.lineSeparator(), stdout.toString());
    }

    // 82: echo ls
    @Test
    void evaluate_EchoLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_ECHO, "hello"};
        String[] arg2 = new String[]{APP_LS};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 83: echo wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_EchoWcWindows_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_ECHO, FILE_2_CONTENT};
        String[] arg2 = new String[]{APP_WC};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE2_WC + System.lineSeparator(), stdout.toString());
    }

    // 84: echo cat
    @Test
    void evaluate_EchoCat_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_ECHO, FILE_1_CONTENT};
        String[] arg2 = new String[]{APP_CAT};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT + System.lineSeparator(), stdout.toString());
    }

    // 85: echo grep
    @Test
    void evaluate_EchoGrep_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_ECHO, FILE_1_CONTENT};
        String[] arg2 = new String[]{APP_GREP, "Z"};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_ZEALOUS + System.lineSeparator(), stdout.toString());
    }

    // 86: echo cut
    @Test
    void evaluate_EchoCut_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_ECHO, FILE_1_CONTENT};
        String[] arg2 = new String[]{APP_CUT, "-c", "1"};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("A" + System.lineSeparator()
                + "B" + System.lineSeparator()
                + "C" + System.lineSeparator()
                + "Z" + System.lineSeparator()
                + "D" + System.lineSeparator()
                + "F" + System.lineSeparator()
                + "G" + System.lineSeparator(), stdout.toString());
    }

    // 87: echo sort
    @Test
    void evaluate_EchoSort_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_ECHO, FILE_1_CONTENT};
        String[] arg2 = new String[]{APP_SORT};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT_S + System.lineSeparator(), stdout.toString());
    }

    // 88: echo tee
    @Test
    void evaluate_EchoTee_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_ECHO, FILE_4_CONTENT};
        String[] arg2 = new String[]{APP_TEE, file2.getPath()};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_CONTENT + System.lineSeparator(), stdout.toString());
        assertEquals(FILE_4_CONTENT + System.lineSeparator(), Files.readString(file2.toPath()));
    }

    // 89: echo paste
    @Test
    void evaluate_EchoPaste_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_ECHO, FILE_1_CONTENT};
        String[] arg2 = new String[]{APP_PASTE, "-", file2.getPath()};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_PASTE_FILE2 + System.lineSeparator(), stdout.toString());
    }

    // 90: echo uniq
    @Test
    void evaluate_EchoUniq_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_ECHO, FILE_4_CONTENT};
        String[] arg2 = new String[]{APP_UNIQ};

        List<CallCommand> commandList = generate2PipeCommand(arg1, arg2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQ + System.lineSeparator(), stdout.toString());
    }

    // 3 apps (2 pipes)
    // 10: wc wc cat
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_WcWcCat_CorrectEval() throws Exception {
        String[] wcArgs1 = new String[]{APP_WC, file1.getPath()};
        String[] wcArgs2 = new String[]{APP_WC, file1.getPath()};
        String[] catArgs = new String[]{APP_CAT};
        CallCommand wcCommand1 = new CallCommand(List.of(wcArgs1), applicationRunner, argumentResolver);
        CallCommand wcCommand2 = new CallCommand(List.of(wcArgs2), applicationRunner, argumentResolver);
        CallCommand catCommand = new CallCommand(List.of(catArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(wcCommand1, wcCommand2, catCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(file1WcName + System.lineSeparator(), stdout.toString());
    }

    // 11: wc cat grep
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_WcCatGrep_CorrectEval() throws Exception {
        String[] wcArgs = new String[]{APP_WC, file1.getPath()};
        String[] catArgs = new String[]{APP_CAT};
        String[] grepArgs = new String[]{APP_GREP, "[0-9]"};
        CallCommand wcCommand = new CallCommand(List.of(wcArgs), applicationRunner, argumentResolver);
        CallCommand catCommand = new CallCommand(List.of(catArgs), applicationRunner, argumentResolver);
        CallCommand grepCommand = new CallCommand(List.of(grepArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(wcCommand, catCommand, grepCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(expectedFileLine(48, 7, 7, file1.getPath()) + System.lineSeparator(), stdout.toString());
    }

    // 12: wc grep cut
    @Test
    void evaluate_WcGrepCut_CorrectEval() throws Exception {
        String[] wcArgs = new String[]{APP_WC, file1.getPath()};
        String[] grepArgs = new String[]{APP_GREP, "[0-9]"};
        String[] cutArgs = new String[]{APP_CUT, "-c", "8"}; // 8th position is the line position
        CallCommand wcCommand = new CallCommand(List.of(wcArgs), applicationRunner, argumentResolver);
        CallCommand grepCommand = new CallCommand(List.of(grepArgs), applicationRunner, argumentResolver);
        CallCommand cutCommand = new CallCommand(List.of(cutArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(wcCommand, grepCommand, cutCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("7" + System.lineSeparator(), stdout.toString());
    }

    // 13: wc cut sort
    @Test
    void evaluate_WcCutSort_CorrectEval() throws Exception {
        String[] wcArgs = new String[]{APP_WC, file2.getPath(), file3.getPath()};
        String[] cutArgs = new String[]{APP_CUT, "-c", "8"}; // 8th position is the line position
        String[] sortArgs = new String[]{APP_SORT};
        CallCommand wcCommand = new CallCommand(List.of(wcArgs), applicationRunner, argumentResolver);
        CallCommand cutCommand = new CallCommand(List.of(cutArgs), applicationRunner, argumentResolver);
        CallCommand sortCommand = new CallCommand(List.of(sortArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(wcCommand, cutCommand, sortCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("1" + System.lineSeparator() + "4" + System.lineSeparator() + "5" + System.lineSeparator(), stdout.toString());
    }

    // 14: wc sort tee
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_WcSortTee_CorrectEval() throws Exception {
        String[] wcArgs = new String[]{APP_WC, file2.getPath(), file3.getPath()};
        String[] sortArgs = new String[]{APP_SORT};
        String[] teeArgs = new String[]{APP_TEE};

        CallCommand wcCommand = new CallCommand(List.of(wcArgs), applicationRunner, argumentResolver);
        CallCommand sortCommand = new CallCommand(List.of(sortArgs), applicationRunner, argumentResolver);
        CallCommand teeCommand = new CallCommand(List.of(teeArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(wcCommand, sortCommand, teeCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(expectedFileLine(65, 1, 12, file3.getPath())
                + System.lineSeparator()
                + expectedFileLine(144, 4, 21, file2.getPath())
                + System.lineSeparator()
                + expectedFileLine(209, 5, 33, KEYWORD_TOTAL)
                + System.lineSeparator(), stdout.toString());
    }

    // 15: wc tee paste
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_WcTeePaste_CorrectEval() throws Exception {
        String[] wcArgs = new String[]{APP_WC, file2.getPath(), file3.getPath()};
        String[] teeArgs = new String[]{APP_TEE};
        String[] pasteArgs = new String[]{APP_PASTE};

        CallCommand wcCommand = new CallCommand(List.of(wcArgs), applicationRunner, argumentResolver);
        CallCommand teeCommand = new CallCommand(List.of(teeArgs), applicationRunner, argumentResolver);
        CallCommand pasteCommand = new CallCommand(List.of(pasteArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(wcCommand, teeCommand, pasteCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(expectedFileLine(144, 4, 21, file2.getPath())
                + System.lineSeparator()
                + expectedFileLine(65, 1, 12, file3.getPath())
                + System.lineSeparator()
                + expectedFileLine(209, 5, 33, KEYWORD_TOTAL)
                + System.lineSeparator(), stdout.toString());
    }

    // 16: wc paste uniq
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_WcPasteUniq_CorrectEval() throws Exception {
        String[] wcArgs = new String[]{APP_WC, file2.getPath(), file3.getPath()};
        String[] pasteArgs = new String[]{APP_PASTE};
        String[] uniqArgs = new String[]{APP_UNIQ};

        CallCommand wcCommand = new CallCommand(List.of(wcArgs), applicationRunner, argumentResolver);
        CallCommand pasteCommand = new CallCommand(List.of(pasteArgs), applicationRunner, argumentResolver);
        CallCommand uniqCommand = new CallCommand(List.of(uniqArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(wcCommand, pasteCommand, uniqCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(expectedFileLine(144, 4, 21, file2.getPath())
                + System.lineSeparator()
                + expectedFileLine(65, 1, 12, file3.getPath())
                + System.lineSeparator()
                + expectedFileLine(209, 5, 33, KEYWORD_TOTAL)
                + System.lineSeparator(), stdout.toString());
    }

    // 17: wc uniq ls
    @Test
    void evaluate_WcUniqLs_CorrectEval() throws Exception {
        String[] wcArgs = new String[]{APP_WC, file2.getPath(), file3.getPath()};
        String[] uniqArgs = new String[]{APP_UNIQ};
        String[] lsArgs = new String[]{APP_LS};

        CallCommand wcCommand = new CallCommand(List.of(wcArgs), applicationRunner, argumentResolver);
        CallCommand uniqCommand = new CallCommand(List.of(uniqArgs), applicationRunner, argumentResolver);
        CallCommand lsCommand = new CallCommand(List.of(lsArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(wcCommand, uniqCommand, lsCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 18: wc ls wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_WcLsWc_CorrectEval() throws Exception {
        String[] wcArgs1 = new String[]{APP_WC, file2.getPath(), file3.getPath()};
        String[] lsArgs = new String[]{APP_LS};
        String[] wcArgs2 = new String[]{APP_WC};

        Environment.currentDirectory = testDir.getPath();

        CallCommand wcCommand1 = new CallCommand(List.of(wcArgs1), applicationRunner, argumentResolver);
        CallCommand lsCommand = new CallCommand(List.of(lsArgs), applicationRunner, argumentResolver);
        CallCommand wcCommand2 = new CallCommand(List.of(wcArgs2), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(wcCommand1, lsCommand, wcCommand2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLsWc + System.lineSeparator(), stdout.toString());
    }

    // 19: cat cat cut
    @Test
    void evaluate_CatCatCut_CorrectEval() throws Exception {
        String[] catArgs1 = new String[]{APP_CAT, file1.getPath()};
        String[] catArgs2 = new String[]{APP_CAT};
        String[] cutArgs = new String[]{APP_CUT, "-c", "1-5"};

        CallCommand catCommand1 = new CallCommand(List.of(catArgs1), applicationRunner, argumentResolver);
        CallCommand catCommand2 = new CallCommand(List.of(catArgs2), applicationRunner, argumentResolver);
        CallCommand cutCommand = new CallCommand(List.of(cutArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(catCommand1, catCommand2, cutCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_APPLE + System.lineSeparator()
                + KEYWORD_BOY + System.lineSeparator()
                + KEYWORD_CAR + System.lineSeparator()
                + "Zealo" + System.lineSeparator()
                + "Donke" + System.lineSeparator()
                + KEYWORD_FAST + System.lineSeparator()
                + "Googl" + System.lineSeparator(), stdout.toString());
    }

    // 20: cat grep sort
    @Test
    void evaluate_CatGrepSort_CorrectEval() throws Exception {
        String[] catArgs = new String[]{APP_CAT, file2.getPath()};
        String[] grepArgs = new String[]{APP_GREP, "CS"};
        String[] sortArgs = new String[]{APP_SORT};

        CallCommand catCommand = new CallCommand(List.of(catArgs), applicationRunner, argumentResolver);
        CallCommand grepCommand = new CallCommand(List.of(grepArgs), applicationRunner, argumentResolver);
        CallCommand sortCommand = new CallCommand(List.of(sortArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(catCommand, grepCommand, sortCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_CS3203 + System.lineSeparator()
                + KEYWORD_CS3235 + System.lineSeparator()
                + KEYWORD_CS4218 + System.lineSeparator(), stdout.toString());
    }

    // 21: cat cut tee
    @Test
    void evaluate_CatCutTee_CorrectEval() throws Exception {
        String[] catArgs = new String[]{APP_CAT, file1.getPath()};
        String[] cutArgs = new String[]{APP_CUT, "-c", "1-5"};
        String[] teeArgs = new String[]{APP_TEE};

        CallCommand catCommand = new CallCommand(List.of(catArgs), applicationRunner, argumentResolver);
        CallCommand cutCommand = new CallCommand(List.of(cutArgs), applicationRunner, argumentResolver);
        CallCommand teeCommand = new CallCommand(List.of(teeArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(catCommand, cutCommand, teeCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_APPLE + System.lineSeparator()
                + KEYWORD_BOY + System.lineSeparator()
                + KEYWORD_CAR + System.lineSeparator()
                + "Zealo" + System.lineSeparator()
                + "Donke" + System.lineSeparator()
                + KEYWORD_FAST + System.lineSeparator()
                + "Googl" + System.lineSeparator(), stdout.toString());
    }

    // 22: cat sort paste
    @Test
    void evaluate_CatSortPaste_CorrectEval() throws Exception {
        String[] catArgs = new String[]{APP_CAT, file1.getPath()};
        String[] sortArgs = new String[]{APP_SORT};
        String[] pasteArgs = new String[]{APP_PASTE};

        CallCommand catCommand = new CallCommand(List.of(catArgs), applicationRunner, argumentResolver);
        CallCommand sortCommand = new CallCommand(List.of(sortArgs), applicationRunner, argumentResolver);
        CallCommand pasteCommand = new CallCommand(List.of(pasteArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(catCommand, sortCommand, pasteCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT_S + System.lineSeparator(), stdout.toString());
    }

    // 23: cat tee uniq
    @Test
    void evaluate_CatTeeUniq_CorrectEval() throws Exception {
        String[] catArgs = new String[]{APP_CAT, file4.getPath()};
        String[] teeArgs = new String[]{APP_TEE};
        String[] uniqArgs = new String[]{APP_UNIQ};

        CallCommand catCommand = new CallCommand(List.of(catArgs), applicationRunner, argumentResolver);
        CallCommand teeCommand = new CallCommand(List.of(teeArgs), applicationRunner, argumentResolver);
        CallCommand uniqCommand = new CallCommand(List.of(uniqArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(catCommand, teeCommand, uniqCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQ + System.lineSeparator(), stdout.toString());
    }

    // 24: cat paste ls
    @Test
    void evaluate_CatPasteLs_CorrectEval() throws Exception {
        String[] catArgs = new String[]{APP_CAT, file1.getPath()};
        String[] pasteArgs = new String[]{APP_PASTE};
        String[] lsArgs = new String[]{APP_LS};

        CallCommand catCommand = new CallCommand(List.of(catArgs), applicationRunner, argumentResolver);
        CallCommand pasteCommand = new CallCommand(List.of(pasteArgs), applicationRunner, argumentResolver);
        CallCommand lsCommand = new CallCommand(List.of(lsArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(catCommand, pasteCommand, lsCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 25: cat uniq ls
    @Test
    void evaluate_CatUniqLs_CorrectEval() throws Exception {
        String[] catArgs = new String[]{APP_CAT, file1.getPath()};
        String[] uniqArgs = new String[]{APP_UNIQ};
        String[] lsArgs = new String[]{APP_LS};

        CallCommand catCommand = new CallCommand(List.of(catArgs), applicationRunner, argumentResolver);
        CallCommand uniqCommand = new CallCommand(List.of(uniqArgs), applicationRunner, argumentResolver);
        CallCommand lsCommand = new CallCommand(List.of(lsArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(catCommand, uniqCommand, lsCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 26: cat ls wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_CatLsWc_CorrectEval() throws Exception {
        String[] catArgs = new String[]{APP_CAT, file1.getPath()};
        String[] lsArgs = new String[]{APP_LS};
        String[] wcArgs = new String[]{APP_WC};

        Environment.currentDirectory = testDir.getPath();

        CallCommand catCommand = new CallCommand(List.of(catArgs), applicationRunner, argumentResolver);
        CallCommand lsCommand = new CallCommand(List.of(lsArgs), applicationRunner, argumentResolver);
        CallCommand wcCommand = new CallCommand(List.of(wcArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(catCommand, lsCommand, wcCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLsWc + System.lineSeparator(), stdout.toString());
    }

    // 27: cat ls cat
    @Test
    void evaluate_CatLsCat_CorrectEval() throws Exception {
        String[] catArgs1 = new String[]{APP_CAT, file1.getPath()};
        String[] lsArgs = new String[]{APP_LS};
        String[] catArgs2 = new String[]{APP_CAT};

        Environment.currentDirectory = testDir.getPath();

        CallCommand catCommand1 = new CallCommand(List.of(catArgs1), applicationRunner, argumentResolver);
        CallCommand lsCommand = new CallCommand(List.of(lsArgs), applicationRunner, argumentResolver);
        CallCommand catCommand2 = new CallCommand(List.of(catArgs2), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(catCommand1, lsCommand, catCommand2);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 28: cat wc grep
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_CatWcGrep_CorrectEval() throws Exception {
        String[] catArgs = new String[]{APP_CAT, file1.getPath()};
        String[] wcArgs = new String[]{APP_WC};
        String[] grepArgs = new String[]{APP_GREP, "[0-9]"};

        CallCommand catCommand = new CallCommand(List.of(catArgs), applicationRunner, argumentResolver);
        CallCommand wcCommand = new CallCommand(List.of(wcArgs), applicationRunner, argumentResolver);
        CallCommand grepCommand = new CallCommand(List.of(grepArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(catCommand, wcCommand, grepCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_WC + System.lineSeparator(), stdout.toString());
    }

    // 29: grep grep tee
    @Test
    void evaluate_GrepGrepTee_CorrectEval() throws Exception {
        String[] grepArgs1 = new String[]{APP_GREP, "CS", file2.getPath()};
        String[] grepArgs2 = new String[]{APP_GREP, KEYWORD_CS4218_C};
        String[] teeArgs = new String[]{APP_TEE};

        CallCommand grepCommand1 = new CallCommand(List.of(grepArgs1), applicationRunner, argumentResolver);
        CallCommand grepCommand2 = new CallCommand(List.of(grepArgs2), applicationRunner, argumentResolver);
        CallCommand teeCommand = new CallCommand(List.of(teeArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(grepCommand1, grepCommand2, teeCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_CS4218 + System.lineSeparator(), stdout.toString());
    }

    // 30: grep cut paste
    @Test
    void evaluate_GrepCutPaste_CorrectEval() throws Exception {
        String[] grepArgs = new String[]{APP_GREP, "CS", file2.getPath()};
        String[] cutArgs = new String[]{APP_CUT, "-c", "1-6"};
        String[] pasteArgs = new String[]{APP_PASTE};

        CallCommand grepCommand = new CallCommand(List.of(grepArgs), applicationRunner, argumentResolver);
        CallCommand cutCommand = new CallCommand(List.of(cutArgs), applicationRunner, argumentResolver);
        CallCommand pasteCommand = new CallCommand(List.of(pasteArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(grepCommand, cutCommand, pasteCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("CS3203" + System.lineSeparator()
                + KEYWORD_CS4218_C + System.lineSeparator()
                + "CS3235" + System.lineSeparator(), stdout.toString());
    }

    // 31: grep sort uniq
    @Test
    void evaluate_GrepSortUniq_CorrectEval() throws Exception {
        String[] grepArgs = new String[]{APP_GREP, "CS", file2.getPath()};
        String[] sortArgs = new String[]{APP_SORT};
        String[] uniqArgs = new String[]{APP_UNIQ};

        CallCommand grepCommand = new CallCommand(List.of(grepArgs), applicationRunner, argumentResolver);
        CallCommand sortCommand = new CallCommand(List.of(sortArgs), applicationRunner, argumentResolver);
        CallCommand uniqCommand = new CallCommand(List.of(uniqArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(grepCommand, sortCommand, uniqCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_CS3203 + System.lineSeparator()
                + KEYWORD_CS3235 + System.lineSeparator()
                + KEYWORD_CS4218 + System.lineSeparator(), stdout.toString());
    }

    // 32: grep tee ls
    @Test
    void evaluate_GrepTeeLs_CorrectEval() throws Exception {
        String[] grepArgs = new String[]{APP_GREP, "CS", file2.getPath()};
        String[] teeArgs = new String[]{APP_TEE};
        String[] lsArgs = new String[]{APP_LS};

        Environment.currentDirectory = testDir.getPath();

        CallCommand grepCommand = new CallCommand(List.of(grepArgs), applicationRunner, argumentResolver);
        CallCommand teeCommand = new CallCommand(List.of(teeArgs), applicationRunner, argumentResolver);
        CallCommand lsCommand = new CallCommand(List.of(lsArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(grepCommand, teeCommand, lsCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 33: grep paste ls
    @Test
    void evaluate_GrepPasteLs_CorrectEval() throws Exception {
        String[] grepArgs = new String[]{APP_GREP, "CS", file2.getPath()};
        String[] pasteArgs = new String[]{APP_PASTE};
        String[] lsArgs = new String[]{APP_LS};

        Environment.currentDirectory = testDir.getPath();

        CallCommand grepCommand = new CallCommand(List.of(grepArgs), applicationRunner, argumentResolver);
        CallCommand pasteCommand = new CallCommand(List.of(pasteArgs), applicationRunner, argumentResolver);
        CallCommand lsCommand = new CallCommand(List.of(lsArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(grepCommand, pasteCommand, lsCommand);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 34: grep uniq wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_GrepUniqWc_CorrectEval() throws Exception {
        String[] grepArgs = new String[]{APP_GREP, "CS", file2.getPath()};
        String[] uniqArgs = new String[]{APP_UNIQ};
        String[] wcArgs = new String[]{APP_WC};

        List<CallCommand> commandList = generate3PipeCommand(grepArgs, uniqArgs, wcArgs);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(expectedFileLine(94, 3, 13, null) + System.lineSeparator(), stdout.toString());
    }

    // 35: grep ls cat
    @Test
    void evaluate_GrepLsCat_CorrectEval() throws Exception {
        String[] grepArgs = new String[]{APP_GREP, "CS", file2.getPath()};
        String[] lsArgs = new String[]{APP_LS};
        String[] catArgs = new String[]{APP_CAT};

        Environment.currentDirectory = testDir.getPath();

        List<CallCommand> commandList = generate3PipeCommand(grepArgs, lsArgs, catArgs);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 36: grep ls grep
    @Test
    void evaluate_GrepLsGrep_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_GREP, "CS", file2.getPath()};
        String[] arg2 = new String[]{APP_LS};
        String[] arg3 = new String[]{APP_GREP, ".*"};

        Environment.currentDirectory = testDir.getPath();

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 37: grep wc cut
    @Test
    void evaluate_GrepWcCut_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_GREP, "CS", file2.getPath()};
        String[] arg2 = new String[]{APP_WC};
        String[] arg3 = new String[]{APP_CUT, "-c", "8"};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("3" + System.lineSeparator(), stdout.toString());
    }

    // 38: grep cat sort
    @Test
    void evaluate_GrepCatSort_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_GREP, GREP_ALL, file1.getPath()};
        String[] arg2 = new String[]{APP_CAT};
        String[] arg3 = new String[]{APP_SORT};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT_S + System.lineSeparator(), stdout.toString());
    }

    // 39: cut cut uniq
    @Test
    void evaluate_CutCutUniq_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", CUT_12, file4.getPath()};
        String[] arg2 = new String[]{APP_CUT, "-b", CUT_10000};
        String[] arg3 = new String[]{APP_UNIQ, "-d"};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("I " + System.lineSeparator() + "AA" + System.lineSeparator(), stdout.toString());
    }

    // 40: cut sort ls
    @Test
    void evaluate_CutSortLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", CUT_12, file2.getPath()};
        String[] arg2 = new String[]{APP_SORT};
        String[] arg3 = new String[]{APP_LS};

        Environment.currentDirectory = testDir.getPath();

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 41: cut tee ls
    @Test
    void evaluate_CutTeeLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", CUT_12, file2.getPath()};
        String[] arg2 = new String[]{APP_TEE};
        String[] arg3 = new String[]{APP_LS};

        Environment.currentDirectory = testDir.getPath();

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 42: cut paste wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_CutPasteWc_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", CUT_12, file2.getPath()};
        String[] arg2 = new String[]{APP_PASTE};
        String[] arg3 = new String[]{APP_WC};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(expectedFileLine(16, 4, 4, null) + System.lineSeparator(), stdout.toString());
    }

    // 43: cut uniq cat
    @Test
    void evaluate_CutUniqCat_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", CUT_12, file2.getPath()};
        String[] arg2 = new String[]{APP_UNIQ};
        String[] arg3 = new String[]{APP_CAT};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("CS" + System.lineSeparator() + "GE" + System.lineSeparator(), stdout.toString());
    }

    // 44: cut ls grep
    @Test
    void evaluate_CutLsGrep_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", CUT_12, file2.getPath()};
        String[] arg2 = new String[]{APP_LS};
        String[] arg3 = new String[]{APP_GREP, GREP_ALL};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 45: cut ls cut
    @Test
    void evaluate_CutLsCut_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", CUT_12, file2.getPath()};
        String[] arg2 = new String[]{APP_LS};
        String[] arg3 = new String[]{APP_CUT, "-c", "5"};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("1" + System.lineSeparator()
                + "2" + System.lineSeparator()
                + "3" + System.lineSeparator()
                + "4" + System.lineSeparator(), stdout.toString());
    }

    // 46: cut wc sort
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_CutWcSort_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", CUT_12, file2.getPath()};
        String[] arg2 = new String[]{APP_WC};
        String[] arg3 = new String[]{APP_SORT};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(expectedFileLine(16, 4, 4, null) + System.lineSeparator(), stdout.toString());
    }

    // 47: cut cat tee
    @Test
    void evaluate_CutCatTee_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", CUT_12, file2.getPath()};
        String[] arg2 = new String[]{APP_CAT};
        String[] arg3 = new String[]{APP_TEE};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("CS" + System.lineSeparator()
                + "CS" + System.lineSeparator()
                + "CS" + System.lineSeparator()
                + "GE" + System.lineSeparator(), stdout.toString());
    }

    // 48: cut grep paste
    @Test
    void evaluate_CutGrepPaste_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_CUT, "-c", "1-100", file2.getPath()};
        String[] arg2 = new String[]{APP_GREP, "CS|GES"};
        String[] arg3 = new String[]{APP_PASTE};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_2_CONTENT + System.lineSeparator(), stdout.toString());
    }

    // 49: sort sort ls
    @Test
    void evaluate_SortSortLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file1.getPath()};
        String[] arg2 = new String[]{APP_SORT};
        String[] arg3 = new String[]{APP_LS};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 50: sort tee wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_SortTeeWc_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file1.getPath()};
        String[] arg2 = new String[]{APP_TEE};
        String[] arg3 = new String[]{APP_WC};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_WC + System.lineSeparator(), stdout.toString());
    }

    // 51: sort paste cat
    @Test
    void evaluate_SortPasteCat_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file1.getPath()};
        String[] arg2 = new String[]{APP_PASTE};
        String[] arg3 = new String[]{APP_CAT};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT_S + System.lineSeparator(), stdout.toString());
    }

    // 52: sort uniq grep
    @Test
    void evaluate_SortUniqGrep_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file4.getPath()};
        String[] arg2 = new String[]{APP_UNIQ, "-d"};
        String[] arg3 = new String[]{APP_GREP, GREP_ALL};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQD_SORT + System.lineSeparator(), stdout.toString());
    }

    // 53: sort ls cut
    @Test
    void evaluate_SortLsCut_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file1.getPath()};
        String[] arg2 = new String[]{APP_LS};
        String[] arg3 = new String[]{APP_CUT, "-c", "5"};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("1" + System.lineSeparator()
                + "2" + System.lineSeparator()
                + "3" + System.lineSeparator()
                + "4" + System.lineSeparator(), stdout.toString());
    }

    // 54: sort ls sort
    @Test
    void evaluate_SortLsSort_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file1.getPath()};
        String[] arg2 = new String[]{APP_LS};
        String[] arg3 = new String[]{APP_SORT};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 55: sort wc tee
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_SortWcTee_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file1.getPath()};
        String[] arg2 = new String[]{APP_WC};
        String[] arg3 = new String[]{APP_TEE};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_WC + System.lineSeparator(), stdout.toString());
    }

    // 56: sort cat paste
    @Test
    void evaluate_SortCatPaste_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file1.getPath()};
        String[] arg2 = new String[]{APP_CAT};
        String[] arg3 = new String[]{APP_PASTE};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT_S + System.lineSeparator(), stdout.toString());
    }

    // 57: sort grep uniq
    @Test
    void evaluate_SortGrepUniq_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file2.getPath()};
        String[] arg2 = new String[]{APP_GREP, "CS3203|CS4218"};
        String[] arg3 = new String[]{APP_UNIQ};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_CS3203 + System.lineSeparator()
                + KEYWORD_CS4218 + System.lineSeparator(), stdout.toString());
    }

    // 58: sort cut ls
    @Test
    void evaluate_SortCutLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_SORT, file1.getPath()};
        String[] arg2 = new String[]{APP_CUT, "-c", "5"};
        String[] arg3 = new String[]{APP_LS};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 59: tee tee cat
    @Test
    void evaluate_TeeTeeCat_CorrectEval() throws Exception {
        String message = "Tee Test Hello";

        String[] arg1 = new String[]{APP_TEE, file1.getPath()};
        String[] arg2 = new String[]{APP_TEE, file2.getPath()};
        String[] arg3 = new String[]{APP_CAT, file1.getPath(), file2.getPath()};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);

        stdin = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(message + System.lineSeparator()
                + message + System.lineSeparator(), stdout.toString());
    }

    // 60: tee paste grep
    @Test
    void evaluate_TeePasteGrep_CorrectEval() throws Exception {
        String message = "Tee Test Hello";

        String[] arg1 = new String[]{APP_TEE, file1.getPath()};
        String[] arg2 = new String[]{APP_PASTE};
        String[] arg3 = new String[]{APP_GREP, "Tee"};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);

        stdin = new ByteArrayInputStream(message.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(message + System.lineSeparator(), stdout.toString());
    }

    // 61: tee uniq cut
    @Test
    void evaluate_TeeUniqCut_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_TEE, file4.getPath()};
        String[] arg2 = new String[]{APP_UNIQ};
        String[] arg3 = new String[]{APP_CUT, "-b", "1-100"};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);

        stdin = new ByteArrayInputStream(FILE_4_CONTENT.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQ + System.lineSeparator(), stdout.toString());
    }

    // 62: tee ls sort
    @Test
    void evaluate_TeeLsSort_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_TEE, file1.getPath()};
        String[] arg2 = new String[]{APP_LS};
        String[] arg3 = new String[]{APP_SORT};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);

        stdin = new ByteArrayInputStream(FILE_4_CONTENT.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 63: tee ls tee
    @Test
    void evaluate_TeeLsTee_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_TEE, file1.getPath()};
        String[] arg2 = new String[]{APP_LS};
        String[] arg3 = new String[]{APP_TEE};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);

        stdin = new ByteArrayInputStream(FILE_4_CONTENT.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 64: tee wc paste
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_TeeWcPaste_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_TEE, file1.getPath()};
        String[] arg2 = new String[]{APP_WC};
        String[] arg3 = new String[]{APP_PASTE};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);

        stdin = new ByteArrayInputStream(FILE_1_CONTENT.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_WC + System.lineSeparator(), stdout.toString());
    }

    // 65: tee cat uniq
    @Test
    void evaluate_TeeCatUniq_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_TEE, file1.getPath()}; // file1 wont be cared
        String[] arg2 = new String[]{APP_CAT};
        String[] arg3 = new String[]{APP_UNIQ};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);

        stdin = new ByteArrayInputStream(FILE_4_CONTENT.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQ + System.lineSeparator(), stdout.toString());
    }

    // 66: tee grep ls
    @Test
    void evaluate_TeeGrepLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_TEE, file1.getPath()};
        String[] arg2 = new String[]{APP_GREP, GREP_ALL};
        String[] arg3 = new String[]{APP_LS};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);

        stdin = new ByteArrayInputStream(FILE_4_CONTENT.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 67: tee cut ls
    @Test
    void evaluate_TeeCutLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_TEE, file1.getPath()};
        String[] arg2 = new String[]{APP_CUT, "-c", "5"};
        String[] arg3 = new String[]{APP_LS};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);

        stdin = new ByteArrayInputStream(FILE_4_CONTENT.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 68: tee sort wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_TeeSortWc_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_TEE};
        String[] arg2 = new String[]{APP_SORT};
        String[] arg3 = new String[]{APP_WC};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);

        stdin = new ByteArrayInputStream(FILE_1_CONTENT.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_WC + System.lineSeparator(), stdout.toString());
    }

    // 69: paste paste cut
    @Test
    void evaluate_PastePasteCut_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE};
        String[] arg2 = new String[]{APP_PASTE, "-", file2.getPath()};
        String[] arg3 = new String[]{APP_CUT, "-c", CUT_10000};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        stdin = new ByteArrayInputStream(FILE_1_CONTENT.getBytes(StandardCharsets.UTF_8));
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_PASTE_FILE2 + System.lineSeparator(), stdout.toString());
    }

    // 70: paste uniq sort
    @Test
    void evaluate_PasteUniqSort_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE, file4.getPath()};
        String[] arg2 = new String[]{APP_UNIQ, "-d"};
        String[] arg3 = new String[]{APP_SORT};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQD_SORT + System.lineSeparator(), stdout.toString());
    }

    // 71: paste ls tee
    @Test
    void evaluate_PasteLsTee_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE, file4.getPath()};
        String[] arg2 = new String[]{APP_LS};
        String[] arg3 = new String[]{APP_TEE};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 72: paste ls paste
    @Test
    void evaluate_PasteLsPaste_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE, file4.getPath()};
        String[] arg2 = new String[]{APP_LS};
        String[] arg3 = new String[]{APP_PASTE};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 73: paste wc uniq
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_PasteWcUniq_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE, file1.getPath()};
        String[] arg2 = new String[]{APP_WC};
        String[] arg3 = new String[]{APP_UNIQ};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_WC + System.lineSeparator(), stdout.toString());
    }

    // 74: paste cat ls
    @Test
    void evaluate_PasteCatLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE, file1.getPath()};
        String[] arg2 = new String[]{APP_CAT};
        String[] arg3 = new String[]{APP_LS};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 75: paste grep ls
    @Test
    void evaluate_PasteGrepLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE, file1.getPath()};
        String[] arg2 = new String[]{APP_GREP, GREP_ALL};
        String[] arg3 = new String[]{APP_LS};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 76: paste cut wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_PasteCutWc_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE, file1.getPath()};
        String[] arg2 = new String[]{APP_CUT, "-c", CUT_1000};
        String[] arg3 = new String[]{APP_WC};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_WC + System.lineSeparator(), stdout.toString());
    }

    // 77: paste sort cat
    @Test
    void evaluate_PasteSortCat_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE, file1.getPath()};
        String[] arg2 = new String[]{APP_SORT};
        String[] arg3 = new String[]{APP_CAT};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT_S + System.lineSeparator(), stdout.toString());
    }

    // 78: paste tee grep
    @Test
    void evaluate_PasteTeeGrep_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_PASTE, file2.getPath()};
        String[] arg2 = new String[]{APP_TEE};
        String[] arg3 = new String[]{APP_GREP, "CS"};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_CS3203 + System.lineSeparator()
                + KEYWORD_CS4218 + System.lineSeparator()
                + KEYWORD_CS3235 + System.lineSeparator(), stdout.toString());
    }

    // 79: uniq uniq tee
    @Test
    void evaluate_UniqUniqTee_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, "-d", file4.getPath()};
        String[] arg2 = new String[]{APP_UNIQ};
        String[] arg3 = new String[]{APP_TEE};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQ_D + System.lineSeparator(), stdout.toString());
    }

    // 80: uniq ls paste
    @Test
    void evaluate_UniqLsPaste_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, "-d", file4.getPath()};
        String[] arg2 = new String[]{APP_LS};
        String[] arg3 = new String[]{APP_PASTE};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 81: uniq ls uniq
    @Test
    void evaluate_UniqLsUniq_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, "-d", file4.getPath()};
        String[] arg2 = new String[]{APP_LS};
        String[] arg3 = new String[]{APP_UNIQ};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 82: uniq wc ls
    @Test
    void evaluate_UniqWcLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, "-d", file4.getPath()};
        String[] arg2 = new String[]{APP_WC};
        String[] arg3 = new String[]{APP_LS};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 83: uniq cat ls
    @Test
    void evaluate_UniqCatLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, "-d", file4.getPath()};
        String[] arg2 = new String[]{APP_CAT};
        String[] arg3 = new String[]{APP_LS};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 84: uniq grep wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_UniqGrepWc_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, "-d", file4.getPath()};
        String[] arg2 = new String[]{APP_GREP, KEYWORD_10_A};
        String[] arg3 = new String[]{APP_WC};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(expectedFileLine(12, 1, 1, null) + System.lineSeparator(), stdout.toString());
    }

    // 85: uniq cut cat
    @Test
    void evaluate_UniqCutCat_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, "-d", file4.getPath()};
        String[] arg2 = new String[]{APP_CUT, "-c", CUT_10000};
        String[] arg3 = new String[]{APP_CAT};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQ_D + System.lineSeparator(), stdout.toString());
    }

    // 86: uniq sort grep
    @Test
    void evaluate_UniqSortGrep_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, "-d", file4.getPath()};
        String[] arg2 = new String[]{APP_SORT};
        String[] arg3 = new String[]{APP_GREP, KEYWORD_10_A};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_10_A + System.lineSeparator(), stdout.toString());
    }

    // 87: uniq tee cut
    @Test
    void evaluate_UniqTeeCut_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, "-d", file4.getPath()};
        String[] arg2 = new String[]{APP_TEE};
        String[] arg3 = new String[]{APP_CUT, "-c", "1"};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("I" + System.lineSeparator()
                + "A" + System.lineSeparator(), stdout.toString());
    }

    // 88: uniq paste sort
    @Test
    void evaluate_UniqPasteSort_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_UNIQ, "-d", file4.getPath()};
        String[] arg2 = new String[]{APP_TEE};
        String[] arg3 = new String[]{APP_SORT};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQD_SORT + System.lineSeparator(), stdout.toString());
    }

    // 89: echo ls uniq
    @Test
    void evaluate_EchoLsUniq_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_ECHO, FILE_1_CONTENT};
        String[] arg2 = new String[]{APP_LS};
        String[] arg3 = new String[]{APP_UNIQ};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 90: echo wc ls
    @Test
    void evaluate_EchoWcLs_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_ECHO, FILE_1_CONTENT};
        String[] arg2 = new String[]{APP_WC};
        String[] arg3 = new String[]{APP_LS};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(tempDirLs + System.lineSeparator(), stdout.toString());
    }

    // 91: echo cat wc
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_EchoCatWc_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_ECHO, FILE_1_CONTENT};
        String[] arg2 = new String[]{APP_CAT};
        String[] arg3 = new String[]{APP_WC};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_WC + System.lineSeparator(), stdout.toString());
    }

    // 92: echo grep cat
    @Test
    void evaluate_EchoGrepCat_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_ECHO, FILE_2_CONTENT};
        String[] arg2 = new String[]{APP_GREP, "GES"};
        String[] arg3 = new String[]{APP_CAT};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(KEYWORD_GES1035 + System.lineSeparator(), stdout.toString());
    }

    // 93: echo cut grep
    @Test
    void evaluate_EchoCutGrep_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_ECHO, FILE_2_CONTENT};
        String[] arg2 = new String[]{APP_CUT, "-c", CUT_12};
        String[] arg3 = new String[]{APP_GREP, "CS"};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals("CS" + System.lineSeparator()
                + "CS" + System.lineSeparator()
                + "CS" + System.lineSeparator(), stdout.toString());
    }

    // 94: echo sort cut
    @Test
    void evaluate_EchoSortCut_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_ECHO, FILE_1_CONTENT};
        String[] arg2 = new String[]{APP_SORT};
        String[] arg3 = new String[]{APP_CUT, "-b", CUT_1000};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT_S + System.lineSeparator(), stdout.toString());
    }

    // 95: echo tee sort
    @Test
    void evaluate_EchoTeeSort_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_ECHO, FILE_1_CONTENT};
        String[] arg2 = new String[]{APP_TEE};
        String[] arg3 = new String[]{APP_SORT};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_1_CONTENT_S + System.lineSeparator(), stdout.toString());
    }

    // 96: echo paste tee
    @Test
    void evaluate_EchoPasteTee_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_ECHO, FILE_1_CONTENT};
        String[] arg2 = new String[]{APP_PASTE, "-", file2.getPath()};
        String[] arg3 = new String[]{APP_TEE};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE1_PASTE_FILE2 + System.lineSeparator(), stdout.toString());
    }

    // 97: echo uniq paste
    @Test
    void evaluate_EchoUniqPaste_CorrectEval() throws Exception {
        String[] arg1 = new String[]{APP_ECHO, FILE_4_CONTENT};
        String[] arg2 = new String[]{APP_UNIQ, "-d"};
        String[] arg3 = new String[]{APP_PASTE};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);
        pipeCommand.evaluate(stdin, stdout);

        assertEquals(FILE_4_UNIQ_D + System.lineSeparator(), stdout.toString());
    }

    // pipe to incorrect command
    @Test
    void evaluate_PipeUnknownFirstApp_ThrowsException() {
        String invalidAppName = "iamunknown";
        String[] arg1 = new String[]{invalidAppName};
        String[] arg2 = new String[]{APP_UNIQ, "-d"};
        String[] arg3 = new String[]{APP_LS};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);

        ShellException shellException = assertThrows(ShellException.class, () -> pipeCommand.evaluate(stdin, stdout));
        assertTrue(shellException.getMessage().contains(invalidAppName));
        assertTrue(shellException.getMessage().contains(ERR_INVALID_APP));
    }

    @Test
    void evaluate_PipeUnknownSecondApp_ThrowsException() {
        String invalidAppName = "catttttt";
        String[] arg1 = new String[]{APP_CAT, file1.getPath()};
        String[] arg2 = new String[]{invalidAppName};
        String[] arg3 = new String[]{APP_ECHO};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);

        ShellException shellException = assertThrows(ShellException.class, () -> pipeCommand.evaluate(stdin, stdout));
        assertTrue(shellException.getMessage().contains(invalidAppName));
        assertTrue(shellException.getMessage().contains(ERR_INVALID_APP));
    }

    @Test
    void evaluate_PipeUnknownThirdApp_ThrowsException() {
        String invalidAppName = "WC";
        String[] arg1 = new String[]{APP_CAT, file1.getPath()};
        String[] arg2 = new String[]{APP_WC};
        String[] arg3 = new String[]{invalidAppName};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);

        ShellException shellException = assertThrows(ShellException.class, () -> pipeCommand.evaluate(stdin, stdout));
        assertTrue(shellException.getMessage().contains(invalidAppName));
        assertTrue(shellException.getMessage().contains(ERR_INVALID_APP));
    }

    // If an exception occurred in any of these parts, the exception is thrown, and the rest of the parts are terminated.
    @Test
    @DisabledOnOs({OS.MAC, OS.LINUX})
    void evaluate_PipeUnknownFlagFirstApp_ThrowsExceptionAndReturn() {
        String[] arg1 = new String[]{APP_PASTE, "-e", "test"};
        String[] arg2 = new String[]{APP_UNIQ, "-d"};
        String[] arg3 = new String[]{APP_ECHO, KEYWORD_HEH};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);

        PasteException pasteException = assertThrows(PasteException.class, () -> pipeCommand.evaluate(stdin, stdout));
        assertTrue(pasteException.getMessage().contains("d"));
        assertTrue(pasteException.getMessage().contains(ERR_INVALID_FLAG));
    }

    @Test
    void evaluate_PipeUnknownFlagSecondApp_ThrowsExceptionAndReturn() {
        String[] arg1 = new String[]{APP_ECHO, "test"};
        String[] arg2 = new String[]{APP_CAT, "-d", KEYWORD_HEH};
        String[] arg3 = new String[]{APP_ECHO, KEYWORD_HEH};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);

        CatException catException = assertThrows(CatException.class, () -> pipeCommand.evaluate(stdin, stdout));
        assertTrue(catException.getMessage().contains("d"));
        assertTrue(catException.getMessage().contains(ERR_INVALID_FLAG));
    }

    @Test
    void evaluate_PipeUnknownFlagThirdApp_ThrowsExceptionAndReturn() {
        String[] arg1 = new String[]{APP_ECHO, "test"};
        String[] arg2 = new String[]{APP_CAT, file1.getPath()};
        String[] arg3 = new String[]{APP_UNIQ, "-z", KEYWORD_HEH};

        List<CallCommand> commandList = generate3PipeCommand(arg1, arg2, arg3);
        pipeCommand = new PipeCommand(commandList);

        UniqException uniqException = assertThrows(UniqException.class, () -> pipeCommand.evaluate(stdin, stdout));
        assertTrue(uniqException.getMessage().contains("z"));
        assertTrue(uniqException.getMessage().contains(ERR_INVALID_FLAG));
    }

    // failed test cases
    @Test
    void evaluate_NoStdin_ThrowsException() {
        String[] echoArgs = new String[]{APP_ECHO, "hello world"};
        CallCommand echoCommand = new CallCommand(List.of(echoArgs), applicationRunner, argumentResolver);

        String[] catArgs = new String[]{APP_CAT};
        CallCommand catCommand = new CallCommand(List.of(catArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(echoCommand, catCommand);

        pipeCommand = new PipeCommand(commandList);

        ShellException shellException = assertThrows(ShellException.class, () -> pipeCommand.evaluate(null, stdout));
        assertTrue(shellException.getMessage().contains(ERR_NO_ISTREAM));
    }

    @Test
    void evaluate_NoStdout_ThrowsException() {
        String[] echoArgs = new String[]{APP_ECHO, "hello world"};
        CallCommand echoCommand = new CallCommand(List.of(echoArgs), applicationRunner, argumentResolver);

        String[] catArgs = new String[]{APP_CAT};
        CallCommand catCommand = new CallCommand(List.of(catArgs), applicationRunner, argumentResolver);

        List<CallCommand> commandList = Arrays.asList(echoCommand, catCommand);

        pipeCommand = new PipeCommand(commandList);

        ShellException shellException = assertThrows(ShellException.class, () -> pipeCommand.evaluate(stdin, null));
        assertTrue(shellException.getMessage().contains(ERR_NO_OSTREAM));
    }
}
