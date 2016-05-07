/**
 * 
 */
package logbook.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import com.dyuproject.protostuff.Tag;

/**
 * 攻撃シーケンス
 * @author Nekopanda
 */
public class BattleAtackDto {
    /** 攻撃の種類 */
    @Tag(1)
    public AtackKind kind;
    /** 砲撃のタイプ */
    @Tag(9)
    public int type = -1;
    /** 味方からの攻撃か？ */
    @Tag(2)
    public boolean friendAtack;
    /** 攻撃元(0-11) */
    @Tag(3)
    public int[] origin;
    /** 雷撃の攻撃先 */
    @Tag(4)
    public int[] ot;
    /** 雷撃の与ダメージ */
    @Tag(5)
    public int[] ydam;
    /** 攻撃先(0-11) */
    @Tag(6)
    public int[] target;
    /** ダメージ */
    @Tag(7)
    public int[] damage;
    /** クリティカル */
    @Tag(8)
    public int[] critical;

    private static List<BattleAtackDto> makeHougeki(
            JsonArray at_list, JsonArray at_type, JsonArray df_list, JsonArray cl_list, JsonArray damage_list) {
        ArrayList<BattleAtackDto> result = new ArrayList<BattleAtackDto>();
        ArrayList<Integer> flatten_df_list = new ArrayList<Integer>();
        ArrayList<Integer> flatten_damage_list = new ArrayList<Integer>();
        ArrayList<Integer> flatten_cl_list = new ArrayList<Integer>();

        for (int i = 1; i < at_list.size(); ++i) {
            int at = at_list.getInt(i);
            JsonArray df = (JsonArray) df_list.get(i);
            JsonArray damage = (JsonArray) damage_list.get(i);
            JsonArray cl = (JsonArray) cl_list.get(i);
            for (int d = 0; d < damage.size(); ++d) {
                int dfd = df.getInt(d);
                int damd = damage.getInt(d);
                int cld = cl.getInt(d);
                if (dfd != -1) {
                    flatten_df_list.add((dfd - 1) % 6);
                    flatten_damage_list.add(damd);
                    flatten_cl_list.add(cld);
                }
            }
            int length = flatten_df_list.size();
            if (length > 0) {
                BattleAtackDto dto = new BattleAtackDto();
                dto.kind = AtackKind.HOUGEKI;
                dto.friendAtack = (at <= 6);
                if (at_type != null) {
                    dto.type = at_type.getInt(i);
                }
                dto.origin = new int[] { (at - 1) % 6 };
                dto.target = new int[length];
                dto.damage = new int[length];
                dto.critical = new int[length];
                for (int c = 0; c < length; ++c) {
                    dto.target[c] = flatten_df_list.get(c);
                    dto.damage[c] = flatten_damage_list.get(c);
                    dto.critical[c] = flatten_cl_list.get(c);
                }
                result.add(dto);
            }
            flatten_df_list.clear();
            flatten_damage_list.clear();
            flatten_cl_list.clear();
        }

        return result;
    }

    private static BattleAtackDto makeRaigeki(boolean friendAtack,
            JsonArray rai_list, JsonArray dam_list, JsonArray cl_list, JsonArray ydam_list) {
        int[] originMap = new int[6];
        int[] targetMap = new int[6];
        boolean[] targetEnabled = new boolean[6];
        BattleAtackDto dto = new BattleAtackDto();
        dto.kind = AtackKind.RAIGEKI;
        dto.friendAtack = friendAtack;

        int idx = 0;
        for (int i = 0; i < 6; ++i) {
            int rai = rai_list.getInt(i + 1);
            if (rai > 0) {
                originMap[i] = idx++;
                targetEnabled[rai - 1] = true;
            }
        }
        dto.origin = new int[idx];
        dto.ydam = new int[idx];
        dto.ot = new int[idx];

        idx = 0;
        for (int i = 0; i < 6; ++i) {
            if (targetEnabled[i]) {
                targetMap[i] = idx++;
            }
        }
        dto.target = new int[idx];
        dto.damage = new int[idx];
        dto.critical = new int[idx];

        for (int i = 0; i < 6; ++i) {
            int rai = rai_list.getInt(i + 1);
            int dam = dam_list.getInt(i + 1);
            int cl = cl_list.getInt(i + 1);
            int ydam = ydam_list.getInt(i + 1);
            if (rai > 0) {
                dto.origin[originMap[i]] = i;
                dto.ydam[originMap[i]] = ydam;
                dto.ot[originMap[i]] = targetMap[rai - 1];
            }
            if (targetEnabled[i]) {
                dto.target[targetMap[i]] = i;
                dto.damage[targetMap[i]] = dam;
                dto.critical[targetMap[i]] = cl;
            }
        }

        return dto;
    }

    private static BattleAtackDto makeAir(boolean friendAtack,
            JsonArray plane_from, JsonArray dam_list, JsonArray cdam_list, JsonArray cl_list, JsonArray ccl_list,
            boolean isBase) {
        BattleAtackDto dto = new BattleAtackDto();
        dto.kind = isBase ? AtackKind.AIRBASE : AtackKind.AIR;
        dto.friendAtack = friendAtack;

        int idx = 0;
        for (int i = 0; i < plane_from.size(); ++i) {
            if (plane_from.getInt(i) != -1)
                ++idx;
        }
        dto.origin = new int[idx];
        idx = 0;
        for (int i = 0; i < plane_from.size(); ++i) {
            if (plane_from.getInt(i) != -1)
                dto.origin[idx++] = (plane_from.getInt(i) - 1) % 6;
        }
        idx = 0;
        for (int i = 0; i < 6; ++i) {
            int dam = dam_list.getInt(i + 1);
            if (dam > 0) {
                idx++;
            }
        }
        if (cdam_list != null) {
            for (int i = 0; i < 6; ++i) {
                int dam = cdam_list.getInt(i + 1);
                if (dam > 0) {
                    idx++;
                }
            }
        }
        dto.target = new int[idx];
        dto.damage = new int[idx];
        dto.critical = new int[idx];
        idx = 0;
        for (int i = 0; i < 6; ++i) {
            int dam = dam_list.getInt(i + 1);
            int cl = cl_list.getInt(i + 1);
            if (dam > 0) {
                dto.target[idx] = i;
                dto.damage[idx] = dam;
                dto.critical[idx] = cl;
                idx++;
            }
        }
        if (cdam_list != null) {
            for (int i = 0; i < 6; ++i) {
                int dam = cdam_list.getInt(i + 1);
                int cl = ccl_list.getInt(i + 1);
                if (dam > 0) {
                    dto.target[idx] = i + 6;
                    dto.damage[idx] = dam;
                    // クリティカルフラグを砲撃と合わせる
                    dto.critical[idx] = cl + 1;
                    idx++;
                }
            }
        }

        return dto;
    }

    private void makeOriginCombined() {
        for (int i = 0; i < this.origin.length; ++i) {
            this.origin[i] += 6;
        }
    }

    private void makeTargetCombined() {
        for (int i = 0; i < this.target.length; ++i) {
            this.target[i] += 6;
        }
    }

    /**
     * 航空戦を読み込む
     * @param plane_from
     * @param raigeki
     * @param combined
     * @return
     */
    public static List<BattleAtackDto> makeAir(JsonValue plane_from, JsonValue raigeki, JsonValue combined,
            boolean isBase) {
        if ((raigeki == null) || (raigeki == JsonValue.NULL) || (plane_from == null) || (plane_from == JsonValue.NULL))
            return null;

        JsonObject raigeki_obj = (JsonObject) raigeki;
        JsonArray fdamCombined = null;
        JsonArray fclCombined = null;
        if ((combined != null) && (combined != JsonValue.NULL)) {
            fdamCombined = ((JsonObject) combined).getJsonArray("api_fdam");
            fclCombined = ((JsonObject) combined).getJsonArray("api_fcl_flag");
        }

        BattleAtackDto fatack = makeAir(
                true,
                ((JsonArray) plane_from).getJsonArray(0),
                raigeki_obj.getJsonArray("api_edam"),
                null,
                raigeki_obj.getJsonArray("api_ecl_flag"),
                null,
                isBase);

        if (isBase) {
            return Arrays.asList(new BattleAtackDto[] { fatack });
        }

        BattleAtackDto eatack = makeAir(
                false,
                ((JsonArray) plane_from).getJsonArray(1),
                raigeki_obj.getJsonArray("api_fdam"),
                fdamCombined,
                raigeki_obj.getJsonArray("api_fcl_flag"),
                fclCombined,
                false);

        return Arrays.asList(new BattleAtackDto[] { fatack, eatack });
    }

    /**
     * 雷撃戦を読み込む
     * @param raigeki
     * @param second
     * @return
     */
    public static List<BattleAtackDto> makeRaigeki(JsonValue raigeki, boolean second) {
        if ((raigeki == null) || (raigeki == JsonValue.NULL))
            return null;

        JsonObject raigeki_obj = (JsonObject) raigeki;

        BattleAtackDto fatack = makeRaigeki(
                true,
                raigeki_obj.getJsonArray("api_frai"),
                raigeki_obj.getJsonArray("api_edam"),
                raigeki_obj.getJsonArray("api_ecl"),
                raigeki_obj.getJsonArray("api_fydam"));

        if (second) {
            fatack.makeOriginCombined();
        }

        BattleAtackDto eatack = makeRaigeki(
                false,
                raigeki_obj.getJsonArray("api_erai"),
                raigeki_obj.getJsonArray("api_fdam"),
                raigeki_obj.getJsonArray("api_fcl"),
                raigeki_obj.getJsonArray("api_eydam"));

        if (second) {
            eatack.makeTargetCombined();
        }

        return Arrays.asList(new BattleAtackDto[] { fatack, eatack });
    }

    /**
     * api_hougeki* を読み込む
     * @param hougeki
     */
    public static List<BattleAtackDto> makeHougeki(JsonValue hougeki, boolean second) {
        if ((hougeki == null) || (hougeki == JsonValue.NULL))
            return null;

        JsonObject hougeki_obj = (JsonObject) hougeki;

        List<BattleAtackDto> seq = makeHougeki(
                hougeki_obj.getJsonArray("api_at_list"),
                hougeki_obj.getJsonArray("api_at_type"),
                hougeki_obj.getJsonArray("api_df_list"),
                hougeki_obj.getJsonArray("api_cl_list"),
                hougeki_obj.getJsonArray("api_damage"));

        // 連ぐ艦隊を反映
        if (second) {
            for (BattleAtackDto dto : seq) {
                if (dto.friendAtack) {
                    dto.makeOriginCombined();
                }
                else {
                    dto.makeTargetCombined();
                }
            }
        }

        return seq;
    }

    /**
     * 支援艦隊の攻撃を読み込む
     * @param dam_list
     * @return
     */
    public static List<BattleAtackDto> makeSupport(JsonArray dam_list) {
        BattleAtackDto dto = new BattleAtackDto();
        dto.kind = AtackKind.SUPPORT;
        dto.friendAtack = true;

        int idx = 0;
        for (int i = 0; i < 6; ++i) {
            int dam = dam_list.getInt(i + 1);
            if (dam > 0) {
                idx++;
            }
        }
        dto.target = new int[idx];
        dto.damage = new int[idx];
        idx = 0;
        for (int i = 0; i < 6; ++i) {
            int dam = dam_list.getInt(i + 1);
            if (dam > 0) {
                dto.target[idx] = i;
                dto.damage[idx] = dam;
                idx++;
            }
        }

        return Arrays.asList(new BattleAtackDto[] { dto });
    }

    public String getHougekiTypeString() {
        switch (this.type) {
        case -1:
            return "";
        case 0:
            //return "通常"; // 見づらくなるので
            return "";
        case 1:
            return "レーザー攻撃";
        case 2:
            return "連撃";
        case 3:
            return "カットイン(主砲/副砲)";
        case 4:
            return "カットイン(主砲/電探)";
        case 5:
            return "カットイン(主砲/徹甲)";
        case 6:
            return "カットイン(主砲/主砲)";
        }
        return "不明(" + this.type + ")";
    }
}
