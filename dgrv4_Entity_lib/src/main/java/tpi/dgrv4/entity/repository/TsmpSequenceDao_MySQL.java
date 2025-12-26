package tpi.dgrv4.entity.repository;

import java.math.BigInteger;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import tpi.dgrv4.common.constant.TsmpDpDBMSType;
import tpi.dgrv4.common.utils.ServiceUtil;


@ConditionalOnProperty(name = TsmpDpDBMSType.KEY //
		, havingValue = TsmpDpDBMSType.MySQL)
@Repository
public class TsmpSequenceDao_MySQL implements TsmpSequenceDao {

	@PersistenceContext
	private EntityManager em;

	@Override
	public Long nextSequence(String sequenceName, Long initial, Long increment) {
		StringBuffer sb = new StringBuffer();
		sb.append("select nextval(" + sequenceName + ")");

		Query query = em.createNativeQuery(sb.toString());
		Object seq = query.getSingleResult();
		Long result = ServiceUtil.parseSequenceToLong(seq);

		return result;
	}

	@Override
	public boolean checkSequence(String sequenceName) {
		// TODO Unknown sequence table
		return true;
	}

}
