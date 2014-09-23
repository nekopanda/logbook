/**
 * 
 */
package logbook.data;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

/**
 * @author Nekopanda
 *
 */
public class TestData implements Data {

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd_HHmmss.SSS");

    private final Date date;
    private final DataType type;
    private final JsonObject json;

    public TestData(String filepath) throws ParseException, IOException {
        String filename = FilenameUtils.getBaseName(filepath);
        int splitpos = filename.indexOf('_');
        splitpos = filename.indexOf('_', splitpos + 1);
        String timeString = filename.substring(0, splitpos);
        String typeString = filename.substring(splitpos + 1);
        this.date = format.parse(timeString);
        this.type = DataType.valueOf(typeString);
        String jsonString = FileUtils.readFileToString(new File(filepath), Charset.forName("MS932"));
        JsonReader jsonreader = Json.createReader(new StringReader(jsonString));
        this.json = jsonreader.readObject();
    }

    @Override
    public DataType getDataType() {
        return this.type;
    }

    @Override
    public Date getCreateDate() {
        return this.date;
    }

    @Override
    public JsonObject getJsonObject() {
        return this.json;
    }

    @Override
    public String getField(String key) {
        if ((this.type == DataType.START) && key.equals("api_deck_id")) {
            // 出撃は第一にしておく
            return "1";
        }
        return "-1";
    }

}
