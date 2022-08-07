package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.app.CpInterface;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.CpException;
import sg.edu.nus.comp.cs4218.impl.parser.CpArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.CopyVisitor;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class CpApplication implements CpInterface {

    public CpArgsParser parser;

    /**
     * Construct an instance of CPApplication with a default instance of argument parser
     */
    public CpApplication() {
        super();
        parser = new CpArgsParser();
    }

    /**
     * Inject a parser to be used for parsing cp's arguments
     *
     * @param newParser New parser to use
     */
    public void setArgsParser(CpArgsParser newParser) {
        parser = newParser;
    }

    /**
     * Copy the content of a source file to a destination file
     *
     * @param isRecursive Copy folders (directories) recursively
     * @param srcFile     String of path to source file
     * @param destFile    String of path to destination file
     * @return An empty string
     * @throws CpException Exception thrown when there is an issue copying the content of a file to another file
     */
    @Override
    public String cpSrcFileToDestFile(Boolean isRecursive, String srcFile, String destFile) throws CpException {
        Path destPath = Path.of(destFile);
        // Destination file is in a non-existent folder
        if (destFile.contains(File.separator) && !Files.exists(destPath.getParent())) {
            throw new CpException(destPath.getParent().toString() + ": " + ERR_DIR_NOT_FOUND);
        }

        // Copy to destination
        try (FileReader reader = new FileReader(srcFile);
             FileWriter writer = new FileWriter(destFile);) {
            int character = reader.read();
            while (character != -1) {
                writer.write(character);
                character = reader.read();
            }
            return "";
        } catch (IOException e) {
            throw new CpException(e.getMessage(), e);
        }
    }

    /**
     * Copy the content (files and sub-folders) of a source folder to a destination folder
     *
     * @param src  String of path to source folder
     * @param dest String of path to destination folder
     * @throws CpException Exception thrown when there is a permission issue or an IO issue
     */
    private void cpFolderToFolder(String src, String dest) throws CpException {
        // Copy the sub-folders and files of src to dest
        try {
            File srcFolder = new File(src);
            if (!srcFolder.canRead()) {
                throw new CpException(ERR_NO_PERM);
            }
            File destFolder = new File(dest);
            if (!destFolder.canWrite()) {
                throw new CpException(ERR_NO_PERM);
            }

            Path srcPath = Path.of(src);
            Path destPath = Path.of(dest);
            Files.walkFileTree(srcPath, new CopyVisitor(srcPath, destPath));
        } catch (IOException e) {
            throw new CpException(e.getMessage(), e);
        }
    }

    /**
     * Copy files and folders into a destination folder
     *
     * @param isRecursive Copy folders (directories) recursively
     * @param destFolder  String of a path to destination folder
     * @param fileName    Array of String of paths to files and folders
     * @return An empty string
     * @throws CpException Exception thrown when a folder is being copied into itself
     */
    @Override
    public String cpFilesToFolder(Boolean isRecursive, String destFolder, String... fileName) throws CpException {
        try {
            Path destPath = Path.of(destFolder);
            if (!Files.exists(destPath)) {
                throw new CpException(destPath.toString() + ": " + ERR_DIR_NOT_FOUND);
            }

            for (String src : fileName) {
                Path srcPath = Path.of(src);
                if (!Files.exists(srcPath)) {
                    System.out.println("cp: " + srcPath.getFileName() + ": " + ERR_FILE_NOT_FOUND);
                }
                if (Files.isRegularFile(srcPath)) {
                    Path destFilePath = destPath.resolve(srcPath.getFileName());
                    cpSrcFileToDestFile(isRecursive, srcPath.toString(), destFilePath.toString());
                }
                if (Files.isDirectory(srcPath)) {
                    if (!isRecursive) {
                        System.out.println("cp: " + srcPath.getFileName() + ": -r/-R " + ERR_FLAG_NOT_GIVEN);
                        continue;
                    }
                    // Prevent a directory from copying itself into itself
                    if (srcPath.toString().equals(destPath.toString())) {
                        throw new CpException(ERR_COPY_TO_SELF);
                    }
                    // Create the containing folder if not available
                    Path destDirPath = destPath.resolve(src.substring(src.lastIndexOf(File.separator) + 1));
                    if (!Files.exists(destDirPath) || !Files.isDirectory(destDirPath)) {
                        destDirPath.toFile().mkdirs();
                    }

                    cpFolderToFolder(srcPath.toString(), destDirPath.toString());
                }
            }

            return "";
        } catch (AbstractApplicationException e) {
            if (e.getMessage().startsWith("cp:")) {
                throw e;
            }
            throw new CpException(e.getMessage(), e);
        }
    }

    /**
     * Run the cp application with the parsed arguments
     *
     * @param args   Arguments given with cp that was parsed
     * @param stdin  Input stream to read from
     * @param stdout Output stream to write to
     * @throws CpException Exception thrown when given arguments is null or source file cannot be found
     */
    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout) throws CpException {
        if (args == null) {
            throw new CpException(ERR_NULL_ARGS);
        }
        try {
            parser.parse(args);
            if (parser.isFileToFile()) {
                cpSrcFileToDestFile(parser.isRecursive(), parser.getSrcPath(), parser.getDestPath());
            } else if (parser.isToDirectory()) {
                cpFilesToFolder(parser.isRecursive(), parser.getDestPath(), parser.getSrcPaths().toArray(new String[0]));
            } else {
                throw new CpException(ERR_FILE_NOT_FOUND);
            }
        } catch (CpException e) {
            throw e;
        } catch (Exception e) {
            throw new CpException(e.getMessage(), e);
        }
    }
}
