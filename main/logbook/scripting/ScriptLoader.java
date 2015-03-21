/**
 * 
 */
package logbook.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import logbook.constants.AppConstants;
import logbook.gui.logic.TableItemCreator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.AbstractFileFilter;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Nekopanda
 *
 */
public class ScriptLoader {
    private static ScriptLoader instance = null;

    private static Logger LOG = LogManager.getLogger("script");

    public static interface MethodInvoke {
        public Object invoke(Object arg);
    }

    public class Script {

        public File scriptFile;
        public long lastModified;
        public ScriptEngine engine;
        private final Class<?> type;
        public Object listener;

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
                LOG.warn("スクリプトファイル " + scriptFile.getPath() + " を読み込み中にエラー", e);
            }
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
                LOG.warn("スクリプトファイル " + this.scriptFile.getPath() + "  を読み込み中にエラー", e);
            }
        }

        public void reload_() throws IOException, ScriptException {
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
                    LOG.warn(this.scriptFile.getPath() + " を実行中にエラー", e);
                    if (this.errorCounter == 20) {
                        LOG.warn(this.scriptFile.getPath() + " はこれ以上エラーを記録しません");
                    }
                }
            }
            return null;
        }
    }

    public class ScriptCollection {
        private final String prefix;
        private final Class<?> type;
        private final Map<String, Script> scripts = new TreeMap<>();

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
            for (File file : this.getScriptFiles()) {
                this.scripts.put(file.getPath(), this.makeScript(file, this.type));
            }
        }

        private File[] getScriptFiles() {
            final String starts = this.prefix + "_";
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

        public void invoke(MethodInvoke invokable) {
            for (Script script : this.get()) {
                script.invoke(invokable);
            }
        }
    }

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
    private final Map<String, ScriptCollection> scriptCollections = new TreeMap<>();
    private final Map<String, Script> scripts = new TreeMap<>();

    static {
        instance = new ScriptLoader();
    }

    private ScriptLoader() {
        if (AppConstants.SCRIPT_DIR.exists() == false) {
            try {
                FileUtils.copyDirectory(new File("./templates/script"), AppConstants.SCRIPT_DIR);
            } catch (IOException e) {
                LOG.warn("スクリプトをテンプレートからコピー中にエラー", e);
            }
        }
    }

    public static TableScriptCollection getTableScript(String prefix, Class<?> type) {
        return instance.getTableScript_(prefix, type);
    }

    public static ScriptCollection getScriptCollection(String prefix, Class<?> type) {
        return instance.getScriptCollection_(prefix, type);
    }

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
            script.reload();
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
            script.reload();
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
