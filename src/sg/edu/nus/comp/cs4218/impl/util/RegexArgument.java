package sg.edu.nus.comp.cs4218.impl.util;

import sg.edu.nus.comp.cs4218.Environment;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_ASTERISK;

@SuppressWarnings("PMD.AvoidStringBufferField")
public final class RegexArgument {
    private StringBuilder plaintext;
    private StringBuilder regex;
    private boolean isRegexPattern;

    public RegexArgument() {
        this.plaintext = new StringBuilder();
        this.regex = new StringBuilder();
        this.isRegexPattern = false;
    }

    public RegexArgument(String str) {
        this();
        merge(str);
    }

    /**
     * Used for `find` command.
     *
     * @param str            regex string
     * @param text           the folder that we want to look in
     * @param isRegexPattern boolean flag to indicate is it a regex
     */
    public RegexArgument(String str, String text, boolean isRegexPattern) {
        this();
        this.plaintext.append(text);
        this.isRegexPattern = isRegexPattern;
        this.regex.append(".*"); // We want to match filenames
        for (char c : str.toCharArray()) {
            if (c == CHAR_ASTERISK) {
                this.regex.append("[^" + StringUtils.fileSeparator() + "]*");
            } else {
                this.regex.append(Pattern.quote(String.valueOf(c)));
            }
        }
    }

    /**
     * Append char to the regex pattern
     *
     * @param chr char to append to the regex pattern
     */
    public void append(char chr) {
        plaintext.append(chr);
        regex.append(Pattern.quote(String.valueOf(chr)));
    }


    /**
     * Append asterisk to the regex pattern
     */
    public void appendAsterisk() {
        plaintext.append(CHAR_ASTERISK);
        regex.append("[^").append(StringUtils.fileSeparator()).append("]*");
        isRegexPattern = true;
    }

    /**
     * Merge by copying another RegexArgument's attributes
     *
     * @param other RegexArgument to be merged
     */
    public void merge(RegexArgument other) {
        plaintext.append(other.plaintext);
        regex.append(other.regex);
        isRegexPattern = isRegexPattern || other.isRegexPattern;
    }

    /**
     * Merge by appending another string to RegexArgument's regex pattern
     *
     * @param str string to append to the regex pattern
     */
    public void merge(String str) {
        plaintext.append(str);
        regex.append(Pattern.quote(str));
    }

    /**
     * Performs globbing and return files that matches the regex pattern
     *
     * @return a ArrayList<String> of files and folders names that matches the regex pattern
     */
    public List<String> globFiles() {
        List<String> globbedFiles = new LinkedList<>();

        if (isRegexPattern) {
            Pattern regexPattern = Pattern.compile(regex.toString());
            String dir = "";
            String[] tokens = plaintext.toString().replaceAll("\\\\", "/").split("/");
            for (int i = 0; i < tokens.length - 1; i++) {
                // BUG FOUND: File.separator can give "\" on Windows which is an invalid separator on unix platform
                // and "/" is usable on either OS
                dir += tokens[i] + "/";
            }

            File currentDir = Paths.get(Environment.currentDirectory + File.separator + dir).toFile();

            for (String candidate : Objects.requireNonNull(currentDir.list())) {
                // BUG FOUND: Didn't include the directory where the source file resides in, cannot copy any file
                // that is >= 1 level deep
                if (regexPattern.matcher(dir + candidate).matches()) {
                    globbedFiles.add(dir + candidate);
                }
            }

            Collections.sort(globbedFiles);
        }

        if (globbedFiles.isEmpty()) {
            globbedFiles.add(plaintext.toString());
        }

        return globbedFiles;
    }


    /**
     * Traverses a given File node and returns a list of absolute path that match the given regexPattern.
     * <p>
     * Assumptions:
     * - ignores files and folders that we do not have access to (insufficient read permissions)
     * - regexPattern should not be null
     *
     * @param regexPattern    Pattern object
     * @param node            File object
     * @param isAbsolute      Boolean option to indicate that the regexPattern refers to an absolute path
     * @param onlyDirectories Boolean option to list only the directories
     */
    private List<String> traverseAndFilter(Pattern regexPattern, File node, boolean isAbsolute, boolean onlyDirectories) {
        List<String> matches = new ArrayList<>();
        if (regexPattern == null || !node.canRead() || !node.isDirectory()) {
            return matches;
        }
        for (String current : Objects.requireNonNull(node.list())) {
            File nextNode = new File(node, current);
            String match = isAbsolute ? nextNode.getPath() : nextNode.getPath().substring(Environment.currentDirectory.length() + 1);
            // TODO: Find a better way to handle this.
            if (onlyDirectories && nextNode.isDirectory()) {
                match += File.separator;
            }
            if (!nextNode.isHidden() && regexPattern.matcher(match).matches()) {
                matches.add(nextNode.getAbsolutePath());
            }
            matches.addAll(traverseAndFilter(regexPattern, nextNode, isAbsolute, onlyDirectories));
        }
        return matches;
    }


    /**
     * Check whether the RegexArgument is a regex
     *
     * @return true if it is a regex
     */
    public boolean isRegex() {
        return isRegexPattern;
    }

    /**
     * Check whether the RegexArgument is empty
     *
     * @return true if it is empty
     */
    public boolean isEmpty() {
        return plaintext.length() == 0;
    }

    /**
     * Retrieve the string value of the regex pattern
     *
     * @return regex pattern in string
     */
    public String toString() {
        return plaintext.toString();
    }
}
