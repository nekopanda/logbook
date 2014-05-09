package logbook.dto;

/**
 * 建造した艦娘のIDとドッグIDを表す
 *
 */
public final class AwaitingDecision extends AbstractDto {

    private final long shipid;

    private final String dock;

    public AwaitingDecision(long shipid, String dock) {

        this.shipid = shipid;
        this.dock = dock;
    }

    /**
     * @return shipid
     */
    public long getShipid() {
        return this.shipid;
    }

    /**
     * @return dock
     */
    public String getDock() {
        return this.dock;
    }

}
