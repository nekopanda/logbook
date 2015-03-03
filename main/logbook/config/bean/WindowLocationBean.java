package logbook.config.bean;

/**
 * ウインドウ位置とサイズを保存します
 *
 */
public final class WindowLocationBean {

    /** x */
    private int x;

    /** y */
    private int y;

    /** width */
    private int width;

    /** height */
    private int height;

    /**
     * xを取得します。
     * @return x
     */
    public int getX() {
        return this.x;
    }

    /**
     * xを設定します。
     * @param x x
     */
    public void setX(int x) {
        this.x = x;
    }

    /**
     * yを取得します。
     * @return y
     */
    public int getY() {
        return this.y;
    }

    /**
     * yを設定します。
     * @param y y
     */
    public void setY(int y) {
        this.y = y;
    }

    /**
     * widthを取得します。
     * @return width
     */
    public int getWidth() {
        return this.width;
    }

    /**
     * widthを設定します。
     * @param width width
     */
    public void setWidth(int width) {
        this.width = width;
    }

    /**
     * heightを取得します。
     * @return height
     */
    public int getHeight() {
        return this.height;
    }

    /**
     * heightを設定します。
     * @param height height
     */
    public void setHeight(int height) {
        this.height = height;
    }
}
