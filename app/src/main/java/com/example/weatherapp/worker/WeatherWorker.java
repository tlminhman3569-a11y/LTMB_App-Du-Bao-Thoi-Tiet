package com.example.weatherapp.worker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.weatherapp.models.common.WeatherResponse;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class WeatherWorker extends Worker{
    private static final String CHANNEL_ID = "weather_notification_channel";
    private static final int NOTIFICATION_ID = 101;

    // Giả định API Interface đặt chung ở đây để bạn dễ quản lý, hoặc bạn có thể tách riêng
    public interface WeatherApiService {
        @GET("weather")
        Call<WeatherResponse> getWeather(
                @Query("q") String cityName,
                @Query("appid") String apiKey,
                @Query("units") String units,
                @Query("lang") String lang
        );
    }

    public WeatherWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Cấu hình các tham số API (Thay thế bằng API Key và Thành phố của bạn)
        android.content.SharedPreferences prefs =
                getApplicationContext().getSharedPreferences("WeatherSettingsPrefs", android.content.Context.MODE_PRIVATE);

        //Đọc tên thành phố cuối cùng được lưu lúc app còn bật (Nếu trống thì mặc định là Hanoi)
        String cityName = prefs.getString("cached_city_name", "Hanoi");

        //Đọc cài đặt đơn vị độ C hay F của bạn để gọi đúng chuẩn API
        boolean isCelsius = prefs.getBoolean("is_celsius", true);
        String units = isCelsius ? "metric" : "imperial"; // metric = độ C, imperial = độ F
        String apiKey = "021b9aa15fd0ca5d671eca611de75ec2";
        String lang = "vi";      // Để lấy mô tả bằng tiếng Việt

        // Khởi tạo Retrofit
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/data/2.5/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        WeatherApiService apiService = retrofit.create(WeatherApiService.class);

        try {
            // Gọi API đồng bộ (Synchronous) vì đang chạy trong Background Thread của WorkManager
            Response<WeatherResponse> response = apiService.getWeather(cityName, apiKey, units, lang).execute();

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
                int tempInCelsiusToCompare = isCelsius ? temp : (int) Math.round((temp - 32) / 1.8);

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
                return Result.retry(); // Lỗi API hoặc sai Key -> Thử lại sau
            }

        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure(); // Lỗi kết nối mạng mạng -> Thất bại
        }
    }

    // Hàm tạo Notification Channel và gửi thông báo lên thanh trạng thái
    private void sendNotification(String title, String content) {
        NotificationManager notificationManager =
                (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

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

        // Xây dựng giao diện thông báo (Bạn nhớ chuẩn bị icon ic_weather trong drawable)
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info) // Có thể thay bằng R.drawable.ic_weather
                .setContentTitle(title)
                .setContentText(content)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(content)) // Cho phép hiển thị nội dung dài
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }
}
