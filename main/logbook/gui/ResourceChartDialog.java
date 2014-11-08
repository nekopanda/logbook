package logbook.gui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import logbook.config.AppConfig;
import logbook.constants.AppConstants;
import logbook.dto.chart.Resource;
import logbook.dto.chart.ResourceLog;
import logbook.dto.chart.ResourceLog.SortableLog;
import logbook.gui.logic.CreateReportLogic;
import logbook.gui.logic.ResourceChart;
import logbook.gui.logic.TableItemCreator;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * 資材チャートのダイアログ
 *
 */
public final class ResourceChartDialog extends Dialog {

    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(ResourceChartDialog.class);

    /** スケールテキスト */
    private static final String[] SCALE_TEXT = { "1日", "1週間", "2週間", "1ヶ月", "2ヶ月", "3ヶ月", "半年", "1年" };
    /** スケールテキストに対応する日 */
    private static final int[] SCALE_DAYS = { 1, 7, 14, 30, 60, 90, 180, 365 };
    /** 資材テーブルに表示する資材のフォーマット */
    private static final String DIFF_FORMAT = "{0,number,0}({1,number,+0;-0})";

    /** シェル */
    private Shell shell;
    /** メニューバー */
    private Menu menubar;
    /** [ファイル]メニュー */
    private Menu filemenu;
    /** スケール */
    private Combo combo;
    /** グラフキャンバス */
    private Canvas canvas;
    /** 資材ログ */
    private ResourceLog log;
    /** 資材テーブル */
    private Table table;
    /** 資材テーブルのヘッダ */
    private final String[] header = Arrays.copyOfRange(CreateReportLogic.getMaterialHeader(), 1, 6);
    /** 資材テーブルのボディ */
    private final List<String[]> body = new ArrayList<>();

    /**
     * Create the dialog.
     * @param parent
     */
    public ResourceChartDialog(Shell parent) {
        super(parent, SWT.SHELL_TRIM);
        this.setText("資材チャート");
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
        this.shell.setMinimumSize(450, 300);
        this.shell.setSize(800, 650);
        this.shell.setText(this.getText());
        GridLayout glShell = new GridLayout(1, false);
        glShell.verticalSpacing = 2;
        glShell.marginWidth = 2;
        glShell.marginHeight = 2;
        glShell.horizontalSpacing = 2;
        this.shell.setLayout(glShell);

        this.menubar = new Menu(this.shell, SWT.BAR);
        this.shell.setMenuBar(this.menubar);

        MenuItem fileroot = new MenuItem(this.menubar, SWT.CASCADE);
        fileroot.setText("ファイル");
        this.filemenu = new Menu(fileroot);
        fileroot.setMenu(this.filemenu);

        MenuItem save = new MenuItem(this.filemenu, SWT.NONE);
        save.setText("画像ファイルとして保存(&S)\tCtrl+S");
        save.setAccelerator(SWT.CTRL + 'S');

        SashForm sashForm = new SashForm(this.shell, SWT.SMOOTH | SWT.VERTICAL);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        Composite compositeChart = new Composite(sashForm, SWT.NONE);
        GridLayout glCompositeChart = new GridLayout(2, false);
        glCompositeChart.verticalSpacing = 1;
        glCompositeChart.marginWidth = 1;
        glCompositeChart.marginHeight = 1;
        glCompositeChart.marginBottom = 1;
        glCompositeChart.horizontalSpacing = 1;
        compositeChart.setLayout(glCompositeChart);

        Label label = new Label(compositeChart, SWT.NONE);
        label.setText("スケール");

        this.combo = new Combo(compositeChart, SWT.READ_ONLY);
        this.combo.setItems(SCALE_TEXT);
        this.combo.select(2);
        this.combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ResourceChartDialog.this.canvas.redraw();
            }
        });
        this.canvas = new Canvas(compositeChart, SWT.NO_BACKGROUND);
        this.canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));

        Composite compositeTable = new Composite(sashForm, SWT.NONE);
        GridLayout glCompositeTable = new GridLayout(1, false);
        glCompositeTable.horizontalSpacing = 1;
        glCompositeTable.marginHeight = 1;
        glCompositeTable.marginWidth = 1;
        glCompositeTable.verticalSpacing = 1;
        compositeTable.setLayout(glCompositeTable);

        this.table = new Table(compositeTable, SWT.BORDER | SWT.FULL_SELECTION);
        this.table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        this.table.setHeaderVisible(true);
        this.table.setLinesVisible(true);

        sashForm.setWeights(new int[] { 3, 1 });
        this.canvas.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                ResourceLog log = ResourceChartDialog.this.log;
                int scale = SCALE_DAYS[ResourceChartDialog.this.combo.getSelectionIndex()];
                String scaleText = "スケール:" + ResourceChartDialog.this.combo.getText();
                Point size = ResourceChartDialog.this.canvas.getSize();
                int width = size.x - 1;
                int height = size.y - 1;

                if (log != null) {
                    Image image = createImage(log, scale, scaleText, width, height);
                    e.gc.drawImage(image, 0, 0);
                    image.dispose();
                }
            }
        });

        // 資材ログ読み込み
        File report = new File(FilenameUtils.concat(AppConfig.get().getReportPath(), "資材ログ.csv"));
        try {
            this.log = ResourceLog.getInstance(report);
        } catch (IOException e) {
            this.log = null;
        }

        // 画像ファイルとして保存のリスナー
        save.addSelectionListener(new SaveImageAdapter(this.shell, this.combo, this.canvas, this.log));
        // 資材テーブルを表示する
        this.setTableHeader();
        if (this.log != null) {
            createTableBody(this.log, this.body);
            this.setTableBody();
            this.packTableHeader();
        }
    }

    /**
     * テーブルヘッダーをセットする
     */
    private void setTableHeader() {
        for (int i = 0; i < this.header.length; i++) {
            TableColumn col = new TableColumn(this.table, SWT.LEFT);
            col.setText(this.header[i]);
        }
        this.packTableHeader();
    }

    /**
     * テーブルヘッダーの幅を調節する
     */
    private void packTableHeader() {
        TableColumn[] columns = this.table.getColumns();
        for (int i = 0; i < columns.length; i++) {
            columns[i].pack();
        }
    }

    /**
     * テーブルボディーをセットする
     */
    private void setTableBody() {
        TableItemCreator creator = CreateReportLogic.DEFAULT_TABLE_ITEM_CREATOR;
        creator.init();
        for (int i = 0; i < this.body.size(); i++) {
            String[] line = this.body.get(i);
            creator.create(this.table, line, i);
        }
    }

    /**
     * 資材ログのグラフイメージを作成する
     * 
     * @param log 資材ログ
     * @param scale 日単位のスケール
     * @param width 幅
     * @param height 高さ
     * @return グラフイメージ
     */
    private static Image createImage(ResourceLog log, int scale, String scaleText, int width, int height) {

        Image image = new Image(Display.getCurrent(), Math.max(width, 1), Math.max(height, 1));
        try {
            GC gc = new GC(image);
            try {
                ResourceChart chart = new ResourceChart(log, scale, scaleText, width, height);
                chart.draw(gc);
            } finally {
                gc.dispose();
            }
        } catch (Exception e) {
            image.dispose();
            LOG.warn("グラフの描画で例外が発生しました", e);
        }
        return image;
    }

    /**
     * 資材テーブルのボディを作成する
     * 
     * @param log 資材ログ
     * @param body テーブルボディ
     */
    private static void createTableBody(ResourceLog log, List<String[]> body) {

        SimpleDateFormat format = new SimpleDateFormat(AppConstants.DATE_DAYS_FORMAT);
        format.setTimeZone(TimeZone.getTimeZone("GMT+4:00"));

        Map<String, SortableLog> resourceofday = new LinkedHashMap<>();
        for (int i = 0; i < log.time.length; i++) {
            String key = format.format(new Date(log.time[i]));
            Resource[] r = log.resources;

            resourceofday.put(key, new SortableLog(log.time[i],
                    r[ResourceLog.RESOURCE_FUEL].values[i],
                    r[ResourceLog.RESOURCE_AMMO].values[i],
                    r[ResourceLog.RESOURCE_METAL].values[i],
                    r[ResourceLog.RESOURCE_BAUXITE].values[i]));
        }

        MessageFormat diffFormat = new MessageFormat(DIFF_FORMAT);

        SortableLog before = null;
        for (Entry<String, SortableLog> entry : resourceofday.entrySet()) {
            SortableLog val = entry.getValue();
            int fuel = val.fuel;
            int ammo = val.ammo;
            int metal = val.metal;
            int bauxite = val.bauxite;
            int fuelDiff = fuel;
            int ammoDiff = ammo;
            int metalDiff = metal;
            int bauxiteDiff = bauxite;
            if (before != null) {
                fuelDiff = fuel - before.fuel;
                ammoDiff = ammo - before.ammo;
                metalDiff = metal - before.metal;
                bauxiteDiff = bauxite - before.bauxite;
            }
            before = val;

            String[] line = new String[] {
                    entry.getKey(),
                    diffFormat.format(new Object[] { fuel, fuelDiff }),
                    diffFormat.format(new Object[] { ammo, ammoDiff }),
                    diffFormat.format(new Object[] { metal, metalDiff }),
                    diffFormat.format(new Object[] { bauxite, bauxiteDiff })
            };
            body.add(line);
        }
        Collections.reverse(body);
    }

    /**
     * 画像ファイルとして保存のリスナー
     *
     */
    private static final class SaveImageAdapter extends SelectionAdapter {
        /** シェル */
        private final Shell shell;
        /** スケール */
        private final Combo combo;
        /** グラフキャンバス */
        private final Canvas canvas;
        /** 資材ログ */
        private final ResourceLog log;

        /**
         * コンストラクター
         */
        public SaveImageAdapter(Shell shell, Combo scaleCombo, Canvas canvas, ResourceLog log) {
            this.shell = shell;
            this.combo = scaleCombo;
            this.canvas = canvas;
            this.log = log;
        }

        @Override
        public void widgetSelected(SelectionEvent e) {
            if (this.log != null) {

                SimpleDateFormat format = new SimpleDateFormat(AppConstants.DATE_DAYS_FORMAT);
                String name = "資材ログ_" + format.format(Calendar.getInstance().getTime()) + ".png";

                FileDialog dialog = new FileDialog(this.shell, SWT.SAVE);
                dialog.setFileName(name);
                dialog.setFilterExtensions(new String[] { "*.png" });

                String filename = dialog.open();
                if (filename != null) {
                    File file = new File(filename);
                    if (file.exists()) {
                        MessageBox messageBox = new MessageBox(this.shell, SWT.YES | SWT.NO);
                        messageBox.setText("確認");
                        messageBox.setMessage("指定されたファイルは存在します。\n上書きしますか？");
                        if (messageBox.open() == SWT.NO) {
                            return;
                        }
                    }
                    int scale = SCALE_DAYS[this.combo.getSelectionIndex()];
                    String scaleText = "スケール:" + this.combo.getText();
                    Point size = this.canvas.getSize();
                    int width = size.x - 1;
                    int height = size.y - 1;
                    try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                        // イメージの生成
                        Image image = createImage(this.log, scale, scaleText, width, height);
                        try {
                            ImageLoader loader = new ImageLoader();
                            loader.data = new ImageData[] { image.getImageData() };
                            loader.save(out, SWT.IMAGE_PNG);
                        } finally {
                            image.dispose();
                        }
                    } catch (Exception ex) {
                        MessageBox messageBox = new MessageBox(this.shell, SWT.ICON_ERROR);
                        messageBox.setText("書き込めませんでした");
                        messageBox.setMessage(e.toString());
                        messageBox.open();
                    }
                }
            }
        }
    }
}
