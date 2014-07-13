package logbook.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import logbook.constants.AppConstants;
import logbook.dto.ItemDto;
import logbook.internal.Item;
import logbook.util.BeanUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 装備マスターを保存・復元します
 * 
 */
public class ItemMasterConfig {
    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(ItemMasterConfig.class);

    /**
     * 設定ファイルに書き込みます
     */
    public static void store() throws IOException {
        Map<Integer, ItemDto> map = new HashMap<Integer, ItemDto>();
        for (Integer id : Item.keySet()) {
            map.put(id, Item.get(id));
        }
        BeanUtils.writeObject(AppConstants.ITEM_MST_CONFIG_FILE, map);
    }

    /**
     * 装備マスターを設定ファイルから読み込みます
     * 
     * @param properties
     * @return
     */
    public static void load() {
        try {
            Map<Integer, ItemDto> map = BeanUtils.readObject(AppConstants.ITEM_MST_CONFIG_FILE, Map.class);
            if (map != null) {
                for (Entry<Integer, ItemDto> entry : map.entrySet()) {
                    Item.set(entry.getKey(), entry.getValue());
                }
            }
        } catch (Exception e) {
            LOG.warn("装備マスターを設定ファイルから読み込みますに失敗しました", e);
        }
    }
}
