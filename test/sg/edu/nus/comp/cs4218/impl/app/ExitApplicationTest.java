package sg.edu.nus.comp.cs4218.impl.app;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.exception.ExitException;
import sg.edu.nus.comp.cs4218.impl.app.ExitApplication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static sg.edu.nus.comp.cs4218.impl.app.ExitApplication.EXIT_MESSAGE;

class ExitApplicationTest {
    private static final String ERROR_PREFIX = "exit: %s";
    private static ExitApplication exitApplication;

    @BeforeAll
    static void setupExitApplication() {
        exitApplication = new ExitApplication();
    }

    @Test
    void run_normalExecution_shouldThrowExitException() {
        Throwable thrown = assertThrows(ExitException.class, () -> exitApplication.run(new String[0], System.in, System.out));
        assertEquals(String.format(ERROR_PREFIX, EXIT_MESSAGE), thrown.getMessage());
    }

    @Test
    void terminateExecution_normalExecution_shouldThrowExitException() {
        Throwable thrown = assertThrows(ExitException.class, () -> exitApplication.terminateExecution());
        assertEquals(String.format(ERROR_PREFIX, EXIT_MESSAGE), thrown.getMessage());
    }
}