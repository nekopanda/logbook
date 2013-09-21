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

import logbook.data.context.GlobalContext;
import logbook.dto.ShipDto;
import logbook.gui.listener.TableKeyShortcutAdapter;
import logbook.gui.listener.TableToClipboardAdapter;
import logbook.gui.listener.TableToCsvSaveAdapter;
import logbook.gui.logic.TimeLogic;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
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
public class BathwaterTableDialog extends Dialog {

    private static boolean removeflg;

    private static String[] header = { "艦娘ID", "艦隊", "疲労", "名前", "Lv", "HP", "時間", "燃料", "鋼材", "" };

    private final List<String[]> body = this.initBody();

    private Shell shell;

    /**
     * Create the dialog.
     * @param parent
     * @param style
     */
    public BathwaterTableDialog(Shell parent) {
        super(parent, SWT.SHELL_TRIM | SWT.MODELESS);
        this.setText("お風呂に入りたい艦娘");
    }

    /**
     * @return
     */
    private List<String[]> initBody() {
        List<ShipDto> ships = new ArrayList<ShipDto>();
        for (ShipDto ship : GlobalContext.getShipMap().values()) {
            if (ship.getDocktime() > 0) {
                ships.add(ship);
            }
        }
        Collections.sort(ships, new Comparator<ShipDto>() {
            @Override
            public int compare(ShipDto o1, ShipDto o2) {
                return Long.compare(o2.getDocktime(), o1.getDocktime());
            }
        });
        List<String[]> body = new ArrayList<String[]>();
        for (ShipDto ship : ships) {
            body.add(new String[] {
                    Long.toString(ship.getId()), ship.getFleetid(), Long.toString(ship.getCond()), ship.getName(),
                    Long.toString(ship.getLv()), Long.toString(ship.getNowhp()) + "/" + Long.toString(ship.getMaxhp()),
                    TimeLogic.toDateRestString(ship.getDocktime() / 1000), Long.toString(ship.getDockfuel()),
                    Long.toString(ship.getDockmetal()), ""
            });
        }
        return body;
    }

    /**
     * Open the dialog.
     * @return 
     * @return the result
     */
    public void open() {
        this.createContents();
        this.shell.open();
        this.shell.layout();
        Display display = this.getParent().getDisplay();
        while (!this.shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        this.shell = new Shell(this.getParent(), this.getStyle());
        this.shell.setSize(600, 350);
        this.shell.setText(this.getText());
        this.shell.setLayout(new FillLayout());

        Menu menubar = new Menu(this.shell, SWT.BAR);
        this.shell.setMenuBar(menubar);
        MenuItem fileroot = new MenuItem(menubar, SWT.CASCADE);
        fileroot.setText("ファイル");
        Menu filemenu = new Menu(fileroot);
        fileroot.setMenu(filemenu);
        MenuItem savecsv = new MenuItem(filemenu, SWT.NONE);
        savecsv.setText("CSVファイルに保存");

        MenuItem operation = new MenuItem(menubar, SWT.CASCADE);
        operation.setText("操作");
        Menu menubar2 = new Menu(operation);
        operation.setMenu(menubar2);
        final MenuItem removecheck = new MenuItem(menubar2, SWT.CHECK);
        removecheck.setText("遠征・入渠中の艦娘を外す");
        removecheck.setSelection(removeflg);

        final Table table = new Table(this.shell, SWT.FULL_SELECTION | SWT.MULTI);
        table.addKeyListener(new TableKeyShortcutAdapter(header, table));
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        TableColumn[] columns = new TableColumn[header.length];

        for (int i = 0; i < header.length; i++) {
            TableColumn col = new TableColumn(table, SWT.LEFT);
            col.setText(header[i]);
            columns[i] = col;
        }
        this.addAllTableItems(table);

        savecsv.addSelectionListener(new TableToCsvSaveAdapter(this.shell, this.getText(), header, table));

        for (int i = 0; i < columns.length; i++) {
            columns[i].pack();
        }

        Menu menu = new Menu(table);
        table.setMenu(menu);
        MenuItem menuItem = new MenuItem(menu, SWT.NONE);
        menuItem.addSelectionListener(new TableToClipboardAdapter(this.header, table));
        menuItem.setText("クリップボードにコピー");

        removecheck.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                BathwaterTableDialog.removeflg = removecheck.getSelection();
                BathwaterTableDialog.this.addAllTableItems(table);
            }
        });
    }

    private void addAllTableItems(Table table) {

        TableItem[] items = table.getItems();
        for (int i = 0; i < items.length; i++) {
            items[i].dispose();
        }

        // 遠征中の艦娘
        Set<Long> deckmissions = this.getDeckMissionShips();

        // 入渠
        Set<Long> docks = this.getDockShips();

        for (String[] line : this.body) {
            Long shipid = Long.valueOf(line[0]);
            Color color = null;

            if (removeflg && ((deckmissions.contains(shipid)) || docks.contains(shipid))) {
                continue;
            }

            // 遠征中の艦娘の色を変更する
            if (deckmissions.contains(shipid)) {
                color = SWTResourceManager.getColor(102, 51, 255);
                line[line.length - 1] = "遠征";
            }
            // 入渠中の艦娘の色を変更する
            if (docks.contains(shipid)) {
                color = SWTResourceManager.getColor(0, 102, 153);
                line[line.length - 1] = "入渠";
            }

            TableItem item = new TableItem(table, SWT.NONE);
            if (color != null) {
                item.setForeground(color);
            }
            item.setText(line);
        }
        table.redraw();
    }

    /**
     * 遠征中の艦娘を取得します
     * 
     * @return
     */
    private Set<Long> getDeckMissionShips() {
        // 遠征
        Set<Long> deckmissions = new HashSet<Long>();
        if ((GlobalContext.getDeck1Mission() != null) && (GlobalContext.getDeck1Mission().getMission() != null)) {
            deckmissions.addAll(GlobalContext.getDeck1Mission().getShips());
        }
        if ((GlobalContext.getDeck2Mission() != null) && (GlobalContext.getDeck2Mission().getMission() != null)) {
            deckmissions.addAll(GlobalContext.getDeck2Mission().getShips());
        }
        if ((GlobalContext.getDeck3Mission() != null) && (GlobalContext.getDeck3Mission().getMission() != null)) {
            deckmissions.addAll(GlobalContext.getDeck3Mission().getShips());
        }
        return deckmissions;
    }

    /**
     * 入渠中の艦娘を取得します
     * 
     * @return
     */
    private Set<Long> getDockShips() {
        // 入渠
        Set<Long> docks = new HashSet<Long>();
        docks.add(GlobalContext.getNdock1id());
        docks.add(GlobalContext.getNdock2id());
        docks.add(GlobalContext.getNdock3id());
        docks.add(GlobalContext.getNdock4id());
        return docks;
    }
}
