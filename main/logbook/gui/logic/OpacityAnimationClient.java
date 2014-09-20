/**
 * 
 */
package logbook.gui.logic;

/**
 * @author Nekopanda
 *
 */
public interface OpacityAnimationClient {

    /** マウスがウィンドウに乗っているか */
    boolean isMouseHovering();

    /** 新しいアルファ値 */
    void setAlpha(int newAlpha);
}
