package sg.edu.nus.comp.cs4218.impl.parser;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.InvalidArgsException;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_ARG;

public class LsArgsParserFixTest {
    LsArgsParser lsArgs;

    @BeforeEach
    void setup() {
        lsArgs = new LsArgsParser();
    }

    // Fix for bug 2
    @Test
    public void parse_emptyArgs_ThrowsError() {
        String[] args = {""};
        InvalidArgsException exception = assertThrows(InvalidArgsException.class, () -> lsArgs.parse(args));
        assertTrue(exception.toString().contains(ERR_INVALID_ARG));
    }
}
