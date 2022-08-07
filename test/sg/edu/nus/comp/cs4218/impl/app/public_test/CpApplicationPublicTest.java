package sg.edu.nus.comp.cs4218.impl.app.public_test;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.impl.app.CpApplication;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CpApplicationPublicTest {
    private static final String NONEXISTENT = "nonexistent_dir";
    private static final String SRC_A_NAME = "srcA_file.txt";
    private static final String SRC_A_DIR = "srcA_dir";
    private static final String SRC_B_NAME = "srcB_file.txt";
    private static final String SRC_B_DIR = "srcB_dir";
    private static final String DEST_NAME = "dest_file.txt";
    private static final String DEST_DIR = "dest_dir";
    private static final String SRC_CONTENT = "Source file content";
    private static final String TEMP = "temp";
    private static Path tempPath;
    private CpApplication cpApplication;

    @BeforeAll
    static void setUp() throws NoSuchFieldException, IllegalAccessException {
        tempPath = Paths.get(TestEnvironmentUtil.getCurrentDirectory(), TEMP);
    }

    @BeforeEach
    void createFiles() throws IOException {
        cpApplication = new CpApplication();
        Files.createDirectory(tempPath);
    }

    @AfterEach
    void deleteFiles() throws IOException {
        Files.walk(tempPath)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    private Path createFile(String name) throws IOException {
        Path path = tempPath.resolve(name);
        Files.createFile(path);
        return path;
    }

    private Path createDirectory(String folder) throws IOException {
        Path path = tempPath.resolve(folder);
        Files.createDirectory(path);
        return path;
    }

    private void writeToFile(Path path, String content) throws IOException {
        Files.write(path, content.getBytes());
    }

    private String[] toArgs(String flag, String... files) {
        List<String> args = new ArrayList<>();
        if (!flag.isEmpty()) {
            args.add("-" + flag);
        }
        for (String file : files) {
            args.add(Paths.get(TEMP, file).toString());
        }
        return args.toArray(new String[0]);
    }

    @Test
    void cpSrcFileToDestFile_NonemptyFileToNonExistingFileNotRecursive_NewFileCreatedWithSameContent() throws Exception {
        Path srcFile = createFile(SRC_A_NAME);
        writeToFile(srcFile, SRC_CONTENT);
        Path destFile = tempPath.resolve(DEST_NAME);
        cpApplication.cpSrcFileToDestFile(false, srcFile.toString(), destFile.toString());
        assertTrue(Files.exists(destFile));
        assertArrayEquals(SRC_CONTENT.getBytes(), Files.readAllBytes(destFile));
    }

    @Test
    void cpSrcFileToDestFile_NonemptyTextFileToNonExistingFileRecursive_newFileCreatedWithSameContent() throws Exception {
        Path srcFile = createFile(SRC_A_NAME);
        writeToFile(srcFile, SRC_CONTENT);
        Path destFile = tempPath.resolve(DEST_NAME);
        cpApplication.cpSrcFileToDestFile(true, srcFile.toString(), destFile.toString());
        assertTrue(Files.exists(destFile));
        assertArrayEquals(SRC_CONTENT.getBytes(), Files.readAllBytes(destFile));
    }

    @Test
    void cpSrcFileToDestFile_DirectoryToNonexistentDirectoryNonRecursive_Throws() throws IOException {
        createDirectory(SRC_A_DIR);
        String src = Paths.get(TEMP, SRC_A_DIR).toString();
        String dest = Paths.get(TEMP, NONEXISTENT).toString();
        assertThrows(Exception.class, () -> cpApplication.cpSrcFileToDestFile(false, src, dest));
    }

    @Test
    void cpFilesToFolder_MultipleFilesToDirectory_CopiesToDirectory() throws Exception {
        createFile(SRC_A_NAME);
        createFile(SRC_B_NAME);
        Path destDir = createDirectory(DEST_DIR);
        cpApplication.cpFilesToFolder(false, Paths.get(TEMP, DEST_DIR).toString(),
                toArgs("", SRC_A_NAME, SRC_B_NAME));
        Path destAFile = destDir.resolve(SRC_A_NAME);
        Path destBFile = destDir.resolve(SRC_B_NAME);
        assertTrue(Files.exists(destAFile));
        assertTrue(Files.exists(destBFile));
    }

    @Test
    void cpFilesToFolder_MultipleFilesToFileRecursive_Throws() throws IOException {
        createFile(SRC_A_NAME);
        createFile(SRC_B_NAME);
        createFile(DEST_NAME);
        assertThrows(Exception.class, () -> cpApplication.cpFilesToFolder(true,
                Paths.get(TEMP, DEST_NAME).toString(),
                toArgs("", SRC_A_NAME, SRC_B_NAME)));
    }

    @Test
    void cpFilesToFolder_MultipleEmptyDirectoriesToDirectory_NothingCopied() throws Exception {
        createDirectory(SRC_A_DIR);
        createDirectory(SRC_B_DIR);
        Path destDir = createDirectory(DEST_DIR);
        cpApplication.cpFilesToFolder(true, Paths.get(TEMP, DEST_DIR).toString(),
                toArgs("", SRC_A_DIR, SRC_B_DIR));
        Path destADir = destDir.resolve(SRC_A_DIR);
        Path destBDir = destDir.resolve(SRC_B_DIR);
        assertTrue(Files.exists(destADir));
        assertTrue(Files.exists(destBDir));
    }

    @Test
    void cpFilesToFolder_MultipleDirectoriesToFile_Throws() throws IOException {
        createDirectory(SRC_A_DIR);
        createDirectory(SRC_B_DIR);
        createFile(DEST_NAME);
        assertThrows(Exception.class, () -> cpApplication.cpFilesToFolder(true,
                Paths.get(TEMP, DEST_NAME).toString(),
                toArgs("", SRC_A_DIR, SRC_B_DIR)));
    }

    @Test
    void cpFilesToFolder_MultipleFilesAndDirectoriesToDirectory_CopiesToDirectory() throws Exception {
        createFile(SRC_A_NAME);
        createDirectory(SRC_B_DIR);
        Path destDir = createDirectory(DEST_DIR);
        cpApplication.cpFilesToFolder(true, Paths.get(TEMP, DEST_DIR).toString(),
                toArgs("", SRC_A_NAME, SRC_B_DIR));
        Path destAFile = destDir.resolve(SRC_A_NAME);
        Path destBFile = destDir.resolve(SRC_B_DIR);
        assertTrue(Files.exists(destAFile));
        assertTrue(Files.exists(destBFile));
    }

    @Test
    void cpFilesToFolder_MultipleFilesAndDirectoriesToFile_Throws() throws IOException {
        createFile(SRC_A_NAME);
        createDirectory(SRC_B_DIR);
        createFile(DEST_NAME);
        assertThrows(Exception.class, () -> cpApplication.cpFilesToFolder(true,
                Paths.get(TEMP, DEST_NAME).toString(),
                toArgs("", SRC_A_NAME, SRC_B_DIR)));
    }

    @Test
    void cpFilesToFolder_NonemptyTextFileToExistingDirectoryWithNoFilesNonRecursive_copiedContentToCreatedDirectoryFile() throws Exception {
        Path srcFile = createFile(SRC_A_NAME);
        writeToFile(srcFile, SRC_CONTENT);
        Path destDir = createDirectory(DEST_DIR);
        cpApplication.cpFilesToFolder(false, destDir.toString(), srcFile.toString());
        File destFile = new File(destDir.resolve(SRC_A_NAME).toString());
        assertTrue(destFile.exists());
        assertArrayEquals(SRC_CONTENT.getBytes(), Files.readAllBytes(destFile.toPath()));
    }
}