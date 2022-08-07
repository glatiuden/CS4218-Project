package sg.edu.nus.comp.cs4218.impl.app.args;

import sg.edu.nus.comp.cs4218.impl.util.CutUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static sg.edu.nus.comp.cs4218.impl.util.StringUtils.CHAR_FLAG_PREFIX;

public class CutArguments {

    public static final char CHAR_CHAR_POS = 'c';
    public static final char CHAR_BYTE_POS = 'b';
    public static final String INVALID_FLAG = "specify valid flags, \"-c\" for characters or \"-b\" for bytes";
    public static final String INVALID_LIST = "specify valid positions without spaces e.g. \"2\" or \"1-7\" or \"1,2,4\"";
    public static final String INVALID_DEC_RANGE = "decreasing range not allowed";
    public static final String INVALID_RANGE = "only positions numbered from 1 and below max integer allowed";
    private final List<String> files;
    private List<int[]> numList;
    private boolean characterPosition, bytePosition;

    /**
     * Creates a CutArguments object.
     */
    public CutArguments() {
        this.characterPosition = false;
        this.bytePosition = false;
        this.files = new ArrayList<>();
        this.numList = new ArrayList<>();
    }

    /**
     * Handles argument list parsing for the `cut` application.
     *
     * @param args Array of arguments to parse
     * @throws Exception if OPTION or LIST provided is invalid
     */
    public void parse(String... args) throws Exception {
        boolean parsingFlag = true, parsingList = true;
        // Parse arguments
        if (args != null && args.length > 0) {
            for (String arg : args) {
                arg = arg.trim();
                if (arg.isEmpty()) {
                    continue;
                }

                // must provide flag first
                if (parsingFlag) {
                    parsingFlag = false;
                    setOptions(arg);
                    continue;
                }

                // must provide list next
                if (parsingList) {
                    parsingList = false;
                    setList(arg);
                    continue;
                }

                // add all files
                this.files.add(arg);

            }
        }
        if (!this.characterPosition && !this.bytePosition) {
            throw new Exception(INVALID_FLAG);
        }
        if (numList.isEmpty()) {
            throw new Exception(INVALID_LIST);
        }
    }

    /**
     * Handles the parsing of OPTION in the argument.
     *
     * @param optionArg Argument that contains the OPTION.
     * @throws Exception if OPTION provided is invalid
     */
    private void setOptions(String optionArg) throws Exception {
        if (optionArg.length() != 2 || optionArg.charAt(0) != CHAR_FLAG_PREFIX) {
            throw new Exception(INVALID_FLAG);
        }

        char option = optionArg.charAt(1);
        if (option != CHAR_CHAR_POS && option != CHAR_BYTE_POS) {
            throw new Exception(INVALID_FLAG);
        }

        if (option == CHAR_CHAR_POS) {
            this.characterPosition = true;
        } else {
            this.bytePosition = true;
        }
    }

    /**
     * Handles the parsing of LIST in the argument.
     *
     * @param listArg Argument that contains the LIST.
     * @throws Exception if LIST provided is invalid
     */
    private void setList(String listArg) throws Exception {
        if (listArg.isEmpty() || listArg.charAt(listArg.length() - 1) == ',') {
            throw new Exception(INVALID_LIST);
        }

        String[] lists = listArg.split(",");
        try {
            for (String list : lists) {
                if (list.isEmpty()) {
                    throw new Exception(INVALID_LIST);
                }
                // check if is range
                int dashCount = CutUtils.countDashAndNumCheck(list);
                if (dashCount == 0) {
                    int num = Integer.parseInt(list);
                    if (num <= 0) {
                        throw new Exception(INVALID_RANGE);
                    }
                    numList.add(new int[]{num, num});
                } else if (dashCount == 1) {
                    numList.add(CutUtils.getNumRange(list));
                } else {
                    throw new Exception(INVALID_LIST);
                }
                numList = CutUtils.mergeNumRanges(numList);
            }
        } catch (NumberFormatException e) {
            throw new Exception(INVALID_RANGE, e);
        }
        numList.sort(Comparator.comparing(o -> o[0]));
    }

    /**
     * Returns the list of position intervals in the argument.
     *
     * @return List of position intervals
     */
    public List<int[]> getNumList() {
        return this.numList;
    }

    /**
     * Returns the list of file paths in the argument.
     *
     * @return List of file paths
     */
    public List<String> getFiles() {
        return this.files;
    }

    /**
     * Returns the boolean indicating to cut by character.
     *
     * @return Boolean indicating cut by character.
     */
    public boolean isCharacterPosition() {
        return this.characterPosition;
    }

    /**
     * Returns the boolean indicating to cut by byte.
     *
     * @return Boolean indicating cut by byte.
     */
    public boolean isBytePosition() {
        return this.bytePosition;
    }
}
