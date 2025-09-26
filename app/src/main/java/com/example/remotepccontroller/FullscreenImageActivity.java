package com.example.remotepccontroller;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap;
import java.io.File;

public class FullscreenImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fullscreen_image);

        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ImageView imageView = findViewById(R.id.fullImage);
        String path = getIntent().getStringExtra("image_path");
        if (path != null) {
            File imgFile = new File(path);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.setImageBitmap(bitmap);
            }
        }

        // Закрытие Activity при нажатии на изображение
        imageView.setOnClickListener(v -> finish());
    }
}
