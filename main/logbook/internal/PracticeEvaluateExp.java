package logbook.internal;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 戦闘での評価
 *
 */
public class PracticeEvaluateExp {

    /**
     * 戦闘での評価プリセット値
     */
    private static final Map<String, Double> EVAL_EXP = new LinkedHashMap<String, Double>() {
        {
            this.put("S完全勝利", 1.2d);
            this.put("S勝利", 1.2d);
            this.put("A勝利", 1.0d);
            this.put("B戦術的勝利", 1.0d);
            this.put("C戦術的敗北", 0.64d);
            this.put("D敗北", 0.56d);
        }
    };

    /**
     * 戦闘での評価を取得します
     * @return 戦闘での評価
     */
    public static Map<String, Double> get() {
        return Collections.unmodifiableMap(EVAL_EXP);
    }
}

