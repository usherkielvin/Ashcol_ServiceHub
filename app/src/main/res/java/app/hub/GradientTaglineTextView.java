package app.hub;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

public class GradientTaglineTextView extends AppCompatTextView {

    public GradientTaglineTextView(Context context) {
        super(context);
        init();
    }

    public GradientTaglineTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public GradientTaglineTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        // Apply shadow to mimic the glow in the image
        setShadowLayer(8f, 0f, 0f, 0xFF81D4FA); // Light blue glow
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            getPaint().setShader(new LinearGradient(0, 0, 0, getHeight(),
                    new int[]{0xFFFFFFFF, 0xFFB3E5FC}, // White to Light Blue gradient
                    null, Shader.TileMode.CLAMP));
        }
    }
}
