package com.example.weatherapp.ui.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.api.RetrofitClient;
import com.example.weatherapp.api.SearchApiService;
import com.example.weatherapp.models.common.SearchResultItem;
import com.example.weatherapp.models.common.WeatherResponse;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private static final String API_KEY = "f5ddeb6ee9905d6e63de7dc0e8742e18";
    private static final String PREF_NAME = "SearchHistory";
    private static final String PREF_KEY_HISTORY = "history";
    private static final int MAX_HISTORY = 10;
    private static final int SEARCH_DELAY_MS = 500;

    private EditText etSearchBar;
    private ImageButton btnBack, btnClearSearch;
    private ProgressBar progressBar;
    private RecyclerView rvSearchResults, rvHistory;
    private TextView tvHistoryLabel, tvNoResult;

    private SearchResultAdapter resultAdapter;
    private HistoryAdapter historyAdapter;
    private List<SearchResultItem> resultList = new ArrayList<>();
    private List<String> historyList = new ArrayList<>();

    private SearchApiService searchApiService;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private Call<WeatherResponse> currentCall;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        initViews();
        initAdapters();
        initApiService();
        loadHistory();
        setupListeners();
        showHistorySection();
    }

    private void initViews() {
        etSearchBar     = findViewById(R.id.et_search_bar);
        btnBack         = findViewById(R.id.btn_back);
        btnClearSearch  = findViewById(R.id.btn_clear_search);
        progressBar     = findViewById(R.id.progress_bar);
        rvSearchResults = findViewById(R.id.rv_search_results);
        rvHistory       = findViewById(R.id.rv_history);
        tvHistoryLabel  = findViewById(R.id.tv_history_label);
        tvNoResult      = findViewById(R.id.tv_no_result);
    }

    private void initAdapters() {
        resultAdapter = new SearchResultAdapter(this, resultList);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(resultAdapter);

        historyAdapter = new HistoryAdapter(this, historyList);
        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        rvHistory.setAdapter(historyAdapter);

        resultAdapter.setOnItemClickListener(new SearchResultAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(SearchResultItem item) {
                saveToHistory(item.getCityName());
                Toast.makeText(SearchActivity.this,
                        "Đã chọn: " + item.getCityName(), Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFavoriteClick(SearchResultItem item) {
                saveCityToDatabase(item.getCityName(), item.getCountry(),
                        item.getTemperature(), item.getWeatherDesc(), item.getIconCode());
            }
        });

        historyAdapter.setOnHistoryClickListener(new HistoryAdapter.OnHistoryClickListener() {
            @Override
            public void onHistoryClick(String cityName) {
                etSearchBar.setText(cityName);
                etSearchBar.setSelection(cityName.length());
                performSearch(cityName);
            }

            @Override
            public void onDeleteClick(int position, String cityName) {
                deleteFromHistory(position, cityName);
            }
        });
    }

    private void initApiService() {
        searchApiService = RetrofitClient.getClient().create(SearchApiService.class);
    }

    private void setupListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnClearSearch.setOnClickListener(v -> {
            etSearchBar.setText("");
            showHistorySection();
        });

        etSearchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                btnClearSearch.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);

                if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
                if (currentCall != null) currentCall.cancel();

                if (query.isEmpty()) {
                    showHistorySection();
                    return;
                }
                searchRunnable = () -> performSearch(query);
                handler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }
        });
    }

    private void performSearch(String cityName) {
        showLoadingState();
        currentCall = searchApiService.searchCityWeather(cityName, API_KEY, "metric", "vi");
        currentCall.enqueue(new Callback<WeatherResponse>() {
            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                if (call.isCanceled()) return;
                if (response.isSuccessful() && response.body() != null) {
                    WeatherResponse data = response.body();
                    String name    = data.getName();
                    String country = (data.getSys() != null) ? data.getSys().getCountry() : "";
                    double temp    = data.getMain().getTemp();
                    String desc    = data.getWeather().get(0).getDescription();
                    String icon    = data.getWeather().get(0).getIcon();

                    SearchResultItem item = new SearchResultItem(name, country, temp, desc, icon);
                    resultList.clear();
                    resultList.add(item);
                    showResultSection();
                } else {
                    showNoResultState();
                }
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                if (call.isCanceled()) return;
                showNoResultState();
                Toast.makeText(SearchActivity.this, "Lỗi kết nối. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoadingState() {
        progressBar.setVisibility(View.VISIBLE);
        rvSearchResults.setVisibility(View.GONE);
        rvHistory.setVisibility(View.GONE);
        tvHistoryLabel.setVisibility(View.GONE);
        tvNoResult.setVisibility(View.GONE);
    }

    private void showResultSection() {
        progressBar.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.VISIBLE);
        rvHistory.setVisibility(View.GONE);
        tvHistoryLabel.setVisibility(View.GONE);
        tvNoResult.setVisibility(View.GONE);
        resultAdapter.updateData(resultList);
    }

    private void showHistorySection() {
        progressBar.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.GONE);
        tvNoResult.setVisibility(View.GONE);
        boolean hasHistory = !historyList.isEmpty();
        tvHistoryLabel.setVisibility(hasHistory ? View.VISIBLE : View.GONE);
        rvHistory.setVisibility(hasHistory ? View.VISIBLE : View.GONE);
    }

    private void showNoResultState() {
        progressBar.setVisibility(View.GONE);
        rvSearchResults.setVisibility(View.GONE);
        rvHistory.setVisibility(View.GONE);
        tvHistoryLabel.setVisibility(View.GONE);
        tvNoResult.setVisibility(View.VISIBLE);
    }

    private void loadHistory() {
        SharedPreferences prefs = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        Set<String> saved = prefs.getStringSet(PREF_KEY_HISTORY, new LinkedHashSet<>());
        historyList.clear();
        historyList.addAll(saved);
        historyAdapter.updateData(historyList);
    }

    private void saveToHistory(String cityName) {
        LinkedHashSet<String> updated = new LinkedHashSet<>();
        updated.add(cityName);
        updated.addAll(historyList);
        List<String> trimmed = new ArrayList<>(updated);
        if (trimmed.size() > MAX_HISTORY) trimmed = trimmed.subList(0, MAX_HISTORY);
        historyList.clear();
        historyList.addAll(trimmed);
        historyAdapter.updateData(historyList);
        persistHistory();
    }

    private void deleteFromHistory(int position, String cityName) {
        historyList.remove(position);
        historyAdapter.notifyItemRemoved(position);
        persistHistory();
        if (historyList.isEmpty()) showHistorySection();
    }

    private void persistHistory() {
        getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
                .edit()
                .putStringSet(PREF_KEY_HISTORY, new LinkedHashSet<>(historyList))
                .apply();
    }

    // Placeholder UC4 - TV4 implement sau
    private void saveCityToDatabase(String cityName, String country,
                                    double temperature, String weatherDesc, String iconCode) {
        Toast.makeText(this, "Đã bấm thích! ❤️ " + cityName, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentCall != null) currentCall.cancel();
        if (searchRunnable != null) handler.removeCallbacks(searchRunnable);
    }
}