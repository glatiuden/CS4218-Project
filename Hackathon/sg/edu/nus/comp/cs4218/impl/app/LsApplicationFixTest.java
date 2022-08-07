package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.createAllFileNFolder;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.deleteAll;

public class LsApplicationFixTest {

    static final PrintStream ORIGINAL_OUT = System.out;
    static final String FILENAME2 = "test2.txt";
    static final String FILENAME3 = "test3.a";
    static final String FILENAME4 = "test4.txt";
    static final String FOLDER_NAME = "deep-folder";
    static final String SEC_FOLDER = "second-folder";
    static final String HIDDEN_FOLDER = "hidden-folder";
    // Test Files
    static File rootTestFolder = new File("test-ls");
    static File[] dirs = new File[]{
            new File(Paths.get(rootTestFolder.toString(), FOLDER_NAME).toUri()),
            new File(Paths.get(rootTestFolder.toString(), FOLDER_NAME, SEC_FOLDER).toUri()),
            new File(Paths.get(rootTestFolder.toString(), HIDDEN_FOLDER).toUri()),
    };
    static File[] files = new File[]{
            new File("test-ls/test.txt"),
            new File(Paths.get(dirs[0].getPath(), FILENAME2).toUri()),
            new File(Paths.get(dirs[0].getPath(), FILENAME3).toUri()),
            new File(Paths.get(dirs[2].getPath(), FILENAME4).toUri()),
    };
    // VARIABLES AND CONSTANTS
    LsApplication lsApp;

    /* TEST FOLDER STRUCTURE:
        test-ls/
        ├─ deep-folder/
        │  ├─ second-folder/
        │  ├─ test2.txt
        │  ├─ test3.a
        ├─ hidden-folder/
        │  ├─ test4.txt
        ├─ test.txt
    */
    ByteArrayOutputStream stdOutResult;
    ByteArrayOutputStream outContent;
    LsArgsParser lsArgsParser = mock(LsArgsParser.class);

    @BeforeAll
    static void setupAll() throws Exception {
        createAllFileNFolder(rootTestFolder, dirs, files);
        Files.setAttribute(dirs[2].toPath(), "dos:hidden", Boolean.TRUE, LinkOption.NOFOLLOW_LINKS);
        Environment.currentDirectory = rootTestFolder.getAbsolutePath();
    }

    @AfterAll
    static void tearDown() {
        deleteAll(rootTestFolder);
        Environment.resetCurrentDirectory();
        System.setOut(ORIGINAL_OUT);
    }

    @BeforeEach
    public void setup() {
        lsApp = new LsApplication();
        stdOutResult = new ByteArrayOutputStream();
        lsApp.setParser(lsArgsParser);
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    // Fix for bug 16
    @Test
    public void listFolderContent_HiddenFolder_ReturnsTrue() throws Exception {
        String expectedAns = HIDDEN_FOLDER + ":" + STRING_NEWLINE + FILENAME4;
        String result = lsApp.listFolderContent(false, false, HIDDEN_FOLDER);

        assertEquals(expectedAns, result);
    }
}
