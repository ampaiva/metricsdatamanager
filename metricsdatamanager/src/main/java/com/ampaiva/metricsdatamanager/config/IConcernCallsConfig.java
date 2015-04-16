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
     * void methodA(){ a(); b(); c();} void methodB(){ a(); c(); d();}
     */
    int getMinSeq();

    /**
     * Gets the maximum distance without coincidences between equals
     * sequences in order to consider two methods as a clone.
     * 
     * Example: if getMaxDistance() returns 2, methodA and methodB
     * does NOT contain a clone of calls: a() and e() have a distance
     * of 3 in methodA, b(), c(), and d().
     * 
     * void methodA(){ a(); b(); c(); d(); e();} void methodB(){ a();
     * e();}
     */
    int getMaxDistance();
}
