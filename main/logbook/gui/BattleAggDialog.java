package logbook.gui;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TreeMap;

import logbook.config.AppConfig;
import logbook.config.bean.TableConfigBean;
import logbook.dto.BattleAggDetailsDto;
import logbook.dto.BattleAggUnitDto;
import logbook.dto.BattleResultDto;
import logbook.dto.MapCellDto;
import logbook.dto.ResultRank;
import logbook.gui.listener.TreeKeyShortcutAdapter;
import logbook.gui.listener.TreeToClipboardAdapter;
import logbook.gui.logic.GuiUpdator;
import logbook.internal.BattleAggDate;
import logbook.internal.BattleAggUnit;
import logbook.internal.BattleResultServer;
import logbook.internal.LoggerHolder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

/**
 * 出撃統計
 *
 */
public class BattleAggDialog extends WindowBase {
    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(BattleAggDialog.class);

    /** ヘッダー */
    private final String[] header = this.getTableHeader();

    private final Shell parent;

    /** タイマー */
    protected Timer timer;

    /** シェル */
    private Shell shell;

    /** ツリーテーブル */
    private Tree tree;

    /** ツリー要素マネージャ */
    private AggTableItems treeItems;

    /** メニューバー */
    private Menu menubar;

    /** [操作]メニュー */
    private Menu opemenu;

    private Menu tablemenu;

    protected TableConfigBean config;

    protected MenuItem cyclicReloadMenuItem;

    protected boolean needsUpdate = true;

    /**
     * Create the dialog.
     * @param parent
     * @param menuItem
     */
    public BattleAggDialog(Shell parent, MenuItem menuItem) {
        super(menuItem);
        this.parent = parent;
    }

    /**
     * Open the dialog.
     * @return the result
     */
    @Override
    public void open() {
        // 初期化済みの場合
        if (this.isWindowInitialized()) {
            // リロードして表示
            this.reloadTable();
            this.setVisible(true);
            return;
        }

        this.createContents();
        this.setWindowInitialized(true);
        this.setVisible(true);
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        // シェルを作成
        super.createContents(this.parent, SWT.SHELL_TRIM | SWT.MODELESS, true);
        this.shell = this.getShell();
        this.shell.setText(this.getTitle());
        this.shell.setLayout(new FillLayout(SWT.HORIZONTAL));
        // メニューバー
        this.createMenubar();
        this.menubar = this.getMenubar();
        // ツリーテーブル
        this.tree = new Tree(this.shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.VIRTUAL | SWT.MULTI);
        this.tree.addKeyListener(new TreeKeyShortcutAdapter(this.header, this.tree));
        this.tree.setLinesVisible(true);
        this.tree.setHeaderVisible(true);
        this.treeItems = new AggTableItems(this.tree);
        // メニューバーのメニュー
        if (this.isNoMenubar()) {
            this.opemenu = this.menubar;
        }
        else {
            MenuItem operoot = new MenuItem(this.menubar, SWT.CASCADE);
            operoot.setText("操作");
            this.opemenu = new Menu(operoot);
            operoot.setMenu(this.opemenu);
        }
        MenuItem reload = new MenuItem(this.opemenu, SWT.NONE);
        reload.setText("再読み込み(&R)\tF5");
        reload.setAccelerator(SWT.F5);
        reload.addSelectionListener(new TableReloadAdapter());

        // ウィンドウの基本メニューを設定
        super.registerEvents();

        // テーブル右クリックメニュー
        this.tablemenu = this.getPopupMenu();
        this.tree.setMenu(this.tablemenu);
        MenuItem sendclipbord = new MenuItem(this.tablemenu, SWT.NONE);
        sendclipbord.addSelectionListener(new TreeToClipboardAdapter(this.header, this.tree));
        sendclipbord.setText("クリップボードにコピー(&C)");
        if (!this.isNoMenubar()) {
            MenuItem reloadtable = new MenuItem(this.tablemenu, SWT.NONE);
            reloadtable.setText("再読み込み(&R)");
            reloadtable.addSelectionListener(new TableReloadAdapter());
        }

        this.setTableHeader();
        this.reloadTable();

        // データの更新を受け取る
        final Runnable listener = new GuiUpdator(new Runnable() {
            @Override
            public void run() {
                BattleAggDialog.this.reloadTable();
            }
        });
        BattleResultServer.addListener(listener);
        this.shell.addListener(SWT.Dispose, new Listener() {
            @Override
            public void handleEvent(Event event) {
                BattleResultServer.removeListener(listener);
            }
        });
    }

    /**
     * タイトルを返します
     * @return String
     */
    protected String getTitle() {
        return "出撃統計";
    }

    /**
     * ウィンドウサイズを保存・リストアするべきか？
     * @return
     */
    @Override
    protected boolean shouldSaveWindowSize() {
        return true;
    }

    /**
     * ウィンドウのデフォルトサイズを取得
     * @return Point
     */
    @Override
    protected Point getDefaultSize() {
        return new Point(600, 350);
    }

    protected TableConfigBean getConfig() {
        if (this.config == null) {
            this.config = AppConfig.get().getTableConfigMap().get(this.getWindowId());
            if (this.config == null) {
                this.config = new TableConfigBean();
            }
        }
        return this.config;
    }

    @Override
    public void save() {
        if (this.config != null) {
            this.config.setCyclicReload(this.cyclicReloadMenuItem.getSelection());
            AppConfig.get().getTableConfigMap().put(this.getWindowId(), this.config);
        }
        super.save();
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
        return new String[] { "集計", "出撃合計", "勝利合計", "S勝利", "A勝利", "B勝利", "C敗北", "D以下" };
    }

    /**
     * テーブルをリロードする
     */
    private void reloadTable() {
        this.tree.setRedraw(false);
        TableItemReference[] selectedItems = this.treeItems.getSelection();
        TableItemReference topItem = this.treeItems.getTopItem();
        this.treeItems.disposeTree();
        this.treeItems.setData(this.load());
        this.treeItems.setSelectedItems(selectedItems);
        this.treeItems.setTopItem(topItem);
        this.packTableHeader();
        this.tree.setRedraw(true);
        this.needsUpdate = false;
    }

    /**
     * 報告書を読み込み、集計結果を返す
     * @return 集計結果
     */
    private Map<BattleAggUnit, BattleAggUnitDto> load() {
        Map<BattleAggUnit, BattleAggUnitDto> aggMap = new EnumMap<>(BattleAggUnit.class);
        // 今日
        Calendar today = BattleAggDate.TODAY.get();
        // 先週
        Calendar lastWeek = BattleAggDate.LAST_WEEK.get();
        // 先月
        Calendar lastMonth = BattleAggDate.LAST_MONTH.get();
        // 読み込む最小の日付(>=)
        Calendar min = lastMonth;

        // 海戦・ドロップ報告書読み込み
        try {
            for (BattleResultDto dto : BattleResultServer.get().getList()) {
                Calendar date = BattleAggDate.fromDate(dto.getBattleDate());
                MapCellDto mapCell = dto.getMapCell();
                ResultRank rank = dto.getRank();
                // 演習はスキップ
                if (mapCell == null)
                    continue;

                // 読み込む最小の日付未満の場合は読み飛ばす
                if (min.compareTo(date) > 0) {
                    continue;
                }

                // デイリー集計
                this.agg(BattleAggUnit.DAILY, aggMap, today, Calendar.DAY_OF_YEAR, date, mapCell, rank);
                // ウィークリー集計
                this.agg(BattleAggUnit.WEEKLY, aggMap, today, Calendar.WEEK_OF_YEAR, date, mapCell, rank);
                // マンスリー集計
                this.agg(BattleAggUnit.MONTHLY, aggMap, today, Calendar.MONTH, date, mapCell, rank);
                // 先週の集計
                this.agg(BattleAggUnit.LAST_WEEK, aggMap, lastWeek, Calendar.WEEK_OF_YEAR, date, mapCell,
                        rank);
                // 先月の集計
                this.agg(BattleAggUnit.LAST_MONTH, aggMap, lastMonth, Calendar.MONTH, date, mapCell, rank);
            }

        } catch (Exception e) {
            ApplicationMain.main.printMessage("出撃統計作成に失敗しました");
            LOG.get().warn("出撃統計作成に失敗", e);
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
     * @param isStart 出撃
     * @param isBoss ボス
     */
    private void agg(BattleAggUnit unit, Map<BattleAggUnit, BattleAggUnitDto> to, Calendar std, int field,
            Calendar target, MapCellDto area, ResultRank rank) {
        int stdn = std.get(field);
        int tarn = target.get(field);
        //if (std.get(field) == target.get(field))
        if (stdn == tarn)
        {
            BattleAggUnitDto aggUnit = to.get(unit);
            if (aggUnit == null) {
                aggUnit = new BattleAggUnitDto();
                to.put(unit, aggUnit);
            }
            aggUnit.add(area, rank);
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

    /**
     * TreeItemはリロードすると消えてしまうので消えない対応データを持っておく
     */
    protected class TableItemReference {
        AggTableItem aggItem;
        boolean boss;

        public TableItemReference(AggTableItem aggItem, boolean boss) {
            this.aggItem = aggItem;
            this.boss = boss;
        }
    }

    protected class AggTableItems {
        private final Map<BattleAggUnit, AggTableItem> childs = new TreeMap<>();
        private final Tree root;

        public AggTableItems(Tree root) {
            this.root = root;
        }

        public void disposeTree() {
            // expand状態を記憶+TreeItemへの参照を削除
            for (AggTableItem item : this.childs.values()) {
                item.storeAndReleaseItem();
            }
            // テーブルをクリア
            this.root.removeAll();
        }

        public void setData(Map<BattleAggUnit, BattleAggUnitDto> aggMap) {
            for (Entry<BattleAggUnit, BattleAggUnitDto> entry : aggMap.entrySet()) {
                AggTableItem aggItem = this.getChild(entry.getKey());
                aggItem.setDto(this.root, entry.getKey().toString(), entry.getValue());
            }
        }

        public TableItemReference[] getSelection() {
            List<TableItemReference> list = new ArrayList<>();
            for (TreeItem item : this.root.getSelection()) {
                AggTableItem aggItem = (AggTableItem) item.getData();
                list.add(aggItem.getItemReference(item));
            }
            return list.toArray(new TableItemReference[list.size()]);
        }

        public void setSelectedItems(TableItemReference[] items) {
            List<TreeItem> list = new ArrayList<>();
            for (TableItemReference item : items) {
                TreeItem treeItem = item.aggItem.getTreeItem(item.boss);
                if (treeItem != null) {
                    list.add(treeItem);
                }
            }
            this.root.setSelection(list.toArray(new TreeItem[list.size()]));
        }

        public TableItemReference getTopItem() {
            TreeItem topItem = this.root.getTopItem();
            if (topItem == null)
                return null;
            AggTableItem aggItem = (AggTableItem) topItem.getData();
            return aggItem.getItemReference(topItem);
        }

        public void setTopItem(TableItemReference item) {
            if (item == null)
                return;
            TreeItem treeItem = item.aggItem.getTreeItem(item.boss);
            if (treeItem != null) {
                this.root.setTopItem(treeItem);
            }
        }

        private AggTableItem getChild(BattleAggUnit unit) {
            if (this.childs.containsKey(unit)) {
                return this.childs.get(unit);
            }
            AggTableItem child = new AggTableItem();
            this.childs.put(unit, child);
            return child;
        }
    }

    protected class AggTableItem {
        private final Map<Integer, AggTableItem> childs = new TreeMap<>();
        private TreeItem item;
        private TreeItem bossItem;
        private boolean expanded;

        public void setDto(Tree root, String key, BattleAggUnitDto dto) {
            this.item = new TreeItem(root, SWT.NONE);
            this.setDto(key, dto.getTotal());
            for (Entry<Integer, BattleAggDetailsDto> areaEntry : dto.getAreaDetails()) {
                AggTableItem aggItem = this.getChild(areaEntry.getKey());
                aggItem.setDto(this, areaEntry.getValue());
            }
        }

        private void setDto(AggTableItem parent, BattleAggDetailsDto area) {
            this.item = new TreeItem(parent.item, SWT.NONE);
            this.setDto(area.getAreaName(), area);
        }

        private void setDto(String title, BattleAggDetailsDto area) {
            // メイン
            this.item.setText(new String[] { title, Integer.toString(area.getStart()), Integer.toString(area.getWin()),
                    Integer.toString(area.getS()), Integer.toString(area.getA()), Integer.toString(area.getB()),
                    Integer.toString(area.getC()), Integer.toString(area.getD()) });
            this.item.setData(this);
            // ボス
            this.bossItem = new TreeItem(this.item, SWT.NONE);
            this.bossItem.setText(new String[] { "ボス", "-", Integer.toString(area.getBossWin()),
                    Integer.toString(area.getBossS()), Integer.toString(area.getBossA()),
                    Integer.toString(area.getBossB()), Integer.toString(area.getBossC()),
                    Integer.toString(area.getBossD()) });
            this.bossItem.setData(this);

            if (this.expanded) {
                this.item.setExpanded(true);
            }
        }

        private AggTableItem getChild(Integer areaId) {
            if (this.childs.containsKey(areaId)) {
                return this.childs.get(areaId);
            }
            AggTableItem child = new AggTableItem();
            this.childs.put(areaId, child);
            return child;
        }

        public TableItemReference getItemReference(TreeItem item) {
            if (this.item == item) {
                return new TableItemReference(this, false);
            }
            else if (this.bossItem == item) {
                return new TableItemReference(this, true);
            }
            return null;
        }

        public TreeItem getTreeItem(boolean boss) {
            return boss ? this.bossItem : this.item;
        }

        public void storeAndReleaseItem() {
            for (AggTableItem item : this.childs.values()) {
                item.storeAndReleaseItem();
            }
            if (this.item != null) {
                this.expanded = this.item.getExpanded();
                this.item = this.bossItem = null;
            }
        }
    }
}