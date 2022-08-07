package sg.edu.nus.comp.cs4218.testutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static sg.edu.nus.comp.cs4218.testutils.FileUtils.*;
import static org.junit.jupiter.api.Assertions.*;

public final class IOAssertUtils {
    private IOAssertUtils() {
    }

    /**
     * Checks if OutputStream opens the correct file.
     *
     * @param path         filepath of opened file
     * @param outputStream OutputStream to check
     * @throws IOException If I/O error occurs
     */
    // write test string to outputstream, read file and check contents
    public static void assertFileOutputStream(Path path, OutputStream outputStream) throws IOException {
        String testString = "K[f-?M+Pz)B@QUZq5#v9&CV-brb-3S =]rYf79nQ]d]v(L,[N(Bbc%FN]jZW{\n9nQ]d]v(L,[N(  QUZq5#v9&CV\n111";
        outputStream.write(testString.getBytes());
        outputStream.close();
        String actualString = getFileContent(path).replaceAll("\r\n", "\n");
        assertEquals(testString, actualString);
    }

    /**
     * Checks if InputStream opens the correct file.
     *
     * @param path              filepth of opened file
     * @param actualInputStream InputStream to check
     * @throws IOException if I/O error occurs
     */
    // read from path and inputstream then compare
    public static void assertFileInputStream(Path path, InputStream actualInputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(actualInputStream));
        String expectedMsg = getFileContent(path).replaceAll("\r\n", "\n");
        String actualMsg = reader.lines().collect(Collectors.joining("\n"));
        reader.close();
        assertEquals(expectedMsg, actualMsg);
    }
}
