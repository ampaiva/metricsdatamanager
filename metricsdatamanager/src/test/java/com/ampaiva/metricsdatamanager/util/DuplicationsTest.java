package com.ampaiva.metricsdatamanager.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.ampaiva.metricsdatamanager.util.Duplications.DuplicationInfo;

public class DuplicationsTest {

    @Test
    public void testNext1() {
        List<List<Integer>> sequences = Arrays.asList(//
                /* [0] */Arrays.asList(1, 6), //
                /* [1] */Arrays.asList(2, 7)); //
        // [[1, 6], [2, 7]
        DuplicationInfo expected = new DuplicationInfo(2, 1, 6);
        Duplications duplications = new Duplications(sequences);
        final DuplicationInfo next = duplications.next();
        assertNotNull(next);
        assertEquals(expected, next);
        assertNull(duplications.next());
    }

    @Test
    public void testNext2() {
        List<List<Integer>> sequences = Arrays.asList(//
                /* [0] */Arrays.asList(1, 6), //
                /* [1] */Arrays.asList(2, 8)); //
        // [[1, 6], [2, 8]
        Duplications duplications = new Duplications(sequences);
        final DuplicationInfo next = duplications.next();
        assertNotNull(next);
        assertEquals(new DuplicationInfo(1, 1, 6), next);
        final DuplicationInfo next2 = duplications.next();
        assertNotNull(next2);
        assertEquals(new DuplicationInfo(1, 2, 8), next2);
        assertNull(duplications.next());
    }

}
