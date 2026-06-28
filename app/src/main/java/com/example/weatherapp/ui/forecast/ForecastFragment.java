package com.example.weatherapp.ui.forecast;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.weatherapp.R;
import com.example.weatherapp.api.ForecastApiService;
import com.example.weatherapp.api.ForecastRetrofitClient;
import com.example.weatherapp.models.forecast.ForecastItem;
import com.example.weatherapp.models.forecast.ForecastResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForecastFragment extends Fragment {

    // Tọa độ mặc định cho TP.HCM trong trường hợp chạy thử nghiệm độc lập hoặc lỗi GPS
    private static final double MOCK_LAT = 10.823;
    private static final double MOCK_LON = 106.629;

    // API Key
    private final String API_KEY = "ec300b0837672f3a17c36026f68a0f00";

    private RecyclerView rvHourlyForecast;
    private RecyclerView rvDailyForecast;
    private HourlyForecastAdapter hourlyAdapter;
    private DailyForecastAdapter dailyAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forecast, container, false);

        // Khoi tao RecyclerView du bao hang gio
        rvHourlyForecast = view.findViewById(R.id.rvHourlyForecast);
        rvHourlyForecast.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        hourlyAdapter = new HourlyForecastAdapter();
        rvHourlyForecast.setAdapter(hourlyAdapter);

        // Khoi tao RecyclerView du bao hang ngay
        rvDailyForecast = view.findViewById(R.id.rvDailyForecast);
        rvDailyForecast.setLayoutManager(new LinearLayoutManager(getContext()));
        dailyAdapter = new DailyForecastAdapter();
        rvDailyForecast.setAdapter(dailyAdapter);

        fetchForecast();

        return view;
    }

    private void fetchForecast() {
        ForecastApiService apiService = ForecastRetrofitClient.getClient().create(ForecastApiService.class);

        String units = isCelsius() ? "metric" : "imperial";
        Call<ForecastResponse> call = apiService.getForecast(MOCK_LAT, MOCK_LON, API_KEY, units, "vi");

        call.enqueue(new Callback<ForecastResponse>() {
            @Override
            public void onResponse(Call<ForecastResponse> call, Response<ForecastResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ForecastItem> fullList = response.body().getList();
                    if (fullList != null && !fullList.isEmpty()) {
                        
                        // Lay du lieu du bao hang gio
                        List<ForecastItem> hourlyList = new ArrayList<>();
                        for (int i = 0; i < Math.min(8, fullList.size()); i++) {
                            hourlyList.add(fullList.get(i));
                        }
                        hourlyAdapter.setData(hourlyList);

                        // Lay du lieu du bao hang ngay
                        List<ForecastItem> dailyList = new ArrayList<>();
                        for (ForecastItem item : fullList) {
                            String dtTxt = item.getDtTxt();
                            if (dtTxt != null && dtTxt.contains("12:00:00")) {
                                dailyList.add(item);
                            }
                        }
                        dailyAdapter.setData(dailyList);
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi dữ liệu dự báo từ API!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ForecastResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isCelsius() {
        return true;
    }
}
