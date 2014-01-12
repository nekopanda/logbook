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
import java.util.Collections;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.CheckForNull;

import logbook.config.bean.AppConfigBean;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;

/**
 * 旧設定ファイルを新設定ファイル(xml形式)に移行するクラス
 *
 */
public class ConfigMigration {

    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(ConfigMigration.class);

    /** 文字コード(Shift_JIS) */
    private static final Charset CHARSET = Charset.forName("MS932");

    /** アプリケーション設定ファイル */
    private static final File APP_CONFIG_FILE = new File("./config/internal.txt");

    /** 艦娘設定ファイル */
    private static final File SHIP_CONFIG_FILE = new File("./config/ship.txt");

    public static void migration() {
        try {
            // アプリケーション設定ファイルを移行します
            if (APP_CONFIG_FILE.exists()) {
                // 旧設定ファイルを読み込む
                Properties properties = readconfig(APP_CONFIG_FILE);
                MigrationApplicationConfig config = new MigrationApplicationConfig(properties);
                // 新設定ファイル
                AppConfigBean bean = AppConfig.get();
                // 新設定ファイルに値を設定する
                bean.setListenPort(config.getListenPort());
                bean.setWidth(config.getWidth());
                bean.setHeight(config.getHeight());
                bean.setOnTop(config.getOnTop() == SWT.ON_TOP);
                bean.setMinimumLayout(config.getMinimumLayout());
                bean.setSoundLevel(config.getSoundLevel());
                bean.setAlpha(config.getAlpha());
                bean.setReportPath(config.getReportPath());
                bean.setCheckUpdate(config.getCheckUpdate());
                bean.setHideWindow(config.getHideWindow());
                bean.setNoticeDeckmission(config.getNoticeDeckmission());
                bean.setNoticeNdock(config.getNoticeNdock());
                bean.setCapturePath(config.getCapturePath());
                bean.setImageFormat(config.getImageFormat());
                Point location = config.getLocation();
                if (location != null) {
                    bean.setLocationX(location.x);
                    bean.setLocationY(location.y);
                }
                bean.setStoreJson(config.getStoreJson());
                bean.setStoreJsonPath(config.getStoreJsonPath());
                // 新設定ファイルを保存する
                AppConfig.store();
                // 旧設定ファイルを削除する
                APP_CONFIG_FILE.delete();
            }
            // 艦娘設定ファイル を削除します
            if (SHIP_CONFIG_FILE.exists()) {
                SHIP_CONFIG_FILE.delete();
            }
        } catch (Exception e) {
            LOG.fatal("設定ファイルの移行に失敗しました", e);
        }
    }

    /**
     * 設定ファイルを読み込みます
     * 
     * @param file File
     * @return 設定ファイル
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
     * 旧形式の設定ファイルを読み込むクラス
     *
     */
    private static final class MigrationApplicationConfig {

        /** プロパティーファイル */
        private final Properties properties;

        /**
         * コンストラクタ
         */
        public MigrationApplicationConfig(Properties properties) {
            this.properties = properties;
        }

        /**
         * ポート番号を取得する
         * 
         * @return ポート番号
         */
        public int getListenPort() {
            return Integer.parseInt(this.properties.getProperty("listen_port", "8888"));
        }

        /**
         * ウインドウサイズ(width)を取得する
         * 
         * @return ウインドウサイズ(width)
         */
        public int getWidth() {
            return Integer.parseInt(this.properties.getProperty("width", "280"));
        }

        /**
         * ウインドウサイズ(height)を取得する
         * 
         * @return ウインドウサイズ(height)
         */
        public int getHeight() {
            return Integer.parseInt(this.properties.getProperty("height", "420"));
        }

        /**
         * 最前面に表示を取得する
         * 
         * @return 最前面に表示
         */
        public int getOnTop() {
            return "1".equals(this.properties.getProperty("on_top", "1")) ? SWT.ON_TOP : SWT.NONE;
        }

        /**
         * 縮小表示を取得する
         * 
         * @return 縮小表示に表示
         */
        public boolean getMinimumLayout() {
            return "1".equals(this.properties.getProperty("minimum_layout", "0"));
        }

        /**
         * 音量を取得する
         * 
         * @return 音量
         */
        public float getSoundLevel() {
            return ((float) Integer.parseInt(this.properties.getProperty("sound_level", "85"))) / 100;
        }

        /**
         * 透明度を取得する
         * 
         * @return 音量
         */
        public int getAlpha() {
            return Integer.parseInt(this.properties.getProperty("alpha", "255"));
        }

        /**
         * 報告書の保存先を取得する
         * 
         * @return
         */
        public String getReportPath() {
            return this.properties.getProperty("report_store_path", new File("").getAbsolutePath());
        }

        /**
         * アップデートチェックを取得する
         * 
         * @return アップデートチェック
         */
        public boolean getCheckUpdate() {
            return "1".equals(this.properties.getProperty("check_update", "1"));
        }

        /**
         * タスクトレイに格納を取得する
         * 
         * @return タスクトレイに格納
         */
        public boolean getHideWindow() {
            return "1".equals(this.properties.getProperty("hide_window", "0"));
        }

        /**
         * 遠征-1分前に通知するを取得する
         * 
         * @return
         */
        public boolean getNoticeDeckmission() {
            return "1".equals(this.properties.getProperty("notice_deckmission", "1"));
        }

        /**
         * 入渠-1分前に通知するを取得する
         * 
         * @return
         */
        public boolean getNoticeNdock() {
            return "1".equals(this.properties.getProperty("notice_ndock", "1"));
        }

        /**
         * 画面キャプチャ-保存先を取得する
         * 
         * @return
         */
        public String getCapturePath() {
            return this.properties.getProperty("image_store_path", new File("").getAbsolutePath());
        }

        /**
         * 画面キャプチャ-フォーマットを取得する
         * 
         * @return
         */
        public String getImageFormat() {
            return this.properties.getProperty("image_format", "jpg");
        }

        /**
         * ウインドウ位置を取得する
         * 
         * @return
         */
        @CheckForNull
        public Point getLocation() {
            String location = this.properties.getProperty("location");
            if (location != null) {
                String[] point = location.split(",");
                int x = Integer.parseInt(point[0]);
                int y = Integer.parseInt(point[1]);
                return new Point(x, y);
            }
            return null;
        }

        /**
         * 開発者オプション-JSONを保存するを取得する
         * 
         * @return JSONを保存する
         */
        public boolean getStoreJson() {
            return "1".equals(this.properties.getProperty("store_json", "0"));
        }

        /**
         * 開発者オプション-JSONの保存先を取得する
         * 
         * @return JSONの保存先
         */
        public String getStoreJsonPath() {
            return this.properties.getProperty("store_json_path", "./json/");
        }
    }
}
