/**
 * 
 */
package logbook.config;

import java.io.IOException;

import logbook.constants.AppConstants;
import logbook.internal.MasterData;
import logbook.util.BeanUtils;

/**
 * @author Nekopanda
 *
 */
public class MasterDataConfig {

    public static MasterData instance = null;

    static {
        load();
    }

    public static MasterData get() {
        if (instance == null) {
            instance = new MasterData();
        }
        return instance;
    }

    /**
     * 設定ファイルに書き込みます
     */
    public static void store() throws IOException {
        BeanUtils.writeObject(AppConstants.MASTER_DATA_CONFIG, instance);
    }

    /**
     * 艦種情報を設定ファイルから読み込みます
     * 
     * @param properties
     * @return
     */
    public static void load() {
        MasterData masterData = BeanUtils.readObject(AppConstants.MASTER_DATA_CONFIG, MasterData.class);
        if (masterData != null) {
            instance = masterData;
        }
    }
}
