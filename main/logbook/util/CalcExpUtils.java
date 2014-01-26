/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.annotation.CheckForNull;

import logbook.internal.ExpTable;

/**
 * 経験値を計算するutilです
 *
 */
public class CalcExpUtils {

    /**
     * 次のレベルまでに必要な経験値。次のレベルがない場合はnullを返します
     * 
     * @param nowlv 現在のレベル
     * @return 次のレベルまでに必要な経験値
     */
    @CheckForNull
    public static Long getNextLvExp(int nowlv) {
        return ExpTable.get().get(nowlv + 1);
    }

    /**
     * 戦闘で得られる経験値を計算します
     * 
     * @param baseexp 海域Exp
     * @param eval 評価倍率
     * @param isFlagship 旗艦
     * @param isMvp MVP
     * @return 得られる経験値
     */
    public static long getExp(int baseexp, double eval, boolean isFlagship, boolean isMvp) {
        // 得られる経験値
        double getexpd = baseexp * eval;
        if (isFlagship) {
            getexpd *= 1.5;
        }
        if (isMvp) {
            getexpd *= 2;
        }
        // 最大累積 3.6倍
        getexpd = Math.min(getexpd, baseexp * 3.6);

        return Math.round(getexpd);
    }

    /**
     * 必要経験値を1回あたりの経験値で割った数値を計算します。端数は切り上げされます
     * 
     * @param needexp 必要経験値
     * @param exp 1回あたりの経験値
     * @return 
     */
    public static int getCount(long needexp, long exp) {
        return BigDecimal.valueOf(needexp).divide(BigDecimal.valueOf(exp), RoundingMode.CEILING)
                .intValue();
    }
}
