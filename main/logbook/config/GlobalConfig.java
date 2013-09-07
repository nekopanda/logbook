package logbook.config;

import java.io.File;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logbook.data.UndefinedData;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;

/**
 * <p>
 * 各種設定はこのクラスに押し込められます<br>
 * 設定ファイルはShift_JIS(CRLF)でKey=Value形式で記述されることを想定しています<br>
 * </p>
 */
public final class GlobalConfig {

    /** 文字コード(Shift_JIS) */
    public static final Charset CHARSET = Charset.forName("MS932");

    /** 日付書式 */
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /** バージョン */
    public static final String VERSION = "0.2.0";

    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(UndefinedData.class);

    /** 各種設定 */
    private static final GlobalConfig CONFIG = new GlobalConfig();

    /** ポート番号 */
    private final int listenPort;

    /** ウインドウサイズ(width) */
    private final int width;

    /** ウインドウサイズ(height) */
    private final int height;

    /** 最前面に表示する */
    private final int onTop;

    /**
     * コンストラクター
     */
    private GlobalConfig() {

        // 内部設定
        Map<String, String> internal =
                configreader(new File("./config/internal.txt"));
        // ポート番号
        String listenport = internal.get("listen_port");
        if ((listenport != null) && StringUtils.isNumeric(listenport)) {
            this.listenPort = Integer.parseInt(listenport);
        } else {
            this.listenPort = 8888;
        }
        // ウインドウサイズ(width)
        String width = internal.get("width");
        if ((width != null) && StringUtils.isNumeric(width)) {
            this.width = Integer.parseInt(width);
        } else {
            this.width = 370;
        }
        // ウインドウサイズ(width)
        String height = internal.get("height");
        if ((height != null) && StringUtils.isNumeric(height)) {
            this.height = Integer.parseInt(height);
        } else {
            this.height = 450;
        }
        // 常に最前面に表示
        String onTop = internal.get("on_top");
        if ((onTop != null) && "1".equals(onTop)) {
            this.onTop = SWT.ON_TOP;
        } else {
            this.onTop = 0;
        }
    }

    /**
     * ポート番号
     * @return ポート番号
     */
    public int getListenPort() {
        return this.listenPort;
    }

    /**
     * ウインドウサイズ(width)
     * @return ウインドウサイズ(width)
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * ウインドウサイズ(height)
     * @return ウインドウサイズ(height)
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * 最前面に表示
     * @return 最前面に表示
     */
    public int getOnTop() {
        return this.onTop;
    }

    /**
     * 設定ファイルを読み込みます
     * @param file
     * @return
     */
    private static Map<String, String> configreader(File file) {

        Map<String, String> ret = new HashMap<String, String>();

        try {
            List<String> lines = FileUtils.readLines(file, CHARSET);
            for (String line : lines) {
                if (!line.startsWith("#") && (line.indexOf('=') > 0)) {
                    String[] kv = line.split("=", 2);
                    if (kv.length == 2) {
                        ret.put(kv[0], kv[1]);
                    }
                }
            }
        } catch (Exception e) {
            LOG.fatal("設定ファイルの読み込みに失敗しました", e);
        }
        return ret;
    }

    public static GlobalConfig getConfig() {
        return CONFIG;
    }
}
