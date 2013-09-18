/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Properties;

import logbook.data.UndefinedData;

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

    /** 日付書式 */
    public static final String DATE_SHORT_FORMAT = "HH:mm:ss";

    /** バージョン */
    public static final String VERSION = "0.3.3";

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

    /** 音量 */
    private final float soundLevel;

    /**
     * コンストラクター
     */
    private GlobalConfig() {

        // 内部設定
        Properties internal = readconfig(new File("./config/internal.txt"));
        // ポート番号
        this.listenPort = Integer.parseInt(internal.getProperty("listen_port", "8888"));
        // ウインドウサイズ(width)
        this.width = Integer.parseInt(internal.getProperty("width", "280"));
        // ウインドウサイズ(width)
        this.height = Integer.parseInt(internal.getProperty("height", "420"));
        // 常に最前面に表示
        String onTop = internal.getProperty("on_top");
        if ((onTop != null) && "1".equals(onTop)) {
            this.onTop = SWT.ON_TOP;
        } else {
            this.onTop = 0;
        }
        // 音量
        this.soundLevel = ((float) Integer.parseInt(internal.getProperty("sound_level", "85"))) / 100;
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
     * 音量
     * @return 音量
     */
    public float getSoundLevel() {
        return this.soundLevel;
    }

    /**
     * 設定ファイルを読み込みます
     * @param file
     * @return
     */
    private static Properties readconfig(File file) {
        Properties properties = new Properties();
        try {
            InputStream in = new FileInputStream(file);
            try {
                InputStreamReader reader = new InputStreamReader(in, CHARSET);
                try {
                    properties.load(reader);
                } finally {
                    reader.close();
                }
            } finally {
                in.close();
            }
        } catch (Exception e) {
            LOG.fatal("設定ファイルの読み込みに失敗しました", e);
        }
        return properties;
    }

    public static GlobalConfig getConfig() {
        return CONFIG;
    }
}
