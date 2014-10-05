package logbook.gui;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import logbook.config.AppConfig;
import logbook.data.context.GlobalContext;
import logbook.dto.ShipDto;
import logbook.internal.EvaluateExp;
import logbook.internal.ExpTable;
import logbook.internal.SeaExp;
import logbook.util.CalcExpUtils;

import org.apache.commons.lang3.StringUtils;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * 経験値計算機
 *
 */
public final class CalcExpDialog extends WindowBase {

    /** 旗艦 */
    private static boolean flag = true;
    /** MVP */
    private static boolean mvp;

    private final Map<String, ShipDto> shipmap = new HashMap<String, ShipDto>();

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
    private Spinner kikanlv;
    private Spinner nisekimelv;
    private Text swinpracticeexp;
    private Text abwinpracticeexp;

    /**
     * Create the dialog.
     * @param parent
     */
    public CalcExpDialog(Shell parent, MenuItem menuItem) {
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
        super.createContents(this.parent, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE, false);
        this.shell = this.getShell();
        this.shell.setText("経験値計算機");
        this.shell.setLayout(new GridLayout(1, false));

        Composite select = new Composite(this.shell, SWT.NONE);
        select.setLayout(new RowLayout());
        select.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.shipcombo = new Combo(select, SWT.READ_ONLY);
        Button reload = new Button(select, SWT.NONE);
        reload.setText("更新");

        Composite plan = new Composite(this.shell, SWT.NONE);
        plan.setLayout(new GridLayout(5, false));
        plan.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label label1 = new Label(plan, SWT.NONE);
        label1.setText("今のレベル");
        this.beforelv = new Spinner(plan, SWT.BORDER);
        GridData gdBeforelv = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdBeforelv.widthHint = 45;
        this.beforelv.setLayoutData(gdBeforelv);
        this.beforelv.setMaximum(150);
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
        this.afterlv.setMaximum(150);
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

        //演習経験値ここから
        Composite plan5 = new Composite(this.shell, SWT.NONE);
        plan5.setLayout(new FillLayout());
        plan5.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        new Label(plan5, SWT.SEPARATOR | SWT.HORIZONTAL);

        Composite plan6 = new Composite(this.shell, SWT.NONE);
        plan6.setLayout(new RowLayout());
        plan6.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label label13 = new Label(plan6, SWT.NONE);
        label13.setText("演習基本経験値(仮)");

        Composite plan7 = new Composite(this.shell, SWT.NONE);
        plan7.setLayout(new GridLayout(3, false));
        plan7.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label label14 = new Label(plan7, SWT.NONE);
        label14.setText("旗艦レベル");
        this.kikanlv = new Spinner(plan7, SWT.BORDER);
        GridData kikan = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        kikan.widthHint = 45;
        this.kikanlv.setLayoutData(kikan);
        this.kikanlv.setMaximum(150);
        this.kikanlv.setMinimum(1);
        Label label15 = new Label(plan7, SWT.NONE);
        label15.setText("Lv");

        Label label16 = new Label(plan7, SWT.NONE);
        label16.setText("2隻目レベル");
        this.nisekimelv = new Spinner(plan7, SWT.BORDER);
        GridData nisekime = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        nisekime.widthHint = 45;
        this.nisekimelv.setLayoutData(nisekime);
        this.nisekimelv.setMaximum(150);
        this.nisekimelv.setMinimum(1);
        Label label17 = new Label(plan7, SWT.NONE);
        label17.setText("Lv");

        SelectionListener KikanLvListener = new KikanLvListener(this.kikanlv);
        this.kikanlv.addSelectionListener(KikanLvListener);
        this.kikanlv.addMouseWheelListener(new WheelListener(this.kikanlv, KikanLvListener));
        SelectionListener NisekimeLvListener = new NisekimeLvListener(this.nisekimelv);
        this.nisekimelv.addSelectionListener(NisekimeLvListener);
        this.nisekimelv.addMouseWheelListener(new WheelListener(this.nisekimelv, NisekimeLvListener));

        Composite practice = new Composite(this.shell, SWT.NONE);
        practice.setLayout(new GridLayout(3, false));
        practice.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label label18 = new Label(practice, SWT.NONE);
        label18.setText("S勝利");
        this.swinpracticeexp = new Text(practice, SWT.BORDER | SWT.READ_ONLY);
        GridData gdPracticeSWinGetexp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdPracticeSWinGetexp.widthHint = 55;
        this.swinpracticeexp.setLayoutData(gdPracticeSWinGetexp);
        Label label19 = new Label(practice, SWT.NONE);
        label19.setText("Exp");
        Label label20 = new Label(practice, SWT.NONE);
        label20.setText("A,B勝利");
        this.abwinpracticeexp = new Text(practice, SWT.BORDER | SWT.READ_ONLY);
        GridData gdPracticeABWinGetexp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdPracticeABWinGetexp.widthHint = 55;
        this.abwinpracticeexp.setLayoutData(gdPracticeABWinGetexp);
        Label label21 = new Label(practice, SWT.NONE);
        label21.setText("Exp");
        //演習経験値ここまで

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
        // 旗艦チェックを復元
        this.flagbtn.setSelection(flag);
        // MVPチェックを復元
        this.mvpbtn.setSelection(mvp);

        this.shipcombo.addSelectionListener(new PresetListener());
        reload.addSelectionListener(new ReloadListener());
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

        // 選択する項目はドラックで移動できないようにする
        for (Control c : new Control[] { this.shipcombo, reload, this.beforexp, this.afterexp, this.getexp,
                this.needexp, this.beforelv, this.afterlv, this.seacombo, this.evalcombo, this.flagbtn,
                this.mvpbtn, this.kikanlv, this.nisekimelv }) {
            c.setData("disable-drag-move", true);
        }

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
            ShipDto ship = CalcExpDialog.this.shipmap
                    .get(this.shipcombo.getItem(this.shipcombo.getSelectionIndex()));
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
                after = Math.min(after, 150);

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
        // 旗艦チェックを保存
        CalcExpDialog.flag = this.flagbtn.getSelection();
        // MVPチェックを保存
        CalcExpDialog.mvp = this.mvpbtn.getSelection();

        //演習獲得経験値
        //敵旗艦の累積経験値
        int kikanexp = this.kikanlv.getSelection();
        //敵2隻目の累積経験値
        int nisekimeexp = this.nisekimelv.getSelection();
        //補正前経験値 (補正前経験値) = (旗艦Lvの必要累計経験値) / 100 + (2隻目Lvの必要累計経験値) / 300
        int practicebase = (ExpTable.get().get(kikanexp) / 100) + (ExpTable.get().get(nisekimeexp) / 300);

        //基本経験値計算
        if (practicebase <= 500) {
            //1) 補正前経験値 ≦ 500 の場合、(基本経験値) = (補正前経験値) * (評価による補正)
            this.swinpracticeexp.setText(String.valueOf((int) (practicebase * 1.2)));
            this.abwinpracticeexp.setText(String.valueOf((int) (practicebase * 1.0)));
        }
        else {
            //2) 補正前経験値 ＞ 500 の場合、(基本経験値) = {500 + √(補正前経験値 - 500)} * (評価による補正)
            this.swinpracticeexp.setText(String.valueOf((int) ((500 + Math.sqrt(practicebase - 500)) * 1.2)));
            this.abwinpracticeexp.setText(String.valueOf((int) ((500 + Math.sqrt(practicebase - 500)) * 1.0)));
        }

    }

    /**
     * コンボボックスに艦娘をセットします
     *
     * @param combo
     */
    private void setShipComboData() {
        // 選択していた艦娘を取得
        int select = 0;
        if (this.shipcombo.getSelectionIndex() >= 0) {
            ShipDto ship = this.shipmap.get(this.shipcombo.getItem(this.shipcombo.getSelectionIndex()));
            select = ship.getId();
        } else {
            // 誰も選択されてない状態の場合は秘書艦を選択する
            ShipDto ship = GlobalContext.getSecretary();
            if (ship != null) {
                select = ship.getId();
            }
        }
        // コンボボックスから全ての艦娘を削除
        this.shipcombo.removeAll();
        // 表示用文字列と艦娘の紐付けを削除
        this.shipmap.clear();
        // 艦娘IDの最大を取得してゼロ埋め長さを算出
        int maxshipid = 0;
        for (ShipDto ship : GlobalContext.getShipMap().values()) {
            maxshipid = Math.max(ship.getId(), maxshipid);
        }
        int padlength = String.valueOf(maxshipid).length();
        // 表示用文字列と艦娘の紐付けを追加
        for (ShipDto ship : GlobalContext.getShipMap().values()) {
            this.shipmap.put(this.getShipLabel(ship, padlength), ship);
        }
        // 艦娘を経験値順でソート
        List<ShipDto> ships = new ArrayList<ShipDto>(this.shipmap.values());
        Collections.sort(ships, new Comparator<ShipDto>() {
            @Override
            public int compare(ShipDto o1, ShipDto o2) {
                return Integer.compare(o2.getExp(), o1.getExp());
            }
        });
        // コンボボックスに追加
        for (int i = 0; i < ships.size(); i++) {
            String key = this.getShipLabel(ships.get(i), padlength);
            this.shipcombo.add(key);
            if (ships.get(i).getId() == select) {
                this.shipcombo.select(i);
            }
        }
        // コントロールを再配置
        this.shipcombo.pack();
        this.shipcombo.getParent().pack();
    }

    /**
     * 艦娘のプルダウン表示用文字列を作成します
     *
     * @param ship
     * @param padlength
     * @return
     */
    private String getShipLabel(ShipDto ship, int padlength) {
        return new StringBuilder().append(StringUtils.leftPad(String.valueOf(ship.getId()), padlength, '0'))
                .append(": ").append(ship.getName()).append(" (Lv").append(ship.getLv() + ")").toString();
    }

    /**
     * 艦娘の状態を更新する
     *
     */
    private final class ReloadListener extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            CalcExpDialog.this.setShipComboData();
            CalcExpDialog.this.preset();
            CalcExpDialog.this.calc();
        }
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
     * 演習旗艦レベル変更された
     *
     */
    private final class KikanLvListener extends SelectionAdapter {
        private final Spinner kikanlv;

        /**
         * @param kikanlv
         */
        private KikanLvListener(Spinner kikanlv) {
            this.kikanlv = kikanlv;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            CalcExpDialog.this.calc();
        }
    }

    /**
     * 演習2隻目のレベル変更された
     *
     */
    private final class NisekimeLvListener extends SelectionAdapter {
        private final Spinner nisekimelv;

        /**
         * @param kikanlv
         */
        private NisekimeLvListener(Spinner nisekimelv) {
            this.nisekimelv = nisekimelv;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
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
