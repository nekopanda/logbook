/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
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

import logbook.data.context.GlobalContext;
import logbook.dto.ShipDto;
import logbook.internal.EvaluateExp;
import logbook.internal.ExpTable;
import logbook.internal.SeaExp;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * 必要経験値計算
 *
 */
public class CalcExp extends Dialog {

    private final Map<String, ShipDto> shipmap = new HashMap<String, ShipDto>();

    protected Object result;
    protected Shell shell;

    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    public CalcExp(Shell parent) {
        super(parent, SWT.SHELL_TRIM | SWT.MODELESS);
        this.setText("必要経験値計算機");

        for (ShipDto ship : GlobalContext.getShipMap().values()) {
            this.shipmap.put(this.getShipLabel(ship), ship);
        }
    }

    /**
     * Open the dialog.
     * @return the result
     */
    public Object open() {
        this.createContents();
        this.shell.open();
        this.shell.layout();
        Display display = this.getParent().getDisplay();
        while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return this.result;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        this.shell = new Shell(this.getParent(), this.getStyle());
        this.shell.setText(this.getText());
        this.shell.setLayout(new GridLayout(1, false));

        Composite select = new Composite(this.shell, SWT.NONE);
        select.setLayout(new RowLayout());
        select.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        final Combo shipcombo = new Combo(select, SWT.READ_ONLY);
        this.setShipComboData(shipcombo);
        Button preset = new Button(select, SWT.NONE);
        preset.setText("セット");

        Composite plan = new Composite(this.shell, SWT.NONE);
        plan.setLayout(new GridLayout(5, false));
        plan.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label label1 = new Label(plan, SWT.NONE);
        label1.setText("今のレベル");
        final Spinner beforelv = new Spinner(plan, SWT.BORDER);
        GridData gdBeforelv = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdBeforelv.widthHint = 20;
        beforelv.setLayoutData(gdBeforelv);
        beforelv.setMaximum(99);
        beforelv.setMinimum(1);
        Label label2 = new Label(plan, SWT.NONE);
        label2.setText("Lv");
        final Text beforexp = new Text(plan, SWT.BORDER | SWT.READ_ONLY);
        GridData gdBeforexp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdBeforexp.widthHint = 55;
        beforexp.setLayoutData(gdBeforexp);
        beforexp.setText("0");
        Label label3 = new Label(plan, SWT.NONE);
        label3.setText("Exp");

        Label label4 = new Label(plan, SWT.NONE);
        label4.setText("目標レベル");
        final Spinner afterlv = new Spinner(plan, SWT.BORDER);
        GridData gdAfterlv = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdAfterlv.widthHint = 20;
        afterlv.setLayoutData(gdAfterlv);
        afterlv.setMaximum(99);
        afterlv.setMinimum(1);
        Label label5 = new Label(plan, SWT.NONE);
        label5.setText("Lv");
        final Text afterexp = new Text(plan, SWT.BORDER | SWT.READ_ONLY);
        GridData gdAfterexp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdAfterexp.widthHint = 55;
        afterexp.setLayoutData(gdAfterexp);
        afterexp.setText("0");
        Label label6 = new Label(plan, SWT.NONE);
        label6.setText("Exp");

        Composite plan2 = new Composite(this.shell, SWT.NONE);
        plan2.setLayout(new RowLayout());
        plan2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label label7 = new Label(plan2, SWT.NONE);
        label7.setText("海域");
        final Combo seacombo = new Combo(plan2, SWT.READ_ONLY);
        for (Entry<String, Integer> entry : SeaExp.get().entrySet()) {
            seacombo.add(entry.getKey());
        }
        Label label8 = new Label(plan2, SWT.NONE);
        label8.setText("評価");
        final Combo evalcombo = new Combo(plan2, SWT.READ_ONLY);
        for (Entry<String, Double> entry : EvaluateExp.get().entrySet()) {
            evalcombo.add(entry.getKey());
        }

        Composite plan3 = new Composite(this.shell, SWT.NONE);
        plan3.setLayout(new RowLayout());
        plan3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        final Button flagShip = new Button(plan3, SWT.CHECK);
        flagShip.setText("旗艦");
        final Button mvp = new Button(plan3, SWT.CHECK);
        mvp.setText("MVP");

        Composite plan4 = new Composite(this.shell, SWT.NONE);
        plan4.setLayout(new FillLayout());
        plan4.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        new Label(plan4, SWT.SEPARATOR | SWT.HORIZONTAL);

        Composite result = new Composite(this.shell, SWT.NONE);
        result.setLayout(new GridLayout(4, false));
        result.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label label10 = new Label(result, SWT.NONE);
        label10.setText("1回あたり");
        final Text getexp = new Text(result, SWT.BORDER | SWT.READ_ONLY);
        GridData gdGetexp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdGetexp.widthHint = 55;
        getexp.setLayoutData(gdGetexp);
        new Label(result, SWT.NONE);
        new Label(result, SWT.NONE);
        Label label11 = new Label(result, SWT.NONE);
        label11.setText("必要経験値");
        final Text needexp = new Text(result, SWT.BORDER | SWT.READ_ONLY);
        GridData gdNeedexp = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdNeedexp.widthHint = 55;
        needexp.setLayoutData(gdNeedexp);
        Label label12 = new Label(result, SWT.NONE);
        label12.setText("戦闘回数");
        final Text battlecount = new Text(result, SWT.BORDER | SWT.READ_ONLY);
        GridData gdBattlecount = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdBattlecount.widthHint = 55;
        battlecount.setLayoutData(gdBattlecount);

        preset.addSelectionListener(new PresetListener(beforexp, afterexp, shipcombo, flagShip, battlecount, needexp,
                seacombo, evalcombo, afterlv, getexp, mvp, beforelv));
        beforelv.addSelectionListener(new BeforeLvListener(beforexp, needexp, flagShip, evalcombo, seacombo,
                battlecount, beforelv, afterexp, getexp, mvp));
        afterlv.addSelectionListener(new AfterLvListener(beforexp, battlecount, getexp, flagShip, seacombo, afterexp,
                needexp, mvp, afterlv, evalcombo));

        seacombo.addSelectionListener(new UpdateListener(getexp, mvp, afterexp, needexp, beforexp, flagShip,
                battlecount, seacombo, evalcombo));
        evalcombo.addSelectionListener(new UpdateListener(getexp, mvp, afterexp, needexp, beforexp, flagShip,
                battlecount, seacombo, evalcombo));
        flagShip.addSelectionListener(new UpdateListener(getexp, mvp, afterexp, needexp, beforexp, flagShip,
                battlecount, seacombo, evalcombo));
        mvp.addSelectionListener(new UpdateListener(getexp, mvp, afterexp, needexp, beforexp, flagShip, battlecount,
                seacombo, evalcombo));

        this.shell.pack();
    }

    private void calc(Text beforexp, Text afterexp, Combo seacombo, Combo evalcombo, boolean flag, boolean mvp,
            Text getexp, Text needexp, Text battlecount) {
        if ((seacombo.getSelectionIndex() < 0) || (evalcombo.getSelectionIndex() < 0)) {
            return;
        }
        // 必要経験値
        int needexpint = Integer.parseInt(afterexp.getText()) - Integer.parseInt(beforexp.getText());
        // 基礎経験値
        int baseexp = SeaExp.get().get(seacombo.getItem(seacombo.getSelectionIndex()));
        // 評価
        double eval = EvaluateExp.get().get(evalcombo.getItem(evalcombo.getSelectionIndex()));
        // 得られる経験値
        double getexpd = baseexp * eval;
        if (flag) {
            getexpd *= 1.5;
        }
        if (mvp) {
            getexpd *= 2;
        }
        // 最大累積3.6倍
        getexpd = Math.min(getexpd, baseexp * 3.6);

        // 戦闘回数
        int count = BigDecimal.valueOf(needexpint).divide(BigDecimal.valueOf(getexpd), RoundingMode.CEILING).intValue();

        // 1回の戦闘
        getexp.setText(Long.toString(Math.round(getexpd)));
        // 必要経験値
        needexp.setText(Integer.toString(needexpint));
        // 戦闘回数
        battlecount.setText(Integer.toString(count));
    }

    /**
     * コンボボックスに艦娘をセットします
     * 
     * @param combo
     */
    private void setShipComboData(Combo combo) {
        List<ShipDto> ships = new ArrayList<ShipDto>(this.shipmap.values());
        Collections.sort(ships, new Comparator<ShipDto>() {
            @Override
            public int compare(ShipDto o1, ShipDto o2) {
                return Long.compare(o2.getExp(), o1.getExp());
            }
        });
        for (ShipDto ship : ships) {
            combo.add(this.getShipLabel(ship));
        }
    }

    /**
     * 艦娘のプルダウン表示用文字列を作成します
     * 
     * @param ship
     * @return
     */
    private String getShipLabel(ShipDto ship) {
        return StringUtils.leftPad(Long.toString(ship.getId()), 5, '0') + ": " + ship.getName() + " (Lv" + ship.getLv()
                + ")";
    }

    /**
     * 計算結果を更新する
     *
     */
    private final class UpdateListener extends SelectionAdapter {
        private final Text getexp;
        private final Button mvp;
        private final Text afterexp;
        private final Text needexp;
        private final Text beforexp;
        private final Button flagShip;
        private final Text battlecount;
        private final Combo seacombo;
        private final Combo evalcombo;

        /**
         * @param getexp
         * @param mvp
         * @param afterexp
         * @param needexp
         * @param beforexp
         * @param flagShip
         * @param battlecount
         * @param seacombo
         * @param evalcombo
         */
        private UpdateListener(Text getexp, Button mvp, Text afterexp, Text needexp, Text beforexp, Button flagShip,
                Text battlecount, Combo seacombo, Combo evalcombo) {
            this.getexp = getexp;
            this.mvp = mvp;
            this.afterexp = afterexp;
            this.needexp = needexp;
            this.beforexp = beforexp;
            this.flagShip = flagShip;
            this.battlecount = battlecount;
            this.seacombo = seacombo;
            this.evalcombo = evalcombo;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            CalcExp.this.calc(this.beforexp, this.afterexp, this.seacombo, this.evalcombo,
                    this.flagShip.getSelection(), this.mvp.getSelection(),
                    this.getexp, this.needexp, this.battlecount);
        }
    }

    /**
     * プリセットを行う
     *
     */
    private final class PresetListener extends SelectionAdapter {
        private final Text beforexp;
        private final Text afterexp;
        private final Combo shipcombo;
        private final Button flagShip;
        private final Text battlecount;
        private final Text needexp;
        private final Combo seacombo;
        private final Combo evalcombo;
        private final Spinner afterlv;
        private final Text getexp;
        private final Button mvp;
        private final Spinner beforelv;

        /**
         * @param beforexp
         * @param afterexp
         * @param shipcombo
         * @param flagShip
         * @param battlecount
         * @param needexp
         * @param seacombo
         * @param evalcombo
         * @param afterlv
         * @param getexp
         * @param mvp
         * @param beforelv
         */
        private PresetListener(Text beforexp, Text afterexp, Combo shipcombo, Button flagShip, Text battlecount,
                Text needexp, Combo seacombo, Combo evalcombo, Spinner afterlv, Text getexp, Button mvp,
                Spinner beforelv) {
            this.beforexp = beforexp;
            this.afterexp = afterexp;
            this.shipcombo = shipcombo;
            this.flagShip = flagShip;
            this.battlecount = battlecount;
            this.needexp = needexp;
            this.seacombo = seacombo;
            this.evalcombo = evalcombo;
            this.afterlv = afterlv;
            this.getexp = getexp;
            this.mvp = mvp;
            this.beforelv = beforelv;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (this.shipcombo.getSelectionIndex() > -1) {
                ShipDto ship = CalcExp.this.shipmap.get(this.shipcombo.getItem(this.shipcombo.getSelectionIndex()));
                if (ship != null) {
                    int before = (int) ship.getLv();
                    int after = Math.min(((int) (ship.getLv() + 20) / 10) * 10, 99);
                    String beforeexpstr = Long.toString(ship.getExp());
                    String afterexpstr = Long.toString(ExpTable.get().get(after));

                    this.beforelv.setSelection(before);
                    this.afterlv.setSelection(after);

                    this.beforexp.setText(beforeexpstr);
                    this.afterexp.setText(afterexpstr);
                }
            }
            CalcExp.this.calc(this.beforexp, this.afterexp, this.seacombo, this.evalcombo,
                    this.flagShip.getSelection(), this.mvp.getSelection(),
                    this.getexp, this.needexp, this.battlecount);
        }
    }

    /**
     * 今のレベルが変更された
     *
     */
    private final class BeforeLvListener extends SelectionAdapter {
        private final Text beforexp;
        private final Text needexp;
        private final Button flagShip;
        private final Combo evalcombo;
        private final Combo seacombo;
        private final Text battlecount;
        private final Spinner beforelv;
        private final Text afterexp;
        private final Text getexp;
        private final Button mvp;

        /**
         * @param beforexp
         * @param needexp
         * @param flagShip
         * @param evalcombo
         * @param seacombo
         * @param battlecount
         * @param beforelv
         * @param afterexp
         * @param getexp
         * @param mvp
         */
        private BeforeLvListener(Text beforexp, Text needexp, Button flagShip, Combo evalcombo, Combo seacombo,
                Text battlecount, Spinner beforelv, Text afterexp, Text getexp, Button mvp) {
            this.beforexp = beforexp;
            this.needexp = needexp;
            this.flagShip = flagShip;
            this.evalcombo = evalcombo;
            this.seacombo = seacombo;
            this.battlecount = battlecount;
            this.beforelv = beforelv;
            this.afterexp = afterexp;
            this.getexp = getexp;
            this.mvp = mvp;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            String beforeexpstr = Long.toString(ExpTable.get().get(this.beforelv.getSelection()));
            this.beforexp.setText(beforeexpstr);
            CalcExp.this.calc(this.beforexp, this.afterexp, this.seacombo, this.evalcombo,
                    this.flagShip.getSelection(), this.mvp.getSelection(),
                    this.getexp, this.needexp, this.battlecount);
        }
    }

    /**
     * 目標レベルが変更された
     *
     */
    private final class AfterLvListener extends SelectionAdapter {
        private final Text beforexp;
        private final Text battlecount;
        private final Text getexp;
        private final Button flagShip;
        private final Combo seacombo;
        private final Text afterexp;
        private final Text needexp;
        private final Button mvp;
        private final Spinner afterlv;
        private final Combo evalcombo;

        /**
         * @param beforexp
         * @param battlecount
         * @param getexp
         * @param flagShip
         * @param seacombo
         * @param afterexp
         * @param needexp
         * @param mvp
         * @param afterlv
         * @param evalcombo
         */
        private AfterLvListener(Text beforexp, Text battlecount, Text getexp, Button flagShip, Combo seacombo,
                Text afterexp, Text needexp, Button mvp, Spinner afterlv, Combo evalcombo) {
            this.beforexp = beforexp;
            this.battlecount = battlecount;
            this.getexp = getexp;
            this.flagShip = flagShip;
            this.seacombo = seacombo;
            this.afterexp = afterexp;
            this.needexp = needexp;
            this.mvp = mvp;
            this.afterlv = afterlv;
            this.evalcombo = evalcombo;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            String afterexpstr = Long.toString(ExpTable.get().get(this.afterlv.getSelection()));
            this.afterexp.setText(afterexpstr);
            CalcExp.this.calc(this.beforexp, this.afterexp, this.seacombo, this.evalcombo,
                    this.flagShip.getSelection(), this.mvp.getSelection(),
                    this.getexp, this.needexp, this.battlecount);
        }
    }
}
