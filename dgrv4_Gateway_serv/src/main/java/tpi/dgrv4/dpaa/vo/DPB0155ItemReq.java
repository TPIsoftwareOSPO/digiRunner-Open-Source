package tpi.dgrv4.dpaa.vo;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import tpi.dgrv4.common.component.validator.BeforeControllerRespItemBuilderSelector;
import tpi.dgrv4.common.component.validator.ReqValidator;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.vo.BeforeControllerRespItem;

/**
 * DPB0155ItemReq VO object
 */
public class DPB0155ItemReq  extends ReqValidator implements Serializable{
	
	// 機率 (Probability)
    private int probability;

    // 目標URL (Target URL)
    private String url;

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
	
    public boolean checkdate() {
        return isProbabilityValid() && isUrlValid();
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

	@Override
	protected List<BeforeControllerRespItem> provideConstraints(String locale) {
		String regex="^(https?:\\/\\/)";

		return Arrays.asList(
				new BeforeControllerRespItemBuilderSelector()
					.buildInt(locale)
					.field("probability")
					.isRequired()
					.build(),
				new BeforeControllerRespItemBuilderSelector()
					.buildString(locale)
					.field("url")
					.isRequired()
					.maxLength(1000)
					.pattern(regex,TsmpDpAaRtnCode._1405.getCode(), null)
					.build());
	}
}
