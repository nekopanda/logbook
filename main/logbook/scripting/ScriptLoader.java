/**
 * 
 */
package logbook.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.script.Compilable;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import logbook.constants.AppConstants;
import logbook.gui.logic.TableItemCreator;
import logbook.internal.LoggerHolder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.ArrayUtils;

/**
 * スクリプトローダ
 * @author Nekopanda
 */
public class ScriptLoader {
    private static ScriptLoader instance = null;

    private static LoggerHolder LOG = new LoggerHolder("script");

    public static interface MethodInvoke {
        public Object invoke(Object arg);
    }

    public class Script {

        private final File scriptFile;
        private long lastModified;
        private ScriptEngine engine;
        private final Class<?> type;
        private Object listener;

        public boolean exception = false;
        public int errorCounter = 0;

        public Script(File scriptFile, Class<?> type, boolean load) {
            this.scriptFile = scriptFile;
            this.lastModified = scriptFile.lastModified();
            this.type = type;
            try {
                if (load) {
                    this.reload_();
                }
            } catch (ScriptException | IOException e) {
                this.listener = null;
                LOG.get().warn("スクリプトファイル " + scriptFile.getPath() + " を読み込み中にエラー", e);
            }
            ScriptLoader.this.allScripts.put(scriptFile.getName(), this);
        }

        public Script(File scriptFile, Class<?> type) {
            this(scriptFile, type, true);
        }

        public boolean isUpdated() {
            return (this.lastModified != this.scriptFile.lastModified());
        }

        public void reload() {
            try {
                this.lastModified = this.scriptFile.lastModified();
                if (!this.scriptFile.exists()) {
                    this.listener = null;
                    return;
                }
                this.reload_();
            } catch (ScriptException | IOException e) {
                this.listener = null;
                LOG.get().warn("スクリプトファイル " + this.scriptFile.getPath() + "  を読み込み中にエラー", e);
            }
        }

        private void reload_() throws IOException, ScriptException {
            try (BufferedReader reader = Files.newBufferedReader(this.scriptFile.toPath(), Charset.forName("UTF-8"))) {
                this.engine = ScriptLoader.this.manager.getEngineByName("nashorn");
                if (this.engine == null) {
                    this.engine = ScriptLoader.this.manager.getEngineByExtension("js");
                    if (this.engine == null) {
                        throw new ScriptException("javascriptエンジンが見つかりません");
                    }
                }

                // eval
                //this.engine.eval(reader);
                ((Compilable) this.engine).compile(reader).eval();
                // 実装を取得
                this.listener = ((Invocable) this.engine).getInterface(this.type);

                if (this.listener == null) {
                    throw new ScriptException("スクリプトが " + this.type.getName() + " インターフェースを実装していません");
                }
            }
            this.errorCounter = 0;
        }

        public Object invoke(MethodInvoke invokable) {
            try {
                if (this.listener == null) {
                    return null;
                }
                this.exception = false;
                return invokable.invoke(this.listener);
            } catch (Exception e) {
                this.exception = true;
                if (this.errorCounter++ < 20) {
                    LOG.get().warn(this.scriptFile.getPath() + " を実行中にエラー", e);
                    if (this.errorCounter == 20) {
                        LOG.get().warn(this.scriptFile.getPath() + " はこれ以上エラーを記録しません");
                    }
                }
            }
            return null;
        }
    }

    /**
     * "prefix_*.js"にマッチするスクリプトの集合
     * @author Nekopanda
     */
    public class ScriptCollection {
        private final String prefix;
        private final Class<?> type;
        private Map<String, Script> scripts = new TreeMap<>();

        public ScriptCollection(String prefix, Class<?> type) {
            this.prefix = prefix;
            this.type = type;
            this.loadScripts();
        }

        public Script makeScript(File file, Class<?> type) {
            return new Script(file, type);
        }

        public Collection<Script> get() {
            return this.scripts.values();
        }

        private void loadScripts() {
            Map<String, Script> oldScripts = this.scripts;
            this.scripts = new TreeMap<>();
            for (File file : this.getScriptFiles()) {
                Script script = oldScripts.get(file.getPath());
                if ((script == null)) {
                    script = this.makeScript(file, this.type);
                }
                else if (script.isUpdated()) {
                    script.reload();
                }
                this.scripts.put(file.getPath(), script);
            }
        }

        private File[] getScriptFiles() {
            final String starts = this.prefix + "_";
            if (AppConstants.SCRIPT_DIR.exists() == false) {
                return new File[0];
            }
            File[] array = FileUtils.listFiles(AppConstants.SCRIPT_DIR, new AbstractFileFilter() {
                @Override
                public boolean accept(File file) {
                    String name = file.getName();
                    return name.endsWith(".js") && name.startsWith(starts);
                }
            }, FileFilterUtils.trueFileFilter()).toArray(new File[0]);
            Arrays.sort(array, new Comparator<File>() {
                @Override
                public int compare(File arg0, File arg1) {
                    return arg0.getPath().compareTo(arg1.getPath());
                }
            });
            return array;
        }

        public boolean isUpdated() {
            File[] files = this.getScriptFiles();
            if (this.scripts.size() != files.length) {
                return true;
            }
            for (File file : files) {
                Script script = this.scripts.get(file.getPath());
                if (script == null) {
                    return true;
                }
                if (script.isUpdated()) {
                    return true;
                }
            }
            return false;
        }

        public void reload() {
            this.scripts.clear();
            this.loadScripts();
        }

        public void update() {
            this.loadScripts();
        }

        /**
         * 各スクリプトで実行
         * @param invokable 実行するメソッド
         */
        public void invoke(MethodInvoke invokable) {
            for (Script script : this.get()) {
                script.invoke(invokable);
            }
        }
    }

    /**
     * 各種テーブルのカラム拡張用スクリプト
     * @author Nekopanda
     */
    public class TableScript extends Script {
        private final MethodInvoke headerMethod = new MethodInvoke() {
            @Override
            public Object invoke(Object arg) {
                return ((TableScriptListener) arg).header();
            }
        };

        private final String[] header;
        private final Comparable[] exceptionBody;

        public TableScript(File scriptFile, Class<?> type) {
            super(scriptFile, type);
            this.header = (String[]) this.invoke(this.headerMethod);
            if (this.header != null) {
                this.exceptionBody = new Comparable[this.header.length];
                for (int i = 0; i < this.exceptionBody.length; ++i) {
                    this.exceptionBody[i] = "例外が発生しました";
                }
            }
            else {
                this.exceptionBody = null;
            }
        }

        public String[] header() {
            return this.header;
        }

        public Comparable[] body(MethodInvoke invokable) {
            if (this.header == null) {
                return null;
            }
            Comparable[] raw = (Comparable[]) this.invoke(invokable);
            if (this.exception) {
                return this.exceptionBody;
            }
            if ((raw != null) && (raw.length == this.header.length)) {
                return raw;
            }
            // 長さを合わせる
            return this.resize(raw);
        }

        private Comparable[] resize(Comparable[] raw) {
            Comparable[] ret = new Comparable[this.header.length];
            if (raw == null) {
                return ret;
            }
            for (int i = 0; i < ret.length; ++i) {
                if (i < raw.length) {
                    ret[i] = raw[i];
                }
            }
            return ret;
        }
    }

    /**
     * テーブルカラム拡張用スクリプトの集合
     * テーブルヘッダは起動中変更できないので、reloadでファイルが増減しないようになっています
     * @author Nekopanda
     */
    public class TableScriptCollection extends ScriptCollection {

        public TableScriptCollection(String prefix, Class<?> type) {
            super(prefix, type);
        }

        @Override
        public Script makeScript(File file, Class<?> type) {
            return new TableScript(file, type);
        }

        @Override
        public void reload() {
            // テーブルヘッダは起動中変更できないので、ファイルは増減させない
            for (Script script : this.get()) {
                script.reload();
            }
        }

        @Override
        public void update() {
            // テーブルヘッダは起動中変更できないので、ファイルは増減させない
            for (Script script : this.get()) {
                if (script.isUpdated()) {
                    script.reload();
                }
            }
        }

        @Override
        public boolean isUpdated() {
            // テーブルヘッダは起動中変更できないので、ファイルは増減させない
            for (Script script : this.get()) {
                if (script.isUpdated()) {
                    return true;
                }
            }
            return false;
        }

        public String[] header() {
            String[] result = null;
            for (Script script : this.get()) {
                result = ArrayUtils.addAll(result, ((TableScript) script).header());
            }
            return result;
        }

        public Comparable[] body(MethodInvoke invokable) {
            Comparable[] result = null;
            for (Script script : this.get()) {
                result = ArrayUtils.addAll(result, ((TableScript) script).body(invokable));
            }
            return result;
        }
    }

    private final ScriptEngineManager manager = new ScriptEngineManager();
    private final Map<String, Script> allScripts = new HashMap<>();
    private final Map<String, ScriptCollection> scriptCollections = new TreeMap<>();
    private final Map<String, Script> scripts = new TreeMap<>();

    static {
        instance = new ScriptLoader();
    }

    private ScriptLoader() {
        final File sourceDir = new File("./templates/script");
        final Set<String> ignoreList = new HashSet<>();

        File ignoreFile = new File(AppConstants.SCRIPT_DIR + "/ignore_update.txt");
        if (ignoreFile.exists()) {
            try {
                for (String filename : FileUtils.readLines(ignoreFile)) {
                    ignoreList.add(filename);
                }
            } catch (IOException e) {
                LOG.get().warn("除外リストファイル読み込み中にエラー", e);
            }
        }

        try {
            FileUtils.copyDirectory(new File("./templates/script"), AppConstants.SCRIPT_DIR, new FileFilter() {
                @Override
                public boolean accept(File src) {
                    if (ignoreList.contains(src.getName())) {
                        LOG.get().info("除外されているためアップデートされません: " + src.getAbsolutePath());
                        return false;
                    }
                    File dstFile = new File(AppConstants.SCRIPT_DIR.getAbsolutePath() +
                            src.getAbsolutePath().substring(sourceDir.getAbsolutePath().length()));
                    // 新規ファイルまたは更新されていたらコピー
                    return ((dstFile.exists() == false) ||
                    (dstFile.lastModified() < src.lastModified()));
                }
            });

            // 除外リストファイルを作っておく
            if (ignoreFile.exists() == false) {
                ignoreFile.createNewFile();
            }
        } catch (IOException e) {
            LOG.get().warn("スクリプトをテンプレートからコピー中にエラー", e);
        }
    }

    /**
     * prefixにマッチするテーブルカラム拡張用スクリプト集合を取得
     * @param prefix
     * @param type
     * @return
     */
    public static TableScriptCollection getTableScript(String prefix, Class<?> type) {
        return instance.getTableScript_(prefix, type);
    }

    /**
     * prefixにマッチするスクリプト集合を取得
     * @param prefix
     * @param type
     * @return
     */
    public static ScriptCollection getScriptCollection(String prefix, Class<?> type) {
        return instance.getScriptCollection_(prefix, type);
    }

    /**
     * テープル行を作るスクリプトを取得
     * @param prefix
     * @return
     */
    public static Script getTableStyleScript(String prefix) {
        return instance.getTableStyleScript_(prefix);
    }

    private synchronized TableScriptCollection getTableScript_(String prefix, Class<?> type) {
        ScriptCollection script = this.scriptCollections.get(prefix);
        if (script == null) {
            script = new TableScriptCollection(prefix, type);
            this.scriptCollections.put(prefix, script);
        }
        else if (script.isUpdated()) {
            script.update();
        }
        return (TableScriptCollection) script;
    }

    private synchronized ScriptCollection getScriptCollection_(String prefix, Class<?> type) {
        ScriptCollection script = this.scriptCollections.get(prefix);
        if (script == null) {
            script = new ScriptCollection(prefix, type);
            this.scriptCollections.put(prefix, script);
        }
        else if (script.isUpdated()) {
            script.update();
        }
        return script;
    }

    private File getTableStyleScriptFile(String prefix) {
        return new File(AppConstants.SCRIPT_DIR + "/"
                + prefix + AppConstants.TABLE_STYLE_SUFFIX + ".js");
    }

    private synchronized Script getTableStyleScript_(String prefix) {
        Script script = this.scripts.get(prefix);
        if (script == null) {
            File scriptFile = this.getTableStyleScriptFile(prefix);
            script = new Script(scriptFile, TableItemCreator.class, scriptFile.exists());
            this.scripts.put(prefix, script);
        }
        else if (script.isUpdated()) {
            script.reload();
        }
        return script;
    }
}
