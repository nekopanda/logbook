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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * 所有艦娘一覧で使用するフィルターダイアログ
 * 
 */
public final class ShipFilterDialog extends Composite {

    private final ShipTable shipTable;

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
    /** 艦種.潜水母艦 */
    private Button submarineTender;
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
    public ShipFilterDialog(ShipTable shipTable, Composite parent) {
        super(parent, SWT.NONE);
        this.shipTable = shipTable;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        GridLayout glShell = new GridLayout(3, false);
        glShell.verticalSpacing = 0;
        glShell.horizontalSpacing = 0;
        glShell.marginHeight = 0;
        glShell.marginWidth = 0;
        this.setLayout(glShell);

        SelectionListener listener = new ApplyFilterSelectionAdapter();

        Group shiptypegroup = new Group(this, SWT.NONE);
        shiptypegroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout glShiptypegroup = new GridLayout(3, false);
        glShiptypegroup.verticalSpacing = 2;
        glShiptypegroup.horizontalSpacing = 2;
        glShiptypegroup.marginHeight = 2;
        glShiptypegroup.marginWidth = 2;
        shiptypegroup.setLayout(glShiptypegroup);
        shiptypegroup.setText("艦種");

        this.destroyer = new Button(shiptypegroup, SWT.CHECK);
        this.destroyer.setText("駆逐艦");
        this.destroyer.setSelection(true);
        this.destroyer.addSelectionListener(listener);

        this.lightCruiser = new Button(shiptypegroup, SWT.CHECK);
        this.lightCruiser.setText("軽巡洋艦");
        this.lightCruiser.setSelection(true);
        this.lightCruiser.addSelectionListener(listener);

        this.torpedoCruiser = new Button(shiptypegroup, SWT.CHECK);
        this.torpedoCruiser.setText("重雷装巡洋艦");
        this.torpedoCruiser.setSelection(true);
        this.torpedoCruiser.addSelectionListener(listener);

        this.heavyCruiser = new Button(shiptypegroup, SWT.CHECK);
        this.heavyCruiser.setText("重巡洋艦");
        this.heavyCruiser.setSelection(true);
        this.heavyCruiser.addSelectionListener(listener);

        this.flyingDeckCruiser = new Button(shiptypegroup, SWT.CHECK);
        this.flyingDeckCruiser.setText("航空巡洋艦");
        this.flyingDeckCruiser.setSelection(true);
        this.flyingDeckCruiser.addSelectionListener(listener);

        this.seaplaneTender = new Button(shiptypegroup, SWT.CHECK);
        this.seaplaneTender.setText("水上機母艦");
        this.seaplaneTender.setSelection(true);
        this.seaplaneTender.addSelectionListener(listener);

        this.escortCarrier = new Button(shiptypegroup, SWT.CHECK);
        this.escortCarrier.setText("軽空母");
        this.escortCarrier.setSelection(true);
        this.escortCarrier.addSelectionListener(listener);

        this.carrier = new Button(shiptypegroup, SWT.CHECK);
        this.carrier.setText("正規空母");
        this.carrier.setSelection(true);
        this.carrier.addSelectionListener(listener);

        this.battleship = new Button(shiptypegroup, SWT.CHECK);
        this.battleship.setText("戦艦");
        this.battleship.setSelection(true);
        this.battleship.addSelectionListener(listener);

        this.flyingDeckBattleship = new Button(shiptypegroup, SWT.CHECK);
        this.flyingDeckBattleship.setText("航空戦艦");
        this.flyingDeckBattleship.setSelection(true);
        this.flyingDeckBattleship.addSelectionListener(listener);

        this.submarine = new Button(shiptypegroup, SWT.CHECK);
        this.submarine.setText("潜水艦");
        this.submarine.setSelection(true);
        this.submarine.addSelectionListener(listener);

        this.carrierSubmarine = new Button(shiptypegroup, SWT.CHECK);
        this.carrierSubmarine.setText("潜水空母");
        this.carrierSubmarine.setSelection(true);
        this.carrierSubmarine.addSelectionListener(listener);

        this.landingship = new Button(shiptypegroup, SWT.CHECK);
        this.landingship.setText("揚陸艦");
        this.landingship.setSelection(true);
        this.landingship.addSelectionListener(listener);

        this.armoredcarrier = new Button(shiptypegroup, SWT.CHECK);
        this.armoredcarrier.setText("装甲空母");
        this.armoredcarrier.setSelection(true);
        this.armoredcarrier.addSelectionListener(listener);

        this.repairship = new Button(shiptypegroup, SWT.CHECK);
        this.repairship.setText("工作艦");
        this.repairship.setSelection(true);
        this.repairship.addSelectionListener(listener);

        this.submarineTender = new Button(shiptypegroup, SWT.CHECK);
        this.submarineTender.setText("潜水母艦");
        this.submarineTender.setSelection(true);
        this.submarineTender.addSelectionListener(listener);

        this.selectall = new Button(shiptypegroup, SWT.CHECK);
        this.selectall.setText("全て選択");
        this.selectall.setSelection(true);
        this.selectall.addSelectionListener(new SelectAllSelectionAdapter());

        Group etcgroup = new Group(this, SWT.NONE);
        etcgroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout glEtcgroup = new GridLayout(2, false);
        glEtcgroup.horizontalSpacing = 2;
        glEtcgroup.marginHeight = 2;
        glEtcgroup.marginWidth = 2;
        etcgroup.setLayout(glEtcgroup);
        etcgroup.setText("その他");

        this.item = new Button(etcgroup, SWT.CHECK);
        this.item.setText("装備");
        this.item.setSelection(false);
        this.item.addSelectionListener(listener);

        this.onfleet = new Button(etcgroup, SWT.CHECK);
        this.onfleet.setText("艦隊に所属");
        this.onfleet.setSelection(true);
        this.onfleet.addSelectionListener(listener);

        this.notonfleet = new Button(etcgroup, SWT.CHECK);
        this.notonfleet.setText("艦隊に所属していない");
        this.notonfleet.setSelection(true);
        this.notonfleet.addSelectionListener(listener);

        this.locked = new Button(etcgroup, SWT.CHECK);
        this.locked.setText("鍵付き");
        this.locked.setSelection(true);
        this.locked.addSelectionListener(listener);

        this.notlocked = new Button(etcgroup, SWT.CHECK);
        this.notlocked.setText("鍵付きではない");
        this.notlocked.setSelection(true);
        this.notlocked.addSelectionListener(listener);

        Group namegroup = new Group(this, SWT.NONE);
        namegroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        namegroup.setLayout(new GridLayout(2, false));
        namegroup.setText("フリーワード検索(半角SPでAND検索)");

        this.nametext = new Text(namegroup, SWT.BORDER);
        this.nametext.setLayoutData(new RowData(180, SWT.DEFAULT));
        this.nametext.addModifyListener(new ApplyFilterModifyAdapter());

        this.regexp = new Button(namegroup, SWT.CHECK);
        this.regexp.setText("正規表現");
        this.regexp.addSelectionListener(listener);

        this.itemcombo = new Combo(namegroup, SWT.READ_ONLY);
        this.itemcombo.setEnabled(false);
        this.itemcombo.addSelectionListener(listener);
        Set<String> items = new TreeSet<String>();
        for (ItemDto entry : GlobalContext.getItemMap().values()) {
            items.add(entry.getName());
        }
        for (String name : items) {
            this.itemcombo.add(name);
        }
        this.item.addSelectionListener(new CheckAdapter(this.item, this.itemcombo));

        // 初期値を復元する
        ShipFilterDto filter = this.shipTable.getFilter();
        if (filter != null) {
            // 名前
            if (!StringUtils.isEmpty(filter.nametext)) {
                this.nametext.setText(filter.nametext);
            }
            // 名前.正規表現を使用する
            this.regexp.setSelection(filter.regexp);

            // 艦種.駆逐艦
            this.destroyer.setSelection(filter.destroyer);
            // 艦種.軽巡洋艦
            this.lightCruiser.setSelection(filter.lightCruiser);
            // 艦種.重雷装巡洋艦
            this.torpedoCruiser.setSelection(filter.torpedoCruiser);
            // 艦種.重巡洋艦
            this.heavyCruiser.setSelection(filter.heavyCruiser);
            // 艦種.航空巡洋艦
            this.flyingDeckCruiser.setSelection(filter.flyingDeckCruiser);
            // 艦種.水上機母艦
            this.seaplaneTender.setSelection(filter.seaplaneTender);
            // 艦種.軽空母
            this.escortCarrier.setSelection(filter.escortCarrier);
            // 艦種.正規空母
            this.carrier.setSelection(filter.carrier);
            // 艦種.戦艦
            this.battleship.setSelection(filter.battleship);
            // 艦種.航空戦艦
            this.flyingDeckBattleship.setSelection(filter.flyingDeckBattleship);
            // 艦種.潜水艦
            this.submarine.setSelection(filter.submarine);
            // 艦種.潜水空母
            this.carrierSubmarine.setSelection(filter.carrierSubmarine);
            // 艦種.揚陸艦
            this.landingship.setSelection(filter.landingship);
            // 艦種.装甲空母
            this.armoredcarrier.setSelection(filter.armoredcarrier);
            // 艦種.工作艦
            this.repairship.setSelection(filter.repairship);
            // 艦種.潜水母艦
            this.submarineTender.setSelection(filter.submarineTender);

            // 艦隊に所属
            this.onfleet.setSelection(filter.onfleet);
            // 艦隊に非所属
            this.notonfleet.setSelection(filter.notonfleet);
            // 鍵付き
            this.locked.setSelection(filter.locked);
            // 鍵付きではない
            this.notlocked.setSelection(filter.notlocked);
        }
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
        filter.submarineTender = this.submarineTender.getSelection();
        filter.group = null;
        filter.onfleet = this.onfleet.getSelection();
        filter.notonfleet = this.notonfleet.getSelection();
        filter.locked = this.locked.getSelection();
        filter.notlocked = this.notlocked.getSelection();

        return filter;
    }

    /**
     * 選択した時にコンボボックスを制御する
     */
    private final class CheckAdapter extends SelectionAdapter {

        private final Button button;
        private final Composite composite;

        public CheckAdapter(Button button, Composite composite) {
            this.button = button;
            this.composite = composite;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            this.composite.setEnabled(this.button.getSelection());
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
            ShipFilterDialog.this.submarineTender.setSelection(select);

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
