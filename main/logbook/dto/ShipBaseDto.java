/**
 * 
 */
package logbook.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.json.JsonObject;

import logbook.config.MasterDataConfig;
import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.internal.MasterData;
import logbook.internal.Ship;
import logbook.proto.Tag;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Nekopanda
 * 味方艦・敵艦のベースクラス
 */
public abstract class ShipBaseDto extends AbstractDto {

    @Tag(40)
    private final ShipInfoDto shipInfo;

    /**
     * コンストラクター
     * 
     * @param object JSON Object
     */
    public ShipBaseDto(JsonObject object) {
        int shipId = object.getJsonNumber("api_ship_id").intValue();
        ShipInfoDto shipinfo = Ship.get(String.valueOf(shipId));
        this.shipInfo = shipinfo;
    }

    public ShipBaseDto(ShipInfoDto shipInfo) {
        this.shipInfo = shipInfo;
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

    public int[] getOnSlot() {
        return this.shipInfo.getMaxeq();
    }

    /**
     * @return 装備
     */
    public List<String> getSlot() {
        List<String> itemNames = new ArrayList<String>();
        Map<Integer, ItemDto> itemMap = GlobalContext.getItemMap();
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
     * @return 装備
     */
    public List<ItemDto> getItem() {
        List<ItemDto> items = new ArrayList<ItemDto>();
        Map<Integer, ItemDto> itemMap = GlobalContext.getItemMap();
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

    public abstract int[] getItemId();

    /**
     * @return 制空値
     */
    public int getSeiku() {
        List<ItemDto> items = this.getItem();
        int seiku = 0;
        for (int i = 0; i < 4; i++) {
            ItemDto item = items.get(i);
            if (item != null) {
                if ((item.getType3() == 6)
                        || (item.getType3() == 7)
                        || (item.getType3() == 8)
                        || ((item.getType3() == 10) && (item.getType2() == 11))) {
                    //6:艦上戦闘機,7:艦上爆撃機,8:艦上攻撃機,10:水上偵察機(ただし瑞雲のみ)の場合は制空値を計算する
                    seiku += (int) Math.floor(item.getTyku() * Math.sqrt(this.getOnSlot()[i]));
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
        List<ItemDto> items = this.getItem();
        int sakuteki = 0;
        for (int i = 0; i < 4; i++) {
            ItemDto item = items.get(i);
            if (item != null) {
                sakuteki += item.getSaku();
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
        List<MasterData.ShipTypeDto> info = MasterDataConfig.get().getStype();
        if (info.size() < stype) // データを取得していない
            return false;
        MasterData.ShipTypeDto shipType = info.get(stype - 1);
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
     * @return 火力
     */
    public int getKaryoku() {
        return this.shipInfo.getHoug();
    }

    /**
     * @return 火力(最大)
     */
    public int getKaryokuMax() {
        return this.shipInfo.getHougMax();
    }

    /**
     * @return 雷装
     */
    public int getRaisou() {
        return this.shipInfo.getRaig();
    }

    /**
     * @return 雷装(最大)
     */
    public int getRaisouMax() {
        return this.shipInfo.getRaigMax();
    }

    /**
     * @return 対空
     */
    public int getTaiku() {
        return this.shipInfo.getTyku();
    }

    /**
     * @return 対空(最大)
     */
    public int getTaikuMax() {
        return this.shipInfo.getTykuMax();
    }

    /**
     * @return 装甲
     */
    public int getSoukou() {
        return this.shipInfo.getSouk();
    }

    /**
     * @return 装甲(最大)
     */
    public int getSoukouMax() {
        return this.shipInfo.getSoukMax();
    }

    /**
     * @return 回避
     */
    public int getKaihi() {
        return this.shipInfo.getKaih();
    }

    /**
     * @return 回避(最大)
     */
    public int getKaihiMax() {
        return this.shipInfo.getKaihMax();
    }

    /**
     * @return 対潜
     */
    public int getTaisen() {
        return this.shipInfo.getTais();
    }

    /**
     * @return 対潜(最大)
     */
    public int getTaisenMax() {
        return this.shipInfo.getTaisMax();
    }

    /**
     * @return 索敵
     */
    public int getSakuteki() {
        return this.shipInfo.getSaku();
    }

    /**
     * @return 索敵(最大)
     */
    public int getSakutekiMax() {
        return this.shipInfo.getSakuMax();
    }

    /**
     * @return 運
     */
    public int getLucky() {
        return this.shipInfo.getLuck();
    }

    /**
     * @return 運(最大)
     */
    public int getLuckyMax() {
        return this.shipInfo.getLuckMax();
    }
}
