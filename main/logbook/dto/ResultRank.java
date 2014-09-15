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
    E("E", "敗北E");

    private String rank;
    private String description;

    private ResultRank(String rank, String desc) {
        this.rank = rank;
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
        }
        return null;
    }

    @Override
    public String toString() {
        return this.description;
    }

    public String rank() {
        return this.rank;
    }
}
