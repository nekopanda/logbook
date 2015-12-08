package logbook.gui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import logbook.config.AppConfig;
import logbook.data.Data;
import logbook.data.DataType;
import logbook.data.EventListener;
import logbook.data.context.GlobalContext;
import logbook.dto.ShipDto;
import logbook.internal.EvaluateExp;
import logbook.internal.ExpTable;
import logbook.internal.SeaExp;
import logbook.util.CalcExpUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseWheelListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * 経験値計算機
 *
 */
public final class CalcExpDialog extends WindowBase {

    private final List<ShipDto> shiplist = new ArrayList<ShipDto>();

    private final Shell parent;
    private Shell shell;
    private Combo shipcombo;
    private Spinner beforelv;
    private Text beforexp;
    private Spinner afterlv;
    private Text afterexp;
    private Combo seacombo;
    private Combo evalcombo;
    private Button flagbtn;
    private Button mvpbtn;
    private Text getexp;
    private Text needexp;
    private Text battlecount;

    /**
     * Create the dialog.
     * @param parent
     */
    public CalcExpDialog(Shell parent, MenuItem menuItem) {
        super(menuItem);
        this.parent = parent;
    }

    /**
     * 「新しいウィンドウを開く」用
     */
    public CalcExpDialog() {
        super();
        // アプリ上での親はないが、メインウィンドウの子ウィンドウとして作成する
        this.parent = ApplicationMain.main.getShell();
    }

    /**
     * Open the dialog.
     */
    @Override
    public void open() {
        // 初期化済みの場合
        if (this.isWindowInitialized()) {
            this.setShipComboData();
            this.preset();
            this.calc();
            this.setVisible(true);
            return;
        }
        this.createContents();
        this.registerEvents();
        this.setWindowInitialized(true);
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
        this.shell.setText("経験値計算機");
        this.shell.setLayout(new GridLayout(1, false));

        Composite select = new Composite(this.shell, SWT.NONE);
        select.setLayout(new RowLayout());
        select.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.shipcombo = new Combo(select, SWT.READ_ONLY);
        Button secretary = new Button(select, SWT.NONE);
        secretary.setText("秘書艦");

        Composite plan = new Composite(this.shell, SWT.NONE);
        plan.setLayout(new GridLayout(5, false));
        plan.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label label1 = new Label(plan, SWT.NONE);
        label1.setText("今のレベル");
        this.beforelv = new Spinner(plan, SWT.BORDER);
        GridData gdBeforelv = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdBeforelv.widthHint = 45;
        this.beforelv.setLayoutData(gdBeforelv);
        this.beforelv.setMaximum(ExpTable.MAX_LEVEL);
        this.beforelv.setMinimum(1);
        Label label2 = new Label(plan, SWT.NONE);
        label2.setText("Lv");
        this.beforexp = new Text(plan, SWT.BORDER | SWT.READ_ONLY);
        GridData gdBeforexp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdBeforexp.widthHint = 60;
        this.beforexp.setLayoutData(gdBeforexp);
        this.beforexp.setText("0");
        Label label3 = new Label(plan, SWT.NONE);
        label3.setText("Exp");

        Label label4 = new Label(plan, SWT.NONE);
        label4.setText("目標レベル");
        this.afterlv = new Spinner(plan, SWT.BORDER);
        GridData gdAfterlv = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdAfterlv.widthHint = 45;
        this.afterlv.setLayoutData(gdAfterlv);
        this.afterlv.setMaximum(ExpTable.MAX_LEVEL);
        this.afterlv.setMinimum(1);
        Label label5 = new Label(plan, SWT.NONE);
        label5.setText("Lv");
        this.afterexp = new Text(plan, SWT.BORDER | SWT.READ_ONLY);
        GridData gdAfterexp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdAfterexp.widthHint = 60;
        this.afterexp.setLayoutData(gdAfterexp);
        this.afterexp.setText("0");
        Label label6 = new Label(plan, SWT.NONE);
        label6.setText("Exp");

        Composite plan2 = new Composite(this.shell, SWT.NONE);
        plan2.setLayout(new RowLayout());
        plan2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label label7 = new Label(plan2, SWT.NONE);
        label7.setText("海域");
        this.seacombo = new Combo(plan2, SWT.READ_ONLY);
        for (Entry<String, Integer> entry : SeaExp.get().entrySet()) {
            this.seacombo.add(entry.getKey());
        }
        Label label8 = new Label(plan2, SWT.NONE);
        label8.setText("評価");
        this.evalcombo = new Combo(plan2, SWT.READ_ONLY);
        for (Entry<String, Double> entry : EvaluateExp.get().entrySet()) {
            this.evalcombo.add(entry.getKey());
        }

        Composite plan3 = new Composite(this.shell, SWT.NONE);
        plan3.setLayout(new RowLayout());
        plan3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.flagbtn = new Button(plan3, SWT.CHECK);
        this.flagbtn.setText("旗艦");
        this.mvpbtn = new Button(plan3, SWT.CHECK);
        this.mvpbtn.setText("MVP");

        Composite plan4 = new Composite(this.shell, SWT.NONE);
        plan4.setLayout(new FillLayout());
        plan4.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        new Label(plan4, SWT.SEPARATOR | SWT.HORIZONTAL);

        Composite result = new Composite(this.shell, SWT.NONE);
        result.setLayout(new GridLayout(4, false));
        result.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label label10 = new Label(result, SWT.NONE);
        label10.setText("1回あたり");
        this.getexp = new Text(result, SWT.BORDER | SWT.READ_ONLY);
        GridData gdGetexp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdGetexp.widthHint = 55;
        this.getexp.setLayoutData(gdGetexp);
        new Label(result, SWT.NONE);
        new Label(result, SWT.NONE);
        Label label11 = new Label(result, SWT.NONE);
        label11.setText("必要経験値");
        this.needexp = new Text(result, SWT.BORDER | SWT.READ_ONLY);
        GridData gdNeedexp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdNeedexp.widthHint = 55;
        this.needexp.setLayoutData(gdNeedexp);
        Label label12 = new Label(result, SWT.NONE);
        label12.setText("戦闘回数");
        this.battlecount = new Text(result, SWT.BORDER | SWT.READ_ONLY);
        GridData gdBattlecount = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdBattlecount.widthHint = 55;
        this.battlecount.setLayoutData(gdBattlecount);

        // 海域のインデックス値を復元
        for (int i = 0; i < this.seacombo.getItemCount(); i++) {
            if (this.seacombo.getItem(i).equals(AppConfig.get().getDefaultSea())) {
                this.seacombo.select(i);
            }
        }
        // 評価のインデックス値を復元
        for (int i = 0; i < this.evalcombo.getItemCount(); i++) {
            if (this.evalcombo.getItem(i).equals(AppConfig.get().getDefaultEvaluate())) {
                this.evalcombo.select(i);
            }
        }

        this.shipcombo.addSelectionListener(new PresetListener());
        secretary.addSelectionListener(new SecretaryListener());
        SelectionListener beforeLvListener = new BeforeLvListener(this.beforexp, this.beforelv);
        this.beforelv.addSelectionListener(beforeLvListener);
        this.beforelv.addMouseWheelListener(new WheelListener(this.beforelv, beforeLvListener));
        SelectionListener afterLvListener = new BeforeLvListener(this.afterexp, this.afterlv);
        this.afterlv.addSelectionListener(afterLvListener);
        this.afterlv.addMouseWheelListener(new WheelListener(this.afterlv, afterLvListener));

        this.seacombo.addSelectionListener(new UpdateListener());
        this.evalcombo.addSelectionListener(new UpdateListener());
        this.flagbtn.addSelectionListener(new UpdateListener());
        this.mvpbtn.addSelectionListener(new UpdateListener());

        final EventListener updateListener = new EventListener() {
            @Override
            public void update(DataType type, Data data) {
                switch (type) {
                case PORT:
                case SHIP2:
                case SHIP3:
                    CalcExpDialog.this.reload();
                    break;
                default:
                    break;
                }
            }
        };
        GlobalContext.addEventListener(updateListener);
        this.getShell().addListener(SWT.Dispose, new Listener() {
            @Override
            public void handleEvent(Event event) {
                GlobalContext.removeEventListener(updateListener);
            }
        });

        // 選択する項目はドラックで移動できないようにする
        for (Control c : new Control[] { this.shipcombo, secretary, this.beforexp, this.afterexp, this.getexp,
                this.needexp, this.beforelv, this.afterlv, this.seacombo, this.evalcombo, this.flagbtn,
                this.mvpbtn }) {
            c.setData("disable-drag-move", true);
        }

        // 「新しいウィンドウを作る」を右クリックメニューに追加する
        final MenuItem createNewWin = new MenuItem(this.getPopupMenu(), SWT.PUSH, 0);
        createNewWin.setText("新しいウィンドウを開く(&N)\tCtrl+N");
        createNewWin.setAccelerator(SWT.CTRL + 'N');
        createNewWin.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new CalcExpDialog().open();
            }
        });
        new MenuItem(this.getPopupMenu(), SWT.SEPARATOR, 1);

        this.shell.pack();
        this.setShipComboData();
        this.preset();
        this.calc();
    }

    /**
     * プリセットを行う
     */
    public void preset() {
        if (this.shipcombo.getSelectionIndex() > -1) {
            ShipDto ship = this.shiplist.get(this.shipcombo.getSelectionIndex());
            if (ship != null) {
                int before = ship.getLv();
                int after = this.afterlv.getSelection();
                // 改造Lv
                int afterlv = ship.getShipInfo().getAfterlv();
                if (before < afterlv) {
                    after = afterlv;
                }
                if (before > after) {
                    after = before + 1;
                }
                // 目標レベルが150を超える場合は150に設定
                after = Math.min(after, ExpTable.MAX_LEVEL);

                String beforeexpstr = String.valueOf(ship.getExp());
                String afterexpstr = String.valueOf(ExpTable.get().get(after));

                this.beforelv.setSelection(before);
                this.afterlv.setSelection(after);

                this.beforexp.setText(beforeexpstr);
                this.afterexp.setText(afterexpstr);
            }
        }
    }

    /**
     * 計算を行う
     */
    private void calc() {
        if ((this.seacombo.getSelectionIndex() < 0) || (this.evalcombo.getSelectionIndex() < 0)) {
            return;
        }
        // 必要経験値
        int needexpint = Integer.parseInt(this.afterexp.getText()) - Integer.parseInt(this.beforexp.getText());
        // 基礎経験値
        int baseexp = SeaExp.get().get(this.seacombo.getItem(this.seacombo.getSelectionIndex()));
        // 評価
        double eval = EvaluateExp.get().get(this.evalcombo.getItem(this.evalcombo.getSelectionIndex()));
        // 得られる経験値
        int getexp = CalcExpUtils.getExp(baseexp, eval, this.flagbtn.getSelection(), this.mvpbtn.getSelection());
        // 戦闘回数
        int count = BigDecimal.valueOf(needexpint).divide(BigDecimal.valueOf(getexp), RoundingMode.CEILING).intValue();
        // 1回の戦闘
        this.getexp.setText(String.valueOf(getexp));
        // 必要経験値
        this.needexp.setText(Integer.toString(needexpint));
        // 戦闘回数
        this.battlecount.setText(Integer.toString(count));
    }

    /**
     * コンボボックスに艦娘をセットします
     * 
     * @param combo
     */
    private void setShipComboData() {
        int selected = -1;
        if (this.shipcombo.getSelectionIndex() != -1) {
            selected = this.shiplist.get(this.shipcombo.getSelectionIndex()).getId();
        }
        else {
            ShipDto secretary = GlobalContext.getSecretary();
            if (secretary != null) {
                selected = secretary.getId();
            }
        }
        // コンボボックスから全ての艦娘を削除
        this.shipcombo.removeAll();
        // 表示用文字列と艦娘の紐付けを削除
        this.shiplist.clear();
        // 艦娘IDの最大を取得してゼロ埋め長さを算出
        for (ShipDto ship : GlobalContext.getShipMap().values()) {
            this.shiplist.add(ship);
        }
        // 艦娘を経験値順でソート
        Collections.sort(this.shiplist, new Comparator<ShipDto>() {
            @Override
            public int compare(ShipDto o1, ShipDto o2) {
                return Integer.compare(o2.getExp(), o1.getExp());
            }
        });
        // コンボボックスに追加
        for (int i = 0; i < this.shiplist.size(); i++) {
            ShipDto ship = this.shiplist.get(i);
            this.shipcombo.add(ship.getFriendlyName());
            if (ship.getId() == selected) {
                this.shipcombo.select(i);
            }
        }
        // コントロールを再配置
        this.shipcombo.pack();
        this.shipcombo.getParent().pack();
    }

    /**
     * 艦娘の状態を更新する
     * 
     */
    private void reload() {
        this.setShipComboData();
        this.preset();
        this.calc();
    }

    /**
     * 計算結果を更新する
     *
     */
    private final class UpdateListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            CalcExpDialog.this.calc();
        }
    }

    /**
     * 秘書艦をセット
     *
     */
    private final class SecretaryListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            CalcExpDialog.this.shipcombo.deselectAll();
            CalcExpDialog.this.reload();
        }
    }

    /**
     * プリセットを行う
     *
     */
    private final class PresetListener extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            CalcExpDialog.this.preset();
            CalcExpDialog.this.calc();
        }
    }

    /**
     * 今のレベルが変更された
     *
     */
    private final class BeforeLvListener extends SelectionAdapter {
        private final Text beforexp;
        private final Spinner beforelv;

        /**
         * @param beforexp
         * @param beforelv
         */
        private BeforeLvListener(Text beforexp, Spinner beforelv) {
            this.beforexp = beforexp;
            this.beforelv = beforelv;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            String beforeexpstr = String.valueOf(ExpTable.get().get(this.beforelv.getSelection()));
            this.beforexp.setText(beforeexpstr);
            CalcExpDialog.this.calc();
        }
    }

    /**
     * 目標レベルが変更された
     *
     */
    private final class AfterLvListener extends SelectionAdapter {
        private final Text afterexp;
        private final Spinner afterlv;

        /**
         * @param afterexp
         * @param afterlv
         */
        private AfterLvListener(Text afterexp, Spinner afterlv) {
            this.afterexp = afterexp;
            this.afterlv = afterlv;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            String afterexpstr = String.valueOf(ExpTable.get().get(this.afterlv.getSelection()));
            this.afterexp.setText(afterexpstr);
            CalcExpDialog.this.calc();
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
