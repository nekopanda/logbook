/**
 * 
 */
package logbook.gui;

import java.util.List;

import logbook.dto.BattleExDto;
import logbook.dto.DockDto;
import logbook.dto.EnemyShipDto;
import logbook.dto.ItemInfoDto;
import logbook.dto.MapCellDto;
import logbook.dto.ShipDto;
import logbook.dto.ShipInfoDto;
import logbook.internal.EnemyData;
import logbook.internal.Item;
import logbook.internal.Ship;

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
 * 自軍敵軍パラメータ
 */
public class BattleShipWindow extends BattleWindowBase {

    // 名前
    private final Label[] friendLabels = new Label[12];
    private final Label[] enemyLabels = new Label[12];
    // 0:火力 1:雷装 2:対空 3:装甲 4:cond, 5:燃料, 6:弾薬
    private final Label[][] fStatusLabels = new Label[6][12];
    private final Label[][] eStatusLabels = new Label[4][12];

    private final Label[][] friendDetail = new Label[2][12];
    private final Label[][] enemyDetail = new Label[2][12];

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
                List<ItemInfoDto> slots = ship.getItem();
                int[] onSlots = ship.getOnSlot(); // 現在の艦載機搭載数
                int[] maxeq = ship.getShipInfo().getMaxeq2(); // 艦載機最大搭載数
                int slotnum = ship.getSlotNum();
                for (int i = 0; i < 4; ++i) {
                    if (i < slotnum) {
                        String onSlot = "";
                        String itemName = "";
                        ItemInfoDto item = (i < slots.size()) ? slots.get(i) : null;
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
                    else {
                        setLabelText(this.friendDetail[0][i], "");
                        this.friendDetail[1][i].setText("");
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
            List<ItemInfoDto> slots;
            ShipInfoDto shipinfo;
            if ((this.getBattle() != null)) {
                EnemyShipDto ship = this.getEnemyShips()[newIndex];
                if (ship == null)
                    return;

                slots = ship.getItem();
                shipinfo = ship.getShipInfo();
            }
            else {
                MapCellDto dto = this.getMapCellDto();
                if (dto == null)
                    return;

                EnemyData enemyData = dto.getEnemyData();
                if (enemyData == null)
                    return;

                int shpid = enemyData.getEnemyShipsId()[newIndex];
                if (shpid == -1)
                    return;

                shipinfo = Ship.get(shpid);
                if (shipinfo == null)
                    return;

                slots = Item.fromIdList(shipinfo.getDefaultSlot());
            }

            this.enemyLabels[newIndex].setFont(this.getBoldFont());

            int[] maxeq = shipinfo.getMaxeq2(); // 艦載機最大搭載数
            int slotnum = shipinfo.getSlotNum();
            for (int i = 0; i < 5; ++i) {
                if (i < slotnum) {
                    String onSlot = "";
                    String itemName = "";
                    ItemInfoDto item = (i < slots.size()) ? slots.get(i) : null;
                    if (item != null) {
                        if (item.isPlane()) {
                            onSlot = (maxeq == null) ? "?" : String.valueOf(maxeq[i]);
                        }
                        itemName = item.getName();
                    }
                    setLabelText(this.enemyDetail[0][i], String.valueOf(i + 1) + ":" + itemName);
                    this.enemyDetail[1][i].setText(onSlot);
                }
                else {
                    setLabelText(this.enemyDetail[0][i], "");
                    this.enemyDetail[1][i].setText("");
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
                this.enemyCurrentIndex = newIndex;
            } finally {
                this.endDraw();
            }
        }
    }

    private void clearFriendArea() {
        for (int i = 0; i < 12; ++i) {
            this.friendLabels[i].setText("-");
            for (int c = 0; c < 6; ++c) {
                this.fStatusLabels[c][i].setText("");
            }
            this.friendDetail[0][i].setText("");
            this.friendDetail[1][i].setText("");
        }
    }

    private void clearEnemyArea() {
        for (int i = 0; i < 12; ++i) {
            this.enemyLabels[i].setText("-");
            for (int c = 0; c < 4; ++c) {
                this.eStatusLabels[c][i].setText("");
            }
            this.enemyDetail[0][i].setText("");
            this.enemyDetail[1][i].setText("");
        }
    }

    @Override
    protected void clearText() {
        // 味方
        this.clearFriendArea();
        // 敵
        this.clearEnemyArea();
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
        int slotitemWidth = 90;
        int nameWidth = 110;

        this.skipSlot(); //1 separator
        this.skipSlot(); //2 separator
        this.addLabel("装備", slotitemWidth, SWT.CENTER, 2, 1);//3-4
        this.skipSlot(); //4 separator
        this.addLabel("艦名", SWT.CENTER, nameWidth);//5
        this.skipSlot(); //6 separator
        this.addLabel("燃料");//8
        this.addLabel("弾薬");//9
        this.skipSlot(); //10 separator
        this.addLabel("火力");//11
        this.addLabel("雷装");//12
        this.addLabel("対空");//13
        this.addLabel("装甲");//14

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
            this.friendDetail[0][i] = this.addLabel("詳細" + (i + 1), SWT.LEFT, slotitemWidth); //3 詳細
            this.friendDetail[1][i] = this.addLabel("00/00"); //3 詳細
            if ((i == 0) || (i == 6))
                this.addVerticalSeparator(6);//4
            this.friendLabels[i] = this.addLabel("艦名" + (i + 1), SWT.LEFT, nameWidth);//5
            if ((i == 0) || (i == 6))
                this.addVerticalSeparator(6);//6
            this.fStatusLabels[4][i] = this.addLabel("000/000"); //8 燃料
            this.fStatusLabels[5][i] = this.addLabel("000/000"); //9 弾薬
            if ((i == 0) || (i == 6))
                this.addVerticalSeparator(6);//10
            this.fStatusLabels[0][i] = this.addLabel("0000"); //11 燃料
            this.fStatusLabels[1][i] = this.addLabel("0000"); //12 弾薬
            this.fStatusLabels[2][i] = this.addLabel("0000"); //13 燃料
            this.fStatusLabels[3][i] = this.addLabel("0000"); //14 弾薬
        }

        this.endCombined();
        this.addHorizontalSeparator(numColumns);

        // 敵エリア
        for (int i = 0; i < 12; ++i) {
            if (i == 6) {
                this.beginEnemyCombined();
            }
            if (i == 0) {
                this.addLabelWithSpan("敵", 1, 6); //1
            }
            if (i == 6) {
                this.addLabelWithSpan("", 1, 6); //1
            }
            if ((i == 0) || (i == 6))
                this.addVerticalSeparator(6); //2
            this.enemyDetail[0][i] = this.addLabel("詳細" + (i + 1), SWT.LEFT, slotitemWidth); //3 詳細
            this.enemyDetail[1][i] = this.addLabel("000"); //3 詳細
            if ((i == 0) || (i == 6))
                this.addVerticalSeparator(6); //5
            this.enemyLabels[i] = this.addLabel("艦名" + (i + 1), SWT.LEFT, nameWidth);//5
            if ((i == 0) || (i == 6))
                this.addVerticalSeparator(6); //5
            this.addLabelWithSpan("", 2, 1); //6
            if ((i == 0) || (i == 6))
                this.addVerticalSeparator(6); //5
            this.eStatusLabels[0][i] = this.addLabel("0000"); //8 燃料
            this.eStatusLabels[1][i] = this.addLabel("0000"); //9 弾薬
            this.eStatusLabels[2][i] = this.addLabel("0000"); //8 燃料
            this.eStatusLabels[3][i] = this.addLabel("0000"); //9 弾薬
        }

        // イベント登録
        for (int i = 0; i < 12; ++i) {
            final int currentIndex = i;
            // このラベルでのウィンドウのドラックとウィンドウの右クリックメニューを無効化
            this.friendLabels[i].setData("disable-drag-move", true);
            this.friendLabels[i].setData("disable-window-menu", true);
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
            // このラベルでのウィンドウのドラックを無効化
            this.enemyLabels[i].setData("disable-drag-move", true);
            this.enemyLabels[i].setData("disable-window-menu", true);
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
            int[] ships = enemyData.getEnemyShipsId();
            for (int i = 0; i < 6; ++i) {
                if (ships[i] != -1) {
                    ShipInfoDto shipinfo = Ship.get(ships[i]);
                    if (shipinfo != null) {
                        this.enemyLabels[i].setText(String.valueOf(i + 1) + "." + shipinfo.getFullName());
                    }
                }
                else {
                    this.enemyLabels[i].setText("-");
                }
            }
        }
        this.enemyUpdatetSlotitem(this.enemyCurrentIndex);
    }

    private void printBattle() {
        BattleExDto battle = this.getBattle();
        if (battle == null)
            return;

        List<EnemyShipDto> enemyShips = battle.getEnemy();
        List<EnemyShipDto> enemyShipsCombined = battle.getEnemyCombined();
        for (int i = 0; i < 2; ++i) {
            List<EnemyShipDto> ships = ((i == 0) ? enemyShips : enemyShipsCombined);
            if (ships != null) {
                for (int c = 0; c < ships.size(); ++c) {
                    EnemyShipDto ship = ships.get(c);
                    int index = (i * 6) + c;
                    setLabelText(this.enemyLabels[index], String.valueOf(index + 1) + "." + ship.getFriendlyName(), "");
                    this.eStatusLabels[0][index].setText(String.valueOf(ship.getKaryoku()));
                    this.eStatusLabels[1][index].setText(String.valueOf(ship.getRaisou()));
                    this.eStatusLabels[2][index].setText(String.valueOf(ship.getTaiku()));
                    this.eStatusLabels[3][index].setText(String.valueOf(ship.getSoukou()));
                }
            }
        }

        // 敵連合艦隊用レイアウトセット
        this.setEnemyCombinedMode(battle.isEnemyCombined());
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
                //this.printMap();
                this.clearEnemyArea();
            }
        } finally {
            this.endDraw();
        }
    }

}
