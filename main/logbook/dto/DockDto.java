package logbook.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 艦隊のドックを表します
 *
 */
public final class DockDto extends AbstractDto {

    /** ドックID */
    private final String id;

    /** 艦隊名 */
    private final String name;

    /** 艦娘達 */
    private final List<ShipDto> ships = new ArrayList<ShipDto>();

    /** 更新フラグ */
    private boolean update;

    /**
     * コンストラクター
     */
    public DockDto(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * ドックIDを取得します。
     * @return ドックID
     */
    public String getId() {
        return this.id;
    }

    /**
     * 艦娘を艦隊に追加します
     * 
     * @param ship
     */
    public void addShip(ShipDto ship) {
        this.ships.add(ship);
    }

    /**
     * 艦隊から艦娘を削除します
     * 
     * @param ship
     */
    public void removeShip(ShipDto ship) {
        this.ships.remove(ship);
    }

    /**
     * 艦隊の艦娘を入れ替えます
     * 
     * @param oldShip
     * @param newShip
     */
    public void replaceShip(ShipDto oldShip, ShipDto newShip) {
        int index = this.ships.indexOf(oldShip);
        if (index != -1) {
            this.ships.set(index, newShip);
        }
    }

    /**
     * 艦隊名を取得します。
     * @return 艦隊名
     */
    public String getName() {
        return this.name;
    }

    /**
     * 艦娘達を取得します。
     * @return 艦娘達
     */
    public List<ShipDto> getShips() {
        return Collections.unmodifiableList(this.ships);
    }

    /**
     * 更新フラグを取得します。
     * @return 更新フラグ
     */
    public boolean isUpdate() {
        return this.update;
    }

    /**
     * 更新フラグを設定します。
     * @param update 更新フラグ
     */
    public void setUpdate(boolean update) {
        this.update = update;
    }

    /**
     * 大破艦がいるか？を取得します
     * @return 大破艦がいるか？
     */
    public boolean isBadlyDamaged() {
        for (ShipDto ship : this.ships) {
            if (ship.isBadlyDamage()) {
                return true;
            }
        }
        return false;
    }
}
