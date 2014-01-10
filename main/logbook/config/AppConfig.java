/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.config;

import java.io.File;
import java.io.IOException;

import logbook.config.bean.AppConfigBean;
import logbook.util.BeanUtils;

/**
 * アプリケーション設定を保存・復元します
 * 
 */
public class AppConfig {

    /** アプリケーション設定 */
    private static AppConfigBean configBean;

    /** 設定ファイル  */
    private static final File CONFIG_FILE = new File("./config/internal.xml");

    /**
     * 設定ファイルに書き込みます
     */
    public static void store() throws IOException {
        if (configBean == null) {
            configBean = new AppConfigBean();
        }
        BeanUtils.writeObject(CONFIG_FILE, configBean);
    }

    /**
     * アプリケーション設定を読み込みます
     * 
     * @param properties
     * @return
     */
    public static void load() {
        AppConfigBean bean = BeanUtils.readObject(CONFIG_FILE, AppConfigBean.class);
        if (bean != null) {
            configBean = bean;
        } else {
            configBean = new AppConfigBean();
        }
    }

    /**
     * アプリケーション設定を取得します
     * 
     * @return アプリケーション設定
     */
    public static AppConfigBean get() {
        return configBean;
    }
}
