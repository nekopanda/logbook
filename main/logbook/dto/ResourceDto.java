/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.dto;

/**
 * 投入資源と秘書艦を表します
 *
 */
public final class ResourceDto extends AbstractDto {

    /** 燃料 */
    private final String fuel;

    /** 弾薬 */
    private final String ammo;

    /** 鋼材 */
    private final String metal;

    /** ボーキサイト */
    private final String bauxite;

    /** 秘書艦 */
    private final ShipDto ship;

    /**
     * コンストラクター
     * 
     * @param fuel 燃料
     * @param ammo 弾薬
     * @param metal 鋼材
     * @param bauxite ボーキサイト
     * @param ship 秘書艦
     */
    public ResourceDto(String fuel, String ammo, String metal, String bauxite, ShipDto ship) {

        this.fuel = fuel;
        this.ammo = ammo;
        this.metal = metal;
        this.bauxite = bauxite;
        this.ship = ship;
    }

    /**
     * @return 燃料
     */
    public String getFuel() {
        return this.fuel;
    }

    /**
     * @return 弾薬
     */
    public String getAmmo() {
        return this.ammo;
    }

    /**
     * @return 鋼材
     */
    public String getMetal() {
        return this.metal;
    }

    /**
     * @return ボーキサイト
     */
    public String getBauxite() {
        return this.bauxite;
    }

    /**
     * @return 秘書艦
     */
    public ShipDto getSecretary() {
        return this.ship;
    }
}
