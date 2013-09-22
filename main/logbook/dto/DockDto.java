/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
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

    /**
     * コンストラクター
     */
    public DockDto(String id, String name) {
        this.id = id;
        this.name = name;
    }

    /**
     * ドックIDを取得します
     * 
     * @return
     */
    public String getId() {
        return this.id;
    }

    /**
     * 艦隊名を取得します
     * 
     * @return
     */
    public String getName() {
        return this.name;
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
     * 艦娘達を取得します
     * 
     * @return
     */
    public List<ShipDto> getShips() {
        return Collections.unmodifiableList(this.ships);
    }
}
