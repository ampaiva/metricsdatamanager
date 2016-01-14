package com.ampaiva.metricsdatamanager.util;

import java.io.File;
import java.io.IOException;
import java.io.Writer;

import com.ampaiva.hlo.util.SourceHandler;

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

    public static String format(String source, int beglin, int endlin, boolean onlyDiff) {
        SourceHandler sourceHandler = new SourceHandler(source, beglin, 0, endlin, 0);
        String[] lines = sourceHandler.getLines();
        StringBuilder sb = new StringBuilder();
        for (int line = 0; line < lines.length; line++) {
            boolean lineBetween = sourceHandler.isLineBetween(beglin, endlin, line + 1);
            if (onlyDiff && !lineBetween) {
                continue;
            }
            sb.append("<font color=\"");
            sb.append(lineBetween ? "red" : "black");
            sb.append("\">");
            sb.append("<b>");
            for (int j = 0; j < (int) (4 - Math.log10(line + 2)); j++) {
                sb.append("&nbsp;");
            }
            sb.append((line + 1) + ".</b>&nbsp;&nbsp;&nbsp;&nbsp;"
                    + lines[line].replace(" ", "&nbsp;").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;") + "</br>");
            sb.append("</font>");
        }
        return sb.toString();
    }

}
