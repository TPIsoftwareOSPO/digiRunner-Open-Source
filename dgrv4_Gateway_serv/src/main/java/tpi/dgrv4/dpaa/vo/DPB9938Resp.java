package tpi.dgrv4.dpaa.vo;

import java.util.Map;

public class DPB9938Resp {
  private Map<String, DPB9938EsResp> esRespMap;


    public Map<String, DPB9938EsResp> getEsRespMap() {
        return esRespMap;
    }

    public void setEsRespMap(Map<String, DPB9938EsResp> esRespMap) {
        this.esRespMap = esRespMap;
    }
}
