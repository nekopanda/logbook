/**
 * 
 */
package logbook.gui.logic;

import java.util.ArrayList;
import java.util.List;

import logbook.config.AppConfig;
import logbook.internal.LoggerHolder;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * @author Nekopanda
 * 透明度アニメーションのロジック
 */
public class OpacityAnimation {
    /** ロガー */
    private static final LoggerHolder LOG = new LoggerHolder(OpacityAnimation.class);

    private static int ALPHA_MAX = 255;
    private static int STEP_TIME = 30;
    private static int NUM_STEPS = 10;

    private static OpacityAnimationServer server = new OpacityAnimationServer();

    private final OpacityAnimationClient client;

    private int baseAlpha; // 設定されたアルファ値
    private boolean mouseHoverAware = false; // マウスホバーに反応する？
    private boolean enabled = false;

    private boolean mouseLeaved = false;
    private boolean activated = false;
    private boolean nowAnimating = false;
    private int remainSteps = 0;
    private int currentAlpha;
    private int alphaStep;
    private int targetAlpha;

    private static class OpacityAnimationServer {
        private final List<OpacityAnimation> clients = new ArrayList<OpacityAnimation>();
        private Listener mouseEventFilter;
        private boolean nowAnimating = false;

        public void register(OpacityAnimation client) {
            if (this.clients.size() == 0) {
                // start;
                this.mouseEventFilter = new Listener() {
                    @Override
                    public void handleEvent(Event event) {
                        if (OpacityAnimationServer.this.shouldActivateByMouseEvent()) {
                            OpacityAnimationServer.this.startAnimation();
                        }
                    }
                };
                Display.getDefault().addFilter(SWT.MouseMove, this.mouseEventFilter);
            }
            this.clients.add(client);
        }

        public void deregister(OpacityAnimation client) {
            if (this.clients.remove(client)) {
                if (this.clients.size() == 0) {
                    // end
                    Display.getDefault().removeFilter(
                            SWT.MouseMove, this.mouseEventFilter);
                }
            }
        }

        /** マウスイベントでアクティブにすべきクライアントがいるか */
        private boolean shouldActivateByMouseEvent() {
            boolean ret = false;
            for (OpacityAnimation client : this.clients) {
                if ((client.activated == false) && client.mouseHoverAware && client.client.isMouseHovering()) {
                    ret = client.nowAnimating = true;
                }
            }
            //System.out.println(ret);
            return ret;
        }

        public void startAnimation() {
            // ２つ以上立ち上げない
            if (this.nowAnimating == false) {
                this.nowAnimating = true;
                Display.getDefault().timerExec(STEP_TIME, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            boolean active = false;
                            for (OpacityAnimation client : OpacityAnimationServer.this.clients) {
                                if (client.nowAnimating) {
                                    client.animateProc();
                                    active = true;
                                }
                            }
                            if (active) {
                                Display.getDefault().timerExec(STEP_TIME, this);
                            }
                            else {
                                OpacityAnimationServer.this.nowAnimating = false;
                            }
                        } catch (Exception e) {
                            LOG.get().warn("OpacityAnimatorでエラー", e);
                        }
                    }
                });
            }
        }
    }

    public OpacityAnimation(OpacityAnimationClient client) {
        this.client = client;
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled != enabled) {
            this.enabled = enabled;
            if (enabled) {
                server.register(this);
            }
            else {
                server.deregister(this);
            }
        }
    }

    /** 初期アルファ値設定(client.setAlpha()も呼び出す） */
    public void setInitialAlpha(int initAlpha, boolean mouseHoverAware) {
        this.baseAlpha = this.currentAlpha = this.targetAlpha = initAlpha;
        this.mouseHoverAware = mouseHoverAware;
        this.client.setAlpha(initAlpha);
    }

    /** 基本アルファ値を設定 */
    public void setAlpha(int newAlpah) {
        if (this.baseAlpha != newAlpah) {
            this.baseAlpha = newAlpah;
            if (this.enabled) {
                this.nowAnimating = true;
                server.startAnimation();
            }
        }
    }

    public int getAlpha() {
        return this.baseAlpha;
    }

    public int getCurrentAlpha() {
        return this.currentAlpha;
    }

    /** マウスに対応するかを設定する */
    public void setHoverAware(boolean aware) {
        if (this.mouseHoverAware != aware) {
            this.mouseHoverAware = aware;
            if (this.enabled) {
                this.nowAnimating = true;
                //System.out.println("mouse hover -> " + aware);
                server.startAnimation();
            }
        }
    }

    public boolean getHoverAware() {
        return this.mouseHoverAware;
    }

    private void animateProc() {
        boolean continueAnimation = false;
        boolean mouseHovering = this.client.isMouseHovering();
        if (mouseHovering) {
            // ウィンドウからのMouseLeaveイベントが取得できないのでマウスがあるときは常に監視する
            continueAnimation = (this.mouseHoverAware && (this.baseAlpha != ALPHA_MAX));
            this.mouseLeaved = false;
            this.activated = true;
        }
        else {
            if (this.mouseLeaved == false) {
                // マウスが離れた処理を開始する
                this.mouseLeaved = true;
                this.remainSteps = (((AppConfig.get().getOpaqueInterval() * 100) + STEP_TIME) - 1) / STEP_TIME;
            }
            if (this.remainSteps > 0) {
                continueAnimation = true;
                this.remainSteps--;
                //System.out.println("Holding=" + this.remainSteps);
            }
            else {
                // カウントがゼロになったので非アクティブ
                this.activated = false;
            }
        }
        int newAlpha;
        if (this.activated && this.mouseHoverAware) {
            newAlpha = ALPHA_MAX;
        }
        else {
            newAlpha = this.baseAlpha;
        }
        if (this.targetAlpha != newAlpha) {
            this.targetAlpha = newAlpha;
            this.alphaStep = ((Math.abs(this.targetAlpha - this.currentAlpha) + NUM_STEPS) - 1)
                    / NUM_STEPS;
            if (this.targetAlpha < this.currentAlpha) {
                this.alphaStep = -this.alphaStep;
            }
            else {
                // 見える化は早くする
                this.alphaStep *= 2;
            }
        }
        if (this.currentAlpha != this.targetAlpha) {
            this.currentAlpha += this.alphaStep;
            // 目標に到達したか
            if ((this.alphaStep > 0) && (this.currentAlpha > this.targetAlpha)) {
                this.currentAlpha = this.targetAlpha;
            }
            else if ((this.alphaStep < 0) && (this.currentAlpha < this.targetAlpha)) {
                this.currentAlpha = this.targetAlpha;
            }
            else {
                continueAnimation = true;
            }
            //System.out.println("Alpha=" + this.currentAlpha);
            this.client.setAlpha(this.currentAlpha);
        }
        if (continueAnimation == false) {
            this.activated = false;
        }
        this.nowAnimating = continueAnimation;
    }
}
