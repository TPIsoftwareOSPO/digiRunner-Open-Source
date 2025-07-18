package tpi.dgrv4.dpaa.record;

import java.util.List;

public record AA0435SrcUrlListRecord(
        String ip,
        List<AA0435SrcUrlAndPercentageRecord> srcUrlAndPercentageList
) {
}
