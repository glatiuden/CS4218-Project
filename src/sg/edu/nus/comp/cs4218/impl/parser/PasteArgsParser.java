package sg.edu.nus.comp.cs4218.impl.parser;

import java.util.List;

public class PasteArgsParser extends ArgsParser {
    private final static char FLAG_IS_SERIAL = 's';

    /**
     * Creates an instance of PasteArgsParser object
     */
    public PasteArgsParser() {
        super();
        legalFlags.add(FLAG_IS_SERIAL);
    }

    /**
     * Check whether the paste application will run in serial mode
     *
     * @return Run in serial mode
     */
    public boolean isSerial() {
        return flags.contains(FLAG_IS_SERIAL);
    }

    /**
     * Check whether the paste application involve only file(s)
     *
     * @return Only file(s) being pasted
     */
    public boolean isFilesOnly() {
        return nonFlagArgs.size() >= 1 && nonFlagArgs.stream().noneMatch("-"::equals);
    }

    /**
     * Check whether the paste application involve only stdin(s)
     *
     * @return Only stdin(s) being pasted
     */
    public boolean isStdinOnly() {
        return nonFlagArgs.size() >= 1 && nonFlagArgs.stream().allMatch("-"::equals);
    }

    /**
     * Get a list of Strings that represent the arguments passed into the Paste application (excluding flags)
     *
     * @return List of arguments excluding flags
     */
    public List<String> getInputs() {
        return nonFlagArgs;
    }
}
