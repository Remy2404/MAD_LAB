package com.example.expense_tracker.routes;

import androidx.annotation.NonNull;
import android.util.Log;

import com.example.expense_tracker.utils.ISO8601DateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.UUID;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String TAG = "RetrofitClient";
    private static final String BASE_URL = "https://expense-tracker-db-kbxp.onrender.com/";
    private static Retrofit retrofit = null;
    private static String dbName;

    /**
     * Set the database name (GUID) to be used in API calls
     */
    public static void setDbName(String name) {
        dbName = name;
        Log.d(TAG, "Database GUID set to: " + dbName);
    }

    /**
     * Get the current database name (GUID)
     */
    public static String getDbName() {
        return dbName;
    }

    /**
     * Get or create the Retrofit client with properly configured interceptors
     */
    public static Retrofit getClient() {
        if (retrofit == null) {
            // Configure Gson for proper date serialization
            Gson gson = new GsonBuilder()
                    .registerTypeAdapterFactory(new ISO8601DateAdapter.TypeAdapterFactory())
                    .create();

            // Add logging interceptor for debugging
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Build OkHttp client with interceptors
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build();

            // Build and return the Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit;
    }
}
