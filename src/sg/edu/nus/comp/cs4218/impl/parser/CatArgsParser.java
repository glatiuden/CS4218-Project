package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

/**
 * The 'cat' application relies on this parser to differentiate between file contents and flags (-n)
 * as well as get such information.
 */
public class CatArgsParser extends ArgsParser {
    private final static char FLAG_IS_PREFIX_NO = 'n';

    /**
     * Initializes a new parser for 'cat' application, accepting -n as a legal flag
     */
    public CatArgsParser() {
        super();
        legalFlags.add(FLAG_IS_PREFIX_NO);
    }

    /**
     * Performs a check on the supplied arguments on whether -n flag is specified
     *
     * @return Boolean: true if the -n flag is specified, false if it is not
     */
    public Boolean isPrefixWithLineNumber() {
        return flags.contains(FLAG_IS_PREFIX_NO);
    }

    /**
     * Retrieves the file names that were being supplied in the arguments
     *
     * @return List of file names (String)
     */
    public List<String> getFiles() {
        return nonFlagArgs;
    }
}
