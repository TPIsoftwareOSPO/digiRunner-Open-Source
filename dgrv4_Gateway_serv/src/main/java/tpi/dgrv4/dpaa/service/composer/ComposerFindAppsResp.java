package tpi.dgrv4.dpaa.service.composer;

import java.util.List;

public class ComposerFindAppsResp {

	/**
	 * applications 文件<br>
	 * ex: ["{ \"_id\" : { \"$oid\" : \"5c651f79cccc7038c2e0d523\"}, \"id\" : \"63981767f194d18fd775.04b4ec2fb61f\"}"]
	 */
	private List<String> applications;

	public List<String> getApplications() {
		return applications;
	}

	public void setApplications(List<String> applications) {
		this.applications = applications;
	}

}