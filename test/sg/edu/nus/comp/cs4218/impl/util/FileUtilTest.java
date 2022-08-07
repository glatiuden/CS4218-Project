package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createAllFileNFolder;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.deleteAll;

public class FileUtilTest {
    /* TEST FOLDER STRUCTURE:
        test-file/
        ├─ deep-folder/
        │  ├─ second-folder/
        │  ├─ test2.txt
        │  ├─ test3.a
        ├─ test.txt
    */

    // Test Files
    static final String FILENAME = "test.txt";
    static final String FILENAME2 = "test2.txt";
    static final String FILENAME3 = "test3.a";
    static final String FOLDER_NAME = "deep-folder";
    static final String SEC_FOLDER = "second-folder";
    // VARIABLES AND CONSTANT FOR SORT FILES
    static final String UPPER_A_TXT = "Ab.txt";
    static final String ABC_NO_EXT = "abc";
    static final String SMALL_A_TXT = "aBc.txt";
    static final String C_EXT = "c.c";
    static final String NO_EXT = "noExtension";
    static final String UPPER_Z_TXT = "Za.txt";
    static final String COMPARE_SEC_TXT = "ZA.txt";
    static final String BIG_T_EXT = "ZA.Txt";
    static File rootTestFolder = new File("test-file");
    static File[] dirs = new File[]{
            new File(Paths.get(rootTestFolder.toString(), FOLDER_NAME).toUri()),
            new File(Paths.get(rootTestFolder.toString(), FOLDER_NAME, SEC_FOLDER).toUri()),
    };
    static File[] files = new File[]{
            new File("test-file/test.txt"),
            new File(Paths.get(dirs[0].getPath(), FILENAME2).toUri()),
            new File(Paths.get(dirs[0].getPath(), FILENAME3).toUri()),
    };
    List<String> allFiles = List.of(UPPER_A_TXT, ABC_NO_EXT, SMALL_A_TXT, C_EXT, NO_EXT, UPPER_Z_TXT, COMPARE_SEC_TXT, BIG_T_EXT);

    // These are only used for getContents() test cases
    static void setup() throws Exception {
        createAllFileNFolder(rootTestFolder, dirs, files);
        Environment.currentDirectory = rootTestFolder.getAbsolutePath();
    }

    static void tearDown() {
        deleteAll(rootTestFolder);
        Environment.resetCurrentDirectory();
    }

    @AfterAll
    static void reset() {
        tearDown();
    }

    // Test case for getContents() starts here
    @Test
    public void getContents_1nesting_ReturnTrue() throws Exception {
        tearDown();
        setup();
        Path directory = rootTestFolder.toPath();
        List<Path> contents = FileUtil.getContents(directory);
        assertEquals(2, contents.size());
        assertEquals("test-file" + File.separator + "deep-folder", contents.get(0).toString());
        assertEquals("test-file" + File.separator + FILENAME, contents.get(1).toString());
    }

    @Test
    public void getContents_deepNesting_ReturnTrue() throws Exception {
        tearDown();
        setup();
        Path directory = dirs[0].toPath();
        List<Path> contents = FileUtil.getContents(directory);
        assertEquals(3, contents.size());
        assertEquals(dirs[1].getAbsolutePath(), contents.get(0).toString());
        assertEquals(files[1].getAbsolutePath(), contents.get(1).toString());
        assertEquals(files[2].getAbsolutePath(), contents.get(2).toString());
    }

    @Test
    public void getContents_file_ThrowsException() throws Exception {
        tearDown();
        setup();
        Path directory = files[0].toPath();
        assertThrows(IllegalArgumentException.class, () -> FileUtil.getContents(directory));
    }

    // Test case for getRelativeToCwd() starts
    // No nesting
    @Test
    public void getRelativeToCwd_noNesting_ReturnTrue() {
        tearDown();
        String pathName = "test.abc";
        Path fullPath = new File(pathName).getAbsoluteFile().toPath();
        Path relativePath = FileUtil.getRelativeToCwd(fullPath);
        assertEquals(pathName, relativePath.toString());
    }

    // With nesting
    @Test
    public void getRelativeToCwd_hasNesting_ReturnTrue() {
        tearDown();
        String pathName = "test.abc" + File.separator + "something" + File.separator + ".gitignore";
        Path fullPath = new File(pathName).getAbsoluteFile().toPath();
        Path relativePath = FileUtil.getRelativeToCwd(fullPath);
        assertEquals(pathName, relativePath.toString());
    }

    // Unknown file that is not relative to current directory
    @Test
    public void getRelativeToCwd_invalidPath_ThrowsException() {
        String pathName = "invalidPath";
        Path fullPath = new File(pathName).toPath();
        assertThrows(IllegalArgumentException.class, () -> FileUtil.getRelativeToCwd(fullPath));
    }

    // Test case for resolvePaths() starts (Ensure that resolvePath is called)
    @Test
    public void resolvesPath_allTypes_ReturnTrue() {
        String absolutePath = Environment.currentDirectory + File.separator + "test";
        String pathName = FILENAME;
        List<Path> resolvedPaths = FileUtil.resolvePaths(absolutePath, Environment.currentDirectory, pathName);
        assertEquals(absolutePath, resolvedPaths.get(0).toString());
        assertEquals(Environment.currentDirectory, resolvedPaths.get(1).toString());
        assertEquals(Environment.currentDirectory + File.separator + pathName, resolvedPaths.get(2).toString());

    }

    // Test case for resolvePath() starts
    // Absolute Path
    @Test
    public void resolvePath_absolutePath_ReturnTrue() {
        String absolutePath = Environment.currentDirectory + File.separator + "test";
        Path resolvedPath = FileUtil.resolvePath(absolutePath);
        assertEquals(absolutePath, resolvedPath.toString());
    }

    // Current Directory
    @Test
    public void resolvePath_currentDir_ReturnTrue() {
        String pathName = Environment.currentDirectory;
        Path resolvedPath = FileUtil.resolvePath(pathName);
        assertEquals(Environment.currentDirectory, resolvedPath.toString());
    }

    // Normal file
    @Test
    public void resolvePath_normalFile_ReturnTrue() {
        String pathName = FILENAME;
        Path resolvedPath = FileUtil.resolvePath(pathName);
        assertEquals(Environment.currentDirectory + File.separator + pathName, resolvedPath.toString());
    }

    // Test case for sortFiles() starts
    // Basically just have 1 test case to ensure that the all the non-ext is at the front, then c.c is before all the .txt
    // and then within each .txt, sort by their natural order, for ZA.txt and Za.txt, ensure that ZA.txt comes first
    @Test
    public void sortFiles_sortFile_CorrectlySorted() {
        List<String> expectedAns = List.of(
                ABC_NO_EXT, NO_EXT, BIG_T_EXT, C_EXT, UPPER_A_TXT, COMPARE_SEC_TXT, UPPER_Z_TXT, SMALL_A_TXT
        );
        List<String> sortedFiles = FileUtil.sortFiles(allFiles);
        for (int i = 0; i < sortedFiles.size(); i++) {
            assertEquals(sortedFiles.get(i), expectedAns.get(i));
        }
    }
}
