package sg.edu.nus.comp.cs4218.impl.util;

import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.Command;
import sg.edu.nus.comp.cs4218.exception.ShellException;
import sg.edu.nus.comp.cs4218.impl.cmd.CallCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.PipeCommand;
import sg.edu.nus.comp.cs4218.impl.cmd.SequenceCommand;
import sg.edu.nus.comp.cs4218.impl.util.ApplicationRunner;
import sg.edu.nus.comp.cs4218.impl.util.CommandBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static sg.edu.nus.comp.cs4218.impl.util.ErrorConstants.*;

public class CommandBuilderTest {

    // commandStrings (1 command, semi-colon)
    @Test
    public void parseCommand_1command_ReturnsTrue() throws ShellException {
        String commandString = "echo abc";
        Command command = CommandBuilder.parseCommand(commandString, new ApplicationRunner());
        assertEquals(command.getClass(), CallCommand.class);
    }

    @Test
    public void parseCommand_1commandEndsSemi_ReturnsTrue() throws ShellException {
        String commandString = "echo abc;";
        Command command = CommandBuilder.parseCommand(commandString, new ApplicationRunner());
        assertEquals(command.getClass(), CallCommand.class);
    }

    @Test
    public void parseCommand_MultiCommand_ReturnsTrue() throws ShellException {
        String commandString = "echo abc; ls *; wc";
        Command command = CommandBuilder.parseCommand(commandString, new ApplicationRunner());
        assertEquals(command.getClass(), SequenceCommand.class);
        List<Command> commands = ((SequenceCommand) command).getCommands();
        assertEquals(commands.size(), 3);
        assertEquals(commands.get(0).getClass(), CallCommand.class);
        assertEquals(commands.get(1).getClass(), CallCommand.class);
        assertEquals(commands.get(2).getClass(), CallCommand.class);
    }

    @Test
    public void parseCommand_MultiCommandEndWSemi_ReturnsTrue() throws ShellException {
        String commandString = "echo abc; ls *; wc;";
        Command command = CommandBuilder.parseCommand(commandString, new ApplicationRunner());
        assertEquals(command.getClass(), SequenceCommand.class);
        List<Command> commands = ((SequenceCommand) command).getCommands();
        assertEquals(commands.size(), 3);
        assertEquals(commands.get(0).getClass(), CallCommand.class);
        assertEquals(commands.get(1).getClass(), CallCommand.class);
        assertEquals(commands.get(2).getClass(), CallCommand.class);
    }

    @Test
    public void parseCommand_PipeCommand_ReturnsTrue() throws ShellException {
        String commandString = "ls *| wc | echo 'abc'";
        Command command = CommandBuilder.parseCommand(commandString, new ApplicationRunner());
        assertEquals(command.getClass(), PipeCommand.class);
        List<CallCommand> commands = ((PipeCommand) command).getCallCommands();
        assertEquals(commands.size(), 3);
        // No need to assure that all is CallCommand since it IS a list of CallCommand
    }

    @Test
    public void parseCommand_CallAndPipeCommand_ReturnsTrue() throws ShellException {
        String commandString = "echo abc; ls *| wc; echo test";
        Command command = CommandBuilder.parseCommand(commandString, new ApplicationRunner());
        assertEquals(command.getClass(), SequenceCommand.class);
        List<Command> commands = ((SequenceCommand) command).getCommands();
        assertEquals(commands.size(), 3);
        assertEquals(commands.get(0).getClass(), CallCommand.class);
        assertEquals(commands.get(1).getClass(), PipeCommand.class);
        assertEquals(commands.get(2).getClass(), CallCommand.class);
    }

    @Test
    public void parseCommand_PipeAndCallCommand_ReturnsTrue() throws ShellException {
        String commandString = "ls *| wc;echo abc; echo test";
        Command command = CommandBuilder.parseCommand(commandString, new ApplicationRunner());
        assertEquals(command.getClass(), SequenceCommand.class);
        List<Command> commands = ((SequenceCommand) command).getCommands();
        assertEquals(commands.size(), 3);
        assertEquals(commands.get(0).getClass(), PipeCommand.class);
        assertEquals(commands.get(1).getClass(), CallCommand.class);
        assertEquals(commands.get(2).getClass(), CallCommand.class);
    }

    // Negative Test Case
    @Test
    public void parseCommand_EmptyCommand_ThrowsException() {
        String commandString = "";
        ShellException exception = assertThrows(ShellException.class,
                () -> CommandBuilder.parseCommand(commandString, new ApplicationRunner()));
        assertTrue(exception.toString().contains(ERR_SYNTAX));
    }

    @Test
    public void parseCommand_nullCommand_ThrowsException() {
        ShellException exception = assertThrows(ShellException.class,
                () -> CommandBuilder.parseCommand(null, new ApplicationRunner()));
        assertTrue(exception.toString().contains(ERR_SYNTAX));
    }

    @Test
    public void parseCommand_onlySpaceCommand_ThrowsException() {
        String commandString = "          ";
        ShellException exception = assertThrows(ShellException.class,
                () -> CommandBuilder.parseCommand(commandString, new ApplicationRunner()));
        assertTrue(exception.toString().contains(ERR_SYNTAX));
    }

    @Test
    public void parseCommand_doubleSemi_ThrowsException() {
        String commandString = "echo ls;; ls *";
        ShellException exception = assertThrows(ShellException.class,
                () -> CommandBuilder.parseCommand(commandString, new ApplicationRunner()));
        assertTrue(exception.toString().contains(ERR_SYNTAX));
    }

    @Test
    public void parseCommand_doublePipe_ThrowsException() {
        String commandString = "echo ls|| ls *";
        ShellException exception = assertThrows(ShellException.class,
                () -> CommandBuilder.parseCommand(commandString, new ApplicationRunner()));
        assertTrue(exception.toString().contains(ERR_SYNTAX));
    }

    @Test
    public void parseCommand_pipeAndSemi_ThrowsException() {
        String commandString = "echo ls|; ls *";
        ShellException exception = assertThrows(ShellException.class,
                () -> CommandBuilder.parseCommand(commandString, new ApplicationRunner()));
        assertTrue(exception.toString().contains(ERR_SYNTAX));
    }
}
