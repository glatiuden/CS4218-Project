/*
 * This file was automatically generated by EvoSuite
 * Sat Mar 19 08:28:04 GMT 2022
 */

package sg.edu.nus.comp.cs4218.impl.app.args;

import org.evosuite.runtime.EvoRunnerJUnit5;
import org.evosuite.runtime.EvoRunnerParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.evosuite.runtime.EvoAssertions.verifyException;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("PMD")
@EvoRunnerParameters(mockJVMNonDeterminism = true, useVFS = true, useVNET = true, resetStaticState = true, separateClassLoader = false)
public class GrepArguments_ESTest extends GrepArguments_ESTest_scaffolding {
    @RegisterExtension
    static EvoRunnerJUnit5 runner = new EvoRunnerJUnit5(GrepArguments_ESTest.class);

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test00() throws Throwable {
        GrepArguments grepArguments0 = new GrepArguments();
        String[] stringArray0 = new String[6];
        stringArray0[0] = "-$W&@QGY-";
        try {
            grepArguments0.parse(stringArray0);
            fail("Expecting exception: Exception");

        } catch (Exception e) {
            //
            // grep: Invalid flag option supplied
            //
            verifyException("sg.edu.nus.comp.cs4218.impl.app.args.GrepArguments", e);
        }
    }

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test01() throws Throwable {
        GrepArguments grepArguments0 = new GrepArguments();
        String[] stringArray0 = new String[6];
        stringArray0[0] = "(I<h>{Z0&]X.h+:LBY";
        try {
            grepArguments0.parse(stringArray0);
            fail("Expecting exception: NullPointerException");

        } catch (NullPointerException e) {
        }
    }

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test02() throws Throwable {
        GrepArguments grepArguments0 = new GrepArguments();
        String[] stringArray0 = new String[1];
        stringArray0[0] = "cEvM`[l(O";
        try {
            grepArguments0.parse(stringArray0);
            fail("Expecting exception: Exception");

        } catch (Exception e) {
            //
            // Invalid regular expression supplied
            //
            verifyException("sg.edu.nus.comp.cs4218.impl.app.args.GrepArguments", e);
        }
    }

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test03() throws Throwable {
        GrepArguments grepArguments0 = new GrepArguments();
        String[] stringArray0 = new String[2];
        stringArray0[0] = "No regular expression supplied";
        stringArray0[1] = "-i";
        grepArguments0.parse(stringArray0);
        assertFalse(grepArguments0.isCaseInsensitive());

        String[] stringArray1 = new String[3];
        stringArray1[0] = "-i";
        stringArray1[1] = "-i";
        stringArray1[2] = "-i";
        grepArguments0.parse(stringArray1);
        boolean boolean0 = grepArguments0.isCaseInsensitive();
        assertTrue(boolean0);
    }

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test04() throws Throwable {
        GrepArguments grepArguments0 = new GrepArguments();
        String[] stringArray0 = new String[5];
        stringArray0[0] = "";
        stringArray0[1] = "DomS";
        stringArray0[2] = "1K4A^i2hX";
        stringArray0[3] = "";
        stringArray0[4] = "";
        grepArguments0.parse(stringArray0);
        grepArguments0.getPattern();
        assertFalse(grepArguments0.isCountOfLinesOnly());
        assertFalse(grepArguments0.isPrefixFileName());
        assertFalse(grepArguments0.isCaseInsensitive());
    }

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test05() throws Throwable {
        GrepArguments grepArguments0 = new GrepArguments();
        String[] stringArray0 = new String[9];
        stringArray0[0] = "6%y5#";
        stringArray0[1] = "sg.edu.nus.comp.cs4218.impl.app.args.GrepArguments";
        stringArray0[2] = "sg.edu.nus.comp.cs4218.exception.AbstractApplicationException";
        stringArray0[3] = "I";
        stringArray0[4] = "";
        stringArray0[5] = "Null arguments";
        stringArray0[6] = "sg.edu.nus.comp.cs4218.impl.app.args.GrepArguments";
        stringArray0[7] = "49;89c";
        stringArray0[8] = "";
        grepArguments0.parse(stringArray0);
        List<String> list0 = grepArguments0.getFiles();
        assertEquals(6, list0.size());
        assertFalse(grepArguments0.isCountOfLinesOnly());
        assertFalse(grepArguments0.isCaseInsensitive());
        assertFalse(grepArguments0.isPrefixFileName());
    }

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test06() throws Throwable {
        try {
            GrepArguments.validate((String) null);
            fail("Expecting exception: Exception");

        } catch (Exception e) {
            //
            // Null arguments
            //
            verifyException("sg.edu.nus.comp.cs4218.impl.app.args.GrepArguments", e);
        }
    }

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test07() throws Throwable {
        GrepArguments grepArguments0 = new GrepArguments();
        String[] stringArray0 = new String[7];
        stringArray0[0] = "-HJ$e";
        try {
            grepArguments0.parse(stringArray0);
            fail("Expecting exception: Exception");

        } catch (Exception e) {
            //
            // grep: Invalid flag option supplied
            //
            verifyException("sg.edu.nus.comp.cs4218.impl.app.args.GrepArguments", e);
        }
    }

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test08() throws Throwable {
        GrepArguments grepArguments0 = new GrepArguments();
        String[] stringArray0 = new String[9];
        stringArray0[0] = "-";
        try {
            grepArguments0.parse(stringArray0);
            fail("Expecting exception: NullPointerException");

        } catch (NullPointerException e) {
        }
    }

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test09() throws Throwable {
        GrepArguments grepArguments0 = new GrepArguments();
        String[] stringArray0 = new String[4];
        stringArray0[0] = "-Hi";
        stringArray0[1] = "-Hi";
        stringArray0[2] = ":";
        stringArray0[3] = ":";
        grepArguments0.parse(stringArray0);
        boolean boolean0 = grepArguments0.isPrefixFileName();
        assertTrue(grepArguments0.isCaseInsensitive());
        assertTrue(boolean0);
    }

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test10() throws Throwable {
        GrepArguments grepArguments0 = new GrepArguments();
        String[] stringArray0 = new String[0];
        try {
            grepArguments0.parse(stringArray0);
            fail("Expecting exception: Exception");

        } catch (Exception e) {
            //
            // No regular expression supplied
            //
            verifyException("sg.edu.nus.comp.cs4218.impl.app.args.GrepArguments", e);
        }
    }

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test11() throws Throwable {
        GrepArguments grepArguments0 = new GrepArguments();
        try {
            grepArguments0.parse((String[]) null);
            fail("Expecting exception: Exception");

        } catch (Exception e) {
            //
            // Null arguments
            //
            verifyException("sg.edu.nus.comp.cs4218.impl.app.args.GrepArguments", e);
        }
    }

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test12() throws Throwable {
        try {
            GrepArguments.validate("");
            fail("Expecting exception: Exception");

        } catch (Exception e) {
            //
            // Regular expression cannot be empty
            //
            verifyException("sg.edu.nus.comp.cs4218.impl.app.args.GrepArguments", e);
        }
    }

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test13() throws Throwable {
        GrepArguments.validate("-Hi");
    }

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test14() throws Throwable {
        GrepArguments grepArguments0 = new GrepArguments();
        grepArguments0.getFiles();
        assertFalse(grepArguments0.isCountOfLinesOnly());
        assertFalse(grepArguments0.isCaseInsensitive());
        assertFalse(grepArguments0.isPrefixFileName());
    }

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test15() throws Throwable {
        GrepArguments grepArguments0 = new GrepArguments();
        boolean boolean0 = grepArguments0.isCountOfLinesOnly();
        assertFalse(grepArguments0.isPrefixFileName());
        assertFalse(boolean0);
        assertFalse(grepArguments0.isCaseInsensitive());
    }

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test16() throws Throwable {
        GrepArguments grepArguments0 = new GrepArguments();
        boolean boolean0 = grepArguments0.isPrefixFileName();
        assertFalse(boolean0);
        assertFalse(grepArguments0.isCaseInsensitive());
        assertFalse(grepArguments0.isCountOfLinesOnly());
    }

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test17() throws Throwable {
        GrepArguments grepArguments0 = new GrepArguments();
        grepArguments0.getPattern();
        assertFalse(grepArguments0.isCountOfLinesOnly());
        assertFalse(grepArguments0.isPrefixFileName());
        assertFalse(grepArguments0.isCaseInsensitive());
    }

    @Test
    @Timeout(value = 4000, unit = TimeUnit.MILLISECONDS)
    public void test18() throws Throwable {
        GrepArguments grepArguments0 = new GrepArguments();
        boolean boolean0 = grepArguments0.isCaseInsensitive();
        assertFalse(grepArguments0.isPrefixFileName());
        assertFalse(grepArguments0.isCountOfLinesOnly());
        assertFalse(boolean0);
    }
}