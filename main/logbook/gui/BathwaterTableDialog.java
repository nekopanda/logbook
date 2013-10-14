/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import logbook.config.GlobalConfig;
import logbook.data.context.GlobalContext;
import logbook.dto.DeckMissionDto;
import logbook.dto.NdockDto;
import logbook.dto.ShipDto;
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

    /** 遠征・入渠中の艦娘を外すフラグ */
    private static boolean removeflg;
    /** 遠征中の艦娘 */
    private Set<Long> deckMissionShips;
    /** 入渠中の艦娘 */
    private Set<Long> nDockShips;

    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    public BathwaterTableDialog(Shell parent) {
        super(parent);
    }

    @Override
    protected void createContents() {
        final MenuItem removecheck = new MenuItem(this.opemenu, SWT.CHECK);
        removecheck.setText("遠征・入渠中の艦娘を外す");
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
        return new String[] { "艦娘ID", "艦隊", "疲労", "名前", "Lv", "HP", "時間", "燃料", "鋼材", "" };
    }

    @Override
    protected void updateTableBody() {
        List<ShipDto> ships = new ArrayList<ShipDto>();
        for (ShipDto ship : GlobalContext.getShipMap().values()) {
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

        this.deckMissionShips = this.getDeckMissionShips();
        this.nDockShips = this.getNDockShips();

        List<String[]> body = new ArrayList<String[]>();
        for (ShipDto ship : ships) {
            // 遠征・入渠の文字が入る
            String action = "";

            if (this.deckMissionShips.contains(ship.getId())) {
                // 遠征・入渠中の艦娘を外すフラグが立っていたら遠征・入渠中の艦娘を外す
                if (removeflg) {
                    continue;
                }
                action = "遠征";
            }
            if (this.nDockShips.contains(ship.getId())) {
                if (removeflg) {
                    continue;
                }
                action = "入渠";
            }
            // 整形
            body.add(new String[] {
                    Long.toString(ship.getId()), ship.getFleetid(), Long.toString(ship.getCond()), ship.getName(),
                    Long.toString(ship.getLv()), Long.toString(ship.getNowhp()) + "/" + Long.toString(ship.getMaxhp()),
                    TimeLogic.toDateRestString(ship.getDocktime() / 1000), Long.toString(ship.getDockfuel()),
                    Long.toString(ship.getDockmetal()), action
            });
        }
        this.body = body;
    }

    @Override
    protected TableItemCreator getTableItemCreator() {
        return new TableItemCreator() {
            @Override
            public TableItem create(Table table, String[] text) {
                TableItem item = new TableItem(table, SWT.NONE);
                item.setText(text);
                if (text[text.length - 1].equals("遠征")) {
                    item.setForeground(SWTResourceManager.getColor(GlobalConfig.MISSION_COLOR));
                }
                if (text[text.length - 1].equals("入渠")) {
                    item.setForeground(SWTResourceManager.getColor(GlobalConfig.NDOCK_COLOR));
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
                    BathwaterTableDialog.this.sortTableItems((TableColumn) e.getSource());
                }
            }
        };
    }

    /**
     * 遠征中の艦娘を取得します
     * 
     * @return
     */
    private Set<Long> getDeckMissionShips() {
        // 遠征
        Set<Long> deckmissions = new HashSet<Long>();
        for (DeckMissionDto deckMission : GlobalContext.getDeckMissions()) {
            if ((deckMission.getMission() != null) && (deckMission.getShips() != null)) {
                deckmissions.addAll(deckMission.getShips());
            }
        }
        return deckmissions;
    }

    /**
     * 入渠中の艦娘を取得します
     * 
     * @return
     */
    private Set<Long> getNDockShips() {
        // 入渠
        Set<Long> docks = new HashSet<Long>();
        for (NdockDto ndock : GlobalContext.getNdocks()) {
            if (ndock.getNdockid() != 0) {
                docks.add(ndock.getNdockid());
            }
        }
        return docks;
    }
}
