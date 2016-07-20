package logbook.gui.logic;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import logbook.data.context.GlobalContext;
import logbook.dto.ItemDto;

/**
 * 艦載機厨氏の艦隊シミュレーター＆デッキビルダーのフォーマットを作成するクラス
 *
 * @author Nishisonic
 */
public class DeckBuilder {
    /**
     * フォーマットのバージョン
     */
    public final int FORMAT_VERSION = 4;

    /**
     * 艦隊シミュレーター＆デッキビルダーのURL
     */
    public final String URL = "http://kancolle-calc.net/deckbuilder.html";

    /**
     * 艦隊シミュレーター＆デッキビルダーのURLにつける語尾
     */
    public final String SUFFIX = "?predeck=";

    /**
     * フォーマットのバージョンを返します
     *
     * @return fORMAT_VERSION
     */
    public int getFormatVersion() {
        return this.FORMAT_VERSION;
    }

    /**
     * 艦隊シミュレーター＆デッキビルダーのURLを返します
     *
     * @return URL
     */
    public String getURL() {
        return this.URL;
    }

    /**
     * 艦隊シミュレーター＆デッキビルダーのURLにつける語尾を返します
     *
     * @return Suffix
     */
    public String getSuffix() {
        return this.SUFFIX;
    }

    /**
     * 艦載機厨氏の艦隊シミュレーター＆デッキビルダーのフォーマットを返します
     * ただし、データが出揃っていない場合はnullが返されます
     *
     * @param needsUsedDock どの艦隊のデータを用いるか[第一艦隊,第二艦隊,第三艦隊,第四艦隊]
     * @return format フォーマット
     */
    public String getDeckBuilderFormat(boolean[] needsUsedDock) {
        JsonObjectBuilder deck = Json.createObjectBuilder();
        deck.add("version", this.FORMAT_VERSION);
        try {
            IntStream.rangeClosed(1, GlobalContext.getBasicInfo().getDeckCount())
                    .filter(dockId -> needsUsedDock[dockId - 1])
                    .boxed()
                    .collect(Collectors.toMap(dockId -> dockId,
                            dockId -> GlobalContext.getDock(dockId.toString()).getShips()))
                    .forEach((dockId, ships) -> {
                        JsonObjectBuilder fleet = Json.createObjectBuilder();

                        IntStream.range(0, ships.size()).forEach(shipIdx -> {
                            JsonObjectBuilder ship = Json.createObjectBuilder();
                            ship.add("id", ships.get(shipIdx).getShipInfo().getShipId());
                            ship.add("lv", ships.get(shipIdx).getLv());
                            ship.add("luck", ships.get(shipIdx).getLucky());
                            JsonObjectBuilder items = Json.createObjectBuilder();
                            List<ItemDto> item2 = ships.get(shipIdx).getItem2();
                            int slotNum = ships.get(shipIdx).getSlotNum();

                            IntStream.range(0, slotNum)
                                    .filter(itemIdx -> Optional.ofNullable(item2.get(itemIdx)).isPresent())
                                    .boxed()
                                    .collect(Collectors.toMap(itemIdx -> itemIdx, itemIdx -> item2.get(itemIdx)))
                                    .forEach((itemIdx, itemDto) -> {
                                        JsonObjectBuilder item = Json.createObjectBuilder();
                                        item.add("id", item2.get(itemIdx).getSlotitemId());
                                        item.add("rf", item2.get(itemIdx).getLevel());
                                        item.add("mas", item2.get(itemIdx).getAlv());
                                        items.add("i" + (itemIdx + 1), item);
                                    });

                            Optional.ofNullable(ships.get(shipIdx).getSlotExItem()).ifPresent(slotExItem -> {
                                JsonObjectBuilder item = Json.createObjectBuilder();
                                item.add("id", slotExItem.getSlotitemId());
                                item.add("rf", slotExItem.getLevel());
                                item.add("mas", slotExItem.getAlv());
                                if (slotNum < 4) {
                                    items.add("i" + (slotNum + 1), item);
                                } else {
                                    items.add("ix", item);
                                }
                            });
                            ship.add("items", items);

                            fleet.add("s" + (shipIdx + 1), ship);
                        });
                        deck.add("f" + dockId, fleet);
                    });
            return deck.build().toString();
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * 艦載機厨氏の艦隊シミュレーター＆デッキビルダーのフォーマットを返します(全艦隊)
     * ただし、データが出揃っていない場合はnullが返されます
     *
     * @return format フォーマット
     */
    public String getDeckBuilderFormat() {
        boolean[] b = { true, true, true, true };
        return this.getDeckBuilderFormat(b);
    }

    /**
     * 艦載機厨氏の艦隊シミュレーター＆デッキビルダーのURLを作成します
     * ただし、データが出揃っていない場合はnullが返されます
     *
     * @param needsUsedDock どの艦隊のデータを用いるか[第一艦隊,第二艦隊,第三艦隊,第四艦隊]
     * @return url URL
     */
    public String getDeckBuilderURL(boolean[] needsUsedDock) {
        Optional<String> formatOpt = Optional.ofNullable(this.getDeckBuilderFormat(needsUsedDock));
        if (formatOpt.isPresent()) {
            return this.URL + this.SUFFIX + formatOpt.get();
        } else {
            return null;
        }
    }

    /**
     * 艦載機厨氏の艦隊シミュレーター＆デッキビルダーのURLを作成します(全艦隊)
     * ただし、データが出揃っていない場合はnullが返されます
     *
     * @return url URL
     */
    public String getDeckBuilderURL() {
        boolean[] b = { true, true, true, true };
        return this.getDeckBuilderURL(b);
    }

    private static String encodeURIComponent(String s) {
        try {
            return URLEncoder.encode(s, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

}
