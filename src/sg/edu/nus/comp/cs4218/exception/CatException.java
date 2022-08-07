package sg.edu.nus.comp.cs4218.exception;

import java.io.File;

import static sg.edu.nus.comp.cs4218.impl.util.FileUtil.getRelativeToCwd;

public class CatException extends AbstractApplicationException {

    private static final long serialVersionUID = 2333796686823942499L;

    public CatException(String message) {
        super("cat: " + message);
    }

    public CatException(String message, Throwable cause) {
        super("cat: " + message, cause);
    }

    public CatException(File file, String message) {
        super("cat: " + getRelativeToCwd(file.toPath()) + ": " + message);
    }
}