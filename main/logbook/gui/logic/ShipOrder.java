/**
 * 
 */
package logbook.gui.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import logbook.data.context.GlobalContext;
import logbook.dto.ShipDto;

/**
 * @author Nekopanda
 *
 */
public class ShipOrder {
    public ShipDto ship;
    /** Lv順, 艦種順, NEW順, 修理順 (ゼロ始まり) */
    public int[] sortNumber = new int[4];

    public ShipOrder(ShipDto ship) {
        this.ship = ship;
    }

    private static class ShipComparatorBase implements Comparator<ShipOrder> {
        @Override
        public int compare(ShipOrder o1, ShipOrder o2) {
            int ret = Integer.compare(o1.ship.getSortno(), o2.ship.getSortno());
            if (ret == 0) {
                ret = Integer.compare(o1.ship.getId(), o2.ship.getId());
            }
            return ret;
        }
    }

    private static void genSortNumber(List<ShipOrder> list, int index, Comparator<ShipOrder> comp) {
        Collections.sort(list, comp);
        for (int i = 0; i < list.size(); ++i) {
            list.get(i).sortNumber[index] = i;
        }
    }

    public static List<ShipOrder> getOrderedShipList() {
        List<ShipOrder> ships = new ArrayList<ShipOrder>();
        for (ShipDto ship : GlobalContext.getShipMap().values()) {
            ships.add(new ShipOrder(ship));
        }
        // Lv順
        genSortNumber(ships, 0, new ShipComparatorBase() {
            @Override
            public int compare(ShipOrder o1, ShipOrder o2) {
                int ret = -Integer.compare(o1.ship.getLv(), o2.ship.getLv());
                if (ret == 0) {
                    return super.compare(o1, o2);
                }
                return ret;
            }
        });
        // 艦種順
        genSortNumber(ships, 1, new Comparator<ShipOrder>() {
            @Override
            public int compare(ShipOrder o1, ShipOrder o2) {
                int ret = -Integer.compare(o1.ship.getShipInfo().getStype(), o2.ship.getShipInfo().getStype());
                if (ret == 0) {
                    ret = Integer.compare(o1.ship.getSortno(), o2.ship.getSortno());
                    /*// Lv順の後に安定ソートするので
                    if (ret == 0) {
                        ret = -Integer.compare(o1.ship.getLv(), o2.ship.getLv());
                        if (ret == 0) {
                            ret = Integer.compare(o1.ship.getId(), o2.ship.getId());
                        }
                    }
                    */
                }
                return ret;
            }
        });
        // NEW
        genSortNumber(ships, 2, new ShipComparatorBase() {
            @Override
            public int compare(ShipOrder o1, ShipOrder o2) {
                return -Integer.compare(o1.ship.getId(), o2.ship.getId());
            }
        });
        // 修理順
        genSortNumber(ships, 3, new ShipComparatorBase() {
            @Override
            public int compare(ShipOrder o1, ShipOrder o2) {
                double o1rate = (double) o1.ship.getNowhp() / (double) o1.ship.getMaxhp();
                double o2rate = (double) o2.ship.getNowhp() / (double) o2.ship.getMaxhp();
                int ret = Double.compare(o1rate, o2rate);
                if (ret == 0) {
                    return super.compare(o1, o2);
                }
                return ret;
            }
        });
        // 最後にID順にしておく
        Collections.sort(ships, new Comparator<ShipOrder>() {
            @Override
            public int compare(ShipOrder o1, ShipOrder o2) {
                return Integer.compare(o1.ship.getId(), o2.ship.getId());
            }
        });
        return ships;
    }
}
