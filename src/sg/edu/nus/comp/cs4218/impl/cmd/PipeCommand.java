package sg.edu.nus.comp.cs4218.impl.cmd;

import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

/**
 * A Pipe Command is a sub-command consisting of two Call Commands separated with a pipe,
 * or a Pipe Command and a Call Command separated with a pipe.
 * <p>
 * Command format: <Call> | <Call> or <Pipe> | <Call>
 */
public class PipeCommand implements Command {
    private final List<CallCommand> callCommands;

    public PipeCommand(List<CallCommand> callCommands) {
        this.callCommands = callCommands;
    }

    // The individual InputStream and OutputStream has been closed at the end of the method
    // therefore, warning is false positive.
    @SuppressWarnings("PMD.CloseResource")
    @Override
    public void evaluate(InputStream stdin, OutputStream stdout)
            throws AbstractApplicationException, ShellException, FileNotFoundException {
        AbstractApplicationException absAppException = null;
        ShellException shellException = null;

        InputStream nextInputStream = stdin;
        OutputStream nextOutputStream = null;

        for (int i = 0; i < callCommands.size(); i++) {
            CallCommand callCommand = callCommands.get(i);

            if (absAppException != null || shellException != null) {
                callCommand.terminate();
                continue;
            }

            try {
                nextOutputStream = new ByteArrayOutputStream();
                // BUG FOUND: The final output stream of piping was not stdout and this can cause terminal output to
                // be incorrect
                if (i == callCommands.size() - 1) {
                    nextOutputStream = stdout;
                }
                callCommand.evaluate(nextInputStream, nextOutputStream);
                // BUG FOUND: No need to pass the input stream further once the final call command is evaluated
                if (i != callCommands.size() - 1) {
                    nextInputStream = new ByteArrayInputStream(((ByteArrayOutputStream) nextOutputStream).toByteArray());
                }
            } catch (AbstractApplicationException e) {
                absAppException = e;
            } catch (ShellException e) {
                shellException = e;
            }
        }

        if (absAppException != null) {
            throw absAppException;
        }
        if (shellException != null) {
            throw shellException;
        }

        IOUtils.closeInputStream(nextInputStream);
        IOUtils.closeOutputStream(nextOutputStream);
    }

    @Override
    public void terminate() {
        // Unused for now
    }

    public List<CallCommand> getCallCommands() {
        return callCommands;
    }
}
