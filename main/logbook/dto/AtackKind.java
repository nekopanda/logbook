/**
 * 
 */
package logbook.dto;

import logbook.proto.LogbookEx.AtackKindPb;

/**
 * @author Nekopanda
 *
 */
public enum AtackKind {
    AIR,
    SUPPORT,
    HOUGEKI,
    RAIGEKI;

    public AtackKindPb toProto() {
        switch (this) {
        case AIR:
            return AtackKindPb.AIR;
        case SUPPORT:
            return AtackKindPb.SUPPORT;
        case HOUGEKI:
            return AtackKindPb.HOUGEKI;
        case RAIGEKI:
            return AtackKindPb.RAIGEKI;
        }
        return null;
    }

    public static AtackKind fromProto(AtackKindPb pb) {
        switch (pb.getNumber()) {
        case 0:
            return AIR;
        case 1:
            return SUPPORT;
        case 2:
            return HOUGEKI;
        case 3:
            return RAIGEKI;
        }
        return null;
    }
}
