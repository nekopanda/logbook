/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import logbook.gui.listener.TableKeyShortcutAdapter;
import logbook.gui.listener.TableToClipboardAdapter;
import logbook.gui.listener.TableToCsvSaveAdapter;
import logbook.gui.logic.TableItemCreator;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author noname
 *
 */
public abstract class AbstractTableDialog extends Dialog {

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
    public final void open() {

        this.shell = new Shell(this.getParent(), this.getStyle());
        this.shell.setSize(this.getSize());
        this.shell.setText(this.getTitle());
        this.shell.setLayout(new FillLayout());

        this.menubar = new Menu(this.shell, SWT.BAR);
        this.shell.setMenuBar(this.menubar);

        this.table = new Table(this.shell, SWT.FULL_SELECTION | SWT.MULTI);
        this.table.addKeyListener(new TableKeyShortcutAdapter(this.header, this.table));
        this.table.setLinesVisible(true);
        this.table.setHeaderVisible(true);

        this.tablemenu = new Menu(this.table);
        this.table.setMenu(this.tablemenu);
        MenuItem menuItem = new MenuItem(this.tablemenu, SWT.NONE);
        menuItem.addSelectionListener(new TableToClipboardAdapter(this.header, this.table));
        menuItem.setText("クリップボードにコピー");

        this.setTableHeader();

        MenuItem fileroot = new MenuItem(this.menubar, SWT.CASCADE);
        fileroot.setText("ファイル");
        this.filemenu = new Menu(fileroot);
        fileroot.setMenu(this.filemenu);

        MenuItem savecsv = new MenuItem(this.filemenu, SWT.NONE);
        savecsv.setText("CSVファイルに保存(Ctrl+&S)");
        savecsv.setAccelerator(SWT.CTRL + 's');
        savecsv.addSelectionListener(new TableToCsvSaveAdapter(this.shell, this.getTitle(), this.getTableHeader(),
                this.table));

        MenuItem operoot = new MenuItem(this.menubar, SWT.CASCADE);
        operoot.setText("操作");
        this.opemenu = new Menu(operoot);
        operoot.setMenu(this.opemenu);

        MenuItem reload = new MenuItem(this.opemenu, SWT.NONE);
        reload.setText("再読み込み(F5)");
        reload.setAccelerator(SWT.F5);
        reload.addSelectionListener(new TableReloadAdapter());

        this.updateTableBody();
        this.setTableBody();
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
    }

    /**
     * テーブルをリロードする
     */
    protected final void reloadTable() {
        this.shell.setRedraw(false);
        TableColumn sortColumn = this.table.getSortColumn();
        this.table.setSortColumn(null);
        this.disposeTableBody();
        this.updateTableBody();
        if (this.comparator.getHasSetConfig()) {
            Collections.sort(this.body, this.comparator);
        }
        this.setTableBody();
        this.packTableHeader();
        this.table.setSortColumn(sortColumn);
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
    protected final void setTableBody() {
        TableItemCreator creator = this.getTableItemCreator();
        for (String[] line : this.body) {
            creator.create(this.table, line);
        }
    }

    /**
     * テーブルボディーをクリアする
     */
    protected final void disposeTableBody() {
        TableItem[] items = this.table.getItems();
        for (int i = 0; i < items.length; i++) {
            items[i].dispose();
        }
    }

    /**
     * テーブルヘッダーの幅を調節する
     */
    protected final void packTableHeader() {
        TableColumn[] columns = this.table.getColumns();
        for (TableColumn tableColumn : columns) {
            tableColumn.pack();
        }
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
     * テーブルをソートする共通ロジック
     * 
     * @param headerColumn
     */
    protected final void sortTableItems(TableColumn headerColumn) {
        this.shell.setRedraw(false);
        this.disposeTableBody();

        int index = 0;
        for (int i = 0; i < this.header.length; i++) {
            if (this.header[i].equals(headerColumn.getText())) {
                index = i;
                break;
            }
        }

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
        private boolean confflg = false;
        /** 列位置 */
        private int index;
        /** 昇順・降順フラグ */
        private boolean order;

        @Override
        public final int compare(String[] o1, String[] o2) {
            int length = Math.max(o1[this.index].length(), o2[this.index].length());
            String o1str = StringUtils.leftPad(o1[this.index], length, '0');
            String o2str = StringUtils.leftPad(o2[this.index], length, '0');
            if (StringUtils.isEmpty(o1[this.index]) && StringUtils.isEmpty(o2[this.index])) {
                return 0;
            }
            if (StringUtils.isEmpty(o1[this.index])) {
                return 1;
            }
            if (StringUtils.isEmpty(o2[this.index])) {
                return -1;
            }
            if (!this.order) {
                return o2str.compareTo(o1str);
            } else {
                return o1str.compareTo(o2str);
            }
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
}
