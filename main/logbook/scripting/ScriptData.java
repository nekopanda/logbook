/**
 * 
 */
package logbook.scripting;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import logbook.constants.AppConstants;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * スクリプトデータ永続化
 * @author Nekopanda
 */
public class ScriptData {
    /** ロガー */
    private static final Logger LOG = LogManager.getLogger("script");

    private static Map<String, Object> dataMap = new HashMap<>();

    private static boolean modified = false;

    // 始めてアクセスがあった時に読み込む
    static {
        try {
            load();
        } catch (IOException e) {
            LOG.warn("スクリプトデータ読み込みに失敗しました", e);
        }
    }

    /**
     * 指定されたキーでデータを保存
     * スクリプトのオブジェクト（連想配列や配列など）はシリアライズできないため保存できません。
     * オブジェクトはJSON.stringify()であらかじめシリアライズしておいてください。
     * （復元はJSON.parse()でできます）
     * @param key データのキー
     * @param value データ
     */
    public static void setData(String key, Object value) {
        dataMap.put(key, value);
        modified = true;
    }

    /**
     * 指定されたキーでデータを取得
     * @param key データのキー
     * @return データ
     */
    public static Object getData(String key) {
        return dataMap.get(key);
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
        try (ZipOutputStream zos = new ZipOutputStream(
                new FileOutputStream(AppConstants.SCRIPT_DATA_FILE)))
        {
            for (Map.Entry<String, Object> entry : dataMap.entrySet()) {
                try {
                    ZipEntry zipentry = new ZipEntry(entry.getKey());
                    zos.putNextEntry(zipentry);
                    ObjectOutputStream oos = new ObjectOutputStream(zos);
                    oos.writeObject(entry.getValue());
                    oos.flush();
                } catch (IOException e) {
                    LOG.warn("データの保存に失敗", e);
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
        //
        try (ZipInputStream zis = new ZipInputStream(
                new FileInputStream(AppConstants.SCRIPT_DATA_FILE)))
        {
            for (ZipEntry entry = zis.getNextEntry(); entry != null; entry = zis.getNextEntry()) {
                try {
                    String key = entry.getName();
                    dataMap.put(key, new ObjectInputStream(zis).readObject());
                } catch (ClassNotFoundException e) {
                    LOG.warn("データの読み込みに失敗", e);
                } catch (IOException e) {
                    LOG.warn("データの読み込みに失敗", e);
                }
            }
        }
    }
}
