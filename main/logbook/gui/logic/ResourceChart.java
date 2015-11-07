package logbook.gui.logic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import logbook.dto.chart.Resource;
import logbook.dto.chart.ResourceLog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Path;
import org.eclipse.swt.graphics.RGB;
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
    private final int LEFT_WIDTH;
    /** グラフエリアの右マージン */
    private final int RIGHT_WIDTH;
    /** グラフエリアの上マージン */
    private final int TOP_HEIGHT;
    /** グラフエリアの下マージン */
    private final int BOTTOM_HEIGHT;

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

    private final ActiveLevel[] activeLevel;

    private final boolean printHeader;

    private int max;
    private int min;
    private int max2;
    private int min2;
    private long[] time = {};
    private Resource[] resources = {};

    public enum ActiveLevel {
        DISABLED,
        INACTIVE,
        NORMAL,
        ACTIVE,
    }

    /**
     * 資材チャート
     * 
     * @param log 資材ログ
     * @param scale 日単位のスケール
     * @param width 幅
     * @param height 高さ
     */
    public ResourceChart(GC gc, ResourceLog log, int scale, String scaleText, int width, int height,
            ActiveLevel[] activeLevel, boolean printHeader) {
        int labelHeight = gc.getFontMetrics().getHeight();
        this.LEFT_WIDTH = getStringWidth(gc, "100000") + 10;
        this.RIGHT_WIDTH = getStringWidth(gc, "1000") + 15;
        this.TOP_HEIGHT = (printHeader ? labelHeight : 0) + 10;
        this.BOTTOM_HEIGHT = labelHeight + 10;

        this.log = log;
        this.term = TimeUnit.DAYS.toMillis(scale);
        this.scaleText = scaleText;
        this.notch = (long) (this.term / ((double) (width - this.LEFT_WIDTH - this.RIGHT_WIDTH) / 4));
        this.width = width;
        this.height = height;
        this.activeLevel = activeLevel;
        this.printHeader = printHeader;
        // データロード
        this.load();
    }

    /**
     * グラフを描画します
     * 
     * @param gc グラフィックコンテキスト
     */
    public void draw(final GC gc) {

        // グラフエリアの幅
        final float w = this.width - this.LEFT_WIDTH - this.RIGHT_WIDTH;
        // グラフエリアの高さ
        final float h = this.height - this.TOP_HEIGHT - this.BOTTOM_HEIGHT;
        // お絵かき開始
        gc.setAntialias(SWT.ON);
        gc.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        gc.fillRectangle(0, 0, this.width, this.height);
        gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
        gc.setLineWidth(2);
        // グラフエリアのラインを描く
        // 縦
        gc.drawLine(this.LEFT_WIDTH, this.TOP_HEIGHT, this.LEFT_WIDTH, this.height - this.BOTTOM_HEIGHT);
        gc.drawLine(this.width - this.RIGHT_WIDTH, this.TOP_HEIGHT, this.width - this.RIGHT_WIDTH, this.height
                - this.BOTTOM_HEIGHT);
        // 横
        gc.drawLine(this.LEFT_WIDTH - 5, this.height - this.BOTTOM_HEIGHT,
                (this.width - this.RIGHT_WIDTH) + 5, this.height - this.BOTTOM_HEIGHT);

        // 縦軸を描く
        gc.setLineWidth(1);
        for (int i = 0; i < 5; i++) {
            // 軸
            gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_GRAY));
            int jh = (int) ((h * i) / 4) + this.TOP_HEIGHT;
            gc.drawLine(this.LEFT_WIDTH - 5, jh, (this.width - this.RIGHT_WIDTH) + 5, jh);
            //ラベルを設定
            gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
            String labelLeft = Integer.toString((int) (((float) (this.max - this.min) * (4 - i)) / 4) + this.min);
            String labelRight = Integer.toString((int) (((float) (this.max2 - this.min2) * (4 - i)) / 4) + this.min2);
            int labelLeftWidth = getStringWidth(gc, labelLeft);
            int labelHeight = gc.getFontMetrics().getHeight();
            if (this.max > this.min) {
                gc.drawString(labelLeft, this.LEFT_WIDTH - labelLeftWidth - 5, jh - (labelHeight / 2));
            }
            if (this.max2 > this.min2) {
                gc.drawString(labelRight, (this.width - this.RIGHT_WIDTH) + 10, jh - (labelHeight / 2));
            }
        }
        SimpleDateFormat format = new SimpleDateFormat("M月d日 HH:mm");
        // 横軸を描く
        for (int i = 0; i < 5; i++) {
            //ラベルを設定
            gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));

            int idx = (int) (((float) (this.time.length - 1) * i) / 4);
            String label = format.format(new Date(normalizeTime(this.time[idx], TimeUnit.MINUTES.toMillis(10))));
            int labelWidth = getStringWidth(gc, label);
            int x = ((int) ((w * i) / 4) + this.LEFT_WIDTH) - (labelWidth / 2);
            int y = (this.height - this.BOTTOM_HEIGHT) + 6;
            gc.drawText(label, x, y, true);
        }
        if (this.printHeader) {
            // 判例を描く
            int hx = this.LEFT_WIDTH;
            int hy = 5;
            for (int i = 0; i < this.resources.length; i++) {
                ActiveLevel level = this.activeLevel[i];
                if (level == ActiveLevel.DISABLED) {
                    continue;
                }
                gc.setLineWidth(3);
                gc.setForeground(ColorManager.getColor(this.resources[i].color));

                String label = this.resources[i].name;
                int labelWidth = getStringWidth(gc, label);
                int labelHeight = gc.getFontMetrics().getHeight();
                gc.drawLine(hx, hy + (labelHeight / 2), hx += 20, hy + (labelHeight / 2));
                hx += 1;
                gc.drawText(label, hx, hy, true);
                hx += labelWidth + 2;
            }
            // スケールテキストを描く
            int sx = this.width - this.RIGHT_WIDTH - getStringWidth(gc, this.scaleText);
            int sy = 5;
            gc.setForeground(SWTResourceManager.getColor(SWT.COLOR_BLACK));
            gc.drawText(this.scaleText, sx, sy, true);
        }

        // よりアクティブな線が前になるように
        class ResourceLine {
            ActiveLevel level;
            int index;

            ResourceLine(ActiveLevel level, int index) {
                this.level = level;
                this.index = index;
            }
        }
        ResourceLine[] drawLines = new ResourceLine[8];
        for (int i = 0; i < this.resources.length; i++) {
            drawLines[i] = new ResourceLine(this.activeLevel[i], i);
        }
        Arrays.sort(drawLines, new Comparator<ResourceLine>() {
            @Override
            public int compare(ResourceLine o1, ResourceLine o2) {
                return o1.level.compareTo(o2.level);
            }
        });

        // グラフを描く
        for (int c = 0; c < drawLines.length; c++) {
            ActiveLevel level = drawLines[c].level;
            int i = drawLines[c].index;
            if (level != ActiveLevel.DISABLED) {
                RGB color = this.resources[i].color;
                if (level == ActiveLevel.INACTIVE) {
                    gc.setLineWidth(1);
                    color = new RGB(
                            (int) (255 - ((255 - color.red) * 0.25)),
                            (int) (255 - ((255 - color.green) * 0.25)),
                            (int) (255 - ((255 - color.blue) * 0.25)));
                }
                else {
                    gc.setLineWidth(2);
                }
                gc.setForeground(ColorManager.getColor(color));

                int[] values = this.resources[i].values;
                Path path = new Path(Display.getCurrent());
                if (i < 4) {
                    this.drawPath(values, this.max, this.min, w, h, path);
                }
                else {
                    this.drawPath(values, this.max2, this.min2, w, h, path);
                }
                gc.drawPath(path);
            }
        }
    }

    private void drawPath(int[] values, int max, int min, float w, float h, Path path) {
        float x = this.LEFT_WIDTH;
        float y = (h * (1 - ((float) (values[0] - min) / (max - min)))) + this.TOP_HEIGHT;
        path.moveTo(x, y);

        for (int j = 1; j < values.length; j++) {
            // 欠損(-1)データは描かない
            if (values[j] != -1) {

                float x1 = ((w * j) / values.length) + this.LEFT_WIDTH;
                float y1 = (h * (1 - ((float) (values[j] - min) / (max - min)))) + this.TOP_HEIGHT;
                path.lineTo(x1, y1);
            }
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
        // データMax値
        this.max2 = Integer.MIN_VALUE;
        // データMin値
        this.min2 = Integer.MAX_VALUE;
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
            // データを必要な配列長に圧縮
            for (int j = minidx + 1; j < prevalues.length; j++) {
                // minidx+1が範囲の外側になることもあるのでマイナスにならないようにしておく
                int idx = Math.max((int) ((log.time[j] - s) / this.notch), 0);
                values[idx] = prevalues[j];
            }
            boolean find = false;
            for (int j = 0; j < length; j++) {
                // 先頭のデータがない場合0扱いにする
                if (!find) {
                    if (values[j] >= 0) {
                        find = true;
                    } else {
                        values[j] = 0;
                    }
                }
                if ((values[j] >= 0) && (this.activeLevel[i] != ActiveLevel.DISABLED)) {
                    if (i < 4) {
                        // 資材最大数を設定
                        this.max = Math.max(values[j], this.max);
                        // 資材最小数を設定
                        this.min = Math.min(values[j], this.min);
                    }
                    else {
                        // 資材最大数を設定
                        this.max2 = Math.max(values[j], this.max2);
                        // 資材最小数を設定
                        this.min2 = Math.min(values[j], this.min2);
                    }
                }
            }
        }
        if (this.max >= this.min) { // 1つ以上有効なデータがある場合
            // 資材の最大数を1000単位にする、資材の最大数が1000未満なら1000に設定
            this.max = (int) Math.ceil((float) (this.max + 100) / 1000) * 1000;
            // 資材の最小数を0.8でかけた後1000単位にする、
            this.min = (int) Math.max(Math.floor((float) (this.min - 100) / 1000) * 1000, 0);
        }
        if (this.max2 >= this.min2) { // 1つ以上有効なデータがある場合
            // 資材の最大数を100単位にする、資材の最大数が100未満なら100に設定
            this.max2 = (int) Math.ceil((float) (this.max2 + 10) / 100) * 100;
            // 資材の最小数を0.8でかけた後100単位にする、
            this.min2 = (int) Math.max(Math.floor((float) (this.min2 - 10) / 100) * 100, 0);
        }
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
     * 時刻を指定した間隔で刻む
     * 
     * @param time 時刻
     * @param notch 刻み
     * @return
     */
    private static long normalizeTime(long time, long notch) {
        return (((time + TZ_OFFSET + (notch / 2)) / notch) * notch) - TZ_OFFSET;
    }
}
