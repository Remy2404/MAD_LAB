package com.example.expense_tracker.utils;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class ISO8601DateAdapter extends TypeAdapter<Date> {
    private final DateFormat dateFormat;

    public ISO8601DateAdapter() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    @Override
    public void write(JsonWriter out, Date date) throws IOException {
        if (date == null) {
            out.nullValue();
        } else {
            out.value(dateFormat.format(date));
        }
    }

    @Override
    public Date read(JsonReader in) throws IOException {
        try {
            if (in.peek() == com.google.gson.stream.JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            String dateStr = in.nextString();
            return dateFormat.parse(dateStr);
        } catch (ParseException e) {
            throw new IOException(e);
        }
    }

    public static class TypeAdapterFactory implements com.google.gson.TypeAdapterFactory {
        @SuppressWarnings("unchecked")
        @Override
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            if (type.getRawType() == Date.class) {
                return (TypeAdapter<T>) new ISO8601DateAdapter();
            }
            return null;
        }
    }
}
