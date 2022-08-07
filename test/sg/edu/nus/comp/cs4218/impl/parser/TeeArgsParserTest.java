package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.TeeException;
import sg.edu.nus.comp.cs4218.impl.parser.TeeArgsParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TeeArgsParserTest {
    private final static String FILE_A = "a.txt";
    private final static String FILE_B = "b.txt";
    private final static String NON_EXIST_FILE_C = "c.txt";
    private final static String NON_EXIST_FILE_D = "d.txt";
    private final static String FOLDER_A = "FOLDER_A";
    private final static String APPEND_FLAG = "-a";
    private final static String INVALID_FLAG = "-i";
    @TempDir
    public static Path folderPath;
    public static Path fileAPath;
    public static Path fileBPath;
    public static Path nonExistFileCPath;
    public static Path nonExistFileDPath;
    public static Path folderAPath;
    private static TeeArgsParser parser;

    @BeforeAll
    public static void setup() {
        fileAPath = folderPath.resolve(FILE_A);
        fileBPath = folderPath.resolve(FILE_B);
        nonExistFileCPath = folderPath.resolve(NON_EXIST_FILE_C);
        nonExistFileDPath = folderPath.resolve(NON_EXIST_FILE_D);
        folderAPath = folderPath.resolve(FOLDER_A);
    }

    @BeforeEach
    public void start() throws IOException {
        parser = new TeeArgsParser();
        Files.createDirectories(folderPath);
        Files.createDirectories(folderAPath);
        Files.write(fileAPath, "".getBytes());
        Files.write(fileBPath, "".getBytes());
    }

    @Test
    public void isAppending_noFlag_false() throws InvalidArgsException {
        parser.parse(fileAPath.toString(), fileBPath.toString());
        assertFalse(parser.isAppending());
    }

    @Test
    public void isAppending_appendFlag_true() throws InvalidArgsException {
        parser.parse(APPEND_FLAG, fileAPath.toString(), fileBPath.toString());
        assertTrue(parser.isAppending());
    }

    @Test
    public void isAppending_invalidFlagAppendFlag_throwInvalidArgsException() {
        assertThrows(InvalidArgsException.class, () -> parser.parse(INVALID_FLAG, APPEND_FLAG, fileAPath.toString(),
                fileBPath.toString()));
    }

    @Test
    public void isAppending_invalidFlag_throwInvalidArgsException() {
        assertThrows(InvalidArgsException.class, () -> parser.parse(INVALID_FLAG, fileAPath.toString(),
                fileBPath.toString()));
    }

    @Test
    public void getFilePaths_noPath_emptyList() throws InvalidArgsException, TeeException {
        parser.parse();
        assertEquals(parser.getFilePaths().size(), 0);
    }

    @Test
    public void getFilePaths_filePathFolderPath_throwTeeException() throws InvalidArgsException {
        parser.parse(fileAPath.toString(), folderAPath.toString());
        assertThrows(TeeException.class, () -> parser.getFilePaths());
    }

    @Test
    public void getFilePaths_filePaths_pathList() throws InvalidArgsException, TeeException {
        parser.parse(fileAPath.toString(), fileBPath.toString());
        assertEquals(fileAPath.toString(), parser.getFilePaths().get(0));
        assertEquals(fileBPath.toString(), parser.getFilePaths().get(1));
    }

    @Test
    public void getFilePaths_nonExistPathFilePath_pathList() throws InvalidArgsException, TeeException {
        parser.parse(nonExistFileCPath.toString(), fileAPath.toString());
        assertEquals(nonExistFileCPath.toString(), parser.getFilePaths().get(0));
        assertEquals(fileAPath.toString(), parser.getFilePaths().get(1));
    }

    @Test
    public void getFilePaths_nonExistPathFilePaths_pathList() throws InvalidArgsException, TeeException {
        parser.parse(nonExistFileCPath.toString(), fileAPath.toString(), fileBPath.toString());
        assertEquals(nonExistFileCPath.toString(), parser.getFilePaths().get(0));
        assertEquals(fileAPath.toString(), parser.getFilePaths().get(1));
        assertEquals(fileBPath.toString(), parser.getFilePaths().get(2));
    }

    @Test
    public void getFilePaths_nonExistPaths_pathList() throws InvalidArgsException, TeeException {
        parser.parse(nonExistFileCPath.toString(), nonExistFileDPath.toString());
        assertEquals(nonExistFileCPath.toString(), parser.getFilePaths().get(0));
        assertEquals(nonExistFileDPath.toString(), parser.getFilePaths().get(1));
    }

    @Test
    public void getFilePaths_nonExistPathsFilePath_pathList() throws InvalidArgsException, TeeException {
        parser.parse(nonExistFileCPath.toString(), nonExistFileDPath.toString(), fileAPath.toString());
        assertEquals(nonExistFileCPath.toString(), parser.getFilePaths().get(0));
        assertEquals(nonExistFileDPath.toString(), parser.getFilePaths().get(1));
        assertEquals(fileAPath.toString(), parser.getFilePaths().get(2));
    }
}
