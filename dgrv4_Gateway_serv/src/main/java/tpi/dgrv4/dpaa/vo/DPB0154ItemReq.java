package tpi.dgrv4.dpaa.vo;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

public class DPB0154ItemReq {

	private int probability; // 機率
	private String url; // 目標URL

	public int getProbability() {
		return probability;
	}

	public void setProbability(int probability) {
		this.probability = probability;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@JsonIgnore
    // 檢查 probability 是否為非負整數
    public boolean isProbabilityValid() {
        return probability >= 0;
    }

	@JsonIgnore
    // 檢查 url 是否符合限制條件：不為空值、長度最大為 1000、字串的開頭為 https:// 或 http://
    public boolean isUrlValid() {
        return url != null && url.length() <= 1000 && (url.startsWith("https:") || url.startsWith("http:"));
    }

}
