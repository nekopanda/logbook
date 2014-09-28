/**
 * 
 */
package logbook.gui;

import java.util.List;

import logbook.dto.BattleExDto;
import logbook.dto.DockDto;
import logbook.dto.EnemyShipDto;
import logbook.dto.ItemDto;
import logbook.dto.ShipDto;
import logbook.internal.EnemyData;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Nekopanda
 *
 */
public class BattleShipWindow extends BattleWindowBase {

    // 名前
    private final Label[] friendLabels = new Label[12];
    private final Label[] enemyLabels = new Label[6];
    // 0:火力 1:雷装 2:対空 3:装甲 4:cond, 5:燃料, 6:弾薬
    private final Label[][] fStatusLabels = new Label[6][12];
    private final Label[][] eStatusLabels = new Label[4][6];

    private final Label[][] friendDetail = new Label[2][12];
    private final Label[][] enemyDetail = new Label[2][6];

    private int friendCurrentIndex = -1;
    private boolean friendSelected = false;
    private int enemyCurrentIndex = -1;
    private boolean enemySelected = false;

    /**
     * Create the dialog.
     * @param parent
     */
    public BattleShipWindow(Shell parent, MenuItem menuItem) {
        super(parent, menuItem, "自軍敵軍パラメータ ");
    }

    private void friendUpdatetSlotitem(int newIndex) {
        if (newIndex != -1) {
            this.friendLabels[newIndex].setFont(this.getBoldFont());
            ShipDto ship = this.getFriendShips()[newIndex];
            if (ship != null) {
                List<ItemDto> slots = ship.getItem();
                int[] onSlots = ship.getOnSlot(); // 現在の艦載機搭載数
                int[] maxeq = ship.getShipInfo().getMaxeq(); // 艦載機最大搭載数
                for (int i = 0; i < 4; ++i) {
                    if (i < slots.size()) {
                        String onSlot = "";
                        String itemName = "";
                        ItemDto item = slots.get(i);
                        if (item != null) {
                            if (item.isPlane()) {
                                String max = (maxeq == null) ? "?" : String.valueOf(maxeq[i]);
                                onSlot = String.valueOf(onSlots[i]) + "/" + max;
                            }
                            itemName = item.getName();
                        }
                        setLabelText(this.friendDetail[0][i], String.valueOf(i + 1) + ":" + itemName);
                        this.friendDetail[1][i].setText(onSlot);
                    }
                }
            }
        }
    }

    private void friendSelectedIndexChanged(int newIndex) {
        if (this.friendCurrentIndex != newIndex) {
            this.beginDraw();
            try {
                if (this.friendCurrentIndex != -1) {
                    this.friendLabels[this.friendCurrentIndex].setFont(this.getNormalFont());
                }
                this.friendUpdatetSlotitem(newIndex);
                this.endDraw();
                this.friendCurrentIndex = newIndex;
            } finally {
                this.endDraw();
            }
        }
    }

    private void enemyUpdatetSlotitem(int newIndex) {
        if (newIndex != -1) {
            if ((this.getBattle() != null) && (newIndex < this.getBattle().getEnemy().size())) {
                this.enemyLabels[newIndex].setFont(this.getBoldFont());
                EnemyShipDto ship = this.getBattle().getEnemy().get(newIndex);
                if (ship != null) {
                    List<ItemDto> slots = ship.getItem();
                    int[] maxeq = ship.getShipInfo().getMaxeq(); // 艦載機最大搭載数
                    for (int i = 0; i < 5; ++i) {
                        if (i < slots.size()) {
                            String onSlot = "";
                            String itemName = "";
                            ItemDto item = slots.get(i);
                            if (item != null) {
                                if (item.isPlane()) {
                                    onSlot = (maxeq == null) ? "?" : String.valueOf(maxeq[i]);
                                }
                                itemName = item.getName();
                            }
                            setLabelText(this.enemyDetail[0][i], String.valueOf(i + 1) + ":" + itemName);
                            this.enemyDetail[1][i].setText(onSlot);
                        }
                    }
                }
            }
        }
    }

    private void enemySelectedIndexChanged(int newIndex) {
        if (this.enemyCurrentIndex != newIndex) {
            this.beginDraw();
            try {
                if (this.enemyCurrentIndex != -1) {
                    this.enemyLabels[this.enemyCurrentIndex].setFont(this.getNormalFont());
                }
                this.enemyUpdatetSlotitem(newIndex);
                this.endDraw();
                this.enemyCurrentIndex = newIndex;
            } finally {
                this.endDraw();
            }
        }
    }

    private void clearText() {
        // 味方
        for (int i = 0; i < 12; ++i) {
            this.friendLabels[i].setText("-");
            for (int c = 0; c < 6; ++c) {
                this.fStatusLabels[c][i].setText("");
            }
            this.friendDetail[0][i].setText("");
            this.friendDetail[1][i].setText("");
        }

        // 敵
        for (int i = 0; i < 6; ++i) {
            this.enemyLabels[i].setText("-");
            for (int c = 0; c < 4; ++c) {
                this.eStatusLabels[c][i].setText("");
            }
            this.enemyDetail[0][i].setText("");
            this.enemyDetail[1][i].setText("");
        }
    }

    @Override
    protected void createContents() {
        int numColumns = 14;

        GridLayout layout = new GridLayout(numColumns, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        this.getShell().setLayout(layout);

        // 各カラムの最小幅を定義 //
        int slotitemWidth = 140;
        int onslotWidth = 40;
        int nameWidth = 100;
        int fuelWidth = 50;
        int bullWidth = 50;
        int paramWidth = 25;

        this.skipSlot(); //1 separator
        this.skipSlot(); //2 separator
        this.addLabel("装備", slotitemWidth, SWT.CENTER, 2, 1);//3-4
        this.skipSlot(); //4 separator
        this.addLabelWithSize("艦名", nameWidth);//5
        this.skipSlot(); //6 separator
        this.addLabelWithSize("燃料", fuelWidth);//8
        this.addLabelWithSize("弾薬", bullWidth);//9
        this.skipSlot(); //10 separator
        this.addLabelWithSize("火力", paramWidth);//11
        this.addLabelWithSize("雷装", paramWidth);//12
        this.addLabelWithSize("対空", paramWidth);//13
        this.addLabelWithSize("装甲", paramWidth);//14

        this.addHorizontalSeparator(numColumns);

        // 味方エリア
        for (int i = 0; i < 12; ++i) {
            if (i == 6) {
                this.beginCombined();
            }
            if (i == 0) {
                this.addLabelWithSpan("自", 1, 6); //1
            }
            if (i == 6) {
                this.addLabelWithSpan("", 1, 6); //1
            }
            if ((i == 0) || (i == 6))
                this.addVerticalSeparator(6); //2
            this.friendDetail[0][i] = this.addLabel("詳細" + (i + 1), slotitemWidth - onslotWidth, SWT.LEFT, 1, 1); //3 詳細
            this.friendDetail[1][i] = this.addLabel("艦載機" + (i + 1), onslotWidth, SWT.CENTER, 1, 1); //3 詳細
            if ((i == 0) || (i == 6))
                this.addVerticalSeparator(6);//4
            this.friendLabels[i] = this.addLabel("艦名" + (i + 1), nameWidth, SWT.LEFT, 1, 1);//5
            if ((i == 0) || (i == 6))
                this.addVerticalSeparator(6);//6
            this.fStatusLabels[4][i] = this.addLabelWithSize("0", fuelWidth); //8 燃料
            this.fStatusLabels[5][i] = this.addLabelWithSize("0", bullWidth); //9 弾薬
            if ((i == 0) || (i == 6))
                this.addVerticalSeparator(6);//10
            this.fStatusLabels[0][i] = this.addLabelWithSize("0", paramWidth); //11 燃料
            this.fStatusLabels[1][i] = this.addLabelWithSize("0", paramWidth); //12 弾薬
            this.fStatusLabels[2][i] = this.addLabelWithSize("0", paramWidth); //13 燃料
            this.fStatusLabels[3][i] = this.addLabelWithSize("0", paramWidth); //14 弾薬
        }

        this.endCombined();
        this.addHorizontalSeparator(numColumns);

        // 敵エリア
        for (int i = 0; i < 6; ++i) {
            if (i == 0) {
                this.addLabelWithSpan("敵", 1, 6); //1
                this.addVerticalSeparator(6); //2
            }
            this.enemyDetail[0][i] = this.addLabel("詳細" + (i + 1), slotitemWidth - 25, SWT.LEFT, 1, 1); //19 詳細
            this.enemyDetail[1][i] = this.addLabel("艦載機" + (i + 1), 25, SWT.CENTER, 1, 1); //3 詳細
            if (i == 0)
                this.addVerticalSeparator(6); //5
            this.enemyLabels[i] = this.addLabel("艦名" + (i + 1), nameWidth, SWT.LEFT, 1, 1); //6
            if (i == 0)
                this.addVerticalSeparator(6); //5
            this.addLabelWithSpan("", 2, 1); //6
            if (i == 0)
                this.addVerticalSeparator(6); //5
            this.eStatusLabels[0][i] = this.addLabelWithSize("0", paramWidth); //8 燃料
            this.eStatusLabels[1][i] = this.addLabelWithSize("0", paramWidth); //9 弾薬
            this.eStatusLabels[2][i] = this.addLabelWithSize("0", paramWidth); //8 燃料
            this.eStatusLabels[3][i] = this.addLabelWithSize("0", paramWidth); //9 弾薬
        }

        // イベント登録
        for (int i = 0; i < 12; ++i) {
            final int currentIndex = i;
            this.friendLabels[i].addMouseMoveListener(new MouseMoveListener() {
                @Override
                public void mouseMove(MouseEvent e) {
                    if (BattleShipWindow.this.friendSelected == false) { // 未選択時のみ
                        BattleShipWindow.this.friendSelectedIndexChanged(currentIndex);
                    }
                }
            });
            this.friendLabels[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseUp(MouseEvent e) {
                    if (e.button == 1) {
                        BattleShipWindow.this.friendSelected = true;
                        BattleShipWindow.this.friendSelectedIndexChanged(currentIndex);
                    }
                    else if (e.button == 3) {
                        BattleShipWindow.this.friendSelected = false;
                        BattleShipWindow.this.friendSelectedIndexChanged(-1);
                    }
                }
            });
            if (i < 6) {
                this.enemyLabels[i].addMouseMoveListener(new MouseMoveListener() {
                    @Override
                    public void mouseMove(MouseEvent e) {
                        if (BattleShipWindow.this.enemySelected == false) { // 未選択時のみ
                            BattleShipWindow.this.enemySelectedIndexChanged(currentIndex);
                        }
                    }
                });
                this.enemyLabels[i].addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseUp(MouseEvent e) {
                        if (e.button == 1) {
                            BattleShipWindow.this.enemySelected = true;
                            BattleShipWindow.this.enemySelectedIndexChanged(currentIndex);
                        }
                        else if (e.button == 3) {
                            BattleShipWindow.this.enemySelected = false;
                            BattleShipWindow.this.enemySelectedIndexChanged(-1);
                        }
                    }
                });
            }
        }

        this.clearText();
    }

    /**
     * 艦名は選択解除があるのでウィンドウメニューを外しておく
     */
    @Override
    protected void createContentsAfter() {
        for (int i = 0; i < 12; ++i) {
            this.friendLabels[i].setMenu(null);
        }
        for (int i = 0; i < 6; ++i) {
            this.enemyLabels[i].setMenu(null);
        }
    }

    private void printDock(DockDto dock, int base) {
        List<ShipDto> ships = dock.getShips();
        for (int i = 0; i < ships.size(); ++i) {
            ShipDto ship = ships.get(i);
            setLabelText(this.friendLabels[base + i], String.valueOf(i + 1) + "." + ship.getFriendlyName(), "");
            this.fStatusLabels[4][base + i].setText(String.valueOf(ship.getFuel()) + "/" + ship.getFuelMax());
            this.fStatusLabels[5][base + i].setText(String.valueOf(ship.getBull()) + "/" + ship.getBullMax());
            this.fStatusLabels[0][base + i].setText(String.valueOf(ship.getKaryoku()));
            this.fStatusLabels[1][base + i].setText(String.valueOf(ship.getRaisou()));
            this.fStatusLabels[2][base + i].setText(String.valueOf(ship.getTaiku()));
            this.fStatusLabels[3][base + i].setText(String.valueOf(ship.getSoukou()));
        }
    }

    private void printDock() {
        List<DockDto> docks = this.getDocks();
        if (docks == null)
            return;

        this.setCombinedMode(docks.size() == 2);
        for (int i = 0; i < docks.size(); ++i) {
            DockDto dock = docks.get(i);
            this.printDock(dock, i * 6);
        }
        this.friendUpdatetSlotitem(this.friendCurrentIndex);
    }

    private void printMap() {
        if (this.getMapCellDto() == null)
            return;

        EnemyData enemyData = this.getMapCellDto().getEnemyData();
        if (enemyData != null) {
            String[] ships = enemyData.getEnemyShips();
            for (int i = 0; i < 6; ++i) {
                this.enemyLabels[i].setText(String.valueOf(i + 1) + "." + ships[i]);
            }
        }
        this.enemyUpdatetSlotitem(this.enemyCurrentIndex);
    }

    private void printBattle() {
        BattleExDto battle = this.getBattle();
        if (battle == null)
            return;

        List<EnemyShipDto> enemyShips = battle.getEnemy();
        for (int i = 0; i < enemyShips.size(); ++i) {
            EnemyShipDto ship = enemyShips.get(i);
            setLabelText(this.enemyLabels[i], String.valueOf(i + 1) + "." + ship.getFriendlyName(), "");
            this.eStatusLabels[0][i].setText(String.valueOf(ship.getKaryoku()));
            this.eStatusLabels[1][i].setText(String.valueOf(ship.getRaisou()));
            this.eStatusLabels[2][i].setText(String.valueOf(ship.getTaiku()));
            this.eStatusLabels[3][i].setText(String.valueOf(ship.getSoukou()));
        }
    }

    @Override
    protected void updateData(boolean start) {
        this.beginDraw();
        try {
            if (this.getBattle() != null) {
                this.printDock();
                this.printMap();
                this.printBattle();
            }
            else if (this.getDocks() == null) {
                // 出撃中でない
                this.clearText();
            }
            else if (this.getMapCellDto() == null) {
                // 移動中
                this.clearText();
                this.printDock();
            }
            else {
                // 移動中
                this.printMap();
            }
        } finally {
            this.endDraw();
        }
    }

}
