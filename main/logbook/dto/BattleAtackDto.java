/**
 * 
 */
package logbook.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import logbook.proto.LogbookEx.BattleAtackDtoPb;
import logbook.proto.Tag;

/**
 * @author Nekopanda
 *
 * 攻撃シーケンスを読み取ってBattleAtackDtoに変換
 */
public class BattleAtackDto {
    @Tag(1)
    public AtackKind kind;
    @Tag(2)
    public boolean friendAtack;
    @Tag(3)
    public int[] origin; // 攻撃元(0-11)
    @Tag(4)
    public int[] ot; // 雷撃の攻撃先
    @Tag(5)
    public int[] ydam; // 雷撃の与ダメージ
    @Tag(6)
    public int[] target; // 攻撃先(0-11)
    @Tag(7)
    public int[] damage; // ダメージ

    public BattleAtackDtoPb toProto() {
        BattleAtackDtoPb.Builder builder = BattleAtackDtoPb.newBuilder();
        builder.setKind(this.kind.toProto());
        builder.setFriendAtack(this.friendAtack);
        if (this.origin != null) {
            for (int b : this.origin) {
                builder.addOrigin(b);
            }
        }
        if (this.ot != null) {
            for (int b : this.ot) {
                builder.addOt(b);
            }
        }
        if (this.ydam != null) {
            for (int b : this.ydam) {
                builder.addYdam(b);
            }
        }
        if (this.target != null) {
            for (int b : this.target) {
                builder.addTarget(b);
            }
        }
        if (this.damage != null) {
            for (int b : this.damage) {
                builder.addDamage(b);
            }
        }
        return builder.build();
    }

    private static List<BattleAtackDto> makeHougeki(
            JsonArray at_list, JsonArray df_list, JsonArray damage_list) {
        ArrayList<BattleAtackDto> result = new ArrayList<BattleAtackDto>();
        ArrayList<Integer> flatten_df_list = new ArrayList<Integer>();
        ArrayList<Integer> flatten_damage_list = new ArrayList<Integer>();

        for (int i = 0; i < at_list.size(); ++i) {
            int at = at_list.getInt(i);
            if (at == -1)
                continue;
            JsonValue df = df_list.get(i);
            JsonValue damage = damage_list.get(i);
            switch (df.getValueType()) {
            case NUMBER:
                int dfi = ((JsonNumber) df).intValue();
                int dami = ((JsonNumber) damage).intValue();
                if (dfi != -1) {
                    flatten_df_list.add((dfi - 1) % 6);
                    flatten_damage_list.add(dami);
                }
                break;
            case ARRAY:
                int length = ((JsonArray) df).size();
                for (int d = 0; d < length; ++d) {
                    int dfd = ((JsonArray) df).getInt(d);
                    int damd = ((JsonArray) damage).getInt(d);
                    if (dfd != -1) {
                        flatten_df_list.add((dfd - 1) % 6);
                        flatten_damage_list.add(damd);
                    }
                }
                break;
            default: // あり得ない
                break;
            }
            int length = flatten_df_list.size();
            if (length > 0) {
                BattleAtackDto dto = new BattleAtackDto();
                dto.kind = AtackKind.HOUGEKI;
                dto.friendAtack = (at <= 6);
                dto.origin = new int[] { (at - 1) % 6 };
                dto.target = new int[length];
                dto.damage = new int[length];
                for (int c = 0; c < length; ++c) {
                    dto.target[c] = flatten_df_list.get(c);
                    dto.damage[c] = flatten_damage_list.get(c);
                }
                result.add(dto);
            }
            flatten_df_list.clear();
            flatten_damage_list.clear();
        }

        return result;
    }

    private static BattleAtackDto makeRaigeki(boolean friendAtack,
            JsonArray rai_list, JsonArray dam_list, JsonArray ydam_list) {
        int[] originMap = new int[6];
        int[] targetMap = new int[6];
        BattleAtackDto dto = new BattleAtackDto();
        dto.kind = AtackKind.RAIGEKI;
        dto.friendAtack = friendAtack;

        int idx = 0;
        for (int i = 0; i < 6; ++i) {
            int rai = rai_list.getInt(i + 1);
            if (rai > 0) {
                originMap[i] = idx++;
                targetMap[rai - 1] = 1;
            }
        }
        dto.origin = new int[idx];
        dto.ydam = new int[idx];
        dto.ot = new int[idx];

        idx = 0;
        for (int i = 0; i < 6; ++i) {
            int tmp = targetMap[i];
            targetMap[i] = idx;
            idx += tmp;
        }
        dto.target = new int[idx];
        dto.damage = new int[idx];

        for (int i = 0; i < 6; ++i) {
            int rai = rai_list.getInt(i + 1);
            int dam = dam_list.getInt(i + 1);
            int ydam = ydam_list.getInt(i + 1);
            if (rai > 0) {
                dto.origin[originMap[i]] = i;
                dto.ydam[originMap[i]] = ydam;
                dto.ot[originMap[i]] = targetMap[rai - 1];
            }
            if (dam > 0) {
                dto.target[targetMap[i]] = i;
                dto.damage[targetMap[i]] = dam;
            }
        }

        return dto;
    }

    private static BattleAtackDto makeAir(boolean friendAtack,
            JsonArray plane_from, JsonArray dam_list, JsonArray cdam_list) {
        BattleAtackDto dto = new BattleAtackDto();
        dto.kind = AtackKind.AIR;
        dto.friendAtack = friendAtack;

        dto.origin = new int[plane_from.size()];
        for (int i = 0; i < plane_from.size(); ++i) {
            dto.origin[i] = (plane_from.getInt(i) - 1) % 6;
        }
        int idx = 0;
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
        idx = 0;
        for (int i = 0; i < 6; ++i) {
            int dam = dam_list.getInt(i + 1);
            if (dam > 0) {
                dto.target[idx] = i;
                dto.damage[idx] = dam;
                idx++;
            }
        }
        if (cdam_list != null) {
            for (int i = 0; i < 6; ++i) {
                int dam = cdam_list.getInt(i + 1);
                if (dam > 0) {
                    dto.target[idx] = i + 6;
                    dto.damage[idx] = dam;
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

    public static List<BattleAtackDto> makeAir(JsonArray plane_from, JsonValue raigeki, JsonValue combined) {
        if ((raigeki == null) || (raigeki == JsonValue.NULL))
            return null;

        JsonObject raigeki_obj = (JsonObject) raigeki;
        JsonArray fdamCombined = null;
        if ((combined != null) && (combined != JsonValue.NULL)) {
            fdamCombined = ((JsonObject) combined).getJsonArray("api_fdam");
        }

        BattleAtackDto fatack = makeAir(
                true,
                plane_from.getJsonArray(0),
                raigeki_obj.getJsonArray("api_edam"),
                null);

        BattleAtackDto eatack = makeAir(
                false,
                plane_from.getJsonArray(1),
                raigeki_obj.getJsonArray("api_fdam"),
                fdamCombined);

        return Arrays.asList(new BattleAtackDto[] { fatack, eatack });
    }

    public static List<BattleAtackDto> makeRaigeki(JsonValue raigeki, boolean second) {
        if ((raigeki == null) || (raigeki == JsonValue.NULL))
            return null;

        JsonObject raigeki_obj = (JsonObject) raigeki;

        BattleAtackDto fatack = makeRaigeki(
                true,
                raigeki_obj.getJsonArray("api_frai"),
                raigeki_obj.getJsonArray("api_edam"),
                raigeki_obj.getJsonArray("api_fydam"));

        if (second) {
            fatack.makeOriginCombined();
        }

        BattleAtackDto eatack = makeRaigeki(
                false,
                raigeki_obj.getJsonArray("api_erai"),
                raigeki_obj.getJsonArray("api_fdam"),
                raigeki_obj.getJsonArray("api_eydam"));

        if (second) {
            eatack.makeTargetCombined();
        }

        return Arrays.asList(new BattleAtackDto[] { fatack, eatack });
    }

    /**
     * api_hougeki* を処理する
     * @param hougeki
     */
    public static List<BattleAtackDto> makeHougeki(JsonValue hougeki, boolean second) {
        if ((hougeki == null) || (hougeki == JsonValue.NULL))
            return null;

        JsonObject hougeki_obj = (JsonObject) hougeki;

        List<BattleAtackDto> seq = makeHougeki(
                hougeki_obj.getJsonArray("api_at_list"),
                hougeki_obj.getJsonArray("api_df_list"),
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
}
