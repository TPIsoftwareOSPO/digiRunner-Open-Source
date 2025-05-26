package tpi.dgrv4.dpaa.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.vo.ReqHeader;
import tpi.dgrv4.dpaa.util.ServiceUtil;
import tpi.dgrv4.dpaa.vo.AA0213GroupAuthorities;
import tpi.dgrv4.dpaa.vo.AA0213Req;
import tpi.dgrv4.dpaa.vo.AA0213Resp;
import tpi.dgrv4.dpaa.vo.AA0213SecurityLevel;
import tpi.dgrv4.dpaa.vo.ConvertTimeUnitRst;
import tpi.dgrv4.entity.entity.TsmpGroup;
import tpi.dgrv4.entity.entity.TsmpGroupAuthorities;
import tpi.dgrv4.entity.entity.TsmpGroupAuthoritiesMap;
import tpi.dgrv4.entity.entity.jpql.TsmpSecurityLevel;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesDao;
import tpi.dgrv4.entity.repository.TsmpGroupAuthoritiesMapDao;
import tpi.dgrv4.entity.repository.TsmpGroupDao;
import tpi.dgrv4.entity.repository.TsmpSecurityLevelDao;
import tpi.dgrv4.gateway.keeper.TPILogger;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class AA0213Service {

	private TPILogger logger = TPILogger.tl;

	private TsmpGroupDao tsmpGroupDao;
	private TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao;
	private TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao;
	private TsmpSecurityLevelDao tsmpSecurityLevelDao;

	@Autowired
	public AA0213Service(TsmpGroupDao tsmpGroupDao, TsmpGroupAuthoritiesMapDao tsmpGroupAuthoritiesMapDao,
			TsmpGroupAuthoritiesDao tsmpGroupAuthoritiesDao, TsmpSecurityLevelDao tsmpSecurityLevelDao) {
		super();
		this.tsmpGroupDao = tsmpGroupDao;
		this.tsmpGroupAuthoritiesMapDao = tsmpGroupAuthoritiesMapDao;
		this.tsmpGroupAuthoritiesDao = tsmpGroupAuthoritiesDao;
		this.tsmpSecurityLevelDao = tsmpSecurityLevelDao;
	}

	public AA0213Resp queryGroupDetail(TsmpAuthorization authorization, AA0213Req req, ReqHeader reqHeader) {
		AA0213Resp resp = new AA0213Resp();
		try {

			Optional<TsmpGroup> opt_tsmpGroup = getTsmpGroupDao().findById(req.getGroupID());
			if (opt_tsmpGroup.isPresent() == false) {
				throw TsmpDpAaRtnCode._1360.throwing();
			}

			// 將Group資料轉換成VO物件
			parsingTsmpGroup(resp, opt_tsmpGroup, reqHeader.getLocale());

		} catch (TsmpDpAaException e) {
			throw e;
		} catch (Exception e) {
			this.logger.error(StackTraceUtil.logStackTrace(e));
			throw TsmpDpAaRtnCode._1297.throwing();
		}
		return resp;
	}

	private void parsingTsmpGroup(AA0213Resp resp, Optional<TsmpGroup> opt_tsmpGroup, String locale) {
		if (opt_tsmpGroup.isEmpty()) {
			var log = Map.of("resp", resp, "locale", locale, "opt_tsmpGroup", opt_tsmpGroup).toString();
			logger.warn(log);
			return;
		}
		TsmpGroup tsmpGroup = opt_tsmpGroup.get();
		
		Integer time = tsmpGroup.getAllowDays();
		if(time == null ) {
			time = 0;
		}
		ConvertTimeUnitRst reult = ServiceUtil.convertTimeUnit(time, locale);
		resp.setApproximateTimeUnit(reult.getApproximateTimeUnit());
		resp.setAllowAccessDays(reult.getTime());
		resp.setTimeUnit(reult.getTimeUnit());
		resp.setTimeUnitName(reult.getTimeUnitName());
		
		
		resp.setAllowAccessUseTimes(tsmpGroup.getAllowTimes() == null ? 0 : tsmpGroup.getAllowTimes());
		
		Optional<String> createDateOpt = DateTimeUtil.dateTimeToString(tsmpGroup.getCreateTime(), null);
		if (createDateOpt.isPresent()) {
			resp.setCreateDate(createDateOpt.get());
		}
		resp.setCreateUser(tsmpGroup.getCreateUser());
		resp.setGroupAccessList(splitParams(tsmpGroup.getGroupAccess()));			
		resp.setGroupAlias(tsmpGroup.getGroupAlias());

		//授權核身種類
		parsingGroupAuthorities(resp, tsmpGroup);
		
		resp.setGroupDesc(tsmpGroup.getGroupDesc());
		resp.setGroupID(tsmpGroup.getGroupId());
		resp.setGroupName(tsmpGroup.getGroupName());
		
		//安全等級
		parsingSecurityLevel(resp, tsmpGroup);
		
		Optional<String> updateDateOpt = DateTimeUtil.dateTimeToString(tsmpGroup.getUpdateTime(), null);
		if (updateDateOpt.isPresent()) {
			resp.setUpdateDate(updateDateOpt.get());
		}
		
		resp.setUpdateUser(tsmpGroup.getUpdateUser());
	}

	private void parsingSecurityLevel(AA0213Resp resp, TsmpGroup tsmpGroup) {
		AA0213SecurityLevel aa0213SecurityLevel = new AA0213SecurityLevel();
		aa0213SecurityLevel.setSecurityLevelId(tsmpGroup.getSecurityLevelId());
		if(tsmpGroup.getSecurityLevelId() != null) {
			Optional<TsmpSecurityLevel> opt_tsmpSecurityLevel = getTsmpSecurityLevelDao().findById(tsmpGroup.getSecurityLevelId());
			if (opt_tsmpSecurityLevel.isPresent()) {
				aa0213SecurityLevel.setSecurityLevelName(opt_tsmpSecurityLevel.get().getSecurityLevelName());
			}
		}
		resp.setSecurityLevel(aa0213SecurityLevel);
	}

	private void parsingGroupAuthorities(AA0213Resp resp, TsmpGroup tsmpGroup) {
		ArrayList<AA0213GroupAuthorities> aa0213GroupAuthorities = new ArrayList<AA0213GroupAuthorities>();
		resp.setGroupAuthorities(aa0213GroupAuthorities);
		List<TsmpGroupAuthoritiesMap> tsmpGroupAuthoritiesMapList = getTsmpGroupAuthoritiesMapDao().findByGroupId(tsmpGroup.getGroupId());
		if (tsmpGroupAuthoritiesMapList != null && tsmpGroupAuthoritiesMapList.size() > 0) {
			for (TsmpGroupAuthoritiesMap tsmpGroupAuthoritiesMap : tsmpGroupAuthoritiesMapList) {
				Optional<TsmpGroupAuthorities> opt_tsmpGroupAuthorities = getTsmpGroupAuthoritiesDao().findById(tsmpGroupAuthoritiesMap.getGroupAuthoritieId());
				if (opt_tsmpGroupAuthorities.isPresent()) {
					AA0213GroupAuthorities groupAuthorities = new AA0213GroupAuthorities();
					groupAuthorities.setGroupAuthoritieId(opt_tsmpGroupAuthorities.get().getGroupAuthoritieId());
					groupAuthorities.setGroupAuthoritieName(opt_tsmpGroupAuthorities.get().getGroupAuthoritieName());
					aa0213GroupAuthorities.add(groupAuthorities);
				}
			}
		}
	}

	private List<String> splitParams(String params) {
		if (StringUtils.isEmpty(params)) {
			return Arrays.asList();
		}
		String splitStr = params.substring(1, params.length() - 1);
		return Arrays.asList(splitStr.split(",")).stream().map(str -> str.replace("\"", ""))
				.collect(Collectors.toList());
	}

	protected TsmpGroupDao getTsmpGroupDao() {
		return tsmpGroupDao;
	}

	protected TsmpGroupAuthoritiesMapDao getTsmpGroupAuthoritiesMapDao() {
		return tsmpGroupAuthoritiesMapDao;
	}

	protected TsmpGroupAuthoritiesDao getTsmpGroupAuthoritiesDao() {
		return tsmpGroupAuthoritiesDao;
	}

	protected TsmpSecurityLevelDao getTsmpSecurityLevelDao() {
		return tsmpSecurityLevelDao;
	}

}
