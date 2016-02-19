/**
 * 
 */
package logbook.dto;

/**
 * @author Nekopanda
 *
 */
public enum ResultRank {

    // protostuffの仕様からenumの途中への追加削除厳禁
    // 後ろへの追加のみ可

    /** 完全勝利S */
    PERFECT("S", "完全勝利!!S"),
    /** 勝利S */
    S("S", "勝利S"),
    /** 勝利A */
    A("A", "勝利A"),
    /** 戦術的勝利B */
    B("B", "戦術的勝利B"),
    /** 戦術的敗北C */
    C("C", "戦術的敗北C"),
    /** 敗北D */
    D("D", "敗北D"),
    /** 敗北E */
    E("E", "敗北E");

    private String rank;
    private String description;

    private ResultRank(String rank, String desc) {
        this.rank = rank;
        this.description = desc;
    }

    public static ResultRank fromRank(String rank) {
        if (rank.equals("S")) {
            return S;
        }
        if (rank.equals("A")) {
            return A;
        }
        if (rank.equals("B")) {
            return B;
        }
        if (rank.equals("C")) {
            return C;
        }
        if (rank.equals("D")) {
            return D;
        }
        return E;
    }

    @Override
    public String toString() {
        return this.description;
    }

    public String rank() {
        return this.rank;
    }
}
