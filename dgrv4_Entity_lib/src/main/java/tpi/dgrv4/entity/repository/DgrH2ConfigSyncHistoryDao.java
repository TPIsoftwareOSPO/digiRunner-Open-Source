package tpi.dgrv4.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tpi.dgrv4.entity.vo.DPB0304SearchCriteria;
import tpi.dgrv4.entity.entity.DgrH2ConfigSyncHistory;

import java.util.List;
import java.util.Optional;

@Repository
public interface DgrH2ConfigSyncHistoryDao extends JpaRepository<DgrH2ConfigSyncHistory, Long>, DgrH2ConfigSyncHistorySuperDao {



    List<DgrH2ConfigSyncHistory> query_DPB0304Service(DPB0304SearchCriteria criteria);
    public enum  SyncType{
        MANUAL, SCHEDULE
    }


    public enum H2ConfigSyncStatus {
        RUNNING("R"), DONE("D"), ERROR("E");
        private final String status;

        H2ConfigSyncStatus(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }

    List<DgrH2ConfigSyncHistory> findByStatus(String status);
    boolean existsByStatus(String status);


    Optional<DgrH2ConfigSyncHistory> findTopByStatusInOrderByEndTimeDesc(List<String> statuses);

}
