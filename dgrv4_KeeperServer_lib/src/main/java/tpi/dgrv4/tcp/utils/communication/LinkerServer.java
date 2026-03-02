package tpi.dgrv4.tcp.utils.communication;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.KryoBufferUnderflowException;
import com.esotericsoftware.kryo.io.Output;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import tpi.dgrv4.tcp.utils.packets.sys.ExitThread;
import tpi.dgrv4.tcp.utils.packets.sys.Packet_i;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.BlockingQueue;

public class LinkerServer implements Runnable {


    private static Logger logger = LoggerFactory.getLogger(LinkerServer.class);
    public int bufferSize = 999;
    @Getter
    private BlockingQueue<Packet_i> rev;
    @Getter
    private BlockingQueue<Packet_i> snd;
    private List<Notifier> notifiers;
    private Socket socket = null;

    private final String keeperHeaerByte = "_|__K_e_E_p_er__H_ea_d_er__|_(";
    private final String keeperFooterByte = ")_|__digi_9999__|_";

    public String userName = "";

    private Role f身份 = Role.valueOf("NoneLogin");

    public HashMap<String, String> param = new HashMap<>();
    public HashMap<String, Object> paramObj = new HashMap<>();

    private boolean isExitThread = false;


    public LinkerServer(Socket s, List<Notifier> notifiers, int bufferSize) {
        this.bufferSize = bufferSize;
        logger.info("[SERVER-INIT] LinkerServer bufferSize : {}", bufferSize);
        this.rev = new java.util.concurrent.ArrayBlockingQueue<>(this.bufferSize);
        this.snd = new java.util.concurrent.ArrayBlockingQueue<>(this.bufferSize);
        socket = s;
        this.notifiers = notifiers;

        logger.info("[SERVER-INIT] Socket 連接建立: Remote={}:{}, Local={}:{}",
                s.getInetAddress().getHostAddress(), s.getPort(),
                s.getLocalAddress().getHostAddress(), s.getLocalPort());

        new Thread() {
            public void run() {
                logger.info("[SERVER-SEND-THREAD] 發送執行緒已啟動");
                while (true) {
                    if (isExitThread) break;
                    sendObject();
                }
            }
        }.start();

        new Thread() {
            public void run() {
                logger.info("[SERVER-PROCESS-THREAD] 處理執行緒已啟動");
                while (true) {
                    if (isExitThread) break;
                    processPacket();
                }
            }
        }.start();

    }

    private byte[] socketReadBuffer = new byte[1024 * 1024 * 10]; // 10MB
    private Input kryoInput = new Input(socketReadBuffer.length);
    private ByteArrayOutputStream baos4ReadObj = new ByteArrayOutputStream();

    public void run() {
        logger.debug("[SERVER-RUN] 開始接收執行緒");
        try {
            Kryo kryo = CommunicationServer.kryoLocal.get();
            while (true) {
                int bytesRead = socket.getInputStream().read(socketReadBuffer);
                if (bytesRead == -1) {
                    logger.warn("[SERVER-RUN] Socket 讀取返回 -1，連接已關閉");
                    break;
                }

                byte[] actualData = Arrays.copyOf(socketReadBuffer, bytesRead);

                baos4ReadObj.write(actualData, 0, actualData.length);
                baos4ReadObj.flush();

                if (bytesRead == "dgR v4 init detect end!".length()) {
                    boolean endFlag = "dgR v4 init detect end!".equals(baos4ReadObj.toString());
                    baos4ReadObj.reset();
                    if (endFlag) {
                        logger.info("[SERVER-RUN] 收到 dgR 初始化偵測訊號，回應並關閉");
                        socket.getOutputStream().write("Yes, I am dgRunner".getBytes());
                        socket.getOutputStream().flush();
                        socket.close();
                        break;
                    }
                } else if (bytesRead == "DP init detect end!".length()) {
                    boolean endFlag = "DP init detect end!".equals(baos4ReadObj.toString());
                    baos4ReadObj.reset();
                    if (endFlag) {
                        logger.info("[SERVER-RUN] 收到 DP 初始化偵測訊號，回應並關閉");
                        socket.getOutputStream().write("Yes, I am DP".getBytes());
                        socket.getOutputStream().flush();
                        socket.close();
                        break;
                    }
                } else {
                    boolean endFlag = procKyroPacket(kryo);
                    if (endFlag) {
                        logger.info("[SERVER-RUN] 收到 ExitThread 訊號，準備結束");
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("[SERVER-RUN] Server 主動斷線了");
            logger.error(logTpiShortStackTrace(e));
        } catch (Throwable t) {
            logger.error("[SERVER-RUN] Server 主動斷線了 (Throwable)");
            logger.error(logTpiShortStackTrace(t));
        } finally {
            logger.info("[SERVER-RUN] 接收執行緒結束，執行清理");
            doExitThread();
            CommunicationServer.cs.doDisconnectProc(this);
        }
    }

    private Deque<ByteArrayOutputStream> bigContinueDeque = null;

    private boolean procKyroPacket(Kryo kryo) {
        byte[]  arr= baos4ReadObj.toByteArray();

        if (arr.length == 0) return false;

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
                logger.error("[SERVER-DESERIALIZE] ✗ 處理第 {} 個封包失敗", processedPackets + 1, e);
            }

            remainingStart = packetEnd;
        }

        if (remainingStart < arr.length) {
            byte[] remaining = Arrays.copyOfRange(arr, remainingStart, arr.length);
            baos4ReadObj.reset();
            try {
                baos4ReadObj.write(remaining);
            } catch (IOException e) {
                logger.error("[SERVER-PROC] 寫入剩餘位元組時出錯", e);
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
            if (bigContinueDeque == null) {
                bigContinueDeque = new ArrayDeque<>();
            }

            ByteArrayOutputStream baosBigContinue = new ByteArrayOutputStream();
            baosBigContinue.write(payload, 0, payload.length);
            bigContinueDeque.addLast(baosBigContinue);
            return;
        } catch (Exception e) {
            logger.error("[SERVER-DESERIALIZE] ✗ Kryo 反序列化失敗", e);
        } finally {
            kryo.reset();
        }

        if (obj == null) {
            return;
        }

        if (!(obj instanceof Packet_i)) {
            logger.error("[SERVER-DESERIALIZE] ✗ 物件不是 Packet_i 類型: {}", obj.getClass().getName());
            return;
        }

        Packet_i packet = (Packet_i) obj;
        if (!rev.offer(packet)) {
            logger.warn("[SERVER-PROCESS] 接收佇列已滿，封包 {} 被捨棄", packet.getClass().getSimpleName());
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

    public void close() {
        logger.info("[SERVER-CLOSE] 關閉連接");
        try {
            socket.close();
            logger.info("[SERVER-CLOSE] Socket 已關閉");
        } catch (Exception e) {
            logger.error("[SERVER-CLOSE] 關閉時發生異常", e);
        }
    }

    private void doExitThread() {
        logger.info("[SERVER-EXIT] 開始清理資源");
        ExitThread ex = new ExitThread();
        try {
            if (rev.size() >= (bufferSize - 100)) logger.error("[SERVER-EXIT] rev 快滿了");
            if (!rev.offer(ex)) {
                logger.warn("[SERVER-EXIT] 接收佇列已滿，無法加入 ExitThread");
            }
            if (snd.size() >= (bufferSize - 100)) logger.error("[SERVER-EXIT] snd 快滿了");
            if (!snd.offer(ex)) {
                logger.warn("[SERVER-EXIT] 發送佇列已滿，無法加入 ExitThread");
            }
            socket.close();
            logger.info("[SERVER-EXIT] Socket 已關閉");
        } catch (Exception e) {
            logger.error("[SERVER-EXIT] 清理資源時發生異常", e);
        }
    }

    public void send(Packet_i o) {
        try {
            snd.put(o);
        } catch (Exception e) {
            logger.error("[SERVER-SEND] ✗ 加入發送佇列時發生異常", e);
            doExitThread();
            CommunicationServer.cs.doDisconnectProc(this);
            Thread.currentThread().interrupt();
        }
    }

    private final Object sendLock = new Object();

    // ===== 關鍵修改：發送物件 (Server) =====
    private void sendObject() {
        Packet_i obj = null;
        try {
            obj = snd.take();
            if (obj instanceof ExitThread) {
                this.isExitThread = true;
                return;
            }

            synchronized (sendLock) {
                Kryo kryo = CommunicationServer.kryoLocal.get();
                try (ByteArrayOutputStream baosSendObj = new ByteArrayOutputStream(socketReadBuffer.length);
                     Output kryoOutput = new Output(baosSendObj, socketReadBuffer.length)) {

                    baosSendObj.write(keeperHeaerByte.getBytes());
                    kryo.writeClassAndObject(kryoOutput, obj);
                    kryoOutput.flush();
                    baosSendObj.write(keeperFooterByte.getBytes());

                    byte[] data = baosSendObj.toByteArray();

                    socket.getOutputStream().write(data);
                    socket.getOutputStream().flush();

                } finally {
                    kryo.reset();
                }
            }
        } catch (InterruptedException e) {
            logger.info("[SERVER-SENDOBJ] 發送執行緒被中斷" , e);
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            this.isExitThread = true;
            logger.error("[SERVER-SENDOBJ] ✗ 發送封包時發生異常", e);
            close();
            CommunicationServer.cs.doDisconnectProc(this);
        }
    }

    // ===== 關鍵修改：處理封包 (Server) =====
    private void processPacket() {
        try {
            Packet_i o = rev.take();

            if (o instanceof ExitThread) {
                logger.info("[SERVER-PROCESS] 收到 ExitThread，停止處理");
                this.isExitThread = true;
                return;
            }
            o.runOnServer(this);


        } catch (Exception e) {
            this.isExitThread = true;
            logger.error("[SERVER-PROCESS] ✗ Server 主動斷線了");
            logger.error(logTpiShortStackTrace(e));
            close();
            CommunicationServer.cs.doDisconnectProc(this);
            Thread.currentThread().interrupt();
        }
    }

    public static String logTpiShortStackTrace(Throwable e) {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        e.printStackTrace(new java.io.PrintWriter(buf, true));
        String msg = buf.toString();
        try {
            buf.close();
        } catch (Exception t) {
            return t.getMessage();
        }
        return getShortErrMsg(msg + "---END\n");
    }

    private static String getShortErrMsg(String errStr) {
        StringBuilder errMsg = new StringBuilder();
        String keyword = "tpi.dgrv4";
        if (StringUtils.hasText(errStr)) {
            String[] errStrArr = errStr.split("\n");
            if (errStrArr != null && errStrArr.length > 0) {
                for (String str : errStrArr) {
                    if (str.contains(keyword) || str.contains("Exception") || str.contains("Throwable")) {
                        errMsg.append("\n").append(str);
                    }
                }
            }
        }
        return errMsg.toString();
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
}
