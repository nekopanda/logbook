/**
 * 
 */
package logbook.scripting;

import logbook.constants.AppConstants;
import logbook.dto.QuestDto;
import logbook.scripting.ScriptLoader.MethodInvoke;
import logbook.scripting.ScriptLoader.TableScriptCollection;

/**
 * @author Nekopanda
 *
 */
public class QuestProxy implements QuestListener {

    private class BodyMethod implements MethodInvoke {
        public QuestDto data;

        @Override
        public Object invoke(Object arg) {
            return ((QuestListener) arg).body(this.data);
        }
    }

    private TableScriptCollection script;
    private final BodyMethod bodyMethod = new BodyMethod();

    private static QuestProxy instance = new QuestProxy();

    public static QuestProxy get() {
        instance.script = ScriptLoader.getTableScript(
                AppConstants.QUESTTABLE_PREFIX, QuestListener.class);
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
                ((QuestListener) arg).begin();
                return null;
            }
        });
    }

    @Override
    public Comparable[] body(QuestDto data) {
        this.bodyMethod.data = data;
        return this.script.body(this.bodyMethod);
    }

    @Override
    public void end() {
        this.script.invoke(new MethodInvoke() {
            @Override
            public Object invoke(Object arg) {
                ((QuestListener) arg).end();
                return null;
            }
        });
    }

}
