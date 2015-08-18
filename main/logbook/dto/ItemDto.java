/**
 * 
 */
package logbook.dto;

import java.beans.Transient;

import javax.json.JsonObject;

import logbook.internal.Item;

import com.dyuproject.protostuff.Tag;

/**
 * 個別装備
 * ロックや改修値などの情報を持つ
 * @author Nekopanda
 */
public class ItemDto extends AbstractDto {

    /** 本体情報（永続化はIDのみで十分なので戻したときはリンクを張り直す） */
    @Tag(1)
    private transient ItemInfoDto info;

    @Tag(2)
    private int slotitemId;

    @Tag(3)
    private int id;

    @Tag(4)
    private boolean locked;

    @Tag(5)
    private int level;

    @Tag(6)
    private int alv;

    public ItemDto() {
    }

    public ItemDto(ItemInfoDto info, int id) {
        this.info = info;
        this.slotitemId = info.getId();
        this.id = id;
    }

    public ItemDto(ItemInfoDto info, JsonObject object) {
        this.info = info;
        this.slotitemId = info.getId();
        this.id = object.getInt("api_id");
        if (object.containsKey("api_locked")) {
            this.locked = object.getInt("api_locked") != 0;
            this.level = object.getInt("api_level");
        }
        else {
            this.locked = false;
            this.level = 0;
        }

        if (object.containsKey("api_alv")) {
            this.alv = object.getInt("api_alv");
        }
        else {
            this.alv = 0;
        }
    }

    /**
     * この装備のマスターデータ
     * @return info
     */
    // java beans はメソッドで認識するのでここに付ける必要がある
    @Transient
    public ItemInfoDto getInfo() {
        if (this.info == null) {
            ItemInfoDto dto = Item.get(this.slotitemId);
            if (dto == null) {
                dto = Item.UNKNOWN;
            }
            this.info = dto;
        }
        return this.info;
    }

    /**
     * @param info セットする info
     */
    public void setInfo(ItemInfoDto info) {
        this.info = info;
    }

    /**
     * 装備ID
     * @return slotitemId
     */
    public int getSlotitemId() {
        return this.slotitemId;
    }

    /**
     * @param slotitemId セットする slotitemId
     */
    public void setSlotitemId(int slotitemId) {
        this.slotitemId = slotitemId;
    }

    /**
     * 装備個別ID
     * @return id
     */
    public int getId() {
        return this.id;
    }

    /**
     * @param id セットする id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * ロックされているか？
     * @return locked
     */
    public boolean isLocked() {
        return this.locked;
    }

    /**
     * @param locked セットする locked
     */
    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * 改修値
     * @return level
     */
    public int getLevel() {
        return this.level;
    }

    /**
     * @param level セットする level
     */
    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isPlane() {
        return this.getInfo().isPlane();
    }

    /**
     * 表示分類名
     * @return 表示分類名
     */
    public String getTypeName() {
        return this.getInfo().getTypeName();
    }

    /**
     * typeを取得します。
     * @return type
     */
    public int[] getType() {
        return this.getInfo().getType();
    }

    /**
     * type0を取得します。
     * @return type0
     */
    public int getType0() {
        return this.getInfo().getType0();
    }

    /**
     * type1を取得します。
     * @return type1
     */
    public int getType1() {
        return this.getInfo().getType1();
    }

    /**
     * type2を取得します。
     * @return type2
     */
    public int getType2() {
        return this.getInfo().getType2();
    }

    /**
     * type3を取得します。
     * @return type3
     */
    public int getType3() {
        return this.getInfo().getType3();
    }

    /**
     * nameを取得します。
     * @return name
     */
    public String getName() {
        return this.getInfo().getName();
    }

    /**
     * 航海日誌における表示名を取得
     * @return
     */
    public String getFriendlyName() {
        String name = this.getInfo().getName();
        if (this.alv > 0) {
            name += "☆" + this.alv;
        }
        if (this.level > 0) {
            name += "★" + this.level;
        }
        return name;
    }

    /**
     * 装備のパラメータ
     * @return
     */
    public ShipParameters getParam() {
        return this.getInfo().getParam();
    }

    /**
     * @return 熟練度
     */
    public int getAlv() {
        return this.alv;
    }

    /**
     * @param alv セットする 熟練度
     */
    public void setAlv(int alv) {
        this.alv = alv;
    }
}
