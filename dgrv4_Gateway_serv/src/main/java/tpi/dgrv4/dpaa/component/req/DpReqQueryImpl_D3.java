package tpi.dgrv4.dpaa.component.req;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.dpaa.component.TsmpMailEventBuilder;
import tpi.dgrv4.common.constant.DateTimeFormatEnum;
import tpi.dgrv4.common.constant.TsmpDpModule;
import tpi.dgrv4.common.constant.TsmpDpPublicFlag;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.dpaa.vo.TsmpMailEvent;
import tpi.dgrv4.entity.component.cache.proxy.TsmpDpItemsCacheProxy;
import tpi.dgrv4.entity.entity.TsmpClient;
import tpi.dgrv4.entity.entity.TsmpDpClientext;
import tpi.dgrv4.entity.entity.TsmpDpItems;
import tpi.dgrv4.entity.entity.TsmpDpItemsId;
import tpi.dgrv4.entity.entity.jpql.TsmpDpReqOrderd3;
import tpi.dgrv4.entity.repository.TsmpClientDao;
import tpi.dgrv4.entity.repository.TsmpDpClientextDao;
import tpi.dgrv4.entity.repository.TsmpDpFileDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrderd3Dao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdermDao;
import tpi.dgrv4.entity.repository.TsmpDpReqOrdersDao;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.escape.MailHelper;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

/**
 * 處理"用戶註冊"簽核單查詢
 * @author Kim
 *
 */
@Service
public class DpReqQueryImpl_D3 extends DpReqQueryAbstract<DpReqQueryResp_D3> implements DpReqQueryIfs<DpReqQueryResp_D3> {

	private TPILogger logger = TPILogger.tl;

	private TsmpClientDao tsmpClientDao;
	private TsmpDpClientextDao tsmpDpClientextDao;
	private TsmpDpReqOrderd3Dao tsmpDpReqOrderd3Dao;
	private MailHelper mailHelper;

	@Autowired
	public DpReqQueryImpl_D3(TsmpDpReqOrdermDao tsmpDpReqOrdermDao, TsmpDpReqOrdersDao tsmpDpReqOrdersDao,
			TsmpDpItemsCacheProxy tsmpDpItemsCacheProxy, TsmpOrganizationDao tsmpOrganizationDao,
			TsmpUserDao tsmpUserDao, TsmpClientDao tsmpClientDao, TsmpDpFileDao tsmpDpFileDao,
			DpReqServiceFactory dpReqServiceFactory, TsmpClientDao tsmpClientDao2,
			TsmpDpClientextDao tsmpDpClientextDao, TsmpDpReqOrderd3Dao tsmpDpReqOrderd3Dao, MailHelper mailHelper) {
		super(tsmpDpReqOrdermDao, tsmpDpReqOrdersDao, tsmpDpItemsCacheProxy, tsmpOrganizationDao, tsmpUserDao,
				tsmpClientDao, tsmpDpFileDao, dpReqServiceFactory);
		tsmpClientDao = tsmpClientDao2;
		this.tsmpDpClientextDao = tsmpDpClientextDao;
		this.tsmpDpReqOrderd3Dao = tsmpDpReqOrderd3Dao;
		this.mailHelper = mailHelper;
	}

	@Override
	protected List<DpReqQueryResp_D3> doQueryDetail(Long reqOrdermId, String locale) {
		TsmpDpReqOrderd3 d3 = getTsmpDpReqOrderd3Dao().findFirstByRefReqOrdermId(reqOrdermId);
		if (d3 == null) {
			return Collections.emptyList();
		}
		Optional<TsmpClient> opt = getTsmpClientDao().findById(d3.getClientId());
		if (!opt.isPresent()) {
			return Collections.emptyList();
		}
		TsmpClient client = opt.get();
		
		String publicFlag = new String();
		String publicFlagName = new String();
		Optional<TsmpDpClientext> opt_ext = getTsmpDpClientextDao().findById(client.getClientId());
		if (opt_ext.isPresent()) {
			TsmpDpClientext ext = opt_ext.get();
			publicFlag = nvl(ext.getPublicFlag());
			publicFlagName = getPublicFlagName(publicFlag, locale);
		}
		
		List<DpReqQueryResp_D3> d3RespList = new ArrayList<>();
		DpReqQueryResp_D3 d3Resp = new DpReqQueryResp_D3();
		d3Resp.setReqOrderd3Id(d3.getReqOrderd3Id());
		d3Resp.setClientId(client.getClientId());
		d3Resp.setClientName(client.getClientName());
		d3Resp.setEmails(nvl(client.getEmails()));
		d3Resp.setPublicFlag(publicFlag);
		d3Resp.setPublicFlagName(publicFlagName);
		d3RespList.add(d3Resp);
		return d3RespList;
	}

	@Override
	protected TsmpMailEvent getTsmpMailEvent(String userId, String recipients, TsmpAuthorization auth,
			DpReqQueryResp<DpReqQueryResp_D3> resp) {
		if (resp == null) {
			this.logger.debug("Query response is empty!");
			return null;
		}
		if (StringUtils.isEmpty(recipients)) {
			this.logger.debug(String.format("USER %s has empty emails!", userId));
			return null;
		}
		
		Map<String, Object> subjectParams = new HashMap<>();
		subjectParams.put("projectName", TsmpDpModule.DP.getChiDesc());
		subjectParams.put("reqOrderNo", resp.getReqOrderNo());
		subjectParams.put("reviewType", resp.getReqTypeName());
		String subject = getMailHelper().buildNestedContent("subject.revi-wait.D3", subjectParams);
		
		List<Map<String, Object>> clientList = new ArrayList<>();
		List<DpReqQueryResp_D3> d3List = resp.getDetailList();
		if (d3List != null && !d3List.isEmpty()) {
			Map<String, Object> client = null;
			for (DpReqQueryResp_D3 d3 : d3List) {
				client = new HashMap<>();
				client.put("clientId", d3.getClientId());
				client.put("clientName", d3.getClientName());
				client.put("emails", d3.getEmails());
				client.put("publicFlag", d3.getPublicFlagName());
				clientList.add(client);
			}
		}
		
		Map<String, Object> detail = new HashMap<>();
		detail.put("clientList", clientList);
		
		Map<String, Object> bodyParams = new HashMap<>();
		bodyParams.put("reviewType", resp.getReqTypeName());
		bodyParams.put("reqOrderNo", resp.getReqOrderNo());
		bodyParams.put("createDateTime", DateTimeUtil.dateTimeToString(resp.getCreateDateTime(), DateTimeFormatEnum.西元年月日).orElse(new String()));
		bodyParams.put("applyUserName", resp.getApplierName());
		bodyParams.put("orgName", resp.getOrgName());
		bodyParams.put("chkStatusName", resp.getCurrentReviewStatusName());
		bodyParams.put("chkPointName", resp.getCurrentLayerName());
		bodyParams.put("detail", detail);
		
		String content = getMailHelper().buildNestedContent("body.revi-wait.D3", bodyParams);
		
		return new TsmpMailEventBuilder() //
		.setSubject(subject)
		.setContent(content)
		.setRecipients(recipients)
		.setCreateUser(auth.getUserName())
		.setRefCode("body.revi-wait.D3")
		.build();
	}

	private String getPublicFlagName(String publicFlag, String locale) {
		if (StringUtils.isEmpty(publicFlag)) {
			publicFlag = TsmpDpPublicFlag.EMPTY.value();
		}
		TsmpDpItemsId id = new TsmpDpItemsId("API_AUTHORITY", publicFlag, locale);
		TsmpDpItems vo = getTsmpDpItemsCacheProxy().findById(id);
		if (vo == null) {
			return new String();
		}
		return vo.getSubitemName();
	}

	protected TsmpClientDao getTsmpClientDao() {
		return this.tsmpClientDao;
	}

	protected TsmpDpClientextDao getTsmpDpClientextDao() {
		return this.tsmpDpClientextDao;
	}

	protected TsmpDpReqOrderd3Dao getTsmpDpReqOrderd3Dao() {
		return this.tsmpDpReqOrderd3Dao;
	}

	protected MailHelper getMailHelper() {
		return this.mailHelper;
	}

}