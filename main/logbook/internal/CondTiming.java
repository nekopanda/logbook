/**
 * 
 */
package logbook.internal;

import java.util.Date;

/**
 * 疲労回復時刻計算
 * @author Nekopanda
 */
public class CondTiming {
    private static long COND_CYCLE = 3 * 60 * 1000;
    private static long MAX_ERROR = 2 * 1000; // ±2秒までの誤差を許容する

    private boolean ignoreNextPort = false;
    private TimeSpan updateTiming = null;
    private Date from = new Date();

    public static class TimeSpan {
        private long offset;
        private long mills;

        public TimeSpan() {
            //
        }

        public TimeSpan(Date from, long mills) {
            this.offset = from.getTime() % COND_CYCLE;
            this.mills = mills;
        }

        public void intersection(Date ofrom, long omills) {
            if (omills >= COND_CYCLE) {
                return;
            }

            long from1 = this.offset;
            long to1 = this.offset + this.mills;
            long to2 = (((ofrom.getTime() + omills) - this.offset) % COND_CYCLE) + this.offset;
            long from2 = to2 - omills;
            long from = Math.max(from1, from2);
            long to = Math.min(to1, to2);

            if (from < to) { // 重なりがあるのでその部分で更新
                this.offset = from;
                this.mills = to - from;
            }
            else { // 重なりがないので新しいので更新
                   // 通信ラグなどの誤差を考慮して十分起こりうる精度の良くないデータは無視する
                long diff = Math.min(from2 - to1, (this.offset + COND_CYCLE) - to2);
                if ((diff > MAX_ERROR) || (omills <= this.mills)) {
                    this.offset = from2;
                    this.mills = omills;
                }
            }
        }

        // 次の回復までの残り時間
        public long getNext(Date from) {
            long mid = this.offset + (this.mills / 2);
            return COND_CYCLE - ((from.getTime() - mid) % COND_CYCLE);
        }

        /**
         * @return mills
         */
        public long getMills() {
            return this.mills;
        }

        /**
         * @return offset
         */
        public long getOffset() {
            return this.offset;
        }

        /**
         * @param offset セットする offset
         */
        public void setOffset(long offset) {
            this.offset = offset;
        }

        /**
         * @param mills セットする mills
         */
        public void setMills(long mills) {
            this.mills = mills;
        }
    }

    /**
     * 疲労が回復する時刻を計算
     * @param latestCond 最新データの疲労度
     * @param time 最新データの更新時間
     * @param targetCond 目標疲労度
     * @return 回復する時刻
     */
    public Date calcCondClearTime(int latestCond, int targetCond) {
        if (latestCond >= targetCond) {
            return null;
        }
        int requiredCycles = (int) Math.ceil((targetCond - latestCond) / 3.0);
        if (this.updateTiming == null) {
            return new Date((this.from.getTime() + (requiredCycles * COND_CYCLE)) - (COND_CYCLE / 2));
        }
        long next = this.updateTiming.getNext(this.from);
        return new Date(this.from.getTime() + next + ((requiredCycles - 1) * COND_CYCLE));
    }

    /**
     * 何回疲労回復タイミングが過ぎたか計算
     * @return 経過した疲労回復回数
     */
    public int calcPastCycles() {
        long pastMills = new Date().getTime() - this.from.getTime();
        if (this.updateTiming == null) {
            return (int) ((pastMills + (COND_CYCLE / 2)) / COND_CYCLE);
        }
        long next = this.updateTiming.getNext(this.from);
        if (pastMills < next) {
            return 0;
        }
        return (int) ((pastMills - next) / COND_CYCLE) + 1;
    }

    /**
     * 次の疲労回復タイミング
     * @param time
     * @return　不明な場合はnull
     */
    public Date getNextUpdateTime(Date time) {
        if (this.updateTiming == null) {
            return null;
        }
        long next = this.updateTiming.getNext(time);
        return new Date(time.getTime() + next);
    }

    /**
     * in milliseconds
     * @return
     */
    public long getCurrentAccuracy() {
        if (this.updateTiming == null) {
            return COND_CYCLE;
        }
        return this.updateTiming.getMills();
    }

    public void ignoreNext() {
        this.ignoreNextPort = true;
    }

    public void onPort(boolean updated) {
        if ((updated == false) || this.ignoreNextPort) {
            this.ignoreNextPort = false;
        }
        else {
            // 区間更新
            long mills = new Date().getTime() - this.from.getTime();
            if (this.updateTiming == null) {
                this.updateTiming = new TimeSpan(this.from, mills);
            }
            else {
                this.updateTiming.intersection(this.from, mills);
            }
        }
        // 起点を更新
        this.from = new Date();
    }

    /**
     * @return updateTiming
     */
    public TimeSpan getUpdateTiming() {
        return this.updateTiming;
    }

    /**
     * @param updateTiming セットする updateTiming
     */
    public void setUpdateTiming(TimeSpan updateTiming) {
        this.updateTiming = updateTiming;
    }
}
