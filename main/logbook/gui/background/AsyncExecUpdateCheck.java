package logbook.gui.background;

import java.awt.Desktop;
import java.nio.charset.Charset;

import logbook.config.AppConfig;
import logbook.constants.AppConstants;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * アップデートチェックを行います
 *
 */
public final class AsyncExecUpdateCheck extends Thread {

    private static final Logger LOG = LogManager.getLogger(AsyncExecUpdateCheck.class);

    private final Shell shell;

    /**
     * コンストラクター
     * 
     * @param shell
     */
    public AsyncExecUpdateCheck(Shell shell) {
        this.shell = shell;
        this.setName("logbook_async_exec_update_check");
    }

    @Override
    public void run() {
        try {
            final String[] okversions = IOUtils.toString(AppConstants.UPDATE_CHECK_URI, Charset.forName("UTF-8"))
                    .split(";");

            boolean ok = false;
            for (String okversion : okversions) {
                if (AppConstants.VERSION.equals(okversion)) {
                    ok = true;
                    break;
                }
            }

            AppConfig.get().setLatestVersion(okversions[0]);

            if ((ok == false) && AppConfig.get().isUpdateCheck()) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        Shell shell = AsyncExecUpdateCheck.this.shell;

                        if (shell.isDisposed()) {
                            // ウインドウが閉じられていたらなにもしない
                            return;
                        }

                        MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO
                                | SWT.ICON_QUESTION);
                        box.setText("新しいバージョン");
                        box.setMessage("新しいバージョンがあります。ホームページを開きますか？\r\n"
                                + "現在のバージョン:" + AppConstants.VERSION + "\r\n"
                                + "新しいバージョン:" + okversions[0] + "\r\n"
                                + "※自動アップデートチェックは[その他]-[設定]からOFFに出来ます");

                        // OKを押されたらホームページへ移動する
                        if (box.open() == SWT.YES) {
                            try {
                                Desktop.getDesktop().browse(AppConstants.HOME_PAGE_URI);
                            } catch (Exception e) {
                                LOG.warn("ウェブサイトに移動が失敗しました", e);
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            // チェックしなくてもいい設定の場合はエラーを無視する
            if (AppConfig.get().isUpdateCheck()) {
                // アップデートチェック失敗はクラス名のみ
                LOG.info(e.getClass().getName() + "が原因でアップデートチェックに失敗しました");
            }
        }
    }
}
