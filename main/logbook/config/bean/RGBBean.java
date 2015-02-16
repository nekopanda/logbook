/**
 * 
 */
package logbook.config.bean;

import org.eclipse.swt.graphics.RGB;

/**
 * @author Nekopanda
 *
 */
public class RGBBean {
    private int red = 1;
    private int green = 1;
    private int blue = 1;

    public RGBBean() {
        //
    }

    public RGBBean(int red, int green, int blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
    }

    public RGB toRGB() {
        return new RGB(this.red, this.green, this.blue);
    }

    public static RGBBean fromRGB(RGB rgb) {
        return new RGBBean(rgb.red, rgb.green, rgb.blue);
    }

    /**
     * @return red
     */
    public int getRed() {
        return this.red;
    }

    /**
     * @param red セットする red
     */
    public void setRed(int red) {
        this.red = red;
    }

    /**
     * @return green
     */
    public int getGreen() {
        return this.green;
    }

    /**
     * @param green セットする green
     */
    public void setGreen(int green) {
        this.green = green;
    }

    /**
     * @return blue
     */
    public int getBlue() {
        return this.blue;
    }

    /**
     * @param blue セットする blue
     */
    public void setBlue(int blue) {
        this.blue = blue;
    }
}
