/**
 * 
 */
package logbook.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import logbook.constants.AppConstants;
import logbook.dto.BattleExDto;
import logbook.dto.BattleResultDto;
import logbook.gui.logic.IntegerPair;
import logbook.util.ReportUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.dyuproject.protostuff.LinkedBuffer;
import com.dyuproject.protostuff.ProtostuffIOUtil;
import com.dyuproject.protostuff.Schema;
import com.dyuproject.protostuff.runtime.RuntimeSchema;

/**
 * @author Nekopanda
 * 出撃ログの保存・読み込み
 */
public class BattleResultServer {
    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(BattleResultServer.class);

    private static DateFormat format = new SimpleDateFormat(AppConstants.BATTLE_LOGFILE_DATE_FORMAT);

    private static Schema<BattleExDto> schema = RuntimeSchema.getSchema(BattleExDto.class);

    private static class BattleResult extends BattleResultDto {
        public File file;
        public int index;

        BattleResult(BattleExDto dto, File file, int index) {
            super(dto);
            this.file = file;
            this.index = index;
        }
    }

    private static BattleResultServer instance = null;

    public static void initialize(String path) {
        instance = new BattleResultServer(path);
    }

    public static BattleResultServer get() {
        return instance;
    }

    private final String path;
    private final LinkedBuffer buffer = LinkedBuffer.allocate(128 * 1024);

    private Date firstBattleTime;
    private Date lastBattleTime;
    private final List<String> dropShipList = new ArrayList<String>();
    private final List<IntegerPair> mapList;
    private final List<Integer> cellList;

    private final List<BattleResult> resultList = new ArrayList<BattleResult>();
    private final Map<String, Integer> numRecordsMap = new HashMap<String, Integer>();

    // キャッシュ
    private File cachedFile;
    private List<BattleExDto> cachedResult;

    private BattleResultServer(String path) {
        this.path = path;
        // ファイルを読み込んで resultList を作成
        for (File file : FileUtils.listFiles(new File(path), new String[] { "dat" }, true)) {
            try {
                List<BattleExDto> result = this.readResultFile(file);
                for (int i = 0; i < result.size(); ++i) {
                    this.resultList.add(new BattleResult(result.get(i), file, i));
                }
                this.numRecordsMap.put(file.getPath(), result.size());
            } catch (IOException e) {
                LOG.warn("出撃ログの読み込みに失敗しました (" + file.getPath() + ")", e);
            }
        }

        // フィルタ用パラメータを計算
        this.firstBattleTime = new Date();
        this.lastBattleTime = new Date(0);
        Set<String> dropShipList = new HashSet<String>();
        Set<IntegerPair> mapList = new TreeSet<IntegerPair>();
        Set<Integer> cellList = new TreeSet<Integer>();
        for (BattleResult battle : this.resultList) {
            Date battleDate = battle.getBattleDate();
            String dropName = battle.getDropName();
            int[] map = battle.getMapCell().getMap();

            if (battleDate.before(this.firstBattleTime)) {
                this.firstBattleTime = battleDate;
            }
            if (battleDate.after(this.lastBattleTime)) {
                this.lastBattleTime = battleDate;
            }
            dropShipList.add(dropName);
            mapList.add(new IntegerPair(map[0], map[1], "-"));
            cellList.add(map[2]);
        }
        this.dropShipList.clear();
        this.dropShipList.addAll(dropShipList);
        this.mapList = new ArrayList<IntegerPair>(mapList);
        this.cellList = new ArrayList<Integer>(cellList);

        // 時刻でソート
        Collections.sort(this.resultList, new Comparator<BattleResult>() {
            @Override
            public int compare(BattleResult arg0, BattleResult arg1) {
                return Long.compare(
                        arg0.getBattleDate().getTime(), arg1.getBattleDate().getTime());
            }
        });
    }

    private List<BattleExDto> readResultFile(File file) throws IOException {
        List<BattleExDto> result = new ArrayList<BattleExDto>();
        FileInputStream input = new FileInputStream(file);
        while (input.available() > 0) {
            BattleExDto battle = schema.newMessage();
            ProtostuffIOUtil.mergeDelimitedFrom(input, battle, schema, this.buffer);
            result.add(battle);
        }
        input.close();
        return result;
    }

    private void dispose() {
        // TODO:
    }

    public void addNewResult(BattleExDto dto) {
        File file = new File(FilenameUtils.concat(this.path, format.format(new Date()) + ".dat"));
        try {
            FileOutputStream output = null;
            try {
                // ファイルとリストに追加
                output = new FileOutputStream(getStoreFile(file), true);
                ProtostuffIOUtil.writeDelimitedTo(output, dto, schema, this.buffer);
                this.buffer.clear();
            } catch (IOException e) {
                LOG.warn("出撃ログの書き込みに失敗しました", e);
            } finally {
                if (output != null) {
                    output.close();
                }
            }
        } catch (IOException e) {
            LOG.warn("出撃ログファイルの後処理に失敗しました", e);
        }
        // ファイルとリストに追加
        int index = this.numRecordsMap.get(file.getPath());
        this.resultList.add(new BattleResult(dto, file, index));
        this.numRecordsMap.put(file.getPath(), index + 1);
        // キャッシュされているときはキャッシュにも追加
        if ((this.cachedFile != null) && file.equals(this.cachedFile)) {
            this.cachedResult.add(dto);
        }
    }

    public List<BattleResultDto> getFilteredList(BattleResultFilter filter) {
        List<BattleResultDto> list = new ArrayList<BattleResultDto>();
        for (BattleResult result : this.resultList) {
            BattleResultDto dto = result;
            if (this.matchFilter(filter, dto)) {
                list.add(dto);
            }
        }
        return list;
    }

    /** 出撃ログがフィルタにマッチしているかどうか
     * @param filter
     * @param dto
     * @return
     */
    private boolean matchFilter(BattleResultFilter filter, BattleResultDto dto) {
        if ((filter.fromTime != null) && filter.fromTime.after(dto.getBattleDate())) {
            return false;
        }
        if ((filter.toTime != null) && filter.toTime.before(dto.getBattleDate())) {
            return false;
        }
        if ((filter.dropShip != null) && (filter.dropShip.equals(dto.getDropName()) == false)) {
            return false;
        }
        if ((filter.map != null)) {
            int[] battleMap = dto.getMapCell().getMap();
            if (filter.map.compareTo(new IntegerPair(battleMap[0], battleMap[1], "-")) != 0) {
                return false;
            }
            if (filter.cell != battleMap[2]) {
                return false;
            }
        }
        if ((filter.rank != null) && (filter.rank.equals(dto.getRank()) == false)) {
            return false;
        }
        return true;
    }

    /** 詳細を読み込む（失敗したら null ） */
    public BattleExDto getBattleDetail(BattleResultDto summary) {
        BattleResult result = (BattleResult) summary;
        if ((this.cachedFile == null) || (result.file.equals(this.cachedFile) == false)) {
            this.cachedFile = result.file;
            try {
                this.cachedResult = this.readResultFile(result.file);
            } catch (IOException e) {
                return null;
            }
        }
        if (this.cachedResult.size() <= result.index) {
            return null;
        }
        return this.cachedResult.get(result.index);
    }

    public Date getFirstBattleTime() {
        return this.firstBattleTime;
    }

    public Date getLastBattleTime() {
        return this.lastBattleTime;
    }

    public List<String> getDropShipList() {
        return this.dropShipList;
    }

    public List<IntegerPair> getMapList() {
        return this.mapList;
    }

    public List<Integer> getCellList() {
        return this.cellList;
    }

    private static File getStoreFile(File file) throws IOException {
        // 報告書の保存先にファイルを保存します
        if ((file.getParentFile() == null) && file.mkdirs()) {
            // 報告書の保存先ディレクトリが無く、ディレクトリの作成に失敗した場合はカレントフォルダにファイルを保存
            file = new File(file.getName());
        }
        File altFile = new File(FilenameUtils.removeExtension(file.getPath()) + "_alternativefile.dat");
        if (ReportUtils.isLocked(file)) {
            // ロックされている場合は代替ファイルに書き込みます
            file = altFile;
        }
        else {
            if (altFile.exists() && !ReportUtils.isLocked(altFile) && (FileUtils.sizeOf(altFile) > 0)) {
                mergeAltFile(file, altFile);
            }
        }
        return file;
    }

    /**
     * alternativeファイルを本体にマージして削除します
     * 
     * @param report ファイル本体
     * @param alt_report alternativeファイル
     * @return
     * @throws IOException
     */
    private static void mergeAltFile(File report, File alt_report) throws IOException {
        // report が空ファイルの場合は、alt ファイルをリネームして終了
        if (!report.exists() || (FileUtils.sizeOf(report) <= 0)) {
            report.delete();
            alt_report.renameTo(report);
            return;
        }
        OutputStream report_stream = new FileOutputStream(report, true);
        InputStream alt_stream = new FileInputStream(alt_report);
        try {
            IOUtils.copy(alt_stream, report_stream);
        } finally {
            report_stream.close();
            alt_stream.close();
        }
        alt_report.delete();
    }
}
