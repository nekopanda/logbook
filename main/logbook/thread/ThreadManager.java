/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.thread;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * スレッドを管理します
 *
 */
public final class ThreadManager {

    private static List<Thread> threads = new ArrayList<Thread>();

    private static Map<Thread, ExceptionHandler> handlermap = new HashMap<Thread, ExceptionHandler>();

    /**
     * スレッドを管理下に置きます
     * 
     * @param thread
     */
    public static void regist(Thread thread) {
        threads.add(thread);
    }

    /**
     * 管理しているスレッドを開始します
     */
    public static void start() {
        for (Thread thread : threads) {
            if (!thread.isAlive()) {
                thread.setDaemon(true);

                ExceptionHandler handler = new ExceptionHandler();
                handlermap.put(thread, handler);
                thread.setUncaughtExceptionHandler(handler);

                thread.start();
            }
        }
    }

    /**
     * 管理しているスレッドを取得します
     * @return
     */
    static List<Thread> getThreads() {
        return Collections.unmodifiableList(threads);
    }

    /**
     * 例外ハンドラを取得します
     * 
     * @param thread
     * @return
     */
    static ExceptionHandler getUncaughtExceptionHandler(Thread thread) {
        return handlermap.get(thread);
    }

    /**
     * エラーハンドラ
     *
     */
    public static final class ExceptionHandler implements Thread.UncaughtExceptionHandler {

        private Throwable throwable;

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            this.throwable = e;
        }

        @Override
        public String toString() {
            if (this.throwable == null) {
                return "";
            }
            StringWriter stringWriter = new StringWriter();
            PrintWriter printWriter = new PrintWriter(stringWriter);
            this.throwable.printStackTrace(printWriter);
            printWriter.close();
            return stringWriter.toString();
        }
    }
}
