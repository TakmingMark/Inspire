package com.example.markwang.inspire;

import android.view.View;

public class MainClickListener implements View.OnClickListener {
    MainActivity mainActivity;

    public MainClickListener(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onClick(View v) {
        switch (v.getTag().toString()) {
            case "photoButton":
                mainActivity.convertPhotoActivity();
                break;

        }
    }
}
