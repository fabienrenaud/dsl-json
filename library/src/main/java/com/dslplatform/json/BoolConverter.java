package com.dslplatform.json;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public abstract class BoolConverter {

	static final JsonReader.ReadObject<Boolean> BooleanReader = new JsonReader.ReadObject<Boolean>() {
		@Override
		public Boolean read(JsonReader reader) throws IOException {
			return deserialize(reader);
		}
	};
	static final JsonReader.ReadObject<Boolean> NullableBooleanReader = new JsonReader.ReadObject<Boolean>() {
		@Override
		public Boolean read(JsonReader reader) throws IOException {
			return reader.wasNull() ? null : deserialize(reader);
		}
	};
	static final JsonWriter.WriteObject<Boolean> BooleanWriter = new JsonWriter.WriteObject<Boolean>() {
		@Override
		public void write(JsonWriter writer, Boolean value) {
			serializeNullable(value, writer);
		}
	};
	static final JsonReader.ReadObject<boolean[]> BooleanArrayReader = new JsonReader.ReadObject<boolean[]>() {
		@Override
		public boolean[] read(JsonReader reader) throws IOException {
			if (reader.wasNull()) return null;
			if (reader.last() != '[') throw reader.expecting("[");
			reader.getNextToken();
			return deserializeBoolArray(reader);
		}
	};
	static final JsonWriter.WriteObject<boolean[]> BooleanArrayWriter = new JsonWriter.WriteObject<boolean[]>() {
		@Override
		public void write(JsonWriter writer, boolean[] value) {
			serialize(value, writer);
		}
	};

	public static void serializeNullable(final Boolean value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else if (value) {
			sw.writeAscii("true");
		} else {
			sw.writeAscii("false");
		}
	}

	public static void serialize(final boolean value, final JsonWriter sw) {
		if (value) {
			sw.writeAscii("true");
		} else {
			sw.writeAscii("false");
		}
	}

	public static void serialize(final boolean[] value, final JsonWriter sw) {
		if (value == null) {
			sw.writeNull();
		} else if (value.length == 0) {
			sw.writeAscii("[]");
		} else {
			sw.writeByte(JsonWriter.ARRAY_START);
			sw.writeAscii(value[0] ? "true" : "false");
			for(int i = 1; i < value.length; i++) {
				sw.writeAscii(value[i] ? ",true" : ",false");
			}
			sw.writeByte(JsonWriter.ARRAY_END);
		}
	}

	public static boolean deserialize(final JsonReader reader) throws IOException {
		if (reader.wasTrue()) {
			return true;
		} else if (reader.wasFalse()) {
			return false;
		}
		throw new IOException("Found invalid boolean value at: " + reader.positionInStream());
	}

	public static boolean[] deserializeBoolArray(final JsonReader reader) throws IOException {
		if (reader.last() == ']') {
			return new boolean[0];
		}
		boolean[] buffer = new boolean[4];
		buffer[0] = deserialize(reader);
		int i = 1;
		while (reader.getNextToken() == ',') {
			reader.getNextToken();
			if (i == buffer.length) {
				buffer = Arrays.copyOf(buffer, buffer.length << 1);
			}
			buffer[i++] = deserialize(reader);
		}
		reader.checkArrayEnd();
		return Arrays.copyOf(buffer, i);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Boolean> deserializeCollection(final JsonReader reader) throws IOException {
		return reader.deserializeCollection(BooleanReader);
	}

	public static void deserializeCollection(final JsonReader reader, final Collection<Boolean> res) throws IOException {
		reader.deserializeCollection(BooleanReader, res);
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<Boolean> deserializeNullableCollection(final JsonReader reader) throws IOException {
		return reader.deserializeNullableCollection(BooleanReader);
	}

	public static void deserializeNullableCollection(final JsonReader reader, final Collection<Boolean> res) throws IOException {
		reader.deserializeNullableCollection(BooleanReader, res);
	}
}
