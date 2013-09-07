/**
 * 
 */
package logbook.gui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import logbook.config.GlobalConfig;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * 一覧のダイアログ
 * 
 */
public final class TableDialog extends Dialog {

    private Display display;
    private Object result;
    private Shell shell;

    private final String[] header;
    private final List<String[]> body;
    private final boolean[] orderflgs;

    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    public TableDialog(Shell parent, String title, String[] header, List<Object[]> body) {
        super(parent, SWT.SHELL_TRIM
                | SWT.SYSTEM_MODAL);
        this.header = header;
        this.body = new ArrayList<String[]>();
        for (Object[] objects : body) {
            String[] values = new String[objects.length];
            for (int i = 0; i < objects.length; i++) {
                if (objects[i] != null) {
                    values[i] = String.valueOf(objects[i]);
                } else {
                    values[i] = "";
                }
            }
            this.body.add(values);
        }

        this.orderflgs = new boolean[header.length];
        this.setText(title);
    }

    /**
     * Open the dialog.
     * @return the result
     */
    public Object open() {
        this.createContents();
        this.shell.open();
        this.shell.layout();
        this.display = this.getParent().getDisplay();
        while (!this.shell.isDisposed()) {
            if (!this.display.readAndDispatch()) {
                this.display.sleep();
            }
        }
        return this.result;
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
        MenuItem item1 = new MenuItem(menubar, SWT.CASCADE);
        item1.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg) {
                FileDialog dialog = new FileDialog(TableDialog.this.shell, SWT.SAVE);
                dialog.setFileName(TableDialog.this.getText() + ".csv");
                dialog.setFilterExtensions(new String[] { "*.csv" });
                String filename = dialog.open();
                if (filename != null) {
                    File file = new File(filename);
                    if (file.exists()) {
                        MessageBox messageBox = new MessageBox(TableDialog.this.shell, SWT.YES | SWT.NO);
                        messageBox.setText("確認");
                        messageBox.setMessage("指定されたファイルは存在します。\n上書きしますか？");
                        if (messageBox.open() == SWT.NO) {
                            return;
                        }
                    }
                    try {
                        OutputStream stream = new BufferedOutputStream(new FileOutputStream(file));
                        try {
                            IOUtils.write(StringUtils.join(TableDialog.this.header, ',') + "\r\n", stream,
                                    GlobalConfig.CHARSET);
                            for (String[] colums : TableDialog.this.body) {
                                IOUtils.write(StringUtils.join(colums, ',') + "\r\n", stream,
                                        GlobalConfig.CHARSET);
                            }
                        } finally {
                            stream.close();
                        }
                    } catch (IOException e) {
                        MessageBox messageBox = new MessageBox(TableDialog.this.shell, SWT.ICON_ERROR);
                        messageBox.setText("書き込めませんでした");
                        messageBox.setMessage(e.toString());
                        messageBox.open();
                    }
                }
            }
        });
        item1.setText("CSVファイルに保存");

        final Table table = new Table(this.shell, SWT.FULL_SELECTION | SWT.MULTI);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        TableColumn[] columns = new TableColumn[this.header.length];

        for (int i = 0; i < this.header.length; i++) {
            TableColumn col = new TableColumn(table, SWT.LEFT);
            col.setText(this.header[i]);
            columns[i] = col;
        }

        this.addAllTableItems(table);

        Menu menu = new Menu(table);
        table.setMenu(menu);

        MenuItem menuItem = new MenuItem(menu, SWT.NONE);
        menuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent arg) {

                TableItem[] tableItems = table.getSelection();

                StringBuilder sb = new StringBuilder();
                sb.append(StringUtils.join(TableDialog.this.header, "\t"));
                sb.append("\r\n");
                for (TableItem column : tableItems) {
                    String[] columns = new String[TableDialog.this.header.length];
                    for (int i = 0; i < TableDialog.this.header.length; i++) {
                        columns[i] = column.getText(i);
                    }
                    sb.append(StringUtils.join(columns, "\t"));
                    sb.append("\r\n");
                }

                Clipboard clipboard = new Clipboard(TableDialog.this.display);
                clipboard.setContents(new Object[] { sb.toString() },
                        new Transfer[] { TextTransfer.getInstance() });
            }
        });
        menuItem.setText("クリップボードにコピー");

        for (int i = 0; i < columns.length; i++) {
            columns[i].pack();
            columns[i].addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent arg) {
                    // ソートを行う
                    if (arg.getSource() instanceof TableColumn) {
                        // ソート前に画面描画を停止する
                        TableDialog.this.shell.setRedraw(false);
                        TableItem[] items = table.getItems();
                        for (int i = 0; i < items.length; i++) {
                            items[i].dispose();
                        }
                        String header = ((TableColumn) arg.getSource()).getText();
                        TableDialog.this.sortTableItems(header);
                        TableDialog.this.addAllTableItems(table);
                        // 画面描画を再開する
                        TableDialog.this.shell.setRedraw(true);
                    }
                    super.widgetSelected(arg);
                }
            });
        }
    }

    private void addAllTableItems(Table table) {
        for (String[] line : this.body) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(line);
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
