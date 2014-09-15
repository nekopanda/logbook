/**
 * 
 */
package logbook.dto;

import logbook.proto.LogbookEx.ResultRankPb;

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

    B_OR_C("B", "C", "戦術的勝利B （Cの可能性もあり）"),
    C_OR_B("C", "B", "戦術的敗北C （Bの可能性もあり）"),
    D_OR_C("D", "C", "敗北D （Cの可能性もあり）");

    private String rank;
    private String perhaps;
    private String description;

    private ResultRank(String rank, String desc) {
        this.rank = rank;
        this.perhaps = null;
        this.description = desc;
    }

    private ResultRank(String rank, String perhaps, String desc) {
        this.rank = rank;
        this.perhaps = perhaps;
        this.description = desc;
    }

    public ResultRankPb toProto() {
        switch (this) {
        case PERFECT:
            return ResultRankPb.PERFECT;
        case S:
            return ResultRankPb.S;
        case A:
            return ResultRankPb.A;
        case B:
            return ResultRankPb.B;
        case C:
            return ResultRankPb.C;
        case D:
            return ResultRankPb.D;
        case E:
            return ResultRankPb.E;
        case B_OR_C:
            return ResultRankPb.B_OR_C;
        case C_OR_B:
            return ResultRankPb.C_OR_B;
        case D_OR_C:
            return ResultRankPb.D_OR_C;
        }
        return null;
    }

    public static ResultRank fromProto(ResultRankPb pb) {
        switch (pb.getNumber()) {
        case 0:
            return PERFECT;
        case 1:
            return S;
        case 2:
            return A;
        case 3:
            return B;
        case 4:
            return C;
        case 5:
            return D;
        case 6:
            return E;
        case 7:
            return B_OR_C;
        case 8:
            return C_OR_B;
        case 9:
            return D_OR_C;
        }
        return null;
    }

    @Override
    public String toString() {
        return this.description;
    }

    public boolean match(String rank) {
        return (this.rank.equals(rank)) ||
                ((this.perhaps != null) && this.perhaps.equals(rank));
    }

    public String rank() {
        return this.rank;
    }
}
