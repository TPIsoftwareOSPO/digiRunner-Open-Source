package tpi.dgrv4.entity.repository;

import java.math.BigDecimal;
import java.math.BigInteger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.common.constant.TsmpDpDBMSType;
import tpi.dgrv4.common.utils.ServiceUtil;

@ConditionalOnProperty(name = TsmpDpDBMSType.KEY //
		, havingValue = TsmpDpDBMSType.Oracle)
@Repository
public class TsmpSequenceDao_Oracle implements TsmpSequenceDao {

	@PersistenceContext
	private EntityManager em;

	@Override
	public Long nextSequence(String sequenceName, Long initial, Long increment) {
		StringBuffer sb = new StringBuffer();
		sb.append("SELECT " + sequenceName + ".NEXTVAL FROM dual");

		Query query = em.createNativeQuery(sb.toString());
		Object seq = query.getSingleResult();
		Long result = ServiceUtil.parseSequenceToLong(seq);

		return result;
	}

	@Override
	public boolean checkSequence(String sequenceName) {
		StringBuffer sb = new StringBuffer();
		sb.append(" select count(1) from user_sequences where sequence_name = :name");
		
		Query query = em.createNativeQuery(sb.toString());
		query.setParameter("name", sequenceName);
		final BigDecimal cnt = (BigDecimal) query.getSingleResult();

		final BigDecimal zero = new BigDecimal("0");
		return (cnt != null && cnt.compareTo(zero) > 0);
	}

}
