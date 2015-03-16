package com.ampaiva.metricsdatamanager.util;

import org.junit.Assert;
import org.junit.Test;

public class LCSTest {

    @Test
    public void testLcsEmpty() {
        Assert.assertArrayEquals(new int[] {}, LCS.lcs(new int[] {}, new int[] {}));
        Assert.assertArrayEquals(new int[] {}, LCS.lcs(new int[] { 1 }, new int[] {}));
        Assert.assertArrayEquals(new int[] {}, LCS.lcs(new int[] {}, new int[] { 1 }));
    }

    @Test
    public void testLcsSimple() {
        Assert.assertArrayEquals(new int[] {}, LCS.lcs(new int[] { 1 }, new int[] { 2 }));
    }

    @Test
    public void testLcsEquals() {
        Assert.assertArrayEquals(new int[] { 7 }, LCS.lcs(new int[] { 7 }, new int[] { 7 }));
    }

    @Test
    public void testLcsEqualsAfter1() {
        Assert.assertArrayEquals(new int[] { 7 }, LCS.lcs(new int[] { 7, 8 }, new int[] { 7 }));
    }

    @Test
    public void testLcsEqualsBefore1() {
        Assert.assertArrayEquals(new int[] { 7 }, LCS.lcs(new int[] { 8, 7 }, new int[] { 7 }));
    }

    @Test
    public void testLcsEqualsAfter2() {
        Assert.assertArrayEquals(new int[] { 7 }, LCS.lcs(new int[] { 7 }, new int[] { 7, 8 }));
    }

    @Test
    public void testLcsEqualsBefore2() {
        Assert.assertArrayEquals(new int[] { 7 }, LCS.lcs(new int[] { 7 }, new int[] { 8, 7 }));
    }

    @Test
    public void testLcsEqualsScenario1() {
        Assert.assertArrayEquals(new int[] { 2, 3, 4, 5, 7, 8, 9 },
                LCS.lcs(new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, new int[] { 2, 3, 3, 4, 8, 5, 7, 8, 3, 9 }));
    }

}
