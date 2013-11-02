/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import logbook.data.context.GlobalContext;
import logbook.dto.ResourceDto;
import logbook.dto.ShipDto;

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
    public static final String VERSION = "0.4.1";

    /** 遠征色 */
    public static final RGB MISSION_COLOR = new RGB(102, 51, 255);

    /** 入渠色 */
    public static final RGB NDOCK_COLOR = new RGB(0, 102, 153);

    /** 疲労赤色 */
    public static final RGB COND_RED_COLOR = new RGB(255, 16, 0);

    /** 疲労オレンジ色 */
    public static final RGB COND_ORANGE_COLOR = new RGB(255, 140, 0);

    /** 5分前 */
    public static final RGB TIME_IN_5_MIN = new RGB(255, 215, 0);

    /** 10分前 */
    public static final RGB TIME_IN_10_MIN = new RGB(255, 239, 153);

    /** 20分前 */
    public static final RGB TIME_IN_20_MIN = new RGB(255, 247, 203);

    /** テーブル行(偶数行)背景色 */
    public static final RGB ROW_BACKGROUND = new RGB(246, 246, 246);

    /** ホームページ */
    public static final URI HOME_PAGE_URI = URI.create("http://kancolle.sanaechan.net/");

    /** アップデートチェック先 */
    public static final URI UPDATE_CHECK_URI = URI.create("http://kancolle.sanaechan.net/checkversion.txt");

    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(GlobalConfig.class);

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
     * 
     * @param listenPort
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
     * 縮小表示を取得する
     * 
     * @return 縮小表示に表示
     */
    public static boolean getMinimumLayout() {
        return "1".equals(PROPERTIES.getProperty("minimum_layout", "0"));
    }

    /**
     * 縮小表示をセットする
     * 
     * @param minimumLayout
     */
    public static void setMinimumLayout(boolean minimumLayout) {
        PROPERTIES.setProperty("minimum_layout", minimumLayout ? "1" : "0");
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
     * アップデートチェックを取得する
     * 
     * @return アップデートチェック
     */
    public static boolean getCheckUpdate() {
        return "1".equals(PROPERTIES.getProperty("check_update", "1"));
    }

    /**
     * アップデートチェックをセットする
     * 
     * @param checkUpdate
     */
    public static void setCheckUpdate(boolean checkUpdate) {
        PROPERTIES.setProperty("check_update", checkUpdate ? "1" : "0");
    }

    /**
     * タスクトレイに格納を取得する
     * 
     * @return タスクトレイに格納
     */
    public static boolean getHideWindow() {
        return "1".equals(PROPERTIES.getProperty("hide_window", "0"));
    }

    /**
     * タスクトレイに格納をセットする
     * 
     * @param hideWindow
     */
    public static void setHideWindow(boolean hideWindow) {
        PROPERTIES.setProperty("hide_window", hideWindow ? "1" : "0");
    }

    /**
     * 遠征-1分前に通知するを取得する
     * 
     * @return
     */
    public static boolean getNoticeDeckmission() {
        return "1".equals(PROPERTIES.getProperty("notice_deckmission", "1"));
    }

    /**
     * 遠征-1分前に通知するをセットする
     * 
     * @param notice 1分前に通知する
     */
    public static void setNoticeDeckmission(boolean notice) {
        PROPERTIES.setProperty("notice_deckmission", notice ? "1" : "0");
    }

    /**
     * 入渠-1分前に通知するを取得する
     * 
     * @return
     */
    public static boolean getNoticeNdock() {
        return "1".equals(PROPERTIES.getProperty("notice_ndock", "1"));
    }

    /**
     * 入渠-1分前に通知するをセットする
     * 
     * @param notice 1分前に通知する
     */
    public static void setNoticeNdock(boolean notice) {
        PROPERTIES.setProperty("notice_ndock", notice ? "1" : "0");
    }

    /**
     * 建造ドックの投入資源を取得する
     * 
     * @return 投入資源
     */
    public static ResourceDto getCreateShipResource(String dock) {
        String key = "createship_" + dock;
        Map<Long, ShipDto> ships = GlobalContext.getShipMap();
        if (ships.size() > 0) {
            String resource = PROPERTIES.getProperty(key);
            if (resource != null) {
                PROPERTIES.remove(key);
                String[] values = resource.split(",");
                if ((values.length != 6) || !StringUtils.isNumeric(values[4]) || !StringUtils.isNumeric(values[5])) {
                    return null;
                }

                String fuel = values[0];
                String ammo = values[1];
                String metal = values[2];
                String bauxite = values[3];
                ShipDto ship = ships.get(Long.parseLong(values[4]));
                int hqLevel = Integer.parseInt(values[5]);

                if (ship != null) {
                    return new ResourceDto(fuel, ammo, metal, bauxite, ship, hqLevel);
                }
            }
        }
        return null;
    }

    /**
     * 建造ドックの投入資源をセットする
     * 
     * @param dock 建造ドック
     * @param resource 投入資源
     */
    public static void setCreateShipResource(String dock, ResourceDto resource) {
        String key = "createship_" + dock;

        String fuel = resource.getFuel();
        String ammo = resource.getAmmo();
        String metal = resource.getMetal();
        String bauxite = resource.getBauxite();
        ShipDto ship = resource.getSecretary();
        String hqLevel = Integer.toString(resource.getHqLevel());

        if (ship != null) {
            String value = fuel + "," + ammo + "," + metal + "," + bauxite + "," + ship.getId() + "," + hqLevel;
            PROPERTIES.setProperty(key, value);
        }
    }

    /**
     * 建造ドックの投入資源を削除する
     * 
     * @param dock 建造ドック
     */
    public static void removeCreateShipResource(String dock) {
        String key = "createship_" + dock;

        PROPERTIES.remove(key);
    }

    /**
     * 開発者オプション-JSONを保存するを取得する
     * 
     * @return JSONを保存する
     */
    public static boolean getStoreJson() {
        return "1".equals(PROPERTIES.getProperty("store_json", "0"));
    }

    /**
     * 開発者オプション-JSONを保存するをセットする
     * 
     * @param storeJson
     */
    public static void setStoreJson(boolean storeJson) {
        PROPERTIES.setProperty("store_json", storeJson ? "1" : "0");
    }

    /**
     * 開発者オプション-JSONの保存先を取得する
     * 
     * @return JSONの保存先
     */
    public static String getStoreJsonPath() {
        return PROPERTIES.getProperty("store_json_path", "./json/");
    }

    /**
     * 開発者オプション-JSONの保存先をセットする
     * 
     * @param storeJsonPath
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
     * @return 設定ファイル
     */
    public static Properties readconfig(File file) {
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
            if (file.exists()) {
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
    public static void saveconfig(Properties properties, File file) {
        try {
            if (!file.exists()) {
                File parent = file.getParentFile();
                if ((parent != null) && (!(parent.mkdirs())) && (!(parent.isDirectory()))) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
            OutputStream out = new FileOutputStream(file);
            try {
                OutputStreamWriter writer = new OutputStreamWriter(out, CHARSET);
                try {
                    properties.store(writer, "");
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
