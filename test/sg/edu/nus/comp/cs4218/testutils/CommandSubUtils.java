package sg.edu.nus.comp.cs4218.testutils;

import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;
import static sg.edu.nus.comp.cs4218.testutils.FileUtils.*;

public final class CommandSubUtils {

    // Used for command substitutions
    public static final String SINGLE_STRING = "`%s`";
    public static final String DOUBLE_STRING = "`%s %s`";
    public static final String TRIPLE_STRING = "`%s %s %s`";
    public static final String QUAD_STRING = "`%s %s %s %s`";

    // Formatting
    public static final String NUMBER_FORMAT = "\t%7d";
    public static final String TAB = "\t";
    public static final String BREAK_LINE = "\n";
    public static final String GREP_PATTERN = "CS4218";
    public static final String INVALID_PATTERN = "CS4218\\";

    // Commands
    public static final String ECHO_CMD = "echo";
    public static final String LS_CMD = "ls";
    public static final String CAT_CMD = "cat";
    public static final String CD_CMD = "cd";
    public static final String GREP_CMD = "grep";
    public static final String CUT_CMD = "cut";
    public static final String SORT_CMD = "sort";
    public static final String CP_CMD = "cp";
    public static final String RM_CMD = "rm";
    public static final String WC_CMD = "wc";
    public static final String TEE_CMD = "tee";
    public static final String EXIT_CMD = "exit";
    public static final String MV_CMD = "mv";
    public static final String UNIQ_CMD = "uniq";
    public static final String PASTE_CMD = "paste";
    public static final String INVALID_CMD = "cs4218";

    // Exception String Format
    public static final String EXIT_EXCEP = "exit: %s";
    public static final String CAT_EXCEP = "cat: %s: %s";
    public static final String CD_EXCEP = "cd: %s: %s";
    public static final String CD_EXCEP_DIR = "cd: %s";
    public static final String CP_EXCEP = "cp: %s: %s";
    public static final String CP_EXCEP_DIR = "cp: %s";
    public static final String CUT_EXCEP = "cut: %s";
    public static final String CUT_EXCEP_DIR = "cut: %s: %s";
    public static final String RM_EXCEP = "rm: %s: %s";
    public static final String SORT_EXCEP = "sort: %s: %s";
    public static final String GREP_EXCEP = "grep: %s";
    public static final String GREP_EXCEP_DIR = "grep: %s: %s";
    public static final String UNIQ_EXCEP = "uniq: %s: %s";
    public static final String PASTE_EXCEP = "paste: %s";
    public static final String WC_EXCEP = "wc: %s: %s";
    public static final String WC_EXCEP_DIR = "wc: %s";
    public static final String MV_EXCEP = "mv: %s";
    public static final String SHELL_EXCEP = "shell: cs4218: Invalid app";
    public static final String LS_EXCEP = "ls: %s";

    // Generic File Names
    public static final String FILE_ONE_NAME = "A.txt";
    public static final String FILE_TWO_NAME = "B.txt";
    public static final String FILE_ONE_CONTENT = "I love CS4218";
    public static final String FILE_TWO_CONTENT = "CS4218";

    // File Names used for specific commands
    public static final String FILE_FILE_NAME = "file.txt";
    public static final String FILE_RM_NAME = "remove.txt";
    public static final String FILE_SORT_NAME = "sort.txt";
    public static final String FILE_TEE_NAME = "tee.txt";
    public static final String FILE_CP_NAME = "cp.txt";
    public static final String FILE_PASTE_NAME = "paste.txt";
    public static final String FILE_UNIQ_NAME = "uniq.txt";
    public static final String FILE_INVALID = "invalid.txt";
    public static final String NEST_DIR = "nest";
    public static final String DIR_INVALID = "invalid";

    // Mv File Names
    public static final String FILE_MV_NAME = "mv.txt";
    public static final String FILE_MV1_NAME = "mv1.txt";
    public static final String FILE_MV2_NAME = "mv2.txt";
    public static final String DIR_MV_NAME = "mv3-folder";
    public static final String FILE_UNIQM_NAME = "uniq-mv.txt";
    public static Path mvPath, mv1Path, mv2Path, mvFolderPath, mvUniqPath;

    /**
     * Private constructor of CommandSubUtils to prevent the creation of any instance as this is an utility class
     */
    private CommandSubUtils() {
    }

    /**
     * Creates temporary files mainly for Mv command usage.
     *
     * @param rootFolderPath Temp directory where the files should be created.
     * @throws IOException If any of the paths is not initialized.
     */
    public static void createMvTestFiles(@TempDir Path rootFolderPath) throws IOException {
        mvPath = rootFolderPath.resolve(FILE_MV_NAME);
        mv1Path = rootFolderPath.resolve(FILE_MV1_NAME);
        mv2Path = rootFolderPath.resolve(FILE_MV2_NAME);
        mvFolderPath = rootFolderPath.resolve(DIR_MV_NAME);
        mvUniqPath = rootFolderPath.resolve(FILE_UNIQM_NAME);

        Files.writeString(mvPath, FILE_MV1_NAME + STRING_NEWLINE + FILE_MV2_NAME);
        Files.writeString(mv1Path, FILE_ONE_CONTENT);
        Files.writeString(mv2Path, FILE_TWO_CONTENT);
        Files.writeString(mvUniqPath, FILE_MV1_NAME + STRING_NEWLINE + FILE_MV1_NAME + STRING_NEWLINE + FILE_MV1_NAME);
        createNewDirs(mvFolderPath);
    }

    /**
     * Delete all the test files.
     *
     * @throws IOException If any of the path not initialized.
     */
    public static void deleteMvTestFiles() throws IOException {
        deleteFileIfExists(mvPath);
        deleteFileIfExists(mv1Path);
        deleteFileIfExists(mv2Path);
        deleteFileIfExists(mvUniqPath);
        if (mvFolderPath != null) {
            deleteAll(mvFolderPath.toFile());
        }
    }
}
