/**
 * 
 */
package logbook.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import logbook.config.ItemMasterConfig;
import logbook.dto.ItemDto;
import logbook.internal.Item;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Nekopanda
 *
 */
public class ItemInfoToCSV {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        ItemMasterConfig.load();
        OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream("itemInfo.csv"), "MS932");

        fw.write(StringUtils.join(new String[] {
                "名前", "ID", "大分類", "種別", "装備種別", "表示分類", "火力", "雷装", "爆装", "対空", "対潜", "索敵", "命中", "射程", "運", "雷撃命中" },
                ','));
        fw.write("\n");

        for (Integer key : Item.keySet()) {
            ItemDto dto = Item.get(key);
            if (dto.getName().length() > 0) {
                fw.write(StringUtils.join(new String[] {
                        dto.getName(), // 名前
                        Integer.toString(dto.getId()), // ID
                        Integer.toString(dto.getType0()), // 
                        Integer.toString(dto.getType1()),
                        Integer.toString(dto.getType2()),
                        Integer.toString(dto.getType3()),
                        Integer.toString(dto.getHoug()),
                        Integer.toString(dto.getRaig()),
                        Integer.toString(dto.getBaku()),
                        Integer.toString(dto.getTyku()),
                        Integer.toString(dto.getTais()),
                        Integer.toString(dto.getSaku()),
                        Integer.toString(dto.getHoum()),
                        Integer.toString(dto.getLeng()),
                        Integer.toString(dto.getLuck()),
                        Integer.toString(dto.getRaim()) }, ','));
                fw.write("\n");
            }
        }

        fw.close();
    }
}
