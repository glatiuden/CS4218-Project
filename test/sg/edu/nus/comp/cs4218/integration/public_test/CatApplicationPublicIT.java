package sg.edu.nus.comp.cs4218.integration.public_test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterAll;

import sg.edu.nus.comp.cs4218.impl.app.CatApplication;
import sg.edu.nus.comp.cs4218.testutils.TestEnvironmentUtil;

@SuppressWarnings("PMD") // reason: provided public tests, not needed to resolve
public class CatApplicationPublicIT {
    private static final String TEMP = "temp-cat";
    private static final String TEXT_ONE = "Test line 1" + STRING_NEWLINE + "Test line 2" + STRING_NEWLINE +
            "Test line 3";
    private static Deque<Path> files = new ArrayDeque<>();
    private static Path TEMP_PATH;
    private static String initialDir;

    private CatApplication catApplication;

    @BeforeAll
    static void createTemp() throws IOException, NoSuchFieldException, IllegalAccessException {
        initialDir = TestEnvironmentUtil.getCurrentDirectory();
        TEMP_PATH = Paths.get(initialDir, TEMP);
        Files.createDirectory(TEMP_PATH);
    }

    @AfterAll
    static void deleteFiles() throws IOException {
        for (Path file : files) {
            Files.deleteIfExists(file);
        }
        Files.delete(TEMP_PATH);
    }

    @BeforeEach
    void setUp() {
        catApplication = new CatApplication();
    }

    private Path createFile(String name, String text) throws IOException {
        Path path = TEMP_PATH.resolve(name);
        Files.createFile(path);
        Files.write(path, text.getBytes(StandardCharsets.UTF_8));
        files.push(path);
        return path;
    }

    private String[] toArgs(String flag, String... files) {
        List<String> args = new ArrayList<>();
        if (!flag.isEmpty()) {
            args.add("-" + flag);
        }
        for (String file : files) {
            if (file.equals("-")) {
                args.add(file);
            } else {
                args.add(Paths.get(TEMP, file).toString());
            }
        }
        return args.toArray(new String[0]);
    }

    @Test
    void run_SingleStdinNullStdout_ThrowsException() throws Exception {
        ByteArrayOutputStream output = null;
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        assertThrows(Exception.class, () -> catApplication.run(toArgs(""), inputStream, output));
    }

    @Test
    void run_NullStdinNullFilesNoFlag_ThrowsException() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = null;
        assertThrows(Exception.class, () -> catApplication.run(toArgs(""), inputStream, output));
    }

    @Test
    void run_NullStdinNullFilesFlag_ThrowsException() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = null;
        assertThrows(Exception.class, () -> catApplication.run(toArgs("n"), inputStream, output));
    }

    //catStdin cases
    @Test
    void run_SingleStdinNoFlag_DisplaysStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs(""), inputStream, output);
        assertEquals((TEXT_ONE + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleStdinFlag_DisplaysNumberedStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String expectedText = "1 Test line 1" + STRING_NEWLINE + "2 Test line 2" + STRING_NEWLINE + "3 Test line 3";
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("n"), inputStream, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleStdinDashNoFlag_DisplaysStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("", "-"), inputStream, output);
        assertEquals((TEXT_ONE + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleStdinDashFlag_DisplaysNumberedStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String expectedText = "1 Test line 1" + STRING_NEWLINE + "2 Test line 2" + STRING_NEWLINE + "3 Test line 3";
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("n", "-"), inputStream, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleEmptyStdinNoFlag_DisplaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = "";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs(""), inputStream, output);
        assertEquals((text + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleEmptyStdinFlag_DisplaysEmpty() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String text = "";
        InputStream inputStream = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        catApplication.run(toArgs("n"), inputStream, output);
        assertEquals((text + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    //catFiles cases
    @Test
    void run_NonexistentFileNoFlag_ThrowsException() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String nonexistentFileName = "nonexistent_file.txt";
        assertThrows(Exception.class, () -> catApplication.run(toArgs("", nonexistentFileName),
                System.in, output));
    }

    @Test
    void run_DirectoryNoFlag_ThrowsException() throws Exception, IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String directoryName = "nonexistent_file.txt";
        Path path = Paths.get(initialDir, directoryName);
        Files.createDirectory(path);
        assertThrows(Exception.class, () -> catApplication.run(toArgs("", directoryName),
                System.in, output));
        Files.delete(path);
    }

    @Test
    void run_SingleFileNoFlag_DisplaysFileContents() throws IOException, Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileA.txt";
        String text = TEXT_ONE;
        Path filePath = createFile(fileName, text);
        File file = new File(filePath.toString());
        catApplication.run(toArgs("", fileName), System.in, output);
        assertEquals((text + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileFlag_DisplaysNumberedFileContents() throws IOException, Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileB.txt";
        String expectedText = "1 Test line 1" + STRING_NEWLINE + "2 Test line 2" + STRING_NEWLINE + "3 Test line 3";
        createFile(fileName, TEXT_ONE);
        catApplication.run(toArgs("n", fileName), System.in, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleEmptyFileNoFlag_DisplaysEmpty() throws IOException, Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileC.txt";
        String text = "";
        Path filePath = createFile(fileName, text);
        File file = new File(filePath.toString());
        catApplication.run(toArgs("", fileName), System.in, output);
        assertEquals((text + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleEmptyFileFlag_DisplaysEmpty() throws IOException, Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileD.txt";
        String text = "";
        createFile(fileName, text);
        catApplication.run(toArgs("n", fileName), System.in, output);
        assertEquals((text + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileUnknownFlag_ThrowsException() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName = "fileE.txt";
        createFile(fileName, TEXT_ONE);
        assertThrows(Exception.class, () -> catApplication.run(toArgs("a", fileName), System.in, output));
    }

    @Test
    void run_MultipleFilesNoFlag_DisplaysCatFileContents() throws IOException, Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileF.txt";
        String fileName2 = "fileG.txt";
        String text1 = "Test line 1.1\nTest line 1.2\nTest line 1.3";
        String text2 = "Test line 2.1\nTest line 2.2";
        String expectedText = "Test line 1.1" + STRING_NEWLINE + "Test line 1.2"
                + STRING_NEWLINE + "Test line 1.3" + STRING_NEWLINE + "Test line 2.1" + STRING_NEWLINE + "Test line 2.2";
        Path filePath1 = createFile(fileName1, text1);
        File file1 = new File(filePath1.toString());
        Path filePath2 = createFile(fileName2, text2);
        File file2 = new File(filePath2.toString());
        catApplication.run(toArgs("", fileName1, fileName2), System.in, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleFilesFlag_DisplaysNumberedCatFileContents() throws IOException, Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileH.txt";
        String fileName2 = "fileI.txt";
        String text1 = "Test line 1.1\nTest line 1.2\nTest line 1.3";
        String text2 = "Test line 2.1\nTest line 2.2";
        String expectedText = "1 Test line 1.1" + STRING_NEWLINE + "2 Test line 1.2"
                + STRING_NEWLINE + "3 Test line 1.3" + STRING_NEWLINE + "1 Test line 2.1" + STRING_NEWLINE + "2 Test line 2.2";
        Path filePath1 = createFile(fileName1, text1);
        File file1 = new File(filePath1.toString());
        Path filePath2 = createFile(fileName2, text2);
        File file2 = new File(filePath2.toString());
        catApplication.run(toArgs("n", fileName1, fileName2), System.in, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleEmptyFilesNoFlag_DisplaysEmpty() throws IOException, Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileJ.txt";
        String fileName2 = "fileK.txt";
        String text = "";
        Path filePath1 = createFile(fileName1, text);
        File file1 = new File(filePath1.toString());
        Path filePath2 = createFile(fileName2, text);
        File file2 = new File(filePath2.toString());
        catApplication.run(toArgs("", fileName1, fileName2), System.in, output);
        assertEquals((text + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_MultipleEmptyFilesFlag_DisplaysEmpty() throws IOException, Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileName1 = "fileL.txt";
        String fileName2 = "fileM.txt";
        String text = "";
        Path filePath1 = createFile(fileName1, text);
        Path filePath2 = createFile(fileName2, text);
        catApplication.run(toArgs("n", fileName1, fileName2), System.in, output);
        assertEquals((text + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    //catFilesAndStdin cases
    @Test
    void run_SingleStdinNonexistentFileNoFlag_ThrowsException() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        String nonexistentFileName = "nonexistent_file.txt";
        assertThrows(Exception.class, () -> catApplication.run(toArgs("", nonexistentFileName),
                inputStream, output));
    }

    @Test
    void run_SingleStdinDirectoryNoFlag_ThrowsException() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        InputStream inputStream = new ByteArrayInputStream(TEXT_ONE.getBytes(StandardCharsets.UTF_8));
        String directoryName = "nonexistent_file.txt";
        Path path = Paths.get(initialDir, directoryName);
        Files.createDirectory(path);
        assertThrows(Exception.class, () -> catApplication.run(toArgs("", directoryName),
                inputStream, output));
        Files.delete(path);
    }

    @Test
    void run_SingleStdinDashSingleFileNoFlag_DisplaysCatStdinFileContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String stdinText = "Test line 1.1\nTest line 1.2\nTest line 1.3";
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes(StandardCharsets.UTF_8));
        String fileName = "fileN.txt";
        String fileText = "Test line 2.1\nTest line 2.2";
        Path filePath = createFile(fileName, fileText);
        String expectedText = "Test line 1.1" + STRING_NEWLINE + "Test line 1.2"
                + STRING_NEWLINE + "Test line 1.3" + STRING_NEWLINE + "Test line 2.1" + STRING_NEWLINE + "Test line 2.2";
        catApplication.run(toArgs("", "-", fileName), inputStream, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }

    @Test
    void run_SingleFileSingleStdinDashNoFlag_DisplaysCatFileStdinContents() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String fileText = "Test line 1.1\nTest line 1.2\nTest line 1.3";
        String fileName = "fileO.txt";
        Path filePath = createFile(fileName, fileText);
        String stdinText = "Test line 2.1\nTest line 2.2";
        InputStream inputStream = new ByteArrayInputStream(stdinText.getBytes(StandardCharsets.UTF_8));
        String expectedText = "Test line 1.1" + STRING_NEWLINE + "Test line 1.2"
                + STRING_NEWLINE + "Test line 1.3" + STRING_NEWLINE + "Test line 2.1" + STRING_NEWLINE + "Test line 2.2";
        catApplication.run(toArgs("", fileName, "-"), inputStream, output);
        assertEquals((expectedText + STRING_NEWLINE), output.toString(StandardCharsets.UTF_8));
    }
}

