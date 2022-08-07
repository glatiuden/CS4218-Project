package sg.edu.nus.comp.cs4218.impl.util;

public class FileWithExtension implements Comparable<FileWithExtension> {
    public String fileName;
    public String extension;

    /**
     * Constructor method for file with extension. Takes in a file name and get the extension of it
     *
     * @param fileName of the given file to be compared
     */
    public FileWithExtension(String fileName) {
        this.fileName = fileName;
        this.extension = getFileExtension(fileName);
    }

    /**
     * Used to get the file extension by checking the last index of .
     *
     * @param fileName of the file to extract out the file extension
     * @return a string representation of the given file's extension
     */
    private String getFileExtension(String fileName) {
        String extension = "";
        int lastIndex = fileName.lastIndexOf('.');
        if (lastIndex >= 0) {
            extension = fileName.substring(lastIndex + 1);
        }
        return extension;
    }

    /**
     * Compare to method to compare against another FileWithExtension object. It is sorted by extension first,
     * and then sorted by alphabetical order if the extension is the same.
     *
     * @param otherFile which this object should compare to
     * @return an int representation of the ordering of the files
     */
    @Override
    public int compareTo(FileWithExtension otherFile) {
        return extension.compareTo(otherFile.extension) == 0
                ? fileName.compareTo(otherFile.fileName)
                : extension.compareTo(otherFile.extension);
    }
}
