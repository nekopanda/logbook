package logbook.dto;

import logbook.config.bean.ShipGroupBean;

/**
 * 所有艦娘一覧で使用するフィルター
 */
public final class ShipFilterDto extends AbstractDto {

    /** 0: グループ, 1: 艦種 2: なし */
    public int groupMode;

    /** 名前 */
    public String nametext;
    /** 名前.正規表現を使用する */
    public boolean regexp;

    /** 艦種 */
    public boolean[] enabledType = null;

    /** グループ */
    public transient ShipGroupBean group;
    /** Beanで保存するときにグループはIDで参照したいので */
    public int groupId = 0;

    /** 艦隊に所属 */
    public boolean onfleet = true;
    /** 艦隊に非所属 */
    public boolean notonfleet = true;
    /** 鍵付き */
    public boolean locked = true;
    /** 鍵付きではない */
    public boolean notlocked = true;
    /** 遠征中 */
    public boolean mission = true;
    /** 遠征中ではない */
    public boolean notmission = true;
    /** 要修理 */
    public boolean needbath = true;
    /** 修理の必要なし */
    public boolean notneedbath = true;
}