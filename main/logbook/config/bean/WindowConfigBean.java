/**
 * 
 */
package logbook.config.bean;

/**
 * @author Nekopanda
 *
 */
public class WindowConfigBean {

    private int locationX;

    private int locationY;

    private int Width;

    private int Height;

    private boolean topMost = false;

    private boolean showTitlebar = true;

    /** マウスに反応するか */
    private boolean mouseHoveringAware;

    /** 設定を共有してたか */
    private boolean shareOpacitySetting;

    /** 透明度 */
    private int opacityIndex;

    /** 開いた状態だったか？ */
    private boolean opened;

    public WindowConfigBean() {
        this.locationX = -1;
        this.locationY = -1;
        this.Width = -1;
        this.Height = -1;
        this.mouseHoveringAware = false;
        this.shareOpacitySetting = false;
        this.opacityIndex = 0;
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

    /**
     * @return mouseHoveringAware
     */
    public boolean isMouseHoveringAware() {
        return this.mouseHoveringAware;
    }

    /**
     * @param mouseHoveringAware セットする mouseHoveringAware
     */
    public void setMouseHoveringAware(boolean mouseHoveringAware) {
        this.mouseHoveringAware = mouseHoveringAware;
    }

    /**
     * @return shareOpacitySetting
     */
    public boolean isShareOpacitySetting() {
        return this.shareOpacitySetting;
    }

    /**
     * @param shareOpacitySetting セットする shareOpacitySetting
     */
    public void setShareOpacitySetting(boolean shareOpacitySetting) {
        this.shareOpacitySetting = shareOpacitySetting;
    }

    /**
     * @return opacityIndex
     */
    public int getOpacityIndex() {
        return this.opacityIndex;
    }

    /**
     * @param opacityIndex セットする opacityIndex
     */
    public void setOpacityIndex(int opacityIndex) {
        this.opacityIndex = opacityIndex;
    }

    /**
     * @return width
     */
    public int getWidth() {
        return this.Width;
    }

    /**
     * @param width セットする width
     */
    public void setWidth(int width) {
        this.Width = width;
    }

    /**
     * @return height
     */
    public int getHeight() {
        return this.Height;
    }

    /**
     * @param height セットする height
     */
    public void setHeight(int height) {
        this.Height = height;
    }

    /**
     * @return topMost
     */
    public boolean isTopMost() {
        return this.topMost;
    }

    /**
     * @param topMost セットする topMost
     */
    public void setTopMost(boolean topMost) {
        this.topMost = topMost;
    }

    /**
     * @return showTitlebar
     */
    public boolean isShowTitlebar() {
        return this.showTitlebar;
    }

    /**
     * @param showTitlebar セットする showTitlebar
     */
    public void setShowTitlebar(boolean showTitlebar) {
        this.showTitlebar = showTitlebar;
    }
}
