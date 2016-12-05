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
import logbook.gui.logic.ColorManager;
import logbook.gui.logic.DamageRate;
import logbook.gui.logic.LayoutLogic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Nekopanda
 * 戦況（横）ウィンドウ
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

    /*
        private static Label addLabel(Composite compo, String text, int align, int width, boolean excess, int horizontalSpan) {
            Label label = new Label(compo, SWT.NONE);
            GridData gd = new GridData(SWT.FILL, SWT.CENTER, excess, false, horizontalSpan, 1);
            gd.widthHint = width;
            label.setAlignment(align);
            label.setLayoutData(gd);
            label.setText(text);
            return label;
        }

        private static Label addLabel(Composite compo, String text, int align, int width) {
            return addLabel(compo, text, align, width, true, 1);
        }

        private static Label addLabel(Composite compo, String text, int horizontalSpan) {
            return addLabel(compo, text, SWT.CENTER, SWT.DEFAULT, false, horizontalSpan);
        }
    */
    private void createHpHeaders() {
        this.addLabel("開始時");
        this.addLabel("Dmg");
        this.addLabel("残");
    }

    private void createHpLabels(Label[][] labels, int i) {
        labels[0][i] = this.addLabel("000/000"); //0 開始時HP
        labels[1][i] = this.addLabel("000"); //2 Dmg
        labels[2][i] = this.addLabel("000"); //3 後HP
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

        int nameWidth = 80;

        this.title = new Label(this.getShell(), SWT.NONE);
        this.title.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
        this.title.setFont(this.getBoldFont());
        this.title.setText("出撃中ではありません");

        int numColumns = 12;
        this.currentCompo = this.damageCompo = new Composite(this.getShell(), SWT.NONE);
        this.damageCompo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 3));
        this.damageCompo.setLayout(this.makeGridLayout(numColumns));

        // ヘッダ行作成
        this.createHpHeaders();
        this.addLabel("");
        this.createHpHeaders();
        this.addLabel("");
        this.createHpHeaders();
        this.addLabel("敵艦名", SWT.LEFT, nameWidth);
        this.addHorizontalSeparator(numColumns);

        // 中身作成
        for (int i = 0; i < 6; ++i) {
            this.createHpLabels(this.friendHpLabels, i);
            if (i == 0)
                this.addVerticalSeparator(this.damageCompo, 6);
            this.createHpLabels(this.friendHpLabels, i + 6);
            if (i == 0)
                this.addVerticalSeparator(this.damageCompo, 6);
            this.createHpLabels(this.enemyHpLabels, i);
            this.enemyLabels[i] = this.addLabel("-", SWT.LEFT, nameWidth);
            // 表示しないのでダミーで入れておく
            this.enemyLabels[i + 6] = this.enemyLabels[i];
        }

        this.currentCompo = this.getShell();
        this.addHorizontalSeparator(2);

        for (int i = 0; i < 2; ++i) {
            this.currentCompo = this.infoCompo[i] = new Composite(this.getShell(), SWT.NONE);
            this.infoCompo[i].setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1));

            this.infoCompo[i].setLayout(this.makeGridLayout(3));

            this.infoLabels[i][0] = this.addLabelWithSpan("艦隊名", 2, 1);
            this.addVerticalSeparator(7);
            this.infoLabels[i][1] = this.addLabel("陣形:単縦");
            this.infoLabels[i][2] = this.addLabel("触接:あり");
            this.infoLabels[i][3] = this.addLabelWithSpan("索敵:", 2, 1);

            if (i == 0) {
                this.infoLabels[i][4] = this.addLabel("航空戦");
                this.infoLabels[i][5] = this.addLabel("制空状態");
                this.infoLabels[i][6] = this.addLabel("Stage1");
            }
            else {
                this.matchLabel = this.addLabelWithSpan("会敵", 2, 1);
                this.infoLabels[i][6] = this.addLabel("Stage1");
                this.infoLabels[i][4] = this.infoLabels[i][5] = this.infoLabels[i][6];
            }
            LayoutLogic.hide(this.infoLabels[i][6], true);

            this.infoLabels[i][7] = this.addLabel("Stage2");
            LayoutLogic.hide(this.infoLabels[i][7], true);
            this.infoLabels[i][8] = this.addLabel("000→000");
            this.infoLabels[i][9] = this.addLabel("000→000");
            //this.beginCombined();
            this.infoLabels[i][10] = this.addLabel("000→000");
            this.infoLabels[i][11] = this.addLabel("000→000");
            //this.endCombined();
        }

        // 結果表示領域
        this.resultCompo = new Composite(this.getShell(), SWT.BORDER);
        this.resultCompo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        RowLayout rowLayout = new RowLayout();
        rowLayout.marginBottom = rowLayout.marginHeight = 0;
        rowLayout.marginLeft = rowLayout.marginRight = 0;
        rowLayout.marginTop = rowLayout.marginWidth = 0;
        rowLayout.wrap = false;
        this.resultCompo.setLayout(rowLayout);

        this.resultLabel[2] = new Label(this.resultCompo, SWT.NONE);
        this.resultLabel[0] = new Label(this.resultCompo, SWT.NONE);
        this.resultLabel[1] = new Label(this.resultCompo, SWT.NONE);
    }

    @Override
    protected void clearText() {
        super.clearText();

        // 味方
        for (int i = 0; i < 12; ++i) {
            for (int c = 0; c < 3; ++c) {
                setLabelText(this.friendHpLabels[c][i], "");
                if ((c == 0) || (c == 2))
                    setLabelNone(this.friendHpLabels[c][i]);
            }
        }

        // 敵
        for (int i = 0; i < 6; ++i) {
            for (int c = 0; c < 3; ++c) {
                setLabelText(this.enemyHpLabels[c][i], "");
                if (c == 2)
                    setLabelNone(this.enemyHpLabels[c][i]);
            }
        }

        this.resultCompo.setBackground(null);
    }

    private void printDock(DockDto dock, int base) {
        List<ShipDto> ships = dock.getShips();
        boolean[] escaped = dock.getEscaped();
        for (int i = 0; i < ships.size(); ++i) {
            ShipDto ship = ships.get(i);
            printFriendHp(this.friendHpLabels, base + i,
                    ship.getNowhp(), ship.getMaxhp(), ship, (escaped != null) && escaped[i]);
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

        this.title.setText(this.getMapCellDto().toString());
    }

    private static void setLabelColor(Label label, DamageRate rate) {
        label.setBackground(rate.getBackground());
        label.setForeground(rate.getForeground());
    }

    private static void setLabelColor(Label label, int nowhp, int maxhp) {
        setLabelColor(label, DamageRate.fromHP(nowhp, maxhp));
    }

    private static void printFriendHp(Label[][] labels, int index, int nowhp, int maxhp, ShipDto ship, boolean escaped) {
        labels[0][index].setToolTipText(ship.getDetailedString());
        if (escaped) {
            setLabelColor(labels[0][index], DamageRate.ESCAPED);
        }
        else {
            printHp(labels[0][index], nowhp, maxhp);
            setLabelColor(labels[0][index], nowhp, maxhp);
        }
    }

    private static void printHp(
            Label[][] labels, int base1, int base2, int[] dam, int[] nowhp, int[] maxhp, boolean[] escaped)
    {
        for (int i = 0; i < nowhp.length; ++i) {
            if ((escaped != null) && escaped[i]) {
                continue;
            }
            labels[base1 + 0][base2 + i].setText(String.valueOf(dam[base2 + i]));
            labels[base1 + 1][base2 + i].setText(String.valueOf(nowhp[i]));
            setLabelColor(labels[base1 + 1][base2 + i], nowhp[i], maxhp[i]);
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
        @SuppressWarnings("unchecked")
        List<ShipDto>[] ships = new List[] {
                battle.getDock().getShips(),
                battle.isCombined() ? battle.getDockCombined().getShips() : null
        };
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
                            startHp[c], maxHp[c], ships[i].get(c), (dockEscaped != null) && dockEscaped[c]);
                }
            }
        }

        // 敵
        List<EnemyShipDto> enemyShips = battle.getEnemy();
        int[] maxEnemyHp = battle.getMaxEnemyHp();
        for (int i = 0; i < enemyShips.size(); ++i) {
            EnemyShipDto ship = enemyShips.get(i);
            this.enemyLabels[i].setText(String.valueOf(i + 1) + "." + ship.getFriendlyName());
            this.enemyLabels[i].setToolTipText(ship.getDetailedString());
            // this.enemyHpLabels[0][i].setText(String.valueOf(maxEnemyHp[i]) + "/" + maxEnemyHp[i]);
            // 3桁だとはみ出すという苦情が来たので・・・
            this.enemyHpLabels[0][i].setText(String.valueOf(maxEnemyHp[i]));
        }

        // 昼戦後HP
        printHp(this.friendHpLabels, 1, 0, this.friendDamages[last],
                lastPhase.getNowFriendHp(), friendMaxHp[0], docks.get(0).getEscaped());
        if (battle.isCombined()) {
            printHp(this.friendHpLabels, 1, 6, this.friendDamages[last],
                    lastPhase.getNowFriendHpCombined(), friendMaxHp[1], docks.get(1).getEscaped());
        }
        printHp(this.enemyHpLabels, 1, 0, this.enemyDamages[last], lastPhase.getNowEnemyHp(), maxEnemyHp, null);

        ResultRank rank = lastPhase.getEstimatedRank();
        if ((rank == ResultRank.C) || (rank == ResultRank.D) || (rank == ResultRank.E)) {
            this.resultCompo.setBackground(ColorManager.getColor(AppConstants.LOSE_BATTLE_COLOR));
        }
        else {
            this.resultCompo.setBackground(null);
        }

        // 敵連合艦隊用レイアウトセット
        this.setEnemyCombinedMode(battle.isEnemyCombined());

        this.resultCompo.layout();
    }
}
