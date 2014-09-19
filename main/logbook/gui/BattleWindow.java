/**
 * 
 */
package logbook.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import logbook.constants.AppConstants;
import logbook.dto.AirBattleDto;
import logbook.dto.BattleAtackDto;
import logbook.dto.BattleExDto;
import logbook.dto.DockDto;
import logbook.dto.ShipDto;
import logbook.dto.ShipInfoDto;
import logbook.internal.EnemyData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * @author Nekopanda
 *
 */
public class BattleWindow extends BattleWindowBase {

    private Label title;
    // 0: 名前, 1: cond, 2: 燃料, 3: 弾薬
    private final Label[] friendLabels = new Label[12];
    // 名前
    private final Label[] enemyLabels = new Label[6];
    // 0,1: 開始前, 2,3,4: 昼戦後, 5,6,7: 夜戦後
    private final Label[][] friendHpLabels = new Label[8][12];
    private final Label[][] enemyHpLabels = new Label[8][6];

    private final Label[][] infoLabels = new Label[2][12];

    private Label matchLabel;
    private final Label resultLabel[] = new Label[2];

    private final Label[] hpLabels = new Label[2];

    private final int[] yDamages = new int[12];
    private final int[][] friendDamages = new int[2][12];
    private final int[][] enemyDamages = new int[2][6];

    private static String AFTER_DAY = "昼戦後";
    private static String AFTER_NIGHT = "夜戦後";
    private static String FORM_PREFIX = "陣形:";
    private static String TOUCH_PREFIX = "触接:";
    private static String SAKUTEKI_PREFIX = "索敵:";

    /**
     * Create the dialog.
     * @param parent
     */
    public BattleWindow(Shell parent, MenuItem menuItem) {
        super(parent, menuItem, "戦況");
    }

    private void addInfoLabel(Label[] labels, int row, int width) {
        switch (row) {
        case 0:
            labels[0] = this.addLabel("艦隊名", width, SWT.CENTER, 2, 1);
            break;
        case 1:
            labels[1] = this.addLabelWithSize("陣形", width / 2);
            labels[2] = this.addLabelWithSize("触接", width / 2);
            break;
        case 2:
            labels[3] = this.addLabel("索敵", width, SWT.CENTER, 2, 1);
            break;
        case 3:
            labels[4] = this.addLabelWithSize("航空戦:", width / 2);
            labels[5] = this.addLabelWithSize("制空状態", width / 2);
            break;
        case 4:
            labels[6] = this.addLabelWithSize("stage1", width / 2);
            labels[7] = this.addLabelWithSize("stage2", width / 2);
            break;
        case 5:
            labels[8] = this.addLabelWithSize("ロスト1", width / 2);
            labels[9] = this.addLabelWithSize("ロスト2", width / 2);
            break;
        case 6:
            labels[10] = this.addLabelWithSize("ロスト3", width / 2);
            labels[11] = this.addLabelWithSize("ロスト4", width / 2);
            break;
        default:
            this.skipSlot();
            this.skipSlot();
            break;
        }
    }

    private void addEnemyInfoLabel(Label[] labels, int row, int width) {
        switch (row) {
        case 0:
            labels[0] = this.addLabel("艦隊名", width, SWT.CENTER, 2, 1);
            break;
        case 1:
            labels[1] = this.addLabelWithSize("陣形", width / 2);
            labels[2] = this.addLabelWithSize("触接", width / 2);
            break;
        case 2:
            labels[3] = this.addLabel("索敵", width, SWT.CENTER, 2, 1);
            break;
        case 3:
            labels[4] = labels[5] = labels[6] = this.addLabelWithSize("stage1", width / 2);
            labels[7] = this.addLabelWithSize("stage2", width / 2);
            break;
        case 4:
            labels[8] = this.addLabelWithSize("ロスト1", width / 2);
            labels[9] = this.addLabelWithSize("ロスト2", width / 2);
            break;
        case 5:
            labels[10] = this.addLabelWithSize("ロスト3", width / 2);
            labels[11] = this.addLabelWithSize("ロスト4", width / 2);
            break;
        }
    }

    private void createHpLabels(Label[][] labels, int i, int hpWidth, int damWidth, int statusWidth) {
        if ((i == 0) || (i == 6)) {
            this.addVerticalSeparator(6); //2
            this.addLabelWithSpan("→", 1, 6);//12
            this.addVerticalSeparator(6); //2
        }
        labels[2][i] = this.addLabelWithSize("0", damWidth); //13 HP
        labels[3][i] = this.addLabelWithSize("0", damWidth); //13 HP
        labels[4][i] = this.addLabelWithSize("健在", statusWidth); //14 HP
        if ((i == 0) || (i == 6)) {
            this.addVerticalSeparator(6); //2
            this.addLabelWithSpan("→", 1, 6);//15
            this.addVerticalSeparator(6); //2
        }
        labels[5][i] = this.addLabelWithSize("0", damWidth); //13 HP
        labels[6][i] = this.addLabelWithSize("0", damWidth); //13 HP
        labels[7][i] = this.addLabelWithSize("健在", statusWidth); //17 HP
    }

    @Override
    protected void createContents() {
        int numColumns = 20;

        GridLayout layout = new GridLayout(numColumns, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        this.getShell().setLayout(layout);

        //フォント取得
        this.title = new Label(this.getShell(), SWT.NONE);
        this.title.setFont(this.getBoldFont());
        this.title.setLayoutData(new GridData(SWT.FILL, SWT.LEFT, false, false, numColumns, 1));

        // 各カラムの最小幅を定義 //
        int battleWidth = 130;
        int nameWidth = 110;
        int hpWidth = 40;
        int statusWidth = 25;
        int damWidth = 28;

        this.skipSlot();//1
        this.skipSlot(); //2 separator
        this.addLabel("状態", battleWidth, SWT.CENTER, 2, 1);//3-4
        this.skipSlot(); //5 separator
        this.addLabelWithSize("艦名", nameWidth);//6
        this.addLabelWithSize("HP", hpWidth);//7-8
        this.addLabelWithSize("", statusWidth);//7-8
        this.skipSlot(); //5 separator
        this.addLabelWithSize("", 15);//9
        this.skipSlot(); //5 separator
        this.addLabelWithSize("Dmg", damWidth);//10
        this.addLabelWithSize("残", damWidth);//11
        this.addLabelWithSize("", statusWidth);//12
        this.skipSlot(); //5 separator
        this.addLabelWithSize("", 15);//13
        this.skipSlot(); //5 separator
        this.addLabelWithSize("Dmg", damWidth);//14
        this.addLabelWithSize("残", damWidth);//15
        this.addLabelWithSize("", statusWidth);//16

        this.addHorizontalSeparator(numColumns);

        // 味方エリア
        for (int i = 0; i < 12; ++i) {
            if (i == 6) {
                this.beginCombined();
            }
            if (i == 0) {
                this.addLabelWithSpan("自", 1, 6); //1
            }
            if (i == 6) {
                this.addLabelWithSpan("", 1, 6); //1
            }
            if ((i == 0) || (i == 6))
                this.addVerticalSeparator(6); //2
            this.addInfoLabel(this.infoLabels[0], i, battleWidth);//3-4
            if ((i == 0) || (i == 6))
                this.addVerticalSeparator(6);//5
            this.friendLabels[i] = this.addLabel("艦名" + (i + 1), nameWidth, SWT.LEFT, 1, 1);//6
            this.friendHpLabels[0][i] = this.addLabelWithSize("0/0", hpWidth); //10 HP
            this.friendHpLabels[1][i] = this.addLabelWithSize("健在", statusWidth); //10 HP
            this.createHpLabels(this.friendHpLabels, i, hpWidth, damWidth, statusWidth); // 10-17
        }

        this.endCombined();
        this.addHorizontalSeparator(numColumns);

        // 間
        this.skipSlot();//1
        this.skipSlot();//2
        this.matchLabel = this.addLabelWithSpan("会敵", 2, 1);//3-4
        this.addLabelWithSpan("", 2, 1);//5-6
        this.addLabelWithSpan("開始時", 2, 1);//7-8
        this.addLabelWithSpan("→", 3, 1);//8
        this.hpLabels[0] = this.addLabelWithSpan(AFTER_DAY, 3, 1);//9-11
        this.addLabelWithSpan("→", 3, 1);//12
        this.hpLabels[1] = this.addLabelWithSpan(AFTER_NIGHT, 3, 1);//13-15

        this.addHorizontalSeparator(numColumns);

        // 敵エリア
        for (int i = 0; i < 6; ++i) {
            if (i == 0) {
                this.addLabelWithSpan("敵", 1, 6); //1
                this.addVerticalSeparator(6); //2
            }
            this.addEnemyInfoLabel(this.infoLabels[1], i, battleWidth);//3-4
            if (i == 0)
                this.addVerticalSeparator(6); //5
            this.enemyLabels[i] = this.addLabel("艦名" + (i + 1), nameWidth, SWT.LEFT, 1, 1); //6
            this.enemyHpLabels[1][i] = this.enemyHpLabels[0][i] =
                    this.addLabelWithSpan("0/0", 2, 1); //10 HP
            this.createHpLabels(this.enemyHpLabels, i, hpWidth, damWidth, statusWidth); // 10-17
        }

        this.addHorizontalSeparator(numColumns);

        // 最後
        this.resultLabel[0] = this.addLabelWithSpan("結果予想", numColumns, 1);//6-17
        this.resultLabel[1] = this.addLabelWithSpan("結果予想", numColumns, 1);//6-17

        this.clearText();
    }

    private void clearText() {
        // 情報
        for (int i = 0; i < 12; ++i) {
            this.infoLabels[0][i].setText("");
            this.infoLabels[1][i].setText("");
        }

        // 味方
        for (int i = 0; i < 12; ++i) {
            this.friendLabels[i].setText("-");
            for (int c = 0; c < 8; ++c) {
                this.friendHpLabels[c][i].setText("");
                if ((c == 1) || (c == 4) || (c == 7))
                    setLabelNone(this.friendHpLabels[c][i]);
            }
        }

        // 敵
        for (int i = 0; i < 6; ++i) {
            this.enemyLabels[i].setText("-");
            for (int c = 0; c < 8; ++c) {
                this.enemyHpLabels[c][i].setText("");
                if ((c == 4) || (c == 7))
                    setLabelNone(this.enemyHpLabels[c][i]);
            }
        }

        // その他
        this.matchLabel.setText("");
        this.resultLabel[0].setText("");
        this.resultLabel[1].setText("");
    }

    private static void setLabelRed(Label label) {
        label.setBackground(SWTResourceManager.getColor(AppConstants.COND_RED_COLOR));
        label.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
    }

    private static void setLabelOrange(Label label) {
        label.setBackground(SWTResourceManager.getColor(AppConstants.COND_ORANGE_COLOR));
        label.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
    }

    private static void setLabelGreen(Label label) {
        label.setBackground(SWTResourceManager.getColor(AppConstants.COND_GREEN_COLOR));
        label.setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
    }

    private static void setLabelNone(Label label) {
        label.setBackground(null);
        label.setForeground(null);
    }

    private static void printHp(Label label, int nowhp, int maxhp) {
        label.setText(String.valueOf(nowhp) + "/" + maxhp);
    }

    private static void printHpStatus(Label statusLabel, int nowhp, int maxhp, boolean friend) {
        double rate = (double) nowhp / (double) maxhp;
        if (rate == 0.0) {
            statusLabel.setText("轟沈");
            if (friend)
                setLabelRed(statusLabel);
            else
                setLabelNone(statusLabel);
        }
        else if (rate <= AppConstants.BADLY_DAMAGE) {
            statusLabel.setText("大破");
            if (friend)
                setLabelRed(statusLabel);
            else
                setLabelGreen(statusLabel);
        }
        else if (rate <= AppConstants.HALF_DAMAGE) {
            statusLabel.setText("中破");
            if (friend)
                setLabelOrange(statusLabel);
            else
                setLabelGreen(statusLabel);
        }
        else if (rate <= AppConstants.SLIGHT_DAMAGE) {
            statusLabel.setText("小破");
            if (friend)
                setLabelNone(statusLabel);
            else
                setLabelGreen(statusLabel);
        }
        else {
            statusLabel.setText("健在");
            if (friend)
                setLabelNone(statusLabel);
            else
                setLabelGreen(statusLabel);
        }
    }

    private static void printFriendHp(Label[][] labels, int index, int nowhp, int maxhp) {
        printHp(labels[0][index], nowhp, maxhp);
        printHpStatus(labels[1][index], nowhp, maxhp, true);
    }

    private void printDock(DockDto dock, int base) {
        List<ShipDto> ships = dock.getShips();
        for (int i = 0; i < ships.size(); ++i) {
            ShipDto ship = ships.get(i);
            this.friendLabels[base + i].setText(String.valueOf(i + 1) + "." +
                    ship.getName() + "(Lv." + ship.getLv() + ")");
            printFriendHp(this.friendHpLabels, base + i, ship.getNowhp(), ship.getMaxhp());
        }
    }

    private void printDock() {
        List<DockDto> docks = this.getDocks();
        if (docks == null)
            return;

        this.setCombinedMode(docks.size() == 2);
        for (int i = 0; i < docks.size(); ++i) {
            DockDto dock = docks.get(i);
            if (i == 0) {
                this.infoLabels[0][0].setText(dock.getName());
            }
            this.printDock(dock, i * 6);
        }
    }

    private void printMap() {
        if (this.getMapCellDto() == null)
            return;

        this.title.setText(this.getMapCellDto().detailedString());
        EnemyData enemyData = this.getMapCellDto().getEnemyData();
        if (enemyData != null) {
            String name = enemyData.getEnemyName();
            if (name != null) {
                this.infoLabels[1][0].setText(name);
            }
            String[] ships = enemyData.getEnemyShips();
            for (int i = 0; i < 6; ++i) {
                this.enemyLabels[i].setText(String.valueOf(i + 1) + "." + ships[i]);
            }
            this.infoLabels[1][1].setText(FORM_PREFIX + enemyData.getFormation());
        }
    }

    private String toPlaneCount(int lost, int total) {
        int after = total - lost;
        return String.valueOf(total) + "→" + after /*+ "(-" + lost + ")"*/;
    }

    // idx: 味方=0, 敵=1
    private void printPlaneCount(Label[] labels, int base, AirBattleDto air, int idx) {
        labels[base + 0].setText("");
        labels[base + 1].setText("");
        if (air != null) {
            if (air.stage1 != null)
                labels[base + 0].setText(this.toPlaneCount(air.stage1[(idx * 2) + 0], air.stage1[(idx * 2) + 1]));
            if (air.stage2 != null)
                labels[base + 1].setText(this.toPlaneCount(air.stage2[(idx * 2) + 0], air.stage2[(idx * 2) + 1]));
        }
    }

    private void printHp(Label[][] labels, int base1, int base2, int[] dam, int[] nowhp, int[] maxhp, boolean friend) {
        for (int i = 0; i < nowhp.length; ++i) {
            labels[base1 + 0][base2 + i].setText(String.valueOf(dam[base2 + i]));
            labels[base1 + 1][base2 + i].setText(String.valueOf(nowhp[i]));
            printHpStatus(labels[base1 + 2][base2 + i], nowhp[i], maxhp[i], friend);
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

    private void printBattle() {
        BattleExDto battle = this.getBattle();
        BattleExDto.Phase phase1 = battle.getPhase1();
        BattleExDto.Phase phase2 = battle.getPhase2();
        BattleExDto.Phase lastPhase = battle.getLastPhase();

        if (lastPhase == null)
            return;

        // ダメージ計算
        int airDamage = 0;
        for (int i = 0; i < this.yDamages.length; ++i)
            this.yDamages[i] = 0;
        if (phase1 != null)
            airDamage += this.computeDamages(this.friendDamages[0], this.enemyDamages[0], this.yDamages, phase1);
        if (phase2 != null)
            airDamage += this.computeDamages(this.friendDamages[1], this.enemyDamages[1], this.yDamages, phase2);
        int[][] sortArray = new int[this.yDamages.length][2];
        for (int i = 0; i < this.yDamages.length; ++i) {
            sortArray[i][0] = this.yDamages[i];
            sortArray[i][1] = i;
        }
        Arrays.sort(sortArray, new Comparator<int[]>() {
            @Override
            public int compare(int[] d1, int[] d2) {
                return -Integer.compare(d1[0], d2[0]);
            }
        });
        List<ShipDto> MVPList = new ArrayList<ShipDto>();
        for (int i = 0; i < 2; ++i) {
            if (sortArray[i][0] == 0) {
                break;
            }
            MVPList.add(this.getFriendShips()[sortArray[i][1]]);
        }
        if (MVPList.size() == 0) {
            MVPList.add(this.getFriendShips()[0]);
        }

        // 初戦が夜戦？
        if (phase1.isNight()) {
            this.hpLabels[0].setText(AFTER_NIGHT);
            this.hpLabels[1].setText(AFTER_DAY);
        }
        else {
            this.hpLabels[0].setText(AFTER_DAY);
            this.hpLabels[1].setText(AFTER_NIGHT);
        }

        // 情報表示
        String[] formation = battle.getFormation();
        int[] touchPlane = lastPhase.getTouchPlane();
        String[] sakuteki = battle.getSakuteki();
        String seiku = lastPhase.getSeiku();
        AirBattleDto[] air = lastPhase.getAirBattleDto();
        double[] damageRate = lastPhase.getDamageRate();

        for (int i = 0; i < 2; ++i) {
            if (formation[i] != null)
                this.infoLabels[i][1].setText(FORM_PREFIX + formation[i]);
            if (touchPlane != null)
                this.infoLabels[i][2].setText(TOUCH_PREFIX + ((touchPlane[i] != -1) ? "あり" : "なし"));
            if (sakuteki != null)
                this.infoLabels[i][3].setText(SAKUTEKI_PREFIX + sakuteki[i]);
            if (i == 0) {
                this.infoLabels[i][4].setText("航空戦:");
                this.infoLabels[i][5].setText((seiku != null) ? seiku : "なし");
            }
            this.infoLabels[i][6].setText("stage1");
            this.infoLabels[i][7].setText("stage2");
            if (air != null) {
                this.printPlaneCount(this.infoLabels[i], 8, air[0], i);
                this.printPlaneCount(this.infoLabels[i], 10, air[1], i);
            }
        }

        this.matchLabel.setText(battle.getFormationMatch());

        String result0 = "MVP(砲雷のみ) ";
        for (int i = 0; i < MVPList.size(); ++i) {
            ShipDto ship = MVPList.get(i);
            result0 += String.format("%d: %s(%d)", i + 1,
                    (ship == null) ? "?" : ship.getName(), sortArray[i][0]);
            if (i != (MVPList.size() - 1))
                result0 += ", ";
        }
        result0 += " 航空戦ダメージ: " + airDamage;
        this.resultLabel[0].setText(result0);

        String rateString = (damageRate[0] == 0.0) ? "" :
                String.format(" (x%.3f)", damageRate[1] / damageRate[0]);
        this.resultLabel[1].setText(String.format("損害率 自: %.1f%% vs. 敵: %.1f%%%s 結果: %s",
                damageRate[0] * 100, damageRate[1] * 100, rateString, battle.getRank().toString()));

        // 味方
        int[][] friendStartHp = new int[][] { battle.getStartFriendHp(), battle.getStartFriendHpCombined() };
        int[][] friendMaxHp = new int[][] { battle.getMaxFriendHp(), battle.getMaxFriendHpCombined() };
        for (int i = 0; i < friendStartHp.length; ++i) {
            int[] startHp = friendStartHp[i];
            int[] maxHp = friendMaxHp[i];
            if (startHp != null) {
                for (int c = 0; c < startHp.length; ++c) {
                    printFriendHp(this.friendHpLabels, (i * 6) + c, startHp[c], maxHp[c]);
                }
            }
        }

        // 敵
        List<ShipInfoDto> enemyShips = battle.getEnemy();
        int[] maxEnemyHp = battle.getMaxEnemyHp();
        for (int i = 0; i < enemyShips.size(); ++i) {
            ShipInfoDto ship = enemyShips.get(i);
            this.enemyLabels[i].setText(String.valueOf(i + 1) + "." + ship.getEnemyShipName());
            this.enemyHpLabels[0][i].setText(String.valueOf(maxEnemyHp[i]) + "/" + maxEnemyHp[i]);
        }

        // 昼戦後HP
        this.printHp(this.friendHpLabels, 2, 0, this.friendDamages[0], phase1.getNowFriendHp(), friendMaxHp[0], true);
        if (battle.isCombined()) {
            this.printHp(this.friendHpLabels, 2, 6, this.friendDamages[0], phase1.getNowFriendHpCombined(),
                    friendMaxHp[1], true);
        }
        this.printHp(this.enemyHpLabels, 2, 0, this.enemyDamages[0], phase1.getNowEnemyHp(), maxEnemyHp, false);

        // 夜戦後HP
        if (phase2 != null) {
            this.printHp(this.friendHpLabels, 5, 0, this.friendDamages[1], phase2.getNowFriendHp(), friendMaxHp[0],
                    true);
            if (battle.isCombined()) {
                this.printHp(this.friendHpLabels, 5, 6, this.friendDamages[1], phase2.getNowFriendHpCombined(),
                        friendMaxHp[1], true);
            }
            this.printHp(this.enemyHpLabels, 5, 0, this.enemyDamages[1], phase2.getNowEnemyHp(), maxEnemyHp, false);
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
        } finally {
            this.endDraw();
        }
    }
}
