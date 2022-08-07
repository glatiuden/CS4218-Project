package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class LsArgsParser extends ArgsParser {
    private final static char FLAG_IS_RECURSIVE = 'R';
    private final static char FLAG_IS_SORT = 'X';

    /**
     * Constructor for LsArgsParser, adds the legal flags to ArgsParser
     */
    public LsArgsParser() {
        super();
        legalFlags.add(FLAG_IS_RECURSIVE);
        legalFlags.add(FLAG_IS_SORT);
    }

    /**
     * Returns if the argument has the recursive flag (-R)
     *
     * @return boolean of whether the recursive flag is present
     */
    public Boolean isRecursive() {
        return flags.contains(FLAG_IS_RECURSIVE);
    }

    /**
     * Returns if the argument has the sort flag (-X)
     *
     * @return boolean of whether the sort flag is present
     */
    public Boolean isSortByExt() {
        return flags.contains(FLAG_IS_SORT);
    }

    /**
     * Returns all the directory / files which is not a flag
     *
     * @return List of directories in the argument
     */
    public List<String> getDirectories() {
        return nonFlagArgs;
    }
}