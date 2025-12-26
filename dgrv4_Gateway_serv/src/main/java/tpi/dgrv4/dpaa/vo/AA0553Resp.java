package tpi.dgrv4.dpaa.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class AA0553Resp {

	private String acPwdStrength;
	private String acPwdStrengthDesc;
	private String clientPwdStrength;
	private String clientPwdStrengthDesc;
	
}