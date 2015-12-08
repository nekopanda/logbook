package logbook.util;

import logbook.internal.ExpTable;
import logbook.internal.PracticeEvaluateExp;

/**
 * 経験値を計算するutilです
 *
 */
public class CalcPracticeExpUtils {

    /**
     * 演習で得られる経験値を計算します
     * 
     * @param firstLevel 旗艦レベル
     * @param secondLevel 2艦目レベル
     * @param eval 評価倍率
     * @param isFlagship 旗艦
     * @param isMvp MVP
     * @return 得られる経験値
     */
    public static int getExp(int firstLevel, int secondLevel, double eval, boolean isFlagship, boolean isMvp) {
        if ((firstLevel < 1) || (firstLevel > ExpTable.MAX_LEVEL) || (secondLevel < 1)
                || (secondLevel > ExpTable.MAX_LEVEL)) {
            return 0;
        }

        double baseexp = Math.floor((ExpTable.get().get(firstLevel) / 100.0)
                + (ExpTable.get().get(secondLevel) / 300.0));
        if (baseexp > 500) {
            baseexp = Math.floor(500 + Math.sqrt(baseexp - 500));
        }
        // 得られる経験値
        double getexpd = Math.floor(baseexp * eval);
        if (isFlagship) {
            getexpd *= 1.5;
        }
        if (isMvp) {
            getexpd *= 2;
        }

        return (int) Math.floor(getexpd);
    }

    public static void main(String[] args) {
        int f = 149, s = 150;
        String[] ranks = { "S勝利", "A勝利", "C戦術的敗北", "D敗北" };
        for (String r : ranks) {
            System.out.println(r);
            System.out.println("基本経験値:"
                    + CalcPracticeExpUtils.getExp(f, s, PracticeEvaluateExp.get().get(r), false, false));
            System.out
                    .println("旗艦:" + CalcPracticeExpUtils.getExp(f, s, PracticeEvaluateExp.get().get(r), true, false));
            System.out.println("MVP:"
                    + CalcPracticeExpUtils.getExp(f, s, PracticeEvaluateExp.get().get(r), false, true));
            System.out.println("旗艦&MVP:"
                    + CalcPracticeExpUtils.getExp(f, s, PracticeEvaluateExp.get().get(r), true, true));
            System.out.println("-------------");
        }
    }
}
