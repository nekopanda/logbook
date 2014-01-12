/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui.widgets;

import java.util.List;

import logbook.config.AppConfig;
import logbook.constants.AppConstants;
import logbook.dto.DockDto;
import logbook.dto.ShipDto;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * 艦隊タブのウィジェットです
 *
 */
public class FleetComposite extends Composite {

    private static final int WARN = 1;
    private static final int FATAL = 2;

    private static final int MAXCHARA = 6;

    private final CTabItem tab;
    private final Font large1;
    private final Font large2;

    private DockDto dock;

    private final Composite fleetGroup;

    private int state;

    /** アイコンラベル */
    private final Label[] iconLabels = new Label[MAXCHARA];
    /** 名前ラベル */
    private final Label[] nameLabels = new Label[MAXCHARA];
    /** 今のHP */
    private final Label[] nowhpLabels = new Label[MAXCHARA];
    /** 最大HP */
    private final Label[] maxhpLabels = new Label[MAXCHARA];
    /** HPステ */
    private final Label[] hpmsgLabels = new Label[MAXCHARA];
    /** コンディション */
    private final Label[] condLabels = new Label[MAXCHARA];
    /** コンディションステータス */
    private final Label[] condstLabels = new Label[MAXCHARA];
    /** 弾ステータス */
    private final Label[] bullstLabels = new Label[MAXCHARA];
    /** 燃料ステータス */
    private final Label[] fuelstLabels = new Label[MAXCHARA];

    /**
     * @param parent 艦隊タブの親
     * @param tabItem 艦隊タブ
     */
    public FleetComposite(CTabFolder parent, CTabItem tabItem) {
        super(parent, SWT.NONE);
        this.tab = tabItem;
        this.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout glParent = new GridLayout(1, false);
        glParent.horizontalSpacing = 0;
        glParent.marginTop = 0;
        glParent.marginHeight = 0;
        glParent.marginBottom = 0;
        glParent.verticalSpacing = 0;
        this.setLayout(glParent);

        FontData normalfd = parent.getShell().getFont().getFontData()[0];
        FontData largefd1 = new FontData(normalfd.getName(), normalfd.getHeight() + 2, normalfd.getStyle());
        FontData largefd2 = new FontData(normalfd.getName(), normalfd.getHeight() + 2, normalfd.getStyle());

        this.large1 = new Font(Display.getCurrent(), largefd1);
        this.large2 = new Font(Display.getCurrent(), largefd2);

        this.fleetGroup = new Composite(this, SWT.NONE);
        this.fleetGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout glShipGroup = new GridLayout(3, false);
        glShipGroup.horizontalSpacing = 0;
        glShipGroup.marginTop = 0;
        glShipGroup.marginHeight = 0;
        glShipGroup.marginBottom = 0;
        glShipGroup.verticalSpacing = 0;
        this.fleetGroup.setLayout(glShipGroup);
        this.init();
    }

    /**
     * 初期化
     */
    private void init() {
        for (int i = 0; i < MAXCHARA; i++) {
            // 名前
            Label iconlabel = new Label(this.fleetGroup, SWT.NONE);
            // 名前
            Label namelabel = new Label(this.fleetGroup, SWT.NONE);
            namelabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            namelabel.setFont(this.large1);
            namelabel.setText("名前");
            // HP
            Composite hpComposite = new Composite(this.fleetGroup, SWT.NONE);
            GridLayout glHp = new GridLayout(3, false);
            glHp.horizontalSpacing = 1;
            glHp.marginTop = 0;
            glHp.marginHeight = 0;
            glHp.marginBottom = 0;
            glHp.verticalSpacing = 0;
            hpComposite.setLayout(glHp);
            hpComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));

            Label nowhp = new Label(hpComposite, SWT.NONE);
            nowhp.setFont(this.large2);
            nowhp.setText("99");
            Label maxhp = new Label(hpComposite, SWT.NONE);
            maxhp.setText("/99");
            Label hpmsg = new Label(hpComposite, SWT.NONE);
            hpmsg.setText("(健在)");

            // ステータス
            new Label(this.fleetGroup, SWT.NONE);
            Composite stateComposite = new Composite(this.fleetGroup, SWT.NONE);
            GridLayout glState = new GridLayout(3, false);
            glState.horizontalSpacing = 1;
            glState.marginTop = 0;
            glState.marginHeight = 0;
            glState.marginBottom = 0;
            glState.verticalSpacing = 0;
            stateComposite.setLayout(glState);
            stateComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

            Label condst = new Label(stateComposite, SWT.NONE);
            condst.setText("疲");
            Label fuelst = new Label(stateComposite, SWT.NONE);
            fuelst.setText("燃");
            Label bullst = new Label(stateComposite, SWT.NONE);
            bullst.setText("弾");

            // 疲労
            Label cond = new Label(this.fleetGroup, SWT.NONE);
            cond.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
            cond.setText("49 cond.");

            this.iconLabels[i] = iconlabel;
            this.nameLabels[i] = namelabel;
            this.nowhpLabels[i] = nowhp;
            this.maxhpLabels[i] = maxhp;
            this.hpmsgLabels[i] = hpmsg;
            this.condLabels[i] = cond;
            this.condstLabels[i] = condst;
            this.bullstLabels[i] = bullst;
            this.fuelstLabels[i] = fuelst;
        }
        this.fleetGroup.layout(true);
    }

    /**
     * 艦隊を更新します
     * 
     * @param dock
     */
    public void updateFleet(DockDto dock) {
        if (this.dock == dock) {
            return;
        }
        this.state = 0;

        List<ShipDto> ships = dock.getShips();
        for (int i = ships.size(); i < MAXCHARA; i++) {
            this.nameLabels[i].setText("");
            this.nowhpLabels[i].setText("");
            this.maxhpLabels[i].setText("");
            this.hpmsgLabels[i].setText("");
            this.condLabels[i].setText("");
            this.condstLabels[i].setText("");
            this.bullstLabels[i].setText("");
            this.fuelstLabels[i].setText("");
        }
        for (int i = 0; i < ships.size(); i++) {
            ShipDto ship = ships.get(i);
            // 名前
            String name = ship.getName();
            // HP
            long nowhp = ship.getNowhp();
            // MaxHP
            long maxhp = ship.getMaxhp();
            // HP比率
            float hpratio = (float) nowhp / (float) maxhp;
            // 疲労
            long cond = ship.getCond();
            // 弾
            int bull = ship.getBull();
            // 弾Max
            int bullmax = ship.getBullMax();
            // 残弾比
            float bullraito = bullmax != 0 ? (float) bull / (float) bullmax : 1f;
            // 燃料
            int fuel = ship.getFuel();
            // 燃料Max
            int fuelmax = ship.getFuelMax();
            // 残燃料比
            float fuelraito = fuelmax != 0 ? (float) fuel / (float) fuelmax : 1f;

            // 体力メッセージ
            if (hpratio <= AppConstants.BADLY_DAMAGE) {
                if (AppConfig.get().isFatalBybadlyDamage()) {
                    // 大破で致命的アイコン
                    this.state |= FATAL;
                }

                this.hpmsgLabels[i].setText("(大破)");
                this.hpmsgLabels[i].setBackground(SWTResourceManager.getColor(AppConstants.COND_RED_COLOR));
                this.hpmsgLabels[i].setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
            } else if (hpratio <= AppConstants.HALF_DAMAGE) {
                if (AppConfig.get().isWarnByHalfDamage()) {
                    // 中破で警告アイコン
                    this.state |= WARN;
                }

                this.hpmsgLabels[i].setText("(中破)");
                this.hpmsgLabels[i].setBackground(SWTResourceManager.getColor(AppConstants.COND_ORANGE_COLOR));
                this.hpmsgLabels[i].setForeground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
            } else if (hpratio <= AppConstants.SLIGHT_DAMAGE) {
                this.hpmsgLabels[i].setText("(小破)");
                this.hpmsgLabels[i].setBackground(null);
                this.hpmsgLabels[i].setForeground(null);
            } else {
                this.hpmsgLabels[i].setText("(健在)");
                this.hpmsgLabels[i].setBackground(null);
                this.hpmsgLabels[i].setForeground(null);
            }

            // ステータス
            // ステータス.疲労
            this.condstLabels[i].setText("疲");
            if (cond >= 49) {
                this.condstLabels[i].setEnabled(false);
            } else {
                this.condstLabels[i].setEnabled(true);
            }
            // ステータス.燃料
            this.fuelstLabels[i].setText("燃");
            if (fuelraito >= 1f) {
                this.fuelstLabels[i].setEnabled(false);
                this.fuelstLabels[i].setForeground(null);
            } else {
                if (AppConfig.get().isWarnByNeedSupply()) {
                    // 補給不足で警告アイコン
                    this.state |= WARN;
                }
                this.fuelstLabels[i].setEnabled(true);
                if (fuelraito <= AppConstants.EMPTY_SUPPLY) {
                    this.fuelstLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_RED_COLOR));
                } else if (fuelraito <= AppConstants.LOW_SUPPLY) {
                    this.fuelstLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_ORANGE_COLOR));
                }
            }
            // ステータス.弾
            this.bullstLabels[i].setText("弾");
            if (bullraito >= 1f) {
                this.bullstLabels[i].setEnabled(false);
                this.bullstLabels[i].setBackground(null);
                this.bullstLabels[i].setForeground(null);
            } else {
                if (AppConfig.get().isWarnByNeedSupply()) {
                    // 補給不足で警告アイコン
                    this.state |= WARN;
                }
                this.bullstLabels[i].setEnabled(true);
                if (bullraito <= AppConstants.EMPTY_SUPPLY) {
                    this.bullstLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_RED_COLOR));
                } else if (bullraito <= AppConstants.LOW_SUPPLY) {
                    this.bullstLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_ORANGE_COLOR));
                }
            }
            // コンディション
            if (cond <= AppConstants.COND_RED) {
                if (AppConfig.get().isWarnByCondState()) {
                    // 疲労状態で警告アイコン
                    this.state |= WARN;
                }
                this.condLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_RED_COLOR));
                this.condstLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_RED_COLOR));
            } else if (cond <= AppConstants.COND_ORANGE) {
                if (AppConfig.get().isWarnByCondState()) {
                    // 疲労状態で警告アイコン
                    this.state |= WARN;
                }
                this.condLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_ORANGE_COLOR));
                this.condstLabels[i].setForeground(SWTResourceManager.getColor(AppConstants.COND_ORANGE_COLOR));
            } else {
                this.condLabels[i].setForeground(null);
                this.condstLabels[i].setForeground(null);
            }

            if ((this.state & FATAL) == FATAL) {
                this.iconLabels[i].setImage(SWTResourceManager.getImage(FleetComposite.class,
                        "/resources/icon/exclamation.png"));
            } else if ((this.state & WARN) == WARN) {
                this.iconLabels[i].setImage(SWTResourceManager.getImage(FleetComposite.class,
                        "/resources/icon/error.png"));
            } else {
                this.iconLabels[i].setImage(null);
            }
            this.nameLabels[i].setText(name);
            this.nameLabels[i].setToolTipText("燃:" + ship.getFuel() + "/" + ship.getFuelMax() + " 弾:"
                    + ship.getBull() + "/" + ship.getBullMax() + " Next:" + ship.getNext() + "exp");
            this.nowhpLabels[i].setText(Long.toString(nowhp));
            this.maxhpLabels[i].setText("/" + maxhp);
            this.condLabels[i].setText(ship.getCond() + " cond.");
            this.bullstLabels[i].getParent().layout(true);
        }
        this.dock = dock;
        this.updateTabIcon();
        this.fleetGroup.layout(true);
    }

    /**
     * 艦隊タブのアイコンを更新します
     */
    private void updateTabIcon() {
        if (this.state == 0) {
            this.tab.setImage(null);
        } else {
            if ((this.state & FATAL) == FATAL) {
                this.tab.setImage(SWTResourceManager.getImage(FleetComposite.class, "/resources/icon/exclamation.png"));
            } else if ((this.state & WARN) == WARN) {
                this.tab.setImage(SWTResourceManager.getImage(FleetComposite.class, "/resources/icon/error.png"));
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        this.large1.dispose();
        this.large2.dispose();
    }
}
