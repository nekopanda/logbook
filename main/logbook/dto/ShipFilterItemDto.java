package logbook.dto;

import logbook.dto.AbstractDto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ShipFilterItemDto extends AbstractDto {

    public enum FilterType {
        ID("ID"),
        LV("LV"),
        SHIP_TYPE("艦種"),
        COND("疲労度"),
        LOCK("ロック"),
        FLEET("艦隊"),
        DOCK("入渠"),
        DAMAGED("損傷"),
        REPAIR("修理"),
        EXPEDITION("遠征"),
        EXPANSION("増設"),
        FIRE_POWER("火力"),
        TORPEDO("雷装"),
        AA("対空"),
        ARMOR("装甲"),
        NIGHT_BATTLE("夜戦"),
        ASW("対潜"),
        EVASION("回避"),
        LOS("索敵"),
        LUCK("運"),
        SPEED("速度"),
        SALLY_AREA("札");

        private String text;

        FilterType(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }

        @Deprecated
        public void setText(String text) {
            this.text = text;
        }

        public static FilterType codeOf(String code) {
            return Arrays.stream(FilterType.values())
                    .filter(data -> data.text.equals(code))
                    .findFirst()
                    .orElse(null);
        }
    }

    public enum EqualSign {
        EQUAL("＝"),
        NOT_EQUAL("≠"),
        LESS("＜"),
        GREATER("＞"),
        LESS_OR_EQUAL("≦"),
        GREATER_OR_EQUAL("≧");

        private String text;

        EqualSign(String text) {
            this.text = text;
        }

        public String getText() {
            return this.text;
        }

        @Deprecated
        public void setText(String text) {
            this.text = text;
        }

        public static EqualSign codeOf(String code) {
            return Arrays.stream(EqualSign.values())
                    .filter(data -> data.text.equals(code))
                    .findFirst()
                    .orElse(null);
        }

        public Boolean compareBySign(int a, int b) {
            switch (this) {
                case EQUAL:
                    return a == b;
                case NOT_EQUAL:
                    return a != b;
                case LESS:
                    return a < b;
                case GREATER:
                    return a > b;
                case LESS_OR_EQUAL:
                    return a <= b;
                case GREATER_OR_EQUAL:
                    return a >= b;
            }
            return false;
        }
    }


    public FilterType type;

    public EqualSign sign;

    public int value = 0;

    // ListはSerializeではない
    public ArrayList<Boolean> enabledType;

    public ShipFilterItemDto(FilterType type, EqualSign sign, int value) {
        this.type = type;
        this.sign = sign;
        this.value = value;
    }

    public ShipFilterItemDto(FilterType type, ArrayList<Boolean> enabledType) {
        this.type = type;
        this.enabledType = enabledType;
    }

    @Deprecated
    public ShipFilterItemDto() {

    }
}
