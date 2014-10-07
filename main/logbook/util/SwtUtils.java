package logbook.util;

import javax.annotation.CheckForNull;

import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TaskBar;
import org.eclipse.swt.widgets.TaskItem;

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
}
