/**
 * 
 */
package logbook.gui;

import java.util.ArrayList;
import java.util.List;

import logbook.config.AppConfig;
import logbook.config.bean.WindowConfigBean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Nekopanda
 * 位置記憶、透明処理などの基本処理を実装
 */
public class WindowBase {

    //　定数
    private static int[] ALPHA_VALUES = new int[] { 255, 230, 205, 154, 102, 26 };
    private static String[] ALPHA_TEXT = new String[] { "不透明化", "90%", "80%", "60%", "40%", "10%" };

    private static WindowTreeNode globalRootNode = new WindowTreeNode(true);

    private final WindowTreeNode treeNode = new WindowTreeNode(this);
    private WindowBase parent;
    private Shell shell;
    private Menu alphamenu;
    private MenuItem menuItem;

    private boolean windowInitialized;

    // メニュー
    private MenuItem opaqueItem;
    private MenuItem shareOpacityItem;
    private MenuItem[] opacity;

    // 設定
    private WindowConfigBean config;
    private boolean shareOpacitySetting = false;
    private int opacityIndex = 0;

    private static class WindowTreeNode implements OpacityAnimationClient {
        private WindowTreeNode parent;
        private final List<WindowTreeNode> childs = new ArrayList<WindowTreeNode>();
        private OpacityAnimation animation;
        public WindowBase window;
        private boolean enabled;

        public WindowTreeNode(WindowBase window) {
            this.window = window;
        }

        public WindowTreeNode(boolean enabled) {
            this.enabled = enabled;
        }

        public void setEnabled(boolean enabled) {
            if (this.enabled != enabled) {
                this.enabled = enabled;
                if (enabled) {
                    this.enable();
                }
                else {
                    if (this.animation != null) {
                        this.animation.setEnabled(false);
                    }
                }
            }
        }

        public void addChild(WindowTreeNode node) {
            this.childs.add(node);
            node.parent = this;
            if (this.enabled) {
                this.enable();
            }
        }

        public void removeChild(WindowTreeNode node) {
            this.childs.remove(node);
            node.parent = null;
            if (node.enabled) {
                node.enable();
            }
        }

        private void enable() {
            if (this.parent != null) {
                this.transferMasterSetting();
            }
            else {
                // ルートなのでアニメーションを有効化
                if (this.animation == null) {
                    this.animation = new OpacityAnimation(this);
                }
                WindowBase from = this.getFirstWindow();
                boolean hoverAware = from.opaqueItem.getSelection();
                int alhpa = ALPHA_VALUES[from.opacityIndex];
                this.animation.setInitialAlpha(alhpa, hoverAware);
                this.animation.setEnabled(true);
            }
        }

        private WindowBase getFirstWindow() {
            if (this.window != null) {
                return this.window;
            }
            for (WindowTreeNode node : this.childs) {
                if (node.window != null) {
                    return node.window;
                }
            }
            return null;
        }

        private WindowTreeNode getRoot() {
            // 一番上を取得
            WindowTreeNode node = this;
            while (node.parent != null)
                node = node.parent;
            return node;
        }

        private void transferMasterSetting() {
            WindowBase from = this.getRoot().getFirstWindow();
            if ((from != null) && (this.window != null) && (from != this.window)) {
                boolean hoverAware = from.opaqueItem.getSelection();
                int alhpa = ALPHA_VALUES[from.opacityIndex];
                this.window.opaqueChanged(hoverAware);
                this.window.opacityChanged(from.opacityIndex);
                this.window.shell.setAlpha(alhpa);
                if (this.animation != null) {
                    this.animation.setHoverAware(hoverAware);
                    this.animation.setAlpha(alhpa);
                }
            }
        }

        public boolean getMouseHoverAware() {
            return this.getRoot().animation.getHoverAware();
        }

        public int getAlpha() {
            return this.getRoot().animation.getAlpha();
        }

        @Override
        public boolean isMouseHovering() {
            if ((this.window != null) && this.window.isMouseHovering()) {
                return true;
            }
            for (WindowTreeNode child : this.childs) {
                if (child.isMouseHovering()) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public void setAlpha(int newAlpha) {
            if (this.window != null) {
                this.window.shell.setAlpha(newAlpha);
            }
            for (WindowTreeNode child : this.childs) {
                child.setAlpha(newAlpha);
            }
        }

        private static interface EventProc {
            void proc(WindowBase window);
        }

        private void routeEvent(EventProc proc) {
            if (this.window != null)
                proc.proc(this.window);

            for (WindowTreeNode node : this.childs) {
                node.routeEvent(proc);
            }
        }

        public void opaqueChanged(WindowBase source) {
            final boolean newValue = source.opaqueItem.getSelection();
            WindowTreeNode node = this.getRoot();
            if (node.animation != null) {
                node.animation.setHoverAware(newValue);
            }
            node.routeEvent(new EventProc() {
                @Override
                public void proc(WindowBase window) {
                    window.opaqueChanged(newValue);
                }
            });
        }

        public void opacityChanged(WindowBase source, final int index) {
            WindowTreeNode node = this.getRoot();
            if (node.animation != null) {
                node.animation.setAlpha(ALPHA_VALUES[index]);
            }
            node.routeEvent(new EventProc() {
                @Override
                public void proc(WindowBase window) {
                    window.opacityChanged(index);
                }
            });
        }
    }

    /**
     * 親ウィンドウ用のコンストラクタ
     */
    public WindowBase() {

    }

    /**
     * 子ウィンドウ用のコンストラクタ
     * @param menuItem
     */
    public WindowBase(MenuItem menuItem) {
        this.menuItem = menuItem;
        this.menuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean open = WindowBase.this.menuItem.getSelection();
                if (open) {
                    WindowBase.this.open();
                    WindowBase.this.shell.setActive();
                }
                else {
                    WindowBase.this.setVisible(false);
                }
            }
        });
    }

    /**
     *　shellを作成。open()などから呼び出す
     * @param display shellの引数
     * @param style shellの引数
     */
    protected void createContents(Display display, int style, boolean menuCascade) {
        if (this.shell == null) {
            this.shell = new Shell(display, style);
            this.createContents(menuCascade);
        }
    }

    /**
     *　shellを作成。open()などから呼び出す
     * @param display shellの引数
     * @param style shellの引数
     */
    protected void createContents(WindowBase parent, int style, boolean menuCascade) {
        if (this.shell == null) {
            this.parent = parent;
            this.shell = new Shell(parent.shell, style);
            this.createContents(menuCascade);
        }
    }

    /**
     * ウィンドウの右クリックメニューの登録
     * これを呼び出した時点でウィンドウに載っているすべてのオブジェクトに右クリックメニューを設定する
     */
    protected void registerEvents() {
        setMenu(this.shell, this.alphamenu);
        this.shell.addListener(SWT.Dispose, new Listener() {
            @Override
            public void handleEvent(Event event) {
                // setMenuされたメニューしかdisposeされないので
                WindowBase.this.alphamenu.dispose();
            }
        });
        if (this.menuItem != null) {
            // 閉じたときにメニューを連動させる
            this.shell.addShellListener(new ShellAdapter() {
                @Override
                public void shellClosed(ShellEvent e) {
                    e.doit = false;
                    // 閉じる前に位置を記憶
                    WindowBase.this.save();
                    WindowBase.this.menuItem.setSelection(false);
                    WindowBase.this.setVisible(false);
                }
            });
        }
    }

    private static void setMenu(Control c, Menu ma) {
        c.setMenu(ma);
        if (c instanceof Composite) {
            for (final Control cc : ((Composite) c).getChildren()) {
                setMenu(cc, ma);
            }
        }
    }

    private boolean isMouseHovering() {
        if (this.shell.getVisible() == false)
            return false;
        Point cursol = WindowBase.this.shell.getDisplay().getCursorLocation();
        Rectangle bounds = this.shell.getBounds();
        //System.out.println(cursol.toString() + "-" + bounds.toString());
        return ((cursol.x >= bounds.x) && (cursol.x < (bounds.x + bounds.width))
                && (cursol.y >= bounds.y) && (cursol.y < (bounds.y + bounds.height)));
    }

    private void opaqueChanged(boolean newValue) {
        this.opaqueItem.setSelection(newValue);
    }

    private void opacityChanged(int newValue) {
        for (int i = 0; i < ALPHA_VALUES.length; ++i) {
            if (newValue != i) {
                this.opacity[i].setSelection(false);
            }
        }
        this.opacity[newValue].setSelection(true);
        this.opacityIndex = newValue;
    }

    private void createContents(boolean cascade) {
        // ウィンドウ基本メニュー
        this.alphamenu = new Menu(this.shell);
        Menu rootMenu = this.alphamenu;
        if (cascade) {
            MenuItem rootItem = new MenuItem(this.alphamenu, SWT.CASCADE);
            rootMenu = new Menu(this.shell, SWT.DROP_DOWN);
            rootItem.setMenu(rootMenu);
            rootItem.setText("ウィンドウ");
        }

        // マウスで不透明化
        this.opaqueItem = new MenuItem(rootMenu, SWT.CHECK);
        this.opaqueItem.setText("マウスで不透明化");
        this.opaqueItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent paramSelectionEvent) {
                WindowBase.this.treeNode.opaqueChanged(WindowBase.this);
            }
        });
        // 他のウィンドウと設定共有
        this.shareOpacityItem = new MenuItem(rootMenu, SWT.CHECK);
        if (this.parent == null) {
            this.shareOpacityItem.setText("他のウィンドウと設定共有");
        }
        else {
            this.shareOpacityItem.setText("親ウィンドウと設定共有");
        }
        this.shareOpacityItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent paramSelectionEvent) {
                boolean newValue = WindowBase.this.shareOpacityItem.getSelection();
                if (WindowBase.this.shareOpacitySetting != newValue) {
                    WindowBase.this.shareOpacitySetting = newValue;
                    if (newValue) {
                        if (WindowBase.this.parent == null) {
                            globalRootNode.addChild(WindowBase.this.treeNode);
                        }
                        else {
                            WindowBase.this.parent.treeNode.addChild(WindowBase.this.treeNode);
                        }
                    }
                    else {
                        if (WindowBase.this.parent == null) {
                            globalRootNode.removeChild(WindowBase.this.treeNode);
                        }
                        else {
                            WindowBase.this.parent.treeNode.removeChild(WindowBase.this.treeNode);
                        }
                    }
                }
            }
        });
        // 透明度
        this.opacity = new MenuItem[ALPHA_VALUES.length];
        for (int i = 0; i < this.opacity.length; ++i) {
            final int index = i;
            this.opacity[i] = new MenuItem(rootMenu, SWT.CHECK);
            this.opacity[i].setText(ALPHA_TEXT[i]);
            this.opacity[i].addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent paramSelectionEvent) {
                    WindowBase.this.treeNode.opacityChanged(WindowBase.this, index);
                }
            });
        }

        // 初期状態を設定
        if (this.config != null) {
            this.restoreSetting();
        }
        // TODO:
        this.opacity[this.opacityIndex].setSelection(true);
        this.shareOpacityItem.setSelection(this.shareOpacitySetting);

        // アニメーション初期化
        if (this.shareOpacitySetting) {
            globalObserver.register(this);
        }
        else {
            this.initAnimation();
        }
    }

    private boolean restoreSetting() {
        if (this.shell == null) { // コンポーネントが作られていない場合は何もしない
            return false;
        }
        // ウインドウ位置
        int locationX = this.config.getLocationX();
        int locationY = this.config.getLocationY();
        if ((locationX != -1) && (locationY != -1)) {
            this.shell.setLocation(new Point(locationX, locationY));
        }
        // 透過設定
        boolean changeAnimation = (this.shareOpacitySetting != this.config.isShareOpacitySetting());
        this.shareOpacitySetting = this.config.isShareOpacitySetting();
        this.shareOpacityItem.setSelection(this.shareOpacitySetting);
        this.opaqueChanged(this.config.isMouseHoveringAware());
        this.opacityChanged(this.config.getOpacityIndex());
        // ウィンドウサイズ
        if (this.shouldSaveWindowSize()) {
            Point size = new Point(this.config.getWidth(), this.config.getHeight());
            if ((size.x == -1) || (size.y == -1)) {
                size = this.getDefaultSize();
            }
            this.shell.setSize(size);
        }

        return changeAnimation;
    }

    /**
     * 状態を復元。必要に応じてopen()を呼び出す
     */
    public void restore() {
        this.getWindowConfig();
        if (this.restoreSetting()) {
            // アニメーションに反映
            // 設定を共有していた場合は解除
            if (this.opacityAnimation == null) {
                globalObserver.deregister(this, false);
            }
            if (this.shareOpacitySetting) {
                globalObserver.register(this);
            }
            else {
                this.initAnimation();
            }
        }
        if (this.config.isOpened()) {
            this.open();
        }
    }

    /**
     * ウィンドウを表示するのに必要なことを全て行ってから表示する。複数回呼ばれることもある
     * デフォルト実装は createContents(shell, int) -> registerEvents() を呼び出してから
     * setWindowInitialized(true) して setVisible(true) する
     * 基底クラスはオーバーライドして自分のクラス用のメソッドを記述すること
     * 基底クラスのopen()を呼ぶ必要はない
     */
    public void open() {
        if (!this.isWindowInitialized()) {
            this.createContents(Display.getDefault(), SWT.NONE, false);
            this.registerEvents();
            this.setWindowInitialized(true);
        }
        this.setVisible(true);
    }

    public void setVisible(boolean visible) {
        if (this.shell.getVisible() != visible) {
            if (this.menuItem != null) {
                this.menuItem.setSelection(visible);
            }
            this.shell.setVisible(visible);
        }
    }

    /**
     * @return shell
     */
    public Shell getShell() {
        return this.shell;
    }

    public Menu getMenu() {
        return this.alphamenu;
    }

    public MenuItem getMenuItem() {
        return this.menuItem;
    }

    /**
     * 状態を保存
     */
    public void save() {
        this.getWindowConfig();
        boolean opened = this.shell.getVisible();
        this.config.setOpened(opened);
        if (opened && !this.shell.getMaximized()) { // 最大化されているときは記憶しない
            Point location = this.shell.getLocation();
            this.config.setLocationX(location.x);
            this.config.setLocationY(location.y);
            if (this.opacityAnimation != null) {
                this.config.setMouseHoveringAware(this.opacityAnimation.getHoverAware());
            }
            else {
                this.config.setMouseHoveringAware(globalObserver.getMouseHoverAware());
            }
            this.config.setShareOpacitySetting(this.shareOpacitySetting);
            this.config.setOpacityIndex(this.opacityIndex);
            if (this.shouldSaveWindowSize()) {
                Point size = this.shell.getSize();
                this.config.setWidth(size.x);
                this.config.setHeight(size.y);
            }
        }
        AppConfig.get().getWindowConfigMap().put(this.getWindowId(), this.config);
    }

    /**
     * 画面からはみ出している場合画面内に戻す
     */
    public void moveIntoDisplay() {
        if (this.shell.getVisible()) {
            Rectangle displayRect = this.shell.getDisplay().getClientArea();
            // ディスプレイサイズより大きい時は小さくする
            Rectangle windowRect = this.shell.getBounds();
            if (displayRect.width < windowRect.width) {
                windowRect.width = displayRect.width;
            }
            if (displayRect.height < windowRect.height) {
                windowRect.height = displayRect.height;
            }
            // 左から出ている？
            if (windowRect.x < displayRect.x) {
                windowRect.x = displayRect.x;
            }
            // 上から出ている？
            if (windowRect.y < displayRect.y) {
                windowRect.y = displayRect.y;
            }
            // 右から出ている？
            if ((windowRect.x + windowRect.width) > (displayRect.width + displayRect.x)) {
                windowRect.x = (displayRect.width + displayRect.x) - windowRect.width;
            }
            // 下から出ている？
            if ((windowRect.y + windowRect.height) > (displayRect.height + displayRect.y)) {
                windowRect.y = (displayRect.height + displayRect.y) - windowRect.height;
            }
            if (!windowRect.equals(this.shell.getBounds())) {
                this.shell.setBounds(windowRect);
            }
        }
    }

    public WindowConfigBean getWindowConfig() {
        if (this.config == null) {
            this.config = AppConfig.get().getWindowConfigMap().get(this.getWindowId());
            if (this.config == null) {
                this.config = new WindowConfigBean();
            }
        }
        return this.config;
    }

    /**
     * ウィンドウ識別ID（デフォルト実装はクラス名フルパス）
     * @return ウィンドウ識別ID
     */
    public String getWindowId() {
        return this.getClass().getName();
    }

    /**
     * ウィンドウサイズを保存・リストアするべきか？
     * @return
     */
    protected boolean shouldSaveWindowSize() {
        return false;
    }

    /**
     * ウィンドウのデフォルトサイズを取得
     * @return
     */
    protected Point getDefaultSize() {
        return new Point(100, 100);
    }

    /**
     * @return windowInitialized
     */
    protected boolean isWindowInitialized() {
        return this.windowInitialized;
    }

    /**
     * @param windowInitialized セットする windowInitialized
     */
    protected void setWindowInitialized(boolean windowInitialized) {
        this.windowInitialized = windowInitialized;
    }
}
