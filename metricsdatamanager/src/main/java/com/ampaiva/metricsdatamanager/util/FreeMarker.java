package com.ampaiva.metricsdatamanager.util;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;

public class FreeMarker {
    private static Configuration cfg;

    public static void configure(String templatesFolder) throws IOException {
        /*
         * -----------------------------------------------------------
         * -------------
         */
        /*
         * You should do this ONLY ONCE in the whole application
         * life-cycle:
         */

        /* Create and adjust the configuration singleton */
        cfg = new Configuration(Configuration.VERSION_2_3_22);
        cfg.setDirectoryForTemplateLoading(new File(templatesFolder));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    }

    public static void run(String template, Object root, Writer out) throws IOException, TemplateException {

        /*
         * -----------------------------------------------------------
         * -------------
         */
        /*
         * You usually do these for MULTIPLE TIMES in the application
         * life-cycle:
         */

        /* Get the template (uses cache internally) */
        Template temp = cfg.getTemplate(template);

        /* Merge data-model with template */
        temp.process(root, out);
        // Note: Depending on what `out` is, you may need to call `out.close()`.
        // This is usually the case for file output, but not for servlet output.
    }

}
