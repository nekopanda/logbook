package logbook.config.bean;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * 所有艦娘のグループを保存します
 *
 */
public final class ShipGroupBean {

    /** 互換性維持のため Long -> Integer 変換を持つSetを定義する */
    public static class ShipSet extends LinkedHashSet<Integer> {
        private static final long serialVersionUID = -7828201548391851142L;

        public void add(Long value) {
            this.add((int) (long) value);
        }
    }

    /** グループ名 */
    private String name;

    /** グループのID */
    private int id;

    /** 艦娘リスト */
    private Set<Integer> ships = new ShipSet();

    /**
     * グループ名を取得します。
     * @return グループ名
     */
    public String getName() {
        return this.name;
    }

    /**
     * グループ名を設定します。
     * @param name グループ名
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 艦娘リストを取得します。
     * @return 艦娘リスト
     */
    public Set<Integer> getShips() {
        return this.ships;
    }

    /**
     * 艦娘リストを設定します。
     * @param ships 艦娘リスト
     */
    public void setShips(Set<Integer> ships) {
        this.ships = ships;
    }

    /**
     * @return id
     */
    public int getId() {
        return this.id;
    }

    /**
     * @param id セットする id
     */
    public void setId(int id) {
        this.id = id;
    }
}
