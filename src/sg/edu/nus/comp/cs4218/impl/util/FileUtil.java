package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.exception.PasteException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FileUtil {

    /**
     * Private constructor to prevent instantiation since this is a utility class
     * Complies with PMD:UseUtilityClass
     */
    private FileUtil() {
    }

    /**
     * Converts a path to a relative path to the current directory.
     *
     * @param path - The path to change to a relative path.
     * @return Path which represents the relative path of the given path.
     */
    public static Path getRelativeToCwd(Path path) {
        return Paths.get(Environment.currentDirectory).relativize(path.normalize());
    }

    /**
     * Resolve all paths given as arguments into a list of Path objects for easy path management.
     *
     * @param directories - All the directories that needs to be resolved.
     * @return List of java.nio.Path objects
     */
    public static List<Path> resolvePaths(String... directories) {
        List<Path> paths = new ArrayList<>();
        for (String directory : directories) {
            paths.add(resolvePath(directory));
        }

        return paths;
    }

    /**
     * Converts a String into a java.nio.Path objects. Also resolves if the current path provided
     * is an absolute path.
     *
     * @param directory - A string directory that needs to be resolved.
     * @return Path of the given directory.
     */
    public static Path resolvePath(String directory) {
        File file = new File(directory);
        if (file.isAbsolute() || Environment.currentDirectory.equals(directory)) {
            return Paths.get(directory).normalize();
        }

        return Paths.get(Environment.currentDirectory, directory).normalize();

    }

    /**
     * Gets the contents in a single specified directory.
     *
     * @param directory - Path of the directory to get the contents for.
     * @return List of files + directories in the past directory.
     */
    public static List<Path> getContents(Path directory)
            throws InvalidDirectoryException {
        if (!Files.exists(directory)) {
            throw new InvalidDirectoryException(getRelativeToCwd(directory).toString());
        }

        if (!Files.isDirectory(directory)) {
            throw new InvalidDirectoryException(getRelativeToCwd(directory).toString());
        }

        List<Path> result = new ArrayList<>();
        File pwd = directory.toFile();
        for (File f : Objects.requireNonNull(pwd.listFiles())) {
            if (!(f.isHidden())) {
                result.add(f.toPath());
            }
        }
        result.sort(Comparator.comparing(Path::toString));

        return result;
    }

    /**
     * Sort the files based on the extension and update the fileNames accordingly.
     */
    public static List<String> sortFiles(List<String> fileNames) {
        List<FileWithExtension> filesList = new ArrayList<>();
        for (String file : fileNames) {
            filesList.add(new FileWithExtension(file));
        }
        Collections.sort(filesList);
        List<String> sortedFileName = new ArrayList<>();
        for (FileWithExtension file : filesList) {
            sortedFileName.add(file.fileName);
        }
        return sortedFileName;
    }

    /**
     * Check if the sub dir is a subset / subdirectory of the base dir.
     *
     * @param subDir Subdirectory of the file to be checked (e.g., test/abc/)
     * @param file   File to check if it is under the subdirectory (e.g., test/abc/cba/test.txt)
     * @return boolean representing if the file is a subdirectory of subDir
     * @throws IOException for any error reading the file
     */
    public static boolean isSubDirectory(File subDir, File file) throws IOException {
        if (subDir == null || !subDir.isDirectory()) {
            return false;
        }
        if (file == null || !(file.isFile()) || file.isDirectory()) {
            return false;
        }
        if (subDir.equals(file)) {
            return true;
        }

        return isSubDirectory(subDir, file.getParentFile());
    }

    /**
     * Returns the content of a file in lines
     *
     * @param path Path to a file
     * @return A list of Strings representing the individual lines in the file
     * @throws IOException Exception thrown when trying to read the file
     */
    public static List<String> getFileLines(Path path) throws IOException {
        return Files.lines(path).collect(Collectors.toList());
    }

    /**
     * Exception class for invalid directory
     */
    public static class InvalidDirectoryException extends Exception {
        InvalidDirectoryException(String directory) {
            super(String.format("ls: cannot access '%s': No such file or directory", directory));
        }
    }
}
