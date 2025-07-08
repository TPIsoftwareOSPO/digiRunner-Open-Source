package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.vo.DPB0291Req;
import tpi.dgrv4.dpaa.vo.DPB0291Resp;
import tpi.dgrv4.dpaa.vo.DPB0291RespItem;
import tpi.dgrv4.entity.entity.DgrGrpcProxyMap;
import tpi.dgrv4.entity.repository.DgrGrpcProxyMapDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;
import tpi.dgrv4.grpcproxy.manager.DynamicGrpcProxyManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DPB0291Service {

    private final TPILogger logger = TPILogger.tl;
    private final DgrGrpcProxyMapDao dgrGrpcProxyMapDao;
    private final DynamicGrpcProxyManager dynamicGrpcProxyManager;

    @Autowired
    public DPB0291Service(
            DgrGrpcProxyMapDao dgrGrpcProxyMapDao,
            DynamicGrpcProxyManager dynamicGrpcProxyManager) {
        this.dgrGrpcProxyMapDao = dgrGrpcProxyMapDao;
        this.dynamicGrpcProxyManager = dynamicGrpcProxyManager;
    }

    /**
     * 批次更新代理映射的啟用狀態
     *
     * @param req 批次操作請求
     * @return 批次操作響應
     */
    @Transactional
    public DPB0291Resp batchUpdateProxyStatus(TsmpAuthorization authorization, DPB0291Req req) {
        DPB0291Resp resp = new DPB0291Resp();
        List<DPB0291RespItem> respItems = new ArrayList<>();

        // 驗證參數
        if (req.getProxyIds() == null || req.getProxyIds().isEmpty()) {
            throw TsmpDpAaRtnCode._1297.throwing();
        }

        if (!"Y".equals(req.getEnable()) && !"N".equals(req.getEnable())) {
            throw TsmpDpAaRtnCode._1352.throwing("enable");
        }

        // 批次更新的代理映射
        List<DgrGrpcProxyMap> updatedMappings = new ArrayList<>();

        logger.debug("批次" + ("Y".equals(req.getEnable()) ? "啟用" : "停用") +
                "代理服務，數量: " + req.getProxyIds().size());

        // 處理每個 ID
        for (String id : req.getProxyIds()) {
            try {
                Long grpcProxyMapId = Long.parseLong(id);
                Optional<DgrGrpcProxyMap> optMapping = dgrGrpcProxyMapDao.findById(grpcProxyMapId);

                if (optMapping.isPresent()) {
                    DgrGrpcProxyMap mapping = optMapping.get();

                    // 更新操作人員
                    mapping.setUpdateUser(authorization.getUserName());
                    mapping.setUpdateDateTime(DateTimeUtil.now());

                    // 更新啟用狀態
                    mapping.setEnable(req.getEnable());

                    // 保存到資料庫
                    dgrGrpcProxyMapDao.save(mapping);

                    // 添加到待更新列表
                    updatedMappings.add(mapping);

                    // 添加到響應列表
                    respItems.add(DPB0291RespItem.fromEntity(mapping));
                }
            } catch (Exception e) {
                logger.error("處理代理 ID 時出錯: " + id);
            }
        }

        // 更新代理通道
        if (!updatedMappings.isEmpty()) {
            try {
                // 調用動態代理管理器更新通道
                boolean enable = "Y".equals(req.getEnable());
                dynamicGrpcProxyManager.batchUpdateProxyStatus(updatedMappings, enable);
            } catch (Exception e) {
                logger.error("批次更新代理通道時出錯" + e);
            }
        }

        // 設置響應
        resp.setItems(respItems);

        logger.debug("批次" + ("Y".equals(req.getEnable()) ? "啟用" : "停用") +
                "代理服務完成，更新數量: " + respItems.size());

        return resp;
    }
}