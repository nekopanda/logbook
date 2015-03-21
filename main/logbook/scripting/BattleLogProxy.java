/**
 * 
 */
package logbook.scripting;

import logbook.constants.AppConstants;
import logbook.dto.BattleExDto;
import logbook.scripting.ScriptLoader.MethodInvoke;
import logbook.scripting.ScriptLoader.TableScriptCollection;

/**
 * @author Nekopanda
 *
 */
public class BattleLogProxy implements BattleLogListener {

    private class BodyMethod implements MethodInvoke {
        public BattleExDto battle;

        @Override
        public Object invoke(Object arg) {
            return ((BattleLogListener) arg).body(this.battle);
        }
    }

    private TableScriptCollection script;
    private final BodyMethod bodyMethod = new BodyMethod();

    private final MethodInvoke beginMethod = new MethodInvoke() {
        @Override
        public Object invoke(Object arg) {
            ((BattleLogListener) arg).begin();
            return null;
        }
    };
    private final MethodInvoke endMethod = new MethodInvoke() {
        @Override
        public Object invoke(Object arg) {
            ((BattleLogListener) arg).end();
            return null;
        }
    };

    private static BattleLogProxy instance = new BattleLogProxy();

    public static BattleLogProxy get() {
        instance.script = ScriptLoader.getTableScript(
                AppConstants.DROPTABLE_PREFIX, BattleLogListener.class);
        return instance;
    }

    @Override
    public String[] header() {
        return this.script.header();
    }

    @Override
    public Comparable[] body(BattleExDto battle) {
        this.bodyMethod.battle = battle;
        return this.script.body(this.bodyMethod);
    }

    @Override
    public void begin() {
        this.script.invoke(this.beginMethod);
    }

    @Override
    public void end() {
        this.script.invoke(this.endMethod);
    }

}
