package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.CpException;
import sg.edu.nus.comp.cs4218.impl.parser.CpArgsParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class CpArgsParserTest {
    private final static String FILE_A = "a.txt";
    private final static String FILE_B = "b.txt";
    private final static String NON_EXIST_FILE = "nonExist.txt";
    private final static String FOLDER_A = "FOLDER_A";
    private final static String FOLDER_B = "FOLDER_B";
    private final static String LOWER_RECUR_FLAG = "-r";
    private final static String UPPER_RECUR_FLAG = "-R";
    private final static String INVALID_FLAG = "-i";
    @TempDir
    public static Path folderPath;
    public static Path fileAPath;
    public static Path fileBPath;
    public static Path nonExistFilePath;
    public static Path folderAPath;
    public static Path folderBPath;
    private static CpArgsParser parser;

    @BeforeAll
    public static void setup() {
        fileAPath = folderPath.resolve(FILE_A);
        fileBPath = folderPath.resolve(FILE_B);
        nonExistFilePath = folderPath.resolve(NON_EXIST_FILE);
        folderAPath = folderPath.resolve(FOLDER_A);
        folderBPath = folderPath.resolve(FOLDER_B);
    }

    @BeforeEach
    public void start() throws IOException {
        parser = new CpArgsParser();
        Files.createDirectories(folderPath);
        Files.createDirectories(folderAPath);
        Files.createDirectories(folderBPath);
        Files.write(fileAPath, "".getBytes());
        Files.write(fileBPath, "".getBytes());
    }

    @Test
    public void isRecursive_lowerUpperFlags_true() throws InvalidArgsException {
        parser.parse(LOWER_RECUR_FLAG, UPPER_RECUR_FLAG, fileAPath.toString(), fileBPath.toString());
        assertTrue(parser.isRecursive());
    }

    @Test
    public void isRecursive_invalidLowerFlags_throwException() {
        assertThrows(InvalidArgsException.class, () -> parser.parse(INVALID_FLAG, LOWER_RECUR_FLAG,
                fileAPath.toString(), fileBPath.toString()));
    }

    @Test
    public void isRecursive_invalidFlag_throwException() {
        assertThrows(InvalidArgsException.class, () -> parser.parse(INVALID_FLAG, fileAPath.toString(),
                fileBPath.toString()));
    }

    @Test
    public void isRecursive_invalidUpperFlags_throwException() {
        assertThrows(InvalidArgsException.class, () -> parser.parse(INVALID_FLAG, UPPER_RECUR_FLAG,
                fileAPath.toString(), fileBPath.toString()));
    }

    @Test
    public void isRecursive_upperFlag_true() throws InvalidArgsException {
        parser.parse(UPPER_RECUR_FLAG, fileAPath.toString(), fileBPath.toString());
        assertTrue(parser.isRecursive());
    }

    @Test
    public void isRecursive_lowerFlag_true() throws InvalidArgsException {
        parser.parse(LOWER_RECUR_FLAG, fileAPath.toString(), fileBPath.toString());
        assertTrue(parser.isRecursive());
    }

    @Test
    public void isRecursive_noFlag_false() throws InvalidArgsException {
        parser.parse(fileAPath.toString(), fileBPath.toString());
        assertFalse(parser.isRecursive());
    }

    @Test
    public void isFileToFile_noPath_throwCpException() throws InvalidArgsException {
        parser.parse();
        assertThrows(CpException.class, () -> parser.isFileToFile());
    }

    @Test
    public void isFileToFile_filePathFolderPath_false() throws InvalidArgsException, CpException {
        parser.parse(fileAPath.toString(), folderAPath.toString());
        assertFalse(parser.isFileToFile());
    }

    @Test
    public void isFileToFile_filePathsFolderPaths_false() throws InvalidArgsException, CpException {
        parser.parse(fileAPath.toString(), fileBPath.toString(), folderAPath.toString(), folderBPath.toString());
        assertFalse(parser.isFileToFile());
    }

    @Test
    public void isFileToFile_nonExistPathFilePathFolderPaths_false() throws InvalidArgsException, CpException {
        parser.parse(nonExistFilePath.toString(), fileAPath.toString(), folderAPath.toString(), folderBPath.toString());
        assertFalse(parser.isFileToFile());
    }

    @Test
    public void isFileToFile_filePathsFolderPath_false() throws InvalidArgsException, CpException {
        parser.parse(fileAPath.toString(), fileBPath.toString(), folderAPath.toString());
        assertFalse(parser.isFileToFile());
    }

    @Test
    public void isFileToFile_folderPaths_false() throws InvalidArgsException, CpException {
        parser.parse(folderAPath.toString(), folderBPath.toString());
        assertFalse(parser.isFileToFile());
    }

    @Test
    public void isFileToFile_filePath_throwCpException() throws InvalidArgsException {
        parser.parse(fileAPath.toString());
        assertThrows(CpException.class, () -> parser.isFileToFile());
    }

    @Test
    public void isFileToFile_filePaths_true() throws InvalidArgsException, CpException {
        parser.parse(fileAPath.toString(), fileBPath.toString());
        assertTrue(parser.isFileToFile());
    }

    @Test
    public void isToDirectory_noPath_throwCpException() throws InvalidArgsException {
        parser.parse();
        assertThrows(CpException.class, () -> parser.isToDirectory());
    }

    @Test
    public void isToDirectory_filePathFolderPathFolderNotAtBack_throwCpException() throws InvalidArgsException {
        parser.parse(folderAPath.toString(), fileAPath.toString());
        assertThrows(CpException.class, () -> parser.isToDirectory());
    }

    @Test
    public void isToDirectory_filePathsFolderPathsFolderAtBack_true() throws InvalidArgsException, CpException {
        parser.parse(fileAPath.toString(), fileBPath.toString(), folderAPath.toString(), folderBPath.toString());
        assertTrue(parser.isToDirectory());
    }

    @Test
    public void isToDirectory_filePathsFolderPathFolderAtBack_true() throws InvalidArgsException, CpException {
        parser.parse(fileAPath.toString(), fileBPath.toString(), folderAPath.toString());
        assertTrue(parser.isToDirectory());
    }

    @Test
    public void isToDirectory_folderPaths_true() throws InvalidArgsException, CpException {
        parser.parse(folderAPath.toString(), folderBPath.toString());
        assertTrue(parser.isToDirectory());
    }

    @Test
    public void isToDirectory_filePath_throwCpException() throws InvalidArgsException {
        parser.parse(fileAPath.toString());
        assertThrows(CpException.class, () -> parser.isToDirectory());
    }

    @Test
    public void isToDirectory_nonExistPathFilePathsFolderPathFolderPathAtBack_false() throws InvalidArgsException,
            CpException {
        parser.parse(nonExistFilePath.toString(), fileAPath.toString(), fileBPath.toString(), folderPath.toString());
        assertTrue(parser.isToDirectory());
    }

    @Test
    public void getSrcPath_noPath_throwCpException() throws InvalidArgsException {
        parser.parse();
        assertThrows(CpException.class, () -> parser.getSrcPath());
    }

    @Test
    public void getSrcPath_path_throwCpException() throws InvalidArgsException {
        parser.parse(fileAPath.toString());
        assertThrows(CpException.class, () -> parser.getSrcPath());
    }

    @Test
    public void getSrcPath_paths_path() throws InvalidArgsException, CpException {
        parser.parse(fileAPath.toString(), fileBPath.toString());
        assertEquals(parser.getSrcPath(), fileAPath.toString());
    }

    @Test
    public void getSrcPaths_noPath_throwCpException() throws InvalidArgsException {
        parser.parse();
        assertThrows(CpException.class, () -> parser.getSrcPaths());
    }

    @Test
    public void getSrcPaths_path_throwCpException() throws InvalidArgsException {
        parser.parse(fileAPath.toString());
        assertThrows(CpException.class, () -> parser.getSrcPaths());
    }

    @Test
    public void getSrcPaths_twoPaths_returnPath() throws InvalidArgsException, CpException {
        parser.parse(fileAPath.toString(), folderAPath.toString());
        assertTrue(parser.getSrcPaths().size() == 1
                && parser.getSrcPaths().get(0).equals(fileAPath.toString()));
    }

    @Test
    public void getSrcPaths_threePaths_returnPath() throws InvalidArgsException, CpException {
        parser.parse(fileAPath.toString(), fileBPath.toString(), folderAPath.toString());
        assertTrue(parser.getSrcPaths().size() == 2
                && parser.getSrcPaths().get(0).equals(fileAPath.toString())
                && parser.getSrcPaths().get(1).equals(fileBPath.toString()));
    }

    @Test
    public void getDestPath_noPath_throwCpException() throws InvalidArgsException {
        parser.parse();
        assertThrows(CpException.class, () -> parser.getDestPath());
    }

    @Test
    public void getDestPath_path_throwCpException() throws InvalidArgsException {
        parser.parse(fileAPath.toString());
        assertThrows(CpException.class, () -> parser.getDestPath());
    }

    @Test
    public void getDestPath_paths_path() throws InvalidArgsException, CpException {
        parser.parse(fileAPath.toString(), fileBPath.toString());
        assertEquals(parser.getDestPath(), fileBPath.toString());
    }
}
