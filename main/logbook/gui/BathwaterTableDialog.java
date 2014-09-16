package logbook.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.dto.ShipDto;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.HpString;
import logbook.gui.logic.TableItemCreator;
import logbook.gui.logic.TimeLogic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * お風呂に入りたい艦娘
 * 
 */
public final class BathwaterTableDialog extends AbstractTableDialog {

    /** 遠征中の艦娘を外すフラグ */
    private static boolean removeflg;
    /** 遠征中の艦娘 */
    private Set<Integer> deckMissionShips;
    /** 入渠中の艦娘 */
    private Set<Integer> nDockShips;

    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    public BathwaterTableDialog(Shell parent, MenuItem menuItem) {
        super(parent, menuItem);
    }

    @Override
    protected void createContents() {
        final MenuItem removecheck = new MenuItem(this.opemenu, SWT.CHECK);
        removecheck.setText("遠征中の艦娘を外す");
        removecheck.setSelection(removeflg);
        removecheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                BathwaterTableDialog.removeflg = removecheck.getSelection();
                BathwaterTableDialog.this.reloadTable();
            }
        });
    }

    @Override
    protected String getTitle() {
        return "お風呂に入りたい艦娘";
    }

    @Override
    protected Point getSize() {
        // TODO 自動生成されたメソッド・スタブ
        return new Point(600, 350);
    }

    @Override
    protected String[] getTableHeader() {
        return new String[] { "", "艦娘ID", "艦隊", "疲労", "名前", "Lv", "HP", "時間", "燃料", "鋼材", "状態", "HP1あたり" };
    }

    @Override
    protected void updateTableBody() {
        this.deckMissionShips = GlobalContext.getMissionShipSet();
        this.nDockShips = GlobalContext.getNDockShipSet();

        List<ShipDto> ships = new ArrayList<ShipDto>();
        for (ShipDto ship : GlobalContext.getShipMap().values()) {
            if (this.nDockShips.contains(ship.getId())) {
                // 入渠中は外す
                continue;
            }
            // 入渠時間が1秒以上を取得
            if (ship.getDocktime() > 0) {
                ships.add(ship);
            }
        }
        // 入渠時間でソート
        Collections.sort(ships, new Comparator<ShipDto>() {
            @Override
            public int compare(ShipDto o1, ShipDto o2) {
                return Long.compare(o2.getDocktime(), o1.getDocktime());
            }
        });

        List<Comparable[]> body = new ArrayList<Comparable[]>();
        for (int i = 0; i < ships.size(); i++) {
            ShipDto ship = ships.get(i);
            // 状態
            String status = "";

            if (ship.isBadlyDamage()) {
                status = "大破";
            } else if (ship.isHalfDamage()) {
                status = "中破";
            } else if (ship.isSlightDamage()) {
                status = "小破";
            }
            if (this.deckMissionShips.contains(ship.getId())) {
                // 遠征中の艦娘を外すフラグが立っていたら遠征中の艦娘を外す
                if (removeflg) {
                    continue;
                }
                status = "遠征";
            }
            // HP1あたりの時間
            long unitSeconds = ((long) (ship.getDocktime()
                    / (float) (ship.getMaxhp() - ship.getNowhp()) / 1000));
            // 整形
            body.add(new Comparable[] {
                    i + 1,
                    ship.getId(),
                    ship.getFleetid(),
                    ship.getCond(),
                    ship.getName(),
                    ship.getLv(),
                    new HpString(ship.getNowhp(), ship.getMaxhp()),
                    new TimeLogic(ship.getDocktime()),
                    ship.getDockfuel(),
                    ship.getDockmetal(),
                    status,
                    TimeLogic.fromSeconds(unitSeconds)
            });
        }
        this.body = body;
    }

    @Override
    protected TableItemCreator getTableItemCreator() {
        return new TableItemCreator() {

            @Override
            public void init() {
            }

            @Override
            public TableItem create(Table table, Comparable[] text, int count) {
                TableItem item = new TableItem(table, SWT.NONE);
                // 偶数行に背景色を付ける
                if ((count % 2) != 0) {
                    item.setBackground(SWTResourceManager.getColor(AppConstants.ROW_BACKGROUND));
                }
                item.setText(CreateReportLogic.toStringArray(text));
                if (text[10].equals("遠征")) {
                    item.setForeground(SWTResourceManager.getColor(AppConstants.MISSION_COLOR));
                }
                return item;
            }
        };
    }

    @Override
    protected SelectionListener getHeaderSelectionListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.getSource() instanceof TableColumn) {
                    TableColumn column = (TableColumn) e.getSource();
                    if ("時間".equals(column.getText())) {
                        // "時間"がクリックされた場合
                        BathwaterTableDialog.this.sortTableItems(0, column);
                    } else {
                        BathwaterTableDialog.this.sortTableItems(column);
                    }
                }
            }
        };
    }
}
