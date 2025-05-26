package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0192Req extends ReqValidator{
	private String connectionName;
	private String jdbcUrl;
	private String userName;
	private String mima;
	private Integer maxPoolSize;
	private Long connectionTimeout;
	private Long idleTimeout;
	private Long maxLifetime;
	private String dataSourceProperty;
	
	
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


	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		return Arrays.asList(new BeforeControllerRespItem[] {
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("connectionName")
				.isRequired()
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("jdbcUrl")
				.isRequired()
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildString(locale)
				.field("userName")
				.isRequired()
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildInt(locale)
				.field("maxPoolSize")
				.isRequired()
				.min(0)
				.max(2147483647)
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildInt(locale)
				.field("connectionTimeout")
				.isRequired()
				.min(0)
				.max(2147483647)
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildInt(locale)
				.field("idleTimeout")
				.isRequired()
				.min(0)
				.max(2147483647)
				.build(),
				new BeforeControllerRespItemBuilderSelector()
				.buildInt(locale)
				.field("maxLifetime")
				.isRequired()
				.min(0)
				.max(2147483647)
				.build(),
				
		});
	}

}
