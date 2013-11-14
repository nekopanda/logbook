/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.config;

import java.io.File;
import java.util.Properties;
import java.util.Set;

import logbook.dto.ShipInfoDto;
import logbook.internal.Ship;

/**
 * <p>
 * 艦娘のIDと名前の紐付けを保存・復元します
 * </p>
 */
public class ShipConfig {

    /** 設定ファイル  */
    private static final File CONFIG_FILE = new File("./config/ship.txt");

    /** 設定プロパティ */
    private static Properties properties;

    /**
     * 設定ファイルを読み込みます
     */
    public static void load() {
        properties = update(GlobalConfig.readconfig(CONFIG_FILE));
    }

    /**
     * 設定ファイルを書き込みます
     */
    public static void store() {
        Set<String> shipids = Ship.keySet();
        for (String key : shipids) {
            ShipInfoDto ship = Ship.get(key);

            if (ShipInfoDto.EMPTY == ship) {
                properties.remove(key.toString());
                continue;
            }

            String name = ship.getName();
            String type = ship.getType();
            String flagship = ship.getFlagship();
            String afterlv = Integer.toString(ship.getAfterlv());
            String maxBull = Integer.toString(ship.getMaxBull());
            String maxFuel = Integer.toString(ship.getMaxFuel());

            String value = name + "," + type + "," + flagship + "," + afterlv + "," + maxBull + "," + maxFuel;

            properties.setProperty(key, value);
        }

        GlobalConfig.saveconfig(properties, CONFIG_FILE);
    }

    /**
     * 艦娘のIDと名前の紐付けを内部クラスに保存します
     * 
     * @param properties
     * @return
     */
    private static Properties update(Properties properties) {
        for (Object key : properties.keySet()) {
            String[] values = properties.getProperty(key.toString()).split(",");
            if (values.length != 6) {
                properties.remove(key.toString());
                continue;
            }

            String name = values[0];
            String type = values[1];
            String flagship = values[2];
            int afterlv = Integer.parseInt(values[3]);
            int maxBull = Integer.parseInt(values[4]);
            int maxFuel = Integer.parseInt(values[5]);

            // 未定義の艦娘
            if ("".equals(name)) {
                continue;
            }

            ShipInfoDto ship = new ShipInfoDto(name, type, flagship, afterlv, maxBull, maxFuel);

            Ship.set(key.toString(), ship);
        }
        return properties;
    }
}
