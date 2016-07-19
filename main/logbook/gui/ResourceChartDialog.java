package logbook.gui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import logbook.config.AppConfig;
import logbook.constants.AppConstants;
import logbook.dto.chart.Resource;
import logbook.dto.chart.ResourceLog;
import logbook.dto.chart.ResourceLog.SortableLog;
import logbook.gui.logic.ColorManager;
import logbook.gui.logic.ResourceChart;
import logbook.gui.logic.ResourceChart.ActiveLevel;
import logbook.gui.logic.TableItemCreator;
import logbook.internal.LoggerHolder;
import logbook.scripting.TableItemCreatorProxy;
import logbook.util.SwtUtils;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
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
public final class ResourceChartDialog extends WindowBase {

    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(ResourceChartDialog.class);

    /** スケールテキスト */
    private static final String[] SCALE_TEXT = { "1日", "1週間", "2週間", "1ヶ月", "2ヶ月", "3ヶ月", "半年", "1年" };
    /** スケールテキストに対応する日 */
    private static final int[] SCALE_DAYS = { 1, 7, 14, 30, 60, 90, 180, 365 };
    /** 資材テーブルに表示する資材のフォーマット */
    private static final String DIFF_FORMAT = "{0,number,0}({1,number,+0;-0})";

    /** シェル */
    private final Shell parent;
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
    private final String[] header = new String[] { "日付", "燃料", "弾薬", "鋼材", "ボーキ", "バーナー", "バケツ", "開発資材", "ネジ" };
    /** 資材テーブルのボディ */
    private List<String[]> body = new ArrayList<>();
    /** 最後に読み込んだ時間 */
    private Date lastLoadDate = new Date(0);

    /** 更新タイマー */
    protected Timer timer;

    private int nextActivated = -1;
    private int nowActivated = -1;

    private Image currentImage;

    private final Button[] enableCheckButtons = new Button[8];

    /**
     * Create the dialog.
     * @param parent
     */
    public ResourceChartDialog(Shell parent, MenuItem menuItem) {
        super(menuItem);
        this.parent = parent;
    }

    /**
     * Open the dialog.
     */
    @Override
    public void open() {
        // 初期化済みの場合
        if (this.isWindowInitialized()) {
            // リロードして表示
            Calendar cal = Calendar.getInstance();
            cal.setTime(this.lastLoadDate);
            cal.add(Calendar.MINUTE, 1);
            if (new Date().after(cal.getTime())) {
                // 最後に読み込んでから1分以上経過していたら再読み込み
                this.updateContents();
            }
            this.setVisible(true);
            return;
        }

        this.createContents();
        this.registerEvents();
        this.setWindowInitialized(true);
        this.setVisible(true);

        // 更新タイマー
        this.timer = new Timer(true);
        // 10分毎に再読み込みするようにスケジュールする
        this.timer.schedule(new CyclicReloadTask(), 0, TimeUnit.MINUTES.toMillis(10));
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        super.createContents(this.parent, SWT.SHELL_TRIM, false);
        this.getShell().setText("資材チャート");
        this.shell = this.getShell();
        this.shell.setMinimumSize(450, 300);
        this.shell.setSize(SwtUtils.DPIAwareSize(new Point(800, 650)));
        this.shell.addListener(SWT.Dispose, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if (ResourceChartDialog.this.currentImage != null) {
                    ResourceChartDialog.this.currentImage.dispose();
                    ResourceChartDialog.this.currentImage = null;
                }
            }
        });
        GridLayout glShell = new GridLayout(1, false);
        glShell.verticalSpacing = 2;
        glShell.marginWidth = 2;
        glShell.marginHeight = 2;
        glShell.horizontalSpacing = 2;
        this.shell.setLayout(glShell);

        this.createMenubar();
        this.menubar = this.getMenubar();

        if (this.isNoMenubar()) {
            this.filemenu = this.menubar;
        }
        else {
            MenuItem fileroot = new MenuItem(this.menubar, SWT.CASCADE);
            fileroot.setText("ファイル");
            this.filemenu = new Menu(fileroot);
            fileroot.setMenu(this.filemenu);
        }

        MenuItem save = new MenuItem(this.filemenu, SWT.NONE);
        save.setText("画像ファイルとして保存(&S)\tCtrl+S");
        save.setAccelerator(SWT.CTRL + 'S');

        SashForm sashForm = new SashForm(this.shell, SWT.SMOOTH | SWT.VERTICAL);
        sashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

        Composite compositeChart = new Composite(sashForm, SWT.NONE);
        GridLayout glCompositeChart = new GridLayout(4, false);
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
                ResourceChartDialog.this.reloadImage();
            }
        });

        Label label2 = new Label(compositeChart, SWT.NONE);
        label2.setText("表示");

        Composite enableCheckGroup = new Composite(compositeChart, SWT.NONE);
        enableCheckGroup.setLayout(new RowLayout(SWT.HORIZONTAL));
        SelectionListener checkboxListener = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ResourceChartDialog.this.reloadImage();
            }
        };
        final Runnable updateImage = new Runnable() {
            @Override
            public void run() {
                ResourceChartDialog.this.updateActivatedImage();
            }
        };
        MouseTrackListener checkboxMouseListener = new MouseTrackListener() {
            @Override
            public void mouseEnter(MouseEvent e) {
                Control control = (Control) e.getSource();
                ResourceChartDialog.this.nextActivated = (Integer) control.getData();
                Display.getDefault().asyncExec(updateImage);
            }

            @Override
            public void mouseExit(MouseEvent e) {
                ResourceChartDialog.this.nextActivated = -1;
                Display.getDefault().timerExec(100, updateImage);
            }

            @Override
            public void mouseHover(MouseEvent e) {
            }
        };
        String[] resourceNames = new String[] {
                "燃料",
                "弾薬",
                "鋼材",
                "ボーキ",
                "バーナー",
                "バケツ",
                "開発資材",
                "ネジ"
        };
        RGB[] colors = AppConfig.get().getResourceColors();
        for (int i = 0; i < 8; ++i) {
            Button check = new Button(enableCheckGroup, SWT.CHECK);
            check.setData(i);
            check.addSelectionListener(checkboxListener);
            check.addMouseTrackListener(checkboxMouseListener);
            check.setSelection(true);

            // ButtonはWindowsだと色を変えられないので画像化して貼り付ける
            String text = resourceNames[i];
            GC gc = new GC(check);
            Point textExtent = gc.stringExtent(text);
            gc.dispose();
            Image image = new Image(check.getDisplay(), new Rectangle(0, 0, textExtent.x, textExtent.y));
            GC gcImage = new GC(image);
            gcImage.setBackground(check.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
            gcImage.setForeground(ColorManager.getColor(colors[i]));
            gcImage.drawText(text, 0, 0);
            gcImage.dispose();
            check.setImage(image);

            this.enableCheckButtons[i] = check;
        }

        this.canvas = new Canvas(compositeChart, SWT.NO_BACKGROUND);
        this.canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));

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
                if (ResourceChartDialog.this.currentImage != null) {
                    e.gc.drawImage(ResourceChartDialog.this.currentImage, 0, 0);
                }
            }
        });
        this.canvas.addListener(SWT.Resize, new Listener() {
            @Override
            public void handleEvent(Event e) {
                ResourceChartDialog.this.reloadImage();
            }
        });

        // 画像ファイルとして保存のリスナー
        save.addSelectionListener(new SaveImageAdapter());
        // 資材テーブルを表示する
        this.setTableHeader();
        // データを読み込んで表示
        this.updateContents();
    }

    private void updateActivatedImage() {
        if (this.nowActivated != this.nextActivated) {
            this.nowActivated = this.nextActivated;
            this.reloadImage();
        }
    }

    private void updateContents() {
        File report = new File(FilenameUtils.concat(AppConfig.get().getReportPath(), AppConstants.LOG_RESOURCE));
        try {
            this.log = ResourceLog.getInstance(report);
            if (this.log != null) {
                this.body = createTableBody(this.log);
                this.reloadImage();
                this.setTableBody();
                this.packTableHeader();
                this.lastLoadDate = new Date();
            }
        } catch (IOException e) {
            this.log = null;
        }
    }

    private void reloadImage() {
        int scale = SCALE_DAYS[ResourceChartDialog.this.combo.getSelectionIndex()];
        String scaleText = "スケール:" + ResourceChartDialog.this.combo.getText();
        Point size = ResourceChartDialog.this.canvas.getSize();
        if ((size.x > 0) && (size.y > 0)) {
            int width = size.x - 1;
            int height = size.y - 1;

            if (this.log != null) {
                if (this.currentImage != null) {
                    this.currentImage.dispose();
                }
                this.currentImage = createImage(this.log, scale, scaleText, width, height,
                        ResourceChartDialog.this.getResourceActiveLevel(), false);
                this.canvas.redraw();
            }
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
        this.table.removeAll();
        TableItemCreator creator = TableItemCreatorProxy.get(AppConstants.RESOURCECHAR_PREFIX);
        creator.begin(this.header);
        for (int i = 0; i < this.body.size(); i++) {
            String[] line = this.body.get(i);
            creator.create(this.table, line, i);
        }
        creator.end();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            // マウスホイールイベントを期間選択コンボボックスに流すためにフォーカスする
            this.combo.setFocus();
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
    private static Image createImage(ResourceLog log, int scale, String scaleText, int width, int height,
            ActiveLevel[] activeLevel, boolean printHeader) {

        Image image = new Image(Display.getCurrent(), Math.max(width, 1), Math.max(height, 1));
        try {
            GC gc = new GC(image);
            try {
                ResourceChart chart = new ResourceChart(
                        gc, log, scale, scaleText, width, height, activeLevel, printHeader);
                chart.draw(gc);
            } finally {
                gc.dispose();
            }
        } catch (Exception e) {
            image.dispose();
            image = null;
            LOG.get().warn("グラフの描画で例外が発生しました", e);
        }
        return image;
    }

    /**
     * 資材テーブルのボディを作成する
     * 
     * @param log 資材ログ
     * @param body テーブルボディ
     */
    private static List<String[]> createTableBody(ResourceLog log) {
        List<String[]> body = new ArrayList<>();

        SimpleDateFormat format = new SimpleDateFormat(AppConstants.DATE_DAYS_FORMAT);
        format.setTimeZone(AppConstants.TIME_ZONE_MISSION);

        Map<String, SortableLog> resourceofday = new LinkedHashMap<>();
        for (int i = 0; i < log.time.length; i++) {
            String key = format.format(new Date(log.time[i]));
            Resource[] r = log.resources;

            resourceofday.put(key, new SortableLog(log.time[i],
                    r[ResourceLog.RESOURCE_FUEL].values[i],
                    r[ResourceLog.RESOURCE_AMMO].values[i],
                    r[ResourceLog.RESOURCE_METAL].values[i],
                    r[ResourceLog.RESOURCE_BAUXITE].values[i],
                    r[ResourceLog.RESOURCE_BURNER].values[i],
                    r[ResourceLog.RESOURCE_BUCKET].values[i],
                    r[ResourceLog.RESOURCE_RESEARCH].values[i],
                    r[ResourceLog.RESOURCE_SCREW].values[i]));
        }

        MessageFormat diffFormat = new MessageFormat(DIFF_FORMAT);

        SortableLog before = null;
        for (Entry<String, SortableLog> entry : resourceofday.entrySet()) {
            SortableLog val = entry.getValue();
            int fuel = val.fuel;
            int ammo = val.ammo;
            int metal = val.metal;
            int bauxite = val.bauxite;
            int burner = val.burner;
            int bucket = val.bucket;
            int research = val.research;
            int screw = val.screw;
            int fuelDiff = fuel;
            int ammoDiff = ammo;
            int metalDiff = metal;
            int bauxiteDiff = bauxite;
            int burnerDiff = burner;
            int bucketDiff = bucket;
            int researchDiff = research;
            int screwDiff = screw;
            if (before != null) {
                fuelDiff = fuel - before.fuel;
                ammoDiff = ammo - before.ammo;
                metalDiff = metal - before.metal;
                bauxiteDiff = bauxite - before.bauxite;
                burnerDiff = burner - before.burner;
                bucketDiff = bucket - before.bucket;
                researchDiff = research - before.research;
                screwDiff = screw - before.screw;
            }
            before = val;

            String[] line = new String[] {
                    entry.getKey(),
                    diffFormat.format(new Object[] { fuel, fuelDiff }),
                    diffFormat.format(new Object[] { ammo, ammoDiff }),
                    diffFormat.format(new Object[] { metal, metalDiff }),
                    diffFormat.format(new Object[] { bauxite, bauxiteDiff }),
                    diffFormat.format(new Object[] { burner, burnerDiff }),
                    diffFormat.format(new Object[] { bucket, bucketDiff }),
                    diffFormat.format(new Object[] { research, researchDiff }),
                    diffFormat.format(new Object[] { screw, screwDiff })
            };
            body.add(line);
        }
        Collections.reverse(body);
        return body;
    }

    private ActiveLevel[] getResourceActiveLevel() {
        ActiveLevel[] activated = new ActiveLevel[this.enableCheckButtons.length];
        int nowActivated = ((this.nowActivated != -1) && this.enableCheckButtons[this.nowActivated].getSelection())
                ? this.nowActivated
                : -1;
        for (int i = 0; i < activated.length; ++i) {
            boolean enabled = this.enableCheckButtons[i].getSelection();
            if (!enabled) {
                activated[i] = ActiveLevel.DISABLED;
            }
            else if (nowActivated == -1) {
                activated[i] = ActiveLevel.NORMAL;
            }
            else if (nowActivated == i) {
                activated[i] = ActiveLevel.ACTIVE;
            }
            else {
                activated[i] = ActiveLevel.INACTIVE;
            }
        }
        return activated;
    }

    private void saveImage() {
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
                    Image image = createImage(
                            this.log, scale, scaleText, width, height, this.getResourceActiveLevel(), true);
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
                    messageBox.setMessage(ex.toString());
                    messageBox.open();
                }
            }
        }
    }

    /**
     * 画像ファイルとして保存のリスナー
     *
     */
    private final class SaveImageAdapter extends SelectionAdapter {
        @Override
        public void widgetSelected(SelectionEvent e) {
            ResourceChartDialog.this.saveImage();
        }
    }

    /**
     * テーブルを定期的に再読み込みする
     */
    protected class CyclicReloadTask extends TimerTask {

        @Override
        public void run() {
            ResourceChartDialog.this.shell.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (!ResourceChartDialog.this.shell.isDisposed()) {
                        // 見えているときだけ処理する
                        if (ResourceChartDialog.this.shell.isVisible()) {
                            ResourceChartDialog.this.updateContents();
                        }
                    }
                    else {
                        // ウインドウが消えていたらタスクをキャンセルする
                        CyclicReloadTask.this.cancel();
                    }
                }
            });
        }
    }
}