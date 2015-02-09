package logbook.data;

/**
 * イベントリスナーがどのイベントを受け取るかを表す注釈です
 *
 */
public @interface EventTarget {

    DataType[] value();
}
