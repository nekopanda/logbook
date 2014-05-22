package logbook.gui;

import java.util.Set;
import java.util.TreeSet;

import logbook.data.context.GlobalContext;
import logbook.dto.ItemDto;
import logbook.dto.ShipFilterDto;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 所有艦娘一覧で使用するフィルターダイアログ
 * 
 */
public final class ShipFilterDialog extends Dialog {

    private Shell shell;

    private final ShipTable shipTable;

    private final ShipFilterDto filter;

    /** 名前 */
    private Text nametext;
    /** 名前.正規表現を使用する */
    private Button regexp;

    /** 艦種.駆逐艦 */
    private Button destroyer;
    /** 艦種.軽巡洋艦 */
    private Button lightCruiser;
    /** 艦種.重雷装巡洋艦 */
    private Button torpedoCruiser;
    /** 艦種.重巡洋艦 */
    private Button heavyCruiser;
    /** 艦種.航空巡洋艦 */
    private Button flyingDeckCruiser;
    /** 艦種.水上機母艦 */
    private Button seaplaneTender;
    /** 艦種.軽空母 */
    private Button escortCarrier;
    /** 艦種.正規空母 */
    private Button carrier;
    /** 艦種.戦艦 */
    private Button battleship;
    /** 艦種.航空戦艦 */
    private Button flyingDeckBattleship;
    /** 艦種.潜水艦 */
    private Button submarine;
    /** 艦種.潜水空母 */
    private Button carrierSubmarine;
    /** 艦種.揚陸艦 */
    private Button landingship;
    /** 艦種.装甲空母 */
    private Button armoredcarrier;
    /** 艦種.工作艦 */
    private Button repairship;
    /** 全て選択 */
    private Button selectall;
    /** 装備 */
    private Button item;
    /** 装備 */
    private Combo itemcombo;
    /** 艦隊に所属 */
    private Button onfleet;
    /** 艦隊に非所属 */
    private Button notonfleet;
    /** 鍵付き */
    private Button locked;
    /** 鍵付きではない */
    private Button notlocked;

    /**
     * Create the dialog.
     * 
     * @param parent シェル
     * @param shipTable 呼び出し元
     * @param filter 初期値
     */
    public ShipFilterDialog(Shell parent, ShipTable shipTable, ShipFilterDto filter) {
        super(parent, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE);
        this.shipTable = shipTable;
        this.filter = filter;
    }

    /**
     * Open the dialog.
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
        this.shell.setText("フィルター");
        this.shell.setLayout(new GridLayout(1, false));

        Composite composite = new Composite(this.shell, SWT.NONE);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        composite.setLayout(new GridLayout(1, false));

        Group namegroup = new Group(composite, SWT.NONE);
        namegroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        namegroup.setLayout(new RowLayout());
        namegroup.setText("名前");

        Label namelabel = new Label(namegroup, SWT.NONE);
        namelabel.setText("名前:");

        this.nametext = new Text(namegroup, SWT.BORDER);
        this.nametext.setLayoutData(new RowData(160, SWT.DEFAULT));
        this.nametext.addModifyListener(new ApplyFilterModifyAdapter());

        this.regexp = new Button(namegroup, SWT.CHECK);
        this.regexp.setText("正規表現");
        this.regexp.addSelectionListener(new ApplyFilterSelectionAdapter());

        Group shiptypegroup = new Group(composite, SWT.NONE);
        shiptypegroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        shiptypegroup.setLayout(new GridLayout(3, false));
        shiptypegroup.setText("艦種");

        this.destroyer = new Button(shiptypegroup, SWT.CHECK);
        this.destroyer.setText("駆逐艦");
        this.destroyer.setSelection(true);
        this.destroyer.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.lightCruiser = new Button(shiptypegroup, SWT.CHECK);
        this.lightCruiser.setText("軽巡洋艦");
        this.lightCruiser.setSelection(true);
        this.lightCruiser.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.torpedoCruiser = new Button(shiptypegroup, SWT.CHECK);
        this.torpedoCruiser.setText("重雷装巡洋艦");
        this.torpedoCruiser.setSelection(true);
        this.torpedoCruiser.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.heavyCruiser = new Button(shiptypegroup, SWT.CHECK);
        this.heavyCruiser.setText("重巡洋艦");
        this.heavyCruiser.setSelection(true);
        this.heavyCruiser.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.flyingDeckCruiser = new Button(shiptypegroup, SWT.CHECK);
        this.flyingDeckCruiser.setText("航空巡洋艦");
        this.flyingDeckCruiser.setSelection(true);
        this.flyingDeckCruiser.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.seaplaneTender = new Button(shiptypegroup, SWT.CHECK);
        this.seaplaneTender.setText("水上機母艦");
        this.seaplaneTender.setSelection(true);
        this.seaplaneTender.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.escortCarrier = new Button(shiptypegroup, SWT.CHECK);
        this.escortCarrier.setText("軽空母");
        this.escortCarrier.setSelection(true);
        this.escortCarrier.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.carrier = new Button(shiptypegroup, SWT.CHECK);
        this.carrier.setText("正規空母");
        this.carrier.setSelection(true);
        this.carrier.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.battleship = new Button(shiptypegroup, SWT.CHECK);
        this.battleship.setText("戦艦");
        this.battleship.setSelection(true);
        this.battleship.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.flyingDeckBattleship = new Button(shiptypegroup, SWT.CHECK);
        this.flyingDeckBattleship.setText("航空戦艦");
        this.flyingDeckBattleship.setSelection(true);
        this.flyingDeckBattleship.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.submarine = new Button(shiptypegroup, SWT.CHECK);
        this.submarine.setText("潜水艦");
        this.submarine.setSelection(true);
        this.submarine.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.carrierSubmarine = new Button(shiptypegroup, SWT.CHECK);
        this.carrierSubmarine.setText("潜水空母");
        this.carrierSubmarine.setSelection(true);
        this.carrierSubmarine.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.landingship = new Button(shiptypegroup, SWT.CHECK);
        this.landingship.setText("揚陸艦");
        this.landingship.setSelection(true);
        this.landingship.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.armoredcarrier = new Button(shiptypegroup, SWT.CHECK);
        this.armoredcarrier.setText("装甲空母");
        this.armoredcarrier.setSelection(true);
        this.armoredcarrier.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.repairship = new Button(shiptypegroup, SWT.CHECK);
        this.repairship.setText("工作艦");
        this.repairship.setSelection(true);
        this.repairship.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.selectall = new Button(shiptypegroup, SWT.CHECK);
        this.selectall.setText("全て選択");
        this.selectall.setSelection(true);
        this.selectall.addSelectionListener(new SelectAllSelectionAdapter());

        Group etcgroup = new Group(composite, SWT.NONE);
        etcgroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        etcgroup.setLayout(new GridLayout(2, false));
        etcgroup.setText("その他");

        Composite itemcomposite = new Composite(etcgroup, SWT.NONE);
        itemcomposite.setLayout(new RowLayout());
        itemcomposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL, SWT.CENTER, false, false, 2, 1));

        this.item = new Button(itemcomposite, SWT.CHECK);
        this.item.setText("装備");
        this.item.setSelection(false);
        this.item.addSelectionListener(new ItemCheckAdapter());
        this.item.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.itemcombo = new Combo(itemcomposite, SWT.READ_ONLY);
        this.itemcombo.setEnabled(false);
        this.itemcombo.addSelectionListener(new ApplyFilterSelectionAdapter());
        Set<String> items = new TreeSet<String>();
        for (ItemDto entry : GlobalContext.getItemMap().values()) {
            items.add(entry.getName());
        }
        for (String name : items) {
            this.itemcombo.add(name);
        }

        this.onfleet = new Button(etcgroup, SWT.CHECK);
        this.onfleet.setText("艦隊に所属");
        this.onfleet.setSelection(true);
        this.onfleet.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.notonfleet = new Button(etcgroup, SWT.CHECK);
        this.notonfleet.setText("艦隊に所属していない");
        this.notonfleet.setSelection(true);
        this.notonfleet.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.locked = new Button(etcgroup, SWT.CHECK);
        this.locked.setText("鍵付き");
        this.locked.setSelection(true);
        this.locked.addSelectionListener(new ApplyFilterSelectionAdapter());

        this.notlocked = new Button(etcgroup, SWT.CHECK);
        this.notlocked.setText("鍵付きではない");
        this.notlocked.setSelection(true);
        this.notlocked.addSelectionListener(new ApplyFilterSelectionAdapter());

        // 初期値を復元する
        if (this.filter != null) {
            // 名前
            if (!StringUtils.isEmpty(this.filter.nametext)) {
                this.nametext.setText(this.filter.nametext);
            }
            // 名前.正規表現を使用する
            this.regexp.setSelection(this.filter.regexp);

            // 艦種.駆逐艦
            this.destroyer.setSelection(this.filter.destroyer);
            // 艦種.軽巡洋艦
            this.lightCruiser.setSelection(this.filter.lightCruiser);
            // 艦種.重雷装巡洋艦
            this.torpedoCruiser.setSelection(this.filter.torpedoCruiser);
            // 艦種.重巡洋艦
            this.heavyCruiser.setSelection(this.filter.heavyCruiser);
            // 艦種.航空巡洋艦
            this.flyingDeckCruiser.setSelection(this.filter.flyingDeckCruiser);
            // 艦種.水上機母艦
            this.seaplaneTender.setSelection(this.filter.seaplaneTender);
            // 艦種.軽空母
            this.escortCarrier.setSelection(this.filter.escortCarrier);
            // 艦種.正規空母
            this.carrier.setSelection(this.filter.carrier);
            // 艦種.戦艦
            this.battleship.setSelection(this.filter.battleship);
            // 艦種.航空戦艦
            this.flyingDeckBattleship.setSelection(this.filter.flyingDeckBattleship);
            // 艦種.潜水艦
            this.submarine.setSelection(this.filter.submarine);
            // 艦種.潜水空母
            this.carrierSubmarine.setSelection(this.filter.carrierSubmarine);
            // 艦種.揚陸艦
            this.landingship.setSelection(this.filter.landingship);
            // 艦種.装甲空母
            this.armoredcarrier.setSelection(this.filter.armoredcarrier);
            // 艦種.工作艦
            this.repairship.setSelection(this.filter.repairship);

            if (!StringUtils.isEmpty(this.filter.itemname)) {
                // 装備
                this.item.setSelection(true);
                this.itemcombo.setEnabled(true);
                int index = 0;
                for (String name : items) {
                    if (this.filter.itemname.equals(name)) {
                        this.itemcombo.select(index);
                        break;
                    }
                    index++;
                }
            }
            // 艦隊に所属
            this.onfleet.setSelection(this.filter.onfleet);
            // 艦隊に非所属
            this.notonfleet.setSelection(this.filter.notonfleet);
            // 鍵付き
            this.locked.setSelection(this.filter.locked);
            // 鍵付きではない
            this.notlocked.setSelection(this.filter.notlocked);
        }

        this.shell.pack();
    }

    /**
     * フィルターを構成する
     * 
     * @return フィルター
     */
    private ShipFilterDto createFilter() {
        ShipFilterDto filter = this.shipTable.getFilter();
        filter.nametext = this.nametext.getText();
        filter.regexp = this.regexp.getSelection();
        filter.destroyer = this.destroyer.getSelection();
        filter.lightCruiser = this.lightCruiser.getSelection();
        filter.torpedoCruiser = this.torpedoCruiser.getSelection();
        filter.heavyCruiser = this.heavyCruiser.getSelection();
        filter.flyingDeckCruiser = this.flyingDeckCruiser.getSelection();
        filter.seaplaneTender = this.seaplaneTender.getSelection();
        filter.escortCarrier = this.escortCarrier.getSelection();
        filter.carrier = this.carrier.getSelection();
        filter.battleship = this.battleship.getSelection();
        filter.flyingDeckBattleship = this.flyingDeckBattleship.getSelection();
        filter.submarine = this.submarine.getSelection();
        filter.carrierSubmarine = this.carrierSubmarine.getSelection();
        filter.landingship = this.landingship.getSelection();
        filter.armoredcarrier = this.armoredcarrier.getSelection();
        filter.repairship = this.repairship.getSelection();
        if (ShipFilterDialog.this.item.getSelection()) {
            if (ShipFilterDialog.this.itemcombo.getSelectionIndex() >= 0) {
                filter.itemname = this.itemcombo.getItem(ShipFilterDialog.this.itemcombo
                        .getSelectionIndex());
            }
        } else {
            filter.itemname = null;
        }
        filter.onfleet = this.onfleet.getSelection();
        filter.notonfleet = this.notonfleet.getSelection();
        filter.locked = this.locked.getSelection();
        filter.notlocked = this.notlocked.getSelection();

        return filter;
    }

    /**
     * 装備を選択した時に装備のコンボボックスを制御する
     */
    private final class ItemCheckAdapter extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            ShipFilterDialog.this.itemcombo.setEnabled(ShipFilterDialog.this.item.getSelection());
        }
    }

    /**
     * フィルターを適用する
     */
    private final class SelectAllSelectionAdapter extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            boolean select = ShipFilterDialog.this.selectall.getSelection();

            ShipFilterDialog.this.destroyer.setSelection(select);
            ShipFilterDialog.this.lightCruiser.setSelection(select);
            ShipFilterDialog.this.torpedoCruiser.setSelection(select);
            ShipFilterDialog.this.heavyCruiser.setSelection(select);
            ShipFilterDialog.this.flyingDeckCruiser.setSelection(select);
            ShipFilterDialog.this.seaplaneTender.setSelection(select);
            ShipFilterDialog.this.escortCarrier.setSelection(select);
            ShipFilterDialog.this.carrier.setSelection(select);
            ShipFilterDialog.this.battleship.setSelection(select);
            ShipFilterDialog.this.flyingDeckBattleship.setSelection(select);
            ShipFilterDialog.this.submarine.setSelection(select);
            ShipFilterDialog.this.carrierSubmarine.setSelection(select);
            ShipFilterDialog.this.landingship.setSelection(select);
            ShipFilterDialog.this.armoredcarrier.setSelection(select);
            ShipFilterDialog.this.repairship.setSelection(select);

            ShipFilterDialog.this.shipTable.updateFilter(ShipFilterDialog.this.createFilter());
        }
    }

    /**
     * フィルターを適用する
     */
    private final class ApplyFilterModifyAdapter implements ModifyListener {
        @Override
        public void modifyText(ModifyEvent e) {
            ShipFilterDialog.this.shipTable.updateFilter(ShipFilterDialog.this.createFilter());
        }
    }

    /**
     * フィルターを適用する
     */
    private final class ApplyFilterSelectionAdapter extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            ShipFilterDialog.this.shipTable.updateFilter(ShipFilterDialog.this.createFilter());
        }
    }
}
