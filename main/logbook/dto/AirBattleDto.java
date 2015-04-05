/**
 * 
 */
package logbook.dto;

import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import logbook.internal.Item;

import com.dyuproject.protostuff.Tag;

/**
 * @author Nekopanda
 *
 */
public class AirBattleDto {

    /** 攻撃シーケンス */
    @Tag(1)
    public List<BattleAtackDto> atacks;
    /** 触接 [味方, 敵] */
    @Tag(2)
    public int[] touchPlane;
    /** 制空状態 */
    @Tag(3)
    public String seiku;
    /** stage1 艦載機数 [味方ロスト, 味方全, 敵ロスト, 敵全] */
    @Tag(4)
    public int[] stage1;
    /** stage2 艦載機数 [味方ロスト, 味方全, 敵ロスト, 敵全] */
    @Tag(5)
    public int[] stage2;

    private static int[] readPlaneCount(JsonObject stage) {
        return new int[] {
                stage.getInt("api_f_lostcount"),
                stage.getInt("api_f_count"),
                stage.getInt("api_e_lostcount"),
                stage.getInt("api_e_count")
        };
    }

    public AirBattleDto(JsonObject kouku, boolean isCombined) {
        JsonValue jsonStage1 = kouku.get("api_stage1");
        if ((jsonStage1 != null) && (jsonStage1 != JsonValue.NULL)) {
            JsonObject jsonStage1Obj = kouku.getJsonObject("api_stage1");
            this.stage1 = readPlaneCount(jsonStage1Obj);
            JsonArray jsonTouchPlane = jsonStage1Obj.getJsonArray("api_touch_plane");
            this.touchPlane = new int[] {
                    jsonTouchPlane.getInt(0),
                    jsonTouchPlane.getInt(1)
            };
            this.seiku = toSeiku(jsonStage1Obj.getInt("api_disp_seiku"));
        }

        JsonValue jsonStage2 = kouku.get("api_stage2");
        if ((jsonStage2 != null) && (jsonStage2 != JsonValue.NULL)) {
            JsonObject jsonStage2Obj = kouku.getJsonObject("api_stage2");
            this.stage2 = readPlaneCount(jsonStage2Obj);
        }

        this.atacks = BattleAtackDto.makeAir(
                kouku.get("api_plane_from"),
                kouku.get("api_stage3"),
                isCombined ? kouku.get("api_stage3_combined") : null);
    }

    private static String toSeiku(int id) {
        switch (id) {
        case 1:
            return "制空権確保";
        case 2:
            return "航空優勢";
        case 0:
            return "航空互角";
        case 3:
            return "航空劣勢";
        case 4:
            return "制空権喪失";
        default:
            return "不明(" + id + ")";
        }
    }

    public String[] getStage1ShortString() {
        return getNumPlaneString(this.stage1, false);
    }

    public String[] getStage2ShortString() {
        return getNumPlaneString(this.stage2, false);
    }

    public String[] getStage1DetailedString() {
        return getNumPlaneString(this.stage1, true);
    }

    public String[] getStage2DetailedString() {
        return getNumPlaneString(this.stage2, true);
    }

    /**
     * 艦載機ロスト表示を生成 [味方・敵]
     * @param stage
     * @return
     */
    private static String[] getNumPlaneString(int[] stage, boolean detail) {
        if (stage == null) {
            return new String[] { "", "" };
        }
        int flost = stage[0];
        int fall = stage[1];
        int elost = stage[2];
        int eall = stage[3];
        int fremain = fall - flost;
        int eremain = eall - elost;
        if (detail) {
            return new String[] {
                    String.valueOf(fall) + "→" + fremain + " (-" + flost + ")",
                    String.valueOf(eall) + "→" + eremain + " (-" + elost + ")"
            };
        }
        else {
            return new String[] {
                    String.valueOf(fall) + "→" + fremain,
                    String.valueOf(eall) + "→" + eremain
            };
        }
    }

    public static String[] toTouchPlaneString(int[] touchPlane) {
        if (touchPlane == null) {
            return new String[] { "", "" };
        }
        String[] ret = new String[2];
        for (int i = 0; i < 2; ++i) {
            if (touchPlane[i] == -1) {
                ret[i] = "なし";
            }
            else {
                ItemInfoDto item = Item.get(touchPlane[i]);
                if (item != null) {
                    ret[i] = item.getName();
                }
                else {
                    ret[i] = "あり（機体不明）";
                }
            }
        }
        return ret;
    }

    /**
     * 触接表示を生成 [味方・敵]
     * @param touchPlane
     * @return
     */
    public String[] getTouchPlane() {
        return toTouchPlaneString(this.touchPlane);
    }
}
