package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.testutils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.condition.OS.WINDOWS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

class CdApplicationTest {
    private static final String ERROR_PREFIX = "cd: %s";
    private static final String ERROR_PREFIX_PATH = "cd: %s: %s";
    private static final String EMPTY_PATH = " ";
    private static final String INVALID_PATH = "/world";
    private final static String FILE_ONE_NAME = "a.txt";
    private final static String FILE_ONE_CONTENT = "Hello from CS4218";
    private static final String FOLDER_NAME = "cd-test";
    private static final String NO_PERM_NAME = "not-allowed";

    @TempDir
    public static Path folderPath;
    private static Path folder;
    private static Path noPermFolder;
    private static CdApplication cdApplication;
    private static File file1;

    @BeforeAll
    static void setupCdApplication() throws IOException {
        cdApplication = new CdApplication();

        // Setting up directory & file
        FileUtils.createNewDirs(folderPath);
        Path path1 = folderPath.resolve(FILE_ONE_NAME);
        Files.writeString(path1, FILE_ONE_CONTENT);
        file1 = path1.toFile();

        folder = folderPath.resolve(FOLDER_NAME);
        FileUtils.createNewDirs(folder);

        noPermFolder = folderPath.resolve(NO_PERM_NAME);
        Files.createDirectories(noPermFolder);
        noPermFolder.toFile().setExecutable(false);
    }

    @AfterAll
    static void tearDownCdApplication() {
        FileUtils.deleteAll(folderPath.toFile());
        Environment.resetCurrentDirectory();
    }

    @Test
    void changeToDirectory_ValidPath_ChangesDirectory() throws CdException {
        cdApplication.changeToDirectory(folderPath.toString());
        assertEquals(folderPath.toString(), Environment.currentDirectory);
    }

    @Test
    void changeToDirectory_RelativePath_ChangesDirectory() throws CdException {
        Path filePath = FileUtils.getFileRelativePathToCd(folder.toFile());
        cdApplication.changeToDirectory(filePath.toString());
        assertEquals(folder.toString(), Environment.currentDirectory);
    }

    @Test
    void changeToDirectory_NullDirectory_ShouldThrowNullArgsCdException() {
        Throwable thrown = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(null));
        assertEquals(String.format(ERROR_PREFIX, ERR_NO_ARGS), thrown.getMessage());
    }

    @Test
    void changeToDirectory_EmptyPath_ShouldThrowInsufficientArgsCdException() {
        Throwable thrown = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(EMPTY_PATH));
        assertEquals(String.format(ERROR_PREFIX, ERR_NO_ARGS), thrown.getMessage());
    }

    @Test
    void changeToDirectory_InvalidPath_ShouldThrowNotFoundCdException() {
        Throwable thrown = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(INVALID_PATH));
        assertEquals(String.format(ERROR_PREFIX_PATH, INVALID_PATH, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    @Test
    void changeToDirectory_NotADirectory_ShouldThrowNotDirectoryCdException() {
        Throwable thrown = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(file1.getPath()));
        assertEquals(String.format(ERROR_PREFIX_PATH, file1.getPath(), ERR_IS_NOT_DIR), thrown.getMessage());
    }

    @DisabledOnOs(WINDOWS)
    @Test
    void changeToDirectory_NotExecutable_ShouldThrowPermissionDeniedCdException() {
        Throwable thrown = assertThrows(CdException.class, () -> cdApplication.changeToDirectory(noPermFolder.toString()));
        assertEquals(String.format(ERROR_PREFIX_PATH, noPermFolder.toString(), ERR_NO_PERM), thrown.getMessage());
    }

    @Test
    void run_ValidPath_ChangesDirectory() throws CdException {
        String[] args = {folderPath.toString()};
        cdApplication.run(args, System.in, System.out);
        assertEquals(folderPath.toString(), Environment.currentDirectory);
    }

    @Test
    void run_RelativePath_ChangesDirectory() throws CdException {
        Path filePath = FileUtils.getFileRelativePathToCd(folder.toFile());
        String[] args = {filePath.toString()};
        cdApplication.run(args, System.in, System.out);
        assertEquals(folder.toString(), Environment.currentDirectory);
    }

    @Test
    void run_MultipleArgs_ShouldThrowTooManyArgsCdException() {
        String[] args = {folderPath.toString(), INVALID_PATH};
        Throwable thrown = assertThrows(CdException.class, () -> cdApplication.run(args, System.in, System.out));
        assertEquals(String.format(ERROR_PREFIX, ERR_TOO_MANY_ARGS), thrown.getMessage());
    }

    @Test
    void run_NullArgs_ShouldThrowNullArgsCdException() {
        Throwable thrown = assertThrows(CdException.class, () -> cdApplication.run(null, System.in, System.out));
        assertEquals(String.format(ERROR_PREFIX, ERR_NULL_ARGS), thrown.getMessage());
    }

    @Test
    void run_EmptyPath_ShouldThrowInsufficientArgsCdException() {
        String[] args = {EMPTY_PATH};
        Throwable thrown = assertThrows(CdException.class, () -> cdApplication.run(args, System.in, System.out));
        assertEquals(String.format(ERROR_PREFIX, ERR_NO_ARGS), thrown.getMessage());
    }

    @Test
    void run_InvalidPath_ShouldThrowNotFoundCdException() {
        String[] args = {INVALID_PATH};
        Throwable thrown = assertThrows(CdException.class, () -> cdApplication.run(args, System.in, System.out));
        assertEquals(String.format(ERROR_PREFIX_PATH, INVALID_PATH, ERR_FILE_NOT_FOUND), thrown.getMessage());
    }

    @Test
    void run_NotADirectory_ShouldThrowNotDirectoryCdException() {
        String[] args = {file1.getPath()};
        Throwable thrown = assertThrows(CdException.class, () -> cdApplication.run(args, System.in, System.out));
        assertEquals(String.format(ERROR_PREFIX_PATH, file1.getPath(), ERR_IS_NOT_DIR), thrown.getMessage());
    }

    @DisabledOnOs(WINDOWS)
    @Test
    void run_NotExecutable_ShouldThrowPermissionDeniedCdException() {
        String[] args = {noPermFolder.toString()};
        Throwable thrown = assertThrows(CdException.class, () -> cdApplication.run(args, System.in, System.out));
        assertEquals(String.format(ERROR_PREFIX_PATH, noPermFolder.toString(), ERR_NO_PERM), thrown.getMessage());
    }

    @Test
    void run_NoArg_ShouldThrowMissingArgumentCdException() {
        Throwable thrown = assertThrows(CdException.class, () -> cdApplication.run(new String[0], System.in, System.out));
        assertEquals(String.format(ERROR_PREFIX, ERR_MISSING_ARG), thrown.getMessage());
    }
}