package tpi.dgrv4.dpaa.service;

import static tpi.dgrv4.dpaa.util.ServiceUtil.getKeywords;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.dpaa.vo.DPB0039Req;
import tpi.dgrv4.dpaa.vo.DPB0039Resp;
import tpi.dgrv4.dpaa.vo.DPB0039TsmpUser;
import tpi.dgrv4.entity.entity.TsmpUser;
import tpi.dgrv4.entity.repository.TsmpUserDao;
import tpi.dgrv4.gateway.component.ServiceConfig;
import tpi.dgrv4.gateway.vo.TsmpAuthorization;

@Service
public class DPB0039Service {

	private TsmpUserDao tsmpUserDao;
	private ServiceConfig serviceConfig;

	private Integer pageSize;

	@Autowired
	public DPB0039Service(TsmpUserDao tsmpUserDao, ServiceConfig serviceConfig) {
		super();
		this.tsmpUserDao = tsmpUserDao;
		this.serviceConfig = serviceConfig;
	}

	public DPB0039Resp queryTsmpUserLikeList(TsmpAuthorization authorization, DPB0039Req req) {
		String userStatus = "1";
		String lastId = req.getUserId();
		String[] words = getKeywords(req.getKeyword(), " ");
		Integer pageSize = getPageSize();
		List<TsmpUser> userList = getTsmpUserDao().query_dpb0039Service(userStatus, words, lastId, pageSize);
		if (userList == null || userList.isEmpty()) {
			throw TsmpDpAaRtnCode.NO_TSMP_USER.throwing();
		}

		DPB0039Resp resp = new DPB0039Resp();
		List<DPB0039TsmpUser> dpb0039UserList = getDpb0039UserList(userList);
		resp.setUserList(dpb0039UserList);
		return resp;
	}

	private List<DPB0039TsmpUser> getDpb0039UserList(List<TsmpUser> userList) {
		List<DPB0039TsmpUser> dpb0039UserList = new ArrayList<>();

		DPB0039TsmpUser dpb0039User;
		for(TsmpUser user : userList) {
			dpb0039User = new DPB0039TsmpUser();
			dpb0039User.setUserId(user.getUserId());
			dpb0039User.setUserName(user.getUserName());
			dpb0039User.setUserAlias(user.getUserAlias());
			dpb0039User.setUserEmail(user.getUserEmail());
			dpb0039User.setOrgId(user.getOrgId());
			dpb0039UserList.add(dpb0039User);
		}
		
		return dpb0039UserList;
	}

	protected TsmpUserDao getTsmpUserDao() {
		return this.tsmpUserDao;
	}

	protected ServiceConfig getServiceConfig() {
		return this.serviceConfig;
	}

	protected Integer getPageSize() {
		this.pageSize = getServiceConfig().getPageSize("dpb0039");
		return this.pageSize;
	}

}
