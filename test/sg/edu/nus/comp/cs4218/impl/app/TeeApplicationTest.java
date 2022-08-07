package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.parser.TeeArgsParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.*;

public class TeeApplicationTest {
    private final static String BASE_TEXT = "New file...";
    private final static String OVERWRITTEN_TEXT = "Overwritten...";
    private final static String ECHO_TEXT = "Echoing...";
    private final static String EXIST_FILE_A = "a.txt";
    private final static String EXIST_FILE_B = "b.txt";
    private final static String NON_EXIST_FILE_C = "c.txt";
    private final static String NON_EXIST_FILE_D = "d.txt";
    private final static String FOLDER = "DIRECTORY";
    private final static String EXIST_DIR_FILE_E = "e.txt";
    private final static String EXIST_DIR_FILE_F = "f.txt";
    private final static String NONEXIST_DIRFILE = "g.txt";
    private final static String ANY_TXT_FILE = "*.txt";
    @TempDir
    public static Path folderPath;
    private static TeeApplication tee;
    private static TeeArgsParser parser;
    private static ByteArrayInputStream inputCapture;
    private static ByteArrayOutputStream outputCapture;

    @BeforeAll
    public static void setup() throws IOException {
        tee = new TeeApplication();

        outputCapture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputCapture));

        Files.createDirectories(folderPath);
        Files.write(folderPath.resolve(EXIST_FILE_A), BASE_TEXT.getBytes());
        Files.write(folderPath.resolve(EXIST_FILE_B), BASE_TEXT.getBytes());
        Files.createDirectories(folderPath.resolve(FOLDER));
        Files.write(folderPath.resolve(FOLDER).resolve(EXIST_DIR_FILE_E), BASE_TEXT.getBytes());
        Files.write(folderPath.resolve(FOLDER).resolve(EXIST_DIR_FILE_F), BASE_TEXT.getBytes());
    }

    @BeforeEach
    public void start() throws InvalidArgsException {
        parser = mock(TeeArgsParser.class);
        tee.setArgsParser(parser);
        inputCapture = new ByteArrayInputStream(OVERWRITTEN_TEXT.getBytes());
        tee.setupReader(inputCapture);
    }

    @AfterEach
    public void cleanUp() throws IOException {
        inputCapture.close();
        outputCapture.reset();
        System.setOut(new PrintStream(outputCapture));
    }

    private boolean hasOutputToFiles(String str, Path... paths) throws IOException {
        for (Path path : paths) {
            if (!Files.exists(path) || !getFileContent(path).equals(str)) {
                return false;
            }
        }
        return true;
    }

    private ByteArrayOutputStream constructStream(byte[]... byteArrs) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        for (byte[] arr : byteArrs) {
            stream.write(arr);
        }
        return stream;
    }

    private void setupMock(boolean isAppending, List<String> paths) throws InvalidArgsException, TeeException {
        doNothing().when(parser).parse();
        when(parser.isAppending()).thenReturn(isAppending);
        when(parser.getFilePaths()).thenReturn(paths);
    }

    @Test
    public void teeFromStdin_noFile_writeTerminal() throws TeeException {
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(false, inputCapture));
    }

    @Test
    public void teeFromStdin_nonExistFile_writeNewFileAndTerminal() throws TeeException, IOException {
        Path nonExistPath = folderPath.resolve(NON_EXIST_FILE_C);
        deleteFileIfExists(nonExistPath);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(false, inputCapture, nonExistPath.toString()));
        assertTrue(hasOutputToFiles(OVERWRITTEN_TEXT, nonExistPath));
    }

    @Test
    public void teeFromStdin_twoNonExistFile_writeNewFilesAndTerminal() throws TeeException, IOException {
        Path nonExistPathC = folderPath.resolve(NON_EXIST_FILE_C);
        deleteFileIfExists(nonExistPathC);
        Path nonExistPathD = folderPath.resolve(NON_EXIST_FILE_D);
        deleteFileIfExists(nonExistPathD);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(false, inputCapture, nonExistPathC.toString(), nonExistPathD.toString()));
        assertTrue(hasOutputToFiles(OVERWRITTEN_TEXT, nonExistPathC, nonExistPathD));
    }

    @Test
    public void teeFromStdin_parentDirFile_writeFileAndTerminal() throws TeeException, IOException {
        File parentDirFile = new File(folderPath.resolve(FOLDER).toString(), ".." + File.separator + EXIST_FILE_A);
        Path parentDirPath = parentDirFile.toPath();
        deleteFileIfExists(parentDirPath);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(false, inputCapture, parentDirPath.toString()));
        assertTrue(hasOutputToFiles(OVERWRITTEN_TEXT, parentDirPath));
    }

    @Test
    public void teeFromStdin_currentDirFile_writeFileAndTerminal() throws TeeException, IOException {
        File currDirFile = new File(folderPath.resolve(FOLDER).toString(), "." + File.separator + EXIST_FILE_A);
        Path currDirPath = currDirFile.toPath();
        deleteFileIfExists(currDirPath);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(false, inputCapture, currDirPath.toString()));
        assertTrue(hasOutputToFiles(OVERWRITTEN_TEXT, currDirPath));
    }

    @Test
    public void teeFromStdin_existFile_writeFileAndTerminal() throws IOException, TeeException {
        Path existFilePath = folderPath.resolve(EXIST_FILE_A);
        eraseFileContent(existFilePath);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(false, inputCapture, existFilePath.toString()));
        assertTrue(hasOutputToFiles(OVERWRITTEN_TEXT, existFilePath));
    }

    @Test
    public void teeFromStdin_dirFile_writeFileAndTerminal() throws TeeException, IOException {
        Path dirFilePath = folderPath.resolve(FOLDER).resolve(EXIST_DIR_FILE_E);
        eraseFileContent(dirFilePath);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(false, inputCapture, dirFilePath.toString()));
        assertTrue(hasOutputToFiles(OVERWRITTEN_TEXT, dirFilePath));
    }

    @Test
    public void teeFromStdin_nonExistFileAndExistFile_writeNewFileFileAndTerminal() throws TeeException, IOException {
        Path nonExistPath = folderPath.resolve(NON_EXIST_FILE_C);
        deleteFileIfExists(nonExistPath);
        Path existPath = folderPath.resolve(EXIST_FILE_A);
        eraseFileContent(existPath);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(false, inputCapture, nonExistPath.toString(), existPath.toString()));
        assertTrue(hasOutputToFiles(OVERWRITTEN_TEXT, nonExistPath, existPath));
    }

    @Test
    public void teeFromStdin_nonExistFileAndDirFile_writeNewFileFileAndTerminal() throws TeeException, IOException {
        Path nonExistPath = folderPath.resolve(NON_EXIST_FILE_C);
        deleteFileIfExists(nonExistPath);
        Path dirFilePath = folderPath.resolve(FOLDER).resolve(EXIST_DIR_FILE_E);
        eraseFileContent(dirFilePath);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(false, inputCapture, nonExistPath.toString(), dirFilePath.toString()));
        assertTrue(hasOutputToFiles(OVERWRITTEN_TEXT, nonExistPath, dirFilePath));
    }

    @Test
    public void teeFromStdin_twoExistFiles_writeFilesAndTerminal() throws TeeException, IOException {
        Path existPathA = folderPath.resolve(EXIST_FILE_A);
        eraseFileContent(existPathA);
        Path existPathB = folderPath.resolve(EXIST_FILE_B);
        eraseFileContent(existPathB);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(false, inputCapture, existPathA.toString(), existPathB.toString()));
        assertTrue(hasOutputToFiles(OVERWRITTEN_TEXT, existPathA, existPathB));
    }

    @Test
    public void teeFromStdin_existFileAndDirFile_writeFilesAndTerminal() throws TeeException, IOException {
        Path existPath = folderPath.resolve(EXIST_FILE_A);
        eraseFileContent(existPath);
        Path dirFilePath = folderPath.resolve(FOLDER).resolve(EXIST_DIR_FILE_E);
        eraseFileContent(dirFilePath);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(false, inputCapture, existPath.toString(), dirFilePath.toString()));
        assertTrue(hasOutputToFiles(OVERWRITTEN_TEXT, existPath, dirFilePath));
    }

    @Test
    public void teeFromStdin_twoDirFiles_writeFilesAndTerminal() throws TeeException, IOException {
        Path dirPathE = folderPath.resolve(FOLDER).resolve(EXIST_DIR_FILE_E);
        eraseFileContent(dirPathE);
        Path dirPathF = folderPath.resolve(FOLDER).resolve(EXIST_DIR_FILE_F);
        eraseFileContent(dirPathF);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(false, inputCapture, dirPathE.toString(), dirPathF.toString()));
        assertTrue(hasOutputToFiles(OVERWRITTEN_TEXT, dirPathE, dirPathF));
    }

    @Test
    public void teeFromStdin_appendExistFile_appendFileAndTerminal() throws TeeException, IOException {
        Path existPath = folderPath.resolve(EXIST_FILE_A);
        overwriteFileContent(existPath, BASE_TEXT);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(true, inputCapture, existPath.toString()));
        assertTrue(hasOutputToFiles(BASE_TEXT + System.lineSeparator() + OVERWRITTEN_TEXT, existPath));
    }

    @Test
    public void teeFromStdin_appendDirFile_appendDirFileAndTerminal() throws TeeException, IOException {
        Path existDirPath = folderPath.resolve(FOLDER).resolve(EXIST_DIR_FILE_E);
        overwriteFileContent(existDirPath, BASE_TEXT);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(true, inputCapture, existDirPath.toString()));
        assertTrue(hasOutputToFiles(BASE_TEXT + System.lineSeparator() + OVERWRITTEN_TEXT, existDirPath));
    }

    @Test
    public void teeFromStdin_appendNonExistFileAndExistFile_appendNewFileFileAndTerminal() throws TeeException, IOException {
        Path nonExistPath = folderPath.resolve(NON_EXIST_FILE_C);
        deleteFileIfExists(nonExistPath);
        Path existPath = folderPath.resolve(EXIST_FILE_A);
        overwriteFileContent(existPath, BASE_TEXT);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(true, inputCapture, nonExistPath.toString(), existPath.toString()));
        assertTrue(hasOutputToFiles(OVERWRITTEN_TEXT, nonExistPath)
                && hasOutputToFiles(BASE_TEXT + System.lineSeparator() + OVERWRITTEN_TEXT, existPath));
    }

    @Test
    public void teeFromStdin_appendNonExistAndDirFile_appendNewFileFileAndTerminal() throws TeeException, IOException {
        Path nonExistPath = folderPath.resolve(NON_EXIST_FILE_C);
        deleteFileIfExists(nonExistPath);
        Path dirPath = folderPath.resolve(FOLDER).resolve(EXIST_DIR_FILE_E);
        overwriteFileContent(dirPath, BASE_TEXT);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(true, inputCapture, nonExistPath.toString(), dirPath.toString()));
        assertTrue(hasOutputToFiles(OVERWRITTEN_TEXT, nonExistPath)
                && hasOutputToFiles(BASE_TEXT + System.lineSeparator() + OVERWRITTEN_TEXT, dirPath));
    }

    @Test
    public void teeFromStdin_appendTwoExistFiles_appendFilesAndTerminal() throws TeeException, IOException {
        Path existPathA = folderPath.resolve(EXIST_FILE_A);
        overwriteFileContent(existPathA, BASE_TEXT);
        Path existPathB = folderPath.resolve(EXIST_FILE_B);
        overwriteFileContent(existPathB, BASE_TEXT);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(true, inputCapture, existPathA.toString(), existPathB.toString()));
        assertTrue(hasOutputToFiles(BASE_TEXT + System.lineSeparator() + OVERWRITTEN_TEXT, existPathA, existPathB));
    }

    @Test
    public void teeFromStdin_appendExistFileAndDirFile_appendFilesAndTerminal() throws TeeException, IOException {
        Path existPath = folderPath.resolve(EXIST_FILE_A);
        overwriteFileContent(existPath, BASE_TEXT);
        Path dirPath = folderPath.resolve(FOLDER).resolve(EXIST_DIR_FILE_E);
        overwriteFileContent(dirPath, BASE_TEXT);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(true, inputCapture, existPath.toString(), dirPath.toString()));
        assertTrue(hasOutputToFiles(BASE_TEXT + System.lineSeparator() + OVERWRITTEN_TEXT, existPath, dirPath));
    }

    @Test
    public void teeFromStdin_appendTwoDirFiles_appendFilesAndTerminal() throws TeeException, IOException {
        Path dirPathE = folderPath.resolve(FOLDER).resolve(EXIST_DIR_FILE_E);
        overwriteFileContent(dirPathE, BASE_TEXT);
        Path dirPathF = folderPath.resolve(FOLDER).resolve(EXIST_DIR_FILE_F);
        overwriteFileContent(dirPathF, BASE_TEXT);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(true, inputCapture, dirPathE.toString(), dirPathF.toString()));
        assertTrue(hasOutputToFiles(BASE_TEXT + System.lineSeparator() + OVERWRITTEN_TEXT, dirPathE, dirPathF));
    }

    @Test
    @DisabledOnOs(OS.WINDOWS)
    public void teeFromStdin_allFilesWithNothing_writeNewFileAndTerminal() throws TeeException, IOException {
        deleteFilesByExtension(folderPath, ".txt");
        Path allFilesPath = folderPath.resolve(ANY_TXT_FILE);
        assertEquals(OVERWRITTEN_TEXT, tee.teeFromStdin(false, inputCapture, allFilesPath.toString()));
        assertTrue(hasOutputToFiles(OVERWRITTEN_TEXT, allFilesPath));
    }

    @Test
    public void run_noFlagNoFilesNoOperator_printToTerminal() throws TeeException, InvalidArgsException {
        setupMock(false, new ArrayList<>());

        tee.run(new String[0], inputCapture, outputCapture);
        assertEquals(OVERWRITTEN_TEXT, outputCapture.toString().trim());
    }

    @Test
    public void run_noFlagFileFilePipeFromEcho_echoAndPrintToTerminalAndFiles() throws TeeException,
            InvalidArgsException, IOException {
        Path filePathA = folderPath.resolve(EXIST_FILE_A);
        eraseFileContent(filePathA);
        Path filePathC = folderPath.resolve(NON_EXIST_FILE_C);
        deleteFileIfExists(filePathC);

        ByteArrayOutputStream stream = constructStream(ECHO_TEXT.getBytes(), OVERWRITTEN_TEXT.getBytes());
        inputCapture = new ByteArrayInputStream(stream.toByteArray());

        setupMock(false, Arrays.asList(filePathA.toString(), filePathC.toString()));

        tee.run(new String[0], inputCapture, outputCapture);
        assertEquals(stream.toString(), outputCapture.toString().trim());
        assertEquals(stream.toString(), getFileContent(filePathA));
        assertEquals(stream.toString(), getFileContent(filePathC));
    }

    @Test
    public void run_noFlagDirFileDirFileRedirectFromFile_redirectAndPrintToTerminalAndFiles() throws TeeException,
            InvalidArgsException, IOException {
        Path filePathA = folderPath.resolve(EXIST_FILE_A);
        overwriteFileContent(filePathA, BASE_TEXT);
        Path dirFilePathE = folderPath.resolve(EXIST_DIR_FILE_E);
        eraseFileContent(dirFilePathE);
        Path dirFilePathF = folderPath.resolve(NONEXIST_DIRFILE);
        deleteFileIfExists(dirFilePathF);

        ByteArrayOutputStream stream = constructStream(Files.readAllBytes(filePathA), OVERWRITTEN_TEXT.getBytes());
        inputCapture = new ByteArrayInputStream(stream.toByteArray());

        setupMock(false, Arrays.asList(dirFilePathE.toString(), dirFilePathF.toString()));

        tee.run(new String[0], inputCapture, outputCapture);
        assertEquals(stream.toString(), outputCapture.toString().trim());
        assertEquals(stream.toString(), getFileContent(dirFilePathE));
        assertEquals(stream.toString(), getFileContent(dirFilePathF));
    }

    @Test
    public void run_noFlagDirFileFileNoOperator_printToTerminalAndFiles() throws TeeException, IOException,
            InvalidArgsException {
        Path dirFilePathE = folderPath.resolve(EXIST_DIR_FILE_E);
        eraseFileContent(dirFilePathE);
        Path filePathC = folderPath.resolve(NON_EXIST_FILE_C);
        deleteFileIfExists(filePathC);

        setupMock(false, Arrays.asList(dirFilePathE.toString(), filePathC.toString()));

        tee.run(new String[0], inputCapture, outputCapture);
        assertEquals(OVERWRITTEN_TEXT, outputCapture.toString().trim());
        assertEquals(OVERWRITTEN_TEXT, getFileContent(dirFilePathE));
        assertEquals(OVERWRITTEN_TEXT, getFileContent(filePathC));
    }

    @Test
    public void run_noFlagFileRedirectFromFile_redirectAndPrintToTerminalAndFile() throws TeeException, IOException,
            InvalidArgsException {
        Path filePathA = folderPath.resolve(EXIST_FILE_A);
        overwriteFileContent(filePathA, BASE_TEXT);
        Path filePathC = folderPath.resolve(NON_EXIST_FILE_C);
        deleteFileIfExists(filePathC);

        ByteArrayOutputStream stream = constructStream(Files.readAllBytes(filePathA), OVERWRITTEN_TEXT.getBytes());
        inputCapture = new ByteArrayInputStream(stream.toByteArray());

        setupMock(false, Arrays.asList(filePathC.toString()));

        tee.run(new String[0], inputCapture, outputCapture);
        assertEquals(stream.toString(), outputCapture.toString().trim());
        assertEquals(stream.toString(), getFileContent(filePathC));
    }

    @Test
    public void run_appendFileDirFileNoOperator_appendToFilesAndPrintToTerminal() throws TeeException, IOException,
            InvalidArgsException {
        Path filePathA = folderPath.resolve(EXIST_FILE_A);
        overwriteFileContent(filePathA, BASE_TEXT);
        Path dirFilePathG = folderPath.resolve(NONEXIST_DIRFILE);
        deleteFileIfExists(dirFilePathG);

        setupMock(true, Arrays.asList(filePathA.toString(), dirFilePathG.toString()));

        tee.run(new String[0], inputCapture, outputCapture);
        assertEquals(OVERWRITTEN_TEXT, outputCapture.toString().trim());
        assertEquals(BASE_TEXT + System.lineSeparator() + OVERWRITTEN_TEXT, getFileContent(filePathA));
        assertEquals(OVERWRITTEN_TEXT, getFileContent(dirFilePathG));
    }

    @Test
    public void run_appendDirFilePipeFromEcho_appendAndEchoToTerminalAndFile() throws TeeException, IOException,
            InvalidArgsException {
        Path dirFilePathE = folderPath.resolve(EXIST_DIR_FILE_E);
        overwriteFileContent(dirFilePathE, BASE_TEXT);

        ByteArrayOutputStream stream = constructStream(ECHO_TEXT.getBytes(), OVERWRITTEN_TEXT.getBytes());
        inputCapture = new ByteArrayInputStream(stream.toByteArray());

        setupMock(true, Arrays.asList(dirFilePathE.toString()));

        tee.run(new String[0], inputCapture, outputCapture);
        assertEquals(stream.toString(), outputCapture.toString().trim());
        assertEquals(BASE_TEXT + System.lineSeparator() + stream.toString(), getFileContent(dirFilePathE));
    }

    @Test
    public void run_appendFileRedirectFromFile_appendAndRedirectToTerminalAndFile() throws TeeException, IOException,
            InvalidArgsException {
        Path filePathA = folderPath.resolve(EXIST_FILE_A);
        overwriteFileContent(filePathA, BASE_TEXT);
        Path filePathB = folderPath.resolve(EXIST_FILE_B);
        overwriteFileContent(filePathB, BASE_TEXT);

        ByteArrayOutputStream stream = constructStream(Files.readAllBytes(filePathA), OVERWRITTEN_TEXT.getBytes());
        inputCapture = new ByteArrayInputStream(stream.toByteArray());

        setupMock(true, Arrays.asList(filePathB.toString()));

        tee.run(new String[0], inputCapture, outputCapture);
        assertEquals(stream.toString(), outputCapture.toString().trim());
        assertEquals(BASE_TEXT + System.lineSeparator() + stream.toString(), getFileContent(filePathB));
    }

    @Test
    public void setArgsParser_null_throwExceptionWhenRun() throws TeeException, InvalidArgsException {
        String[] args = {};
        tee.setArgsParser(null);
        setupMock(false, Arrays.asList(args));
        assertThrows(TeeException.class, () -> tee.run(args, inputCapture, outputCapture));
    }

    @Test
    public void setArgsParser_properParser_parserChanged() throws TeeException, InvalidArgsException {
        String[] args = {};
        tee.setArgsParser(new TeeArgsParser());
        setupMock(false, Arrays.asList(args));
        assertDoesNotThrow(() -> tee.run(args, inputCapture, outputCapture));
    }

    @Test
    public void setupReader_null_throwException() {
        assertThrows(InvalidArgsException.class, () -> tee.setupReader(null));
    }

    @Test
    public void setupReader_properReader_readerChanged() {
        assertDoesNotThrow(() -> tee.setupReader(inputCapture));
    }
}
