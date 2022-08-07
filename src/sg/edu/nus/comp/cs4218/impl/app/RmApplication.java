package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.RmInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.RmException;
import sg.edu.nus.comp.cs4218.impl.parser.RmArgsParser;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NO_OSTREAM;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_ARGS;
import static sg.edu.nus.comp.cs4218.impl.util.FileUtil.getRelativeToCwd;

public class RmApplication implements RmInterface {

    public static final String ERR_FILE_OR_DIR = "No such file or directory";
    public static final String IS_DIRECTORY = "is a directory";
    public static final String DIR_NOT_EMPTY = "Directory not empty";
    public static final String ERROR_DELETING = "Unable to delete";
    public static final String ERROR_DOT_REMOVE = "\".\" and \"..\" may not be removed";

    private RmArgsParser rmArgsParser;

    /**
     * Creates a new RmApplication instance, together with RmArgsParser as the default parser
     */
    public RmApplication() {
        super();
        rmArgsParser = new RmArgsParser();
    }

    /**
     * Responsible for setting the parser to be used for the rm application
     *
     * @param parser Parser to be used for parsing rm application arguments
     */
    public void setRmArgsParser(RmArgsParser parser) {
        rmArgsParser = parser;
    }

    /**
     * Runs the rm application with the specified arguments
     *
     * @param args   Array of file names that is meant to be deleted
     * @param stdin  An InputStream
     * @param stdout An OutputStream. The output of the command is written to this OutputStream
     * @throws RmException If no file names are specified, or unavailable output stream
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws RmException {
        if (args == null) {
            throw new RmException(ERR_NULL_ARGS);
        }

        if (stdout == null) {
            throw new RmException(ERR_NO_OSTREAM);
        }

        RmArgsParser parser = rmArgsParser;
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new RmException(e);
        }

        Boolean isRecursive = parser.isRecursive();
        Boolean isEmptyDir = parser.isEmptyDir();

        // validation for . and ..
        if (parser.getFiles().contains(".") || parser.getFiles().contains("..")) {
            throw new RmException(ERROR_DOT_REMOVE);
        }

        String[] files = parser.getFiles().stream()
                .map(s -> {
                    if (new File(s).isAbsolute()) {
                        return new File(s).getPath();
                    } else {
                        return new File(Environment.currentDirectory, s).getPath();
                    }
                })
                .collect(Collectors.toList())
                .toArray(new String[parser.getFiles().size()]);

        try {
            remove(isEmptyDir, isRecursive, files);
        } catch (RmException rmException) {
            throw rmException;
        } catch (Exception e) {
            throw new RmException(e);
        }
    }

    /**
     * Responsible for deleting files based on the specified empty folder, recursive flags and file name
     *
     * @param isEmptyFolder Boolean option to delete a folder only if it is empty
     * @param isRecursive   Boolean option to recursively delete the folder contents (traversing
     *                      through all folders inside the specified folder)
     * @param fileName      Array of String of file names
     * @throws Exception Thrown when certain operations are forbidden, for example: using the empty folder flag
     *                   when the folder supplied is not empty
     */
    @Override
    public void remove(Boolean isEmptyFolder, Boolean isRecursive, String... fileName) throws Exception { //NOPMD - As specified in the given interface

        List<File> missingFiles = new ArrayList<>();

        for (String fileN : fileName) {
            File file = new File(fileN);

            File[] filesInDir = file.listFiles(); // applicable for directories only

            // if file does not exist
            if (!file.exists()) {
                missingFiles.add(file);
                continue;
            }

            // handle: directories only used when -r, -d flag is used, else throw
            if (!isEmptyFolder && !isRecursive && file.isDirectory()) {
                throw new RmException(getRelativeToCwd(file.toPath()) + ": " + IS_DIRECTORY);
            }

            // handle: when -d flag is used, directory must be empty
            if (isEmptyFolder && !isRecursive && file.isDirectory() && filesInDir != null && filesInDir.length > 0) {
                throw new RmException(getRelativeToCwd(file.toPath()) + ": " + DIR_NOT_EMPTY);
            }

            // files
            if (file.isFile() && !file.delete()) {
                throw new RmException(getRelativeToCwd(file.toPath()) + ": " + ERROR_DELETING);
            }

            // directories
            if (file.isDirectory() && isRecursive && filesInDir != null) {
                remove(isEmptyFolder, true,
                        Arrays.stream(filesInDir).map(File::getPath).toArray(String[]::new));
                if (!file.delete()) {
                    throw new RmException(getRelativeToCwd(file.toPath()) + ": " + ERROR_DELETING);
                }
            }

            if (file.isDirectory() && isEmptyFolder && !file.delete()) {
                throw new RmException(getRelativeToCwd(file.toPath()) + ": " + ERROR_DELETING);
            }
        }

        // throw exceptions for those files that do not exist
        StringBuilder exceptionString = new StringBuilder();
        for (int i = 0; i < missingFiles.size(); i++) {
            File missingFile = missingFiles.get(i);

            exceptionString
                    .append(missingFile.getName())
                    .append(": ")
                    .append(ERR_FILE_OR_DIR);
            if (i != missingFiles.size() - 1) {
                exceptionString.append('\n');
            }
        }
        if (!missingFiles.isEmpty()) {
            throw new RmException(exceptionString.toString());
        }
    }
}
