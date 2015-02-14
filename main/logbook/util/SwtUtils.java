package logbook.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.CheckForNull;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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

    private static String defaultFontName = null;
    private static int defaultFontSize = 0;
    private static int defaultFontStyle = 0;
    private static int DPI_Y = 0;

    private static void checkControl(Control control) {
        if (defaultFontName == null) {
            FontData fd = control.getFont().getFontData()[0];
            defaultFontName = fd.getName();
            defaultFontSize = fd.getHeight();
            defaultFontStyle = fd.getStyle();
            DPI_Y = control.getDisplay().getDPI().y;
        }
    }

    public static int ComputeHeaderHeight(Group group, double lineHeight) {
        checkControl(group);
        return (int) (((defaultFontSize * DPI_Y) / 72.0) * lineHeight);
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
