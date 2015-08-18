/**
 * 
 */
package logbook.dto;

import java.util.HashMap;
import java.util.Map;

import javax.json.JsonArray;
import javax.json.JsonObject;

import logbook.constants.AppConstants;
import logbook.data.Data;

/**
 * 消費した or 増えた 資源・アイテムを表します
 * @author Nekopanda
 */
public class ResourceItemDto {
    /** fuel, ammo, metal, bauxite */
    private final int[] baseMaterials = new int[4];

    private final Map<Integer, UseItemDto> items = new HashMap<Integer, UseItemDto>();

    public ResourceItemDto() {
    }

    public void loadBaseMaterialsFromString(String[] data, int offset) {
        for (int i = 0; i < 4; ++i) {
            this.baseMaterials[i] = Integer.valueOf(data[i + offset]);
        }
    }

    public void loadBaseMaterialsFromField(Data data) {
        this.baseMaterials[0] = Integer.valueOf(data.getField("api_item1"));
        this.baseMaterials[1] = Integer.valueOf(data.getField("api_item2"));
        this.baseMaterials[2] = Integer.valueOf(data.getField("api_item3"));
        this.baseMaterials[3] = Integer.valueOf(data.getField("api_item4"));
    }

    /** JSONの配列で表された資源を読み取る。長さ4 と　長さ8 に対応 */
    public void loadMaterialFronJson(JsonArray data) {
        this.baseMaterials[0] = data.getInt(0);
        this.baseMaterials[1] = data.getInt(1);
        this.baseMaterials[2] = data.getInt(2);
        this.baseMaterials[3] = data.getInt(3);
        if (data.size() >= 8) {
            this.setBurners(data.getInt(4));
            this.setBuckets(data.getInt(5));
            this.setResearchMaterials(data.getInt(6));
            this.setScrew(data.getInt(7));
        }
    }

    /** 遠征結果の獲得資源・アイテムを読み取る */
    public void loadMissionResult(JsonObject apidata) {
        // baseMaterialsを読み取る
        this.loadMaterialFronJson(apidata.getJsonArray("api_get_material"));
        // UseItemを読み取る
        JsonArray jsonUseItemFlag = apidata.getJsonArray("api_useitem_flag");
        for (int i = 0; i < 2; ++i) {
            int useItemFlag = jsonUseItemFlag.getInt(i);
            if (useItemFlag > 0) {
                JsonObject jsonGetItem = apidata.getJsonObject("api_get_item" + (i + 1));
                int itemId = jsonGetItem.getInt("api_useitem_id");
                if (itemId <= 0) {
                    itemId = useItemFlag;
                }
                int itemCount = jsonGetItem.getInt("api_useitem_count");
                this.items.put(itemId,
                        new UseItemDto(itemId, itemCount));
            }
        }
    }

    /** クエスト結果の獲得資源・アイテムを読み取る */
    public void loadQuestClear(JsonObject apidata) {
        // baseMaterialsを読み取る
        this.loadMaterialFronJson(apidata.getJsonArray("api_material"));
        // UseItemを読み取る
        JsonArray bounus = apidata.getJsonArray("api_bounus");
        for (int i = 0; i < bounus.size(); ++i) {
            JsonObject item = bounus.getJsonObject(i);
            int type = item.getInt("api_type");
            if (type == 1) { // 1以外は知らん
                int itemId = item.getJsonObject("api_item").getInt("api_id");
                int itemCount = item.getInt("api_count");
                switch (itemId) {
                case 5:
                    this.items.put(AppConstants.USEITEM_BURNER,
                            new UseItemDto(AppConstants.USEITEM_BURNER, itemCount));
                    break;
                case 6:
                    this.items.put(AppConstants.USEITEM_BUCKET,
                            new UseItemDto(AppConstants.USEITEM_BUCKET, itemCount));
                    break;
                case 7:
                    this.items.put(AppConstants.USEITEM_RESEARCH,
                            new UseItemDto(AppConstants.USEITEM_RESEARCH, itemCount));
                    break;
                case 8:
                    this.items.put(AppConstants.USEITEM_SCREW,
                            new UseItemDto(AppConstants.USEITEM_SCREW, itemCount));
                    break;
                default: // 5-8以外は知らん
                    break;
                }
            }
        }
    }

    public void setBurners(int amount) {
        this.items.put(AppConstants.USEITEM_BURNER,
                new UseItemDto(AppConstants.USEITEM_BURNER, amount));
    }

    public void setBuckets(int amount) {
        this.items.put(AppConstants.USEITEM_BUCKET,
                new UseItemDto(AppConstants.USEITEM_BUCKET, amount));
    }

    public void setResearchMaterials(int amount) {
        this.items.put(AppConstants.USEITEM_RESEARCH,
                new UseItemDto(AppConstants.USEITEM_RESEARCH, amount));
    }

    public void setScrew(int amount) {
        this.items.put(AppConstants.USEITEM_SCREW,
                new UseItemDto(AppConstants.USEITEM_SCREW, amount));
    }

    public void setItem(int id, int amount) {
        this.items.put(id, new UseItemDto(id, amount));
    }

    public int getFuel() {
        return this.baseMaterials[0];
    }

    public void setFuel(int fuel) {
        this.baseMaterials[0] = fuel;
    }

    public int getAmmo() {
        return this.baseMaterials[1];
    }

    public void setAmmo(int ammo) {
        this.baseMaterials[1] = ammo;
    }

    public int getMetal() {
        return this.baseMaterials[2];
    }

    public void setMetal(int metal) {
        this.baseMaterials[2] = metal;
    }

    public int getBauxite() {
        return this.baseMaterials[3];
    }

    public void setBauxite(int bauxite) {
        this.baseMaterials[3] = bauxite;
    }

    public int getBurners() {
        UseItemDto item = this.items.get(AppConstants.USEITEM_BURNER);
        if (item == null)
            return 0;
        return item.getItemCount();
    }

    public int getBuckets() {
        UseItemDto item = this.items.get(AppConstants.USEITEM_BUCKET);
        if (item == null)
            return 0;
        return item.getItemCount();
    }

    public int getResearch() {
        UseItemDto item = this.items.get(AppConstants.USEITEM_RESEARCH);
        if (item == null)
            return 0;
        return item.getItemCount();
    }

    public int getScrew() {
        UseItemDto item = this.items.get(AppConstants.USEITEM_SCREW);
        if (item == null)
            return 0;
        return item.getItemCount();
    }

    public Map<Integer, UseItemDto> getItems() {
        return this.items;
    }

    public MaterialDto toMaterialDto() {
        MaterialDto ret = new MaterialDto();
        return ret.obtained(this);
    }
}
