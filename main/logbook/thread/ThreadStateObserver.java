/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.thread;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

/**
 * スレッドの状態を監視します
 *
 */
public final class ThreadStateObserver extends Thread {

    private final Shell shell;

    private final List<Thread> observthread;

    /**
     * コンストラクター
     */
    public ThreadStateObserver(Shell shell) {
        this.observthread = new ArrayList<Thread>(ThreadManager.getThreads());
        this.shell = shell;
        this.setName("logbook_thread_state_observer");
    }

    /* (非 Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        try {
            while (true) {
                for (int i = 0; i < this.observthread.size(); i++) {
                    Thread target = this.observthread.get(i);
                    if (!target.isAlive()) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(target.getClass().getName());
                        sb.append("[");
                        sb.append(target.getName());
                        sb.append("]");
                        sb.append("が予期せず終了しました、エラーログを確認して下さい。\n");
                        sb.append("補足した例外:\n");
                        sb.append(ThreadManager.getUncaughtExceptionHandler(target));

                        final String message = sb.toString();
                        Display.getDefault().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                MessageBox box = new MessageBox(ThreadStateObserver.this.shell, SWT.YES
                                        | SWT.ICON_ERROR);
                                box.setText("スレッドが予期せず終了しました");
                                box.setMessage(message);
                                box.open();
                            }
                        });
                        this.observthread.remove(i);
                    }
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
