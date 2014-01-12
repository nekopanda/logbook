/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.config.bean;

import java.util.ArrayList;
import java.util.List;

/**
 * 所有艦娘グループのリストを保存します
 *
 */
public final class ShipGroupListBean {

    /** 所有艦娘グループのリスト */
    private List<ShipGroupBean> group = new ArrayList<ShipGroupBean>();

    /**
     * 所有艦娘グループのリストを取得します。
     * @return 所有艦娘グループのリスト
     */
    public List<ShipGroupBean> getGroup() {
        return this.group;
    }

    /**
     * 所有艦娘グループのリストを設定します。
     * @param group 所有艦娘グループのリスト
     */
    public void setGroup(List<ShipGroupBean> group) {
        this.group = group;
    }
}
