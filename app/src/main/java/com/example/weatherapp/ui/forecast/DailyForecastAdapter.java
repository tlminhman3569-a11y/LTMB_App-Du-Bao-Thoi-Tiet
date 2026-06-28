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

public class DailyForecastAdapter extends RecyclerView.Adapter<DailyForecastAdapter.DailyViewHolder> {

    private List<ForecastItem> dailyList = new ArrayList<>();

    public void setData(List<ForecastItem> list) {
        this.dailyList = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DailyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_daily_forecast, parent, false);
        return new DailyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DailyViewHolder holder, int position) {
        ForecastItem item = dailyList.get(position);
        // Nen kinh mo trang (Glassmorphism) - lay mau tu Design System
        holder.cardDailyView.setCardBackgroundColor(
                androidx.core.content.ContextCompat.getColor(holder.itemView.getContext(), R.color.colorGlassCard));

        // Hiển thị ngày (ví dụ: "2025-06-28 12:00:00" -> "28/06")
        String dtTxt = item.getDtTxt();
        if (dtTxt != null && dtTxt.length() >= 10) {
            String date = dtTxt.substring(8, 10) + "/" + dtTxt.substring(5, 7);
            
            // Lấy thứ tương ứng từ ngày (Tùy chọn hiển thị nâng cao)
            try {
                java.text.SimpleDateFormat format1 = new java.text.SimpleDateFormat("yyyy-MM-dd");
                java.util.Date dt = format1.parse(dtTxt.substring(0, 10));
                java.text.DateFormat format2 = new java.text.SimpleDateFormat("EEEE", new java.util.Locale("vi"));
                String dayOfWeek = format2.format(dt);
                holder.tvDailyDate.setText(dayOfWeek + "\n" + date);
            } catch (Exception e) {
                holder.tvDailyDate.setText(date);
            }
        }

        // Hiển thị nhiệt độ min/max
        if (item.getMain() != null) {
            int tempMin = (int) Math.round(item.getMain().getTempMin());
            int tempMax = (int) Math.round(item.getMain().getTempMax());
            String unit = isCelsius() ? "°" : "°F";
            holder.tvDailyTemp.setText(tempMin + unit + "/" + tempMax + unit);
        }

        // Hiển thị mô tả và load icon bằng Glide
        if (item.getWeather() != null && !item.getWeather().isEmpty()) {
            String desc = item.getWeather().get(0).getDescription();
            if (desc != null && !desc.isEmpty()) {
                desc = desc.substring(0, 1).toUpperCase() + desc.substring(1);
            }
            
            // Them phan tram kha nang co mua neu > 0
            double pop = item.getPop();
            if (pop > 0) {
                int popPercent = (int) Math.round(pop * 100);
                desc = desc + " (" + popPercent + "%)";
            }
            
            holder.tvDailyDesc.setText(desc);

            String iconCode = item.getWeather().get(0).getIcon();
            String iconUrl = "https://openweathermap.org/img/wn/" + iconCode + "@4x.png";
            Glide.with(holder.itemView.getContext())
                    .load(iconUrl)
                    .placeholder(android.R.drawable.ic_menu_report_image)
                    .into(holder.imgDailyIcon);
        }
    }

    @Override
    public int getItemCount() {
        return dailyList.size();
    }

    static class DailyViewHolder extends RecyclerView.ViewHolder {
        TextView tvDailyDate, tvDailyTemp, tvDailyDesc;
        ImageView imgDailyIcon;
        com.google.android.material.card.MaterialCardView cardDailyView;

        public DailyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDailyDate = itemView.findViewById(R.id.tvDailyDate);
            tvDailyTemp = itemView.findViewById(R.id.tvDailyTemp);
            tvDailyDesc = itemView.findViewById(R.id.tvDailyDesc);
            imgDailyIcon = itemView.findViewById(R.id.imgDailyIcon);
            cardDailyView = itemView.findViewById(R.id.cardDailyView);
        }
    }

    private boolean isCelsius() { return true; }
}
