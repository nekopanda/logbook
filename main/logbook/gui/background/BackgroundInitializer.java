package logbook.gui.background;

import java.util.List;

import logbook.config.AppConfig;
import logbook.config.ShipGroupConfig;
import logbook.data.context.GlobalContext;
import logbook.dto.CreateItemDto;
import logbook.dto.GetShipDto;
import logbook.dto.MissionResultDto;
import logbook.gui.ApplicationMain;
import logbook.gui.logic.CreateReportLogic;
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

    /**
     * コンストラクター
     * 
     * @param shell
     */
    public BackgroundInitializer(Shell shell) {
        this.display = shell.getDisplay();
        this.setName("logbook_async_initializer");
    }

    @Override
    public void run() {
        ApplicationMain.sysPrint("バックグラウンド初期化開始");
        try {
            // プロキシサーバーを開始する
            String host = (AppConfig.get().isAllowOnlyFromLocalhost() ? "localhost" : null);
            ProxyServer.start(AppConfig.get().getListenPort(), host);

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
            // 遠征ログ
            final List<MissionResultDto> missionResultList = AppConfig.get().isLoadMissionLog() ?
                    CreateReportLogic.loadMissionReport() : null;
            if (missionResultList != null) {
                this.display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        GlobalContext.addMissionResultList(missionResultList);
                        ApplicationMain.logPrint("遠征ログ読み込み完了(" + missionResultList.size() + "件)");
                    }
                });
            }
        } catch (Exception e) {
            LOG.warn("遠征ログ読み込みでエラー", e);
        }

        try {
            // 建造ログ
            final List<GetShipDto> createShipList = AppConfig.get().isLoadCreateShipLog() ?
                    CreateReportLogic.loadCreateShipReport() : null;
            if (createShipList != null) {
                this.display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        GlobalContext.addGetshipList(createShipList);
                        ApplicationMain.logPrint("建造ログ読み込み完了(" + createShipList.size() + "件)");
                    }
                });
            }
        } catch (Exception e) {
            LOG.warn("建造ログ読み込みでエラー", e);
        }

        try {
            // 開発ログ
            final List<CreateItemDto> createItemList = AppConfig.get().isLoadCreateItemLog() ?
                    CreateReportLogic.loadCreateItemReport() : null;
            if (createItemList != null) {
                this.display.asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        GlobalContext.addCreateItemList(createItemList);
                        ApplicationMain.logPrint("開発ログ読み込み完了(" + createItemList.size() + "件)");
                    }
                });
            }
        } catch (Exception e) {
            LOG.warn("開発ログ読み込みでエラー", e);
        }

        try {
            // 出撃ログファイル読み込み
            final int numLogRecord = BattleResultServer.get().size();
            ApplicationMain.sysPrint("出撃ログ読み込み完了");
            ApplicationMain.logPrint("出撃ログ読み込み完了(" + numLogRecord + "件)");
        } catch (Exception e) {
            LOG.warn("出撃ログの読み込みに失敗しました (" + AppConfig.get().getBattleLogPath() + ")", e);
        }

        ApplicationMain.logPrint("バックグラウンド初期化完了");
    }
}