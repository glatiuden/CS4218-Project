package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.EchoException;
import sg.edu.nus.comp.cs4218.impl.app.EchoApplication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class EchoApplicationTest {

    // VARIABLES
    final String[] emptyArg = {};
    final String[] oneArg = {"A*B*C"};
    final String[] multiArgs = {"a", "b", "c"};

    EchoApplication echoApp = new EchoApplication();
    InputStream inputStreamStub = new ByteArrayInputStream("".getBytes());
    ByteArrayOutputStream stdOutResult;

    @BeforeEach
    public void setup() {
        stdOutResult = new ByteArrayOutputStream();
    }

    // Test case for constructResult()
    @Test
    public void constructResult_Empty_ReturnsTrue() throws EchoException {
        // echo
        assertEquals(STRING_NEWLINE, echoApp.constructResult(emptyArg));
    }

    @Test
    public void constructResult_OneArg_ReturnsTrue() throws EchoException {
        // echo A*B*C
        String expectedAns = "A*B*C" + STRING_NEWLINE;
        assertEquals(expectedAns, echoApp.constructResult(oneArg));
    }

    @Test
    public void constructResult_MultiArg_ReturnsTrue() throws EchoException {
        // echo a b c
        String expectedAns = "a b c" + STRING_NEWLINE;
        assertEquals(expectedAns, echoApp.constructResult(multiArgs));
    }

    @Test
    public void constructResult_NullArg_ThrowsException() {
        EchoException echoException = assertThrows(EchoException.class, () -> echoApp.constructResult(null));
        assertTrue(echoException.getMessage().contains(ERR_NULL_ARGS));
    }

    // Test case for Run()
    // args (empty, 1, multi), stdIn (Not used, ignored), stdOut (Normal stdOut)
    @Test
    public void run_empty_ReturnsTrue() throws EchoException {
        // echo
        echoApp.run(emptyArg, inputStreamStub, stdOutResult);
        assertEquals(STRING_NEWLINE, stdOutResult.toString());
    }

    @Test
    public void run_OneArg_ReturnsTrue() throws EchoException {
        // echo A*B*C
        String expectedAns = "A*B*C" + STRING_NEWLINE;
        echoApp.run(oneArg, inputStreamStub, stdOutResult);
        assertEquals(expectedAns, stdOutResult.toString());
    }

    @Test
    public void run_MultiArg_ReturnsTrue() throws EchoException {
        // echo a b c
        String expectedAns = "a b c" + STRING_NEWLINE;
        echoApp.run(multiArgs, inputStreamStub, stdOutResult);
        assertEquals(expectedAns, stdOutResult.toString());
    }

    // Negative Case (null stdout, null args)
    @Test
    public void run_NullArg_ThrowsException() {
        EchoException echoException = assertThrows(EchoException.class,
                () -> echoApp.run(null, inputStreamStub, stdOutResult));
        assertTrue(echoException.getMessage().contains(ERR_NULL_ARGS));
    }

    @Test
    public void run_NullStdOut_ThrowsException() {
        EchoException echoException = assertThrows(EchoException.class,
                () -> echoApp.run(new String[]{"test"}, inputStreamStub, null));
        assertTrue(echoException.getMessage().contains(ERR_NO_OSTREAM));
    }
}
