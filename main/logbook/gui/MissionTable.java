/**
 * 
 */
package logbook.gui;

import logbook.constants.AppConstants;
import logbook.data.Data;
import logbook.data.DataType;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;
import logbook.scripting.TableItemCreatorProxy;
import logbook.util.ReportUtils;
import logbook.util.SwtUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Nekopanda
 *
 */
public class MissionTable extends AbstractTableDialog {

    private Composite fleetCompo;

    private int selectedFleetId = 2;

    /**
     * @param parent
     * @param menuItem
     */
    public MissionTable(Shell parent, MenuItem menuItem) {
        super(parent, menuItem);
    }

    @Override
    protected void createContentsBefore() {
        GridLayout shellLayout = new GridLayout(1, false);
        shellLayout.verticalSpacing = 1;
        shellLayout.marginWidth = 1;
        shellLayout.marginHeight = 1;
        shellLayout.marginBottom = 1;
        shellLayout.horizontalSpacing = 1;
        this.shell.setLayout(shellLayout);

        this.fleetCompo = new Composite(this.getShell(), SWT.NONE);
        this.fleetCompo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
        this.fleetCompo.setLayout(new RowLayout(SWT.HORIZONTAL));

        SelectionListener fleetListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MissionTable.this.fleetSelected((Button) e.getSource());
            }
        };

        for (int i = 2; i <= 4; ++i) {
            Button button = new Button(this.fleetCompo, SWT.RADIO);
            button.setText("第" + i + "艦隊");
            button.setData(i);
            button.setSelection(i == this.selectedFleetId);
            button.addSelectionListener(fleetListener);
        }
    }

    private void fleetSelected(Button source) {
        if (source.getSelection()) {
            this.selectedFleetId = (Integer) source.getData();
            this.reloadTable();
        }
    }

    @Override
    protected void createContents() {
        this.table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
    }

    @Override
    protected String getTitleMain() {
        return "遠征一覧";
    }

    @Override
    protected Point getSize() {
        return SwtUtils.DPIAwareSize(new Point(600, 350));
    }

    @Override
    protected String[] getTableHeader() {
        return CreateReportLogic.getMissionHeader();
    }

    @Override
    protected void updateTableBody() {
        this.body = CreateReportLogic.getMissionBody(this.selectedFleetId);
    }

    @Override
    protected TableItemCreator getTableItemCreator() {
        return TableItemCreatorProxy.get(AppConstants.MISSIONTABLE_PREFIX);
    }

    /**
     * 更新する必要のあるデータ
     */
    @Override
    public void update(DataType type, Data data) {
        if (ReportUtils.isShipUpdate(type)) {
            this.needsUpdate = true;
        }
    }

}
