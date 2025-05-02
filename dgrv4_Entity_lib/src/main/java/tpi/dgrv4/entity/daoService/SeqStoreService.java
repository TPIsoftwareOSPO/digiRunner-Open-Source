package tpi.dgrv4.entity.daoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import tpi.dgrv4.common.keeper.ITPILogger;
import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.constant.TsmpSequenceName;
import tpi.dgrv4.entity.repository.SeqStoreDao;
import tpi.dgrv4.entity.repository.TsmpSequenceDao;

@RequiredArgsConstructor
@Getter(AccessLevel.PROTECTED)
@Service
public class SeqStoreService {

	private final SeqStoreDao seqStoreDao;
	private final TsmpSequenceDao tsmpSequenceDao;

	private static Object lock = new Object();

	/**
	 * 取得下一個序號，起始值為2000000000L
	 * @param sequenceName
	 * @return
	 */
	public Long nextSequence(String sequenceName) {
		return nextSequence(sequenceName, 2000000000L, 1L);
	}

	public Long nextSequence(String sequenceName, Long initial, //
			Long increment) {
		if (initial == null) {
			initial = 2000000000L;
		}
		if (increment == null || increment.longValue() < 1L) {
			increment = 2000000000L;
		}

		Long nextVal = null;
		synchronized (lock) {
			nextVal = continuousGetSequence(sequenceName, initial, increment);
		}
		return nextVal;
	}

	private Long continuousGetSequence(String sequenceName, Long initial, Long increment) {
		Long nextVal = null;
		do {
			nextVal = getSeqStoreDao().nextSequence(sequenceName, initial, increment);
			if (nextVal == null) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					ITPILogger.tl.debug("InterruptedException:\n" + StackTraceUtil.logStackTrace(e));
					Thread.currentThread().interrupt();
					nextVal = initial;
				}
			}
		} while (nextVal == null);
		return nextVal;
	}
	
	/**
	 * 取得由TSMP平台控管的下一個序號, 起始值為2000000000L, 增量為1
	 * @param sequenceName
	 * @return
	 */
	public Long nextTsmpSequence(TsmpSequenceName sequenceName) {
		try {
			return getTsmpSequenceDao().nextSequence(sequenceName.name());
		} catch (Exception e) {
			ITPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			return null;
		}
	}
	
	protected SeqStoreDao getSeqStoreDao() {
		return this.seqStoreDao;
	}

	protected TsmpSequenceDao getTsmpSequenceDao() {
		return this.tsmpSequenceDao;
	}
}