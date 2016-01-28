package com.ampaiva.metricsdatamanager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.ampaiva.metricsdatamanager.model.Analyse;
import com.ampaiva.metricsdatamanager.model.Call;
import com.ampaiva.metricsdatamanager.model.Clone;
import com.ampaiva.metricsdatamanager.model.Method;
import com.ampaiva.metricsdatamanager.tools.pmd.PmdClone;
import com.ampaiva.metricsdatamanager.tools.pmd.PmdOccurrence;
import com.ampaiva.metricsdatamanager.util.Conventions;

public class CloneGroup implements Comparable<CloneGroup> {
    public static final String ID_SEPARATOR = "-";
    public final CloneSnippet[] snippets;
    public final boolean found;

    public CloneGroup(CloneSnippet[] sides, boolean found) {
        this.snippets = sort(sides);
        this.found = found;
    }

    private CloneSnippet[] sort(CloneSnippet[] snippets) {
        Set<CloneSnippet> set = Collections.synchronizedSortedSet(new TreeSet<>());
        for (CloneSnippet cloneSnippet : snippets) {
            set.add(cloneSnippet);
        }
        return set.toArray(new CloneSnippet[set.size()]);
    }

    public static <T> List<CloneGroup> getCloneGroups(T clone, boolean found) {
        List<CloneGroup> cloneGroups = new ArrayList<>();
        CloneSnippet[] snippets = convert(clone);
        cloneGroups.add(new CloneGroup(snippets, found));

        return cloneGroups;
    }

    private static <T> CloneSnippet[] convert(T clone) {
        return clone instanceof Analyse ? convert((Analyse) clone) : convert((PmdClone) clone);

    }

    private static CloneSnippet[] convert(PmdClone clone) {
        List<CloneSnippet> clones = new ArrayList<>();
        for (int i = 0; i < clone.ocurrencies.size(); i++) {
            PmdOccurrence ocurrency_i = clone.ocurrencies.get(i);
            clones.add(new CloneSnippet(ocurrency_i.file, String.valueOf(ocurrency_i.hashCode()), clone.tokens,
                    ocurrency_i.line, ocurrency_i.line + clone.lines, ocurrency_i.source));
        }
        return clones.toArray(new CloneSnippet[clones.size()]);
    }

    private static CloneSnippet[] convert(Analyse cloneGroup) {
        List<CloneSnippet> cloneSnippets = new ArrayList<>();
        for (Clone snippet : cloneGroup.getClones()) {
            CloneSnippet cloneSnippet = getCloneSnippet(snippet);
            cloneSnippets.add(cloneSnippet);
        }
        return cloneSnippets.toArray(new CloneSnippet[cloneSnippets.size()]);

    }

    public static CloneSnippet getCloneSnippet(Clone snippet) {
        Call call = snippet.getBegin();
        Method method = call.getMethodBean();
        String unit = Conventions.fileNameInRepository(method.getUnitBean().getRepositoryBean().getLocation(),
                method.getUnitBean().getName());
        int beglin = call.getBeglin();
        int endlin = method.getCalls().get(call.getPosition() + snippet.getSize() - 1).getEndlin();

        CloneSnippet cloneSnippet = new CloneSnippet(unit, String.valueOf(method.hashCode()), snippet.getSize(), beglin,
                endlin, method.getSource());
        return cloneSnippet;
    }

    public static CloneSnippet getCloneSnippet(String location, PmdOccurrence snippet) {
        String unit = Conventions.fileNameInRepository(location, snippet.file);
        int beglin = snippet.line;
        int endlin = snippet.line + snippet.pmdClone.lines;
        String source = snippet.source;

        CloneSnippet cloneSnippet = new CloneSnippet(unit, unit, snippet.tokens, beglin, endlin, source);
        return cloneSnippet;
    }

    @Override
    public int compareTo(CloneGroup other) {
        for (int i = 0; i < Math.min(snippets.length, other.snippets.length); i++) {
            int compare = snippets[i].compareTo(other.snippets[i]);
            if (compare != 0) {
                return compare;
            }
        }
        int compare = Integer.compare(snippets.length, other.snippets.length);
        if (compare != 0) {
            return compare;
        }
        // false (not found) > true (found)
        // however, in Java
        // false < true
        // so, we invert the comparison
        return new Boolean(other.found).compareTo(new Boolean(found));
    }

    public String getKey() {
        StringBuilder sb = new StringBuilder();
        for (CloneSnippet cloneSnippet : snippets) {
            if (sb.length() > 0) {
                sb.append("/");
            }
            sb.append(cloneSnippet.name);
        }
        return sb.toString();
    }

    public String toId() {
        StringBuilder sb = new StringBuilder();
        sb.append(found ? "+" : "-");
        sb.append("[" + filesCount() + "]");
        sb.append("[" + sizeCount() + "]");
        for (CloneSnippet cloneSnippet : snippets) {
            if (sb.length() > 1) {
                sb.append(ID_SEPARATOR);
            }
            sb.append(cloneSnippet.toId());
        }
        return sb.toString();
    }

    private int filesCount() {
        CloneSnippet cloneBase = snippets[0];
        int total = 1;
        for (int i = 1; i < snippets.length; i++) {
            if (!cloneBase.key.equals(snippets[i].key)) {
                total++;
                cloneBase = snippets[i];
            }
        }
        return total;
    }

    private int sizeCount() {
        CloneSnippet cloneBase = snippets[0];
        int total = cloneBase.size;
        for (int i = 1; i < snippets.length; i++) {
            if (!cloneBase.key.equals(snippets[i].key)) {
                break;
            }

            total += snippets[i].size;
            cloneBase = snippets[i];
        }
        return total;
    }

    @Override
    public String toString() {
        return "CloneGroup [snippets=" + Arrays.toString(snippets) + ", found=" + found + "]";
    }
}
