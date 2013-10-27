/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui.background;

import java.awt.Desktop;

import logbook.config.GlobalConfig;

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
    }

    @Override
    public void run() {
        try {
            final String newversion = IOUtils.toString(GlobalConfig.UPDATE_CHECK_URI);

            if (!GlobalConfig.VERSION.equals(newversion)) {
                Display.getDefault().asyncExec(new Runnable() {
                    @Override
                    public void run() {

                        MessageBox box = new MessageBox(AsyncExecUpdateCheck.this.shell, SWT.YES | SWT.NO
                                | SWT.ICON_QUESTION);
                        box.setText("新しいバージョン");
                        box.setMessage("新しいバージョンがあります。ホームページを開きますか？\r\n"
                                + "現在のバージョン:" + GlobalConfig.VERSION + "\r\n"
                                + "新しいバージョン:" + newversion + "\r\n"
                                + "※自動アップデートチェックは[ヘルプ]-[設定]からOFFに出来ます");

                        // OKを押されたらホームページへ移動する
                        if (box.open() == SWT.YES) {
                            try {
                                Desktop.getDesktop().browse(GlobalConfig.HOME_PAGE_URI);
                            } catch (Exception e) {
                                LOG.warn("ウェブサイトに移動が失敗しました", e);
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            // アップデートチェック失敗はクラス名のみ
            LOG.info(e.getClass().getName() + "が原因でアップデートチェックに失敗しました");
        }
    }
}
