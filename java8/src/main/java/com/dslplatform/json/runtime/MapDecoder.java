package com.dslplatform.json.runtime;

import com.dslplatform.json.JsonReader;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.Callable;

public final class MapDecoder<K, V, T extends Map<K, V>> implements JsonReader.ReadObject<T> {

	private final Type manifest;
	private final Callable<T> newInstance;
	private final JsonReader.ReadObject<K> keyReader;
	private final JsonReader.ReadObject<V> valueReader;

	public MapDecoder(
			final Type manifest,
			final Callable<T> newInstance,
			final JsonReader.ReadObject<K> keyReader,
			final JsonReader.ReadObject<V> valueReader) {
		if (manifest == null) throw new IllegalArgumentException("manifest can't be null");
		if (newInstance == null) throw new IllegalArgumentException("newInstance can't be null");
		if (keyReader == null) throw new IllegalArgumentException("keyReader can't be null");
		if (valueReader == null) throw new IllegalArgumentException("valueReader can't be null");
		this.manifest = manifest;
		this.newInstance = newInstance;
		this.keyReader = keyReader;
		this.valueReader = valueReader;
	}

	@Override
	public T read(JsonReader reader) throws IOException {
		if (reader.wasNull()) return null;
		if (reader.last() != '{') {
			throw new IOException("Expecting '{' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
		}
		final T instance;
		try {
			instance = newInstance.call();
		} catch (Exception e) {
			throw new IOException("Unable to create a new instance of " + manifest, e);
		}
		if (reader.getNextToken() == '}') return instance;
		K key = keyReader.read(reader);
		if (key == null) {
			throw new IOException("Null value detected for key element of " + manifest + " at position " + reader.positionInStream());
		}
		if (reader.getNextToken() != ':') {
			throw new IOException("Expecting ':' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
		}
		reader.getNextToken();
		V value = valueReader.read(reader);
		instance.put(key, value);
		while (reader.getNextToken() == ','){
			reader.getNextToken();
			key = keyReader.read(reader);
			if (key == null) {
				throw new IOException("Null value detected for key element of " + manifest + " at position " + reader.positionInStream());
			}
			if (reader.getNextToken() != ':') {
				throw new IOException("Expecting ':' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
			}
			reader.getNextToken();
			value = valueReader.read(reader);
			instance.put(key, value);
		}
		if (reader.last() != '}') {
			throw new IOException("Expecting '}' at position " + reader.positionInStream() + ". Found " + (char)reader.last());
		}
		return instance;
	}
}
