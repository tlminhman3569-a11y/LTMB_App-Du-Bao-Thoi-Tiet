package com.example.weatherapp.ui.search;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.weatherapp.R;
import com.example.weatherapp.models.common.SearchResultItem;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(SearchResultItem item);
        void onFavoriteClick(SearchResultItem item);
    }

    private List<SearchResultItem> results;
    private final Context context;
    private OnItemClickListener listener;

    public SearchResultAdapter(Context context, List<SearchResultItem> results) {
        this.context = context;
        this.results = results;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void updateData(List<SearchResultItem> newResults) {
        this.results = newResults;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_search_result, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SearchResultItem item = results.get(position);
        
        // 1. Hiển thị tên thành phố (tránh dư dấu phẩy nếu country rỗng)
        if (item.getCountry() != null && !item.getCountry().isEmpty()) {
            holder.tvCityName.setText(item.getCityName() + ", " + item.getCountry());
        } else {
            holder.tvCityName.setText(item.getCityName());
        }

        // 2. Hiển thị nhiệt độ (tránh hiển thị NaN và tự động theo cài đặt độ C/độ F)
        if (Double.isNaN(item.getTemperature())) {
            holder.tvTemperature.setText("--");
        } else {
            if (com.example.weatherapp.utils.WeatherUtils.isCelsius(context)) {
                holder.tvTemperature.setText(String.format(java.util.Locale.getDefault(), "%.0f°C", item.getTemperature()));
            } else {
                int tempF = com.example.weatherapp.utils.WeatherUtils.convertCelsiusToFahrenheit(item.getTemperature());
                holder.tvTemperature.setText(String.format(java.util.Locale.getDefault(), "%d°F", tempF));
            }
        }

        // 3. Hiển thị mô tả thời tiết
        holder.tvWeatherDesc.setText(item.getWeatherDesc());

        // 4. Tải icon thời tiết (nếu rỗng thì dùng icon vị trí mặc định)
        if (item.getIconCode() != null && !item.getIconCode().isEmpty()) {
            String iconUrl = "https://openweathermap.org/img/wn/" + item.getIconCode() + "@2x.png";
            Glide.with(context)
                    .load(iconUrl)
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .into(holder.ivWeatherIcon);
        } else {
            holder.ivWeatherIcon.setImageResource(android.R.drawable.ic_menu_mylocation);
        }

        updateFavoriteIcon(holder, item);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
        holder.btnFavorite.setOnClickListener(v -> {
            item.setFavorite(!item.isFavorite());
            updateFavoriteIcon(holder, item);
            if (listener != null) listener.onFavoriteClick(item);
        });
    }

    private void updateFavoriteIcon(ViewHolder holder, SearchResultItem item) {
        if (item.isFavorite()) {
            holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_on);
            holder.btnFavorite.setColorFilter(0xFFFFB300); // vàng - đã thích
        } else {
            holder.btnFavorite.setImageResource(android.R.drawable.btn_star_big_off);
            holder.btnFavorite.setColorFilter(0xFF9E9E9E); // xám - chưa thích
        }
    }

    @Override
    public int getItemCount() {
        return results != null ? results.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCityName, tvTemperature, tvWeatherDesc;
        ImageView ivWeatherIcon;
        ImageButton btnFavorite;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCityName = itemView.findViewById(R.id.tv_city_name);
            tvTemperature = itemView.findViewById(R.id.tv_temperature);
            tvWeatherDesc = itemView.findViewById(R.id.tv_weather_desc);
            ivWeatherIcon = itemView.findViewById(R.id.iv_weather_icon);
            btnFavorite = itemView.findViewById(R.id.btn_favorite);
        }
    }
}