/**
 * 
 */
package logbook.gui.logic;

import java.util.Arrays;
import java.util.List;

import logbook.data.context.GlobalContext;
import logbook.dto.BattleExDto;
import logbook.dto.DockDto;
import logbook.dto.MapCellDto;
import logbook.gui.ApplicationMain;

/**
 * コンソールを更新するイベントハンドラ
 *
 */
public class MainAppListener {
    private static final int MAX_LOG_LINES = 200;

    private final ApplicationMain main;

    /**
     * コンソールを更新するイベントハンドラ
     * 
     * @param console
     */
    public MainAppListener(ApplicationMain main) {
        this.main = main;
    }

    /**
     * コンソールを更新します
     * @param message コンソールに表示するメッセージ
     */
    public void printMessage(final String message) {
        org.eclipse.swt.widgets.List console = this.main.getConsole();
        int size = console.getItemCount();
        if (size >= MAX_LOG_LINES) {
            console.remove(0);
        }
        console.add(message);
        console.setSelection(console.getItemCount() - 1);
    }

    private List<DockDto> getSortieDocks() {
        boolean[] isSortie = GlobalContext.getIsSortie();
        for (int i = 0; i < 4; i++) {
            if (isSortie[i]) {
                DockDto dock = GlobalContext.getDock(Integer.toString(i + 1));
                if (GlobalContext.isCombined()) {
                    return Arrays.asList(new DockDto[] {
                            dock,
                            GlobalContext.getDock("2")
                    });
                }
                else {
                    return Arrays.asList(new DockDto[] { dock });
                }
            }
        }
        return null;
    }

    public void startSortie() {
        List<DockDto> sortieDocks = this.getSortieDocks();
        if (sortieDocks != null) {
            this.main.getBattleWindow().updateSortieDock(sortieDocks);
            this.main.getBattleShipWindow().updateSortieDock(sortieDocks);
        }
    }

    public void endSortie() {
        this.main.getBattleWindow().endSortie();
        this.main.getBattleShipWindow().endSortie();
    }

    public void updateSortieDock() {
        List<DockDto> sortieDocks = this.getSortieDocks();
        if (sortieDocks != null) {
            this.main.getBattleWindow().updateSortieDock(sortieDocks);
            this.main.getBattleShipWindow().updateSortieDock(sortieDocks);
        }
    }

    public void updateMapCell(MapCellDto mapCellDto) {
        this.main.getBattleWindow().updateMapCell(mapCellDto);
        this.main.getBattleShipWindow().updateMapCell(mapCellDto);
    }

    public void updateBattle(BattleExDto battleDto) {
        this.main.getBattleWindow().updateBattle(battleDto);
        this.main.getBattleShipWindow().updateBattle(battleDto);
    }
}
