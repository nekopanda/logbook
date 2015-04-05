/**
 * 
 */
package logbook.scripting;

import logbook.constants.AppConstants;
import logbook.gui.logic.ItemInfo;
import logbook.scripting.ScriptLoader.MethodInvoke;
import logbook.scripting.ScriptLoader.TableScriptCollection;

/**
 * @author Nekopanda
 *
 */
public class ItemInfoProxy implements ItemInfoListener {

    private class BodyMethod implements MethodInvoke {
        public ItemInfo data;

        @Override
        public Object invoke(Object arg) {
            return ((ItemInfoListener) arg).body(this.data);
        }
    }

    private TableScriptCollection script;
    private final BodyMethod bodyMethod = new BodyMethod();

    private static ItemInfoProxy instance = new ItemInfoProxy();

    public static ItemInfoProxy get() {
        instance.script = ScriptLoader.getTableScript(
                AppConstants.ITEMTABLE_PREFIX, ItemInfoListener.class);
        return instance;
    }

    @Override
    public String[] header() {
        return this.script.header();
    }

    @Override
    public void begin() {
        this.script.invoke(new MethodInvoke() {
            @Override
            public Object invoke(Object arg) {
                ((ItemInfoListener) arg).begin();
                return null;
            }
        });
    }

    @Override
    public Comparable[] body(ItemInfo data) {
        this.bodyMethod.data = data;
        return this.script.body(this.bodyMethod);
    }

    @Override
    public void end() {
        this.script.invoke(new MethodInvoke() {
            @Override
            public Object invoke(Object arg) {
                ((ItemInfoListener) arg).end();
                return null;
            }
        });
    }

}
