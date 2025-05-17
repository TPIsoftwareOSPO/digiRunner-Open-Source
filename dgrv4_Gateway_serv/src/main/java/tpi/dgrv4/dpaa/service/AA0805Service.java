package tpi.dgrv4.dpaa.service;

import java.util.Date;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.dpaa.vo.AA0805Req;
import tpi.dgrv4.dpaa.vo.AA0805Resp;
import tpi.dgrv4.entity.entity.jpql.TsmpRegHost;
import tpi.dgrv4.entity.repository.TsmpRegHostDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0805Service {

	private TsmpRegHostDao tsmpRegHostDao;

	private TPILogger logger = TPILogger.tl;

	@Autowired
	public AA0805Service(TsmpRegHostDao tsmpRegHostDao) {
		super();
		this.tsmpRegHostDao = tsmpRegHostDao;
	}

	public AA0805Resp sendHeartbeat(TsmpAuthorization auth, AA0805Req req) {
		AA0805Resp resp = null;
		try {

			String regHost = req.getRegHost();
			String regHostStatus = req.getRegHostStatus();
			
			if (StringUtils.isEmpty(regHost)) {
				throw TsmpDpAaRtnCode._1488.throwing();
			}			
			if (regHost.length()>=31) {
				throw TsmpDpAaRtnCode._1489.throwing("30", String.valueOf(regHost.length()));
			}
			
			if (regHostStatus !=null && regHostStatus.length()>=2) {
				throw TsmpDpAaRtnCode._1490.throwing("1", String.valueOf(regHostStatus.length()));
			}
			HashSet<String> map = new HashSet<String>();
			map.add("A");
			map.add("S");
			if (StringUtils.isEmpty(regHostStatus)==false&&map.contains(regHostStatus)==false) {
				throw TsmpDpAaRtnCode._1487.throwing();
			}
			

			List<TsmpRegHost> tsmpRegHostList = getTsmpRegHostDao().findByReghost(regHost);
			if (tsmpRegHostList == null || tsmpRegHostList.isEmpty()) {
				throw TsmpDpAaRtnCode._1484.throwing();
			}

			TsmpRegHost tsmpRegHost = tsmpRegHostList.get(0);
			if (!auth.getClientId().equals(tsmpRegHost.getClientid())) {
				throw TsmpDpAaRtnCode._1485.throwing();
			}

			if ("N".equals(tsmpRegHost.getEnabled())) {
				throw TsmpDpAaRtnCode._1486.throwing();
			}

			if (StringUtils.isEmpty(regHostStatus)) {
				regHostStatus = "A";
			}

			if ("A".equals(regHostStatus)) {
				tsmpRegHost.setReghostStatus(regHostStatus);
				tsmpRegHost.setHeartbeat(new Date());
			} else {
				tsmpRegHost.setReghostStatus(regHostStatus);
			}
			getTsmpRegHostDao().saveAndFlush(tsmpRegHost);

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	protected TsmpRegHostDao getTsmpRegHostDao() {
		return tsmpRegHostDao;
	}

}
