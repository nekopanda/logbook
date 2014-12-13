package logbook.internal;

import java.util.Calendar;

import logbook.constants.AppConstants;

import org.apache.commons.lang3.time.DateUtils;

/**
 * 出撃統計の基準日
 *
 */
public enum BattleAggDate {

    /**
     * 今日
     */
    TODAY {
        @Override
        public Calendar get() {
            // 現在時刻から時分秒を切り捨て
            return DateUtils.truncate(now(), Calendar.DAY_OF_MONTH);
        }
    },
    /**
     * 先週
     */
    LAST_WEEK {
        @Override
        public Calendar get() {
            Calendar cal = DateUtils.truncate(now(), Calendar.DAY_OF_MONTH);
            cal.add(Calendar.WEEK_OF_YEAR, -1);
            return cal;
        }
    },
    /**
     * 先月
     */
    LAST_MONTH {
        @Override
        public Calendar get() {
            // 日付を1日にして現在時刻から時分秒を切り捨て
            Calendar cal = DateUtils.truncate(now(), Calendar.MONTH);
            cal.add(Calendar.MONTH, -1);
            return cal;
        }
    };

    /**
     * @return カレンダーを取得します
     */
    public Calendar get() {
        throw new AbstractMethodError();
    }

    /**
     * 任務の更新タイミングに合わせたカレンダーを取得します
     * 
     * @return 現在時刻
     */
    private static Calendar now() {
        // 日々の任務の更新は05:00
        Calendar cal = Calendar.getInstance(AppConstants.TIME_ZONE_MISSION);
        // 週次の任務の更新は月曜
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        return cal;
    }
}
