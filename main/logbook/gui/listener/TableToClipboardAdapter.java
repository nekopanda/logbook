/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui.listener;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * テーブルをクリップボードにコピーするアダプターです
 *
 */
public final class TableToClipboardAdapter extends SelectionAdapter {

    /** テーブルヘッダー */
    private final String[] header;

    /** テーブル */
    private final Table table;

    /**
     * コンストラクター
     * 
     * @param header ヘッダー
     * @param table テーブル
     */
    public TableToClipboardAdapter(String[] header, Table table) {
        this.header = header;
        this.table = table;
    }

    @Override
    public void widgetSelected(SelectionEvent arg) {
        TableItem[] tableItems = this.table.getSelection();
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.join(this.header, "\t"));
        sb.append("\r\n");
        for (TableItem column : tableItems) {
            String[] columns = new String[this.header.length];
            for (int i = 0; i < this.header.length; i++) {
                columns[i] = column.getText(i);
            }
            sb.append(StringUtils.join(columns, "\t"));
            sb.append("\r\n");
        }
        Clipboard clipboard = new Clipboard(Display.getDefault());
        clipboard.setContents(new Object[] { sb.toString() }, new Transfer[] { TextTransfer.getInstance() });
    }
}
