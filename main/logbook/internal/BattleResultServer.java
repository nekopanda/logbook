/**
 * 
 */
package logbook.internal;

import java.io.EOFException;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import logbook.constants.AppConstants;
import logbook.dto.BattleExDto;
import logbook.dto.BattleResultDto;
import logbook.gui.logic.IntegerPair;
import logbook.scripting.BattleLogListener;
import logbook.scripting.BattleLogProxy;
import logbook.util.ReportUtils;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.widgets.Display;

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
    private static final LoggerHolder LOG = new LoggerHolder(BattleResultServer.class);

    private static DateFormat format = new SimpleDateFormat(AppConstants.BATTLE_LOGFILE_DATE_FORMAT);

    private static Schema<BattleExDto> schema = RuntimeSchema.getSchema(BattleExDto.class);

    private static class BattleResult extends BattleResultDto {
        public DataFile file;
        public int index;

        BattleResult(BattleExDto dto, DataFile file, int index, Comparable[] extData) {
            super(dto, extData);
            this.file = file;
            this.index = index;
        }
    }

    private static String logPath = null;
    private static volatile BattleResultServer instance = new BattleResultServer();

    private static List<Runnable> eventListeners = new ArrayList<>();

    public static void setLogPath(String path) {
        logPath = path;
    }

    public static void addListener(Runnable listener) {
        eventListeners.add(listener);
    }

    public static void removeListener(Runnable listener) {
        eventListeners.remove(listener);
    }

    private static void fireEvent() {
        for (Runnable listener : eventListeners) {
            listener.run();
        }
    }

    public static void load() {
        final BattleResultServer data = new BattleResultServer(logPath);
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                // 一時的にストアしてたのを処理する
                for (BattleExDto dto : instance.tmpDat) {
                    data.addNewResult(dto);
                }
                instance = data;
                fireEvent();

            }
        });
    }

    public static void dispose() {
        instance = null;
    }

    public static BattleResultServer get() {
        return instance;
    }

    // member
    private final String path;
    private final LinkedBuffer buffer = LinkedBuffer.allocate(128 * 1024);

    // フィルタ用
    private Date firstBattleTime;
    private Date lastBattleTime;
    private final Set<String> dropShipList = new TreeSet<String>();
    private final Set<IntegerPair> mapList = new TreeSet<IntegerPair>();
    private final Set<Integer> cellList = new TreeSet<Integer>();

    private final List<BattleResult> resultList = new ArrayList<BattleResult>();
    private final Map<String, DataFile> fileMap = new HashMap<>();

    // 重複検出用
    private final Set<Date> resultDateSet = new HashSet<Date>();

    // キャッシュ
    private DataFile cachedFile;
    private List<BattleExDto> cachedResult;

    // 一時ストア
    private List<BattleExDto> tmpDat = null;

    private abstract class DataFile {
        final File file;
        int numRecords = 0;

        public DataFile(File file) {
            this.file = file;
        }

        public List<BattleExDto> readAll() throws IOException {
            throw new UnsupportedOperationException();
        }

        public String getPath() {
            throw new UnsupportedOperationException();
        }

        public void addToFile(BattleExDto dto) {
            throw new UnsupportedOperationException();
        }

        public int getNumRecords() {
            return this.numRecords;
        }

        List<BattleExDto> load(InputStream input) throws IOException {
            List<BattleExDto> result = loadFromInputStream(input, BattleResultServer.this.buffer);
            this.numRecords = result.size();
            return result;
        }

    }

    private class NormalDataFile extends DataFile {

        public NormalDataFile(File file) {
            super(file);
        }

        @Override
        public List<BattleExDto> readAll() throws IOException {
            try (InputStream input = new FileInputStream(this.file)) {
                return this.load(input);
            }
        }

        @Override
        public String getPath() {
            return this.file.getAbsolutePath();
        }

        @Override
        public void addToFile(BattleExDto dto) {
            // ファイルとリストに追加
            try (FileOutputStream output = new FileOutputStream(getStoreFile(this.file), true)) {
                ProtostuffIOUtil.writeDelimitedTo(output, dto, schema, BattleResultServer.this.buffer);
                BattleResultServer.this.buffer.clear();
            } catch (IOException e) {
                LOG.get().warn("出撃ログの書き込みに失敗しました", e);
            }
            ++this.numRecords;
        }
    }

    private class ZipDataFile extends DataFile {

        private final String zipName;

        public ZipDataFile(File file, String zipName) {
            super(file);
            this.zipName = zipName;
        }

        @Override
        public List<BattleExDto> readAll() throws IOException {
            try (ZipFile zipFile = new ZipFile(this.file)) {
                try (InputStream input = zipFile.getInputStream(zipFile.getEntry(this.zipName))) {
                    return this.load(input);
                }
            }
        }

        @Override
        public String getPath() {
            return this.file.getAbsolutePath() + ":" + this.zipName;
        }
    }

    private static List<BattleExDto> loadFromInputStream(InputStream input, LinkedBuffer buffer) throws IOException {
        List<BattleExDto> result = new ArrayList<BattleExDto>();
        try {
            while (input.available() > 0) {
                BattleExDto battle = schema.newMessage();
                ProtostuffIOUtil.mergeDelimitedFrom(input, battle, schema, buffer);
                battle.readFromJson();
                result.add(battle);
            }
        } catch (EOFException e) {
        }
        return result;
    }

    private BattleResultServer() {
        this.path = null;
        this.firstBattleTime = new Date();
        this.lastBattleTime = new Date();
        // とりあえず貯める
        this.tmpDat = new ArrayList<>();
    }

    private BattleResultServer(String path) {
        this.path = path;
        // ファイルを読み込んで resultList を作成
        File dir = new File(path);
        if (dir.exists()) {
            // ファイルリストを作成
            for (File file : FileUtils.listFiles(dir, new String[] { "dat", "zip" }, true)) {
                try {
                    if (file.getName().endsWith("dat")) {
                        DataFile dataFile = new NormalDataFile(file);
                        this.fileMap.put(dataFile.getPath(), dataFile);
                    }
                    else {
                        try (ZipFile zipFile = new ZipFile(file)) {
                            Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
                            while (enumeration.hasMoreElements()) {
                                ZipEntry entry = enumeration.nextElement();
                                DataFile dataFile = new ZipDataFile(file, entry.getName());
                                this.fileMap.put(dataFile.getPath(), dataFile);
                            }
                        }
                    }
                } catch (IOException e) {
                    LOG.get().warn("出撃ログの読み込みに失敗しました (" + file.getPath() + ")", e);
                }
            }
            this.reloadFiles();
        }

        // フィルタ用パラメータを計算
        this.firstBattleTime = new Date();
        this.lastBattleTime = new Date(0);
        for (BattleResult battle : this.resultList) {
            this.update(battle);
        }
    }

    public void reloadFiles() {
        this.resultDateSet.clear();
        this.resultList.clear();

        BattleLogListener battleLogScript = BattleLogProxy.get();

        battleLogScript.begin();
        // 全部読み込む
        for (DataFile file : this.fileMap.values()) {
            try {
                List<BattleExDto> result = file.readAll();
                for (int i = 0; i < result.size(); ++i) {
                    BattleExDto dto = result.get(i);
                    if (dto.isCompleteResult() && !this.resultDateSet.contains(dto.getBattleDate())) {
                        this.resultDateSet.add(dto.getBattleDate());
                        this.resultList.add(new BattleResult(dto, file, i,
                                battleLogScript.body(dto)));
                    }
                }
            } catch (IOException e) {
                LOG.get().warn("出撃ログの読み込みに失敗しました (" + file.getPath() + ")", e);
            }
        }
        battleLogScript.end();

        // 時刻でソート
        Collections.sort(this.resultList, new Comparator<BattleResult>() {
            @Override
            public int compare(BattleResult arg0, BattleResult arg1) {
                return Long.compare(
                        arg0.getBattleDate().getTime(), arg1.getBattleDate().getTime());
            }
        });

        fireEvent();
    }

    private void update(BattleResultDto battle) {
        Date battleDate = battle.getBattleDate();
        if (battleDate.before(this.firstBattleTime)) {
            this.firstBattleTime = battleDate;
        }
        if (battleDate.after(this.lastBattleTime)) {
            this.lastBattleTime = battleDate;
        }
        if (battle.isPractice() == false) {
            if (!StringUtils.isEmpty(battle.getDropName())) {
                this.dropShipList.add(battle.getDropName());
            }
            if (!StringUtils.isEmpty(battle.getDropItemName())) {
                this.dropShipList.add(battle.getDropItemName());
            }

            int[] map = battle.getMapCell().getMap();
            this.mapList.add(new IntegerPair(map[0], map[1], "%d-%d"));
            this.cellList.add(map[2]);
        }
    }

    public void addNewResult(BattleExDto dto) {
        // ファイルとリストに追加
        if (dto.isCompleteResult()) {
            if (this.tmpDat != null) {
                this.tmpDat.add(dto);
            }
            else {
                File file = new File(FilenameUtils.concat(this.path, format.format(dto.getBattleDate()) + ".dat"));
                DataFile dataFile = this.fileMap.get(file.getAbsolutePath());
                if (dataFile == null) {
                    dataFile = new NormalDataFile(file);
                    this.fileMap.put(dataFile.getPath(), dataFile);
                }

                BattleLogListener battleLogScript = BattleLogProxy.get();
                BattleResult resultEntry = new BattleResult(dto, dataFile, dataFile.getNumRecords(),
                        battleLogScript.body(dto));
                this.update(resultEntry);
                this.resultList.add(resultEntry);

                dataFile.addToFile(dto);

                // キャッシュされているときはキャッシュにも追加
                if ((this.cachedFile != null) && (dataFile == this.cachedFile)) {
                    this.cachedResult.add(dto);
                }
            }

            fireEvent();
        }
    }

    public int size() {
        return this.resultList.size();
    }

    public BattleResultDto[] getList() {
        return this.resultList.toArray(new BattleResultDto[this.resultList.size()]);
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
        if ((filter.dropShip != null) &&
                (filter.dropShip.equals(dto.getDropName()) == false) &&
                (filter.dropShip.equals(dto.getDropItemName()) == false)) {
            return false;
        }
        if (filter.timeSpan != null) {
            Date from = filter.timeSpan.getFrom();
            Date to = filter.timeSpan.getTo();
            if (from.after(dto.getBattleDate())) {
                return false;
            }
            if (to.before(dto.getBattleDate())) {
                return false;
            }
        }
        if ((filter.map != null)) {
            if (dto.isPractice()) {
                return false;
            }
            int[] battleMap = dto.getMapCell().getMap();
            if (filter.map.compareTo(new IntegerPair(battleMap[0], battleMap[1], "%d-%d")) != 0) {
                return false;
            }
        }
        if ((filter.cell != null)) {
            if (dto.isPractice()) {
                return false;
            }
            int[] battleMap = dto.getMapCell().getMap();
            if (filter.cell != battleMap[2]) {
                return false;
            }
        }
        if ((filter.rank != null) && (filter.rank.equals(dto.getRank()) == false)) {
            return false;
        }
        if (filter.printPractice != null) {
            // 排他的論理和です
            if (dto.isPractice() ^ filter.printPractice) {
                return false;
            }
        }
        return true;
    }

    /** 詳細を読み込む（失敗したら null ） */
    public BattleExDto getBattleDetail(BattleResultDto summary) {
        BattleResult result = (BattleResult) summary;
        if ((this.cachedFile == null) || (result.file != this.cachedFile)) {
            try {
                this.cachedResult = result.file.readAll();
                this.cachedFile = result.file;
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
        return new ArrayList<String>(this.dropShipList);
    }

    public List<IntegerPair> getMapList() {
        return new ArrayList<IntegerPair>(this.mapList);
    }

    public List<Integer> getCellList() {
        return new ArrayList<Integer>(this.cellList);
    }

    private static File getStoreFile(File file) throws IOException {
        // 報告書の保存先にファイルを保存します
        File dir = file.getParentFile();
        if ((dir == null) || !(dir.exists() || dir.mkdirs())) {
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
        try (OutputStream report_stream = new FileOutputStream(report, true)) {
            try (InputStream alt_stream = new FileInputStream(alt_report)) {
                IOUtils.copy(alt_stream, report_stream);
            }
        }
        alt_report.delete();
    }
}
