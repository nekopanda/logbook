/**
 * 
 */
package logbook.gui;

import java.util.List;

import logbook.dto.BattleExDto;
import logbook.dto.DockDto;
import logbook.dto.EnemyShipDto;
import logbook.dto.ShipDto;
import logbook.gui.logic.DamageRate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Nekopanda
 * 戦況ウィンドウ通常版
 */
public class BattleWindowLarge extends BattleWindow {

    private Label title;

    // 名前
    private final Label[] friendLabels = new Label[12];
    // 0,1: 開始前, 2,3,4: 昼戦後, 5,6,7: 夜戦後
    private final Label[][] friendHpLabels = new Label[8][12];
    private final Label[][] enemyHpLabels = new Label[8][6];

    private final Label[] hpLabels = new Label[2];

    public BattleWindowLarge(Shell parent, MenuItem menuItem) {
        super(parent, menuItem);
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
        this.beginCombined();
        this.resultLabel[1] = this.addLabelWithSpan("結果予想", numColumns, 1);//6-17
        this.endCombined();
        this.resultLabel[2] = this.addLabelWithSpan("結果予想", numColumns, 1);//6-17

        this.clearText();
    }

    @Override
    protected void clearText() {
        super.clearText();

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
            for (int c = 0; c < 8; ++c) {
                this.enemyHpLabels[c][i].setText("");
                if ((c == 4) || (c == 7))
                    setLabelNone(this.enemyHpLabels[c][i]);
            }
        }
    }

    private void printDock(DockDto dock, int base) {
        List<ShipDto> ships = dock.getShips();
        for (int i = 0; i < ships.size(); ++i) {
            ShipDto ship = ships.get(i);
            this.friendLabels[base + i].setText(String.valueOf(i + 1) + "." +
                    ship.getFriendlyName());
            printFriendHp(this.friendHpLabels, base + i, ship.getNowhp(), ship.getMaxhp());
        }
    }

    @Override
    protected void printDock() {
        super.printDock();

        List<DockDto> docks = this.getDocks();
        if (docks == null)
            return;

        this.setCombinedMode(docks.size() == 2);
        for (int i = 0; i < docks.size(); ++i) {
            DockDto dock = docks.get(i);
            this.printDock(dock, i * 6);
        }
    }

    @Override
    protected void printMap() {
        super.printMap();

        if (this.getMapCellDto() == null)
            return;

        this.title.setText(this.getMapCellDto().detailedString());
    }

    private static void printDamageLebel(Label label, int nowhp, int maxhp, boolean friend) {
        DamageRate rate = DamageRate.fromHP(nowhp, maxhp);
        label.setText(rate.toString());
        if (friend) {
            label.setBackground(rate.getFriendBackground());
            label.setForeground(rate.getFriendForeground());
        }
        else {
            label.setBackground(rate.getEnemyBackground());
            label.setForeground(rate.getEnemyForeground());
        }
    }

    private static void printFriendHp(Label[][] labels, int index, int nowhp, int maxhp) {
        printHp(labels[0][index], nowhp, maxhp);
        printDamageLebel(labels[1][index], nowhp, maxhp, true);
    }

    private static void printHp(
            Label[][] labels, int base1, int base2, int[] dam, int[] nowhp, int[] maxhp, boolean friend)
    {
        for (int i = 0; i < nowhp.length; ++i) {
            labels[base1 + 0][base2 + i].setText(String.valueOf(dam[base2 + i]));
            labels[base1 + 1][base2 + i].setText(String.valueOf(nowhp[i]));
            printDamageLebel(labels[base1 + 2][base2 + i], nowhp[i], maxhp[i], friend);
        }
    }

    @Override
    protected void printBattle() {
        super.printBattle();

        BattleExDto battle = this.getBattle();
        BattleExDto.Phase phase1 = battle.getPhase1();
        BattleExDto.Phase phase2 = battle.getPhase2();
        BattleExDto.Phase lastPhase = battle.getLastPhase();

        if (lastPhase == null)
            return;

        // 初戦が夜戦？
        if (phase1.isNight()) {
            this.hpLabels[0].setText(AFTER_NIGHT);
            this.hpLabels[1].setText(AFTER_DAY);
        }
        else {
            this.hpLabels[0].setText(AFTER_DAY);
            this.hpLabels[1].setText(AFTER_NIGHT);
        }

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
        List<EnemyShipDto> enemyShips = battle.getEnemy();
        int[] maxEnemyHp = battle.getMaxEnemyHp();
        for (int i = 0; i < enemyShips.size(); ++i) {
            EnemyShipDto ship = enemyShips.get(i);
            this.enemyLabels[i].setText(String.valueOf(i + 1) + "." + ship.getFriendlyName());
            this.enemyHpLabels[0][i].setText(String.valueOf(maxEnemyHp[i]) + "/" + maxEnemyHp[i]);
        }

        // 昼戦後HP
        printHp(this.friendHpLabels, 2, 0, this.friendDamages[0], phase1.getNowFriendHp(), friendMaxHp[0], true);
        if (battle.isCombined()) {
            printHp(this.friendHpLabels, 2, 6, this.friendDamages[0], phase1.getNowFriendHpCombined(),
                    friendMaxHp[1], true);
        }
        printHp(this.enemyHpLabels, 2, 0, this.enemyDamages[0], phase1.getNowEnemyHp(), maxEnemyHp, false);

        // 夜戦後HP
        if (phase2 != null) {
            printHp(this.friendHpLabels, 5, 0, this.friendDamages[1], phase2.getNowFriendHp(), friendMaxHp[0],
                    true);
            if (battle.isCombined()) {
                printHp(this.friendHpLabels, 5, 6, this.friendDamages[1], phase2.getNowFriendHpCombined(),
                        friendMaxHp[1], true);
            }
            printHp(this.enemyHpLabels, 5, 0, this.enemyDamages[1], phase2.getNowEnemyHp(), maxEnemyHp, false);
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
