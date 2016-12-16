/**
 *
 */
package logbook.gui.logic;

import java.util.List;

import logbook.dto.ItemDto;
import logbook.dto.ShipBaseDto;
import logbook.dto.ShipParameters;

/**
 * @author Nishisonic
 *
 */
public class CalcTaiku {

    /**
     * 加重対空値を返します。
     *
     * @param ship 艦娘
     * @return kajuu 加重対空値
     */
    public <SHIP extends ShipBaseDto> int getKajuuValue(SHIP ship) {
        // 装備
        List<ItemDto> items = ship.getItem2();

        // 艦娘の素の対空値
        int taiku = ship.getTaiku() - items.stream().filter(item -> item != null)
                .map(ItemDto::getParam)
                .mapToInt(ShipParameters::getTaiku)
                .sum();

        // 一つでも装備している場合は2、装備していない場合は1
        int modifier = items.stream().allMatch(item -> item == null) ? 1 : 2;

        // X
        double x = taiku + items.stream().filter(item -> item != null)
                .mapToDouble(item -> {
                    // 種別
                    int type3 = item.getType3();
                    // 装備倍率
                    int magnification = this.getKajuuItemMagnification(type3);
                    // 装備対空値
                    int taik = item.getParam().getTaiku();
                    // 改修係数
                    int factor = this.getKajuuKaishuFactor(type3);
                    // 装備改修値
                    int level = item.getLevel();

                    return (magnification * taik) + (factor * Math.sqrt(level));
                }).sum();

        return (int) (modifier * Math.floor(x / modifier));
    }

    // 装備倍率(加重対空)
    private int getKajuuItemMagnification(int type3) {
        switch (type3) {
        case 16: // 高角砲
        case 30: // 高射装置
            return 4;
        case 15: // 機銃
            return 6;
        case 11: // 電探
            return 3;
        default:
            return 0;
        }
    }

    // 改修係数(加重対空)
    private int getKajuuKaishuFactor(int type3) {
        switch (type3) {
        case 16: // 高角砲
            return 3;
        case 15: // 機銃
            return 4;
        default:
            return 0;
        }
    }

    /**
     * 艦隊防空値を返します。
     * 連合艦隊の場合は、shipsに全艦入れる。
     *
     * @param ships 艦隊
     * @param formation 陣形(単縦:1,複縦:2,輪形:3,梯形:4,単横:5,第一:11,第二:12,第三:13,第四:14)
     * @return kajuu 加重対空値
     */
    public <SHIP extends ShipBaseDto> double getKantaiValue(List<SHIP> ships, int formation) {
        int kantaiBonus = ships.stream().mapToInt(ship -> {
            List<ItemDto> items = ship.getItem2();

            // 各艦の艦隊対空ボーナス値
            int shipBonus = (int) items.stream().filter(item -> item != null)
                    .mapToDouble(item -> {
                        // 種別
                        int type3 = item.getType3();
                        // 装備倍率
                        double magnification = this.getKantaiItemMagnification(type3);
                        // 装備対空値
                        int taik = item.getParam().getTaiku();
                        // 改修係数
                        double factor = this.getKantaiKaishuFactor(type3);
                        // 装備改修値
                        int level = item.getLevel();

                        return (magnification * taik) + (factor * Math.sqrt(level));
                    }).sum();
            return shipBonus;
        }).sum();

        return (Math.floor(this.getFormationBonus(formation) * kantaiBonus) * 2) / 1.3;
    }

    // 装備倍率(艦隊防空)
    private double getKantaiItemMagnification(int type3) {
        switch (type3) {
        case 16: // 高角砲
        case 30: // 高射装置
            return 0.35;
        case 11: // 電探
            return 0.4;
        case 12: // 対空強化弾
            return 0.6;
        default:
            return 0.2;
        }
    }

    // 改修係数(艦隊防空)
    private double getKantaiKaishuFactor(int type3) {
        switch (type3) {
        case 16: // 高角砲
            return 3.0;
        case 11: // 電探
            return 1.5;
        default:
            return 0;
        }
    }

    // 陣形補正
    private double getFormationBonus(int formation) {
        switch (formation) {
        case 2: // 複縦陣
            return 1.2;
        case 3: // 輪形陣
            return 1.6;
        case 11: // 第一警戒航行序列
            return 1.1;
        case 13: // 第三警戒航行序列
            return 1.5;
        default:
            return 1.0;
        }
    }

    /**
     * 割合撃墜数を返します。(連合艦隊専用)
     *
     * @param ship 艦娘
     * @param combinedKind 連合艦隊の種類(水上:10の位が1、機動:10の位が2、輸送:10の位が3。あと、第1艦隊ならば一の位に1、第2なら2。例:水上第1艦隊の場合、11
     * @return value 割合撃墜数
     */
    public <SHIP extends ShipBaseDto> double getProportionalShootDownCombined(SHIP ship, int combinedKind) {
        return this.getProportionalShootDownCombined(this.getKajuuValue(ship), combinedKind);
    }

    /**
     * 割合撃墜数を返します。(通常艦隊専用)
     *
     * @param ship 艦娘
     * @return value 割合撃墜数
     */
    public <SHIP extends ShipBaseDto> double getProportionalShootDown(SHIP ship) {
        return this.getProportionalShootDown(this.getKajuuValue(ship));
    }

    /**
     * 割合撃墜数を返します。(連合艦隊専用)
     *
     * @param kajuu 加重対空値
     * @param combinedKind 連合艦隊の種類(水上:10の位が1、機動:10の位が2、輸送:10の位が3。あと、第1艦隊ならば一の位に1、第2なら2。例:水上第1艦隊の場合、11
     * @return value 割合撃墜数
     */
    public double getProportionalShootDownCombined(int kajuu, int combinedKind) {
        return (kajuu * this.getCombinedBonus(combinedKind)) / 400.0;
    }

    /**
     * 割合撃墜数を返します。(通常艦隊専用)
     *
     * @param kajuu 加重対空値
     * @return value 割合撃墜数
     */
    public double getProportionalShootDown(int kajuu) {
        return this.getProportionalShootDownCombined(kajuu, -1);
    }

    /**
     * 固定撃墜数を返します。(連合艦隊専用)
     *
     * @param ship 艦娘
     * @param allShips 艦隊(第二艦隊も一緒に入れる)
     * @param formation 陣形(単縦:1,複縦:2,輪形:3,梯形:4,単横:5,第一:11,第二:12,第三:13,第四:14)
     * @param cutinKind 対空カットインの種別
     * @param combinedKind 連合艦隊の種類(水上:10の位が1、機動:10の位が2、輸送:10の位が3。あと、第1艦隊ならば一の位に1、第2なら2。例:水上第1艦隊の場合、11
     * @return value 固定撃墜数
     */
    public <SHIP extends ShipBaseDto> int getFixedShootDownCombined(SHIP ship, List<SHIP> allShips,
            int formation,
            int cutinKind,
            int combinedKind) {

        return this.getFixedShootDownCombined(this.getKajuuValue(ship), this.getKantaiValue(allShips, formation),
                cutinKind,
                combinedKind);
    }

    /**
     * 固定撃墜数を返します。(通常艦隊専用)
     *
     * @param ship 艦娘
     * @param ships 艦隊
     * @param formation 陣形(単縦:1,複縦:2,輪形:3,梯形:4,単横:5,第一:11,第二:12,第三:13,第四:14)
     * @param cutinKind 対空カットインの種別
     * @return value 固定撃墜数
     */
    public <SHIP extends ShipBaseDto> int getFixedShootDown(SHIP ship, List<SHIP> ships, int formation, int cutinKind) {
        return this.getFixedShootDown(this.getKajuuValue(ship), this.getKantaiValue(ships, formation), cutinKind);
    }

    /**
     * 固定撃墜数を返します。(連合艦隊専用)
     *
     * @param ship 艦娘
     * @param allShips 艦隊(第二艦隊も一緒に入れる)
     * @param formation 陣形(単縦:1,複縦:2,輪形:3,梯形:4,単横:5,第一:11,第二:12,第三:13,第四:14)
     * @param combinedKind 連合艦隊の種類(水上:10の位が1、機動:10の位が2、輸送:10の位が3。あと、第1艦隊ならば一の位に1、第2なら2。例:水上第1艦隊の場合、11
     * @return value 固定撃墜数
     */
    public <SHIP extends ShipBaseDto> int getFixedShootDownCombind(SHIP ship, List<SHIP> allShips,
            int formation, int combinedKind) {

        return this.getFixedShootDownCombined(this.getKajuuValue(ship), this.getKantaiValue(allShips, formation), -1,
                combinedKind);
    }

    /**
     * 固定撃墜数を返します。(通常艦隊専用)
     *
     * @param ship 艦娘
     * @param ships 艦隊
     * @param formation 陣形(単縦:1,複縦:2,輪形:3,梯形:4,単横:5,第一:11,第二:12,第三:13,第四:14)
     * @return value 固定撃墜数
     */
    public <SHIP extends ShipBaseDto> int getFixedShootDown(SHIP ship, List<SHIP> ships, int formation) {
        return this.getFixedShootDown(this.getKajuuValue(ship), this.getKantaiValue(ships, formation), -1);
    }

    /**
     * 固定撃墜数を返します。(連合艦隊専用)
     *
     * @param kajuu 加重対空値
     * @param kantai 艦隊防空値
     * @param cutinKind 対空カットインの種別
     * @param combinedKind 連合艦隊の種類(水上:10の位が1、機動:10の位が2、輸送:10の位が3。あと、第1艦隊ならば一の位に1、第2なら2。例:水上第1艦隊の場合、11
     * @return value 固定撃墜数
     */
    public int getFixedShootDownCombined(int kajuu, double kantai, int cutinKind, int combinedKind) {
        return (int) ((kajuu + kantai) * this.getCombinedBonus(combinedKind)
                * this.getTaikuCutinVariableBonus(cutinKind)) / 10;
    }

    /**
     * 固定撃墜数を返します。(通常艦隊専用)
     *
     * @param kajuu 加重対空値
     * @param kantai 艦隊防空値
     * @param cutinKind 対空カットインの種別
     * @return value 固定撃墜数
     */
    public int getFixedShootDown(int kajuu, double kantai, int cutinKind) {
        return this.getFixedShootDownCombined(kajuu, kantai, cutinKind, -1);
    }

    /**
     * 固定撃墜数を返します。(通常艦隊専用)
     *
     * @param kajuu 加重対空値
     * @param kantai 艦隊防空値
     * @return value 固定撃墜数
     */
    public int getFixedShootDown(int kajuu, double kantai) {
        return this.getFixedShootDown(kajuu, kantai, -1);
    }

    /**
     * 最低保証数を返します。
     *
     * @param cutinKind 対空カットインの種別
     * @return value 最低保証数
     */
    public int getSecurity(int cutinKind) {
        return 1 + this.getTaikuCutinFixedBonus(cutinKind);
    }

    /**
     * 最低保証数を返します。
     *
     * @return value 最低保証数
     */
    public int getSecurity() {
        return 1;
    }

    // 連合艦隊補正
    private double getCombinedBonus(int kind) {
        switch (kind) {
        case 11: // 水上第一艦隊
            return 0.8 * 0.9;
        case 12: // 水上第二艦隊
            return 0.8 * 0.6;
        default:
            return 1.0;
        }
    }

    // 対空カットイン変動ボーナス(七四式準拠)
    private double getTaikuCutinVariableBonus(int kind) {
        switch (kind) {
        case 1: // 高角砲x2/電探(秋月型)
            return 1.7;
        case 2: // 高角砲/電探(秋月型)
            return 1.7;
        case 3: // 高角砲x2(秋月型)
            return 1.6;
        case 4: // 大口径主砲/三式弾/高射装置/電探
            return 1.5;
        case 5: // 高角砲+高射装置x2/電探
            return 1.5;
        case 6: // 大口径主砲/三式弾/高射装置
            return 1.5;
        case 7: // 高角砲/高射装置/電探
            return 1.35;
        case 8: // 高角砲+高射装置/電探
            return 1.4;
        case 9: // 高角砲/高射装置
            return 1.3;
        case 10: // 高角砲/集中機銃/電探(摩耶改二)
            return 1.65;
        case 11: // 高角砲/集中機銃(摩耶改二)
            return 1.5;
        case 12: // 集中機銃/機銃/電探
            return 1.25;
        case 13: // Unknown
            return 1.35;
        case 14: // 高角砲/機銃/電探(五十鈴改二)
            return 1.0;
        case 15: // 高角砲/機銃(五十鈴改二)
            return 1.0;
        case 16: // 高角砲/機銃/電探(霞改二乙)
            return 1.0;
        case 17: // 高角砲/機銃(霞改二乙)
            return 1.0;
        case 18: // 集中機銃(皐月改二)
            return 1.2;
        case 19: // 高角砲(非高射装置)/集中機銃(鬼怒改二)
            return 1.45;
        case 20: // 集中機銃(鬼怒改二)
            return 1.25;
        default:
            return 1.0;
        }
    }

    // 対空カットイン固定ボーナス(七四式準拠)
    private int getTaikuCutinFixedBonus(int kind) {
        switch (kind) {
        case 1: // 高角砲x2/電探(秋月型)
            return 7;
        case 2: // 高角砲/電探(秋月型)
            return 6;
        case 3: // 高角砲x2(秋月型)
            return 4;
        case 4: // 大口径主砲/三式弾/高射装置/電探
            return 6;
        case 5: // 高角砲+高射装置x2/電探
            return 4;
        case 6: // 大口径主砲/三式弾/高射装置
            return 4;
        case 7: // 高角砲/高射装置/電探
            return 3;
        case 8: // 高角砲+高射装置/電探
            return 4;
        case 9: // 高角砲/高射装置
            return 2;
        case 10: // 高角砲/集中機銃/電探(摩耶改二)
            return 8;
        case 11: // 高角砲/集中機銃(摩耶改二)
            return 6;
        case 12: // 集中機銃/機銃/電探
            return 3;
        case 13: // Unknown
            return 4;
        case 14: // 高角砲/機銃/電探(五十鈴改二)
            return 4;
        case 15: // 高角砲/機銃(五十鈴改二)
            return 3;
        case 16: // 高角砲/機銃/電探(霞改二乙)
            return 4;
        case 17: // 高角砲/機銃(霞改二乙)
            return 2;
        case 18: // 集中機銃(皐月改二)
            return 2;
        case 19: // 高角砲(非高射装置)/集中機銃(鬼怒改二)
            return 5;
        case 20: // 集中機銃(鬼怒改二)
            return 3;
        default:
            return 0;
        }
    }
}
