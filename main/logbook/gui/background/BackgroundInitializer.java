package logbook.gui.background;

import logbook.config.AppConfig;
import logbook.config.ShipGroupConfig;
import logbook.data.context.GlobalContext;
import logbook.gui.ApplicationMain;
import logbook.internal.BattleResultServer;
import logbook.internal.EnemyData;
import logbook.internal.Item;
import logbook.internal.MasterData;
import logbook.internal.Ship;
import logbook.server.proxy.ProxyServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * 時間のかかる初期化を別スレッドで実行します
 */
public final class BackgroundInitializer extends Thread {

    private static final Logger LOG = LogManager.getLogger(BackgroundInitializer.class);

    private final Display display;
    private final ApplicationMain main;

    /**
     * コンストラクター
     * 
     * @param shell
     */
    public BackgroundInitializer(Shell shell, ApplicationMain main) {
        this.display = shell.getDisplay();
        this.main = main;
        this.setName("logbook_async_load_battle_log");
    }

    @Override
    public void run() {
        ApplicationMain.sysPrint("バックグラウンド初期化開始");
        try {
            // プロキシサーバーを開始する
            ProxyServer.start(AppConfig.get().getListenPort());

        } catch (Exception e) {
            LOG.warn("サーバ起動に失敗しました", e);
        }
        ApplicationMain.sysPrint("サーバ起動完了");

        // 設定ファイルを読み込む（遅延初期化が実装されているが先読みしておく）
        try {
            boolean success = true;
            success &= Ship.INIT_COMPLETE; // ShipConfig
            success &= MasterData.INIT_COMPLETE; // MasterData
            ShipGroupConfig.get(); // ShipGroupConfig
            success &= Item.INIT_COMPLETE; // ItemMasterConfig
            success &= GlobalContext.INIT_COMPLETE; // ItemConfig
            success &= EnemyData.INIT_COMPLETE; // EnemyData
            if (!success) {
                LOG.warn("設定ファイルの読み込みに失敗したっぽい？");
            }
        } catch (Exception e) {
            LOG.warn("設定ファイル読み込みでエラーが発生しました", e);
        }
        ApplicationMain.sysPrint("設定ファイル読み込み完了");

        try {
            // 出撃ログファイル読み込み
            final int numLogRecord = BattleResultServer.get().size();
            ApplicationMain.sysPrint("バックグラウンド初期化完了");
            this.display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    BackgroundInitializer.this.main.printMessage("出撃ログ読み込み完了(" + numLogRecord + "件)");
                }
            });
        } catch (Exception e) {
            LOG.warn("出撃ログの読み込みに失敗しました (" + AppConfig.get().getBattleLogPath() + ")", e);
        }
    }
}
