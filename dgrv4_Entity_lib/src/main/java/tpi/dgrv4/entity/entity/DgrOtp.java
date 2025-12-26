package tpi.dgrv4.entity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;

@Accessors(chain = true)
@Getter
@Setter
@ToString
@Entity
@Table(name = "dgr_otp")
public class DgrOtp implements DgrSequenced {

	@Id
	@DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
	@Column(name = "otp_id")
	private Long otpId;

	@Column(name = "email")
	private String email;
	
	@Column(name = "otp_code")
	private String otpCode;
	
	@Column(name = "expire_key")
	private String expireKey;
	
	@Column(name = "error_limit")
	private int errorLimit = 0;
	
	@Override
	public Long getPrimaryKey() {
		return otpId;
	}


	public DgrOtp() {}


}
