package com.example.weatherapp.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.weatherapp.R;
import com.example.weatherapp.utils.WeatherUtils;
import com.example.weatherapp.worker.WeatherWorker;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import android.content.Intent;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import java.util.concurrent.TimeUnit;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup rgTemperature, rgWindSpeed;
    private RadioButton rbCelsius, rbFahrenheit, rbKmh, rbMph;
    private com.google.android.material.materialswitch.MaterialSwitch switchDarkMode, switchNotification;
    private SharedPreferences sharedPreferences;

    public static final String SETTINGS_PREFS = "WeatherSettingsPrefs";

    // TranLeMinhMan_Bien cho google singin và UI
    private TextView tvUserInfo;
    private Button btnGoogleLogin;
    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Ánh xạ nút quay lại gán kết thúc màn hình
        LinearLayout btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        // Ánh xạ các thành phần giao diện
        rgTemperature = findViewById(R.id.rgTemperature);
        rgWindSpeed = findViewById(R.id.rgWindSpeed);
        rbCelsius = findViewById(R.id.rbCelsius);
        rbFahrenheit = findViewById(R.id.rbFahrenheit);
        rbKmh = findViewById(R.id.rbKmh);
        rbMph = findViewById(R.id.rbMph);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchNotification = findViewById(R.id.switchNotification);

        sharedPreferences = getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE);


        // 1. Đọc dữ liệu cũ để hiển thị đúng trạng thái giao diện khi vừa vào màn hình
        setupCurrentSettingsView();

        // 2. Lắng nghe sự kiện thay đổi của Nhóm Nhiệt độ
        rgTemperature.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbCelsius) {
                sharedPreferences.edit().putBoolean("is_celsius", true).apply();
            } else if (checkedId == R.id.rbFahrenheit) {
                sharedPreferences.edit().putBoolean("is_celsius", false).apply();
            }
        });

        // 3. Lắng nghe sự kiện thay đổi của Nhóm Tốc độ gió
        rgWindSpeed.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbKmh) {
                sharedPreferences.edit().putBoolean("is_kmh", true).apply();
            } else if (checkedId == R.id.rbMph) {
                sharedPreferences.edit().putBoolean("is_kmh", false).apply();
            }
        });

        // 4. Lắng nghe chế độ tối
        switchDarkMode.setOnClickListener(v -> {
            boolean isChecked = switchDarkMode.isChecked();
            sharedPreferences.edit().putBoolean("is_dark_mode", isChecked).apply();

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // 5. LẮNG NGHE SỰ KIỆN BẬT/TẮT THÔNG BÁO CHẠY NGẦM
        switchNotification.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Lưu trạng thái lựa chọn của người dùng vào máy
            sharedPreferences.edit().putBoolean("is_notification_enabled", isChecked).apply();

            if (isChecked) {
                // 1. TÍNH TOÁN THỜI GIAN CHỜ ĐỂ BẮN THÔNG BÁO VÀO ĐÚNG 7 GIỜ SÁNG
                java.util.Calendar currentDate = java.util.Calendar.getInstance();
                java.util.Calendar dueDate = java.util.Calendar.getInstance();

                // Thiết lập khung giờ vàng bạn muốn: 7 giờ 00 phút 0 giây sáng
                //Có thể chỉnh sửa khung giờ để Test
                dueDate.set(java.util.Calendar.HOUR_OF_DAY, 7);
                dueDate.set(java.util.Calendar.MINUTE, 0);
                dueDate.set(java.util.Calendar.SECOND, 0);

                // Nếu thời điểm hiện tại đã qua 7 giờ sáng rồi, thì phải hẹn vào 7 giờ sáng NGÀY MAI
                if (dueDate.before(currentDate)) {
                    dueDate.add(java.util.Calendar.HOUR_OF_DAY, 24);
                }

                // Tính số mili-giây chênh lệch cần phải chờ (Delay)
                long timeDiff = dueDate.getTimeInMillis() - currentDate.getTimeInMillis();

                androidx.work.Constraints constraints = new androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED) // Chỉ chạy khi có mạng
                        .build();
                // 2. THIẾT LẬP LỊCH CHẠY ĐỊNH KỲ MỖI 24 TIẾNG (MỖI NGÀY 1 LẦN)
                PeriodicWorkRequest weatherWorkRequest =
                        new PeriodicWorkRequest.Builder(WeatherWorker.class, 24, TimeUnit.HOURS)
                                .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS) // Ép chờ đến đúng 7:00 sáng mới chạy
                                .setConstraints(constraints)
                                .build();

                // Đăng ký vào hệ thống
                WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                        "WeatherPeriodicCheck",
                        ExistingPeriodicWorkPolicy.UPDATE,
                        weatherWorkRequest
                );
                //dùng để test: Bật là thông báo ngay
                androidx.work.OneTimeWorkRequest instantRequest =
                        new androidx.work.OneTimeWorkRequest.Builder(WeatherWorker.class)
                                .build();
                WorkManager.getInstance(this).enqueue(instantRequest);

                Toast.makeText(this, "Đã hẹn lịch thông báo vào 7:00 sáng hàng ngày!", Toast.LENGTH_SHORT).show();
            } else {
                // Người dùng TẮT -> Hủy toàn bộ lịch chạy
                WorkManager.getInstance(this).cancelUniqueWork("WeatherPeriodicCheck");
                Toast.makeText(this, "Đã tắt thông báo thời tiết!", Toast.LENGTH_SHORT).show();
            }
        });

        // TranLeMinhMan: Logic khởi tạo và click đăng nhập google
        tvUserInfo = findViewById(R.id.tvUserInfo);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        updateUI(mAuth.getCurrentUser());

        btnGoogleLogin.setOnClickListener(v -> {
            if (mAuth.getCurrentUser() != null) {
                mAuth.signOut();
                mGoogleSignInClient.signOut();
                updateUI(null);
                Toast.makeText(this, "Đã đăng xuất Cloud", Toast.LENGTH_SHORT).show();
            } else {
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                googleSignInLauncher.launch(signInIntent);
            }
        });

        // Nút xóa dữ liệu trên FireBase
        // 1. Ánh xạ nút từ giao diện
        Button btnXoaCloud = findViewById(R.id.btnXoaCloud);

        // 2. Gắn sự kiện khi bấm nút
        btnXoaCloud.setOnClickListener(v -> {

            // Hiện bảng cảnh báo
            new androidx.appcompat.app.AlertDialog.Builder(v.getContext())
                    .setTitle("Xác nhận xóa")
                    .setMessage("Bạn có chắc chắn muốn xóa toàn bộ danh sách yêu thích đã sao lưu trên Đám mây không?")
                    .setPositiveButton("Xóa sạch", (dialog, which) -> {

                        // Xóa trực tiếp trên Firebase
                        String userId = com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser().getUid();

                        com.google.firebase.database.FirebaseDatabase.getInstance("https://weatherapp-deb84-default-rtdb.asia-southeast1.firebasedatabase.app")
                                .getReference()
                                .child("Users")
                                .child(userId)
                                .child("Favorites")
                                .removeValue()
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        android.widget.Toast.makeText(v.getContext(), "Đã xóa sạch sao lưu trên Cloud!", android.widget.Toast.LENGTH_SHORT).show();
                                    } else {
                                        android.widget.Toast.makeText(v.getContext(), "Lỗi khi xóa dữ liệu!", android.widget.Toast.LENGTH_SHORT).show();
                                    }
                                });

                    })
                    .setNegativeButton("Hủy bỏ", null) // Bấm hủy thì đóng bảng
                    .show();
        });
    }

    private void setupCurrentSettingsView() {
        // Đơn vị C hay F
        if (WeatherUtils.isCelsius(this)) {
            rbCelsius.setChecked(true);
        } else {
            rbFahrenheit.setChecked(true);
        }

        // Đơn vị Tốc độ gió: km/h hay mph
        if (WeatherUtils.isKmH(this)) {
            rbKmh.setChecked(true);
        } else {
            rbMph.setChecked(true);
        }

        // Kiểm tra trực tiếp xem hệ thống đang thực sự ở chế độ tối hay không
        int currentNightMode = AppCompatDelegate.getDefaultNightMode();
        boolean isNowDark = (currentNightMode == AppCompatDelegate.MODE_NIGHT_YES);

        if (currentNightMode == AppCompatDelegate.MODE_NIGHT_UNSPECIFIED) {
            // Nếu chưa cấu hình gì, kiểm tra theo cấu hình máy điện thoại
            int sysMode = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
            isNowDark = (sysMode == android.content.res.Configuration.UI_MODE_NIGHT_YES);
        }

        // Đọc từ bộ nhớ đã lưu, nếu không có thì lấy theo trạng thái đang hiển thị thực tế
        boolean isDarkModeSaved = sharedPreferences.getBoolean("is_dark_mode", isNowDark);

        // Ép nút Switch hiển thị đúng 100% theo trạng thái thực tế
        switchDarkMode.setChecked(isDarkModeSaved);

        // Xử lý nút thông báo
        switchNotification.setChecked(sharedPreferences.getBoolean("is_notification_enabled", false));
    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    // TranLeMinhMan: Các hàm xử lý kết quả đăng nhập google và UI
    private final ActivityResultLauncher<Intent> googleSignInLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                    try {
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        firebaseAuthWithGoogle(account.getIdToken());
                    } catch (ApiException e) {
                        Log.e("Auth", "Google sign in failed", e);
                        Toast.makeText(this, "Đăng nhập Google thất bại!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                        Toast.makeText(SettingsActivity.this, "Kết nối Cloud thành công!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(SettingsActivity.this, "Lỗi kết nối Firebase", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            tvUserInfo.setText("Tài khoản: " + user.getEmail());
            btnGoogleLogin.setText("Đăng xuất");
            btnGoogleLogin.setBackgroundTintList(getResources().getColorStateList(android.R.color.holo_red_light, null));
        } else {
            tvUserInfo.setText("Bạn chưa đăng nhập");
            btnGoogleLogin.setText("Đăng nhập bằng Google");
            btnGoogleLogin.setBackgroundTintList(getResources().getColorStateList(R.color.colorPrimary, null));
        }
    }

}
