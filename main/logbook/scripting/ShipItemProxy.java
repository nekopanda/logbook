/**
 * 
 */
package logbook.scripting;

import logbook.constants.AppConstants;
import logbook.dto.ShipDto;
import logbook.dto.ShipFilterDto;
import logbook.scripting.ScriptLoader.MethodInvoke;
import logbook.scripting.ScriptLoader.TableScriptCollection;

/**
 * @author Nekopanda
 *
 */
public class ShipItemProxy implements ShipItemListener {

    private class BodyMethod implements MethodInvoke {
        public ShipDto ship;

        @Override
        public Object invoke(Object arg) {
            return ((ShipItemListener) arg).body(this.ship);
        }
    }

    private TableScriptCollection script;
    private final BodyMethod bodyMethod = new BodyMethod();

    private static ShipItemProxy instance = new ShipItemProxy();

    public static ShipItemProxy get() {
        instance.script = ScriptLoader.getTableScript(
                AppConstants.SHIPTABLE_PREFIX, ShipItemListener.class);
        return instance;
    }

    @Override
    public String[] header() {
        return this.script.header();
    }

    @Override
    public void begin(final boolean specdiff, final ShipFilterDto filter, final int specdisp) {
        this.script.invoke(new MethodInvoke() {
            @Override
            public Object invoke(Object arg) {
                ((ShipItemListener) arg).begin(specdiff, filter, specdisp);
                return null;
            }
        });
    }

    @Override
    public Comparable[] body(ShipDto ship) {
        this.bodyMethod.ship = ship;
        return this.script.body(this.bodyMethod);
    }

    @Override
    public void end() {
        this.script.invoke(new MethodInvoke() {
            @Override
            public Object invoke(Object arg) {
                ((ShipItemListener) arg).end();
                return null;
            }
        });
    }

}
