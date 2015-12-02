package com.ampaiva.metricsdatamanager.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ampaiva.hlo.util.view.IProgressUpdate;
import com.ampaiva.hlo.util.view.ProgressUpdate;

public class SequenceMatch {
    private static final Log LOG = LogFactory.getLog(SequenceMatch.class);

    private final List<List<Integer>> sequences;
    private final int minSequence;

    public SequenceMatch(List<List<Integer>> sequences, int minSequence) {
        this.sequences = sequences;
        this.minSequence = minSequence;
        if (LOG.isDebugEnabled()) {
            LOG.debug(this);
        }
    }

    public List<MatchesData> getMatches(Map<Integer, List<List<Integer>>> map) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMatches()");
        }

        Map<Integer, MatchesData> mapMerge = new HashMap<Integer, MatchesData>();
        List<MatchesData> result = new ArrayList<MatchesData>();
        IProgressUpdate update = ProgressUpdate.start("Processing map", map.entrySet().size());
        for (Entry<Integer, List<List<Integer>>> entry : map.entrySet()) {
            update.beginIndex(entry);
            if (LOG.isDebugEnabled()) {
                LOG.debug("getMatches() entry: " + entry);
            }
            List<List<Integer>> listMatches = entry.getValue();
            for (int i = 1; i < listMatches.size(); i++) {
                List<Integer> copy = listMatches.get(i - 1);
                List<List<Integer>> pastes = listMatches.subList(i, listMatches.size());
                MatchesData matchesData = getMatches(copy, pastes);
                if (matchesData != null) {
                    MatchesData existingMatchesData = mapMerge.get(matchesData.groupIndex);
                    if (existingMatchesData == null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("getMatches() entry: " + entry + " put " + matchesData.groupIndex + ", "
                                    + matchesData);
                        }
                        existingMatchesData = new MatchesData(matchesData.groupIndex);
                        mapMerge.put(existingMatchesData.groupIndex, existingMatchesData);
                        result.add(existingMatchesData);
                    }
                    mergeMatchedData(existingMatchesData, matchesData);
                }
            }
            update.endIndex(entry);
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("getMatches() return " + result);
        }
        return result;
    }

    //    private void printMemory() {
    //        if (LOG.isDebugEnabled()) {
    //            Runtime runtime = Runtime.getRuntime();
    //            StringBuilder sb = new StringBuilder();
    //            long maxMemory = runtime.maxMemory();
    //            long allocatedMemory = runtime.totalMemory();
    //            long freeMemory = runtime.freeMemory();
    //
    //            NumberFormat format = NumberFormat.getInstance();
    //            sb.append("free memory: " + format.format(freeMemory / 1024) + " ");
    //            sb.append("allocated memory: " + format.format(allocatedMemory / 1024) + " ");
    //            sb.append("max memory: " + format.format(maxMemory / 1024) + " ");
    //            sb.append("total free memory: " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024) + " ");
    //            LOG.debug(sb.toString());
    //            System.out.println(sb.toString());
    //        }
    //    }

    private int getInsertPosition(List<Integer> existingElements, int newElement) {
        for (int i = 0; i < existingElements.size(); i++) {
            if (existingElements.get(i) >= newElement) {
                return i;
            }
        }
        return existingElements.size();
    }

    private void mergeMatchedData(MatchesData existingMatchesData, MatchesData newMatchesData) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("mergeMatchedData(" + existingMatchesData + ", " + newMatchesData + ")");
        }
        for (int k = 0; k < newMatchesData.groupsMatched.size(); k++) {
            int newMatchedGroup = newMatchesData.groupsMatched.get(k);
            int position = getInsertPosition(existingMatchesData.groupsMatched, newMatchedGroup);
            List<List<Integer>> newSequenceMatched = newMatchesData.sequencesMatches.get(k);
            if (existingMatchesData.groupsMatched.size() > position
                    && existingMatchesData.groupsMatched.get(position) == newMatchedGroup) {
                existingMatchesData.sequencesMatches.get(position).addAll(newSequenceMatched);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("mergeMatchedData: existing " + existingMatchesData);
                }
            } else {
                existingMatchesData.groupsMatched.add(position, newMatchedGroup);
                existingMatchesData.sequencesMatches.add(position, newSequenceMatched);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("mergeMatchedData: new " + existingMatchesData);
                }
            }
        }
    }

    private MatchesData getMatches(List<Integer> copy, List<List<Integer>> pastes) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMatches(" + copy + ", " + pastes + ")");
        }
        int groupIndex = copy.get(0);
        List<Integer> groupsMatched = new ArrayList<Integer>();
        List<List<List<Integer>>> sequencesMatches = new ArrayList<List<List<Integer>>>();
        for (List<Integer> paste : pastes) {
            if (groupIndex == paste.get(0)) {
                // Same group
                continue;
            }
            if (hasMatchesAbove(copy, paste)) {
                continue;
            }
            List<List<Integer>> sequenceMatches = getSequenceMatches(copy, paste);
            if (sequenceMatches.size() == minSequence) {
                groupsMatched.add(paste.get(0));
                sequencesMatches.add(sequenceMatches);
            }
        }
        MatchesData matchesData = null;
        if (groupsMatched.size() > 0) {
            matchesData = new MatchesData(groupIndex, groupsMatched, sequencesMatches);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("getMatches(" + copy + ", " + pastes + ") return " + matchesData);
        }
        return matchesData;
    }

    private boolean hasMatchesAbove(List<Integer> copy, List<Integer> paste) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("hasMatchesAbove(" + copy + ", " + paste + ")");
        }
        List<Integer> sequenceCopy = sequences.get(copy.get(0));
        List<Integer> sequencePaste = sequences.get(paste.get(0));
        boolean result = hasMatchesAbove(copy.get(1), paste.get(1), sequenceCopy, sequencePaste);
        if (LOG.isDebugEnabled()) {
            LOG.debug("hasMatchesAbove(" + copy + ", " + paste + ") return " + result);
        }
        return result;
    }

    public static boolean hasMatchesAbove(Integer copy, Integer paste, List<Integer> sequenceCopy,
            List<Integer> sequencePaste) {
        if ((copy > 0) && (sequenceCopy.get(copy - 1).intValue() == sequenceCopy.get(copy).intValue())) {
            return true;
        }
        if ((paste > 0) && (sequencePaste.get(paste - 1).intValue() == sequencePaste.get(paste).intValue())) {
            return true;
        }
        if (copy == 0 || paste == 0) {
            return false;
        }
        return sequenceCopy.get(copy - 1).intValue() == sequencePaste.get(paste - 1).intValue();
    }

    private List<List<Integer>> getSequenceMatches(List<Integer> copy, List<Integer> paste) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSequenceMatches(" + copy + ", " + paste + ")");
        }
        List<Integer> sequenceCopy = sequences.get(copy.get(0));
        List<Integer> sequencePaste = sequences.get(paste.get(0));
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSequenceMatches: " + sequenceCopy + " x " + sequencePaste);
        }

        List<List<Integer>> sequenceMatches = new ArrayList<List<Integer>>();
        sequenceMatches.add(Arrays.asList(copy.get(1), paste.get(1)));
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSequenceMatches: " + sequenceMatches);
        }
        int indexCopy = copy.get(1) + 1;
        int indexPaste = paste.get(1) + 1;
        for (int i = indexCopy; i < sequenceCopy.size(); i++) {
            boolean found = false;
            for (int j = indexPaste; j < sequencePaste.size(); j++) {
                if (sequenceCopy.get(i).intValue() == sequencePaste.get(j).intValue()) {
                    sequenceMatches.add(Arrays.asList(i, j));
                    indexPaste = j + 1;
                    found = true;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getSequenceMatches: add" + sequenceMatches);
                    }
                    break;
                } else {
                    break;
                }
            }
            if (!found) {
                break;
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSequenceMatches(" + copy + ", " + paste + ") return " + sequenceMatches);
        }
        return sequenceMatches;
    }

    @Override
    public String toString() {
        return "SequenceMatch [sequences=" + sequences + ", minSequence=" + minSequence + "]";
    }
}
