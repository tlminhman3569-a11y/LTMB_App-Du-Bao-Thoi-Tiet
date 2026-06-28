package com.example.weatherapp.ui.forecast;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.weatherapp.R;
import com.example.weatherapp.models.forecast.ForecastItem;
import java.util.ArrayList;
import java.util.List;

public class HourlyForecastAdapter extends RecyclerView.Adapter<HourlyForecastAdapter.HourlyViewHolder> {

    private List<ForecastItem> hourlyList = new ArrayList<>();

    public void setData(List<ForecastItem> list) {
        this.hourlyList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HourlyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hourly_forecast, parent, false);
        return new HourlyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyViewHolder holder, int position) {
        ForecastItem item = hourlyList.get(position);
        // Thiết lập màu nền kính mờ cho thẻ
        holder.cardHourlyView.setCardBackgroundColor(
                androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.colorGlassCard));

        // Hiển thị giờ (ví dụ: "2025-06-28 09:00:00" -> "09:00")
        String dtTxt = item.getDtTxt();
        if (dtTxt != null && dtTxt.length() >= 16) {
            String time = dtTxt.substring(11, 16);
            holder.tvHourlyTime.setText(time);
        }

        // Hiển thị nhiệt độ
        if (item.getMain() != null) {
            int temp = (int) Math.round(item.getMain().getTemp());
            String unit = isCelsius() ? "°" : "°F";
            holder.tvHourlyTemp.setText(temp + unit);
        }

        // Load icon bằng Glide
        if (item.getWeather() != null && !item.getWeather().isEmpty()) {
            String iconCode = item.getWeather().get(0).getIcon();
            String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@4x.png";
            Glide.with(holder.itemView.getContext())
                    .load(iconUrl)
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .into(holder.imgHourlyIcon);

            // Thiết lập mô tả chi tiết thời tiết
            String desc = item.getWeather().get(0).getDescription();
            if (desc != null && !desc.isEmpty()) {
                desc = desc.substring(0, 1).toUpperCase() + desc.substring(1);
                final String finalDesc = desc;

                // Tooltip hiển thị mặc định khi nhấn giữ (hệ thống Android tự quản lý)
                androidx.appcompat.widget.TooltipCompat.setTooltipText(holder.itemView, finalDesc);

                // Hiển thị Toast thông báo nhanh khi click thường vào thẻ
                holder.itemView.setOnClickListener(v -> {
                    android.widget.Toast.makeText(v.getContext(), finalDesc, android.widget.Toast.LENGTH_SHORT).show();
                });
            }
        }

        // Hien thi kha nang co mua (Probability of Precipitation)
        double pop = item.getPop();
        if (pop > 0) {
            int popPercent = (int) Math.round(pop * 100);
            holder.tvHourlyPop.setText(popPercent + "%");
            holder.tvHourlyPop.setVisibility(View.VISIBLE);
        } else {
            // Sử dụng INVISIBLE để giữ nguyên diện tích của thẻ, tránh bị lệch chiều cao
            holder.tvHourlyPop.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return hourlyList.size();
    }

    static class HourlyViewHolder extends RecyclerView.ViewHolder {
        TextView tvHourlyTime, tvHourlyTemp, tvHourlyPop;
        ImageView imgHourlyIcon;
        com.google.android.material.card.MaterialCardView cardHourlyView;

        public HourlyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHourlyTime = itemView.findViewById(R.id.tvHourlyTime);
            tvHourlyTemp = itemView.findViewById(R.id.tvHourlyTemp);
            tvHourlyPop = itemView.findViewById(R.id.tvHourlyPop);
            imgHourlyIcon = itemView.findViewById(R.id.imgHourlyIcon);
            cardHourlyView = itemView.findViewById(R.id.cardHourlyView);
        }
    }

    private boolean isCelsius() { return true; }
}
