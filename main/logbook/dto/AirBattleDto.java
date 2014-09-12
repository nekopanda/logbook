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
    public boolean[] touchPlane;
    /** 制空 */
    public int seiku;

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
            this.touchPlane = new boolean[] {
                    (jsonTouchPlane.getInt(0) != -1),
                    (jsonTouchPlane.getInt(1) != -1)
            };
            this.seiku = jsonStage1Obj.getInt("api_disp_seiku");
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
}
