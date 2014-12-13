package logbook.gui.listener;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * ツリーをクリップボードにコピーするアダプターです
 *
 */
public final class TreeToClipboardAdapter extends SelectionAdapter {

    /** ツリーヘッダー */
    private final String[] header;

    /** ツリー */
    private final Tree tree;

    /**
     * コンストラクター
     * 
     * @param header ヘッダー
     * @param tree ツリー
     */
    public TreeToClipboardAdapter(String[] header, Tree tree) {
        this.header = header;
        this.tree = tree;
    }

    @Override
    public void widgetSelected(SelectionEvent arg) {
        copyTree(this.header, this.tree);
    }

    /**
     * ツリーの選択されている部分をヘッダー付きでクリップボードにコピーします
     * 
     * @param header ヘッダー
     * @param tree ツリー
     */
    public static void copyTree(String[] header, Tree tree) {
        TreeItem[] treeItems = tree.getSelection();
        StringBuilder sb = new StringBuilder();
        sb.append(StringUtils.join(header, "\t"));
        sb.append("\r\n");
        for (TreeItem column : treeItems) {
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
