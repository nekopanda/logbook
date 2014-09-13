/**
 * 
 */
package logbook.dto;

import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

/**
 * @author Nekopanda
 *
 */
public class AirBattleDto {

    public List<BattleAtackDto> atacks;

    /** 接触（味方・敵） */
    public int[] touchPlane;
    /** 制空 */
    public String seiku;

    /** 艦載機数 [味方ロスト, 味方全, 敵ロスト, 敵全] */
    public int[] stage1;
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
                kouku.getJsonArray("api_plane_from"),
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
}
