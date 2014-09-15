package logbook.dto;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * 建造した艦娘を表します
 */
public final class GetShipDto extends AbstractDto {

    /** 日付 */
    private final Date getDate;

    /** 建造した艦娘 */
    private final ShipDto ship;

    /** 投入資源 */
    private final ResourceDto resources;

    /**
     * コンストラクター
     * 
     * @param object 艦娘情報
     * @param resources 投入資源
     */
    public GetShipDto(ShipDto ship, ResourceDto resources) {

        this.getDate = Calendar.getInstance().getTime();
        this.ship = ship;
        this.resources = resources != null ? resources : new ResourceDto(0, 0, 0, 0, null, 0);
    }

    /**
     * @return 建造日時
     */
    public Date getGetDate() {
        return this.getDate;
    }

    /**
     * @return 艦娘個人を識別するID
     */
    public long getId() {
        return this.ship.getId();
    }

    /**
     * @return 名前
     */
    public String getName() {
        return this.ship.getName();
    }

    /**
     * @return 艦種
     */
    public String getType() {
        return this.ship.getType();
    }

    /**
     * @return Lv
     */
    public long getLv() {
        return this.ship.getLv();
    }

    /**
     * @return 経験値
     */
    public long getExp() {
        return this.ship.getExp();
    }

    /**
     * @return HP
     */
    public long getNowhp() {
        return this.ship.getNowhp();
    }

    /**
     * @return MaxHP
     */
    public long getMaxhp() {
        return this.ship.getMaxhp();
    }

    /**
     * @return 装備
     */
    public List<String> getSlot() {
        return this.ship.getSlot();
    }

    /**
     * @return 火力
     */
    public long getKaryoku() {
        return this.ship.getKaryoku();
    }

    /**
     * @return 雷装
     */
    public long getRaisou() {
        return this.ship.getRaisou();
    }

    /**
     * @return 対空
     */
    public long getTaiku() {
        return this.ship.getTaiku();
    }

    /**
     * @return 装甲
     */
    public long getSoukou() {
        return this.ship.getSoukou();
    }

    /**
     * @return 回避
     */
    public long getKaihi() {
        return this.ship.getKaihi();
    }

    /**
     * @return 対潜
     */
    public long getTaisen() {
        return this.ship.getTaisen();
    }

    /**
     * @return 索敵
     */
    public long getSakuteki() {
        return this.ship.getSakuteki();
    }

    /**
     * @return 運
     */
    public long getLucky() {
        return this.ship.getLucky();
    }

    /**
     * @return 建造種類
     */
    public String getBuildType() {
        Integer type = this.resources.getType();
        if (type == 1) {
            return "大型艦建造";
        }
        return "通常艦建造";
    }

    /**
     * @return 燃料
     */
    public int getFuel() {
        return this.resources.getFuel();
    }

    /**
     * @return 弾薬
     */
    public int getAmmo() {
        return this.resources.getAmmo();
    }

    /**
     * @return 鋼材
     */
    public int getMetal() {
        return this.resources.getMetal();
    }

    /**
     * @return ボーキサイト
     */
    public int getBauxite() {
        return this.resources.getBauxite();
    }

    /**
     * @return 開発資材
     */
    public Integer getResearchMaterials() {
        return this.resources.getResearchMaterials();
    }

    /**
     * @return 空きドック
     */
    public int getFreeDock() {
        return this.resources.getFreeDock();
    }

    /**
     * @return 秘書艦
     */
    public String getSecretary() {
        ShipDto ship = this.resources.getSecretary();
        if (ship != null) {
            return ship.getName() + "(Lv" + ship.getLv() + ")";
        }
        return "";
    }

    /**
     * @return 司令部Lv
     */
    public int getHqLevel() {
        return this.resources.getHqLevel();
    }
}
