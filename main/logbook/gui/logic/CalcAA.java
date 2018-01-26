package logbook.gui.logic;

import logbook.dto.ItemDto;
import logbook.dto.ShipBaseDto;
import logbook.dto.ShipDto;
import logbook.dto.ShipParameters;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 対空関連を扱うクラス。
 * 艦これ改+検証内容をミックスしたものになっているため、wikiの計算方法とは異なります。
 *
 * @author Nishisonic
 * @see <a href="http://wikiwiki.jp/kancolle/?%B9%D2%B6%F5%C0%EF">wiki 航空戦</a>
 * @see <a href="https://twitter.com/KennethWWKK/status/780793591387959297">艦これ改のソフトって解析翻訳</a>
 */
public class CalcAA {

    /**
     * 航空戦定数
     */
    private static final double AIR_BATTLE_FACTOR = 0.25;

    /**
     * 対空CIボーナス
     */
    private final Map<Integer, AA_CI> AA_CI_BONUS = new LinkedHashMap<Integer, AA_CI>() {
        {
            this.put(1, new AA_CI(7, 1.75));  // 高角砲x2/電探(秋月型)
            this.put(2, new AA_CI(6, 1.7));   // 高角砲/電探(秋月型)
            this.put(3, new AA_CI(4, 1.6));   // 高角砲x2(秋月型)
            this.put(4, new AA_CI(6, 1.5));   // 大口径主砲/三式弾/高射装置/対空電探
            this.put(5, new AA_CI(4, 1.55));  // 高角砲+高射装置x2/対空電探
            this.put(6, new AA_CI(4, 1.5));   // 大口径主砲/三式弾/高射装置
            this.put(7, new AA_CI(3, 1.35));  // 高角砲/高射装置/対空電探
            this.put(8, new AA_CI(4, 1.45));  // 高角砲+高射装置/対空電探
            this.put(9, new AA_CI(2, 1.3));   // 高角砲/高射装置
            this.put(10, new AA_CI(8, 1.65)); // 高角砲/特殊機銃/対空電探(摩耶改二)
            this.put(11, new AA_CI(6, 1.5));  // 高角砲/特殊機銃(摩耶改二)
            this.put(12, new AA_CI(3, 1.25)); // 特殊機銃/機銃(非特殊機銃)/電探
            this.put(13, new AA_CI(4, 1.35)); // 高角砲/特殊機銃/電探(摩耶改二不可)
            this.put(14, new AA_CI(4, 1.45)); // 高角砲/機銃/対空電探(五十鈴改二)
            this.put(15, new AA_CI(3, 1.3));  // 高角砲/機銃(五十鈴改二)
            this.put(16, new AA_CI(4, 1.4));  // 高角砲/機銃/対空電探(霞改二乙)
            this.put(17, new AA_CI(2, 1.25)); // 高角砲/機銃(霞改二乙)
            this.put(18, new AA_CI(2, 1.2));  // 特殊機銃(皐月改二)
            this.put(19, new AA_CI(5, 1.45)); // 高角砲(非高射装置)/特殊機銃(鬼怒改二)
            this.put(20, new AA_CI(3, 1.25)); // 特殊機銃(鬼怒改二)
            this.put(21, new AA_CI(5, 1.45)); // 高角砲/対空電探(由良改二)
            this.put(22, new AA_CI(2, 1.2));  // 特殊機銃(文月改二)
            this.put(23, new AA_CI(1, 1.05)); // 機銃(非特殊機銃)(UIT-25/伊504)
            this.put(24, new AA_CI(3, 1.25)); // 高角砲/機銃(非特殊機銃)(龍田改二)
        }
    };

    /**
     * 最低保証数を返します。
     *
     * @param isFriend 味方か
     * @param ciKind   対空CI種別
     * @return 最低保証数
     */
    public int getMinShotDown(boolean isFriend, int ciKind) {
        if (ciKind > 0 && ciKind <= AA_CI_BONUS.size()) {
            return AA_CI_BONUS.get(ciKind).getFixedBonus() + 1;
        }
        return isFriend ? 1 : 0;
    }

    /**
     * 割合撃墜(%)を返します。
     *
     * @param ship       艦娘
     * @param isFriend   味方か
     * @param isCombined 連合艦隊か
     * @param isSecond   第二艦隊か(連合艦隊時使用)
     * @return 加重対空値
     */
    public <SHIP extends ShipBaseDto> double getPropShotDown(SHIP ship, boolean isFriend, boolean isCombined, boolean isSecond) {
        return 0.02 * AIR_BATTLE_FACTOR * this.getWeightedAirValue(ship, isFriend) * this.getCombinedBonus(isFriend, isCombined, isSecond);
    }

    /**
     * 固定撃墜を返します。
     *
     * @param ship          艦娘
     * @param ships         全艦隊艦娘(通常はそれぞれ出撃している艦隊、連合の場合は第一+第二)
     * @param isFriend      味方か
     * @param isCombined    連合艦隊か
     * @param isSecond      第二艦隊か(連合艦隊時使用)
     * @param formationKind 陣形
     * @param ciKind        対空CI種別
     * @return 固定撃墜
     */
    public <SHIP extends ShipBaseDto> int getFixedShotDown(SHIP ship, List<SHIP> ships, boolean isFriend, boolean isCombined, boolean isSecond, int formationKind, int ciKind) {
        return (int) (this.getFinalWeightedAirValue(ship, ships, isFriend, formationKind)
                * (ciKind > 0 && ciKind <= AA_CI_BONUS.size() ? AA_CI_BONUS.get(ciKind).getVariableBonus() : 1)
                * this.getCombinedBonus(isFriend, isCombined, isSecond));
    }

    /**
     * 最終加重対空値を返します。
     *
     * @param ship          艦娘
     * @param ships         全艦隊艦娘(通常はそれぞれ出撃している艦隊、連合の場合は第一+第二)
     * @param isFriend      味方か
     * @param formationKind 陣形
     * @return 最終加重対空値
     */
    public <SHIP extends ShipBaseDto> double getFinalWeightedAirValue(SHIP ship, List<SHIP> ships, boolean isFriend, int formationKind) {
        double weighted = this.getWeightedAirValue(ship, isFriend);
        int fleet = this.getFleetAirDefenseValue(ships, isFriend, formationKind);
        return (weighted + fleet) * AIR_BATTLE_FACTOR * (isFriend ? 0.8 : 0.75);
    }

    /**
     * 連合艦隊補正を返します。
     *
     * @param isFriend   味方か
     * @param isCombined 連合艦隊か
     * @param isSecond   第二艦隊か(連合艦隊時使用)
     * @return 連合艦隊補正
     */
    private double getCombinedBonus(boolean isFriend, boolean isCombined, boolean isSecond) {
        if (isCombined) {
            if (isFriend) {
                return !isSecond ? 0.72 : 0.48;
            }
            return !isSecond ? 0.8 : 0.48;
        }
        return 1.0;
    }

    /**
     * 加重対空値を返します。
     *
     * @param ship     艦娘
     * @param isFriend 味方か
     * @return 加重対空値
     */
    public <SHIP extends ShipBaseDto> double getWeightedAirValue(SHIP ship, boolean isFriend) {
        List<ItemDto> items = this.getShipAllItems(ship, isFriend);
        int shipAA = ship.getTaiku() - items.stream().map(ItemDto::getParam).mapToInt(ShipParameters::getTaiku).sum();
        return (isFriend ? (shipAA / 2.0) : Math.floor(Math.sqrt(ship.getTaiku()))) + this.getItemsWeightedAirBonus(items);
    }

    /**
     * 艦隊防空値を返します。
     *
     * @param ships         全艦隊艦娘(通常はそれぞれ出撃している艦隊、連合の場合は第一+第二)
     * @param isFriend      味方か
     * @param formationKind 陣形
     * @return 艦隊防空値
     */
    public <SHIP extends ShipBaseDto> int getFleetAirDefenseValue(List<SHIP> ships, boolean isFriend, int formationKind) {
        int fleetBonus = ships.stream()
                .filter(Objects::nonNull)
                .map(ship -> this.getShipAllItems(ship, isFriend))
                .mapToInt(items -> (int) this.getItemsFleetAirDefenseBonus(items))
                .sum();
        return (int) (this.getFormationBonus(formationKind) * fleetBonus / (isFriend ? 1.3 : 1.0));
    }

    /**
     * 艦娘の全装備を返します。
     * nullの場合、削除されます。
     *
     * @param ship     艦娘
     * @param isFriend 味方か
     * @return 艦娘の全装備
     */
    private <SHIP extends ShipBaseDto> List<ItemDto> getShipAllItems(SHIP ship, boolean isFriend) {
        List<ItemDto> items = new ArrayList<>();
        items.addAll(ship.getItem2());
        if (isFriend) {
            items.add(((ShipDto) ship).getSlotExItem());
        }
        return items.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 装備合計加重対空値を返します。
     *
     * @param items 装備
     * @return 装備合計加重対空値
     */
    private double getItemsWeightedAirBonus(List<ItemDto> items) {
        // 丸め誤差対策
        return items.stream()
                .mapToDouble(item -> item.getParam().getTaiku() * this.getItemAirBonus(item) * 100 + this.getItemImprovementAirBonus(item) * Math.sqrt(item.getLevel()) * 100)
                .sum() / 100;
    }

    /**
     * 装備合計艦隊防空値を返します。
     *
     * @param items 装備
     * @return 装備合計艦隊防空値
     */
    private double getItemsFleetAirDefenseBonus(List<ItemDto> items) {
        // 丸め誤差対策
        return items.stream()
                .mapToDouble(item -> item.getParam().getTaiku() * this.getItemFleetAirBonus(item) * 100 + this.getItemImprovementFleetAirBonus(item) * Math.sqrt(item.getLevel()) * 100)
                .sum() / 100;
    }

    /**
     * 陣形補正を返します。
     *
     * @param formationKind 陣形
     * @return 陣形補正
     */
    private double getFormationBonus(int formationKind) {
        switch (formationKind) {
            case 1:
                return 1.0; // 単縦陣
            case 2:
                return 1.2; // 複縦陣
            case 3:
                return 1.6; // 輪形陣
            case 4:
                return 1.0; // 梯形陣
            case 5:
                return 1.0; // 単横陣
            case 6:
                return 1.1; // 警戒陣
            case 11:
                return 1.1; // 第一警戒航行序列
            case 12:
                return 1.0; // 第二警戒航行序列
            case 13:
                return 1.5; // 第三警戒航行序列
            case 14:
                return 1.0; // 第四警戒航行序列
        }
        return 1.0;
    }

    /**
     * 装備個艦防空定数を返します。
     *
     * @param item 装備
     * @return 個艦装備改修定数
     */
    private double getItemAirBonus(ItemDto item) {
        switch (item.getType3()) {
            case 16:    // 高角砲
            case 30:    // 高射装置
                return 2;
            case 15:    // 機銃
                return 3;
            case 11:    // 電探
                return 1.5;
        }
        return 0;
    }

    /**
     * 装備改修個艦防空定数を返します。
     *
     * @param item 装備
     * @return 個艦装備改修定数
     */
    private double getItemImprovementAirBonus(ItemDto item) {
        switch (item.getType3()) {
            case 16:    // 高角砲
            case 30:    // 高射装置
                return item.getParam().getTaiku() <= 7 ? 1 : 1.5;
            case 15:    // 機銃
                return item.getParam().getTaiku() <= 7 ? 2 : 3;
        }
        return 0;
    }

    /**
     * 装備艦隊防空定数を返します。
     *
     * @param item 装備
     * @return 艦隊防空装備定数
     */
    private double getItemFleetAirBonus(ItemDto item) {
        switch (item.getType3()) {
            case 16:    // 高角砲
            case 30:    // 高射装置
                return 0.35;
            case 12:    // 対空強化弾
                return 0.6;
            case 11:    // 電探
                return 0.4;
        }
        if (item.getSlotitemId() == 9) return 0.25;  // 46cm三連装砲
        return 0.2;
    }

    /**
     * 装備改修艦隊防空定数を返します。
     *
     * @param item 装備
     * @return 艦隊防空装備定数
     */
    private double getItemImprovementFleetAirBonus(ItemDto item) {
        switch (item.getType3()) {
            case 16:    // 高角砲
            case 30:    // 高射装置
                return item.getParam().getTaiku() <= 7 ? 2 : 3;
            case 11:    // 電探
                if (item.getParam().getTaiku() > 1) return 1.5;
        }
        return 0;
    }

    /**
     * 対空CI用クラス
     */
    private class AA_CI {

        final private double variable;
        final private int fixed;

        /**
         * @param fixed    対空CI固定ボーナス
         * @param variable 対空CI変動ボーナス
         */
        AA_CI(int fixed, double variable) {
            this.fixed = fixed;
            this.variable = variable;
        }

        /**
         * 対空CI変動ボーナスを返します。
         *
         * @return 対空CI変動ボーナス
         */
        public double getVariableBonus() {
            return this.variable;
        }

        /**
         * 対空CI固定ボーナスを返します。
         *
         * @return 対空CI固定ボーナス
         */
        public int getFixedBonus() {
            return this.fixed;
        }
    }
}
