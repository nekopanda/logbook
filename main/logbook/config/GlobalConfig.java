/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import logbook.data.UndefinedData;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

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
    public static final String VERSION = "0.3.5";

    /** 遠征色 */
    public static final RGB MISSION_COLOR = new RGB(102, 51, 255);

    /** 入渠色 */
    public static final RGB NDOCK_COLOR = new RGB(0, 102, 153);

    /** 疲労赤色 */
    public static final RGB COND_RED_COLOR = new RGB(255, 16, 0);

    /** 疲労オレンジ色 */
    public static final RGB COND_ORANGE_COLOR = new RGB(255, 140, 0);

    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(UndefinedData.class);

    /** 設定ファイル  */
    private static final File CONFIG_FILE = new File("./config/internal.txt");

    /** 設定プロパティ */
    private static final Properties PROPERTIES = readconfig(CONFIG_FILE);

    /**
     * コンストラクター
     */
    private GlobalConfig() {
    }

    /**
     * ポート番号を取得する
     * 
     * @return ポート番号
     */
    public static int getListenPort() {
        return Integer.parseInt(PROPERTIES.getProperty("listen_port", "8888"));
    }

    /**
     * ポート番号をセットする
     */
    public static void setListenPort(String listenPort) {
        if (StringUtils.isNumeric(listenPort)) {
            PROPERTIES.setProperty("listen_port", listenPort);
        }
    }

    /**
     * ウインドウサイズ(width)を取得する
     * 
     * @return ウインドウサイズ(width)
     */
    public static int getWidth() {
        return Integer.parseInt(PROPERTIES.getProperty("width", "280"));
    }

    /**
     * ウインドウサイズ(width)をセットする
     * 
     * @param width
     */
    public static void setWidth(String width) {
        if (StringUtils.isNumeric(width)) {
            PROPERTIES.setProperty("width", width);
        }
    }

    /**
     * ウインドウサイズ(height)を取得する
     * 
     * @return ウインドウサイズ(height)
     */
    public static int getHeight() {
        return Integer.parseInt(PROPERTIES.getProperty("height", "420"));
    }

    /**
     * ウインドウサイズ(height)をセットする
     * 
     * @param height
     */
    public static void setHeight(String height) {
        if (StringUtils.isNumeric(height)) {
            PROPERTIES.setProperty("height", height);
        }
    }

    /**
     * 最前面に表示を取得する
     * 
     * @return 最前面に表示
     */
    public static int getOnTop() {
        return "1".equals(PROPERTIES.getProperty("on_top", "1")) ? SWT.ON_TOP : SWT.NONE;
    }

    /**
     * 最前面に表示をセットする
     * 
     * @param ontop
     */
    public static void setOnTop(boolean ontop) {
        PROPERTIES.setProperty("on_top", ontop ? "1" : "0");
    }

    /**
     * 音量を取得する
     * 
     * @return 音量
     */
    public static float getSoundLevel() {
        return ((float) Integer.parseInt(PROPERTIES.getProperty("sound_level", "85"))) / 100;
    }

    /**
     * 音量をセットする
     * 
     * @param level
     */
    public static void setSoundLevel(String level) {
        if (StringUtils.isNumeric(level)) {
            PROPERTIES.setProperty("sound_level", level);
        }
    }

    /**
     * 開発者オプション-JSONを保存するを取得する
     * 
     * @return
     */
    public static boolean getStoreJson() {
        return "1".equals(PROPERTIES.getProperty("store_json", "0"));
    }

    /**
     * 開発者オプション-JSONを保存するをセットする
     * 
     * @return
     */
    public static void setStoreJson(boolean storeJson) {
        PROPERTIES.setProperty("store_json", storeJson ? "1" : "0");
    }

    /**
     * 開発者オプション-JSONの保存先を取得する
     * 
     * @return
     */
    public static String getStoreJsonPath() {
        return PROPERTIES.getProperty("store_json_path", "./json/");
    }

    /**
     * 開発者オプション-JSONの保存先をセットする
     * 
     * @return
     */
    public static void setStoreJsonPath(String storeJsonPath) {
        PROPERTIES.setProperty("store_json_path", storeJsonPath);
    }

    /**
     * 設定ファイルを書き込みます
     */
    public static void store() {
        saveconfig(PROPERTIES, CONFIG_FILE);
    }

    /**
     * 設定ファイルを読み込みます
     * 
     * @param file File
     * @return
     */
    private static Properties readconfig(File file) {
        Properties properties = new Properties() {
            @Override
            public Set<Object> keySet() {
                return Collections.unmodifiableSet(new TreeSet<Object>(super.keySet()));
            }

            @Override
            public synchronized Enumeration<Object> elements() {
                return Collections.enumeration(new TreeSet<Object>(super.keySet()));
            }
        };
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

    /**
     * 設定ファイルを書き込みます
     * 
     * @param properties Properties
     * @param file File
     */
    private static void saveconfig(Properties properties, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            try {
                OutputStreamWriter writer = new OutputStreamWriter(out, CHARSET);
                try {
                    properties.store(writer, "内部設定");
                } finally {
                    writer.close();
                }
            } finally {
                out.close();
            }
        } catch (Exception e) {
            LOG.fatal("設定ファイルの書き込みに失敗しました", e);
        }
    }
}
