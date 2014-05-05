package logbook.config.bean;

import java.util.HashMap;
import java.util.Map;

/**
 * 建造ドックの投入資源をドックごとに保存します
 *
 */
public final class KdockMapBean {

    /** 建造ドックMap */
    private Map<String, KdockBean> kdockMap = new HashMap<String, KdockBean>();

    /**
     * 建造ドックMapを取得します。
     * @return 建造ドックMap
     */
    public Map<String, KdockBean> getKdockMap() {
        return this.kdockMap;
    }

    /**
     * 建造ドックMapを設定します。
     * @param kdockMap 建造ドックMap
     */
    public void setKdockMap(Map<String, KdockBean> kdockMap) {
        this.kdockMap = kdockMap;
    }

}
