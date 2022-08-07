package sg.edu.nus.comp.cs4218.testutils;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public final class CutAssertUtils {
    private CutAssertUtils() {
    }

    /**
     * Assert two list of integer arrays are equal
     *
     * @param list1 first list of integer array to compare
     * @param list2 second list of integer array to compare
     */
    public static void assertListEquals(List<int[]> list1, List<int[]> list2) {
        assertEquals(list1.size(), list2.size());
        for (int i = 0; i < list1.size(); i++) {
            int[] array1 = list1.get(i), array2 = list2.get(i);
            assertTrue(Arrays.equals(array1, array2));
        }
    }
}
