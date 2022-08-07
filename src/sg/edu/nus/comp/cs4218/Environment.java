package sg.edu.nus.comp.cs4218;

import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.nio.file.Files;

// ClassNamingConventions: The class name clearly describes the intention and usage of the class.
// Since it static class (cannot be initialized), I believe the name should remain as it is as it will not be misused.
@SuppressWarnings("PMD.ClassNamingConventions")
public final class Environment {

    /**
     * Java VM does not support changing the current working directory.
     * For this reason, we use Environment.currentDirectory instead.
     */
    public static volatile String currentDirectory = System.getProperty("user.dir");
    private static String originalDirectory = System.getProperty("user.dir");

    private Environment() {
    }

    /**
     * Sets the current directory of Shell.
     *
     * @param path a valid absolute path
     */
    public static void setCurrentDirectory(String path) {
        if (Files.isDirectory(IOUtils.resolveFilePath(path))) {
            currentDirectory = IOUtils.resolveFilePath(path).toString();
        }
    }

    /**
     * Resets the directory to the original directory.
     */
    public static void resetCurrentDirectory() {
        currentDirectory = originalDirectory;
    }
}
