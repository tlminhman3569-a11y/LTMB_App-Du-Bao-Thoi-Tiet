package com.example.weatherapp.ui.forecast;

import android.view.View;
import android.widget.EdgeEffect;
import androidx.annotation.NonNull;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import androidx.recyclerview.widget.RecyclerView;

// Cấu hình hiệu ứng kéo giãn đàn hồi khi danh sách cuộn chạm biên giới hạn
public class SpringyEdgeEffectFactory extends RecyclerView.EdgeEffectFactory {

    // Tỷ lệ dịch chuyển của danh sách khi kéo lố hoặc vuốt theo quán tính
    private static final float OVERSCROLL_TRANSLATION_MAGNITUDE = 0.2f;
    private static final float FLING_TRANSLATION_MAGNITUDE = 0.2f;

    @NonNull
    @Override
    protected EdgeEffect createEdgeEffect(@NonNull RecyclerView view, int direction) {
        return new EdgeEffect(view.getContext()) {
            private SpringAnimation translationAnim = null;

            @Override
            public void onPull(float deltaDistance, float displacement) {
                super.onPull(deltaDistance, displacement);
                handlePull(deltaDistance);
            }

            @Override
            public void onPull(float deltaDistance) {
                super.onPull(deltaDistance);
                handlePull(deltaDistance);
            }

            // Dịch chuyển danh sách tương ứng theo khoảng cách kéo tay của người dùng
            private void handlePull(float deltaDistance) {
                int sign = (direction == DIRECTION_RIGHT) ? -1 : 1;
                float translationXDelta = sign * view.getWidth() * deltaDistance * OVERSCROLL_TRANSLATION_MAGNITUDE;
                view.setTranslationX(view.getTranslationX() + translationXDelta);

                if (translationAnim != null) {
                    translationAnim.cancel();
                }
            }

            // Kích hoạt hoạt ảnh lò xo kéo danh sách về lại vị trí cũ khi thả tay
            @Override
            public void onRelease() {
                super.onRelease();
                if (view.getTranslationX() != 0) {
                    getOrCreateAnimation().start();
                }
            }

            // Tạo hoạt lực nảy quán tính dựa trên vận tốc vuốt khi danh sách đập vào cạnh biên
            @Override
            public void onAbsorb(int velocity) {
                super.onAbsorb(velocity);
                int sign = (direction == DIRECTION_RIGHT) ? -1 : 1;
                float translationXDelta = sign * velocity * FLING_TRANSLATION_MAGNITUDE;

                SpringAnimation anim = getOrCreateAnimation();
                anim.setStartVelocity(translationXDelta);
                anim.start();
            }

            // Khởi tạo hoặc tái sử dụng đối tượng SpringAnimation điều khiển trục X
            private SpringAnimation getOrCreateAnimation() {
                if (translationAnim == null) {
                    translationAnim = new SpringAnimation(view, SpringAnimation.TRANSLATION_X, 0);
                    translationAnim.getSpring().setDampingRatio(SpringForce.DAMPING_RATIO_MEDIUM_BOUNCY);
                    translationAnim.getSpring().setStiffness(SpringForce.STIFFNESS_LOW);
                }
                return translationAnim;
            }
        };
    }
}
