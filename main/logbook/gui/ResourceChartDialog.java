package logbook.gui;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import logbook.config.AppConfig;
import logbook.dto.chart.ResourceLog;
import logbook.gui.logic.ResourceChart;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * 資材ログチャートのダイアログ
 *
 */
public final class ResourceChartDialog extends Dialog {

    /** スケールテキスト */
    private static final String[] SCALE_TEXT = { "1日", "1週間", "2週間", "1ヶ月", "2ヶ月", "3ヶ月", "半年", "1年" };
    /** スケールテキストに対応する日 */
    private static final int[] SCALE_DAYS = { 1, 7, 14, 30, 60, 90, 180, 365 };

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

    /**
     * Create the dialog.
     * @param parent
     */
    public ResourceChartDialog(Shell parent) {
        super(parent, SWT.SHELL_TRIM);
        this.setText("資材ログチャート");
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
        this.shell.setSize(800, 500);
        this.shell.setText(this.getText());
        GridLayout glShell = new GridLayout(2, false);
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

        Label label = new Label(this.shell, SWT.NONE);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label.setText("スケール");

        this.combo = new Combo(this.shell, SWT.READ_ONLY);
        this.combo.setItems(SCALE_TEXT);
        this.combo.select(2);
        this.combo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ResourceChartDialog.this.canvas.redraw();
            }
        });

        // 資材ログ読み込み
        File report = new File(FilenameUtils.concat(AppConfig.get().getReportPath(), "資材ログ.csv"));
        try {
            this.log = ResourceLog.getInstance(report);
        } catch (IOException e) {
            this.log = null;
        }

        this.canvas = new Canvas(this.shell, SWT.NO_BACKGROUND);
        this.canvas.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        this.canvas.addPaintListener(new PaintListener() {
            @Override
            public void paintControl(PaintEvent e) {
                ResourceLog log = ResourceChartDialog.this.log;
                int scale = SCALE_DAYS[ResourceChartDialog.this.combo.getSelectionIndex()];
                Point size = ResourceChartDialog.this.canvas.getSize();
                int width = size.x - 1;
                int height = size.y - 1;

                if (log != null) {
                    Image image = createImage(log, scale, width, height);
                    e.gc.drawImage(image, 0, 0);
                    image.dispose();
                }
            }
        });
        // 画像ファイルとして保存のリスナー
        save.addSelectionListener(new SaveImageAdapter(this.shell, this.combo, this.canvas, this.log));
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
    private static Image createImage(ResourceLog log, int scale, int width, int height) {
        Image image = new Image(Display.getCurrent(), width, height);
        try {
            GC gc = new GC(image);
            try {
                ResourceChart chart = new ResourceChart(log, scale, width, height);
                chart.draw(gc);
            } finally {
                gc.dispose();
            }
        } catch (Exception t) {
            image.dispose();
        }
        return image;
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

                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
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
                    Point size = this.canvas.getSize();
                    int width = size.x - 1;
                    int height = size.y - 1;
                    try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                        // イメージの生成
                        Image image = createImage(this.log, scale, width, height);
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
