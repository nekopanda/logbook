/**
 * 
 */
package logbook.dto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;

import logbook.util.JsonUtils;

import com.dyuproject.protostuff.Tag;

/**
 * 攻撃シーケンス
 * @author Nekopanda
 */
public class BattleAtackDto {

    private static final int ENEMY_SECOND_BASE = 6;

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

    @Tag(11)
    public boolean combineEnabled;

    private static List<BattleAtackDto> makeHougeki(int baseidx,
            JsonArray at_efalg, JsonArray at_list,
            JsonArray at_type, JsonArray df_list, JsonArray cl_list, JsonArray damage_list) {
        ArrayList<BattleAtackDto> result = new ArrayList<BattleAtackDto>();
        ArrayList<Integer> flatten_df_list = new ArrayList<Integer>();
        ArrayList<Integer> flatten_damage_list = new ArrayList<Integer>();
        ArrayList<Integer> flatten_cl_list = new ArrayList<Integer>();
        // 7隻実装以降は常にある
        boolean hasEflag = (at_efalg != null);

        for (int i = baseidx; i < at_list.size(); ++i) {
            int at = at_list.getInt(i);
            JsonArray df = (JsonArray) df_list.get(i);
            JsonArray damage = (JsonArray) damage_list.get(i);
            JsonArray cl = (JsonArray) cl_list.get(i);
            for (int d = 0; d < damage.size(); ++d) {
                int dfd = df.getInt(d);
                int damd = damage.getInt(d);
                int cld = cl.getInt(d);
                if (dfd != -1) {
                    // hasEflagがない場合は最大6隻
                    flatten_df_list.add(hasEflag ? (dfd - baseidx) : ((dfd - baseidx) % 6));
                    flatten_damage_list.add(damd);
                    flatten_cl_list.add(cld);
                }
            }
            int length = flatten_df_list.size();
            if (length > 0) {
                BattleAtackDto dto = new BattleAtackDto();
                dto.kind = AtackKind.HOUGEKI;
                // hasEflagがない場合は最大6隻
                dto.friendAtack = hasEflag ? (at_efalg.getInt(i) == 0) : (at <= 6);
                if (at_type != null) {
                    dto.type = at_type.getInt(i);
                }
                // hasEflagがない場合は最大6隻
                dto.origin = new int[] { hasEflag ? (at - baseidx) : ((at - baseidx) % 6) };
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

    private static BattleAtackDto makeRaigeki(int baseidx, boolean friendAtack,
            JsonArray rai_list, JsonArray dam_list, JsonArray cl_list, JsonArray ydam_list) {
        // originとtargetの個数が違うようになった
        int oelems = rai_list.size() - baseidx;
        int telems = dam_list.size() - baseidx;
        int[] originMap = new int[oelems];
        int[] targetMap = new int[telems];
        boolean[] targetEnabled = new boolean[telems];
        BattleAtackDto dto = new BattleAtackDto();
        dto.kind = AtackKind.RAIGEKI;
        dto.friendAtack = friendAtack;

        int idx = 0;
        for (int i = 0; i < oelems; ++i) {
            int rai = rai_list.getInt(i + baseidx);
            if (rai >= baseidx) {
                originMap[i] = idx++;
                targetEnabled[rai - baseidx] = true;
            }
        }
        dto.origin = new int[idx];
        dto.ydam = new int[idx];
        dto.critical = new int[idx];
        dto.ot = new int[idx];

        idx = 0;
        for (int i = 0; i < telems; ++i) {
            if (targetEnabled[i]) {
                targetMap[i] = idx++;
            }
        }
        dto.target = new int[idx];
        dto.damage = new int[idx];

        for (int i = 0; i < oelems; ++i) {
            int rai = rai_list.getInt(i + baseidx);
            int cl = cl_list.getInt(i + baseidx);
            int ydam = ydam_list.getInt(i + baseidx);
            if (rai >= baseidx) {
                dto.origin[originMap[i]] = i;
                dto.ydam[originMap[i]] = ydam;
                dto.critical[originMap[i]] = cl;
                dto.ot[originMap[i]] = targetMap[rai - baseidx];
            }
        }
        for (int i = 0; i < telems; ++i) {
            int dam = dam_list.getInt(i + baseidx);
            if (targetEnabled[i]) {
                dto.target[targetMap[i]] = i;
                dto.damage[targetMap[i]] = dam;
            }
        }

        // 連合艦隊を考慮した配列構成になっているか
        // （6-5敵連合艦隊実装まで連合艦隊の雷撃は随伴艦隊だけが受けることになっていたのでelems==6だったが6-5敵連合艦隊では敵の全艦が攻撃を受ける対象となったのでelems==12になった）
        dto.combineEnabled = ((oelems > 6) || (telems > 6));

        return dto;
    }

    private static BattleAtackDto makeAir(int baseidx, int originSecondBase, int targetSecondBase, boolean friendAtack,
            JsonArray plane_from, JsonArray dam_list, JsonArray cdam_list, JsonArray cl_list, JsonArray ccl_list,
            boolean isBase) {

        int elems = dam_list.size() - baseidx;
        BattleAtackDto dto = new BattleAtackDto();
        dto.kind = isBase ? AtackKind.AIRBASE : AtackKind.AIR;
        dto.friendAtack = friendAtack;

        int idx = 0;
        if (plane_from != null) {
            for (int i = 0; i < plane_from.size(); ++i) {
                if (plane_from.getInt(i) != -1)
                    ++idx;
            }
        }
        dto.origin = new int[idx];
        idx = 0;
        if (plane_from != null) {
            for (int i = 0; i < plane_from.size(); ++i) {
                if (plane_from.getInt(i) != -1)
                    // api_plane_fromは1始まりのまま
                    dto.origin[idx++] = (plane_from.getInt(i) - 1) % originSecondBase;
            }
        }
        idx = 0;
        for (int i = 0; i < elems; ++i) {
            int dam = dam_list.getInt(i + baseidx);
            if (dam > 0) {
                idx++;
            }
        }
        if (cdam_list != null) {
            for (int i = 0; i < elems; ++i) {
                int dam = cdam_list.getInt(i + baseidx);
                if (dam > 0) {
                    idx++;
                }
            }
        }
        dto.target = new int[idx];
        dto.damage = new int[idx];
        dto.critical = new int[idx];
        idx = 0;
        for (int i = 0; i < elems; ++i) {
            int dam = dam_list.getInt(i + baseidx);
            int cl = cl_list.getInt(i + baseidx);
            if (dam > 0) {
                dto.target[idx] = i;
                dto.damage[idx] = dam;
                // クリティカルフラグを砲撃と合わせる
                dto.critical[idx] = cl + baseidx;
                idx++;
            }
        }
        if (cdam_list != null) {
            for (int i = 0; i < elems; ++i) {
                int dam = cdam_list.getInt(i + baseidx);
                int cl = ccl_list.getInt(i + baseidx);
                if (dam > 0) {
                    dto.target[idx] = i + targetSecondBase;
                    dto.damage[idx] = dam;
                    // クリティカルフラグを砲撃と合わせる
                    dto.critical[idx] = cl + 1;
                    idx++;
                }
            }
        }

        return dto;
    }

    private void makeOriginCombined(int secondBase) {
        for (int i = 0; i < this.origin.length; ++i) {
            this.origin[i] += secondBase;
        }
    }

    private void makeTargetCombined(int secondBase) {
        for (int i = 0; i < this.target.length; ++i) {
            this.target[i] += secondBase;
        }
    }

    /**
     * 航空戦を読み込む
     * @param plane_from
     * @param raigeki
     * @param combined
     * @return
     */
    public static List<BattleAtackDto> makeAir(int baseidx, int friendSecondBase,
            JsonArray plane_from, JsonObject raigeki,
            JsonObject combined,
            boolean isBase) {
        if ((raigeki == null) || (plane_from == null))
            return null;

        JsonArray fdamCombined = null;
        JsonArray fclCombined = null;
        JsonArray edamCombined = null;
        JsonArray eclCombined = null;
        if (combined != null) {
            if (combined.containsKey("api_fdam")) {
                fdamCombined = JsonUtils.getJsonArray(combined, "api_fdam");
                fclCombined = JsonUtils.getJsonArray(combined, "api_fcl_flag");
            }
            if (combined.containsKey("api_edam")) {
                edamCombined = JsonUtils.getJsonArray(combined, "api_edam");
                eclCombined = JsonUtils.getJsonArray(combined, "api_ecl_flag");
            }
        }

        BattleAtackDto fatack = makeAir(
                baseidx, friendSecondBase, ENEMY_SECOND_BASE,
                true,
                JsonUtils.getJsonArray(plane_from, 0),
                JsonUtils.getJsonArray(raigeki, "api_edam"),
                edamCombined,
                JsonUtils.getJsonArray(raigeki, "api_ecl_flag"),
                eclCombined,
                isBase);

        if (isBase) {
            return Arrays.asList(new BattleAtackDto[] { fatack });
        }

        BattleAtackDto eatack = makeAir(
                baseidx, ENEMY_SECOND_BASE, friendSecondBase,
                false,
                JsonUtils.getJsonArray(plane_from, 1),
                JsonUtils.getJsonArray(raigeki, "api_fdam"),
                fdamCombined,
                JsonUtils.getJsonArray(raigeki, "api_fcl_flag"),
                fclCombined,
                false);

        return Arrays.asList(new BattleAtackDto[] { fatack, eatack });
    }

    /**
     * 雷撃戦を読み込む
     * @param raigeki
     * @param isFriendSecond 味方が連合艦隊か
     * @return
     */
    public static List<BattleAtackDto> makeRaigeki(
            int baseidx, int friendSecondBase,
            JsonObject raigeki, boolean isFriendSecond) {
        if (raigeki == null)
            return null;

        List<BattleAtackDto> attaks = new ArrayList<BattleAtackDto>();

        BattleAtackDto fatack = null;
        BattleAtackDto eatack = null;

        if (JsonUtils.hasKey(raigeki, "api_frai")) {

            fatack = makeRaigeki(
                    baseidx,
                    true,
                    JsonUtils.getJsonArray(raigeki, "api_frai"),
                    JsonUtils.getJsonArray(raigeki, "api_edam"),
                    JsonUtils.getJsonArray(raigeki, "api_fcl"),
                    JsonUtils.getJsonArray(raigeki, "api_fydam"));

            if ((baseidx == 1) && (fatack.combineEnabled == false)) {
                // 旧APIとの互換性: 味方の随伴艦のみが雷撃を行う場合(6-5実装以前の連合艦隊はこれ。6-5実装以降の連合艦隊は不明)
                if (isFriendSecond) {
                    fatack.makeOriginCombined(friendSecondBase);
                }
            }
            attaks.add(fatack);
        }

        if (JsonUtils.hasKey(raigeki, "api_erai")) {
            eatack = makeRaigeki(
                    baseidx,
                    false,
                    JsonUtils.getJsonArray(raigeki, "api_erai"),
                    JsonUtils.getJsonArray(raigeki, "api_fdam"),
                    JsonUtils.getJsonArray(raigeki, "api_ecl"),
                    JsonUtils.getJsonArray(raigeki, "api_eydam"));

            if ((baseidx == 1) && (fatack != null) && (fatack.combineEnabled == false)) {
                // 旧APIとの互換性: 味方の随伴艦のみが雷撃を受ける場合(6-5実装以前の連合艦隊はこれ。6-5実装以降の連合艦隊は不明)
                if (isFriendSecond) {
                    eatack.makeTargetCombined(friendSecondBase);
                }
            }
            attaks.add(eatack);
        }

        return attaks;
    }

    /**
     * api_hougeki* を読み込む
     * @param baseidx 基点(0 or 1)
     * @param friendSecondBase 最初の艦隊の最大艦数(6 or 7)
     * @param hougeki
     */
    public static List<BattleAtackDto> makeHougeki(
            int baseidx, int friendSecondBase,
            JsonObject hougeki,
            boolean isFriendSecond, boolean isEnemySecond) {
        if (hougeki == null)
            return null;

        if (JsonUtils.hasKey(hougeki, "api_damage") == false)
            return null;

        List<BattleAtackDto> seq = makeHougeki(
                baseidx,
                JsonUtils.getJsonArray(hougeki, "api_at_eflag"),
                JsonUtils.getJsonArray(hougeki, "api_at_list"),
                JsonUtils.getJsonArray(hougeki, "api_at_type"),
                JsonUtils.getJsonArray(hougeki, "api_df_list"),
                JsonUtils.getJsonArray(hougeki, "api_cl_list"),
                JsonUtils.getJsonArray(hougeki, "api_damage"));

        // 旧APIとの互換性: 味方連合艦隊を反映
        if ((baseidx == 1) && isFriendSecond) {
            for (BattleAtackDto dto : seq) {
                if (dto.friendAtack) {
                    dto.makeOriginCombined(friendSecondBase);
                }
                else {
                    dto.makeTargetCombined(friendSecondBase);
                }
            }
        }
        // 旧APIとの互換性: 敵連合艦隊を反映（現状夜戦のみに適用）
        if ((baseidx == 1) && isEnemySecond) {
            for (BattleAtackDto dto : seq) {
                if (!dto.friendAtack) {
                    dto.makeOriginCombined(ENEMY_SECOND_BASE);
                }
                else {
                    dto.makeTargetCombined(ENEMY_SECOND_BASE);
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
    public static List<BattleAtackDto> makeSupport(int baseidx, JsonArray dam_list) {
        BattleAtackDto dto = new BattleAtackDto();
        dto.kind = AtackKind.SUPPORT;
        dto.friendAtack = true;

        int idx = 0;
        // dam_listの要素数は敵が連合艦隊の場合、艦数+1=13
        int elems = dam_list.size() - baseidx;
        for (int i = 0; i < elems; ++i) {
            int dam = dam_list.getInt(i + baseidx);
            if (dam > 0) {
                idx++;
            }
        }
        dto.target = new int[idx];
        dto.damage = new int[idx];
        idx = 0;
        for (int i = 0; i < elems; ++i) {
            int dam = dam_list.getInt(i + baseidx);
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
        case 7:
            return "カットイン(空母)";
        case 100:
            return "ネルソンタッチ";
        case 101:
            return "一斉射かッ…胸が熱いな！";
        case 102:
            return "長門、いい？ いくわよ！ 主砲一斉射ッ！";
        case 103:
            return "コロラド特殊攻撃";
        case 200:
            return "瑞雲立体攻撃";
        case 201:
            return "海空立体攻撃";
        }
        return "不明(" + this.type + ")";
    }
}
