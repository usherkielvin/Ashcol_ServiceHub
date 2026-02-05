package app.hub;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

public class GradientTaglineTextView extends AppCompatTextView {

    private LinearGradient mLinearGradient;
    private final float glowRadius = 12f;
    private final int glowColor = 0xFF00E5FF; // Cyan glow

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
        // Essential for shadow/glow to work on some devices
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        setShadowLayer(glowRadius, 0f, 0f, glowColor);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (h > 0) {
            // White to Light Cyan gradient
            mLinearGradient = new LinearGradient(0, 0, 0, h,
                    new int[]{0xFFFFFFFF, 0xFFB2EBF2, 0xFFFFFFFF}, 
                    new float[]{0f, 0.5f, 1f}, Shader.TileMode.CLAMP);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mLinearGradient != null) {
            getPaint().setShader(mLinearGradient);
        }
        super.onDraw(canvas);
    }
}
