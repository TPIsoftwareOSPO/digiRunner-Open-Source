package tpi.dgrv4.dpaa.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA1110Req;
import tpi.dgrv4.dpaa.vo.AA1110Resp;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpGroupAuthorities;
import tpi.dgrv4.entity.entity.TsmpGroupAuthoritiesMap;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesDao;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesMapDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1110Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao;
	private TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao;
	private TsmpGroupDao tsmpGroupDao;

	@Autowired
	public AA1110Service(TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao,
			TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao, TsmpGroupDao tsmpGroupDao) {
		super();
		this.tsmpGroupAuthoritiesDao = tsmpGroupAuthoritiesDao;
		this.tsmpGroupAuthoritiesMapDao = tsmpGroupAuthoritiesMapDao;
		this.tsmpGroupDao = tsmpGroupDao;
	}


	public AA1110Resp deleteTGroupAuthority(TsmpAuthorization authorization, AA1110Req req) {
		AA1110Resp resp = new AA1110Resp();

		try {
			String groupAuthoritieId = req.getGroupAuthoritieId();
			String groupAuthoritieName = req.getGroupAuthoritieName();
			
			//1. 以 核身型式代碼、核身型式名稱 查詢資料，若查無資料則 throw 1298。
			TsmpGroupAuthorities groupAuthVo = getTsmpGroupAuthoritiesDao().findFirstByGroupAuthoritieIdAndGroupAuthoritieName(groupAuthoritieId, groupAuthoritieName);
			if(groupAuthVo == null) {
				//1298:查無資料
				throw TsmpDpAaRtnCode._1298.throwing();
			}
			
			//2. 若仍有群組與此核身資料相關聯(TSMP_GROUP_AUTHORITIES_MAP), 則不允許刪除並 throw 1404。
			//  -> 找出第一筆的 TSMP_GROUP_AUTHORITIES_MAP 資料，並以 GROUP_ID 關聯 TSMP_GROUP 取得回傳訊息的參數
			//   -> 參數格式為 "groupName(groupAlias)"，訊息："無法刪除，請解除群組的授權核身種類: 群組代號(群組名稱)"
//			/   -> 若無 groupAlias 則帶空字串
			List<TsmpGroupAuthoritiesMap> groupAuthMapList = getTsmpGroupAuthoritiesMapDao().findByGroupAuthoritieId(groupAuthoritieId);
			if(groupAuthMapList.size() > 0) {
				TsmpGroupAuthoritiesMap groupAuthMapVo = groupAuthMapList.get(0);
				TsmpGroup groupVo = getTsmpGroupDao().findById(groupAuthMapVo.getGroupId()).orElse(null);
				String errMsg = "";
				if(groupVo != null) {
					errMsg = groupVo.getGroupName() + "(" + ServiceUtil.nvl(groupVo.getGroupAlias())+")";
				}
				//1298:查無資料
				throw TsmpDpAaRtnCode._1404.throwing(errMsg);
			}
			
			//刪除資料
			getTsmpGroupAuthoritiesDao().delete(groupAuthVo);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			//1287:刪除失敗
			throw TsmpDpAaRtnCode._1287.throwing();
		}
		return resp;
	}


	protected TsmpGroupAuthoritiesDao getTsmpGroupAuthoritiesDao() {
		return tsmpGroupAuthoritiesDao;
	}

	protected TsmpGroupAuthoritiesMapDao getTsmpGroupAuthoritiesMapDao() {
		return tsmpGroupAuthoritiesMapDao;
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}

}
