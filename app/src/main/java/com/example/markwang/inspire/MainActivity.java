package com.example.markwang.inspire;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.hitomi.cmlibrary.OnMenuSelectedListener;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        Button button = (Button) findViewById(R.id.photoButton);
        MainClickListener mainClickListener = new MainClickListener(this);
        button.setOnClickListener(mainClickListener);

        CircleFoatingMenu circleFoatingMenu = (CircleFoatingMenu) findViewById(R.id.circleFoatingMenu);

        circleFoatingMenu.setMainMenu(Color.parseColor("#A916ED"), R.drawable.circle_enter, R.drawable.circle_cancel)
                .addSubMenu(Color.parseColor("#ED1616"), R.drawable.circle_news)
                .addSubMenu(Color.parseColor("#EDED16"), R.drawable.circle_photo)
                .addSubMenu(Color.parseColor("#47ED16"), R.drawable.circle_donate)
                .addSubMenu(Color.parseColor("#16EDE6"), R.drawable.circle_help)
                .addSubMenu(Color.parseColor("#163AED"), R.drawable.circle_activity);

        circleFoatingMenu.setOnMenuSelectedListener(new OnMenuSelectedListener() {
            @Override
            public void onMenuSelected(int i) {
                Log.e("onMenuSelected", i + "");
            }
        });

    }

    public void convertPhotoActivity() {
        Log.d("test", "convertPhotoActivity: this");
        Intent intent = new Intent();
        intent.setClass(MainActivity.this, PhotoActivity.class);
        startActivity(intent);
        this.finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
