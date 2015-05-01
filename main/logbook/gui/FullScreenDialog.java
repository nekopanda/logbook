package logbook.gui;

import logbook.gui.logic.ColorManager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * 矩形選択ウインドウ
 * 
 */
public final class FullScreenDialog extends Dialog {

    private Shell shell;
    private ScreenCanvas canvas;

    private final Image image;
    private final Display monitor;

    /**
     * Create the dialog.
     * 
     * @param parent
     * @param image 矩形選択の背景画像
     */
    public FullScreenDialog(Shell parent, Image image, Display monitor) {
        super(parent, SWT.NO_TRIM);
        this.setText("矩形選択");

        this.image = image;
        this.monitor = monitor;
    }

    /**
     * Open the dialog.
     * 
     * @return 選択された領域
     */
    public Rectangle open() {
        this.createContents();
        try {
            this.shell.open();
            this.shell.layout();
            Display display = this.getParent().getDisplay();
            while (!this.shell.isDisposed()) {
                if (!display.readAndDispatch()) {
                    display.sleep();
                }
            }
        } finally {
            this.canvas.dispose();
        }
        return this.canvas.getAbsoluteRectangle();
    }

    /**
     * Create contents of the dialog.
     */
    private void createContents() {
        // フルスクリーンのウインドウを作成します
        this.shell = new Shell(this.getParent(), this.getStyle());
        this.shell.setText(this.getText());
        this.shell.setBounds(this.monitor.getBounds());
        this.shell.setFullScreen(true);

        GridLayout glShell = new GridLayout(1, false);
        glShell.verticalSpacing = 0;
        glShell.marginWidth = 0;
        glShell.horizontalSpacing = 0;
        glShell.marginHeight = 0;
        this.shell.setLayout(glShell);

        // ウインドウいっぱいにキャプチャした画像を貼り付けます
        this.canvas = new ScreenCanvas(this.shell, this.image, this.monitor);
        this.canvas.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.canvas.addPaintListener(this.canvas);
        this.canvas.addMouseListener(this.canvas);
        this.canvas.addMouseMoveListener(this.canvas);
        this.canvas.setCursor(SWTResourceManager.getCursor(SWT.CURSOR_CROSS));

        this.shell.setActive();
    }

    /**
     * 矩形選択のためのキャンバスクラス
     *
     */
    private static final class ScreenCanvas extends Canvas implements PaintListener, MouseListener, MouseMoveListener {

        private final Image image;
        private final Display monitor;

        private final Color white = ColorManager.getColor(new RGB(255, 255, 255));
        private final Color black = ColorManager.getColor(new RGB(0, 0, 0));
        private final Font normalfont;
        private final Font largefont;

        private int x1;
        private int y1;
        private int x2;
        private int y2;

        public ScreenCanvas(Shell shell, Image image, Display monitor) {
            super(shell, SWT.NO_BACKGROUND);
            this.image = image;
            this.monitor = monitor;
            // 描画に使用するフォントを設定します
            FontData normal = shell.getFont().getFontData()[0];
            FontData large = new FontData(normal.getName(), 18, normal.getStyle());
            this.normalfont = new Font(Display.getDefault(), normal);
            this.largefont = new Font(Display.getDefault(), large);
        }

        @Override
        public void paintControl(PaintEvent e) {
            Rectangle m = this.monitor.getBounds();

            GC gc = e.gc;
            gc.drawImage(this.image, 0, 0, m.width, m.height, 0, 0, m.width, m.height);
            gc.setFont(this.largefont);
            gc.setForeground(this.black);
            gc.setBackground(this.white);
            gc.drawString("キャプチャする領域をマウスでドラッグして下さい。 [Esc]キーでキャンセル", -m.x + 2, -m.y + 2);
        }

        @Override
        public void mouseMove(MouseEvent e) {
            if ((e.stateMask & SWT.BUTTON1) != 0) {
                GC gc = new GC(this);
                gc.setXORMode(true);
                gc.setForeground(this.white);

                gc.drawRectangle(this.getRectangle());
                this.x2 = e.x;
                this.y2 = e.y;
                gc.drawRectangle(this.getRectangle());

                gc.setXORMode(false);

                gc.setFont(this.normalfont);
                gc.setForeground(this.black);
                gc.setBackground(this.white);
                String msg = "(" + this.x1 + "," + this.y1 + ")-(" + this.x2 + "," + this.y2 + ")";
                gc.drawString(msg, this.x1, this.y1);

                gc.dispose();
            }
        }

        @Override
        public void mouseDoubleClick(MouseEvent e) {

        }

        @Override
        public void mouseDown(MouseEvent e) {
            if (e.button == 1) {
                this.x1 = this.x2 = e.x;
                this.y1 = this.y2 = e.y;
                this.setCapture(true);
            }
        }

        @Override
        public void mouseUp(MouseEvent e) {
            if (e.button == 1) {
                this.x2 = e.x;
                this.y2 = e.y;
                this.setCapture(false);

                Rectangle rectangle = this.getRectangle();

                // 範囲が十分ある場合
                if ((rectangle.width > 2) && (rectangle.height > 2)) {
                    MessageBox msg = new MessageBox(this.getShell(), SWT.YES | SWT.NO | SWT.ICON_QUESTION);
                    msg.setText("矩形選択");
                    msg.setMessage("この範囲でよろしいですか？");
                    if (msg.open() == SWT.YES) {
                        this.getShell().close();
                        return;
                    }
                }
                this.redraw();
            }
        }

        @Override
        public void dispose() {
            this.normalfont.dispose();
            this.largefont.dispose();
            super.dispose();
        }

        public Rectangle getRectangle() {
            return new Rectangle(Math.min(this.x1, this.x2), Math.min(this.y1, this.y2), Math.abs(this.x2 - this.x1),
                    Math.abs(this.y2 - this.y1));
        }

        public Rectangle getAbsoluteRectangle() {
            Rectangle m = this.monitor.getBounds();
            Rectangle r = this.getRectangle();
            return new Rectangle(r.x + m.x, r.y + m.y, r.width, r.height);
        }
    }
}
