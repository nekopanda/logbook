package logbook.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import logbook.constants.AppConstants;
import logbook.dto.ItemDto;
import logbook.internal.Item;
import logbook.util.BeanUtils;

/**
 * 装備マスターを保存・復元します
 * 
 */
public class ItemMasterConfig {

    /**
     * 設定ファイルに書き込みます
     */
    public static void store() throws IOException {
        Map<String, ItemDto> map = new HashMap<String, ItemDto>();
        for (String id : Item.keySet()) {
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
        Map<String, ItemDto> map = BeanUtils.readObject(AppConstants.ITEM_MST_CONFIG_FILE, Map.class);
        if (map != null) {
            for (Entry<String, ItemDto> entry : map.entrySet()) {
                Item.set(entry.getKey(), entry.getValue());
            }
        }
    }
}
