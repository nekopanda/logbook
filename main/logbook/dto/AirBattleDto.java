/**
 *
 */
package logbook.dto;

import com.dyuproject.protostuff.Tag;
import logbook.internal.Item;
import logbook.util.JsonUtils;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Nekopanda
 */
public class AirBattleDto {

    /**
     * 攻撃シーケンス
     */
    @Tag(1)
    public List<BattleAtackDto> atacks;
    /**
     * 触接 [味方, 敵]
     */
    @Tag(2)
    public int[] touchPlane;
    /**
     * 制空状態
     */
    @Tag(3)
    public String seiku;
    /**
     * stage1 艦載機数 [味方ロスト, 味方全, 敵ロスト, 敵全]
     */
    @Tag(4)
    public int[] stage1;
    /**
     * stage2 艦載機数 [味方ロスト, 味方全, 敵ロスト, 敵全]
     */
    @Tag(5)
    public int[] stage2;
    /**
     * 対空カットイン [発動艦0-, 種別]
     */
    @Tag(8)
    public int[] airFire;
    @Tag(9)
    public int[] airFireItems;
    /**
     * 基地航空隊ID
     */
    @Tag(21)
    public int airBaseId;

    private static int[] readPlaneCount(JsonObject stage) {
        return new int[]{
                stage.getInt("api_f_lostcount"),
                stage.getInt("api_f_count"),
                stage.getInt("api_e_lostcount"),
                stage.getInt("api_e_count")
        };
    }

    public AirBattleDto(int baseidx, int friendSecondBase, JsonObject kouku, boolean isCombined, boolean isBase) {
        JsonObject jsonStage1 = JsonUtils.getJsonObject(kouku, "api_stage1");
        if (jsonStage1 != null) {
            this.stage1 = readPlaneCount(jsonStage1);
            JsonArray jsonTouchPlane = JsonUtils.getJsonArray(jsonStage1, "api_touch_plane");
            if (jsonTouchPlane != null) {
                this.touchPlane = new int[]{
                        jsonTouchPlane.getInt(0),
                        jsonTouchPlane.getInt(1)
                };
                this.seiku = toSeiku(jsonStage1.getInt("api_disp_seiku"));
            }
        }

        JsonObject jsonStage2 = JsonUtils.getJsonObject(kouku, "api_stage2");
        if (jsonStage2 != null) {
            this.stage2 = readPlaneCount(jsonStage2);

            JsonObject jsonAirFire = JsonUtils.getJsonObject(jsonStage2, "api_air_fire");
            if (jsonAirFire != null) {
                this.airFire = new int[]{
                        jsonAirFire.getInt("api_idx"),
                        jsonAirFire.getInt("api_kind")
                };
                this.airFireItems = JsonUtils.getIntArray(jsonAirFire, "api_use_items");
            }
        }

        this.atacks = BattleAtackDto.makeAir(
                baseidx, friendSecondBase,
                JsonUtils.getJsonArray(kouku, "api_plane_from"),
                JsonUtils.getJsonObject(kouku, "api_stage3"),
                isCombined ? JsonUtils.getJsonObject(kouku, "api_stage3_combined") : null,
                isBase);
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

    private static String toTaikuCutin(int id) {
        switch (id) {
            case 1:
                return "1:高角砲x2/電探";
            case 2:
                return "2:高角砲/電探";
            case 3:
                return "3:高角砲x2";
            case 4:
                return "4:大口径主砲/三式弾/高射装置/電探";
            case 5:
                return "5:高角砲+高射装置x2/電探";
            case 6:
                return "6:大口径主砲/三式弾/高射装置";
            case 7:
                return "7:高角砲/高射装置/電探";
            case 8:
                return "8:高角砲+高射装置/電探";
            case 9:
                return "9:高角砲/高射装置";
            case 10:
                return "10:高角砲/集中機銃/電探";
            case 11:
                return "11:高角砲/集中機銃";
            case 12:
                return "12:集中機銃/機銃/電探";
            case 13:
                return "13:高角砲/集中機銃/電探";
            case 14:
                return "14:高角砲/機銃/電探";
            case 15:
                return "15:高角砲/機銃";
            case 16:
                return "16:高角砲/機銃/電探";
            case 17:
                return "17:高角砲/機銃";
            case 18:
                return "18:集中機銃";
            case 19:
                return "19:高角砲(非高射装置)/集中機銃";
            case 20:
                return "20:集中機銃";
            case 21:
                return "21:高角砲/電探";
            case 22:
                return "22:集中機銃";
            case 23:
                return "23:機銃(非集中)";
            case 24:
                return "24:高角砲/機銃(非集中)";
        }
        return "不明(" + id + ")";
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

    public String getTaikuCutinString() {
        return toTaikuCutin(this.airFire[1]);
    }

    public String getTaikuCutinItemsString() {
        return String.join("/", Arrays.stream(this.airFireItems)
                .boxed()
                .map(Item::get)
                .map(item -> Objects.nonNull(item) ? item.getName() : "装備不明")
                .toArray(String[]::new));
    }

    /**
     * 艦載機ロスト表示を生成 [味方・敵]
     *
     * @param stage
     * @return
     */
    private static String[] getNumPlaneString(int[] stage, boolean detail) {
        if (stage == null) {
            return new String[]{"", ""};
        }
        int flost = stage[0];
        int fall = stage[1];
        int elost = stage[2];
        int eall = stage[3];
        int fremain = fall - flost;
        int eremain = eall - elost;
        if (detail) {
            return new String[]{
                    String.valueOf(fall) + "→" + fremain + " (-" + flost + ")",
                    String.valueOf(eall) + "→" + eremain + " (-" + elost + ")"
            };
        } else {
            return new String[]{
                    String.valueOf(fall) + "→" + fremain,
                    String.valueOf(eall) + "→" + eremain
            };
        }
    }

    public static String[] toTouchPlaneString(int[] touchPlane) {
        if (touchPlane == null) {
            return new String[]{"", ""};
        }
        String[] ret = new String[2];
        for (int i = 0; i < 2; ++i) {
            if (touchPlane[i] == -1) {
                ret[i] = "なし";
            } else {
                ItemInfoDto item = Item.get(touchPlane[i]);
                if (item != null) {
                    ret[i] = item.getName();
                } else {
                    ret[i] = "あり（機体不明）";
                }
            }
        }
        return ret;
    }

    /**
     * 触接表示を生成 [味方・敵]
     *
     * @param touchPlane
     * @return
     */
    public String[] getTouchPlane() {
        return toTouchPlaneString(this.touchPlane);
    }
}
