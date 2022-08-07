package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_MULTIPLE_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_NULL_STREAMS;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_SYNTAX;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_INPUT;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_REDIR_OUTPUT;

public class IORedirectionHandler {
    private final List<String> argsList;
    private final ArgumentResolver argumentResolver;
    private final InputStream origInputStream;
    private final OutputStream origOutputStream;
    private List<String> noRedirArgsList;
    private InputStream inputStream;
    private OutputStream outputStream;

    /**
     * Constructs an IORedirectionHandler object.
     *
     * @param argsList         Argument list to be parsed
     * @param origInputStream  Original InputStream
     * @param origOutputStream Original OutputStream
     * @param argumentResolver ArgumentResolver
     */
    public IORedirectionHandler(List<String> argsList, InputStream origInputStream,
                                OutputStream origOutputStream, ArgumentResolver argumentResolver) {
        this.argsList = argsList;
        this.inputStream = origInputStream;
        this.origInputStream = origInputStream;
        this.outputStream = origOutputStream;
        this.origOutputStream = origOutputStream;
        this.argumentResolver = argumentResolver;
    }

    /**
     * Extracts the files specified for IO redirection and opens their stream.
     *
     * @throws AbstractApplicationException If error occur while resolving argument
     * @throws ShellException               If invalid IO Redirection syntax or error opening streams
     * @throws FileNotFoundException        If file is not found
     */
    public void extractRedirOptions() throws AbstractApplicationException, ShellException, FileNotFoundException { //NOPMD - suppressed ExcessiveMethodLength - Part of the skeleton code provided and mainly due to the amount of exceptions needed to be thrown.
        // BUG FOUND: If condition was wrong. Should be using || instead of &&, check if argsList is null or empty, not and.
        if (argsList == null || argsList.isEmpty()) {
            throw new ShellException(ERR_SYNTAX);
        }
        noRedirArgsList = new LinkedList<>();

        // extract redirection operators (with their corresponding files) from argsList
        ListIterator<String> argsIterator = argsList.listIterator();
        while (argsIterator.hasNext()) {
            String arg = argsIterator.next();

            // leave the other args untouched
            if (!isRedirOperator(arg)) {
                noRedirArgsList.add(arg);
                continue;
            }

            // if current arg is < or > but with no specified file, wrong syntax.
            if (!argsIterator.hasNext()) {
                closeIOStreams();
                throw new ShellException(ERR_SYNTAX);
            }

            // if current arg is < or >, fast-forward to the next arg to extract the specified file
            String file = argsIterator.next();

            // if supposed file name is < or >, wrong syntax since no argument.
            if (isRedirOperator(file)) {
                closeIOStreams();
                throw new ShellException(ERR_SYNTAX);
            }

            // handle quoting + globing + command substitution in file arg
            List<String> fileSegment = argumentResolver.resolveOneArgument(file);
            if (fileSegment.size() > 1) {
                // ambiguous redirect if file resolves to more than one parsed arg
                closeIOStreams();
                throw new ShellException(ERR_SYNTAX);
            }
            file = fileSegment.get(0);

            // replace existing inputStream / outputStream
            if (arg.equals(String.valueOf(CHAR_REDIR_INPUT))) {
                IOUtils.closeInputStream(inputStream);
                if (inputStream == null) {
                    closeIOStreams();
                    throw new ShellException(ERR_NULL_STREAMS);
                }
                if (!inputStream.equals(origInputStream)) { // Already have a stream
                    closeIOStreams();
                    throw new ShellException(ERR_MULTIPLE_STREAMS);
                }
                inputStream = IOUtils.openInputStream(file);
            } else if (arg.equals(String.valueOf(CHAR_REDIR_OUTPUT))) {
                IOUtils.closeOutputStream(outputStream);
                if (outputStream == null) {
                    closeIOStreams();
                    throw new ShellException(ERR_NULL_STREAMS);
                }
                if (!outputStream.equals(origOutputStream)) { // Already have a stream
                    closeIOStreams();
                    throw new ShellException(ERR_MULTIPLE_STREAMS);
                }
                outputStream = IOUtils.openOutputStream(file);
            }
        }
    }

    /**
     * Closes input and output streams.
     *
     * @throws ShellException If error occur while closing streams
     */
    private void closeIOStreams() throws ShellException {
        if (inputStream != null && !inputStream.equals(origInputStream)) {
            IOUtils.closeInputStream(inputStream);
        }
        if (outputStream != null && !outputStream.equals(origOutputStream)) {
            IOUtils.closeOutputStream(outputStream);
        }
    }

    /**
     * Returns the list of arguments that is not part of IO redirection.
     *
     * @return list of arguments not part of IO redirection.
     */
    public List<String> getNoRedirArgsList() {
        return noRedirArgsList;
    }

    /**
     * Returns the opened InputStream.
     *
     * @return opened InputStream.
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * Returns the opened OutputStream.
     *
     * @return opened OutputStream.
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Checks whether the string is a redirection operator.
     *
     * @param str Input string to check.
     * @return True if string is redirection operator. False otherwise.
     */
    private boolean isRedirOperator(String str) {
        // BUG FOUND: Should check if string equals ">" as well. Should check if string equals either "<" or ">".
        return str.equals(String.valueOf(CHAR_REDIR_INPUT)) || str.equals(String.valueOf(CHAR_REDIR_OUTPUT));
    }
}