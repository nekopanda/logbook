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
        copyTable(this.header, this.table);
    }

    /**
     * テーブルの選択されている部分をヘッダー付きでクリップボードにコピーします
     * 
     * @param header ヘッダー
     * @param table テーブル
     */
    public static void copyTable(String[] header, Table table) {
        TableItem[] tableItems = table.getSelection();
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.join(header, "\t"));
        sb.append("\r\n");
        for (TableItem column : tableItems) {
            String[] columns = new String[header.length];
            for (int i = 0; i < header.length; i++) {
                columns[i] = column.getText(i);
            }
            sb.append(StringUtils.join(columns, "\t"));
            sb.append("\r\n");
        }
        Clipboard clipboard = new Clipboard(Display.getDefault());
        clipboard.setContents(new Object[] { sb.toString() }, new Transfer[] { TextTransfer.getInstance() });
    }
}
