/**
 * 
 */
package logbook.test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import logbook.dto.ShipInfoDto;
import logbook.internal.MasterData;
import logbook.internal.Ship;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Nekopanda
 *
 */
public class ShipInfoToCSV {

    /**
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        boolean init = MasterData.INIT_COMPLETE;
        OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream("shipInfo.csv"), "MS932");

        fw.write(StringUtils.join(new String[] {
                "名前", "艦ID", "タイプID", "タイプ名", "改造Lv", "改造後の艦ID", "Flagship", "Max弾", "Max燃料" }, ','));
        fw.write("\n");

        for (int key : Ship.getMap().keySet()) {
            ShipInfoDto dto = Ship.get(key);
            if (dto.getName().length() > 0) {
                fw.write(StringUtils.join(new String[] {
                        dto.getName(),
                        Integer.toString(dto.getShipId()),
                        Integer.toString(dto.getStype()),
                        dto.getType(),
                        Integer.toString(dto.getAfterlv()),
                        Integer.toString(dto.getAftershipid()),
                        dto.getFlagship(),
                        Integer.toString(dto.getMaxBull()),
                        Integer.toString(dto.getMaxFuel()) }, ','));
                fw.write("\n");
            }
        }

        fw.close();
    }

}
