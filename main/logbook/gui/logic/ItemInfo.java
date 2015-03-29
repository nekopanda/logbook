/**
 * 
 */
package logbook.gui.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import logbook.dto.ItemDto;
import logbook.dto.ItemInfoDto;
import logbook.dto.ShipDto;

/**
 * 装備アイテムに関する情報
 * @author Nekopanda
 */
public class ItemInfo {
    private final ItemInfoDto info;
    private final List<ItemDto> items = new ArrayList<>();
    private final Set<ShipDto> ships = new TreeSet<>();

    public ItemInfo(ItemInfoDto item) {
        this.info = item;
    }

    /**
     * 装備のマスターデータ
     * @return info
     */
    public ItemInfoDto getInfo() {
        return this.info;
    }

    /**
     * 持っている装備
     * @return items
     */
    public List<ItemDto> getItems() {
        return this.items;
    }

    /**
     * この装備を1つ以上装備してる艦娘
     * @return ships
     */
    public Set<ShipDto> getShips() {
        return this.ships;
    }
}
