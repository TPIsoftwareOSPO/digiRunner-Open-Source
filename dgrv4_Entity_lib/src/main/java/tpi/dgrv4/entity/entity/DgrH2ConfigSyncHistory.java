package tpi.dgrv4.entity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.common.utils.DateTimeUtil;
import tpi.dgrv4.entity.component.dgrSeq.DgrSeq;

import java.util.Date;
@Entity
@Data
@Table(name = "DGR_H2_CONFIG_SYNC_HISTORY")
public class DgrH2ConfigSyncHistory implements DgrSequenced {

    @Id
    @DgrSeq(strategy = RandomLongTypeEnum.YYYYMMDD)
    @Column(name = "SYNC_ID")
    private Long syncId;

    @Column(name = "SYNC_TYPE")
    private String syncType;

    @Column(name = "SCHEDULE_ID")
    private Long scheduleId;

    @Column(name = "SOURCE_ID")
    private String sourceId;

    @Column(name = "TARGET_IDS")
    private String targetIds;

    @Column(name = "STATUS")
    private String status;

    @Column(name = "CURRENT_STEP")
    private String currentStep;

    @Column(name = "PROGRESS")
    private Integer progress;

    @Column(name = "ERROR_MESSAGE")
    private String errorMessage;

    @Column(name = "START_TIME")
    private Date startTime;

    @Column(name = "END_TIME")
    private Date endTime;

    @Column(name = "DURATION")
    private Long duration;

    @Column(name = "CREATE_DATE_TIME")
    private Date createDateTime = DateTimeUtil.now();

    @Column(name = "CREATE_USER")
    private String createUser = "SYSTEM";


    @Column(name = "UPDATE_DATE_TIME")
    private Date updateDateTime ;

    @Override
    public Long getPrimaryKey() {
        return syncId;
    }
}
