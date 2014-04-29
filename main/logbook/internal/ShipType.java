/**
 * 
 */
package logbook.internal;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import logbook.dto.ShipTypeDto;

/**
 * @author iedeg_000
 *
 */
public class ShipType {

    private static final Map<Integer, ShipTypeDto> SHIPTYPE = new ConcurrentHashMap<Integer, ShipTypeDto>();

    /**
     * 艦娘を取得します
     * 
     * @param id ID
     * @return 艦娘
     */
    public static ShipTypeDto get(Integer id) {
        return SHIPTYPE.get(id);
    }

    /**
     * 艦娘を設定します
     */
    public static void set(Integer id, ShipTypeDto item) {
        SHIPTYPE.put(id, item);
    }

    /**
     * IDの一覧を取得します
     * 
     * @return IDの一覧
     */
    public static Set<Integer> keySet() {
        return SHIPTYPE.keySet();
    }
}
