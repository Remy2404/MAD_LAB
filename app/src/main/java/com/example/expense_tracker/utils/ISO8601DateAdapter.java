package com.example.expense_tracker.utils;

import com.google.gson.Gson;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ISO8601DateAdapter implements JsonSerializer<Date>, JsonDeserializer<Date> {
    private final SimpleDateFormat iso8601Format;

    public ISO8601DateAdapter() {
        this.iso8601Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        this.iso8601Format.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(iso8601Format.format(src));
    }

    @Override
    public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        try {
            return iso8601Format.parse(json.getAsString());
        } catch (ParseException e) {
            throw new JsonParseException(e);
        }
    }

    public static class TypeAdapterFactory implements com.google.gson.TypeAdapterFactory {
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (type.getRawType() == Date.class) {
                final TypeAdapter<Date> dateAdapter = gson.getDelegateAdapter(this, TypeToken.get(Date.class));
                final ISO8601DateAdapter iso8601DateAdapter = new ISO8601DateAdapter();

                return new TypeAdapter<T>() {
                    @Override
                    public void write(JsonWriter out, T value) throws IOException {
                        if (value == null) {
                            out.nullValue();
                        } else {
                            JsonElement element = iso8601DateAdapter.serialize((Date) value, null, null);
                            gson.toJson(element, out);
                        }
                    }

                    @SuppressWarnings("unchecked")
                    @Override
                    public T read(JsonReader in) throws IOException {
                        if (in.peek() == JsonToken.NULL) {
                            in.nextNull();
                            return null;
                        }
                        String dateStr = in.nextString();
                        try {
                            return (T) iso8601DateAdapter.iso8601Format.parse(dateStr);
                        } catch (ParseException e) {
                            throw new IOException(e);
                        }
                    }
                };
            }
            return null;
        }
    }
}
