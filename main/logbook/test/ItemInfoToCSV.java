/**
 * 
 */
package logbook.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import logbook.dto.ItemInfoDto;
import logbook.dto.ShipParameters;
import logbook.internal.Item;
import logbook.internal.MasterData;

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
        boolean init = MasterData.INIT_COMPLETE;
        OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream("itemInfo.csv"), "MS932");

        fw.write(StringUtils.join(new String[] {
                "名前", "ID", "大分類", "種別", "装備種別", "表示分類", "火力", "雷装", "爆装", "対空", "対潜", "索敵", "命中", "射程", "運", "雷撃命中" },
                ','));
        fw.write("\n");

        for (Integer key : Item.keySet()) {
            ItemInfoDto dto = Item.get(key);
            ShipParameters param = dto.getParam();
            if (dto.getName().length() > 0) {
                fw.write(StringUtils.join(new String[] {
                        dto.getName(), // 名前
                        Integer.toString(dto.getId()), // ID
                        Integer.toString(dto.getType0()), // 
                        Integer.toString(dto.getType1()),
                        Integer.toString(dto.getType2()),
                        Integer.toString(dto.getType3()),
                        Integer.toString(param.getHoug()),
                        Integer.toString(param.getRaig()),
                        Integer.toString(param.getBaku()),
                        Integer.toString(param.getTyku()),
                        Integer.toString(param.getTais()),
                        Integer.toString(param.getSaku()),
                        Integer.toString(param.getHoum()),
                        Integer.toString(param.getLeng()),
                        Integer.toString(param.getLuck()),
                        Integer.toString(param.getSouk()) }, ','));
                fw.write("\n");
            }
        }

        fw.close();
    }
}
