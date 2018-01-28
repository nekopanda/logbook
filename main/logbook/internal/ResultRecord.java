package logbook.internal;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * 戦果
 *
 * @author Nishisonic
 */
public class ResultRecord {

    /**
     * タイムゾーン
     */
    private static final ZoneId TIME_ZONE = ZoneId.of("Asia/Tokyo");

    /**
     * 最後に艦これを読み込んだ時刻(ZonedDateTimeでは保存が出来ないため、Stringで代用)
     */
    private String lastUpdateTimeString;

    /**
     * 半日開始時点での提督経験値
     */
    private long admiralExpAtBeggingOfHalfDay;

    /**
     * 一日開始時点での提督経験値
     */
    private long admiralExpAtBeggingOfDay;

    /**
     * 一月開始時点での提督経験値
     */
    private long admiralExpAtBeggingOfMonth;

    /**
     * 現在の提督経験値
     */
    private long nowAdmiralExp;

    /**
     * コンストラクタ
     */
    public ResultRecord() {
        this.reset();
    }

    /**
     * 状態を更新
     */
    public void update(long nowAdmiralExp) {
        ZonedDateTime nowTime = ZonedDateTime.now(TIME_ZONE);
        if(Objects.nonNull(lastUpdateTimeString)){
            // 分以下は邪魔なため、0に整える
            ZonedDateTime border = ZonedDateTime.parse(lastUpdateTimeString).withMinute(0).withSecond(0).withNano(0);
            // 現在時刻が最後に艦これのデータを読み込んだ時刻の戦果締切時刻を過ぎていないか調べる
            // 半日戦果:2時と14時時点
            if (border.plusHours(12 - ((10 + border.getHour()) % 12)).isBefore(nowTime)) {
                this.admiralExpAtBeggingOfHalfDay = nowAdmiralExp;
            }
            // 一日戦果:5時時点
            if (border.plusHours(24 - ((19 + border.getHour()) % 24)).isBefore(nowTime)) {
                this.admiralExpAtBeggingOfDay = nowAdmiralExp;
            }
            // 一月戦果:最終日22時時点
            if (border.withDayOfMonth(border.toLocalDate().lengthOfMonth()).withHour(22).isBefore(nowTime)) {
                this.admiralExpAtBeggingOfMonth = nowAdmiralExp;
            }
        } else {
            this.admiralExpAtBeggingOfHalfDay = this.admiralExpAtBeggingOfDay = this.admiralExpAtBeggingOfMonth = nowAdmiralExp;
        }
        this.nowAdmiralExp = nowAdmiralExp;
        this.lastUpdateTimeString = nowTime.toString();
    }

    /**
     * リセット
     */
    public void reset() {
        this.lastUpdateTimeString = null;
        this.admiralExpAtBeggingOfHalfDay = 0;
        this.admiralExpAtBeggingOfDay = 0;
        this.admiralExpAtBeggingOfMonth = 0;
        this.nowAdmiralExp = 0;
    }

    /**
     * 半日で獲得した提督経験値の量
     */
    public long getAcquiredAdmiralExpOfHalfDay() {
        return this.nowAdmiralExp - this.admiralExpAtBeggingOfHalfDay;
    }

    /**
     * 一日で獲得した提督経験値の量
     */
    public long getAcquiredAdmiralExpOfDay() {
        return this.nowAdmiralExp - this.admiralExpAtBeggingOfDay;
    }

    /**
     * 一月で獲得した提督経験値の量
     */
    public long getAcquiredAdmiralExpOfMonth() {
        return this.nowAdmiralExp - this.admiralExpAtBeggingOfMonth;
    }

    /**
     * 半日戦果
     */
    public double getAcquiredValueOfHalfDay() {
        return this.getAcquiredAdmiralExpOfHalfDay() * 7 / 10000.0;
    }

    /**
     * 一日戦果
     */
    public double getAcquiredValueOfDay() {
        return this.getAcquiredAdmiralExpOfDay() * 7 / 10000.0;
    }

    /**
     * 一月戦果
     */
    public double getAcquiredValueOfMonth() {
        return this.getAcquiredAdmiralExpOfMonth() * 7 / 10000.0;
    }

    // 保存用 //

    public String getLastUpdateTimeString() {
        return lastUpdateTimeString;
    }

    public void setLastUpdateTimeString(String lastUpdateTimeString) {
        this.lastUpdateTimeString = lastUpdateTimeString;
    }

    public long getAdmiralExpAtBeggingOfHalfDay() {
        return admiralExpAtBeggingOfHalfDay;
    }

    public void setAdmiralExpAtBeggingOfHalfDay(long admiralExpAtBeggingOfHalfDay) {
        this.admiralExpAtBeggingOfHalfDay = admiralExpAtBeggingOfHalfDay;
    }

    public long getAdmiralExpAtBeggingOfDay() {
        return admiralExpAtBeggingOfDay;
    }

    public void setAdmiralExpAtBeggingOfDay(long admiralExpAtBeggingOfDay) {
        this.admiralExpAtBeggingOfDay = admiralExpAtBeggingOfDay;
    }

    public long getAdmiralExpAtBeggingOfMonth() {
        return admiralExpAtBeggingOfMonth;
    }

    public void setAdmiralExpAtBeggingOfMonth(long admiralExpAtBeggingOfMonth) {
        this.admiralExpAtBeggingOfMonth = admiralExpAtBeggingOfMonth;
    }


    /**
     * 現在の提督経験値
     */
    public long getNowAdmiralExp() {
        return nowAdmiralExp;
    }

    public void setNowAdmiralExp(long nowAdmiralExp) {
        this.nowAdmiralExp = nowAdmiralExp;
    }
}
