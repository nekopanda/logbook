package logbook.gui;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.dto.BattleDto;
import logbook.dto.BattleResultDto;
import logbook.dto.DockDto;
import logbook.dto.ItemInfoDto;
import logbook.dto.ShipDto;
import logbook.dto.ShipInfoDto;
import logbook.util.SwtUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * 戦闘詳細
 *
 */
public final class BattleDialog extends Dialog {

    private Shell shell;

    private final String id;

    /**
     * Create the dialog.
     * @param parent
     * @param id
     */
    public BattleDialog(Shell parent, String id) {
        super(parent, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE);

        this.id = id;
    }

    /**
     * Open the dialog.
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
        this.shell.setText("会敵報告");
        this.shell.setLayout(new GridLayout(1, false));

        //フォント取得
        FontData fontData = this.shell.getFont().getFontData()[0];
        String fontName = fontData.getName();
        int size = fontData.getHeight();

        int id = Integer.valueOf(this.id) - 1;
        BattleResultDto result = GlobalContext.getBattleResultList().get(id);
        BattleDto battle = null;

        // タイトル
        Label lblTitle = new Label(this.shell, SWT.NONE);
        lblTitle.setFont(SWTResourceManager.getFont(fontName, size, SWT.BOLD));
        lblTitle.setText("「" + result.getQuestName() + "」で作戦行動中に「" + result.getEnemyName() + "」と対峙しました ("
                + battle.getIntercept() + ")");

        Label lblsp1 = new Label(this.shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblsp1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Composite fComposite = new Composite(this.shell, SWT.NONE);
        fComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
        fComposite.setLayout(new GridLayout(9, false));

        // 味方艦隊
        List<DockDto> docks = battle.getFriends();
        for (int i = 0; i < docks.size(); i++) {
            DockDto dock = docks.get(i);
            List<ShipDto> ships = dock.getShips();

            Label lblfName = new Label(fComposite, SWT.NONE);
            lblfName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 9, 1));
            lblfName.setFont(SWTResourceManager.getFont(fontName, size, SWT.BOLD));
            if (i == 0) {
                lblfName.setText(dock.getName() + "(" + battle.getFriendFormation() + ")");
            } else {
                lblfName.setText(dock.getName());
            }

            new Label(fComposite, SWT.NONE);

            Label lblfHp = new Label(fComposite, SWT.NONE);
            lblfHp.setFont(SWTResourceManager.getFont(fontName, size, SWT.BOLD));
            lblfHp.setText("耐久");

            Label lblfCond = new Label(fComposite, SWT.NONE);
            lblfCond.setFont(SWTResourceManager.getFont(fontName, size, SWT.BOLD));
            lblfCond.setText("cond.");

            Label lblfSeiku = new Label(fComposite, SWT.NONE);
            lblfSeiku.setFont(SWTResourceManager.getFont(fontName, size, SWT.BOLD));
            lblfSeiku.setText("制空");

            Label lblfItem1 = new Label(fComposite, SWT.NONE);
            lblfItem1.setFont(SWTResourceManager.getFont(fontName, size, SWT.BOLD));
            lblfItem1.setText("装備1");
            GridData gdfItem1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gdfItem1.widthHint = SwtUtils.DPIAwareWidth(100);
            lblfItem1.setLayoutData(gdfItem1);

            Label lblfItem2 = new Label(fComposite, SWT.NONE);
            lblfItem2.setFont(SWTResourceManager.getFont(fontName, size, SWT.BOLD));
            lblfItem2.setText("装備2");
            GridData gdfItem2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gdfItem2.widthHint = SwtUtils.DPIAwareWidth(100);
            lblfItem2.setLayoutData(gdfItem2);

            Label lblfItem3 = new Label(fComposite, SWT.NONE);
            lblfItem3.setFont(SWTResourceManager.getFont(fontName, size, SWT.BOLD));
            lblfItem3.setText("装備3");
            GridData gdfItem3 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gdfItem3.widthHint = SwtUtils.DPIAwareWidth(100);
            lblfItem3.setLayoutData(gdfItem3);

            Label lblfItem4 = new Label(fComposite, SWT.NONE);
            lblfItem4.setFont(SWTResourceManager.getFont(fontName, size, SWT.BOLD));
            lblfItem4.setText("装備4");
            GridData gdfItem4 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gdfItem4.widthHint = SwtUtils.DPIAwareWidth(100);
            lblfItem4.setLayoutData(gdfItem4);

            Label lblfItem5 = new Label(fComposite, SWT.NONE);
            lblfItem5.setFont(SWTResourceManager.getFont(fontName, size, SWT.BOLD));
            lblfItem5.setText("装備5");
            GridData gdfItem5 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
            gdfItem5.widthHint = SwtUtils.DPIAwareWidth(100);
            lblfItem5.setLayoutData(gdfItem5);

            for (int j = 0; j < ships.size(); j++) {
                ShipDto ship = ships.get(j);

                Label lblLv = new Label(fComposite, SWT.NONE);
                lblLv.setText(ship.getName() + "(Lv" + ship.getLv() + ")");

                Label lblHp = new Label(fComposite, SWT.NONE);
                lblHp.setText(ship.getNowhp() + "/" + ship.getMaxhp());

                Label lblCond = new Label(fComposite, SWT.NONE);
                lblCond.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
                lblCond.setText(String.valueOf(ship.getCond()));

                Label lblSeiku = new Label(fComposite, SWT.NONE);
                lblSeiku.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
                lblSeiku.setText(String.valueOf(ship.getSeiku()));

                List<String> slots = ship.getSlot();

                for (String name : slots) {
                    Label lblSlot = new Label(fComposite, SWT.NONE);
                    lblSlot.setText(name);
                }
            }
        }

        Label lblsp2 = new Label(this.shell, SWT.SEPARATOR | SWT.HORIZONTAL);
        lblsp2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

        Composite eComposite = new Composite(this.shell, SWT.NONE);
        eComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        eComposite.setLayout(new GridLayout(7, false));

        Label lbleName = new Label(eComposite, SWT.NONE);
        lbleName.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 7, 1));
        lbleName.setFont(SWTResourceManager.getFont(fontName, size, SWT.BOLD));
        lbleName.setText(result.getEnemyName() + "(" + battle.getEnemyFormation() + ")");

        new Label(eComposite, SWT.NONE);

        Label lbleHp = new Label(eComposite, SWT.NONE);
        lbleHp.setFont(SWTResourceManager.getFont(fontName, size, SWT.BOLD));
        lbleHp.setText("耐久");

        Label lbleItem1 = new Label(eComposite, SWT.NONE);
        lbleItem1.setFont(SWTResourceManager.getFont(fontName, size, SWT.BOLD));
        lbleItem1.setText("装備1");
        GridData gdeItem1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdeItem1.widthHint = SwtUtils.DPIAwareWidth(100);
        lbleItem1.setLayoutData(gdeItem1);

        Label lbleItem2 = new Label(eComposite, SWT.NONE);
        lbleItem2.setFont(SWTResourceManager.getFont(fontName, size, SWT.BOLD));
        lbleItem2.setText("装備2");
        GridData gdeItem2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdeItem2.widthHint = SwtUtils.DPIAwareWidth(100);
        lbleItem2.setLayoutData(gdeItem2);

        Label lbleItem3 = new Label(eComposite, SWT.NONE);
        lbleItem3.setFont(SWTResourceManager.getFont(fontName, size, SWT.BOLD));
        lbleItem3.setText("装備3");
        GridData gdeItem3 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdeItem3.widthHint = SwtUtils.DPIAwareWidth(100);
        lbleItem3.setLayoutData(gdeItem3);

        Label lbleItem4 = new Label(eComposite, SWT.NONE);
        lbleItem4.setFont(SWTResourceManager.getFont(fontName, size, SWT.BOLD));
        lbleItem4.setText("装備4");
        GridData gdeItem4 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdeItem4.widthHint = SwtUtils.DPIAwareWidth(100);
        lbleItem4.setLayoutData(gdeItem4);

        Label lbleItem5 = new Label(eComposite, SWT.NONE);
        lbleItem5.setFont(SWTResourceManager.getFont(fontName, size, SWT.BOLD));
        lbleItem5.setText("装備5");
        GridData gdeItem5 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gdeItem5.widthHint = SwtUtils.DPIAwareWidth(100);
        lbleItem5.setLayoutData(gdeItem5);

        List<ShipInfoDto> enemyships = battle.getEnemy();
        List<ItemInfoDto[]> enemySlots = battle.getEnemySlot();

        for (int i = 0; i < enemyships.size(); i++) {
            ShipInfoDto ship = enemyships.get(i);
            ItemInfoDto[] slot = enemySlots.get(i);

            String name = ship.getName();
            if (!StringUtils.isEmpty(ship.getFlagship())) {
                name += "(" + ship.getFlagship() + ")";
            }

            Label lblName = new Label(eComposite, SWT.NONE);
            lblName.setText(name);

            Label lblHp = new Label(eComposite, SWT.NONE);
            lblHp.setText(battle.getNowEnemyHp()[i] + "/" + battle.getMaxEnemyHp()[i]);

            for (int j = 0; j < slot.length; j++) {
                Label lblSlot = new Label(eComposite, SWT.NONE);
                if (slot[j] != null) {
                    lblSlot.setText(slot[j].getName());
                } else {
                    lblSlot.setText("");
                }
            }
        }

        Button save = new Button(this.shell, SWT.NONE);
        save.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        save.setText("HTMLファイルとして保存");
        save.addSelectionListener(new SaveAdapter());

        this.shell.pack();
    }

    private String getHtml() {
        // 文書を用意する
        // freemarker使いたい

        int id = Integer.valueOf(this.id) - 1;
        BattleResultDto result = GlobalContext.getBattleResultList().get(id);
        BattleDto battle = null;

        String time = new SimpleDateFormat(AppConstants.DATE_FORMAT).format(result.getBattleDate());
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>").append("\r\n");
        sb.append("<html>").append("\r\n");
        sb.append("<head>").append("\r\n");
        sb.append("<meta charset=\"UTF-8\">").append("\r\n");
        sb.append("<title>会敵報告</title>").append("\r\n");
        sb.append("<style type=\"text/css\">").append("\r\n");
        sb.append("body,table {").append("\r\n");
        sb.append(" font-family: Meiryo, CI, Arial, Helvetica, Clean,").append("\r\n");
        sb.append("     \"Hiragino Kaku Gothic Pro\", \"qMmpS Pro W3\", Osaka, \"MS P Gothic\",").append("\r\n");
        sb.append("     \"lr oSVbN\", sans-serif;").append("\r\n");
        sb.append("}").append("\r\n");
        sb.append("</style>").append("\r\n");
        sb.append("</head>").append("\r\n");
        sb.append("<body>").append("\r\n");
        sb.append(" <strong>「" + result.getQuestName() + "」で作戦行動中に「" + result.getEnemyName() + "」と対峙しました ("
                + battle.getIntercept() + ")(" + time + ")</strong>").append("\r\n");
        sb.append(" <hr>").append("\r\n");

        List<DockDto> docks = battle.getFriends();
        for (int i = 0; i < docks.size(); i++) {
            DockDto dock = docks.get(i);
            List<ShipDto> ships = dock.getShips();
            sb.append(" <table>").append("\r\n");
            if (i == 0) {
                sb.append(
                        "     <caption>" + dock.getName() + "(" + battle.getFriendFormation() + ")" + "</caption>")
                        .append("\r\n");
            } else {
                sb.append("     <caption>" + dock.getName() + "</caption>").append("\r\n");
            }
            sb.append("     <thead>").append("\r\n");
            sb.append("         <tr>").append("\r\n");
            sb.append("             <th></th>").append("\r\n");
            sb.append("             <th>耐久</th>").append("\r\n");
            sb.append("             <th>cond.</th>").append("\r\n");
            sb.append("             <th>制空</th>").append("\r\n");
            sb.append("             <th>装備1</th>").append("\r\n");
            sb.append("             <th>装備2</th>").append("\r\n");
            sb.append("             <th>装備3</th>").append("\r\n");
            sb.append("             <th>装備4</th>").append("\r\n");
            sb.append("             <th>装備5</th>").append("\r\n");
            sb.append("         </tr>").append("\r\n");
            sb.append("     </thead>").append("\r\n");
            sb.append("     <tbody>").append("\r\n");

            for (int j = 0; j < ships.size(); j++) {
                ShipDto ship = ships.get(j);

                sb.append("         <tr>").append("\r\n");
                sb.append("             <td>" + ship.getName() + "(Lv" + ship.getLv() + ")" + "</td>").append(
                        "\r\n");
                sb.append("             <td>" + ship.getNowhp() + "/" + ship.getMaxhp() + "</td>").append("\r\n");
                sb.append("             <td>" + String.valueOf(ship.getCond()) + "</td>").append("\r\n");
                sb.append("             <td>" + String.valueOf(ship.getSeiku()) + "</td>").append("\r\n");

                List<String> slots = ship.getSlot();
                for (String name : slots) {
                    sb.append("             <td>" + name + "</td>").append("\r\n");
                }
                sb.append("         </tr>").append("\r\n");
            }
            sb.append("     </tbody>").append("\r\n");
            sb.append(" </table>").append("\r\n");
        }

        sb.append(" <hr>").append("\r\n");
        sb.append(" <table>").append("\r\n");
        sb.append("     <caption>" + result.getEnemyName() + "(" + battle.getEnemyFormation() + ")" + "</caption>")
                .append("\r\n");
        sb.append("     <thead>").append("\r\n");
        sb.append("         <tr>").append("\r\n");
        sb.append("             <th></th>").append("\r\n");
        sb.append("             <th>耐久</th>").append("\r\n");
        sb.append("             <th>装備1</th>").append("\r\n");
        sb.append("             <th>装備2</th>").append("\r\n");
        sb.append("             <th>装備3</th>").append("\r\n");
        sb.append("             <th>装備4</th>").append("\r\n");
        sb.append("             <th>装備5</th>").append("\r\n");
        sb.append("         </tr>").append("\r\n");
        sb.append("     </thead>").append("\r\n");
        sb.append("     <tbody>").append("\r\n");

        List<ShipInfoDto> enemyships = battle.getEnemy();
        List<ItemInfoDto[]> enemySlots = battle.getEnemySlot();
        for (int i = 0; i < enemyships.size(); i++) {
            ShipInfoDto ship = enemyships.get(i);
            ItemInfoDto[] slot = enemySlots.get(i);

            String name = ship.getName();
            if (!StringUtils.isEmpty(ship.getFlagship())) {
                name += "(" + ship.getFlagship() + ")";
            }

            sb.append("         <tr>").append("\r\n");
            sb.append("             <td>" + name + "</td>").append("\r\n");
            sb.append("             <td>" + battle.getNowEnemyHp()[i] + "/" + battle.getMaxEnemyHp()[i] + "</td>")
                    .append("\r\n");
            for (int j = 0; j < slot.length; j++) {
                if (slot[j] != null) {
                    sb.append("             <td>" + slot[j].getName() + "</td>").append("\r\n");
                } else {
                    sb.append("             <td></td>").append("\r\n");
                }
            }
            sb.append("         </tr>").append("\r\n");
        }

        sb.append("     </tbody>").append("\r\n");
        sb.append(" </table>").append("\r\n");
        sb.append("</body>").append("\r\n");
        sb.append("</html>").append("\r\n");

        return sb.toString();
    }

    /**
     * HTMLファイルとして保存のリスナー
     *
     */
    private class SaveAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {

            FileDialog dialog = new FileDialog(BattleDialog.this.shell, SWT.SAVE);
            dialog.setFileName("会敵報告.html");
            dialog.setFilterExtensions(new String[] { "*.html" });
            String filename = dialog.open();
            if (filename != null) {
                File file = new File(filename);
                if (file.exists()) {
                    MessageBox messageBox = new MessageBox(BattleDialog.this.shell, SWT.YES | SWT.NO);
                    messageBox.setText("確認");
                    messageBox.setMessage("指定されたファイルは存在します。\n上書きしますか？");
                    if (messageBox.open() == SWT.NO) {
                        return;
                    }
                }
                try {
                    FileUtils.writeStringToFile(file, BattleDialog.this.getHtml(), "UTF-8");
                } catch (IOException ex) {
                    MessageBox messageBox = new MessageBox(BattleDialog.this.shell, SWT.ICON_ERROR);
                    messageBox.setText("書き込めませんでした");
                    messageBox.setMessage(ex.toString());
                    messageBox.open();
                }
            }
        }

    }

}
