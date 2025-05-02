package tpi.dgrv4.dpaa.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA1108Req;
import tpi.dgrv4.dpaa.vo.AA1108Resp;
import tpi.dgrv4.entity.entity.TsmpGroupAuthorities;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesDao;
import tpi.dgrv4.gateway.constant.DgrDataType;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA1108Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao;

	@Autowired
	public AA1108Service(TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao) {
		super();
		this.tsmpGroupAuthoritiesDao = tsmpGroupAuthoritiesDao;
	}


	public AA1108Resp updateTGroupAuthority(TsmpAuthorization authorization, AA1108Req req) {
		AA1108Resp resp = new AA1108Resp();

		try {
			String groupAuthoritieId = req.getGroupAuthoritieId();
			String oriGroupAuthoritieName = req.getOriGroupAuthoritieName();
			String newGroupAuthoritieName = req.getNewGroupAuthoritieName();
			String newGroupAuthoritieDesc = req.getNewGroupAuthoritieDesc();
			String newGroupAuthoritieLevel = req.getNewGroupAuthoritieLevel();
			

			//1. 依照 原始資料-核身型式代碼 及 原始資料-核身型式名稱 找不到資料時(TSMP_GROUP_AUTHORITIES) throw 1298。				
			TsmpGroupAuthorities groupAuthVo = getTsmpGroupAuthoritiesDao().findFirstByGroupAuthoritieIdAndGroupAuthoritieName(groupAuthoritieId, oriGroupAuthoritieName);
			if(groupAuthVo == null) {
				//1298:查無資料
				throw TsmpDpAaRtnCode._1298.throwing();
			}

			//2. 當 原始資料-核身型式名稱 與 新-核身型式名稱 相異時進行檢查，新-核身型式名稱 不可在 TSMP_GROUP_AUTHORITIES 中重複,
			//否則 throw 1284 ([{{newGroupAuthoritieName}}] 不得重複)
			if(!oriGroupAuthoritieName.equals(newGroupAuthoritieName)) {
				TsmpGroupAuthorities vo = getTsmpGroupAuthoritiesDao().findFirstByGroupAuthoritieName(newGroupAuthoritieName);
				if(vo != null) {
					//1284:[{{0}}] 不得重複
					throw TsmpDpAaRtnCode._1284.throwing("{{newGroupAuthoritieName}}");
				}
			}
			
			
			groupAuthVo.setGroupAuthoritieId(groupAuthoritieId);
			groupAuthVo.setGroupAuthoritieName(newGroupAuthoritieName);
			groupAuthVo.setGroupAuthoritieDesc(newGroupAuthoritieDesc);
			groupAuthVo.setGroupAuthoritieLevel(newGroupAuthoritieLevel);
			
			getTsmpGroupAuthoritiesDao().saveAndFlush(groupAuthVo);

			// in-memory, 用列舉的值傳入值
			TPILogger.updateTime4InMemory(DgrDataType.CLIENT.value());
			
		} catch (TsmpDpAaException aa1108_e) {
			throw aa1108_e;
		} catch (Exception aa1108_e) {
			if(ServiceUtil.isValueTooLargeException(aa1108_e)) {
				//1220:儲存失敗，資料長度過大
				throw TsmpDpAaRtnCode._1220.throwing();
			}else {
				this.logger.error(StackTraceUtil.logStackTrace(aa1108_e));
				//1286:更新失敗
				throw TsmpDpAaRtnCode._1286.throwing();
			}
			
		}
		return resp;
	}


	protected TsmpGroupAuthoritiesDao getTsmpGroupAuthoritiesDao() {
		return tsmpGroupAuthoritiesDao;
	}

}
