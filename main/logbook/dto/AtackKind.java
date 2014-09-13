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
}
