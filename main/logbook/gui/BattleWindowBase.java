/**
 * 
 */
package logbook.gui;

import java.util.ArrayList;
import java.util.List;

import logbook.dto.BattleExDto;
import logbook.dto.DockDto;
import logbook.dto.EnemyShipDto;
import logbook.dto.MapCellDto;
import logbook.dto.ShipDto;
import logbook.gui.logic.LayoutLogic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * @author Nekopanda
 *
 */
public class BattleWindowBase extends WindowBase {

    /** addLabel の対象となる Composite */
    protected Composite currentCompo;

    private final Shell parent;

    private Font normalFont;
    private Font boldFont;

    // タイトル
    private final String windowText;

    /**
     *  連合艦隊の第二艦隊用のラベル
     */
    private final List<Label> labelsForCombined = new ArrayList<Label>();
    /**
     *  最初の表示で大きさを決定するラベルたち
     *  SWT.DEFAULTで追加されたLabelは最初の表示でサイズを決定しその後サイズを変えない
     */
    private final List<Label> fixedSizedLabels = new ArrayList<Label>();
    private boolean combinedMode = false;

    private final ShipDto[] friendShips = new ShipDto[12];
    private final EnemyShipDto[] enemyShips = new EnemyShipDto[6];

    private List<DockDto> docks;
    private MapCellDto mapCellDto;
    private BattleExDto battle;

    /**
     * Create the dialog.
     * @param parent
     */
    public BattleWindowBase(Shell parent, MenuItem menuItem, String windowText) {
        super(menuItem);
        this.parent = parent;
        this.windowText = windowText;
    }

    @Override
    public void open() {
        if (!this.isWindowInitialized()) {
            super.createContents(this.parent, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE, false);
            this.normalFont = this.getShell().getFont();
            FontData fontData = this.normalFont.getFontData()[0];
            String fontName = fontData.getName();
            int size = fontData.getHeight();
            this.boldFont = SWTResourceManager.getFont(fontName, size, SWT.BOLD);
            this.getShell().setText(this.windowText);

            this.currentCompo = this.getShell();
            this.createContents();
            super.registerEvents();
            this.createContentsAfter();
            this.combinedMode = true;
            this.setCombinedMode(false);
            this.getShell().pack();
            this.setWindowInitialized(true);
            this.setVisible(true);

            // 表示後に実行する
            // (表示前にサイズを取得すると正しく取得できないことがある？)
            this.getShell().getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    // 最初の表示で大きさを固定する
                    for (Label label : BattleWindowBase.this.fixedSizedLabels) {
                        Object data = label.getLayoutData();
                        if (data instanceof GridData) {
                            GridData gd = (GridData) data;
                            gd.widthHint = label.getSize().x;
                        }
                    }

                    //        BattleWindowBase.this.clearText();
                    // ウィンドウサイズ復元
                    Point winSize = BattleWindowBase.this.getWindowConfig().getSize();
                    if ((winSize.x != -1) && (winSize.y != -1)) {
                        BattleWindowBase.this.getShell().setSize(winSize);
                    }

                    BattleWindowBase.this.clearText();
                    BattleWindowBase.this.updateData(false);
                }
            });
        }
        else {
            this.clearText();
            this.updateData(false);
            this.setVisible(true);
        }
    }

    /**
     * 
     */
    protected void createContentsAfter() {
    }

    /**
     * ウィンドウサイズを保存・リストアするべきか？
     * @return
     */
    @Override
    protected boolean shouldSaveWindowSize() {
        return true;
    }

    @Override
    protected boolean moveWithDrag() {
        return true;
    }

    @Override
    protected void showTitlebarChanged(boolean newValue) {
        super.showTitlebarChanged(newValue);
        this.getShell().pack();
    }

    protected void beginDraw() {
        this.getShell().setRedraw(false);
    }

    protected void endDraw() {
        this.getShell().layout();
        this.getShell().setRedraw(true);
        //this.getShell().redraw();
        //this.getShell().update();
    }

    protected void beginCombined() {
        this.combinedMode = true;
    }

    protected void endCombined() {
        this.combinedMode = false;
    }

    protected Label addLabel(String text, int width, int textalign, int align, boolean excess,
            int horizontalSpan, int verticalSpan) {
        Label label = new Label(this.currentCompo, SWT.NONE);
        if (this.combinedMode) {
            this.labelsForCombined.add(label);
        }
        label.setAlignment(textalign);
        GridData gd = new GridData(align, SWT.CENTER, excess, false, horizontalSpan, verticalSpan);
        gd.widthHint = width;
        label.setLayoutData(gd);
        label.setText(text);
        if (width == SWT.DEFAULT) {
            this.fixedSizedLabels.add(label);
        }
        return label;
    }

    protected Label addLabel(String text, int width, int align, int horizontalSpan, int verticalSpan) {
        return this.addLabel(text, width, align, align, true, horizontalSpan, verticalSpan);
    }

    protected Label addLabel(String text, int align, int width) {
        return this.addLabel(text, width, align, SWT.FILL, true, 1, 1);
    }

    protected Label addLabel(String text) {
        return this.addLabel(text, SWT.DEFAULT, SWT.CENTER, SWT.FILL, false, 1, 1);
    }

    protected Label addLabelWithSpan(String text, int horizontalSpan, int verticalSpan) {
        return this.addLabel(text, SWT.DEFAULT, SWT.CENTER, SWT.FILL, false, horizontalSpan, verticalSpan);
    }

    protected void addHorizontalSeparator(int span) {
        Label label = new Label(this.currentCompo, SWT.SEPARATOR | SWT.HORIZONTAL);
        label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, span, 1));
        if (this.combinedMode) {
            this.labelsForCombined.add(label);
        }
    }

    protected void addVerticalSeparator(int span) {
        Label label = new Label(this.currentCompo, SWT.SEPARATOR | SWT.VERTICAL);
        GridData gd = new GridData(SWT.CENTER, SWT.FILL, false, false, 1, span);
        gd.widthHint = 10;
        label.setLayoutData(gd);
        if (this.combinedMode) {
            this.labelsForCombined.add(label);
        }
    }

    protected void skipSlot() {
        Label label = new Label(this.currentCompo, SWT.NONE);
        label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
        if (this.combinedMode) {
            this.labelsForCombined.add(label);
        }
    }

    protected static void setLabelText(Label label, String text) {
        setLabelText(label, text, text);
    }

    protected static void setLabelText(Label label, String text, String tooltipText) {
        label.setText(text);
        label.setToolTipText(tooltipText);
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
        if (this.isWindowInitialized() && this.getVisible()) {
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
        if (this.isWindowInitialized() && this.getVisible()) {
            this.updateData(start);
        }
    }

    public void updateMapCell(MapCellDto data) {
        this.mapCellDto = data;
        if (this.isWindowInitialized() && this.getVisible()) {
            this.updateData(false);
        }
    }

    public void updateBattle(BattleExDto data) {
        this.battle = data;
        List<EnemyShipDto> enemyShips = data.getEnemy();
        for (int i = 0; i < enemyShips.size(); ++i) {
            this.enemyShips[i] = enemyShips.get(i);
        }
        if (this.isWindowInitialized() && this.getVisible()) {
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

    protected void clearText() {
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
    protected EnemyShipDto[] getEnemyShips() {
        return this.enemyShips;
    }
}
