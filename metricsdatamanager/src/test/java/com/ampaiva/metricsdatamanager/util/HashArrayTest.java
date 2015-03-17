package com.ampaiva.metricsdatamanager.util;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HashArrayTest {

    private static final String A = "A";
    private static final String B = "B";

    @Test
    public void testHashArray() {
        HashArray hashArray = new HashArray();
        assertEquals(0, hashArray.put(A));
        assertEquals(0, hashArray.getByKey(A));
        assertEquals(A, hashArray.getByIndex(0));

        assertEquals(1, hashArray.put(B));
        assertEquals(1, hashArray.getByKey(B));
        assertEquals(B, hashArray.getByIndex(1));

        assertEquals(0, hashArray.put(A));
        assertEquals(0, hashArray.getByKey(A));
        assertEquals(A, hashArray.getByIndex(0));
    }

}
