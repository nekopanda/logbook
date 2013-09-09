package logbook.gui.listener;

import java.io.File;
import java.io.IOException;

import logbook.gui.logic.CreateReportLogic;
import logbook.server.proxy.ProxyServer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Resource;
import org.eclipse.swt.widgets.Widget;

/**
 * シェルイベントのハンドリング
 *
 */
public final class ShellEventAdapter extends ShellAdapter {
    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(ShellEventAdapter.class);

    private final Object[] objects;

    /**
     * コンストラクター
     */
    public ShellEventAdapter(Object... disposeWidgets) {
        this.objects = disposeWidgets;
    }

    @Override
    public void shellClosed(ShellEvent paramShellEvent) {

        // 報告書を保存する
        try {
            CreateReportLogic.writeCsv(new File("./海戦・ドロップ報告書.csv"), CreateReportLogic.getBattleResultHeader(),
                    CreateReportLogic.getBattleResultBody(), true);
            CreateReportLogic.writeCsv(new File("./建造報告書.csv"), CreateReportLogic.getCreateShipHeader(),
                    CreateReportLogic.getCreateShipBody(), true);
            CreateReportLogic.writeCsv(new File("./開発報告書.csv"), CreateReportLogic.getCreateItemHeader(),
                    CreateReportLogic.getCreateItemBody(), true);
        } catch (IOException e) {
            LOG.warn("報告書の保存に失敗しました", e);
        }

        // プロキシサーバーをシャットダウンする
        ProxyServer.end();

        // リソースを開放する
        for (int i = 0; i < this.objects.length; i++) {
            if (this.objects[i] instanceof Widget) {
                ((Widget) this.objects[i]).dispose();
            }
            if (this.objects[i] instanceof Resource) {
                ((Resource) this.objects[i]).dispose();
            }
            this.objects[i] = null;
        }

        super.shellClosed(paramShellEvent);
    }
}
