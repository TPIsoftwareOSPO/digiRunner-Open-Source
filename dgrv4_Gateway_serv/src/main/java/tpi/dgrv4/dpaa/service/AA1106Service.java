package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA1106Req;
import tpi.dgrv4.dpaa.vo.AA1106Resp;
import tpi.dgrv4.entity.entity.TsmpGroupAuthorities;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1106Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao;

	@Autowired
	public AA1106Service(TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao) {
		super();
		this.tsmpGroupAuthoritiesDao = tsmpGroupAuthoritiesDao;
	}


	public AA1106Resp addTGroupAuthority(TsmpAuthorization authorization, AA1106Req req) {
		AA1106Resp resp = new AA1106Resp();

		try {
			String groupAuthoritieId = req.getGroupAuthoritieId();
			String groupAuthoritieName = req.getGroupAuthoritieName();
			String groupAuthoritieDesc = req.getGroupAuthoritieDesc();
			String groupAuthoritieLevel = req.getGroupAuthoritieLevel();
			
			//1. 核身型式代碼 不可在 TSMP_GROUP_AUTHORITIES 中重複, 否則 throw 1284 ([{{groupAuthoritieId}}] 不得重複)				
			TsmpGroupAuthorities groupAuthVo = getTsmpGroupAuthoritiesDao().findById(groupAuthoritieId).orElse(null);
			if(groupAuthVo != null) {
				//1284:[{{0}}] 不得重複
				throw TsmpDpAaRtnCode._1284.throwing("{{groupAuthoritieId}}");
			}
			
			//2. 核身型式名稱 不可在 TSMP_GROUP_AUTHORITIES 中重複, 否則 throw 1284 ([{{groupAuthoritieName}}] 不得重複)				
			groupAuthVo = getTsmpGroupAuthoritiesDao().findFirstByGroupAuthoritieName(groupAuthoritieName);
			if(groupAuthVo != null) {
				//1284:[{{0}}] 不得重複
				throw TsmpDpAaRtnCode._1284.throwing("{{groupAuthoritieName}}");
			}
			
			groupAuthVo = new TsmpGroupAuthorities();
			groupAuthVo.setGroupAuthoritieId(groupAuthoritieId);
			groupAuthVo.setGroupAuthoritieName(groupAuthoritieName);
			groupAuthVo.setGroupAuthoritieDesc(groupAuthoritieDesc);
			groupAuthVo.setGroupAuthoritieLevel(groupAuthoritieLevel.trim());
			
			getTsmpGroupAuthoritiesDao().saveAndFlush(groupAuthVo);
			
			resp.setGroupAuthoritieName(groupAuthoritieName);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());
			
		} catch (TsmpDpAaException aa1106_e) {
			throw aa1106_e;
		} catch (Exception aa1106_e) {
			if(ServiceUtil.isValueTooLargeException(aa1106_e)) {
				//1220:儲存失敗，資料長度過大
				throw TsmpDpAaRtnCode._1220.throwing();
			}else {
				this.logger.error(StackTraceUtil.logStackTrace(aa1106_e));
				//1297:執行錯誤
				throw TsmpDpAaRtnCode._1297.throwing();
			}
			
		}
		return resp;
	}


	protected TsmpGroupAuthoritiesDao getTsmpGroupAuthoritiesDao() {
		return tsmpGroupAuthoritiesDao;
	}

}
