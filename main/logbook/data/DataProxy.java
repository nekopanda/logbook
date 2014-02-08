/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.data;

import logbook.data.context.GlobalContext;

import org.eclipse.swt.widgets.Display;

/**
 * <p>
 * サーバースレッドから渡されるデータをスレッドセーフに GlobalContext に反映させます<br>
 * </p>
 */
public class DataProxy {

    public static void add(final Data data) {
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                GlobalContext.updateContext(data);
            }
        });
    }

}
