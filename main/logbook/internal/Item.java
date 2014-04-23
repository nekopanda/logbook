package logbook.internal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import logbook.dto.ItemDto;

/**
 * アイテム
 *
 */
public class Item {

    /**
     * アイテムプリセット値
     */
    private static final Map<String, ItemDto> ITEM = new ConcurrentHashMap<String, ItemDto>() {
        {
        }
    };

    /**
     * アイテムを設定します
     */
    public static void set(String id, ItemDto item) {
        ITEM.put(id, item);
    }

    /**
     * アイテムを取得します
     * 
     * @param type ID
     * @return アイテム
     */
    public static ItemDto get(String type) {
        return ITEM.get(type);
    }
}
