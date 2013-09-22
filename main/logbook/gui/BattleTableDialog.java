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
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
 * ドロップ報告書のダイアログ
 * 
 */
public final class BattleTableDialog extends Dialog {

    private Display display;
    private Shell shell;

    private final String[] header;
    private final List<String[]> body;
    private final boolean[] orderflgs;
    private final TableItemCreator creater;

    /**
     * Create the dialog.
     * @param parent
     * @param title
     * @param header
     * @param body
     * @param creater
     */
    public BattleTableDialog(Shell parent, String title, String[] header, List<String[]> body,
            TableItemCreator creater) {
        super(parent, SWT.SHELL_TRIM | SWT.MODELESS);
        this.shell = parent;
        this.header = header;
        this.body = body;
        this.creater = creater;

        this.orderflgs = new boolean[header.length];
        this.setText(title);
    }

    /**
     * Open the dialog.
     */
    public void open() {
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
     * Create contents of the dialog.
     */
    private void createContents() {
        this.shell = new Shell(this.getParent(), this.getStyle());
        this.shell.setSize(600, 350);
        this.shell.setText(this.getText());
        this.shell.setLayout(new FillLayout());

        Menu menubar = new Menu(this.shell, SWT.BAR);
        this.shell.setMenuBar(menubar);
        MenuItem fileroot = new MenuItem(menubar, SWT.CASCADE);
        fileroot.setText("ファイル");
        Menu filemenu = new Menu(fileroot);
        fileroot.setMenu(filemenu);

        MenuItem savecsv = new MenuItem(filemenu, SWT.NONE);
        savecsv.setText("CSVファイルに保存");

        final Table table = new Table(this.shell, SWT.FULL_SELECTION | SWT.MULTI);
        table.addKeyListener(new TableKeyShortcutAdapter(this.header, table));
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                TableItem[] items = table.getSelection();
                for (TableItem tableItem : items) {
                    new BattleDialog(BattleTableDialog.this.shell, tableItem.getText(0)).open();
                }
            }
        });
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        TableColumn[] columns = new TableColumn[this.header.length];

        for (int i = 0; i < this.header.length; i++) {
            TableColumn col = new TableColumn(table, SWT.LEFT);
            col.setText(this.header[i]);
            columns[i] = col;
        }

        this.addAllTableItems(table);

        savecsv.addSelectionListener(new TableToCsvSaveAdapter(this.shell, this.getText(), this.header, table));

        Menu menu = new Menu(table);
        table.setMenu(menu);
        MenuItem menuItem = new MenuItem(menu, SWT.NONE);
        menuItem.addSelectionListener(new TableToClipboardAdapter(this.header, table));
        menuItem.setText("クリップボードにコピー");

        for (int i = 0; i < columns.length; i++) {
            columns[i].pack();
            columns[i].addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent arg) {
                    // ソートを行う
                    if (arg.getSource() instanceof TableColumn) {
                        // ソート前に画面描画を停止する
                        BattleTableDialog.this.shell.setRedraw(false);
                        TableItem[] items = table.getItems();
                        for (int i = 0; i < items.length; i++) {
                            items[i].dispose();
                        }
                        String header = ((TableColumn) arg.getSource()).getText();
                        BattleTableDialog.this.sortTableItems(header);
                        BattleTableDialog.this.addAllTableItems(table);
                        // 画面描画を再開する
                        BattleTableDialog.this.shell.setRedraw(true);
                    }
                    super.widgetSelected(arg);
                }
            });
        }
    }

    private void addAllTableItems(Table table) {
        for (String[] line : this.body) {
            this.creater.create(table, line);
        }
        table.redraw();
    }

    private void sortTableItems(String headerText) {
        int idxtemp = 0;
        for (int i = 0; i < this.header.length; i++) {
            if (this.header[i].equals(headerText)) {
                idxtemp = i;
                break;
            }
        }
        final int idx = idxtemp;
        final boolean orderflg = !this.orderflgs[idx];
        for (int i = 0; i < this.orderflgs.length; i++) {
            this.orderflgs[i] = false;
        }
        this.orderflgs[idx] = orderflg;

        Collections.sort(this.body, new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                if (StringUtils.isEmpty(o1[idx]) && StringUtils.isEmpty(o2[idx])) {
                    return 0;
                }
                if (StringUtils.isEmpty(o1[idx])) {
                    return 1;
                }
                if (StringUtils.isEmpty(o2[idx])) {
                    return -1;
                }
                if (!orderflg) {
                    return StringUtils.leftPad(o2[idx], 12, '0').compareTo(StringUtils.leftPad(o1[idx], 12, '0'));
                } else {
                    return StringUtils.leftPad(o1[idx], 12, '0').compareTo(StringUtils.leftPad(o2[idx], 12, '0'));
                }
            }
        });
    }
}
