package com.stephenfong.pepperchat.view.activities.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.jsibbold.zoomage.ZoomageView;
import com.stephenfong.pepperchat.R;

public class DialogReviewSendImage {
    private Context context;
    private Dialog dialog;
    private Bitmap bitmap;
    ZoomageView image;
    FloatingActionButton btnSend;

    public DialogReviewSendImage(Context context, Bitmap bitmap) {
        this.context = context;
        this.bitmap = bitmap;
        this.dialog = new Dialog(context);

        initialize();
    }

    public void initialize() {
        dialog.requestWindowFeature(Window.FEATURE_ACTION_BAR); // enable the action bar
        dialog.setContentView(R.layout.activity_review_send_image);

        // make the dialog the focus of the screen, without this line the background will be visible.
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setCancelable(true);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        // set all predefined attributes and then add width & height
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setAttributes(lp);

        image = dialog.findViewById(R.id.image_view);
        btnSend = dialog.findViewById(R.id.btn_send);
    }

    public void show(OnCallBack onCallBack) {
        dialog.show();
        image.setImageBitmap(bitmap);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCallBack.onButtonSendClick();
                dialog.dismiss();
            }
        });
    }

    public interface OnCallBack {
        void onButtonSendClick();
    }
}
