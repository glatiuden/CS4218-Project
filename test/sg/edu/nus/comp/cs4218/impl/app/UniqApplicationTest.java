package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.impl.parser.UniqArgsParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static sg.edu.nus.comp.cs4218.impl.app.UniqApplication.ERR_C_CAP_D;
import static sg.edu.nus.comp.cs4218.impl.app.UniqApplication.ERR_EXTRA_FILE;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_DIR_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_PERM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.*;

class UniqApplicationTest {
    private final static String STDIN_DASH = "-";
    private final static String COUNT_FLAG = "-c";
    private final static String REPEAT_FLAG = "-d";
    private final static String ALL_REPEAT_FLAG = "-D";
    private final static String DUP_FILE = "d.txt";
    private final static String FAKE_DUP_FILE = "fd.txt";
    private final static String LAST_UNIQ_FILE = "lu.txt";
    private final static String ALL_UNIQ_FILE = "au.txt";
    private final static String SINGLE_FILE = "s.txt";
    private final static String EMPTY_FILE = "e.txt";
    private final static String OUT_FILE = "o.txt";
    private final static String NE_OUT_FILE = "neo.txt";
    private final static String NEST_DIR = "nest";
    private final static String NE_FILE = "ne.txt";
    private final static String NO_PERM_FILE = "np.txt";
    private final static String NE_DIR = "ne";
    private final static String EMPTY = "";
    private final static String NORM_NEWLINE = "\n";
    private final static String SINGLE_CONT = "single\n";
    private final static String DUP_CONT = "duplicate\nduplicate\nno duplicate\nmore dup\nmore dup\nmoredup\nno more\n123 123\n123 123\n123 123\nevenmore\nevenmore\nevenmore\nevenmore\n";
    private final static String FAKE_DUP_CONT = "dup licate\ndup licate\n!@#\n!@#\n!@#\nno dup\ndup\ndup\nlast dup\nlast dup";
    private final static String LAST_UNIQ_CONT = "dup !icate\ndup !icate\ndup licate\ndup licate\ndup\ndup\ndup\nno dup\n";
    private final static String ALL_UNIQ_CONT = "all uniq\nuniq\nall uniq\n uniq\ndup?\nno dup";
    private final static String SINGLE = "single";
    private final static String C_SINGLE = "\t1 single";
    private final static String DUP = "duplicate\nno duplicate\nmore dup\nmoredup\nno more\n123 123\nevenmore".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String C_DUP = "\t2 duplicate\n\t1 no duplicate\n\t2 more dup\n\t1 moredup\n\t1 no more\n\t3 123 123\n\t4 evenmore".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String D_DUP = "duplicate\nmore dup\n123 123\nevenmore".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String CAP_D_DUP = "duplicate\nduplicate\nmore dup\nmore dup\n123 123\n123 123\n123 123\nevenmore\nevenmore\nevenmore\nevenmore".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String CD_DUP = "\t2 duplicate\n\t2 more dup\n\t3 123 123\n\t4 evenmore".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String FAKE_DUP = "dup licate\n!@#\nno dup\ndup\nlast dup".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String C_FAKE_DUP = "\t2 dup licate\n\t3 !@#\n\t1 no dup\n\t2 dup\n\t2 last dup".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String D_FAKE_DUP = "dup licate\n!@#\ndup\nlast dup".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String CAP_D_FAKE_DUP = "dup licate\ndup licate\n!@#\n!@#\n!@#\ndup\ndup\nlast dup\nlast dup".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String CD_FAKE_DUP = "\t2 dup licate\n\t3 !@#\n\t2 dup\n\t2 last dup".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String LAST_UNIQ = "dup !icate\ndup licate\ndup\nno dup".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String C_LAST_UNIQ = "\t2 dup !icate\n\t2 dup licate\n\t3 dup\n\t1 no dup".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String D_LAST_UNIQ = "dup !icate\ndup licate\ndup".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String CAP_D_LAST_UNIQ = "dup !icate\ndup !icate\ndup licate\ndup licate\ndup\ndup\ndup".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String CD_LAST_UNIQ = "\t2 dup !icate\n\t2 dup licate\n\t3 dup".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String ALL_UNIQ = "all uniq\nuniq\nall uniq\n uniq\ndup?\nno dup".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String C_ALL_UNIQ = "\t1 all uniq\n\t1 uniq\n\t1 all uniq\n\t1  uniq\n\t1 dup?\n\t1 no dup".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String UNIQ_EXCEP = "uniq: ";
    @TempDir
    public static Path folderPath;
    private static Path dupPath;
    private static Path fakeDupPath;
    private static Path lastUniqPath;
    private static Path allUniqPath;
    private static Path singlePath;
    private static Path emptyPath;
    private static Path outPath;
    private static Path neOutPath;
    private static Path dirFilePath;
    private static Path dirOutFilePath;
    private static Path dirPath;
    private static Path nePath;
    private static Path neDirPath;
    private static Path noPermPath;
    private static UniqApplication uniqApp;
    private static UniqArgsParser uniqParser;
    private static ByteArrayOutputStream testOutputStream;
    private static ByteArrayInputStream testStdin;

    @BeforeAll
    public static void setUp() throws IOException {
        uniqApp = new UniqApplication();
        uniqParser = mock(UniqArgsParser.class);
        uniqApp.setUniqParser(uniqParser);

        testOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOutputStream));
        uniqApp.setStdout(testOutputStream);

        dupPath = folderPath.resolve(DUP_FILE);
        fakeDupPath = folderPath.resolve(FAKE_DUP_FILE);
        lastUniqPath = folderPath.resolve(LAST_UNIQ_FILE);
        allUniqPath = folderPath.resolve(ALL_UNIQ_FILE);
        singlePath = folderPath.resolve(SINGLE_FILE);
        emptyPath = folderPath.resolve(EMPTY_FILE);
        outPath = folderPath.resolve(OUT_FILE);
        neOutPath = folderPath.resolve(NE_OUT_FILE);
        dirFilePath = folderPath.resolve(NEST_DIR).resolve(DUP_FILE);
        dirOutFilePath = folderPath.resolve(NEST_DIR).resolve(OUT_FILE);
        dirPath = folderPath.resolve(NEST_DIR);
        nePath = folderPath.resolve(NE_FILE);
        neDirPath = folderPath.resolve(NE_DIR).resolve(DUP_FILE);
        noPermPath = folderPath.resolve(NO_PERM_FILE);

        // ./nest
        Files.createDirectories(dirPath);
        // ./d.txt
        Files.write(dupPath, DUP_CONT.getBytes());
        // ./fd.txt
        Files.write(fakeDupPath, FAKE_DUP_CONT.getBytes());
        // ./lu.txt
        Files.write(lastUniqPath, LAST_UNIQ_CONT.getBytes());
        // ./au.txt
        Files.write(allUniqPath, ALL_UNIQ_CONT.getBytes());
        // ./s.txt
        Files.write(singlePath, SINGLE_CONT.getBytes());
        // ./e.txt
        Files.createFile(emptyPath);
        // ./o.txt
        Files.write(outPath, "something written".getBytes());
        // ./nest/d.txt
        Files.write(dirFilePath, DUP_CONT.getBytes());
        // ./nest/o.txt
        Files.write(dirOutFilePath, "already written".getBytes());

        // ./np.txt no read/write permissions file
        Files.write(noPermPath, "no permissions".getBytes());
        removeFilePermissions(noPermPath);
    }

    @AfterAll
    public static void tearDown() {
        resetFilePermissions(noPermPath);
    }

    @AfterEach
    public void resetStreams() throws IOException {
        uniqApp.setStdout(testOutputStream);
        testOutputStream.reset();
        if (testStdin != null) {
            testStdin.close();
        }
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input: given, output: given
    @Test
    public void uniqFromFile_TTFFileFile_TTFFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(true, true, false, dupPath.toString(), outPath.toString());
        assertEquals(CD_DUP, getFileContent(outPath));
        assertEquals(CD_DUP, actualResult);
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input: given, output: not given
    @Test
    public void uniqFromFile_TTFFileStdout_TTFOutput() throws Exception {
        String actualResult = uniqApp.uniqFromFile(true, true, false, dupPath.toString(), EMPTY);
        assertEquals(CD_DUP + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CD_DUP, actualResult);
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input: file in directory, output: given
    @Test
    public void uniqFromFile_TTFFileDirFile_TTFFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(true, true, false, dirFilePath.toString(), outPath.toString());
        assertEquals(CD_DUP, getFileContent(outPath));
        assertEquals(CD_DUP, actualResult);
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input: given, output: file in directory
    @Test
    public void uniqFromFile_TTFFileFile_TTFFileDir() throws Exception {
        String actualResult = uniqApp.uniqFromFile(true, true, false, dirFilePath.toString(), dirOutFilePath.toString());
        assertEquals(CD_DUP, getFileContent(dirOutFilePath));
        assertEquals(CD_DUP, actualResult);
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input: given, output: non-exist file
    @Test
    public void uniqFromFile_TTFFileNonExistFile_TTFFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(true, true, false, dirFilePath.toString(), neOutPath.toString());
        assertEquals(CD_DUP, getFileContent(neOutPath));
        assertEquals(CD_DUP, actualResult);
        deleteFileIfExists(neOutPath);
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input: empty content, output: given
    @Test
    public void uniqFromFile_TTFEmptyFileFile_emptyFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(true, true, false, emptyPath.toString(), outPath.toString());
        assertEquals(EMPTY, getFileContent(outPath));
        assertEquals(EMPTY, actualResult);
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input: single line, output: given
    // single line, -cd option, no duplicate to output
    @Test
    public void uniqFromFile_TTFSingleLineFile_emptyFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(true, true, false, singlePath.toString(), outPath.toString());
        assertEquals(EMPTY, getFileContent(outPath));
        assertEquals(EMPTY, actualResult);
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input: "fake" duplicate, output: given
    @Test
    public void uniqFromFile_TTFFakeDupFile_TTFFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(true, true, false, fakeDupPath.toString(), outPath.toString());
        assertEquals(CD_FAKE_DUP, getFileContent(outPath));
        assertEquals(CD_FAKE_DUP, actualResult);
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input: last uniq, output: given
    @Test
    public void uniqFromFile_TTFLastUniqFile_TTFFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(true, true, false, lastUniqPath.toString(), outPath.toString());
        assertEquals(CD_LAST_UNIQ, getFileContent(outPath));
        assertEquals(CD_LAST_UNIQ, actualResult);
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input: all uniq, output: given
    @Test
    public void uniqFromFile_TTFAllUniqFile_emptyFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(true, true, false, allUniqPath.toString(), outPath.toString());
        assertEquals(EMPTY, getFileContent(outPath));
        assertEquals(EMPTY, actualResult);
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, input: given, output: given
    @Test
    public void uniqFromFile_TFFFileFile_TFFFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(true, false, false, dupPath.toString(), outPath.toString());
        assertEquals(C_DUP, getFileContent(outPath));
        assertEquals(C_DUP, actualResult);
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, input: given, output: not given
    @Test
    public void uniqFromFile_TFFFileStdout_TFFOutput() throws Exception {
        String actualResult = uniqApp.uniqFromFile(true, false, false, dupPath.toString(), EMPTY);
        assertEquals(C_DUP + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(C_DUP, actualResult);
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, input: single line, output: given
    @Test
    public void uniqFromFile_TFFSingleLineFile_TFFFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(true, false, false, singlePath.toString(), outPath.toString());
        assertEquals(C_SINGLE, getFileContent(outPath));
        assertEquals(C_SINGLE, actualResult);
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, input: "fake" duplicate, output: given
    @Test
    public void uniqFromFile_TFFFakeDupFile_TFFFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(true, false, false, fakeDupPath.toString(), outPath.toString());
        assertEquals(C_FAKE_DUP, getFileContent(outPath));
        assertEquals(C_FAKE_DUP, actualResult);
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, input: last uniq, output: given
    @Test
    public void uniqFromFile_TFFLastUniqFile_TFFFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(true, false, false, lastUniqPath.toString(), outPath.toString());
        assertEquals(C_LAST_UNIQ, getFileContent(outPath));
        assertEquals(C_LAST_UNIQ, actualResult);
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, input: all uniq, output: given
    @Test
    public void uniqFromFile_TFFAllUniqFile_TFFFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(true, false, false, allUniqPath.toString(), outPath.toString());
        assertEquals(C_ALL_UNIQ, getFileContent(outPath));
        assertEquals(C_ALL_UNIQ, actualResult);
    }

    // if -dD flag, only D flag result, according to document and linux
    // isCount: false, isRepeated: true, isAllRepeated: true, input: given, output: given
    @Test
    public void uniqFromFile_FTTFileFile_FTTFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, true, true, dupPath.toString(), outPath.toString());
        assertEquals(CAP_D_DUP, getFileContent(outPath));
        assertEquals(CAP_D_DUP, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, input: given, output: not given
    @Test
    public void uniqFromFile_FTTFileStdout_FTTOutput() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, true, true, dupPath.toString(), EMPTY);
        assertEquals(CAP_D_DUP + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CAP_D_DUP, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, input: single line, output: given
    // single line, -dD option, no duplicate to output
    @Test
    public void uniqFromFile_FTTSingleLineFile_emptyFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, true, true, singlePath.toString(), outPath.toString());
        assertEquals(EMPTY, getFileContent(outPath));
        assertEquals(EMPTY, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, input: "fake" duplicate, output: given
    @Test
    public void uniqFromFile_FTTFakeDupFile_FTTFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, true, true, fakeDupPath.toString(), outPath.toString());
        assertEquals(CAP_D_FAKE_DUP, getFileContent(outPath));
        assertEquals(CAP_D_FAKE_DUP, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, input: last uniq, output: given
    @Test
    public void uniqFromFile_FTTLastUniqFile_FTTFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, true, true, lastUniqPath.toString(), outPath.toString());
        assertEquals(CAP_D_LAST_UNIQ, getFileContent(outPath));
        assertEquals(CAP_D_LAST_UNIQ, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, input: all uniq, output: given
    @Test
    public void uniqFromFile_FTTAllUniqFile_emptyFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, true, true, allUniqPath.toString(), outPath.toString());
        assertEquals(EMPTY, getFileContent(outPath));
        assertEquals(EMPTY, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, input: given, output: given
    @Test
    public void uniqFromFile_FTFFileFile_FTFFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, true, false, dupPath.toString(), outPath.toString());
        assertEquals(D_DUP, getFileContent(outPath));
        assertEquals(D_DUP, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, input: given, output: not given
    @Test
    public void uniqFromFile_FTFFileStdout_FTFOutput() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, true, false, dupPath.toString(), EMPTY);
        assertEquals(D_DUP + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(D_DUP, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, input: single line, output: given
    // single line, -d option, no duplicate to output
    @Test
    public void uniqFromFile_FTFSingleLineFile_emptyFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, true, false, singlePath.toString(), outPath.toString());
        assertEquals(EMPTY, getFileContent(outPath));
        assertEquals(EMPTY, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, input: "fake" duplicate, output: given
    @Test
    public void uniqFromFile_FTFFakeDupFile_FTFFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, true, false, fakeDupPath.toString(), outPath.toString());
        assertEquals(D_FAKE_DUP, getFileContent(outPath));
        assertEquals(D_FAKE_DUP, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, input: last uniq, output: given
    @Test
    public void uniqFromFile_FTFLastUniqFile_FTFFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, true, false, lastUniqPath.toString(), outPath.toString());
        assertEquals(D_LAST_UNIQ, getFileContent(outPath));
        assertEquals(D_LAST_UNIQ, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, input: all uniq, output: given
    @Test
    public void uniqFromFile_FTFAllUniqFile_emptyFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, true, false, allUniqPath.toString(), outPath.toString());
        assertEquals(EMPTY, getFileContent(outPath));
        assertEquals(EMPTY, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, input: given, output: given
    @Test
    public void uniqFromFile_FFTFileFile_FFTFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, false, true, dupPath.toString(), outPath.toString());
        assertEquals(CAP_D_DUP, getFileContent(outPath));
        assertEquals(CAP_D_DUP, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, input: given, output: not given
    @Test
    public void uniqFromFile_FFTFileStdout_FFTOutput() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, false, true, dupPath.toString(), EMPTY);
        assertEquals(CAP_D_DUP + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CAP_D_DUP, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, input: single line, output: given
    // single line, -D option, no duplicate to output
    @Test
    public void uniqFromFile_FFTSingleLineFile_emptyFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, false, true, singlePath.toString(), outPath.toString());
        assertEquals(EMPTY, getFileContent(outPath));
        assertEquals(EMPTY, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, input: "fake" duplicate, output: given
    @Test
    public void uniqFromFile_FFTFakeDupFile_FFTFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, false, true, fakeDupPath.toString(), outPath.toString());
        assertEquals(CAP_D_FAKE_DUP, getFileContent(outPath));
        assertEquals(CAP_D_FAKE_DUP, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, input: last uniq, output: given
    @Test
    public void uniqFromFile_FFTLastUniqFile_FFTFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, false, true, lastUniqPath.toString(), outPath.toString());
        assertEquals(CAP_D_LAST_UNIQ, getFileContent(outPath));
        assertEquals(CAP_D_LAST_UNIQ, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, input: all uniq, output: given
    @Test
    public void uniqFromFile_FFTAllUniqFile_emptyFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, false, true, allUniqPath.toString(), outPath.toString());
        assertEquals(EMPTY, getFileContent(outPath));
        assertEquals(EMPTY, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: given, output: given
    @Test
    public void uniqFromFile_FFFFileFile_FFFFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, false, false, dupPath.toString(), outPath.toString());
        assertEquals(DUP, getFileContent(outPath));
        assertEquals(DUP, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: given, output: not given
    @Test
    public void uniqFromFile_FFFFileStdout_FFFOutput() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, false, false, dupPath.toString(), EMPTY);
        assertEquals(DUP + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(DUP, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: single line, output: given
    @Test
    public void uniqFromFile_FFFSingleLineFile_FFFFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, false, false, singlePath.toString(), outPath.toString());
        assertEquals(SINGLE, getFileContent(outPath));
        assertEquals(SINGLE, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: "fake" duplicate, output: given
    @Test
    public void uniqFromFile_FFFFakeDupFile_FFFFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, false, false, fakeDupPath.toString(), outPath.toString());
        assertEquals(FAKE_DUP, getFileContent(outPath));
        assertEquals(FAKE_DUP, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: last uniq, output: given
    @Test
    public void uniqFromFile_FFFLastUniqFile_FFFFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, false, false, lastUniqPath.toString(), outPath.toString());
        assertEquals(LAST_UNIQ, getFileContent(outPath));
        assertEquals(LAST_UNIQ, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: all uniq, output: given
    @Test
    public void uniqFromFile_FFFAllUniqFile_FFFFile() throws Exception {
        String actualResult = uniqApp.uniqFromFile(false, false, false, allUniqPath.toString(), outPath.toString());
        assertEquals(ALL_UNIQ, getFileContent(outPath));
        assertEquals(ALL_UNIQ, actualResult);
    }

    // isCount: true, isRepeated: false, isAllRepeated: true, input: given, output: given
    @Test
    public void uniqFromFile_TFTFileFile_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> uniqApp.uniqFromFile(true, false, true, dupPath.toString(), outPath.toString()));
        assertEquals(ERR_C_CAP_D, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: null, output: given
    @Test
    public void uniqFromFile_FFFNullFile_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> uniqApp.uniqFromFile(false, false, false, null, outPath.toString()));
        assertEquals(ERR_NULL_ARGS, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: given, output: null
    @Test
    public void uniqFromFile_FFFFileNull_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> uniqApp.uniqFromFile(false, false, false, dupPath.toString(), null));
        assertEquals(ERR_NULL_ARGS, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: non-existent file, output: given
    @Test
    public void uniqFromFile_FFFNonExistFileFile_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> uniqApp.uniqFromFile(false, false, false, nePath.toString(), outPath.toString()));
        assertEquals(nePath + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: directory, output: given
    @Test
    public void uniqFromFile_FFFDirFile_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> uniqApp.uniqFromFile(false, false, false, dirPath.toString(), outPath.toString()));
        assertEquals(dirPath + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: non-existent directory, output: given
    @Test
    public void uniqFromFile_FFFNonExistDirFile_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> uniqApp.uniqFromFile(false, false, false, neDirPath.toString(), outPath.toString()));
        assertEquals(neDirPath + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: given, output: directory
    @Test
    public void uniqFromFile_FFFFileDir_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> uniqApp.uniqFromFile(false, false, false, dupPath.toString(), dirPath.toString()));
        assertEquals(dirPath + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: given, output: non-existent directory
    @Test
    public void uniqFromFile_FFFFileNonExistDir_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> uniqApp.uniqFromFile(false, false, false, dupPath.toString(), neDirPath.toString()));
        assertEquals(neDirPath + ": " + ERR_DIR_NOT_FOUND, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: given, output: no permission
    @Test
    public void uniqFromFile_FFFFileNoPerm_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> uniqApp.uniqFromFile(false, false, false, dupPath.toString(), noPermPath.toString()));
        assertEquals(noPermPath + ": " + ERR_NO_PERM, exception.getMessage());
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, stdin: given, output: given
    @Test
    public void uniqFromStdin_TTFStdinFile_TTFFile() throws Exception {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(true, true, false, testStdin, outPath.toString());
        assertEquals(CD_DUP, getFileContent(outPath));
        assertEquals(CD_DUP, actualResult);
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, stdin: given, output: not given
    @Test
    public void uniqFromStdin_TTFStdinStdout_TTFOutput() throws Exception {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(true, true, false, testStdin, EMPTY);
        assertEquals(CD_DUP + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CD_DUP, actualResult);
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, stdin: given, output: file in directory
    @Test
    public void uniqFromStdin_TTFStdinFile_TTFFileDir() throws Exception {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(true, true, false, testStdin, dirOutFilePath.toString());
        assertEquals(CD_DUP, getFileContent(dirOutFilePath));
        assertEquals(CD_DUP, actualResult);
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, stdin: given, output: non-exist file
    @Test
    public void uniqFromStdin_TTFStdinNonExistFile_TTFFile() throws Exception {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(true, true, false, testStdin, neOutPath.toString());
        assertEquals(CD_DUP, getFileContent(neOutPath));
        assertEquals(CD_DUP, actualResult);
        deleteFileIfExists(neOutPath);
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, stdin: empty content, output: given
    @Test
    public void uniqFromStdin_TTFEmptyFile_emptyFile() throws Exception {
        testStdin = new ByteArrayInputStream(STRING_NEWLINE.getBytes());
        String actualResult = uniqApp.uniqFromStdin(true, true, false, testStdin, outPath.toString());
        assertEquals(EMPTY, getFileContent(outPath));
        assertEquals(EMPTY, actualResult);
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, stdin: single line, output: given
    // single line, -cd option, no duplicate to output
    @Test
    public void uniqFromStdin_TTFSingleLineFile_emptyFile() throws Exception {
        testStdin = new ByteArrayInputStream(SINGLE_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(true, true, false, testStdin, outPath.toString());
        assertEquals(EMPTY, getFileContent(outPath));
        assertEquals(EMPTY, actualResult);
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, stdin: "fake" duplicate, output: given
    @Test
    public void uniqFromStdin_TTFFakeDupFile_TTFFile() throws Exception {
        testStdin = new ByteArrayInputStream(FAKE_DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(true, true, false, testStdin, outPath.toString());
        assertEquals(CD_FAKE_DUP, getFileContent(outPath));
        assertEquals(CD_FAKE_DUP, actualResult);
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, stdin: last uniq, output: given
    @Test
    public void uniqFromStdin_TTFLastUniqFile_TTFFile() throws Exception {
        testStdin = new ByteArrayInputStream(LAST_UNIQ_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(true, true, false, testStdin, outPath.toString());
        assertEquals(CD_LAST_UNIQ, getFileContent(outPath));
        assertEquals(CD_LAST_UNIQ, actualResult);
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, stdin: all uniq, output: given
    @Test
    public void uniqFromStdin_TTFAllUniqFile_emptyFile() throws Exception {
        testStdin = new ByteArrayInputStream(ALL_UNIQ_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(true, true, false, testStdin, outPath.toString());
        assertEquals(EMPTY, getFileContent(outPath));
        assertEquals(EMPTY, actualResult);
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, stdin: given, output: given
    @Test
    public void uniqFromStdin_TFFStdinFile_TFFFile() throws Exception {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(true, false, false, testStdin, outPath.toString());
        assertEquals(C_DUP, getFileContent(outPath));
        assertEquals(C_DUP, actualResult);
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, stdin: given, output: not given
    @Test
    public void uniqFromStdin_TFFStdinStdout_TFFOutput() throws Exception {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(true, false, false, testStdin, EMPTY);
        assertEquals(C_DUP + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(C_DUP, actualResult);
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, stdin: single line, output: given
    @Test
    public void uniqFromStdin_TFFSingleLineFile_TFFFile() throws Exception {
        testStdin = new ByteArrayInputStream(SINGLE_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(true, false, false, testStdin, outPath.toString());
        assertEquals(C_SINGLE, getFileContent(outPath));
        assertEquals(C_SINGLE, actualResult);
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, stdin: "fake" duplicate, output: given
    @Test
    public void uniqFromStdin_TFFFakeDupFile_TFFFile() throws Exception {
        testStdin = new ByteArrayInputStream(FAKE_DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(true, false, false, testStdin, outPath.toString());
        assertEquals(C_FAKE_DUP, getFileContent(outPath));
        assertEquals(C_FAKE_DUP, actualResult);
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, stdin: last uniq, output: given
    @Test
    public void uniqFromStdin_TFFLastUniqFile_TFFFile() throws Exception {
        testStdin = new ByteArrayInputStream(LAST_UNIQ_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(true, false, false, testStdin, outPath.toString());
        assertEquals(C_LAST_UNIQ, getFileContent(outPath));
        assertEquals(C_LAST_UNIQ, actualResult);
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, stdin: all uniq, output: given
    @Test
    public void uniqFromStdin_TFFAllUniqFile_TFFFile() throws Exception {
        testStdin = new ByteArrayInputStream(ALL_UNIQ_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(true, false, false, testStdin, outPath.toString());
        assertEquals(C_ALL_UNIQ, getFileContent(outPath));
        assertEquals(C_ALL_UNIQ, actualResult);
    }

    // if -dD flag, only D flag result, according to document and linux
    // isCount: false, isRepeated: true, isAllRepeated: true, stdin: given, output: given
    @Test
    public void uniqFromStdin_FTTStdinFile_FTTFile() throws Exception {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, true, true, testStdin, outPath.toString());
        assertEquals(CAP_D_DUP, getFileContent(outPath));
        assertEquals(CAP_D_DUP, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, stdin: given, output: not given
    @Test
    public void uniqFromStdin_FTTStdinStdout_FTTOutput() throws Exception {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, true, true, testStdin, EMPTY);
        assertEquals(CAP_D_DUP + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CAP_D_DUP, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, stdin: single line, output: given
    // single line, -dD option, no duplicate to output
    @Test
    public void uniqFromStdin_FTTSingleLineFile_emptyFile() throws Exception {
        testStdin = new ByteArrayInputStream(SINGLE_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, true, true, testStdin, outPath.toString());
        assertEquals(EMPTY, getFileContent(outPath));
        assertEquals(EMPTY, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, stdin: "fake" duplicate, output: given
    @Test
    public void uniqFromStdin_FTTFakeDupFile_FTTFile() throws Exception {
        testStdin = new ByteArrayInputStream(FAKE_DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, true, true, testStdin, outPath.toString());
        assertEquals(CAP_D_FAKE_DUP, getFileContent(outPath));
        assertEquals(CAP_D_FAKE_DUP, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, stdin: last uniq, output: given
    @Test
    public void uniqFromStdin_FTTLastUniqFile_FTTFile() throws Exception {
        testStdin = new ByteArrayInputStream(LAST_UNIQ_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, true, true, testStdin, outPath.toString());
        assertEquals(CAP_D_LAST_UNIQ, getFileContent(outPath));
        assertEquals(CAP_D_LAST_UNIQ, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, stdin: all uniq, output: given
    @Test
    public void uniqFromStdin_FTTAllUniqFile_emptyFile() throws Exception {
        testStdin = new ByteArrayInputStream(ALL_UNIQ.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, true, true, testStdin, outPath.toString());
        assertEquals(EMPTY, getFileContent(outPath));
        assertEquals(EMPTY, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, stdin: given, output: given
    @Test
    public void uniqFromStdin_FTFStdinFile_FTFFile() throws Exception {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, true, false, testStdin, outPath.toString());
        assertEquals(D_DUP, getFileContent(outPath));
        assertEquals(D_DUP, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, stdin: given, output: not given
    @Test
    public void uniqFromStdin_FTFStdinStdout_FTFOutput() throws Exception {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, true, false, testStdin, EMPTY);
        assertEquals(D_DUP + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(D_DUP, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, stdin: single line, output: given
    // single line, -d option, no duplicate to output
    @Test
    public void uniqFromStdin_FTFSingleLineFile_emptyFile() throws Exception {
        testStdin = new ByteArrayInputStream(SINGLE.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, true, false, testStdin, outPath.toString());
        assertEquals(EMPTY, getFileContent(outPath));
        assertEquals(EMPTY, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, stdin: "fake" duplicate, output: given
    @Test
    public void uniqFromStdin_FTFFakeDupFile_FTFFile() throws Exception {
        testStdin = new ByteArrayInputStream(FAKE_DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, true, false, testStdin, outPath.toString());
        assertEquals(D_FAKE_DUP, getFileContent(outPath));
        assertEquals(D_FAKE_DUP, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, stdin: last uniq, output: given
    @Test
    public void uniqFromStdin_FTFLastUniqFile_FTFFile() throws Exception {
        testStdin = new ByteArrayInputStream(LAST_UNIQ_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, true, false, testStdin, outPath.toString());
        assertEquals(D_LAST_UNIQ, getFileContent(outPath));
        assertEquals(D_LAST_UNIQ, actualResult);
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, stdin: all uniq, output: given
    @Test
    public void uniqFromStdin_FTFAllUniqFile_emptyFile() throws Exception {
        testStdin = new ByteArrayInputStream(ALL_UNIQ_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, true, false, testStdin, outPath.toString());
        assertEquals(EMPTY, getFileContent(outPath));
        assertEquals(EMPTY, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, stdin: given, output: given
    @Test
    public void uniqFromStdin_FFTStdinFile_FFTFile() throws Exception {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, false, true, testStdin, outPath.toString());
        assertEquals(CAP_D_DUP, getFileContent(outPath));
        assertEquals(CAP_D_DUP, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, stdin: given, output: not given
    @Test
    public void uniqFromStdin_FFTStdinStdout_FFTOutput() throws Exception {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, false, true, testStdin, EMPTY);
        assertEquals(CAP_D_DUP + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(CAP_D_DUP, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, stdin: single line, output: given
    // single line, -D option, no duplicate to output
    @Test
    public void uniqFromStdin_FFTSingleLineFile_emptyFile() throws Exception {
        testStdin = new ByteArrayInputStream(SINGLE_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, false, true, testStdin, outPath.toString());
        assertEquals(EMPTY, getFileContent(outPath));
        assertEquals(EMPTY, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, stdin: "fake" duplicate, output: given
    @Test
    public void uniqFromStdin_FFTFakeDupFile_FFTFile() throws Exception {
        testStdin = new ByteArrayInputStream(FAKE_DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, false, true, testStdin, outPath.toString());
        assertEquals(CAP_D_FAKE_DUP, getFileContent(outPath));
        assertEquals(CAP_D_FAKE_DUP, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, stdin: last uniq, output: given
    @Test
    public void uniqFromStdin_FFTLastUniqFile_FFTFile() throws Exception {
        testStdin = new ByteArrayInputStream(LAST_UNIQ_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, false, true, testStdin, outPath.toString());
        assertEquals(CAP_D_LAST_UNIQ, getFileContent(outPath));
        assertEquals(CAP_D_LAST_UNIQ, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, stdin: all uniq, output: given
    @Test
    public void uniqFromStdin_FFTAllUniqFile_emptyFile() throws Exception {
        testStdin = new ByteArrayInputStream(ALL_UNIQ_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, false, true, testStdin, outPath.toString());
        assertEquals(EMPTY, getFileContent(outPath));
        assertEquals(EMPTY, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, stdin: given, output: given
    @Test
    public void uniqFromStdin_FFFStdinFile_FFFFile() throws Exception {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, false, false, testStdin, outPath.toString());
        assertEquals(DUP, getFileContent(outPath));
        assertEquals(DUP, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, stdin: given, output: not given
    @Test
    public void uniqFromStdin_FFFStdinStdout_FFFOutput() throws Exception {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, false, false, testStdin, EMPTY);
        assertEquals(DUP + STRING_NEWLINE, testOutputStream.toString());
        assertEquals(DUP, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, stdin: single line, output: given
    @Test
    public void uniqFromStdin_FFFSingleLineFile_FFFFile() throws Exception {
        testStdin = new ByteArrayInputStream(SINGLE_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, false, false, testStdin, outPath.toString());
        assertEquals(SINGLE, getFileContent(outPath));
        assertEquals(SINGLE, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, stdin: "fake" duplicate, output: given
    @Test
    public void uniqFromStdin_FFFFakeDupFile_FFFFile() throws Exception {
        testStdin = new ByteArrayInputStream(FAKE_DUP_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, false, false, testStdin, outPath.toString());
        assertEquals(FAKE_DUP, getFileContent(outPath));
        assertEquals(FAKE_DUP, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, stdin: last uniq, output: given
    @Test
    public void uniqFromStdin_FFFLastUniqFile_FFFFile() throws Exception {
        testStdin = new ByteArrayInputStream(LAST_UNIQ_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, false, false, testStdin, outPath.toString());
        assertEquals(LAST_UNIQ, getFileContent(outPath));
        assertEquals(LAST_UNIQ, actualResult);
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, stdin: all uniq, output: given
    @Test
    public void uniqFromStdin_FFFAllUniqFile_FFFFile() throws Exception {
        testStdin = new ByteArrayInputStream(ALL_UNIQ_CONT.getBytes());
        String actualResult = uniqApp.uniqFromStdin(false, false, false, testStdin, outPath.toString());
        assertEquals(ALL_UNIQ, getFileContent(outPath));
        assertEquals(ALL_UNIQ, actualResult);
    }

    // isCount: true, isRepeated: false, isAllRepeated: true, stdin: given, output: given
    @Test
    public void uniqFromStdin_TFTStdinFile_throwsException() {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> uniqApp.uniqFromStdin(true, false, true, testStdin, outPath.toString()));
        assertEquals(ERR_C_CAP_D, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, stdin: null, output: given
    @Test
    public void uniqFromStdin_FFFNullFile_throwsException() {
        Exception exception = assertThrows(Exception.class, () -> uniqApp.uniqFromStdin(false, false, false, null, outPath.toString()));
        assertEquals(ERR_NULL_STREAMS, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, stdin: given, output: null
    @Test
    public void uniqFromStdin_FFFStdinNull_throwsException() {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> uniqApp.uniqFromStdin(false, false, false, testStdin, null));
        assertEquals(ERR_NULL_ARGS, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, stdin: given, output: directory
    @Test
    public void uniqFromStdin_FFFStdinDir_throwsException() {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> uniqApp.uniqFromStdin(false, false, false, testStdin, dirPath.toString()));
        assertEquals(dirPath + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, stdin: given, output: non-existent directory
    @Test
    public void uniqFromStdin_FFFStdinNonExistDir_throwsException() {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> uniqApp.uniqFromStdin(false, false, false, testStdin, neDirPath.toString()));
        assertEquals(neDirPath + ": " + ERR_DIR_NOT_FOUND, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, stdin: given, output: no permission
    @Test
    public void uniqFromStdin_FFFStdinNoPerm_throwsException() {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        Exception exception = assertThrows(Exception.class, () -> uniqApp.uniqFromStdin(false, false, false, testStdin, noPermPath.toString()));
        assertEquals(noPermPath + ": " + ERR_NO_PERM, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: not given (no "-"), output: not given
    @Test
    public void run_FFFNoDashStdout_FFFOutput() throws Exception {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        String[] args = {};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList());
        when(uniqParser.isCount()).thenReturn(false);
        when(uniqParser.isRepeated()).thenReturn(false);
        when(uniqParser.isAllRepeated()).thenReturn(false);

        uniqApp.run(args, testStdin, testOutputStream);
        assertEquals(DUP + STRING_NEWLINE, testOutputStream.toString());
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, input: stdin ("-"), output: not given
    @Test
    public void run_FFFDashStdout_FFFOutput() throws Exception {
        testStdin = new ByteArrayInputStream(FAKE_DUP_CONT.getBytes());
        String[] args = {ALL_REPEAT_FLAG, STDIN_DASH};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList(STDIN_DASH));
        when(uniqParser.isCount()).thenReturn(false);
        when(uniqParser.isRepeated()).thenReturn(false);
        when(uniqParser.isAllRepeated()).thenReturn(true);

        uniqApp.run(args, testStdin, testOutputStream);
        assertEquals(CAP_D_FAKE_DUP + STRING_NEWLINE, testOutputStream.toString());
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, input: stdin ("-"), output: given
    @Test
    public void run_FTFDashFile_FTFFile() throws Exception {
        testStdin = new ByteArrayInputStream(LAST_UNIQ_CONT.getBytes());
        String[] args = {REPEAT_FLAG, STDIN_DASH, outPath.toString()};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList(STDIN_DASH, outPath.toString()));
        when(uniqParser.isCount()).thenReturn(false);
        when(uniqParser.isRepeated()).thenReturn(true);
        when(uniqParser.isAllRepeated()).thenReturn(false);

        uniqApp.run(args, testStdin, testOutputStream);
        assertEquals(D_LAST_UNIQ, getFileContent(outPath));
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, input: file, output: not given
    @Test
    public void run_FTTFileStdout_FTTOutput() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {REPEAT_FLAG, ALL_REPEAT_FLAG, dupPath.toString()};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList(dupPath.toString()));
        when(uniqParser.isCount()).thenReturn(false);
        when(uniqParser.isRepeated()).thenReturn(true);
        when(uniqParser.isAllRepeated()).thenReturn(true);

        uniqApp.run(args, testStdin, testOutputStream);
        assertEquals(CAP_D_DUP + STRING_NEWLINE, testOutputStream.toString());
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, input: file, output: given
    @Test
    public void run_TFFFileFile_TFFFile() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {COUNT_FLAG, singlePath.toString(), outPath.toString()};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList(singlePath.toString(), outPath.toString()));
        when(uniqParser.isCount()).thenReturn(true);
        when(uniqParser.isRepeated()).thenReturn(false);
        when(uniqParser.isAllRepeated()).thenReturn(false);

        uniqApp.run(args, testStdin, testOutputStream);
        assertEquals(C_SINGLE, getFileContent(outPath));
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input: file, output: given
    @Test
    public void run_TTFFileFile_TTFFile() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {COUNT_FLAG, REPEAT_FLAG, fakeDupPath.toString(), outPath.toString()};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList(fakeDupPath.toString(), outPath.toString()));
        when(uniqParser.isCount()).thenReturn(true);
        when(uniqParser.isRepeated()).thenReturn(true);
        when(uniqParser.isAllRepeated()).thenReturn(false);

        uniqApp.run(args, testStdin, testOutputStream);
        assertEquals(CD_FAKE_DUP, getFileContent(outPath));
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input: file in directory, output: given
    @Test
    public void run_TTFFileDirFile_TTFFile() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {COUNT_FLAG, REPEAT_FLAG, dirFilePath.toString(), outPath.toString()};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList(dirFilePath.toString(), outPath.toString()));
        when(uniqParser.isCount()).thenReturn(true);
        when(uniqParser.isRepeated()).thenReturn(true);
        when(uniqParser.isAllRepeated()).thenReturn(false);

        uniqApp.run(args, testStdin, testOutputStream);
        assertEquals(CD_DUP, getFileContent(outPath));
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input: file, output: file in directory
    @Test
    public void run_TTFFileFileDir_TTFFile() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {COUNT_FLAG, REPEAT_FLAG, fakeDupPath.toString(), dirOutFilePath.toString()};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList(fakeDupPath.toString(), dirOutFilePath.toString()));
        when(uniqParser.isCount()).thenReturn(true);
        when(uniqParser.isRepeated()).thenReturn(true);
        when(uniqParser.isAllRepeated()).thenReturn(false);

        uniqApp.run(args, testStdin, testOutputStream);
        assertEquals(CD_FAKE_DUP, getFileContent(dirOutFilePath));
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input: file, output: non-existent file
    @Test
    public void run_TTFFileNonExistFile_TTFFile() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {COUNT_FLAG, REPEAT_FLAG, lastUniqPath.toString(), neOutPath.toString()};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList(lastUniqPath.toString(), neOutPath.toString()));
        when(uniqParser.isCount()).thenReturn(true);
        when(uniqParser.isRepeated()).thenReturn(true);
        when(uniqParser.isAllRepeated()).thenReturn(false);

        uniqApp.run(args, testStdin, testOutputStream);
        assertEquals(CD_LAST_UNIQ, getFileContent(neOutPath));
        deleteFileIfExists(neOutPath);
    }

    // isCount: true, isRepeated: true, isAllRepeated: true, input: file, output: given
    @Test
    public void run_TTTFileFile_throwsUniqException() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {COUNT_FLAG, REPEAT_FLAG, ALL_REPEAT_FLAG, dupPath.toString(), outPath.toString()};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList(dupPath.toString(), outPath.toString()));
        when(uniqParser.isCount()).thenReturn(true);
        when(uniqParser.isRepeated()).thenReturn(true);
        when(uniqParser.isAllRepeated()).thenReturn(true);

        Exception exception = assertThrows(UniqException.class, () -> uniqApp.run(args, testStdin, testOutputStream));
        assertEquals(UNIQ_EXCEP + ERR_C_CAP_D, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: directory, output: given
    @Test
    public void run_FFFDirFile_throwsUniqException() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {dirPath.toString(), outPath.toString()};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList(dirPath.toString(), outPath.toString()));
        when(uniqParser.isCount()).thenReturn(false);
        when(uniqParser.isRepeated()).thenReturn(false);
        when(uniqParser.isAllRepeated()).thenReturn(false);

        Exception exception = assertThrows(UniqException.class, () -> uniqApp.run(args, testStdin, testOutputStream));
        assertEquals(UNIQ_EXCEP + dirPath + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: file, output: directory
    @Test
    public void run_FFFFileDir_throwsUniqException() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {dupPath.toString(), dirPath.toString()};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList(dupPath.toString(), dirPath.toString()));
        when(uniqParser.isCount()).thenReturn(false);
        when(uniqParser.isRepeated()).thenReturn(false);
        when(uniqParser.isAllRepeated()).thenReturn(false);

        Exception exception = assertThrows(UniqException.class, () -> uniqApp.run(args, testStdin, testOutputStream));
        assertEquals(UNIQ_EXCEP + dirPath + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: non-existent file, output: given
    @Test
    public void run_FFFNonExistFileFile_throwsUniqException() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {nePath.toString(), outPath.toString()};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList(nePath.toString(), outPath.toString()));
        when(uniqParser.isCount()).thenReturn(false);
        when(uniqParser.isRepeated()).thenReturn(false);
        when(uniqParser.isAllRepeated()).thenReturn(false);

        Exception exception = assertThrows(UniqException.class, () -> uniqApp.run(args, testStdin, testOutputStream));
        assertEquals(UNIQ_EXCEP + nePath + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: non-existent directory, output: given
    @Test
    public void run_FFFNonExistDirFile_throwsUniqException() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {neDirPath.toString(), outPath.toString()};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList(neDirPath.toString(), outPath.toString()));
        when(uniqParser.isCount()).thenReturn(false);
        when(uniqParser.isRepeated()).thenReturn(false);
        when(uniqParser.isAllRepeated()).thenReturn(false);

        Exception exception = assertThrows(UniqException.class, () -> uniqApp.run(args, testStdin, testOutputStream));
        assertEquals(UNIQ_EXCEP + neDirPath + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: file, output: non-existent directory
    @Test
    public void run_FFFFileNonExistDir_throwsUniqException() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {dupPath.toString(), neDirPath.toString()};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList(dupPath.toString(), neDirPath.toString()));
        when(uniqParser.isCount()).thenReturn(false);
        when(uniqParser.isRepeated()).thenReturn(false);
        when(uniqParser.isAllRepeated()).thenReturn(false);

        Exception exception = assertThrows(UniqException.class, () -> uniqApp.run(args, testStdin, testOutputStream));
        assertEquals(UNIQ_EXCEP + neDirPath + ": " + ERR_DIR_NOT_FOUND, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: null stdin, output: given
    @Test
    public void run_FFFNullFile_throwsUniqException() throws Exception {
        String[] args = {STDIN_DASH, outPath.toString()};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList(STDIN_DASH, outPath.toString()));
        when(uniqParser.isCount()).thenReturn(false);
        when(uniqParser.isRepeated()).thenReturn(false);
        when(uniqParser.isAllRepeated()).thenReturn(false);

        Exception exception = assertThrows(UniqException.class, () -> uniqApp.run(args, null, testOutputStream));
        assertEquals(UNIQ_EXCEP + ERR_NULL_STREAMS, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: file, output: null stdout
    @Test
    public void run_FFFFileNull_throwsUniqException() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {dupPath.toString(), outPath.toString()};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList(dupPath.toString(), outPath.toString()));
        when(uniqParser.isCount()).thenReturn(false);
        when(uniqParser.isRepeated()).thenReturn(false);
        when(uniqParser.isAllRepeated()).thenReturn(false);

        Exception exception = assertThrows(UniqException.class, () -> uniqApp.run(args, testStdin, null));
        assertEquals(UNIQ_EXCEP + ERR_NULL_STREAMS, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: file, output: no permission
    @Test
    public void run_FFFFileNoPerm_throwsUniqException() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {dupPath.toString(), noPermPath.toString()};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList(dupPath.toString(), noPermPath.toString()));
        when(uniqParser.isCount()).thenReturn(false);
        when(uniqParser.isRepeated()).thenReturn(false);
        when(uniqParser.isAllRepeated()).thenReturn(false);

        Exception exception = assertThrows(UniqException.class, () -> uniqApp.run(args, testStdin, testOutputStream));
        assertEquals(UNIQ_EXCEP + noPermPath + ": " + ERR_NO_PERM, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, > 2 files
    @Test
    public void run_FFFExtraFile_throwsUniqException() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {dupPath.toString(), outPath.toString(), dirFilePath.toString()};

        doNothing().when(uniqParser).parse(args);
        when(uniqParser.getFiles()).thenReturn(Arrays.asList(dupPath.toString(), outPath.toString(), dirFilePath.toString()));
        when(uniqParser.isCount()).thenReturn(false);
        when(uniqParser.isRepeated()).thenReturn(false);
        when(uniqParser.isAllRepeated()).thenReturn(false);

        Exception exception = assertThrows(UniqException.class, () -> uniqApp.run(args, testStdin, testOutputStream));
        assertEquals(UNIQ_EXCEP + ERR_EXTRA_FILE + "'" + dirFilePath + "'", exception.getMessage());
    }
}
