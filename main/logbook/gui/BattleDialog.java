package logbook.gui;

import java.util.List;

import logbook.data.context.GlobalContext;
import logbook.dto.BattleDto;
import logbook.dto.BattleResultDto;
import logbook.dto.DockDto;
import logbook.dto.ShipDto;
import logbook.dto.ShipInfoDto;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * 戦闘詳細
 *
 */
public final class BattleDialog extends Dialog {

    private Shell shell;

    private final String id;

    /**
     * Create the dialog.
     * @param parent
     * @param id
     */
    public BattleDialog(Shell parent, String id) {
        super(parent, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE);

        this.id = id;
    }

    /**
     * Open the dialog.
     */
    public void open() {
        this.createContents();
        this.shell.open();
        this.shell.layout();
        Display display = this.getParent().getDisplay();
        while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        this.shell = new Shell(this.getParent(), this.getStyle());
        this.shell.setText("会敵報告");
        this.shell.setLayout(new GridLayout(1, false));

        Composite titleArea = new Composite(this.shell, SWT.NONE);
        titleArea.setLayout(new RowLayout());
        titleArea.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label title = new Label(titleArea, SWT.NONE);
        title.setText("「<海域名が入ります>」で作戦行動中に「<敵艦隊名>」と対峙しました");

        Composite kaiteki = new Composite(this.shell, SWT.NONE);
        kaiteki.setLayout(new GridLayout(2, true));
        kaiteki.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label top = new Label(kaiteki, SWT.NONE);
        top.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, false, false, 2, 1));
        top.setText("交戦戦力");

        Label label1 = new Label(kaiteki, SWT.NONE);
        label1.setText("味方艦隊名が入ります");

        Label label2 = new Label(kaiteki, SWT.NONE);
        label2.setText("敵艦隊名が入ります");

        Composite friend = new Composite(kaiteki, SWT.NONE);
        friend.setLayout(new GridLayout(2, false));

        Label fname1 = new Label(friend, SWT.NONE);

        Label fhp1 = new Label(friend, SWT.NONE);

        Label fname2 = new Label(friend, SWT.NONE);

        Label fhp2 = new Label(friend, SWT.NONE);

        Label fname3 = new Label(friend, SWT.NONE);

        Label fhp3 = new Label(friend, SWT.NONE);

        Label fname4 = new Label(friend, SWT.NONE);

        Label fhp4 = new Label(friend, SWT.NONE);

        Label fname5 = new Label(friend, SWT.NONE);

        Label fhp5 = new Label(friend, SWT.NONE);

        Label fname6 = new Label(friend, SWT.NONE);

        Label label14 = new Label(friend, SWT.NONE);

        Composite enemy = new Composite(kaiteki, SWT.NONE);
        enemy.setLayout(new GridLayout(2, false));

        Label ename1 = new Label(enemy, SWT.NONE);

        Label ehp1 = new Label(enemy, SWT.NONE);

        Label ename2 = new Label(enemy, SWT.NONE);

        Label ehp2 = new Label(enemy, SWT.NONE);

        Label ename3 = new Label(enemy, SWT.NONE);

        Label ehp3 = new Label(enemy, SWT.NONE);

        Label ename4 = new Label(enemy, SWT.NONE);

        Label ehp4 = new Label(enemy, SWT.NONE);

        Label ename5 = new Label(enemy, SWT.NONE);

        Label ehp5 = new Label(enemy, SWT.NONE);

        Label ename6 = new Label(enemy, SWT.NONE);

        Label ehp6 = new Label(enemy, SWT.NONE);

        int id = Integer.valueOf(this.id) - 1;
        BattleResultDto result = GlobalContext.getBattleResultList().get(id);
        BattleDto battle = result.getBattleDto();
        // タイトル
        title.setText("「" + result.getQuestName() + "」で作戦行動中に「" + result.getEnemyName() + "」と対峙しました");

        if (battle != null) {
            DockDto dock = battle.getDock();
            if (dock != null) {
                // 味方艦隊
                label1.setText(dock.getName());
                // 敵艦隊
                label2.setText(result.getEnemyName());
                // 味方艦隊
                Label[] friendnames = new Label[] { fname1, fname2, fname3, fname4, fname5, fname6 };
                Label[] friendhps = new Label[] { fhp1, fhp2, fhp3, fhp4, fhp5, label14 };
                List<ShipDto> friendships = dock.getShips();
                for (int i = 0; i < friendships.size(); i++) {
                    ShipDto ship = friendships.get(i);
                    String name = ship.getName() + "(Lv" + ship.getLv() + ")";
                    // 名前
                    friendnames[i].setText(name);
                    // HP
                    friendhps[i].setText(battle.getNowFriendHp()[i] + "/" + battle.getMaxFriendHp()[i]);
                }
                // 敵艦隊
                Label[] enemynames = new Label[] { ename1, ename2, ename3, ename4, ename5, ename6 };
                Label[] enemyhps = new Label[] { ehp1, ehp2, ehp3, ehp4, ehp5, ehp6 };
                List<ShipInfoDto> enemyships = battle.getEnemy();
                for (int i = 0; i < enemyships.size(); i++) {
                    ShipInfoDto ship = enemyships.get(i);
                    String name = ship.getName();
                    if (!StringUtils.isEmpty(ship.getFlagship())) {
                        name += "(" + ship.getFlagship() + ")";
                    }
                    // 名前
                    enemynames[i].setText(name);
                    // HP
                    enemyhps[i].setText(battle.getNowEnemyHp()[i] + "/" + battle.getMaxEnemyHp()[i]);
                }
            }
        }
        this.shell.pack();
    }

}
