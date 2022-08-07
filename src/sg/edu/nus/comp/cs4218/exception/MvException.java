package sg.edu.nus.comp.cs4218.exception;

public class MvException extends AbstractApplicationException {

    private static final long serialVersionUID = -8535567786679220113L;

    public MvException(String message) {
        super("mv: " + message);
    }

    public MvException(Exception exception, String message) {
        super("mv: " + exception.getMessage() + ": " + message);
    }
}
