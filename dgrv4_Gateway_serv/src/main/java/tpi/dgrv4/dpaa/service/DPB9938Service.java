package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB9938Req;
import tpi.dgrv4.dpaa.vo.DPB9938Resp;
import tpi.dgrv4.dpaa.vo.DPB9938EsResp;
import tpi.dgrv4.entity.repository.TsmpSettingDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.service.TsmpSettingService;
import tpi.dgrv4.httpu.utils.HttpUtil;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class DPB9938Service {
    private TsmpSettingService tsmpSettingService;

    @Autowired
    public DPB9938Service(TsmpSettingService tsmpSettingService) {
		super();
		this.tsmpSettingService = tsmpSettingService;
	}

	public DPB9938Resp testEsConnection(DPB9938Req req) {
        DPB9938Resp resp = new DPB9938Resp();
        try {
            String esUrl = getTsmpSettingService().getVal_ES_URL();
            String esIdMima = getTsmpSettingService().getVal_ES_ID_PWD();
            String[] arrEsUrl = getEsConnUrl(esUrl);
            String[] arrIdPwd = getEsConnIDPWD(esUrl, esIdMima);

            HttpUtil.HttpRespData respData = null;
            Map<String, DPB9938EsResp> respMap = new HashMap<>();
            for (int index = 0; index < arrEsUrl.length; index++) {
                boolean isConnection = false;
                DPB9938EsResp esResp = new DPB9938EsResp();
                respData = checkConnection(arrEsUrl[index], arrIdPwd[index]);
                if (respData.statusCode > 0 && respData.statusCode < 400) {
                    isConnection = true;

                }

                esResp.setResp(respData.respStr);
                esResp.setConnection(isConnection);
                respMap.put(arrEsUrl[index], esResp);
            }
            resp.setEsRespMap(respMap);
        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            //執行錯誤
            throw TsmpDpAaRtnCode._1297.throwing();
        }
        return resp;
    }

    protected HttpUtil.HttpRespData checkConnection(String arrEsUrl, String idMima) throws IOException {
        return HttpUtil.httpReqByGet(arrEsUrl, getHeader(idMima), false);
    }

    private Map<String, String> getHeader(String idMima) {
        Map<String, String> header = new HashMap<>();
        header.put("Accept", "application/json");
        header.put("Content-Type", "application/json");
        header.put("Authorization", "Basic " + idMima);
        return header;
    }

    private String[] getEsConnIDPWD(String esUrl, String esIdMima) {
        // 檢查是否有ES_ID_PWD參數
        if (!StringUtils.hasText(esIdMima)) {
            throw TsmpDpAaRtnCode._1474.throwing(TsmpSettingDao.Key.ES_ID_PWD);
        }
        // 檢查ES_ID_PWD參數內容值個數和URL個數有沒有吻合

        String[] arrIdPwd = esIdMima.split(",");
        if (arrIdPwd.length != getEsConnUrl(esUrl).length) {
            throw TsmpDpAaRtnCode._1559.throwing("ES connection info and id pwd is not mapping");
        }
        return arrIdPwd;
    }

    private String[] getEsConnUrl(String esUrl) {
        // 檢查是否有ES_URL參數
        if (!StringUtils.hasText(esUrl)) {
            throw TsmpDpAaRtnCode._1474.throwing(TsmpSettingDao.Key.ES_URL);
        }

        String[] arrEsUrl = esUrl.split(",");
        List<String> invalidPaths = Stream.of(arrEsUrl)
                .filter(url -> !url.endsWith("/"))
                .collect(Collectors.toList());
        if (!invalidPaths.isEmpty()){
            throw TsmpDpAaRtnCode._1352.throwing(invalidPaths.stream().collect(Collectors.joining(",")));
        }

        return arrEsUrl;

    }

    protected TsmpSettingService getTsmpSettingService() {
        return tsmpSettingService;
    }
}
