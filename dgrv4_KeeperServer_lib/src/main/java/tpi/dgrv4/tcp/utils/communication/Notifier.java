package tpi.dgrv4.tcp.utils.communication;

/**
 * 斷線通知器, 只要我想要讓外部程式在 server 執行斷線時做一件事, 則
 * 只要 create Class implement DisconnetNotifier,
 * 並在 constructer 加入就可以了 
 * @author John
 * 
 */
public interface Notifier {
	void runDisconnect(LinkerServer conn);
	void runConnection(LinkerServer conn);
	void setRole(Role f身份, LinkerServer conn);
}
