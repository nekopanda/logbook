/**
 * 
 */
package logbook.dto;

import java.util.List;

import javax.json.JsonObject;

import com.dyuproject.protostuff.Tag;

/**
 * 艦や装備のパラメータ
 * @author Nekopanda
 */
public class ShipParameters {

    /** HP */
    @Tag(1)
    private int taik;

    /** 火力 */
    @Tag(2)
    private int houg;

    /** 命中 (現状アイテムのみ) */
    @Tag(3)
    private int houm;

    /** 雷装 */
    @Tag(4)
    private int raig;

    /** 爆装 (現状アイテムのみ) */
    @Tag(5)
    private int baku;

    /** 対空 */
    @Tag(6)
    private int tyku;

    /** 装甲 or 雷撃命中 */
    @Tag(7)
    private int souk;

    /** 回避 */
    @Tag(8)
    private int kaih;

    /** 対潜 */
    @Tag(9)
    private int tais;

    /** 索敵 */
    @Tag(10)
    private int saku;

    /** 運 */
    @Tag(11)
    private int luck;

    /** 速力 */
    @Tag(12)
    private int soku;

    /** 射程 */
    @Tag(13)
    private int leng;

    public static String sokuToString(int soku) {
        switch (soku) {
        case 0:
            return "陸上";
        case 5:
            return "低速";
        case 10:
            return "高速";
        }
        return "不明(" + soku + ")";
    }

    public static String lengToString(int leng) {
        switch (leng) {
        case 1:
            return "短";
        case 2:
            return "中";
        case 3:
            return "長";
        case 4:
            return "超長";
        }
        return "不明(" + leng + ")";
    }

    public ShipParameters() {
    }

    public ShipParameters(int taik, int houg, int houm, int raig, int baku, int tyku, int souk,
            int kaih, int tais, int saku, int luck, int soku, int leng) {
        this.taik = taik;
        this.houg = houg;
        this.houm = houm;
        this.leng = leng;
        this.luck = luck;
        this.raig = raig;
        this.baku = baku;
        this.saku = saku;
        this.soku = soku;
        this.souk = souk;
        this.taik = taik;
        this.tais = tais;
        this.tyku = tyku;
        this.kaih = kaih;
    }

    /** マスターデータ用 */
    public static ShipParameters fromMasterItem(JsonObject object) {
        ShipParameters param = new ShipParameters();
        param.houg = object.getInt("api_houg");
        param.kaih = object.getInt("api_houk");
        param.houm = object.getInt("api_houm");
        param.leng = object.getInt("api_leng");
        param.luck = object.getInt("api_luck");
        param.raig = object.getInt("api_raig");
        param.baku = object.getInt("api_baku");
        param.saku = object.getInt("api_saku");
        param.soku = object.getInt("api_soku");
        param.souk = object.getInt("api_souk");
        param.taik = object.getInt("api_taik");
        param.tais = object.getInt("api_tais");
        param.tyku = object.getInt("api_tyku");
        return param;
    }

    /** マスターデータ用 (現在値?, MAX?) */
    public static ShipParameters[] fromMasterShip(JsonObject object) {
        ShipParameters[] ret = new ShipParameters[2];
        for (int i = 0; i < 2; ++i) {
            ShipParameters param = new ShipParameters();
            param.taik = object.getJsonArray("api_taik").getInt(i);
            param.houg = object.getJsonArray("api_houg").getInt(i);
            param.leng = object.getInt("api_leng");
            param.raig = object.getJsonArray("api_raig").getInt(i);
            param.tyku = object.getJsonArray("api_tyku").getInt(i);
            param.soku = object.getInt("api_soku");
            param.souk = object.getJsonArray("api_souk").getInt(i);
            //param.kaih = object.getJsonArray("api_kaih").getInt(i);
            //param.tais = object.getJsonArray("api_tais").getInt(i);
            //param.saku = object.getJsonArray("api_saku").getInt(i);
            param.luck = object.getJsonArray("api_luck").getInt(i);
            ret[i] = param;
        }
        return ret;
    }

    public static ShipParameters[] fromMasterEnemyShip(JsonObject object) {
        ShipParameters[] ret = new ShipParameters[2];
        for (int i = 0; i < 2; ++i) {
            ShipParameters param = new ShipParameters();
            param.soku = object.getJsonNumber("api_soku").intValue();
            ret[i] = param;
        }
        return ret;
    }

    /** 艦娘用 (現在値, MAX, 装備による上昇分) */
    public static ShipParameters[] fromShip(JsonObject object, List<ItemInfoDto> slotitem, ShipInfoDto masterShip) {
        ShipParameters[] ret = new ShipParameters[3];
        for (int i = 0; i < 2; ++i) {
            ShipParameters param = new ShipParameters();
            if (i == 0) {
                param.taik = object.getInt("api_nowhp");
            }
            else {
                param.taik = object.getInt("api_maxhp");
            }
            param.houg = object.getJsonArray("api_karyoku").getInt(i);
            param.leng = object.getInt("api_leng");
            param.raig = object.getJsonArray("api_raisou").getInt(i);
            param.tyku = object.getJsonArray("api_taiku").getInt(i);
            param.soku = masterShip.getParam().getSoku();
            param.souk = object.getJsonArray("api_soukou").getInt(i);
            param.kaih = object.getJsonArray("api_kaihi").getInt(i);
            param.tais = object.getJsonArray("api_taisen").getInt(i);
            param.saku = object.getJsonArray("api_sakuteki").getInt(i);
            param.luck = object.getJsonArray("api_lucky").getInt(i);
            ret[i] = param;
        }

        // 装備の上昇分を計算
        ShipParameters slotParam = new ShipParameters();
        for (ItemInfoDto item : slotitem) {
            if (item != null) {
                slotParam.add(item.getParam());
            }
        }
        ret[2] = slotParam;

        return ret;
    }

    /** 敵艦用 (装備込, 装備による上昇分) */
    public static ShipParameters[] fromBaseAndSlotItem(ShipParameters base, int[] param, List<ItemInfoDto> slotitem) {
        ShipParameters slotParam = new ShipParameters();
        ShipParameters total = new ShipParameters();

        // 装備の上昇分を計算
        for (ItemInfoDto item : slotitem) {
            if (item != null) {
                slotParam.add(item.getParam());
            }
        }

        // 合計を計算
        if (param != null) {
            total.add(base, param);
        }
        else {
            total.add(base);
        }
        total.add(slotParam);

        return new ShipParameters[] { total, slotParam };
    }

    /**
     * パラメータoを自分に足す
     * @param o　足すパラメータ
     */
    public void add(ShipParameters o) {
        this.taik += o.taik;
        this.houg += o.houg;
        this.houm += o.houm;
        this.raig += o.raig;
        this.baku += o.baku;
        this.tyku += o.tyku;
        this.souk += o.souk;
        this.kaih += o.kaih;
        this.tais += o.tais;
        this.saku += o.saku;
        this.luck += o.luck;
        this.leng = Math.max(this.leng, o.leng);
        this.soku = Math.max(this.soku, o.soku);
    }

    /**
     * パラメータoを自分に足す
     * @param o　足すパラメータ
     */
    public void add(ShipParameters o, int[] param) {
        this.taik += o.taik;
        this.houg += param[0];
        this.houm += o.houm;
        this.raig += param[1];
        this.baku += o.baku;
        this.tyku += param[2];
        this.souk += param[3];
        this.kaih += o.kaih;
        this.tais += o.tais;
        this.saku += o.saku;
        this.luck += o.luck;
        this.leng = Math.max(this.leng, o.leng);
        this.soku = Math.max(this.soku, o.soku);
    }

    /**
     * パラメータoを自分から引く
     * @param o　引くパラメータ
     */
    public void subtract(ShipParameters o) {
        this.taik -= o.taik;
        this.houg -= o.houg;
        this.houm -= o.houm;
        this.raig -= o.raig;
        this.tyku -= o.tyku;
        this.souk -= o.souk;
        this.kaih -= o.kaih;
        this.tais -= o.tais;
        this.saku -= o.saku;
        this.luck -= o.luck;
        this.leng = Math.max(this.leng, o.leng);
        this.soku = Math.max(this.soku, o.soku);
    }

    @Override
    public int hashCode() {
        return this.taik + this.houg + this.houm + this.leng + this.raig + this.baku +
                this.tyku + this.soku + this.souk + this.kaih + this.tais + this.saku + this.luck;
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof ShipParameters)) {
            ShipParameters o = (ShipParameters) obj;
            if ((this.taik == o.taik) &&
                    (this.houg == o.houg) &&
                    (this.houm == o.houm) &&
                    (this.leng == o.leng) &&
                    (this.raig == o.raig) &&
                    (this.baku == o.baku) &&
                    (this.tyku == o.tyku) &&
                    (this.soku == o.soku) &&
                    (this.souk == o.souk) &&
                    (this.kaih == o.kaih) &&
                    (this.tais == o.tais) &&
                    (this.saku == o.saku) &&
                    (this.luck == o.luck)) {
                return true;
            }
        }
        return false;
    }

    ///////////////////////

    /**
     * HP
     * @return HP
     */
    public int getHP() {
        return this.taik;
    }

    /**
     * 火力
     * @return 火力
     */
    public int getKaryoku() {
        return this.houg;
    }

    /**
     * 雷装
     * @return 雷装
     */
    public int getRaisou() {
        return this.raig;
    }

    /**
     * 対空
     * @return 対空
     */
    public int getTaiku() {
        return this.tyku;
    }

    /**
     * 装甲
     * @return 装甲
     */
    public int getSoukou() {
        return this.souk;
    }

    /**
     * 回避
     * @return 回避
     */
    public int getKaihi() {
        return this.kaih;
    }

    /**
     * 対潜
     * @return 対潜
     */
    public int getTaisen() {
        return this.tais;
    }

    /**
     * 索敵
     * @return 索敵
     */
    public int getSakuteki() {
        return this.saku;
    }

    /**
     * 運
     * @return 運
     */
    public int getLucky() {
        return this.luck;
    }

    /**
     * kaih (= houk)
     * @return kaih (= houk)
     */
    public int getHouk() {
        return this.kaih;
    }

    ///////////////////////

    /**
     * @return taik
     */
    public int getTaik() {
        return this.taik;
    }

    /**
     * @param taik セットする taik
     */
    public void setTaik(int taik) {
        this.taik = taik;
    }

    /**
     * @return houg
     */
    public int getHoug() {
        return this.houg;
    }

    /**
     * @param houg セットする houg
     */
    public void setHoug(int houg) {
        this.houg = houg;
    }

    /**
     * @return raig
     */
    public int getRaig() {
        return this.raig;
    }

    /**
     * @param raig セットする raig
     */
    public void setRaig(int raig) {
        this.raig = raig;
    }

    /**
     * @return tyku
     */
    public int getTyku() {
        return this.tyku;
    }

    /**
     * @param tyku セットする tyku
     */
    public void setTyku(int tyku) {
        this.tyku = tyku;
    }

    /**
     * @return souk
     */
    public int getSouk() {
        return this.souk;
    }

    /**
     * @param souk セットする souk
     */
    public void setSouk(int souk) {
        this.souk = souk;
    }

    /**
     * @return kaih
     */
    public int getKaih() {
        return this.kaih;
    }

    /**
     * @param kaih セットする kaih
     */
    public void setKaih(int kaih) {
        this.kaih = kaih;
    }

    /**
     * @return tais
     */
    public int getTais() {
        return this.tais;
    }

    /**
     * @param tais セットする tais
     */
    public void setTais(int tais) {
        this.tais = tais;
    }

    /**
     * @return saku
     */
    public int getSaku() {
        return this.saku;
    }

    /**
     * @param saku セットする saku
     */
    public void setSaku(int saku) {
        this.saku = saku;
    }

    /**
     * @return luck
     */
    public int getLuck() {
        return this.luck;
    }

    /**
     * @param luck セットする luck
     */
    public void setLuck(int luck) {
        this.luck = luck;
    }

    /**
     * @return soku
     */
    public int getSoku() {
        return this.soku;
    }

    /**
     * @param soku セットする soku
     */
    public void setSoku(int soku) {
        this.soku = soku;
    }

    public String getSokuString() {
        return sokuToString(this.soku);
    }

    /**
     * @return leng
     */
    public int getLeng() {
        return this.leng;
    }

    /**
     * @param leng セットする leng
     */
    public void setLeng(int leng) {
        this.leng = leng;
    }

    public String getLengString() {
        return lengToString(this.leng);
    }

    /**
     * @return houm
     */
    public int getHoum() {
        return this.houm;
    }

    /**
     * @param houm セットする houm
     */
    public void setHoum(int houm) {
        this.houm = houm;
    }

    /**
     * @return baku
     */
    public int getBaku() {
        return this.baku;
    }

    /**
     * @param baku セットする baku
     */
    public void setBaku(int baku) {
        this.baku = baku;
    }
}
