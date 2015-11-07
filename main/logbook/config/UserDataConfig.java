package logbook.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.dto.DeckMissionDto;
import logbook.dto.ItemDto;
import logbook.internal.CondTiming;
import logbook.internal.LoggerHolder;
import logbook.util.BeanUtils;

/**
 * ユーザーゲームデータを保存・復元します
 * 
 */
public class UserDataConfig {
    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(UserDataConfig.class);

    private List<ItemDto> items;

    private DeckMissionDto[] previousMissions;

    private CondTiming.TimeSpan condTiming;

    private Date akashiStartTime;

    /**
     * ユーザーゲームデータをファイルに書き込みます
     */
    public static void store() throws IOException {
        UserDataConfig config = new UserDataConfig();
        config.items = new ArrayList<ItemDto>(GlobalContext.getItemMap().values());
        config.previousMissions = GlobalContext.getPreviousMissions();
        config.condTiming = GlobalContext.getCondTiming().getUpdateTiming();
        config.akashiStartTime = GlobalContext.getAkashiTimer().getStartTime();
        BeanUtils.writeObject(AppConstants.USER_DATA_CONFIG, config);
    }

    /**
     * ユーザーゲームデータをファイルから読み込みます
     * 
     * @param properties
     * @return
     */
    public static void load() {
        try {
            UserDataConfig config = BeanUtils.readObject(AppConstants.USER_DATA_CONFIG, UserDataConfig.class);
            if (config != null) {
                GlobalContext.load(config);
            }
        } catch (Exception e) {
            LOG.get().warn("ユーザーゲームデータファイルから読み込みますに失敗しました", e);
        }
    }

    /**
     * @return items
     */
    public List<ItemDto> getItems() {
        return this.items;
    }

    /**
     * @param items セットする items
     */
    public void setItems(List<ItemDto> items) {
        this.items = items;
    }

    /**
     * @return previousMissions
     */
    public DeckMissionDto[] getPreviousMissions() {
        return this.previousMissions;
    }

    /**
     * @param previousMissions セットする previousMissions
     */
    public void setPreviousMissions(DeckMissionDto[] previousMissions) {
        this.previousMissions = previousMissions;
    }

    /**
     * @return condTiming
     */
    public CondTiming.TimeSpan getCondTiming() {
        return this.condTiming;
    }

    /**
     * @param condTiming セットする condTiming
     */
    public void setCondTiming(CondTiming.TimeSpan condTiming) {
        this.condTiming = condTiming;
    }

    /**
     * @return akashiStartTime
     */
    public Date getAkashiStartTime() {
        return akashiStartTime;
    }

    /**
     * @param akashiStartTime セットする akashiStartTime
     */
    public void setAkashiStartTime(Date akashiStartTime) {
        this.akashiStartTime = akashiStartTime;
    }
}
