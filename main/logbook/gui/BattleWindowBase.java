/**
 * 
 */
package logbook.gui;

import java.util.ArrayList;
import java.util.List;

import logbook.config.bean.WindowPositionBean;
import logbook.dto.BattleExDto;
import logbook.dto.DockDto;
import logbook.dto.MapCellDto;
import logbook.dto.ShipDto;
import logbook.dto.ShipInfoDto;
import logbook.gui.logic.LayoutLogic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * @author Nekopanda
 *
 */
public class BattleWindowBase {

    private final Shell shell;

    private final MenuItem menuItem;

    private final WindowPositionBean windowPos;

    private final Font normalFont;
    private final Font boldFont;

    private boolean windowCreated = false;

    // タイトル
    private final String windowText;

    private final List<Label> labelsForCombined = new ArrayList<Label>();
    private boolean combinedMode = false;

    private final ShipDto[] friendShips = new ShipDto[12];
    private final ShipInfoDto[] enemyShips = new ShipInfoDto[6];

    private List<DockDto> docks;
    private MapCellDto mapCellDto;
    private BattleExDto battle;

    /**
     * Create the dialog.
     * @param parent
     */
    public BattleWindowBase(Shell parent, MenuItem menuItem, WindowPositionBean windowPos, String windowText) {
        this.shell = new Shell(parent, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE);
        this.menuItem = menuItem;
        this.windowPos = windowPos;
        this.windowText = windowText;
        this.normalFont = this.getShell().getFont();
        FontData fontData = this.normalFont.getFontData()[0];
        String fontName = fontData.getName();
        int size = fontData.getHeight();
        this.boldFont = SWTResourceManager.getFont(fontName, size, SWT.BOLD);

        this.menuItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean open = BattleWindowBase.this.menuItem.getSelection();
                if (open) {
                    BattleWindowBase.this.open();
                }
                else {
                    BattleWindowBase.this.close();
                }
            }
        });
        this.shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                e.doit = false;
                BattleWindowBase.this.menuItem.setSelection(false);
                BattleWindowBase.this.close();
            }
        });

        int locationX = windowPos.getLocationX();
        int locationY = windowPos.getLocationY();
        if ((locationX != -1) && (locationY != -1)) {
            this.shell.setLocation(new Point(locationX, locationY));
        }
    }

    public void open() {
        if (!this.shell.getVisible()) {
            if (this.windowCreated == false) {
                this.shell.setText(this.windowText);
                this.createContents();
                this.windowCreated = true;
                this.combinedMode = true;
                this.setCombinedMode(false);
                this.getShell().pack();
            }
            this.menuItem.setSelection(true);
            this.shell.open();
            this.updateData(false);
            this.shell.layout();
        }
    }

    public void close() {
        if (this.shell.getVisible()) {
            this.menuItem.setSelection(false);
            this.shell.setVisible(false);
        }
    }

    public WindowPositionBean getWindowPos() {
        boolean opened = this.shell.getVisible();
        this.windowPos.setOpened(opened);
        if (opened) {
            Point location = this.shell.getLocation();
            this.windowPos.setLocationX(location.x);
            this.windowPos.setLocationY(location.y);
        }
        return this.windowPos;
    }

    protected void beginDraw() {
        this.shell.setRedraw(false);
    }

    protected void endDraw() {
        this.shell.layout();
        this.shell.setRedraw(true);
        //this.shell.redraw();
        //this.shell.update();
    }

    protected void beginCombined() {
        this.combinedMode = true;
    }

    protected void endCombined() {
        this.combinedMode = false;
    }

    protected Label addLabel(String text, int width, int align, int horizontalSpan, int verticalSpan) {
        Label label = new Label(this.shell, SWT.NONE);
        if (this.combinedMode) {
            this.labelsForCombined.add(label);
        }
        label.setAlignment(align);
        GridData gd = new GridData(align, SWT.CENTER, false, false, horizontalSpan, verticalSpan);
        gd.widthHint = width;
        label.setLayoutData(gd);
        label.setText(text);
        return label;
    }

    protected Label addLabelWithSize(String text, int width) {
        return this.addLabel(text, width, SWT.CENTER, 1, 1);
    }

    protected Label addLabel(String text) {
        return this.addLabel(text, SWT.DEFAULT, SWT.CENTER, 1, 1);
    }

    protected Label addLabelWithAlign(String text, int align) {
        return this.addLabel(text, SWT.DEFAULT, align, 1, 1);
    }

    protected Label addLabelWithSpan(String text, int horizontalSpan, int verticalSpan) {
        return this.addLabel(text, SWT.DEFAULT, SWT.CENTER, horizontalSpan, verticalSpan);
    }

    protected void addHorizontalSeparator(int span) {
        Label label = new Label(this.shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, span, 1));
        if (this.combinedMode) {
            this.labelsForCombined.add(label);
        }
    }

    protected void addVerticalSeparator(int span) {
        //Label lblsp1 = new Label(this.shell, SWT.SEPARATOR | SWT.VERTICAL);
        //lblsp1.setLayoutData(new GridData(SWT.CENTER, SWT.FILL, false, false, 1, span));
        Label label = new Label(this.shell, SWT.SEPARATOR | SWT.VERTICAL);
        GridData gd = new GridData(SWT.CENTER, SWT.FILL, false, false, 1, span);
        gd.widthHint = 10;
        label.setLayoutData(gd);
        if (this.combinedMode) {
            this.labelsForCombined.add(label);
        }
    }

    protected void skipSlot() {
        Label label = new Label(this.shell, SWT.NONE);
        label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        if (this.combinedMode) {
            this.labelsForCombined.add(label);
        }
    }

    public void endSortie() {
        this.mapCellDto = null;
        this.battle = null;
        this.docks = null;
        for (int i = 0; i < this.friendShips.length; ++i) {
            this.friendShips[i] = null;
        }
        for (int i = 0; i < this.enemyShips.length; ++i) {
            this.enemyShips[i] = null;
        }
        if (this.shell.getVisible()) {
            this.updateData(false);
        }
    }

    public void updateSortieDock(List<DockDto> docks) {
        boolean start = (this.docks == null);
        // 開始！
        this.mapCellDto = null;
        this.battle = null;
        this.docks = docks;
        for (int i = 0; (i < 2) && (i < docks.size()); ++i) {
            List<ShipDto> dockShips = docks.get(i).getShips();
            for (int c = 0; c < dockShips.size(); ++c) {
                this.friendShips[(i * 6) + c] = dockShips.get(c);
            }
        }
        if (this.shell.getVisible()) {
            this.updateData(start);
        }
    }

    public void updateMapCell(MapCellDto data) {
        this.mapCellDto = data;
        if (this.shell.getVisible()) {
            this.updateData(false);
        }
    }

    public void updateBattle(BattleExDto data) {
        this.battle = data;
        List<ShipInfoDto> enemyShips = data.getEnemy();
        for (int i = 0; i < enemyShips.size(); ++i) {
            this.enemyShips[i] = enemyShips.get(i);
        }
        if (this.shell.getVisible()) {
            this.updateData(false);
        }
    }

    protected void setCombinedMode(boolean combined) {
        if (this.combinedMode != combined) {
            for (Label label : this.labelsForCombined) {
                LayoutLogic.hide(label, !combined);
            }
            this.combinedMode = combined;
        }
    }

    protected void createContents() {
    }

    protected void updateData(boolean start) {
        //
    }

    /**
     * @return docks
     */
    protected List<DockDto> getDocks() {
        return this.docks;
    }

    /**
     * @return mapCellDto
     */
    protected MapCellDto getMapCellDto() {
        return this.mapCellDto;
    }

    /**
     * @return battleDto
     */
    protected BattleExDto getBattle() {
        return this.battle;
    }

    /**
     * @return shell
     */
    public Shell getShell() {
        return this.shell;
    }

    /**
     * @return normalFont
     */
    protected Font getNormalFont() {
        return this.normalFont;
    }

    /**
     * @return boldFont
     */
    protected Font getBoldFont() {
        return this.boldFont;
    }

    /**
     * @return friendShips
     */
    protected ShipDto[] getFriendShips() {
        return this.friendShips;
    }

    /**
     * @return enemyShips
     */
    protected ShipInfoDto[] getEnemyShips() {
        return this.enemyShips;
    }
}
