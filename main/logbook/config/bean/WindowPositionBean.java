/**
 * 
 */
package logbook.config.bean;

/**
 * @author Nekopanda
 *
 */
public class WindowPositionBean {

    private int locationX;

    private int locationY;

    /** 開いた状態だったか？ */
    private boolean opened;

    public WindowPositionBean() {
        this.locationX = -1;
        this.locationY = -1;
        this.opened = false;
    }

    /**
     * @return locationX
     */
    public int getLocationX() {
        return this.locationX;
    }

    /**
     * @param locationX セットする locationX
     */
    public void setLocationX(int locationX) {
        this.locationX = locationX;
    }

    /**
     * @return locationY
     */
    public int getLocationY() {
        return this.locationY;
    }

    /**
     * @param locationY セットする locationY
     */
    public void setLocationY(int locationY) {
        this.locationY = locationY;
    }

    /**
     * @return opened
     */
    public boolean isOpened() {
        return this.opened;
    }

    /**
     * @param opened セットする opened
     */
    public void setOpened(boolean opened) {
        this.opened = opened;
    }
}
