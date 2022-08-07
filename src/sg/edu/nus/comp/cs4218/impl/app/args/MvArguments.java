package sg.edu.nus.comp.cs4218.impl.app.args;

import sg.edu.nus.comp.cs4218.exception.MvException;

import java.util.ArrayList;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_FLAG;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;


public class MvArguments {

    public static final char OVERWRITE_OPTION = 'n';
    private final List<String> files;
    private boolean overwrite;

    public MvArguments() {
        this.overwrite = true;
        this.files = new ArrayList<>();
    }

    /**
     * Handles argument list parsing for the `wc` application.
     *
     * @param args Array of arguments to parse
     */
    public void parse(String... args) throws MvException {
        boolean parsingFlag = true, isOverwrite = true;
        // Parse arguments
        if (args != null && args.length > 0) {
            for (String arg : args) {
                if (arg.isEmpty()) {
                    continue;
                }
                // `parsingFlag` is to ensure all flags come first, followed by files.
                if (parsingFlag && arg.charAt(0) == CHAR_FLAG_PREFIX && arg.length() > 1) {
                    for (char c : arg.toCharArray()) {
                        if (c == CHAR_FLAG_PREFIX) {
                            continue;
                        }
                        if (c == OVERWRITE_OPTION) {
                            isOverwrite = false;
                            continue;
                        }
                        throw new MvException(ERR_INVALID_FLAG);
                    }
                } else {
                    parsingFlag = false;
                    this.files.add(arg.trim());
                }
            }
        }
        this.overwrite = isOverwrite;
    }

    /**
     * Check if the "overwrite" return flag is set to turn
     *
     * @return the boolean that represent if the "overwrite" flag is set to true or false
     */
    public boolean isOverwrite() {
        return overwrite;
    }

    /**
     * Get the files given in the argument
     *
     * @return a list of files given in the argument
     */
    public List<String> getFiles() {
        return files;
    }
}
