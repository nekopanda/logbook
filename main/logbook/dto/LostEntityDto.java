/**
 * 
 */
package logbook.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author iedeg_000
 *
 */
public class LostEntityDto {

    private Date time = new Date();
    private String lostEntity;
    private int entityId;
    private String name;
    private String eventCaused;

    private LostEntityDto(ShipDto lostShip) {
        this.setLostEntity("艦娘");
        this.setEntityId((int) lostShip.getId());
        this.setName(lostShip.getName() + " (Lv:" + lostShip.getLv() + ")");
        if (lostShip.getNowhp() != 0) {
            // 解体
            this.setEventCaused("艦娘の解体");
        }
        else {
            // 轟沈
            this.setEventCaused("艦娘の轟沈");
        }
    }

    private LostEntityDto(long id, String itemName, String causedBy) {
        this.setLostEntity("装備アイテム");
        this.setEntityId((int) id);
        this.setName(itemName);
        this.setEventCaused(causedBy);
    }

    /** ロストした艦からデータ作成 */
    public static List<LostEntityDto> make(ShipDto lostShip) {
        LostEntityDto lostShipDto = new LostEntityDto(lostShip);
        List<LostEntityDto> list = new ArrayList<LostEntityDto>();
        list.add(lostShipDto);
        List<ItemDto> itemList = lostShip.getItem();
        List<Long> itemIdList = lostShip.getItemId();
        for (int i = 0; i < itemList.size(); ++i) {
            ItemDto itemDto = itemList.get(i);
            Long itemId = itemIdList.get(i);
            if (itemDto != null) {
                list.add(new LostEntityDto(itemId, itemDto.getName(), lostShipDto.getEventCaused()));
            }
            else if (itemId != -1) {
                list.add(new LostEntityDto(itemId, lostShipDto.getName() + "が装備していた不明なアイテム" + (i + 1),
                        lostShipDto.getEventCaused()));
            }
        }
        return list;
    }

    /** アイテムの廃棄データ作成 */
    public static LostEntityDto make(long id, ItemDto lostItem) {
        return new LostEntityDto(id, lostItem.getName(), "装備の廃棄");
    }

    /**
     * @return date
     */
    public Date getTime() {
        return this.time;
    }

    /**
     * @param date セットする date
     */
    public void setTime(Date time) {
        this.time = time;
    }

    /**
     * @return lostEntity
     */
    public String getLostEntity() {
        return this.lostEntity;
    }

    /**
     * @param lostEntity セットする lostEntity
     */
    public void setLostEntity(String lostEntity) {
        this.lostEntity = lostEntity;
    }

    /**
     * @return entityId
     */
    public int getEntityId() {
        return this.entityId;
    }

    /**
     * @param entityId セットする entityId
     */
    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    /**
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @param name セットする name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return eventCaused
     */
    public String getEventCaused() {
        return this.eventCaused;
    }

    /**
     * @param eventCaused セットする eventCaused
     */
    public void setEventCaused(String eventCaused) {
        this.eventCaused = eventCaused;
    }

}
