package tpi.dgrv4.gateway.service;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface ISysInfoService {
	public String getCpuMemoryInfo();
	
	public String getSysInfo() throws JsonProcessingException;
}
