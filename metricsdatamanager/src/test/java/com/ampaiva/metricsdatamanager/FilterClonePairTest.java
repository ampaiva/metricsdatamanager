package com.ampaiva.metricsdatamanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FilterClonePairTest {

    @Test
    public void testGetCloneGroups() {
        CloneGroup clone1 = new CloneGroup(
                new CloneSnippet[] { new CloneSnippet("B", 1, 2, ""), new CloneSnippet("A", 1, 2, "") }, true);
        CloneGroup clone2 = new CloneGroup(
                new CloneSnippet[] { new CloneSnippet("A", 1, 2, ""), new CloneSnippet("B", 1, 2, "") }, true);
        CloneGroup clone3 = new CloneGroup(
                new CloneSnippet[] { new CloneSnippet("C", 1, 2, ""), new CloneSnippet("D", 1, 2, "") }, false);
        CloneGroup clone4 = new CloneGroup(
                new CloneSnippet[] { new CloneSnippet("C", 3, 4, ""), new CloneSnippet("D", 3, 4, "") }, true);
        List<CloneGroup> clones = Arrays.asList(clone1, clone2, clone3, clone4);
        List<CloneGroup> result = FilterClonePair.getClonePairs(clones);
        assertNotNull(result);
        assertEquals(3, result.size());
        CloneGroup result1 = result.get(1);
        assertEquals("C", result1.snippets[0].name);
        assertFalse(result1.found);
        CloneGroup result2 = result.get(2);
        assertEquals("C", result1.snippets[0].name);
        assertTrue(result2.found);
    }

}
