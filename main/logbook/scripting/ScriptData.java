/**
 * 
 */
package logbook.scripting;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import logbook.constants.AppConstants;
import logbook.internal.LoggerHolder;

/**
 * スクリプトデータ永続化
 * @author Nekopanda
 */
public class ScriptData {
    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder("script");

    private static class DataObject implements Serializable {
        private static final long serialVersionUID = -115021202763582793L;
        public Date lastAccessed = null;
        public Object data;
        public transient boolean persist;

        public DataObject(Object data, boolean persist) {
            this.data = data;
            this.persist = persist;
        }

        @Override
        public String toString() {
            return this.data.toString();
        }
    }

    private static Map<String, DataObject> dataMap = new HashMap<>();

    private static boolean modified = false;

    // 始めてアクセスがあった時に読み込む
    static {
        try {
            load();
            // 60日間アクセスがないデータを削除
            cleanup(60);
        } catch (IOException e) {
            LOG.get().warn("スクリプトデータ読み込みに失敗しました", e);
        }
    }

    /**
     * 指定されたキーでデータを格納
     * 
     * setData(key, value, true)と同じです
     * @param key データのキー
     * @param value データ
     */
    public static void setData(String key, Object value) {
        setData(key, value, true);
    }

    /**
     * 指定されたキーでデータを格納
     * 
     * データを保存する場合は、はシリアライズ可能でなければなりません。
     * スクリプトのオブジェクト（連想配列や配列など）はシリアライズできないため保存できません。
     * オブジェクトを保存したい場合はJSON.stringify()であらかじめシリアライズしておいてください。
     * （復元はJSON.parse()でできます）
     * @param key データのキー
     * @param value データ
     * @param persist データを保存するかどうか
     */
    public static void setData(String key, Object value, boolean persist) {
        DataObject data = dataMap.get(key);
        // lastAccessedはstore()で書き込まれる
        if (data != null) {
            data.data = value;
            data.lastAccessed = null;
            data.persist = persist;
        }
        else {
            dataMap.put(key, new DataObject(value, persist));
        }
        if (persist) {
            modified = true;
        }
    }

    /**
     * 指定されたキーでデータを取得
     * @param key データのキー
     * @return データ
     */
    public static Object getData(String key) {
        DataObject data = dataMap.get(key);
        if (data == null) {
            return null;
        }
        data.lastAccessed = null;
        return data.data;
    }

    /**
     * （システム用です。スクリプトから呼び出す必要はありません。）
     * 変更があった場合は現在のデータをファイルに保存
     * @throws IOException
     */
    public static void store() throws IOException {
        if (!modified) {
            return;
        }
        Date time = new Date();
        try (ZipOutputStream zos = new ZipOutputStream(
                new BufferedOutputStream(new FileOutputStream(AppConstants.SCRIPT_DATA_FILE))))
        {
            for (Map.Entry<String, DataObject> entry : dataMap.entrySet()) {
                DataObject data = entry.getValue();
                if (data.persist == false) {
                    continue;
                }
                if (data.lastAccessed == null) {
                    data.lastAccessed = time;
                }
                try {
                    ZipEntry zipentry = new ZipEntry(entry.getKey());
                    zos.putNextEntry(zipentry);
                    ObjectOutputStream oos = new ObjectOutputStream(zos);
                    oos.writeObject(data);
                    oos.flush();
                } catch (IOException e) {
                    LOG.get().warn("データの保存に失敗(" + entry.getKey() + ")", e);
                }
            }
            modified = false;
        }
    }

    /**
     * （システム用です。スクリプトから呼び出す必要はありません。）
     * ファイルから復元
     * @throws IOException
     */
    public static void load() throws IOException {
        if (AppConstants.SCRIPT_DATA_FILE.exists()) {
            try (ZipInputStream zis = new ZipInputStream(
                    new BufferedInputStream(new FileInputStream(AppConstants.SCRIPT_DATA_FILE))))
            {
                for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                    String key = entry.getName();
                    try {
                        DataObject data = (DataObject) (new ObjectInputStream(zis).readObject());
                        data.persist = true;
                        dataMap.put(key, data);
                    } catch (ClassNotFoundException | ClassCastException e) {
                        LOG.get().warn("データの読み込みに失敗(" + key + ")", e);
                    } catch (IOException e) {
                        LOG.get().warn("データの読み込みに失敗(" + key + ")", e);
                    }
                }
            }
        }
    }

    /**
     * 記憶しているデータ数
     * @return
     */
    public static int size() {
        return dataMap.size();
    }

    /**
     * （システム用です。スクリプトから呼び出す必要はありません。）
     * daysBefore日前以前のデータを削除
     * @param daysBefore
     */
    public static void cleanup(int daysBefore) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, -daysBefore);
        Date time = calendar.getTime();
        for (Map.Entry<String, DataObject> entry : dataMap.entrySet()) {
            Date lastAccessed = entry.getValue().lastAccessed;
            if ((lastAccessed != null) && lastAccessed.before(time)) {
                // 終了時までアクセスされなかったら保存されない
                entry.getValue().persist = false;
            }
        }
    }
}
