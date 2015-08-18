package logbook.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.CheckForNull;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TaskBar;
import org.eclipse.swt.widgets.TaskItem;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * SWTのutilです
 *
 */
public final class SwtUtils {

    /**
     * TaskItemを取得します
     * 
     * @param shell
     * @return
     */
    @CheckForNull
    public static TaskItem getTaskBarItem(Shell shell) {
        TaskBar bar = Display.getDefault().getSystemTaskBar();
        if (bar == null)
            return null;
        TaskItem item = bar.getItem(shell);
        if (item == null)
            item = bar.getItem(null);
        return item;
    }

    /**
     * Composite.layout()をすべての要素で呼び出します
     * @param composite
     */
    public static void layoutCompositeRecursively(Composite composite) {
        for (org.eclipse.swt.widgets.Control control : composite.getChildren()) {
            if (control instanceof Composite) {
                layoutCompositeRecursively((Composite) control);
            }
        }
        composite.layout();
    }

    private static class ButtonImagePainter implements Listener {
        private final Button button;
        private final Image image;

        public ButtonImagePainter(Button button, Image image) {
            this.button = button;
            this.image = image;
        }

        @Override
        public void handleEvent(Event event) {
            Rectangle rect = this.button.getBounds();
            Image scaled = SwtUtils.scaleToFit(this.image, rect.width, rect.height);
            Image old = this.button.getImage();
            this.button.setImage(scaled);
            if (old != null) {
                old.dispose();
            }
        }
    }

    public static void setButtonImage(Button button, Image image) {
        button.addListener(SWT.Resize, new ButtonImagePainter(button, image));
    }

    public static FormData makeFormData(
            FormAttachment left, FormAttachment right,
            FormAttachment top, FormAttachment bottom)
    {
        FormData data = new FormData();
        data.left = left;
        data.right = right;
        data.top = top;
        data.bottom = bottom;
        return data;
    }

    public static GridLayout makeGridLayout(
            int numColumns,
            int horizontalSpacing, int verticalSpacing,
            int marginWidth, int marginHeight)
    {
        GridLayout gl = new GridLayout(numColumns, false);
        gl.horizontalSpacing = horizontalSpacing;
        gl.verticalSpacing = verticalSpacing;
        gl.marginWidth = marginWidth;
        gl.marginHeight = marginHeight;
        return gl;
    }

    public static RowLayout makeRowLayout(boolean horizontal, int spacing, int margin, boolean wrap) {
        RowLayout rl = new RowLayout(horizontal ? SWT.HORIZONTAL : SWT.VERTICAL);
        rl.spacing = spacing;
        rl.center = true;
        rl.marginBottom = rl.marginLeft = rl.marginRight = rl.marginTop = margin;
        rl.wrap = wrap;
        return rl;
    }

    private static String defaultFontName = null;
    private static int defaultFontSize = 0;
    private static int defaultFontStyle = 0;
    private static int defaultLineHeight = 0;
    private static int DPI_Y = 0;

    private static void checkControl(Control control) {
        if (defaultFontName == null) {
            FontData fd = control.getFont().getFontData()[0];
            defaultFontName = fd.getName();
            defaultFontSize = fd.getHeight();
            defaultFontStyle = fd.getStyle();
            DPI_Y = control.getDisplay().getDPI().y;

            GC gc = new GC(control);
            FontMetrics fm = gc.getFontMetrics();
            defaultLineHeight = fm.getAscent() + fm.getLeading() + fm.getDescent();
            gc.dispose();
        }
    }

    public static int ComputeHeaderHeight(Group group, double lineHeight) {
        checkControl(group);
        return (int) (((defaultFontSize * DPI_Y) / 72.0) * lineHeight);
    }

    public static int ComputeLineSpacing(double lineHeight) {
        return (int) (((defaultFontSize * DPI_Y) / 72.0) * lineHeight) - defaultLineHeight;
    }

    /**
     * 行間はデフォルト値 120%
     * @param lbl
     * @param text
     * @param gd
     * @return
     */
    public static GridData initLabel(Label lbl, String text, GridData gd) {
        return initLabel(lbl, text, 0, 1.2, gd);
    }

    public static GridData initLabel(Label lbl, String text, int fontSizeDiff, GridData gd) {
        return initLabel(lbl, text, fontSizeDiff, 1.2, gd);
    }

    public static GridData initLabel(Label lbl, String text, int fontSizeDiff, double lineHeight, GridData gd) {
        checkControl(lbl);
        int heightInPoint = defaultFontSize + fontSizeDiff;
        Font font = SWTResourceManager.getFont(defaultFontName, heightInPoint, defaultFontStyle);
        lbl.setFont(font);
        lbl.setText(text);
        gd.heightHint = (int) (((heightInPoint * DPI_Y) / 72.0) * lineHeight);
        lbl.setLayoutData(gd);
        return gd;
    }

    public static Image makeImage(File file) throws IOException {
        InputStream stream = new FileInputStream(file);
        try {
            Display display = Display.getCurrent();
            ImageData data = new ImageData(stream);
            if (data.transparentPixel > 0) {
                return new Image(display, data, data.getTransparencyMask());
            }
            return new Image(display, data);
        } finally {
            stream.close();
        }
    }

    public static Image scaleToFit(Image img, int width, int height) {
        ImageData data = img.getImageData();
        float factor = Math.min((float) width / data.width, (float) height / data.height);
        if (factor <= 0) {
            return null;
        }
        ImageData scaled = data.scaledTo((int) (data.width * factor), (int) (data.height * factor));
        return new Image(Display.getDefault(), scaled);
    }

    public static void errorDialog(Exception e, Shell parent) {
        MessageBox box1 = new MessageBox(parent, SWT.OK | SWT.ICON_ERROR);
        box1.setMessage(e.getMessage());
        box1.open();
    }

    public static void messageDialog(String mes, Shell parent) {
        MessageBox box1 = new MessageBox(parent, SWT.OK | SWT.ICON_INFORMATION);
        box1.setMessage(mes);
        box1.open();
    }
}
