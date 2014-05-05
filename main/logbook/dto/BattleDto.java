/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.dto;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonNumber;
import javax.json.JsonObject;
import javax.json.JsonValue;

import logbook.data.context.GlobalContext;
import logbook.internal.Ship;

/**
 * 会敵を表します
 *
 */
public final class BattleDto extends AbstractDto {

    /** 味方艦隊 */
    private final DockDto dock;

    /** 味方艦 */
    private final List<ShipDto> fships = new ArrayList<ShipDto>();

    /** 敵艦隊 */
    private final List<ShipInfoDto> enemy = new ArrayList<ShipInfoDto>();

    /** 味方HP */
    private final int[] nowFriendHp = new int[6];

    /** 敵HP */
    private final int[] nowEnemyHp = new int[6];

    /** 味方MaxHP */
    private final int[] maxFriendHp = new int[6];

    /** 敵MaxHP */
    private final int[] maxEnemyHp = new int[6];

    /**
     * コンストラクター
     */
    public BattleDto(JsonObject object) {

        String dockId;

        if (object.containsKey("api_dock_id")) {
            dockId = object.get("api_dock_id").toString();
        } else {
            dockId = object.get("api_deck_id").toString();
        }

        this.dock = GlobalContext.getDock(dockId);

        JsonArray shipKe = object.getJsonArray("api_ship_ke");
        for (int i = 1; i < shipKe.size(); i++) {
            long id = shipKe.getJsonNumber(i).longValue();
            ShipInfoDto dto = Ship.get(Long.toString(id));
            if (dto != null) {
                this.enemy.add(dto);
            }
        }

        JsonArray nowhps = object.getJsonArray("api_nowhps");
        for (int i = 1; i < nowhps.size(); i++) {
            if (i <= 6) {
                this.nowFriendHp[i - 1] = nowhps.getJsonNumber(i).intValue();
            } else {
                this.nowEnemyHp[i - 1 - 6] = nowhps.getJsonNumber(i).intValue();
            }
        }

        JsonArray maxhps = object.getJsonArray("api_maxhps");
        for (int i = 1; i < maxhps.size(); i++) {
            if (i <= 6) {
                this.maxFriendHp[i - 1] = maxhps.getJsonNumber(i).intValue();
            } else {
                this.maxEnemyHp[i - 1 - 6] = maxhps.getJsonNumber(i).intValue();
            }
        }

        // ダメージ計算 //

        // 航空戦
        JsonObject kouku = object.getJsonObject("api_kouku");
        if (kouku != null)
            this.doRaigeki(kouku.get("api_stage3"));

        // 支援艦隊
        JsonNumber support_flag = object.getJsonNumber("api_support_flag");
        if ((support_flag != null) && (support_flag.intValue() != 0)) {
            JsonObject support = object.getJsonObject("api_support_info");
            if (support != null) {
                JsonObject support_hourai = support.getJsonObject("api_support_hourai");
                if (support_hourai != null) {
                    JsonArray edam = support_hourai.getJsonArray("api_damage");
                    if (edam != null) {
                        for (int i = 1; i <= 6; i++) {
                            this.nowEnemyHp[i - 1] -= edam.getJsonNumber(i).intValue();
                        }
                    }
                }
            }
        }

        // 開幕
        this.doRaigeki(object.get("api_opening_atack"));

        // 砲撃
        this.doHougeki(object.get("api_hougeki"));
        this.doHougeki(object.get("api_hougeki1"));
        this.doHougeki(object.get("api_hougeki2"));
        this.doHougeki(object.get("api_hougeki3"));

        // 雷撃
        this.doRaigeki(object.get("api_raigeki"));

        // HP0以下を0にする
        for (int i = 0; i < 6; i++) {
            if (this.nowFriendHp[i] < 0)
                this.nowFriendHp[i] = 0;
            if (this.nowEnemyHp[i] < 0)
                this.nowEnemyHp[i] = 0;
        }

        List<ShipDto> dock_ships = this.dock.getShips();
        for (int i = 0; i < dock_ships.size(); i++) {
            ShipDto ship = dock_ships.get(i);
            this.fships.add(ship);
            ship.setNowhp(this.nowFriendHp[i]);
        }
    }

    private void doRaigeki(JsonValue raigeki) {
        if ((raigeki == null) || (raigeki == JsonValue.NULL))
            return;

        JsonObject raigeki_obj = (JsonObject) raigeki;
        JsonArray fdam = raigeki_obj.getJsonArray("api_fdam");
        JsonArray edam = raigeki_obj.getJsonArray("api_edam");
        for (int i = 1; i <= 6; i++) {
            this.nowFriendHp[i - 1] -= fdam.getJsonNumber(i).intValue();
            this.nowEnemyHp[i - 1] -= edam.getJsonNumber(i).intValue();
        }
    }

    private ArrayList<Integer> listupDamage(JsonArray damage_list) {
        ArrayList<Integer> list = new ArrayList<Integer>();

        for (JsonValue atack : damage_list) {
            switch (atack.getValueType()) {
            case NUMBER:
                list.add(((JsonNumber) atack).intValue());
                break;
            case ARRAY:
                for (JsonValue ship : (JsonArray) atack) {
                    list.add(((JsonNumber) ship).intValue());
                }
                break;
            }
        }

        return list;
    }

    /**
     * api_hougeki* を処理する
     * @param hougeki
     */
    private void doHougeki(JsonValue hougeki) {
        if ((hougeki == null) || (hougeki == JsonValue.NULL))
            return;

        JsonObject hougeki_obj = (JsonObject) hougeki;
        ArrayList<Integer> df_list = this.listupDamage(hougeki_obj.getJsonArray("api_df_list"));
        ArrayList<Integer> damage = this.listupDamage(hougeki_obj.getJsonArray("api_damage"));

        if (df_list.size() != damage.size()) {
            throw new IndexOutOfBoundsException("df_list と damage の長さが合いません");
        }

        for (int i = 0; i < df_list.size(); ++i) {
            int shipIdx = df_list.get(i);
            if (shipIdx == -1)
                continue;
            if (shipIdx <= 6) {
                this.nowFriendHp[shipIdx - 1] -= damage.get(i);
            }
            else {
                this.nowEnemyHp[shipIdx - 1 - 6] -= damage.get(i);
            }
        }
    }

    /**
     * @return dock
     */
    public DockDto getDock() {
        return this.dock;
    }

    public List<ShipDto> getFriendShips() {
        return this.fships;
    }

    /**
     * @return enemy
     */
    public List<ShipInfoDto> getEnemy() {
        return this.enemy;
    }

    /**
     * @return nowFriendHp
     */
    public int[] getNowFriendHp() {
        return this.nowFriendHp;
    }

    /**
     * @return nowEnemyHp
     */
    public int[] getNowEnemyHp() {
        return this.nowEnemyHp;
    }

    /**
     * @return maxFriendHp
     */
    public int[] getMaxFriendHp() {
        return this.maxFriendHp;
    }

    /**
     * @return maxEnemyHp
     */
    public int[] getMaxEnemyHp() {
        return this.maxEnemyHp;
    }
}
