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
    private final BattleAggDetailsDto total = new BattleAggDetailsDto("すべて");

    /**
     * 海域毎
     */
    private final Map<Integer, BattleAggDetailsDto> areaDetails = new TreeMap<>();

    /**
     * 評価を集計する
     * 
     * @param area 海域名
     * @param rank 評価
     * @param isBoss ボス
     */
    public void add(MapCellDto area, ResultRank rank) {
        // 合算に評価を加算
        this.total.add(rank, area.isBoss());
        // 海域毎に評価を加算
        BattleAggDetailsDto areaAgg = this.areaDetails.get(area.getAreaId());
        if (areaAgg == null) {
            areaAgg = new BattleAggDetailsDto(area.getAreaString());
            this.areaDetails.put(area.getAreaId(), areaAgg);
        }
        areaAgg.add(rank, area.isBoss());
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
    public Set<Entry<Integer, BattleAggDetailsDto>> getAreaDetails() {
        return this.areaDetails.entrySet();
    }
}
