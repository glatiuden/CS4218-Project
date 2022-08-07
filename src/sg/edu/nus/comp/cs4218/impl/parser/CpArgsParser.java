package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.CpException;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class CpArgsParser extends ArgsParser {
    private final static char FLAG_RECUR_UPPER = 'R';
    private final static char FLAG_RECUR_LOWER = 'r';

    /**
     * Creates an instance of CpArgsParser object
     */
    public CpArgsParser() {
        super();
        legalFlags.add(FLAG_RECUR_UPPER);
        legalFlags.add(FLAG_RECUR_LOWER);
    }

    /**
     * Check whether the cp application will run in recursive mode
     *
     * @return Run in recursive mode
     */
    public Boolean isRecursive() {
        return flags.contains(FLAG_RECUR_UPPER) || flags.contains(FLAG_RECUR_LOWER);
    }

    /**
     * Check whether the cp application will be copying the content of a file to another file
     *
     * @return Copy the content of a file to another file
     */
    public Boolean isFileToFile() throws CpException {
        if (nonFlagArgs.size() < 2) {
            throw new CpException(ERR_MISSING_ARG);
        }
        if (nonFlagArgs.size() > 2) {
            return false;
        }

        String srcPath = absolutePathArg(nonFlagArgs.get(0));
        File src = new File(srcPath);
        if (!src.exists()) {
            throw new CpException(ERR_FILE_NOT_FOUND);
        }
        if (src.exists() && src.isDirectory()) {
            return false;
        }

        String destPath = absolutePathArg(nonFlagArgs.get(1));
        File dest = new File(destPath);
        if (dest.exists() && dest.isDirectory()) {
            return false;
        }

        return true;
    }

    /**
     * Check whether the cp application will be copying files and folders to a folder
     *
     * @return Copy files and folders to a folder
     */
    public Boolean isToDirectory() throws CpException {
        if (nonFlagArgs.size() < 2) {
            throw new CpException(ERR_MISSING_ARG);
        }

        String destPath = absolutePathArg(nonFlagArgs.get(nonFlagArgs.size() - 1));
        File dir = new File(destPath);
        if (dir.exists() && !dir.isDirectory()) {
            throw new CpException(ERR_IS_NOT_DIR);
        }

        return true;
    }

    /**
     * Get a String of a path to the source file
     *
     * @return String of a path to the source file
     * @throws CpException Exception thrown when insufficient number of arguments are given
     */
    public String getSrcPath() throws CpException {
        if (nonFlagArgs.size() < 2) {
            throw new CpException(ERR_NO_ARGS);
        }
        return absolutePathArg(nonFlagArgs.get(0));
    }

    /**
     * Get a list of Strings that represent paths to source files and folders
     *
     * @return List of Strings that represent paths to source files and folders
     * @throws CpException Exception thrown when insufficient number of arguments are given
     */
    public List<String> getSrcPaths() throws CpException {
        if (nonFlagArgs.size() < 2) {
            throw new CpException(ERR_NO_ARGS);
        }
        List<String> srcLocalPaths = nonFlagArgs.subList(0, nonFlagArgs.size() - 1);
        return srcLocalPaths.stream().map(this::absolutePathArg).collect(Collectors.toList());
    }

    /**
     * Get a String of a path to the destination file or folder
     *
     * @return String of a path to the destination file or folder
     * @throws CpException Exception thrown when insufficient number of arguments are given
     */
    public String getDestPath() throws CpException {
        if (nonFlagArgs.size() < 2) {
            throw new CpException(ERR_NO_ARGS);
        }
        return absolutePathArg(nonFlagArgs.get(nonFlagArgs.size() - 1));
    }
}
