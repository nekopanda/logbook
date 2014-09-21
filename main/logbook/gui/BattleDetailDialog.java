/**
 * 
 */
package logbook.gui;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import logbook.constants.AppConstants;
import logbook.dto.BattleExDto;
import logbook.dto.BattleExDto.Phase;
import logbook.dto.BattleResultDto;
import logbook.internal.BattleResultServer;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Nekopanda
 *
 */
public class BattleDetailDialog extends WindowBase {
    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(BattleResultServer.class);

    private BattleResultDto result;
    private BattleExDto detail;
    private Browser browser;

    /*
    private StyledText content;

    private static class StyledTextBuilder {
        List<StyleRange> styles = new ArrayList<StyleRange>();
        StringBuilder text = new StringBuilder();

        public StyledTextBuilder add(String str, StyleRange style) {
            StyleRange addStyle = style;
            if (addStyle == null) {
                addStyle = new StyleRange();
            }
            addStyle.start = this.text.length();
            addStyle.length = str.length();
            this.styles.add(addStyle);

            return this;
        }

        public void apply(StyledText target) {
            target.setText(this.text.toString());
            StyleRange[] styles = new StyleRange[this.styles.size()];
            this.styles.toArray(styles);
            target.setStyleRanges(styles);
        }
    }
    */

    private static class HTMLGenerator {
        private final StringBuilder sb = new StringBuilder();
        private final List<String> tagStack = new ArrayList<String>();
        private int nestedCount = 0;

        private static String TAB = "    ";

        public void genHeader(String title) throws IOException {
            this.sb.append("<!DOCTYPE html>").append("\r\n");
            this.sb.append("<html>").append("\r\n");
            this.sb.append("<head>").append("\r\n");
            this.sb.append("<meta charset=\"UTF-8\">").append("\r\n");
            this.sb.append("<title>").append(title).append("</title>").append("\r\n");
            this.sb.append("<style type=\"text/css\">").append("\r\n");
            if (AppConstants.BATTLE_LOG_CSS_FILE.exists()) {
                String css = IOUtils.toString(new FileInputStream(AppConstants.BATTLE_LOG_CSS_FILE));
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

        public void inline(String tag, String innerText, String[] cls) {
            this.genIndent();
            this.sb.append("<").append(tag);
            this.genClass(cls);
            this.sb.append(">").append(innerText);
            this.sb.append("</").append(tag).append(">").append("\r\n");
        }

        public void inline(String tag, String[] cls) {
            this.genIndent();
            this.sb.append("<").append(tag);
            this.genClass(cls);
            this.sb.append(">").append("\r\n");
        }
    }

    public BattleDetailDialog(WindowBase parent) {
        super.createContents(parent, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE, false);
    }

    /**
     * Open the dialog.
     * @return the result
     */
    @Override
    public void open() {
        if (!this.isWindowInitialized()) {
            this.createContents();
            super.registerEvents();
            // 閉じたときに dispose しない
            this.getShell().addShellListener(new ShellAdapter() {
                @Override
                public void shellClosed(ShellEvent e) {
                    e.doit = false;
                    BattleDetailDialog.this.setVisible(false);
                }
            });
            this.setWindowInitialized(true);
        }
        this.setVisible(true);
        this.getShell().setActive();
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        Shell shell = this.getShell();
        shell.setLayout(new FillLayout(SWT.VERTICAL));
        this.browser = new Browser(shell, SWT.NONE);
    }

    public void setBattle(BattleResultDto result, BattleExDto detail) {
        Shell shell = this.getShell();
        String title = "会敵報告: " + result.getMapCell().detailedString();
        shell.setText(title);
        try {
            this.browser.setText(generateHTML(title, result, detail));
            this.result = result;
            this.detail = detail;
        } catch (IOException e) {
            LOG.warn("会敵報告作成に失敗: CSSファイル読み込みに失敗しました", e);
        }
    }

    private static void genPhase(HTMLGenerator gen, Phase phase) {
        //
    }

    private static String generateHTML(String title, BattleResultDto result, BattleExDto detail)
            throws IOException
    {
        HTMLGenerator gen = new HTMLGenerator();
        gen.genHeader(title);
        gen.begin("body", null);

        // タイトル
        String time = new SimpleDateFormat(AppConstants.DATE_FORMAT).format(result.getBattleDate());
        String header = result.getMapCell().detailedString() +
                " 「" + result.getQuestName() + "」で作戦行動中に「" +
                result.getEnemyName() + "」と対峙しました(" + time + ")";
        gen.inline("h2", header, null);

        gen.inline("hr", null);

        int numPhases = detail.getPhaseList().size();
        for (int i = 0; i < numPhases; ++i) {
            Phase phase = detail.getPhaseList().get(i);
            String phaseTitle = (i + 1) + "/" + numPhases + "フェイズ: " + (phase.isNight() ? "夜戦" : "昼戦");
            gen.inline("h3", phaseTitle, null);
            genPhase(gen, phase);
        }

        gen.end(); // body
        return gen.result();
    }
}
