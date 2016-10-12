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
 * 戦況ウィンドウ（通常サイズ）
 */
public class BattleWindowLarge extends BattleWindow {

    // 名前
    private final Label[] friendLabels = new Label[12];
    // 0,1: 開始前, 2,3,4: 昼戦後, 5,6,7: 夜戦後
    private final Label[][] friendHpLabels = new Label[8][12];
    private final Label[][] enemyHpLabels = new Label[8][12];

    private final Label[] hpLabels = new Label[2];

    public BattleWindowLarge(Shell parent, MenuItem menuItem) {
        super(parent, menuItem);
    }

    private void addInfoLabel(Label[] labels, int row) {
        switch (row) {
        case 0:
            labels[0] = this.addLabelWithSpan("艦隊名", 2, 1);
            break;
        case 1:
            labels[1] = this.addLabel("陣形:単縦陣");
            labels[2] = this.addLabel("触接:あり");
            break;
        case 2:
            labels[3] = this.addLabelWithSpan("索敵", 2, 1);
            break;
        case 3:
            labels[4] = this.addLabel("航空戦:");
            labels[5] = this.addLabel("制空状態");
            break;
        case 4:
            labels[6] = this.addLabel("Stage1");
            labels[7] = this.addLabel("Stage2");
            break;
        case 5:
            labels[8] = this.addLabel("000→000");
            labels[9] = this.addLabel("000→000");
            break;
        case 6:
            labels[10] = this.addLabel("000→000");
            labels[11] = this.addLabel("000→000");
            break;
        default:
            this.skipSlot();
            this.skipSlot();
            break;
        }
    }

    private void addEnemyInfoLabel(Label[] labels, int row) {
        switch (row) {
        case 0:
            labels[0] = this.addLabelWithSpan("艦隊名", 2, 1);
            break;
        case 1:
            labels[1] = this.addLabel("陣形:単縦陣");
            labels[2] = this.addLabel("触接:あり");
            break;
        case 2:
            labels[3] = this.addLabelWithSpan("索敵:", 2, 1);
            break;
        case 3:
            labels[4] = labels[5] = labels[6] = this.addLabel("Stage1");
            labels[7] = this.addLabel("Stage2");
            break;
        case 4:
            labels[8] = this.addLabel("000→000");
            labels[9] = this.addLabel("000→000");
            break;
        case 5:
            labels[10] = this.addLabel("000→000");
            labels[11] = this.addLabel("000→000");
            break;
        default:
            this.skipSlot();
            this.skipSlot();
        }
    }

    private void createHpLabels(Label[][] labels, int i) {
        if ((i == 0) || (i == 6)) {
            this.addVerticalSeparator(6); //2
            this.addLabelWithSpan("→", 1, 6);//12
            this.addVerticalSeparator(6); //2
        }
        labels[2][i] = this.addLabel("000"); //13 HP
        labels[3][i] = this.addLabel("000"); //13 HP
        labels[4][i] = this.addLabel("健在"); //14 HP
        if ((i == 0) || (i == 6)) {
            this.addVerticalSeparator(6); //2
            this.addLabelWithSpan("→", 1, 6);//15
            this.addVerticalSeparator(6); //2
        }
        labels[5][i] = this.addLabel("000"); //13 HP
        labels[6][i] = this.addLabel("000"); //13 HP
        labels[7][i] = this.addLabel("健在"); //17 HP
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
        this.title.setText("出撃中ではありません");

        // 各カラムの最小幅を定義 //
        /*
        int battleWidth = 130;
        int nameWidth = 110;
        int hpWidth = 40;
        int statusWidth = 25;
        int damWidth = 28;
        */
        int nameWidth = 60;

        this.skipSlot();//1
        this.skipSlot(); //2 separator
        this.addLabelWithSpan("状態", 2, 1);//3-4
        this.skipSlot(); //5 separator
        this.addLabel("艦名", SWT.CENTER, nameWidth);//6
        this.addLabel("HP");//7-8
        this.addLabel("");//7-8
        this.skipSlot(); //5 separator
        this.addLabel("");//9
        this.skipSlot(); //5 separator
        this.addLabel("Dmg");//10
        this.addLabel("残");//11
        this.addLabel("");//12
        this.skipSlot(); //5 separator
        this.addLabel("");//13
        this.skipSlot(); //5 separator
        this.addLabel("Dmg");//14
        this.addLabel("残");//15
        this.addLabel("");//16

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
            this.addInfoLabel(this.infoLabels[0], i);//3-4
            if ((i == 0) || (i == 6))
                this.addVerticalSeparator(6);//5
            this.friendLabels[i] = this.addLabel("艦名" + (i + 1), SWT.LEFT, nameWidth);//6
            this.friendHpLabels[0][i] = this.addLabel("000/000"); //10 HP
            this.friendHpLabels[1][i] = this.addLabel("健在"); //10 HP
            this.createHpLabels(this.friendHpLabels, i); // 10-17
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
        for (int i = 0; i < 12; ++i) {
            if (i == 6) {
                this.beginEnemyCombined();
            }
            if (i == 0) {
                this.addLabelWithSpan("敵", 1, 6); //1
            }
            if (i == 6) {
                this.addLabelWithSpan("", 1, 6); //1
            }
            if ((i == 0) || (i == 6))
                this.addVerticalSeparator(6); //2
            this.addEnemyInfoLabel(this.infoLabels[1], i);//3-4
            if ((i == 0) || (i == 6))
                this.addVerticalSeparator(6);//5
            this.enemyLabels[i] = this.addLabel("艦名" + (i + 1), SWT.LEFT, nameWidth); //6
            this.enemyHpLabels[1][i] = this.enemyHpLabels[0][i] =
                    this.addLabelWithSpan("0000", 2, 1); //10 HP
            this.createHpLabels(this.enemyHpLabels, i); // 10-17
        }

        this.endEnemyCombined();
        this.addHorizontalSeparator(numColumns);

        // 最後
        this.resultLabel[0] = this.addLabel("結果予想", SWT.DEFAULT, SWT.CENTER, SWT.FILL, true, numColumns, 1);//6-17
        this.beginCombined();
        this.resultLabel[1] = this.addLabel("結果予想", SWT.DEFAULT, SWT.CENTER, SWT.FILL, true, numColumns, 1);//6-17
        this.endCombined();
        this.resultLabel[2] = this.addLabel("結果予想", SWT.DEFAULT, SWT.CENTER, SWT.FILL, true, numColumns, 1);//6-17

        // この状態で長さを決める
        this.getShell().layout();
    }

    @Override
    protected void clearText() {
        super.clearText();

        // 味方
        for (int i = 0; i < 12; ++i) {
            setLabelText(this.friendLabels[i], "-", "");
            for (int c = 0; c < 8; ++c) {
                setLabelText(this.friendHpLabels[c][i], "");
                if ((c == 1) || (c == 4) || (c == 7))
                    setLabelNone(this.friendHpLabels[c][i]);
            }
        }

        // 敵
        for (int i = 0; i < 12; ++i) {
            for (int c = 0; c < 8; ++c) {
                setLabelText(this.enemyHpLabels[c][i], "");
                if ((c == 4) || (c == 7))
                    setLabelNone(this.enemyHpLabels[c][i]);
            }
        }
    }

    private void printDock(DockDto dock, int base) {
        List<ShipDto> ships = dock.getShips();
        boolean[] escaped = dock.getEscaped();
        for (int i = 0; i < ships.size(); ++i) {
            ShipDto ship = ships.get(i);
            this.friendLabels[base + i].setText(String.valueOf(i + 1) + "." +
                    ship.getFriendlyName());
            this.friendLabels[base + i].setToolTipText(ship.getDetailedString());
            printFriendHp(this.friendHpLabels, base + i,
                    ship.getNowhp(), ship.getMaxhp(), true, (escaped != null) && escaped[i]);
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

    private static void setDamageLabel(Label label, DamageRate rate, boolean friend) {
        label.setText(rate.toString(friend));
        label.setBackground(rate.getBackground());
        label.setForeground(rate.getForeground());
    }

    private static void printDamageLebel(Label label, int nowhp, int maxhp, boolean friend) {
        setDamageLabel(label, DamageRate.fromHP(nowhp, maxhp), friend);
    }

    private static void printFriendHp(Label[][] labels, int index, int nowhp, int maxhp, boolean friend, boolean escaped) {
        if (escaped) {
            setDamageLabel(labels[1][index], DamageRate.ESCAPED, friend);
        }
        else {
            printHp(labels[0][index], nowhp, maxhp);
            printDamageLebel(labels[1][index], nowhp, maxhp, friend);
        }
    }

    private static void printHp(
            Label[][] labels, int base1, int base2, int[] dam, int[] nowhp, int[] maxhp, boolean[] escaped,
            boolean friend)
    {
        for (int i = 0; i < nowhp.length; ++i) {
            if ((escaped != null) && escaped[i]) {
                continue;
            }
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
        List<DockDto> docks = this.getDocks();
        for (int i = 0; i < friendStartHp.length; ++i) {
            int[] startHp = friendStartHp[i];
            int[] maxHp = friendMaxHp[i];
            if (startHp != null) {
                boolean[] dockEscaped = docks.get(i).getEscaped();
                for (int c = 0; c < startHp.length; ++c) {
                    printFriendHp(this.friendHpLabels, (i * 6) + c,
                            startHp[c], maxHp[c], true, (dockEscaped != null) && dockEscaped[c]);
                }
            }
        }

        // 敵
        List<EnemyShipDto> enemyShips = battle.getEnemy();
        List<EnemyShipDto> enemyShipsCombined = battle.getEnemyCombined();
        int[][] enemyMaxHp = new int[][] { battle.getMaxEnemyHp(), battle.getMaxEnemyHpCombined() };
        for (int i = 0; i < enemyMaxHp.length; ++i) {
            if ((i == 1) && (battle.isEnemyCombined() == false))
                break;

            List<EnemyShipDto> ships = (i == 0) ? enemyShips : enemyShipsCombined;
            int[] maxHp = enemyMaxHp[i];

            for (int c = 0; c < ships.size(); ++c) {
                EnemyShipDto ship = ships.get(c);
                int index = (i * 6) + c;
                this.enemyLabels[index].setText(String.valueOf(index + 1) + "." + ship.getFriendlyName());
                this.enemyLabels[index].setToolTipText(ship.getDetailedString());
                this.enemyHpLabels[0][index].setText(String.valueOf(maxHp[c]) + "/" + maxHp[c]);
            }
        }

        // 昼戦後HP
        printHp(this.friendHpLabels, 2, 0, this.friendDamages[0],
                phase1.getNowFriendHp(), friendMaxHp[0], docks.get(0).getEscaped(), true);
        if (battle.isCombined()) {
            printHp(this.friendHpLabels, 2, 6, this.friendDamages[0],
                    phase1.getNowFriendHpCombined(), friendMaxHp[1], docks.get(1).getEscaped(), true);
        }
        printHp(this.enemyHpLabels, 2, 0, this.enemyDamages[0], phase1.getNowEnemyHp(), enemyMaxHp[0], null, false);
        if (battle.isEnemyCombined()) {
            printHp(this.enemyHpLabels, 2, 6, this.enemyDamages[0],
                    phase1.getNowEnemyHpCombined(), enemyMaxHp[1], null, false);
        }

        // 夜戦後HP
        if (phase2 != null) {
            printHp(this.friendHpLabels, 5, 0, this.friendDamages[1],
                    phase2.getNowFriendHp(), friendMaxHp[0], docks.get(0).getEscaped(), true);
            if (battle.isCombined()) {
                printHp(this.friendHpLabels, 5, 6, this.friendDamages[1],
                        phase2.getNowFriendHpCombined(), friendMaxHp[1], docks.get(1).getEscaped(), true);
            }
            printHp(this.enemyHpLabels, 5, 0, this.enemyDamages[1], phase2.getNowEnemyHp(), enemyMaxHp[0], null, false);
            if (battle.isEnemyCombined()) {
                printHp(this.enemyHpLabels, 5, 6, this.enemyDamages[1],
                        phase2.getNowEnemyHpCombined(), enemyMaxHp[1], null, false);
            }
        }

        // 敵連合艦隊用レイアウトセット
        this.setEnemyCombinedMode(battle.isEnemyCombined());
    }
}
