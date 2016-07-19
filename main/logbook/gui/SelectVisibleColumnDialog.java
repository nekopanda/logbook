package logbook.gui;

import logbook.util.SwtUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * テーブルの列を表示・非表示選択するダイアログ
 *
 */
public final class SelectVisibleColumnDialog extends WindowBase {

    /** 親ダイアログ */
    private final AbstractTableDialog dialog;

    /** シェル */
    private Shell shell;

    /**
     * Create the dialog.
     * @param parent 親シェル
     * @param dialog 親ダイアログ
     */
    public SelectVisibleColumnDialog(AbstractTableDialog dialog) {
        super.createContents(dialog, SWT.SHELL_TRIM | SWT.PRIMARY_MODAL, false);
        this.dialog = dialog;
    }

    /**
     * Open the dialog.
     */
    @Override
    public void open() {
        this.createContents();
        this.registerEvents();
        this.shell.open();
        this.shell.layout();
        Display display = this.shell.getDisplay();
        while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        this.shell = this.getShell();
        this.shell.setSize(SwtUtils.DPIAwareSize(new Point(300, 275)));
        this.shell.setText("列の表示非表示");
        this.shell.setLayout(new FillLayout(SWT.HORIZONTAL));

        // ヘッダー
        String[] header = this.dialog.header;
        // カラム設定を取得
        boolean[] visibles = this.dialog.getConfig().getVisibleColumn();

        Tree tree = new Tree(this.shell, SWT.BORDER | SWT.CHECK);

        for (int i = 0; i < header.length; i++) {
            TreeItem column = new TreeItem(tree, SWT.CHECK);
            column.setText(header[i]);
            column.setChecked(visibles[i]);
            column.setExpanded(true);
        }
        this.shell.addShellListener(new TreeShellAdapter(tree, this.dialog));
    }

    /**
     * チェックされた内容をウインドウが閉じるタイミングで保存します
     *
     */
    private static final class TreeShellAdapter extends ShellAdapter {

        private final Tree tree;
        private final AbstractTableDialog dialog;

        public TreeShellAdapter(Tree tree, AbstractTableDialog dialog) {
            this.tree = tree;
            this.dialog = dialog;
        }

        @Override
        public void shellClosed(ShellEvent e) {
            TreeItem[] items = this.tree.getItems();
            boolean[] visibles = new boolean[items.length];
            for (int i = 0; i < items.length; i++) {
                visibles[i] = items[i].getChecked();
            }
            this.dialog.setColumnVisible(visibles);
        }
    }
}
