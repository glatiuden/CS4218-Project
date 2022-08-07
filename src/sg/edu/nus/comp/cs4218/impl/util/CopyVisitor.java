package sg.edu.nus.comp.cs4218.impl.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

public class CopyVisitor extends SimpleFileVisitor<Path> {
    private final Path srcPath;
    private final Path destPath;

    /**
     * Constructs a CopyVisitor object
     *
     * @param srcPath  Path to the source folder
     * @param destPath Path to the destination folder
     */
    public CopyVisitor(Path srcPath, Path destPath) {
        super();
        this.srcPath = srcPath;
        this.destPath = destPath;
    }

    /**
     * Callback that is executed before this visitor enters directory
     *
     * @param dir   Path to the directory that is going to be visited
     * @param attrs Attributes of the directory that is going to be visited
     * @return Results of the visit
     * @throws IOException Exception thrown when trying to create the directory if it does
     */
    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        Files.createDirectories(destPath.resolve(srcPath.relativize(dir)));
        return FileVisitResult.CONTINUE;
    }

    /**
     * Callback that is executed when this visitor access a file
     *
     * @param file  Path to the file that is being accessed
     * @param attrs Attributes of the file that is being accessed
     * @return Results of the access
     * @throws IOException Exception thrown when trying to copy the file
     */
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.copy(file, destPath.resolve(srcPath.relativize(file)), StandardCopyOption.REPLACE_EXISTING);
        return FileVisitResult.CONTINUE;
    }
}
