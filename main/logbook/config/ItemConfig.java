package logbook.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.dto.ItemDto;
import logbook.util.BeanUtils;

/**
 * 装備のIDと装備IDの紐付けを保存・復元します
 * 
 */
public class ItemConfig {

    /**
     * 設定ファイルに書き込みます
     */
    public static void store() throws IOException {
        Map<Long, String> map = new HashMap<Long, String>();
        for (Entry<Long, ItemDto> entry : GlobalContext.getItemMap().entrySet()) {
            map.put(entry.getKey(), Long.toString(entry.getValue().getId()));
        }
        BeanUtils.writeObject(AppConstants.ITEM_CONFIG_FILE, map);
    }

    /**
     * 艦娘のIDと名前の紐付けを設定ファイルから読み込みます
     * 
     * @param properties
     * @return
     */
    public static void load() {
        Map<Long, String> map = BeanUtils.readObject(AppConstants.ITEM_CONFIG_FILE, Map.class);
        if (map != null) {
            GlobalContext.setItemMap(map);
        }
    }
}
