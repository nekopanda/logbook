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
import java.util.zip.ZipOutputStream;

import logbook.constants.AppConstants;
import logbook.dto.BattleExDto;
import logbook.dto.BattleResultDto;
import logbook.gui.logic.BattleHtmlGenerator;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.TableItemCreator;
import logbook.gui.logic.TableRowHeader;
import logbook.internal.BattleResultFilter;
import logbook.internal.BattleResultServer;

import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;

/**
 * ドロップ報告書
 *
 */
public final class DropReportTable extends AbstractTableDialog {
    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(DropReportTable.class);

    private static DateFormat dateFormat = new SimpleDateFormat(AppConstants.DATE_FORMAT);

    private BattleResultFilter filter = new BattleResultFilter();

    private final BattleDetailDialog detailDialog;

    /**
     * @param parent
     */
    public DropReportTable(Shell parent, MenuItem menuItem) {
        super(parent, menuItem);
        this.detailDialog = new BattleDetailDialog(this);
    }

    public void updateFilter(BattleResultFilter filter) {
        this.filter = filter;
        this.reloadTable();
    }

    @Override
    protected void createContents() {
        this.table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                int selected = DropReportTable.this.table.getSelectionIndex();
                if (selected != -1) {
                    BattleResultDto result = DropReportTable.this.getItemFromIndex(selected);
                    DropReportTable.this.detailDialog.setBattle(
                            DropReportTable.this.getHTMLOfItem(result, false),
                            DropReportTable.this.getBattleTitle(result));
                    DropReportTable.this.detailDialog.open();
                }
            }
        });
        // フィルターメニュー
        final MenuItem filter = new MenuItem(this.opemenu, SWT.PUSH);
        filter.setText("フィルター(&F)\tCtrl+F");
        filter.setAccelerator(SWT.CTRL + 'F');
        filter.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new BattleFilterDialog(DropReportTable.this).open();
            }
        });
        // セパレータ
        new MenuItem(this.tablemenu, SWT.SEPARATOR);
        // 右クリックメニューに追加する
        final MenuItem filtertable = new MenuItem(this.tablemenu, SWT.NONE);
        filtertable.setText("フィルター(&F)");
        filtertable.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                new BattleFilterDialog(DropReportTable.this).open();
            }
        });
    }

    private BattleResultDto getItemFromIndex(int index) {
        TableRowHeader rowHeader = (TableRowHeader) DropReportTable.this.body.get(index)[0];
        return (BattleResultDto) rowHeader.get();
    }

    private String getBattleTitle(BattleResultDto item) {
        return "会敵報告: " + item.getMapCell().detailedString();
    }

    private String getOutputFileName(BattleResultDto item) {
        int[] map = item.getMapCell().getMap();
        String rank = item.getRank();
        return dateFormat.format(item.getBattleDate()) +
                " " + map[0] + "-" + map[1] + "-" + map[2] + " " + rank;
    }

    private String getHTMLOfItem(BattleResultDto item, boolean forFile) {
        String title = this.getBattleTitle(item);
        BattleExDto detail = BattleResultServer.get().getBattleDetail(item);
        try {
            BattleHtmlGenerator gen = new BattleHtmlGenerator();
            return gen.generateHTML(title, item, detail, forFile);
        } catch (IOException e) {
            LOG.warn("会敵報告作成に失敗: CSSファイル読み込みに失敗しました", e);
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
        try {
            zipOutStream = new ZipOutputStream(
                    new BufferedOutputStream(new FileOutputStream(file)));
            for (BattleResultDto item : items) {
                final ZipEntry entry = new ZipEntry(this.getOutputFileName(item));
                zipOutStream.putNextEntry(entry);
                zipOutStream.write(
                        this.getHTMLOfItem(item, true).getBytes(Charset.forName("UTF-8")));
                zipOutStream.closeEntry();
            }
            zipOutStream.finish();
            zipOutStream.close();
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
    protected String getTitle() {
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
        return CreateReportLogic.DEFAULT_TABLE_ITEM_CREATOR;
    }

    @Override
    protected SelectionListener getHeaderSelectionListener() {
        return new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (e.getSource() instanceof TableColumn) {
                    DropReportTable.this.sortTableItems((TableColumn) e.getSource());
                }
            }
        };
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
}
