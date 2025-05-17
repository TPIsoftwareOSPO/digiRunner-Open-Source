package tpi.dgrv4.dpaa.vo;

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class AA1213Resp {
	private List<AA1213RespItem> dataList;
}
