package com.ampaiva.metricsdatamanager.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.easymock.EasyMockSupport;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SequenceMatchTest extends EasyMockSupport {
    private SequenceMatch sequenceMatch;

    /**
     * Setup mocks before each test.
     * 
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    /**
     * Verifies all mocks after each test.
     */
    @After()
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testGetMatchesCase0() {
        List<List<Integer>> sequences = Arrays.asList(Arrays.asList(new Integer[0]));
        sequenceMatch = new SequenceMatch(sequences, 1);
        List<MatchesData> result = sequenceMatch.getMatches();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetMatchesCase1() {
        List<List<Integer>> sequences = Arrays.asList(Arrays.asList(1));
        sequenceMatch = new SequenceMatch(sequences, 1);
        List<MatchesData> result = sequenceMatch.getMatches();
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testGetMatchesCase2() {
        List<List<Integer>> sequences = Arrays.asList(Arrays.asList(new Integer(13)), Arrays.asList(new Integer(13)));
        sequenceMatch = new SequenceMatch(sequences, 1);
        List<MatchesData> result = sequenceMatch.getMatches();
        assertNotNull(result);
        assertEquals(1, result.size());
        MatchesData matchesData = result.get(0);
        assertEquals(0, matchesData.groupIndex);
        assertEquals(Arrays.asList(1), matchesData.groupsMatched);
        assertNotNull(matchesData.sequencesMatches);
        assertEquals(1, matchesData.sequencesMatches.size());
        assertEquals(Arrays.asList(Arrays.asList(0, 0)), matchesData.sequencesMatches.get(0));
    }

    @Test
    public void testGetMatchesCase2_1() {
        List<List<Integer>> sequences = Arrays.asList(//
                /* [0] */Arrays.asList(10), //
                /* [1] */Arrays.asList(10, 10)); //
        // [0] [1] [[[0, 0]]]
        List<MatchesData> expected = Arrays.asList(//
                new MatchesData(0, //
                        Arrays.asList(1), //
                        Arrays.asList( //
                        /* 1 */Arrays.asList( //
                                Arrays.asList(0, 0)))));
        sequenceMatch = new SequenceMatch(sequences, 1);
        List<MatchesData> result = sequenceMatch.getMatches();
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void testGetMatchesCase2_2() {
        List<List<Integer>> sequences = Arrays.asList(//
                /* [0] */Arrays.asList(10, 10), //
                /* [1] */Arrays.asList(10)); //
        // [0] [1] [[[0, 0]]]
        List<MatchesData> expected = Arrays.asList(//
                new MatchesData(0, //
                        Arrays.asList(1), //
                        Arrays.asList( //
                        /* 1 */Arrays.asList( //
                                Arrays.asList(0, 0)))));
        sequenceMatch = new SequenceMatch(sequences, 1);
        List<MatchesData> result = sequenceMatch.getMatches();
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void testGetMatchesCase3() {
        List<List<Integer>> sequences = Arrays.asList(//
                /* [0] */Arrays.asList(10, 20, 30, 40), //
                /* [1] */Arrays.asList(40, 20, 30, 10)); //
        // [0] [1] [[[1, 1], [2, 2]]]
        List<MatchesData> expected = Arrays.asList(//
                new MatchesData(0, //
                        Arrays.asList(1), //
                        Arrays.asList( //
                        /* 1 */Arrays.asList( //
                                Arrays.asList(1, 1), //
                                Arrays.asList(2, 2)))));
        sequenceMatch = new SequenceMatch(sequences, 2);
        List<MatchesData> result = sequenceMatch.getMatches();
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void testGetMatchesCase4() {
        List<List<Integer>> sequences = Arrays.asList(//
                /* [0] */Arrays.asList(10, 2, 3), //
                /* [1] */Arrays.asList(2, 11, 3));
        // [0] [1] [[1, 0],[2, 2]]
        List<MatchesData> expected = Arrays.asList();
        sequenceMatch = new SequenceMatch(sequences, 2);
        List<MatchesData> result = sequenceMatch.getMatches();
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void testGetMatchesCase5() {
        List<List<Integer>> sequences = Arrays.asList(//
                /******************** ..0 ..1 ..2 ..3 ..4 ..5 ..6 ..7 */
                /* [0] */Arrays.asList(10, 20, 30, 10, 10, 40, 50), //
                /* [1] */Arrays.asList(20, 11, 30, 11, 11, 40, 11, 50));
        // [0] [1] [[1, 0],[2, 2],[5,5],[6,7]]
        List<MatchesData> expected = Arrays.asList();
        sequenceMatch = new SequenceMatch(sequences, 2);
        List<MatchesData> result = sequenceMatch.getMatches();
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void testGetMatchesCase6() {
        List<List<Integer>> sequences = Arrays.asList(//
                /******************** ..0 ..1 ..2 ..3 ..4 ..5 ..6 ..7 */
                /* [0] */Arrays.asList(10, 20, 30, 40), //
                /* [1] */Arrays.asList(10, 20, 80, 40, 10, 20, 80, 40));
        // [0] [1] [[[0, 0], [1, 1], [3, 3]]]
        List<MatchesData> expected = Arrays.asList(//
                new MatchesData(0, //
                        Arrays.asList(1), //
                        Arrays.asList( //
                        /* 1 */Arrays.asList( //
                                Arrays.asList(0, 0), //
                                Arrays.asList(1, 1), //
                                Arrays.asList(0, 4), //
                                Arrays.asList(1, 5)))));
        sequenceMatch = new SequenceMatch(sequences, 2);
        List<MatchesData> result = sequenceMatch.getMatches();
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void testGetMatchesCase7() {
        List<List<Integer>> sequences = Arrays.asList(//
                /******************** ..0 ..1 ..2 ..3 ..4 ..5 ..6 ..7 */
                /* [0] */Arrays.asList(10, 20, 30, 40, 50, 60, 70), //
                /* [1] */Arrays.asList(10, 20, 80, 40, 12, 11, 60, 70));
        // [0] [1] [[[0, 0], [1, 1], [3, 3], [5, 6], [6, 7]]]
        List<MatchesData> expected = Arrays.asList(//
                new MatchesData(0, //
                        Arrays.asList(1), //
                        Arrays.asList( //
                        /* 1 */Arrays.asList( //
                                Arrays.asList(0, 0), //
                                Arrays.asList(1, 1), //
                                Arrays.asList(5, 6), //
                                Arrays.asList(6, 7)))));
        sequenceMatch = new SequenceMatch(sequences, 2);
        List<MatchesData> result = sequenceMatch.getMatches();
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void testGetMatchesCase8() {
        List<List<Integer>> sequences = Arrays.asList(//
                /******************** ..0 ..1 ..2 ..3 ..4 ..5 ..6 */
                /* [0] */Arrays.asList(10, 20, 30, 40, 50, 60, 70), //
                /* [1] */Arrays.asList(10, 20, 30, 40, 50, 60, 70));
        // [0] [1] [[[0, 0], [1, 1], [2, 2], [3, 3], [4, 4], [5, 5], [6, 6]]]
        List<MatchesData> expected = Arrays.asList(//
                new MatchesData(0, //
                        Arrays.asList(1), //
                        Arrays.asList( //
                        /* 1 */Arrays.asList( //
                                Arrays.asList(0, 0), //
                                Arrays.asList(1, 1), //
                                Arrays.asList(2, 2), //
                                Arrays.asList(3, 3), //
                                Arrays.asList(4, 4), //
                                Arrays.asList(5, 5), //
                                Arrays.asList(6, 6)))));
        sequenceMatch = new SequenceMatch(sequences, 7);
        List<MatchesData> result = sequenceMatch.getMatches();
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void testGetMatchesCase9() {
        List<List<Integer>> sequences = new ArrayList<List<Integer>>();
        List<Integer> method1 = new ArrayList<Integer>();
        int maxSeq = 2;
        for (int i = 0; i < maxSeq; i++) {
            method1.add(new Integer(i * 13 + 1));
        }
        List<Integer> method2 = new ArrayList<Integer>();
        for (int i = 0; i < maxSeq; i++) {
            method2.add(new Integer(i * 13 + 1));
        }
        sequences.add(method1);
        sequences.add(method2);
        sequenceMatch = new SequenceMatch(sequences, maxSeq);
        List<MatchesData> result = sequenceMatch.getMatches();
        assertNotNull(result);
        assertEquals(1, result.size());
        MatchesData matchesData = result.get(0);
        assertEquals(0, matchesData.groupIndex);
        assertEquals(Arrays.asList(1), matchesData.groupsMatched);
        assertNotNull(matchesData.sequencesMatches);
        assertEquals(1, matchesData.sequencesMatches.size());
    }

    @Test
    public void testGetMatchesCaseN() {
        List<List<Integer>> sequences = Arrays.asList(//
                /******************** ..0 ..1 ..2 ..3 ..4 ..5 ..6 ..7 */
                /* [0] */Arrays.asList(10, 20, 30, 40, 50, 60, 70, 61, 71), //
                /* [1] */Arrays.asList(10, 20, 80, 40, 12, 11, 60, 70), //
                /* [2] */Arrays.asList(12, 11), //
                /* [3] */Arrays.asList(20, 30, 90, 40, 13, 13, 61, 71));
        // [0] [1, 3] [[[0, 0], [1, 1], [3, 3], [5, 6], [6, 7]][[1, 0], [2, 1], [3, 3]]]
        // [1] [2] [[4, 0], [5, 1]]
        List<MatchesData> expected = Arrays.asList(//
                new MatchesData(0, //
                        Arrays.asList(1, 3), //
                        Arrays.asList( //
                                /* 1 */Arrays.asList( //
                                        Arrays.asList(0, 0), //
                                        Arrays.asList(1, 1), //
                                        Arrays.asList(5, 6), //
                                        Arrays.asList(6, 7)), //
                                /* 3 */Arrays.asList( //
                                        Arrays.asList(1, 0), //
                                        Arrays.asList(2, 1), //
                                        Arrays.asList(7, 6), //
                                        Arrays.asList(8, 7)))), //
                new MatchesData(1, //
                        Arrays.asList(2), //
                        Arrays.asList( //
                        /* 2 */Arrays.asList( //
                                Arrays.asList(4, 0), //
                                Arrays.asList(5, 1)))));
        sequenceMatch = new SequenceMatch(sequences, 2);
        List<MatchesData> result = sequenceMatch.getMatches();
        assertNotNull(result);
        assertEquals(expected, result);
    }
}
