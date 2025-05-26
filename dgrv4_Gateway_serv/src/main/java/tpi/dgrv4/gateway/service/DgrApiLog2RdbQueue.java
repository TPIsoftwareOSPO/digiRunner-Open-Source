package tpi.dgrv4.gateway.service;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.entity.entity.jpql.TsmpReqLog;
import tpi.dgrv4.entity.entity.jpql.TsmpResLog;
import tpi.dgrv4.entity.repository.TsmpReqLogDao;
import tpi.dgrv4.entity.repository.TsmpResLogDao;
import tpi.dgrv4.gateway.keeper.TPILogger;

public abstract class DgrApiLog2RdbQueue  {
	
	private final static int bufferSize = 500; // 3分鐘壓測
	public static BlockingQueue<DgrApiLog2RdbQueue> rdb_LoggerQueue = new ArrayBlockingQueue<DgrApiLog2RdbQueue>(bufferSize);

	public abstract void run();
	
	public static void putByPoll(DgrApiLog2RdbQueue logObj) {
//		當 logPool 已滿( size 筆)時,丟棄最舊的 log (poll)
//		然後重試加入新的 log (offer)
//		以維持固定大小的 log 隊列
		while (rdb_LoggerQueue.offer(logObj) == false) {
			rdb_LoggerQueue.poll();
//			TPILogger.tl.warn("\n\t.....Remove Head  RDB_log .....");
		}
	}
	
	public static int abortNum = 0;
	public static void put(DgrApiLog2RdbQueue logObj) throws InterruptedException {
		
		// If there are already 150 unwritten logs, skip writing the remaining ones.
		if (rdb_LoggerQueue.size() < 150) {   
			putByPoll(logObj); // put 時若滿了就丟棄最舊的, 不會造成阻塞
		} else {
			// JVM memory
			long freeMemory = Runtime.getRuntime().freeMemory() / 1024 / 1024;
			long totalMemory = Runtime.getRuntime().totalMemory() / 1024 / 1024;
			long maxMemory = Runtime.getRuntime().maxMemory() / 1024 / 1024;
			//118MB / 2048MB / 2048MB...Memory(free/total/Max)
			// total 擴到最大, 且 free 大於 256 MB, 626MB
			if (freeMemory > 256) {
				putByPoll(logObj); // put 時若滿了就丟棄最舊的, 不會造成阻塞
			} else {
				abortNum++;
				if (abortNum % 100 == 0) { // 不需要每次都印出來
					TPILogger.tl.warn("\n\t.....Abort  RDB_log ....." + abortNum + ", freeMem::" + freeMemory);
					if (abortNum > Integer.MAX_VALUE - 100000) {
						abortNum = 10;
					}
				}
			}
		}
		DgrApiLog2RdbQueue.startThread(); //雖然每次都 start 但要確保它只會做一次
	}

	// 確保只能做一次
	private static boolean startFlag = false;
	public static void startThread() {
		if (startFlag == false) {
			startFlag = true; //表示已啟動, 只能做一次
			new Thread("RDB-Log") {
				public void run() {
					try {
						processLogOut();  // java.lang.OutOfMemoryError: Java heap space
					} catch (java.lang.OutOfMemoryError e) {
						// java.lang.OutOfMemoryError: Java heap space
						startFlag = false; //再啟動一次
						StringBuilder sb = new StringBuilder();
						sb.append(StackTraceUtil.logStackTrace(e));
						sb.append("\n\t.....ES_LoggerQueue.size() " + rdb_LoggerQueue.size() + " .....");
						sb.append("\n\t.....JVM.freeMemory() " + Runtime.getRuntime().freeMemory() / 1024 / 1024 + "MB" + " .....");
						sb.append("\n\t.....ES_LoggerQueue.clear().....");
						TPILogger.tl.error(sb.toString());
						rdb_LoggerQueue.clear();
						System.exit(1);
						// 包個 image 來試試, 
						// 使用 AI 這個方法 https://claude.ai/chat/12fa92ea-eb8a-4b6f-8430-32fcd67a39cd
					}
				}
			}.start();
			
			// 為了證明只有做一次
			TPILogger.tl.debug("[#queueTrace#] \nRDB log Queue has bean START...\n" );
		}
	}

	
	// 自定義一個 ThreadPoolExecutor
//	private static int corePoolSize = 1; // 核心 Thread 的數量，基本上 Thread 數量不會低於此數字
//	private static int maxPoolSize = 20;  // Thread Pool 的最大數量
//	private static long keepAliveTime = 10;  // 當閒置時間超過此設定的時間的話，系統會開始回收 corePoolSize 以上多餘的 Thread
//	private static BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(bufferSize);
//	private static RejectedExecutionHandler handler = new RejectedExecutionHandler() {
//		@Override
//		public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
//			// r.run(); // 以主Thread來執行它,所以會阻塞
//			TPILogger.tl.debug("RDB Q already full....size=" + workQueue.size());
//		}
//	};
//	private static ThreadPoolExecutor executor = new ThreadPoolExecutor(
//		    corePoolSize,
//		    maxPoolSize,
//		    keepAliveTime,
//		    TimeUnit.SECONDS,
//		    workQueue,
//		    handler
//		);
	public static List<TsmpReqLog> arrayListQ = new LinkedList<TsmpReqLog>();
	public static List<TsmpResLog> arrayListP = new LinkedList<TsmpResLog>();
	public static TsmpReqLogDao tsmpReqLogDaoObj;
	public static TsmpResLogDao tsmpResLogDaoObj;
	private static Object processLogOutLock = new Object();
	public static void processLogOut() {
		try {
			while (rdb_LoggerQueue.remainingCapacity() > -1) {
				DgrApiLog2RdbQueue o = rdb_LoggerQueue.take();
				o.run(); //add self Entity (Req/Res)
//				Thread.sleep(5000); // wait 後面的 log
//				synchronized (processLogOutLock) {
//					processLogOutLock.wait(5000);// wait 後面的 log 
//				}
				
				int nowSize = rdb_LoggerQueue.size();
				for (; nowSize > 0 ; nowSize--) {
					o = rdb_LoggerQueue.take();
					o.run(); //add 後面積纍的 Entity
				}
				
				if (arrayListQ.size() > 0) {
					tsmpReqLogDaoObj.saveAll(arrayListQ); //寫入一批 Req
					arrayListQ.clear();
				}
				
				if (arrayListP.size() > 0) {
					tsmpResLogDaoObj.saveAll(arrayListP); //寫入一批 Rep
					arrayListP.clear();
				}
			}
		} catch (InterruptedException e) {
			TPILogger.tl.error(StackTraceUtil.logStackTrace(e));
			Thread.currentThread().interrupt();
		}
	}
	
}
