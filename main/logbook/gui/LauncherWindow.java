/**
 * 
 */
package logbook.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import logbook.config.AppConfig;
import logbook.constants.AppConstants;
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

    private final List<Button> currentButtons = new ArrayList<>();

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

    public static WindowBase[] getWindowList() {
        WindowBase[] winList = ApplicationMain.main.getWindowList();
        return Arrays.copyOf(winList, winList.length - 5);
    }

    public static Map<String, Integer> getWindowKeyMap() {
        WindowBase[] winList = getWindowList();
        Map<String, Integer> keyMap = new HashMap<>();
        for (int i = 0; i < winList.length; i++) {
            keyMap.put(winList[i].getWindowId(), i);
        }
        return keyMap;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        super.createContents(this.parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.TOOL, false);
        this.getShell().setText("ツール");
        final Shell shell = this.getShell();
        shell.setLayout(new RowLayout(SWT.HORIZONTAL));

        this.recreateButtons(AppConfig.get().getToolButtons());

        // 設定右クリックメニュー
        final MenuItem configButton = new MenuItem(this.getPopupMenu(), SWT.PUSH, 0);
        configButton.setText("ボタン設定");
        configButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                final ConfigDialog configDialog = new ConfigDialog(ApplicationMain.main);
                configDialog.open();
                shell.getDisplay().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        configDialog.selectPane("ツール");
                    }
                });
            }
        });
        new MenuItem(this.getPopupMenu(), SWT.SEPARATOR, 1);

        shell.layout();
    }

    public void configUpdated() {
        if (this.isWindowInitialized()) {
            List<String> keys = AppConfig.get().getToolButtons();
            if (this.isChanged(keys)) {
                this.recreateButtons(keys);
                this.getShell().layout();
            }
        }
    }

    private boolean isChanged(List<String> keys) {
        if (keys.size() != this.currentButtons.size()) {
            return true;
        }
        for (int i = 0; i < keys.size(); ++i) {
            if (keys.get(i).equals(this.currentButtons.get(i).getData("key")) == false) {
                return true;
            }
        }
        return false;
    }

    private void recreateButtons(List<String> toolButtons) {
        Shell shell = this.getShell();

        // ボタンを削除
        for (Button button : this.currentButtons) {
            WindowBase win = (WindowBase) button.getData("window");
            win.removeWindowListener((WindowListener) button.getData("window-listener"));
            button.setMenu(null);
            button.dispose();
        }
        this.currentButtons.clear();

        // 作成
        RowData rowData = new RowData(40, 30);
        WindowBase[] winList = getWindowList();
        Map<String, Integer> keyMap = getWindowKeyMap();
        for (String key : toolButtons) {
            int i = keyMap.get(key);
            final WindowBase win = winList[i];
            final Button button = new Button(shell, SWT.TOGGLE);
            this.currentButtons.add(button);
            button.setText(AppConstants.SHORT_WINDOW_NAME_LIST[i]);
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
            WindowListener winListener = new WindowListener() {
                @Override
                public void windowShown() {
                    button.setSelection(true);
                }

                @Override
                public void windowHidden() {
                    button.setSelection(false);
                }
            };
            win.addWindowListener(winListener);
            // ボタンのサイズを設定
            button.setLayoutData(rowData);
            button.setData("disable-drag-move", true);

            // 関連データをストア
            button.setData("key", key);
            button.setData("window-listener", winListener);
            button.setData("window", win);
        }
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
