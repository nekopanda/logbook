/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.dto;

import java.util.regex.Pattern;

/**
 * 所有艦娘一覧で使用するフィルター
 */
public final class ShipFilterDto extends AbstractDto {

    /** 名前 */
    public String nametext;
    /** 名前.正規表現を使用する */
    public boolean regexp;
    /** 名前.正規表現パターン */
    public Pattern namepattern;

    /** 艦種.駆逐艦 */
    public boolean destroyer;
    /** 艦種.軽巡洋艦 */
    public boolean lightCruiser;
    /** 艦種.重雷装巡洋艦 */
    public boolean torpedoCruiser;
    /** 艦種.重巡洋艦 */
    public boolean heavyCruiser;
    /** 艦種.航空巡洋艦 */
    public boolean flyingDeckCruiser;
    /** 艦種.水上機母艦 */
    public boolean seaplaneTender;
    /** 艦種.軽空母 */
    public boolean escortCarrier;
    /** 艦種.正規空母 */
    public boolean carrier;
    /** 艦種.戦艦 */
    public boolean battleship;
    /** 艦種.航空戦艦 */
    public boolean flyingDeckBattleship;
    /** 艦種.潜水艦 */
    public boolean submarine;
    /** 艦種.潜水空母 */
    public boolean carrierSubmarine;

    /** 装備 */
    public String itemname;
    /** 艦隊に所属 */
    public boolean onfleet;
    /** 艦隊に非所属 */
    public boolean notonfleet;
    /** 鍵付き */
    public boolean locked;
    /** 鍵付きではない */
    public boolean notlocked;
}
