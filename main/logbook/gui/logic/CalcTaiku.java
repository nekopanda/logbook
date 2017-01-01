/**
 *
 */
package logbook.gui.logic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import logbook.dto.ItemDto;
import logbook.dto.ShipBaseDto;
import logbook.dto.ShipDto;
import logbook.dto.ShipParameters;

/**
 * 対空関連を扱うクラス。
 * ソースは関連事項を参照。
 *
 * @see <a href="http://wikiwiki.jp/kancolle/?%B9%D2%B6%F5%C0%EF">wiki 航空戦</a>
 * @see <a href="https://github.com/andanteyk/ElectronicObserver/blob/30fd425c41299370a947abc9005fe50682b868ab/ElectronicObserver/Utility/Data/Calculator.cs">七四式 ソース</a>
 * @see <a href="https://twitter.com/galpokopii/status/812180211416502272">対空カットイン変動ボーナス・高射装置の艦隊防空への改修効果係数検証</a>
 * @see <a href="https://twitter.com/KennethWWKK/status/780793591387959297">艦これ改のソフトって解析翻訳</a>
 * @author Nishisonic
 */
public final class CalcTaiku {

    /**
     * 艦娘の加重対空値を返します。
     *
     * @param ship 艦娘
     * @return kajuu 加重対空値
     */
    public int getFriendKajuuValue(ShipDto ship) {
        // 装備
        List<ItemDto> items = new ArrayList<ItemDto>();
        items.addAll(ship.getItem2());
        items.add(ship.getSlotExItem());

        // 艦娘の素の対空値
        int taiku = ship.getTaiku() - items.stream().filter(item -> item != null)
                .map(ItemDto::getParam)
                .mapToInt(ShipParameters::getTaiku)
                .sum();

        // 一つでも装備している場合は2、装備していない場合は1
        int modifier = items.stream().allMatch(item -> item == null) ? 1 : 2;

        // X
        BigDecimal x = items.stream().filter(item -> item != null)
                .map(item -> {
                    // 種別
                    int type3 = item.getType3();
                    // 装備倍率
                    BigDecimal magnification = new BigDecimal(this.getKajuuItemMagnification(type3));
                    // 装備対空値
                    BigDecimal taik = new BigDecimal(item.getParam().getTaiku());
                    // 改修係数
                    BigDecimal factor = new BigDecimal(this.getKajuuKaishuFactor(type3));
                    // 装備改修値
                    int level = item.getLevel();

                    return magnification.multiply(taik).add(factor.multiply(new BigDecimal(Math.sqrt(level))));
                }).reduce(BigDecimal.ZERO, BigDecimal::add).add(new BigDecimal(taiku));

        return (int) (modifier * Math.floor(x.doubleValue() / modifier));
    }

    /**
     * 深海棲艦の加重対空値を返します。
     *
     * @param ship 深海棲艦
     * @return kajuu 加重対空値
     */
    public <SHIP extends ShipBaseDto> int getEnemyKajuuValue(SHIP ship) {
        // 装備
        List<ItemDto> items = ship.getItem2();

        // 深海棲艦の素の対空値
        int taiku = ship.getTaiku() - items.stream().filter(item -> item != null)
                .map(ItemDto::getParam)
                .mapToInt(ShipParameters::getTaiku)
                .sum();

        // X
        BigDecimal itemTotal = items.stream().filter(item -> item != null)
                .map(item -> {
                    // 種別
                    int type3 = item.getType3();
                    // 装備倍率
                    BigDecimal magnification = new BigDecimal(this.getKajuuItemMagnification(type3));
                    // 装備対空値
                    BigDecimal taik = new BigDecimal(item.getParam().getTaiku());

                    return magnification.multiply(taik);
                }).reduce(BigDecimal.ZERO, BigDecimal::add);

        return (int) (Math.floor(2 * Math.sqrt(taiku)) + itemTotal.doubleValue());
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
     * 艦娘の艦隊防空値を返します。
     * 連合艦隊の場合は、shipsに全艦入れる。
     *
     * @param ships 艦隊
     * @param formation 陣形(単縦:1,複縦:2,輪形:3,梯形:4,単横:5,第一:11,第二:12,第三:13,第四:14)
     * @return kantai 艦隊防空値
     */
    public double getFriendKantaiValue(List<ShipDto> ships, int formation) {
        int kantaiBonus = ships.stream().map(ship -> {
            List<ItemDto> items = new ArrayList<ItemDto>();
            items.addAll(ship.getItem2());
            items.add(ship.getSlotExItem());

            // 各艦の艦隊対空ボーナス値
            BigDecimal shipBonus = items.stream().filter(item -> item != null)
                    .map(item -> {
                        // 種別
                        int type3 = item.getType3();
                        // 装備倍率
                        BigDecimal magnification = new BigDecimal(this.getKantaiItemMagnification(type3));
                        // 装備対空値
                        BigDecimal taik = new BigDecimal(item.getParam().getTaiku());
                        // 改修係数
                        BigDecimal factor = new BigDecimal(this.getKantaiKaishuFactor(type3));
                        // 装備改修値
                        int level = item.getLevel();

                        return magnification.multiply(taik)
                                .add(factor.multiply(new BigDecimal(Math.sqrt(level))));
                    }).reduce(BigDecimal.ZERO, BigDecimal::add);
            return shipBonus;
        }).reduce(BigDecimal.ZERO, BigDecimal::add).intValue();

        return Math.floor(this.getFormationBonus(formation) * kantaiBonus) * (2 / 1.3);
    }

    /**
     * 深海棲艦の艦隊防空値を返します。
     * 連合艦隊の場合は、shipsに全艦入れる。
     *
     * @param ships 艦隊
     * @param formation 陣形(単縦:1,複縦:2,輪形:3,梯形:4,単横:5,第一:11,第二:12,第三:13,第四:14)
     * @return kantai 艦隊防空値
     */
    public <SHIP extends ShipBaseDto> int getEnemyKantaiValue(List<SHIP> ships, int formation) {
        int kantaiBonus = ships.stream().map(ship -> {
            List<ItemDto> items = ship.getItem2();

            BigDecimal shipBonus = items.stream().filter(item -> item != null)
                    .map(item -> {
                        // 種別
                        int type3 = item.getType3();
                        // 装備倍率
                        BigDecimal magnification = new BigDecimal(this.getKantaiItemMagnification(type3));
                        // 装備対空値
                        BigDecimal taik = new BigDecimal(item.getParam().getTaiku());

                        return magnification.multiply(taik);
                    }).reduce(BigDecimal.ZERO, BigDecimal::add);
            return shipBonus;
        }).reduce(BigDecimal.ZERO, BigDecimal::add).intValue();

        return (int) (Math.floor(this.getFormationBonus(formation) * kantaiBonus) * 2);
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
        case 30: // 高射装置
            return 2.0;
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
     * 艦娘の割合撃墜数を返します。(連合艦隊専用)
     *
     * @param ship 艦娘
     * @param combinedKind 連合艦隊の種類(機動:10の位が1、水上:10の位が2、輸送:10の位が3。あと、第1艦隊ならば一の位に1、第2なら2。例:水上第1艦隊の場合、11)
     * @return value 割合撃墜数
     */
    public double getFriendProportionalShootDownCombined(ShipDto ship, int combinedKind) {
        return this.getFriendProportionalShootDownCombined(this.getFriendKajuuValue(ship), combinedKind);
    }

    /**
     * 深海棲艦の割合撃墜数を返します。(連合艦隊専用)
     *
     * @param ship 深海棲艦
     * @param combinedKind 種類(第1艦隊ならば1、第2艦隊ならば2)
     * @return value 割合撃墜数
     */
    public <SHIP extends ShipBaseDto> double getEnemyProportionalShootDownCombined(SHIP ship, int combinedKind) {
        return this.getEnemyProportionalShootDownCombined(this.getEnemyKajuuValue(ship), combinedKind);
    }

    /**
     * 艦娘の割合撃墜数を返します。(通常艦隊専用)
     *
     * @param ship 艦娘
     * @return value 割合撃墜数
     */
    public double getFriendProportionalShootDown(ShipDto ship) {
        return this.getFriendProportionalShootDown(this.getFriendKajuuValue(ship));
    }

    /**
     * 深海棲艦の割合撃墜数を返します。(通常艦隊専用)
     *
     * @param ship 深海棲艦
     * @return value 割合撃墜数
     */
    public <SHIP extends ShipBaseDto> double getEnemyProportionalShootDown(SHIP ship) {
        return this.getEnemyProportionalShootDown(this.getEnemyKajuuValue(ship));
    }

    /**
     * 艦娘の割合撃墜数を返します。(連合艦隊専用)
     *
     * @param kajuu 加重対空値
     * @param combinedKind 連合艦隊の種類(機動:10の位が1、水上:10の位が2、輸送:10の位が3。あと、第1艦隊ならば一の位に1、第2なら2。例:水上第1艦隊の場合、11)
     * @return value 割合撃墜数
     */
    public double getFriendProportionalShootDownCombined(int kajuu, int combinedKind) {
        return (kajuu * this.getFriendCombinedBonus(combinedKind)) / 400.0;
    }

    /**
     * 深海棲艦の割合撃墜数を返します。(連合艦隊専用)
     *
     * @param kajuu 加重対空値
     * @param combinedKind 種類(第1艦隊ならば1、第2艦隊ならば2)
     * @return value 割合撃墜数
     */
    public double getEnemyProportionalShootDownCombined(int kajuu, int combinedKind) {
        return (kajuu * this.getEnemyCombinedBonus(combinedKind)) / 400.0;
    }

    /**
     * 艦娘の割合撃墜数を返します。(通常艦隊専用)
     *
     * @param kajuu 加重対空値
     * @return value 割合撃墜数
     */
    public double getFriendProportionalShootDown(int kajuu) {
        return this.getFriendProportionalShootDownCombined(kajuu, -1);
    }

    /**
     * 深海棲艦の割合撃墜数を返します。(通常艦隊専用)
     *
     * @param kajuu 加重対空値
     * @return value 割合撃墜数
     */
    public double getEnemyProportionalShootDown(int kajuu) {
        return this.getEnemyProportionalShootDownCombined(kajuu, -1);
    }

    /**
     * 艦娘の固定撃墜数を返します。(連合艦隊専用)
     *
     * @param ship 艦娘
     * @param allShips 艦隊(第二艦隊も一緒に入れる)
     * @param formation 陣形(単縦:1,複縦:2,輪形:3,梯形:4,単横:5,第一:11,第二:12,第三:13,第四:14)
     * @param cutinKind 対空カットインの種別
     * @param combinedKind 連合艦隊の種類(機動:10の位が1、水上:10の位が2、輸送:10の位が3。あと、第1艦隊ならば一の位に1、第2なら2。例:水上第1艦隊の場合、11)
     * @return value 固定撃墜数
     */
    public int getFriendFixedShootDownCombined(ShipDto ship, List<ShipDto> allShips,
            int formation,
            int cutinKind,
            int combinedKind) {

        return this.getFriendFixedShootDownCombined(this.getFriendKajuuValue(ship),
                this.getFriendKantaiValue(allShips, formation),
                cutinKind,
                combinedKind);
    }

    /**
     * 深海棲艦の固定撃墜数を返します。(連合艦隊専用)
     *
     * @param ship 深海棲艦
     * @param allShips 艦隊(第二艦隊も一緒に入れる)
     * @param formation 陣形(単縦:1,複縦:2,輪形:3,梯形:4,単横:5,第一:11,第二:12,第三:13,第四:14)
     * @param cutinKind 対空カットインの種別
     * @param combinedKind 種類(第1艦隊ならば1、第2艦隊ならば2)
     * @return value 固定撃墜数
     */
    public <SHIP extends ShipBaseDto> int getEnemyFixedShootDownCombined(SHIP ship, List<SHIP> allShips,
            int formation,
            int cutinKind,
            int combinedKind) {

        return this.getEnemyFixedShootDownCombined(this.getEnemyKajuuValue(ship),
                this.getEnemyKantaiValue(allShips, formation),
                cutinKind,
                combinedKind);
    }

    /**
     * 艦娘の固定撃墜数を返します。(通常艦隊専用)
     *
     * @param ship 艦娘
     * @param ships 艦隊
     * @param formation 陣形(単縦:1,複縦:2,輪形:3,梯形:4,単横:5,第一:11,第二:12,第三:13,第四:14)
     * @param cutinKind 対空カットインの種別
     * @return value 固定撃墜数
     */
    public int getFriendFixedShootDown(ShipDto ship, List<ShipDto> ships, int formation,
            int cutinKind) {
        return this.getFriendFixedShootDown(this.getFriendKajuuValue(ship), this.getFriendKantaiValue(ships, formation),
                cutinKind);
    }

    /**
     * 深海棲艦の固定撃墜数を返します。(通常艦隊専用)
     *
     * @param ship 深海棲艦
     * @param ships 艦隊
     * @param formation 陣形(単縦:1,複縦:2,輪形:3,梯形:4,単横:5,第一:11,第二:12,第三:13,第四:14)
     * @param cutinKind 対空カットインの種別
     * @return value 固定撃墜数
     */
    public <SHIP extends ShipBaseDto> int getEnemyFixedShootDown(SHIP ship, List<SHIP> ships, int formation,
            int cutinKind) {
        return this.getEnemyFixedShootDown(this.getEnemyKajuuValue(ship), this.getEnemyKantaiValue(ships, formation),
                cutinKind);
    }

    /**
     * 艦娘の固定撃墜数を返します。(連合艦隊専用)
     *
     * @param ship 艦娘
     * @param allShips 艦隊(第二艦隊も一緒に入れる)
     * @param formation 陣形(単縦:1,複縦:2,輪形:3,梯形:4,単横:5,第一:11,第二:12,第三:13,第四:14)
     * @param combinedKind 連合艦隊の種類(機動:10の位が1、水上:10の位が2、輸送:10の位が3。あと、第1艦隊ならば一の位に1、第2なら2。例:水上第1艦隊の場合、11)
     * @return value 固定撃墜数
     */
    public int getFriendFixedShootDownCombind(ShipDto ship, List<ShipDto> allShips,
            int formation, int combinedKind) {

        return this.getFriendFixedShootDownCombined(this.getFriendKajuuValue(ship),
                this.getFriendKantaiValue(allShips, formation),
                -1,
                combinedKind);
    }

    /**
     * 深海棲艦の固定撃墜数を返します。(連合艦隊専用)
     *
     * @param ship 深海棲艦
     * @param allShips 艦隊(第二艦隊も一緒に入れる)
     * @param formation 陣形(単縦:1,複縦:2,輪形:3,梯形:4,単横:5,第一:11,第二:12,第三:13,第四:14)
     * @param combinedKind 種類(第1艦隊ならば1、第2艦隊ならば2)
     * @return value 固定撃墜数
     */
    public <SHIP extends ShipBaseDto> int getEnemyFixedShootDownCombind(SHIP ship, List<SHIP> allShips,
            int formation, int combinedKind) {

        return this.getEnemyFixedShootDownCombined(this.getEnemyKajuuValue(ship),
                this.getEnemyKantaiValue(allShips, formation),
                -1,
                combinedKind);
    }

    /**
     * 艦娘の固定撃墜数を返します。(通常艦隊専用)
     *
     * @param ship 艦娘
     * @param ships 艦隊
     * @param formation 陣形(単縦:1,複縦:2,輪形:3,梯形:4,単横:5,第一:11,第二:12,第三:13,第四:14)
     * @return value 固定撃墜数
     */
    public int getFriendFixedShootDown(ShipDto ship, List<ShipDto> ships, int formation) {
        return this.getFriendFixedShootDown(this.getFriendKajuuValue(ship), this.getFriendKantaiValue(ships, formation),
                -1);
    }

    /**
     * 深海棲艦の固定撃墜数を返します。(通常艦隊専用)
     *
     * @param ship 深海棲艦
     * @param ships 艦隊
     * @param formation 陣形(単縦:1,複縦:2,輪形:3,梯形:4,単横:5,第一:11,第二:12,第三:13,第四:14)
     * @return value 固定撃墜数
     */
    public <SHIP extends ShipBaseDto> int getEnemyFixedShootDown(SHIP ship, List<SHIP> ships, int formation) {
        return this.getEnemyFixedShootDown(this.getEnemyKajuuValue(ship), this.getEnemyKantaiValue(ships, formation),
                -1);
    }

    /**
     * 艦娘の固定撃墜数を返します。(連合艦隊専用)
     *
     * @param kajuu 加重対空値
     * @param kantai 艦隊防空値
     * @param cutinKind 対空カットインの種別
     * @param combinedKind 連合艦隊の種類(機動:10の位が1、水上:10の位が2、輸送:10の位が3。あと、第1艦隊ならば一の位に1、第2なら2。例:水上第1艦隊の場合、11)
     * @return value 固定撃墜数
     */
    public int getFriendFixedShootDownCombined(int kajuu, double kantai, int cutinKind, int combinedKind) {
        return (int) ((kajuu + kantai) * this.getFriendCombinedBonus(combinedKind)
                * this.getTaikuCutinVariableBonus(cutinKind)) / 10;
    }

    /**
     * 深海棲艦の固定撃墜数を返します。(連合艦隊専用)
     *
     * @param kajuu 加重対空値
     * @param kantai 艦隊防空値
     * @param cutinKind 対空カットインの種別
     * @param combinedKind 種類(第1艦隊ならば1、第2艦隊ならば2)
     * @return value 固定撃墜数
     */
    public int getEnemyFixedShootDownCombined(int kajuu, double kantai, int cutinKind, int combinedKind) {
        return (int) (((kajuu + kantai) * this.getEnemyCombinedBonus(combinedKind)
                * this.getTaikuCutinVariableBonus(cutinKind)) / 10.6);
    }

    /**
     * 艦娘の固定撃墜数を返します。(通常艦隊専用)
     *
     * @param kajuu 加重対空値
     * @param kantai 艦隊防空値
     * @param cutinKind 対空カットインの種別
     * @return value 固定撃墜数
     */
    public int getFriendFixedShootDown(int kajuu, double kantai, int cutinKind) {
        return this.getFriendFixedShootDownCombined(kajuu, kantai, cutinKind, -1);
    }

    /**
     * 深海棲艦の固定撃墜数を返します。(通常艦隊専用)
     *
     * @param kajuu 加重対空値
     * @param kantai 艦隊防空値
     * @param cutinKind 対空カットインの種別
     * @return value 固定撃墜数
     */
    public int getEnemyFixedShootDown(int kajuu, double kantai, int cutinKind) {
        return this.getEnemyFixedShootDownCombined(kajuu, kantai, cutinKind, -1);
    }

    /**
     * 艦娘の最低保証数を返します。
     *
     * @param cutinKind 対空カットインの種別
     * @return value 最低保証数
     */
    public int getFriendSecurity(int cutinKind) {
        return 1 + this.getTaikuCutinFixedBonus(cutinKind);
    }

    /**
     * 艦娘の最低保証数を返します。
     *
     * @return value 最低保証数
     */
    public int getFriendSecurity() {
        return 1;
    }

    /**
     * 深海棲艦の最低保証数を返します。
     *
     * @param cutinKind 対空カットインの種別
     * @return value 最低保証数
     */
    public int getEnemySecurity(int cutinKind) {
        return this.getTaikuCutinFixedBonus(cutinKind);
    }

    /**
     * 深海棲艦の最低保証数を返します。
     *
     * @return value 最低保証数
     */
    public int getEnemySecurity() {
        return 0;
    }

    // 連合艦隊補正(艦娘)
    private double getFriendCombinedBonus(int kind) {
        switch (kind) {
        case 21: // 水上第一艦隊
            return 0.8 * 0.9;
        case 22: // 水上第二艦隊
            return 0.8 * 0.6;
        default:
            return 1.0;
        }
    }

    // 連合艦隊補正(深海棲艦)
    private double getEnemyCombinedBonus(int kind) {
        switch (kind) {
        case 1: // 第一艦隊
            return 0.8 * 1.0;
        case 2: // 第二艦隊
            return 0.8 * 0.6;
        default:
            return 1.0;
        }
    }

    // 対空カットイン変動ボーナス
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
            return 1.45;
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
        case 13: // 高角砲/集中機銃/電探(摩耶改二不可)
            return 1.35;
        case 14: // 高角砲/機銃/電探(五十鈴改二)
            return 1.45;
        case 15: // 高角砲/機銃(五十鈴改二)
            return 1.3;
        case 16: // 高角砲/機銃/電探(霞改二乙)
            return 1.4;
        case 17: // 高角砲/機銃(霞改二乙)
            return 1.25;
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

    // 対空カットイン固定ボーナス
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
        case 13: // 高角砲/集中機銃/電探(摩耶改二不可)
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
