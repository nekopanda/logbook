package logbook.gui;

import java.util.List;

import logbook.dto.PracticeUserDetailDto;
import logbook.dto.ShipInfoDto;
import logbook.internal.ExpTable;
import logbook.internal.PracticeEvaluateExp;
import logbook.util.CalcPracticeExpUtils;
import logbook.util.SwtUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * 演習経験値計算機
 *
 */
public final class CalcPracticeExpDialog extends WindowBase {

    private final Shell parent;
    private Shell shell;
    private final Label[] shipNameLabels = new Label[6];
    private final Text[] shipLvLabels = new Text[4]; // 2~6艦目
    private Spinner firstShipLevel;
    private Spinner secondShipLevel;
    private final Text[][] expTableText = new Text[4][4]; //[result][rank]
    private final String[] ranks = { "S勝利", "A勝利", "C戦術的敗北", "D敗北" };
    private final boolean[] isFlagship = { false, true, false, true };
    private final boolean[] isMvp = { false, false, true, true };
    private final int[] levels = { 1, 1, 1, 1, 1, 1 };
    private final String[] shipNames = { "敵旗艦", "敵2艦目", "敵3艦目", "敵4艦目", "敵5艦目", "敵6艦目" };

    private PracticeUserDetailDto practiceUser;

    /**
     * Create the dialog.
     * @param parent
     */
    public CalcPracticeExpDialog(Shell parent, MenuItem menuItem) {
        super(menuItem);
        this.parent = parent;
    }

    /**
     * Open the dialog.
     */
    @Override
    public void open() {
        // 初期化済みの場合
        if (this.isWindowInitialized()) {
            this.setVisible(true);
            this.clearText();
            this.updateData();
            return;
        }
        this.createContents();
        this.registerEvents();
        this.setWindowInitialized(true);
        this.calc();
        this.setVisible(true);
    }

    @Override
    protected boolean moveWithDrag() {
        return true;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        // シェルを作成
        super.createContents(this.parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE, false);
        this.shell = this.getShell();
        this.shell.setText("演習経験値計算機");
        this.shell.setLayout(new GridLayout(1, false));

        Composite practiceinfo = new Composite(this.shell, SWT.NONE);
        practiceinfo.setLayout(new GridLayout(6, false));
        practiceinfo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        this.shipNameLabels[0] = new Label(practiceinfo, SWT.NONE);
        this.shipNameLabels[0].setText(this.shipNames[0]);
        GridData gdFirstShipName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdFirstShipName.widthHint = SwtUtils.DPIAwareWidth(60);
        this.shipNameLabels[0].setLayoutData(gdFirstShipName);
        this.firstShipLevel = new Spinner(practiceinfo, SWT.BORDER);
        GridData gdFirstShipLevel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdFirstShipLevel.widthHint = SwtUtils.DPIAwareWidth(45);
        this.firstShipLevel.setLayoutData(gdFirstShipLevel);
        this.firstShipLevel.setMaximum(ExpTable.MAX_LEVEL);
        this.firstShipLevel.setMinimum(1);
        this.firstShipLevel.setSelection(this.levels[0]);
        new Label(practiceinfo, SWT.NONE).setText("Lv");

        this.shipNameLabels[1] = new Label(practiceinfo, SWT.NONE);
        this.shipNameLabels[1].setText(this.shipNames[1]);
        GridData gdSecondShipName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdSecondShipName.widthHint = SwtUtils.DPIAwareWidth(60);
        this.shipNameLabels[1].setLayoutData(gdSecondShipName);
        this.secondShipLevel = new Spinner(practiceinfo, SWT.BORDER);
        GridData gdsecondShipLevel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdsecondShipLevel.widthHint = SwtUtils.DPIAwareWidth(45);
        this.secondShipLevel.setLayoutData(gdsecondShipLevel);
        this.secondShipLevel.setMaximum(ExpTable.MAX_LEVEL);
        this.secondShipLevel.setMinimum(1);
        this.secondShipLevel.setSelection(this.levels[1]);
        new Label(practiceinfo, SWT.NONE).setText("Lv");

        for (int i = 0; i < 4; i++) {

            this.shipNameLabels[i + 2] = new Label(practiceinfo, SWT.NONE);
            this.shipNameLabels[i + 2].setText(this.shipNames[i + 2]);
            GridData gdShipName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gdShipName.widthHint = SwtUtils.DPIAwareWidth(60);
            this.shipNameLabels[i + 2].setLayoutData(gdShipName);
            this.shipLvLabels[i] = new Text(practiceinfo, SWT.BORDER | SWT.READ_ONLY);
            GridData gdLv = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gdLv.widthHint = SwtUtils.DPIAwareWidth(60);
            this.shipLvLabels[i].setLayoutData(gdLv);
            this.shipLvLabels[i].setText(Integer.toString(this.levels[i + 2]));
            Label lvlabel = new Label(practiceinfo, SWT.NONE);
            lvlabel.setText("Lv");
        }

        Composite separator = new Composite(this.shell, SWT.NONE);
        separator.setLayout(new FillLayout());
        separator.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        new Label(separator, SWT.SEPARATOR | SWT.HORIZONTAL);

        Composite expTable = new Composite(this.shell, SWT.NONE);
        expTable.setLayout(new GridLayout(5, false));
        expTable.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        String[] results = { "基本経験値", "旗艦", "MVP", "旗艦&MVP" };
        String[] ranks = { "ランクS", "ランクA", "ランクC", "ランクD" };
        GridData gdResult = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdResult.widthHint = SwtUtils.DPIAwareWidth(60);
        GridData gdExp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdExp.widthHint = SwtUtils.DPIAwareWidth(45);
        Text blank = new Text(expTable, SWT.NONE);
        blank.setLayoutData(gdResult);
        blank.setVisible(false);
        for (int i = 0; i < 4; i++) {
            Text text = new Text(expTable, SWT.BORDER | SWT.READ_ONLY);
            text.setLayoutData(gdExp);
            text.setText(ranks[i]);
        }

        for (int i = 0; i < results.length; i++) {
            Text text = new Text(expTable, SWT.BORDER | SWT.READ_ONLY);
            text.setLayoutData(gdResult);
            text.setText(results[i]);
            for (int j = 0; j < ranks.length; j++) {
                this.expTableText[i][j] = new Text(expTable, SWT.RIGHT | SWT.BORDER | SWT.READ_ONLY);
                this.expTableText[i][j].setLayoutData(gdExp);
                this.expTableText[i][j].setData("disable-drag-move", true);
            }
        }

        ShipLvListener firstShipLvListener = new ShipLvListener(this.firstShipLevel);
        this.firstShipLevel.addSelectionListener(firstShipLvListener);
        this.firstShipLevel.addMouseWheelListener(new WheelListener(this.firstShipLevel, firstShipLvListener));

        ShipLvListener secondShipLvListener = new ShipLvListener(this.secondShipLevel);
        this.secondShipLevel.addSelectionListener(secondShipLvListener);
        this.secondShipLevel.addMouseWheelListener(new WheelListener(this.secondShipLevel, secondShipLvListener));

        for (Control c : new Control[] { this.firstShipLevel, this.secondShipLevel }) {
            c.setData("disable-drag-move", true);
        }
        for (Control c : this.shipLvLabels) {
            c.setData("disable-drag-move", true);
        }

        this.shell.pack();
    }

    /**
     * 計算を行う
     */
    private void calc() {
        int firstLevel = Integer.parseInt(this.firstShipLevel.getText());
        int secondLevel = Integer.parseInt(this.secondShipLevel.getText());

        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                this.expTableText[i][j].setText(Integer.toString(CalcPracticeExpUtils.getExp(firstLevel, secondLevel,
                        PracticeEvaluateExp.get().get(this.ranks[j]), this.isFlagship[i], this.isMvp[i])));
            }
        }
    }

    protected void updateData() {
        this.getShell().setRedraw(false);
        for (int i = 0; i < this.shipNameLabels.length; i++) {
            switch (i) {
            case 0:
                this.firstShipLevel.setSelection(this.levels[i]);
                break;
            case 1:
                this.secondShipLevel.setSelection(this.levels[i]);
                break;
            default:
                this.shipLvLabels[i - 2].setText(Integer.toString(this.levels[i]));
            }
            this.shipNameLabels[i].setText(this.shipNames[i]);
        }
        this.calc();
        this.getShell().layout();
        this.getShell().setRedraw(true);
    }

    public void updatePracticeUser(PracticeUserDetailDto practiceUserExDto) {
        this.clearData();
        this.practiceUser = practiceUserExDto;
        List<ShipInfoDto> ships = this.practiceUser.getShips();
        System.arraycopy(this.practiceUser.getShipsLevel(), 0, this.levels, 0, 6);
        for (int i = 0; i < ships.size(); i++) {
            this.shipNames[i] = ships.get(i).getName();
        }
        if (this.isWindowInitialized() && this.getVisible()) {
            this.clearText();
            this.updateData();
        }
    }

    private void clearData() {
        this.shipNames[0] = "敵旗艦";
        this.levels[0] = 1;
        for (int i = 1; i < 6; i++) {
            this.shipNames[i] = "敵" + (i + 1) + "艦目";
            this.levels[i] = 1;
        }
    }

    private void clearText() {
        for (int i = 0; i < 6; i++) {
            this.shipNameLabels[i].setText(this.shipNames[i]);
        }
        this.firstShipLevel.setSelection(1);
        this.secondShipLevel.setSelection(1);
        for (int i = 0; i < 4; i++) {
            this.shipLvLabels[i].setText(Integer.toString(this.levels[i + 2]));
        }
        this.calc();

    }

    /**
     * 艦のレベルが変更された
     *
     */
    private final class ShipLvListener extends SelectionAdapter {
        private final Spinner shiplv;

        private ShipLvListener(Spinner shiplv) {
            this.shiplv = shiplv;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            CalcPracticeExpDialog.this.calc();
        }
    }

    /**
     * ホイールでレベルを動かす
     */
    private final class WheelListener implements MouseWheelListener {

        private final Spinner spinner;
        private final SelectionListener listener;

        public WheelListener(Spinner spinner, SelectionListener listener) {
            this.spinner = spinner;
            this.listener = listener;
        }

        @Override
        public void mouseScrolled(MouseEvent e) {
            if (e.count > 0) {
                int cur = this.spinner.getSelection();
                if (cur < this.spinner.getMaximum()) {
                    this.spinner.setSelection(cur + 1);
                    this.listener.widgetSelected(null);
                }
            }
            else if (e.count < 0) {
                int cur = this.spinner.getSelection();
                if (cur > this.spinner.getMinimum()) {
                    this.spinner.setSelection(cur - 1);
                    this.listener.widgetSelected(null);
                }
            }
        }

    }
}
