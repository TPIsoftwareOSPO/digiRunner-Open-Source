package tpi.dgrv4.gateway.component.cache.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotSerializableException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import tpi.dgrv4.common.utils.StackTraceUtil;
import tpi.dgrv4.gateway.keeper.TPILogger;

public class CacheValueDefaultAdapter implements CacheValueAdapter {

	private TPILogger logger = TPILogger.tl;

	@Override
	public String getIdentifier() {
		return null;
	}

	@Override
	public byte[] serialize(Object obj) {
		ByteArrayOutputStream baos = null;
		ObjectOutputStream oos = null;
		byte[] data = null;
		try {
			baos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(baos);
			oos.writeObject(obj);
			data = baos.toByteArray();
		} catch (NotSerializableException e) {
			this.logger.error(
				obj.getClass() + " doesn't implement java.io.Serializable?\n" + StackTraceUtil.logStackTrace(e));
		} catch (Exception e) {
			this.logger.error("An error occurred while serializing object into cacheMap, class=" + obj.getClass() + "\n"
				+ StackTraceUtil.logStackTrace(e));
		} finally {
			try {
				if (oos != null) {
					oos.close();
					oos = null;
				}
				if (baos != null) {
					baos.close();
					baos = null;
				}
			} catch (Exception e) {
				this.logger.error(
					"An error occurred while closing stream during serialization of object for cacheMap, class=" + obj.getClass()
						+ "\n" + StackTraceUtil.logStackTrace(e));
			}
		}
		return data;
	}

	@Override
	public Object deserialize(byte[] data) {
		ByteArrayInputStream bais = null;
		ObjectInputStream ois = null;
		Object obj = null;
		try {
			bais = new ByteArrayInputStream(data);
			ois = new ObjectInputStream(bais);
			obj = ois.readObject();
		} catch (Exception e) {
			this.logger.error("An error occurred while deserializing object in cacheMap.\n"
					+ StackTraceUtil.logStackTrace(e));
		} finally {
			try {
				if (ois != null) {
					ois.close();
					ois = null;
				}
				if (bais != null) {
					bais.close();
					bais = null;
				}
			} catch (Exception e) {
				this.logger.error(
					"An error occurred while closing stream during deserialization of object in cacheMap.\n"
						+ StackTraceUtil.logStackTrace(e));
			}
		}
		return obj;
	}

}