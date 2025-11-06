package com.example.clientsyncapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.TypedValue;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.clientsyncapp.data.LogRepository;
import com.example.clientsyncapp.network.ApiService;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final String TAG = "MainActivity";

    private EditText etCI, etNombreCompleto, etDireccion, etTelefono;
    private ImageView ivFotoCasa1, ivFotoCasa2, ivFotoCasa3;
    private Button btnEnviar, btnIrASubirArchivos;

    private Uri fotoUri1, fotoUri2, fotoUri3;
    private int currentImageView = 0;

    private LogRepository logRepository;

    private final ActivityResultLauncher<Intent> cameraLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getExtras() != null) {
                    Bundle extras = result.getData().getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    Uri imageUri = saveBitmapToFile(imageBitmap);

                    ImageView targetImageView = null;
                    switch (currentImageView) {
                        case 1:
                            targetImageView = ivFotoCasa1;
                            fotoUri1 = imageUri;
                            break;
                        case 2:
                            targetImageView = ivFotoCasa2;
                            fotoUri2 = imageUri;
                            break;
                        case 3:
                            targetImageView = ivFotoCasa3;
                            fotoUri3 = imageUri;
                            break;
                    }

                    if (targetImageView != null && imageBitmap != null) {
                        targetImageView.setImageBitmap(imageBitmap);
                        targetImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        targetImageView.setPadding(0, 0, 0, 0);
                        targetImageView.setImageTintList(null);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logRepository = new LogRepository(getApplication());

        etCI = findViewById(R.id.etCI);
        etNombreCompleto = findViewById(R.id.etNombreCompleto);
        etDireccion = findViewById(R.id.etDireccion);
        etTelefono = findViewById(R.id.etTelefono);
        ivFotoCasa1 = findViewById(R.id.ivFotoCasa1);
        ivFotoCasa2 = findViewById(R.id.ivFotoCasa2);
        ivFotoCasa3 = findViewById(R.id.ivFotoCasa3);
        btnEnviar = findViewById(R.id.btnEnviar);
        btnIrASubirArchivos = findViewById(R.id.btnIrASubirArchivos);

        // Aplicar tinte a los iconos desde el código
        TypedValue typedValue = new TypedValue();
        //getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        //getTheme().resolveAttribute(R.attr.colorPrimary,typedValue,true);
        //int colorPrimary = typedValue.data;
        //int colorPrimary = ContextCompat.getColor(this, R.color.colorPrimary);


        ivFotoCasa1.setOnClickListener(v -> openCamera(1));
        ivFotoCasa2.setOnClickListener(v -> openCamera(2));
        ivFotoCasa3.setOnClickListener(v -> openCamera(3));

        btnEnviar.setOnClickListener(v -> {
            if (validateInput()) {
                sendClientData();
            }
        });

        btnIrASubirArchivos.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UploadFilesActivity.class);
            startActivity(intent);
        });
    }

    private void openCamera(int imageView) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        } else {
            currentImageView = imageView;
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            cameraLauncher.launch(takePictureIntent);
        }
    }

    private Uri saveBitmapToFile(Bitmap bitmap) {
        if (bitmap == null) return null;
        File filesDir = getApplicationContext().getFilesDir();
        File imageFile = new File(filesDir, "image_" + System.currentTimeMillis() + ".jpg");

        try (FileOutputStream fos = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        } catch (IOException e) {
            Log.e(TAG, "Error saving bitmap", e);
            logRepository.insertLog("Error saving bitmap: " + e.getMessage(), TAG);
            return null;
        }
        return Uri.fromFile(imageFile);
    }

    private boolean validateInput() {
        if (etCI.getText().toString().trim().isEmpty() ||
                etNombreCompleto.getText().toString().trim().isEmpty() ||
                etDireccion.getText().toString().trim().isEmpty() ||
                etTelefono.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Por favor, complete todos los campos de texto.", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (fotoUri1 == null || fotoUri2 == null || fotoUri3 == null) {
            Toast.makeText(this, "Por favor, capture las tres imágenes.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void sendClientData() {
        Map<String, String> clientData = new HashMap<>();
        clientData.put("ci", etCI.getText().toString());
        clientData.put("nombreCompleto", etNombreCompleto.getText().toString());
        clientData.put("direccion", etDireccion.getText().toString());
        clientData.put("telefono", etTelefono.getText().toString());

        Gson gson = new Gson();
        String clientJson = gson.toJson(clientData);

        RequestBody clientJsonBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), clientJson);

        MultipartBody.Part fotoCasa1Part = prepareFilePart("fotoCasa1", fotoUri1);
        MultipartBody.Part fotoCasa2Part = prepareFilePart("fotoCasa2", fotoUri2);
        MultipartBody.Part fotoCasa3Part = prepareFilePart("fotoCasa3", fotoUri3);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://webhook.site/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);

        Call<Void> call = apiService.uploadClientData("https://webhook.site/019e0cf0-63bb-4ee3-960d-5390b65dfc46", clientJsonBody, fotoCasa1Part, fotoCasa2Part, fotoCasa3Part);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Datos enviados con éxito", Toast.LENGTH_SHORT).show();
                } else {
                    String error = "Error en la respuesta: " + response.code();
                    Log.e(TAG, error);
                    logRepository.insertLog(error, TAG);
                    Toast.makeText(MainActivity.this, "Error al enviar los datos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                String error = "Fallo en la conexión: " + t.getMessage();
                Log.e(TAG, error, t);
                logRepository.insertLog(error, TAG);
                Toast.makeText(MainActivity.this, "Fallo en la conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        if (fileUri == null || fileUri.getPath() == null) return null;
        File file = new File(fileUri.getPath());
        String mediaType = getContentResolver().getType(fileUri);
        if (mediaType == null) {
            mediaType = "image/jpeg";
        }
        RequestBody requestFile = RequestBody.create(MediaType.parse(mediaType), file);
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera(currentImageView);
            } else {
                Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
