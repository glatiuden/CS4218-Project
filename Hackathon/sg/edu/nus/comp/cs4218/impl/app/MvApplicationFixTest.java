package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.MvException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_COPY_TO_SELF;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createAllFileNFolder;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.deleteAll;

public class MvApplicationFixTest {

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
    MvApplication mvApp;
    MvApplication mockMvApp;
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

    // Fix for bug 15
    @Test
    public void mvSrcFileToDestFile_SameFile_Failure() throws MvException {
        // Ensure that the file's status is correct for this test case
        assertTrue(SRCFILE.exists());

        // Move file
        String errorMsg = mvApp.mvSrcFileToDestFile(true, SRCFILE_STRING, SRCFILE_STRING);
        assertTrue(errorMsg.contains(ERR_COPY_TO_SELF));
    }
}
