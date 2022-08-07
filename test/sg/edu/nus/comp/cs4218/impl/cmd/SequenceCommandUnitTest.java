package sg.edu.nus.comp.cs4218.impl.cmd;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.AbstractApplicationException;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.ArgumentResolver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.ERR_INVALID_APP;
import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.STRING_NEWLINE;

public class SequenceCommandUnitTest {
    final static String RIGHT_ANSWER = "abc";
    final static List<String> VALID_ARG_STUB = List.of("echo", RIGHT_ANSWER);
    final static List<String> INVALID_ARG_STUB = List.of(";ech;");
    List<Command> commands;
    ArgumentResolver argumentResolver = new ArgumentResolver();
    ApplicationRunner appRunner = new ApplicationRunner();
    ByteArrayOutputStream stdOutResult;
    InputStream inputStreamStdIn = mock(ByteArrayInputStream.class);
    CallCommand callCmdStub = new CallCommand(VALID_ARG_STUB, appRunner, argumentResolver);
    CallCommand invalidCallStub = new CallCommand(INVALID_ARG_STUB, appRunner, argumentResolver);

    @BeforeEach
    void setup() {
        commands = new LinkedList<>();
        stdOutResult = new ByteArrayOutputStream();
    }


    // Test case for evaluate(), commands with empty, 1 multi args, test to ensure that even with failed argument,
    // the other commands should work as expected
    @Test
    public void evaluate_empty_ReturnsTrue() throws FileNotFoundException, AbstractApplicationException, ShellException {
        SequenceCommand seqCommand = new SequenceCommand(commands);
        seqCommand.evaluate(inputStreamStdIn, stdOutResult);
        assertEquals("", stdOutResult.toString());
    }

    @Test
    public void evaluate_1Command_ReturnsTrue() throws FileNotFoundException, AbstractApplicationException, ShellException {
        commands.add(callCmdStub);
        SequenceCommand seqCommand = new SequenceCommand(commands);
        seqCommand.evaluate(inputStreamStdIn, stdOutResult);
        assertEquals(RIGHT_ANSWER + STRING_NEWLINE, stdOutResult.toString());
    }

    @Test
    public void evaluate_MultiCommand_ReturnsTrue() throws FileNotFoundException, AbstractApplicationException, ShellException {
        commands.add(callCmdStub);
        commands.add(callCmdStub);
        commands.add(callCmdStub);
        commands.add(callCmdStub);
        SequenceCommand seqCommand = new SequenceCommand(commands);
        String expectedAns = RIGHT_ANSWER + STRING_NEWLINE + RIGHT_ANSWER + STRING_NEWLINE + RIGHT_ANSWER + STRING_NEWLINE
                + RIGHT_ANSWER + STRING_NEWLINE;
        seqCommand.evaluate(inputStreamStdIn, stdOutResult);
        assertEquals(expectedAns, stdOutResult.toString());
    }

    // Ensure that having a fail command still allows the other command to run
    @Test
    public void evaluate_FailCommand_ReturnsTrue() throws FileNotFoundException, AbstractApplicationException, ShellException {
        commands.add(callCmdStub);
        commands.add(invalidCallStub);
        commands.add(callCmdStub);
        SequenceCommand seqCommand = new SequenceCommand(commands);
        String expectedAns = RIGHT_ANSWER + STRING_NEWLINE
                + "shell: ;ech;: " + ERR_INVALID_APP + STRING_NEWLINE
                + RIGHT_ANSWER + STRING_NEWLINE;
        seqCommand.evaluate(inputStreamStdIn, stdOutResult);
        assertEquals(expectedAns, stdOutResult.toString());
    }

    // Test case for getCommands()
    @Test
    public void getCommands_correctSize_ReturnsTrue() {
        commands.add(callCmdStub);
        commands.add(callCmdStub);
        commands.add(callCmdStub);
        SequenceCommand seqCommand = new SequenceCommand(commands);
        assertEquals(seqCommand.getCommands().size(), 3);
        assertEquals(seqCommand.getCommands().get(0).getClass(), CallCommand.class);
        assertEquals(seqCommand.getCommands().get(1).getClass(), CallCommand.class);
        assertEquals(seqCommand.getCommands().get(2).getClass(), CallCommand.class);
    }
}
