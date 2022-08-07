package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class SortArgsParser extends ArgsParser {
    public static final char CHAR_FIRST_W_NUM = 'n';
    public static final char CHAR_REV_ORDER = 'r';
    public static final char CHAR_CASE_IGNORE = 'f';

    /**
     * Creates a SortArgsParser object.
     */
    public SortArgsParser() {
        super();
        legalFlags.add(CHAR_FIRST_W_NUM);
        legalFlags.add(CHAR_REV_ORDER);
        legalFlags.add(CHAR_CASE_IGNORE);
    }

    /**
     * Returns the list of file paths in the argument.
     *
     * @return list of file paths
     */
    public List<String> getFiles() {
        return nonFlagArgs;
    }

    /**
     * Returns the boolean indicating if lines should be sorted by treating the first word as a number.
     *
     * @return Boolean indicating sort by treating first word as number.
     */
    public boolean isFirstWordNumber() {
        return flags.contains(CHAR_FIRST_W_NUM);
    }

    /**
     * Returns the boolean indicating to sort in reverse order.
     *
     * @return Boolean indicating sort in reverse order.
     */
    public boolean isReverseOrder() {
        return flags.contains(CHAR_REV_ORDER);
    }

    /**
     * Returns the boolean indicating to sort regardless of character case.
     *
     * @return Boolean indicating sort regardless of character case.
     */
    public boolean isCaseIndependent() {
        return flags.contains(CHAR_CASE_IGNORE);
    }
}
