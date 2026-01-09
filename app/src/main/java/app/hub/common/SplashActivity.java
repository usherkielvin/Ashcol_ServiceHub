package app.hub.common;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import app.hub.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        VideoView videoView = findViewById(R.id.videoView);
        
        // Path to your video in res/raw
        String path = "android.resource://" + getPackageName() + "/" + R.raw.splash_video;
        videoView.setVideoURI(Uri.parse(path));

        videoView.setOnCompletionListener(mp -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            // Apply transition: next activity slides in from top, this one slides out to bottom
            overridePendingTransition(R.anim.slide_in_top, R.anim.slide_out_bottom);
            finish();
        });

        videoView.start();
    }
}
