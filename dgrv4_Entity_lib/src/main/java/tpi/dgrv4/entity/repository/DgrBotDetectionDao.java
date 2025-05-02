package tpi.dgrv4.entity.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.entity.entity.DgrBotDetection;

@Repository
public interface DgrBotDetectionDao extends JpaRepository<DgrBotDetection, Long>, DgrBotDetectionSuperDao {

	public enum BotDetectionType {
		WHITELIST("W"), BLACKLIST("B"),;

		private final String type;

		BotDetectionType(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}
	}

	List<DgrBotDetection> findAllByOrderByCreateDateTimeDescBotDetectionIdDesc();

	Optional<DgrBotDetection> findFirstByTypeOrderByUpdateDateTimeDescBotDetectionIdDesc(String type);

	List<DgrBotDetection> findByTypeOrderByCreateDateTimeAscBotDetectionIdAsc(String type);

	List<DgrBotDetection> findByTypeOrderByCreateDateTimeDescBotDetectionIdDesc(String type);

	Optional<DgrBotDetection> findByBotDetectionId(Long botDetectionId);

	List<DgrBotDetection> findByType(String type);

	List<Long> findBotDetectionIdByTypeOrderByCreateDateTimeDescBotDetectionIdDesc(String type);

	List<Long> findBotDetectionIdByTypeOrderByCreateDateTimeAscBotDetectionIdAsc(String type);

	void deleteByBotDetectionIdIn(List<Long> ids);

}
