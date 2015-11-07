package logbook.dto;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;

import logbook.data.context.GlobalContext;
import logbook.internal.Item;
import logbook.internal.Ship;

/**
 * 会敵を表します
 *
 */
public final class BattleDto extends AbstractDto {

    /** 味方艦隊 */
    private final List<DockDto> friends = new ArrayList<>();

    /** 敵艦隊 */
    private final List<ShipInfoDto> enemy = new ArrayList<>();

    /** 敵装備 */
    private final List<ItemInfoDto[]> enemySlot = new ArrayList<>();

    /** 味方HP */
    private final int[] nowFriendHp = new int[6];

    /** 敵HP */
    private final int[] nowEnemyHp = new int[6];

    /** 味方MaxHP */
    private final int[] maxFriendHp = new int[6];

    /** 敵MaxHP */
    private final int[] maxEnemyHp = new int[6];

    /** 味方陣形 */
    private final String friendFormation;

    /** 敵陣形 */
    private final String enemyFormation;

    /** 艦隊行動 */
    private final String intercept;

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
        this.friends.add(GlobalContext.getDock(dockId));

        if (object.containsKey("api_fParam_combined")) {
            this.friends.add(GlobalContext.getDock("2"));
        }

        JsonArray shipKe = object.getJsonArray("api_ship_ke");
        for (int i = 1; i < shipKe.size(); i++) {
            int id = shipKe.getInt(i);
            ShipInfoDto dto = Ship.get(id);
            if (dto != null) {
                this.enemy.add(dto);
            }
        }

        JsonArray eSlots = object.getJsonArray("api_eSlot");
        for (int i = 0; i < eSlots.size(); i++) {
            JsonArray eSlot = eSlots.getJsonArray(i);
            ItemInfoDto[] slot = new ItemInfoDto[5];
            for (int j = 0; j < eSlot.size(); j++) {
                slot[j] = Item.get(eSlot.getInt(j));
            }
            this.enemySlot.add(slot);
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

        if (object.containsKey("api_formation")) {
            JsonArray formation = object.getJsonArray("api_formation");
            // 味方陣形
            switch (formation.get(0).getValueType()) {
            case NUMBER:
                this.friendFormation = toFormation(formation.getInt(0));
                break;
            default:
                this.friendFormation = toFormation(Integer.parseInt(formation.getString(0)));
            }
            // 敵陣形
            switch (formation.get(1).getValueType()) {
            case NUMBER:
                this.enemyFormation = toFormation(formation.getInt(1));
                break;
            default:
                this.enemyFormation = toFormation(Integer.parseInt(formation.getString(1)));
            }
            // 艦隊行動
            switch (formation.get(2).getValueType()) {
            case NUMBER:
                this.intercept = toIntercept(formation.getInt(2));
                break;
            default:
                this.intercept = toIntercept(Integer.parseInt(formation.getString(2)));
            }
        } else {
            this.friendFormation = "陣形不明";
            this.enemyFormation = "陣形不明";
            this.intercept = "不明";
        }
    }

    private static String toFormation(int f) {
        String formation;
        switch (f) {
        case 1:
            formation = "単縦陣";
            break;
        case 2:
            formation = "複縦陣";
            break;
        case 3:
            formation = "輪形陣";
            break;
        case 4:
            formation = "梯形陣";
            break;
        case 5:
            formation = "単横陣";
            break;
        case 11:
            formation = "第一警戒航行序列";
            break;
        case 12:
            formation = "第二警戒航行序列";
            break;
        case 13:
            formation = "第三警戒航行序列";
            break;
        case 14:
            formation = "第四警戒航行序列";
            break;
        default:
            formation = "単縦陣";
            break;
        }
        return formation;
    }

    private static String toIntercept(int i) {
        String intercept;
        switch (i) {
        case 1:
            intercept = "同航戦";
            break;
        case 2:
            intercept = "反航戦";
            break;
        case 3:
            intercept = "Ｔ字戦(有利)";
            break;
        case 4:
            intercept = "Ｔ字戦(不利)";
            break;
        default:
            intercept = "同航戦";
        }
        return intercept;
    }

    /**
     * 味方艦隊を取得します。
     * @return 味方艦隊
     */
    public List<DockDto> getFriends() {
        return this.friends;
    }

    /**
     * 敵艦隊を取得します。
     * @return 敵艦隊
     */
    public List<ShipInfoDto> getEnemy() {
        return this.enemy;
    }

    /**
     * 敵装備を取得します。
     * @return 敵装備
     */
    public List<ItemInfoDto[]> getEnemySlot() {
        return this.enemySlot;
    }

    /**
     * 味方HPを取得します。
     * @return 味方HP
     */
    public int[] getNowFriendHp() {
        return this.nowFriendHp;
    }

    /**
     * 敵HPを取得します。
     * @return 敵HP
     */
    public int[] getNowEnemyHp() {
        return this.nowEnemyHp;
    }

    /**
     * 味方MaxHPを取得します。
     * @return 味方MaxHP
     */
    public int[] getMaxFriendHp() {
        return this.maxFriendHp;
    }

    /**
     * 敵MaxHPを取得します。
     * @return 敵MaxHP
     */
    public int[] getMaxEnemyHp() {
        return this.maxEnemyHp;
    }

    /**
     * 味方陣形を取得します。
     * @return 味方陣形
     */
    public String getFriendFormation() {
        return this.friendFormation;
    }

    /**
     * 敵陣形を取得します。
     * @return 敵陣形
     */
    public String getEnemyFormation() {
        return this.enemyFormation;
    }

    /**
     * 艦隊行動を取得します。
     * @return 艦隊行動
     */
    public String getIntercept() {
        return this.intercept;
    }
}
