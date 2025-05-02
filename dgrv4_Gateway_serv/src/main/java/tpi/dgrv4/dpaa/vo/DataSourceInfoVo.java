package tpi.dgrv4.dpaa.vo;

import com.zaxxer.hikari.HikariDataSource;

public class DataSourceInfoVo {
	private Long version;
	private HikariDataSource hikariDataSource;
	
	public Long getVersion() {
		return version;
	}
	public void setVersion(Long version) {
		this.version = version;
	}
	public HikariDataSource getHikariDataSource() {
		return hikariDataSource;
	}
	public void setHikariDataSource(HikariDataSource hikariDataSource) {
		this.hikariDataSource = hikariDataSource;
	}
	
	
	
}
