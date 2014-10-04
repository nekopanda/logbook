/**
 * 
 */
package logbook.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.JsonObject;

import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.internal.MasterData;
import logbook.internal.Ship;
import logbook.util.JsonUtils;

import org.apache.commons.lang3.StringUtils;

import com.dyuproject.protostuff.Tag;

/**
 * @author Nekopanda
 * 味方艦・敵艦のベースクラス
 */
public abstract class ShipBaseDto extends AbstractDto {

    @Tag(1)
    protected final ShipInfoDto shipInfo;

    /** 装備 */
    @Tag(2)
    protected final int[] slot;

    @Tag(3)
    protected final List<ItemDto> slotItem;

    /** 装備込のパラメータ */
    @Tag(4)
    protected final ShipParameters param;

    /** 装備なしのMAX(艦娘のみ) */
    @Tag(5)
    protected final ShipParameters max;

    /** 装備による上昇分 */
    @Tag(6)
    protected final ShipParameters slotParam;

    /**
     * 艦娘用コンストラクター
     * @param object JSON Object
     */
    public ShipBaseDto(JsonObject object) {
        int shipId = object.getJsonNumber("api_ship_id").intValue();
        ShipInfoDto shipinfo = Ship.get(String.valueOf(shipId));
        this.shipInfo = shipinfo;
        this.slot = JsonUtils.getIntArray(object, "api_slot");
        this.slotItem = this.createItemList();
        ShipParameters[] params = ShipParameters.fromShip(object, this.getItem(), shipinfo);
        this.param = params[0];
        this.max = params[1];
        this.slotParam = params[2];
    }

    /**
     * 敵艦用コンストラクター
     * @param object JSON Object
     */
    public ShipBaseDto(int shipId, int[] slot) {
        this.shipInfo = Ship.get(String.valueOf(shipId));
        this.slot = slot;
        this.slotItem = this.createItemList();
        ShipParameters[] params = ShipParameters.fromBaseAndSlotItem(
                this.shipInfo.getParam(), this.getItem());
        this.param = params[0];
        this.max = null;
        this.slotParam = params[1];
    }

    /**
     * @return shipInfo
     */
    public ShipInfoDto getShipInfo() {
        return this.shipInfo;
    }

    /**
     * @return 艦娘を識別するID
     */
    public int getShipId() {
        return this.shipInfo.getShipId();
    }

    /**
     * @return 名前
     */
    public String getName() {
        return this.shipInfo.getName();
    }

    /**
     * @return 表示名
     */
    public String getFriendlyName() {
        String name = this.shipInfo.getName();
        if (this.shipInfo.getMaxBull() > 0) { // 艦娘
            name += "(Lv." + this.getLv() + ")";
        }
        else { // 深海棲艦
            if (!StringUtils.isEmpty(this.shipInfo.getFlagship())) {
                name += " " + this.shipInfo.getFlagship();
            }
        }
        return name;
    }

    public int getLv() {
        return 1;
    }

    /**
     * @return 艦種
     */
    public String getType() {
        return this.shipInfo.getType();
    }

    /**
     * @return 艦種
     */
    public int getStype() {
        return this.shipInfo.getStype();
    }

    /**
     * @return 弾Max
     */
    public int getBullMax() {
        return this.shipInfo.getMaxBull();
    }

    /**
     * @return 燃料Max
     */
    public int getFuelMax() {
        return this.shipInfo.getMaxFuel();
    }

    /**
     * @return 現在の艦載機搭載数
     */
    public int[] getOnSlot() {
        return this.shipInfo.getMaxeq();
    }

    /**
     * @return 艦載機最大搭載数
     */
    public int[] getMaxeq() {
        return this.shipInfo.getMaxeq();
    }

    protected Map<Integer, ItemDto> getItemMap() {
        return GlobalContext.getItemMap();
    }

    /**
     * @return 装備
     */
    public List<String> getSlot() {
        List<String> itemNames = new ArrayList<String>();
        Map<Integer, ItemDto> itemMap = this.getItemMap();
        for (int itemid : this.getItemId()) {
            if (-1 != itemid) {
                ItemDto name = itemMap.get(itemid);
                if (name != null) {
                    itemNames.add(name.getName());
                } else {
                    itemNames.add("<UNKNOWN>");
                }
            } else {
                itemNames.add("");
            }
        }
        return itemNames;
    }

    /**
     * @return 装備ID
     */
    public int[] getItemId() {
        return this.slot;
    }

    private List<ItemDto> createItemList() {
        List<ItemDto> items = new ArrayList<ItemDto>();
        Map<Integer, ItemDto> itemMap = this.getItemMap();
        for (int itemid : this.getItemId()) {
            if (-1 != itemid) {
                ItemDto item = itemMap.get(itemid);
                if (item != null) {
                    items.add(item);
                } else {
                    items.add(null);
                }
            } else {
                items.add(null);
            }
        }
        return items;
    }

    /**
     * @return 装備
     */
    public List<ItemDto> getItem() {
        if (this.slotItem == null) {
            return this.createItemList();
        }
        return this.slotItem;
    }

    /**
     * @return 制空値
     */
    public int getSeiku() {
        List<ItemDto> items = this.getItem();
        int seiku = 0;
        for (int i = 0; i < items.size(); i++) {
            ItemDto item = items.get(i);
            if (item != null) {
                if ((item.getType2() == 6)
                        || (item.getType2() == 7)
                        || (item.getType2() == 8)
                        || (item.getType2() == 11)) {
                    //6:艦上戦闘機,7:艦上爆撃機,8:艦上攻撃機,11:瑞雲系の水上偵察機の場合は制空値を計算する
                    seiku += (int) Math.floor(item.getParam().getTyku() * Math.sqrt(this.getOnSlot()[i]));
                }
            }
        }
        return seiku;
    }

    /**
     * アイテムの索敵合計を計算します
     * @return アイテムの索敵合計
     */
    public int getSlotSakuteki() {
        int sakuteki = 0;
        for (ItemDto item : this.getItem()) {
            if (item != null) {
                sakuteki += item.getParam().getSaku();
            }
        }
        return sakuteki;
    }

    /**
     * @return 飛行機を装備できるか？
     */
    public boolean canEquipPlane() {
        if (this.shipInfo == null) // データを取得していない
            return false;
        int stype = this.shipInfo.getStype();
        MasterData.ShipTypeDto shipType = MasterData.getShipType(stype);
        if (shipType == null) // データを取得していない
            return false;
        List<Boolean> equipType = shipType.getEquipType();
        for (int i = 0; i < AppConstants.PLANE_ITEM_TYPES.length; ++i) {
            if (equipType.get(AppConstants.PLANE_ITEM_TYPES[i] - 1)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 名前:装備1,装備2,...
     * @return
     */
    public String getDetailedString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getFriendlyName()).append(": ");
        int idx = 0;
        for (ItemDto item : this.getItem()) {
            if (item != null) {
                if (idx++ > 0) {
                    sb.append(", ");
                }
                sb.append(item.getName());
            }
        }
        return sb.toString();
    }

    /**
     * @return 火力
     */
    public int getKaryoku() {
        return this.getParam().getHoug();
    }

    /**
     * @return 火力(最大)(艦娘のみ)
     */
    public int getKaryokuMax() {
        return this.getMax().getHoug();
    }

    /**
     * @return 雷装
     */
    public int getRaisou() {
        return this.getParam().getRaig();
    }

    /**
     * @return 雷装(最大)(艦娘のみ)
     */
    public int getRaisouMax() {
        return this.getMax().getRaig();
    }

    /**
     * @return 対空
     */
    public int getTaiku() {
        return this.getParam().getTyku();
    }

    /**
     * @return 対空(最大)(艦娘のみ)
     */
    public int getTaikuMax() {
        return this.getMax().getTyku();
    }

    /**
     * @return 装甲
     */
    public int getSoukou() {
        return this.getParam().getSouk();
    }

    /**
     * @return 装甲(最大)(艦娘のみ)
     */
    public int getSoukouMax() {
        return this.getMax().getSouk();
    }

    /**
     * @return 回避
     */
    public int getKaihi() {
        return this.getParam().getKaih();
    }

    /**
     * @return 回避(最大)(艦娘のみ)
     */
    public int getKaihiMax() {
        return this.getMax().getKaih();
    }

    /**
     * @return 対潜
     */
    public int getTaisen() {
        return this.getParam().getTais();
    }

    /**
     * @return 対潜(最大)(艦娘のみ)
     */
    public int getTaisenMax() {
        return this.getMax().getTais();
    }

    /**
     * @return 索敵
     */
    public int getSakuteki() {
        return this.getParam().getSaku();
    }

    /**
     * @return 索敵(最大)(艦娘のみ)
     */
    public int getSakutekiMax() {
        return this.getMax().getSaku();
    }

    /**
     * @return 運
     */
    public int getLucky() {
        return this.getParam().getLuck();
    }

    /**
     * @return 運(最大)(艦娘のみ)
     */
    public int getLuckyMax() {
        return this.getMax().getLuck();
    }

    /**
     * @return 装備込のパラメータ
     */
    public ShipParameters getParam() {
        return this.param;
    }

    /**
     * @return 装備による上昇分
     */
    public ShipParameters getSlotParam() {
        return this.slotParam;
    }

    /**
     * @return この艦の最大パラメータ（装備なしで）
     */
    public ShipParameters getMax() {
        return this.max;
    }
}
