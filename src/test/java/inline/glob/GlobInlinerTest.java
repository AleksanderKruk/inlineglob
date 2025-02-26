package inline.glob;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * Tests to make sure that the generated code
 * matches the expected behaviour
 */
public class GlobInlinerTest
{

    @Test
    public void simplePattern()
    {
        var predicate = GlobInliner.compile("abcd");
        assertTrue(predicate.test("abcd"));
        assertFalse(predicate.test("bcd"));
        assertFalse(predicate.test("abcde"));
        assertFalse(predicate.test("abd"));
    }

    @Test
    public void anyChar()
    {
        var predicate = GlobInliner.compile("?");
        assertTrue(predicate.test("a"));
        assertTrue(predicate.test("*"));
        assertTrue(predicate.test("?"));
        assertFalse(predicate.test(""));
    }

    // @Test
    // public void simplePattern()
    // {
    //     var predicate = GlobInliner.compile("abcd");
    //     assertTrue(predicate.test("abcd"));
    //     assertFalse(predicate.test("bcd"));
    //     assertFalse(predicate.test("abcde"));
    //     assertFalse(predicate.test("abd"));
    // }

    // @Test
    // public void simplePattern()
    // {
    //     var predicate = GlobInliner.compile("abcd");
    //     assertTrue(predicate.test("abcd"));
    //     assertFalse(predicate.test("bcd"));
    //     assertFalse(predicate.test("abcde"));
    //     assertFalse(predicate.test("abd"));
    // }
}
