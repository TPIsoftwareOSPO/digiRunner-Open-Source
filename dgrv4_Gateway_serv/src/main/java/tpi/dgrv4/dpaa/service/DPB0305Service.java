package tpi.dgrv4.dpaa.service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tpi.dgrv4.codec.constant.RandomLongTypeEnum;
import tpi.dgrv4.codec.utils.RandomSeqLongUtil;
import tpi.dgrv4.common.constant.TsmpDpAaRtnCode;
import tpi.dgrv4.common.exceptions.TsmpDpAaException;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.dpaa.vo.DPB0305Req;
import tpi.dgrv4.dpaa.vo.DPB0305Resp;
import tpi.dgrv4.entity.entity.DgrH2ConfigSyncHistory;
import tpi.dgrv4.entity.repository.DgrH2ConfigSyncHistoryDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

import java.util.Arrays;
import java.util.Optional;

@Service
public class DPB0305Service {
    @Getter(AccessLevel.PROTECTED)
    @Setter(onMethod_ = @Autowired)
    private DgrH2ConfigSyncHistoryDao dgrH2ConfigSyncHistoryDao;

    public DPB0305Resp h2ConfigSyncHistoryDetail(DPB0305Req req) {
        DPB0305Resp resp = new DPB0305Resp();
        try {
            String syncIdStr = req.getSyncId();
            Long syncId = validateSyncId(syncIdStr);
            Optional<DgrH2ConfigSyncHistory> syncManualIOpt = getDgrH2ConfigSyncHistoryDao().findById(syncId);
            if (syncManualIOpt.isPresent()) {
                DgrH2ConfigSyncHistory syncManual = syncManualIOpt.get();
                resp.setSyncId(String.valueOf(syncManual.getSyncId()));
                resp.setSyncType(syncManual.getSyncType());
                resp.setScheduleId(syncManual.getScheduleId());
                resp.setSourceId(syncManual.getSourceId());
                resp.setTargetIds(Arrays.stream(syncManual.getTargetIds().split(",")).toList());
                resp.setStatus(syncManual.getStatus());
                resp.setCurrentStep(syncManual.getCurrentStep());
                resp.setProgress(syncManual.getProgress());
                resp.setStartTime(syncManual.getStartTime());
                resp.setEndTime(syncManual.getEndTime());
                resp.setDuration(syncManual.getDuration());
                resp.setErrorMessage(syncManual.getErrorMessage());
                resp.setCreateUser(syncManual.getCreateUser());

            }
        } catch (TsmpDpAaException e) {
            throw e;
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1297.throwing(); // 1297:執行錯誤
        }
        return resp;
    }

    private long validateSyncId(String syncIdStr) {
        Long syncId = null;
        if (syncIdStr == null) {
            throw TsmpDpAaRtnCode._1350.throwing("syncId");
        }
        try {
            syncId = Long.valueOf(syncIdStr);
            RandomSeqLongUtil.toHexString(syncId, RandomLongTypeEnum.YYYYMMDD);
        } catch (Exception e) {
            TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
            throw TsmpDpAaRtnCode._1352.throwing("syncId");
        }

        if (!getDgrH2ConfigSyncHistoryDao().existsById(syncId)) {
            throw TsmpDpAaRtnCode._1298.throwing(); // 1298:查無資料
        }

        return syncId;

    }
}
