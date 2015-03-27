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
 * @author Nekopanda
 *
 */
public class ItemInfo {
    private final ItemInfoDto info;
    private final List<ItemDto> items = new ArrayList<>();
    private final Set<ShipDto> ships = new TreeSet<>();

    public ItemInfo(ItemInfoDto item) {
        this.info = item;
    }

    /**
     * @return info
     */
    public ItemInfoDto getInfo() {
        return this.info;
    }

    /**
     * @return items
     */
    public List<ItemDto> getItems() {
        return this.items;
    }

    /**
     * @return ships
     */
    public Set<ShipDto> getShips() {
        return this.ships;
    }
}
