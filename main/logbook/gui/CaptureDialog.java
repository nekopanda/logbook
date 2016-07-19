package logbook.gui;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
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
import logbook.gui.twitter.TweetDialog;
import logbook.gui.twitter.TwitterClient;
import logbook.internal.LoggerHolder;
import logbook.util.AwtUtils;
import logbook.util.SwtUtils;

import org.apache.commons.io.FilenameUtils;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * キャプチャダイアログ
 *
 */
public final class CaptureDialog extends WindowBase {
    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(CaptureDialog.class);

    private final Shell parent;
    private Shell shell;

    private Composite composite;
    private Text text;
    private Button capture;
    private Button twitter;
    private Button interval;
    private Spinner intervalms;

    /** キャプチャ範囲 */
    private Rectangle rectangle;
    private Timer timer;
    private boolean isAlive;

    private Font font;

    /** Jpeg品質 */
    private static final float QUALITY = 0.9f;
    /** 日付フォーマット(ファイル名) */
    private final SimpleDateFormat fileNameFormat = new SimpleDateFormat(AppConstants.DATE_LONG_FORMAT);
    /** 日付フォーマット(ディレクトリ名) */
    private final SimpleDateFormat dirNameFormat = new SimpleDateFormat(AppConstants.DATE_DAYS_FORMAT);
    /** トリム範囲 */
    private java.awt.Rectangle trimRect;

    /**
     * Create the dialog.
     * @param parent
     */
    public CaptureDialog(Shell parent, MenuItem menuItem) {
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
            this.setCaptureRect(intToRect(AppConfig.get().getCaptureRect()));
            this.setVisible(true);
            return;
        }

        this.createContents();
        this.registerEvents();
        this.setWindowInitialized(true);
        this.setVisible(true);
    }

    @Override
    protected boolean moveWithDrag() {
        return true;
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        // シェル
        super.createContents(this.parent, SWT.CLOSE | SWT.TITLE | SWT.RESIZE | SWT.TOOL, false);
        this.getShell().setText("キャプチャ");
        this.shell = this.getShell();
        // レイアウト
        GridLayout glShell = new GridLayout(2, false);
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
        rangeComposite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        rangeComposite.setLayout(new GridLayout(2, false));

        // 範囲設定
        this.text = new Text(rangeComposite, SWT.BORDER | SWT.READ_ONLY);
        GridData gdText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
        gdText.widthHint = SwtUtils.DPIAwareWidth(120);
        this.text.setLayoutData(gdText);
        this.text.setText("範囲が未設定です");

        Button button = new Button(rangeComposite, SWT.NONE);
        button.setText("範囲を選択");
        button.addSelectionListener(new SelectRectangleAdapter());

        // コンポジット
        this.composite = new Composite(this.shell, SWT.NONE);
        this.composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        this.composite.setLayout(new GridLayout(3, false));

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

        Button openFolderButton = new Button(this.shell, SWT.NONE);
        openFolderButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        openFolderButton.setText("保存先を開く");
        openFolderButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                CaptureDialog.this.openCaptureDir();
            }
        });

        Composite buttonComposite = new Composite(this.shell, SWT.NONE);
        buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
        buttonComposite.setLayout(new GridLayout(2, true));

        this.capture = new Button(buttonComposite, SWT.NONE);
        this.capture.setFont(this.font);
        GridData gdCapture = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
        gdCapture.heightHint = SwtUtils.DPIAwareHeight(64);
        this.capture.setLayoutData(gdCapture);
        this.capture.setEnabled(false);
        this.capture.setText(getCaptureButtonText(false, this.interval.getSelection()));
        this.capture.addSelectionListener(new CaptureStartAdapter());

        this.twitter = new Button(buttonComposite, SWT.NONE);
        this.twitter.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL));
        this.twitter.setEnabled(false);
        this.twitter.addSelectionListener(new TwitterAdapter());
        SwtUtils.setButtonImage(this.twitter, SWTResourceManager.getImage(WindowBase.class, AppConstants.TWITTER));

        this.shell.addListener(SWT.Dispose, new Listener() {
            @Override
            public void handleEvent(Event event) {
                // タイマーを停止させる
                if (CaptureDialog.this.timer != null) {
                    CaptureDialog.this.timer.cancel();
                }
                // フォントを開放
                if (CaptureDialog.this.font != null) {
                    CaptureDialog.this.font.dispose();
                }
            }
        });

        // 選択する項目はドラックで移動できないようにする
        for (Control c : new Control[] { this.text, button, this.interval, this.intervalms, this.capture }) {
            c.setData("disable-drag-move", true);
        }

        // 設定を反映
        this.setCaptureRect(intToRect(AppConfig.get().getCaptureRect()));

        this.shell.pack();
    }

    private static Rectangle intToRect(int[] intRect) {
        if (intRect == null)
            return null;
        return new Rectangle(intRect[0], intRect[1], intRect[2], intRect[3]);
    }

    private static int[] rectToInt(Rectangle rect) {
        if (rect == null)
            return null;
        return new int[] { rect.x, rect.y, rect.width, rect.height };
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

    private void setCaptureRect(Rectangle rectangle) {
        if ((rectangle != null) && (rectangle.width > 1) && (rectangle.height > 1)) {
            this.rectangle = rectangle;
            this.trimRect = AwtUtils.getTrimSize(captureImage(rectangle, null));
            AppConfig.get().setCaptureRect(rectToInt(rectangle));
            this.text.setText("(" + rectangle.x + "," + rectangle.y + ")-("
                    + (rectangle.x + rectangle.width) + "," + (rectangle.y + rectangle.height) + ")");
            this.capture.setEnabled(true);
            this.twitter.setEnabled(true);
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
                Rectangle rect = display.getBounds();
                Image image = new Image(display, rect);
                gc.copyArea(image, rect.x, rect.y);
                gc.dispose();

                try {
                    // 範囲を取得する
                    Rectangle rectangle = new FullScreenDialog(CaptureDialog.this.shell, image, display)
                            .open();

                    CaptureDialog.this.setCaptureRect(rectangle);
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
                    timer.scheduleAtFixedRate(new CaptureTask(), 0, intervalms);
                    CaptureDialog.this.isAlive = true;
                } else {
                    // 一回だけキャプチャ
                    timer.schedule(new CaptureTask(), 0);
                }

                CaptureDialog.this.capture.setText(getCaptureButtonText(true, interval));

                if (interval) {
                    LayoutLogic.enable(CaptureDialog.this.composite, false);
                }
            }
            CaptureDialog.this.timer = timer;
        }
    }

    private BufferedImage captureImage() {
        return captureImage(this.rectangle, this.trimRect);
    }

    private static BufferedImage captureImage(Rectangle rectangle, java.awt.Rectangle trimRect) {
        // 範囲をキャプチャする
        BufferedImage image = AwtUtils.getCapture(rectangle);
        if (image != null) {
            if (trimRect != null) {
                return AwtUtils.trim(image, trimRect);
            }
            return image;
        }
        return null;
    }

    private File getSaveFile() throws IOException {
        // 時刻からファイル名を作成
        Date now = new Date();

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
        return file;
    }

    private void openCaptureDir() {
        try {
            File file = this.getSaveFile();
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(file.getParent()));
            }
        } catch (Exception e) {
            LOG.get().warn("保存先を開くで例外が発生しました", e);
        }
    }

    private void saveImageToFile(BufferedImage image, File file) throws IOException {
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

                writer.write(null, new IIOImage(image, null, null), iwp);
                ApplicationMain.logPrint("キャプチャしました [" + file.getName() + "]");
            } finally {
                writer.dispose();
            }
        } finally {
            ios.close();
        }
    }

    private File captureAndSave() throws IOException {
        File file = CaptureDialog.this.getSaveFile();
        // 範囲をキャプチャする
        BufferedImage image = CaptureDialog.this.captureImage();
        if (image != null) {
            CaptureDialog.this.saveImageToFile(image, file);
        }
        return file;
    }

    /**
     * 画面キャプチャスレッド
     *
     */
    public final class CaptureTask extends TimerTask {
        @Override
        public void run() {
            try {
                CaptureDialog.this.captureAndSave();
            } catch (Exception e) {
                LOG.get().warn("キャプチャ中に例外が発生しました", e);
            }
        }
    }

    /**
     * Twitterボタンを押した時
     *
     */
    public final class TwitterAdapter extends SelectionAdapter {

        @Override
        public void widgetSelected(SelectionEvent ev) {
            try {
                File file = CaptureDialog.this.captureAndSave();
                if (TwitterClient.getInstance().prepareAccessToken(CaptureDialog.this)) {
                    TweetDialog tweetDialog = new TweetDialog(
                            CaptureDialog.this, file);
                    tweetDialog.open();
                }
            } catch (Exception e) {
                LOG.get().warn("つぶやく途中で例外が発生しました", e);
            }
        }
    }
}
