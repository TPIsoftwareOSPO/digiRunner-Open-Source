package tpi.dgrv4.dpaa.service;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.vo.AA0434DTO;
import tpi.dgrv4.dpaa.vo.AA1002List;
import tpi.dgrv4.dpaa.vo.AA1002Resp;
import tpi.dgrv4.entity.entity.TsmpApi;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.repository.TsmpApiDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class ApiOrgMangerService {

    private static final String ORG_ID_REQUIRED_MSG = "orgId is required";

    private TsmpOrganizationDao tsmpOrganizationDao;
    private TsmpApiDao tsmpApiDao;

    @Autowired
    public ApiOrgMangerService(TsmpOrganizationDao tsmpOrganizationDao, TsmpApiDao tsmpApiDao) {
        this.tsmpOrganizationDao = tsmpOrganizationDao;
        this.tsmpApiDao = tsmpApiDao;
    }

    public AA1002Resp queryOrgList(TsmpAuthorization authorization) {

        String orgId;

        if (authorization.getOrgId() != null && !authorization.getOrgId().isEmpty()) {
            orgId = authorization.getOrgId();
        } else {
            TPILogger.tl.error(ORG_ID_REQUIRED_MSG);
            throw TsmpDpAaRtnCode._1559.throwing(ORG_ID_REQUIRED_MSG);
        }

        List<TsmpOrganization> orgList = tsmpOrganizationDao.queryOrgDescendingByOrgId_rtn_entity(orgId,
                Integer.MAX_VALUE);

        if (orgList.isEmpty()) {
            TPILogger.tl.error("orgId is not found");
            throw TsmpDpAaRtnCode._1559.throwing("orgId is not found");
        }

        List<AA1002List> aa1002List = orgList.stream().map(org -> {
            AA1002List aa1002 = new AA1002List();
            aa1002.setOrgID(org.getOrgId());
            aa1002.setOrgName(org.getOrgName());
            aa1002.setOrgCode(org.getOrgCode());
            aa1002.setParentID(org.getParentId());
            return aa1002;
        }).toList();

        AA1002Resp aa1002Resp = new AA1002Resp();
        aa1002Resp.setOrgList(aa1002List);
        return aa1002Resp;
    }

    @Transactional
    public List<AA0434DTO> batchApiOrg(TsmpAuthorization authorization, List<AA0434DTO> body) {

        String orgId;

        if (authorization.getOrgId() != null && !authorization.getOrgId().isEmpty()) {
            orgId = authorization.getOrgId();
        } else {
            TPILogger.tl.error(ORG_ID_REQUIRED_MSG);
            throw TsmpDpAaRtnCode._1559.throwing(ORG_ID_REQUIRED_MSG);
        }

        List<String> orgList = tsmpOrganizationDao.queryOrgDescendingByOrgId_rtn_id(orgId,
                Integer.MAX_VALUE);

        body.forEach(dto -> {

            String apiKey = dto.getApiKey();
            String moduleName = dto.getModuleName();
            String orgID = dto.getOrgID();

            if (!StringUtils.hasText(apiKey)) {
                throw TsmpDpAaRtnCode._1559.throwing("apiKey is required");
            }

            if (!StringUtils.hasText(moduleName)) {
                throw TsmpDpAaRtnCode._1559.throwing("moduleName is required");
            }

            if (!StringUtils.hasText(orgID)) {
                throw TsmpDpAaRtnCode._1559.throwing("orgID is required");
            }

            if (!orgList.contains(orgID)) {
                throw TsmpDpAaRtnCode._1559.throwing("orgID: " + orgID + " is not found in orgList, no permission to operate this organization");
            }

            TsmpApi api = tsmpApiDao.findByModuleNameAndApiKey(moduleName, apiKey);
            if (api == null) {
                TPILogger.tl.error("api is not found ,moduleName: " + moduleName + ", apiKey: " + apiKey);
                throw TsmpDpAaRtnCode._1559.throwing("api is not found");
            }

            String apiOrgId = api.getOrgId();

            if (!orgList.contains(apiOrgId)) {
                throw TsmpDpAaRtnCode._1559.throwing("No permission to modify this API");
            }

            api.setOrgId(orgID);
            api.setUpdateTime(new Date());
            api.setUpdateUser(authorization.getUserName());

            tsmpApiDao.save(api);
        });

        return body;
    }

}
