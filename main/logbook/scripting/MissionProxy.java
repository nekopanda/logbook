/**
 * 
 */
package logbook.scripting;

import logbook.constants.AppConstants;
import logbook.internal.MasterData.MissionDto;
import logbook.scripting.ScriptLoader.MethodInvoke;
import logbook.scripting.ScriptLoader.TableScriptCollection;

/**
 * @author Nekopanda
 *
 */
public class MissionProxy implements MissionListener {

    private class BodyMethod implements MethodInvoke {
        public MissionDto data;

        @Override
        public Object invoke(Object arg) {
            return ((MissionListener) arg).body(this.data);
        }
    }

    private TableScriptCollection script;
    private final BodyMethod bodyMethod = new BodyMethod();

    private static MissionProxy instance = new MissionProxy();

    public static MissionProxy get() {
        instance.script = ScriptLoader.getTableScript(
                AppConstants.MISSIONTABLE_PREFIX, MissionListener.class);
        return instance;
    }

    @Override
    public String[] header() {
        return this.script.header();
    }

    @Override
    public void begin(final int fleetid) {
        this.script.invoke(new MethodInvoke() {
            @Override
            public Object invoke(Object arg) {
                ((MissionListener) arg).begin(fleetid);
                return null;
            }
        });
    }

    @Override
    public Comparable[] body(MissionDto data) {
        this.bodyMethod.data = data;
        return this.script.body(this.bodyMethod);
    }

    @Override
    public void end() {
        this.script.invoke(new MethodInvoke() {
            @Override
            public Object invoke(Object arg) {
                ((MissionListener) arg).end();
                return null;
            }
        });
    }

}
