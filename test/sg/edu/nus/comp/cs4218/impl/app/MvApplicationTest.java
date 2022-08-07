package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.MvException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createAllFileNFolder;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.deleteAll;

public class MvApplicationTest {

    /* TEST FOLDER STRUCTURE:
        test-mv/
        ├─ deep-folder/
        │  ├─ other-folder/
        │  ├─ existingFile.java
        ├─ deep-folder2/
        │  ├─ existingFile.java
        ├─ other-folder/
        │  ├─ test.txt
        ├─ test.txt
        ├─ test.java
        ├─ existingFile.java
    */

    // Test Files
    static final String TEST_FOLDER = "test-mv";
    static final String DESTFILE_STRING = TEST_FOLDER + File.separator + "test2.txt";
    static final File DESTFILE = new File(DESTFILE_STRING);
    static final String INVAL_FOLDER_STR = TEST_FOLDER + File.separator + "invalidFolder";
    static final String NEWFOLDER_STR = TEST_FOLDER + File.separator + "new-folder";
    static final File NEWFOLDER = new File(NEWFOLDER_STR);
    static final String N_OPTION = "-n";
    static File rootTestFolder = new File(TEST_FOLDER);
    static File[] dirs = new File[]{
            new File(Paths.get(rootTestFolder.toString(), "deep-folder").toUri()),
            new File(Paths.get(rootTestFolder.toString(), "deep-folder2").toUri()),
            new File(Paths.get(rootTestFolder.toString(), "deep-folder", "other-folder").toUri()),
            new File(Paths.get(rootTestFolder.toString(), "other-folder").toUri()),
    };
    static File[] files = new File[]{
            new File("test-mv/test.txt"),
            new File("test-mv/test.java"),
            new File("test-mv/existingFile.java"),
            new File(Paths.get(dirs[0].getPath(), "existingFile.java").toUri()),
            new File(Paths.get(dirs[1].getPath(), "existingFile.java").toUri()),
            new File(Paths.get(dirs[3].getPath(), "test.txt").toUri()),
    };
    // VARIABLES for mvSrcFileToDestFile
    static final String SRCFILE_STRING = files[0].toString();
    static final File SRCFILE = files[0];
    static final String SRCFILE1_STRING = files[3].toString(); // SRCFILE1 & SRCFILE2 has the same name in diff folder
    static final File SRCFILE1 = files[3];
    static final String SRCFILE2_STRING = files[4].toString();
    static final File SRCFILE2 = files[4];
    static final String EXISTINGSRC_STR = files[2].toString();
    static final File EXISTINGSRC = files[2];
    // VARIABLE for mvFilesToFolder
    static final String SRCFOLDER_STRING = dirs[1].toString(); // Has the same file in destFolder
    static final File SRCFOLDER = dirs[1];
    static final String SRCFOLDER2_STRING = dirs[3].toString(); // No same file in destFolder
    static final File SRCFOLDER2 = dirs[3];
    static final String DESTFOLDER_STRING = dirs[0].toString();
    static final File DESTFOLDER = dirs[0];
    MvApplication mvApp;
    MvApplication mockMvApp;
    InputStream inputStreamStdIn = mock(ByteArrayInputStream.class);
    ByteArrayOutputStream stdOutResult;

    @BeforeAll
    static void reset() {
        Environment.resetCurrentDirectory();
    }

    @BeforeEach
    void setup() throws Exception {
        mvApp = new MvApplication();
        mockMvApp = mock(MvApplication.class);
        doCallRealMethod().when(mockMvApp).run(any(), any(), any());
        stdOutResult = new ByteArrayOutputStream();
        createAllFileNFolder(rootTestFolder, dirs, files);
        System.setOut(new PrintStream(stdOutResult));
    }

    @AfterEach
    void tearDown() {
        deleteAll(rootTestFolder);
    }

    // Test case for mvSrcFileToDestFile()
    // isOverWrite (T/F), srcFile(File, folder), destFile(File / Existing File / Existing file in folder)
    // 1: False, file, file
    @Test
    public void mvSrcFileToDestFile_FFileToFile_Success() throws Exception {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE.exists());
        assertFalse(DESTFILE.exists());

        // Move file
        mvApp.mvSrcFileToDestFile(false, SRCFILE_STRING, DESTFILE_STRING);

        // Result
        assertFalse(SRCFILE.exists());
        assertTrue(DESTFILE.exists());
    }

    // 2: True, file, file
    @Test
    public void mvSrcFileToDestFile_TFileToFile_Success() throws Exception {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE.exists());
        assertFalse(DESTFILE.exists());

        // Move file
        mvApp.mvSrcFileToDestFile(true, SRCFILE_STRING, DESTFILE_STRING);

        // Result
        assertFalse(SRCFILE.exists());
        assertTrue(DESTFILE.exists());
    }

    // 3: False, file, existing file in current directory
    @Test
    public void mvSrcFileToDestFile_FFileToExistingFile_Failure() throws Exception {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE1.exists());
        assertTrue(EXISTINGSRC.exists());

        // Move File
        mvApp.mvSrcFileToDestFile(false, SRCFILE1_STRING, EXISTINGSRC_STR);

        // Result
        assertTrue(SRCFILE1.exists()); // Doesn't overwrite, so both exist
        assertTrue(EXISTINGSRC.exists());
    }

    // 4: False, file, existing file in current directory
    @Test
    public void mvSrcFileToDestFile_TFileToExistingFile_Success() throws Exception {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE1.exists());
        assertTrue(EXISTINGSRC.exists());

        // Move File
        mvApp.mvSrcFileToDestFile(true, SRCFILE1_STRING, EXISTINGSRC_STR);

        // Result
        assertFalse(SRCFILE1.exists());
        assertTrue(EXISTINGSRC.exists());
    }

    // 5: False, folder, new folder
    @Test
    public void mvSrcFileToDestFile_FFolderToNewFolder_Success() throws Exception {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFOLDER.exists());
        assertFalse(NEWFOLDER.exists());

        // Move file
        mvApp.mvSrcFileToDestFile(false, SRCFOLDER_STRING, NEWFOLDER_STR);

        assertFalse(SRCFOLDER.exists());
        assertTrue(NEWFOLDER.exists());
    }

    // 6: True, folder, new folder
    @Test
    public void mvSrcFileToDestFile_TFolderToNewFolder_Success() throws Exception {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFOLDER.exists());
        assertFalse(NEWFOLDER.exists());

        // Move file
        mvApp.mvSrcFileToDestFile(true, SRCFOLDER_STRING, NEWFOLDER_STR);

        assertFalse(SRCFOLDER.exists());
        assertTrue(NEWFOLDER.exists());
    }

    // 7: False, file, new folder
    @Test
    public void mvSrcFileToDestFile_FFileToNewFolder_Success() throws Exception {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE.exists());
        assertFalse(NEWFOLDER.exists());

        // Move file
        mvApp.mvSrcFileToDestFile(false, SRCFILE_STRING, NEWFOLDER_STR);

        assertFalse(SRCFILE.exists());
        assertTrue(NEWFOLDER.exists());
    }

    // 8: False, file, new folder
    @Test
    public void mvSrcFileToDestFile_TFileToNewFolder_Success() throws Exception {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE.exists());
        assertFalse(NEWFOLDER.exists());

        // Move file
        mvApp.mvSrcFileToDestFile(true, SRCFILE_STRING, NEWFOLDER_STR);

        assertFalse(SRCFILE.exists());
        assertTrue(NEWFOLDER.exists());
    }

    // 9: False, file, file existing in other directory
    @Test
    public void mvSrcFileToDestFile_FFileToExistFile_Success() throws Exception {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE1.exists());
        assertTrue(SRCFILE2.exists());

        // Move file
        mvApp.mvSrcFileToDestFile(false, SRCFILE1_STRING, SRCFILE2_STRING);

        assertTrue(SRCFILE1.exists()); // Doesn't overwrite
        assertTrue(SRCFILE2.exists());
    }

    // 10: False, file, file existing in other directory
    @Test
    public void mvSrcFileToDestFile_TFileToExistFile_Success() throws Exception {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE1.exists());
        assertTrue(SRCFILE2.exists());

        // Move file
        mvApp.mvSrcFileToDestFile(true, SRCFILE1_STRING, SRCFILE2_STRING);

        assertFalse(SRCFILE1.exists()); // Overwritten SRCFILE2, so now it is gone
        assertTrue(SRCFILE2.exists());
    }

    // Negative Test Case (Null source / destFile / same file name, invalid srcFile)
    @Test
    public void mvSrcFileToDestFile_NullSrcFile_Failure() throws MvException {
        String errorMsg = mvApp.mvSrcFileToDestFile(true, null, DESTFILE_STRING);
        assertTrue(errorMsg.contains(ERR_FILE_NOT_FOUND));
    }

    @Test
    public void mvSrcFileToDestFile_NullSrcFolder_Failure() throws MvException {
        String errorMsg = mvApp.mvSrcFileToDestFile(true, SRCFILE_STRING, null);
        assertTrue(errorMsg.contains(ERR_FILE_NOT_FOUND));
    }

    @Test
    public void mvSrcFileToDestFile_TInvalidFileToFile_Failure() throws MvException {
        // Ensure that the file's status is correct for this test case
        final String invalidFileStr = TEST_FOLDER + File.separator + "invalid.txt";
        final File invalidFile = new File(invalidFileStr);
        assertFalse(invalidFile.exists());
        assertTrue(SRCFILE.exists());

        // Move file
        String errorMsg = mvApp.mvSrcFileToDestFile(true, invalidFileStr, SRCFILE_STRING);
        assertTrue(errorMsg.contains(ERR_FILE_NOT_FOUND));
    }

    // Fix for bug 15
    @Test
    public void mvSrcFileToDestFile_SameFile_Failure() throws MvException {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE.exists());

        // Move file
        String errorMsg = mvApp.mvSrcFileToDestFile(true, SRCFILE_STRING, SRCFILE_STRING);
        assertTrue(errorMsg.contains(ERR_COPY_TO_SELF));
    }

    // Test case for mvFilesToFolder()
    // isOverwrite (T/F), destFolder (Existing Folder), files (1 source, > 1 source, existingFile, some need to overwrite while some don't need  )
    // Combination = 8 test cases
    // 1: False, folder, 1 source
    @Test
    public void mvFilesToFolder_F1FSource_Success() throws Exception {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE.exists());
        assertTrue(DESTFOLDER.exists());
        long destFolderLen = Objects.requireNonNull(DESTFOLDER.list()).length;

        // Move File
        mvApp.mvFilesToFolder(false, DESTFOLDER_STRING, SRCFILE_STRING);

        assertFalse(SRCFILE.exists());
        assertTrue(DESTFOLDER.exists());
        assertEquals(destFolderLen + 1, Objects.requireNonNull(DESTFOLDER.list()).length);
    }

    // 2: True, folder, 1 source
    @Test
    public void mvFilesToFolder_T1Source_Success() throws Exception {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFOLDER2.exists());
        assertTrue(DESTFOLDER.exists());
        long destFolderLen = Objects.requireNonNull(DESTFOLDER.listFiles()).length;

        // Move File
        mvApp.mvFilesToFolder(true, DESTFOLDER_STRING, SRCFOLDER2_STRING);

        assertFalse(SRCFOLDER2.exists());
        assertTrue(DESTFOLDER.exists());
        assertEquals(destFolderLen, Objects.requireNonNull(DESTFOLDER.listFiles()).length); // No new files are created
    }

    // 3: False, folder, multi source
    @Test
    public void mvFilesToFolder_FMultiSource_Success() throws Exception {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE.exists());
        assertTrue(SRCFILE2.exists());
        assertTrue(DESTFOLDER.exists());
        long destFolderLen = Objects.requireNonNull(DESTFOLDER.list()).length;

        // Move File
        mvApp.mvFilesToFolder(false, DESTFOLDER_STRING, SRCFILE_STRING, SRCFILE2_STRING);

        assertFalse(SRCFILE.exists());
        assertTrue(SRCFILE2.exists()); // True because of overlapping existFile.txt in SRCFIle2 and isOverwite is false
        assertTrue(DESTFOLDER.exists());
        assertEquals(destFolderLen + 1, Objects.requireNonNull(DESTFOLDER.list()).length);
    }

    // 4: True, folder, multi source
    @Test
    public void mvFilesToFolder_TMultiSource_Success() throws Exception {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFOLDER2.exists());
        assertTrue(SRCFILE.exists());
        assertTrue(DESTFOLDER.exists());
        long destFolderLen = Objects.requireNonNull(DESTFOLDER.list()).length;

        // Move File
        mvApp.mvFilesToFolder(false, DESTFOLDER_STRING, SRCFOLDER2_STRING, SRCFILE_STRING);

        assertTrue(SRCFOLDER2.exists()); // It will still exist because the common test.txt is not edited because isOverWrite is false
        assertFalse(SRCFILE.exists());
        assertTrue(DESTFOLDER.exists());
        assertEquals(destFolderLen + 1, Objects.requireNonNull(DESTFOLDER.list()).length);
    }

    // 5: False, folder, existing file in folder
    @Test
    public void mvFilesToFolder_FExistingFileInFolder_Failure() throws Exception {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE.exists());
        assertTrue(SRCFOLDER2.exists());
        long destFolderLen = Objects.requireNonNull(SRCFOLDER2.list()).length;

        // Move File
        mvApp.mvFilesToFolder(false, SRCFOLDER2_STRING, SRCFILE_STRING);

        // Does nothing
        assertTrue(SRCFILE.exists());
        assertTrue(SRCFOLDER2.exists());
        assertEquals(destFolderLen, Objects.requireNonNull(SRCFOLDER2.list()).length);
    }

    // 6: False, folder, existing file in folder
    @Test
    public void mvFilesToFolder_TExistingFileInFolder_Failure() throws Exception {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE.exists());
        assertTrue(SRCFOLDER2.exists());
        long destFolderLen = Objects.requireNonNull(SRCFOLDER2.list()).length;

        // Move File
        mvApp.mvFilesToFolder(false, SRCFOLDER2_STRING, SRCFILE_STRING);

        // Overwrites
        assertTrue(SRCFILE.exists()); // Will still exist because of overlapping folder and isOverwrite is false
        assertTrue(SRCFOLDER2.exists());
        assertEquals(destFolderLen, Objects.requireNonNull(SRCFOLDER2.list()).length);
    }

    // 7: False, folder, some source needs to overwrite, some don’t need to
    @Test
    public void mvFilesToFolder_FMixSourceType_Success() throws Exception {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE.exists());
        assertTrue(EXISTINGSRC.exists());
        assertTrue(DESTFOLDER.exists());
        long destFolderLen = Objects.requireNonNull(DESTFOLDER.list()).length;

        // Move File
        mvApp.mvFilesToFolder(false, DESTFOLDER_STRING, EXISTINGSRC_STR, SRCFILE_STRING);

        // Existing src doesn't overwrite, hence it still exists
        assertFalse(SRCFILE.exists());
        assertTrue(EXISTINGSRC.exists());
        assertTrue(DESTFOLDER.exists());
        assertEquals(destFolderLen + 1, Objects.requireNonNull(DESTFOLDER.list()).length);
    }

    // 8: False, folder, some source needs to overwrite, some don’t need to
    @Test
    public void mvFilesToFolder_TMixSourceType_Success() throws Exception {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE.exists());
        assertTrue(EXISTINGSRC.exists());
        assertTrue(DESTFOLDER.exists());
        long destFolderLen = Objects.requireNonNull(DESTFOLDER.list()).length;

        // Move File
        mvApp.mvFilesToFolder(true, DESTFOLDER_STRING, EXISTINGSRC_STR, SRCFILE_STRING);

        // Existing src doesn't overwrite, hence it still exists
        assertFalse(SRCFILE.exists());
        assertFalse(EXISTINGSRC.exists());
        assertTrue(DESTFOLDER.exists());
        assertEquals(destFolderLen + 1, Objects.requireNonNull(DESTFOLDER.list()).length);
    }

    // Negative Case (Invalid destFolder, null dest folder, null src file)
    @Test
    public void mvFilesToFolder_invalidDestFile_Failure() throws MvException {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE.exists());

        // Move file
        String errorMsg = mvApp.mvFilesToFolder(true, INVAL_FOLDER_STR, SRCFILE_STRING);
        assertTrue(errorMsg.contains(ERR_NO_PERM));
    }

    @Test
    public void mvSrcFileToDestFile_nullDestFile_Failure() throws MvException {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE.exists());

        // Move file
        String errorMsg = mvApp.mvFilesToFolder(true, null, SRCFILE_STRING);
        assertTrue(errorMsg.contains(ERR_FILE_NOT_FOUND));
    }

    @Test
    public void mvFilesToFolder_nullSrcFile_Failure() throws MvException {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE.exists());

        // Move file
        String errorMsg = mvApp.mvFilesToFolder(true, DESTFOLDER_STRING, SRCFILE_STRING, null);
        assertTrue(errorMsg.contains(ERR_FILE_NOT_FOUND));
    }

    // Same folder
    @Test
    public void mvFilesToFolder_sameFolder_Failure() throws MvException {
        // Move file
        String errorMsg = mvApp.mvFilesToFolder(true, SRCFOLDER_STRING, SRCFOLDER_STRING);
        assertTrue(errorMsg.contains(ERR_COPY_TO_SELF));
    }

    // Test case for run()
    // Ensure that run calls the correct method (If last arg is Directory, should call mvFilesToFolder
    // , else calls mvSrcFileToDestFile
    // Test option independently

    // 0: Options anywhere
    @Test
    public void run_optionAnywhere_onlyInOrderOptionsRun() throws Exception {
        String[] args1 = {N_OPTION, SRCFILE_STRING, SRCFILE2_STRING};
        String[] args2 = {SRCFILE_STRING, N_OPTION, SRCFILE2_STRING};

        mvApp.run(args1, inputStreamStdIn, stdOutResult);
        mvApp.run(args2, inputStreamStdIn, stdOutResult);
        assertTrue(stdOutResult.toString().contains(ERR_IS_NOT_DIR));
    }

    // 1: True, Source, existing folder
    @Test
    public void run_T1sourceToExistingFolder_Success() throws Exception {
        String[] args = {SRCFILE_STRING, DESTFOLDER_STRING};
        mockMvApp.run(args, inputStreamStdIn, stdOutResult);
        verify(mockMvApp, atLeastOnce()).mvFilesToFolder(true, DESTFOLDER_STRING, SRCFILE_STRING);
    }

    // 2: False, Multi Source, existing folder
    @Test
    public void run_FMultiSourceToExistingFolder_Success() throws Exception {
        String[] args = {N_OPTION, SRCFILE_STRING, SRCFILE2_STRING, DESTFOLDER_STRING};
        mockMvApp.run(args, inputStreamStdIn, stdOutResult);
        verify(mockMvApp, atLeastOnce()).mvFilesToFolder(false, DESTFOLDER_STRING, SRCFILE_STRING, SRCFILE2_STRING);
    }

    // 3: True, Source, non-existing folder
    @Test
    public void run_F1sourceToNonExistingFolder_Success() throws Exception {
        String[] args = {N_OPTION, SRCFILE_STRING, NEWFOLDER_STR};
        mockMvApp.run(args, inputStreamStdIn, stdOutResult);
        verify(mockMvApp, atLeastOnce()).mvSrcFileToDestFile(false, SRCFILE_STRING, NEWFOLDER_STR);
    }

    // 4: True, Multi Source, non-existing folder
    // mv [Option] SOURCE TARGET only accepts 1 SOURCE
    @Test
    public void run_TMultiSourceToNonExistingFolder_Failure() throws AbstractApplicationException {
        String[] args = {SRCFILE_STRING, SRCFILE2_STRING, NEWFOLDER_STR};
        mvApp.run(args, inputStreamStdIn, stdOutResult);
        assertTrue(stdOutResult.toString().contains(ERR_IS_NOT_DIR));
    }

    // 5: True, Source, file
    @Test
    public void run_T1sourceToFile_Success() throws Exception {
        String[] args = {N_OPTION, SRCFILE_STRING, DESTFILE_STRING};
        mockMvApp.run(args, inputStreamStdIn, stdOutResult);
        verify(mockMvApp, atLeastOnce()).mvSrcFileToDestFile(false, SRCFILE_STRING, DESTFILE_STRING);
    }

    // 6: False, Source, file
    // mv [Option] SOURCE TARGET only accepts 1 SOURCE
    @Test
    public void run_FMultiSourceToFile_Failure() throws AbstractApplicationException {
        String[] args = {N_OPTION, SRCFILE_STRING, SRCFILE2_STRING, DESTFILE_STRING};
        mvApp.run(args, inputStreamStdIn, stdOutResult);
        assertTrue(stdOutResult.toString().contains(ERR_IS_NOT_DIR));
    }

    // Negative Test Cases
    @Test
    public void run_notEnoughFile1_Failure() {
        String[] args = {SRCFILE_STRING};

        assertThrows(Exception.class,
                () -> mvApp.run(args, inputStreamStdIn, stdOutResult));
    }

    @Test
    public void run_notEnoughFile2_Failure() {
        String[] args = {N_OPTION, SRCFILE_STRING};

        assertThrows(Exception.class,
                () -> mvApp.run(args, inputStreamStdIn, stdOutResult));
    }

    @Test
    public void run_nullInArg_Failure() {
        String[] args = {N_OPTION, SRCFILE_STRING, null};

        assertThrows(Exception.class,
                () -> mvApp.run(args, inputStreamStdIn, stdOutResult));
    }

    @Test
    public void run_nullStdOut_Failure() {
        String[] args = {N_OPTION, SRCFILE_STRING, SRCFILE_STRING};

        assertThrows(Exception.class,
                () -> mvApp.run(args, inputStreamStdIn, null));
    }

    @Test
    public void run_invalidFile_Failure() throws AbstractApplicationException {
        String[] args = {INVAL_FOLDER_STR, SRCFILE_STRING};

        mvApp.run(args, inputStreamStdIn, stdOutResult);
        assertTrue(stdOutResult.toString().contains(ERR_FILE_NOT_FOUND));
    }
}
