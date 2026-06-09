package tpi.dgrv4.entity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;
import tpi.dgrv4.entity.constant.FhirInteraction;

@Entity
@Table(name = "dgr_smart_on_fhir_proxy_sticky")
@Getter
@Setter
@ToString
public class DgrSmartOnFhirProxySticky implements DgrSequenced {

	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "sof_proxy_sticky_id")
	private Long sofProxyStickyId;

	@Column(name = "sof_proxy_id")
	private Long sofProxyId;

	@Column(name = "sof_proxy_diversion_id")
	private Long sofProxyDiversionId;

	@Enumerated(EnumType.STRING)
	@Column(name = "sof_proxy_sticky_interaction")
	private FhirInteraction sofProxyStickyInteraction;

	@Column(name = "sof_proxy_sticky_type")
	private String sofProxyStickyType;

	@Column(name = "sof_proxy_sticky_type_id")
	private String sofProxyStickyTypeId;

	@Column(name = "sof_proxy_sticky_verb")
	private String sofProxyStickyVerb;

	@Column(name = "sof_proxy_sticky_path")
	private String sofProxyStickyPath;

	@Column(name = "sof_proxy_sticky_hashcode")
	private String sofProxyStickyHashcode;

	@Column(name = "create_date_time")
	private Date createDateTime = DateTimeUtil.now();

	@Column(name = "create_user")
	private String createUser = "SYSTEM";

	@Column(name = "update_date_time")
	private Date updateDateTime;

	@Column(name = "update_user")
	private String updateUser;

	@Version
	@Column(name = "version")
	private Long version = 1L;

	@Override
	public Long getPrimaryKey() {
		return sofProxyStickyId;
	}
}
