package logbook.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.dto.ItemDto;
import logbook.util.BeanUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 装備のIDと装備IDの紐付けを保存・復元します
 * 
 */
public class ItemConfig {
    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(ItemConfig.class);

    /**
     * 設定ファイルに書き込みます
     */
    public static void store() throws IOException {
        Map<Integer, Integer> map = new HashMap<Integer, Integer>();
        for (Entry<Integer, ItemDto> entry : GlobalContext.getItemMap().entrySet()) {
            map.put(entry.getKey(), entry.getValue().getId());
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
        try {
            Map<Integer, Integer> map = BeanUtils.readObject(AppConstants.ITEM_CONFIG_FILE, Map.class);
            if ((map != null) && (map.size() > 0)) {
                GlobalContext.setItemMap(map);
            }
        } catch (Exception e) {
            LOG.warn("艦娘のIDと名前の紐付けを設定ファイルから読み込みますに失敗しました", e);
        }
    }
}
