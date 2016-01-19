package com.ampaiva.metricsdatamanager;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.ampaiva.metricsdatamanager.model.Repository;
import com.github.javaparser.ParseException;

public class ExtractClonesTest {

    @Test
    public void testRun() throws IOException, ParseException {
        ExtractClones extractClones = new ExtractClones(10);
        List<Repository> repositories = extractClones.run("target/test-classes/02-ecommerce", false);
        assertNotNull(repositories);
    }

}
