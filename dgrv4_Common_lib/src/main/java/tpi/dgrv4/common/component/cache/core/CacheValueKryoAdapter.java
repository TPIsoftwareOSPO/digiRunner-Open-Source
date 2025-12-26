package tpi.dgrv4.common.component.cache.core;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class CacheValueKryoAdapter implements CacheValueAdapter {

	private final String identifier;

	private final Consumer<Kryo> customRegistration;

	/*
	 * Because Kryo is not thread safe and constructing and configuring a Kryo instance is relatively expensive, 
	 * in a multithreaded environment.
	 */
//	private final ThreadLocal<Kryo> kryos;
	private final Map<String, ThreadLocal<Kryo>> kryos = new HashMap<>();

	public CacheValueKryoAdapter() {
		this(null, null);
	}

	public CacheValueKryoAdapter(String identifier, final Consumer<Kryo> kryoRegistration) {
		ThreadLocal<Kryo> kryos = null;
		this.identifier = identifier;
		this.customRegistration = kryoRegistration;
		kryos = ThreadLocal.withInitial(() -> {
			Kryo kryo = new Kryo();
			kryo.setRegistrationRequired(false);

			// 註冊所有需要序列化的類型
			kryo.register(byte[].class, 1000);
			kryo.register(java.util.ArrayList.class, 1001);
			kryo.register(java.util.Date.class, 1002);
			kryo.register(java.util.Optional.class, 1003);
			kryo.register(java.sql.Timestamp.class, 1004);

			if (customRegistration != null) {
				customRegistration.accept(kryo);
			}
			
			return kryo;
	    });
		this.kryos.put("kryo", kryos);
	}

	@Override
	public String getIdentifier() {
		return this.identifier;
	}

	@Override
	public byte[] serialize(Object obj) {
		Output output = new Output(16, 1_048_576);	// 1MB max capacity
		Kryo kryo = getKryo();
		kryo.writeClassAndObject(output, obj);
		output.close();
		return output.getBuffer();
	}

	@Override
	public Object deserialize(byte[] data) {
		Input input = new Input(new ByteArrayInputStream(data));
		Kryo kryo = getKryo();
		Object obj = kryo.readClassAndObject(input);
		input.close();
		return obj;
	}

	private Kryo getKryo() {
		return this.kryos.get("kryo").get();
	}

}
