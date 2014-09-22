/**
 * 
 */
package logbook.gui;

import java.util.List;

import logbook.dto.BattleExDto;
import logbook.dto.DockDto;
import logbook.dto.EnemyShipDto;
import logbook.dto.ShipDto;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Nekopanda
 * 戦況ウィンドウ縮小版
 */
public class BattleWindowSmall extends BattleWindow {

    private final Composite[] infoCompo = new Composite[2];
    private Composite damageCompo;
    private Composite resultCompo;

    // 0,1: 開始前, 2,3,4: 後
    private final Label[][] friendHpLabels = new Label[5][12];
    private final Label[][] enemyHpLabels = new Label[5][6];

    public BattleWindowSmall(Shell parent, MenuItem menuItem) {
        super(parent, menuItem);
    }

    private static Label addLabel(Composite compo, String text, int width, int horizontalSpan) {
        Label label = new Label(compo, SWT.NONE);
        GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false, horizontalSpan, 1);
        gd.widthHint = width;
        label.setLayoutData(gd);
        label.setText(text);
        return label;
    }

    private void createHpHeaders(int hpWidth, int damWidth, int statusWidth) {
        addLabel(this.damageCompo, "開始時", hpWidth, 1);
        addLabel(this.damageCompo, "", statusWidth, 1);
        addLabel(this.damageCompo, "Dmg", damWidth, 1);
        addLabel(this.damageCompo, "残", damWidth, 1);
        addLabel(this.damageCompo, "", statusWidth, 1);
    }

    private void createHpLabels(Label[][] labels, int i, int hpWidth, int damWidth, int statusWidth) {
        labels[0][i] = addLabel(this.damageCompo, "0", hpWidth, 1); //0 開始時HP
        labels[1][i] = addLabel(this.damageCompo, "健在", statusWidth, 1); //1 開始時状態
        labels[2][i] = addLabel(this.damageCompo, "0", damWidth, 1); //2 Dmg
        labels[3][i] = addLabel(this.damageCompo, "0", damWidth, 1); //3 後HP
        labels[4][i] = addLabel(this.damageCompo, "健在", statusWidth, 1); //4 後状態
    }

    @Override
    protected void addVerticalSeparator(int span) {
        Label label = new Label(this.damageCompo, SWT.SEPARATOR | SWT.VERTICAL);
        GridData gd = new GridData(SWT.CENTER, SWT.FILL, false, false, 1, span);
        gd.widthHint = 10;
        label.setLayoutData(gd);
    }

    private GridLayout makeGridLayout(int numColumns) {
        GridLayout layout = new GridLayout(2, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        return layout;
    }

    @Override
    protected void createContents() {
        this.getShell().setLayout(this.makeGridLayout(3));

        int battleWidth = 130;
        int nameWidth = 110;
        int hpWidth = 40;
        int statusWidth = 25;
        int damWidth = 28;

        for (int i = 0; i < 2; ++i) {
            this.infoCompo[i] = new Composite(this.getShell(), SWT.BORDER);

            this.infoCompo[i].setLayout(this.makeGridLayout(2));

            if (i == 1) {
                this.matchLabel = addLabel(this.infoCompo[i], "会敵", battleWidth, 2);
            }

            this.infoLabels[i][0] = addLabel(this.infoCompo[i], "艦隊名", battleWidth, 2);
            this.infoLabels[i][1] = addLabel(this.infoCompo[i], "陣形", battleWidth, 1);
            this.infoLabels[i][2] = addLabel(this.infoCompo[i], "触接", battleWidth, 1);
            this.infoLabels[i][3] = addLabel(this.infoCompo[i], "索敵", battleWidth, 1);

            this.infoLabels[i][6] = addLabel(this.infoCompo[i], "Stage1", battleWidth, 1);
            if (i == 0) {
                this.infoLabels[i][4] = addLabel(this.infoCompo[i], "航空戦", battleWidth, 1);
                this.infoLabels[i][5] = addLabel(this.infoCompo[i], "制空状態", battleWidth, 1);
            }
            else {
                this.infoLabels[i][4] = this.infoLabels[i][5] = this.infoLabels[i][6];
            }

            this.infoLabels[i][7] = addLabel(this.infoCompo[i], "Stage2", battleWidth, 1);
            this.infoLabels[i][8] = addLabel(this.infoCompo[i], "ロスト1", battleWidth, 1);
            this.infoLabels[i][9] = addLabel(this.infoCompo[i], "ロスト2", battleWidth, 1);
            this.infoLabels[i][10] = addLabel(this.infoCompo[i], "ロスト3", battleWidth, 1);
            this.infoLabels[i][11] = addLabel(this.infoCompo[i], "ロスト4", battleWidth, 1);
        }

        int numColumns = 18;
        this.damageCompo = new Composite(this.getShell(), SWT.BORDER);
        this.damageCompo.setLayout(this.makeGridLayout(numColumns));

        // ヘッダ行作成
        this.createHpHeaders(hpWidth, damWidth, statusWidth);
        addLabel(this.damageCompo, "", SWT.DEFAULT, 1);
        this.createHpHeaders(hpWidth, damWidth, statusWidth);
        addLabel(this.damageCompo, "", SWT.DEFAULT, 1);
        this.createHpHeaders(hpWidth, damWidth, statusWidth);
        addLabel(this.damageCompo, "敵艦名", nameWidth, 1);

        // 中身作成
        for (int i = 0; i < 6; ++i) {
            this.createHpLabels(this.friendHpLabels, i, hpWidth, damWidth, statusWidth);
            if (i == 0)
                this.addVerticalSeparator(6);
            this.createHpLabels(this.friendHpLabels, i + 6, hpWidth, damWidth, statusWidth);
            if (i == 0)
                this.addVerticalSeparator(6);
            this.createHpLabels(this.enemyHpLabels, i, hpWidth, damWidth, statusWidth);
            this.enemyLabels[i] = addLabel(this.damageCompo, "-", nameWidth, 1);
        }

        // 結果表示領域
        this.resultCompo = new Composite(this.getShell(), SWT.BORDER);
        this.resultCompo.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false, 3, 1));
        this.resultCompo.setLayout(new FillLayout(SWT.HORIZONTAL));

        this.resultLabel[0] = new Label(this.resultCompo, SWT.NONE);
        this.resultLabel[1] = new Label(this.resultCompo, SWT.NONE);
    }

    @Override
    protected void clearText() {
        super.clearText();

        // 味方
        for (int i = 0; i < 12; ++i) {
            for (int c = 0; c < 5; ++c) {
                this.friendHpLabels[c][i].setText("");
                if ((c == 1) || (c == 4) || (c == 7))
                    setLabelNone(this.friendHpLabels[c][i]);
            }
        }

        // 敵
        for (int i = 0; i < 6; ++i) {
            for (int c = 0; c < 5; ++c) {
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
    protected void printBattle() {
        super.printBattle();

        BattleExDto battle = this.getBattle();
        BattleExDto.Phase lastPhase = battle.getLastPhase();
        int last = battle.getPhaseList().size() - 1;

        if (lastPhase == null)
            return;

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
        printHp(this.friendHpLabels, 2, 0, this.friendDamages[last], lastPhase.getNowFriendHp(), friendMaxHp[0], true);
        if (battle.isCombined()) {
            printHp(this.friendHpLabels, 2, 6, this.friendDamages[last], lastPhase.getNowFriendHpCombined(),
                    friendMaxHp[1], true);
        }
        printHp(this.enemyHpLabels, 2, 0, this.enemyDamages[last], lastPhase.getNowEnemyHp(), maxEnemyHp, false);
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
            }
            else if (this.getMapCellDto() == null) {
                // 移動中
                this.clearText();
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
