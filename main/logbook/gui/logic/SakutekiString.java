/**
 * 
 */
package logbook.gui.logic;

import java.util.List;

import logbook.config.AppConfig;
import logbook.dto.ItemDto;
import logbook.dto.ShipBaseDto;
import logbook.dto.ShipDto;

/**
 * @author Nekopanda
 *
 */
public class SakutekiString implements Comparable<SakutekiString> {
    // 装備込の索敵値計
    private int totalSakuteki = 0;
    // 装備の索敵値計
    private int slotSakuteki = 0;
    // 偵察機の索敵値計
    private int teisatsuSakuteki = 0;
    // 電探の索敵値計
    private int dentanSakuteki = 0;

    private double calc25;
    // 情報が不足してて計算できなかった
    private boolean sakutekiFailed = false;

    public SakutekiString(List<ShipDto> ships) {
        for (ShipDto ship : ships) {
            this.add(ship);
        }
        this.calc();
    }

    public SakutekiString(ShipDto ship) {
        this.add(ship);
        this.calc();
    }

    private void add(ShipBaseDto ship) {
        List<ItemDto> items = ship.getItem();
        int[] onslot = ship.getOnSlot();
        // 装備込の索敵値計
        this.totalSakuteki += ship.getSakuteki();
        for (int i = 0; i < 4; i++) {
            ItemDto item = items.get(i);
            if (item != null) {
                // 装備の索敵値
                this.slotSakuteki += item.getSaku();
                if (item.getType1() == 0) { // 取得できていない
                    this.sakutekiFailed = true;
                }
                if ((item.getType1() == 7) && (onslot[i] > 0)) { // 7: 偵察機 (搭載数>0の場合のみ)
                    this.teisatsuSakuteki += item.getSaku();
                }
                if (item.getType1() == 8) { // 8: 電探
                    this.dentanSakuteki += item.getSaku();
                }
            }
        }
    }

    private void calc() {
        double base = Math.sqrt(this.totalSakuteki - this.teisatsuSakuteki - this.dentanSakuteki);
        this.calc25 = (this.teisatsuSakuteki * 2) + this.dentanSakuteki + base;
    }

    public double getValue() {
        if (AppConfig.get().getSakutekiMethod() == 0) {
            return this.totalSakuteki;
        }
        return this.calc25;
    }

    @Override
    public String toString() {
        double base = Math.sqrt(this.totalSakuteki - this.teisatsuSakuteki - this.dentanSakuteki);
        String failedMessage = "<ゲーム画面をリロードしてください>";
        switch (AppConfig.get().getSakutekiMethod()) {
        case 0: // 艦隊素の索敵値 + 装備の索敵値
            return String.format("%d+%d",
                    this.totalSakuteki - this.slotSakuteki, this.slotSakuteki);
        case 1: // 右の計算結果(偵察機×2 + 電探 + √(装備込みの艦隊索敵値合計-偵察機-電探))
            if (this.sakutekiFailed)
                return failedMessage;
            else
                return String.format("%.1f (%d+%d+%.1f)",
                        this.calc25, this.teisatsuSakuteki * 2, this.dentanSakuteki, base);
        case 2: // 装備込みの艦隊索敵値合計(Bの計算結果)
            if (this.sakutekiFailed)
                return failedMessage;
            else
                return String.format("%d (%.1f)", this.totalSakuteki, this.calc25);
        }
        return Integer.toString(this.totalSakuteki);
    }

    @Override
    public int compareTo(SakutekiString o) {
        return Double.compare(this.getValue(), o.getValue());
    }
}
