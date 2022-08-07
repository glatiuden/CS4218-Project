package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public final class IOUtils {
    private IOUtils() {
    }

    /**
     * Open an inputStream based on the file name.
     *
     * @param fileName String containing file name.
     * @return InputStream of file opened.
     * @throws ShellException If file destination is inaccessible.
     */
    public static InputStream openInputStream(String fileName) throws ShellException {
        String resolvedFileName = resolveFilePath(fileName).toString();
        File file = new File(resolvedFileName);
        FileInputStream fileInputStream;

        if (file.isDirectory()) {
            throw new ShellException(fileName + ": " + ERR_IS_DIR);
        }
        if (file.exists() && !file.canRead()) {
            throw new ShellException(fileName + ": " + ERR_NO_PERM);
        }

        try {
            fileInputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            throw new ShellException(fileName + ": " + ERR_FILE_NOT_FOUND, e);
        }
        return fileInputStream;
    }

    /**
     * Prints out the current string results onto the provided OutputStream.
     *
     * @param results string to print
     * @param stdout  OutputStream to print on
     * @throws Exception If cannot write to OutputStream
     */
    public static void outputCurrentResults(String results, OutputStream stdout) throws Exception {
        if (stdout == null) {
            throw new Exception(ERR_NULL_STREAMS);
        }

        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdout));
            writer.write(results);
            writer.write(STRING_NEWLINE);
            writer.flush();
        } catch (IOException e) {
            throw new Exception(ERR_WRITE_STREAM, e);
        }
    }

    /**
     * Open an outputStream based on the file name.
     *
     * @param fileName String containing file name.
     * @return OutputStream of file opened.
     * @throws ShellException If file destination is inaccessible.
     */
    public static OutputStream openOutputStream(String fileName) throws ShellException {
        String resolvedFileName = resolveFilePath(fileName).toString();
        File file = new File(resolvedFileName);
        FileOutputStream fileOutputStream;

        if (file.isDirectory()) {
            throw new ShellException(fileName + ": " + ERR_IS_DIR);
        }
        if (file.exists() && !file.canWrite()) {
            throw new ShellException(fileName + ": " + ERR_NO_PERM);
        }

        try {
            fileOutputStream = new FileOutputStream(file);
        } catch (FileNotFoundException e) {
            throw new ShellException(fileName + ": " + ERR_DIR_NOT_FOUND, e);
        }
        return fileOutputStream;
    }

    /**
     * Close an inputStream. If inputStream provided is System.in or null, it will be ignored.
     *
     * @param inputStream InputStream to be closed.
     * @throws ShellException If inputStream cannot be closed successfully.
     */
    public static void closeInputStream(InputStream inputStream) throws ShellException {
        if (inputStream == null || inputStream.equals(System.in)) {
            return;
        }

        try {
            inputStream.close();
        } catch (IOException e) {
            throw new ShellException(ERR_CLOSING_STREAMS, e);
        }
    }

    /**
     * Close an outputStream. If outputStream provided is System.out or null, it will be ignored.
     *
     * @param outputStream OutputStream to be closed.
     * @throws ShellException If outputStream cannot be closed successfully.
     */
    public static void closeOutputStream(OutputStream outputStream) throws ShellException {
        if (outputStream == null || outputStream.equals(System.out)) {
            return;
        }

        try {
            outputStream.close();
        } catch (IOException e) {
            throw new ShellException(ERR_CLOSING_STREAMS, e);
        }
    }

    /**
     * Resolves the filepath from given filename or filepath string
     *
     * @param fileName filename or filepath to resolve
     * @return resolved path
     */
    public static Path resolveFilePath(String fileName) {
        Path currentDirectory = Paths.get(Environment.currentDirectory);
        return currentDirectory.resolve(fileName);
    }

    /**
     * Returns a list of lines based on the given InputStream.
     *
     * @param input InputStream containing arguments from System.in or FileInputStream
     * @throws IOException If an I/O error occurs
     */
    public static List<String> getLinesFromInputStream(InputStream input) throws IOException {
        List<String> output = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        String line;
        while ((line = reader.readLine()) != null) {
            output.add(line);
        }
        reader.close();
        return output;
    }
}