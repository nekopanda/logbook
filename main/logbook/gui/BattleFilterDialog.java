/**
 * 
 */
package logbook.gui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import logbook.dto.ResultRank;
import logbook.gui.logic.GuiUpdator;
import logbook.gui.logic.IntegerPair;
import logbook.gui.logic.LayoutLogic;
import logbook.internal.BattleResultFilter;
import logbook.internal.BattleResultServer;
import logbook.internal.TimeSpanKind;
import logbook.util.ReportUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Nekopanda
 *
 */
public class BattleFilterDialog extends WindowBase {

    private static final String ALL_RANKS = "SABCDE";
    private static final String SELECT_RANK = "SELECT";

    private final DropReportTable parent;

    private List<IntegerPair> mapList;
    private CheckAndCombo mapCombo;

    private List<Integer> cellList;
    private CheckAndCombo cellCombo;

    private List<String> shipList;
    private CheckAndCombo shipCombo;

    private List<String> rankList;
    private CheckAndCombo rankCombo;

    private List<Boolean> kindList;
    private CheckAndCombo kindCombo;

    private List<TimeSpanKind> timeList;
    private CheckAndCombo timeCombo;

    private LabelAndCalender fromDate;
    private LabelAndCalender toDate;

    private Composite rankCompo;
    private Label rankPlaceholder;
    private List<Button> rankCheckboxList;

    public BattleFilterDialog(DropReportTable parent) {
        super.createContents(parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE, false);
        this.parent = parent;
    }

    /**
     * Open the dialog.
     */
    @Override
    public void open() {
        if (!this.isWindowInitialized()) {
            this.createContents();
            this.registerEvents();
            // 閉じたときに dispose しない
            this.getShell().addShellListener(new ShellAdapter() {
                @Override
                public void shellClosed(ShellEvent e) {
                    e.doit = false;
                    BattleFilterDialog.this.setVisible(false);
                }
            });
            this.setWindowInitialized(true);
        }
        this.updateContents();
        if (this.getVisible() == false) {
            this.getShell().pack();
            this.setVisible(true);
        }
        this.getShell().setActive();
        return;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        Shell shell = this.getShell();
        shell.setText("フィルター");
        GridLayout glShell = new GridLayout(5, false);
        shell.setLayout(glShell);

        SelectionListener listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                BattleFilterDialog.this.updateCalenderVisible();
                BattleFilterDialog.this.updateRankCheckboxVisible();
                BattleFilterDialog.this.parent.updateFilter(BattleFilterDialog.this.createFilter());
            }
        };

        // マップ
        this.mapCombo = new CheckAndCombo(shell, "マップ", 1);
        this.mapCombo.initState(listener);

        // セル
        this.cellCombo = new CheckAndCombo(shell, "マス", 1);
        this.cellCombo.initState(listener);

        // ランクチェックボックス
        this.rankCompo = new Composite(shell, SWT.NONE);
        this.rankCompo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 5));
        RowLayout rankCompoLayout = new RowLayout(SWT.VERTICAL);
        rankCompoLayout.wrap = false;
        this.rankCompo.setLayout(rankCompoLayout);
        this.rankCheckboxList = new ArrayList<Button>();
        for (char rank : ALL_RANKS.toCharArray()) {
            Button checkbox = new Button(this.rankCompo, SWT.CHECK);
            this.rankCheckboxList.add(checkbox);
            checkbox.setText(String.valueOf(rank));
            checkbox.setData(rank);
            checkbox.addSelectionListener(listener);
        }
        this.rankPlaceholder = new Label(shell, SWT.NONE);
        this.rankPlaceholder.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 5));

        // ドロップ艦
        this.shipCombo = new CheckAndCombo(shell, "ドロップ", 1);
        this.shipCombo.initState(listener);

        // ランク
        this.rankList = new ArrayList<String>();
        for (char rank : ALL_RANKS.toCharArray()) {
            this.rankList.add(String.valueOf(rank));
        }
        this.rankList.add("SAB");
        this.rankList.add("CDE");
        this.rankList.add(SELECT_RANK);
        this.rankCombo = new CheckAndCombo(shell, "ランク", 1);
        for (String rankString : this.rankList) {
            if (rankString.length() == 1) {
                this.rankCombo.combo.add(ResultRank.fromRank(rankString).toString());
            }
            else {
                break;
            }
        }
        this.rankCombo.combo.add("勝利(S,A,B)");
        this.rankCombo.combo.add("敗北(C,D,E)");
        this.rankCombo.combo.add("右から選択");
        this.rankCombo.initState(listener);

        // 戦闘種別
        this.kindList = new ArrayList<Boolean>();
        this.kindCombo = new CheckAndCombo(shell, "戦闘種別", 1);
        this.kindCombo.combo.add("出撃のみ");
        this.kindList.add(false);
        this.kindCombo.combo.add("演習のみ");
        this.kindList.add(true);
        this.kindCombo.initState(listener);

        // 期間
        this.timeList = new ArrayList<TimeSpanKind>();
        this.timeCombo = new CheckAndCombo(shell, "期間", 1);
        for (TimeSpanKind time : TimeSpanKind.values()) {
            this.timeCombo.combo.add(time.toString());
            this.timeList.add(time);
        }
        this.timeCombo.initState(listener);

        // 時刻
        Label startLabel = new Label(shell, SWT.NONE);
        Label endLabel = new Label(shell, SWT.NONE);
        this.fromDate = new LabelAndCalender(shell, startLabel, "開始", 2);
        this.toDate = new LabelAndCalender(shell, endLabel, "終了", 2);
        this.fromDate.initState(listener, BattleResultServer.get().getFirstBattleTime());
        this.toDate.initState(listener, BattleResultServer.get().getLastBattleTime());

        // 初期値
        BattleResultFilter filter = this.parent.getFilter();
        if (filter != null) {
            int mapIdx = (filter.map == null) ? -1 : this.mapList.indexOf(filter.map);
            int cellIdx = (filter.cell == null) ? -1 : this.cellList.indexOf(filter.cell);
            int dtopShipIdx = (filter.dropShip == null) ? -1 : this.shipList.indexOf(filter.dropShip);

            int rankIdx = -1;
            if ((filter.rankCombo != null) && (filter.rankCombo.length() == 1)) {
                rankIdx = this.rankList.indexOf(filter.rankCombo);
                if (rankIdx == -1) {
                    rankIdx = this.rankList.indexOf(SELECT_RANK);
                }
            }

            int kindIdx =
                    (filter.printPractice == null) ? -1 :
                            filter.printPractice ? 1 : 0;
            int timeSpanIdx = (filter.timeSpan == null) ? -1 : this.timeList.indexOf(filter.timeSpan);

            if (mapIdx != -1) {
                this.mapCombo.select(mapIdx);
            }
            if (cellIdx != -1) {
                this.cellCombo.select(cellIdx);
            }
            if (dtopShipIdx != -1) {
                this.shipCombo.select(dtopShipIdx);
            }
            if (rankIdx != -1) {
                this.rankCombo.select(rankIdx);
            }

            if (kindIdx != -1) {
                this.kindCombo.select(kindIdx);
            }
            if (timeSpanIdx != -1) {
                this.timeCombo.select(timeSpanIdx);
            }
            if (filter.fromTime != null) {
                this.fromDate.setDate(toCalendar(filter.fromTime), false);
            }
            if (filter.toTime != null) {
                this.toDate.setDate(toCalendar(filter.toTime), true);
            }
        }

        this.updateCalenderVisible();
        this.updateRankCheckboxVisible();
        shell.pack();

        // データの更新を受け取る
        final Runnable datalistener = new GuiUpdator(new Runnable() {
            @Override
            public void run() {
                BattleFilterDialog.this.updateContents();
            }
        });
        BattleResultServer.addListener(datalistener);
        this.getShell().addListener(SWT.Dispose, new Listener() {
            @Override
            public void handleEvent(Event event) {
                BattleResultServer.removeListener(datalistener);
            }
        });
    }

    private void updateContents() {
        // マップ
        this.mapList = BattleResultServer.get().getMapList();
        this.mapCombo.updateContents(this.mapList);
        // セル
        this.cellList = BattleResultServer.get().getCellList();
        this.cellCombo.updateContents(this.cellList);
        // ドロップ艦
        this.shipList = BattleResultServer.get().getDropShipList();
        this.shipCombo.updateContents(this.shipList);
    }

    private static Calendar toCalendar(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar;
    }

    private BattleResultFilter createFilter() {
        BattleResultFilter filter = this.parent.getFilter();
        if (filter == null) {
            filter = new BattleResultFilter();
        }
        filter.map = (IntegerPair) getSelectedItem(this.mapCombo, this.mapList);
        filter.cell = (Integer) getSelectedItem(this.cellCombo, this.cellList);
        filter.dropShip = (String) getSelectedItem(this.shipCombo, this.shipList);
        filter.rankCombo = (String) getSelectedItem(this.rankCombo, this.rankList);
        if ((filter.rankCombo != null) && filter.rankCombo.equals(SELECT_RANK)) {
            filter.rankCombo = "";
            for (Button check : this.rankCheckboxList) {
                if (check.getSelection()) {
                    Character rank = (Character) check.getData();
                    filter.rankCombo += rank;
                }
            }
        }
        filter.printPractice = (Boolean) getSelectedItem(this.kindCombo, this.kindList);
        TimeSpanKind timeSpan = (TimeSpanKind) getSelectedItem(this.timeCombo, this.timeList);
        if (timeSpan == TimeSpanKind.MANUAL) {
            filter.timeSpan = null;
            filter.fromTime = this.fromDate.getSelectedDate(false);
            filter.toTime = this.toDate.getSelectedDate(true);
        }
        else {
            filter.timeSpan = timeSpan;
            filter.fromTime = null;
            filter.toTime = null;
        }
        return filter;
    }

    private static Object getSelectedItem(CheckAndCombo combo, List<?> data) {
        if (combo.button.getSelection()) {
            int idx = combo.combo.getSelectionIndex();
            if ((idx >= 0) && (idx < data.size())) {
                return data.get(idx);
            }
        }
        return null;
    }

    private void updateCalenderVisible() {
        TimeSpanKind timeSpan = (TimeSpanKind) getSelectedItem(this.timeCombo, this.timeList);
        boolean isManual = (timeSpan == TimeSpanKind.MANUAL);
        if (isManual ^ this.fromDate.getVisible()) {
            this.fromDate.setVisible(isManual);
            this.toDate.setVisible(isManual);
            if (isManual) {
                this.getShell().pack();
            }
        }
    }

    private void updateRankCheckboxVisible() {
        String selected = (String) getSelectedItem(this.rankCombo, this.rankList);
        boolean isSelect = ((selected != null) && selected.equals(SELECT_RANK));
        if (isSelect ^ this.rankCompo.getVisible()) {
            LayoutLogic.hide(this.rankCompo, !isSelect);
            LayoutLogic.hide(this.rankPlaceholder, isSelect);
            if (isSelect) {
                this.getShell().pack();
            }
        }
    }

    /**
     * 選択した時にコンボボックスを制御する
     */
    private static class CheckAdapter extends SelectionAdapter {

        final Button button;
        final Composite composite;

        public CheckAdapter(Button button, Composite composite) {
            this.button = button;
            this.composite = composite;
            button.addSelectionListener(this);
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            this.composite.setEnabled(this.button.getSelection());
        }
    }

    private static class CheckAndCombo extends CheckAdapter {
        final Combo combo;

        public CheckAndCombo(Shell shell, String text, int comboSpan) {
            super(new Button(shell, SWT.CHECK), new Combo(shell, SWT.READ_ONLY));
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = comboSpan;
            this.composite.setLayoutData(data);
            this.button.setText(text);
            this.combo = (Combo) this.composite;
            this.combo.setEnabled(false);
        }

        public void select(int idx) {
            this.button.setSelection(true);
            this.combo.setEnabled(true);
            this.combo.select(idx);
        }

        public void initState(SelectionListener listener) {
            this.button.addSelectionListener(listener);
            this.combo.addSelectionListener(listener);
            this.combo.select(0);
        }

        public void updateContents(List<?> items) {
            int prevSelection = this.combo.getSelectionIndex();
            this.combo.removeAll();
            for (Object item : items) {
                this.combo.add(item.toString());
            }
            this.combo.select(prevSelection);
        }
    }

    private static class LabelAndCalender {
        final DateTime datetime;
        final Label label;
        boolean visible;

        public LabelAndCalender(Shell shell, Label label, String text, int comboSpan) {
            this.label = label;
            this.datetime = new DateTime(shell, SWT.CALENDAR);
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = comboSpan;
            data.horizontalAlignment = SWT.CENTER;
            this.label.setLayoutData(data);
            GridData data2 = new GridData(GridData.FILL_HORIZONTAL);
            data2.horizontalSpan = comboSpan;
            this.datetime.setLayoutData(data2);
            this.label.setText(text);
            this.visible = true;
        }

        public void setDate(Calendar calendar, boolean endOfDay) {
            this.datetime.setYear(calendar.get(Calendar.YEAR));
            this.datetime.setMonth(calendar.get(Calendar.MONTH));
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            this.datetime.setDay(endOfDay ? (day - 1) : day);
        }

        public Date getSelectedDate(boolean endOfDay) {
            int year = this.datetime.getYear();
            int month = this.datetime.getMonth();
            int day = this.datetime.getDay();
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, endOfDay ? (day + 1) : day, 0, 0, 0);
            return cal.getTime();
        }

        public void setVisible(boolean visible) {
            if (this.visible != visible) {
                this.visible = visible;
                LayoutLogic.hide(this.label, !visible);
                LayoutLogic.hide(this.datetime, !visible);
            }
        }

        public boolean getVisible() {
            return this.visible;
        }

        public void initState(SelectionListener listener, Date date) {
            this.datetime.addSelectionListener(listener);
            this.setDate(ReportUtils.calendarFromDate(date), false);
        }
    }
}
