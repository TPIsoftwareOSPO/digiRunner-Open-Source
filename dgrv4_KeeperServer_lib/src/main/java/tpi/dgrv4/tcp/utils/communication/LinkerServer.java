package tpi.dgrv4.tcp.utils.communication;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.util.StringUtils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.KryoException;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.KryoBufferUnderflowException;
import com.esotericsoftware.kryo.io.Output;

import tpi.dgrv4.tcp.utils.packets.sys.ExitThread;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

public class LinkerServer implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(LinkerServer.class);

	private int bufferSize = 999;
	private BlockingQueue<Packet_i> rev = new ArrayBlockingQueue<Packet_i>(bufferSize);
	private BlockingQueue<Packet_i> snd = new ArrayBlockingQueue<Packet_i>(bufferSize);
	private List<Notifier> notifiers;
	private Socket socket = null;

	private final String keeperHeaerByte = "_|__K_e_E_p_er__H_ea_d_er__|_(";
	private final String keeperFooterByte = ")_|__digi_9999__|_";

//	private Input in = null;

//	private Output out = null;

	public String userName = "";

	private Role f身份 = Role.valueOf("NoneLogin");

	// 可以用來放其它的參數, 方便讓 CommunicationServer 來管理使用
	public HashMap<String, String> param = new HashMap<String, String>();
	public HashMap<String, Object> paramObj = new HashMap<String, Object>();

	Object input;

	private boolean isExitThread = false;

	public LinkerServer(Socket s, List<Notifier> notifiers) {
		socket = s;
		this.notifiers = notifiers;
		try {
//	        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//            out = new Output(baos);
		} catch (Exception e) {
			logger.error("", e);
			//e.printStackTrace();
		}

		// send object packet
		new Thread() {
			public void run() {
				while (true) {
					if (isExitThread)
						break;
					sendObject();
				}
			}
		}.start();

		// 取 object packet
		new Thread() {
			public void run() {
				while (true) {
					if (isExitThread)
						break;
					processPacket();
				}
			}
		}.start();

		// Urgent 測試
//		new Thread(){
//			public void run(){
//				while(true){
//					if (isExitThread)
//						break;
//					try {
//						System.out.println("start.... LS 送出 Urgent ....  !\n");
//						Thread.sleep(2000);
//						
//						send(new Liveness()); //........送出 Hello
//						
//					} catch (Exception e) {
//						//System.out.println(".... LS 送出 Urgent .... 失敗 !\n");
//						e.printStackTrace();
//						break;
//					}
//				}
//			}
//		}.start();
	}

	private byte[] socket_read_buffer = new byte[1024 * 1024 * 10]; // 10MB
	private Input kryoInput = new Input(socket_read_buffer.length);
	private ByteArrayOutputStream baos_4_readObj = new ByteArrayOutputStream();

	public void run() {
		try {
			Kryo kryo = CommunicationServer.kryoLocal.get();
			while (true) {
				
				int bytesRead = socket.getInputStream().read(socket_read_buffer);

				if (bytesRead == -1) {
					break; // socket close
				}
				// 只處理實際讀到的資料
			    byte[] actualData = Arrays.copyOf(socket_read_buffer, bytesRead);

				// copy to ByteArrayOS
				baos_4_readObj.write(actualData, 0, actualData.length);
				baos_4_readObj.flush();

				// Determine the dgR Detection behavior is correct?
				if (bytesRead == "dgR v4 init detect end!".length()) {
					boolean endFlag = "dgR v4 init detect end!".equals(baos_4_readObj.toString());
					baos_4_readObj.reset();
					if (endFlag) {
						socket.getOutputStream().write("Yes, I am dgRunner".getBytes());
						socket.getOutputStream().flush();
						socket.close();
						break;
					}
				} else {
					// Kryo object
					boolean endFlag = procKyroPacket(kryo);

					// false 表示收到 exit 封包, true表示已處理或續傳
					if (endFlag == true) {
						break; // 收到 'ExitThread' 就要離開
					}
				}
			}
		} catch (Exception e) {
			logger.error("Server 主動斷線了");
			logger.error(logTpiShortStackTrace(e));
		} catch (Throwable t) {
			logger.error("Server 主動斷線了");
			logger.error(logTpiShortStackTrace(t));
		} finally {
		}
		doExitThread();
		CommunicationServer.cs.doDisconnectProc(this);
	}

	// 續傳使用
	private Deque<ByteArrayOutputStream> bigContinueDeque = null;

	private boolean procKyroPacket(Kryo kryo) throws Exception {
		byte arr[] = baos_4_readObj.toByteArray();
		baos_4_readObj.reset();
//		logger.info("暫時印出來看(Server)...1\n..." + new String(arr, "UTF-8") + "... 暫時印出來看(Server)...1\n");

		boolean hasDoneToPack = hasDone(arr); // 是否收到'尾'資料了

		if (hasDoneToPack == false) {
			return false; // 未到'尾', 只收集, 不做反序列化
		}
		try {
			for (ByteArrayOutputStream bigContinue : bigContinueDeque) {
				arr = bigContinue.toByteArray();
//				logger.info("暫時印出來看(Server)...2\n..." + new String(arr, "UTF-8") + "... 暫時印出來看(Server)...2\n");
	
				ByteArrayInputStream bais = new ByteArrayInputStream(arr);
				kryoInput.setInputStream(bais);
				Object inputObj = kryo.readClassAndObject(kryoInput);
				kryoInput.close();
				kryoInput.reset();
				if (inputObj instanceof Packet_i) {
					Packet_i obj = (Packet_i) inputObj;
					if (obj instanceof ExitThread) {
						this.isExitThread = true;
						return true;
					}
					rev.put(obj);
	//System.err.print(">.");
				}
			}
		} catch (KryoBufferUnderflowException e) {

			// 若是 Buffer Underflow 則啟動續傳
			continueFile(arr);

			StringBuffer sb = new StringBuffer();
			sb.append("\n無法解析 LS 封包 (Can't parser Kryo packet )..."); // + LinkerServer.logTpiShortStackTrace(e)
			sb.append("\n 略過不處理, 進行續傳 (ignore packet and continue)");
			sb.append("\n LinkerServer name: " + userName);
			sb.append("\nbyte[] length: " + arr.length);
			sb.append("\n");
			logger.warn(sb.toString()); // 輸出基本資料
			
			sb = new StringBuffer();
			sb.append("\nbyte[] length: " + arr.length);
			sb.append("\narr[] as String: \n" + new String(arr, "UTF-8") + "\n");
			logger.warn(sb.toString()); // 以字串輸出封包內容			
		} catch (KryoException e) {
			baos_4_readObj.reset(); // 如果出錯了就要 reset 以免資料一直留存
			StringBuffer sb = new StringBuffer();
			sb.append("\n無法解析 LS 封包:" + logTpiShortStackTrace(e));
			sb.append("byte[] length: " + arr.length);
			sb.append("\nbyte[] to String: " + getSubstring(arr));
//			sb.append("\nbyte[] b64 string: " + Base64Utils.encodeToUrlSafeString(arr));
			sb.append("\n");
			logger.error(sb.toString());
		}
		baos_4_readObj.reset();
		bigContinueDeque.clear(); // 因為累加的資料能跑到這裡表示已經用完了, 所以清空
		return false;
	}

//	public static void main(String arg[]) {
//		String keeperHeaerByte = "__K_e_E_p_er__H_ea_d_er__";
//		String keeperFooterByte = "__digi_9999__";
//		byte keperHeader[] = keeperHeaerByte.getBytes();
//		ByteArrayOutputStream baos_sendObj = new ByteArrayOutputStream();
//		try {
////			baos_sendObj.write(keeperHeaerByte.getBytes());
//			baos_sendObj.write("...DATA...!".getBytes());
//			baos_sendObj.write(keeperFooterByte.getBytes());
//			byte arr[] = baos_sendObj.toByteArray();
//			
//			// remove header/footer
//			ByteArrayOutputStream bigContinue = new ByteArrayOutputStream();
//			
//			// save data byte
////			bigContinue.write(arr, 
////					keperHeader.length, 
////					arr.length - keeperFooterByte.getBytes().length - keperHeader.length);
//			// save data byte
////			bigContinue.write(arr, keperHeader.length, arr.length - keperHeader.length); 
//			// save data byte
//			bigContinue.write(arr, 0, arr.length - keeperFooterByte.getBytes().length);
//			
//			String teststr = new String(bigContinue.toByteArray());
//			System.err.println("$"+teststr+"$");
//			
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	private Pair<Boolean, byte[]> handleSuffix(byte[] packet, byte[] suffix) {
	    // Get the length of the suffix
	    int suffixLength = suffix.length;

	    // Check if the packet is long enough to possibly contain the suffix
	    if (packet.length >= suffixLength) {
	        // Extract the end of the packet that could be the suffix
	        byte[] endOfPacket = Arrays.copyOfRange(packet, packet.length - suffixLength, packet.length);
	        
	        // Check if the end of the packet is equal to the suffix
	        if (Arrays.equals(endOfPacket, suffix)) {
	            // If yes, return a pair with true and the packet without the suffix
	            return Pair.of(true, getSubArray(packet, 0, packet.length - suffixLength));
	        }
	    }

	    // If the packet is not long enough or the suffix is not at the end, return a pair with false and the original packet
	    return Pair.of(false, packet);
	}


	private Pair<Boolean, byte[]> handlePrefix(byte[] packet, byte[] prefix) {
	    // Get the length of the prefix
	    int prefixLength = prefix.length;

	    // Check if the packet is long enough to possibly contain the prefix
	    if (packet.length >= prefixLength) {
	        // Extract the beginning of the packet that could be the prefix
	        byte[] startOfPacket = Arrays.copyOfRange(packet, 0, prefixLength);
	        
	        // Check if the beginning of the packet is equal to the prefix
	        if (Arrays.equals(startOfPacket, prefix)) {
	            // If yes, return a pair with true and the packet without the prefix
	            return Pair.of(true, getSubArray(packet, prefixLength, packet.length));
	        }
	    }

	    // If the packet is not long enough or the prefix is not at the beginning, return a pair with false and the original packet
	    return Pair.of(false, packet);
	}


	private void handleSuffixPrefix(byte[] input, byte[] sequence) throws IOException {
	    
	    // Split the input byte array into a list of byte arrays using the sequence byte array as a separator
	    List<byte[]> list = splitByteArray(input, sequence);

	    // Loop through each byte array in the list
	    for (int i = 0; i < list.size(); i++) {
	        // If it's the first byte array in the list
	        if (i == 0) {
	            // Add it to the "BigContinue" (exact functionality depends on your implementation of addBigContinue)
	            addBigContinue(list.get(i));
	        } else {
	            // If it's not the first byte array, add it to a new "BigContinue" 
	            // (exact functionality depends on your implementation of addNewBigContinue)
	            addNewBigContinue(list.get(i));
	        }
	    }
	}

    
    // This method splits a byte array into a list of byte arrays using a separator byte array
    private List<byte[]> splitByteArray(byte[] source, byte[] separator) {
        List<byte[]> result = new ArrayList<>();
        int[] table = buildTable(separator);
        int start = 0;
        int match = 0;

        // Loop through each byte in the source array
        for (int i = 0; i < source.length; i++) {
            // While the match index is greater than 0 and the current byte is not equal to the byte at the match index in the separator
            while (match > 0 && source[i] != separator[match]) {
                // Move the match index to the value in the table at the match index
                match = table[match];
            }
            // If the current byte is equal to the byte at the match index in the separator
            if (source[i] == separator[match]) {
                // Increment the match index
                match++;
                // If the match index is equal to the length of the separator
                if (match == separator.length) {
                    // Copy the bytes from start to current index minus separator length plus 1 into a new byte array
                    byte[] segment = Arrays.copyOfRange(source, start, i - separator.length + 1);
                    // If this segment is not equal to separator
                    if (!Arrays.equals(segment, separator)) {
                        // Add the segment to the result list
                        result.add(segment);
                    }
                    // Move the start index to the current index plus 1 and reset match index to 0
                    start = i + 1;
                    match = 0;
                }
            }
        }

        // After looping through all bytes, copy the remaining bytes from start to end of source into a new byte array
        byte[] lastSegment = Arrays.copyOfRange(source, start, source.length);
        // If this last segment is not equal to separator
        if (!Arrays.equals(lastSegment, separator)) {
            // Add it to the result list
            result.add(lastSegment);
        }

        // Return the result list
        return result;
    }

    // This method builds a table for KMP algorithm, which is used to find a byte sequence in a byte array
    private int[] buildTable(byte[] separator) {
        int[] table = new int[separator.length + 1];
        for (int i = 2; i < table.length; i++) {
            int j = table[i - 1];
            while (j > 0 && separator[j] != separator[i - 1]) {
                j = table[j];
            }
            if (separator[j] == separator[i - 1]) {
                j++;
            }
            table[i] = j;
        }
        return table;
    }
    
    // This method adds a byte array to an existing ByteArrayOutputStream in your Deque collection or creates a new one if none exists
    private void addBigContinue(byte[] arr) throws IOException {

        if (bigContinueDeque == null) {
            bigContinueDeque = new ArrayDeque<>();
        }

        ByteArrayOutputStream bigContinue = bigContinueDeque.pollLast();
        if (bigContinue == null) {
            bigContinue = new ByteArrayOutputStream(bufferSize);
        }

        bigContinue.write(arr);
        bigContinueDeque.addLast(bigContinue);

    }
    
    // This method creates a new ByteArrayOutputStream, adds a byte array to it, and adds it to your Deque collection
    private void addNewBigContinue(byte[] arr) throws IOException {

        if (bigContinueDeque == null) {
            bigContinueDeque = new ArrayDeque<>();
        }

        ByteArrayOutputStream bigContinue = new ByteArrayOutputStream(bufferSize);
        bigContinue.write(arr);
        bigContinueDeque.addLast(bigContinue);

    }

    // This method returns a subarray of a byte array from the specified start index to the end index
    private byte[] getSubArray(byte[] array, int start, int end) {
        return Arrays.copyOfRange(array, start, end);
    }

    // This method checks if a byte array has a specific header and footer, and handles them accordingly
    private boolean hasDone(byte[] arr) throws IOException {

        // Convert the header and footer strings to byte arrays
        byte[] header = keeperHeaerByte.getBytes();
        byte[] footer = keeperFooterByte.getBytes();
        String footerHeader = keeperFooterByte + keeperHeaerByte;

        boolean hasSuffix = false;
        Pair<Boolean, byte[]> handleResult = null;

        // Check if the array starts with the header, and remove it if it does
        handleResult = handlePrefix(arr, header);
        // Get the array after removing the header
        arr = handleResult.getSecond();

        // Check if the array ends with the footer, and remove it if it does
        handleResult = handleSuffix(arr, footer);
        // If the array had a footer, set hasSuffix to true
        hasSuffix = handleResult.getFirst();
        // Get the array after removing the footer
        arr = handleResult.getSecond();

        // Handle any instances of the footer immediately followed by the header in the array
        handleSuffixPrefix(arr, footerHeader.getBytes());

        // Return whether the original array had a footer
        return hasSuffix;
    }


//	private boolean hasDone(byte[] arr) throws IOException {
//		// 檢查 keeper header
//		boolean hasKH = hasKeeperHeader(arr);
//		boolean hasFoot = hasDigiFooter(arr);
//		byte keperHeader[] = keeperHeaerByte.getBytes();
//		if (hasFoot && hasKH) {
//			// 單獨完整一包
//			bigContinue = new ByteArrayOutputStream(bufferSize);
//			// ** 去頭,去尾
//			bigContinue.write(arr, keperHeader.length,
//					arr.length - keeperFooterByte.getBytes().length - keperHeader.length);
//			return true;
//		} else if (hasKH) {
//			// start 啟用續傳
//			bigContinue = new ByteArrayOutputStream(bufferSize);
//			int len = arr.length - keperHeader.length;
//			// ** 去頭
//			bigContinue.write(arr, keperHeader.length, len);
//			return false;
//		} else if (hasFoot) {
//			// end
//			// ** 去尾
//			bigContinue.write(arr, 0, arr.length - keeperFooterByte.getBytes().length);
//			return true;
//		} else {
//			// body
//			if (bigContinue == null) {
//				bigContinue = new ByteArrayOutputStream(bufferSize);
//			}
//			bigContinue.write(arr);
//			return false;
//		}
//
//		// 回傳累加資料
//		// return bigContinue.toByteArray();
//	}

//	private boolean hasDigiFooter(byte[] arr) {
//		byte[] foo = keeperFooterByte.getBytes();
//		boolean hasFoo = true;
//		// data 一定要大於 footer
//		if (arr.length >= keeperFooterByte.getBytes().length) {
//			// 只比對後方的 foo 之 byte 內容
//			for (int i = arr.length - 1; i >= (arr.length - foo.length); i--) {
//				hasFoo &= arr[i] == foo[i - ((arr.length - foo.length))];
//			}
//			return hasFoo;
//		}
//
//		return false;
//	}
//
//	private boolean hasKeeperHeader(byte[] arr) {
//		byte keperHeader[] = keeperHeaerByte.getBytes();
//		boolean hasHeader = true;
//		// 長度至少要相等
//		if (arr.length >= keperHeader.length) {
//			for (int i = 0; i < keperHeader.length; i++) {
//				// 要全部相等才行
//				hasHeader &= keperHeader[i] == arr[i];
//			}
//			return hasHeader;
//		}
//		return false;
//	}

	private void continueFile(byte[] arr) throws IOException {
		baos_4_readObj.reset(); // 如果出錯了就要 reset 以免資料一直留存
		addNewBigContinue(arr);
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

	public void close() {
		try {
//			in.close();
//			out.close();
			socket.close();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	private void doExitThread() {
		ExitThread ex = new ExitThread();
		try {
			rev.put(ex);
			snd.put(ex);
			socket.close();
		} catch (Exception e) {
			logger.error("", e);
		}
	}

	public void send(Packet_i o) {
		try {
			snd.put(o);
		} catch (Exception e) {
			logger.error("", e);
			doExitThread();
			CommunicationServer.cs.doDisconnectProc(this);
		}
	}

	ByteArrayOutputStream baos_sendObj = new ByteArrayOutputStream();
//	Output kryoOutput = new Output(baos_sendObj);
	Output kryoOutput = new Output(baos_sendObj, socket_read_buffer.length);

	private void sendObject() {
		try {
			Kryo kryo = CommunicationServer.kryoLocal.get();
			Packet_i obj = snd.take();
			if (obj instanceof ExitThread) {
				this.isExitThread = true;
				return;
			}

			// header added
			baos_sendObj.write(keeperHeaerByte.getBytes());

			// BODY
			kryo.writeClassAndObject(kryoOutput, obj);
			kryoOutput.flush();
			kryoOutput.close();

			// footer added
			baos_sendObj.write(keeperFooterByte.getBytes());

			byte[] data = baos_sendObj.toByteArray(); // 網上說這是錯誤用法, 這是 kryo 與Java原生框架的區別
//	        byte[] data = kryoOutput.toBytes();

//	        System.out.println("@@@ LS send to C "+data.length+ " , class = " + obj.getClass());
			socket.getOutputStream().write(data);
			socket.getOutputStream().flush();
			kryoOutput.reset();
			baos_sendObj.reset();
		} catch (Exception e) {
			this.isExitThread = true;
			logger.error("", e);
			close();
			CommunicationServer.cs.doDisconnectProc(this);
		}
	}

	private void processPacket() {
		try {
			Packet_i o = rev.take();
			if (o instanceof ExitThread) {
				this.isExitThread = true;
				return;
			}
			o.runOnServer(this);

			// 不要每次都印出來, 偶數秒才印, 觀察使用量
//			if (System.currentTimeMillis() / 1000 % 5 == 0) {
//				System.out.println("Linker Server REV Queue size:" + rev.size());
//			}
		} catch (Exception e) {
			this.isExitThread = true;
			logger.error("Server 主動斷線了");
			logger.error(logTpiShortStackTrace(e));
			close();
			CommunicationServer.cs.doDisconnectProc(this);
		}
	}

	public static String logTpiShortStackTrace(Throwable e) {
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		e.printStackTrace(new java.io.PrintWriter(buf, true));
		String msg = buf.toString();
		try {
			buf.close();
		} catch (Exception t) {
			return e.getMessage();
		}
		return getShortErrMsg(msg + "---END\n");
	}

	/**
	 * 業務邏輯錯誤應該不要印出所有 stack trace 把定義明確的 exception, logger Level 改為 INFO
	 * 
	 * @param errStr
	 * @return
	 */
	private static String getShortErrMsg(String errStr) {
		String errMsg = "";
		String keyword = "tpi.dgrv4";
		if (StringUtils.hasText(errStr)) {
			String[] errStrArr = errStr.split("\n");
			if (errStrArr != null && errStrArr.length > 0) {
				// errMsg = errStrArr[0];
				for (String str : errStrArr) {
					// 只印出有包含以下文字的錯誤訊息
					if (str.contains(keyword) || str.contains("Exception") || str.contains("Throwable")) {
						errMsg += "\n" + str;
					}
				}
			}
		}
		return errMsg;
	}

	public String getRemoteIP() {
		return this.socket.getInetAddress().getHostAddress();
	}

	public int getRemotePort() {
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

	public void setIdentity(Role f身份) {
		this.f身份 = f身份;
		if (notifiers != null) {
			for (Notifier notifier : notifiers) {
				notifier.setRole(f身份, this);
			}
		}
	}

	public Role getIdentity() {
		return this.f身份;
	}

	public Socket getSocket() {
		return socket;
	}
}// ---------------inner class Linker end
