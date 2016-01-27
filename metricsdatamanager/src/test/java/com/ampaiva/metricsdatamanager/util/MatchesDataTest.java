package com.ampaiva.metricsdatamanager.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class MatchesDataTest {

    @Test
    public void testmergeIdentical() {
        //MatchesData [groupIndex=1, groupsMatched=[2], sequencesMatches=[[[0, 3]]]
        //MatchesData [groupIndex=1, groupsMatched=[2], sequencesMatches=[[[0, 3]]]
        List<MatchesData> matchesDatas = Arrays.asList(//
                new MatchesData(1, //
                        Arrays.asList(2), //
                        Arrays.asList( //
                                /* 2 */Arrays.asList(Arrays.asList(0, 3)))), //
                new MatchesData(1, //
                        Arrays.asList(2), //
                        Arrays.asList( //
                                /* 2 */Arrays.asList(Arrays.asList(0, 3)))));
        // [methodIndexes[1,2,4], sequenceMatches=[[0, 1, 3], [3, 5, 7], [7, 8, 9]]
        // [methodIndexes[1,2,4,5], sequenceMatches=[[0, 3, 7, 10]]
        // [methodIndexes[1,5], sequenceMatches=[[0, 10], [11, 12]]
        List<CloneInfo> expected = Arrays.asList(//
                new CloneInfo(Arrays.asList(1, 2), //
                        Arrays.asList( //
                                /* 1 */Arrays.asList(0), Arrays.asList(3))//
        ));
        List<CloneInfo> result = MatchesData.merge(matchesDatas);
        assertNotNull(result);
        assertEquals(expected, result);
    }

    @Test
    public void testmerge2() {
        //MatchesData [groupIndex=1, groupsMatched=[2, 4, 5], sequencesMatches=[[[0, 3], [1, 5], [3, 7]], 
        //                                                                      [[0, 7], [1, 8], [3, 9]], 
        //                                                                      [[0, 10], [11, 12]]]
        //MatchesData [groupIndex=2, groupsMatched=[4], sequencesMatches=[[[3, 7], [5, 8], [7, 9]]]
        List<MatchesData> matchesDatas = Arrays.asList(//
                new MatchesData(1, //
                        Arrays.asList(2, 4, 5), //
                        Arrays.asList( //
                                /* 2 */Arrays.asList(Arrays.asList(0, 3), Arrays.asList(1, 5), Arrays.asList(3, 7)), //
                                /* 4 */Arrays.asList(Arrays.asList(0, 7), Arrays.asList(1, 8), Arrays.asList(3, 9)),
                                /* 5 */Arrays.asList(Arrays.asList(0, 10), Arrays.asList(11, 12)))), //
                new MatchesData(2, //
                        Arrays.asList(4), //
                        Arrays.asList( //
                                /* 4 */Arrays.asList(Arrays.asList(3, 7), Arrays.asList(5, 8), Arrays.asList(7, 9)))//
        ));
        // [methodIndexes[1,2,4], sequenceMatches=[[0, 1, 3], [3, 5, 7], [7, 8, 9]]
        // [methodIndexes[1,2,4,5], sequenceMatches=[[0, 3, 7, 10]]
        // [methodIndexes[1,5], sequenceMatches=[[0, 10], [11, 12]]
        List<CloneInfo> expected = Arrays.asList(//
                new CloneInfo(Arrays.asList(1, 2, 4), //
                        Arrays.asList( //
                                /* 1 */Arrays.asList(0, 1, 3), Arrays.asList(3, 5, 7), Arrays.asList(7, 8, 9))), //
                //                new CloneInfo(Arrays.asList(1, 2, 4, 5), //
                //                        Arrays.asList( //
                //                                /* 1 */Arrays.asList(0), Arrays.asList(3), Arrays.asList(7), Arrays.asList(10))), //
                new CloneInfo(Arrays.asList(1, 5), //
                        Arrays.asList( //
                                /* 1 */Arrays.asList(0, 11), Arrays.asList(10, 12))//
        ));
        List<CloneInfo> result = MatchesData.merge(matchesDatas);
        assertNotNull(result);
        assertEquals(expected, result);
    }
}
