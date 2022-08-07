package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.MvInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.MvException;
import sg.edu.nus.comp.cs4218.impl.app.args.MvArguments;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.FileUtil.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class MvApplication implements MvInterface {
    /**
     * renames the file named by the source operand to the destination path named by the target operand
     *
     * @param isOverwrite Boolean option to perform overwriting
     * @param srcFile     of path to source file
     * @param destFile    of path to destination file
     * @throws MvException For empty inputs.
     */
    public String mvSrcFileToDestFile(Boolean isOverwrite, String srcFile, String destFile) throws MvException {
        if (srcFile == null || srcFile.equals("") || destFile == null || destFile.equals("")) {
            return ERR_FILE_NOT_FOUND;
        }
        try {
            Path srcPath = resolvePath(srcFile);
            Path destPath = resolvePath(destFile);
            if (!srcPath.toFile().exists()) {
                return ERR_FILE_NOT_FOUND;
            }
            if (srcPath.equals(destPath) || isSubDirectory(srcPath.toFile(), destPath.toFile())) {
                return ERR_COPY_TO_SELF;
            }
            if (!Files.isWritable(srcPath)) {
                return ERR_NO_PERM;
            }
            if (isOverwrite) {
                Files.move(srcPath, destPath, StandardCopyOption.REPLACE_EXISTING);
            } else {
                if (destPath.toFile().exists()) {
                    return ERR_FILE_EXIST; // Doesn't do anything if the dest file exist, and we shouldn't overwrite
                }
                Files.move(srcPath, destPath);
            }
        } catch (Exception ex) {
            return ex.getMessage() + ": " + ERR_GENERAL;
        }
        return "";
    }

    /**
     * move files to destination folder
     *
     * @param isOverwrite Boolean option to perform overwriting
     * @param destFolder  of path to destination folder
     * @param fileName    Array of String of file names
     * @throws MvException For empty inputs.
     */
    public String mvFilesToFolder(Boolean isOverwrite, String destFolder, String... fileName) throws MvException {
        List<String> errorList = new ArrayList<>();
        try {
            if (destFolder == null) {
                return ERR_FILE_NOT_FOUND;
            }
            Path destPath = resolvePath(destFolder);
            if (!Files.isWritable(destPath)) {
                return ERR_NO_PERM;
            }
            for (String sourceFile : fileName) {
                if (sourceFile == null) {
                    errorList.add(ERR_FILE_NOT_FOUND);
                    continue;
                }
                if (resolvePath(sourceFile).equals(destPath)) {
                    errorList.add(ERR_COPY_TO_SELF);
                    continue;
                }
                String newDestFile = destPath + File.separator + resolvePath(sourceFile).toFile().getName();
                String errorMsg = mvSrcFileToDestFile(isOverwrite, sourceFile, newDestFile);
                if (!errorMsg.isEmpty()) {
                    errorList.add(errorMsg);
                }
            }
        } catch (Exception ex) {
            errorList.add(ex.getMessage());
        }

        return String.join(STRING_NEWLINE, errorList);
    }

    /**
     * Runs application with specified input data and specified output stream.
     *
     * @param args   Array of arguments for the application. Each array element is the path to a
     *               file. If no files are specified stdin is used.
     * @param stdin  An InputStream. The input for the command is read from this InputStream if no
     *               files are specified.
     * @param stdout An OutputStream. The output of the command is written to this OutputStream.
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws AbstractApplicationException {
        if (stdout == null) {
            throw new MvException(ERR_NULL_STREAMS);
        }

        MvArguments mvArg = new MvArguments();
        mvArg.parse(args);

        int fileSize = mvArg.getFiles().size();
        if (fileSize < 2) {
            throw new MvException(ERR_NO_ARGS);
        }

        String lastFile = mvArg.getFiles().get(fileSize - 1);
        Path lastFilePath = resolvePath(lastFile);
        String errorMsg = "";
        try {
            if (lastFilePath.toFile().isDirectory()) {
                String[] sourceFiles = Arrays.copyOfRange(mvArg.getFiles().toArray(new String[0]), 0, fileSize - 1);
                errorMsg = mvFilesToFolder(mvArg.isOverwrite(), lastFile, sourceFiles);
            } else {
                if (fileSize > 2) {
                    errorMsg = ERR_IS_NOT_DIR;
                } else {
                    errorMsg = mvSrcFileToDestFile(mvArg.isOverwrite(), mvArg.getFiles().get(0), lastFile);
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        if (errorMsg != null && !errorMsg.isEmpty()) {
            System.out.println("mv: " + errorMsg);
        }
    }
}
