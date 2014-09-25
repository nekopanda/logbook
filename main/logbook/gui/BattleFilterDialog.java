/**
 * 
 */
package logbook.gui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import logbook.dto.ResultRank;
import logbook.gui.logic.IntegerPair;
import logbook.internal.BattleResultFilter;
import logbook.internal.BattleResultServer;
import logbook.util.ReportUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Nekopanda
 *
 */
public class BattleFilterDialog extends WindowBase {

    private final DropReportTable parent;

    private List<IntegerPair> mapList;
    private CheckAndCombo mapCombo;

    private List<Integer> cellList;
    private CheckAndCombo cellCombo;

    private List<String> shipList;
    private CheckAndCombo shipCombo;

    private List<ResultRank> rankList;
    private CheckAndCombo rankCombo;

    private CheckAndCalender fromDate;
    private CheckAndCalender toDate;

    public BattleFilterDialog(DropReportTable parent) {
        super.createContents(parent, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE, false);
        this.parent = parent;
    }

    /**
     * Open the dialog.
     * @return the result
     */
    @Override
    public void open() {
        Shell shell = this.getShell();
        this.createContents();
        this.registerEvents();
        shell.open();
        shell.layout();
        Display display = shell.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        Shell shell = this.getShell();
        shell.setText("フィルター");
        GridLayout glShell = new GridLayout(4, false);
        shell.setLayout(glShell);

        SelectionListener listener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                BattleFilterDialog.this.parent.updateFilter(BattleFilterDialog.this.createFilter());
            }
        };

        // マップ
        this.mapList = BattleResultServer.get().getMapList();
        this.mapCombo = new CheckAndCombo(shell, "マップ", 1);
        for (IntegerPair mapno : this.mapList) {
            this.mapCombo.combo.add(mapno.toString());
        }
        this.mapCombo.initState(listener);

        // セル
        this.cellList = BattleResultServer.get().getCellList();
        this.cellCombo = new CheckAndCombo(shell, "マス", 1);
        for (Integer cellno : this.cellList) {
            this.cellCombo.combo.add(String.valueOf(cellno));
        }
        this.cellCombo.initState(listener);

        // ドロップ艦
        this.shipList = BattleResultServer.get().getDropShipList();
        this.shipCombo = new CheckAndCombo(shell, "ドロップ", 1);
        for (String shipName : this.shipList) {
            this.shipCombo.combo.add(shipName);
        }
        this.shipCombo.initState(listener);

        // ランク
        this.rankList = new ArrayList<ResultRank>();
        this.rankCombo = new CheckAndCombo(shell, "ランク", 1);
        for (ResultRank rank : ResultRank.values()) {
            this.rankCombo.combo.add(rank.toString());
            this.rankList.add(rank);
        }
        this.rankCombo.initState(listener);

        // 時刻
        Button startCheck = new Button(shell, SWT.CHECK);
        Button endCheck = new Button(shell, SWT.CHECK);
        this.fromDate = new CheckAndCalender(shell, startCheck, "開始日時", 2);
        this.toDate = new CheckAndCalender(shell, endCheck, "終了日時", 2);
        this.fromDate.initState(listener, BattleResultServer.get().getFirstBattleTime());
        this.toDate.initState(listener, BattleResultServer.get().getLastBattleTime());

        // 初期値
        BattleResultFilter filter = this.parent.getFilter();
        if (filter != null) {
            int mapIdx = (filter.map == null) ? -1 : this.mapList.indexOf(filter.map);
            int cellIdx = (filter.cell == null) ? -1 : this.cellList.indexOf(filter.cell);
            int dtopShipIdx = (filter.dropShip == null) ? -1 : this.shipList.indexOf(filter.dropShip);
            int mapComboIdx = (filter.rank == null) ? -1 : this.rankList.indexOf(filter.rank);

            if (mapIdx != -1) {
                this.mapCombo.select(mapIdx);
            }
            if (cellIdx != -1) {
                this.cellCombo.select(cellIdx);
            }
            if (dtopShipIdx != -1) {
                this.shipCombo.select(dtopShipIdx);
            }
            if (mapComboIdx != -1) {
                this.rankCombo.select(mapComboIdx);
            }
            if (filter.fromTime != null) {
                this.fromDate.setDate(toCalendar(filter.fromTime), false);
            }
            if (filter.toTime != null) {
                this.toDate.setDate(toCalendar(filter.toTime), true);
            }
        }

        shell.pack();
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
        filter.map = (IntegerPair) this.getSelectedItem(this.mapCombo, this.mapList);
        filter.cell = (Integer) this.getSelectedItem(this.cellCombo, this.cellList);
        filter.dropShip = (String) this.getSelectedItem(this.shipCombo, this.shipList);
        ResultRank rank = (ResultRank) this.getSelectedItem(this.rankCombo, this.rankList);
        filter.rank = (rank != null) ? rank : null;
        filter.fromTime = this.fromDate.getSelectedDate(false);
        filter.toTime = this.toDate.getSelectedDate(true);
        return filter;
    }

    private Object getSelectedItem(CheckAndCombo combo, List data) {
        if (combo.button.getSelection()) {
            int idx = combo.combo.getSelectionIndex();
            if ((idx >= 0) && (idx < data.size())) {
                return data.get(idx);
            }
        }
        return null;
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
    }

    private static class CheckAndCalender extends CheckAdapter {
        final DateTime datetime;

        public CheckAndCalender(Shell shell, Button check, String text, int comboSpan) {
            super(check, new DateTime(shell, SWT.CALENDAR));
            GridData data = new GridData(GridData.FILL_HORIZONTAL);
            data.horizontalSpan = comboSpan;
            this.button.setLayoutData(data);
            GridData data2 = new GridData(GridData.FILL_HORIZONTAL);
            data2.horizontalSpan = comboSpan;
            this.composite.setLayoutData(data2);
            this.button.setText(text);
            this.datetime = (DateTime) this.composite;
        }

        public void setDate(Calendar calendar, boolean endOfDay) {
            this.button.setSelection(true);
            this.datetime.setEnabled(true);
            this.datetime.setYear(calendar.get(Calendar.YEAR));
            this.datetime.setMonth(calendar.get(Calendar.MONTH));
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            this.datetime.setDay(endOfDay ? (day - 1) : day);
        }

        public Date getSelectedDate(boolean endOfDay) {
            if (this.button.getSelection()) {
                int year = this.datetime.getYear();
                int month = this.datetime.getMonth();
                int day = this.datetime.getDay();
                Calendar cal = Calendar.getInstance();
                cal.set(year, month, endOfDay ? (day + 1) : day);
                return cal.getTime();
            }
            return null;
        }

        public void initState(SelectionListener listener, Date date) {
            this.button.addSelectionListener(listener);
            this.datetime.addSelectionListener(listener);
            this.setDate(ReportUtils.calendarFromDate(date), false);
        }
    }
}
