package logbook.internal;

/**
 * 出撃海域
 *
 */
public enum SallyArea {

    NOTHING(0, ""),

    EVENT_AL(1, "AL作戦"),

    EVENT_MI(2, "MI作戦");

    private static SallyArea[] areas;

    private final int val;

    private final String name;

    /**
     * 出撃海域
     */
    private SallyArea(int val, String name) {
        this.val = val;
        this.name = name;
    }

    public int getValue() {
        return this.val;
    }

    public String getName() {
        return this.name;
    }

    public static SallyArea valueOf(int val) {
        if (areas.length > val) {
            return areas[val];
        }
        return valueOf(0);
    }

    static {
        SallyArea[] values = values();
        areas = new SallyArea[values.length];
        for (int i = 0; i < values.length; i++) {
            areas[values[i].getValue()] = values[i];
        }
    }
}
