package com.example.weatherapp.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.weatherapp.api.HomeApiService;
import com.example.weatherapp.models.common.WeatherResponse;
import com.example.weatherapp.ui.home.MainActivity;
import com.example.weatherapp.utils.AppConfig;
import com.example.weatherapp.utils.WeatherUtils;

import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WeatherWorker extends Worker {
    private static final String CHANNEL_ID = "weather_notification_channel";
    private static final int NOTIFICATION_ID = 101;

    public WeatherWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Cấu hình các tham số API (Thay thế bằng API Key và Thành phố của bạn)
        android.content.SharedPreferences prefs =
                getApplicationContext().getSharedPreferences("WeatherCachePrefs", android.content.Context.MODE_PRIVATE);

        //Đọc tên thành phố cuối cùng được lưu lúc app còn bật (Nếu trống thì mặc định là Hanoi)

        //Đọc cài đặt đơn vị độ C hay F của bạn để gọi đúng chuẩn API
        boolean isCelsius = WeatherUtils.isCelsius(getApplicationContext());
        String units = isCelsius ? "metric" : "imperial"; // metric = độ C, imperial = độ F

        double lat = prefs.getFloat("last_lat", (float) AppConfig.DEFAULT_LAT);
        double lon = prefs.getFloat("last_lon", (float) AppConfig.DEFAULT_LON);

        // Khởi tạo Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(AppConfig.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        HomeApiService apiService = retrofit.create(HomeApiService.class);

        try {
            // Gọi API đồng bộ (Synchronous) vì đang chạy trong Background Thread của WorkManager
            Response<WeatherResponse> response = apiService.getCurrentWeather(lat, lon, AppConfig.API_KEY, units, AppConfig.DEFAULT_LANG).execute();

            if (response.isSuccessful() && response.body() != null) {
                WeatherResponse data = response.body();

                // 1. Lấy thông số từ API gửi về
                int temp = (int) Math.round(data.getMain().getTemp());
                String desc = data.getWeather().get(0).getDescription();
                String iconCode = data.getWeather().get(0).getIcon();
                String nameOfCity = data.getName();

                // Viết hoa chữ cái đầu của mô tả thời tiết cho đẹp
                if (desc != null && !desc.isEmpty()) {
                    desc = desc.substring(0, 1).toUpperCase() + desc.substring(1);
                }

                // 2. TẠO BIẾN ĐƠN VỊ ĐỘ ĐỘNG ĐỂ HIỂN THỊ TRÊN THÔNG BÁO
                String tempUnit = isCelsius ? "°C" : "°F";

                // 3. QUY ĐỔI NHIỆT ĐỘ VỀ ĐỘ C ĐỂ CHẠY LOGIC SO SÁNH IF-ELSE CHÍNH XÁC
                // Nếu app đang bật độ F (isCelsius = false), ta đổi tạm thời số temp sang độ C để so sánh nóng/lạnh
                int tempInCelsiusToCompare = isCelsius ? temp : WeatherUtils.convertFahrenheitToCelsius(temp);

                String weatherAdvice = "Chúc bạn một ngày mới tốt lành!";

                // Dựa trên mã trạng thái thời tiết (Trời mưa, dông, nắng...)
                if (iconCode != null) {
                    if (iconCode.startsWith("09") || iconCode.startsWith("10") || iconCode.startsWith("11")) {
                        weatherAdvice = "Trời có mưa dông. Đừng quên mang theo ô hoặc áo mưa nhé!";
                    } else if (iconCode.startsWith("13")) {
                        weatherAdvice = "Trời có tuyết rơi, di chuyển cẩn thận bạn nhé.";
                    } else if (iconCode.startsWith("01")) {
                        weatherAdvice = "Trời nắng ráo. Bạn nên mang theo áo chống nắng khi ra ngoài.";
                    }
                }

                // Lời khuyên theo nhiệt độ cảm nhận (SỬ DỤNG BIẾN ĐÃ QUY ĐỔI ĐỂ SO SÁNH)
                if (tempInCelsiusToCompare < 18) {
                    weatherAdvice += " Thời tiết khá lạnh, nhớ mặc thêm áo ấm nhé!";
                } else if (tempInCelsiusToCompare > 35) {
                    weatherAdvice += " Trời rất oi bức, hãy nhớ uống nhiều nước.";
                }

                // 3. Tạo nội dung hoàn chỉnh cho thông báo
                String title = "Thời tiết tại " + nameOfCity;
                String content = temp + tempUnit + " - " + desc + ". " + weatherAdvice;

                // 4. Phát thông báo
                sendNotification(title, content);

                return Result.success();
            } else {
                String errorMsg = "Lỗi mã: " + response.code();
                android.util.Log.e("WeatherWorker", errorMsg);

                // Bắn tạm thông báo lỗi lên màn hình để bạn nhìn thấy ngay lập tức khi test
                sendNotification("Lỗi tải thời tiết", errorMsg);

                return Result.retry();
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure(); // Lỗi kết nối mạng mạng -> Thất bại
        }
    }

    // Hàm tạo Notification Channel và gửi thông báo lên thanh trạng thái
    private void sendNotification(String title, String content) {
        Context context = getApplicationContext();
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo Notification Channel đối với Android 8.0 (Oreo) trở lên
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Weather Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            channel.setDescription("Kênh hiển thị thông báo thời tiết định kỳ");
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

        PendingIntent pendingIntent = createClickIntent(context);

        // Xây dựng giao diện thông báo (Bạn nhớ chuẩn bị icon ic_weather trong drawable)
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Có thể thay bằng R.drawable.ic_weather
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content)) // Cho phép hiển thị nội dung dài
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
    /**
     * Hàm hỗ trợ tạo PendingIntent để khi người dùng bấm vào thông báo sẽ mở MainActivity
     */
    private PendingIntent createClickIntent(Context context) {
        // 1. Tạo Intent trỏ thẳng đến màn hình chính MainActivity
        Intent intent = new Intent(context, MainActivity.class);

        // 2. Thiết lập Flags: Xóa các Activity cũ đang chạy dưới nền và đưa MainActivity lên đầu cấu trúc Stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // 3. Cấu hình Flags bảo mật cho PendingIntent tùy theo phiên bản Android (Bắt buộc IMMUTABLE từ Android 23+)
        int flags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ?
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT;

        // 4. Trả về đối tượng PendingIntent hoàn chỉnh
        return PendingIntent.getActivity(context, 0, intent, flags);
    }
}
