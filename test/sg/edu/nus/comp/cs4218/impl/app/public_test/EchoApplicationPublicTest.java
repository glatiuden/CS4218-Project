package sg.edu.nus.comp.cs4218.impl.app.public_test;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.impl.app.EchoApplication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.testutils.TestStringUtils.STRING_NEWLINE;

public class EchoApplicationPublicTest {
    private static final String TEXT_TEST = "test";
    private static final String TEXT_NEW_LINE = "testing" + STRING_NEWLINE;
    private static final String EXPECTED_SINGLE = TEXT_TEST;
    private static final String EXPECT_MULTILINE = TEXT_NEW_LINE + " " + TEXT_TEST;

    private EchoApplication echoApplication;

    @BeforeEach
    void setUp() {
        echoApplication = new EchoApplication();
    }

    @Test
    void constructResults_NullArguments_ThrowsException() {
        assertThrows(Exception.class, () -> echoApplication.constructResult(null));
    }

    @Test
    void constructResults_NoArguments_ReturnsEmptyString() throws Exception {
        String[] args = new String[0];
        String output = echoApplication.constructResult(args);
        assertEquals(STRING_NEWLINE, output);
    }

    @Test
    void constructResults_SingleArgument_ReturnsArgument() throws Exception {
        String output = echoApplication.constructResult(new String[]{TEXT_TEST});
        assertEquals(EXPECTED_SINGLE + STRING_NEWLINE, output);
    }

    @Test
    void constructResults_MultipleArgumentsWithNewLine_ReturnsTextWithWhiteSpace() throws Exception {
        String output = echoApplication.constructResult(new String[]{TEXT_NEW_LINE, TEXT_TEST});
        assertEquals(EXPECT_MULTILINE + STRING_NEWLINE, output);
    }
}
