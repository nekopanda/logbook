/**
 * 
 */
package logbook.server.web;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import logbook.config.MasterDataConfig;
import logbook.data.context.GlobalContext;
import logbook.dto.BasicInfoDto;
import logbook.dto.BattleDto;
import logbook.dto.DeckMissionDto;
import logbook.dto.DockDto;
import logbook.dto.ItemDto;
import logbook.dto.KdockDto;
import logbook.dto.MapCellDto;
import logbook.dto.MaterialDto;
import logbook.dto.NdockDto;
import logbook.dto.PracticeUserDto;
import logbook.dto.QuestDto;
import logbook.dto.ShipDto;
import logbook.dto.ShipInfoDto;
import logbook.internal.Item;
import logbook.internal.MasterData;
import logbook.internal.MasterData.MapAreaDto;
import logbook.internal.MasterData.MapInfoDto;
import logbook.internal.MasterData.MissionDto;
import logbook.internal.MasterData.ShipTypeDto;
import logbook.internal.Ship;

import org.eclipse.swt.widgets.Display;

/**
 * @author Nekopanda
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

        String requestURI = req.getRequestURI();
        JsonWriter writer = Json.createWriter(resp.getOutputStream());
        if (requestURI.endsWith("query")) {
            writer.writeObject(createQueryRespons());
        }
        else if (requestURI.endsWith("battle")) { // battle
            writer.writeObject(createBattleRespons());
        }
        else { // master
            writer.writeObject(createMasterResponse());
        }
        writer.close();
    }

    private static JsonObjectBuilder shipInfoToJson(ShipInfoDto ship) {
        JsonArrayBuilder powup_array = Json.createArrayBuilder();
        for (int item_number : ship.getPowup()) {
            powup_array.add(item_number);
        }
        JsonArrayBuilder maxeq_array = Json.createArrayBuilder();
        for (int item_number : ship.getMaxeq()) {
            maxeq_array.add(item_number);
        }
        return Json.createObjectBuilder()
                .add("ship_id", ship.getShipId())
                .add("ship_type", ship.getStype())
                .add("name", ship.getName())
                .add("afterlv", ship.getAfterlv())
                .add("aftershipid", ship.getAftershipid())
                .add("powup", powup_array)
                .add("maxeq", maxeq_array);
    }

    private static JsonObjectBuilder itemInfoToJson(ItemDto item) {
        return Json.createObjectBuilder()
                .add("id", item.getId())
                .add("name", item.getName())
                .add("type", item.getType2());
    }

    private static JsonObjectBuilder mapAreaToJson(MapAreaDto item) {
        return Json.createObjectBuilder()
                .add("id", item.getId())
                .add("name", item.getName());
    }

    private static JsonObjectBuilder mapInfoToJson(MapInfoDto item, Integer state) {
        return Json.createObjectBuilder()
                .add("id", item.getId())
                .add("maparea_id", item.getMaparea_id())
                .add("name", item.getName())
                .add("state", (state == null) ? -1 : state)
                .add("no", item.getNo());
    }

    private static JsonObjectBuilder missionToJson(MissionDto item, Integer state) {
        return Json.createObjectBuilder()
                .add("id", item.getId())
                .add("name", item.getName())
                .add("maparea_id", item.getMaparea_id())
                .add("state", (state == null) ? -1 : state)
                .add("time", item.getTime());
    }

    private static JsonObjectBuilder shipTypeToJson(ShipTypeDto item) {
        JsonArrayBuilder equip_type = Json.createArrayBuilder();
        for (Boolean item_number : item.getEquipType()) {
            equip_type.add(item_number);
        }
        return Json.createObjectBuilder()
                .add("id", item.getId())
                .add("name", item.getName())
                .add("equip_type", equip_type);
    }

    private static JsonObject createMasterResponse() {
        final JsonObjectBuilder jb = Json.createObjectBuilder();
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                { // 艦
                    JsonArrayBuilder ship_array = Json.createArrayBuilder();
                    for (String itemid : Ship.keySet()) {
                        ship_array.add(shipInfoToJson(Ship.get(itemid)));
                    }
                    jb.add("master_ships", ship_array);
                }

                { // 装備
                    JsonArrayBuilder item_array = Json.createArrayBuilder();
                    for (int itemid : Item.keySet()) {
                        item_array.add(itemInfoToJson(Item.get(itemid)));
                    }
                    jb.add("master_items", item_array);
                }

                MasterData data = MasterDataConfig.get();

                { // マップ
                    JsonArrayBuilder maparea_array = Json.createArrayBuilder();
                    for (MapAreaDto dto : data.getMaparea()) {
                        maparea_array.add(mapAreaToJson(dto));
                    }
                    jb.add("master_maparea", maparea_array);
                }

                { // マップ
                    JsonArrayBuilder mapinfo_array = Json.createArrayBuilder();
                    Map<Integer, Integer> mapState = data.getMapState();
                    for (MapInfoDto dto : data.getMapinfo().values()) {
                        mapinfo_array.add(mapInfoToJson(dto, mapState.get(dto.getId())));
                    }
                    jb.add("master_mapinfo", mapinfo_array);
                }

                { // 遠征
                    JsonArrayBuilder mission_array = Json.createArrayBuilder();
                    Map<Integer, Integer> missionState = data.getMissionState();
                    for (MissionDto dto : data.getMission().values()) {
                        mission_array.add(missionToJson(dto, missionState.get(dto.getId())));
                    }
                    jb.add("master_mission", mission_array);
                }

                { // 艦種
                    JsonArrayBuilder item_array = Json.createArrayBuilder();
                    for (ShipTypeDto dto : data.getStype()) {
                        item_array.add(shipTypeToJson(dto));
                    }
                    jb.add("master_stype", item_array);
                }

                jb.add("last_update_time", data.getLastUpdateTime().getTime());
            }
        });
        return jb.build();
    }

    private static JsonObjectBuilder shipToJson(ShipDto ship) {
        JsonArrayBuilder slot_array = Json.createArrayBuilder();
        JsonArrayBuilder onSlot_array = Json.createArrayBuilder();
        for (Long item_number : ship.getRawSlot()) {
            slot_array.add(item_number);
        }
        for (Integer item_number : ship.getOnSlot()) {
            onSlot_array.add(item_number);
        }

        // 成長の余地
        long karyoku = ship.getKaryokuMax() - ship.getKaryoku();
        long raisou = ship.getRaisouMax() - ship.getRaisou();
        long taiku = ship.getTaikuMax() - ship.getTaiku();
        long souko = ship.getSoukouMax() - ship.getSoukou();
        long lucky = ship.getLuckyMax() - ship.getLucky();
        for (ItemDto item : ship.getItem()) {
            if (item != null) {
                karyoku += item.getHoug();
                raisou += item.getRaig();
                taiku += item.getTyku();
                lucky += item.getLuck();
            }
        }

        JsonArrayBuilder status_array = Json.createArrayBuilder();
        JsonArrayBuilder statusSpace_array = Json.createArrayBuilder();
        status_array.add(ship.getKaryoku()); // 火力
        status_array.add(ship.getRaisou()); // 雷装
        status_array.add(ship.getTaiku()); // 対空
        status_array.add(ship.getSoukou()); // 装甲
        status_array.add(ship.getLucky()); // 運
        statusSpace_array.add(karyoku); // 火力
        statusSpace_array.add(raisou); // 雷装
        statusSpace_array.add(taiku); // 対空
        statusSpace_array.add(souko); // 装甲
        statusSpace_array.add(lucky); // 運

        return Json.createObjectBuilder()
                .add("id", ship.getId())
                .add("ship_id", ship.getShipId())
                .add("char_id", ship.getCharId())
                .add("ship_type", ship.getShipInfo().getStype())
                .add("level", ship.getLv())
                .add("cond", ship.getEstimatedCond())
                .add("cond_clear_time", ship.getCondClearTime().getTimeInMillis())
                .add("bull", ship.getBull())
                .add("bull_max", ship.getBullMax())
                .add("fuel", ship.getFuel())
                .add("fuel_max", ship.getFuelMax())
                .add("now_hp", ship.getNowhp())
                .add("max_hp", ship.getMaxhp())
                .add("locked", ship.getLocked())
                .add("dock_time", ship.getDocktime())
                .add("slot_num", ship.getSlotNum())
                .add("slot_item", slot_array)
                .add("on_slot", onSlot_array)
                .add("status", status_array)
                .add("status_space", statusSpace_array)
                .add("name", ship.getName());
    }

    private static JsonObjectBuilder itemToJson(int id, ItemDto item) {
        return Json.createObjectBuilder()
                .add("id", id)
                .add("item_id", item.getId());
    }

    private static JsonObjectBuilder questToJson(QuestDto item) {
        return Json.createObjectBuilder()
                .add("no", item.getNo())
                .add("page", item.getPage())
                .add("pos", item.getPos())
                .add("title", item.getTitle())
                .add("state", item.getState());
    }

    private static JsonObject createQueryRespons() {
        final JsonObjectBuilder jb = Json.createObjectBuilder();
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                { // 資源量を配列で追加
                    JsonArrayBuilder materials_array = Json.createArrayBuilder();
                    MaterialDto dto = GlobalContext.getMaterial();
                    if (dto != null) {
                        materials_array.add(dto.getFuel()); // 燃料
                        materials_array.add(dto.getAmmo()); // 弾薬
                        materials_array.add(dto.getMetal()); // 鋼材
                        materials_array.add(dto.getBauxite()); // ボーキ
                        materials_array.add(dto.getBurner()); // 高速建造材
                        materials_array.add(dto.getBucket()); // 高速修理材
                        materials_array.add(dto.getResearch()); // 開発資源
                        jb.add("materials", materials_array);
                    }
                }

                { // 艦娘リストを配列で追加
                    JsonArrayBuilder ship_array = Json.createArrayBuilder();
                    for (ShipDto ship : GlobalContext.getShipMap().values()) {
                        ship_array.add(shipToJson(ship));
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
                        JsonObjectBuilder ndock_ship = Json.createObjectBuilder();
                        if (ndock.getNdockid() != 0) {
                            ndock_ship.add("ship_id", ndock.getNdockid());
                            ndock_ship.add("comp_time", ndock.getNdocktime().getTime());
                        }
                        else {
                            ndock_ship.add("ship_id", -1);
                            ndock_ship.add("comp_time", 0);
                        }
                        ndock_root.add(ndock_ship);
                    }
                    jb.add("ndock", ndock_root);
                }

                { // 建造ドック情報
                    JsonArrayBuilder kdock_root = Json.createArrayBuilder();
                    for (KdockDto kdock : GlobalContext.getKdocks()) {
                        JsonObjectBuilder kdock_item = Json.createObjectBuilder();
                        kdock_item.add("now_using", kdock.getNowUsing());
                        kdock_item.add("comp_time", kdock.getKdocktime() != null ? kdock.getKdocktime().getTime() : 0);
                        kdock_root.add(kdock_item);
                    }
                    jb.add("kdock", kdock_root);
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

                { // 装備
                    JsonArrayBuilder item_array = Json.createArrayBuilder();
                    Map<Long, ItemDto> itemMap = GlobalContext.getItemMap();
                    for (Long id : itemMap.keySet()) {
                        item_array.add(itemToJson(id.intValue(), itemMap.get(id)));
                    }
                    jb.add("items", item_array);
                }

                { // クエスト
                    JsonArrayBuilder quest_array = Json.createArrayBuilder();
                    for (QuestDto quest : GlobalContext.getQuest()) {
                        if (quest == null)
                            continue;
                        quest_array.add(questToJson(quest));
                    }
                    jb.add("quest", quest_array);
                    jb.add("num_quest", GlobalContext.getQuest().size());
                }

                { // 出撃
                    JsonArrayBuilder sortie = Json.createArrayBuilder();
                    for (boolean mission : GlobalContext.getIsSortie()) {
                        sortie.add(mission);
                    }
                    jb.add("sortie", sortie);
                }

                { // 演習相手
                    JsonArrayBuilder ensyu_root = Json.createArrayBuilder();
                    for (PracticeUserDto dto : GlobalContext.getPracticeUser()) {
                        if (dto != null) {
                            JsonObjectBuilder user_item = Json.createObjectBuilder();
                            user_item.add("id", dto.getId());
                            user_item.add("name", dto.getName());
                            user_item.add("state", dto.getState());
                            ensyu_root.add(user_item);
                        }
                    }
                    jb.add("practice", ensyu_root);
                }

                { // 出撃数, 遠征数
                    JsonObjectBuilder basic = Json.createObjectBuilder();
                    BasicInfoDto basicDto = GlobalContext.getBasicInfo();
                    if (basicDto != null) {
                        basic.add("nickname", basicDto.getNickname());
                        basic.add("deck_count", basicDto.getDeckCount());
                        basic.add("kdock_count", basicDto.getKdockCount());
                        basic.add("ndock_count", basicDto.getNdockCount());
                        basic.add("ms_count", basicDto.getMissionCount());
                        basic.add("ms_success", basicDto.getMissionSuccess());
                        basic.add("pt_win", basicDto.getPracticeWin());
                        basic.add("pt_lose", basicDto.getPracticeLose());
                        basic.add("st_win", basicDto.getSortieWin());
                        basic.add("st_lose", basicDto.getSortieLose());
                    }
                    jb.add("basic", basic);
                }

                {
                    jb.add("max_ships", GlobalContext.maxChara());
                    jb.add("max_slotitems", GlobalContext.maxSlotitem());
                    jb.add("master_last_update_time", MasterDataConfig.get().getLastUpdateTime().getTime());
                }
            }
        });

        return jb.build();
    }

    private static JsonObject createBattleRespons() {
        final JsonObjectBuilder jb = Json.createObjectBuilder();
        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                BattleDto battleDto = GlobalContext.getLastBattleDto();
                boolean isSortie = false;
                for (boolean sortie : GlobalContext.getIsSortie()) {
                    if (sortie) {
                        isSortie = true;
                        break;
                    }
                }

                if (isSortie) {
                    {// 戦闘中のマップ
                        JsonArrayBuilder map_array = Json.createArrayBuilder();
                        MapCellDto map = GlobalContext.getSortieMap();
                        map_array.add(map.getMap()[0]);
                        map_array.add(map.getMap()[1]);
                        map_array.add(map.getMap()[2]);
                        jb.add("map", map_array);
                        jb.add("enemy_id", map.getEnemyId());
                        jb.add("is_boss", map.isBoss());
                    }
                }
                // 演習もあるので
                if (battleDto != null) {// HP
                    JsonArrayBuilder fship_array = Json.createArrayBuilder();
                    JsonArrayBuilder eship_array = Json.createArrayBuilder();

                    List<ShipDto> fships = battleDto.getFriendShips();
                    for (ShipDto ship : fships) {
                        fship_array.add(shipToJson(ship));
                    }

                    List<ShipInfoDto> eships = battleDto.getEnemy();
                    int[] enowhp = battleDto.getNowEnemyHp();
                    int[] emaxhp = battleDto.getMaxEnemyHp();
                    for (int i = 0; i < eships.size(); ++i) {
                        ShipInfoDto ship = eships.get(i);
                        String flagship = ship.getFlagship();
                        int level = (flagship.equals("flagship") ? 2
                                : flagship.equals("elite") ? 1
                                        : 0);
                        eship_array.add(Json.createObjectBuilder()
                                .add("ship_id", ship.getShipId())
                                .add("ship_type", ship.getStype())
                                .add("level", level)
                                .add("now_hp", enowhp[i])
                                .add("max_hp", emaxhp[i]));
                    }

                    jb.add("friend", fship_array);
                    jb.add("enemy", eship_array);
                    jb.add("rank", battleDto.getRank().rank());
                }
            }
        });

        return jb.build();
    }
}
