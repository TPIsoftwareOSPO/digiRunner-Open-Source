package tpi.dgrv4.tcp.utils.communication;



import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.KryoBufferUnderflowException;
import com.esotericsoftware.kryo.io.Output;

import tpi.dgrv4.tcp.utils.packets.DoSetUserName;
import tpi.dgrv4.tcp.utils.packets.sys.ExitThread;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class LinkerClient implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(LinkerClient.class);
	
	private int bufferSize = 999;
	private BlockingQueue<Packet_i> rev = new ArrayBlockingQueue<Packet_i>(
			bufferSize);
	private BlockingQueue<Packet_i> snd = new ArrayBlockingQueue<Packet_i>(
			bufferSize);
	public HashMap<String, String> param = new HashMap<String, String>();
	public HashMap<String, Object> paramObj = new HashMap<String, Object>();
	
	public List<ClinetNotifier> cnf;

	private final String keeperHeaerByte = "_|__K_e_E_p_er__H_ea_d_er__|_(";
	private final String keeperFooterByte = ")_|__digi_9999__|_";
	
	Socket socket;
//	Input in;
//	java.io.OutputStream out;
	public String serverIP;
	public int port;
	// 使用者身分
	public Role identity;
	public String userName;
	
	// private boolean isExit;
	private boolean isExitThread = false;

	public void addListner(ClinetNotifier c) {
		if (cnf == null) {
			cnf = new ArrayList<ClinetNotifier>();
		}
		if (c != null) {
			cnf.add(c);
		}
	}

	public LinkerClient(String ip, int port, Role identity)
			throws UnknownHostException, IOException {
		this(ip, port, identity, null);
	}

	public LinkerClient(String ip, int port, Role identity, ClinetNotifier c)
			throws UnknownHostException, IOException {
		this.serverIP = ip;
		this.port = port;
		this.identity = identity;
		// this.isExit = isExit;
		socket = new Socket(serverIP, port);
		
//        out = new OutputStream(socket.getOutputStream());
//		in = new Input(socket.getInputStream());
		addListner(c);
		for (ClinetNotifier cn : cnf) {
			cn.runConnection(this);
		}

//		TcpIdentity p = new TcpIdentity();
//		p.f_identity = this.identity;
//		this.send(p);

		Thread t = new Thread(this);
		t.start();

		new Thread() {
			public void run() {
				while (true) {
					if (isExitThread)
						break;
					sendObject();
				}
			}
		}.start();

		new Thread() {
			public void run() {
				while (true) {
					if (isExitThread)
						break;
					processPacket();
				}
			}
		}.start();
	}

	private byte[] socket_read_buffer = new byte[1024*1024*10]; // 10MB
	private Input kryoInput = new Input(socket_read_buffer.length);
	private ByteArrayOutputStream baos_4_readObj = new ByteArrayOutputStream();
	public void run() {
		try {
			Kryo kryo = CommunicationServer.kryoLocal.get();
	        while (true) {
	            int bytesRead = socket.getInputStream().read(socket_read_buffer);
	            if(bytesRead == -1) {
	            	break; //socket close
	            }
	            
	            // 只處理實際讀到的資料
	            byte[] actualData = Arrays.copyOf(socket_read_buffer, bytesRead);
	            
	            // copy to ByteArrayOS
	            baos_4_readObj.write(actualData, 0, actualData.length);
	            baos_4_readObj.flush();

	            // Kryo object
				boolean endFlag = procKyroPacket(kryo);
				
				// false 表示收到 exit 封包, true表示已處理或續傳
				if (endFlag == true) {
					break; // 收到 'ExitThread' 就要離開
				}
	        }
		} catch (Exception e) {
			logger.error(LinkerServer.logTpiShortStackTrace(e));
		} finally {
			isExitThread = true;
			doExitThread();
		}
	}
	
	// 續傳使用
	private ByteArrayOutputStream bigContinue = null;
	private boolean procKyroPacket(Kryo kryo) throws Exception {
		byte arr[] = baos_4_readObj.toByteArray();
//		logger.info("\n...開始 deserialize 封包...size:"+arr.length);
//		logger.info("\n...封包值:"+new String(arr));
		
		boolean hasDoneToPack = hasDone(arr); // 是否收到'尾'資料了
		
		if(hasDoneToPack == false) {
			return false; //未到'尾', 只收集, 不做反序列化
		} 
		
		arr = bigContinue.toByteArray();
		
		ByteArrayInputStream bais = new ByteArrayInputStream(arr);
		kryoInput.setInputStream(bais);
		try {
			Object inputObj = kryo.readClassAndObject(kryoInput);
			kryoInput.close();
			kryoInput.reset();
			baos_4_readObj.reset();
			if (inputObj instanceof Packet_i) {
				Packet_i obj = (Packet_i) inputObj;
				if (obj instanceof ExitThread) {
					this.isExitThread = true;
					return true;
				}
				rev.put(obj);
//System.err.print(">.");
				bigContinue = null; //因為累加的資料能跑到這裡表示已經用完了, 所以清空
			}
		} catch (KryoBufferUnderflowException e) {

			// 若是 Buffer Underflow 則啟動續傳
			continueFile(arr);
			
			StringBuffer sb = new StringBuffer();
			sb.append("\n無法解析 LC 封包 (Kryo Buffer Underflow)..."); // + LinkerServer.logTpiShortStackTrace(e)
			sb.append("byte[] length: " + arr.length);
//			sb.append("\nbyte[] to String: " + getSubstring(arr));
			sb.append("\n 略過不處理, 進行續傳" );
			sb.append("\n");
			logger.debug(sb.toString());
		} catch (KryoException e) {
			baos_4_readObj.reset(); //如果出錯了就要 reset 以免資料一直留存
			StringBuffer sb = new StringBuffer();
			sb.append("\n無法解析 LC 封包:" + LinkerServer.logTpiShortStackTrace(e));
			sb.append("byte[] length: " + arr.length);
			sb.append("\nbyte[] to String: " + getSubstring(arr));
//			sb.append("\nbyte[] b64 string: " + Base64Utils.encodeToUrlSafeString(arr));
			sb.append("\n");
			logger.error(sb.toString());
		}
		return false;
	}
	
	private boolean hasDone(byte[] arr) throws IOException {
		// 檢查 keeper header
		boolean hasKH = hasKeeperHeader(arr);
		boolean hasFoot = hasDigiFooter(arr);
		byte keperHeader[] = keeperHeaerByte.getBytes();
		if (hasFoot && hasKH ) {
			// 單獨完整一包
			bigContinue = new ByteArrayOutputStream(bufferSize);
			// ** 去頭,去尾
			bigContinue.write(arr, keperHeader.length, arr.length - keeperFooterByte.getBytes().length - keperHeader.length);
			return true;
		} else if (hasKH) {
			// start 啟用續傳
			bigContinue = new ByteArrayOutputStream(bufferSize);
			int len = arr.length - keperHeader.length;
			// ** 去頭
			bigContinue.write(arr, keperHeader.length, len);
			return false;
		} else if (hasFoot) {
			// end 
			// ** 去尾
			bigContinue.write(arr, 0, arr.length - keeperFooterByte.getBytes().length);
			return true;
		} else {
			// body
			if (bigContinue == null) {
				bigContinue = new ByteArrayOutputStream(bufferSize);
			}
			bigContinue.write(arr);
			return false;
		}
		
		// 回傳累加資料
		// return bigContinue.toByteArray();
	}
	
	private boolean hasDigiFooter(byte[] arr) {
		byte[] foo = keeperFooterByte.getBytes();
		boolean hasFoo = true;
		// data 一定要大於 footer 
		if (arr.length >= keeperFooterByte.getBytes().length ) {
			// 只比對後方的 foo 之 byte 內容
			for (int i = arr.length-1; i >= (arr.length - foo.length); i--) {
				hasFoo &= arr[i] == foo[i - ((arr.length - foo.length))];
			}
			return hasFoo;
		}
		
		return false;
	}
	
	private boolean hasKeeperHeader(byte[] arr) {
		byte keperHeader[] = keeperHeaerByte.getBytes();
		boolean hasHeader = true;
		//長度至少要相等
		if (arr.length >= keperHeader.length) {
			for (int i = 0; i < keperHeader.length; i++) {
				// 要全部相等才行
				hasHeader &= keperHeader[i] == arr[i];
			}
			return hasHeader;
		}
		return false;
	}

	private void continueFile(byte[] arr) throws IOException {
		baos_4_readObj.reset(); //如果出錯了就要 reset 以免資料一直留存
		if (bigContinue == null) {
			bigContinue = new ByteArrayOutputStream(bufferSize);
		}
		bigContinue.write(arr);
	}

	private String getSubstring(byte[] arr) {
		String tmpStr = null;
		if (arr.length <= 200) {
			tmpStr = new String(arr);
		} else {
			tmpStr = new String(arr, 0, 200);
		}
		return tmpStr;
	}

	private void doExitThread() {
		ExitThread ex = new ExitThread();
		try {
			rev.put(ex);
			snd.put(ex);
			socket.close();
			for (ClinetNotifier cn : cnf) {
				cn.runDisconnect(this);
			}
		} catch (Exception e) {
			//e.printStackTrace();
			logger.error(LinkerServer.logTpiShortStackTrace(e));
		}
	}

	/**
	 * 此方法不需要去修改它
	 */
	public void send(Packet_i obj) {
		try {
			snd.put(obj);
		} catch (InterruptedException e) {
			logger.error("", e);
		}
	}

	ByteArrayOutputStream baos_sendObj = new ByteArrayOutputStream();
	Output kryoOutput = new Output(baos_sendObj, socket_read_buffer.length); 

	private void sendObject() {
		try {
			Kryo kryo = CommunicationServer.kryoLocal.get();
	        Packet_i obj = snd.take();
	        if (obj instanceof ExitThread) {
	            this.isExitThread = true;
	            return;
	        }
	        
//	        baos_sendObj = new ByteArrayOutputStream(); // 每次都 new 避免前後封包混肴
//	        kryoOutput = new Output(baos_sendObj, socket_read_buffer.length); // 每次都 new 避免前後封包混肴
	        
	        // header added
	        baos_sendObj.write(keeperHeaerByte.getBytes());
	        
	        // BODY
	        kryo.writeClassAndObject(kryoOutput, obj);
	        kryoOutput.flush();
	        kryoOutput.close();
	        
	        // footer added
	        baos_sendObj.write(keeperFooterByte.getBytes());
	        
	        byte[] data = baos_sendObj.toByteArray(); //網上說這是錯誤用法, 這是 kryo 與Java原生框架的區別
	        
//	        logger.info("暫時印出來看(Client)\n..." + new String(data, "UTF-8") + "... 暫時印出來看(Client)\n");

	        socket.getOutputStream().write(data);
	        socket.getOutputStream().flush();
	        logger.trace("\n...socket狀態: " + socket.isClosed() + "," + socket.isConnected() + "," + socket.isOutputShutdown());	        
	        kryoOutput.reset();
	        baos_sendObj.reset();
	        kryo.reset();
	        
//	        Thread.sleep(0,2000);
	        
		} catch (Exception e) {
			//e.printStackTrace();
			logger.error(LinkerServer.logTpiShortStackTrace(e));
		}
	}
	
	public static String logStackTrace(Throwable e) {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		e.printStackTrace(new java.io.PrintWriter(buf, true));
		String msg = buf.toString();
		try {
			buf.close();
		} catch (Exception t) {
			return e.getMessage();
		}
		return msg;
	}

	private void processPacket() {
		try {
			Packet_i o = rev.take();
			if (o instanceof ExitThread) {
				this.isExitThread = true;
				return;
			}
			o.runOnClient(this);
			
			// 不要每次都印出來, 偶數秒才印, 觀察使用量
//			if (System.currentTimeMillis() / 1000 % 5 == 0) {
//				System.out.println("Linker CLIENT REV Queue size:" + rev.size());
//			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public void close() {
		try {
//			in.close();
//			out.close();
			socket.close();
		} catch (Exception e) {
			//e.printStackTrace();
			logger.error(LinkerServer.logTpiShortStackTrace(e));
		}
	}

	public String getServerIP() {
		return this.socket.getInetAddress().getHostAddress();
	}

	public String getLocalIP() {
		return this.socket.getLocalAddress().getHostAddress();
	}

	public int getServerPort() {
		return this.socket.getPort();
	}

	public int getLocalPort() {
		return this.socket.getLocalPort();
	}

	/**
	 * 是否還有連線
	 * 
	 * @return
	 */
	public boolean isConnected() {
		return !socket.isClosed();
	}
	
	public void removeMonitor(ClinetNotifier o){
		cnf.remove(o);
	}
	
	public void setUserName(String userName){
		this.userName = userName;
		DoSetUserName john = new DoSetUserName(userName);
		this.send(john);
	}
	
	public String getLocalIpAdress() {
		return socket.getLocalAddress().getHostAddress();
	}
	
	public String getLocalIpFQDN() {
		return socket.getLocalAddress().getCanonicalHostName();
	}
}
