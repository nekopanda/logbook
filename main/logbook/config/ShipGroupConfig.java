package logbook.config;

import java.io.IOException;

import logbook.config.bean.ShipGroupListBean;
import logbook.constants.AppConstants;
import logbook.util.BeanUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 所有艦娘グループを保存・復元します
 *
 */
public class ShipGroupConfig {
    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(ShipGroupConfig.class);

    /** 所有艦娘グループ */
    private static ShipGroupListBean group;

    /**
     * 設定ファイルに書き込みます
     */
    public static void store() throws IOException {
        if (group == null) {
            group = new ShipGroupListBean();
        }
        BeanUtils.writeObject(AppConstants.GROUP_CONFIG_FILE, group);
    }

    /**
     * 所有艦娘グループを読み込みます
     */
    public static void load() {
        try {
            ShipGroupListBean bean = BeanUtils.readObject(AppConstants.GROUP_CONFIG_FILE, ShipGroupListBean.class);
            if (bean != null) {
                group = bean;
            } else {
                group = new ShipGroupListBean();
            }
        } catch (Exception e) {
            LOG.warn("所有艦娘グループを読み込みますに失敗しました", e);
        }
    }

    /**
     * 所有艦娘グループを取得します
     * 
     * @return 所有艦娘グループ
     */
    public static ShipGroupListBean get() {
        return group;
    }
}
