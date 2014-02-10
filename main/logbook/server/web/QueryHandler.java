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
        String qid_str = req.getParameter("qid");
        System.out.println("qid=" + qid_str);

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
                { // 艦娘リストを配列で追加
                    JsonArrayBuilder ship_array = Json.createArrayBuilder();
                    for (ShipDto ship : GlobalContext.getShipMap().values()) {
                        ship_array.add(Json.createObjectBuilder()
                                .add("id", ship.getId())
                                .add("ship_id", ship.getShipId())
                                .add("char_id", ship.getCharId())
                                .add("level", ship.getLv())
                                .add("cond", ship.getCond())
                                .add("cond_clear_time", ship.getCondClearTime().getTimeInMillis())
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
                    JsonArrayBuilder ndock_ships = Json.createArrayBuilder();
                    JsonArrayBuilder ndock_comp_time = Json.createArrayBuilder();
                    for (NdockDto ndock : GlobalContext.getNdocks()) {
                        if (ndock.getNdockid() != 0) {
                            ndock_ships.add(ndock.getNdockid());
                            ndock_comp_time.add(ndock.getNdocktime().getTime());
                        }
                        else {
                            ndock_ships.add(-1);
                            ndock_comp_time.add(0);
                        }
                    }
                    jb.add("ndock_ships", ndock_ships);
                    jb.add("ndock_complete_time", ndock_comp_time);
                }

                { // 遠征情報
                    JsonArrayBuilder mission_comp_time = Json.createArrayBuilder();
                    for (DeckMissionDto mission : GlobalContext.getDeckMissions()) {
                        Date comp_time = mission.getTime();
                        if (comp_time != null)
                            mission_comp_time.add(comp_time.getTime());
                        else
                            mission_comp_time.add(0);
                    }
                    jb.add("mission_complete_time", mission_comp_time);
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
