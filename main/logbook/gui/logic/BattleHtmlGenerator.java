/**
 * 
 */
package logbook.gui.logic;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import logbook.constants.AppConstants;
import logbook.dto.AirBattleDto;
import logbook.dto.AtackKind;
import logbook.dto.BattleAtackDto;
import logbook.dto.BattleExDto;
import logbook.dto.BattleExDto.Phase;
import logbook.dto.BattlePhaseKind;
import logbook.dto.BattleResultDto;
import logbook.dto.ItemDto;
import logbook.dto.ShipBaseDto;
import logbook.dto.ShipDto;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Nekopanda
 *
 */
public class BattleHtmlGenerator extends HTMLGenerator {

    private static String getRowSpan(int span) {
        return "rowspan=\"" + span + "\"";
    }

    private static String getColSpan(int span) {
        return "colspan=\"" + span + "\"";
    }

    private static DateFormat dateFormat = new SimpleDateFormat(AppConstants.DATE_FORMAT);

    private static String[] BOX_CLASS = new String[] { "box" };

    private static String[][][] TEXT_CLASS = new String[][][] {
            new String[][] {
                    new String[] { "friend" },
                    new String[] { "enemy" }
            }, new String[][] {
                    new String[] { "enemy" },
                    new String[] { "friend" }
            }
    };

    private static String[][][] DAMAGE_CLASS = new String[][][] {
            new String[][] {
                    new String[] { "friend-damage" },
                    new String[] { "enemy-damage" }
            }, new String[][] {
                    new String[] { "enemy-damage" },
                    new String[] { "friend-damage" }
            }
    };

    private static String[][] DAMAGE_LABEL_CLASS = new String[][] {
            new String[] { "label-mukizu" },
            new String[] { "label-kenzai" },
            new String[] { "label-syoha" },
            new String[] { "label-tyuha" },
            new String[] { "label-taiha" },
            new String[] { "label-gotin" }
    };

    private static String[][] PARAM_TABLE_CLASS = new String[][] {
            new String[] { "friend", "friend-param" },
            new String[] { "enemy", "enemy-param" }
    };

    private static String[][] SLOTITEM_TABLE_CLASS = new String[][] {
            new String[] { "friend", "friend-slotitem" },
            new String[] { "enemy", "enemy-slotitem" }
    };

    private static String[][] FORMATION_CLASS = new String[][] {
            new String[] { "friend", "friend-formation" },
            new String[] { "enemy", "enemy-formation" }
    };

    private static String[][] DAMAGE_TABLE_CLASS = new String[][] {
            new String[] { "friend", "friend-damage" },
            new String[] { "enemy", "enemy-damage" }
    };

    private static String[][][] AIR_DAMAGE_TABLE_CLASS = new String[][][] {
            new String[][] {
                    new String[] { "friend", "friend-air-atack" },
                    new String[] { "enemy", "enemy-air-damage" }
            },
            new String[][] {
                    new String[] { "enemy", "enemy-air-atack" },
                    new String[] { "friend", "friend-air-damage" }
            }
    };

    private static String[] AIR_TABLE_CLASS = new String[] { "air-stage12" };

    private static String[][][] RAIGEKI_DAMAGE_TABLE_CLASS = new String[][][] {
            new String[][] {
                    new String[] { "friend", "friend-raigeki-atack" },
                    new String[] { "enemy", "enemy-raigeki-damage" }
            },
            new String[][] {
                    new String[] { "enemy", "enemy-raigeki-atack" },
                    new String[] { "friend", "friend-raigeki-damage" }
            }
    };

    private String getShipName(ShipBaseDto[] ships, int index) {
        return String.valueOf(index + 1) + "." + ships[index].getFriendlyName();
    }

    /**
     * パラメータテーブルを生成
     * @param gen
     * @param tableTitle
     * @param ships
     * @param hp
     * @param phaseName
     */
    private <SHIP extends ShipBaseDto>
            void genParmeters(String tableTitle,
                    List<SHIP> ships, int[][] hp, String[] phaseName, int hqLv, boolean isSecond)
    {
        if (ships.size() == 0) {
            return;
        }

        boolean isFriend;
        int ci;
        if (ships.get(0) instanceof ShipDto) {
            isFriend = true;
            ci = 0;
        }
        else {
            isFriend = false;
            ci = 1;
        }

        int numPhases = (hp.length / 2) - 1;

        this.begin("div", BOX_CLASS);
        this.begin("table", PARAM_TABLE_CLASS[ci]);
        this.inline("caption", tableTitle, null);

        this.begin("tr", null);

        this.inline("th", "", null);
        this.inline("th", "艦名", null);
        this.inline("th", "cond.", null);
        this.inline("th", "制空", null);
        this.inline("th", "索敵", null);
        this.inline("th", "開始時", null);
        for (int i = 0; i < numPhases; ++i) {
            this.inline("th", "", null);
            this.inline("th", "Dmg", null);
            this.inline("th", "残", null);
            this.inline("th", "", null);
        }

        this.inline("th", "火力", null);
        this.inline("th", "雷装", null);
        this.inline("th", "対空", null);
        this.inline("th", "装甲", null);
        this.inline("th", "回避", null);
        this.inline("th", "対潜", null);
        this.inline("th", "索敵", null);
        this.inline("th", "運", null);
        this.inline("th", "速力", null);
        this.inline("th", "射程", null);

        this.end(); // tr

        int totalNowHp = 0;
        int totalMaxHp = 0;

        for (int i = 0; i < ships.size(); ++i) {
            SHIP ship = ships.get(i);
            SeikuString seiku = new SeikuString(ship);
            SakutekiString sakuteki = new SakutekiString(ship);
            int nowhp = hp[0][i];
            int maxhp = hp[1][i];

            totalNowHp += nowhp;
            totalMaxHp += maxhp;

            this.begin("tr", null);

            this.inline("td", String.valueOf(i + (isSecond ? 7 : 1)), null);
            this.inline("td", ship.getFriendlyName(), null);

            if (isFriend) {
                this.inline("td", String.valueOf(((ShipDto) ship).getCond()), null);
            }
            else {
                this.inline("td", "", null);
            }

            this.inline("td", seiku.toString(), null);
            this.inline("td", sakuteki.toString(), null);
            this.inline("td", nowhp + "/" + maxhp, null);

            for (int p = 1; p <= numPhases; ++p) {
                if (i == 0) {
                    this.inline("td", getRowSpan(ships.size()), "→", null);
                }
                int dam = hp[(p * 2) + 0][i];
                int remain = hp[(p * 2) + 1][i];
                DamageRate rate = DamageRate.fromHP(remain, maxhp);
                this.inline("td", String.valueOf(dam), null);
                this.inline("td", String.valueOf(remain), null);
                this.inline("td", rate.toString(isFriend), DAMAGE_LABEL_CLASS[rate.getLevel()]);
            }

            this.inline("td", String.valueOf(ship.getKaryoku()), null);
            this.inline("td", String.valueOf(ship.getRaisou()), null);
            this.inline("td", String.valueOf(ship.getTaiku()), null);
            this.inline("td", String.valueOf(ship.getSoukou()), null);
            this.inline("td", String.valueOf(ship.getKaihi()), null);
            this.inline("td", String.valueOf(ship.getTaisen()), null);
            this.inline("td", String.valueOf(ship.getSakuteki()), null);
            this.inline("td", String.valueOf(ship.getLucky()), null);
            this.inline("td", String.valueOf(ship.getParam().getSokuString()), null);
            this.inline("td", String.valueOf(ship.getParam().getLengString()), null);

            this.end(); // tr
        }

        this.begin("tr", null);

        SeikuString totalSeiku = new SeikuString(ships);
        SakutekiString totalSakuteki = new SakutekiString(ships, hqLv);
        this.inline("td", "", null);
        this.inline("td", "合計", null);
        this.inline("td", "", null);
        this.inline("td", totalSeiku.toString(), null);
        this.inline("td", totalSakuteki.toString(), null);
        this.inline("td", totalNowHp + "/" + totalMaxHp, null);

        for (int i = 0; i < numPhases; ++i) {
            this.inline("td", "", null);
            this.inline("td", getColSpan(3), phaseName[i], null);
        }

        this.inline("td", getColSpan(10), "", null);

        this.end(); // tr

        this.end(); // table
        this.end(); // p
    }

    /**
     * 装備テーブルを生成
     * @param gen
     * @param ships
     */
    private <SHIP extends ShipBaseDto>
            void genSlotitemTable(List<SHIP> ships, boolean isSecond)
    {
        if (ships.size() == 0) {
            return;
        }
        boolean isFriend = ships.get(0) instanceof ShipDto;
        int ci = isFriend ? 0 : 1;

        this.begin("div", BOX_CLASS);
        this.begin("table", SLOTITEM_TABLE_CLASS[ci]);

        this.begin("tr", null);
        this.inline("th", "艦名", null);
        for (int c = 0; c < 5; ++c) {
            this.inline("th", getColSpan(2), "装備" + (c + 1), null);
        }
        if (isFriend) {
            this.inline("th", "補助装備", null);
        }
        this.end(); // tr

        for (int i = 0; i < ships.size(); ++i) {
            this.begin("tr", null);
            SHIP ship = ships.get(i);
            this.inline("td", String.valueOf(i + (isSecond ? 7 : 1)) + "." + ship.getFriendlyName(), null);
            List<ItemDto> items = ship.getItem2();
            for (int c = 0; c < 5; ++c) {
                String onSlot = "";
                String itemName = "";
                int[] onSlots = ship.getOnSlot(); // 現在の艦載機搭載数
                int[] maxeq = ship.getShipInfo().getMaxeq2(); // 艦載機最大搭載数
                if (c < items.size()) {
                    ItemDto item = items.get(c);
                    if (item != null) {
                        if (item.isPlane()) {
                            String max = (maxeq == null) ? "?" : String.valueOf(maxeq[c]);
                            String cur = (onSlots == null) ? "?" : String.valueOf(onSlots[c]);
                            onSlot = cur + "/" + max;
                        }
                        itemName += item.getFriendlyName();
                    }
                }
                this.inline("td", itemName, null);
                this.inline("td", onSlot, null);
            }
            if (isFriend) {
                String itemName = "";
                ItemDto dto = ((ShipDto) ship).getSlotExItem();
                if (dto != null) {
                    itemName = dto.getFriendlyName();
                }
                this.inline("td", itemName, null);
            }
            this.end(); // tr
        }

        this.end(); // table
        this.end(); // p
    }

    /**
     * 会敵情報を生成
     * @param gen
     * @param battle
     */
    private void genFormation(BattleExDto battle) {
        String fSakuteki = "";
        String eSakuteki = "";
        if (battle.getSakuteki() != null) { // 夜戦開始マスは索敵なし
            fSakuteki = battle.getSakuteki()[0];
            eSakuteki = battle.getSakuteki()[1];
        }

        this.inline("span", "会敵: " + battle.getFormationMatch(), null);
        this.begin("table", null);
        this.begin("tr", null);
        this.inline("th", "", null);
        this.inline("th", "陣形", null);
        this.inline("th", "索敵", null);
        this.end(); // tr
        this.begin("tr", FORMATION_CLASS[0]);
        this.inline("td", "自", null);
        this.inline("td", battle.getFormation()[0], null);
        this.inline("td", fSakuteki, null);
        this.end(); // tr
        this.begin("tr", FORMATION_CLASS[1]);
        this.inline("td", "敵", null);
        this.inline("td", battle.getFormation()[1], null);
        this.inline("td", eSakuteki, null);
        this.end(); // tr
        this.end(); // table
    }

    private static <T> void copyToOffset(List<? extends T> src, T[] array, int offset) {
        if (src == null) {
            return;
        }
        for (int i = 0; i < src.size(); ++i) {
            array[i + offset] = src.get(i);
        }
    }

    private static void copyToOffset(int[] src, int[] dst, int offset) {
        if (src == null) {
            return;
        }
        System.arraycopy(src, 0, dst, offset, src.length);
    }

    /**
     * ダメージによる変化を計算して表示用文字列を返す
     * @param hp
     * @param target
     * @param damage
     * @param index
     * @return
     */
    private static String doDamage(int[] hp, int[] target, int[] damage, int index) {
        int before = hp[target[index]];
        int after = before - damage[index];
        if (after < 0) {
            after = 0;
        }
        if (before == after) {
            return "";
        }
        hp[target[index]] = after;
        return String.valueOf(before) + "→" + after;
    }

    private static String getDamageString(int damage, int critical) {
        if (damage == 0) {
            return "ミス";
        }
        if (critical == 2) {
            return "<b>" + damage + "</b>";
        }
        return String.valueOf(damage);
    }

    /**
     * 「○ ダメージ ○→○」のテーブルを生成
     * @param gen
     * @param atack
     * @param targetShips
     * @param targetHp
     */
    private void genDamageTableContent(BattleAtackDto atack,
            ShipBaseDto[] targetShips, int[] targetHp) {
        int ci = (atack.friendAtack) ? 0 : 1;

        if (atack.damage.length == 0) {
            this.begin("tr", null);
            this.inline("td", "なし", null);
            this.end(); // tr
            return;
        }

        this.begin("tr", null);
        this.inline("th", "艦", null);
        this.inline("th", "ダメージ", DAMAGE_CLASS[ci][1]);
        this.inline("th", "残りHP", null);
        this.end(); // tr

        for (int i = 0; i < atack.damage.length; ++i) {
            this.begin("tr", null);
            this.inline("td", this.getShipName(targetShips, atack.target[i]), TEXT_CLASS[ci][1]);
            this.inline("td", getDamageString(atack.damage[i], 0), DAMAGE_CLASS[ci][1]);
            this.inline("td", doDamage(targetHp, atack.target, atack.damage, i), TEXT_CLASS[ci][1]);
            this.end(); // tr
        }
    }

    /**
     * 「○→○　ダメージ」のテーブルを生成
     * @param gen
     * @param atack
     * @param originShips
     * @param targetShips
     */
    private void genAtackTableContent(BattleAtackDto atack,
            ShipBaseDto[] originShips, ShipBaseDto[] targetShips)
    {
        if (atack.origin.length == 0) {
            this.begin("tr", null);
            this.inline("td", "なし", null);
            this.end(); // tr
            return;
        }
        int ci = (atack.friendAtack) ? 0 : 1;

        this.begin("tr", null);
        this.inline("th", "艦", TEXT_CLASS[ci][0]);
        this.inline("th", "", null);
        this.inline("th", "艦", TEXT_CLASS[ci][1]);
        this.inline("th", "ダメージ", DAMAGE_CLASS[ci][1]);
        this.end(); // tr

        for (int i = 0; i < atack.origin.length; ++i) {
            this.begin("tr", null);
            this.inline("td", this.getShipName(originShips, atack.origin[i]), TEXT_CLASS[ci][0]);
            this.inline("td", "→", null);
            this.inline("td", this.getShipName(targetShips, atack.target[atack.ot[i]]), TEXT_CLASS[ci][1]);
            int critical = atack.critical != null ? atack.critical[i] : 0;
            this.inline("td", getDamageString(atack.ydam[i], critical), DAMAGE_CLASS[ci][1]);
            this.end(); // tr
        }
    }

    /**
     * 砲撃戦を生成
     * @param gen
     * @param atacks
     * @param friendShips
     * @param enemyShips
     * @param friendHp
     * @param enemyHp
     */
    private void genHougekiTableContent(List<BattleAtackDto> atacks,
            ShipBaseDto[] friendShips, ShipBaseDto[] enemyShips, int[] friendHp, int[] enemyHp) {
        this.begin("tr", null);
        this.inline("th", "", null);
        this.inline("th", "艦", null);
        this.inline("th", "", null);
        this.inline("th", "", null);
        this.inline("th", "艦", null);
        this.inline("th", "攻撃タイプ", null);
        this.inline("th", "ダメージ", null);
        this.inline("th", "残りHP", null);
        this.end(); // tr

        for (BattleAtackDto atack : atacks) {
            ShipBaseDto[] origin;
            ShipBaseDto[] target;
            int[] targetHp;
            String[] text;
            String[][] textClass;
            String[][] damageClass;
            if (atack.friendAtack) {
                origin = friendShips;
                target = enemyShips;
                targetHp = enemyHp;
                text = new String[] { "自軍", "敵軍" };
                textClass = TEXT_CLASS[0];
                damageClass = DAMAGE_CLASS[0];
            }
            else {
                origin = enemyShips;
                target = friendShips;
                targetHp = friendHp;
                text = new String[] { "敵軍", "自軍" };
                textClass = TEXT_CLASS[1];
                damageClass = DAMAGE_CLASS[1];
            }

            for (int i = 0; i < atack.damage.length; ++i) {
                this.begin("tr", null);

                if (i == 0) {
                    this.inline("td", text[0], null);
                    this.inline("td", this.getShipName(origin, atack.origin[0]), textClass[0]);
                }
                else {
                    this.inline("td", getColSpan(2), "", null);
                }

                this.inline("td", "→", null);
                this.inline("td", text[1], null);
                this.inline("td", this.getShipName(target, atack.target[i]), textClass[1]);
                if (i == 0) {
                    this.inline("td", getRowSpan(atack.damage.length), atack.getHougekiTypeString(), null);
                }
                int critical = atack.critical != null ? atack.critical[i] : 0;
                this.inline("td", getDamageString(atack.damage[i], critical), damageClass[1]);
                this.inline("td", doDamage(targetHp, atack.target, atack.damage, i), textClass[1]);

                this.end(); // tr
            }
        }
    }

    /**
     * 航空戦を生成
     * @param gen
     * @param air
     * @param title
     * @param friendShips
     * @param enemyShips
     * @param friendHp
     * @param enemyHp
     */
    private void genAirBattle(AirBattleDto air, String title,
            ShipBaseDto[] friendShips, ShipBaseDto[] enemyShips, int[] friendHp, int[] enemyHp)
    {
        if (air == null) {
            return;
        }

        this.inline("h3", title, AIR_TABLE_CLASS);
        String[] stage1 = air.getStage1DetailedString();
        String[] stage2 = air.getStage2DetailedString();
        String[] touch = air.getTouchPlane();
        this.begin("table", null);
        this.begin("tr", null);
        this.inline("th", getRowSpan(2), "", null);
        this.inline("th", getRowSpan(2), "制空権", null);
        this.inline("th", getColSpan(2), "艦載機", null);
        this.inline("th", getRowSpan(2), "触接", null);
        this.end(); // tr
        this.begin("tr", null);
        this.inline("th", "ステージ1", null);
        this.inline("th", "ステージ2", null);
        this.end(); // tr
        this.begin("tr", TEXT_CLASS[0][0]);
        this.inline("td", "自", null);
        this.inline("td", air.seiku, null);
        this.inline("td", stage1[0], null);
        this.inline("td", stage2[0], null);
        this.inline("td", touch[0], null);
        this.end(); // tr
        this.begin("tr", TEXT_CLASS[0][1]);
        this.inline("td", "敵", null);
        this.inline("td", "", null);
        this.inline("td", stage1[1], null);
        this.inline("td", stage2[1], null);
        this.inline("td", touch[1], null);
        this.end(); // tr
        this.end(); // table

        if (air.airFire != null) {
            this.inline("span", "対空カットイン", null);
            this.begin("table", null);
            this.begin("tr", null);
            this.inline("th", "発動艦娘", null);
            this.inline("th", "種別", null);
            this.inline("th", "使用装備", null);
            this.end(); // tr
            this.begin("tr", null);
            this.inline("td", this.getShipName(friendShips, air.airFire[0]), null);
            this.inline("td", air.getTaikuCutinString(), null);
            this.inline("td", air.getTaikuCutinItemsString(), null);
            this.end(); // tr
            this.end(); // table
        }

        if ((air.atacks == null) || (air.atacks.size() == 0)) {
            //this.inline("h4", "航空戦による攻撃なし", null);
            return;
        }

        for (BattleAtackDto atack : air.atacks) {

            ShipBaseDto[] origin;
            ShipBaseDto[] target;
            int[] targetHp;
            String[] text;
            String[][] tableClass;
            String[][] textClass;
            if (atack.friendAtack) {
                origin = friendShips;
                target = enemyShips;
                targetHp = enemyHp;
                text = new String[] { "自軍", "敵軍ダメージ" };
                tableClass = AIR_DAMAGE_TABLE_CLASS[0];
                textClass = TEXT_CLASS[0];
            }
            else {
                origin = enemyShips;
                target = friendShips;
                targetHp = friendHp;
                text = new String[] { "敵軍", "自軍ダメージ" };
                tableClass = AIR_DAMAGE_TABLE_CLASS[1];
                textClass = TEXT_CLASS[1];
            }

            if (atack.kind != AtackKind.AIRBASE) { // 基地航空隊の攻撃機表示は未実装
                this.begin("div", BOX_CLASS);
                this.inline("span", text[0] + ": 攻撃に参加した艦", null);
                this.begin("table", tableClass[0]);
                if (atack.origin.length == 0) {
                    this.begin("tr", null);
                    this.inline("td", "なし", null);
                    this.end(); // tr
                }
                else {
                    for (int i = 0; i < atack.origin.length; ++i) {
                        this.begin("tr", null);
                        this.inline("td", this.getShipName(origin, atack.origin[i]), textClass[0]);
                        this.end(); // tr
                    }
                }
                this.end(); // table
                this.end(); // p
            }

            this.begin("div", BOX_CLASS);
            this.inline("span", text[1], null);
            this.begin("table", tableClass[1]);
            this.genDamageTableContent(atack, target, targetHp);
            this.end(); // table
            this.end(); // p
        }
    }

    /**
     * 雷撃戦を生成
     * @param gen
     * @param raigeki
     * @param title
     * @param friendShips
     * @param enemyShips
     * @param friendHp
     * @param enemyHp
     */
    private void genRaigekiBattle(List<BattleAtackDto> raigeki, String title,
            ShipBaseDto[] friendShips, ShipBaseDto[] enemyShips, int[] friendHp, int[] enemyHp)
    {
        if ((raigeki == null) || (raigeki.size() == 0)) {
            return;
        }

        this.inline("h3", title, null);
        for (BattleAtackDto atack : raigeki) {
            ShipBaseDto[] origin;
            ShipBaseDto[] target;
            int[] targetHp;
            String[] text;
            String[][] textClass;
            if (atack.friendAtack) {
                origin = friendShips;
                target = enemyShips;
                targetHp = enemyHp;
                text = new String[] { "自軍攻撃", "敵軍ダメージ" };
                textClass = RAIGEKI_DAMAGE_TABLE_CLASS[0];
            }
            else {
                origin = enemyShips;
                target = friendShips;
                targetHp = friendHp;
                text = new String[] { "敵軍攻撃", "自軍ダメージ" };
                textClass = RAIGEKI_DAMAGE_TABLE_CLASS[1];
            }

            // 攻撃
            this.begin("div", BOX_CLASS);
            this.inline("span", text[0], null);
            this.begin("table", textClass[0]);
            this.genAtackTableContent(atack, origin, target);
            this.end(); // table
            this.end(); // p

            // ダメージ
            this.begin("div", BOX_CLASS);
            this.inline("span", text[1], null);
            this.begin("table", textClass[1]);
            this.genDamageTableContent(atack, target, targetHp);
            this.end(); // table
            this.end(); // p
        }
    }

    /**
     * １回目の砲撃の後、雷撃を行う戦闘か？
     * 空母機動部隊の昼戦、および、敵連合艦隊戦の昼戦の場合
     * @param kind
     * @return
     */
    private static boolean isCombinedMidRaigekiBattle(BattlePhaseKind kind) {
        return (kind == BattlePhaseKind.COMBINED_BATTLE) || (kind == BattlePhaseKind.COMBINED_EC_BATTLE);
    }

    /**
     * フェイズを生成
     * @param gen
     * @param battle
     * @param index
     */
    private void genPhase(BattleExDto battle, int index,
            ShipBaseDto[] friendShips, ShipBaseDto[] enemyShips, int[] friendHp, int[] enemyHp)
    {
        BattleExDto.Phase phase = battle.getPhaseList().get(index);

        List<AirBattleDto> airList = new ArrayList<>();
        List<List<BattleAtackDto>> hougekiList = new ArrayList<>();

        if (phase.getAir() != null)
            airList.add(phase.getAir());
        if (phase.getAir2() != null)
            airList.add(phase.getAir2());
        if (phase.getHougeki1() != null)
            hougekiList.add(phase.getHougeki1());
        if (phase.getHougeki2() != null)
            hougekiList.add(phase.getHougeki2());
        if (phase.getHougeki3() != null)
            hougekiList.add(phase.getHougeki3());

        // 基地航空隊
        List<AirBattleDto> airBaseList = phase.getAirBase();
        if (airBaseList != null) {
            for (int i = 0; i < airBaseList.size(); ++i) {
                AirBattleDto attack = airBaseList.get(i);
                this.genAirBattle(attack, "基地航空隊攻撃(" + (i + 1) + "/" + airBaseList.size() + ")",
                        friendShips, enemyShips, friendHp, enemyHp);
            }
        }

        // 航空戦 → 支援艦隊による攻撃 →　開幕対潜 → 開幕 → 航空戦２回目
        for (int i = 0; i < airList.size(); ++i) {
            this.genAirBattle(airList.get(i), "航空戦(" + (i + 1) + "/" + airList.size() + ")",
                    friendShips, enemyShips, friendHp, enemyHp);

            if (i == 0) {
                if (phase.getSupport() != null) {
                    for (BattleAtackDto atack : phase.getSupport()) {
                        this.inline("span", "支援艦隊による攻撃", null);
                        this.begin("table", DAMAGE_TABLE_CLASS[1]);
                        this.genDamageTableContent(atack, enemyShips, enemyHp);
                        this.end(); // table
                    }
                }
                if (phase.getOpeningTaisen() != null) {
                    this.inline("h3", "対潜先制爆雷攻撃", null);
                    this.begin("table", null);
                    this.genHougekiTableContent(phase.getOpeningTaisen(), friendShips, enemyShips, friendHp, enemyHp);
                    this.end(); // table
                }
                if (phase.getOpening() != null) {
                    this.genRaigekiBattle(phase.getOpening(), "開幕",
                            friendShips, enemyShips, friendHp, enemyHp);
                }
            }
        }

        // 夜戦
        if (phase.getHougeki() != null) {
            //
            if ((phase.getTouchPlane() != null) || (phase.getFlarePos() != null)) {
                String[] touch = AirBattleDto.toTouchPlaneString(phase.getTouchPlane());
                String[] flare = { "", "" };
                int[] flarePos = phase.getFlarePos();
                if (flarePos != null) {
                    if (flarePos[0] != -1) {
                        int base = phase.getKind().isHougekiSecond() ? 6 : 0;
                        flare[0] = this.getShipName(friendShips, (flarePos[0] - 1) + base);
                    }
                    if (flarePos[1] != -1) {
                        int base = phase.isEnemySecond() ? 6 : 0;
                        flare[1] = this.getShipName(enemyShips, (flarePos[1] - 1) + base);
                    }
                }

                this.begin("table", null);
                this.begin("tr", null);
                this.inline("th", "", null);
                this.inline("th", "触接", null);
                this.inline("th", "照明弾", null);
                this.end(); // tr
                this.begin("tr", null);
                this.inline("td", "自", null);
                this.inline("td", touch[0], null);
                this.inline("td", flare[0], null);
                this.end(); // tr
                this.begin("tr", null);
                this.inline("td", "敵", null);
                this.inline("td", touch[1], null);
                this.inline("td", flare[1], null);
                this.end(); // tr
                this.end(); // table
            }

            this.inline("h3", "砲雷撃", null);
            this.begin("table", null);
            this.genHougekiTableContent(phase.getHougeki(), friendShips, enemyShips, friendHp, enemyHp);
            this.end(); // table
        }

        // 砲撃+雷撃
        for (int i = 0; i < hougekiList.size(); ++i) {
            this.inline("h3", "砲撃(" + (i + 1) + "/" + hougekiList.size() + ")", null);
            this.begin("table", null);
            this.genHougekiTableContent(hougekiList.get(i), friendShips, enemyShips, friendHp, enemyHp);
            this.end(); // table

            if ((i == 0) && isCombinedMidRaigekiBattle(phase.getKind())) {
                // 空母機動部隊の昼戦の場合はここで雷撃
                this.genRaigekiBattle(phase.getRaigeki(), "雷撃戦",
                        friendShips, enemyShips, friendHp, enemyHp);
            }
        }

        // 空母機動部隊の昼戦以外の雷撃
        if (isCombinedMidRaigekiBattle(phase.getKind()) == false) {
            this.genRaigekiBattle(phase.getRaigeki(), "雷撃戦",
                    friendShips, enemyShips, friendHp, enemyHp);
        }
    }

    /**
     * フェイズのダメージ合計を計算
     * @param friend
     * @param enemy
     * @param phase
     */
    private void computeDamages(int[] friend, int[] enemy, BattleExDto.Phase phase) {
        for (int i = 0; i < friend.length; ++i) {
            friend[i] = 0;
        }
        for (int i = 0; i < enemy.length; ++i) {
            enemy[i] = 0;
        }
        for (BattleAtackDto[] atacks : phase.getAtackSequence()) {
            if (atacks != null) {
                for (BattleAtackDto dto : atacks) {
                    for (int i = 0; i < dto.target.length; ++i) {
                        int target = dto.target[i];
                        int damage = dto.damage[i];
                        if (dto.friendAtack) {
                            enemy[target] += damage;
                        }
                        else {
                            friend[target] += damage;
                        }
                    }
                }
            }
        }
    }

    /**
     * 開始時HPとダメージ -> ダメージと戦闘後のHP
     * @param start
     * @param inDam
     * @param offset
     * @param dam
     * @param after
     */
    private void storeDamageAndHp(int[] start, int[] inDam, int offset, int[] dam, int[] after) {
        if (start != null) {
            for (int i = 0; i < start.length; ++i) {
                dam[i] = inDam[offset + i];
                after[i] = Math.max(0, start[i] - inDam[offset + i]);
            }
        }
    }

    /**
     *  パラメータテーブルに表示するHPを計算
     *  配列インデックスは [艦隊][now, max, フェイズ, ダメージ][艦]
     * @param battle
     * @return
     */
    private int[][][] calcHP(BattleExDto battle) {
        int[][][] hp = new int[4][2 + (battle.getPhaseList().size() * 2)][6];
        int[][] startHp = new int[][] {
                battle.getStartFriendHp(),
                battle.getStartFriendHpCombined(),
                battle.getStartEnemyHp(),
                battle.getStartEnemyHpCombined() };

        hp[0][0] = battle.getStartFriendHp();
        hp[1][0] = battle.getStartFriendHpCombined();
        hp[2][0] = battle.getStartEnemyHp();
        hp[3][0] = battle.getStartEnemyHpCombined();
        hp[0][1] = battle.getMaxFriendHp();
        hp[1][1] = battle.getMaxFriendHpCombined();
        hp[2][1] = battle.getMaxEnemyHp();
        hp[3][1] = battle.getMaxEnemyHpCombined();

        int[] friendDamages = new int[12];
        int[] enemyDamages = new int[12];
        for (int pi = 0; pi < battle.getPhaseList().size(); ++pi) {
            BattleExDto.Phase phase = battle.getPhaseList().get(pi);
            this.computeDamages(friendDamages, enemyDamages, phase);
            this.storeDamageAndHp(startHp[0], friendDamages, 0, hp[0][(pi * 2) + 2], hp[0][(pi * 2) + 3]);
            this.storeDamageAndHp(startHp[1], friendDamages, 6, hp[1][(pi * 2) + 2], hp[1][(pi * 2) + 3]);
            this.storeDamageAndHp(startHp[2], enemyDamages, 0, hp[2][(pi * 2) + 2], hp[2][(pi * 2) + 3]);
            this.storeDamageAndHp(startHp[3], enemyDamages, 6, hp[3][(pi * 2) + 2], hp[3][(pi * 2) + 3]);
            startHp = new int[][] {
                    hp[0][(pi * 2) + 3], hp[1][(pi * 2) + 3],
                    hp[2][(pi * 2) + 3], hp[3][(pi * 2) + 3] };
        }
        return hp;
    }

    private void genResultTable(BattleResultDto result, BattleExDto detail) {
        this.begin("table", null);
        this.begin("tr", null);
        this.inline("td", "ランク", null);
        this.inline("td", detail.getRank().toString(), null);
        this.end(); // tr

        // MVP
        String mvp1 = "なし";
        String mvp2 = "なし";
        if (detail.getMvp() != -1) { // 敗北Eの時はMVPなし
            mvp1 = detail.getDock().getShips().get(detail.getMvp() - 1).getFriendlyName();
        }
        if (detail.isCombined()) {
            if (detail.getMvpCombined() != -1) { // 敗北Eの時はMVPなし
                mvp2 = detail.getDockCombined().getShips()
                        .get(detail.getMvpCombined() - 1).getFriendlyName();
            }
        }

        if (detail.isCombined()) {
            this.begin("tr", null);
            this.inline("td", "MVP(第一艦隊)", null);
            this.inline("td", mvp1, null);
            this.end(); // tr
            this.begin("tr", null);
            this.inline("td", "MVP(第二艦隊)", null);
            this.inline("td", mvp2, null);
            this.end(); // tr
        }
        else {
            this.begin("tr", null);
            this.inline("td", "MVP", null);
            this.inline("td", mvp1, null);
            this.end(); // tr
        }
        this.begin("tr", null);
        this.inline("td", "ドロップ", null);
        String dropText = StringUtils.join(
                detail.getDropName(),
                (detail.isDropShip() && detail.isDropItem()) ? "," : null,
                detail.getDropItemName());
        this.inline("td", StringUtils.isEmpty(dropText) ? "なし" : dropText, null);
        this.end(); // tr
        this.end(); // table
    }

    /** 戦闘結果が不完全の場合はnullが返ることがある
     * @param title HTMLのタイトル
     * @param result 戦闘結果概要データ
     * @param battle 戦闘結果詳細データ
     * @param getCharset charsetを生成するか。生成する場合UTF-8が指定される
     * @return 生成されたHTML
     * @throws java.io.IOException ファイルの書き込みに失敗した
     *  */
    public String generateHTML(String title, BattleResultDto result, BattleExDto battle, boolean genCharset)
            throws IOException
    {
        if (battle.isCompleteResult() == false) {
            return null;
        }

        this.genHeader(title, genCharset);
        this.begin("body", null);

        String[] sectionTitleClass = new String[] { "sec-title" };

        // タイトル
        String time = dateFormat.format(result.getBattleDate());
        String header;
        if (battle.isPractice()) {
            header = "「" + battle.getEnemyName() + "」との演習 (" + time + ")";
        }
        else {
            header = result.getMapCell().detailedString() + " (" + time + ")";
        }
        this.inline("div", "<h1>" + header + "</h1>", new String[] { "title" });

        // 結果
        this.inline("div", "<h2>結果</h2>", sectionTitleClass);
        this.genResultTable(result, battle);

        // パラメータテーブル生成 //
        this.inline("div", "<h2>パラメータ</h2>", sectionTitleClass);

        int[][][] hpList = this.calcHP(battle);
        String[] phaseName = (battle.getPhase1().isNight() ?
                new String[] { "夜戦後", "昼戦後" } : new String[] { "昼戦後", "夜戦後" });
        this.genParmeters(battle.getDock().getName(),
                battle.getDock().getShips(), hpList[0], phaseName, battle.getHqLv(), false);
        if (battle.isCombined()) {
            this.genParmeters(battle.getDockCombined().getName(),
                    battle.getDockCombined().getShips(), hpList[1], phaseName, battle.getHqLv(), true);
        }
        this.genParmeters(battle.getEnemyName(), battle.getEnemy(), hpList[2], phaseName, 0, false);
        if (battle.isEnemyCombined()) {
            this.genParmeters("敵護衛部隊",
                    battle.getEnemyCombined(), hpList[3], phaseName, 0, true);
        }

        // 装備を生成 //
        this.genSlotitemTable(battle.getDock().getShips(), false);
        if (battle.isCombined()) {
            this.genSlotitemTable(battle.getDockCombined().getShips(), true);
        }
        this.genSlotitemTable(battle.getEnemy(), false);
        if (battle.isEnemyCombined()) {
            this.genSlotitemTable(battle.getEnemyCombined(), true);
        }

        // 会敵情報 //
        this.inline("div", "<h2>会敵情報</h2>", sectionTitleClass);

        this.genFormation(battle);

        // フェイズ //
        ShipBaseDto[] friendShips = new ShipBaseDto[12];
        ShipBaseDto[] enemyShips = new ShipBaseDto[12];
        int[] friendHp = new int[12];
        int[] enemyHp = new int[12];

        copyToOffset(battle.getDock().getShips(), friendShips, 0);
        copyToOffset(battle.getEnemy(), enemyShips, 0);
        copyToOffset(battle.getStartFriendHp(), friendHp, 0);
        copyToOffset(battle.getStartEnemyHp(), enemyHp, 0);
        if (battle.isCombined()) {
            copyToOffset(battle.getDockCombined().getShips(), friendShips, 6);
            copyToOffset(battle.getStartFriendHpCombined(), friendHp, 6);
        }
        if (battle.isEnemyCombined()) {
            copyToOffset(battle.getEnemyCombined(), enemyShips, 6);
            copyToOffset(battle.getStartEnemyHpCombined(), enemyHp, 6);
        }

        int numPhases = battle.getPhaseList().size();
        for (int i = 0; i < numPhases; ++i) {
            Phase phase = battle.getPhaseList().get(i);
            String phaseTitle = (i + 1) + "/" + numPhases + "フェイズ: " + (phase.isNight() ? "夜戦" : "昼戦");
            this.inline("div", "<h2>" + phaseTitle + "</h2>", sectionTitleClass);

            this.genPhase(battle, i, friendShips, enemyShips, friendHp, enemyHp);
        }

        this.inline("p", "※<b>太字</b>はクリティカルヒットです<br>" +
                "<i>Generated by " + AppConstants.TITLEBAR_TEXT + "  (BattleEx ver." + battle.getExVersion() + ")"
                + "</i>", null);
        this.end(); // body
        return this.result();
    }
}
