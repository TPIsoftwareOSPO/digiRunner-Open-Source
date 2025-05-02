package tpi.dgrv4.dpaa.vo;

import java.io.Serializable;

public class DPB0158ItemReq implements Serializable{

	// 機率
    private int probability;

    // 目標URL
    private String url;


    // Getters and Setters
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
}
