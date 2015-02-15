package logbook.gui.logic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import logbook.dto.chart.Resource;
import logbook.dto.chart.ResourceLog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.widgets.Display;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * 資材チャートを描画する
 *
 */
public class ResourceChart {
    /** タイムゾーンオフセット */
    private static final long TZ_OFFSET = Calendar.getInstance().get(Calendar.ZONE_OFFSET);
    /** グラフエリアの左マージン */
    private static final int LEFT_WIDTH = 70;
    /** グラフエリアの右マージン */
    private static final int RIGHT_WIDTH = 45;
    /** グラフエリアの上マージン */
    private static final int TOP_HEIGHT = 30;
    /** グラフエリアの下マージン */
    private static final int BOTTOM_HEIGHT = 30;

    /** 資材ログ */
    private final ResourceLog log;
    /** 期間 */
    private final long term;
    /** スケールテキスト */
    private final String scaleText;
    /** 刻み */
    private final long notch;
    /** Width */
    private final int width;
    /** Height */
    private final int height;

    private int max;
    private int min;
    private long[] time = {};
    private Resource[] resources = {};

    /**
     * 資材チャート
     * 
     * @param log 資材ログ
     * @param scale 日単位のスケール
     * @param width 幅
     * @param height 高さ
     */
    public ResourceChart(ResourceLog log, int scale, String scaleText, int width, int height) {
        this.log = log;
        this.term = TimeUnit.DAYS.toMillis(scale);
        this.scaleText = scaleText;
        this.notch = (long) (this.term / ((double) (width - LEFT_WIDTH - RIGHT_WIDTH) / 4));
        this.width = width;
        this.height = height;
        // データロード
        this.load();
    }

    /**
     * グラフを描画します
     * 
     * @param gc グラフィックコンテキスト
     */
    public void draw(GC gc) {

        // グラフエリアの幅
        float w = this.width - LEFT_WIDTH - RIGHT_WIDTH;
        // グラフエリアの高さ
        float h = this.height - TOP_HEIGHT - BOTTOM_HEIGHT;
        // お絵かき開始
        gc.setAntialias(SWT.ON);
        gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        gc.fillRectangle(0, 0, this.width, this.height);
        gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
        gc.setLineWidth(2);
        // グラフエリアのラインを描く
        // 縦
        gc.drawLine(LEFT_WIDTH, TOP_HEIGHT, LEFT_WIDTH, this.height - BOTTOM_HEIGHT);
        // 横
        gc.drawLine(LEFT_WIDTH - 5, this.height - BOTTOM_HEIGHT, this.width - RIGHT_WIDTH, this.height - BOTTOM_HEIGHT);

        // 縦軸を描く
        gc.setLineWidth(1);
        for (int i = 0; i < 5; i++) {
            // 軸
            gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
            int jh = (int) ((h * i) / 4) + TOP_HEIGHT;
            gc.drawLine(LEFT_WIDTH - 5, jh, this.width - RIGHT_WIDTH, jh);
            //ラベルを設定
            gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
            String label = Integer.toString((int) (((float) (this.max - this.min) * (4 - i)) / 4) + this.min);
            int labelWidth = getStringWidth(gc, label);
            int labelHeight = gc.getFontMetrics().getHeight();
            int x = LEFT_WIDTH - labelWidth - 5;
            int y = jh - (labelHeight / 2);
            gc.drawString(label, x, y);
        }
        SimpleDateFormat format = new SimpleDateFormat("M月d日 HH:mm");
        // 横軸を描く
        for (int i = 0; i < 5; i++) {
            //ラベルを設定
            gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));

            int idx = (int) (((float) (this.time.length - 1) * i) / 4);
            String label = format.format(new Date(normalizeTime(this.time[idx], TimeUnit.MINUTES.toMillis(10))));
            int labelWidth = getStringWidth(gc, label);
            int x = ((int) ((w * i) / 4) + LEFT_WIDTH) - (labelWidth / 2);
            int y = (this.height - BOTTOM_HEIGHT) + 6;
            gc.drawText(label, x, y, true);
        }
        // 判例を描く
        int hx = LEFT_WIDTH;
        int hy = 5;
        for (int i = 0; i < this.resources.length; i++) {
            gc.setLineWidth(3);
            gc.setForeground(SWTResourceManager.getColor(this.resources[i].color));

            String label = this.resources[i].name;
            int labelWidth = getStringWidth(gc, label);
            int labelHeight = gc.getFontMetrics().getHeight();
            gc.drawLine(hx, hy + (labelHeight / 2), hx += 20, hy + (labelHeight / 2));
            hx += 1;
            gc.drawText(label, hx, hy, true);
            hx += labelWidth + 2;
        }
        // スケールテキストを描く
        int sx = this.width - RIGHT_WIDTH - getStringWidth(gc, this.scaleText);
        int sy = 5;
        gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
        gc.drawText(this.scaleText, sx, sy, true);

        // グラフを描く
        for (int i = 0; i < this.resources.length; i++) {
            gc.setLineWidth(2);
            gc.setForeground(SWTResourceManager.getColor(this.resources[i].color));

            int[] values = this.resources[i].values;

            Path path = new Path(Display.getCurrent());

            float x = LEFT_WIDTH;
            float y = (h * (1 - ((float) (values[0] - this.min) / (this.max - this.min)))) + TOP_HEIGHT;
            path.moveTo(x, y);

            for (int j = 1; j < values.length; j++) {
                // 欠損(-1)データは描かない
                if (values[j] != -1) {

                    float x1 = ((w * j) / values.length) + LEFT_WIDTH;
                    float y1 = (h * (1 - ((float) (values[j] - this.min) / (this.max - this.min)))) + TOP_HEIGHT;
                    path.lineTo(x1, y1);
                }
            }
            gc.drawPath(path);
        }
    }

    /**
     * 資材ログを読み込む
     */
    private void load() {
        ResourceLog log = this.log;

        // 時間はソートされている前提
        // 最新の時間インデックス
        int maxidx = log.time.length - 1;
        // スケールで指定した範囲外で最も最新の時間インデックス、範囲外の時間がない場合0
        int minidx = Math.max(Math.abs(Arrays.binarySearch(log.time, log.time[maxidx] - this.term)) - 2, 0);

        // データを準備する
        // データMax値
        this.max = Integer.MIN_VALUE;
        // データMin値
        this.min = Integer.MAX_VALUE;
        // グラフに必要なデータ配列の長さ
        int length = (int) (this.term / this.notch) + 1;
        // 時間軸
        this.time = new long[length];
        // グラフデータ(資材)
        List<Resource> resourceList = new ArrayList<Resource>();
        for (int i = 0; i < log.resources.length; i++) {
            if (log.resources[i].color != null) {
                resourceList.add(log.resources[i]);
            }
        }
        this.resources = new Resource[resourceList.size()];
        for (int i = 0; i < resourceList.size(); i++) {
            this.resources[i] = new Resource(resourceList.get(i).name, resourceList.get(i).color, new int[length]);
        }
        // 時間を用意する
        for (int i = 0; i < this.time.length; i++) {
            this.time[i] = (log.time[maxidx] - this.term) + ((this.term / (length - 1)) * i);
        }
        // 資材を用意する
        float fr = (float) (this.time[0] - log.time[minidx]) / (float) (log.time[minidx + 1] - log.time[minidx]);
        long s = log.time[maxidx] - this.term;
        for (int i = 0; i < this.resources.length; i++) {
            // 補正前のデータ
            int[] prevalues = resourceList.get(i).values;
            // 補正されたスケールで指定した範囲のデータ
            int[] values = this.resources[i].values;
            // 初期値は-1(欠損)
            Arrays.fill(values, -1);

            if (log.time[minidx] <= this.time[0]) {
                // スケール外データがある場合最初の要素を補完する
                values[0] = (int) (prevalues[minidx] + ((prevalues[minidx + 1] - prevalues[minidx]) * fr));
            }
            for (int j = minidx + 1; j < prevalues.length; j++) {
                int idx = (int) ((log.time[j] - s) / this.notch);
                values[idx] = prevalues[j];
            }
            boolean find = false;
            for (int j = 0; j < (length - 1); j++) {
                // 先頭のデータがない場合0扱いにする
                if (!find) {
                    if (values[j] >= 0) {
                        find = true;
                    } else {
                        values[j] = 0;
                    }
                }
                if (values[j] >= 0) {
                    // 資材最大数を設定
                    this.max = Math.max(values[j], this.max);
                    // 資材最小数を設定
                    this.min = Math.min(values[j], this.min);
                }
            }
        }
        // 資材の最大数を1000単位にする、資材の最大数が1000未満なら1000に設定
        this.max = (int) Math.max(normalize(this.max, 1000), 1000);
        // 資材の最小数を0.8でかけた後1000単位にする、
        this.min = (int) Math.max(normalize((long) (this.min * 0.8f), 1000), 0);
    }

    /**
     * 文字列のデバイス上の幅を返す
     * 
     * @param gc GC
     * @param str 文字列
     * @return 文字列幅
     */
    private static int getStringWidth(GC gc, String str) {
        return gc.textExtent(str).x;
    }

    /**
     * 数値を指定した間隔で刻む
     * 
     * @param value 数値
     * @param notch 刻み
     * @return
     */
    private static long normalize(long value, long notch) {
        long t = value;
        long half = notch / 2;
        long mod = t % notch;
        if (mod >= half) {
            t += notch - mod;
        } else {
            t -= mod;
        }
        return t;
    }

    /**
     * 時刻を指定した間隔で刻む
     * 
     * @param time 時刻
     * @param notch 刻み
     * @return
     */
    private static long normalizeTime(long time, long notch) {
        return normalize(time + TZ_OFFSET, notch) - TZ_OFFSET;
    }
}
