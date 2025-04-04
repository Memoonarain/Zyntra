package com.example.zyntra;

import android.app.AlertDialog;
import android.content.Context;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoadingDialogue {
    Context context;
    private AlertDialog loadingDialog;
    public LoadingDialogue(Context context){
        this.context = context;
    }
    public void showLoadingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false); // disable dismiss on outside touch

        // Create a LinearLayout with a ProgressBar and TextView
        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setPadding(40, 40, 40, 40);
        layout.setGravity(Gravity.CENTER_VERTICAL);

        ProgressBar progressBar = new ProgressBar(context);
        progressBar.setIndeterminate(true);

        TextView loadingText = new TextView(context);
        loadingText.setText("Loading...");
        loadingText.setTextSize(18);
        loadingText.setPadding(30, 0, 0, 0);

        layout.addView(progressBar);
        layout.addView(loadingText);

        builder.setView(layout);
        loadingDialog = builder.create();
        loadingDialog.show();
    }
    public void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
