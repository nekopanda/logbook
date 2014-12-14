package logbook.gui.listener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Tree;

/**
 * ツリーウィジェットのキー操作のアダプターです
 *
 */
public final class TreeKeyShortcutAdapter extends KeyAdapter {

    /** ツリーヘッダー */
    private final String[] header;

    /** ツリー */
    private final Tree tree;

    /**
     * コンストラクター
     */
    public TreeKeyShortcutAdapter(String[] header, Tree tree) {
        this.header = header;
        this.tree = tree;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if ((e.stateMask == SWT.CTRL) && (e.keyCode == 'c')) {
            TreeToClipboardAdapter.copyTree(this.header, this.tree);
        }
        if ((e.stateMask == SWT.CTRL) && (e.keyCode == 'a')) {
            this.tree.selectAll();
        }
    }
}
