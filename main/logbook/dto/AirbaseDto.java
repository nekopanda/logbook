package logbook.dto;

import logbook.config.AppConfig;
import logbook.data.context.GlobalContext;

import javax.json.JsonArray;
import javax.json.JsonObject;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class AirbaseDto {

    private Map<Integer, Map<Integer, AirCorpsDto>> airbase;

    public AirbaseDto(JsonArray json) {
        this.airbase = json.stream()
                .map(JsonObject.class::cast)
                .collect(Collectors.groupingBy(api -> Integer.parseInt(api.get("api_area_id").toString())))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().stream()
                        .map(api -> new AirCorpsDto(api))
                        .collect(Collectors.toMap(a -> a.getRid(), a -> a))));
    }

    public Map<Integer, Map<Integer, AirCorpsDto>> get() {
        return airbase;
    }

    public void set(Map<Integer, Map<Integer, AirCorpsDto>> airbase) {
        this.airbase = airbase;
    }

    /**
     * 航空隊
     */
    public static class AirCorpsDto {

        private static int[][] SKILLED_BONUS_TABLE = {
                {0, 0, 2, 5, 9, 14, 14, 22}, // 艦上戦闘機、水上戦闘機、夜間戦闘機
                {0, 0, 0, 0, 0, 0, 0, 0}, // 艦上爆撃機、艦上攻撃機、噴式戦闘爆撃機
                {0, 0, 1, 1, 1, 3, 3, 6}, // 水上爆撃機
        };

        private static int[] INTERNAL_SKILLED_TABLE = {0, 10, 25, 40, 55, 70, 85, 100, 121};

        private int rid;
        private String name;
        private int distance;
        private int actionKind;
        private Map<Integer, SquadronDto> squadrons;

        public AirCorpsDto(JsonObject json) {
            this(
                    Integer.parseInt(json.get("api_rid").toString()),
                    json.get("api_name").toString(),
                    Integer.parseInt(json.get("api_distance").toString()),
                    Integer.parseInt(json.get("api_action_kind").toString()),
                    ((JsonArray) json.get("api_plane_info")).stream()
                            .map(JsonObject.class::cast)
                            .map(SquadronDto::new)
                            .collect(Collectors.toMap(k -> k.getId(), v -> v))
            );
        }

        public AirCorpsDto(int rid, String name, int distance, int actionKind, Map<Integer, SquadronDto> squadrons) {
            this.rid = rid;
            this.name = name;
            this.distance = distance;
            this.actionKind = actionKind;
            this.squadrons = new HashMap<>();
            this.squadrons.putAll(squadrons);
        }

        public void setPlane(JsonObject json) {
            this.distance = Integer.parseInt(json.get("api_distance").toString());
            this.supply(json);
        }

        public void supply(JsonObject json) {
            this.squadrons.putAll(((JsonArray) json.get("api_plane_info")).stream()
                    .map(JsonObject.class::cast)
                    .map(SquadronDto::new)
                    .collect(Collectors.toMap(k -> k.getId(), v -> v)));
        }

        public int getRid() {
            return this.rid;
        }

        public void setRid(int rid) {
            this.rid = rid;
        }

        public String getName() {
            return this.name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getDistance() {
            return this.distance;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }

        public int getActionKind() {
            return this.actionKind;
        }

        public void setActionKind(int actionKind) {
            this.actionKind = actionKind;
        }

        public String getActionKindString() {
            switch (this.actionKind) {
                case 0:
                    return "待機";
                case 1:
                    return "出撃";
                case 2:
                    return "防空";
                case 3:
                    return "退避";
                case 4:
                    return "休息";
                default:
                    return "不明";
            }
        }

        public Map<Integer, SquadronDto> getSquadrons() {
            return this.squadrons;
        }

        public void setSquadrons(Map<Integer, SquadronDto> squadrons) {
            this.squadrons = squadrons;
        }

        public AirPower getAirPower() {
            int method = AppConfig.get().getSeikuMethod();
            AirPower power = squadrons.values().stream().map(squadron -> {
                ItemDto item = GlobalContext.getItem(squadron.slotid);
                int constSkilledBonus;
                int[] skilledBonus;
                double base;
                switch (item.getType2()) {
                    case 6: // 艦上戦闘機、夜間戦闘機
                    case 45: // 水上戦闘機
//                case 56: // 噴式戦闘機
                        base = (item.getParam().getTaiku() + 0.2 * item.getLevel()) * Math.sqrt(squadron.count);
                        constSkilledBonus = SKILLED_BONUS_TABLE[0][item.getAlv()];
                        skilledBonus = new int[]{INTERNAL_SKILLED_TABLE[item.getAlv()], INTERNAL_SKILLED_TABLE[item.getAlv() + 1] - 1};
                        break;
                    case 48: // 局地戦闘機
                        if (this.getActionKind() == 2) {
                            // 防空
                            base = (item.getParam().getTaiku() + item.getParam().getKaihi() + 2 * item.getParam().getHoum() + 0.2 * item.getLevel()) * Math.sqrt(squadron.count);
                        } else {
                            // 出撃
                            base = (item.getParam().getTaiku() + 1.5 * item.getParam().getKaihi() + 0.2 * item.getLevel()) * Math.sqrt(squadron.count);
                        }
                        constSkilledBonus = SKILLED_BONUS_TABLE[0][item.getAlv()];
                        skilledBonus = new int[]{INTERNAL_SKILLED_TABLE[item.getAlv()], INTERNAL_SKILLED_TABLE[item.getAlv() + 1] - 1};
                        break;
                    case 7: // 艦上爆撃機、夜間爆撃機?
                        base = (item.getParam().getTaiku() + 0.25 * item.getLevel()) * Math.sqrt(squadron.count);
                        constSkilledBonus = SKILLED_BONUS_TABLE[1][item.getAlv()];
                        skilledBonus = new int[]{INTERNAL_SKILLED_TABLE[item.getAlv()], INTERNAL_SKILLED_TABLE[item.getAlv() + 1] - 1};
                        break;
                    case 8: // 艦上攻撃機、夜間攻撃機
                    case 47: // 陸上攻撃機
                    case 57: // 噴式戦闘爆撃機
//                case 58: // 噴式攻撃機
                        base = item.getParam().getTaiku() * Math.sqrt(squadron.count);
                        constSkilledBonus = SKILLED_BONUS_TABLE[1][item.getAlv()];
                        skilledBonus = new int[]{INTERNAL_SKILLED_TABLE[item.getAlv()], INTERNAL_SKILLED_TABLE[item.getAlv() + 1] - 1};
                        break;
                    case 11: // 水上爆撃機
                        base = item.getParam().getTaiku() * Math.sqrt(squadron.count);
                        constSkilledBonus = SKILLED_BONUS_TABLE[2][item.getAlv()];
                        skilledBonus = new int[]{INTERNAL_SKILLED_TABLE[item.getAlv()], INTERNAL_SKILLED_TABLE[item.getAlv() + 1] - 1};
                        break;
                    default: // 偵察機etc.
                        base = item.getParam().getTaiku() * Math.sqrt(squadron.count);
                        constSkilledBonus = SKILLED_BONUS_TABLE[1][item.getAlv()];
                        skilledBonus = new int[]{INTERNAL_SKILLED_TABLE[item.getAlv()], INTERNAL_SKILLED_TABLE[item.getAlv() + 1] - 1};
                        break;
                }
                int min = (int) Math.floor(base + constSkilledBonus + Math.sqrt(skilledBonus[0] / 10));
                int max = (int) Math.floor(base + constSkilledBonus + Math.sqrt(skilledBonus[1] / 10));
                if (method == 1 || method == 2 || method == 3 || method == 4) {
                    // 熟練度付き制空値
                    return new AirPower(min, max);
                } else {
                    // 素の制空値
                    return new AirPower((int) base);
                }
            }).reduce(new AirPower(0), (p, v) -> {
                p.add(v);
                return p;
            });
            // 防空:偵察機補正(100倍することで丸め誤差をなくしている)
            if (this.actionKind == 2) {
                int defenseBonus = squadrons.values().stream().map(squadron -> GlobalContext.getItem(squadron.slotid)).mapToInt(item -> {
                    int search = item.getParam().getSakuteki();
                    switch (item.getType2()) {
                        case 9: // 艦上偵察機
                        case 94: // 艦上偵察機(II)
//                      case 59: // 噴式偵察機
                            if (search >= 9) return 130;
//                          if (search == 8) return 125; // 適当
                            return 120;
                        case 10: // 水上偵察機
                        case 41: // 大型飛行艇
                            if (search >= 9) return 116;
                            if (search == 8) return 113;
                            return 110;
                    }
                    return 100;
                }).max().orElse(100);
                power.setMin(power.getMin() * defenseBonus / 100);
                power.setMax(power.getMax() * defenseBonus / 100);
            }
            return power;
        }

        public class AirPower {

            private int min;
            private int max;

            AirPower(int mid) {
                this(mid, mid);
            }

            AirPower(int min, int max) {
                this.min = min;
                this.max = max;
            }

            public void add(AirPower air) {
                this.add(air.min, air.max);
            }

            public void add(int min, int max) {
                this.addMin(min);
                this.addMax(max);
            }

            public void addMin(int min) {
                this.min += min;
            }

            public void addMax(int max) {
                this.max += max;
            }

            public int getMin() {
                return min;
            }

            public void setMin(int min) {
                this.min = min;
            }

            public int getMax() {
                return max;
            }

            public void setMax(int max) {
                this.max = max;
            }

            @Override
            public String toString() {
                int method = AppConfig.get().getSeikuMethod();
                switch (method) {
                    case 0: // 艦載機素の制空値
                        return Integer.toString(this.max);
                    case 1: // 制空推定値範囲
                    case 2: // 制空推定値範囲(艦載機素の制空値 + 熟練度ボーナス推定値)
                        return this.min == this.max ? Integer.toString(this.max) : this.min + "-" + this.max;
                    case 3: // 制空推定値中央
                    case 4: // 制空推定値中央(艦載機素の制空値 + 熟練度ボーナス推定値)
                        return Integer.toString((this.min + this.max) / 2);
                }
                return Integer.toString(this.max);
            }
        }

        /**
         * 中隊
         */
        public static class SquadronDto {

            private int id;
            private int state;
            private int slotid;
            private int count;
            private int maxCount;
            private int cond;

            SquadronDto(JsonObject json) {
                this(
                        Integer.parseInt(json.get("api_squadron_id").toString()),
                        Integer.parseInt(json.get("api_state").toString()),
                        Integer.parseInt(json.get("api_slotid").toString()),
                        Integer.parseInt(json.get("api_count").toString()),
                        Integer.parseInt(json.get("api_max_count").toString()),
                        Integer.parseInt(json.get("api_cond").toString())
                );
            }

            SquadronDto(int id, int state, int slotid, int count, int maxCount, int cond) {
                this.id = id;
                this.state = state;
                this.slotid = slotid;
                this.count = count;
                this.maxCount = maxCount;
                this.cond = cond;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public int getState() {
                return state;
            }

            public void setState(int state) {
                this.state = state;
            }

            public int getSlotid() {
                return slotid;
            }

            public void setSlotid(int slotid) {
                this.slotid = slotid;
            }

            public int getCount() {
                return count;
            }

            public void setCount(int count) {
                this.count = count;
            }

            public int getMaxCount() {
                return maxCount;
            }

            public void setMaxCount(int maxCount) {
                this.maxCount = maxCount;
            }

            public int getCond() {
                return cond;
            }

            public void setCond(int cond) {
                this.cond = cond;
            }
        }
    }
}
