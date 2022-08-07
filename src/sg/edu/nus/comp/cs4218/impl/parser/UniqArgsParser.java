package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class UniqArgsParser extends ArgsParser {
    private final static char FLAG_IS_COUNT = 'c';
    private final static char FLAG_IS_REPEAT = 'd';
    private final static char FLAG_IS_A_REPEAT = 'D';

    /**
     * Creates a UniqArgsParser object.
     */
    public UniqArgsParser() {
        super();
        legalFlags.add(FLAG_IS_COUNT);
        legalFlags.add(FLAG_IS_REPEAT);
        legalFlags.add(FLAG_IS_A_REPEAT);
    }

    /**
     * Returns boolean option indicating whether to prefix lines by the number
     * of occurrences of adjacent duplicate lines.
     *
     * @return boolean to prefix lines by the number of occurrences of adjacent duplicate lines
     */
    public boolean isCount() {
        return flags.contains(FLAG_IS_COUNT);
    }

    /**
     * Returns boolean option indicating whether to print only duplicate lines, one for each group.
     *
     * @return boolean to print only duplicate lines, one for each group
     */
    public boolean isRepeated() {
        return flags.contains(FLAG_IS_REPEAT);
    }

    /**
     * Returns boolean option indicating whether to print all duplicate lines
     *
     * @return boolean to print all duplicate lines
     */
    public boolean isAllRepeated() {
        return flags.contains(FLAG_IS_A_REPEAT);
    }

    /**
     * Returns the list of file paths in the argument.
     *
     * @return list of file paths
     */
    public List<String> getFiles() {
        return nonFlagArgs;
    }
}
