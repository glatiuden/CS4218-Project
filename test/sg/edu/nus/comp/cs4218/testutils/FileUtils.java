package sg.edu.nus.comp.cs4218.testutils;

import sg.edu.nus.comp.cs4218.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public final class FileUtils {

    private static final String NUMBER_FORMAT = "\t%7d";

    /**
     * Private constructor of FileUtils to prevent the creation of any instance as this is an utility class
     */
    private FileUtils() {
    }

    /**
     * Concatenate the content of the whole file and return it as a strong
     *
     * @param path Path to the file
     * @return Concatenation of the file's content
     * @throws IOException When there is error reading the file
     */
    public static String getFileContent(Path path) throws IOException {
        Optional<String> content =
                Files.readAllLines(path).stream().reduce((prev, curr) -> prev + System.lineSeparator() + curr);
        return content.orElse("");
    }


    /**
     * Utility method which helps to create specified directories and files
     *
     * @param rootTestFolder Root directory in which the specified directories and files will be created upon
     * @param dirs           Directories that will be created under the root directory
     * @param files          Files that will be created under the given path
     * @throws Exception Thrown when unable to create directories or files
     */
    // UseVarargs: This syntactic sugar provides flexibility for users of these methods and constructors,
    // allowing them to avoid having to deal with the creation of an array.
    // Suppress Reason: As this is a file testing utils, in our individual test files, we will normally initialize
    // array of files, which makes it easier for us to call this method with array of files as parameter. Therefore
    // there is no need to use varargs.
    @SuppressWarnings("PMD.UseVarargs")
    public static void createAllFileNFolder(File rootTestFolder, File[] dirs, File[] files) throws Exception {
        boolean createDir = rootTestFolder.mkdir();
        if (!createDir) {
            throw new Exception("Unable to create root directory");
        }

        // create folders
        for (File f : dirs) {
            boolean createFile = f.mkdir();
            if (!createFile) {
                throw new Exception("Unable to create folder: " + f);
            }
        }

        // create files
        for (File f : files) {
            boolean createFile = f.createNewFile();
            if (!createFile) {
                throw new Exception("Unable to create file: " + f);
            }
        }
    }

    /**
     * Utility function which helps to write supplied string into supplied file name
     *
     * @param fileName path of the file to be written into
     * @param fileTxt  content to be written into file
     */
    public static void writeToFile(String fileName, String fileTxt) {
        try {
            FileWriter myWriter = new FileWriter(fileName); //NOPMD - Writer will be closed in line 76
            myWriter.write(fileTxt);
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    /**
     * Utility function which deletes all files and directories recursively with the supplied root path
     *
     * @param root Root path where its underlying files and directories will be deleted
     */
    public static void deleteAll(File root) {
        if (root.isDirectory()) {
            for (File file : Objects.requireNonNull(root.listFiles())) {
                deleteAll(file);
            }
        }
        if (root.exists()) {
            root.delete(); // ignore results since it could've just been deleted before
        }
    }

    /**
     * Overwrite the content of a file with the given content
     *
     * @param path    Path to the file
     * @param content Content to overwrite with
     * @throws FileNotFoundException Thrown when the file to the path does not exist
     */
    public static void overwriteFileContent(Path path, String content) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(path.toString()); //NOPMD - suppressed CloseResource - Writer is already closed
        writer.println(content);
        writer.close();
    }

    /**
     * Erase the content of a file
     *
     * @param path Path to the file
     * @throws FileNotFoundException when files can't be found in the given path
     */
    public static void eraseFileContent(Path path) throws FileNotFoundException {
        PrintWriter writer = new PrintWriter(path.toString()); //NOPMD - suppressed CloseResource - Writer already closed
        writer.close();
    }

    /**
     * Delete a folder with its sub-folders and files
     *
     * @param path Path to the folder
     */
    public static void deleteFolder(Path path) {
        File folder = new File(path.toString());
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteFolder(file.toPath());
                } else {
                    file.delete();
                }
            }
        }
        folder.delete();
    }

    /**
     * Create a empty file at the given path
     *
     * @param path Path to create the file at
     * @throws IOException Thrown when the file cannot be created
     */
    public static void createNewFile(Path path) throws IOException {
        if (!Files.exists(path)) {
            Files.createFile(path);
        }
    }

    /**
     * Create all the folders that is absent from the given path
     *
     * @param path Path to reference from
     * @throws IOException Thrown when there's an error to create directories
     */
    public static void createNewDirs(Path path) throws IOException {
        if (Files.exists(path)) {
            deleteFolder(path);
        }
        Files.createDirectories(path);
    }

    /**
     * Delete a file if it exists in the given path
     *
     * @param path Path where the file should be located at
     * @throws IOException Thrown when the file cannot be deleted
     */
    public static void deleteFileIfExists(Path path) throws IOException {
        if (path != null && Files.exists(path)) {
            Files.delete(path);
        }
    }

    /**
     * Delete the folder and its content that is specified by the given path
     *
     * @param path Path to the folder
     */
    public static void deleteDirsIfExists(Path path) {
        if (Files.exists(path)) {
            deleteFolder(path);
        }
    }

    /**
     * Delete all the files in the given path with the given extension
     *
     * @param path Path where all the files are located
     * @param ext  Extension of files to delete
     */
    public static void deleteFilesByExtension(Path path, String ext) {
        if (Files.exists(path)) {
            File folder = path.toFile();
            for (File file : folder.listFiles()) {
                if (file.getName().endsWith(ext)) {
                    file.delete();
                }
            }
        }
    }

    /**
     * Utility function to remove file permissions from the specified file: unable to read and write
     *
     * @param path Path to file with stripped permissions
     */
    public static void removeFilePermissions(Path path) {
        File file = path.toFile();
        file.setReadable(false);
        file.setWritable(false);
    }

    /**
     * Utility function to enable file permissions for the specified file: allow read and write
     *
     * @param path Path to file to enable permissions
     */
    public static void resetFilePermissions(Path path) {
        File file = path.toFile();
        file.setReadable(true);
        file.setWritable(true);
    }

    /**
     * Returns a file path relative to the current working directory
     *
     * @param file A file object.
     * @return A path object, which represents the relative path from current working directory to supplied file object
     */
    public static Path getFileRelativePathToCd(File file) {
        return Paths.get(Environment.currentDirectory)
                .relativize(Paths.get(file.getPath()));
    }

    /**
     * Returns the expected result for a given file
     *
     * @param byteNum   Number of bytes of the given file
     * @param line      Number of lines of the given file
     * @param wordCount Number of words of the given file
     * @param fileName  Name of the file that is used for the calculation
     * @return a string representation of the wc result for a given file
     */
    public static String expectedFileLine(Integer byteNum, Integer line, Integer wordCount, String fileName) {
        StringBuilder finalString = new StringBuilder();
        if (line != null) {
            finalString.append(String.format(NUMBER_FORMAT, line));
        }
        if (wordCount != null) {
            finalString.append(String.format(NUMBER_FORMAT, wordCount));
        }
        if (byteNum != null) {
            finalString.append(String.format(NUMBER_FORMAT, byteNum));
        }
        if (fileName != null) {
            finalString.append(String.format("\t%s", fileName));
        }
        return finalString.toString();
    }
}