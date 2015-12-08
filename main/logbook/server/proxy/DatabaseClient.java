package logbook.server.proxy;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import logbook.config.AppConfig;
import logbook.constants.AppConstants;
import logbook.data.UndefinedData;
import logbook.gui.ApplicationMain;
import logbook.internal.LoggerHolder;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.util.UrlEncoded;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.swt.widgets.Display;

/**
 * 艦これ統計データベースに送信する
 * @author Nekopanda
 */
public class DatabaseClient extends Thread {
    private static final LoggerHolder LOG = new LoggerHolder(DatabaseClient.class);
    private static DatabaseClient instance = null;

    private static final String[] sendDatabaseUrls = new String[]
    {
            "api_port/port",
            "api_get_member/kdock",
            "api_get_member/ship2",
            "api_get_member/ship3",
            "api_get_member/slot_item",
            "api_get_member/mapinfo",
            "api_req_hensei/change",
            "api_req_kousyou/createship",
            "api_req_kousyou/getship",
            "api_req_kousyou/createitem",
            "api_req_map/start",
            "api_req_map/next",
            "api_req_map/select_eventmap_rank",
            "api_req_sortie/battle",
            "api_req_battle_midnight/battle",
            "api_req_battle_midnight/sp_midnight",
            "api_req_sortie/night_to_day",
            "api_req_sortie/battleresult",
            "api_req_combined_battle/airbattle",
            "api_req_combined_battle/battle",
            "api_req_combined_battle/midnight_battle",
            "api_req_combined_battle/sp_midnight",
            "api_req_combined_battle/battleresult",
            "api_req_sortie/airbattle",
            "api_req_combined_battle/battle_water",
            "api_req_combined_battle/sp_midnight"
    };

    private static class QueueItem {
        public UndefinedData data;

        public QueueItem(UndefinedData data) {
            this.data = data;
        }
    }

    private static synchronized DatabaseClient getInstance() {
        if (instance == null) {
            instance = new DatabaseClient();
            instance.start();
        }
        return instance;
    }

    public static void send(UndefinedData data) {
        if (AppConfig.get().isSendDatabase() && (AppConfig.get().getAccessKey().length() > 0)) {
            for (String entry : sendDatabaseUrls)
            {
                if (data.getUrl().endsWith(entry))
                {
                    getInstance().dataQueue.offer(new QueueItem(data));
                    break;
                }
            }
        }
    }

    public static synchronized void end() {
        if (instance != null) {
            instance.endRequested = true;
            instance.dataQueue.offer(new QueueItem(null));
            try {
                instance.join();
                instance = null;
            } catch (InterruptedException e) {
                LOG.get().fatal("DatabaseClientスレッド終了時に何かのエラー", e);
            }
        }
    }

    /** この正規表現イミフになりつつある・・・ */
    private final Pattern apiTokenPattern = Pattern
            .compile("&api(_|%5F)token=[0-9a-f]+|api(_|%5F)token=[0-9a-f]+&?");

    private final BlockingQueue<QueueItem> dataQueue = new ArrayBlockingQueue<>(32);

    private HttpClient httpClient = null;

    private boolean endRequested = false;

    private Request createRequest(UndefinedData data) throws UnsupportedEncodingException {
        // api_tokenを取り除く
        String origRequest = new String(data.getRequest(), "UTF-8");
        String sendRequestBody = this.apiTokenPattern.matcher(origRequest).replaceAll("");
        String sendResponseBody = new String(data.getResponse(), "UTF-8");
        UrlEncoded body = new UrlEncoded();
        body.add("token", AppConfig.get().getAccessKey());
        // このクライアントのエージェントキー
        body.add("agent", "6nENnnGzRgSTVeuU652r");
        body.add("url", data.getFullUrl());
        body.add("requestbody", sendRequestBody);
        body.add("responsebody", sendResponseBody); //
        return this.httpClient.POST("http://api.kancolle-db.net/2/")
                .agent("logbook/v" + AppConstants.VERSION)
                .content(new StringContentProvider(body.encode()), "application/x-www-form-urlencoded");
    }

    /* (非 Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        try {
            int skipCount = 0;
            int errorCount = 0;
            this.httpClient = new HttpClient();
            this.httpClient.setExecutor(new QueuedThreadPool(2, 1));
            this.httpClient.setMaxConnectionsPerDestination(2);
            this.httpClient.start();

            while (true) {
                final UndefinedData data = this.dataQueue.take().data;
                if (this.endRequested) {
                    return;
                }
                if (skipCount > 0) {
                    --skipCount;
                    continue;
                }
                for (int retly = 0;; ++retly) {
                    String errorReason = null;
                    try {
                        // 60秒でタイムアウト
                        Request request = this.createRequest(data).timeout(60, TimeUnit.SECONDS);
                        ContentResponse response = request.send();
                        if (this.endRequested) {
                            return;
                        }
                        if (HttpStatus.isSuccess(response.getStatus())) {
                            // 成功したらエラーカウンタをリセット
                            skipCount = errorCount = 0;
                            // ログに出す
                            if (AppConfig.get().isDatabaseSendLog()) {
                                Display.getDefault().asyncExec(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (!ApplicationMain.main.getShell().isDisposed()) {
                                            String url = data.getUrl();
                                            ApplicationMain.main.printMessage("DBへ送信しました("
                                                    + url.substring(url.lastIndexOf('/') + 1) + ")");
                                        }
                                    }
                                });
                            }
                            break;
                        }
                        else {
                            errorReason = response.getReason();
                        }
                    } catch (Exception e) {
                        errorReason = e.getMessage();
                    }
                    if (errorReason != null) {
                        // 少し時間をおいてリトライ
                        Thread.sleep(1000);
                        if (this.endRequested) {
                            return;
                        }
                        if (retly >= 4) {
                            // リトライが多すぎたらエラーにする
                            skipCount = (errorCount++) * 4;
                            LOG.get().warn("データベースへの送信に失敗しました. " + errorReason);
                            if (skipCount > 0) {
                                LOG.get().warn("以降 " + skipCount + " 個の送信をスキップします.");
                            }
                            break;
                        }
                    }
                }
            }

        } catch (Exception e) {
            if (!this.endRequested) {
                LOG.get().fatal("スレッドが異常終了しました", e);
            }
        } finally {
            if (this.httpClient != null) {
                try {
                    this.httpClient.stop();
                } catch (Exception e) {
                    LOG.get().fatal("HttpClientの終了に失敗", e);
                }
            }
        }
    }
}
