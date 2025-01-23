package com.appsonair.appremark.activities;

import static com.appsonair.appremark.utils.FileUtils.getFileFromUri;
import static com.appsonair.appremark.utils.FileUtils.getFileName;
import static com.appsonair.appremark.utils.FileUtils.getFileType;
import static com.appsonair.appremark.utils.FileUtils.getFileSize;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appsonair.appremark.BuildConfig;
import com.appsonair.appremark.R;
import com.appsonair.appremark.adapters.ImageAdapter;
import com.appsonair.appremark.interfaces.OnItemClickListener;
import com.appsonair.appremark.models.ImageData;
import com.appsonair.appremark.models.RemarkFileInfo;
import com.appsonair.appremark.services.AppRemarkService;
import com.appsonair.appremark.utils.ApiUtils;
import com.appsonair.appremark.utils.RemarkTypeMapper;
import com.appsonair.core.services.CoreService;
import com.appsonair.core.services.NetworkService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.skydoves.powerspinner.OnSpinnerItemSelectedListener;
import com.skydoves.powerspinner.PowerSpinnerView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class RemarkActivity extends AppCompatActivity {

    private static final String TAG = "RemarkActivity";
    private static final int PICK_IMAGE = 100;
    private final List<ImageData> imageList = new ArrayList<>();
    private final List<RemarkFileInfo> remarkFileInfoList = new ArrayList<>();
    String remarkType, description, appId;
    ProgressBar progressBar;
    private ImageAdapter imageAdapter;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private boolean hasNetwork;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remark);
        NetworkService.checkConnectivity(this, isAvailable -> hasNetwork = isAvailable);

        //init views
        LinearLayout linearLayout = findViewById(R.id.ll_main);
        LinearLayout llAppbar = findViewById(R.id.ll_appbar);
        progressBar = findViewById(R.id.progress_bar);

        TextView tvAppbarTitle = findViewById(R.id.tv_appbar_title);
        TextView tvRemarkType = findViewById(R.id.tv_remark_type);

        TextView tvDescription = findViewById(R.id.tv_description);
        TextInputEditText etDescription = findViewById(R.id.et_description);
        TextInputLayout tilDescription = findViewById(R.id.til_description);

        PowerSpinnerView spinner = findViewById(R.id.sp_remark_type);
        Button btnSubmit = findViewById(R.id.btn_submit);
        ImageView imgClose = findViewById(R.id.img_close);
        ImageView imgAdd = findViewById(R.id.img_add);

        RecyclerView recyclerView = findViewById(R.id.rv_image);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        imageAdapter = new ImageAdapter(imageList, new OnItemClickListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onItemClick(int position) {
                imageList.remove(position);
                if (imageList.size() < 2) {
                    imgAdd.setVisibility(View.VISIBLE);
                }
                imageAdapter.notifyDataSetChanged();
            }
        });

        recyclerView.setAdapter(imageAdapter);

        linearLayout.setBackgroundColor(parseColorToInt(getOption( AppRemarkService.Properties.pageBackgroundColor)));

        llAppbar.setBackgroundColor(parseColorToInt(getOption(AppRemarkService.Properties.appBarBackgroundColor)));
        tvAppbarTitle.setText(getOption(AppRemarkService.Properties.appBarTitleText));
        tvAppbarTitle.setTextColor(parseColor(getOption(AppRemarkService.Properties.appBarTitleColor)));

        tvRemarkType.setText(getOption(AppRemarkService.Properties.remarkTypeLabelText));
        tvRemarkType.setTextColor(parseColor(getOption(AppRemarkService.Properties.labelColor)));

        tvDescription.setText(getOption(AppRemarkService.Properties.descriptionLabelText));
        tvDescription.setTextColor(parseColor(getOption(AppRemarkService.Properties.labelColor)));
        etDescription.setTextColor(parseColor(getOption(AppRemarkService.Properties.inputTextColor)));
        InputFilter[] filters = new InputFilter[1];
        filters[0] = new InputFilter.LengthFilter(Integer.parseInt(getOption(AppRemarkService.Properties.descriptionMaxLength)) );
        etDescription.setFilters(filters);
        tilDescription.setCounterMaxLength(Integer.parseInt(getOption(AppRemarkService.Properties.descriptionMaxLength)));
        tilDescription.setCounterTextColor(parseColor(getOption(AppRemarkService.Properties.labelColor)));
        tilDescription.setPlaceholderText(getOption(AppRemarkService.Properties.descriptionHintText));
        tilDescription.setPlaceholderTextColor(parseColor(getOption(AppRemarkService.Properties.hintColor)));

        btnSubmit.setText(getOption(AppRemarkService.Properties.buttonText));
        btnSubmit.setTextColor(parseColor(getOption(AppRemarkService.Properties.buttonTextColor)));
        btnSubmit.setBackgroundTintList(parseColor(getOption(AppRemarkService.Properties.buttonBackgroundColor)));

        // Retrieve image path from Intent extras
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("IMAGE_PATH")) {
            Uri imagePath = intent.getParcelableExtra("IMAGE_PATH");
            if (imagePath != null) {
                String fileName = getFileName(this, imagePath);
                // Get image file size
                double imageSize = getFileSize(this, imagePath);
                String fileType = getFileType(this, imagePath);
                ImageData imageData = new ImageData.Builder()
                        .setImageUri(imagePath)
                        .setFileName(fileName)
                        .setFileType(fileType)
                        .setFileSize(imageSize)
                        .build();
                imageList.add(imageData);
                imageAdapter.notifyItemInserted(imageList.size() - 1);
            }
        }

        imgClose.setOnClickListener(view -> onBackPressed());

        imgAdd.setOnClickListener(view -> openGallery());

        spinner.selectItemByIndex(0);
        remarkType = getResources().getStringArray(R.array.remark_type_array)[0];
        spinner.setOnSpinnerItemSelectedListener((OnSpinnerItemSelectedListener<String>) (oldIndex, oldItem, newIndex, newItem) -> remarkType = newItem);

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri selectedImage = result.getData().getData();
                if (selectedImage != null) {
                    String fileName = getFileName(this, selectedImage);
                    String fileType = getFileType(this, selectedImage);
                    // Get image file size
                    double imageSize = getFileSize(this, selectedImage);
                    ImageData imageData = new ImageData.Builder()
                            .setImageUri(selectedImage)
                            .setFileName(fileName)
                            .setFileType(fileType)
                            .setFileSize(imageSize)
                            .build();
                    imageList.add(imageData);
                    if (imageList.size() > 1) {
                        imgAdd.setVisibility(View.GONE);
                    }
                    imageAdapter.notifyItemInserted(imageList.size() - 1);
                }
            }
        });

        btnSubmit.setOnClickListener(view -> {
            description = Objects.requireNonNullElse(etDescription.getText(), "").toString().trim();
                hideKeyboard();
                etDescription.setError(null);
                if (hasNetwork) {
                    if (progressBar.getVisibility() == View.GONE) {
                        appId = CoreService.getAppId(this);
                        if (appId.isEmpty()) {
                            Log.d(TAG, "AppId: " + getString(R.string.error_something_wrong));
                        } else {
                            Log.d(TAG, "AppId: " + appId);
                            progressBar.setVisibility(View.VISIBLE);
                            if (!imageList.isEmpty()) {
                                getUploadImageURL();
                            } else {
                                submitRemark(appId, description);
                            }
                        }
                    }
                } else {
                    Toast.makeText(this, "Please check your internet connection!", Toast.LENGTH_SHORT).show();
                }
        });
    }

    private String getOption(String key) {
        return String.valueOf(AppRemarkService.Properties.INSTANCE.getOptions().get(key));
    }

    private void openGallery() {
        hideKeyboard();
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        activityResultLauncher.launch(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PICK_IMAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: Permission granted!");
            }
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private ColorStateList parseColor(String color) {
        return ColorStateList.valueOf(Color.parseColor(color));
    }

    private int parseColorToInt(String color) {
        return Color.parseColor(color);
    }

    public void getUploadImageURL() {
        final String signedApiURL = BuildConfig.BASE_URL + ApiUtils.getSignedUrl;
        for (int i = 0; i < imageList.size(); i++) {
            ImageData imageData = imageList.get(i);
            final MediaType JSON = MediaType.get("application/json; charset=utf-8");
            OkHttpClient client = new OkHttpClient().newBuilder()
                    .build();

            JSONObject dataObject = new JSONObject();
            JSONObject fileObject = new JSONObject();
            try {
                fileObject.put("fileName", imageData.getFileName());
                fileObject.put("fileType", imageData.getFileType());
                dataObject.put("data", fileObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestBody body = RequestBody.create(dataObject.toString(), JSON);
            Request request = new Request.Builder()
                    .url(signedApiURL)
                    .method("POST", body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.d("Failure : ", String.valueOf(e));
                    hideProgressbar();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    try {
                        if (response.code() == 200) {
                            String responseBody = response.body() != null ? response.body().string() : "";
                            if (!responseBody.isEmpty()) {
                                JSONObject jsonObject = new JSONObject(responseBody);
                                JSONObject dataObject = jsonObject.getJSONObject("data");
                                String fileUrl = dataObject.getString("fileUrl");
                                String signedURL = dataObject.getString("signedURL");
                                uploadImage(signedURL, fileUrl, imageData);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("Failure : ", String.valueOf(e.getMessage()));
                        hideProgressbar();
                    }
                }
            });
        }
    }

    public void uploadImage(String signedURL, String fileUrl, ImageData imageData) {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        File imageFile = getFileFromUri(this, imageData.getImageUri());
        if (imageFile.exists()) {
            RequestBody body = RequestBody.create(imageFile,
                    MediaType.parse(imageData.getFileType()));

            Request request = new Request.Builder()
                    .url(signedURL)
                    .addHeader("Content-Type", imageData.getFileType())
                    .addHeader("x-amz-acl", "public-read")
                    .method("PUT", body)
                    .build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.d("Failure : ", String.valueOf(e));
                    hideProgressbar();
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) {
                    try {
                        if (response.code() == 200) {
                            RemarkFileInfo remarkFileInfo = new RemarkFileInfo.Builder()
                                    .setKey(fileUrl)
                                    .setFileType(imageData.getFileType())
                                    .setFileSize(imageData.getFileSize())
                                    .build();
                            remarkFileInfoList.add(remarkFileInfo);
                            if (imageList.size() == remarkFileInfoList.size()) {
                                submitRemark(appId, description);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("Failure : ", String.valueOf(e.getMessage()));
                        hideProgressbar();
                    }
                }
            });
        } else {
            hideProgressbar();
        }
    }

    public void submitRemark(String appId, String description) {
        final String remarkApiURL = BuildConfig.BASE_URL + ApiUtils.createRemark;
        final MediaType JSON = MediaType.get("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();

        JSONObject jsonObject = new JSONObject();
        try {
            Map<String, Object> additionalInfo = Collections.singletonMap("appRemarkVersion", BuildConfig.VERSION_NAME);
            JSONObject deviceInfoWithAdditionalInfo = CoreService.getDeviceInfo(this, additionalInfo); // if you don't want to pass additionalInfo then just pass [Collections.emptyMap()]
            JSONObject appInfo = deviceInfoWithAdditionalInfo.getJSONObject("appInfo");
            JSONObject deviceInfo = deviceInfoWithAdditionalInfo.getJSONObject("deviceInfo");
            JSONObject whereObject = new JSONObject();
            whereObject.put("appId", appId);

            JSONObject dataObject = new JSONObject();
            if (!AppRemarkService.Properties.INSTANCE.getOptions().isEmpty()) {
                JSONObject metaDataObject = new JSONObject(AppRemarkService.Properties.INSTANCE.getExtraPayload());
                dataObject.put("additionalMetadata", metaDataObject);
            }

            dataObject.put("description", description);
            dataObject.put("type", RemarkTypeMapper.getType(remarkType));

            dataObject.put("deviceInfo", deviceInfo);
            dataObject.put("appInfo", appInfo);

            if (!remarkFileInfoList.isEmpty()) {
                Gson gson = new Gson();
                String remarkFileList = gson.toJson(remarkFileInfoList);
                JSONArray remarkFileListJsonArray = new JSONArray(remarkFileList);
                dataObject.put("attachments", remarkFileListJsonArray);
            }

            jsonObject.put("where", whereObject);
            jsonObject.put("data", dataObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody body = RequestBody.create(jsonObject.toString(), JSON);
        Request request = new Request.Builder()
                .url(remarkApiURL)
                .method("POST", body)
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.d("Failure : ", String.valueOf(e));
                runOnUiThread(() -> progressBar.setVisibility(View.GONE));
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    try {
                        if (response.code() == 200) {
                            String responseBody = response.body() != null ? response.body().string() : "";
                            String message = "";
                            if (!responseBody.isEmpty()) {
                                JSONObject bodyObject = new JSONObject(responseBody);
                                message = bodyObject.getString("message");
                            }
                            String successMessage = message.isEmpty() ? getString(R.string.remark_added_successfully) : message;
                            Toast.makeText(RemarkActivity.this, successMessage, Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            Toast.makeText(RemarkActivity.this, R.string.something_wrong, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.d("Failure : ", String.valueOf(e.getMessage()));
                    }
                });
            }
        });
    }

    private void hideProgressbar() {
        progressBar.setVisibility(View.GONE);
        Toast.makeText(RemarkActivity.this, R.string.something_wrong, Toast.LENGTH_LONG).show();
    }

    /**
     * @noinspection deprecation
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}