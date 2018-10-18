package com.example.markwang.inspire;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class PhotoActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        Log.d("test", "PhotoActivity: this");
    }
}
