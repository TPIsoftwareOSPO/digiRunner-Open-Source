package tpi.dgrv4.tcp.utils.communication;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.KryoBufferUnderflowException;
import com.esotericsoftware.kryo.io.Output;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tpi.dgrv4.tcp.utils.packets.DoSetUserName;
import tpi.dgrv4.tcp.utils.packets.sys.ExitThread;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;

public class LinkerClient implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(LinkerClient.class);
    public int bufferSize = 999;
    private BlockingQueue<Packet_i> rev;
    @Getter
    private BlockingQueue<Packet_i> snd;
    public HashMap<String, String> param = new HashMap<>();
    public HashMap<String, Object> paramObj = new HashMap<>();

    public List<ClinetNotifier> cnf;

    private final String keeperHeaerByte = "_|__K_e_E_p_er__H_ea_d_er__|_(";
    private final String keeperFooterByte = ")_|__digi_9999__|_";

    Socket socket;
    public String serverIP;
    public int port;
    // 使用者身分
    public Role identity;
    public String userName;



    private boolean isExitThread = false;

    public void addListner(ClinetNotifier c) {
        if (cnf == null) {
            cnf = new ArrayList<>();
        }
        if (c != null) {
            cnf.add(c);
        }
    }

    public LinkerClient(String ip, int port, Role identity, int bufferSize)
            throws  IOException {
        this(ip, port, identity, null, bufferSize);
    }

    public LinkerClient(String ip, int port, Role identity, ClinetNotifier c, int bufferSize)
            throws  IOException {
        this.serverIP = ip;
        this.port = port;
        this.identity = identity;
        this.bufferSize = bufferSize;
        this.rev = new java.util.concurrent.ArrayBlockingQueue<>(this.bufferSize);
        this.snd = new java.util.concurrent.ArrayBlockingQueue<>(this.bufferSize);

        socket = new Socket(serverIP, port);
        addListner(c);
        if (cnf != null) {
            for (ClinetNotifier cn : cnf) {
                cn.runConnection(this);
            }
        }

        Thread t = new Thread(this);
        t.start();

        new Thread() {
            public void run() {
                while (true) {
                    if (isExitThread)
                        break;
                    sendObject();
                    // 【Bug 4 修正】
                    // sendObjectClaude() 收到 ExitThread 時，設定 isExitThread=true 後 return。
                    // 若只在 while 頂部檢查，執行緒會再次進入 sendObject() → snd.take() 阻塞，
                    // 永遠無法退出迴圈。在 sendObject() 返回後立即二次檢查，確保能正常結束。
                    if (isExitThread)
                        break;
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

    private final byte[] socketReadBuffer = new byte[1024 * 1024 * 10]; // 10MB
    private Input kryoInput = new Input(socketReadBuffer.length);
    private ByteArrayOutputStream baos4ReadObj = new ByteArrayOutputStream();

    public void run() {
        try {
            Kryo kryo = CommunicationServer.kryoLocal.get();
            while (true) {
                int bytesRead = socket.getInputStream().read(socketReadBuffer);
                if (bytesRead == -1) {
                    break; //socket close
                }

                byte[] actualData = Arrays.copyOf(socketReadBuffer, bytesRead);

                baos4ReadObj.write(actualData, 0, actualData.length);
                baos4ReadObj.flush();

                boolean endFlag = procKyroPacket(kryo);

                if (endFlag) {
                    break;
                }
            }
        } catch (Exception e) {
            logger.error(LinkerServer.logTpiShortStackTrace(e));
        } finally {
            isExitThread = true;
            doExitThread();
        }
    }

    private Deque<ByteArrayOutputStream> bigContinueDeque = null;

    private boolean procKyroPacket(Kryo kryo) {
        byte[] arr = baos4ReadObj.toByteArray();

        if (arr.length == 0) {
            return false;
        }

        byte[] headerBytes = keeperHeaerByte.getBytes();
        byte[] footerBytes = keeperFooterByte.getBytes();
        int headerCount = countOccurrencesInBytes(arr, headerBytes);
        int footerCount = countOccurrencesInBytes(arr, footerBytes);

        if (headerCount == 0 || footerCount == 0) {
            return false;
        }

        int processedPackets = 0;
        int remainingStart = 0;

        while (remainingStart < arr.length) {
            int headerPos = indexOf(arr, headerBytes, remainingStart);
            if (headerPos == -1) break;

            int footerPos = indexOf(arr, footerBytes, headerPos);
            if (footerPos == -1) break;

            int packetStart = headerPos;
            int packetEnd = footerPos + footerBytes.length;
            byte[] singlePacket = Arrays.copyOfRange(arr, packetStart, packetEnd);

            try {
                processSinglePacket(singlePacket, kryo);
                processedPackets++;
            } catch (Exception e) {
                logger.error("[CLIENT-DESERIALIZE] ✗ 處理第 {} 個封包失敗", processedPackets + 1, e);
            }

            remainingStart = packetEnd;
        }

        if (remainingStart < arr.length) {
            byte[] remaining = Arrays.copyOfRange(arr, remainingStart, arr.length);
            baos4ReadObj.reset();
            try {
                baos4ReadObj.write(remaining);
            } catch (IOException e) {
                logger.error("[CLIENT-PROC] 寫入剩餘位元組時出錯", e);
            }
        } else {
            baos4ReadObj.reset();
        }

        return false;
    }

    private void processSinglePacket(byte[] singlePacket, Kryo kryo) {
        byte[] headerBytes = keeperHeaerByte.getBytes();
        byte[] footerBytes = keeperFooterByte.getBytes();

        byte[] payload = Arrays.copyOfRange(singlePacket, headerBytes.length,
                singlePacket.length - footerBytes.length);

        Object obj = null;

        try (Input input = new Input(new ByteArrayInputStream(payload))) {
            obj = kryo.readClassAndObject(input);
        } catch (KryoBufferUnderflowException e) {
            // 【Bug 2 修正】
            // 原始邏輯：將 payload 存入 bigContinueDeque，期望後續拼接。
            // 問題：bigContinueDeque 只有寫入，程式碼中從未讀取，導致：
            //   1. 封包靜默丟失，不會進入 rev 佇列被處理。
            //   2. bigContinueDeque 無限增長，造成記憶體洩漏。
            // 根本原因分析：sendObjectClaude() 已確保 Header+Kryo資料+Footer
            //   在記憶體中完整組裝後才一次性寫入 Socket，正常情況下封包邊界完整，
            //   不應出現此異常。若仍發生，代表封包本身已損壞，繼續拼接無意義。
            // 修正方式：記錄錯誤並丟棄，不再累積無法消費的資料。
            logger.error("[CLIENT-DESERIALIZE] ✗ 封包資料不完整 (KryoBufferUnderflow)，已丟棄。" +
                    " payload size={} bytes。請確認發送端使用 sendObjectClaude() 確保封包原子性。", payload.length, e);
            return;
        } catch (Exception e) {
            logger.error("[CLIENT-DESERIALIZE] ✗ Kryo 反序列化失敗", e);
            return;
        } finally {
            kryo.reset();
        }

        if (obj == null) {
            return;
        }
        if (!(obj instanceof Packet_i)) {
            logger.error("[CLIENT-DESERIALIZE] ✗ 物件不是 Packet_i 類型: {}", obj.getClass().getName());
            return;
        }

        Packet_i packet = (Packet_i) obj;
        if (!rev.offer(packet)) {
            logger.warn("[CLIENT-PROCESS] 接收佇列已滿，封包 {} 被捨棄", packet.getClass().getSimpleName());
        }

    }

    private int countOccurrencesInBytes(byte[] data, byte[] pattern) {
        int count = 0;
        for (int i = 0; i <= data.length - pattern.length; i++) {
            boolean found = true;
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    found = false;
                    break;
                }
            }
            if (found) {
                count++;
                i += pattern.length - 1;
            }
        }
        return count;
    }

    private int indexOf(byte[] data, byte[] pattern, int start) {
        for (int i = start; i <= data.length - pattern.length; i++) {
            boolean found = true;
            for (int j = 0; j < pattern.length; j++) {
                if (data[i + j] != pattern[j]) {
                    found = false;
                    break;
                }
            }
            if (found) return i;
        }
        return -1;
    }

    private void doExitThread() {
        logger.info("[CLIENT-EXIT] 開始清理資源");
        ExitThread ex = new ExitThread();
        try {
            if (rev.size() >= (bufferSize - 100)) logger.error("[CLIENT-EXIT] rev 快滿了");
            if (snd.size() >= (bufferSize - 100)) logger.error("[CLIENT-EXIT] snd 快滿了");

            // 嘗試放入 ExitThread，如果不成功也不強求
            if (!rev.offer(ex)) {
                logger.warn("[CLIENT-EXIT] 接收佇列已滿，無法加入 ExitThread");
            }
            if (!snd.offer(ex)) {
                logger.warn("[CLIENT-EXIT] 發送佇列已滿，無法加入 ExitThread");
            }

            socket.close();
            logger.info("[CLIENT-EXIT] Socket 已關閉");
            if (cnf != null) {
                for (ClinetNotifier cn : cnf) {
                    cn.runDisconnect(this);
                }
            }
        } catch (Exception e) {
            logger.error("[CLIENT-EXIT] 清理資源時發生異常", e);
        }
    }

    public void send(Packet_i obj) {
        try {

            if (snd.size() >= (bufferSize - 100)) {
                logger.error("[CLIENT-SEND] ⚠ 發送佇列快滿了！當前: {} / {}", snd.size(), bufferSize);
            }
            snd.put(obj);

        } catch (InterruptedException e) {
            logger.error("[CLIENT-SEND] ✗ 加入發送佇列時被中斷", e);
            Thread.currentThread().interrupt();
        }
    }

    private final Object sendLock = new Object();

    // ===== 關鍵修改：發送物件 (加入 Log 過濾) =====
    private void sendObject() {
//        Packet_i obj = null;
//        try {
//            obj = snd.take();
//
//            if (obj instanceof ExitThread) {
//                logger.info("[CLIENT-SENDOBJ] 收到 ExitThread，停止發送");
//                this.isExitThread = true;
//                return;
//            }
//
//            synchronized (sendLock) {
//                Kryo kryo = CommunicationServer.kryoLocal.get();
//                try (ByteArrayOutputStream baosSendObj = new ByteArrayOutputStream(socketReadBuffer.length);
//                     Output kryoOutput = new Output(baosSendObj, socketReadBuffer.length)) {
//
//                    baosSendObj.write(keeperHeaerByte.getBytes());
//                    kryo.writeClassAndObject(kryoOutput, obj);
//                    kryoOutput.flush();
//                    baosSendObj.write(keeperFooterByte.getBytes());
//
//                    byte[] data = baosSendObj.toByteArray();
//
//
//                    socket.getOutputStream().write(data);
//                    socket.getOutputStream().flush();
//                    if (logger.isTraceEnabled()) {
//                        logger.trace("socket狀態: {},{},{}", socket.isClosed(), socket.isConnected(), socket.isOutputShutdown());
//                    }
//
//                } finally {
//                    kryo.reset();
//                }
//            }
//
//        } catch (InterruptedException e) {
//            logger.info("[CLIENT-SENDOBJ] 發送執行緒被中斷");
//            Thread.currentThread().interrupt();
//        } catch (Exception e) {
//            logger.error("[CLIENT-SENDOBJ] ✗ 發送封包時發生異常", e);
//            logger.error(LinkerServer.logTpiShortStackTrace(e));
//        }
    	
    	// 2026/03/29 
    	// 使用了 ByteArrayOutputStream(socketReadBuffer.length)，這會直接在堆記憶體中申請一個 10MB 的連續空間。
    	// 二次分配：呼叫 baosSendObj.toByteArray() 時，會產生第二次 10MB 的陣列分配。
    	// GC 壓力：這就是你看到「巨型物件分配」的元兇。這兩個 10MB 的陣列加起來 20MB，在 G1 GC (Region 4MB) 下，會瘋狂觸發 Concurrent Mark Cycle。
    	// 故請 Claude 和 Gemini 優化這段
    	sendObjectClaude();
    }
    
    private void sendObjectClaude() {
        Packet_i obj = null;
        try {
            obj = snd.take();

            if (obj instanceof ExitThread) {
                logger.info("[CLIENT-SENDOBJ] 收到 ExitThread，停止發送");
                this.isExitThread = true;
                return;
            }

            synchronized (sendLock) {
                Kryo kryo = CommunicationServer.kryoLocal.get();
                try {
                    // 【修正說明】
                    // 原錯誤版本：直接對 Socket OutputStream 寫入，
                    // Kryo Output 超過 64KB 時會自動 flush 到 Socket，
                    // 導致 Header + 資料 + Footer 分批送出，破壞封包原子性，造成少傳資料。
                    //
                    // 正確做法：保留 ByteArrayOutputStream 作為組裝緩衝區，
                    // 初始給 4KB（動態擴展，非固定 10MB），避免 Humongous Allocation。
                    // 改用 writeTo() 取代 toByteArray()，消除第二次陣列複製，
                    // 確保 Header + Kryo資料 + Footer 完整組裝後，再一次性原子寫入 Socket。
                    ByteArrayOutputStream baosSendObj = new ByteArrayOutputStream(4 * 1024);

                    baosSendObj.write(keeperHeaerByte.getBytes());

                    // Output 包住 BAOS（非 Socket），超過 64KB 自動擴展到 BAOS，不會提前送出
                    // try-with-resources 的 close() 只關閉 BAOS，不會關閉 Socket ✓
                    try (Output kryoOutput = new Output(baosSendObj, 64 * 1024)) {
                        kryo.writeClassAndObject(kryoOutput, obj);
                        kryoOutput.flush();
                    }

                    baosSendObj.write(keeperFooterByte.getBytes());

                    // 一次性原子寫入 Socket（writeTo 直接串流寫出，無二次陣列複製）
                    baosSendObj.writeTo(socket.getOutputStream());
                    socket.getOutputStream().flush();

                    if (logger.isTraceEnabled()) {
                        logger.trace("socket狀態: {},{},{}", socket.isClosed(), socket.isConnected(), socket.isOutputShutdown());
                    }

                } finally {
                    kryo.reset();
                }
            }

        } catch (InterruptedException e) {
            logger.info("[CLIENT-SENDOBJ] 發送執行緒被中斷");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("[CLIENT-SENDOBJ] ✗ 發送封包時發生異常", e);
            logger.error(LinkerServer.logTpiShortStackTrace(e));
        }
    }
    
    private void sendObjectGemini() {
        Packet_i obj = null;
        try {
            obj = snd.take();

            if (obj instanceof ExitThread) {
                logger.info("[CLIENT-SENDOBJ] 收到 ExitThread，停止發送");
                this.isExitThread = true;
                return;
            }

            synchronized (sendLock) {
                Kryo kryo = CommunicationServer.kryoLocal.get();
                // 直接獲取 Socket 的 OutputStream，避免使用 ByteArrayOutputStream 緩衝整個物件
                OutputStream os = socket.getOutputStream();
                
                // 使用固定且較小的 Buffer (例如 64KB)，Kryo 會自動處理分段寫入
                // 這樣就不會產生超過 2MB 的巨型物件
                try (Output kryoOutput = new Output(os, 64 * 1024)) { 

                    // 1. 寫入 Header
                    os.write(keeperHeaerByte.getBytes());
                    
                    // 2. 直接序列化到 Socket Stream
                    kryo.writeClassAndObject(kryoOutput, obj);
                    
                    // 3. 確保 Kryo 的內部緩衝區全部推送到 Socket
                    kryoOutput.flush(); 
                    
                    // 4. 寫入 Footer
                    os.write(keeperFooterByte.getBytes());
                    
                    // 5. 最後整波 Flush 出去
                    os.flush();

                } finally {
                    kryo.reset();
                }
            }

        } catch (InterruptedException e) {
            logger.info("[CLIENT-SENDOBJ] 發送執行緒被中斷");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.error("[CLIENT-SENDOBJ] ✗ 發送封包時發生異常", e);
            logger.error(LinkerServer.logTpiShortStackTrace(e));
        }
    }

    // ===== 關鍵修改：處理封包 (加入 Log 過濾) =====
    private void processPacket() {
        logger.debug("[CLIENT-PROCESS] 封包處理執行緒已啟動");
        try {
            while (!isExitThread) {
                try {
                    Packet_i o = rev.take();

                    if (o instanceof ExitThread) {
                        logger.info("[CLIENT-PROCESS] 收到 ExitThread，停止處理");
                        this.isExitThread = true;
                        break;
                    }
                    o.runOnClient(this);

                } catch (InterruptedException e) {
                    logger.info("[CLIENT-PROCESS] 處理執行緒收到中斷訊號", e);
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    logger.error("[CLIENT-PROCESS] ✗ 處理封包時發生錯誤", e);
                }
            }
        } catch (Exception e) {
            logger.error("[CLIENT-PROCESS] ✗ 處理執行緒發生嚴重錯誤", e);
        }
        logger.debug("[CLIENT-PROCESS] 封包處理執行緒已結束");
    }

    public void close() {
        logger.info("[CLIENT-CLOSE] 關閉連接");
        try {
            socket.close();
            logger.info("[CLIENT-CLOSE] Socket 已關閉");
        } catch (Exception e) {
            logger.error("[CLIENT-CLOSE] 關閉時發生異常", e);
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

    public boolean isConnected() {
        return !socket.isClosed();
    }

    public void removeMonitor(ClinetNotifier o) {
        if (cnf != null) {
            cnf.remove(o);
        }
    }

    public void setUserName(String userName) {
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