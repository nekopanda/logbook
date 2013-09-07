package logbook.data;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * <p>
 * サーバースレッドから渡されるデータをスレッドセーフに管理します<br>
 * キューに追加されたデータは別スレッドから安全に取り出せます
 * </p>
 */
public class DataQueue {

    private static final ConcurrentLinkedQueue<Data> DATA_QUEUE = new ConcurrentLinkedQueue<Data>();

    public static void add(Data data) {
        DATA_QUEUE.add(data);
    }

    public static Data poll() {
        return DATA_QUEUE.poll();
    }
}
