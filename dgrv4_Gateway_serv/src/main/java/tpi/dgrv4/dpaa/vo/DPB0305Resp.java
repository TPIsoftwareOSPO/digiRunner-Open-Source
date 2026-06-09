package tpi.dgrv4.dpaa.vo;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DPB0305Resp {
    private String syncId;
    private String syncType;
    private Long scheduleId;
    private String sourceId;
    private List<String> targetIds;
    private String status;
    private String currentStep;
    private Integer progress;
    private Date startTime;
    private Date endTime;
    private Long duration;
    private String errorMessage;
    private String createUser;
    private Date updateDateTime;
    private Date createDateTime;
}
