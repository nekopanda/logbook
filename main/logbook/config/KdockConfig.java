package logbook.config;

import java.io.IOException;
import java.util.Map;

import javax.annotation.CheckForNull;

import logbook.config.bean.KdockBean;
import logbook.config.bean.KdockMapBean;
import logbook.constants.AppConstants;
import logbook.data.context.GlobalContext;
import logbook.dto.GetShipDto;
import logbook.dto.ResourceItemDto;
import logbook.dto.ShipDto;
import logbook.util.BeanUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 建造ドックの投入資源を保存・復元します
 *
 */
public class KdockConfig {
    /** ロガー */
    private static final Logger LOG = LogManager.getLogger(KdockConfig.class);

    /** 建造ドックのBean */
    private static KdockMapBean mapBean;

    /**
     * 建造ドックの投入資源を設定します
     * 
     * @param dock ドック
     * @param resource 資源
     * @throws IOException IOException
     */
    public static void store(String dock, GetShipDto data) throws IOException {
        if (mapBean == null) {
            mapBean = new KdockMapBean();
        }
        KdockBean kdock = new KdockBean();
        kdock.setType(data.isOogata() ? 1 : 0);
        kdock.setFuel(data.getFuel());
        kdock.setAmmo(data.getAmmo());
        kdock.setMetal(data.getMetal());
        kdock.setBauxite(data.getBauxite());
        kdock.setResearchMaterials(data.getResearchMaterials());
        kdock.setShipId(data.getSecretlyId());
        kdock.setHqLevel(data.getHqLevel());
        kdock.setFreeDock(data.getFreeDock());
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
    public static GetShipDto load(String dock) {
        try {
            if (mapBean == null) {
                mapBean = BeanUtils.readObject(AppConstants.KDOCK_CONFIG_FILE, KdockMapBean.class);
            }
            if (mapBean != null) {
                KdockBean kdock = mapBean.getKdockMap().get(dock);

                if (kdock == null) {
                    return null;
                }

                Map<Integer, ShipDto> ships = GlobalContext.getShipMap();
                if (!ships.isEmpty() && ships.containsKey(kdock.getShipId())) {
                    ResourceItemDto res = new ResourceItemDto();
                    res.setFuel(kdock.getFuel());
                    res.setAmmo(kdock.getAmmo());
                    res.setMetal(kdock.getMetal());
                    res.setBauxite(kdock.getBauxite());
                    res.setResearchMaterials(kdock.getResearchMaterials());
                    return new GetShipDto(kdock.getType() == 1, res,
                            ships.get(kdock.getShipId()), kdock.getHqLevel(), kdock.getFreeDock());
                }
            }
        } catch (Exception e) {
            LOG.warn("建造ドックの投入資源を取得しますに失敗しました", e);
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
