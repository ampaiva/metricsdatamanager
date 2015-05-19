package com.ampaiva.metricsdatamanager.config;

/**
 * @author ampaiva
 *
 */
public interface IConcernCallsConfig {
    /**
     * Gets the minimum number of equals sequences in order to
     * consider those sequences as a clone.
     * 
     * Example: if getMinSeq() returns 2, methodA and methodB contains
     * a clone of calls: a() and c().
     * 
     * void methodA(){b(); a(); c();} void methodB(){ a(); c(); d();}
     */
    int getMinSeq();
}
