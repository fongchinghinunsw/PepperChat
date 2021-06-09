package com.stephenfong.pepperchat.view.activities.display;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.stephenfong.pepperchat.R;
import com.stephenfong.pepperchat.common.Common;
import com.stephenfong.pepperchat.databinding.ActivityViewImageBinding;

public class ViewImageActivity extends AppCompatActivity {

    private ActivityViewImageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view_image);

        binding.imageView.setImageBitmap(Common.IMAGE_BITMAP);
    }
}