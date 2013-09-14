/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.server.proxy;

/**
 * 動作に必要なデータのみ取得するためのフィルターです。
 *
 */
public class Filter {

    /** フィルターするContent-Type */
    public static final String CONTENT_TYPE_FILTER = "text/plain";

    /** キャプチャーするリクエストのバイトサイズ上限 */
    public static final int MAX_POST_FIELD_SIZE = 1024 * 1024;

    /** setAttribute用のキー(Response) */
    public static final String RESPONSE_BODY = "res-body";

    /** setAttribute用のキー(Request) */
    public static final String REQUEST_BODY = "req-body";

    private static String serverName;

    /**
     * 鎮守府サーバー名を設定する
     * @param name 鎮守府サーバー名
     */
    public static void setServerName(String name) {
        serverName = name;
    }

    /**
     * 鎮守府サーバー名を取得する
     * @param name 鎮守府サーバー名
     */
    public static String getServerName() {
        return serverName;
    }

    /**
     * 鎮守府サーバー名を検出した場合true
     * 
     * @return 鎮守府サーバー名を検出した場合true
     */
    public static boolean isServerDetected() {
        return serverName != null;
    }

    /**
     * <p>
     * 取得が必要なデータかを調べます<br>
     * 鎮守府サーバーが検出された場合はサーバー名から必要かどうかを判別します<br>
     * 鎮守府サーバーが検出できていない場合は常にtrue<br>
     * 
     * @param name サーバー名
     * @return 取得が必要なデータか
     */
    public static boolean isNeed(String name) {
        if ((!isServerDetected() || (isServerDetected() && serverName.equals(name)))) {
            return true;
        }
        return false;
    }

    /**
     * <p>
     * 取得が必要なデータかを調べます<br>
     * 鎮守府サーバーが検出された場合はサーバー名とContent-Typeから必要かどうかを判別します<br>
     * 鎮守府サーバーが検出できていない場合はContent-Typeから必要かどうかを判別します<br>
     * 
     * @param name サーバー名
     * @param contentType Content-Type
     * @return 取得が必要なデータか
     */
    public static boolean isNeed(String name, String contentType) {
        if ((!isServerDetected() || serverName.equals(name))
                && CONTENT_TYPE_FILTER.equals(contentType)) {
            return true;
        }
        return false;
    }
}
