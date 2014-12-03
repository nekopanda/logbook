package logbook.gui;

import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import logbook.config.AppConfig;
import logbook.gui.listener.TableKeyShortcutAdapter;
import logbook.gui.listener.TableToClipboardAdapter;
import logbook.gui.listener.TableToCsvSaveAdapter;
import logbook.gui.logic.TableItemCreator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * テーブルで構成されるダイアログの基底クラス
 *
 */
public abstract class AbstractTableDialog extends Dialog {

    /** タイマー */
    protected Timer timer;

    /** ヘッダー */
    protected String[] header = this.getTableHeader();

    /** テーブルに表示しているボディー */
    protected List<String[]> body;

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

    /** テーブルソート */
    protected final TableComparator comparator = new TableComparator();

    private Display display;

    /**
     * コンストラクター
     */
    public AbstractTableDialog(Shell parent) {
        super(parent, SWT.SHELL_TRIM | SWT.MODELESS);
    }

    /**
     * Open the dialog.
     */
    public void open() {
        // シェルを作成
        this.shell = new Shell(this.getParent(), this.getStyle());
        this.shell.setSize(this.getSize());
        this.shell.setText(this.getTitle());
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

        new MenuItem(this.opemenu, SWT.SEPARATOR);

        // テーブル右クリックメニュー
        this.tablemenu = new Menu(this.table);
        this.table.setMenu(this.tablemenu);
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

        this.createContents();
        this.shell.open();
        this.shell.layout();
        this.display = this.getParent().getDisplay();
        while (!this.shell.isDisposed()) {
            if (!this.display.readAndDispatch()) {
                this.display.sleep();
            }
        }
        // タイマーの終了
        if (this.timer != null) {
            this.timer.cancel();
        }
    }

    /**
     * テーブルをリロードする
     */
    protected void reloadTable() {
        this.shell.setRedraw(false);
        TableColumn sortColumn = this.table.getSortColumn();
        int topindex = this.table.getTopIndex();
        int[] selection = this.table.getSelectionIndices();
        this.table.setSortColumn(null);
        this.disposeTableBody();
        this.updateTableBody();
        if (this.comparator.getHasSetConfig()) {
            Collections.sort(this.body, this.comparator);
        }
        this.setTableBody();
        this.packTableHeader();
        this.table.setSortColumn(sortColumn);
        this.table.setSelection(selection);
        this.table.setTopIndex(topindex);
        this.shell.setRedraw(true);
    }

    /**
     * テーブルヘッダーをセットする
     */
    private void setTableHeader() {
        SelectionListener listener = this.getHeaderSelectionListener();
        for (int i = 0; i < this.header.length; i++) {
            TableColumn col = new TableColumn(this.table, SWT.LEFT);
            col.setText(this.header[i]);
            col.addSelectionListener(listener);
        }
        this.packTableHeader();
    }

    /**
     * テーブルボディーをセットする
     */
    protected void setTableBody() {
        TableItemCreator creator = this.getTableItemCreator();
        creator.init();
        for (int i = 0; i < this.body.size(); i++) {
            String[] line = this.body.get(i);
            creator.create(this.table, line, i);
        }
    }

    /**
     * テーブルボディーをクリアする
     */
    protected void disposeTableBody() {
        TableItem[] items = this.table.getItems();
        for (int i = 0; i < items.length; i++) {
            items[i].dispose();
        }
    }

    /**
     * テーブルヘッダーの幅を調節する
     */
    protected void packTableHeader() {
        boolean[] visibles = AppConfig.get().getVisibleColumnMap().get(this.getClass().getName());

        TableColumn[] columns = this.table.getColumns();

        // 列の表示・非表示設定のサイズがカラム数と異なっている場合は破棄する
        if (visibles != null) {
            if (visibles.length != columns.length) {
                AppConfig.get().getVisibleColumnMap().remove(this.getClass().getName());
                visibles = null;
            }
        }

        for (int i = 0; i < columns.length; i++) {
            if ((visibles == null) || visibles[i]) {
                columns[i].pack();
            } else {
                columns[i].setWidth(0);
            }
        }
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
     * タイトルを返します
     * @return String
     */
    protected abstract String getTitle();

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
        this.shell.setRedraw(false);
        this.disposeTableBody();

        final boolean orderflg = !this.orderflgs[index];
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

        this.comparator.setIndex(index);
        this.comparator.setOrder(orderflg);
        Collections.sort(this.body, this.comparator);
        this.setTableBody();

        this.shell.setRedraw(true);

    }

    /**
     * テーブルをソートする{@link java.util.Comparator}です。
     */
    protected class TableComparator implements Comparator<String[]> {

        /** ソート設定済みフラグ */
        private boolean confflg;
        /** 列位置 */
        private int index;
        /** 昇順・降順フラグ */
        private boolean order;

        @Override
        public final int compare(String[] o1, String[] o2) {
            String t1 = o1[this.index];
            String t2 = o2[this.index];
            if (StringUtils.isEmpty(t1) && StringUtils.isEmpty(t2)) {
                return 0;
            }
            if (StringUtils.isEmpty(t1)) {
                return 1;
            }
            if (StringUtils.isEmpty(t2)) {
                return -1;
            }
            if (StringUtils.isNumeric(t1) && StringUtils.isNumeric(t2)) {
                // 数値文字列の場合
                Long o1l = Long.valueOf(t1);
                Long o2l = Long.valueOf(t2);
                return this.compareTo(o1l, o2l, this.order);
            } else if (t1.matches("(?:\\d+日)?(?:\\d+時間)?(?:\\d+分)?(?:\\d+秒)?")) {
                try {
                    // 時刻文字列の場合
                    // SimpleDateFormatは24時間超えるような時刻でも正しく?パースしてくれる
                    Date o1date = DateUtils.parseDate(t1, "ss秒", "mm分ss秒", "HH時間mm分", "dd日HH時間mm分");
                    Date o2date = DateUtils.parseDate(t2, "ss秒", "mm分ss秒", "HH時間mm分", "dd日HH時間mm分");
                    return this.compareTo(o1date, o2date, this.order);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            // 文字列の場合
            return this.compareTo(t1, t2, this.order);
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
            new SelectVisibleColumnDialog(AbstractTableDialog.this.shell, AbstractTableDialog.this).open();
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
            synchronized (this.dialog.shell) {
                if (!this.dialog.shell.isDisposed()) {
                    this.dialog.display.asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            CyclicReloadTask.this.dialog.reloadTable();
                        }
                    });
                } else {
                    // ウインドウが消えていたらタスクをキャンセルする
                    this.cancel();
                }
            }
        }
    }
}
