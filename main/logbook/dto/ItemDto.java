package logbook.dto;

import javax.json.JsonObject;

import logbook.internal.ItemType;

/**
 * 装備を表します
 *
 */
public final class ItemDto extends AbstractDto {

    private long id;
    private long type2;
    private long type3;
    private long atap;
    private long bakk;
    private long baku;
    private long houg;
    private long houk;
    private long houm;
    private long leng;
    private long luck;
    private String name;
    private long raig;
    private long raik;
    private long raim;
    private long rare;
    private long sakb;
    private long saku;
    private long soku;
    private long souk;
    private long taik;
    private long tais;
    private long tyku;

    /**
     * コンストラクター
     */
    public ItemDto() {
    }

    /**
     * コンストラクター
     * 
     * @param object JSON Object
     */
    public ItemDto(JsonObject object) {

        this.type2 = object.getJsonArray("api_type").getInt(2);
        this.type3 = object.getJsonArray("api_type").getInt(3);

        this.id = object.getJsonNumber("api_id").longValue();
        this.atap = object.getJsonNumber("api_atap").longValue();
        this.bakk = object.getJsonNumber("api_bakk").longValue();
        this.baku = object.getJsonNumber("api_baku").longValue();
        this.houg = object.getJsonNumber("api_houg").longValue();
        this.houk = object.getJsonNumber("api_houk").longValue();
        this.houm = object.getJsonNumber("api_houm").longValue();
        this.leng = object.getJsonNumber("api_leng").longValue();
        this.luck = object.getJsonNumber("api_luck").longValue();
        this.name = object.getString("api_name");
        this.raig = object.getJsonNumber("api_raig").longValue();
        this.raik = object.getJsonNumber("api_raik").longValue();
        this.raim = object.getJsonNumber("api_raim").longValue();
        this.rare = object.getJsonNumber("api_rare").longValue();
        this.sakb = object.getJsonNumber("api_sakb").longValue();
        this.saku = object.getJsonNumber("api_saku").longValue();
        this.soku = object.getJsonNumber("api_soku").longValue();
        this.souk = object.getJsonNumber("api_souk").longValue();
        this.taik = object.getJsonNumber("api_taik").longValue();
        this.tais = object.getJsonNumber("api_tais").longValue();
        this.tyku = object.getJsonNumber("api_tyku").longValue();
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
    public ItemDto(long id, long type2, long type3, long atap, long bakk, long baku, long houg, long houk, long houm,
            long leng, long luck, String name, long raig, long raik, long raim, long rare, long sakb, long saku,
            long soku, long souk, long taik, long tais, long tyku) {
        this.id = id;
        this.type2 = type2;
        this.type3 = type3;
        this.atap = atap;
        this.bakk = bakk;
        this.baku = baku;
        this.houg = houg;
        this.houk = houk;
        this.houm = houm;
        this.leng = leng;
        this.luck = luck;
        this.name = name;
        this.raig = raig;
        this.raik = raik;
        this.raim = raim;
        this.rare = rare;
        this.sakb = sakb;
        this.saku = saku;
        this.soku = soku;
        this.souk = souk;
        this.taik = taik;
        this.tais = tais;
        this.tyku = tyku;
    }

    /**
     * @return type
     */
    public String getType() {
        return ItemType.get(Long.toString(this.type3));
    }

    /**
     * @return type2
     */
    public String getTypeId2() {
        return Long.toString(this.type2);
    }

    /**
     * type3を設定します。
     * @param type2 type2
     */
    public void setType2(long type2) {
        this.type2 = type2;
    }

    /**
     * @return type3
     */
    public String getTypeId3() {
        return Long.toString(this.type3);
    }

    /**
     * type3を設定します。
     * @param type3 type3
     */
    public void setType3(long type3) {
        this.type3 = type3;
    }

    /**
     * atapを取得します。
     * @return atap
     */
    public long getAtap() {
        return this.atap;
    }

    /**
     * atapを設定します。
     * @param atap atap
     */
    public void setAtap(long atap) {
        this.atap = atap;
    }

    /**
     * bakkを取得します。
     * @return bakk
     */
    public long getBakk() {
        return this.bakk;
    }

    /**
     * bakkを設定します。
     * @param bakk bakk
     */
    public void setBakk(long bakk) {
        this.bakk = bakk;
    }

    /**
     * bakuを取得します。
     * @return baku
     */
    public long getBaku() {
        return this.baku;
    }

    /**
     * bakuを設定します。
     * @param baku baku
     */
    public void setBaku(long baku) {
        this.baku = baku;
    }

    /**
     * hougを取得します。
     * @return houg
     */
    public long getHoug() {
        return this.houg;
    }

    /**
     * hougを設定します。
     * @param houg houg
     */
    public void setHoug(long houg) {
        this.houg = houg;
    }

    /**
     * houkを取得します。
     * @return houk
     */
    public long getHouk() {
        return this.houk;
    }

    /**
     * houkを設定します。
     * @param houk houk
     */
    public void setHouk(long houk) {
        this.houk = houk;
    }

    /**
     * houmを取得します。
     * @return houm
     */
    public long getHoum() {
        return this.houm;
    }

    /**
     * houmを設定します。
     * @param houm houm
     */
    public void setHoum(long houm) {
        this.houm = houm;
    }

    /**
     * idを取得します。
     * @return id
     */
    public long getId() {
        return this.id;
    }

    /**
     * idを設定します。
     * @param id id
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * lengを取得します。
     * @return leng
     */
    public long getLeng() {
        return this.leng;
    }

    /**
     * lengを設定します。
     * @param leng leng
     */
    public void setLeng(long leng) {
        this.leng = leng;
    }

    /**
     * luckを取得します。
     * @return luck
     */
    public long getLuck() {
        return this.luck;
    }

    /**
     * luckを設定します。
     * @param luck luck
     */
    public void setLuck(long luck) {
        this.luck = luck;
    }

    /**
     * nameを取得します。
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

    /**
     * raigを取得します。
     * @return raig
     */
    public long getRaig() {
        return this.raig;
    }

    /**
     * raigを設定します。
     * @param raig raig
     */
    public void setRaig(long raig) {
        this.raig = raig;
    }

    /**
     * raikを取得します。
     * @return raik
     */
    public long getRaik() {
        return this.raik;
    }

    /**
     * raikを設定します。
     * @param raik raik
     */
    public void setRaik(long raik) {
        this.raik = raik;
    }

    /**
     * raimを取得します。
     * @return raim
     */
    public long getRaim() {
        return this.raim;
    }

    /**
     * raimを設定します。
     * @param raim raim
     */
    public void setRaim(long raim) {
        this.raim = raim;
    }

    /**
     * rareを取得します。
     * @return rare
     */
    public long getRare() {
        return this.rare;
    }

    /**
     * rareを設定します。
     * @param rare rare
     */
    public void setRare(long rare) {
        this.rare = rare;
    }

    /**
     * sakbを取得します。
     * @return sakb
     */
    public long getSakb() {
        return this.sakb;
    }

    /**
     * sakbを設定します。
     * @param sakb sakb
     */
    public void setSakb(long sakb) {
        this.sakb = sakb;
    }

    /**
     * sakuを取得します。
     * @return saku
     */
    public long getSaku() {
        return this.saku;
    }

    /**
     * sakuを設定します。
     * @param saku saku
     */
    public void setSaku(long saku) {
        this.saku = saku;
    }

    /**
     * sokuを取得します。
     * @return soku
     */
    public long getSoku() {
        return this.soku;
    }

    /**
     * sokuを設定します。
     * @param soku soku
     */
    public void setSoku(long soku) {
        this.soku = soku;
    }

    /**
     * soukを取得します。
     * @return souk
     */
    public long getSouk() {
        return this.souk;
    }

    /**
     * soukを設定します。
     * @param souk souk
     */
    public void setSouk(long souk) {
        this.souk = souk;
    }

    /**
     * taikを取得します。
     * @return taik
     */
    public long getTaik() {
        return this.taik;
    }

    /**
     * taikを設定します。
     * @param taik taik
     */
    public void setTaik(long taik) {
        this.taik = taik;
    }

    /**
     * taisを取得します。
     * @return tais
     */
    public long getTais() {
        return this.tais;
    }

    /**
     * taisを設定します。
     * @param tais tais
     */
    public void setTais(long tais) {
        this.tais = tais;
    }

    /**
     * tykuを取得します。
     * @return tyku
     */
    public long getTyku() {
        return this.tyku;
    }

    /**
     * tykuを設定します。
     * @param tyku tyku
     */
    public void setTyku(long tyku) {
        this.tyku = tyku;
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if ((obj != null) && (obj instanceof ItemDto)) {
            return this.name.equals(((ItemDto) obj).getName());
        }
        return false;
    }
}
