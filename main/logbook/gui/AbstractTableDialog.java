package logbook.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import logbook.config.AppConfig;
import logbook.config.bean.TableConfigBean;
import logbook.config.bean.TableConfigBean.Column;
import logbook.config.bean.TableConfigBean.SortKey;
import logbook.data.Data;
import logbook.data.DataType;
import logbook.data.EventListener;
import logbook.data.context.GlobalContext;
import logbook.gui.listener.TableKeyShortcutAdapter;
import logbook.gui.listener.TableToClipboardAdapter;
import logbook.gui.listener.TableToCsvSaveAdapter;
import logbook.gui.logic.TableItemCreator;
import logbook.gui.logic.TableRowHeader;
import logbook.internal.LoggerHolder;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * テーブルで構成されるダイアログの基底クラス
 *
 */
public abstract class AbstractTableDialog extends WindowBase implements EventListener {
    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(AbstractTableDialog.class);

    private static int MAX_PRINT_ITEMS = 2000;

    private final Shell parent;

    /** タイマー */
    protected Timer timer;

    /** ヘッダー */
    protected String[] header;

    protected String[] headerId;

    /** テーブルに表示しているボディー */
    protected List<Comparable[]> body;

    /** ソート順序 */
    protected boolean[] orderflgs;

    /** シェル */
    protected Shell shell;

    /** メニューバー */
    protected Menu menubar;

    /** [ファイル]メニュー */
    protected Menu filemenu;

    /** [操作]メニュー */
    protected Menu opemenu;

    /** テーブル */
    protected Table table;

    /** テーブルのメニュー */
    protected Menu tablemenu;

    /** ヘッダーのメニュー */
    protected Menu headermenu;

    /** テーブルソート */
    protected final TableComparator comparator = new TableComparator();

    protected TableConfigBean config;

    protected MenuItem cyclicReloadMenuItem;

    private Display display;

    protected boolean needsUpdate = true;

    /**
     * コンストラクター
     */
    public AbstractTableDialog(Shell parent, MenuItem menuItem) {
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
            // リロードして表示
            this.reloadTable();
            this.setVisible(true);
            return;
        }

        // シェルを作成
        super.createContents(this.parent, SWT.SHELL_TRIM | SWT.MODELESS, true);
        this.shell = this.getShell();
        this.shell.setLayout(new FillLayout());
        // メニューバー
        this.createMenubar();
        this.menubar = this.getMenubar();
        // テーブルより前に作成する必要があるコンポジットを作成
        this.createContentsBefore();
        // ヘッダ
        String[] headerWithId = this.getTableHeader();
        this.header = new String[headerWithId.length];
        this.headerId = new String[headerWithId.length];
        for (int i = 0; i < headerWithId.length; ++i) {
            String[] splitted = headerWithId[i].split("#");
            this.header[i] = splitted[0];
            this.headerId[i] = splitted[(splitted.length == 1) ? 0 : 1];
        }
        this.orderflgs = new boolean[this.header.length];
        // テーブル
        this.table = new Table(this.getTableParent(), SWT.FULL_SELECTION | SWT.MULTI);
        this.table.addKeyListener(new TableKeyShortcutAdapter(this.header, this.table));
        this.table.setLinesVisible(true);
        this.table.setHeaderVisible(true);
        this.table.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                AbstractTableDialog.this.shell.setText(AbstractTableDialog.this.getTitle());
            }
        });
        // メニューバーのメニュー
        if (this.isNoMenubar()) {
            this.filemenu = this.menubar;
        }
        else {
            MenuItem fileroot = new MenuItem(this.menubar, SWT.CASCADE);
            fileroot.setText("ファイル");
            this.filemenu = new Menu(fileroot);
            fileroot.setMenu(this.filemenu);
        }

        MenuItem savecsv = new MenuItem(this.filemenu, SWT.NONE);
        savecsv.setText("CSVファイルに保存(&S)\tCtrl+S");
        savecsv.setAccelerator(SWT.CTRL + 'S');
        savecsv.addSelectionListener(new TableToCsvSaveAdapter(this.shell, this.getTitle(), this.getTableHeader(),
                this.table));

        if (this.isNoMenubar()) {
            this.opemenu = this.menubar;
        }
        else {
            MenuItem operoot = new MenuItem(this.menubar, SWT.CASCADE);
            operoot.setText("操作");
            this.opemenu = new Menu(operoot);
            operoot.setMenu(this.opemenu);
        }

        if (this.opemenu.getItemCount() > 0) {
            new MenuItem(this.opemenu, SWT.SEPARATOR);
        }

        MenuItem reload = new MenuItem(this.opemenu, SWT.NONE);
        reload.setText("再読み込み(&R)\tF5");
        reload.setAccelerator(SWT.F5);
        reload.addSelectionListener(new TableReloadAdapter());

        this.cyclicReloadMenuItem = new MenuItem(this.opemenu, SWT.CHECK);
        this.cyclicReloadMenuItem.setText("定期的に再読み込み(1秒)(&A)\tCtrl+F5");
        this.cyclicReloadMenuItem.setAccelerator(SWT.CTRL + SWT.F5);
        this.cyclicReloadMenuItem.addSelectionListener(new CyclicReloadAdapter(this.cyclicReloadMenuItem));

        MenuItem selectVisible = new MenuItem(this.opemenu, SWT.NONE);
        selectVisible.setText("列の表示・非表示(&V)");
        selectVisible.addSelectionListener(new SelectVisibleColumnAdapter());

        MenuItem resetOrder = new MenuItem(this.opemenu, SWT.NONE);
        resetOrder.setText("列の順番をリセット");
        resetOrder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox box = new MessageBox(AbstractTableDialog.this.getShell(), SWT.YES | SWT.NO
                        | SWT.ICON_QUESTION);
                box.setText("列の順番をリセット");
                box.setMessage("列の順番を初期表示に戻します。よろしいですか？");
                if (box.open() == SWT.YES) {
                    AbstractTableDialog.this.resetColumnOrder();
                }
            }
        });

        MenuItem resetWidth = new MenuItem(this.opemenu, SWT.NONE);
        resetWidth.setText("列幅を自動調整");
        resetWidth.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox box = new MessageBox(AbstractTableDialog.this.getShell(), SWT.YES | SWT.NO
                        | SWT.ICON_QUESTION);
                box.setText("列幅を自動調整");
                box.setMessage("表示されているすべての列の幅が現在の表示データを使って自動調整されます。よろしいですか？");
                if (box.open() == SWT.YES) {
                    AbstractTableDialog.this.table.setRedraw(false);
                    // ソートマーカーを取ってから列幅を調整する
                    AbstractTableDialog.this.table.setSortColumn(null);
                    AbstractTableDialog.this.restoreColumnWidth(true);
                    AbstractTableDialog.this.setSortDirectionToHeader();
                    AbstractTableDialog.this.table.setRedraw(true);
                }
            }
        });

        // ウィンドウの基本メニューを設定
        super.registerEvents();

        // テーブル右クリックメニュー
        this.tablemenu = this.getPopupMenu();
        this.table.setMenu(this.tablemenu);
        if (this.tablemenu.getItemCount() > 0) {
            new MenuItem(this.tablemenu, SWT.SEPARATOR);
        }
        MenuItem sendclipbord = new MenuItem(this.tablemenu, SWT.NONE);
        sendclipbord.addSelectionListener(new TableToClipboardAdapter(this.header, this.table));
        sendclipbord.setText("クリップボードにコピー(&C)");

        if (!this.isNoMenubar()) {
            MenuItem reloadtable = new MenuItem(this.tablemenu, SWT.NONE);
            reloadtable.setText("再読み込み(&R)");
            reloadtable.addSelectionListener(new TableReloadAdapter());
        }

        // テーブルにヘッダーをセット
        this.setTableHeader();
        try {
            // テーブルに内容をセット
            this.updateTableBody();
        } catch (Exception e) {
            // データの読み取りでエラーが発生するかもしれないので落ちないようにしておく
            this.body = new ArrayList<>();
            LOG.get().warn("テーブルの内容生成でエラー", e);
        }
        this.sortBody();
        this.setTableBody();
        // 列幅を復元
        this.restoreColumnWidth(false);
        this.setSortDirectionToHeader();

        // ヘッダの右クリックメニュー
        this.headermenu = new Menu(this.table);
        for (int i = 0; i < this.header.length; ++i) {
            final MenuItem item = new MenuItem(this.headermenu, SWT.CHECK);
            final int column_index = i;
            item.setText(this.header[i]);
            item.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    boolean selected = item.getSelection();
                    boolean[] visibles = AbstractTableDialog.this.getConfig().getVisibleColumn();
                    visibles[column_index] = selected;
                    AbstractTableDialog.this.setColumnVisible(column_index, selected);
                }
            });
        }
        this.headermenu.addMenuListener(new MenuAdapter() {
            @Override
            public void menuShown(MenuEvent e) {
                // 開くときに設定を反映させておく
                MenuItem[] items = AbstractTableDialog.this.headermenu.getItems();
                boolean[] visibles = AbstractTableDialog.this.getConfig().getVisibleColumn();
                for (int i = 0; i < items.length; ++i) {
                    items[i].setSelection(visibles[i]);
                }
            }
        });

        // クリック位置によってメニュー切り替え
        this.table.addListener(SWT.MenuDetect, new Listener() {
            @Override
            public void handleEvent(Event event) {
                Point pt = AbstractTableDialog.this.display.map(null, AbstractTableDialog.this.table, new Point(
                        event.x, event.y));
                Rectangle clientArea = AbstractTableDialog.this.table.getClientArea();
                boolean header = (clientArea.y <= pt.y)
                        && (pt.y < (clientArea.y + AbstractTableDialog.this.table.getHeaderHeight()));
                AbstractTableDialog.this.table.setMenu(
                        header ? AbstractTableDialog.this.headermenu : AbstractTableDialog.this.tablemenu);
            }
        });

        this.table.addListener(SWT.Dispose, new Listener() {
            @Override
            public void handleEvent(Event event) {
                // setMenuされたメニューしかdisposeされないので
                AbstractTableDialog.this.headermenu.dispose();
                AbstractTableDialog.this.tablemenu.dispose();
                // タイマーの終了
                AbstractTableDialog.this.disableCyclicReload();
                // GlobalContextへのリスナ登録解除
                GlobalContext.removeEventListener(AbstractTableDialog.this);
            }
        });

        // 更新リスナ登録
        GlobalContext.addEventListener(this);

        // orderflgsを初期化
        TableConfigBean.SortKey[] sortKeys = this.getConfig().getSortKeys();
        if (sortKeys != null) {
            for (TableConfigBean.SortKey key : sortKeys) {
                if (key != null) {
                    this.orderflgs[key.index] = key.order;
                }
            }
        }

        // 自動更新設定を反映
        if (this.getConfig().isCyclicReload()) {
            this.cyclicReloadMenuItem.setSelection(true);
            this.enableCyclicReload();
        }

        this.display = this.shell.getDisplay();
        this.createContents();
        this.shell.setText(this.getTitle());
        this.setWindowInitialized(true);
        this.setVisible(true);
    }

    /**
     * テーブルをリロードする
     */
    protected void reloadTable() {
        //ApplicationMain.timeLogPrint("[S] reloadTable");
        this.table.setRedraw(false);
        int topindex = this.table.getTopIndex();
        int[] selection = this.table.getSelectionIndices();
        this.table.setSortColumn(null);
        this.disposeTableBody();
        //ApplicationMain.timeLogPrint("[S] updateTableBody");
        try {
            // テーブルに内容をセット
            this.updateTableBody();
        } catch (Exception e) {
            // データの読み取りでエラーが発生するかもしれないので落ちないようにしておく
            this.body = new ArrayList<>();
            LOG.get().warn("テーブルの内容生成でエラー", e);
        }
        //ApplicationMain.timeLogPrint("[E] updateTableBody");
        this.sortBody();
        //ApplicationMain.timeLogPrint("[S] setTableBody");
        this.setTableBody();
        //ApplicationMain.timeLogPrint("[E] setTableBody");
        this.setSortDirectionToHeader();
        this.table.setSelection(selection);
        this.table.setTopIndex(topindex);
        this.getShell().setText(this.getTitle());
        this.table.setRedraw(true);
        this.table.setTopIndex(topindex);
        //ApplicationMain.timeLogPrint("[E] reloadTable");
        this.needsUpdate = false;
    }

    /**
     * テーブルヘッダーをセットする
     */
    private void setTableHeader() {
        this.getConfig();
        SelectionListener listener = this.getHeaderSelectionListener();
        for (int i = 0; i < this.header.length; i++) {
            TableColumn col = new TableColumn(this.table, SWT.LEFT);
            col.setText(this.header[i]);
            col.setMoveable(true);
            col.addSelectionListener(listener);
            if (i == 0) {
                // No.列はソートしない
                col.setData("unsortable", new Object());
            }
        }
        if (this.config.getColumnOrder() != null) {
            this.table.setColumnOrder(this.config.getColumnOrder());
        }
    }

    /**
     * テーブルボディーをセットする
     */
    protected void setTableBody() {
        TableItemCreator creator = this.getTableItemCreator();
        creator.begin(this.getTableHeader());
        // 表示最大件数を制限する
        int numPrintItems = Math.min(MAX_PRINT_ITEMS, this.body.size());
        for (int i = 0; i < numPrintItems; i++) {
            Comparable[] line = this.body.get(i);
            ((TableRowHeader) line[0]).setNumber(i + 1); // ソート順に関係ない番号
            creator.create(this.table, line, i);
        }
        creator.end();
    }

    /**
     * テーブルボディーをクリアする
     */
    protected void disposeTableBody() {
        this.table.removeAll();
        /*
        TableItem[] items = this.table.getItems();
        for (int i = 0; i < items.length; i++) {
            items[i].dispose();
        }
        */
    }

    /**
     * テーブルヘッダーの幅を復元する
     */
    protected void restoreColumnWidth(boolean resetAll) {
        boolean[] visibles = this.getConfig().getVisibleColumn();
        int[] widths = this.getConfig().getColumnWidth();

        TableColumn[] columns = this.table.getColumns();

        for (int i = 0; i < columns.length; i++) {
            if (visibles[i]) {
                if ((widths[i] < 5) || resetAll) {
                    columns[i].pack();
                }
                else {
                    columns[i].setWidth(widths[i]);
                }
            } else {
                columns[i].setWidth(0);
            }
        }
    }

    /** １列だけ操作する */
    protected void setColumnVisible(int index, boolean visible) {
        TableColumn[] columns = this.table.getColumns();
        if (visible) {
            columns[index].pack();
        }
        else {
            columns[index].setWidth(0);
        }
    }

    /** まとめて変更する */
    public void setColumnVisible(boolean[] visibles) {
        this.table.setRedraw(false);
        boolean[] old = this.getConfig().getVisibleColumn();
        for (int i = 0; i < old.length; i++) {
            if (old[i] != visibles[i]) {
                old[i] = visibles[i];
                this.setColumnVisible(i, visibles[i]);
            }
        }
        this.table.setRedraw(true);
    }

    protected int[] defaultColumnOrder() {
        int[] columnOrder = new int[this.header.length];
        for (int i = 0; i < this.header.length; ++i) {
            columnOrder[i] = i;
        }
        return columnOrder;
    }

    private void resetColumnOrder() {
        int[] columnOrder = this.defaultColumnOrder();
        if (this.config != null) {
            this.config.setColumnOrder(columnOrder);
        }
        this.table.setColumnOrder(columnOrder);
    }

    private static void renumberColumnPosision(Column[] columns) {
        Arrays.sort(columns, comparePosition);
        int next = 0;
        for (Column col : columns) {
            col.pos = next++;
        }
    }

    private static Comparator<Column> comparePosition = new Comparator<Column>() {
        @Override
        public int compare(Column arg0, Column arg1) {
            return Integer.compare(arg0.pos, arg1.pos);
        }
    };

    private void updateConfig() {

        Map<String, Column> columns = this.config.getColumns();
        String[] oldIds = this.config.getHeaderNames();
        boolean[] oldVisibles = this.config.getVisibleColumn();
        int[] oldWidth = this.config.getColumnWidth();
        int[] oldOrder = this.config.getColumnOrder();

        int oldLength = oldVisibles.length;

        // 各カラムの位置
        int[] oldPos = new int[oldLength];
        for (int i = 0; i < oldLength; ++i) {
            if (oldOrder[i] < oldLength) {
                oldPos[oldOrder[i]] = i;
            }
        }

        // 互換性維持
        if (oldWidth == null) {
            oldWidth = new int[oldLength];
        }
        if (oldIds == null) {
            // ヘッダー情報がない場合は今のヘッダーから作る
            oldLength = Math.min(oldLength, this.header.length);
            oldIds = ArrayUtils.subarray(this.headerId, 0, oldLength);
        }

        // pos順にする
        Column[] oldColumns = new Column[oldLength];
        for (int i = 0; i < oldLength; ++i) {
            oldColumns[i] = new Column(oldIds[i], oldVisibles[i], oldWidth[i], oldPos[i]);
        }
        Arrays.sort(oldColumns, comparePosition);

        // columnsデータに追加
        int next = 0;
        for (Column col : oldColumns) {
            Column colm = columns.get(col.id);
            if (colm != null) {
                // 順番を維持するためposをすりあわせる
                next = Math.max(next, colm.pos);
            }
            col.pos = next++;
            columns.put(col.id, col);
        }

        // columnsデータのpos番号を整理
        renumberColumnPosision(columns.values().toArray(new Column[0]));

        // 設定情報を引き継いだデータを作成
        Column[] newColumns = new Column[this.header.length];
        int nextNew = columns.size();
        for (int i = 0; i < this.header.length; ++i) {
            String id = this.headerId[i];
            Column colm = columns.get(id);
            if (colm != null) {
                newColumns[i] = colm.clone();
            }
            else {
                newColumns[i] = new Column(id, true, 0, nextNew++);
            }
        }

        // pos番号を整理
        renumberColumnPosision(newColumns.clone());

        // 完成したのでデータを戻す
        boolean[] visibles = new boolean[this.header.length];
        int[] columnWidth = new int[this.header.length];
        int[] columnOrder = new int[this.header.length];
        for (int i = 0; i < this.header.length; ++i) {
            visibles[i] = newColumns[i].visible;
            columnWidth[i] = newColumns[i].width;
            columnOrder[newColumns[i].pos] = i;
        }

        this.config.setColumns(columns);
        this.config.setHeaderNames(this.headerId);
        this.config.setVisibleColumn(visibles);
        this.config.setColumnWidth(columnWidth);
        this.config.setColumnOrder(columnOrder);

        // sortOrderをチェック
        SortKey[] sortKeys = this.config.getSortKeys();
        for (int i = 0; i < sortKeys.length; ++i) {
            if (sortKeys[i] != null) {
                if (sortKeys[i].index >= this.header.length) {
                    // 超えてる
                    sortKeys[i] = null;
                }
            }
        }
        this.config.setSortKeys(sortKeys);
    }

    protected TableConfigBean getConfig() {
        if (this.config == null) {
            this.config = AppConfig.get().getTableConfigMap().get(this.getWindowId());

            if (this.config != null) {
                if ((this.config.getVisibleColumn() == null) ||
                        (this.config.getColumnOrder() == null)) {
                    // これがないとお話にならないので
                    this.config = null;
                }
                else {
                    // 古い設定を新しいバージョンに対応させる
                    this.updateConfig();
                }
            }
            if (this.config == null) {
                this.config = this.getDefaultTableConfig();
            }
        }
        return this.config;
    }

    protected final String getTitle() {
        String title = this.getTitleMain();
        if ((this.body != null) && (this.table != null)) {
            if (this.table.getItemCount() != this.body.size()) {
                title += " " + this.body.size() + "件中" + this.table.getItemCount() + "件のみ表示";
            }
            int selectionCount = this.table.getSelectionCount();
            if (selectionCount > 1) {
                title += " " + selectionCount + "件選択中";
            }
        }
        return title;
    }

    @Override
    public void save() {
        if (this.config != null) {
            // 列の順番
            this.config.setColumnOrder(this.table.getColumnOrder());
            // 列のサイズ
            int[] widths = this.config.getColumnWidth();
            TableColumn[] columns = this.table.getColumns();
            for (int i = 0; i < columns.length; i++) {
                int cur = columns[i].getWidth();
                if (cur >= 5) {
                    widths[i] = cur;
                }
            }
            this.config.setColumnWidth(widths);
            this.config.setCyclicReload(this.cyclicReloadMenuItem.getSelection());
            // 将来の互換性維持のため
            this.config.setHeaderNames(this.headerId);

            AppConfig.get().getTableConfigMap().put(this.getWindowId(), this.config);
        }
        super.save();
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
     * @return
     */
    @Override
    protected Point getDefaultSize() {
        return this.getSize();
    }

    /**
     * テーブルの親コンポジット
     * @return テーブルの親コンポジットを取得します
     */
    protected Composite getTableParent() {
        return this.shell;
    }

    /**
     * Create contents of the dialog.
     */
    protected void createContentsBefore() {
    }

    /**
     * Create contents of the dialog.
     */
    protected abstract void createContents();

    /**
     * タイトル本文部分を返します
     * @return String
     */
    protected abstract String getTitleMain();

    /**
     * ウインドウサイズを返します
     * @return Point
     */
    protected abstract Point getSize();

    /**
     * テーブルヘッダーを返します
     * @return String[]
     */
    protected abstract String[] getTableHeader();

    /**
     * テーブルボディーをアップデートします
     */
    protected abstract void updateTableBody();

    /**
     * テーブル行を作成するクリエイターを返します
     * 
     * @return TableItemCreator
     */
    protected abstract TableItemCreator getTableItemCreator();

    /**
     * テーブルヘッダーの{@link org.eclipse.swt.events.SelectionListener}です
     * @return SelectionListener
     */
    protected SelectionListener getHeaderSelectionListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.getSource() instanceof TableColumn) {
                    TableColumn col = (TableColumn) e.getSource();
                    if (col.getData("unsortable") == null) {
                        AbstractTableDialog.this.sortTableItems(col);
                    }
                }
            }
        };
    }

    /**
     * テーブルの初期状態
     * @return
     */
    protected TableConfigBean getDefaultTableConfig() {
        TableConfigBean config = new TableConfigBean();
        boolean[] visibles = new boolean[this.header.length];
        Arrays.fill(visibles, true);
        config.setVisibleColumn(visibles);
        int[] columnWidth = new int[this.header.length];
        int[] columnOrder = new int[this.header.length];
        for (int i = 0; i < this.header.length; ++i) {
            columnOrder[i] = i;
        }
        config.setColumnWidth(columnWidth);
        config.setColumnOrder(columnOrder);
        TableConfigBean.SortKey[] sortKeys = new TableConfigBean.SortKey[3];
        config.setSortKeys(sortKeys);
        return config;
    }

    /**
     * テーブルをソートします
     * 
     * @param headerColumn ソートするカラム
     */
    protected void sortTableItems(TableColumn headerColumn) {
        int index = 0;
        for (int i = 0; i < this.header.length; i++) {
            if (this.header[i].equals(headerColumn.getText())) {
                index = i;
                break;
            }
        }
        this.sortTableItems(index, headerColumn);
    }

    /**
     * テーブルをソートします
     * 
     * @param index カラムインデックス
     * @param headerColumn ソートするカラム
     */
    protected void sortTableItems(int index, TableColumn headerColumn) {
        final boolean orderflg = !this.orderflgs[index];

        // ソートキーに追加
        TableConfigBean config = this.getConfig();
        TableConfigBean.SortKey[] sortKeys = config.getSortKeys();
        TableConfigBean.SortKey[] newKeys = new TableConfigBean.SortKey[3];
        newKeys[0] = new TableConfigBean.SortKey(index, orderflg);
        for (int i = 1, j = 0; (i < newKeys.length) && (j < sortKeys.length); ++j) {
            // キーは重複させない
            if ((sortKeys[j] != null) && (newKeys[0].index != sortKeys[j].index)) {
                newKeys[i++] = sortKeys[j];
            }
        }
        config.setSortKeys(newKeys);

        //this.shell.setRedraw(false); // shellだと速くならない
        this.table.setRedraw(false);
        this.disposeTableBody();

        for (int i = 0; i < this.orderflgs.length; i++) {
            this.orderflgs[i] = false;
        }
        this.orderflgs[index] = orderflg;

        this.setSortDirectionToHeader();
        this.sortBody();
        this.setTableBody();

        //this.shell.setRedraw(true);
        this.table.setRedraw(true);
    }

    private void setSortDirectionToHeader() {
        TableConfigBean.SortKey[] sortKeys = this.getConfig().getSortKeys();
        if ((sortKeys != null) && (sortKeys[0] != null)) {
            TableColumn headerColumn = this.table.getColumn(sortKeys[0].index);
            boolean orderflg = sortKeys[0].order;
            if (orderflg) {
                this.table.setSortColumn(headerColumn);
                this.table.setSortDirection(SWT.UP);
            } else {
                this.table.setSortColumn(headerColumn);
                this.table.setSortDirection(SWT.DOWN);
            }
        }
    }

    private void sortBody() {
        TableConfigBean.SortKey[] sortKeys = this.getConfig().getSortKeys();
        if (sortKeys != null) {
            for (int i = sortKeys.length - 1; i >= 0; --i) {
                if (sortKeys[i] != null) {
                    this.comparator.setIndex(sortKeys[i].index);
                    this.comparator.setOrder(sortKeys[i].order);
                    try {
                        Collections.sort(this.body, this.comparator);
                    } catch (ClassCastException e) {
                        MessageBox box = new MessageBox(this.shell, SWT.OK | SWT.ICON_ERROR);
                        box.setText("テーブルレコードをソート中にエラー");
                        box.setMessage(this.getTitleMain() + "のレコードをソート中に型変換エラーが発生しました\n" +
                                "外部スクリプトの返したデータに問題があるようです\n" +
                                "最近インストールしたスクリプトがある場合は取り除くと解決されるかもしれません\n" +
                                e.getMessage());
                        box.open();
                        return;
                    }
                }
            }
        }
    }

    private void enableCyclicReload() {
        // タイマーを作成
        if (this.timer == null) {
            this.timer = new Timer(true);
            // 1秒毎に再読み込みするようにスケジュールする
            this.timer.schedule(new CyclicReloadTask(AbstractTableDialog.this), 0,
                    TimeUnit.SECONDS.toMillis(1));
        }
    }

    private void disableCyclicReload() {
        // タイマーを終了
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
    }

    /**
     * データ受信
     * デフォルト動作はどんなデータでも更新をONにする
     */
    @Override
    public void update(DataType type, Data data) {
        this.needsUpdate = true;
    }

    /**
     * 次の更新タイミングでテーブルを更新します
     */
    public void update() {
        this.needsUpdate = true;
    }

    /**
     * テーブルをソートする{@link java.util.Comparator}です。
     */
    protected class TableComparator implements Comparator<Comparable[]> {

        /** ソート設定済みフラグ */
        private boolean confflg;
        /** 列位置 */
        private int index;
        /** 昇順・降順フラグ */
        private boolean order;

        @Override
        public final int compare(Comparable[] o1, Comparable[] o2) {
            Comparable o1c = o1[this.index];
            Comparable o2c = o2[this.index];
            if (o1c == null) {
                if (o2c == null) {
                    return 0;
                }
                else {
                    return 1;
                }
            }
            else if (o2c == null) {
                return -1;
            }
            int ret = o1[this.index].compareTo(o2[this.index]);
            return this.order ? ret : -ret;
        }

        /**
         * 列位置をセットする
         * @param index
         */
        public final void setIndex(int index) {
            this.index = index;
            this.confflg = true;
        }

        /**
         * 昇順・降順フラグをセットする
         * @param order
         */
        public final void setOrder(boolean order) {
            this.order = order;
            this.confflg = true;
        }

        /**
         * ソート設定済みフラグ
         * @return
         */
        public final boolean getHasSetConfig() {
            return this.confflg;
        }

        /**
         * 比較する
         * 
         * @param o1
         * @param o2
         * @param order
         * @return
         */
        private <T extends Comparable<? super T>> int compareTo(T o1, T o2, boolean order) {
            if (this.order) {
                return o1.compareTo(o2);
            } else {
                return o2.compareTo(o1);
            }
        }
    }

    /**
     * テーブルを再読み込みするリスナーです
     */
    protected class TableReloadAdapter extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            AbstractTableDialog.this.reloadTable();
        }
    }

    /**
     * テーブルの列を表示・非表示選択するダイアログを表示する
     */
    protected class SelectVisibleColumnAdapter extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            new SelectVisibleColumnDialog(AbstractTableDialog.this).open();
        }
    }

    /**
     * テーブルを定期的に再読み込みする
     */
    protected class CyclicReloadAdapter extends SelectionAdapter {

        private final MenuItem menuitem;

        public CyclicReloadAdapter(MenuItem menuitem) {
            this.menuitem = menuitem;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (this.menuitem.getSelection()) {
                AbstractTableDialog.this.enableCyclicReload();
            } else {
                AbstractTableDialog.this.disableCyclicReload();
            }
        }
    }

    /**
     * テーブルを定期的に再読み込みする
     */
    protected static class CyclicReloadTask extends TimerTask {

        private final AbstractTableDialog dialog;

        public CyclicReloadTask(AbstractTableDialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void run() {
            if (!this.dialog.needsUpdate) {
                // 更新の必要はない
                return;
            }
            this.dialog.display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (!CyclicReloadTask.this.dialog.shell.isDisposed()) {
                        // 見えているときだけ処理する
                        if (CyclicReloadTask.this.dialog.shell.isVisible()) {
                            CyclicReloadTask.this.dialog.reloadTable();
                        }
                    }
                    else {
                        // ウインドウが消えていたらタスクをキャンセルする
                        CyclicReloadTask.this.cancel();
                    }
                }
            });
        }
    }
}