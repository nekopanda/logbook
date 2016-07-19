/**
 * 
 */
package logbook.gui;

import java.util.BitSet;
import java.util.List;

import logbook.config.AppConfig;
import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.dto.DockDto;
import logbook.dto.ShipDto;
import logbook.gui.logic.WindowListener;
import logbook.gui.widgets.FleetComposite;
import logbook.util.SwtUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * @author Nekopanda
 *
 */
public class FleetWindow extends WindowBase {

    private final int fleetid;

    private final Shell parent;
    /** タブ */
    private final CTabFolder tabFolder;

    private CTabItem tabItem;

    private FleetComposite tabComposite;

    private FleetComposite windowComposite;

    private Menu tabMenu;

    private String titleName;

    public FleetWindow(Shell parent, MenuItem menuItem, CTabFolder tabFolder, int fleetid) {
        super(menuItem);
        this.parent = parent;
        this.tabFolder = tabFolder;
        this.fleetid = fleetid;
    }

    /**
     * Open the dialog.
     */
    @Override
    public void open() {
        // 初期化済みの場合
        if (this.isWindowInitialized()) {
            this.setVisible(true);
            return;
        }
        this.createContents();
        this.registerEvents();
        this.setWindowInitialized(true);
        this.setVisible(true);
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        // シェルを作成
        super.createContents(this.parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE, false);
        this.getShell().setLayout(new FillLayout());
        this.windowComposite = new FleetComposite(this.getShell());
        DockDto dock = GlobalContext.getDock(String.valueOf(this.fleetid));
        if (dock == null) {
            this.titleName = "第" + this.fleetid + "艦隊";
        }
        else {
            this.titleName = dock.getName();
        }
        this.getShell().setText(this.titleName);

        this.addWindowListener(new WindowListener() {
            @Override
            public void windowShown() {
                if (FleetWindow.this.tabItem != null) {
                    FleetWindow.this.tabItem.dispose();
                    FleetWindow.this.tabItem = null;
                }
            }

            @Override
            public void windowHidden() {
            }
        });
    }

    /**
     * ウィンドウ識別ID（デフォルト実装はクラス名フルパス）
     * @return ウィンドウ識別ID
     */
    @Override
    public String getWindowId() {
        return this.getClass().getName() + this.fleetid;
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
     * ウィンドウがドラックで動かすことができるか？
     * trueにすると"disable-drag-move"が設定されていないコントロールを動かすとウィンドウが動くようになる
     * @return
     */
    @Override
    protected boolean moveWithDrag() {
        return true;
    }

    /**
     * ウィンドウのデフォルトサイズを取得
     * @return
     */
    @Override
    protected Point getDefaultSize() {
        return SwtUtils.DPIAwareSize(new Point(280, 350));
    }

    public void updateFleet(boolean combinedFleetBadlyDamaed, List<ShipDto> badlyDamaged) {
        if ((this.getShell() != null) && this.getShell().isDisposed()) {
            return;
        }

        DockDto dock = GlobalContext.getDock(String.valueOf(this.fleetid));
        if (dock != null) {
            if ((this.getShell() != null) && this.getVisible()) {
                this.windowComposite.updateFleet(dock,
                        (this.fleetid <= 2) ? combinedFleetBadlyDamaed : false, badlyDamaged);

                if (!dock.getName().equals(this.titleName)) {
                    this.titleName = dock.getName();
                    this.getShell().setText(this.titleName);
                }
            }
            else {
                if (this.tabItem == null) {

                    // 挿入位置を計算
                    CTabItem[] tabItems = this.tabFolder.getItems();
                    int insertPosition = tabItems.length;
                    for (int i = 0; i < tabItems.length; ++i) {
                        Object data = tabItems[i].getData();
                        if (data instanceof FleetWindow) {
                            FleetWindow fw = (FleetWindow) data;
                            if (this.fleetid < fw.getFleetid()) {
                                insertPosition = i;
                                break;
                            }
                        }
                    }

                    this.tabItem = new CTabItem(this.tabFolder, SWT.NONE, insertPosition);
                    this.tabItem.setText(dock.getName());

                    // メインコンポジット
                    this.tabComposite = new FleetComposite(this.tabFolder);
                    this.tabItem.setControl(this.tabComposite);
                    this.tabItem.setData(this);

                    // メニュー
                    this.tabMenu = new Menu(this.tabFolder.getShell(), SWT.POP_UP);
                    MenuItem floatWin = new MenuItem(this.tabMenu, SWT.PUSH);
                    floatWin.setText("切り離し");
                    floatWin.addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(SelectionEvent e) {
                            FleetWindow.this.open();
                        }
                    });
                    this.tabItem.addListener(SWT.Dispose, new Listener() {
                        @Override
                        public void handleEvent(Event event) {
                            FleetWindow.this.tabMenu.dispose();
                        }
                    });
                }
                this.tabComposite.updateFleet(dock,
                        (this.fleetid <= 2) ? combinedFleetBadlyDamaed : false, badlyDamaged);

                if (!dock.getName().equals(this.titleName)) {
                    this.titleName = dock.getName();
                    this.tabItem.setText(dock.getName());
                }

                this.updateTabIcon();
            }

            dock.setUpdate(false);
        }
    }

    /**
     * 艦隊タブのアイコンを更新します
     */
    private void updateTabIcon() {
        BitSet state = this.tabComposite.getState();
        if (state.get(FleetComposite.FATAL)) {
            this.tabItem.setImage(SWTResourceManager.getImage(FleetComposite.class,
                    AppConfig.get().isMonoIcon()
                            ? AppConstants.R_ICON_EXCLAMATION_MONO
                            : AppConstants.R_ICON_EXCLAMATION));
        } else if (state.get(FleetComposite.WARN)) {
            this.tabItem.setImage(SWTResourceManager.getImage(FleetComposite.class,
                    AppConfig.get().isMonoIcon()
                            ? AppConstants.R_ICON_ERROR_MONO
                            : AppConstants.R_ICON_ERROR));
        } else {
            this.tabItem.setImage(null);
        }
    }

    public void showTabMenu() {
        if (this.tabMenu != null) {
            this.tabMenu.setVisible(true);
        }
    }

    /**
     * @return fleetid
     */
    public int getFleetid() {
        return this.fleetid;
    }
}
