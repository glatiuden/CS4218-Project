package sg.edu.nus.comp.cs4218.impl.parser;

import sg.edu.nus.comp.cs4218.exception.TeeException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_IS_DIR;

public class TeeArgsParser extends ArgsParser {
    private final static char FLAG_IS_APPENDING = 'a';

    /**
     * Creates an instance of TeeArgsParser object
     */
    public TeeArgsParser() {
        super();
        legalFlags.add(FLAG_IS_APPENDING);
    }

    /**
     * Check whether the tee application should append the input to the files' content
     *
     * @return Append input to the files
     */
    public Boolean isAppending() {
        return flags.contains(FLAG_IS_APPENDING);
    }


    /**
     * Validate whether the given list of paths are all paths to file
     *
     * @param pathStrs List of strings representing paths to file
     * @throws TeeException Exception thrown during a tee command
     */
    private void validatePaths(List<String> pathStrs) throws TeeException {
        for (String pathStr : pathStrs) {
            Path path = Path.of(pathStr);
            if (Files.exists(path) && Files.isDirectory(path)) {
                throw new TeeException(ERR_IS_DIR);
            }
        }
    }

    /**
     * Get a list of Strings of paths to the files that will be written to
     *
     * @return List of Strings of paths to the files to be written to
     */
    public List<String> getFilePaths() throws TeeException {
        validatePaths(nonFlagArgs);
        for (int i = 0; i < nonFlagArgs.size(); i++) {
            nonFlagArgs.set(i, absolutePathArg(nonFlagArgs.get(i)));
        }
        return nonFlagArgs;
    }
}
