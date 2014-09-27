/**
 * 
 */
package logbook.gui;

import java.util.List;

import logbook.constants.AppConstants;
import logbook.dto.BattleExDto;
import logbook.dto.DockDto;
import logbook.dto.EnemyShipDto;
import logbook.dto.ResultRank;
import logbook.dto.ShipDto;
import logbook.gui.logic.DamageRate;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * @author Nekopanda
 * 戦況ウィンドウ縮小版
 */
public class BattleWindowSmall extends BattleWindow {

    private final Composite[] infoCompo = new Composite[2];
    private Composite damageCompo;
    private Composite resultCompo;

    // 0: 開始前, 1,2: 後
    private final Label[][] friendHpLabels = new Label[3][12];
    private final Label[][] enemyHpLabels = new Label[3][6];

    public BattleWindowSmall(Shell parent, MenuItem menuItem) {
        super(parent, menuItem);
    }

    private static Label addLabel(Composite compo, int align, String text, int width, int horizontalSpan) {
        Label label = new Label(compo, SWT.NONE);
        GridData gd = new GridData(SWT.CENTER, SWT.CENTER, false, false, horizontalSpan, 1);
        gd.widthHint = width;
        label.setAlignment(align);
        label.setLayoutData(gd);
        label.setText(text);
        return label;
    }

    private static Label addLabel(Composite compo, int galign, int align, String text) {
        Label label = new Label(compo, SWT.NONE);
        GridData gd = new GridData(galign, SWT.CENTER, false, false, 1, 1);
        label.setAlignment(align);
        label.setLayoutData(gd);
        label.setText(text);
        return label;
    }

    private void createHpHeaders(int hpWidth, int damWidth) {
        addLabel(this.damageCompo, SWT.CENTER, "開始時", hpWidth, 1);
        addLabel(this.damageCompo, SWT.CENTER, "Dmg", damWidth, 1);
        addLabel(this.damageCompo, SWT.CENTER, "残", damWidth, 1);
    }

    private void createHpLabels(Label[][] labels, int i, int hpWidth, int damWidth) {
        labels[0][i] = addLabel(this.damageCompo, SWT.CENTER, "0", hpWidth, 1); //0 開始時HP
        labels[1][i] = addLabel(this.damageCompo, SWT.CENTER, "0", damWidth, 1); //2 Dmg
        labels[2][i] = addLabel(this.damageCompo, SWT.CENTER, "0", damWidth, 1); //3 後HP
    }

    protected void addHorizontalSeparator(Composite compo, int span) {
        Label label = new Label(compo, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false, span, 1);
        label.setLayoutData(gd);
    }

    protected void addVerticalSeparator(Composite compo, int span) {
        Label label = new Label(compo, SWT.SEPARATOR | SWT.VERTICAL);
        GridData gd = new GridData(SWT.CENTER, SWT.FILL, false, false, 1, span);
        label.setLayoutData(gd);
    }

    private GridLayout makeGridLayout(int numColumns) {
        GridLayout layout = new GridLayout(numColumns, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        return layout;
    }

    @Override
    protected void createContents() {
        this.getShell().setLayout(this.makeGridLayout(3));

        int battleWidth = 120;
        int nameWidth = 80;
        int hpWidth = 40;
        int damWidth = 28;

        for (int i = 0; i < 2; ++i) {
            this.infoCompo[i] = new Composite(this.getShell(), SWT.NONE);

            this.infoCompo[i].setLayout(this.makeGridLayout(3));

            this.infoLabels[i][0] = addLabel(this.infoCompo[i], SWT.CENTER, "艦隊名", battleWidth, 2);
            this.addVerticalSeparator(this.infoCompo[i], 7);
            this.infoLabels[i][1] = addLabel(this.infoCompo[i], SWT.CENTER, "陣形", battleWidth / 2, 1);
            this.infoLabels[i][2] = addLabel(this.infoCompo[i], SWT.CENTER, "触接", battleWidth / 2, 1);
            this.infoLabels[i][3] = addLabel(this.infoCompo[i], SWT.CENTER, "索敵", battleWidth, 2);

            if (i == 0) {
                this.infoLabels[i][4] = addLabel(this.infoCompo[i], SWT.CENTER, "航空戦", battleWidth / 2, 1);
                this.infoLabels[i][5] = addLabel(this.infoCompo[i], SWT.CENTER, "制空状態", battleWidth / 2, 1);
                this.infoLabels[i][6] = addLabel(this.infoCompo[i], SWT.CENTER, "Stage1", battleWidth / 2, 1);
            }
            else {
                this.matchLabel = addLabel(this.infoCompo[i], SWT.CENTER, "会敵", battleWidth, 2);
                this.infoLabels[i][6] = addLabel(this.infoCompo[i], SWT.CENTER, "Stage1", battleWidth / 2, 1);
                this.infoLabels[i][4] = this.infoLabels[i][5] = this.infoLabels[i][6];
            }

            this.infoLabels[i][7] = addLabel(this.infoCompo[i], SWT.CENTER, "Stage2", battleWidth / 2, 1);
            this.infoLabels[i][8] = addLabel(this.infoCompo[i], SWT.CENTER, "ロスト1", battleWidth / 2, 1);
            this.infoLabels[i][9] = addLabel(this.infoCompo[i], SWT.CENTER, "ロスト2", battleWidth / 2, 1);
            this.infoLabels[i][10] = addLabel(this.infoCompo[i], SWT.CENTER, "ロスト3", battleWidth / 2, 1);
            this.infoLabels[i][11] = addLabel(this.infoCompo[i], SWT.CENTER, "ロスト4", battleWidth / 2, 1);
        }

        int numColumns = 12;
        this.damageCompo = new Composite(this.getShell(), SWT.NONE);
        this.damageCompo.setLayout(this.makeGridLayout(numColumns));

        // ヘッダ行作成
        this.createHpHeaders(hpWidth, damWidth);
        addLabel(this.damageCompo, SWT.CENTER, "", SWT.DEFAULT, 1);
        this.createHpHeaders(hpWidth, damWidth);
        addLabel(this.damageCompo, SWT.CENTER, "", SWT.DEFAULT, 1);
        this.createHpHeaders(hpWidth, damWidth);
        addLabel(this.damageCompo, SWT.LEFT, "敵艦名", nameWidth, 1);
        this.addHorizontalSeparator(this.damageCompo, numColumns);

        // 中身作成
        for (int i = 0; i < 6; ++i) {
            this.createHpLabels(this.friendHpLabels, i, hpWidth, damWidth);
            if (i == 0)
                this.addVerticalSeparator(this.damageCompo, 6);
            this.createHpLabels(this.friendHpLabels, i + 6, hpWidth, damWidth);
            if (i == 0)
                this.addVerticalSeparator(this.damageCompo, 6);
            this.createHpLabels(this.enemyHpLabels, i, hpWidth, damWidth);
            this.enemyLabels[i] = addLabel(this.damageCompo, SWT.LEFT, "-", nameWidth, 1);
        }

        // 結果表示領域
        this.resultCompo = new Composite(this.getShell(), SWT.BORDER);
        this.resultCompo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 3, 1));
        this.resultCompo.setLayout(this.makeGridLayout(3));

        this.resultLabel[2] = addLabel(this.resultCompo, SWT.CENTER, SWT.LEFT, "");
        this.resultLabel[0] = addLabel(this.resultCompo, SWT.CENTER, SWT.LEFT, "");
        this.resultLabel[1] = addLabel(this.resultCompo, SWT.FILL, SWT.CENTER, "");
    }

    @Override
    protected void clearText() {
        super.clearText();

        // 味方
        for (int i = 0; i < 12; ++i) {
            for (int c = 0; c < 3; ++c) {
                this.friendHpLabels[c][i].setText("");
                if ((c == 0) || (c == 2))
                    setLabelNone(this.friendHpLabels[c][i]);
            }
        }

        // 敵
        for (int i = 0; i < 6; ++i) {
            for (int c = 0; c < 3; ++c) {
                this.enemyHpLabels[c][i].setText("");
                if (c == 2)
                    setLabelNone(this.enemyHpLabels[c][i]);
            }
        }

        this.resultCompo.setBackground(null);
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

    private static void setLabelColor(Label label, int nowhp, int maxhp, boolean friend) {
        DamageRate rate = DamageRate.fromHP(nowhp, maxhp);
        label.setBackground(rate.getBackground());
        label.setForeground(rate.getForeground());
    }

    private static void printFriendHp(Label[][] labels, int index, int nowhp, int maxhp) {
        printHp(labels[0][index], nowhp, maxhp);
        setLabelColor(labels[0][index], nowhp, maxhp, true);
    }

    private static void printHp(
            Label[][] labels, int base1, int base2, int[] dam, int[] nowhp, int[] maxhp, boolean friend)
    {
        for (int i = 0; i < nowhp.length; ++i) {
            labels[base1 + 0][base2 + i].setText(String.valueOf(dam[base2 + i]));
            labels[base1 + 1][base2 + i].setText(String.valueOf(nowhp[i]));
            setLabelColor(labels[base1 + 1][base2 + i], nowhp[i], maxhp[i], friend);
        }
    }

    @Override
    protected String getMVPText(MVPShip[] mvp, int airDamage) {
        if (mvp == null) {
            return "";
        }
        String result0 = "MVP ";
        for (int i = 0; i < mvp.length; ++i) {
            ShipDto ship = mvp[i].ship;
            result0 += String.format("%s(%d) ",
                    (ship == null) ? "?" : ship.getName(), mvp[i].ydam);
        }
        if (airDamage > 0) {
            result0 += " 航ダメ(" + airDamage + ") ";
        }
        return result0;
    }

    @Override
    protected String getReulstText(double[] damageRate, String rank) {
        String rateString = (damageRate[0] == 0.0) ? "" :
                String.format(" (x%.3f)", damageRate[1] / damageRate[0]);
        return String.format("結果: %s 損害率 %.1f%%vs%.1f%%%s ",
                rank, damageRate[0] * 100, damageRate[1] * 100, rateString);
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
        printHp(this.friendHpLabels, 1, 0, this.friendDamages[last], lastPhase.getNowFriendHp(), friendMaxHp[0], true);
        if (battle.isCombined()) {
            printHp(this.friendHpLabels, 1, 6, this.friendDamages[last], lastPhase.getNowFriendHpCombined(),
                    friendMaxHp[1], true);
        }
        printHp(this.enemyHpLabels, 1, 0, this.enemyDamages[last], lastPhase.getNowEnemyHp(), maxEnemyHp, false);

        ResultRank rank = lastPhase.getEstimatedRank();
        if ((rank == ResultRank.C) || (rank == ResultRank.D) || (rank == ResultRank.E)) {
            this.resultCompo.setBackground(SWTResourceManager.getColor(AppConstants.LOSE_BATTLE_COLOR));
        }
        else {
            this.resultCompo.setBackground(null);
        }

        this.resultCompo.layout();
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
