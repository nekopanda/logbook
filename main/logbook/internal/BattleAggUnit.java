package logbook.internal;

/**
 * 出撃統計の単位
 */
public enum BattleAggUnit {
    /** デイリー */
    DAILY("デイリー"),

    /** ウィークリー */
    WEEKLY("ウィークリー"),

    /** マンスリー */
    MONTHLY("マンスリー"),

    /** 先週 */
    LAST_WEEK("先週"),

    /** 先月 */
    LAST_MONTH("先月");

    /** 名前 */
    private String name;

    /**
     * コンストラクター 
     */
    private BattleAggUnit(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
