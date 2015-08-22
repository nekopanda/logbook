/**
 * 
 */
package logbook.gui.logic;

import java.util.List;

import logbook.config.AppConfig;
import logbook.dto.ItemDto;
import logbook.dto.ShipBaseDto;

/**
 * @author Nekopanda
 *
 */
public class SeikuString implements Comparable<SeikuString> {
    // 従来の数値
    private double seikuBase;
    // 熟練度によるボーナス
    private double seikuBonus;

    // 情報が不足してて計算できなかった
    private boolean seikuFailed = false;

    public <SHIP extends ShipBaseDto> SeikuString(List<SHIP> ships) {
        for (SHIP ship : ships) {
            this.add(ship);
        }
    }

    public SeikuString(ShipBaseDto ship) {
        this.add(ship);
    }

    private static double calcBonusSeiku(double factor, int alv) {
        return factor * ((0.3 * alv * alv) + (1.5 * alv));
    }

    private void add(ShipBaseDto ship) {
        List<ItemDto> items = ship.getItem2();
        for (int i = 0; i < items.size(); i++) {
            ItemDto item = items.get(i);
            if (item != null) {
                switch (item.getType2()) {
                case 6: // 艦上戦闘機
                case 7: // 艦上爆撃機
                case 8: // 艦上攻撃機
                case 11: // 瑞雲系の水上偵察機

                    // スロット数が分からないと計算できない
                    if (ship.getOnSlot() == null) {
                        this.seikuFailed = true;
                        return;
                    }

                    // 基本制空値
                    this.seikuBase += item.getParam().getTyku() * Math.sqrt(ship.getOnSlot()[i]);

                    switch (item.getType2()) {
                    case 6: // 艦上戦闘機
                        this.seikuBonus += calcBonusSeiku(1, item.getAlv());
                        break;
                    case 7: // 艦上爆撃機
                    case 8: // 艦上攻撃機
                    case 11: // 瑞雲系の水上偵察機
                        if (item.getParam().getTaiku() > 0) {
                            this.seikuBonus += calcBonusSeiku(0.16, item.getAlv());
                        }
                        else {
                            this.seikuBonus += calcBonusSeiku(0.12, item.getAlv());
                        }
                        break;
                    }

                    break;
                }
            }
        }
    }

    public double getValue() {
        int method = AppConfig.get().getSeikuMethod();
        switch (method) {
        case 0:
            return Math.floor(this.seikuBase);
        case 1:
        case 2:
            return Math.floor(this.seikuBase + this.seikuBonus);
        default:
            return 0;
        }
    }

    @Override
    public String toString() {
        int method = AppConfig.get().getSeikuMethod();
        if (this.seikuFailed) {
            return "?";
        }
        int total = (int) Math.floor(this.seikuBase + this.seikuBonus);
        switch (method) {
        case 0: // 艦載機素の制空値
            return Integer.toString((int) Math.floor(this.seikuBase));
        case 1: // 熟練度込みの制空推定値
            return Integer.toString(total);
        case 2: // 熟練度込みの制空推定値(艦載機素の制空値 + 熟練度ボーナス推定値)
            return String.format("%d (%.1f+%.1f)",
                    total, this.seikuBase, this.seikuBonus);
        }
        return Integer.toString(total);
    }

    @Override
    public int compareTo(SeikuString o) {
        return Double.compare(this.getValue(), o.getValue());
    }
}
