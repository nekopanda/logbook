/**
 * 
 */
package logbook.dto;

/**
 * @author Nekopanda
 *
 */
public enum ResultRank {

    PERFECT("S", "完全勝利!!S"),
    S("S", "勝利S"),
    A("A", "勝利A"),
    B("B", "戦術的勝利B"),
    C("C", "戦術的敗北C"),
    D("D", "敗北D"),
    E("E", "敗北E"),
    D_OR_E("D or E", "敗北D or E"),
    B_OR_C("C", "戦術的敗北C （Bの可能性もあり）");

    private String rank;
    private String description;

    private ResultRank(String rank, String desc) {
        this.rank = rank;
        this.description = desc;
    }

    @Override
    public String toString() {
        return this.description;
    }

    public String rank() {
        return this.rank;
    }
}
