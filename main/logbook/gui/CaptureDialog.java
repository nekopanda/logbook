package logbook.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;

import logbook.config.AppConfig;
import logbook.constants.AppConstants;
import logbook.gui.logic.LayoutLogic;
import logbook.util.AwtUtils;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

/**
 * キャプチャダイアログ
 *
 */
public final class CaptureDialog extends Dialog {

    private Shell shell;

    private Composite composite;
    private Text text;
    private Button capture;
    private Button interval;
    private Spinner intervalms;

    private Rectangle rectangle;
    private Timer timer;
    private boolean isAlive;

    private Font font;

    /**
     * Create the dialog.
     * @param parent
     */
    public CaptureDialog(Shell parent) {
        super(parent, SWT.CLOSE | SWT.TITLE | SWT.MIN | SWT.RESIZE);
        this.setText("キャプチャ");
    }

    /**
     * Open the dialog.
     * @return the result
     */
    public void open() {
        try {
            this.createContents();
            this.shell.open();
            this.shell.layout();
            Display display = this.getParent().getDisplay();
            while (!this.shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        } finally {
            // タイマーを停止させる
            if (this.timer != null) {
                this.timer.cancel();
            }
            // フォントを開放
            if (this.font != null) {
                this.font.dispose();
            }
        }
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        // シェル
        this.shell = new Shell(this.getParent(), this.getStyle());
        this.shell.setText(this.getText());
        // レイアウト
        GridLayout glShell = new GridLayout(1, false);
        glShell.horizontalSpacing = 1;
        glShell.marginHeight = 1;
        glShell.marginWidth = 1;
        glShell.verticalSpacing = 1;
        this.shell.setLayout(glShell);

        // 太字にするためのフォントデータを作成する
        FontData defaultfd = this.shell.getFont().getFontData()[0];
        FontData fd = new FontData(defaultfd.getName(), defaultfd.getHeight(), SWT.BOLD);
        this.font = new Font(Display.getDefault(), fd);

        // コンポジット
        Composite rangeComposite = new Composite(this.shell, SWT.NONE);
        rangeComposite.setLayout(new GridLayout(2, false));

        // 範囲設定
        this.text = new Text(rangeComposite, SWT.BORDER | SWT.READ_ONLY);
        GridData gdText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdText.widthHint = 120;
        this.text.setLayoutData(gdText);
        this.text.setText("範囲が未設定です");

        Button button = new Button(rangeComposite, SWT.NONE);
        button.setText("範囲を選択");
        button.addSelectionListener(new SelectRectangleAdapter());

        // コンポジット
        this.composite = new Composite(this.shell, SWT.NONE);
        GridLayout loglayout = new GridLayout(3, false);
        this.composite.setLayout(loglayout);
        this.composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));

        // 周期設定
        this.interval = new Button(this.composite, SWT.CHECK);
        this.interval.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CaptureDialog.this.capture.setText(getCaptureButtonText(false,
                        CaptureDialog.this.interval.getSelection()));
            }
        });
        this.interval.setText("周期");

        this.intervalms = new Spinner(this.composite, SWT.BORDER);
        this.intervalms.setMaximum(60000);
        this.intervalms.setMinimum(100);
        this.intervalms.setSelection(1000);
        this.intervalms.setIncrement(100);

        Label label = new Label(this.composite, SWT.NONE);
        label.setText("ミリ秒");

        this.capture = new Button(this.shell, SWT.NONE);
        this.capture.setFont(this.font);
        GridData gdCapture = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
        gdCapture.horizontalSpan = 3;
        gdCapture.heightHint = 64;
        this.capture.setLayoutData(gdCapture);
        this.capture.setEnabled(false);
        this.capture.setText(getCaptureButtonText(false, this.interval.getSelection()));
        this.capture.addSelectionListener(new CaptureStartAdapter());

        this.shell.pack();
    }

    /**
     * キャプチャボタンの文字を取得します
     * 
     * @param isrunning
     * @param interval
     * @return
     */
    private static String getCaptureButtonText(boolean isrunning, boolean interval) {
        if (isrunning && interval) {
            return "停 止";
        } else if (interval) {
            return "開 始";
        } else {
            return "キャプチャ";
        }
    }

    /**
     * 範囲の選択を押した時
     *
     */
    public final class SelectRectangleAdapter extends SelectionAdapter {
        /** ダイアログが完全に消えるまで待つ時間 */
        private static final int WAIT = 250;

        @Override
        public void widgetSelected(SelectionEvent paramSelectionEvent) {
            try {
                Display display = Display.getDefault();
                // ダイアログを非表示にする
                CaptureDialog.this.shell.setVisible(false);
                // 消えるまで待つ
                Thread.sleep(WAIT);
                // ディスプレイに対してGraphics Contextを取得する(フルスクリーンキャプチャ)
                GC gc = new GC(display);
                Image image = new Image(display, display.getBounds());
                gc.copyArea(image, 0, 0);
                gc.dispose();

                try {
                    // 範囲を取得する
                    Rectangle rectangle = new FullScreenDialog(CaptureDialog.this.shell, image,
                            CaptureDialog.this.shell.getMonitor())
                            .open();

                    if ((rectangle != null) && (rectangle.width > 1) && (rectangle.height > 1)) {
                        CaptureDialog.this.rectangle = rectangle;
                        CaptureDialog.this.text.setText("(" + rectangle.x + "," + rectangle.y + ")-("
                                + (rectangle.x + rectangle.width) + "," + (rectangle.y + rectangle.height) + ")");
                        CaptureDialog.this.capture.setEnabled(true);
                    }
                } finally {
                    image.dispose();
                }
                CaptureDialog.this.shell.setVisible(true);
                CaptureDialog.this.shell.setActive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * キャプチャボタンを押した時
     *
     */
    public final class CaptureStartAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent e) {
            Timer timer = CaptureDialog.this.timer;

            Rectangle rectangle = CaptureDialog.this.rectangle;
            boolean interval = CaptureDialog.this.interval.getSelection();
            int intervalms = CaptureDialog.this.intervalms.getSelection();

            if (timer != null) {
                // タイマーを停止させる
                timer.cancel();
                timer = null;
            }

            if (CaptureDialog.this.isAlive) {

                CaptureDialog.this.capture.setText(getCaptureButtonText(false, interval));
                LayoutLogic.enable(CaptureDialog.this.composite, true);
                CaptureDialog.this.isAlive = false;
            } else {
                timer = new Timer(true);
                if (interval) {
                    // 固定レートで周期キャプチャ
                    timer.scheduleAtFixedRate(new CaptureTask(rectangle), 0, intervalms);
                    CaptureDialog.this.isAlive = true;
                } else {
                    // 一回だけキャプチャ
                    timer.schedule(new CaptureTask(rectangle), 0);
                }

                CaptureDialog.this.capture.setText(getCaptureButtonText(true, interval));

                if (interval) {
                    LayoutLogic.enable(CaptureDialog.this.composite, false);
                }
            }
            CaptureDialog.this.timer = timer;
        }
    }

    /**
     * 画面キャプチャスレッド
     *
     */
    public static final class CaptureTask extends TimerTask {
        /** ロガー */
        private static final Logger LOG = LogManager.getLogger(CaptureTask.class);
        /** Jpeg品質 */
        private static final float QUALITY = 0.9f;
        /** 日付フォーマット(ファイル名) */
        private final SimpleDateFormat fileNameFormat = new SimpleDateFormat(AppConstants.DATE_LONG_FORMAT);
        /** 日付フォーマット(ディレクトリ名) */
        private final SimpleDateFormat dirNameFormat = new SimpleDateFormat(AppConstants.DATE_DAYS_FORMAT);
        /** キャプチャ範囲 */
        private final Rectangle rectangle;
        /** トリム範囲 */
        private java.awt.Rectangle trimRect;

        public CaptureTask(Rectangle rectangle) {
            this.rectangle = rectangle;
        }

        @Override
        public void run() {
            try {
                // 時刻からファイル名を作成
                Date now = Calendar.getInstance().getTime();

                String dir = null;
                if (AppConfig.get().isCreateDateFolder()) {
                    dir = FilenameUtils.concat(AppConfig.get().getCapturePath(), this.dirNameFormat.format(now));
                } else {
                    dir = AppConfig.get().getCapturePath();
                }

                String fname = FilenameUtils.concat(dir, this.fileNameFormat.format(now) + "."
                        + AppConfig.get().getImageFormat());
                File file = new File(fname);

                if (file.exists()) {
                    if (file.isDirectory()) {
                        throw new IOException("File '" + file + "' exists but is a directory");
                    }
                    if (!(file.canWrite()))
                        throw new IOException("File '" + file + "' cannot be written to");
                } else {
                    File parent = file.getParentFile();
                    if ((parent != null) &&
                            (!(parent.mkdirs())) && (!(parent.isDirectory()))) {
                        throw new IOException("Directory '" + parent + "' could not be created");
                    }
                }

                // 範囲をキャプチャする
                BufferedImage image = AwtUtils.getCapture(this.rectangle);
                if (image != null) {

                    ImageOutputStream ios = ImageIO.createImageOutputStream(file);
                    try {
                        ImageWriter writer = ImageIO.getImageWritersByFormatName(AppConfig.get().getImageFormat())
                                .next();
                        try {
                            ImageWriteParam iwp = writer.getDefaultWriteParam();
                            if (iwp.canWriteCompressed()) {
                                iwp.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                                iwp.setCompressionQuality(QUALITY);
                            }
                            writer.setOutput(ios);

                            if (this.trimRect == null) {
                                this.trimRect = AwtUtils.getTrimSize(image);
                            }

                            writer.write(null, new IIOImage(AwtUtils.trim(image, this.trimRect), null, null), iwp);
                        } finally {
                            writer.dispose();
                        }
                    } finally {
                        ios.close();
                    }
                }
            } catch (Exception e) {
                LOG.warn("キャプチャ中に例外が発生しました", e);
            }
        }
    }
}
