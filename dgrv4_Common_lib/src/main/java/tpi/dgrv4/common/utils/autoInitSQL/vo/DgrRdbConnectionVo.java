package tpi.dgrv4.common.utils.autoInitSQL.vo;

public class DgrRdbConnectionVo {

	private String connectionName;

	private String jdbcUrl;

	private String userName;

	private String mima;
	
	private Integer maxPoolSize = 10;

	private Long connectionTimeout = 30000L;

	private Long idleTimeout = 600000L;
	
	private Long maxLifetime = 1800000L;
	
	private String dataSourceProperty;

	@Override
	public String toString() {
		return "DgrRdbConnectionVo [connectionName=" + connectionName + ", jdbcUrl=" + jdbcUrl + ", userName="
				+ userName + ", mima=" + mima + ", maxPoolSize=" + maxPoolSize + ", connectionTimeout="
				+ connectionTimeout + ", idleTimeout=" + idleTimeout + ", maxLifetime=" + maxLifetime
				+ ", dataSourceProperty=" + dataSourceProperty + "]";
	}

	public String getConnectionName() {
		return connectionName;
	}

	public void setConnectionName(String connectionName) {
		this.connectionName = connectionName;
	}

	public String getJdbcUrl() {
		return jdbcUrl;
	}

	public void setJdbcUrl(String jdbcUrl) {
		this.jdbcUrl = jdbcUrl;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getMima() {
		return mima;
	}

	public void setMima(String mima) {
		this.mima = mima;
	}

	public Integer getMaxPoolSize() {
		return maxPoolSize;
	}

	public void setMaxPoolSize(Integer maxPoolSize) {
		this.maxPoolSize = maxPoolSize;
	}

	public Long getConnectionTimeout() {
		return connectionTimeout;
	}

	public void setConnectionTimeout(Long connectionTimeout) {
		this.connectionTimeout = connectionTimeout;
	}

	public Long getIdleTimeout() {
		return idleTimeout;
	}

	public void setIdleTimeout(Long idleTimeout) {
		this.idleTimeout = idleTimeout;
	}

	public Long getMaxLifetime() {
		return maxLifetime;
	}

	public void setMaxLifetime(Long maxLifetime) {
		this.maxLifetime = maxLifetime;
	}

	public String getDataSourceProperty() {
		return dataSourceProperty;
	}

	public void setDataSourceProperty(String dataSourceProperty) {
		this.dataSourceProperty = dataSourceProperty;
	}

}
