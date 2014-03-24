/**
 * No Rights Reserved.
 * This program and the accompanying materials
 * are made available under the terms of the Public Domain.
 */
package logbook.config;

import java.io.IOException;
import java.util.Map;

import javax.annotation.CheckForNull;

import logbook.config.bean.KdockBean;
import logbook.config.bean.KdockMapBean;
import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.dto.ResourceDto;
import logbook.dto.ShipDto;
import logbook.util.BeanUtils;

/**
 * 建造ドックの投入資源を保存・復元します
 *
 */
public class KdockConfig {

    /** 建造ドックのBean */
    private static KdockMapBean mapBean;

    /**
     * 建造ドックの投入資源を設定します
     * 
     * @param dock ドック
     * @param resource 資源
     * @throws IOException IOException
     */
    public static void store(String dock, ResourceDto resource) throws IOException {
        if (mapBean == null) {
            mapBean = new KdockMapBean();
        }
        KdockBean kdock = new KdockBean();
        kdock.setType(resource.getType());
        kdock.setFuel(resource.getFuel());
        kdock.setAmmo(resource.getAmmo());
        kdock.setMetal(resource.getMetal());
        kdock.setBauxite(resource.getBauxite());
        kdock.setResearchMaterials(resource.getResearchMaterials());
        kdock.setShipId(resource.getSecretary().getId());
        kdock.setHqLevel(resource.getHqLevel());
        kdock.setFreeDock(resource.getFreeDock());
        mapBean.getKdockMap().put(dock, kdock);

        BeanUtils.writeObject(AppConstants.KDOCK_CONFIG_FILE, mapBean);
    }

    /**
     * 建造ドックの投入資源を取得します
     * 
     * @param dock ドック
     * @return 建造ドックの投入資源
     */
    @CheckForNull
    public static ResourceDto load(String dock) {
        if (mapBean == null) {
            mapBean = BeanUtils.readObject(AppConstants.KDOCK_CONFIG_FILE, KdockMapBean.class);
        }
        if (mapBean != null) {
            KdockBean kdock = mapBean.getKdockMap().get(dock);

            Map<Long, ShipDto> ships = GlobalContext.getShipMap();
            if (!ships.isEmpty() && ships.containsKey(kdock.getShipId())) {
                ResourceDto resource = new ResourceDto(kdock.getType(), kdock.getFuel(), kdock.getAmmo(),
                        kdock.getMetal(),
                        kdock.getBauxite(), kdock.getResearchMaterials(), ships.get(kdock.getShipId()),
                        kdock.getHqLevel());
                resource.setFreeDock(kdock.getFreeDock());
                return resource;
            }
        }
        return null;
    }

    /**
     * 建造ドックの投入資源を削除します
     * 
     * @param dock ドック
     * @throws IOException IOException
     */
    public static void remove(String dock) throws IOException {
        if (mapBean != null) {
            mapBean.getKdockMap().remove(dock);
        }

        BeanUtils.writeObject(AppConstants.KDOCK_CONFIG_FILE, mapBean);
    }
}
