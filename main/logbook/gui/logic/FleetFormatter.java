package logbook.gui.logic;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import logbook.data.context.GlobalContext;
import logbook.dto.ShipDto;
import logbook.dto.ShipInfoDto;
import logbook.internal.Ship;

/**
 * @author Nishisonic
 *
 */
public class FleetFormatter {
    /**
     * 艦隊晒しのフォーマットを取得します
     *
     * @param isLockedOnly ロックしている艦限定にするか
     * @return フォーマット
     */
    public String get(boolean isLockedOnly) {
        String prefix = ".2";
        StringBuilder result = new StringBuilder(prefix);
        Map<Integer, StringBuilder> format = isLockedOnly ? this.getLockedOnlyFormat() : this.getFreeFormat();
        format.forEach((id, value) -> {
            result.append("|");
            result.append(id);
            result.append(":");
            result.append(value);
        });
        return result.toString();
    }

    private Map<Integer, StringBuilder> getLockedOnlyFormat() {
        Map<Integer, StringBuilder> format = new HashMap<Integer, StringBuilder>();
        GlobalContext.getShipMap().values().stream().sorted(Comparator.comparing(ShipDto::getLv).reversed())
                .filter(ShipDto::getLocked).forEach(ship -> {
                    int charId = ship.getCharId();
                    int lv = ship.getLv();
                    int shipId = ship.getShipId();
                    int lvSuffix = this.getLvSuffix(charId, shipId);

                    if (format.containsKey(charId)) {
                        format.put(charId, format.get(charId).append(","));
                    } else {
                        format.put(charId, new StringBuilder());
                    }
                    format.put(charId, format.get(charId).append(lv + "." + lvSuffix));
                });
        return format;
    }

    private Map<Integer, StringBuilder> getFreeFormat() {
        Map<Integer, StringBuilder> format = new HashMap<Integer, StringBuilder>();
        GlobalContext.getShipMap().values().stream().sorted(Comparator.comparing(ShipDto::getLv).reversed())
                .forEach(ship -> {
                    int charId = ship.getCharId();
                    int lv = ship.getLv();
                    int shipId = ship.getShipId();
                    int lvSuffix = this.getLvSuffix(charId, shipId);

                    if (format.containsKey(charId)) {
                        format.put(charId, format.get(charId).append(","));
                    } else {
                        format.put(charId, new StringBuilder());
                    }
                    format.put(charId, format.get(charId).append(lv + "." + lvSuffix));
                });
        return format;
    }

    private int lvSuffix(ShipInfoDto shipinfo, int id, int count) {
        count++;
        if (shipinfo.getShipId() == id) {
            return count;
        }
        return this.lvSuffix(Ship.get(shipinfo.getAftershipid()), id, count);

    }

    private int getLvSuffix(int charId, int shipId) {
        int count = 0;
        return this.lvSuffix(Ship.get(charId), shipId, count);
    }
}
