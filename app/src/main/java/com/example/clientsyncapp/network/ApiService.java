package com.example.clientsyncapp.network;

import com.example.clientsyncapp.data.LogApp;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

public interface ApiService {
    // Recuerda reemplazar "YOUR_UNIQUE_ID" por el ID único que te proporciona webhook.site
    @Multipart
    @POST
    Call<Void> uploadClientData(
            @Url String url,
            @Part("cliente") RequestBody clienteJson,
            @Part MultipartBody.Part fotoCasa1,
            @Part MultipartBody.Part fotoCasa2,
            @Part MultipartBody.Part fotoCasa3
    );

    // Recuerda reemplazar "YOUR_UNIQUE_ID" por el ID único que te proporciona webhook.site
    @Multipart
    @POST
    Call<Void> uploadFiles(
            @Url String url,
            @Part("ci") RequestBody ci,
            @Part MultipartBody.Part file
    );

    // Endpoint para enviar los logs de la app
    @POST
    Call<Void> uploadLogs(@Url String url, @Body List<LogApp> logs);
}
