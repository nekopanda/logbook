package logbook.gui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import logbook.constants.AppConstants;
import logbook.data.Data;
import logbook.data.DataType;
import logbook.dto.BattleExDto;
import logbook.dto.BattleResultDto;
import logbook.gui.logic.BattleHtmlGenerator;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.GuiUpdator;
import logbook.gui.logic.TableItemCreator;
import logbook.gui.logic.TableRowHeader;
import logbook.internal.BattleResultFilter;
import logbook.internal.BattleResultServer;
import logbook.internal.LoggerHolder;
import logbook.internal.TimeSpanKind;
import logbook.scripting.TableItemCreatorProxy;

import org.apache.commons.io.FileUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * ドロップ報告書
 *
 */
public final class DropReportTable extends AbstractTableDialog {
    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(DropReportTable.class);

    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss.SSS");

    private BattleResultFilter filter = new BattleResultFilter();

    private BattleFilterDialog battleFilterDialog;

    private final BattleDetailDialog detailDialog;

    /**
     * @param parent
     */
    public DropReportTable(Shell parent, MenuItem menuItem) {
        super(parent, menuItem);
        this.detailDialog = new BattleDetailDialog(parent);
        this.filter.printPractice = false;
        this.filter.timeSpan = TimeSpanKind.LAST_24HOURS;
    }

    public void updateFilter(BattleResultFilter filter) {
        this.filter = filter;
        this.reloadTable();
    }

    @Override
    protected void createContents() {

        final MenuItem reloadDB = new MenuItem(this.opemenu, SWT.NONE);
        reloadDB.setText("データベースを再読み込み");
        reloadDB.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                MessageBox box = new MessageBox(DropReportTable.this.shell, SWT.YES | SWT.NO | SWT.ICON_QUESTION);
                box.setText("データベース再読み込み");
                box.setMessage("件数によっては時間がかかることがあります。よろしいですか？");

                if (box.open() == SWT.YES) {
                    BattleResultServer.get().reloadFiles();
                    DropReportTable.this.reloadTable();
                }
            }
        });

        this.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                int selected = DropReportTable.this.table.getSelectionIndex();
                if (selected != -1) {
                    BattleResultDto result = DropReportTable.this.getItemFromIndex(selected);
                    DropReportTable.this.detailDialog.open();
                    DropReportTable.this.detailDialog.setBattle(
                            DropReportTable.this.getHTMLOfItem(result, false),
                            DropReportTable.this.getBattleTitle(result));
                }
            }
        });
        SelectionListener filterListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (DropReportTable.this.battleFilterDialog == null)
                    DropReportTable.this.battleFilterDialog =
                            new BattleFilterDialog(DropReportTable.this);
                DropReportTable.this.battleFilterDialog.open();
            }
        };
        if (!this.isNoMenubar()) {
            // フィルターメニュー
            final MenuItem filter = new MenuItem(this.menubar, SWT.PUSH);
            filter.setText("フィルター");
            filter.setAccelerator(SWT.CTRL + 'F');
            filter.addSelectionListener(filterListener);
        }
        // セパレータ
        new MenuItem(this.tablemenu, SWT.SEPARATOR);
        // 右クリックメニューに追加する
        final MenuItem filtertable = new MenuItem(this.tablemenu, SWT.NONE);
        filtertable.setText("フィルター(&F)\tCtrl+F");
        filtertable.addSelectionListener(filterListener);
        // 保存メニュー
        final MenuItem save = new MenuItem(this.tablemenu, SWT.NONE);
        save.setText("選択したログを保存する");
        save.addSelectionListener(new SaveAdapter());

        // データの更新を受け取る
        final Runnable listener = new GuiUpdator(new Runnable() {
            @Override
            public void run() {
                DropReportTable.this.reloadTable();
            }
        });
        BattleResultServer.addListener(listener);
        this.shell.addListener(SWT.Dispose, new Listener() {
            @Override
            public void handleEvent(Event event) {
                BattleResultServer.removeListener(listener);
            }
        });
    }

    private BattleResultDto getItemFromIndex(int index) {
        TableRowHeader rowHeader = (TableRowHeader) DropReportTable.this.body.get(index)[0];
        return (BattleResultDto) rowHeader.get();
    }

    private String getBattleTitle(BattleResultDto item) {
        if (item.isPractice()) {
            return "演習報告: " + item.getEnemyName();
        }
        return "会敵報告: " + item.getMapCell().detailedString();
    }

    private String getOutputFileName(BattleResultDto item) {
        String rank = item.getRank().toString();
        if (item.isPractice()) {
            return dateFormat.format(item.getBattleDate()) + "演習" + rank;
        }
        else {
            int[] map = item.getMapCell().getMap();
            return dateFormat.format(item.getBattleDate()) +
                    " " + map[0] + "-" + map[1] + "-" + map[2] + " " + rank;
        }
    }

    private String getHTMLOfItem(BattleResultDto item, boolean forFile) {
        String title = this.getBattleTitle(item);
        BattleExDto detail = BattleResultServer.get().getBattleDetail(item);
        try {
            BattleHtmlGenerator gen = new BattleHtmlGenerator();
            return gen.generateHTML(title, item, detail, forFile);
        } catch (IOException e) {
            LOG.get().warn("会敵報告作成に失敗: CSSファイル読み込みに失敗しました", e);
        } catch (Exception e) {
            ApplicationMain.main.printMessage("会敵報告作成に失敗しました");
            LOG.get().warn("会敵報告作成に失敗", e);
        }
        return null;
    }

    private void writeToFile(BattleResultDto item, File file) {
        Shell shell = this.getShell();
        try {
            FileUtils.writeStringToFile(file, this.getHTMLOfItem(item, true), "UTF-8");
        } catch (IOException ex) {
            MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
            messageBox.setText("書き込めませんでした");
            messageBox.setMessage(ex.toString());
            messageBox.open();
        }
    }

    private void writeToZipFile(List<BattleResultDto> items, File file) {
        Shell shell = this.getShell();
        ZipOutputStream zipOutStream = null;
        List<String> failedList = new ArrayList<String>();
        try {
            zipOutStream = new ZipOutputStream(
                    new BufferedOutputStream(new FileOutputStream(file)));
            for (BattleResultDto item : items) {
                String fileName = this.getOutputFileName(item) + ".html";
                String html = this.getHTMLOfItem(item, true);
                if (html != null) {
                    final ZipEntry entry = new ZipEntry(fileName);
                    try {
                        zipOutStream.putNextEntry(entry);
                        zipOutStream.write(html.getBytes(Charset.forName("UTF-8")));
                        zipOutStream.closeEntry();
                    } catch (ZipException e) {
                        failedList.add(fileName);
                    }
                }
                else {
                    failedList.add(fileName);
                }
            }
            zipOutStream.finish();
            zipOutStream.close();
            if (failedList.size() > 0) {
                StringBuilder sb = new StringBuilder("以下のファイルを書き込めませんでした\r\n");
                for (String failedFile : failedList) {
                    sb.append(failedFile).append("\r\n");
                }
                MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
                messageBox.setText("一部でエラーが発生しました");
                messageBox.setMessage(sb.toString());
                messageBox.open();
            }
        } catch (IOException ex) {
            MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR);
            messageBox.setText("書き込めませんでした");
            messageBox.setMessage(ex.toString());
            messageBox.open();
        } finally {
            if (zipOutStream != null) {
                try {
                    zipOutStream.close();
                } catch (IOException e) {
                    //
                }
            }
        }
    }

    private List<BattleResultDto> getSelectedItemList() {
        int[] selectedIndices = this.table.getSelectionIndices();
        List<BattleResultDto> ret = new ArrayList<BattleResultDto>();
        for (int index : selectedIndices) {
            ret.add(this.getItemFromIndex(index));
        }
        return ret;
    }

    public BattleResultFilter getFilter() {
        return this.filter;
    }

    @Override
    protected String getTitleMain() {
        return "ドロップ報告書";
    }

    @Override
    protected Point getSize() {
        return new Point(600, 350);
    }

    @Override
    protected String[] getTableHeader() {
        return CreateReportLogic.getBattleResultHeader();
    }

    @Override
    protected void updateTableBody() {
        this.body = CreateReportLogic.getBattleResultBody(this.filter);
    }

    @Override
    protected TableItemCreator getTableItemCreator() {
        //return CreateReportLogic.DEFAULT_TABLE_ITEM_CREATOR;
        return TableItemCreatorProxy.get(AppConstants.DROPTABLE_PREFIX);
    }

    /**
     * HTMLファイルとして保存のリスナー
     *
     */
    private class SaveAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (DropReportTable.this.table.getSelectionIndex() == -1) {
                // 選択されていないときは返る
                return;
            }

            List<BattleResultDto> selectedItems = DropReportTable.this.getSelectedItemList();
            Shell shell = DropReportTable.this.getShell();
            FileDialog dialog = new FileDialog(shell, SWT.SAVE);
            String fileName = DropReportTable.this.getOutputFileName(selectedItems.get(0));

            if (selectedItems.size() > 1) {
                // ２つ以上選択されているときはzip出力
                dialog.setFileName(fileName + ".zip");
                dialog.setFilterExtensions(new String[] { "*.zip" });
            }
            else {
                dialog.setFileName(fileName + ".html");
                dialog.setFilterExtensions(new String[] { "*.html" });
            }

            String filename = dialog.open();
            if (filename != null) {
                File file = new File(filename);
                if (file.exists()) {
                    MessageBox messageBox = new MessageBox(shell, SWT.YES | SWT.NO);
                    messageBox.setText("確認");
                    messageBox.setMessage("指定されたファイルは存在します。\n上書きしますか？");
                    if (messageBox.open() == SWT.NO) {
                        return;
                    }
                }
                if (selectedItems.size() > 1) {
                    DropReportTable.this.writeToZipFile(selectedItems, file);
                }
                else {
                    DropReportTable.this.writeToFile(selectedItems.get(0), file);
                }
            }
        }

    }

    /**
     * 更新する必要のあるデータ
     */
    @Override
    public void update(DataType type, Data data) {
        // BattleResultServerから直接更新を受け取るので何もしない
    }
}
