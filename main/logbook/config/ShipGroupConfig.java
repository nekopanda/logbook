package logbook.config;

import java.io.IOException;

import logbook.config.bean.ShipGroupBean;
import logbook.config.bean.ShipGroupListBean;
import logbook.constants.AppConstants;
import logbook.gui.ApplicationMain;
import logbook.gui.logic.ShipGroupListener;
import logbook.gui.logic.ShipGroupObserver;
import logbook.internal.LoggerHolder;
import logbook.util.BeanUtils;

/**
 * 所有艦娘グループを保存・復元します
 *
 */
public class ShipGroupConfig {
    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(ShipGroupConfig.class);

    /** 所有艦娘グループ */
    private static ShipGroupListBean group;

    /** 変更があったか */
    private static boolean modified = false;

    // 変更検出用
    private static class ChangeListener implements ShipGroupListener {
        @Override
        public void listChanged() {
            modified = true;
        }

        @Override
        public void groupNameChanged(ShipGroupBean group) {
            modified = true;
        }

        @Override
        public void groupShipChanged(ShipGroupBean group) {
            modified = true;
        }
    }

    // 始めてアクセスした時に読み込む
    static {
        load();
        // 変更を検知する
        ShipGroupObserver.addListener(new ChangeListener());
    }

    /**
     * 設定ファイルに書き込みます
     */
    public static void store() throws IOException {
        // 変更があったときだけ書き込む
        if (modified) {
            if (group == null) {
                group = new ShipGroupListBean();
            }
            ApplicationMain.sysPrint("グループファイル更新");
            BeanUtils.writeObject(AppConstants.GROUP_CONFIG_FILE, group);
            modified = false;
        }
    }

    /**
     * 所有艦娘グループを読み込みます
     */
    private static void load() {
        try {
            ShipGroupListBean bean = BeanUtils.readObject(AppConstants.GROUP_CONFIG_FILE, ShipGroupListBean.class);
            if (bean != null) {
                group = bean;
            } else {
                group = new ShipGroupListBean();
            }
        } catch (Exception e) {
            LOG.get().warn("所有艦娘グループを読み込みますに失敗しました", e);
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
