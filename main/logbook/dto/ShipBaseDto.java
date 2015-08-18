/**
 *
 */
package logbook.dto;

import java.util.ArrayList;
import java.util.List;

import javax.json.JsonObject;

import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.internal.Item;
import logbook.internal.MasterData;
import logbook.internal.Ship;
import logbook.util.JsonUtils;

import com.dyuproject.protostuff.Tag;

/**
 * @author Nekopanda
 * 味方艦・敵艦のベースクラス
 */
public abstract class ShipBaseDto extends AbstractDto {

    @Tag(1)
    protected final ShipInfoDto shipInfo;

    /** 装備
     * 艦娘の場合は 装備個別ID
     * 敵艦の場合は 装備ID (slotitem_id)
     */
    @Tag(2)
    protected final int[] slot;

    @Tag(3)
    protected final List<ItemInfoDto> slotItem;

    /** 装備込のパラメータ */
    @Tag(4)
    protected final ShipParameters param;

    /** 装備なしのMAX(艦娘のみ) */
    @Tag(5)
    protected final ShipParameters max;

    /** 装備による上昇分 */
    @Tag(6)
    protected final ShipParameters slotParam;

    /** 装備のロックや改修値情報 */
    @Tag(7)
    private final List<ItemDto> slotItem2;

    /**
     * 艦娘用コンストラクター
     * @param object JSON Object
     */
    public ShipBaseDto(JsonObject object) {
        int shipId = object.getJsonNumber("api_ship_id").intValue();
        ShipInfoDto shipinfo = Ship.get(String.valueOf(shipId));
        this.shipInfo = shipinfo;
        this.slot = JsonUtils.getIntArray(object, "api_slot");
        this.slotItem2 = createItemDtoList(this.slot);
        this.slotItem = new ArrayList<ItemInfoDto>();
        for (ItemDto dto : this.slotItem2) {
            if (dto != null) {
                this.slotItem.add(dto.getInfo());
            }
        }
        ShipParameters[] params = ShipParameters.fromShip(object, this.getItem(), shipinfo);
        this.param = params[0];
        this.max = params[1];
        this.slotParam = params[2];
    }

    /**
     * 敵艦用コンストラクター
     * @param shipId ship_id
     * @param slot 装備
     */
    public ShipBaseDto(ShipInfoDto shipinfo, int[] slot, int[] param, boolean friend) {
        this.shipInfo = shipinfo;
        this.slot = slot;
        if (friend) {
            this.slotItem2 = createItemDtoList(this.slot);
            this.slotItem = new ArrayList<ItemInfoDto>();
            for (ItemDto dto : this.slotItem2) {
                this.slotItem.add((dto == null) ? null : dto.getInfo());
            }
        }
        else {
            this.slotItem = Item.fromIdList(slot);
            this.slotItem2 = createItemList(this.slotItem);
        }
        ShipParameters[] params = ShipParameters.fromBaseAndSlotItem(
                this.shipInfo.getParam(), param, this.getItem());
        this.param = params[0];
        this.max = this.shipInfo.getMax();
        this.slotParam = params[1];
    }

    /**
     * slot から List<ItemDto> を作成
     * 艦娘用
     * @param item
     * @return
     */
    private static List<ItemDto> createItemDtoList(int[] slot) {
        List<ItemDto> items = new ArrayList<>();
        for (int itemid : slot) {
            items.add(GlobalContext.getItem(itemid));
        }
        return items;
    }

    /**
     * List<ItemInfoDto> から List<ItemDto> を作成
     * 敵艦および旧データとの互換性用
     * @param item
     * @return
     */
    private static List<ItemDto> createItemList(List<ItemInfoDto> iteminfo) {
        List<ItemDto> items = new ArrayList<>();
        for (ItemInfoDto info : iteminfo) {
            if (info == null) {
                items.add(null);
            }
            else {
                items.add(new ItemDto(info, -1));
            }
        }
        return items;
    }

    public boolean isFriend() {
        return !this.shipInfo.isEnemy();
    }

    /**
     * 艦のマスター情報を取得
     * @return shipInfo
     */
    public ShipInfoDto getShipInfo() {
        return this.shipInfo;
    }

    /**
     * 艦娘を識別するID
     * @return 艦娘を識別するID
     */
    public int getShipId() {
        return this.shipInfo.getShipId();
    }

    /**
     * 名前
     * @return 名前
     */
    public String getName() {
        return this.shipInfo.getName();
    }

    /**
     * 航海日誌における表示名
     * @return 表示名
     */
    public String getFriendlyName() {
        if (this.isFriend()) {
            return this.shipInfo.getName() + "(Lv." + this.getLv() + ")";
        }
        // 深海棲艦
        return this.shipInfo.getFullName();
    }

    /**
     * レベル
     * @return
     */
    public int getLv() {
        return 1;
    }

    /**
     * 艦種
     * @return 艦種
     */
    public String getType() {
        return this.shipInfo.getType();
    }

    /**
     * 艦種ID
     * @return 艦種
     */
    public int getStype() {
        return this.shipInfo.getStype();
    }

    /**
     * 弾Max
     * @return 弾Max
     */
    public int getBullMax() {
        return this.shipInfo.getMaxBull();
    }

    /**
     * 燃料Max
     * @return 燃料Max
     */
    public int getFuelMax() {
        return this.shipInfo.getMaxFuel();
    }

    /**
     * 現在の艦載機搭載数
     * @return 現在の艦載機搭載数
     */
    public int[] getOnSlot() {
        return this.shipInfo.getMaxeq2();
    }

    /**
     * 艦載機最大搭載数
     * @return 艦載機最大搭載数
     */
    public int[] getMaxeq() {
        return this.shipInfo.getMaxeq2();
    }

    /**
     * 装備（名前）
     * @return 装備
     */
    public List<String> getSlot() {
        List<String> itemNames = new ArrayList<String>();
        for (ItemInfoDto dto : this.slotItem) {
            itemNames.add(dto.getName());
        }
        return itemNames;
    }

    /**
     * 装備ID
     * 艦娘の場合は 装備個別ID
     * 敵艦の場合は 装備ID (slotitem_id)
     * @return 装備ID
     */
    public int[] getItemId() {
        return this.slot;
    }

    /**
     * 装備(マスターデータ)
     * @return 装備
     */
    public List<ItemInfoDto> getItem() {
        if (this.slotItem == null) {
            // 古いバージョンはslotItemを作るのを忘れていたのでnullの場合がある
            // 同じ番号の装備はもうない可能性があるが失われた情報なので仕方ない
            return Item.fromIdList(this.slot);
        }
        return this.slotItem;
    }

    /**
     * 装備(個別)
     * @return slotItem2
     */
    public List<ItemDto> getItem2() {
        if (this.slotItem2 == null) {
            // 古いバージョンはslotItem2がないのでnullの場合がある
            // その場合はItemInfoDtoから作成
            return createItemList(this.getItem());
        }
        return this.slotItem2;
    }

    /**
     * 制空値
     * @return 制空値
     */
    public Integer getSeiku() {
        List<ItemInfoDto> items = this.getItem();
        int seiku = 0;
        for (int i = 0; i < items.size(); i++) {
            ItemInfoDto item = items.get(i);
            if (item != null) {
                //6:艦上戦闘機,7:艦上爆撃機,8:艦上攻撃機,11:瑞雲系の水上偵察機の場合は制空値を計算する
                if ((item.getType2() == 6)
                        || (item.getType2() == 7)
                        || (item.getType2() == 8)
                        || (item.getType2() == 11)) {
                    // スロット数が分からないと計算できない
                    if (this.getOnSlot() == null)
                        return null;

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
        for (ItemInfoDto item : this.getItem()) {
            if (item != null) {
                sakuteki += item.getParam().getSaku();
            }
        }
        return sakuteki;
    }

    /**
     * /ドラム缶の合計を計算します
     * @return ドラム缶の合計値
     */
    public int getDram() {
        // ドラム缶合計
        int dram = 0;
        List<ItemInfoDto> items = this.getItem();
        for (int i = 0; i < items.size(); i++) {
            ItemInfoDto item = items.get(i);
            if (item != null) {
                if (item.getName().equals("ドラム缶(輸送用)")) {
                    dram++;
                }
            }
        }
        return dram;
    }

    /**
     * /大発の合計を計算します
     * @return 大発の合計値
     */
    public int getDaihatsu() {
        // 大発合計
        int daihatsu = 0;
        List<ItemInfoDto> items = this.getItem();
        for (int i = 0; i < items.size(); i++) {
            ItemInfoDto item = items.get(i);
            if (item != null) {
                if (item.getName().equals("大発動艇")) {
                    daihatsu++;
                }
            }
        }
        return daihatsu;
    }

    /**
     * 飛行機を装備できるか？
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

    public static String makeDetailedString(String shipname, List<ItemInfoDto> slot) {
        StringBuilder sb = new StringBuilder();
        sb.append(shipname).append(": ");
        int idx = 0;
        for (ItemInfoDto item : slot) {
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
     * 名前:装備1,装備2,...
     * @return
     */
    public String getDetailedString() {
        return makeDetailedString(this.getFriendlyName(), this.getItem());
    }

    /**
     * 火力
     * @return 火力
     */
    public int getKaryoku() {
        return this.getParam().getHoug();
    }

    /**
     * 火力(最大)(艦娘のみ)
     * @return 火力(最大)(艦娘のみ)
     */
    public int getKaryokuMax() {
        return this.getMax().getHoug();
    }

    /**
     * 雷装
     * @return 雷装
     */
    public int getRaisou() {
        return this.getParam().getRaig();
    }

    /**
     * 雷装(最大)(艦娘のみ)
     * @return 雷装(最大)(艦娘のみ)
     */
    public int getRaisouMax() {
        return this.getMax().getRaig();
    }

    /**
     * 対空
     * @return 対空
     */
    public int getTaiku() {
        return this.getParam().getTyku();
    }

    /**
     * 対空(最大)(艦娘のみ)
     * @return 対空(最大)(艦娘のみ)
     */
    public int getTaikuMax() {
        return this.getMax().getTyku();
    }

    /**
     * 装甲
     * @return 装甲
     */
    public int getSoukou() {
        return this.getParam().getSouk();
    }

    /**
     * 装甲(最大)(艦娘のみ)
     * @return 装甲(最大)(艦娘のみ)
     */
    public int getSoukouMax() {
        return this.getMax().getSouk();
    }

    /**
     * 回避
     * @return 回避
     */
    public int getKaihi() {
        return this.getParam().getKaih();
    }

    /**
     * 回避(最大)(艦娘のみ)
     * @return 回避(最大)(艦娘のみ)
     */
    public int getKaihiMax() {
        return this.getMax().getKaih();
    }

    /**
     * 対潜
     * @return 対潜
     */
    public int getTaisen() {
        return this.getParam().getTais();
    }

    /**
     * 対潜(最大)(艦娘のみ)
     * @return 対潜(最大)(艦娘のみ)
     */
    public int getTaisenMax() {
        return this.getMax().getTais();
    }

    /**
     * 索敵
     * @return 索敵
     */
    public int getSakuteki() {
        return this.getParam().getSaku();
    }

    /**
     * 索敵(最大)(艦娘のみ)
     * @return 索敵(最大)(艦娘のみ)
     */
    public int getSakutekiMax() {
        return this.getMax().getSaku();
    }

    /**
     * 運
     * @return 運
     */
    public int getLucky() {
        return this.getParam().getLuck();
    }

    /**
     * 運(最大)(艦娘のみ)
     * @return 運(最大)(艦娘のみ)
     */
    public int getLuckyMax() {
        return this.getMax().getLuck();
    }

    /**
     * 装備込のパラメータ
     * @return 装備込のパラメータ
     */
    public ShipParameters getParam() {
        return this.param;
    }

    /**
     * 装備による上昇分
     * @return 装備による上昇分
     */
    public ShipParameters getSlotParam() {
        return this.slotParam;
    }

    /**
     * この艦の最大パラメータ（装備なしで）
     * @return この艦の最大パラメータ（装備なしで）
     */
    public ShipParameters getMax() {
        return this.max;
    }

    /**
     * @return
     */
    public String getFullName() {
        return this.shipInfo.getFullName();
    }
}
