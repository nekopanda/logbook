/**
 * 
 */
package logbook.gui;

import logbook.config.AppConfig;
import logbook.gui.logic.WindowListener;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Nekopanda
 *
 */
public class LauncherWindow extends WindowBase {

    private final Shell parent;

    /**
     * Create the dialog.
     * @param parent
     */
    public LauncherWindow(Shell parent, MenuItem menuItem) {
        super(menuItem);
        this.parent = parent;
    }

    /**
     * Open the dialog.
     */
    @Override
    public void open() {
        // 初期化済みの場合
        if (this.isWindowInitialized()) {
            // リロードして表示
            this.setVisible(true);
            return;
        }

        this.createContents();
        this.registerEvents();
        this.setWindowInitialized(true);
        this.setVisible(true);
    }

    @Override
    protected boolean moveWithDrag() {
        return true;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        super.createContents(this.parent, SWT.CLOSE | SWT.ON_TOP | SWT.TITLE | SWT.RESIZE | SWT.TOOL, false);
        this.getShell().setText("ツール");
        Shell shell = this.getShell();
        shell.setLayout(new RowLayout(SWT.HORIZONTAL));

        WindowBase[] winList = ApplicationMain.main.getWindowList();
        String[] nameList = new String[] {
                "Cap",
                "ドロ",
                "建造",
                "開発",
                "遠征",
                "一覧",
                "装備",
                "艦1",
                "艦2",
                "艦3",
                "艦4",
                "風呂",
                "任務",
                "戦況",
                "戦横",
                "パラ",
                "経験",
                "演習",
                "グル",
                "資材",
                "統計",
                "#1",
                "#2",
                "#3",
                "#4",
                "ロー" // 最後は自分
        };

        RowData rowData = new RowData(40, 30);
        for (int i = 0; i < (winList.length - 5); i++) {
            final WindowBase win = winList[i];
            final Button button = new Button(shell, SWT.TOGGLE);
            button.setText(nameList[i]);
            button.setSelection((win.getShell() != null) ? win.getVisible() : false);
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    boolean toggleEnabled = AppConfig.get().isToggleToolButton();
                    if (!toggleEnabled || button.getSelection()) {
                        if (!toggleEnabled) {
                            // offにしない
                            button.setSelection(true);
                        }
                        win.open();
                        win.getShell().setActive();
                    }
                    else {
                        win.hideWindow();
                    }
                }
            });
            win.addWindowListener(new WindowListener() {
                @Override
                public void windowShown() {
                    button.setSelection(true);
                }

                @Override
                public void windowHidden() {
                    button.setSelection(false);
                }
            });
            // ボタンのサイズを設定
            button.setLayoutData(rowData);
            button.setData("disable-drag-move", true);
        }

        shell.layout();
    }

    /**
     * ウィンドウサイズを保存・リストアするべきか？
     * @return
     */
    @Override
    protected boolean shouldSaveWindowSize() {
        return true;
    }

    /**
     * ウィンドウのデフォルトサイズを取得
     * @return
     */
    @Override
    protected Point getDefaultSize() {
        return new Point(500, 80);
    }
}
