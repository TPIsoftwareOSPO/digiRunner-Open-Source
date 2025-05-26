package tpi.dgrv4.entity.component;

import tpi.dgrv4.entity.vo.VersionInfo;

public interface IVersionService {

	VersionInfo getVersion();
	
	String getProjectName();
}
