package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;
import sg.edu.nus.comp.cs4218.exception.CatException;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.impl.parser.CatArgsParser;
import sg.edu.nus.comp.cs4218.testutils.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class CatApplicationTest {
    private static final File ROOT_TEST_FOLDER = new File("test-cat");
    private static final File[] FILES = new File[]{
            new File(Paths.get(ROOT_TEST_FOLDER.getPath(), "singlelinefile.txt").toUri()),
            new File(Paths.get(ROOT_TEST_FOLDER.getPath(), "multilinefile.txt").toUri()),
            new File(Paths.get(ROOT_TEST_FOLDER.getPath(), "cannotreadme.txt").toUri()),
    };
    private static final String FILE1_R_PATH = ROOT_TEST_FOLDER + "/" + FILES[0].getName();
    private static final String FILE2_R_PATH = ROOT_TEST_FOLDER + "/" + FILES[1].getName();
    private static final String FILE3_R_PATH = ROOT_TEST_FOLDER + "/" + FILES[2].getName();
    private static final String[] FILE_CONTENT = new String[]{
            "cat test, this is a single line file!",
            "cat test number 2, this is a multi line." + System.lineSeparator() +
                    "previously it was line 1, now it's line 2" + System.lineSeparator() +
                    "and now we get line 3, is line 4 next?" + System.lineSeparator() +
                    "yes indeed line 4 is the next and last line!",
            "tests"
    };
    private static final String[] FILE_LINE_CONTENT = new String[]{
            "1 cat test, this is a single line file!",
            "1 cat test number 2, this is a multi line." + System.lineSeparator() +
                    "2 previously it was line 1, now it's line 2" + System.lineSeparator() +
                    "3 and now we get line 3, is line 4 next?" + System.lineSeparator() +
                    "4 yes indeed line 4 is the next and last line!",
            "1 tests"
    };
    private static final String STDIN_DASH = "-";
    private static final String ERR_CREATE_DIR = "Unable to create directory";
    private static final String ERR_CREATE_FILE = "Unable to create file";
    private static final String ERR_WRITE_FILE = "Unable to write file";
    private static CatApplication catApp;
    private static CatArgsParser catArgsParser;
    private static InputStream inputStream;
    private static OutputStream outputStream;

    @BeforeAll
    static void setup() throws Exception {
        catApp = new CatApplication();

        // create test folder
        boolean createDir = ROOT_TEST_FOLDER.mkdir();
        if (!createDir) {
            throw new Exception(ERR_CREATE_DIR + ": " + ROOT_TEST_FOLDER.getName());
        }

        // create files
        for (File file : FILES) {
            boolean createFile = file.createNewFile();
            if (!createFile) {
                throw new Exception(ERR_CREATE_FILE + ": " + file);
            }
        }

        // write to files
        for (int i = 0; i < FILES.length; i++) {
            File file = FILES[i];
            try {
                Files.writeString(file.toPath(), FILE_CONTENT[i]);
            } catch (IOException e) {
                throw new Exception(ERR_WRITE_FILE + ": " + file.getName() + " : ", e);
            }
        }
    }

    @AfterAll
    static void teardown() {
        FileUtils.deleteFolder(ROOT_TEST_FOLDER.toPath());
    }

    @BeforeEach
    void setupEach() {
        outputStream = new ByteArrayOutputStream();

        // setup mock
        catArgsParser = mock(CatArgsParser.class);
        catApp.setCatArgsParser(catArgsParser);
    }

    @AfterEach
    void teardownEach() throws Exception {
        outputStream.flush();
        outputStream.close();

        Boolean setReadable = FILES[2].setReadable(true);
        assertTrue(setReadable);
    }

    // helper functions
    void setupMock(Boolean isLineNumber, String... args) throws InvalidArgsException {
        doNothing().when(catArgsParser).parse();
        when(catArgsParser.isPrefixWithLineNumber()).thenReturn(isLineNumber);
        when(catArgsParser.getFiles()).thenReturn(List.of(args));
    }

    // run() tests
    // no. of files (none/single/multiple), stdin object (null/exist), stdout object (null/exist), stdin - (yes/no)
    // isLineNumber (true/false)

    // 1: none/exist/exist/yes/false
    @Test
    public void run_NoFileWithStdinObjWithStdoutObjWithStdinNoLineNumber_ReturnFileContent() throws Exception {
        String[] args = new String[]{STDIN_DASH};

        setupMock(false, args);

        inputStream = new ByteArrayInputStream(FILE_CONTENT[0].getBytes(StandardCharsets.UTF_8));
        catApp.run(args, inputStream, outputStream);
        assertEquals(FILE_CONTENT[0] + System.lineSeparator(), outputStream.toString());
    }

    // 2: single/exist/null/yes/true
    @Test
    public void run_SingleFileWithStdinObjNullStdoutObjWithStdinWithLineNumber_ReturnFileContent() throws Exception {
        String[] args = new String[]{FILE1_R_PATH, STDIN_DASH};

        setupMock(true, args);

        inputStream = new ByteArrayInputStream(FILE_CONTENT[1].getBytes(StandardCharsets.UTF_8));
        catApp.run(args, inputStream, outputStream);
        assertEquals(String.format("%s%s%s%s", FILE_LINE_CONTENT[0], System.lineSeparator(), FILE_LINE_CONTENT[1], System.lineSeparator()), outputStream.toString());
    }

    // 3: single/null/exist/no/false
    @Test
    public void run_SingleFileNullStdinObjWithStdoutObjNoStdinNoLineNumber_ThrowsException() throws Exception {
        String[] args = new String[]{FILE2_R_PATH};

        setupMock(false, args);

        CatException catException = assertThrows(CatException.class, () -> {
            catApp.run(args, null, outputStream);
        });
        assertTrue(catException.getMessage().contains(ERR_NO_ISTREAM));
    }

    // 4: multiple/null/null/no/true
    @Test
    public void run_MultipleFileNullStdinObjNullStdoutObjNoStdinWithLineNumber_ThrowsException() throws Exception {
        String[] args = new String[]{FILE2_R_PATH, FILE3_R_PATH};

        setupMock(true, args);

        CatException catException = assertThrows(CatException.class, () -> {
            catApp.run(args, null, null);
        });
        assertTrue(catException.getMessage().contains(ERR_NO_ISTREAM));
    }

    // 5: multiple/exist/exist/yes/false
    @Test
    public void run_MultipleFileWithStdinObjWithStdoutObjWithStdinNoLineNumber_ReturnFileContent() throws Exception {
        String[] args = new String[]{FILE2_R_PATH, STDIN_DASH, FILE3_R_PATH};

        setupMock(false, args);

        inputStream = new ByteArrayInputStream(FILE_CONTENT[0].getBytes(StandardCharsets.UTF_8));

        catApp.run(args, inputStream, outputStream);
        assertEquals(String.format("%s%s%s%s%s%s",
                FILE_CONTENT[1],
                System.lineSeparator(),
                FILE_CONTENT[0],
                System.lineSeparator(),
                FILE_CONTENT[2],
                System.lineSeparator()), outputStream.toString());
    }

    // 6: none/exist/null/yes/true
    @Test
    public void run_NoFileWithStdinObjNullStdoutObjWithStdinWithLineNumber_ThrowsException() throws Exception {
        String[] args = new String[]{STDIN_DASH};

        setupMock(true, args);

        inputStream = new ByteArrayInputStream(FILE_CONTENT[2].getBytes(StandardCharsets.UTF_8));

        CatException catException = assertThrows(CatException.class, () -> {
            catApp.run(args, inputStream, null);
        });
        assertTrue(catException.getMessage().contains(ERR_WRITE_STREAM));
    }

    // 7: none/null/exist/no/false
    @Test
    public void run_NoFileNullStdinObjWithStdoutObjNoStdinNoLineNumber_ThrowsException() throws Exception {
        String[] args = new String[]{};

        setupMock(false, args);

        CatException catException = assertThrows(CatException.class, () -> {
            catApp.run(args, null, outputStream);
        });
        assertTrue(catException.getMessage().contains(ERR_NO_ISTREAM));
    }

    // catFiles() tests
    // 1
    @Test
    public void catFiles_SingleFileWithLineNumberRelativePath_returnFileContentWithLineNo() throws Exception {
        String result = catApp.catFiles(true, FILE1_R_PATH);
        assertEquals(FILE_LINE_CONTENT[0] + System.lineSeparator(), result);
    }

    // no. 2
    @Test
    public void catFiles_MultipleFileWithLineNumberAbsolutePath_returnFileContentWithLineNo() throws Exception {

        String expected = Arrays.stream(FILE_LINE_CONTENT).reduce("", (acc, str) -> acc + str + System.lineSeparator());
        String result = catApp.catFiles(true,
                Arrays.stream(FILES).map(file -> ROOT_TEST_FOLDER + "/" + file.getName()).toArray(String[]::new));

        assertEquals(expected, result);
    }

    // no. 3
    @Test
    public void catFiles_SingleFileWithoutLineNumberAbsolutePath_returnFileContent() throws Exception {
        String result = catApp.catFiles(false, FILE1_R_PATH);
        assertEquals(FILE_CONTENT[0] + System.lineSeparator(), result);
    }

    // no. 4
    @Test
    public void catFiles_MultipleFileWithoutLineNumberRelativePath_returnFileContent() throws Exception {

        String expected = Arrays.stream(FILE_CONTENT).reduce("", (acc, str) -> acc + str + System.lineSeparator());
        String result = catApp.catFiles(false, Arrays.stream(FILES).map(File::getPath).toArray(String[]::new));

        assertEquals(expected, result);
    }

    // no. 5
    @Test
    public void catFiles_MultipleFileWithLineNumberMixedPath_returnFileContentWithLineNo() throws Exception {
        String[] filePaths = new String[]{
                ROOT_TEST_FOLDER + "/" + FILES[0].getName(),
                FILE2_R_PATH,
                FILE3_R_PATH,
        };

        String expected = Arrays.stream(FILE_LINE_CONTENT).reduce("", (acc, str) -> acc + str + System.lineSeparator());
        String result = catApp.catFiles(true, filePaths);

        assertEquals(expected, result);
    }

    @Test
    public void catFiles_InvalidFile_ThrowsError() {
        String invalidFileName = "thisisrandomfile.txt";
        CatException catException = assertThrows(CatException.class, () -> {
            catApp.catFiles(false, invalidFileName);
        });
        assertTrue(catException.getMessage().contains(CatApplication.ERR_NO_SUCH_FILE)
                && catException.getMessage().contains(invalidFileName));
    }

    @Test
    public void catFiles_FileNameAsFolder_ThrowsError() {
        CatException catException = assertThrows(CatException.class, () -> {
            catApp.catFiles(false, ROOT_TEST_FOLDER.getName());
        });
        assertTrue(catException.getMessage().contains(CatApplication.ERR_IS_DIR)
                && catException.getMessage().contains(ROOT_TEST_FOLDER.getName()));
    }

    @Test
    @DisabledOnOs({OS.WINDOWS}) // readable permissions does not work on windows
    public void catFiles_UnreadableFile_ThrowsError() {
        boolean setUnreadable = FILES[2].setReadable(false);
        assertTrue(setUnreadable);

        CatException catException = assertThrows(CatException.class, () -> {
            catApp.catFiles(false, FILES[2].getPath());
        });
        assertTrue(catException.getMessage().contains(CatApplication.ERR_READING_FILE)
                && catException.getMessage().contains(ROOT_TEST_FOLDER.getName()));

        boolean setReadable = FILES[2].setReadable(true);
        assertTrue(setReadable);
    }

    @Test
    public void catFiles_NullFileName_ThrowsError() {
        CatException catException = assertThrows(CatException.class, () -> {
            catApp.catFiles(false, null);
        });
        assertTrue(catException.getMessage().contains(ERR_NULL_ARGS));
    }

    // catStdin
    @Test
    public void catStdin_WithoutLineNumberSingleLine_ReturnInputContent() throws Exception {
        inputStream = new ByteArrayInputStream(FILE_CONTENT[0].getBytes(StandardCharsets.UTF_8));
        String result = catApp.catStdin(false, inputStream);

        assertEquals(FILE_CONTENT[0] + System.lineSeparator(), result);
    }

    @Test
    public void catStdin_WithoutLineNumberMultipleLine_ReturnInputContent() throws Exception {
        inputStream = new ByteArrayInputStream(FILE_CONTENT[1].getBytes(StandardCharsets.UTF_8));
        String result = catApp.catStdin(false, inputStream);

        assertEquals(FILE_CONTENT[1] + System.lineSeparator(), result);
    }

    @Test
    public void catStdin_WithLineNumberSingleLine_ReturnInputContent() throws Exception {
        inputStream = new ByteArrayInputStream(FILE_CONTENT[0].getBytes(StandardCharsets.UTF_8));
        String result = catApp.catStdin(true, inputStream);

        assertEquals(FILE_LINE_CONTENT[0] + System.lineSeparator(), result);
    }

    @Test
    public void catStdin_WithLineNumberMultipleLine_ReturnInputContent() throws Exception {
        inputStream = new ByteArrayInputStream(FILE_CONTENT[1].getBytes(StandardCharsets.UTF_8));
        String result = catApp.catStdin(true, inputStream);

        assertEquals(FILE_LINE_CONTENT[1] + System.lineSeparator(), result);
    }

    @Test
    public void catStdin_NullStdin_ThrowsError() {
        CatException catException = assertThrows(CatException.class, () -> {
            catApp.catStdin(true, null);
        });
        assertTrue(catException.getMessage().contains(ERR_NO_ISTREAM));
    }

    // catFileAndStdin
    // 1
    @Test
    public void catFileAndStdin_SingleFileSingleStdinWithLineNumberWithAbsolutePath_ReturnContent() throws Exception {
        String stdInContent = "this is stdin content";
        String stdInContentLine = "1 this is stdin content";
        inputStream = new ByteArrayInputStream(stdInContent.getBytes(StandardCharsets.UTF_8));

        String result = catApp.catFileAndStdin(true, inputStream, FILES[0].getPath(), STDIN_DASH);
        String expected = String.format("%s%s%s%s", FILE_LINE_CONTENT[0], System.lineSeparator(), stdInContentLine, System.lineSeparator());

        assertEquals(expected, result);
    }

    // 2
    @Test
    public void catFileAndStdin_SingleFileNoStdinWithoutLineNumberWithRelativePath_ReturnContent() throws Exception {
        inputStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));

        String result = catApp.catFileAndStdin(false, inputStream, FILE1_R_PATH);
        String expected = String.format("%s%s", FILE_CONTENT[0], System.lineSeparator());

        assertEquals(expected, result);
    }

    // 3
    @Test
    public void catFileAndStdin_MultipleFileNoStdinWithoutLineNumberWithAbsolutePath_ReturnContent() throws Exception {
        inputStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));

        String result = catApp.catFileAndStdin(false, inputStream, FILES[0].getPath(), FILES[1].getPath());
        String expected = String.format("%s%s%s%s", FILE_CONTENT[0], System.lineSeparator(), FILE_CONTENT[1], System.lineSeparator());

        assertEquals(expected, result);
    }

    // 4
    @Test
    public void catFileAndStdin_NoFileSingleStdinWithoutLineNumber_ReturnContent() throws Exception {
        String stdInContent = "This is an input from the standard input!";
        inputStream = new ByteArrayInputStream(stdInContent.getBytes(StandardCharsets.UTF_8));

        String result = catApp.catFileAndStdin(false, inputStream, STDIN_DASH);
        String expected = String.format("%s%s", stdInContent, System.lineSeparator());

        assertEquals(expected, result);
    }

    // 5
    @Test
    public void catFileAndStdin_MultipleFileSingleStdinWithLineNumberWithRelativePath_ReturnContent() throws Exception {
        String stdInContent = "this is content 1";
        String stdInContentLine = "1 this is content 1";
        inputStream = new ByteArrayInputStream(stdInContent.getBytes(StandardCharsets.UTF_8));

        String result = catApp.catFileAndStdin(true, inputStream, STDIN_DASH, FILE1_R_PATH, FILE2_R_PATH);
        String expected = String.format("%s%s%s%s%s%s", stdInContentLine, System.lineSeparator(), FILE_LINE_CONTENT[0], System.lineSeparator(), FILE_LINE_CONTENT[1], System.lineSeparator());

        assertEquals(expected, result);
    }

    // 6
    @Test
    public void catFileAndStdin_NoFileNoStdinWithLineNumber_ReturnEmpty() throws Exception {
        inputStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        String result = catApp.catFileAndStdin(true, inputStream);

        assertEquals("", result);
    }

    // 7
    @Test
    public void catFileAndStdin_SingleFileNoStdinWithLineNumberWithRelativePath_ReturnEmpty() throws Exception {
        inputStream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
        String file1 = ROOT_TEST_FOLDER + "/" + FILES[0].getName();

        String result = catApp.catFileAndStdin(true, inputStream, file1);
        String expected = String.format("%s%s", FILE_LINE_CONTENT[0], System.lineSeparator());

        assertEquals(expected, result);
    }

    // cat - file1.txt - file2.txt (example)
    // expected behaviour: stdin output, file1.txt content, file2.txt content
    @Test
    public void catFileAndStdin_TwoFilesTwoStdinWithLineNumberWithAbsolutePath_ReturnContent() throws Exception {
        String inputContent = "this is from stdin";
        inputStream = new ByteArrayInputStream(inputContent.getBytes(StandardCharsets.UTF_8));
        String file1 = FILES[0].getPath();
        String file2 = FILES[1].getPath();

        String result = catApp.catFileAndStdin(true, inputStream, STDIN_DASH, file1, STDIN_DASH, file2);
        String expected = String.format(
                "%s%s%s%s%s%s%s",
                "1 ", inputContent, System.lineSeparator(),
                FILE_LINE_CONTENT[0], System.lineSeparator(),
                FILE_LINE_CONTENT[1], System.lineSeparator()
        );
        assertEquals(expected, result);
    }

    @Test
    public void catFileAndStdin_NullStdin_ThrowsError() {
        CatException catException = assertThrows(CatException.class, () -> {
            catApp.catFileAndStdin(true, null);
        });
        assertTrue(catException.getMessage().contains(ERR_NO_ISTREAM));
    }
}
