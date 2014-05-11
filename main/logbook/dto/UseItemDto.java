/**
 * 
 */
package logbook.dto;

import javax.json.JsonArray;

import logbook.internal.MasterData;

/**
 * 開発資材や家具箱など、装備アイテムではなくて普通のアイテム
 * @author Nekopanda
 *
 */
public class UseItemDto {
    private int useItemId;
    private int itemCount;

    public UseItemDto() {
    }

    public UseItemDto(int id, int count) {
        this.useItemId = id;
        this.itemCount = count;
    }

    public UseItemDto(JsonArray data) {
        this.setUseItemId(data.getInt(0));
        this.setItemCount(data.getInt(1));
    }

    /**
     * @return useItemId
     */
    public int getUseItemId() {
        return this.useItemId;
    }

    /**
     * @param useItemId セットする useItemId
     */
    public void setUseItemId(int useItemId) {
        this.useItemId = useItemId;
    }

    /**
     * @return itemCount
     */
    public int getItemCount() {
        return this.itemCount;
    }

    /**
     * @param itemCount セットする itemCount
     */
    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    /**
     * アイテム名を取得
     * @param masterData マスターデータを入れてください
     * @return
     */
    public String getItemName(MasterData masterData) {
        MasterData.UseItemInfoDto useItemDto = masterData.getUseItem(this.useItemId);
        if (useItemDto != null) {
            return useItemDto.getName();
        }
        return "";
    }
}
