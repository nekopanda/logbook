/**
 * 
 */
package logbook.config;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import logbook.constants.AppConstants;
import logbook.dto.ShipTypeDto;
import logbook.internal.ShipType;
import logbook.util.BeanUtils;

/**
 * @author iedeg_000
 *
 */
public class ShipTypeConfig {

    /**
     * 設定ファイルに書き込みます
     */
    public static void store() throws IOException {
        Set<Integer> itemids = ShipType.keySet();
        Map<Integer, ShipTypeDto> map = new HashMap<Integer, ShipTypeDto>();
        for (Integer key : itemids) {
            ShipTypeDto item = ShipType.get(key);
            map.put(key, item);
        }
        BeanUtils.writeObject(AppConstants.SHIPTYPE_CONFIG_FILE, map);
    }

    /**
     * 艦種情報を設定ファイルから読み込みます
     * 
     * @param properties
     * @return
     */
    public static void load() {
        Map<Integer, ShipTypeDto> map = BeanUtils.readObject(AppConstants.SHIPTYPE_CONFIG_FILE, Map.class);
        if (map != null) {
            for (Entry<Integer, ShipTypeDto> entry : map.entrySet()) {
                ShipType.set(entry.getKey(), entry.getValue());
            }
        }
    }
}
