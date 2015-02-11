package logbook.dto;

import logbook.config.bean.ShipGroupBean;

/**
 * 所有艦娘一覧で使用するフィルター
 */
public final class ShipFilterDto extends AbstractDto {

    /** 名前 */
    public String nametext;
    /** 名前.正規表現を使用する */
    public boolean regexp;

    /** 艦種.駆逐艦 */
    public boolean destroyer = true;
    /** 艦種.軽巡洋艦 */
    public boolean lightCruiser = true;
    /** 艦種.重雷装巡洋艦 */
    public boolean torpedoCruiser = true;
    /** 艦種.重巡洋艦 */
    public boolean heavyCruiser = true;
    /** 艦種.航空巡洋艦 */
    public boolean flyingDeckCruiser = true;
    /** 艦種.水上機母艦 */
    public boolean seaplaneTender = true;
    /** 艦種.軽空母 */
    public boolean escortCarrier = true;
    /** 艦種.正規空母 */
    public boolean carrier = true;
    /** 艦種.戦艦 */
    public boolean battleship = true;
    /** 艦種.航空戦艦 */
    public boolean flyingDeckBattleship = true;
    /** 艦種.潜水艦 */
    public boolean submarine = true;
    /** 艦種.潜水空母 */
    public boolean carrierSubmarine = true;
    /** 艦種.揚陸艦 */
    public boolean landingship = true;
    /** 艦種.装甲空母 */
    public boolean armoredcarrier = true;
    /** 艦種.装甲空母 */
    public boolean repairship = true;
    /** 艦種.潜水母艦 */
    public boolean submarineTender = true;
    /** 艦種.練習巡洋艦 */
    public boolean trainingShip = true;

    /** グループ */
    public ShipGroupBean group;
    /** 装備 */
    public String itemname;
    /** 艦隊に所属 */
    public boolean onfleet = true;
    /** 艦隊に非所属 */
    public boolean notonfleet = true;
    /** 鍵付き */
    public boolean locked = true;
    /** 鍵付きではない */
    public boolean notlocked = true;
}
