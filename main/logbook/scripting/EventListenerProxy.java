/**
 * 
 */
package logbook.scripting;

import logbook.data.Data;
import logbook.data.DataType;
import logbook.data.EventListener;
import logbook.scripting.ScriptLoader.MethodInvoke;
import logbook.scripting.ScriptLoader.ScriptCollection;

/**
 * @author Nekopanda
 *
 */
public class EventListenerProxy implements EventListener {

    private class UpdateMethod implements MethodInvoke {
        public DataType type;
        public Data data;

        @Override
        public Object invoke(Object arg) {
            ((EventListener) arg).update(this.type, this.data);
            return null;
        }
    }

    private ScriptCollection script;
    private final UpdateMethod updateMethod = new UpdateMethod();

    private static EventListenerProxy instance = new EventListenerProxy();

    public static EventListenerProxy get() {
        instance.script = ScriptLoader.getScriptCollection("update", EventListener.class);
        return instance;
    }

    @Override
    public void update(DataType type, Data data) {
        this.updateMethod.type = type;
        this.updateMethod.data = data;
        this.script.invoke(this.updateMethod);
    }

}
