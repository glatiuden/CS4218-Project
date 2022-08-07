package sg.edu.nus.comp.cs4218.impl.app;

import sg.edu.nus.comp.cs4218.Environment;
import sg.edu.nus.comp.cs4218.app.LsInterface;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;
import sg.edu.nus.comp.cs4218.exception.LsException;
import sg.edu.nus.comp.cs4218.impl.parser.LsArgsParser;
import sg.edu.nus.comp.cs4218.impl.util.FileWithExtension;
import sg.edu.nus.comp.cs4218.impl.util.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.FileUtil.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.*;

public class LsApplication implements LsInterface {

    private final static String PATH_CURR_DIR = STRING_CURR_DIR + CHAR_FILE_SEP;
    List<String> fileNames = new ArrayList<>();
    List<String> errorMsg = new ArrayList<>();
    int resultCount = 0;
    private LsArgsParser lsArgsParser = new LsArgsParser();

    @Override
    public String listFolderContent(Boolean isRecursive, Boolean isSortByExt,
                                    String... folderName) throws LsException {
        if (folderName.length == 0 && !isRecursive) {
            return listCwdContent(isSortByExt);
        }

        List<Path> paths;
        if (folderName.length == 0) {
            String[] directories = new String[1];
            directories[0] = Environment.currentDirectory;
            paths = resolvePaths(directories);
        } else {
            try {
                paths = resolvePaths(folderName);
            } catch (InvalidPathException e) {
                throw new LsException(e, ERR_INVALID_PATH);
            }
        }

        StringBuilder result = new StringBuilder();
        fileNames.clear(); // Reset the content to ensure correctness
        errorMsg.clear();
        resultCount = 0;
        String folderResult = buildResult(paths, isRecursive, isSortByExt, true).trim();
        System.out.print(String.join(STRING_NEWLINE, errorMsg));

        if (!errorMsg.isEmpty() && (!fileNames.isEmpty() || !folderResult.isEmpty())) {
            System.out.println();
        }
        // 2nd Layer result, fileNames in Alphabetical order
        if (isSortByExt) {
            fileNames = sortFiles(fileNames);
        }
        result.append(String.join(STRING_NEWLINE, fileNames));
        if (!fileNames.isEmpty() && !folderResult.isEmpty()) {
            result.append(StringUtils.STRING_NEWLINE);
            result.append(StringUtils.STRING_NEWLINE);
        }
        // 3rd layer result of the folder results
        result.append(folderResult);

        return result.toString();
    }

    /**
     * Set the ls argument parser
     *
     * @param lsArgsParser parser to be set.
     */
    public void setParser(LsArgsParser lsArgsParser) {
        this.lsArgsParser = lsArgsParser;
    }

    @Override
    public void run(String[] args, InputStream stdin, OutputStream stdout)
            throws LsException {
        if (args == null) {
            throw new LsException(ERR_NULL_ARGS);
        }

        if (stdout == null) {
            throw new LsException(ERR_NO_OSTREAM);
        }

        LsArgsParser parser = lsArgsParser;
        try {
            parser.parse(args);
        } catch (InvalidArgsException e) {
            throw new LsException(e, "");
        }

        Boolean recursive = parser.isRecursive();
        Boolean sortByExt = parser.isSortByExt();
        List<String> directoriesList = parser.getDirectories();
        String[] directories = directoriesList.toArray(new String[0]);
        String result = listFolderContent(recursive, sortByExt, directories);

        try {
            stdout.write(result.getBytes());
            stdout.write(StringUtils.STRING_NEWLINE.getBytes());
        } catch (Exception e) {
            throw new LsException(e, ERR_WRITE_STREAM);
        }
    }

    /**
     * Lists only the current directory's content and RETURNS. This does not account for recursive
     * mode in cwd.
     *
     * @param isSortByExt - sorts folder contents alphabetically by file extension (characters after the last ‘.’ (without quotes)). Files with no extension are sorted first.
     * @return String representing the current directory's content
     */
    private String listCwdContent(Boolean isSortByExt) throws LsException {
        String cwd = Environment.currentDirectory;
        try {
            return formatContents(getContents(Paths.get(cwd)), isSortByExt);
        } catch (InvalidDirectoryException e) {
            throw new LsException(e, "Unexpected error occurred!"); // LsException is more specific than InvalidDirectoryException
        }
    }

    /**
     * Builds the resulting string to be written into the output stream.
     * <p>
     * NOTE: This is recursively called if user wants recursive mode.
     *
     * @param paths       - list of java.nio.Path objects to list
     * @param isRecursive - recursive mode, repeatedly ls the child directories
     * @param isSortByExt - sorts folder contents alphabetically by file extension (characters after the last ‘.’ (without quotes)). Files with no extension are sorted first.
     * @param isMainPath  - checks whether it is a main path or path from recursive definition
     * @return String to be written to output stream.
     */
    private String buildResult(List<Path> paths, Boolean isRecursive, Boolean isSortByExt, Boolean isMainPath) {
        StringBuilder result = new StringBuilder();
        for (Path path : paths) {
            try {
                if (path.toFile().isHidden() && !isMainPath) {
                    continue; // Do not produce result for hidden files unless it's a given argument (e.g. ls .git)
                }
                List<Path> contents;
                String formatted;

                if (Files.exists(path) && !Files.isDirectory(path) && isMainPath) {
                    contents = List.of(path);
                    formatted = formatContents(contents, isSortByExt);
                    fileNames.add(formatted);
                    continue;
                } else {
                    contents = getContents(path);
                    formatted = formatContents(contents, isSortByExt);
                    String relativePath = getRelativeToCwd(path).toString();
                    result.append(StringUtils.isBlank(relativePath) ? PATH_CURR_DIR : relativePath);
                    result.append(':').append(System.lineSeparator()).append(formatted);
                }

                if (!formatted.isEmpty()) {
                    // Empty directories should not have an additional new line
                    result.append(StringUtils.STRING_NEWLINE);
                }

                result.append(StringUtils.STRING_NEWLINE);

                if (isRecursive) {
                    result.append(buildResult(contents, true, isSortByExt, false));
                }
            } catch (InvalidDirectoryException e) {
                // If the user is in recursive mode, and if we resolve a file that isn't a directory we should not spew the error message.
                if (!isRecursive || isMainPath) {
                    // 1st layer result of the error messages
                    errorMsg.add(e.getMessage());
                }
            } catch (Exception ex) {
                errorMsg.add("ls: " + ERR_GENERAL + ": " + ex.getMessage());
            }
        }
        return result.toString();
    }

    /**
     * Formats the contents of a directory into a single string.
     *
     * @param contents    - list of items in a directory
     * @param isSortByExt - sorts folder contents alphabetically by file extension (characters after the last ‘.’ (without quotes)). Files with no extension are sorted first.
     * @return A string representing all the contents of a directory with new line between each result.
     */
    private String formatContents(List<Path> contents, Boolean isSortByExt) {
        // Implement sorting by extension done!
        List<String> allFilesString = new ArrayList<>();
        List<FileWithExtension> allFiles = new ArrayList<>();
        for (Path path : contents) {
            if (path.toFile().isHidden()) {
                continue; // Do not produce result for hidden fields - Follow Linux implementation
            }
            allFilesString.add(path.getFileName().toString());
            allFiles.add(new FileWithExtension(path.getFileName().toString()));
        }

        StringBuilder result = new StringBuilder();

        if (isSortByExt) {
            Collections.sort(allFiles);

            for (FileWithExtension file : allFiles) {
                result.append(file.fileName);
                result.append(STRING_NEWLINE);
                resultCount++;
            }
        } else {
            Collections.sort(allFilesString);

            for (String fileName : allFilesString) {
                result.append(fileName);
                result.append(STRING_NEWLINE);
                resultCount++;
            }
        }

        return result.toString().trim();
    }
}