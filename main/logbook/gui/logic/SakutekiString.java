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
    // 新秋式
    private double calc25v2;
    // 新秋式　- 艦素索敵分
    private double v2Ship = 0;
    // 新秋式- 装備アイテム分
    private double v2Item = 0;
    // 新秋式- 提督レベル分
    private double v2HqLv = 0;
    // 新秋式簡易
    private double calc25v3;
    // 新秋式簡易 - 艦素索敵分
    private double v3Ship = 0;
    // 新秋式簡易- 装備アイテム分
    private double v3Item = 0;
    // 新秋式簡易- 提督レベル分
    private double v3HqLv = 0;

    // 情報が不足してて計算できなかった
    private boolean sakutekiFailed = false;

    private static class ShipParam {
        public int sakuteki;
        public int kanbaku;
        public int kanko;
        public int kantei;
        public int suitei;
        public int suibaku;
        public int kogataDentan;
        public int oogataDentan;
        public int tansyouto;
        public int other;
        public boolean failed;

        ShipParam(ShipBaseDto ship) {
            List<ItemInfoDto> items = ship.getItem();
            this.sakuteki = ship.getSakuteki();
            this.kanbaku = 0;
            this.kanko = 0;
            this.kantei = 0;
            this.suitei = 0;
            this.suibaku = 0;
            this.kogataDentan = 0;
            this.oogataDentan = 0;
            this.tansyouto = 0;
            this.other = 0;
            boolean failed = false;
            for (int i = 0; i < items.size(); i++) {
                ItemInfoDto item = items.get(i);
                if (item != null) {
                    // 装備の索敵値
                    this.sakuteki -= item.getParam().getSaku();
                    if (item.getType2() == 0) { // 取得できていない
                        failed = true;
                    }
                    int saku = item.getParam().getSaku();
                    switch (item.getType2()) {
                    case 7: // 艦上爆撃機
                        this.kanbaku += saku;
                        break;
                    case 8: // 艦上攻撃機
                        this.kanko += saku;
                        break;
                    case 9: // 艦上偵察機
                        this.kantei += saku;
                        break;
                    case 10: // 水上偵察機
                        this.suitei += saku;
                        break;
                    case 11: // 水上爆撃機
                        this.suibaku += saku;
                        break;
                    case 12: // 小型電探
                        this.kogataDentan += saku;
                        break;
                    case 13: // 大型電探
                        this.oogataDentan += saku;
                        break;
                    case 29: // 探照灯
                        this.tansyouto += saku;
                        break;
                    default: // その他
                        this.other += saku;
                        break;
                    }
                }
            }
        }
    }

    public <SHIP extends ShipBaseDto> SakutekiString(List<SHIP> ships, int hqLv) {
        for (SHIP ship : ships) {
            ShipParam param = new ShipParam(ship);
            this.add(ship);
            this.addToCalcV2(param);
            this.addToCalcV3(param);
        }
        this.addToCalcV2(hqLv);
        this.addToCalcV3(hqLv);
        this.calc();
    }

    public SakutekiString(ShipBaseDto ship) {
        ShipParam param = new ShipParam(ship);
        this.add(ship);
        this.addToCalcV2(param);
        this.addToCalcV3(param);
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
                if ((item.getType1() == 7) && ((onslot != null) && (onslot[i] > 0))) { // 7: 偵察機 (搭載数>0の場合のみ)
                    this.teisatsuSakuteki += item.getParam().getSaku();
                }
                if (item.getType1() == 8) { // 8: 電探
                    this.dentanSakuteki += item.getParam().getSaku();
                }
            }
        }
    }

    private void addToCalcV2(ShipParam p) {
        this.v2Item += (p.kanbaku * (1.0376255))
                + (p.kanko * (1.3677954))
                + (p.kantei * (1.6592780))
                + (p.suitei * (2.0000000))
                + (p.suibaku * (1.7787282))
                + (p.kogataDentan * (1.0045358))
                + (p.oogataDentan * (0.9906638))
                + (p.tansyouto * (0.9067950));
        this.v2Ship += (Math.sqrt(p.sakuteki) * (1.6841056));
    }

    private void addToCalcV2(int hqLv) {
        if (hqLv == 0)
            this.v2HqLv = 0;
        else
            this.v2HqLv = Math.ceil((double) hqLv / 5) * 5 * (-0.6142467);
    }

    private void addToCalcV3(ShipParam p) {
        double fromItem = (p.kanbaku * 0.6)
                + (p.kanko * 0.8)
                + (p.kantei * 1.0)
                + (p.suitei * 1.2)
                + (p.suibaku * 1.0)
                + (p.kogataDentan * 0.6)
                + (p.oogataDentan * 0.6)
                + (p.tansyouto * 0.5)
                + (p.other * 0.5);
        double fromShip = Math.sqrt(p.sakuteki);

        this.calc25v3 += Math.floor(fromItem + fromShip);
        this.v3Item += fromItem;
        this.v3Ship += fromShip;
    }

    private void addToCalcV3(int hqLv) {
        if (hqLv == 0)
            this.v3HqLv = 0;
        else
            this.v3HqLv = -Math.floor(hqLv * 0.4);
    }

    private void calc() {
        double base = Math.sqrt(this.totalSakuteki - this.teisatsuSakuteki - this.dentanSakuteki);
        this.calc25 = (this.teisatsuSakuteki * 2) + this.dentanSakuteki + base;
        this.calc25v2 = this.v2Ship + this.v2Item + this.v2HqLv;
        this.calc25v3 += this.v3HqLv;
    }

    public double getValue() {
        int method = AppConfig.get().getSakutekiMethod();
        switch (method) {
        case 0:
            return this.totalSakuteki;
        case 1:
        case 2:
            return this.calc25v3;
        case 3:
        case 4:
            return this.calc25v2;
        case 5:
        case 6:
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
        case 1: // ほっぼアルファVer2.0.1(艦素索敵分 + 装備分 + 提督Lv分)
            return String.format("%d (%.1f%+.1f%+.1f)", (int) this.calc25v3, this.v3Ship, this.v3Item, this.v3HqLv);
        case 2: // ほっぼアルファVer2.0.1(旧:2-5式(秋))
            return String.format("%d (%.1f)", (int) this.calc25v3, this.calc25v2);
        case 3: // 2-5式(秋)(艦素索敵分 + 装備分 + 提督Lv分)
            return String.format("%.1f (%.1f%+.1f%+.1f)", this.calc25v2, this.v2Ship, this.v2Item, this.v2HqLv);
        case 4: // 装備込みの艦隊索敵値合計(2-5式(秋))
            return String.format("%d (%.1f)", this.totalSakuteki, this.calc25v2);
        case 5: // 2-5式(旧)(偵察機×2 + 電探 + √(装備込みの艦隊索敵値合計-偵察機-電探))
            return String.format("%.1f (%d+%d+%.1f)",
                    this.calc25, this.teisatsuSakuteki * 2, this.dentanSakuteki, base);
        case 6: // 装備込みの艦隊索敵値合計(2-5式(旧))
            return String.format("%d (%.1f)", this.totalSakuteki, this.calc25);
        }
        return Integer.toString(this.totalSakuteki);
    }

    @Override
    public int compareTo(SakutekiString o) {
        return Double.compare(this.getValue(), o.getValue());
    }
}
