/**
 * 
 */
package logbook.gui;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import logbook.constants.AppConstants;
import logbook.dto.AirBattleDto;
import logbook.dto.BattleAtackDto;
import logbook.dto.BattleExDto;
import logbook.dto.BattlePhaseKind;
import logbook.dto.DockDto;
import logbook.dto.ResultRank;
import logbook.dto.ShipDto;
import logbook.gui.logic.ColorManager;
import logbook.internal.LoggerHolder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Nekopanda
 *
 */
public class BattleWindow extends BattleWindowBase {
    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(BattleWindow.class);

    protected Label title;

    // 名前
    protected final Label[] enemyLabels = new Label[12];

    protected final Label[][] infoLabels = new Label[2][12];

    protected Label matchLabel;
    protected final Label resultLabel[] = new Label[3];

    protected final int[] yDamages = new int[12];
    protected final int[][] friendDamages = new int[2][12];
    protected final int[][] enemyDamages = new int[2][12];

    protected static String AFTER_DAY = "昼戦後";
    protected static String AFTER_NIGHT = "夜戦後";
    protected static String FORM_PREFIX = "陣形:";
    protected static String TOUCH_PREFIX = "触接:";
    protected static String SAKUTEKI_PREFIX = "索敵:";

    /**
     * Create the dialog.
     * @param parent
     */
    protected BattleWindow(Shell parent, MenuItem menuItem) {
        super(parent, menuItem, "戦況");
    }

    @Override
    protected void clearText() {
        // 情報
        for (int i = 0; i < 12; ++i) {
            setLabelText(this.infoLabels[0][i], "");
            setLabelText(this.infoLabels[1][i], "");
        }

        // 敵
        for (int i = 0; i < 12; ++i) {
            setLabelText(this.enemyLabels[i], "-", "");
        }

        // その他
        this.matchLabel.setText("");
        for (int i = 0; i < 3; ++i) {
            this.resultLabel[i].setText("");
            this.resultLabel[i].setBackground(null);
            this.resultLabel[i].setForeground(null);
        }
    }

    protected static void setLabelNone(Label label) {
        label.setBackground(null);
        label.setForeground(null);
    }

    protected static void printHp(Label label, int nowhp, int maxhp) {
        label.setText(String.valueOf(nowhp) + "/" + maxhp);
    }

    protected void printDock() {
        List<DockDto> docks = this.getDocks();
        if (docks == null)
            return;

        this.infoLabels[0][0].setText(docks.get(0).getName());
    }

    protected void printMap() {
        if (this.getMapCellDto() == null)
            return;

        // 2015/07/18 先読みできなくなった
        /*
        MapCellDto dto = this.getMapCellDto();
        EnemyData enemyData = dto.getEnemyData();
        if (dto.getEnemyId() == -1) {
            // 次のマスは敵がいない
        }
        else if (enemyData != null) {
            String name = enemyData.getEnemyName();
            if (!StringUtils.isEmpty(name)) {
                this.infoLabels[1][0].setText(name);
            }
            else {
                this.infoLabels[1][0].setText("KCRDB互換データ");
            }
            int[] ships = enemyData.getEnemyShipsId();
            for (int i = 0; i < 6; ++i) {
                ShipInfoDto shipinfo = Ship.get(String.valueOf(ships[i]));
                if (shipinfo != null) {
                    String tooltip = ShipBaseDto.makeDetailedString(
                            shipinfo.getFullName(), Item.fromIdList(shipinfo.getDefaultSlot()));
                    this.enemyLabels[i].setText(String.valueOf(i + 1) + "." + shipinfo.getFullName());
                    this.enemyLabels[i].setToolTipText(tooltip);
                }
            }
            this.infoLabels[1][1].setText(FORM_PREFIX + enemyData.getFormation());
        }
        else {
            this.infoLabels[1][0].setText("データがありません[" + dto.getEnemyId() + "]");
        }
        */
    }

    // idx: 味方=0, 敵=1
    private void printPlaneCount(Label[] labels, int base, AirBattleDto air, int idx) {
        labels[base + 0].setText("");
        labels[base + 1].setText("");
        if (air != null) {
            String[] short1 = air.getStage1ShortString();
            String[] short2 = air.getStage2ShortString();
            String[] detail1 = air.getStage1DetailedString();
            String[] detail2 = air.getStage2DetailedString();
            if (air.stage1 != null)
                setLabelText(labels[base + 0], short1[idx], detail1[idx]);
            if (air.stage2 != null)
                setLabelText(labels[base + 1], short2[idx], detail2[idx]);
        }
    }

    private int computeDamages(int[] friend, int[] enemy, int[] ydam, BattleExDto.Phase phase) {
        BattleAtackDto[][] sequence = phase.getAtackSequence();

        for (int i = 0; i < friend.length; ++i)
            friend[i] = 0;
        for (int i = 0; i < enemy.length; ++i)
            enemy[i] = 0;
        int airDamage = 0;

        for (BattleAtackDto[] atacks : sequence) {
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
                    if (dto.friendAtack) {
                        switch (dto.kind) {
                        case HOUGEKI:
                            for (int damage : dto.damage) {
                                ydam[dto.origin[0]] += damage;
                            }
                            break;
                        case RAIGEKI:
                            for (int i = 0; i < dto.origin.length; ++i) {
                                ydam[dto.origin[i]] += dto.ydam[i];
                            }
                            break;
                        case AIR:
                            for (int damage : dto.damage) {
                                airDamage += damage;
                            }
                            break;
                        default:
                            break;
                        }
                    }
                }
            }
        }

        return airDamage;
    }

    protected static class MVPShip {
        public ShipDto ship;
        public int ydam;

        MVPShip(ShipDto ship, int ydam) {
            this.ship = ship;
            this.ydam = ydam;
        }
    }

    /**
     * MVP候補を計算
     * @param ydam
     * @param ships
     * @return
     */
    private MVPShip[] computeMVP(int[] ydam, List<ShipDto> ships) {
        MVPShip[] sortArray = new MVPShip[ships.size()];
        for (int i = 0; i < ships.size(); ++i) {
            sortArray[i] = new MVPShip(ships.get(i), ydam[i]);
        }
        Arrays.sort(sortArray, new Comparator<MVPShip>() {
            @Override
            public int compare(MVPShip d1, MVPShip d2) {
                return -Integer.compare(d1.ydam, d2.ydam);
            }
        });
        int numPrintShips = 0;
        for (int i = 0; (i < 2) && (i < ships.size()); ++i) {
            if (sortArray[i].ydam == 0) {
                break;
            }
            numPrintShips++;
        }
        if (numPrintShips == 0) {
            numPrintShips = 1;
        }
        return Arrays.copyOf(sortArray, numPrintShips);
    }

    protected String getMVPText(MVPShip[] mvp, int airDamage) {
        if (mvp == null) {
            return "";
        }
        String result0 = "MVP(砲雷のみ) ";
        for (int i = 0; i < mvp.length; ++i) {
            ShipDto ship = mvp[i].ship;
            result0 += String.format("%d: %s(%d)", i + 1,
                    (ship == null) ? "?" : ship.getName(), mvp[i].ydam);
            if (i != (mvp.length - 1))
                result0 += ", ";
        }
        result0 += " 航空戦ダメージ: " + airDamage;
        return result0;
    }

    protected String getReulstText(double[] damageRate, String rank) {
        String rateString = (damageRate[0] == 0.0) ? "" :
                String.format(" (x%.3f)", damageRate[1] / damageRate[0]);
        return String.format("損害率 自: %.1f%% vs. 敵: %.1f%%%s 結果: %s",
                damageRate[0] * 100, damageRate[1] * 100, rateString, rank);
    }

    protected void printBattle() {
        BattleExDto battle = this.getBattle();
        BattleExDto.Phase phase1 = battle.getPhase1();
        BattleExDto.Phase phase2 = battle.getPhase2();
        BattleExDto.Phase lastPhase = battle.getLastPhase();
        List<ShipDto> friendShips = battle.getDock().getShips();
        List<ShipDto> friendShipsCombined = battle.isCombined() ? battle.getDockCombined().getShips() : null;

        if (lastPhase == null)
            return;

        // ダメージ計算
        int airDamage = 0;
        for (int i = 0; i < this.yDamages.length; ++i)
            this.yDamages[i] = 0;

        // 連合艦隊夜戦の場合、MVP計算時、昼戦は考慮しない
        if ((phase1 != null) && (lastPhase.getKind() != BattlePhaseKind.COMBINED_MIDNIGHT))
            airDamage += this.computeDamages(this.friendDamages[0], this.enemyDamages[0], this.yDamages, phase1);

        if (phase2 != null)
            airDamage += this.computeDamages(this.friendDamages[1], this.enemyDamages[1], this.yDamages, phase2);

        MVPShip[] mvp1 = this.computeMVP(Arrays.copyOf(this.yDamages, friendShips.size()), friendShips);
        MVPShip[] mvp2 = battle.isCombined() ? this.computeMVP(
                Arrays.copyOfRange(this.yDamages, 6, 6 + friendShipsCombined.size()), friendShipsCombined) : null;

        // 情報表示
        String[] formation = battle.getFormation();
        int[] rawTouchPlane = lastPhase.getTouchPlane();
        String[] touchPlane = null;
        String[] sakuteki = battle.getSakuteki();
        String seiku = lastPhase.getSeiku();
        AirBattleDto[] air = lastPhase.getAirBattleDto();
        double[] damageRate = lastPhase.getDamageRate();

        if (rawTouchPlane != null) {
            touchPlane = AirBattleDto.toTouchPlaneString(rawTouchPlane);
        }

        for (int i = 0; i < 2; ++i) {
            if (formation[i] != null) {
                setLabelText(this.infoLabels[i][1], FORM_PREFIX + formation[i]);
            }
            if (touchPlane != null)
                setLabelText(this.infoLabels[i][2],
                        TOUCH_PREFIX + ((rawTouchPlane[i] != -1) ? "あり" : "なし"),
                        TOUCH_PREFIX + touchPlane[i]);
            if (sakuteki != null) {
                setLabelText(this.infoLabels[i][3], SAKUTEKI_PREFIX + sakuteki[i]);
            }
            if (i == 0) {
                this.infoLabels[i][4].setText("航空戦:");
                this.infoLabels[i][5].setText((seiku != null) ? seiku : "なし");
            }
            this.infoLabels[i][6].setText("Stage1");
            this.infoLabels[i][7].setText("Stage2");
            if (air != null) {
                this.printPlaneCount(this.infoLabels[i], 8, air[0], i);
                this.printPlaneCount(this.infoLabels[i], 10, air[1], i);
            }
        }

        this.matchLabel.setText(battle.getFormationMatch());

        ResultRank rank = lastPhase.getEstimatedRank();
        this.resultLabel[0].setText(this.getMVPText(mvp1, airDamage));
        this.resultLabel[1].setText(this.getMVPText(mvp2, 0)); // 第二艦隊は航空戦ダメージゼロ
        this.resultLabel[2].setText(this.getReulstText(damageRate, rank.toString()));

        for (int i = 0; i < 3; ++i) {
            if ((rank == ResultRank.C) || (rank == ResultRank.D) || (rank == ResultRank.E)) {
                this.resultLabel[i].setBackground(ColorManager.getColor(AppConstants.LOSE_BATTLE_COLOR));
                this.resultLabel[i].setForeground(ColorManager.getColor(SWT.COLOR_WHITE));
            }
            else {
                this.resultLabel[i].setBackground(null);
                this.resultLabel[i].setForeground(null);
            }
        }
    }

    @Override
    protected void updateData(boolean start) {
        this.beginDraw();
        try {
            if (this.getBattle() != null) {
                this.printDock();
                this.printMap();
                this.printBattle();
            }
            else if (this.getDocks() == null) {
                // 出撃中でない
                this.clearText();
                this.title.setText("出撃中ではありません");
            }
            else if (this.getMapCellDto() == null) {
                // 移動中
                this.clearText();
                if (start) {
                    this.title.setText("出撃しました");
                }
                else {
                    this.title.setText("移動中...");
                }
                this.printDock();
            }
            else {
                // 移動中
                this.printMap();
            }
        } catch (Exception e) {
            LOG.get().warn("戦況ウィンドウの更新に失敗しました", e);
        } finally {
            this.endDraw();
        }
    }
}
