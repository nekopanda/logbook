/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.gui.logic;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;

/**
 * 画像操作
 *
 */
public class AwtUtils {

    private static final int WHITE = Color.WHITE.getRGB();

    /**
     * <p>
     * スクリーンショットをrectangleで指定した領域で取得します<br>
     * 失敗した場合nullを返します
     * </p>
     * 
     * @param rectangle
     * @return スクリーンショット
     */
    public static BufferedImage getCapture(org.eclipse.swt.graphics.Rectangle rectangle) {
        return getCapture(new Rectangle(rectangle.x, rectangle.y, rectangle.width, rectangle.height));
    }

    /**
     * <p>
     * スクリーンショットをrectangleで指定した領域で取得します<br>
     * 失敗した場合nullを返します
     * </p>
     * 
     * @param rectangle
     * @return スクリーンショット
     */
    public static BufferedImage getCapture(Rectangle rectangle) {
        try {
            Robot robot = new Robot();
            return robot.createScreenCapture(rectangle);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * イメージを簡易トリムします
     * 
     * @param image
     * @return
     */
    public static BufferedImage trim(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int startwidth = width / 2;
        int startheight = height / 2;

        int x = 0;
        int y = 0;
        int w = 0;
        int h = 0;

        for (int i = 0; i < width; i++) {
            if (image.getRGB(i, startheight) != WHITE) {
                x = i;
                break;
            }
        }
        for (int i = 0; i < height; i++) {
            if (image.getRGB(startwidth, i) != WHITE) {
                y = i;
                break;
            }
        }
        for (int i = width - 1; i >= 0; i--) {
            if (image.getRGB(i, startheight) != WHITE) {
                w = (i - x) + 1;
                break;
            }
        }
        for (int i = height - 1; i >= 0; i--) {
            if (image.getRGB(startwidth, i) != WHITE) {
                h = (i - y) + 1;
                break;
            }
        }
        if ((w == 0) || (h == 0)) {
            return image;
        } else {
            return image.getSubimage(x, y, w, h);
        }
    }
}
