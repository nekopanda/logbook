package logbook.dto.chart;

import java.util.List;

import org.eclipse.swt.graphics.RGB;

/**
 * 資材を表します
 *
 */
public class Resource {

    /** ラベル */
    public final String name;
    /** 線色 */
    public final RGB color;
    /** 値 */
    public final int[] values;

    public Resource(String name, RGB color, int[] values) {
        this.color = color;
        this.name = name;
        this.values = values;
    }

    public Resource(String name, RGB color, List<Integer> values) {
        this.color = color;
        this.name = name;
        this.values = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            this.values[i] = values.get(i);
        }
    }
}
