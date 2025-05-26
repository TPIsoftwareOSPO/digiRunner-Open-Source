package tpi.dgrv4.dpaa.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class AA1213RespItem {
	private String nodeName;
	private String apiName = "N/A";
	private String uri;
	private List<String> labelList;
	private Integer statusCode;
	private Integer elapsedTime;
}
