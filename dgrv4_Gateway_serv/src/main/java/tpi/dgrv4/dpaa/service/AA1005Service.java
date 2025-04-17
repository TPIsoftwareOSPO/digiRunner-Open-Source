package tpi.dgrv4.dpaa.service;

import java.util.Optional;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA1005Req;
import tpi.dgrv4.dpaa.vo.AA1005Resp;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1005Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpOrganizationDao tsmpOrganizationDao;

	@Autowired
	public AA1005Service(TsmpOrganizationDao tsmpOrganizationDao) {
		super();
		this.tsmpOrganizationDao = tsmpOrganizationDao;
	}

	@Transactional
	public AA1005Resp queryTOrgDetail (TsmpAuthorization auth, AA1005Req req) {
		AA1005Resp resp = new AA1005Resp();
		try {
			String orgId = ServiceUtil.nvl(req.getOrgId());
			// "1.查詢TSMP_ORGANIZATION資料表，條件ORG_ID = AA1005Req.orgId，若找不到資料則throw RTN CODE 1298:查無資料。
			// 2.將步驟1查詢到單筆資料轉換成AA1005Resp物件。
			Optional<TsmpOrganization> optOrg = getTsmpOrganizationDao().findById(orgId);
			
			if(!optOrg.isPresent()) {
				throw TsmpDpAaRtnCode._1298.throwing();
				
			}else {
				String createDateStr = "";
				Optional<String> createDateOpt = DateTimeUtil.dateTimeToString(optOrg.get().getCreateTime(), null);
				if(createDateOpt.isPresent())
					createDateStr = createDateOpt.get();

				resp.setContactMail(ServiceUtil.nvl(optOrg.get().getContactMail()));
				resp.setContactName(ServiceUtil.nvl(optOrg.get().getContactName()));
				
				
				resp.setContactTel(ServiceUtil.nvl(optOrg.get().getContactTel()));
				resp.setCreateTime(createDateStr);
				resp.setCreateUser(optOrg.get().getCreateUser());
				resp.setOrgCode(ServiceUtil.nvl(optOrg.get().getOrgCode()));
				resp.setOrgId(ServiceUtil.nvl(optOrg.get().getOrgId()));
				resp.setOrgName(ServiceUtil.nvl(optOrg.get().getOrgName()));
				
				
				//---------------------------------------set parentId---------------------------------------
				String parentId = ServiceUtil.nvl(optOrg.get().getParentId());
				String parentName = getParentName(parentId, orgId);
				resp.setParentName(parentName);	//用TSMP_ORGANIZATION.PARENT_ID查詢出ORG_NAME
					
			}
			
			
		}catch (TsmpDpAaException e){
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();			//1297:執行錯誤
		}
		
		return resp;
	}
	
	private String getParentName(String parentId, String orgId) {
		String parentOrgName = "";
		// 會檢查parentId為空抱錯是因為在前端 TSMPDefaultRoot 不可以更新 所以接收到的req 排除TSMPDefaultRoot 都應該要有parentId
		if(!"".equals(parentId)) {
			Optional<TsmpOrganization> optPatrntOrg = getTsmpOrganizationDao().findById(parentId);
			if(!optPatrntOrg.isPresent()) {
				logger.debug("orgId = "+ orgId + "parentId is empty ");
				throw TsmpDpAaRtnCode._1297.throwing();	
				
			}else {
				parentOrgName = ServiceUtil.nvl(optPatrntOrg.get().getOrgName());
				if("".equals(parentOrgName)) {
					logger.debug("orgId = "+ orgId + "parentId is empty ");
					throw TsmpDpAaRtnCode._1297.throwing();	
				}
			}
			
		}else {
			if(!"100000".equals(orgId)) {
				logger.debug("orgId = "+ orgId + "parentId is empty ");
				throw TsmpDpAaRtnCode._1297.throwing();
			}
		}
		return parentOrgName;
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}
	
}
