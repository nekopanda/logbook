package logbook.gui.background;

import java.nio.charset.Charset;

import logbook.constants.AppConstants;

import org.apache.commons.io.IOUtils;

/**
 * アップデートチェックを行います
 *
 */
public final class AsyncExecUpdateCheck extends Thread {

    public static interface UpdateResult {
        void onSuccess(String[] okversions);

        void onError(Exception e);
    }

    private final UpdateResult handler;

    /**
     * コンストラクター
     * 
     * @param handler
     */
    public AsyncExecUpdateCheck(UpdateResult handler) {
        this.handler = handler;
        this.setName("logbook_async_exec_update_check");
    }

    @Override
    public void run() {
        try {
            String[] okversions = IOUtils.toString(AppConstants.UPDATE_CHECK_URI, Charset.forName("UTF-8"))
                    .split(";");
            this.handler.onSuccess(okversions);
        } catch (Exception e) {
            this.handler.onError(e);
        }
    }
}
