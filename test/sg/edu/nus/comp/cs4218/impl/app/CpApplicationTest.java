package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.CpException;
import sg.edu.nus.comp.cs4218.impl.parser.CpArgsParser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_FLAG_NOT_GIVEN;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_NOT_DIR;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.*;

public class CpApplicationTest {
    private final static String LOWER_RECUR_FLAG = "-r";
    private final static String UPPER_RECUR_FLAG = "-R";
    private final static String INVALID_FLAG = "-i";
    private final static String FILE = "a.txt";
    private final static String CLONE_FILE = "b.txt";
    private final static String NESTED_FOLDER_1 = "NEST_DIR";
    private final static String NESTED_FILE = "c.txt";
    private final static String NESTED_CLONE_FILE = "d.txt";
    private final static String NESTED_FOLDER_2 = "OTHER_NEST_DIR";
    private final static String NESTED_FOLDER_3 = "ANOTHER_NEST_DIR";
    private final static String NON_EXIST_FILE = "nosuchfile.txt";
    private final static String NON_EXIST_FOLDER = "nosuchfolder";
    private final static String CP_ERROR_STARTER = "cp: ";
    private final static String PLACEHOLDER_TEXT = "Just some text in a file...";
    private final static String MSG_NO_FILE_DIR = "No such file or directory";
    @TempDir
    public static Path folderPath;
    private static CpApplication cpApp;
    private static CpArgsParser parser;
    private static ByteArrayOutputStream outputCapture = new ByteArrayOutputStream();

    @BeforeAll
    public static void setup() throws IOException {
        cpApp = new CpApplication();
        System.setOut(new PrintStream(outputCapture));
        Files.createDirectories(folderPath);
        Files.createDirectories(folderPath.resolve(NESTED_FOLDER_1));
        Files.write(folderPath.resolve(FILE), PLACEHOLDER_TEXT.getBytes());
    }

    private String[] absoluteArgs(String... args) {
        return Arrays.stream(args)
                .map(arg -> arg.startsWith("-") ? arg : folderPath.resolve(arg).toString())
                .toArray(String[]::new);
    }

    @BeforeEach
    public void start() {
        parser = mock(CpArgsParser.class);
        cpApp.setArgsParser(parser);
    }

    @AfterEach
    public void end() {
        outputCapture.reset();
    }

    @Test
    public void cpSrcFileToDestFile_fileToFile_contentCopiedOver() throws CpException, IOException {
        Path filePath = folderPath.resolve(FILE);
        overwriteFileContent(filePath, PLACEHOLDER_TEXT);
        Path cloneFilePath = folderPath.resolve(CLONE_FILE);
        createNewFile(cloneFilePath);

        cpApp.cpSrcFileToDestFile(false, filePath.toString(), cloneFilePath.toString());
        assertEquals(getFileContent(cloneFilePath), PLACEHOLDER_TEXT);
    }

    @Test
    public void cpSrcFileToDestFile_fileToDirectoryFile_contentCopiedOver() throws CpException, IOException {
        Path filePath = folderPath.resolve(FILE);
        overwriteFileContent(filePath, PLACEHOLDER_TEXT);
        Path dirPath = folderPath.resolve(NESTED_FOLDER_1).resolve(NESTED_FILE);
        createNewFile(dirPath);

        cpApp.cpSrcFileToDestFile(false, filePath.toString(), dirPath.toString());
        assertEquals(getFileContent(dirPath), PLACEHOLDER_TEXT);
    }

    @Test
    public void cpSrcFileToDestFile_fileToNonExistingFile_createAndCopyOver() throws CpException, IOException {
        Path filePath = folderPath.resolve(FILE);
        overwriteFileContent(filePath, PLACEHOLDER_TEXT);
        Path nonExistPath = folderPath.resolve(NESTED_FILE);
        deleteFileIfExists(nonExistPath);

        cpApp.cpSrcFileToDestFile(false, filePath.toString(), nonExistPath.toString());
        assertEquals(getFileContent(nonExistPath), PLACEHOLDER_TEXT);
    }

    @Test
    public void cpSrcFileToDestFile_fileToNotAFile_throwCPException() {
        Path notAFilePath = folderPath.resolve(NESTED_FOLDER_1);
        assertThrows(CpException.class, () -> cpApp.cpSrcFileToDestFile(false,
                folderPath.resolve(FILE).toString(), notAFilePath.toString()));
    }

    @Test
    public void cpSrcFileToDestFile_dirFileToFile_contentCopyOver() throws CpException, IOException {
        Path srcPath = folderPath.resolve(NESTED_FOLDER_1).resolve(NESTED_FILE);
        cpApp.cpSrcFileToDestFile(false, srcPath.toString(), folderPath.resolve(FILE).toString());
        assertEquals(getFileContent(folderPath.resolve(FILE)), PLACEHOLDER_TEXT);
    }

    @Test
    public void cpSrcFileToDestFile_dirFileToDirFile_contentCopyOver() throws CpException, IOException {
        Path srcPath = folderPath.resolve(NESTED_FOLDER_1).resolve(NESTED_FILE);
        overwriteFileContent(srcPath, PLACEHOLDER_TEXT);
        Path destPath = folderPath.resolve(NESTED_FOLDER_1).resolve(NESTED_CLONE_FILE);
        eraseFileContent(destPath);

        cpApp.cpSrcFileToDestFile(false, srcPath.toString(), destPath.toString());
        assertEquals(getFileContent(destPath), PLACEHOLDER_TEXT);
    }

    @Test
    public void cpSrcFileToDestFile_dirFileToNonExistingFile_contentCopyOver() throws IOException, CpException {
        Path dirPath = folderPath.resolve(NESTED_FOLDER_1).resolve(NESTED_FILE);
        overwriteFileContent(dirPath, PLACEHOLDER_TEXT);
        Path nonExistPath = folderPath.resolve(NON_EXIST_FILE);
        deleteFileIfExists(nonExistPath);

        cpApp.cpSrcFileToDestFile(false, dirPath.toString(), nonExistPath.toString());
        assertEquals(getFileContent(nonExistPath), PLACEHOLDER_TEXT);
    }

    @Test
    public void cpSrcFileToDestFile_dirFileToNotAFile_throwCPException() {
        Path notAFilePath = folderPath.resolve(NESTED_FOLDER_1);
        assertThrows(CpException.class, () -> cpApp.cpSrcFileToDestFile(false,
                folderPath.resolve(NESTED_FOLDER_1).resolve(NESTED_FILE).toString(), notAFilePath.toString()));
    }

    @Test
    public void cpSrcFileToDestFile_nonExistingFileToFile_throwCPException() throws IOException {
        Path nonExistPath = folderPath.resolve(NON_EXIST_FILE);
        deleteFileIfExists(nonExistPath);
        assertThrows(CpException.class, () -> cpApp.cpSrcFileToDestFile(false, nonExistPath.toString(),
                folderPath.resolve(FILE).toString()));
    }

    @Test
    public void cpSrcFileToDestFile_notAFileToFile_throwCPException() {
        Path notAFilePath = folderPath.resolve(NESTED_FOLDER_1);
        assertThrows(CpException.class, () -> cpApp.cpSrcFileToDestFile(false, notAFilePath.toString(),
                folderPath.resolve(FILE).toString()));
    }

    @Test
    public void cpFilesToFolder_fileToFolder_fileCopyOver() throws CpException, IOException {
        Path filePath = folderPath.resolve(FILE);
        Path destFolderPath = folderPath.resolve(NESTED_FOLDER_1);
        Path destFilePath = destFolderPath.resolve(FILE);
        deleteFileIfExists(destFolderPath.resolve(FILE));

        cpApp.cpFilesToFolder(false, destFolderPath.toString(), filePath.toString());
        assertEquals(getFileContent(destFilePath), PLACEHOLDER_TEXT);
    }

    @Test
    public void cpFilesToFolder_fileToNestedFolder_fileCopyOver() throws CpException, IOException {
        Path filePath = folderPath.resolve(FILE);
        overwriteFileContent(filePath, PLACEHOLDER_TEXT);
        Path destFolderPath = folderPath.resolve(NESTED_FOLDER_1).resolve(NESTED_FOLDER_1);
        Path destFilePath = destFolderPath.resolve(FILE);
        deleteFileIfExists(destFilePath);

        cpApp.cpFilesToFolder(false, destFolderPath.toString(), filePath.toString());
        assertEquals(getFileContent(destFilePath), PLACEHOLDER_TEXT);
    }

    @Test
    public void cpFilesToFolder_fileToNonExistingFolder_throwCPException() {
        Path destPath = folderPath.resolve(NON_EXIST_FOLDER);
        deleteDirsIfExists(destPath);
        assertThrows(CpException.class,
                () -> cpApp.cpFilesToFolder(false, destPath.toString(), folderPath.resolve(FILE).toString()));
    }

    @Test
    public void cpFilesToFolder_folderToFolder_folderCopyOver() throws CpException, IOException {
        Path destPath = folderPath.resolve(NESTED_FOLDER_2);
        Path clonedFolderPath = destPath.resolve(NESTED_FOLDER_1);
        createNewDirs(destPath);
        cpApp.cpFilesToFolder(true, destPath.toString(), folderPath.resolve(NESTED_FOLDER_1).toString());
        assertTrue(Files.exists(clonedFolderPath));
    }

    @Test
    public void cpFilesToFolder_folderToNestedFolder_folderCopyOver() throws CpException, IOException {
        Path srcPath = folderPath.resolve(NESTED_FOLDER_2);
        createNewDirs(srcPath);
        Path destPath = folderPath.resolve(NESTED_FOLDER_1).resolve(NESTED_FOLDER_1);
        createNewDirs(destPath);

        cpApp.cpFilesToFolder(true, destPath.toString(), srcPath.toString());
        assertTrue(Files.exists(destPath.resolve(NESTED_FOLDER_2)));
    }

    @Test
    public void cpFilesToFolder_folderToNonExistingFolder_throwCPException() {
        Path destPath = folderPath.resolve(NON_EXIST_FOLDER);
        deleteDirsIfExists(destPath);
        assertThrows(CpException.class, () -> cpApp.cpFilesToFolder(true, destPath.toString(),
                folderPath.resolve(NESTED_FOLDER_1).toString()));
    }

    @Test
    public void cpFilesToFolder_folderToFolderNoRecursive_noCopying() throws CpException {
        Path srcPath = folderPath.resolve(NESTED_FOLDER_1);
        Path destPath = folderPath.resolve(NESTED_FOLDER_2);
        deleteDirsIfExists(destPath.resolve(NESTED_FOLDER_1));

        cpApp.cpFilesToFolder(false, destPath.toString(), srcPath.toString());
        assertFalse(Files.exists(destPath.resolve(NESTED_FOLDER_1)));
    }

    @Test
    public void cpFilesToFolder_nestedFolderToFolder_folderCopyOver() throws CpException, IOException {
        Path srcPath = folderPath.resolve(NESTED_FOLDER_1).resolve(NESTED_FOLDER_1);
        createNewDirs(srcPath);
        Path destPath = folderPath.resolve(NESTED_FOLDER_2);
        createNewDirs(destPath);

        cpApp.cpFilesToFolder(true, destPath.toString(), srcPath.toString());
        assertTrue(Files.exists(destPath.resolve(NESTED_FOLDER_1)));
    }

    @Test
    public void cpFilesToFolder_nestedFolderToNestedFolder_folderCopyOver() throws CpException, IOException {
        Path srcPath = folderPath.resolve(NESTED_FOLDER_1).resolve(NESTED_FOLDER_1);
        createNewDirs(srcPath);
        Path destPath = folderPath.resolve(NESTED_FOLDER_2).resolve(NESTED_FOLDER_2);
        createNewDirs(destPath);

        cpApp.cpFilesToFolder(true, destPath.toString(), srcPath.toString());
        assertTrue(Files.exists(destPath.resolve(NESTED_FOLDER_1)));
    }


    @Test
    public void cpFilesToFolder_nestedFolderToNonExistingFolder_createFolderFolderCopyOver() throws CpException {
        Path srcPath = folderPath.resolve(NESTED_FOLDER_1);
        Path destPath = folderPath.resolve(NON_EXIST_FOLDER);
        cpApp.cpFilesToFolder(true, destPath.toString(), srcPath.toString());
        assertTrue(Files.exists(destPath.resolve(NESTED_FOLDER_1)));
    }

    @Test
    public void cpFilesToFolder_fileAndFoldersToFolder_copyOver() throws CpException, IOException {
        Path srcFilePath = folderPath.resolve(FILE);
        Path srcFolderPath = folderPath.resolve(NESTED_FOLDER_1);
        Path srcNestFolderPath = srcFolderPath.resolve(NESTED_FOLDER_1);
        createNewDirs(srcNestFolderPath);
        Path destPath = folderPath.resolve(NESTED_FOLDER_2);
        createNewDirs(destPath);

        cpApp.cpFilesToFolder(true, destPath.toString(), srcFilePath.toString(), srcFolderPath.toString(),
                srcNestFolderPath.toString());
        assertTrue(Files.exists(destPath.resolve(FILE))
                && Files.exists(destPath.resolve(NESTED_FOLDER_1))
                && Files.exists(destPath.resolve(NESTED_FOLDER_1).resolve(NESTED_FOLDER_1)));
    }

    @Test
    public void cpFilesToFolder_fileAndFoldersToNestedFolder_copyOver() throws CpException, IOException {
        Path srcFilePath = folderPath.resolve(FILE);
        Path srcFolderPath = folderPath.resolve(NESTED_FOLDER_1);
        Path srcNestFolderPath = srcFolderPath.resolve(NESTED_FOLDER_1);
        createNewDirs(srcNestFolderPath);
        Path destPath = folderPath.resolve(NESTED_FOLDER_2).resolve(NESTED_FOLDER_2);
        createNewDirs(destPath);

        cpApp.cpFilesToFolder(true, destPath.toString(), srcFilePath.toString(), srcFolderPath.toString(),
                srcNestFolderPath.toString());
        assertTrue(Files.exists(destPath.resolve(FILE))
                && Files.exists(destPath.resolve(NESTED_FOLDER_1))
                && Files.exists(destPath.resolve(NESTED_FOLDER_1).resolve(NESTED_FOLDER_1)));
    }

    @Test
    public void cpFilesToFolder_fileAndFoldersToNonExistingFolder_createFolderCopyOver() throws CpException, IOException {
        Path srcFilePath = folderPath.resolve(FILE);
        Path srcFolderPath = folderPath.resolve(NESTED_FOLDER_1);
        Path srcNestFolderPath = srcFolderPath.resolve(NESTED_FOLDER_1);
        createNewDirs(srcNestFolderPath);
        Path destPath = folderPath.resolve(NON_EXIST_FOLDER);
        createNewDirs(destPath);

        cpApp.cpFilesToFolder(true, destPath.toString(), srcFilePath.toString(), srcFolderPath.toString(),
                srcNestFolderPath.toString());
        assertTrue(Files.exists(destPath.resolve(FILE))
                && Files.exists(destPath.resolve(NESTED_FOLDER_1))
                && Files.exists(destPath.resolve(NESTED_FOLDER_1).resolve(NESTED_FOLDER_1)));
    }

    @Test
    public void cpFilesToFolder_nonExistingFileToFolder_throwCPException() throws CpException, IOException {
        Path srcPath = folderPath.resolve(NON_EXIST_FILE);
        deleteFileIfExists(srcPath);
        Path destPath = folderPath.resolve(NESTED_FOLDER_1);
        createNewDirs(destPath);
        cpApp.cpFilesToFolder(true, destPath.toString(), srcPath.toString());
        assertEquals(outputCapture.toString().trim(),
                CP_ERROR_STARTER + srcPath.getFileName() + ": " + MSG_NO_FILE_DIR);
    }

    @Test
    public void cpFilesToFolder_nonExistingFolderToFolder_throwCPException() throws CpException {
        Path srcPath = folderPath.resolve(NON_EXIST_FOLDER);
        deleteDirsIfExists(srcPath);
        Path destPath = folderPath.resolve(NESTED_FOLDER_1);

        cpApp.cpFilesToFolder(true, destPath.toString(), srcPath.toString());
        assertEquals(outputCapture.toString().trim(),
                CP_ERROR_STARTER + srcPath.getFileName() + ": " + MSG_NO_FILE_DIR);
    }

    @Test
    public void cpFilesToFolder_nonExistingNestedFolderToFolder_throwCPException() throws CpException {
        Path srcPath = folderPath.resolve(NESTED_FOLDER_1).resolve(NON_EXIST_FOLDER);
        deleteDirsIfExists(srcPath);
        Path destPath = folderPath.resolve(NESTED_FOLDER_1);

        cpApp.cpFilesToFolder(true, destPath.toString(), srcPath.toString());
        assertEquals(outputCapture.toString().trim(),
                CP_ERROR_STARTER + srcPath.getFileName() + ": " + MSG_NO_FILE_DIR);
    }

    @Test
    public void run_noArgs_throwCPException() throws InvalidArgsException, CpException {
        String[] args = {};

        doNothing().when(parser).parse();
        when(parser.isFileToFile()).thenReturn(false);
        when(parser.isToDirectory()).thenReturn(false);

        assertThrows(CpException.class, () -> cpApp.run(args, System.in, outputCapture));
    }

    @Test
    public void run_nonExistPathFilePathNonExistPath_throwCPException() throws InvalidArgsException, CpException {
        String[] args = {NON_EXIST_FILE, FILE, NON_EXIST_FOLDER};

        doNothing().when(parser).parse();
        when(parser.isFileToFile()).thenReturn(false);
        when(parser.isToDirectory()).thenReturn(false);

        assertThrows(CpException.class, () -> cpApp.run(args, System.in, outputCapture));
    }

    @Test
    public void run_filePathFolderPathFilePath_throwCPException() throws CpException, InvalidArgsException {
        String[] args = {FILE, NESTED_FOLDER_1, CLONE_FILE};

        doNothing().when(parser).parse();
        when(parser.isFileToFile()).thenReturn(false);
        when(parser.isToDirectory()).thenThrow(new CpException(ERR_IS_NOT_DIR));

        assertThrows(CpException.class, () -> cpApp.run(args, System.in, outputCapture));
    }

    @Test
    public void run_folderPathFolderPath_noFlagMsgNoCopyOver() throws IOException, CpException, InvalidArgsException {
        String[] args = absoluteArgs(NESTED_FOLDER_1, NESTED_FOLDER_2);
        createNewDirs(folderPath.resolve(NESTED_FOLDER_1));
        createNewDirs(folderPath.resolve(NESTED_FOLDER_2));

        doNothing().when(parser).parse();
        when(parser.isFileToFile()).thenReturn(false);
        when(parser.isToDirectory()).thenReturn(true);
        when(parser.isRecursive()).thenReturn(false);
        when(parser.getDestPath()).thenReturn(args[1]);
        when(parser.getSrcPaths()).thenReturn(Arrays.asList(args[0]));

        cpApp.run(args, System.in, outputCapture);
        assertEquals(outputCapture.toString().trim(),
                CP_ERROR_STARTER + folderPath.resolve(NESTED_FOLDER_1).getFileName() + ": -r/-R " + ERR_FLAG_NOT_GIVEN);
        assertFalse(Files.exists(folderPath.resolve(NESTED_FOLDER_2).resolve(NESTED_FOLDER_1)));
    }

    @Test
    public void run_invalidFlagFolderPathNonExistPath_throwCPException() throws InvalidArgsException {
        String[] args = absoluteArgs(INVALID_FLAG, NESTED_FOLDER_1, NON_EXIST_FOLDER);
        doThrow(InvalidArgsException.class).when(parser).parse(args);
        assertThrows(CpException.class, () -> cpApp.run(args, System.in, outputCapture));
    }

    @Test
    public void run_upperRecurFlagFilePathFilePath_contentCopyOver() throws CpException, IOException,
            InvalidArgsException {
        String[] args = absoluteArgs(UPPER_RECUR_FLAG, FILE, CLONE_FILE);
        deleteFileIfExists(folderPath.resolve(CLONE_FILE));

        doNothing().when(parser).parse(args);
        when(parser.isFileToFile()).thenReturn(true);
        when(parser.isRecursive()).thenReturn(true);
        when(parser.getSrcPath()).thenReturn(args[1]);
        when(parser.getDestPath()).thenReturn(args[2]);

        cpApp.run(args, System.in, outputCapture);
        assertEquals(getFileContent(folderPath.resolve(CLONE_FILE)), PLACEHOLDER_TEXT);
    }

    @Test
    public void run_upperRecurFlagFolderPathFilePathFolderPath_contentFilesFoldersCopyOver() throws IOException,
            CpException, InvalidArgsException {
        String[] args = absoluteArgs(UPPER_RECUR_FLAG, NESTED_FOLDER_1, FILE, NESTED_FOLDER_2);
        createNewFile(folderPath.resolve(NESTED_FOLDER_1).resolve(NESTED_FILE));
        Path destPath = folderPath.resolve(NESTED_FOLDER_2);
        createNewDirs(destPath);

        doNothing().when(parser).parse(args);
        when(parser.isFileToFile()).thenReturn(false);
        when(parser.isToDirectory()).thenReturn(true);
        when(parser.isRecursive()).thenReturn(true);
        when(parser.getDestPath()).thenReturn(args[3]);
        when(parser.getSrcPaths()).thenReturn(Arrays.asList(args[1], args[2]));

        cpApp.run(args, System.in, outputCapture);
        assertTrue(Files.exists(destPath.resolve(FILE))
                && Files.exists(destPath.resolve(NESTED_FOLDER_1).resolve(NESTED_FILE)));
    }

    @Test
    public void run_upperRecurFlagFilePathFolderPathNonExistPath_throwCPException() throws IOException,
            CpException, InvalidArgsException {
        String[] args = absoluteArgs(UPPER_RECUR_FLAG, FILE, NESTED_FOLDER_1, NESTED_FOLDER_2);
        createNewFile(folderPath.resolve(NESTED_FOLDER_1).resolve(NESTED_FILE));
        Path destPath = folderPath.resolve(NESTED_FOLDER_2);
        deleteDirsIfExists(destPath);

        doNothing().when(parser).parse(args);
        when(parser.isFileToFile()).thenReturn(false);
        when(parser.isToDirectory()).thenReturn(true);
        when(parser.isRecursive()).thenReturn(true);
        when(parser.getDestPath()).thenReturn(args[3]);
        when(parser.getSrcPaths()).thenReturn(Arrays.asList(args[1], args[2]));

        assertThrows(CpException.class, () -> cpApp.run(args, System.in, outputCapture));
    }

    @Test
    public void run_lowerRecurFlagFolderPathFolderPathNonExistPath_throwCPException() throws IOException
            , CpException, InvalidArgsException {
        String[] args = absoluteArgs(LOWER_RECUR_FLAG, NESTED_FOLDER_1, NESTED_FOLDER_2, NESTED_FOLDER_3);
        createNewFile(folderPath.resolve(NESTED_FOLDER_1).resolve(NESTED_FILE));
        createNewDirs(folderPath.resolve(NESTED_FOLDER_2));

        doNothing().when(parser).parse(args);
        when(parser.isFileToFile()).thenReturn(false);
        when(parser.isToDirectory()).thenReturn(true);
        when(parser.isRecursive()).thenReturn(true);
        when(parser.getDestPath()).thenReturn(args[3]);
        when(parser.getSrcPaths()).thenReturn(Arrays.asList(args[1], args[2]));

        assertThrows(CpException.class, () -> cpApp.run(args, System.in, outputCapture));
    }

    @Test
    public void run_lowerRecurFlagFilePathFilePathFolderPath_filesCopyOver() throws IOException, CpException,
            InvalidArgsException {
        String[] args = absoluteArgs(LOWER_RECUR_FLAG, FILE, CLONE_FILE, NESTED_FOLDER_1);
        createNewFile(folderPath.resolve(CLONE_FILE));
        Path destPath = folderPath.resolve(NESTED_FOLDER_1);
        createNewDirs(destPath);

        doNothing().when(parser).parse(args);
        when(parser.isFileToFile()).thenReturn(false);
        when(parser.isToDirectory()).thenReturn(true);
        when(parser.isRecursive()).thenReturn(true);
        when(parser.getDestPath()).thenReturn(args[3]);
        when(parser.getSrcPaths()).thenReturn(Arrays.asList(args[1], args[2]));

        cpApp.run(args, System.in, outputCapture);
        assertTrue(Files.exists(destPath.resolve(FILE)) && Files.exists(destPath.resolve(CLONE_FILE)));
    }

    @Test
    public void run_lowerRecurFlagFilePathFolderPathFolderPath_filesFoldersCopyOver() throws CpException, IOException,
            InvalidArgsException {
        String[] args = absoluteArgs(LOWER_RECUR_FLAG, FILE, NESTED_FOLDER_1, NESTED_FOLDER_2);
        Path destPath = folderPath.resolve(NESTED_FOLDER_2);
        createNewDirs(destPath);

        doNothing().when(parser).parse(args);
        when(parser.isFileToFile()).thenReturn(false);
        when(parser.isToDirectory()).thenReturn(true);
        when(parser.isRecursive()).thenReturn(true);
        when(parser.getDestPath()).thenReturn(args[3]);
        when(parser.getSrcPaths()).thenReturn(Arrays.asList(args[1], args[2]));

        cpApp.run(args, System.in, outputCapture);
        assertTrue(Files.exists(destPath.resolve(FILE)) && Files.exists(destPath.resolve(NESTED_FOLDER_1)));
    }

    @Test
    public void setArgsParser_null_throwExceptionWhenRun() {
        String[] args = {};
        cpApp.setArgsParser(null);
        assertThrows(CpException.class, () -> cpApp.run(args, System.in, outputCapture));
    }

    @Test
    public void setArgsParser_properParser_idk() throws InvalidArgsException, CpException {
        String[] args = {};
        cpApp.setArgsParser(parser);

        doNothing().when(parser).parse();
        when(parser.isFileToFile()).thenReturn(false);
        when(parser.isToDirectory()).thenReturn(false);

        assertThrows(CpException.class, () -> cpApp.run(args, System.in, outputCapture));
    }
}
