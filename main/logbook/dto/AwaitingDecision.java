package logbook.dto;

/**
 * 建造した艦娘のIDとドッグIDを表す
 *
 */
public final class AwaitingDecision extends AbstractDto {

    private final int shipid;

    private final String dock;

    public AwaitingDecision(int shipid, String dock) {

        this.shipid = shipid;
        this.dock = dock;
    }

    /**
     * @return shipid
     */
    public int getShipid() {
        return this.shipid;
    }

    /**
     * @return dock
     */
    public String getDock() {
        return this.dock;
    }

}
