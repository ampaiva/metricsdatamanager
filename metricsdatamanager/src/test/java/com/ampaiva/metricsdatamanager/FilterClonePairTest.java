package com.ampaiva.metricsdatamanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FilterClonePairTest {

    @Test
    public void testGetClonePairs() {
        ClonePair clone1 = new ClonePair(new CloneSide("B", 1, 2, ""), new CloneSide("A", 1, 2, ""), true);
        ClonePair clone2 = new ClonePair(new CloneSide("A", 1, 2, ""), new CloneSide("B", 1, 2, ""), true);
        ClonePair clone3 = new ClonePair(new CloneSide("C", 1, 2, ""), new CloneSide("D", 1, 2, ""), false);
        ClonePair clone4 = new ClonePair(new CloneSide("C", 3, 4, ""), new CloneSide("D", 3, 4, ""), true);
        List<ClonePair> clones = Arrays.asList(clone1, clone2, clone3, clone4);
        List<ClonePair> result = FilterClonePair.getClonePairs(clones);
        assertNotNull(result);
        assertEquals(2, result.size());
        ClonePair result1 = result.get(1);
        assertEquals("C", result1.copy.name);
        assertTrue(result1.found);
    }

}
