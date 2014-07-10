package logbook.dto.chart;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.CheckForNull;

import logbook.config.AppConfig;
import logbook.dto.AbstractDto;

import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.math.NumberUtils;

/**
 * 資材ログを表します
 *
 */
public class ResourceLog extends AbstractDto {

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
        // 日付フォーマット
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        List<SortableLog> logs = new ArrayList<>();
        // データを読み込む
        try (Reader reader = new BufferedReader(new FileReader(file))) {
            LineIterator ite = new LineIterator(reader);
            // ヘッダーを読み飛ばす
            if (ite.hasNext()) {
                ite.next();
            }
            while (ite.hasNext()) {
                String line = ite.next();
                // 日付,（直前のイベント,）燃料,弾薬,鋼材,ボーキ,高速修復材,高速建造材,開発資材
                String[] colums = line.split(",");
                try {
                    Date date = format.parse(colums[0]);
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
                    logs.add(new SortableLog(date.getTime(),
							Integer.parseInt(colums[baseIdx + 0]), Integer.parseInt(colums[baseIdx + 1]),
                            Integer.parseInt(colums[baseIdx + 2]), Integer.parseInt(colums[baseIdx + 3])));

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
        for (int i = 0; i < logs.size(); i++) {
            SortableLog log = logs.get(i);
            time[i] = log.time;
            fuel[i] = log.fuel;
            ammo[i] = log.ammo;
            metal[i] = log.metal;
            bauxite[i] = log.bauxite;
        }
        Resource[] resources = new Resource[] {
                new Resource("燃料", AppConfig.get().getFuelColor(), fuel),
                new Resource("弾薬", AppConfig.get().getAmmoColor(), ammo),
                new Resource("鋼材", AppConfig.get().getMetalColor(), metal),
                new Resource("ボーキ", AppConfig.get().getBauxiteColor(), bauxite)
        };
        return new ResourceLog(time, resources);
    }

    /**
     * 資材ログの行
     */
    private static final class SortableLog implements Comparable<SortableLog> {

        long time;
        int fuel;
        int ammo;
        int metal;
        int bauxite;

        public SortableLog(long time, int fuel, int ammo, int metal, int bauxite) {
            this.time = time;
            this.fuel = fuel;
            this.ammo = ammo;
            this.metal = metal;
            this.bauxite = bauxite;
        }

        @Override
        public int compareTo(SortableLog o) {
            return Long.compare(this.time, o.time);
        }
    }
}