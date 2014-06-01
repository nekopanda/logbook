package logbook.util;

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
    public static BufferedImage trim(BufferedImage image, Rectangle trimRect) {
        return image.getSubimage(trimRect.x, trimRect.y, trimRect.width, trimRect.height);
    }

    /**
     * トリムサイズを返します
     * 
     * @param image
     * @return
     */
    public static Rectangle getTrimSize(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        int startwidth = width / 2;
        int startheightTop = (height / 3) * 2;
        int startheightButton = height / 3;

        int x = 0;
        int y = 0;
        int w = 0;
        int h = 0;

        // 左トリム(上)
        for (int i = 0; i < width; i++) {
            if (image.getRGB(i, startheightTop) != WHITE) {
                x = i;
                break;
            }
        }
        // 左トリム(下)
        for (int i = 0; i < width; i++) {
            if (image.getRGB(i, startheightButton) != WHITE) {
                x = Math.min(x, i);
                break;
            }
        }
        // 上トリム
        for (int i = 0; i < height; i++) {
            if (image.getRGB(startwidth, i) != WHITE) {
                y = i;
                break;
            }
        }
        // 右トリム(上)
        for (int i = width - 1; i >= 0; i--) {
            if (image.getRGB(i, startheightTop) != WHITE) {
                w = (i - x) + 1;
                break;
            }
        }
        // 右トリム(下)
        for (int i = width - 1; i >= 0; i--) {
            if (image.getRGB(i, startheightButton) != WHITE) {
                w = Math.max(w, (i - x) + 1);
                break;
            }
        }
        // 下トリム
        for (int i = height - 1; i >= 0; i--) {
            if (image.getRGB(startwidth, i) != WHITE) {
                h = (i - y) + 1;
                break;
            }
        }

        if ((w == 0) || (h == 0)) {
            return new Rectangle(0, 0, image.getWidth(), image.getHeight());
        } else {
            return new Rectangle(x, y, w, h);
        }
    }
}
