/**
 * 
 */
package logbook.internal;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import logbook.dto.DockDto;
import logbook.dto.ShipDto;

/**
 * 泊地修理タイマー
 * @author Nekopanda
 */
public class AkashiTimer {

    public static final long MINIMUM_TIME = 20 * 60 * 1000;
    public static final long REPAIR_BASE = 30 * 1000;
    public static final long AKASHI_DELAY = 30 * 1000;

    private Date startTime = new Date();

    /** shipId -> HP */
    private final Map<Integer, ShipState> stateMap = new TreeMap<>();

    public static class RepairState {
        ShipState[] ships;
        long elapsed;
        boolean firstNotify;

        RepairState(ShipState[] ships, long elapsed, boolean firstNotify) {
            this.ships = ships;
            this.elapsed = elapsed;
            this.firstNotify = firstNotify;
        }

        /**
         * 泊地修理中か？
         * @return
         */
        public boolean isRepairing() {
            return this.ships != null;
        }

        public List<ShipState> get() {
            if (this.ships == null) {
                throw new IllegalStateException("泊地修理中ではありません");
            }
            return Arrays.asList(this.ships);
        }

        /**
         * 今回の更新で最初の20分が経過したか？
         * @return
         */
        public boolean isFirstNotify() {
            return this.firstNotify;
        }

        /**
         * @return elapsed
         */
        public long getElapsed() {
            return this.elapsed;
        }
    }

    public static class ShipState {
        ShipDto ship;
        int currentGain;
        long next;
        Date finish;
        long elapsed;
        boolean stepNotify;

        /**
         * 母港更新すると回復するHP
         * @return 
         */
        public int getCurrentGain() {
            return this.currentGain;
        }

        /**
         * 次の回復ポイントまでの時間
         * @return 
         */
        public long getNext() {
            return this.next;
        }

        /**
         * 全回復する時間
         * @return 
         */
        public Date getFinish() {
            return this.finish;
        }

        /**
         * 今回の更新で回復したか
         * @return 
         */
        public boolean isStepNotify() {
            return this.stepNotify;
        }

        /**
         * @return ship
         */
        public ShipDto getShip() {
            return this.ship;
        }

        /**
         * @param ship セットする ship
         */
        public void setShip(ShipDto ship) {
            this.ship = ship;
        }
    }

    public void reset() {
        this.startTime = new Date();
        this.stateMap.clear();
    }

    /** 状態を更新 */
    public RepairState update(DockDto dock, Date now) {
        int akashiCapacity = dock.getAkashiCapacity();
        if (akashiCapacity == 0) {
            return new RepairState(null, 0, false); // ショートカット
        }
        List<ShipDto> ships = dock.getShips();
        ShipState[] states = new ShipState[ships.size()];
        boolean repairing = false;
        boolean firstNotify = false;
        long start = this.startTime.getTime();
        long elapsed = now.getTime() - start;

        for (int p = 0; p < ships.size(); ++p) {
            ShipDto ship = ships.get(p);
            if ((p < akashiCapacity) && !ship.isHalfDamage() && (ship.getNowhp() != ship.getMaxhp())) {
                repairing = true;

                ShipState state = this.stateMap.get(ship.getShipId());
                if (state == null) {
                    state = new ShipState();
                }

                int nowhp = ship.getNowhp();
                int maxhp = ship.getMaxhp();
                long docktime = ship.getDocktime();
                // 修理時間は30秒足されている.素の修理時間からHP1あたりを算出
                long time1pt = (docktime - REPAIR_BASE) / (maxhp - nowhp);
                // 修理時間で30秒+泊地修理遅延で30秒
                long rawAkashiTime = (time1pt * (maxhp - nowhp)) + REPAIR_BASE + AKASHI_DELAY;
                // 本当に必要な時間
                long requiredTime = Math.max(MINIMUM_TIME, rawAkashiTime);

                int gain; // 現在まで回復したポイント
                long next; // 次の回復までの時間
                if (elapsed < MINIMUM_TIME) {
                    gain = 0;
                    next = MINIMUM_TIME - elapsed;
                }
                else {
                    // 実質的な修理経過時間
                    long validTime = elapsed - (REPAIR_BASE + AKASHI_DELAY);
                    gain = (int) (validTime / time1pt);
                    if (gain == 0) { // 20分は経過しているので最低1は回復している
                        gain = 1;
                    }
                    next = (time1pt * (gain + 1)) - validTime;

                    if (state.elapsed < MINIMUM_TIME) {
                        firstNotify = true;
                    }
                }

                int damage = ship.getMaxhp() - ship.getNowhp();
                if (gain > damage) {
                    // MAX以上は回復しない
                    gain = damage;
                }

                boolean stepNotify = (state.currentGain != gain);

                state.setShip(ship);
                state.currentGain = gain;
                state.next = next;
                state.finish = new Date(start + requiredTime);
                state.elapsed = elapsed;
                state.stepNotify = stepNotify;
                states[p] = state;
                this.stateMap.put(ship.getShipId(), state);
            }
        }
        return new RepairState(repairing ? states : null, elapsed, firstNotify);
    }
}
