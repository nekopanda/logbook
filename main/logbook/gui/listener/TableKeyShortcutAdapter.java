package logbook.gui.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Table;

/**
 * テーブルウィジェットのキー操作のアダプターです
 *
 */
public final class TableKeyShortcutAdapter extends KeyAdapter {

    /** テーブルヘッダー */
    private final String[] header;

    /** テーブル */
    private final Table table;

    /**
     * コンストラクター
     */
    public TableKeyShortcutAdapter(String[] header, Table table) {
        this.header = header;
        this.table = table;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if ((e.stateMask == SWT.CTRL) && (e.keyCode == 'c')) {
            TableToClipboardAdapter.copyTable(this.header, this.table);
        }
        if ((e.stateMask == SWT.CTRL) && (e.keyCode == 'a')) {
            this.table.selectAll();
        }
    }
}
