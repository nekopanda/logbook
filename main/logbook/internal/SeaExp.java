package logbook.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 海域Exp
 *
 */
public class SeaExp {

    /**
     * 海域Expプリセット値
     */
    private static final Map<String, Integer> SEA_EXP = new LinkedHashMap<String, Integer>() {
        {
            this.put("1-1", 30);
            this.put("1-2", 50);
            this.put("1-3", 80);
            this.put("1-4", 100);
            this.put("1-5", 150);
            this.put("2-1", 120);
            this.put("2-2", 150);
            this.put("2-3", 200);
            this.put("2-4", 300);
            this.put("3-1", 310);
            this.put("3-2", 320);
            this.put("3-3", 330);
            this.put("3-4", 350);
            this.put("4-1", 310);
            this.put("4-2", 320);
            this.put("4-3", 330);
            this.put("4-4", 340);
            this.put("5-1", 360);
            this.put("5-2", 380);
            this.put("5-3", 400);
            this.put("5-4", 420);
            this.put("5-5", 450);
        }
    };

    /**
     * 海域Expを取得します
     * 
     * @return
     */
    public static Map<String, Integer> get() {
        return Collections.unmodifiableMap(SEA_EXP);
    }
}
