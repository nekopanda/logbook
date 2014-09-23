package logbook.gui.background;

import logbook.config.AppConfig;
import logbook.gui.ApplicationMain;
import logbook.internal.BattleResultServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.widgets.Shell;

/**
 * 出撃ログを読み込みます
 *
 */
public final class AsyncLoadBattleLog extends Thread {

    private static final Logger LOG = LogManager.getLogger(AsyncLoadBattleLog.class);

    private final Shell shell;
    private final ApplicationMain main;
    private final String logPath = AppConfig.get().getBattleLogPath();

    /**
     * コンストラクター
     * 
     * @param shell
     */
    public AsyncLoadBattleLog(Shell shell, ApplicationMain main) {
        this.shell = shell;
        this.main = main;
        this.setName("logbook_async_load_battle_log");
    }

    @Override
    public void start() {
        super.start();
        // スレッドが開始したことを確認してから帰る
        try {
            this.wait();
        } catch (InterruptedException e) {
            //
        }
    }

    @Override
    public void run() {
        try {
            BattleResultServer.initialize(this.logPath, this);

            final int numLogRecord = BattleResultServer.get().size();
            this.shell.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    AsyncLoadBattleLog.this.main.printMessage("出撃ログ読み込み完了(" + numLogRecord + "件)");
                }
            });
        } catch (Exception e) {
            LOG.warn("出撃ログの読み込みに失敗しました (" + this.logPath + ")", e);
        }
    }
}
