/**
 * 
 */
package logbook.dto;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonArray;
import javax.json.JsonObject;

import logbook.data.context.GlobalContext;
import logbook.internal.Ship;

/**
 * @author noname
 *
 */
public final class BattleDto extends AbstractDto {

    /** 見方艦隊 */
    private final DockDto dock;

    /** 敵艦隊 */
    private final List<ShipInfoDto> enemy = new ArrayList<ShipInfoDto>();

    /** 見方HP */
    private final int[] nowFriendHp = new int[6];

    /** 敵HP */
    private final int[] nowEnemyHp = new int[6];

    /** 見方MaxHP */
    private final int[] maxFriendHp = new int[6];

    /** 敵MaxHP */
    private final int[] maxEnemyHp = new int[6];

    /**
     * コンストラクター
     */
    public BattleDto(JsonObject object) {

        String dockId = Long.toString(object.getJsonNumber("api_dock_id").longValue());
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
    }

    /**
     * @return dock
     */
    public DockDto getDock() {
        return this.dock;
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
