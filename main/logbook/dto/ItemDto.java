/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.dto;

import javax.json.JsonObject;

import logbook.internal.ItemType;

/**
 * 装備を表します
 *
 */
public final class ItemDto extends AbstractDto {

    private final long type4;
    private final long atap;
    private final long bakk;
    private final long baku;
    private final long houg;
    private final long houk;
    private final long houm;
    private final long id;
    private final long kaih;
    private final long leng;
    private final long luck;
    private final String name;
    private final long raig;
    private final long raik;
    private final long raim;
    private final long rare;
    private final long sakb;
    private final long saku;
    private final long soku;
    private final long souk;
    private final long taik;
    private final long tais;
    private final long tyku;

    /**
     * コンストラクター
     * 
     * @param object JSON Object
     */
    public ItemDto(JsonObject object) {

        this.type4 = object.getJsonArray("api_type").getInt(3);

        this.atap = object.getJsonNumber("api_atap").longValue();
        this.bakk = object.getJsonNumber("api_bakk").longValue();
        this.baku = object.getJsonNumber("api_baku").longValue();
        this.houg = object.getJsonNumber("api_houg").longValue();
        this.houk = object.getJsonNumber("api_houk").longValue();
        this.houm = object.getJsonNumber("api_houm").longValue();
        this.id = object.getJsonNumber("api_id").longValue();
        this.kaih = object.getJsonNumber("api_kaih").longValue();
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
     * @return type
     */
    public String getType() {
        return ItemType.get(Long.toString(this.type4));
    }

    /**
     * @return atap
     */
    public long getAtap() {
        return this.atap;
    }

    /**
     * @return bakk
     */
    public long getBakk() {
        return this.bakk;
    }

    /**
     * @return baku
     */
    public long getBaku() {
        return this.baku;
    }

    /**
     * @return houg
     */
    public long getHoug() {
        return this.houg;
    }

    /**
     * @return houk
     */
    public long getHouk() {
        return this.houk;
    }

    /**
     * @return houm
     */
    public long getHoum() {
        return this.houm;
    }

    /**
     * @return id
     */
    public long getId() {
        return this.id;
    }

    /**
     * @return kaih
     */
    public long getKaih() {
        return this.kaih;
    }

    /**
     * @return leng
     */
    public long getLeng() {
        return this.leng;
    }

    /**
     * @return luck
     */
    public long getLuck() {
        return this.luck;
    }

    /**
     * @return name
     */
    public String getName() {
        return this.name;
    }

    /**
     * @return raig
     */
    public long getRaig() {
        return this.raig;
    }

    /**
     * @return raik
     */
    public long getRaik() {
        return this.raik;
    }

    /**
     * @return raim
     */
    public long getRaim() {
        return this.raim;
    }

    /**
     * @return rare
     */
    public long getRare() {
        return this.rare;
    }

    /**
     * @return sakb
     */
    public long getSakb() {
        return this.sakb;
    }

    /**
     * @return saku
     */
    public long getSaku() {
        return this.saku;
    }

    /**
     * @return soku
     */
    public long getSoku() {
        return this.soku;
    }

    /**
     * @return souk
     */
    public long getSouk() {
        return this.souk;
    }

    /**
     * @return taik
     */
    public long getTaik() {
        return this.taik;
    }

    /**
     * @return tais
     */
    public long getTais() {
        return this.tais;
    }

    /**
     * @return tyku
     */
    public long getTyku() {
        return this.tyku;
    }
}
