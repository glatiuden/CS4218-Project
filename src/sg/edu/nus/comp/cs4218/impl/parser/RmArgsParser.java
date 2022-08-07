package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class RmArgsParser extends ArgsParser {
    private final static char FLAG_IS_RECURSIVE = 'r';
    private final static char FLAG_IS_EMPTY_DIR = 'd';

    /**
     * Initializes a new parser for rm application, accepting -r and -d as legal flags
     */
    public RmArgsParser() {
        super();
        legalFlags.add(FLAG_IS_RECURSIVE);
        legalFlags.add(FLAG_IS_EMPTY_DIR);
    }

    /**
     * Performs a check on the supplied arguments on whether -r flag is specified
     *
     * @return Boolean: true if the -r flag is specified, else false
     */
    public Boolean isRecursive() {
        return flags.contains(FLAG_IS_RECURSIVE);
    }

    /**
     * Performs a check on the supplied arguments on whether -n flag is specified
     *
     * @return Boolean: true if the -n flag is specified, else false
     */
    public Boolean isEmptyDir() {
        return flags.contains(FLAG_IS_EMPTY_DIR);
    }

    /**
     * Retrieves the file names that were being supplied in the arguments
     *
     * @return List of file names
     */
    public List<String> getFiles() {
        return nonFlagArgs;
    }
}
