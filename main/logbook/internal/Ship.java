package logbook.internal;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import logbook.dto.ShipInfoDto;
import logbook.dto.ShipParameters;

import org.apache.commons.lang3.StringUtils;

/**
 * 艦娘
 *
 */
public class Ship {

    /**
     * 艦娘プリセット値
     */
    private static final Map<Integer, ShipInfoDto> SHIP = new ConcurrentHashMap<Integer, ShipInfoDto>();

    /**
     * 艦娘を取得します
     * 
     * @param id ID
     * @return 艦娘
     */
    public static ShipInfoDto get(int id) {
        ShipInfoDto dto = MasterData.get().getStart2().getShips().get(id);
        if (dto == null) {
            dto = SHIP.get(id);
            if (dto == null) {
                dto = ShipInfoDto.EMPTY;
            }
        }
        return dto;
    }

    public static Map<Integer, ShipInfoDto> getMap() {
        return MasterData.get().getStart2().getShips();
    }

    /** 敵艦名からの変換マップ */
    public static Map<String, ShipInfoDto> getEnemyNameMap() {
        Map<String, ShipInfoDto> nameMap = new HashMap<>();
        for (ShipInfoDto ship : getMap().values()) {
            nameMap.put(ship.getFullName(), ship);
        }
        return nameMap;
    }

    public static void dumpCSV(OutputStreamWriter fw) throws IOException {
        fw.write(StringUtils.join(new String[] {
                "名前", "ID", "艦種", "速力", "耐久", "燃料", "弾薬",
                "火力", "火力(max)", "雷装", "雷装(max)", "対空", "対空(max)", "装甲", "装甲(max)", "射程", "運", "運(max)",
                "搭載機数1", "搭載機数2", "搭載機数3", "搭載機数4" },
                ','));
        fw.write("\n");

        for (int key : getMap().keySet()) {
            ShipInfoDto dto = Ship.get(key);
            ShipParameters param = dto.getParam();
            ShipParameters max = dto.getMax();
            if (dto.getName().length() > 0) {
                String name = dto.getName();
                if (dto.isEnemy()) { // 敵
                    if (!StringUtils.isEmpty(dto.getFlagship())) {
                        name += " " + dto.getFlagship();
                    }
                }
                int[] maxeq = dto.getMaxeq2();
                fw.write(StringUtils.join(new String[] {
                        name, // 名前
                        Integer.toString(dto.getShipId()), // ID
                        dto.getType(), // 
                        Integer.toString(dto.getMax().getSoku()),
                        Integer.toString(dto.getMax().getHP()),
                        Integer.toString(dto.getMaxFuel()),
                        Integer.toString(dto.getMaxBull()),
                        Integer.toString(param.getHoug()),
                        Integer.toString(max.getHoug()),
                        Integer.toString(param.getRaig()),
                        Integer.toString(max.getRaig()),
                        Integer.toString(param.getTyku()),
                        Integer.toString(max.getTyku()),
                        Integer.toString(param.getSouk()),
                        Integer.toString(max.getSouk()),
                        Integer.toString(param.getLeng()),
                        Integer.toString(param.getLuck()),
                        Integer.toString(max.getLuck()),
                        (maxeq != null) ? Integer.toString(maxeq[0]) : "?",
                        (maxeq != null) ? Integer.toString(maxeq[1]) : "?",
                        (maxeq != null) ? Integer.toString(maxeq[2]) : "?",
                        (maxeq != null) ? Integer.toString(maxeq[3]) : "?" }, ','));
                fw.write("\n");
            }
        }
    }
}
