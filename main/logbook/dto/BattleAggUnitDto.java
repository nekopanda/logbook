package logbook.dto;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * 集計結果
 */
public final class BattleAggUnitDto extends AbstractDto {

    /**
     * 海域合算
     */
    private final BattleAggDetailsDto total = new BattleAggDetailsDto();

    /**
     * 海域毎
     */
    private final Map<String, BattleAggDetailsDto> areaDetails = new TreeMap<>();

    /**
     * 評価を集計する
     * 
     * @param area 海域名
     * @param rank 評価
     * @param isStart 出撃
     * @param isBoss ボス
     */
    public void add(String area, String rank, boolean isStart, boolean isBoss) {
        // 合算に評価を加算
        this.total.add(rank, isStart, isBoss);
        // 海域毎に評価を加算
        BattleAggDetailsDto areaAgg = this.areaDetails.get(area);
        if (areaAgg == null) {
            areaAgg = new BattleAggDetailsDto();
            this.areaDetails.put(area, areaAgg);
        }
        areaAgg.add(rank, isStart, isBoss);
    }

    /**
     * 集計結果を取得します
     * 
     * @return 集計結果
     */
    public BattleAggDetailsDto getTotal() {
        return this.total;
    }

    /**
     * 海域毎の集計結果を取得します
     * 
     * @return 海域毎の集計結果
     */
    public Set<Entry<String, BattleAggDetailsDto>> getAreaDetails() {
        return this.areaDetails.entrySet();
    }
}
