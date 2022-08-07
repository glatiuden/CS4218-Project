package sg.edu.nus.comp.cs4218.impl.cmd;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;
import sg.edu.nus.comp.cs4218.impl.util.IORedirectionHandler;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

/**
 * A Call Command is a sub-command consisting of at least one non-keyword or quoted.
 * <p>
 * Command format: (<non-keyword> or <quoted>) *
 */
public class CallCommand implements Command {
    private final List<String> argsList;
    private final ApplicationRunner appRunner;
    private final ArgumentResolver argumentResolver;

    /**
     * Constructor for call command, sets the arguments needed for this command.
     *
     * @param argsList         The original list of arguments
     * @param appRunner        Application runner for the application
     * @param argumentResolver Helps to resolve the arguments and handle quoting + globing + command substitution issues
     */
    public CallCommand(List<String> argsList, ApplicationRunner appRunner, ArgumentResolver argumentResolver) {
        this.argsList = argsList;
        this.appRunner = appRunner;
        this.argumentResolver = argumentResolver;
    }

    /**
     * Evaluates command using data provided through stdin stream. Write result to stdout stream.
     *
     * @param stdin  Input data from the user
     * @param stdout Output stream to write the result
     * @throws AbstractApplicationException when there is any unknown error
     * @throws ShellException               when there is any shell error
     * @throws FileNotFoundException        when there is any file error
     */
    @Override
    public void evaluate(InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException, ShellException, FileNotFoundException {
        if (stdin == null) {
            throw new ShellException(ERR_NO_ISTREAM);
        }
        if (stdout == null) {
            throw new ShellException(ERR_NO_OSTREAM);
        }
        if (argsList == null || argsList.isEmpty()) {
            throw new ShellException(ERR_SYNTAX);
        }

        // Handle IO redirection
        IORedirectionHandler redirHandler = new IORedirectionHandler(argsList, stdin, stdout, argumentResolver);
        redirHandler.extractRedirOptions();
        List<String> noRedirArgsList = redirHandler.getNoRedirArgsList();
        InputStream inputStream = redirHandler.getInputStream(); //NOPMD - closed in line 65
        OutputStream outputStream = redirHandler.getOutputStream(); //NOPMD - closed in line 66

        // Handle quoting + globing + command substitution
        List<String> parsedArgsList = argumentResolver.parseArguments(noRedirArgsList);
        try {
            // BUG FOUND: It should be !parsedArgsList.isEmpty() instead of parsedArgsList.isEmpty() as it should run the app if argument is not empty
            if (!parsedArgsList.isEmpty()) {
                String app = parsedArgsList.remove(0);
                appRunner.runApp(app, parsedArgsList.toArray(new String[0]), inputStream, outputStream);
            }
        } finally {
            // Close out output streams to IO-redirected files, allowing clean up and tear down
            IOUtils.closeOutputStream(outputStream);
            IOUtils.closeInputStream(inputStream);
        }
    }

    @Override
    public void terminate() {
        // Unused for now
    }
}
