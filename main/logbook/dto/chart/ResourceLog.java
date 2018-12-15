package logbook.dto.chart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.CheckForNull;

import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.math.NumberUtils;

import logbook.config.AppConfig;
import logbook.dto.AbstractDto;

/**
 * 資材ログを表します
 *
 */
public class ResourceLog extends AbstractDto {
    public static final int RESOURCE_FUEL = 0;
    public static final int RESOURCE_AMMO = 1;
    public static final int RESOURCE_METAL = 2;
    public static final int RESOURCE_BAUXITE = 3;
    public static final int RESOURCE_BURNER = 4;
    public static final int RESOURCE_BUCKET = 5;
    public static final int RESOURCE_RESEARCH = 6;
    public static final int RESOURCE_SCREW = 7;

    public long[] time;

    public Resource[] resources;

    public ResourceLog(long[] time, Resource[] resources) {
        this.time = time;
        this.resources = resources;
    }

    /**
     * 資材ログを読み込む
     * 
     * @param file 資材ログ
     * @return
     * @throws IOException
     */
    @CheckForNull
    public static ResourceLog getInstance(File file) throws IOException {
        // 日付フォーマット（複数対応する）
        SimpleDateFormat[] formats = new SimpleDateFormat[] {
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"), // オリジナルの記録フォーマット
                new SimpleDateFormat("yyyy/MM/dd HH:mm") // Excelで保存した時のフォーマット
        };

        List<SortableLog> logs = new ArrayList<>();
        // データを読み込む
        try (Reader reader = new BufferedReader(new FileReader(file))) {
            LineIterator ite = new LineIterator(reader);
            // ヘッダーを読み飛ばす
            if (ite.hasNext()) {
                ite.next();
            }
            ParsePosition pos = new ParsePosition(0);
            while (ite.hasNext()) {
                String line = ite.next();
                // 日付,（直前のイベント,）燃料,弾薬,鋼材,ボーキ,高速建造材,高速修復材,開発資材,ネジ
                // 高速建造材,高速修復材が逆になっているので注意
                String[] colums;
                if (line.contains("\t")) {
                    colums = line.split("\t", -1);
                }
                else {
                    colums = line.split(",", -1);
                }
                try {
                    pos.setIndex(0);
                    Date date = null;
                    for (DateFormat format : formats) {
                        date = format.parse(colums[0], pos);
                        if (date != null) {
                            break;
                        }
                    }
                    if (date == null) {
                        continue;
                    }

                    int baseIdx;
                    // 拡張版の方は１列追加してしまったので、両方に対応させる！
                    if (NumberUtils.isNumber(colums[1])) {
                        // 本家のログ
                        baseIdx = 1;
                    }
                    else {
                        // 拡張版のログ
                        baseIdx = 2;
                    }
                    int screw = (colums.length > (baseIdx + 7)) ? Integer.parseInt(colums[baseIdx + 7]) : 0;
                    logs.add(new SortableLog(date.getTime(),
                            Integer.parseInt(colums[baseIdx + 0]), Integer.parseInt(colums[baseIdx + 1]),
                            Integer.parseInt(colums[baseIdx + 2]), Integer.parseInt(colums[baseIdx + 3]),
                            Integer.parseInt(colums[baseIdx + 5]), Integer.parseInt(colums[baseIdx + 4]),
                            Integer.parseInt(colums[baseIdx + 6]), screw));

                } catch (Exception e) {
                    continue;
                }
            }
        }
        // 資材ログが2行以下の場合はグラフを描画出来ないのでnullを返す
        if (logs.size() <= 2) {
            return null;
        }
        // ソート
        Collections.sort(logs);

        long[] time = new long[logs.size()];
        int[] fuel = new int[logs.size()];
        int[] ammo = new int[logs.size()];
        int[] metal = new int[logs.size()];
        int[] bauxite = new int[logs.size()];
        int[] burner = new int[logs.size()];
        int[] bucket = new int[logs.size()];
        int[] research = new int[logs.size()];
        int[] screw = new int[logs.size()];
        for (int i = 0; i < logs.size(); i++) {
            SortableLog log = logs.get(i);
            time[i] = log.time;
            fuel[i] = log.fuel;
            ammo[i] = log.ammo;
            metal[i] = log.metal;
            bauxite[i] = log.bauxite;
            burner[i] = log.burner;
            bucket[i] = log.bucket;
            research[i] = log.research;
            screw[i] = log.screw;
        }
        Resource[] resources = new Resource[] {
                new Resource("燃料", AppConfig.get().getFuelColor(), fuel),
                new Resource("弾薬", AppConfig.get().getAmmoColor(), ammo),
                new Resource("鋼材", AppConfig.get().getMetalColor(), metal),
                new Resource("ボーキ", AppConfig.get().getBauxiteColor(), bauxite),
                new Resource("バーナー", AppConfig.get().getBurnerColor(), burner),
                new Resource("バケツ", AppConfig.get().getBucketColor(), bucket),
                new Resource("開発", AppConfig.get().getResearchColor(), research),
                new Resource("ネジ", AppConfig.get().getScrewColor(), screw)
        };
        return new ResourceLog(time, resources);
    }

    /**
     * 資材ログの行
     */
    public static final class SortableLog implements Comparable<SortableLog> {

        public long time;
        public int fuel;
        public int ammo;
        public int metal;
        public int bauxite;
        public int burner;
        public int bucket;
        public int research;
        public int screw;

        public SortableLog(long time, int fuel, int ammo, int metal, int bauxite,
                int burner, int bucket, int research, int screw) {
            this.time = time;
            this.fuel = fuel;
            this.ammo = ammo;
            this.metal = metal;
            this.bauxite = bauxite;
            this.burner = burner;
            this.bucket = bucket;
            this.research = research;
            this.screw = screw;
        }

        @Override
        public int compareTo(SortableLog o) {
            return Long.compare(this.time, o.time);
        }
    }
}