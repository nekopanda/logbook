/**
 * 
 */
package logbook.gui.widgets;

import java.util.List;

import logbook.config.AppConfig;
import logbook.dto.ItemDto;
import logbook.dto.ShipDto;

/**
 * @author Nekopanda
 *
 */
public class FleetStatus {
    private final List<ShipDto> ships;
    // 装備込の索敵値計
    int totalSakuteki = 0;
    // 装備の索敵値計
    int slotSakuteki = 0;
    // 偵察機の索敵値計
    int teisatsuSakuteki = 0;
    // 電探の索敵値計
    int dentanSakuteki = 0;

    public FleetStatus(List<ShipDto> ships) {
        this.ships = ships;
        int totalSakuteki = 0;
        int slotSakuteki = 0;
        int teisatsuSakuteki = 0;
        int dentanSakuteki = 0;
        for (ShipDto ship : ships) {
            List<ItemDto> items = ship.getItem();
            List<Integer> onslot = ship.getOnSlot();
            // 装備込の索敵値計
            totalSakuteki += ship.getSakuteki();
            for (int i = 0; i < 4; i++) {
                ItemDto item = items.get(i);
                if (item != null) {
                    // 装備の索敵値
                    slotSakuteki += item.getSaku();
                    if ((item.getType1() == 7) && (onslot.get(i) > 0)) { // 7: 偵察機 (搭載数>0の場合のみ)
                        teisatsuSakuteki += item.getSaku();
                    }
                    if (item.getType1() == 8) { // 8: 電探
                        dentanSakuteki += item.getSaku();
                    }
                }
            }
        }
        this.totalSakuteki = totalSakuteki;
        this.slotSakuteki = slotSakuteki;
        this.teisatsuSakuteki = teisatsuSakuteki;
        this.dentanSakuteki = dentanSakuteki;
    }

    public String getSakuteki() {
        double base = Math.sqrt(this.totalSakuteki - this.teisatsuSakuteki - this.dentanSakuteki);
        double b = (this.teisatsuSakuteki * 2) + this.dentanSakuteki + base;
        switch (AppConfig.get().getSakutekiMethod()) {
        case 0: // 艦隊素の索敵値 + 装備の索敵値
            return String.format("%d+%d",
                    this.totalSakuteki - this.slotSakuteki, this.slotSakuteki);
        case 1: // 右の計算結果(偵察機×2 + 電探 + √(装備込みの艦隊索敵値合計-偵察機-電探))
            return String.format("%.1f (%d+%d+%.1f)",
                    b, this.teisatsuSakuteki * 2, this.dentanSakuteki, base);
        case 2: // 装備込みの艦隊索敵値合計(Bの計算結果)
            return String.format("%d (%.1f)", this.totalSakuteki, b);
        }
        return Integer.toString(this.totalSakuteki);
    }
}
