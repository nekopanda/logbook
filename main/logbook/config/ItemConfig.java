package logbook.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
        List<ItemDto> tmp = new ArrayList<ItemDto>(GlobalContext.getItemMap().values());
        BeanUtils.writeObject(AppConstants.ITEM_CONFIG_FILE, tmp);
    }

    /**
     * 艦娘のIDと名前の紐付けを設定ファイルから読み込みます
     * 
     * @param properties
     * @return
     */
    public static void load() {
        try {
            Collection<ItemDto> list = BeanUtils.readObject(AppConstants.ITEM_CONFIG_FILE, Collection.class);
            if ((list != null) && (list.size() > 0)) {
                GlobalContext.setItemMap(list);
            }
        } catch (Exception e) {
            LOG.warn("艦娘のIDと名前の紐付けを設定ファイルから読み込みますに失敗しました", e);
        }
    }
}
