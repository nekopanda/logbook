/**
 * 
 */
package logbook.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import logbook.config.AppConfig;
import logbook.constants.AppConstants;
import logbook.dto.BattleAggDetailsDto;
import logbook.dto.BattleAggUnitDto;
import logbook.gui.listener.TreeKeyShortcutAdapter;
import logbook.gui.listener.TreeToClipboardAdapter;
import logbook.internal.BattleAggDate;
import logbook.internal.BattleAggUnit;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * @author noname
 *
 */
public class BattleAggDialog extends Dialog {

    /** ヘッダー */
    private final String[] header = this.getTableHeader();

    /** シェル */
    private Shell shell;

    /** ツリーテーブル */
    private Tree tree;

    /** メニューバー */
    private Menu menubar;

    /** [操作]メニュー */
    private Menu opemenu;

    private Menu tablemenu;

    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    public BattleAggDialog(Shell parent) {
        super(parent, SWT.SHELL_TRIM | SWT.MODELESS);
        this.setText("SWT Dialog");
    }

    /**
     * Open the dialog.
     * @return the result
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
        // シェルを作成
        this.shell = new Shell(this.getParent(), this.getStyle());
        this.shell.setSize(this.getSize());
        this.shell.setText(this.getTitle());
        this.shell.setLayout(new FillLayout(SWT.HORIZONTAL));
        // メニューバー
        this.menubar = new Menu(this.shell, SWT.BAR);
        this.shell.setMenuBar(this.menubar);
        // ツリーテーブル
        this.tree = new Tree(this.shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
        this.tree.addKeyListener(new TreeKeyShortcutAdapter(this.header, this.tree));
        this.tree.setLinesVisible(true);
        this.tree.setHeaderVisible(true);
        // メニューバーのメニュー
        MenuItem operoot = new MenuItem(this.menubar, SWT.CASCADE);
        operoot.setText("操作");
        this.opemenu = new Menu(operoot);
        operoot.setMenu(this.opemenu);
        MenuItem reload = new MenuItem(this.opemenu, SWT.NONE);
        reload.setText("再読み込み(&R)\tF5");
        reload.setAccelerator(SWT.F5);
        reload.addSelectionListener(new TableReloadAdapter());
        // テーブル右クリックメニュー
        this.tablemenu = new Menu(this.tree);
        this.tree.setMenu(this.tablemenu);
        MenuItem sendclipbord = new MenuItem(this.tablemenu, SWT.NONE);
        sendclipbord.addSelectionListener(new TreeToClipboardAdapter(this.header, this.tree));
        sendclipbord.setText("クリップボードにコピー(&C)");
        MenuItem reloadtable = new MenuItem(this.tablemenu, SWT.NONE);
        reloadtable.setText("再読み込み(&R)");
        reloadtable.addSelectionListener(new TableReloadAdapter());

        this.setTableHeader();
        this.reloadTable();
    }

    /**
     * タイトルを返します
     * @return String
     */
    private String getTitle() {
        return "出撃統計";
    }

    /**
     * ウインドウサイズを返します
     * @return Point
     */
    private Point getSize() {
        return new Point(600, 350);
    }

    /**
     * テーブルボディーをクリアする
     */
    private void disposeTableBody() {
        TreeItem[] items = this.tree.getItems();
        for (int i = 0; i < items.length; i++) {
            items[i].dispose();
        }
    }

    /**
     * テーブルヘッダーをセットする
     */
    private void setTableHeader() {
        for (int i = 0; i < this.header.length; i++) {
            TreeColumn col = new TreeColumn(this.tree, SWT.LEFT);
            col.setText(this.header[i]);
        }
        this.packTableHeader();
    }

    /**
     * テーブルヘッダーの幅を調節する
     */
    private void packTableHeader() {
        TreeColumn[] columns = this.tree.getColumns();

        for (int i = 0; i < columns.length; i++) {
            columns[i].pack();
        }
    }

    /**
     * テーブルヘッダーを返します
     * @return String[]
     */
    private String[] getTableHeader() {
        return new String[] { "集計", "勝利合計", "S勝利", "A勝利", "B勝利", "C敗北", "D敗北" };
    }

    /**
     * テーブルをリロードする
     */
    private void reloadTable() {
        this.disposeTableBody();
        boolean first = true;
        Map<BattleAggUnit, BattleAggUnitDto> aggMap = this.load();
        for (Entry<BattleAggUnit, BattleAggUnitDto> entry : aggMap.entrySet()) {
            BattleAggUnitDto dto = entry.getValue();
            BattleAggDetailsDto total = dto.getTotal();

            TreeItem root = new TreeItem(this.tree, SWT.NONE);
            // 合計
            root.setText(new String[] { entry.getKey().toString(), Integer.toString(total.getWin()),
                    Integer.toString(total.getS()), Integer.toString(total.getA()), Integer.toString(total.getB()),
                    Integer.toString(total.getC()), Integer.toString(total.getD()) });
            // ボス
            TreeItem boss = new TreeItem(root, SWT.NONE);
            boss.setText(new String[] { "ボス", Integer.toString(total.getBossWin()),
                    Integer.toString(total.getBossS()), Integer.toString(total.getBossA()),
                    Integer.toString(total.getBossB()), Integer.toString(total.getBossC()),
                    Integer.toString(total.getBossD()) });
            // 海域毎
            for (Entry<String, BattleAggDetailsDto> areaEntry : dto.getAreaDetails()) {
                BattleAggDetailsDto area = areaEntry.getValue();

                TreeItem sub = new TreeItem(root, SWT.NONE);
                sub.setText(new String[] { areaEntry.getKey(), Integer.toString(area.getWin()),
                        Integer.toString(area.getS()), Integer.toString(area.getA()), Integer.toString(area.getB()),
                        Integer.toString(area.getC()), Integer.toString(area.getD()) });
                // ボス
                TreeItem subBoss = new TreeItem(sub, SWT.NONE);
                subBoss.setText(new String[] { "ボス", Integer.toString(area.getBossWin()),
                        Integer.toString(area.getBossS()), Integer.toString(area.getBossA()),
                        Integer.toString(area.getBossB()), Integer.toString(area.getBossC()),
                        Integer.toString(area.getBossD()) });
            }
            if (first)
                root.setExpanded(true);
            first = false;
        }
        this.packTableHeader();
    }

    /**
     * 報告書を読み込み、集計結果を返す
     * @return 集計結果
     */
    private Map<BattleAggUnit, BattleAggUnitDto> load() {
        Map<BattleAggUnit, BattleAggUnitDto> aggMap = new EnumMap<>(BattleAggUnit.class);
        // 日付書式
        SimpleDateFormat format = new SimpleDateFormat(AppConstants.DATE_FORMAT);
        // 今日
        Calendar today = BattleAggDate.TODAY.get();
        // 先週
        Calendar lastWeek = BattleAggDate.LAST_WEEK.get();
        // 先月
        Calendar lastMonth = BattleAggDate.LAST_MONTH.get();

        // 海戦・ドロップ報告書読み込み
        File report = new File(FilenameUtils.concat(AppConfig.get().getReportPath(), AppConstants.LOG_BATTLE_RESULT));
        try {
            LineIterator ite = new LineIterator(
                    new InputStreamReader(new FileInputStream(report), AppConstants.CHARSET));
            try {
                // ヘッダーを読み飛ばす
                if (ite.hasNext()) {
                    ite.next();
                }
                while (ite.hasNext()) {
                    try {
                        String line = ite.next();
                        String[] cols = line.split(",", -1);
                        // 日付
                        Calendar date = DateUtils.toCalendar(format.parse(cols[0]));
                        date.setTimeZone(AppConstants.TIME_ZONE_MISSION);
                        date.setFirstDayOfWeek(Calendar.MONDAY);
                        // 海域
                        String area = cols[1];
                        // ランク
                        String rank = cols[4];
                        // ボス
                        boolean isBoss = "ボス".equals(cols[3]);

                        // デイリー集計
                        this.agg(BattleAggUnit.DAILY, aggMap, today, Calendar.DAY_OF_YEAR, date, area, rank, isBoss);
                        // ウィークリー集計
                        this.agg(BattleAggUnit.WEEKLY, aggMap, today, Calendar.WEEK_OF_YEAR, date, area, rank, isBoss);
                        // マンスリー集計
                        this.agg(BattleAggUnit.MONTHLY, aggMap, today, Calendar.MONTH, date, area, rank, isBoss);
                        // 先週の集計
                        this.agg(BattleAggUnit.LAST_WEEK, aggMap, lastWeek, Calendar.WEEK_OF_YEAR, date, area,
                                rank, isBoss);
                        // 先月の集計
                        this.agg(BattleAggUnit.LAST_MONTH, aggMap, lastMonth, Calendar.MONTH, date, area, rank,
                                isBoss);

                    } catch (Exception e) {
                        continue;
                    }
                }
            } finally {
                ite.close();
            }

        } catch (Exception e) {
        }
        return aggMap;
    }

    /**
     * 集計する
     * 
     * @param unit 集計単位(デイリーなど)
     * @param to 集計結果
     * @param std 基準日
     * @param field {@link Calendar#get(int)}のフィールド値
     * @param target 集計対象の日付
     * @param area 海域
     * @param rank ランク
     * @param isBoss ボス
     */
    private void agg(BattleAggUnit unit, Map<BattleAggUnit, BattleAggUnitDto> to, Calendar std, int field,
            Calendar target, String area, String rank, boolean isBoss) {
        if (std.get(field) == target.get(field)) {
            BattleAggUnitDto aggUnit = to.get(unit);
            if (aggUnit == null) {
                aggUnit = new BattleAggUnitDto();
                to.put(unit, aggUnit);
            }
            aggUnit.add(area, rank, isBoss);
        }
    }

    /**
     * テーブルを再読み込みするリスナーです
     */
    protected class TableReloadAdapter extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            BattleAggDialog.this.reloadTable();
        }
    }
}
