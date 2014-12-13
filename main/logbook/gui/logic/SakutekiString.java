/**
 * 
 */
package logbook.gui.logic;

import java.util.List;

import logbook.config.AppConfig;
import logbook.dto.ItemInfoDto;
import logbook.dto.ShipBaseDto;

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

    // 初期の計算式: 偵察機×2 + 電探 + √(装備込みの艦隊索敵値合計-偵察機-電探)
    private double calc25;
    // 2014年秋までの調査結果に基く新計算式
    private double calc25v2;
    // 2014年秋までの調査結果に基く新計算式 - 艦素索敵分
    private double v2Ship = 0;
    // 2014年秋までの調査結果に基く新計算式 - 装備アイテム分
    private double v2Item = 0;
    // 2014年秋までの調査結果に基く新計算式 - 提督レベル分
    private double v2HqLv = 0;

    // 情報が不足してて計算できなかった
    private boolean sakutekiFailed = false;

    public <SHIP extends ShipBaseDto> SakutekiString(List<SHIP> ships, int hqLv) {
        for (SHIP ship : ships) {
            this.add(ship);
            this.addToCalcV2(ship);
        }
        this.addToCalcV2(hqLv);
        this.calc();
    }

    public SakutekiString(ShipBaseDto ship) {
        this.add(ship);
        this.addToCalcV2(ship);
        this.calc();
    }

    private void add(ShipBaseDto ship) {
        List<ItemInfoDto> items = ship.getItem();
        int[] onslot = ship.getOnSlot();
        // 装備込の索敵値計
        this.totalSakuteki += ship.getSakuteki();
        for (int i = 0; i < items.size(); i++) {
            ItemInfoDto item = items.get(i);
            if (item != null) {
                // 装備の索敵値
                this.slotSakuteki += item.getParam().getSaku();
                if (item.getType1() == 0) { // 取得できていない
                    this.sakutekiFailed = true;
                }
                if ((item.getType1() == 7) && (onslot[i] > 0)) { // 7: 偵察機 (搭載数>0の場合のみ)
                    this.teisatsuSakuteki += item.getParam().getSaku();
                }
                if (item.getType1() == 8) { // 8: 電探
                    this.dentanSakuteki += item.getParam().getSaku();
                }
            }
        }
    }

    private void addToCalcV2(ShipBaseDto ship) {
        List<ItemInfoDto> items = ship.getItem();
        int shipSakuteki = ship.getSakuteki();
        int kanbaku = 0;
        int kanko = 0;
        int kantei = 0;
        int suitei = 0;
        int suibaku = 0;
        int kogataDentan = 0;
        int oogataDentan = 0;
        int tansyouto = 0;
        for (int i = 0; i < items.size(); i++) {
            ItemInfoDto item = items.get(i);
            if (item != null) {
                // 装備の索敵値
                shipSakuteki -= item.getParam().getSaku();
                if (item.getType2() == 0) { // 取得できていない
                    this.sakutekiFailed = true;
                }
                int saku = item.getParam().getSaku();
                switch (item.getType2()) {
                case 7: // 艦上爆撃機
                    kanbaku += saku;
                    break;
                case 8: // 艦上攻撃機
                    kanko += saku;
                    break;
                case 9: // 艦上偵察機
                    kantei += saku;
                    break;
                case 10: // 水上偵察機
                    suitei += saku;
                    break;
                case 11: // 水上爆撃機
                    suibaku += saku;
                    break;
                case 12: // 小型電探
                    kogataDentan += saku;
                    break;
                case 13: // 大型電探
                    oogataDentan += saku;
                    break;
                case 29: // 探照灯
                    tansyouto += saku;
                    break;
                default:
                    break;
                }
            }
        }
        this.v2Item += (kanbaku * (1.0376255)) + (kanko * (1.3677954)) + (kantei * (1.6592780))
                + (suitei * (2.0000000)) + (suibaku * (1.7787282)) + (kogataDentan * (1.0045358))
                + (oogataDentan * (0.9906638)) + (tansyouto * (0.9067950));
        this.v2Ship += (Math.sqrt(shipSakuteki) * (1.6841056));
    }

    private void addToCalcV2(int hqLv) {
        if (hqLv == 0)
            this.v2HqLv = 0;
        else
            this.v2HqLv = ((hqLv / 5) + 1) * 5 * (-0.6142467);
    }

    private void calc() {
        double base = Math.sqrt(this.totalSakuteki - this.teisatsuSakuteki - this.dentanSakuteki);
        this.calc25 = (this.teisatsuSakuteki * 2) + this.dentanSakuteki + base;
        this.calc25v2 = this.v2Ship + this.v2Item + this.v2HqLv;
    }

    public double getValue() {
        int method = AppConfig.get().getSakutekiMethod();
        switch (method) {
        case 0:
            return this.totalSakuteki;
        case 1:
        case 2:
            return this.calc25v2;
        case 3:
        case 4:
            return this.calc25;
        default:
            return 0;
        }
    }

    @Override
    public String toString() {
        double base = Math.sqrt(this.totalSakuteki - this.teisatsuSakuteki - this.dentanSakuteki);
        int method = AppConfig.get().getSakutekiMethod();
        if ((method != 0) && this.sakutekiFailed) {
            return "<ゲーム画面をリロードしてください>";
        }
        switch (method) {
        case 0: // 艦隊素の索敵値 + 装備の索敵値
            return String.format("%d+%d",
                    this.totalSakuteki - this.slotSakuteki, this.slotSakuteki);
        case 1: // 2014秋2-5計算式(艦素索敵分 + 装備分 + 提督Lv分)
            return String.format("%.1f (%.1f%+.1f%+.1f)", this.calc25v2, this.v2Ship, this.v2Item, this.v2HqLv);
        case 2: // 装備込みの艦隊索敵値合計(2014秋2-5計算式)
            return String.format("%d (%.1f)", this.totalSakuteki, this.calc25v2);
        case 3: // 右の計算結果(偵察機×2 + 電探 + √(装備込みの艦隊索敵値合計-偵察機-電探))
            return String.format("%.1f (%d+%d+%.1f)",
                    this.calc25, this.teisatsuSakuteki * 2, this.dentanSakuteki, base);
        case 4: // 装備込みの艦隊索敵値合計(Bの計算結果)
            return String.format("%d (%.1f)", this.totalSakuteki, this.calc25);
        }
        return Integer.toString(this.totalSakuteki);
    }

    @Override
    public int compareTo(SakutekiString o) {
        return Double.compare(this.getValue(), o.getValue());
    }
}
