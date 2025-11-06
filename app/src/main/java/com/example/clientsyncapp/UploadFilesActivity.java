package com.example.clientsyncapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.clientsyncapp.data.LogRepository;
import com.example.clientsyncapp.network.ApiService;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UploadFilesActivity extends AppCompatActivity {

    private static final String TAG = "UploadFilesActivity";

    private EditText etCI;
    private Button btnSeleccionarArchivos, btnSubirArchivos;
    private TextView tvArchivosSeleccionados;

    private List<Uri> selectedFiles = new ArrayList<>();
    private LogRepository logRepository;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    if (result.getData().getClipData() != null) {
                        int count = result.getData().getClipData().getItemCount();
                        for (int i = 0; i < count; i++) {
                            selectedFiles.add(result.getData().getClipData().getItemAt(i).getUri());
                        }
                    } else if (result.getData().getData() != null) {
                        selectedFiles.add(result.getData().getData());
                    }
                    tvArchivosSeleccionados.setText("Archivos seleccionados: " + selectedFiles.size());
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_files);

        logRepository = new LogRepository(getApplication());

        etCI = findViewById(R.id.etCI_upload);
        btnSeleccionarArchivos = findViewById(R.id.btnSeleccionarArchivos);
        btnSubirArchivos = findViewById(R.id.btnSubirArchivos);
        tvArchivosSeleccionados = findViewById(R.id.tvArchivosSeleccionados);

        btnSeleccionarArchivos.setOnClickListener(v -> openFilePicker());
        btnSubirArchivos.setOnClickListener(v -> {
            if (validateInput()) {
                try {
                    File zipFile = createZipFile();
                    uploadFiles(zipFile);
                } catch (IOException e) {
                    String error = "Error creating zip file: " + e.getMessage();
                    Log.e(TAG, error, e);
                    logRepository.insertLog(error, TAG);
                    Toast.makeText(this, "Error al comprimir los archivos", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        filePickerLauncher.launch(intent);
    }

    private boolean validateInput() {
        if (etCI.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor, ingrese el CI del cliente.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (selectedFiles.isEmpty()) {
            Toast.makeText(this, "Por favor, seleccione al menos un archivo.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private File createZipFile() throws IOException {
        File zipFile = new File(getCacheDir(), "files.zip");
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));

        for (Uri fileUri : selectedFiles) {
            BufferedInputStream origin = new BufferedInputStream(getContentResolver().openInputStream(fileUri));
            ZipEntry entry = new ZipEntry(new File(fileUri.getPath()).getName());
            out.putNextEntry(entry);
            byte[] data = new byte[1024];
            int count;
            while ((count = origin.read(data, 0, 1024)) != -1) {
                out.write(data, 0, count);
            }
            origin.close();
        }
        out.close();
        return zipFile;
    }

    private void uploadFiles(File zipFile) {
        RequestBody ciBody = RequestBody.create(MediaType.parse("text/plain"), etCI.getText().toString());
        RequestBody requestFile = RequestBody.create(MediaType.parse("application/zip"), zipFile);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", zipFile.getName(), requestFile);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://webhook.site/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<Void> call = apiService.uploadFiles("https://webhook.site/019e0cf0-63bb-4ee3-960d-5390b65dfc46", ciBody, body);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(UploadFilesActivity.this, "Archivos subidos con éxito", Toast.LENGTH_SHORT).show();
                } else {
                    String error = "Error en la respuesta: " + response.code();
                    Log.e(TAG, error);
                    logRepository.insertLog(error, TAG);
                    Toast.makeText(UploadFilesActivity.this, "Error al subir los archivos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String error = "Fallo en la conexión: " + t.getMessage();
                Log.e(TAG, error, t);
                logRepository.insertLog(error, TAG);
                Toast.makeText(UploadFilesActivity.this, "Fallo en la conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
