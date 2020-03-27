package logbook.gui.logic;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import logbook.data.context.GlobalContext;
import logbook.dto.ItemDto;

/**
 * @author
 *
 */
public class ItemFormatter {
    /**
     * 艦隊分析の装備フォーマットを取得します
     *
     * @param isLockedOnly ロックしている装備限定にするか
     * @return フォーマット
     */
    public String get(boolean isLockedOnly) {
        String prefix = "[";
        StringBuilder result = new StringBuilder(prefix);
        Map<Integer, StringBuilder> format = isLockedOnly ? this.getLockedOnlyFormat() : null;
        format.forEach((id, value) -> {
            result.append("{");
            result.append(value);
            result.append("},");
        });
        int length = result.length();
        result.replace(length-1, length,"]");
        return result.toString();
    }

    private Map<Integer, StringBuilder> getLockedOnlyFormat() {
        Map<Integer, StringBuilder> format = new HashMap<Integer, StringBuilder>();
        GlobalContext.getItemMap().values().stream().sorted(Comparator.comparing(ItemDto::getId).reversed())
                .filter(ItemDto::isLocked).forEach(Item -> {
                    int charId = Item.getId();
                    int slotitemId = Item.getSlotitemId();
                    int level = Item.getLevel();
                    if (format.containsKey(charId)) {
                        format.put(charId, format.get(charId).append(","));
                    } else {
                        format.put(charId, new StringBuilder());
                    }
                    format.put(charId, format.get(charId).append("\"api_slotitem_id\":" + slotitemId + ",\"api_level\":" + level));
                });
        return format;
    }
}
