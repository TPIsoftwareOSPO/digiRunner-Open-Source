package tpi.dgrv4.dpaa.vo;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class DPB0300Resp{

	@JsonProperty("isConn")
    private boolean isConn;
    private String errMsg;
    
}