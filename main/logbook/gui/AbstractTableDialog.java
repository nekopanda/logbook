package logbook.gui;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import logbook.config.AppConfig;
import logbook.config.bean.TableConfigBean;
import logbook.gui.listener.TableKeyShortcutAdapter;
import logbook.gui.listener.TableToClipboardAdapter;
import logbook.gui.listener.TableToCsvSaveAdapter;
import logbook.gui.logic.TableItemCreator;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * テーブルで構成されるダイアログの基底クラス
 *
 */
public abstract class AbstractTableDialog extends WindowBase {

    private static int MAX_PRINT_ITEMS = 2000;

    private final Shell parent;

    /** タイマー */
    protected Timer timer;

    /** ヘッダー */
    protected String[] header = this.getTableHeader();

    /** テーブルに表示しているボディー */
    protected List<Comparable[]> body;

    /** ソート順序 */
    protected final boolean[] orderflgs = new boolean[this.header.length];

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

    private Display display;

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
        this.menubar = new Menu(this.shell, SWT.BAR);
        this.shell.setMenuBar(this.menubar);
        // テーブルより前に作成する必要があるコンポジットを作成
        this.createContentsBefore();
        // テーブル
        this.table = new Table(this.getTableParent(), SWT.FULL_SELECTION | SWT.MULTI);
        this.table.addKeyListener(new TableKeyShortcutAdapter(this.header, this.table));
        this.table.setLinesVisible(true);
        this.table.setHeaderVisible(true);
        // メニューバーのメニュー
        MenuItem fileroot = new MenuItem(this.menubar, SWT.CASCADE);
        fileroot.setText("ファイル");
        this.filemenu = new Menu(fileroot);
        fileroot.setMenu(this.filemenu);

        MenuItem savecsv = new MenuItem(this.filemenu, SWT.NONE);
        savecsv.setText("CSVファイルに保存(&S)\tCtrl+S");
        savecsv.setAccelerator(SWT.CTRL + 'S');
        savecsv.addSelectionListener(new TableToCsvSaveAdapter(this.shell, this.getTitle(), this.getTableHeader(),
                this.table));

        MenuItem operoot = new MenuItem(this.menubar, SWT.CASCADE);
        operoot.setText("操作");
        this.opemenu = new Menu(operoot);
        operoot.setMenu(this.opemenu);

        MenuItem reload = new MenuItem(this.opemenu, SWT.NONE);
        reload.setText("再読み込み(&R)\tF5");
        reload.setAccelerator(SWT.F5);
        reload.addSelectionListener(new TableReloadAdapter());

        MenuItem cyclicReload = new MenuItem(this.opemenu, SWT.CHECK);
        cyclicReload.setText("定期的に再読み込み(3秒)(&A)\tCtrl+F5");
        cyclicReload.setAccelerator(SWT.CTRL + SWT.F5);
        cyclicReload.addSelectionListener(new CyclicReloadAdapter(cyclicReload));

        MenuItem selectVisible = new MenuItem(this.opemenu, SWT.NONE);
        selectVisible.setText("列の表示・非表示(&V)");
        selectVisible.addSelectionListener(new SelectVisibleColumnAdapter());

        MenuItem resetOrder = new MenuItem(this.opemenu, SWT.NONE);
        resetOrder.setText("列の順番をリセット");
        resetOrder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                AbstractTableDialog.this.resetColumnOrder();
            }
        });

        // ウィンドウの基本メニューを設定
        super.registerEvents();

        new MenuItem(this.opemenu, SWT.SEPARATOR);

        // テーブル右クリックメニュー
        this.tablemenu = this.getMenu();
        this.table.setMenu(this.tablemenu);
        new MenuItem(this.tablemenu, SWT.SEPARATOR);
        MenuItem sendclipbord = new MenuItem(this.tablemenu, SWT.NONE);
        sendclipbord.addSelectionListener(new TableToClipboardAdapter(this.header, this.table));
        sendclipbord.setText("クリップボードにコピー(&C)");
        MenuItem reloadtable = new MenuItem(this.tablemenu, SWT.NONE);
        reloadtable.setText("再読み込み(&R)");
        reloadtable.addSelectionListener(new TableReloadAdapter());
        // テーブルにヘッダーをセット
        this.setTableHeader();
        // テーブルに内容をセット
        this.updateTableBody();
        this.setTableBody();
        // 列幅を整える
        this.packTableHeader();

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
                if (AbstractTableDialog.this.timer != null) {
                    AbstractTableDialog.this.timer.cancel();
                }
            }
        });

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
        this.table.setRedraw(false);
        TableColumn sortColumn = this.table.getSortColumn();
        int topindex = this.table.getTopIndex();
        int selection = this.table.getSelectionIndex();
        int prevItemCount = this.table.getItemCount();
        this.table.setSortColumn(null);
        this.disposeTableBody();
        this.updateTableBody();
        this.sortBody();
        this.setTableBody();
        if (prevItemCount == 0) { // 表示アイテムがなかった時だけカラムの大きさを再計算
            this.packTableHeader();
        }
        this.table.setSortColumn(sortColumn);
        this.table.setSelection(selection);
        this.table.setTopIndex(topindex);
        this.getShell().setText(this.getTitle());
        this.table.setRedraw(true);
        this.table.setTopIndex(topindex);
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
        }
        if (this.config.getColumnOrder() != null) {
            this.table.setColumnOrder(this.config.getColumnOrder());
        }
        this.packTableHeader();
    }

    /**
     * テーブルボディーをセットする
     */
    protected void setTableBody() {
        TableItemCreator creator = this.getTableItemCreator();
        creator.init();
        // 表示最大件数を制限する
        int numPrintItems = Math.min(MAX_PRINT_ITEMS, this.body.size());
        for (int i = 0; i < numPrintItems; i++) {
            Comparable[] line = this.body.get(i);
            creator.create(this.table, line, i);
        }
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
     * テーブルヘッダーの幅を調節する
     */
    protected void packTableHeader() {
        boolean[] visibles = this.getConfig().getVisibleColumn();

        TableColumn[] columns = this.table.getColumns();

        for (int i = 0; i < columns.length; i++) {
            if (visibles[i]) {
                columns[i].pack();
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

    private void resetColumnOrder() {
        int[] columnOrder = new int[this.header.length];
        for (int i = 0; i < this.header.length; ++i) {
            columnOrder[i] = i;
        }
        if (this.config != null) {
            this.config.setColumnOrder(columnOrder);
        }
        this.table.setColumnOrder(columnOrder);
    }

    protected TableConfigBean getConfig() {
        if (this.config == null) {
            this.config = AppConfig.get().getTableConfigMap().get(this.getWindowId());

            // 列の表示・非表示設定のサイズがカラム数と異なっている場合は破棄する
            if (this.config != null) {
                if (this.config.getVisibleColumn() == null) {
                    this.config = null;
                }
                else if (this.config.getVisibleColumn().length != this.header.length) {
                    this.config = null;
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
        }
        return title;
    }

    @Override
    public void save() {
        if (this.config != null) {
            this.config.setColumnOrder(this.table.getColumnOrder());
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
    protected abstract SelectionListener getHeaderSelectionListener();

    /**
     * テーブルの初期状態
     * @return
     */
    protected TableConfigBean getDefaultTableConfig() {
        TableConfigBean config = new TableConfigBean();
        boolean[] visibles = new boolean[this.header.length];
        Arrays.fill(visibles, true);
        config.setVisibleColumn(visibles);
        int[] columnOrder = new int[this.header.length];
        for (int i = 0; i < this.header.length; ++i) {
            columnOrder[i] = i;
        }
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

        if (orderflg) {
            this.table.setSortColumn(headerColumn);
            this.table.setSortDirection(SWT.UP);
        } else {
            this.table.setSortColumn(headerColumn);
            this.table.setSortDirection(SWT.DOWN);
        }

        this.sortBody();
        this.setTableBody();

        //this.shell.setRedraw(true);
        this.table.setRedraw(true);
    }

    private void sortBody() {
        TableConfigBean.SortKey[] sortKeys = this.getConfig().getSortKeys();
        if (sortKeys != null) {
            for (int i = sortKeys.length - 1; i >= 0; --i) {
                if (sortKeys[i] != null) {
                    this.comparator.setIndex(sortKeys[i].index);
                    this.comparator.setOrder(sortKeys[i].order);
                    Collections.sort(this.body, this.comparator);
                }
            }
        }
    }

    /**
     * テーブルをソートする{@link java.util.Comparator}です。
     */
    protected class TableComparator implements Comparator<Comparable[]> {

        /** ソート設定済みフラグ */
        private boolean confflg = false;
        /** 列位置 */
        private int index;
        /** 昇順・降順フラグ */
        private boolean order;

        @Override
        public final int compare(Comparable[] o1, Comparable[] o2) {
            int ret;
            Comparable o1c = o1[this.index];
            Comparable o2c = o2[this.index];
            if (o1c == null) {
                if (o2c == null) {
                    ret = 0;
                }
                else {
                    ret = -1;
                }
            }
            else if (o2c == null) {
                ret = 1;
            }
            else {
                ret = o1[this.index].compareTo(o2[this.index]);
            }
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
                // タイマーを作成
                if (AbstractTableDialog.this.timer == null) {
                    AbstractTableDialog.this.timer = new Timer(true);
                }
                // 3秒毎に再読み込みするようにスケジュールする
                AbstractTableDialog.this.timer.schedule(new CyclicReloadTask(AbstractTableDialog.this), 0,
                        TimeUnit.SECONDS.toMillis(3));
            } else {
                // タイマーを終了
                if (AbstractTableDialog.this.timer != null) {
                    AbstractTableDialog.this.timer.cancel();
                    AbstractTableDialog.this.timer = null;
                }
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
            this.dialog.display.asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (!CyclicReloadTask.this.dialog.shell.isDisposed()) {
                        CyclicReloadTask.this.dialog.reloadTable();
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
