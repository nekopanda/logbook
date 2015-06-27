/**
 * 
 */
package logbook.gui.logic;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import logbook.constants.AppConstants;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * @author Nekopanda
 *
 */
public class HTMLGenerator {
    private final StringBuilder sb = new StringBuilder();
    private final List<String> tagStack = new ArrayList<String>();
    private int nestedCount = 0;

    private static String TAB = "    ";

    static {
        if (AppConstants.BATTLE_LOG_CSS_FILE.exists() == false) {
            // CSSファイルが存在しない場合はtemplatesからコピー
            try {
                FileUtils.copyFile(AppConstants.BATTLE_LOG_CSS_TMPL_FILE, AppConstants.BATTLE_LOG_CSS_FILE);
            } catch (IOException e) {
                //
            }
        }
    }

    public void genHeader(String title, boolean genCharset) throws IOException {
        this.sb.append("<!DOCTYPE html>").append("\r\n");
        this.sb.append("<html>").append("\r\n");
        this.sb.append("<head>").append("\r\n");
        if (genCharset) {
            this.sb.append("<meta charset=\"UTF-8\">").append("\r\n");
        }
        this.sb.append("<title>").append(title).append("</title>").append("\r\n");
        this.sb.append("<style type=\"text/css\">").append("\r\n");
        if (AppConstants.BATTLE_LOG_CSS_FILE.exists()) {
            String css = IOUtils.toString(new FileInputStream(AppConstants.BATTLE_LOG_CSS_FILE), "UTF-8");
            this.sb.append(css);
        }
        this.sb.append("</style>").append("\r\n");
        this.sb.append("</head>").append("\r\n");
    }

    public String result() {
        return this.sb.toString();
    }

    private void genIndent() {
        for (int i = 0; i < this.nestedCount; ++i) {
            this.sb.append(TAB);
        }
    }

    private void genClass(String[] cls) {
        if ((cls != null) && (cls.length > 0)) {
            this.sb.append(" class=\"");
            for (int i = 0; i < cls.length; ++i) {
                if (i > 0) {
                    this.sb.append(" ");
                }
                this.sb.append(cls[i]);
            }
            this.sb.append("\"");
        }
    }

    public void begin(String tag, String[] cls) {
        this.tagStack.add(tag);
        this.genIndent();
        this.nestedCount++;
        this.sb.append("<").append(tag);
        this.genClass(cls);
        this.sb.append(">").append("\r\n");
    }

    public void end() {
        this.nestedCount--;
        this.genIndent();
        int tagIndex = this.tagStack.size() - 1;
        this.sb.append("</").append(this.tagStack.get(tagIndex)).append(">").append("\r\n");
        this.tagStack.remove(tagIndex);
    }

    public void inline(String tag, String option, String innerText, String[] cls) {
        this.genIndent();
        this.sb.append("<").append(tag).append(" ").append(option);
        this.genClass(cls);
        this.sb.append(">");
        if (innerText != null) {
            this.sb.append(innerText);
        }
        this.sb.append("</").append(tag).append(">").append("\r\n");
    }

    public void inline(String tag, String innerText, String[] cls) {
        this.genIndent();
        this.sb.append("<").append(tag);
        this.genClass(cls);
        this.sb.append(">");
        if (innerText != null) {
            this.sb.append(innerText);
        }
        this.sb.append("</").append(tag).append(">").append("\r\n");
    }

    public void inline(String tag, String[] cls) {
        this.genIndent();
        this.sb.append("<").append(tag);
        this.genClass(cls);
        this.sb.append(">").append("\r\n");
    }
}
