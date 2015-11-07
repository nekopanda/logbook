package logbook.dto;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import logbook.constants.AppConstants;

import org.apache.commons.lang3.StringUtils;

/**
 * 海戦とドロップした艦娘を表します
 */
public class BattleResultDto extends AbstractDto {

    /** 日付 */
    private final Date battleDate;

    /** 海域名 */
    private final String questName;

    /** ランク */
    private final ResultRank rank;

    /** マス */
    private final MapCellDto mapCell;

    /** 敵艦隊名 */
    private final String enemyName;

    /** ドロップフラグ */
    private final boolean dropShip;

    /** ドロップフラグ */
    private final boolean dropItem;

    /** 艦種 */
    private final String dropType;

    /** 艦名 */
    private final String dropName;

    /** アイテム名 */
    private final String dropItemName;

    /** 母港の空きがない？ */
    private final boolean noSpaceForShip;

    /** スクリプトサポート */
    private final Comparable[] extData;

    public BattleResultDto(BattleExDto dto, Comparable[] extData) {
        this.battleDate = dto.getBattleDate();
        this.questName = dto.getQuestName();
        this.rank = dto.getRank();
        this.mapCell = dto.getMapCellDto();
        this.enemyName = dto.getEnemyName();
        this.dropShip = dto.isDropShip();
        this.dropItem = dto.isDropItem();
        this.dropType = dto.getDropType();
        this.dropName = dto.getDropName();
        this.dropItemName = dto.getDropItemName();
        this.noSpaceForShip = (dto.getExVersion() >= 1) && (dto.getShipSpace() == 0);
        this.extData = extData;
    }

    private boolean hasTaihaInFleet(int[] nowhp, int[] maxhp) {
        if ((nowhp == null) || (maxhp == null)) {
            return false;
        }
        for (int i = 0; i < nowhp.length; ++i) {
            double rate = (double) nowhp[i] / (double) maxhp[i];
            if (rate <= AppConstants.BADLY_DAMAGE) {
                return true;
            }
        }
        return false;
    }

    /**
     * 日付を取得します。
     * @return 日付
     */
    public Date getBattleDate() {
        return this.battleDate;
    }

    /**
     * 海域名を取得します。
     * @return 海域名
     */
    public String getQuestName() {
        return this.questName;
    }

    public boolean isPractice() {
        return (this.questName == null);
    }

    /**
     * ランクを取得します。
     * @return ランク
     */
    public ResultRank getRank() {
        return this.rank;
    }

    /**
     * マスを取得します。
     * @return マス
     */
    public MapCellDto getMapCell() {
        return this.mapCell;
    }

    /**
     * 出撃を取得します
     * @return 出撃
     */
    public boolean isStart() {
        return (this.mapCell != null) ? this.mapCell.isStart() : false;
    }

    /**
     * ボスマスを取得します
     * @return ボスマス
     */
    public boolean isBoss() {
        return (this.mapCell != null) ? this.mapCell.isBoss() : false;
    }

    /**
     * 出撃・ボステキストを取得します
     * @return 出撃・ボステキスト
     */
    public String getBossText() {
        if (this.isStart() || this.isBoss()) {
            List<String> list = new ArrayList<>();
            if (this.isStart()) {
                list.add("出撃");
            }
            if (this.isBoss()) {
                list.add("ボス");
            }
            return StringUtils.join(list, "&");
        }
        return "";
    }

    /**
     * 敵艦隊名を取得します。
     * @return 敵艦隊名
     */
    public String getEnemyName() {
        return this.enemyName;
    }

    /**
     * ドロップフラグを取得します。
     * @return ドロップフラグ
     */
    public boolean isDropShip() {
        return this.dropShip;
    }

    /**
     * ドロップフラグを取得します。
     * @return ドロップフラグ
     */
    public boolean isDropItem() {
        return this.dropItem;
    }

    /**
     * 艦種を取得します。
     * @return 艦種
     */
    public String getDropType() {
        return this.dropType;
    }

    /**
     * 艦名を取得します。
     * @return 艦名
     */
    public String getDropName() {
        return this.dropName;
    }

    /**
     * 表示するドロップ艦名
     * @return 艦名
     */
    public String getScreenDropName() {
        if (StringUtils.isEmpty(this.dropName) && this.noSpaceForShip) {
            return "※空きなし";
        }
        return this.dropName;
    }

    /**
     * アイテム名を取得します。
     * @return 艦名
     */
    public String getDropItemName() {
        return this.dropItemName;
    }

    /**
     * @return noSpaceForShip
     */
    public boolean isNoSpaceForShip() {
        return this.noSpaceForShip;
    }

    /**
     * @return extData
     */
    public Comparable[] getExtData() {
        return this.extData;
    }
}