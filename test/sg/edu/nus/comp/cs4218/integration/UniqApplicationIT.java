package sg.edu.nus.comp.cs4218.integration;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.UniqException;
import sg.edu.nus.comp.cs4218.impl.app.UniqApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.app.UniqApplication.ERR_C_CAP_D;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_DIR_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FILE_NOT_FOUND;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.deleteFileIfExists;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.getFileContent;

public class UniqApplicationIT {
    private final static String NORM_NEWLINE = "\n";
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
    private final static String NE_DIR = "ne";
    private final static String EMPTY = "";
    private final static String SINGLE_CONT = "single\n";
    private final static String DUP_CONT = "duplicate\nduplicate\nno duplicate\nmore dup\nmore dup\nmoredup\nno more\n123 123\n123 123\n123 123\nevenmore\nevenmore\nevenmore\nevenmore\n";
    private final static String FAKE_DUP_CONT = "dup licate\ndup licate\n!@#\n!@#\n!@#\nno dup\ndup\ndup\nlast dup\nlast dup";
    private final static String LAST_UNIQ_CONT = "dup !icate\ndup !icate\ndup licate\ndup licate\ndup\ndup\ndup\nno dup\n";
    private final static String ALL_UNIQ_CONT = "all uniq\nuniq\nall uniq\n uniq\ndup?\nno dup";
    private final static String C_SINGLE = "\t1 single";
    private final static String DUP = "duplicate\nno duplicate\nmore dup\nmoredup\nno more\n123 123\nevenmore".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String CAP_D_DUP = "duplicate\nduplicate\nmore dup\nmore dup\n123 123\n123 123\n123 123\nevenmore\nevenmore\nevenmore\nevenmore".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String CD_DUP = "\t2 duplicate\n\t2 more dup\n\t3 123 123\n\t4 evenmore".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String CAP_D_FAKE_DUP = "dup licate\ndup licate\n!@#\n!@#\n!@#\ndup\ndup\nlast dup\nlast dup".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String CD_FAKE_DUP = "\t2 dup licate\n\t3 !@#\n\t2 dup\n\t2 last dup".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String D_LAST_UNIQ = "dup !icate\ndup licate\ndup".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
    private final static String CD_LAST_UNIQ = "\t2 dup !icate\n\t2 dup licate\n\t3 dup".replaceAll(NORM_NEWLINE, STRING_NEWLINE);
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
    private static UniqApplication uniqApp;
    private static ByteArrayOutputStream testOutputStream;
    private static ByteArrayInputStream testStdin;

    @BeforeAll
    public static void setUpAll() throws IOException {
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
    }

    @BeforeEach
    public void setUpEach() {
        uniqApp = new UniqApplication();
        testOutputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(testOutputStream));
    }

    @AfterEach
    public void resetStreams() throws IOException {
        if (testStdin != null) {
            testStdin.close();
        }
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: not given (no "-"), output: not given
    @Test
    public void run_FFFNoDashStdout_FFFOutput() throws Exception {
        testStdin = new ByteArrayInputStream(DUP_CONT.getBytes());
        String[] args = {};
        uniqApp.run(args, testStdin, testOutputStream);
        assertEquals(DUP + STRING_NEWLINE, testOutputStream.toString());
    }

    // isCount: false, isRepeated: false, isAllRepeated: true, input: stdin ("-"), output: not given
    @Test
    public void run_FFFDashStdout_FFFOutput() throws Exception {
        testStdin = new ByteArrayInputStream(FAKE_DUP_CONT.getBytes());
        String[] args = {ALL_REPEAT_FLAG, STDIN_DASH};
        uniqApp.run(args, testStdin, testOutputStream);
        assertEquals(CAP_D_FAKE_DUP + STRING_NEWLINE, testOutputStream.toString());
    }

    // isCount: false, isRepeated: true, isAllRepeated: false, input: stdin ("-"), output: given
    @Test
    public void run_FTFDashFile_FTFFile() throws Exception {
        testStdin = new ByteArrayInputStream(LAST_UNIQ_CONT.getBytes());
        String[] args = {REPEAT_FLAG, STDIN_DASH, outPath.toString()};
        uniqApp.run(args, testStdin, testOutputStream);
        assertEquals(D_LAST_UNIQ, getFileContent(outPath));
    }

    // isCount: false, isRepeated: true, isAllRepeated: true, input: file, output: not given
    @Test
    public void run_FTTFileStdout_FTTOutput() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {REPEAT_FLAG, ALL_REPEAT_FLAG, dupPath.toString()};
        uniqApp.run(args, testStdin, testOutputStream);
        assertEquals(CAP_D_DUP + STRING_NEWLINE, testOutputStream.toString());
    }

    // isCount: true, isRepeated: false, isAllRepeated: false, input: file, output: given
    @Test
    public void run_TFFFileFile_TFFFile() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {COUNT_FLAG, singlePath.toString(), outPath.toString()};
        uniqApp.run(args, testStdin, testOutputStream);
        assertEquals(C_SINGLE, getFileContent(outPath));
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input: file, output: given
    @Test
    public void run_TTFFileFile_TTFFile() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {COUNT_FLAG, REPEAT_FLAG, fakeDupPath.toString(), outPath.toString()};
        uniqApp.run(args, testStdin, testOutputStream);
        assertEquals(CD_FAKE_DUP, getFileContent(outPath));
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input: file in directory, output: given
    @Test
    public void run_TTFFileDirFile_TTFFile() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {COUNT_FLAG, REPEAT_FLAG, dirFilePath.toString(), outPath.toString()};
        uniqApp.run(args, testStdin, testOutputStream);
        assertEquals(CD_DUP, getFileContent(outPath));
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input: file, output: file in directory
    @Test
    public void run_TTFFileFileDir_TTFFile() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {COUNT_FLAG, REPEAT_FLAG, fakeDupPath.toString(), dirOutFilePath.toString()};
        uniqApp.run(args, testStdin, testOutputStream);
        assertEquals(CD_FAKE_DUP, getFileContent(dirOutFilePath));
    }

    // isCount: true, isRepeated: true, isAllRepeated: false, input: file, output: non-existent file
    @Test
    public void run_TTFFileNonExistFile_TTFFile() throws Exception {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {COUNT_FLAG, REPEAT_FLAG, lastUniqPath.toString(), neOutPath.toString()};
        uniqApp.run(args, testStdin, testOutputStream);
        assertEquals(CD_LAST_UNIQ, getFileContent(neOutPath));
        deleteFileIfExists(neOutPath);
    }

    // isCount: true, isRepeated: true, isAllRepeated: true, input: file, output: given
    @Test
    public void run_TTTFileFile_throwsUniqException() {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {COUNT_FLAG, REPEAT_FLAG, ALL_REPEAT_FLAG, dupPath.toString(), outPath.toString()};
        Exception exception = assertThrows(UniqException.class, () -> uniqApp.run(args, testStdin, testOutputStream));
        assertEquals(UNIQ_EXCEP + ERR_C_CAP_D, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: directory, output: given
    @Test
    public void run_FFFDirFile_throwsUniqException() {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {dirPath.toString(), outPath.toString()};
        Exception exception = assertThrows(UniqException.class, () -> uniqApp.run(args, testStdin, testOutputStream));
        assertEquals(UNIQ_EXCEP + dirPath + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: file, output: directory
    @Test
    public void run_FFFFileDir_throwsUniqException() {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {dupPath.toString(), dirPath.toString()};
        Exception exception = assertThrows(UniqException.class, () -> uniqApp.run(args, testStdin, testOutputStream));
        assertEquals(UNIQ_EXCEP + dirPath + ": " + ERR_IS_DIR, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: non-existent file, output: given
    @Test
    public void run_FFFNonExistFileFile_throwsUniqException() {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {nePath.toString(), outPath.toString()};
        Exception exception = assertThrows(UniqException.class, () -> uniqApp.run(args, testStdin, testOutputStream));
        assertEquals(UNIQ_EXCEP + nePath + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: non-existent directory, output: given
    @Test
    public void run_FFFNonExistDirFile_throwsUniqException() {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {neDirPath.toString(), outPath.toString()};
        Exception exception = assertThrows(UniqException.class, () -> uniqApp.run(args, testStdin, testOutputStream));
        assertEquals(UNIQ_EXCEP + neDirPath + ": " + ERR_FILE_NOT_FOUND, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: file, output: non-existent directory
    @Test
    public void run_FFFFileNonExistDir_throwsUniqException() {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {dupPath.toString(), neDirPath.toString()};
        Exception exception = assertThrows(UniqException.class, () -> uniqApp.run(args, testStdin, testOutputStream));
        assertEquals(UNIQ_EXCEP + neDirPath + ": " + ERR_DIR_NOT_FOUND, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: null stdin, output: given
    @Test
    public void run_FFFNullFile_throwsUniqException() {
        String[] args = {STDIN_DASH, outPath.toString()};
        Exception exception = assertThrows(UniqException.class, () -> uniqApp.run(args, null, testOutputStream));
        assertEquals(UNIQ_EXCEP + ERR_NULL_STREAMS, exception.getMessage());
    }

    // isCount: false, isRepeated: false, isAllRepeated: false, input: file, output: null stdout
    @Test
    public void run_FFFFileNull_throwsUniqException() {
        testStdin = new ByteArrayInputStream(EMPTY.getBytes());
        String[] args = {dupPath.toString(), outPath.toString()};
        Exception exception = assertThrows(UniqException.class, () -> uniqApp.run(args, testStdin, null));
        assertEquals(UNIQ_EXCEP + ERR_NULL_STREAMS, exception.getMessage());
    }
}
