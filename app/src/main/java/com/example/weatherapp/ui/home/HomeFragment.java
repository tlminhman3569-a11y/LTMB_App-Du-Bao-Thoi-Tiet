package com.example.weatherapp.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.example.weatherapp.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class HomeFragment extends Fragment {

    private FusedLocationProviderClient fusedLocationClient;

    // Bộ phóng kích hoạt hộp thoại xin quyền của Android
    private final ActivityResultLauncher<String[]> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
                Boolean coarseLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false);

                if (fineLocationGranted != null && fineLocationGranted) {
                    // Người dùng đã cho phép quyền chính xác
                    getDeviceLocation();
                } else if (coarseLocationGranted != null && coarseLocationGranted) {
                    // Người dùng chỉ cho phép quyền tương đối
                    getDeviceLocation();
                } else {
                    // Người dùng từ chối cấp quyền
                    Toast.makeText(getContext(), "Ứng dụng cần quyền vị trí để dự báo thời tiết nơi bạn ở!", Toast.LENGTH_LONG).show();
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_current_weather, container, false);

        // Khởi tạo công cụ định vị của Google
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());

        // Kiểm tra quyền ngay khi mở màn hình
        checkLocationPermissions();

        return view;
    }

    // Hàm kiểm tra xem ứng dụng đã được cấp quyền vị trí chưa
    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Đã có quyền, tiến hành lấy vị trí luôn
            getDeviceLocation();
        } else {
            // Chưa có quyền, hiển thị hộp thoại xin quyền
            locationPermissionLauncher.launch(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            });
        }
    }

    // Hàm trực tiếp lấy tọa độ GPS của thiết bị
    private void getDeviceLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
                if (location != null) {
                    double latitude = location.getLatitude();   // Vĩ độ
                    double longitude = location.getLongitude(); // Kinh độ

                    // In thử ra màn hình Toast để kiểm tra xem đã lấy được tọa độ chưa
                    Toast.makeText(getContext(), "GPS: " + latitude + ", " + longitude, Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getContext(), "Không thể lấy được vị trí hiện tại. Hãy bật GPS trên máy!", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    // Phần giả lập trước cho UC 5 (Setting)
    private boolean isCelsius() { return true; }
    private boolean isKmH() { return true; }
}