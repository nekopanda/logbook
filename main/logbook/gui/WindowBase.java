/**
 * 
 */
package logbook.gui;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import logbook.config.AppConfig;
import logbook.config.bean.WindowConfigBean;
import logbook.constants.AppConstants;
import logbook.gui.logic.OpacityAnimation;
import logbook.gui.logic.OpacityAnimationClient;
import logbook.gui.logic.WindowListener;
import logbook.gui.logic.WindowNativeSupport;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
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
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * @author Nekopanda
 * 位置記憶、透明処理などの基本処理を実装
 */
public class WindowBase {

    //　定数
    private static int[] ALPHA_VALUES = new int[] { 255, 230, 205, 154, 102, 26 };
    private static String[] ALPHA_TEXT = new String[] { "不透明化", "90%", "80%", "60%", "40%", "10%" };

    private static WindowTreeNode globalRootNode = new WindowTreeNode(true);
    public final static WindowNativeSupport nativeService = new WindowNativeSupport();

    /** アクティブになったウィンドウ順 */
    private final static Set<WindowBase> activatedWindows = new LinkedHashSet<>();

    public static Set<WindowBase> getActivatedWindows() {
        return activatedWindows;
    }

    private final WindowTreeNode treeNode = new WindowTreeNode(this);
    private final boolean noMenubar = AppConfig.get().isNoMenubar();
    private final boolean disableWindowMenu = AppConfig.get().isDisableWindowMenu();
    // アプリ上の仮想的な親子関係
    private WindowBase parent;
    private Shell shell;
    private Menu menubar;
    private Menu popupmenu;
    private MenuItem menuItem;

    private DragMoveEventHandler dragMoveHandler;

    private boolean windowInitialized;
    // ウィンドウが開いた状態か？
    // Mainウィンドウの最小化に連動して setVisible するため
    private boolean topMost = false;
    private boolean showTitlebar = true;

    // shell.getVisible() は最小化状態だとfalseを返すので独自の状態を持つ
    private boolean minimized = false;

    // メニュー
    private MenuItem topMostItem;
    private MenuItem showTitlebarItem;
    private MenuItem opaqueItem;
    private MenuItem shareOpacityItem;
    private MenuItem[] opacity;
    private MenuItem closeItem;

    // 設定
    private WindowConfigBean config;
    private boolean shareOpacitySetting = false;
    private int opacityIndex = 0;

    // イベントリスナ
    private final List<WindowListener> windowListeners = new ArrayList<>();

    private static interface EventProc {
        void proc(WindowBase window);
    }

    private static class WindowTreeNode implements OpacityAnimationClient {
        private WindowTreeNode parent;
        private final List<WindowTreeNode> childs = new ArrayList<WindowTreeNode>();
        private OpacityAnimation animation;
        public WindowBase window;
        private boolean enabled;
        private boolean hasWindow;

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
                    if (this.parent != null) {
                        this.parent.enable();
                    }
                    this.enable();
                }
                else {
                    if (this.animation != null) {
                        this.dbgprint("this.animation.setEnabled(false);");
                        this.animation.setEnabled(false);
                    }
                    this.hasWindow = false;
                }
            }
        }

        public void addChild(WindowTreeNode node) {
            node.dbgprint("addChild");
            this.childs.add(node);
            node.parent = this;
            node.hasWindow = false;
            if (node.enabled) {
                node.transferMasterSetting();
                this.enable();
            }
        }

        public void removeChild(WindowTreeNode node) {
            node.dbgprint("removeChild");
            this.childs.remove(node);
            node.parent = null;
            if (node.enabled) {
                node.enable();
            }
        }

        public void routeEvent(EventProc proc) {
            if (this.window != null)
                proc.proc(this.window);

            for (WindowTreeNode node : this.childs) {
                node.routeEvent(proc);
            }
        }

        private void enable() {
            if (this.parent != null) {
                this.transferMasterSetting();
            }
            else {
                // ルートなのでアニメーションを有効化
                if (this.animation == null) {
                    this.dbgprint("new OpacityAnimation(this);");
                    this.animation = new OpacityAnimation(this);
                }
                WindowBase from = this.getFirstWindow();
                if (from == null) {
                    this.dbgprint("this.hasWindow = false");
                    this.hasWindow = false;
                }
                else {
                    boolean hoverAware = from.opaqueItem.getSelection();
                    int alhpa = ALPHA_VALUES[from.opacityIndex];
                    if (this.hasWindow == false) {
                        this.dbgprint("this.animation.setInitialAlpha(" + alhpa + ", " + hoverAware + ");");
                        this.animation.setInitialAlpha(alhpa, hoverAware);
                        this.hasWindow = true;
                    }
                }
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
            this.dbgprint("transferMasterSetting()");
            if (this.window != null) {
                WindowTreeNode root = this.getRoot();
                WindowBase from = root.getFirstWindow();
                if ((from != null) && (from != this.window)) {
                    boolean hoverAware = from.opaqueItem.getSelection();
                    this.window.opaqueChanged(hoverAware);
                    this.window.opacityChanged(from.opacityIndex);
                    this.dbgprint("this.window.shell.setAlpha(alhpa);");
                    if (root.animation != null) {
                        this.window.shell.setAlpha(root.animation.getCurrentAlpha());
                    }
                    else {
                        int alhpa = ALPHA_VALUES[from.opacityIndex];
                        this.window.shell.setAlpha(alhpa);
                    }
                    if (this.animation != null) {
                        this.animation.setEnabled(false);
                    }
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
            this.dbgprint("setAlpha(" + newAlpha + ")");
            if (this.window != null) {
                this.window.shell.setAlpha(newAlpha);
            }
            for (WindowTreeNode child : this.childs) {
                child.setAlpha(newAlpha);
            }
        }

        public void topMostChanged(WindowBase source) {
            final boolean newValue = source.topMostItem.getSelection();
            WindowTreeNode node = this.getRoot();
            node.routeEvent(new EventProc() {
                @Override
                public void proc(WindowBase window) {
                    window.topMostChanged(newValue);
                }
            });
            if (newValue == false) {
                // 他のウィンドウがつられて非最前面になっているので、再設定する
                startUpdateTopMost(source.getShell());
            }
        }

        public void showTitlebarChanged(WindowBase source) {
            final boolean newValue = source.showTitlebarItem.getSelection();
            WindowTreeNode node = this.getRoot();
            node.routeEvent(new EventProc() {
                @Override
                public void proc(WindowBase window) {
                    window.showTitlebarChanged(newValue);
                }
            });
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

        private void dbgprint(String text) {
            //String wid = (this.window != null) ? this.window.getWindowId() : "null";
            //System.out.println("[" + wid + "] " + text);
        }
    }

    private static class DragMoveEventHandler implements MouseListener, MouseMoveListener {
        private final Shell shell;
        private boolean blnMouseDown = false;
        private final Point pos = new Point(0, 0);

        public DragMoveEventHandler(Shell shell) {
            this.shell = shell;
        }

        @Override
        public void mouseUp(MouseEvent arg0) {
            this.blnMouseDown = false;
        }

        @Override
        public void mouseDown(MouseEvent e) {
            if (e.button == 1) { // 左クリックのみ
                this.blnMouseDown = true;
                this.pos.x = e.x;
                this.pos.y = e.y;
            }
        }

        @Override
        public void mouseDoubleClick(MouseEvent arg0) {
        }

        @Override
        public void mouseMove(MouseEvent e) {
            if (this.blnMouseDown) {
                Point loc = this.shell.getLocation();
                this.shell.setLocation(loc.x + (e.x - this.pos.x), loc.y + (e.y - this.pos.y));
            }
        }
    }

    public static boolean isCommonTopMostEnabled() {
        return (nativeService.isTopMostAvailable() == false) && // Windows
                !"gtk".equals(SWT.getPlatform()); // gtkでON_TOPするとフレームが表示されない
    }

    /**
     * 親ウィンドウ用のコンストラクタ
     */
    public WindowBase() {

    }

    void dispose() {
        if (this.shell != null) {
            this.shell.dispose();
        }
    }

    /**
     * 子ウィンドウ用のコンストラクタ
     * @param menuItem
     */
    public WindowBase(MenuItem menuItem) {
        if (menuItem != null) {
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
                        WindowBase.this.hideWindow();
                    }
                }
            });
        }
    }

    public void addWindowListener(WindowListener listener) {
        this.windowListeners.add(listener);
    }

    public void removeWindowListener(WindowListener listener) {
        this.windowListeners.remove(listener);
    }

    public static void setDataToAllChild(Control c, String key, Object data) {
        c.setData(key, data);
        if (c instanceof Composite) {
            for (final Control cc : ((Composite) c).getChildren()) {
                setDataToAllChild(cc, key, data);
            }
        }
    }

    private int getDefaultStyle() {
        if (isCommonTopMostEnabled() && AppConfig.get().isOnTop()) {
            return SWT.ON_TOP;
        }
        return 0;
    }

    /**
     *　shellを作成。open()などから呼び出す
     * @param display shellの引数
     * @param style shellの引数
     */
    protected void createContents(Display display, int style, boolean menuCascade) {
        if (this.shell == null) {
            this.shell = new Shell(display, style | this.getDefaultStyle());
            this.createContents(menuCascade);
        }
    }

    /**
     *　shellを作成。open()などから呼び出す
     * @param display shellの引数
     * @param style shellの引数
     */
    protected void createContents(Shell shell, int style, boolean menuCascade) {
        if (this.shell == null) {
            this.shell = new Shell(shell, style | this.getDefaultStyle());
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
            this.shell = new Shell(parent.shell, style | this.getDefaultStyle());
            this.createContents(menuCascade);
        }
    }

    /**
     * ウィンドウの右クリックメニューの登録
     * これを呼び出した時点でウィンドウに載っているすべてのオブジェクトに右クリックメニューを設定する
     */
    protected void registerEvents() {
        setMenu(this.shell, this.popupmenu);
        this.shell.addListener(SWT.Dispose, new Listener() {
            @Override
            public void handleEvent(Event event) {
                // setMenuされたメニューしかdisposeされないので
                WindowBase.this.popupmenu.dispose();
                // アニメーションをオフ
                WindowBase.this.treeNode.setEnabled(false);
                // ツリーから切り離す
                WindowBase.this.shareOpacitySetting = false;
                WindowBase.this.updateTreeNodeState();
                activatedWindows.remove(WindowBase.this);
            }
        });
        // アクティブウィンドウ順検出用
        this.shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellActivated(ShellEvent event) {
                activatedWindows.remove(WindowBase.this);
                activatedWindows.add(WindowBase.this);
            }
        });
        // ドラックでウィンドウ移動
        if (this.moveWithDrag() && AppConfig.get().isEnableMoveWithDD()) { // dragMoveHandler
            this.dragMoveHandler = new DragMoveEventHandler(this.shell);
            this.addMouseListener(this.shell);
        }
        if (this.menuItem != null) {
            // 閉じたときにメニューを連動させる
            this.shell.addShellListener(new ShellAdapter() {
                @Override
                public void shellClosed(ShellEvent e) {
                    e.doit = false;
                    WindowBase.this.hideWindow();
                }
            });
        }
    }

    protected void createMenubar() {
        if (this.noMenubar) {
            if (!this.disableWindowMenu) {
                // ウィンドウメニューがあるときはセパレータだけ追加しておく
                new MenuItem(this.popupmenu, SWT.SEPARATOR);
            }
            return;
        }
        if (this.menubar == null) {
            if (this.shell == null) {
                throw new IllegalStateException("You need to create shell before creating menu bar.");
            }
            this.menubar = new Menu(this.shell, SWT.BAR);
            this.shell.setMenuBar(this.menubar);
        }
    }

    private static void setMenu(Control c, Menu ma) {
        if (c.getData("disable-window-menu") == null) {
            if (c instanceof Composite) {
                for (final Control cc : ((Composite) c).getChildren()) {
                    setMenu(cc, ma);
                }
            }
            if (c.getData("disable-window-menu-this") == null) {
                c.setMenu(ma);
            }
        }
    }

    private void addMouseListener(Control c) {
        if (c.getData("disable-drag-move") == null) {
            if (c instanceof Composite) {
                for (final Control cc : ((Composite) c).getChildren()) {
                    this.addMouseListener(cc);
                }
            }
            if (c.getData("disable-drag-move-this") == null) {
                c.addMouseListener(this.dragMoveHandler);
                c.addMouseMoveListener(this.dragMoveHandler);
            }
        }
    }

    public void hideWindow() {
        // 閉じる前に位置を記憶
        this.save();
        if (this.menuItem != null) {
            this.menuItem.setSelection(false);
        }
        this.setVisible(false);
    }

    private void topMostChanged(boolean topMost) {
        this.topMostItem.setSelection(topMost);
        if (this.disableWindowMenu) {
            return;
        }
        if (this.topMost != topMost) {
            this.topMost = topMost;
            nativeService.setTopMost(this.getShell(), topMost);
        }
    }

    private static void updateTopMost(Shell shell) {
        WindowBase window = (WindowBase) shell.getData();
        if ((window != null) && window.topMost) {
            // TopMostだったら設定して終わり
            nativeService.setTopMost(window.getShell(), window.topMost);
        }
        else {
            // 子Shellを見ていく
            for (Shell s : shell.getShells()) {
                updateTopMost(s);
            }
        }
    }

    private static void startUpdateTopMost(Shell shell) {
        for (Shell s : shell.getDisplay().getShells()) {
            if (s.getParent() == null) {
                // root shell
                updateTopMost(s);
            }
        }
    }

    /**
     * @param newValue
     */
    protected void showTitlebarChanged(boolean showTitlebar) {
        this.showTitlebarItem.setSelection(showTitlebar);
        if (this.disableWindowMenu) {
            return;
        }
        if (this.showTitlebar != showTitlebar) {
            this.showTitlebar = showTitlebar;
            nativeService.toggleTitlebar(this.getShell(), showTitlebar);
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

    private void updateTreeNodeState() {
        if (this.shareOpacitySetting) {
            if (this.parent == null) {
                globalRootNode.addChild(this.treeNode);
            }
            else {
                this.parent.treeNode.addChild(this.treeNode);
            }
        }
        else {
            if (this.parent == null) {
                globalRootNode.removeChild(this.treeNode);
            }
            else {
                this.parent.treeNode.removeChild(this.treeNode);
            }
        }
    }

    private void createWindowMenu(Menu rootMenu) {
        if (nativeService.isTopMostAvailable()) {
            // 最前面に表示する
            this.topMostItem = new MenuItem(rootMenu, SWT.CHECK);
            this.topMostItem.setText("最前面に表示する");
            this.topMostItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent paramSelectionEvent) {
                    WindowBase.this.treeNode.topMostChanged(WindowBase.this);
                }
            });

            //if (this.moveWithDrag()) {
            // タイトルバーを表示にする
            this.showTitlebarItem = new MenuItem(rootMenu, SWT.CHECK);
            this.showTitlebarItem.setText("タイトルバーを表示する");
            this.showTitlebarItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent paramSelectionEvent) {
                    WindowBase.this.treeNode.showTitlebarChanged(WindowBase.this);
                }
            });
            //}
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
                    WindowBase.this.updateTreeNodeState();
                }
            }
        });
        // セパレータ
        new MenuItem(rootMenu, SWT.SEPARATOR);
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
        if (this.menuItem != null) {
            // セパレータ
            new MenuItem(rootMenu, SWT.SEPARATOR);
            // ウィンドウを閉じる
            this.closeItem = new MenuItem(rootMenu, SWT.NONE);
            this.closeItem.setText("ウィンドウを閉じる");
            this.closeItem.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent paramSelectionEvent) {
                    WindowBase.this.hideWindow();
                }
            });
        }
    }

    private void createContents(boolean cascade) {
        this.shell.setData(this);
        this.shell.setImage(SWTResourceManager.getImage(WindowBase.class, AppConstants.LOGO));
        // ウィンドウ基本メニュー
        this.popupmenu = new Menu(this.shell);
        Menu rootMenu = this.popupmenu;

        if (this.disableWindowMenu) {
            // ダミーメニューを作る
            final Menu dummy = rootMenu = new Menu(this.shell, SWT.DROP_DOWN);
            this.shell.addListener(SWT.Dispose, new Listener() {
                @Override
                public void handleEvent(Event event) {
                    dummy.dispose();
                }
            });
        }
        else if (cascade) {
            MenuItem rootItem = new MenuItem(this.popupmenu, SWT.CASCADE);
            rootMenu = new Menu(this.shell, SWT.DROP_DOWN);
            rootItem.setMenu(rootMenu);
            rootItem.setText("ウィンドウ");
        }

        this.createWindowMenu(rootMenu);

        // 初期状態を設定
        if (this.parent != null) {
            this.shareOpacitySetting = true;
        }
        if (this.config != null) {
            this.restoreSetting();
        }
        this.opacity[this.opacityIndex].setSelection(true);
        this.shareOpacityItem.setSelection(this.shareOpacitySetting);

        this.dbgprint("before setEnabled(true) alpha=" + this.shell.getAlpha());
        this.dbgprint("after setEnabled(true) alpha=" + this.shell.getAlpha());
        this.updateTreeNodeState();
        this.treeNode.setEnabled(true);
        this.dbgprint("after updateTreeNodeState() alpha=" + this.shell.getAlpha());
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
        // 最前面に表示
        if (nativeService.isTopMostAvailable()) {
            this.topMostChanged(this.config.isTopMost());
            //if (this.moveWithDrag()) {
            this.showTitlebarChanged(this.config.isShowTitlebar());
            //}
        }
        // 透過設定
        boolean changeAnimation = false;
        changeAnimation = (this.shareOpacitySetting != this.config.isShareOpacitySetting());
        this.shareOpacitySetting = this.config.isShareOpacitySetting();
        this.shareOpacityItem.setSelection(this.shareOpacitySetting);
        this.opaqueChanged(this.config.isMouseHoveringAware());
        this.opacityChanged(this.config.getOpacityIndex());
        // ウィンドウサイズ
        if (this.shouldSaveWindowSize()) {
            this.shell.setSize(this.getRestoreSize());
        }

        return changeAnimation;
    }

    public void setBehindTo(WindowBase behindTo) {
        if (behindTo.shell.isDisposed() || this.shell.isDisposed()) {
            return;
        }
        if (nativeService.isTopMostAvailable()) {
            if (behindTo.topMostItem.getSelection() == this.topMostItem.getSelection()) {
                // 同じ場合だけbehindToをセットする
                nativeService.setBehindTo(this.shell, behindTo.shell);
            }
            else {
                nativeService.setBehindTo(this.shell, null);
            }
        }
    }

    /** parent is un-minimized */
    public void shellDeiconified() {
        if ((this.menuItem != null) && this.menuItem.getSelection() && this.minimized) {
            this.minimized = false;
            this.shell.setVisible(true);
        }
    }

    /** parent is minimized */
    public void shellIconified() {
        if ((this.menuItem != null) && this.menuItem.getSelection() && this.shell.getVisible()) {
            this.minimized = true;
            this.shell.setVisible(false);
        }
    }

    public Point getRestoreSize() {
        this.getWindowConfig();
        Point size = new Point(this.config.getWidth(), this.config.getHeight());
        if ((size.x == -1) || (size.y == -1)) {
            size = this.getDefaultSize();
        }
        return size;
    }

    /**
     * 状態を復元。必要に応じてopen()を呼び出す
     */
    public void restore() {
        this.getWindowConfig();
        if (this.shell != null) {
            this.dbgprint("disable");
            this.treeNode.setEnabled(false);
            if (this.restoreSetting()) {
                this.updateTreeNodeState();
            }
            this.dbgprint("enable");
            this.treeNode.setEnabled(true);
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
            // 関連付けられたメニューがある場合はメニューも連動させる
            if (this.menuItem != null) {
                this.menuItem.setSelection(visible);
            }
            if (visible) {
                activatedWindows.remove(this);
                activatedWindows.add(this);
            }
            this.dbgprint("setVisible alpha=" + this.shell.getAlpha());
            this.shell.setVisible(visible);
            // イベント発行
            for (WindowListener listener : this.windowListeners) {
                if (visible) {
                    listener.windowShown();
                }
                else {
                    listener.windowHidden();
                }
            }
        }
    }

    public boolean getVisible() {
        return this.minimized || this.shell.getVisible();
    }

    public Shell getActualParent() {
        if (this.shell.getParent() == null)
            return null;
        return this.shell.getParent().getShell();
    }

    /**
     * @return shell
     */
    public Shell getShell() {
        return this.shell;
    }

    public Menu getMenubar() {
        if (this.noMenubar) {
            return this.popupmenu;
        }
        return this.menubar;
    }

    public Menu getPopupMenu() {
        return this.popupmenu;
    }

    public MenuItem getMenuItem() {
        return this.menuItem;
    }

    /**
     * 状態を保存
     */
    public void save() {
        if (this.shell != null) { // ウィンドウが作られていないときは何もしない
            this.getWindowConfig();
            boolean opened = this.shell.getVisible();
            this.config.setOpened(opened);
            if (opened && !this.shell.getMaximized()) { // 最大化されているときは記憶しない
                Point location = this.shell.getLocation();
                this.config.setLocationX(location.x);
                this.config.setLocationY(location.y);
                // 最前面に表示
                if (nativeService.isTopMostAvailable()) {
                    this.config.setTopMost(this.topMostItem.getSelection());
                    //if (this.moveWithDrag()) {
                    this.config.setShowTitlebar(this.showTitlebarItem.getSelection());
                    //}
                }
                this.config.setMouseHoveringAware(this.treeNode.getMouseHoverAware());
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
    }

    /**
     * 画面からはみ出している場合画面内に戻す
     * 子ウィンドウがある場合は子も戻す
     */
    public void moveIntoDisplay() {
        this.treeNode.routeEvent(new EventProc() {
            @Override
            public void proc(WindowBase window) {
                if ((window.shell != null) && window.shell.getVisible()) {
                    Rectangle displayRect = window.shell.getDisplay().getClientArea();
                    // ディスプレイサイズより大きい時は小さくする
                    Rectangle windowRect = window.shell.getBounds();
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
                    if (!windowRect.equals(window.shell.getBounds())) {
                        window.shell.setBounds(windowRect);
                    }
                }
            }
        });
    }

    public WindowConfigBean getWindowConfig() {
        if (this.config == null) {
            this.config = AppConfig.get().getWindowConfigMap().get(this.getWindowId());
            if (this.config == null) {
                this.config = new WindowConfigBean();
                if (this.parent != null) {
                    this.config.setShareOpacitySetting(true);
                    this.dbgprint("this.config.setShareOpacitySetting(true);");
                }
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
     * ウィンドウがドラックで動かすことができるか？
     * trueにすると"disable-drag-move"が設定されていないコントロールを動かすとウィンドウが動くようになる
     * @return
     */
    protected boolean moveWithDrag() {
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

    private void dbgprint(String text) {
        //System.out.println("[" + this.getWindowId() + "] " + text);
    }

    /**
     * @return noMenubar
     */
    public boolean isNoMenubar() {
        return this.noMenubar;
    }
}
