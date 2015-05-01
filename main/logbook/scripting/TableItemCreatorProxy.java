/**
 * 
 */
package logbook.scripting;

import logbook.constants.AppConstants;
import logbook.gui.logic.ColorManager;
import logbook.gui.logic.TableItemCreator;
import logbook.scripting.ScriptLoader.MethodInvoke;
import logbook.scripting.ScriptLoader.Script;
import logbook.util.ReportUtils;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * @author Nekopanda
 *
 */
public class TableItemCreatorProxy implements TableItemCreator {

    private class CreateMethod implements MethodInvoke {
        public Table table;
        public Comparable[] data;
        public int index;

        @Override
        public Object invoke(Object arg) {
            return ((TableItemCreator) arg).create(this.table, this.data, this.index);
        }
    }

    private Script script;
    private final CreateMethod createMethod = new CreateMethod();

    private static TableItemCreatorProxy instance = new TableItemCreatorProxy();

    public static TableItemCreatorProxy get(String prefix) {
        instance.script = ScriptLoader.getTableStyleScript(prefix);
        return instance;
    }

    @Override
    public TableItem create(Table table, Comparable[] data, int index) {
        this.createMethod.table = table;
        this.createMethod.data = data;
        this.createMethod.index = index;
        TableItem item = (TableItem) this.script.invoke(this.createMethod);
        if (item == null) {
            // 作れてなかったらデフォルトロジックで作る
            item = this.defautlCreate(table, data, index);
        }
        return item;
    }

    private TableItem defautlCreate(Table table, Comparable[] data, int index) {
        TableItem item = new TableItem(table, SWT.NONE);
        // 偶数行に背景色を付ける
        if ((index % 2) != 0) {
            item.setBackground(ColorManager.getColor(AppConstants.ROW_BACKGROUND));
        }
        item.setText(ReportUtils.toStringArray(data));
        return item;
    }

    @Override
    public void begin(final String[] header) {
        this.script.invoke(new MethodInvoke() {
            @Override
            public Object invoke(Object arg) {
                ((TableItemCreator) arg).begin(header);
                return null;
            }
        });
    }

    @Override
    public void end() {
        this.script.invoke(new MethodInvoke() {
            @Override
            public Object invoke(Object arg) {
                ((TableItemCreator) arg).end();
                return null;
            }
        });
    }

}
