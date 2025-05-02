package tpi.dgrv4.dpaa.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA1002List;
import tpi.dgrv4.dpaa.vo.AA1002Req;
import tpi.dgrv4.dpaa.vo.AA1002Resp;
import tpi.dgrv4.entity.entity.TsmpOrganization;
import tpi.dgrv4.entity.repository.TsmpOrganizationDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1002Service {

	private TPILogger logger = TPILogger.tl;
	
	private TsmpOrganizationDao tsmpOrganizationDao;

	@Autowired
	public AA1002Service(TsmpOrganizationDao tsmpOrganizationDao) {
		super();
		this.tsmpOrganizationDao = tsmpOrganizationDao;
	}

	public AA1002Resp queryTOrgList(TsmpAuthorization auth, AA1002Req req) {
		AA1002Resp resp = new AA1002Resp();
		List<TsmpOrganization> allOrgList = new ArrayList<>();
		try {
			//1.取得TSMP_ORGANIZATION資料表全部資料。
			//2.將步驟1的資料轉換成AA1002List。
		
			/*2020729 修改 API上下架也會用到組織
			1.若AA1002Req.orgID不為空則執行步驟2，反之則執行步驟3.。
			2.查詢TSMP_ORGANIZATION資料表，條件 ORG_ID = AA1002Req.orgID。
			3.取得TSMP_ORGANIZATION資料表全部資料。
			4.將步驟2或步驟3是的資料轉換成AA1002List。 */
			
			String orgId = ServiceUtil.nvl(req.getOrgID());
			if(!StringUtils.isEmpty(orgId)) {
				Optional<TsmpOrganization> optOrg = getTsmpOrganizationDao().findById(orgId);
				if(optOrg.isPresent())
					allOrgList.add(optOrg.get());
				
			}else {
				allOrgList = getTsmpOrganizationDao().findAll();
				
			}
			
			if(allOrgList == null || allOrgList.size() == 0)
				throw TsmpDpAaRtnCode._1298.throwing();		//1298:查無資料
			
			List<AA1002List> dataList = getAA1002Resp(allOrgList);
			resp.setOrgList(dataList);
				
			
		}catch (TsmpDpAaException e){
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();			//1297:執行錯誤
		}
		
		return resp;
	}
	
	private List<AA1002List> getAA1002Resp( List<TsmpOrganization> allOrgList ){
		List<AA1002List> dataList = new ArrayList<AA1002List>();
		
		allOrgList.forEach((org)->{
			AA1002List vo = getAA1002List(org);
			dataList.add(vo);
			
		});
		
		return dataList;
	}
	
	private AA1002List getAA1002List (TsmpOrganization org) {
		AA1002List data = new AA1002List();
		
		data.setOrgCode(org.getOrgCode());
		data.setOrgID(org.getOrgId());
		data.setOrgName(org.getOrgName());
		data.setParentID(org.getParentId());
		
		return data;
		
	}
	
	protected TsmpOrganizationDao getTsmpOrganizationDao() {
		return this.tsmpOrganizationDao;
	}

}
