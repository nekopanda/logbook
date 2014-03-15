/**
 * 
 */
package logbook.server.web;

import java.io.IOException;
import java.util.Date;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import logbook.data.context.GlobalContext;
import logbook.dto.DeckMissionDto;
import logbook.dto.DockDto;
import logbook.dto.NdockDto;
import logbook.dto.ShipDto;

import org.eclipse.swt.widgets.Display;

/**
 * @author Koji Ueno
 *
 */
public class QueryHandler extends HttpServlet {

    /**
     * 
     */
    private static final long serialVersionUID = -2833563128459893536L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //
        //String qid_str = req.getParameter("qid");
        //System.out.println("qid=" + qid_str);

        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");

        JsonWriter writer = Json.createWriter(resp.getOutputStream());
        writer.writeObject(createJsonRespons());
        writer.close();
    }

    private static JsonObject createJsonRespons() {
        final JsonObjectBuilder jb = Json.createObjectBuilder();
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                { // 資源量を配列で追加
                    JsonArrayBuilder materials_array = Json.createArrayBuilder();
                    for (int material : GlobalContext.getMaterials()) {
                        materials_array.add(material);
                    }
                    jb.add("materials", materials_array);
                }

                { // 艦娘リストを配列で追加
                    JsonArrayBuilder ship_array = Json.createArrayBuilder();
                    for (ShipDto ship : GlobalContext.getShipMap().values()) {
                        ship_array.add(Json.createObjectBuilder()
                                .add("id", ship.getId())
                                .add("ship_id", ship.getShipId())
                                .add("char_id", ship.getCharId())
                                .add("ship_type", ship.getShipInfo().getStype())
                                .add("level", ship.getLv())
                                .add("cond", ship.getCond())
                                .add("cond_clear_time", ship.getCondClearTime().getTimeInMillis())
                                .add("bull", ship.getBull())
                                .add("bull_max", ship.getBullMax())
                                .add("fuel", ship.getFuel())
                                .add("fuel_max", ship.getFuelMax())
                                .add("now_hp", ship.getNowhp())
                                .add("max_hp", ship.getMaxhp())
                                .add("dock_time", ship.getDocktime()));
                    }
                    jb.add("ships", ship_array);
                }

                {// 艦隊情報を配列で追加
                    JsonArrayBuilder dock_array = Json.createArrayBuilder();
                    for (int i = 0; i < 4; i++) {
                        DockDto dock = GlobalContext.getDock(Integer.toString(i + 1));
                        if (dock != null) {
                            JsonArrayBuilder dock_ship_array = Json.createArrayBuilder();
                            for (ShipDto ship : dock.getShips()) {
                                dock_ship_array.add(ship.getId());
                            }
                            dock_array.add(Json.createObjectBuilder()
                                    .add("name", dock.getName())
                                    .add("ships", dock_ship_array));
                        }
                    }
                    jb.add("dock", dock_array);
                }

                { // 入渠ドック情報
                    JsonArrayBuilder ndock_root = Json.createArrayBuilder();
                    for (NdockDto ndock : GlobalContext.getNdocks()) {
                        JsonArrayBuilder ndock_ship = Json.createArrayBuilder();
                        if (ndock.getNdockid() != 0) {
                            ndock_ship.add(ndock.getNdockid());
                            ndock_ship.add(ndock.getNdocktime().getTime());
                        }
                        else {
                            ndock_ship.add(-1);
                            ndock_ship.add(0);
                        }
                        ndock_root.add(ndock_ship);
                    }
                    jb.add("ndock", ndock_root);
                }

                { // 遠征情報
                    JsonArrayBuilder mission_root = Json.createArrayBuilder();
                    for (DeckMissionDto mission : GlobalContext.getDeckMissions()) {
                        JsonArrayBuilder mission_item = Json.createArrayBuilder();
                        Date comp_time = mission.getTime();
                        if (comp_time != null) {
                            mission_item.add(mission.getMissionId());
                            mission_item.add(comp_time.getTime());
                        }
                        else {
                            mission_item.add(-1);
                            mission_item.add(0);
                        }
                        mission_root.add(mission_item);
                    }
                    jb.add("mission", mission_root);
                }

                { // 出撃
                    JsonArrayBuilder sortie = Json.createArrayBuilder();
                    for (boolean mission : GlobalContext.getIsSortie()) {
                        sortie.add(mission);
                    }
                    jb.add("sortie", sortie);
                }
            }
        });

        return jb.build();
    }
}
