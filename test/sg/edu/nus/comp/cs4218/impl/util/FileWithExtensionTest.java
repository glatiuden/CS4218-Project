package sg.edu.nus.comp.cs4218.impl.util;


import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sg.edu.nus.comp.cs4218.impl.util.FileWithExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileWithExtensionTest {

    // CONSTANTS AND VARIABLE
    static final String UPPER_A_TXT = "Ab.txt";
    static final String ABC_NO_EXT = "abc";
    static final String SMALL_A_TXT = "aBc.txt";
    static final String C_EXT = "c.c";
    static final String NO_EXT = "noExtension";
    static final String UPPER_Z_TXT = "Za.txt";
    static final String COMPARE_SEC_TXT = "ZA.txt";
    static final String BIG_T_EXT = "ZA.Txt";
    static String[] allFiles = new String[]{
            UPPER_A_TXT, ABC_NO_EXT, SMALL_A_TXT, C_EXT, NO_EXT, UPPER_Z_TXT, COMPARE_SEC_TXT, BIG_T_EXT
    };
    static List<FileWithExtension> filesWithExt = new ArrayList<>();

    @BeforeAll
    static void setupAll() {
        for (String fileName : allFiles) {
            filesWithExt.add(new FileWithExtension(fileName));
        }
    }

    // File extension (file with no extension, file with extension)
    @Test
    public void getFileExtension_fileWithNoExtension_ReturnTrue() {
        FileWithExtension tempFile = new FileWithExtension(NO_EXT);
        assertEquals(tempFile.extension, "");
    }

    @Test
    public void getFileExtension_fileWithExtension_ReturnTrue() {
        FileWithExtension tempFile = new FileWithExtension(BIG_T_EXT);
        assertEquals(tempFile.extension, "Txt");
    }

    // Basically just have 1 test case to ensure that the all the non-ext is at the front, then c.c is before all the .txt
    // and then within each .txt, sort by their natural order, for ZA.txt and Za.txt, ensure that ZA.txt comes first
    @Test
    public void compareTo_sortFile_CorrectlySorted() {
        String[] expectedAns = new String[]{
                ABC_NO_EXT, NO_EXT, BIG_T_EXT, C_EXT, UPPER_A_TXT, COMPARE_SEC_TXT, UPPER_Z_TXT, SMALL_A_TXT
        };
        Collections.sort(filesWithExt);
        for (int i = 0; i < filesWithExt.size(); i++) {
            assertEquals(filesWithExt.get(i).fileName, expectedAns[i]);
        }
    }
}
