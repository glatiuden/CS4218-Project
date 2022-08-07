package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.CdInterface;
import sg.edu.nus.comp.cs4218.exception.CdException;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class CdApplication implements CdInterface {

    public static final String ERROR_FORMAT = "%s: %s";

    /**
     * Sets the current directory to the new path.
     *
     * @param path Path to the directory.
     * @throws CdException
     */
    @Override
    public void changeToDirectory(String path) throws CdException {
        Environment.currentDirectory = getNormalizedAbsolutePath(path);
    }

    /**
     * Runs the cd application with the specified arguments.
     * Assumption: The application must take in one arg. (cd without args is not supported)
     * If > 1 args, the application will throw error as well.
     *
     * @param args   Array of arguments for the application.
     * @param stdin  An InputStream, not used.
     * @param stdout An OutputStream, not used.
     * @throws CdException If the input arguments/input stream/output stream is null, missing arguments or too many arguments.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws CdException {
        if (args == null) {
            throw new CdException(ERR_NULL_ARGS);
        }

        if (stdin == null) {
            throw new CdException(ERR_NO_ISTREAM);
        }

        if (stdout == null) {
            throw new CdException(ERR_WRITE_STREAM);
        }

        int argsLength = args.length;
        if (argsLength == 0) {
            throw new CdException(ERR_MISSING_ARG);
        } else if (argsLength > 1) {
            throw new CdException(ERR_TOO_MANY_ARGS);
        }

        changeToDirectory(args[0]);
    }

    /**
     * Gets the absolute path of the directory.
     *
     * @param pathStr Path to the directory.
     * @throws CdException If there are no arguments, file not found in the path, the input path is not a directory or the path is not executable.
     */
    private String getNormalizedAbsolutePath(String pathStr) throws CdException {
        if (StringUtils.isBlank(pathStr)) {
            throw new CdException(ERR_NO_ARGS);
        }

        try {
            Path path = new File(pathStr).toPath();
            if (!path.isAbsolute()) {
                path = Paths.get(Environment.currentDirectory, pathStr);
            }

            if (!Files.exists(path)) {
                throw new CdException(String.format(ERROR_FORMAT, pathStr, ERR_FILE_NOT_FOUND));
            }

            if (!Files.isDirectory(path)) {
                throw new CdException(String.format(ERROR_FORMAT, pathStr, ERR_IS_NOT_DIR));
            }

            if (!Files.isExecutable(path)) {
                throw new CdException(String.format(ERROR_FORMAT, pathStr, ERR_NO_PERM));
            }

            return path.normalize().toString();
        } catch (InvalidPathException e) {
            throw new CdException(pathStr + ": " + ERR_INVALID_PATH, e);
        }
    }
}
