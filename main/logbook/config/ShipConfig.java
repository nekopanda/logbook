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

import org.apache.commons.lang3.StringUtils;

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
            String name = ship.getName();
            String type = ship.getType();
            String afterlv = Integer.toString(ship.getAfterlv());
            String flagship = ship.getFlagship();

            String value = name + "," + type + "," + afterlv + "," + flagship;

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
            if ((values.length != 4) || !StringUtils.isNumeric(values[2])) {
                continue;
            }

            String name = values[0];
            String type = values[1];
            int afterlv = Integer.parseInt(values[2]);
            String flagship = values[3];

            ShipInfoDto ship = new ShipInfoDto(name, type, afterlv, flagship);

            Ship.set(key.toString(), ship);
        }
        return properties;
    }
}
