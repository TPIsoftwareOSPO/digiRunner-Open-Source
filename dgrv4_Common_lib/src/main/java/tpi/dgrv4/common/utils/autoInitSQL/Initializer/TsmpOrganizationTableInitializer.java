package tpi.dgrv4.common.utils.autoInitSQL.Initializer;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Service;

import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.common.utils.autoInitSQL.vo.TsmpOrganizationVo;

@Service
public class TsmpOrganizationTableInitializer {

	public static final String DEFAULT_ORG_ID="100000";

	private  List<TsmpOrganizationVo> tsmpOrganizationList = new LinkedList<>();

	public List<TsmpOrganizationVo> insertTsmpOrganization() {
		try {
			String orgId;
			String orgName;
			String parentId;
			String orgPath;
			String createUser;
			createTsmpOrganization((orgId = DEFAULT_ORG_ID), (orgName = "TSMPDefaultRoot"), (parentId = ""),(orgPath = "100000"), (createUser = "manager"));

		} catch (Exception e) {
			StackTraceUtil.logStackTrace(e);
			throw e;
		}
		return tsmpOrganizationList;
	}

	protected void createTsmpOrganization(String orgId, String orgName, String parentId, String orgPath,
			String createUser) {

		TsmpOrganizationVo tsmpOrganization = new TsmpOrganizationVo();
		tsmpOrganization.setOrgId(orgId);
		tsmpOrganization.setOrgName(orgName);
		tsmpOrganization.setParentId(parentId);
		tsmpOrganization.setOrgPath(orgPath);
		tsmpOrganization.setCreateTime(DateTimeUtil.now());
		tsmpOrganization.setCreateUser(createUser);
		tsmpOrganizationList.add(tsmpOrganization);

	}

}
