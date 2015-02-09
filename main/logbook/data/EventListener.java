package logbook.data;

/**
 * 受け取った通信データを処理するリスナーです
 *
 */
public interface EventListener {

    /**
     * 通信データを処理します
     * 
     * @param type データの種類
     * @param data データ
     */
    void update(DataType type, Data data);
}
