package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.EchoException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.impl.app.RmApplication;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class RmApplicationTest {

    /* TEST FOLDER STRUCTURE:
        test-rm/
        ├─ deep-folder/
        │  ├─ deeper-folder/
        │  │  ├─ deepest-folder/
        │  │  │  ├─ this-is-empty-folder/
        │  │  │  ├─ deepest-delete-me
        │  │  │  ├─ cheatsheet.docx
        │  │  ├─ deeper-deleteme
        │  │  ├─ cs4218_hax.txt
        │  ├─ deleteme.abc
        ├─ file1.txt
        ├─ file2.c
        ├─ file3.dfhjgdfg
    */

    static final File ROOT_TEST_FOLDER = new File("test-rm");
    static final String DEEP_FOLDER = "deep-folder";
    private static final File[] DIRS = new File[]{
            new File(Paths.get(ROOT_TEST_FOLDER.toString(), DEEP_FOLDER).toUri()),
            new File(Paths.get(ROOT_TEST_FOLDER.toString(), DEEP_FOLDER, "deeper-folder").toUri()),
            new File(Paths.get(ROOT_TEST_FOLDER.toString(), DEEP_FOLDER, "deeper-folder", "deepest-folder").toUri()),
            new File(Paths.get(ROOT_TEST_FOLDER.toString(), DEEP_FOLDER, "deeper-folder", "deepest-folder", "this-is-empty-folder").toUri()),
    };
    private static final File[] FILES = new File[]{
            new File("test-rm/file1.txt"),
            new File("test-rm/file2.c"),
            new File("test-rm/file3.dfhjgdfg"),
            new File(Paths.get(DIRS[0].getPath(), "deleteme").toAbsolutePath().toString()),
            new File(Paths.get(DIRS[1].getPath(), "deeper-deleteme").toUri()),
            new File(Paths.get(DIRS[1].getPath(), "cs4218_hax.txt").toUri()),
            new File(Paths.get(DIRS[2].getPath(), "cheatsheet.docx").toUri()),
            new File(Paths.get(DIRS[2].getPath(), "deepest-deleteme").toUri()),
    };
    private static final String FILE1_A_PATH = FILES[0].getPath();
    private static final String FILE2_A_PATH = FILES[1].getPath();
    private static final String FILE1_R_PATH = ROOT_TEST_FOLDER + "/file1.txt";
    private static final String FILE2_R_PATH = ROOT_TEST_FOLDER + "/file2.c";
    private static final String FILE3_R_PATH = ROOT_TEST_FOLDER + "/file3.dfhjgdfg";
    private static RmArgsParser rmArgsParser;
    private static OutputStream outputStream;
    RmApplication rmApp;

    @BeforeEach
    void init() throws Exception {
        rmApp = new RmApplication();
        outputStream = new ByteArrayOutputStream();

        // create test folder
        boolean createDir = ROOT_TEST_FOLDER.mkdir();
        if (!createDir) {
            throw new Exception("Unable to  create test-rm directory");
        }

        // create folders
        for (File f : DIRS) {
            boolean createFile = f.mkdir();
            if (!createFile) {
                throw new Exception("Unable to create folder: " + f);
            }
        }

        // create files
        for (File f : FILES) {
            boolean createFile = f.createNewFile();
            if (!createFile) {
                throw new Exception("Unable to create file: " + f);
            }
        }

        // setup mock
        rmArgsParser = mock(RmArgsParser.class);
        rmApp.setRmArgsParser(rmArgsParser);
    }

    @AfterEach
    void teardown() throws Exception {
        // recursively delete whole test folder
        delete(ROOT_TEST_FOLDER);
    }

    // helper functions
    void setupMock(Boolean isRecursive, Boolean isEmptyDir, String... files) throws InvalidArgsException {
        doNothing().when(rmArgsParser).parse();
        when(rmArgsParser.isRecursive()).thenReturn(isRecursive);
        when(rmArgsParser.isEmptyDir()).thenReturn(isEmptyDir);
        if (files == null) {
            when(rmArgsParser.getFiles()).thenReturn(null);
        } else {
            when(rmArgsParser.getFiles()).thenReturn(List.of(files));
        }
    }

    // run() tests
    // args: null/ none (empty)/single/multiple
    // stdout: null/exist
    // isEmptyFolder: true/false
    // isRecursive: true/false
    // files: none/single/multiple
    // file path: absolute/relative

    // we are <b>excluding</b> stdin to test as it's not used anywhere in the function (irrelevant)
    // 1: args: single, stdout: exist, isEmptyFolder: true, isRecursive: true, files: single, file path: absolute
    @Test
    public void run_SingleArgsWithStdoutWithEmptyFolderWithRecursiveSingleFileAbsolutePath_DeletesFile() throws Exception {
        String[] args = new String[]{FILE1_A_PATH};
        setupMock(true, true, args);

        assertTrue(FILES[0].exists());
        rmApp.run(args, null, outputStream);
        assertFalse(FILES[0].exists());
    }

    // 2: args: single, stdout: null, isEmptyFolder: true, isRecursive: true, files: single, file path: relative
    @Test
    public void run_SingleArgsNullStdoutWithEmptyFolderWithRecursiveSingleFileRelativePath_ThrowsException() throws Exception {
        String[] args = new String[]{FILE1_R_PATH};
        setupMock(true, true, args);

        assertTrue(FILES[0].exists());

        RmException rmException = assertThrows(RmException.class, () -> {
            rmApp.run(args, null, null);
        });
        assertTrue(rmException.getMessage().contains(ERR_NO_OSTREAM));
    }

    // 3: args: single, stdout: exist, isEmptyFolder: false, isRecursive: true, files: multiple, file path: absolute
    @Test
    public void run_SingleArgsWithStdoutNoEmptyFolderWithRecursiveMultipleFileAbsolutePath_DeletesFile() throws Exception {
        String[] args = new String[]{FILE1_A_PATH, FILE2_A_PATH};
        setupMock(true, false, args);

        assertTrue(FILES[0].exists());
        assertTrue(FILES[1].exists());

        rmApp.run(args, null, outputStream);

        assertFalse(FILES[0].exists());
        assertFalse(FILES[1].exists());
    }

    // 4: args: multiple, stdout: exist, isEmptyFolder: false, isRecursive: true, files: multiple, file path: relative
    @Test
    public void run_MultipleArgsWithStdoutNoEmptyFolderWithRecursiveMultipleFileRelativePath_DeletesFile() throws Exception {
        String[] args = new String[]{FILE2_R_PATH, FILE3_R_PATH};
        setupMock(true, false, args);

        assertTrue(FILES[1].exists());
        assertTrue(FILES[2].exists());

        rmApp.run(args, null, outputStream);

        assertFalse(FILES[1].exists());
        assertFalse(FILES[2].exists());
    }

    // 5: args: multiple, stdout: null, isEmptyFolder: true, isRecursive: false, files: single, file path: absolute
    @Test
    public void run_MultipleArgsNullStdoutWithEmptyFolderNoRecursiveSingleFileAbsolutePath_ThrowsException() throws Exception {
        String[] args = new String[]{FILE1_A_PATH, FILE2_A_PATH};
        setupMock(false, true, args);

        assertTrue(FILES[0].exists());
        assertTrue(FILES[1].exists());

        assertFalse(rmArgsParser.isRecursive());
        assertTrue(rmArgsParser.isEmptyDir());
        assertEquals(FILE1_A_PATH, rmArgsParser.getFiles().get(0));
        assertEquals(FILE2_A_PATH, rmArgsParser.getFiles().get(1));

        RmException rmException = assertThrows(RmException.class, () -> {
            rmApp.run(args, null, null);
        });
        assertTrue(rmException.getMessage().contains(ERR_NO_OSTREAM));
    }

    // 6: args: null, stdout: exist, isEmptyFolder: true, isRecursive: true, files: none, file path: none
    @Test
    public void run_NullArgsWithStdoutWithEmptyFolderWithRecursiveNoFileNoPath_ThrowsException() throws Exception {
        setupMock(true, true, null);

        assertTrue(rmArgsParser.isRecursive());
        assertTrue(rmArgsParser.isEmptyDir());
        assertNull(rmArgsParser.getFiles());

        RmException rmException = assertThrows(RmException.class, () -> {
            rmApp.run(null, null, null);
        });
        assertTrue(rmException.getMessage().contains(ERR_NULL_ARGS));
    }

    // FAIL CASES
    @Test
    public void run_SupplySingleDot_ThrowsException() throws Exception {
        String[] args = new String[]{"."};
        setupMock(false, false, args);
        RmException rmException = assertThrows(RmException.class, () -> {
            rmApp.run(args, null, outputStream);
        });
        assertTrue(rmException.getMessage().contains(RmApplication.ERROR_DOT_REMOVE));
    }

    @Test
    public void run_SupplyDoubleDot_ThrowsException() throws Exception {
        String[] args = new String[]{".."};
        setupMock(false, false, args);
        RmException rmException = assertThrows(RmException.class, () -> {
            rmApp.run(args, null, outputStream);
        });
        assertTrue(rmException.getMessage().contains(RmApplication.ERROR_DOT_REMOVE));
    }

    // remove() tests
    // 1: isEmptyFolder: true, isRecursive: false, files: single, file path: relative
    @Test
    public void remove_IsEmptyFolderNotRecursiveSingleFileRelativePath_DeleteFile() throws Exception {
        File fileToTest = FILES[0];
        assertTrue(fileToTest.exists());

        rmApp.remove(true, false, ROOT_TEST_FOLDER + "/" + fileToTest.getName());

        assertFalse(fileToTest.exists());
    }

    // 2: isEmptyFolder: false, isRecursive: false, files: multiple, file path: absolute
    @Test
    public void remove_NotEmptyFolderNotRecursiveMultipleFileAbsolutePath_DeleteFile() throws Exception {
        File file1 = FILES[0];
        File file2 = FILES[1];

        assertTrue(file1.exists());
        assertTrue(file2.exists());

        rmApp.remove(true, false, file1.getPath(), file2.getPath());

        assertFalse(file1.exists());
        assertFalse(file2.exists());
    }

    // 3: isEmptyFolder: false, isRecursive: true, files: none, file path: relative
    @Test
    public void remove_IsEmptyFolderIsRecursiveNoFileRelativePath_DeleteNoFile() throws Exception {
        long count = Files.list(ROOT_TEST_FOLDER.toPath()).count();
        rmApp.remove(false, true);
        long afterCount = Files.list(ROOT_TEST_FOLDER.toPath()).count();
        assertEquals(count, afterCount);
    }

    // 4: isEmptyFolder: false, isRecursive: true, files: single, file path: absolute
    @Test
    public void remove_NotEmptyFolderIsRecursiveSingleFileAbsolutePath_DeleteFile() throws Exception {
        File file3 = FILES[2];
        assertTrue(file3.exists());

        rmApp.remove(false, true, file3.getPath());

        assertFalse(file3.exists());
    }

    // 5: isEmptyFolder: true, isRecursive: true, files: single, file path: absolute
    @Test
    public void remove_IsEmptyFolderIsRecursiveSingleFileAbsolutePath_DeleteFile() throws Exception {
        File file4 = FILES[3];
        assertTrue(file4.exists());

        rmApp.remove(true, true, file4.getPath());

        assertFalse(file4.exists());
    }

    // 6: isEmptyFolder: true, isRecursive: true, files: multiple, file path: relative
    @Test
    public void remove_IsEmptyFolderIsRecursiveMultipleFileRelativePath_DeleteFile() throws Exception {
        assertTrue(ROOT_TEST_FOLDER.exists());
        rmApp.remove(true, true, ROOT_TEST_FOLDER.getName());
        assertFalse(ROOT_TEST_FOLDER.exists());
    }

    // 7: isEmptyFolder: true, isRecursive: false, files: none, file path: absolute
    @Test
    public void remove_IsEmptyFolderNotRecursiveNoFileAbsolutePath_DeleteNoFile() throws Exception {
        long count = Files.list(ROOT_TEST_FOLDER.toPath()).count();
        rmApp.remove(true, false);
        long afterCount = Files.list(ROOT_TEST_FOLDER.toPath()).count();
        assertEquals(count, afterCount);
    }

    @Test
    public void remove_RemoveFile1Txt_Success() throws Exception {
        // check if file1.txt exist
        assertTrue(FILES[0].exists());

        // remove file
        rmApp.remove(false, false, FILES[0].getPath());

        // file1.txt should not exist after delete
        assertFalse(FILES[0].exists());
    }


    /* FAIL TESTS */
    @Test
    public void remove_RemoveKnownDirectoryWithoutRecursiveFlag_ThrowException() {
        File file = DIRS[0];

        RmException rmException = assertThrows(RmException.class, () -> {
            rmApp.remove(false, false, file.getPath());
        });

        assertTrue(rmException.getMessage().contains(RmApplication.IS_DIRECTORY)
                && rmException.getMessage().contains(file.getName()));
    }

    @Test
    public void remove_RemoveFilledDirectoryWithFolderFlag_ThrowException() {
        File file = DIRS[0];

        RmException rmException = assertThrows(RmException.class, () -> {
            rmApp.remove(true, false, file.getPath());
        });

        assertTrue(rmException.getMessage().contains(RmApplication.DIR_NOT_EMPTY)
                && rmException.getMessage().contains(file.getName()));
    }

    @Test
    public void remove_RemoveUnknownFile_ThrowException() {
        File unknownFile = new File(Paths.get(ROOT_TEST_FOLDER.toString(), "unknownfile.wee").toUri());

        RmException rmException = assertThrows(RmException.class, () -> {
            rmApp.remove(false, false, unknownFile.getPath());
        });

        assertTrue(rmException.getMessage().contains(RmApplication.ERR_FILE_OR_DIR)
                && rmException.getMessage().contains(unknownFile.getName()));
    }

    // helper function
    void delete(File file) throws IOException {
        if (file.isDirectory()) {
            for (File fileN : file.listFiles()) {
                delete(fileN);
            }
        }
        if (file.exists()) {
            file.delete(); // ignore results since it could've just been deleted before
        }
    }

}
