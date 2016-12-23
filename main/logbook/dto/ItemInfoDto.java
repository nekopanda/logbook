package logbook.dto;

import javax.json.JsonObject;

import logbook.constants.AppConstants;
import logbook.internal.ItemType;
import logbook.util.JsonUtils;

import com.dyuproject.protostuff.Tag;

/**
 * 装備を表します
 */
public final class ItemInfoDto extends AbstractDto {

    @Tag(1)
    private int id;
    /**
     * [0]: 大分類（砲、魚雷、艦載機、...）
     * [1]: 図鑑の背景にある英語表記分類
     * [2]: 装備可能艦種別分類
     * [3]: アイコンの分類
     */
    @Tag(2)
    private int[] type = new int[4];
    @Tag(3)
    private String name;
    @Tag(4)
    private ShipParameters param;

    /**
     * コンストラクター
     */
    public ItemInfoDto() {
    }

    /**
     * コンストラクター
     * 
     * @param object JSON Object
     */
    public ItemInfoDto(JsonObject object) {
        this.id = object.getJsonNumber("api_id").intValue();
        this.type = JsonUtils.getIntArray(object, "api_type");
        this.name = object.getString("api_name");
        this.param = ShipParameters.fromMasterItem(object);
    }

    /**
     * コンストラクター
     * 
     * @param id
     * @param type2
     * @param type3
     * @param atap
     * @param bakk
     * @param baku
     * @param houg
     * @param houk
     * @param houm
     * @param leng
     * @param luck
     * @param name
     * @param raig
     * @param raik
     * @param raim
     * @param rare
     * @param sakb
     * @param saku
     * @param soku
     * @param souk
     * @param taik
     * @param tais
     * @param tyku
     */
    public ItemInfoDto(int id, int type2, int type3, int atap, int bakk, int baku, int houg, int houk, int houm,
            int leng, int luck, String name, int raig, int raik, int raim, int rare, int sakb, int saku,
            int soku, int souk, int taik, int tais, int tyku) {
        this.id = id;
        this.type[2] = type2;
        this.type[3] = type3;
        this.name = name;
        this.param = new ShipParameters(taik, houg, houm, raig, baku, tyku, souk,
                houk, tais, saku, luck, soku, leng);
    }

    public boolean isPlane() {
        for (int i = 0; i < AppConstants.PLANE_ITEM_TYPES.length; ++i) {
            if (this.type[2] == AppConstants.PLANE_ITEM_TYPES[i]) {
                return true;
            }
        }
        return false;
    }

    public boolean canEqipExslot() {
        for (int i = 0; i < AppConstants.EXSLOT_ITEM_TYPES.length; ++i) {
            if (this.type[2] == AppConstants.EXSLOT_ITEM_TYPES[i]) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return 表示分類名
     */
    public String getTypeName() {
        return ItemType.get(this.type[3]);
    }

    /**
     * typeを設定します。
     * @param type type
     */
    public void setType(int[] type) {
        this.type = type;
    }

    /**
     * typeを取得します。
     * @return type
     */
    public int[] getType() {
        return this.type;
    }

    /**
     * type[0]: 大分類（砲、魚雷、艦載機、...）
     * @return type0
     */
    public int getType0() {
        return this.type[0];
    }

    /**
     * type[1]: 図鑑の背景にある英語表記分類
     * @return type1
     */
    public int getType1() {
        return this.type[1];
    }

    /**
     * type[2]: 装備可能艦種別分類
     * @return type2
     */
    public int getType2() {
        return this.type[2];
    }

    /**
     * type[3]: アイコンの分類
     * @return type3
     */
    public int getType3() {
        return this.type[3];
    }

    /**
     * slotitem_id
     * @return id
     */
    public int getId() {
        return this.id;
    }

    /**
     * idを設定します。
     * @param id id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * 名前
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * nameを設定します。
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof ItemInfoDto)) {
            return this.name.equals(((ItemInfoDto) obj).getName());
        }
        return false;
    }

    /**
     * 装備のパラメータ
     * @return param
     */
    public ShipParameters getParam() {
        return this.param;
    }

    /**
     * @param param セットする param
     */
    public void setParam(ShipParameters param) {
        this.param = param;
    }
}