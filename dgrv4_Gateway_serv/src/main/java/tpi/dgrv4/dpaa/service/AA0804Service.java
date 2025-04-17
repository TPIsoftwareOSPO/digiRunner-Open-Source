package tpi.dgrv4.dpaa.service;


import static tpi.dgrv4.dpaa.util.ServiceUtil.nvl;

import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.AA0804Req;
import tpi.dgrv4.dpaa.vo.AA0804Resp;
import tpi.dgrv4.entity.entity.TsmpApiReg;
import tpi.dgrv4.entity.entity.jpql.TsmpRegHost;
import tpi.dgrv4.entity.repository.AuthoritiesDao;
import tpi.dgrv4.entity.repository.TsmpApiRegDao;
import tpi.dgrv4.entity.repository.TsmpRegHostDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

@Service
public class AA0804Service {

	private TPILogger logger = TPILogger.tl;

	private AuthoritiesDao authoritiesDao;
	private TsmpRegHostDao tsmpRegHostDao;
	private TsmpApiRegDao tsmpApiRegDao;
	
	@Autowired
	public AA0804Service(AuthoritiesDao authoritiesDao, TsmpRegHostDao tsmpRegHostDao, TsmpApiRegDao tsmpApiRegDao) {
		super();
		this.authoritiesDao = authoritiesDao;
		this.tsmpRegHostDao = tsmpRegHostDao;
		this.tsmpApiRegDao = tsmpApiRegDao;
	}

	@Transactional
	public AA0804Resp deleteRegHost(AA0804Req req) {
		AA0804Resp resp = new AA0804Resp();
		try {
			String regHost = nvl(req.getRegHost());
			String regHostId = nvl(req.getRegHostID());
			
			checkParams(req, regHost, regHostId);
			Long l = getTsmpRegHostDao().deleteByReghostIdAndReghost(regHostId, regHost);
			
			if(l != 1) {
				throw TsmpDpAaRtnCode._1287.throwing();		//1287:刪除失敗
			}
			
		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1287.throwing();		//1287:刪除失敗
		}
		return resp;
	}
	
	private void checkParams(AA0804Req req, String regHost, String regHostId ) {
		TsmpRegHost regHos = getTsmpRegHostDao().findFirstByReghostIdAndReghost(regHostId, regHost);
		if(regHos == null ) {
			throw TsmpDpAaRtnCode._1420.throwing();		//1420:主機不存在
		}
		
		String enabled = nvl(regHos.getEnabled());
		if("Y".equals(enabled)) {
			throw TsmpDpAaRtnCode._1421.throwing();		//1421:註冊主機心跳必須停用
		}
		
		//檢查註冊主機是否被註冊API參考，若有被參考則throw 1422。
		List<TsmpApiReg> apiRegList =  getTsmpApiRegDao().findByReghostId(regHostId);
		if(apiRegList != null && apiRegList.size() > 0) {
			throw TsmpDpAaRtnCode._1422.throwing();		//1422:註冊主機被註冊API參考
		}
	}
	
	protected AuthoritiesDao getAuthoritiesDao() {
		return this.authoritiesDao;
	}
	
	protected TsmpRegHostDao getTsmpRegHostDao() {
		return this.tsmpRegHostDao;
	}

	protected TsmpApiRegDao getTsmpApiRegDao() {
		return this.tsmpApiRegDao;
	}
	
	
}
