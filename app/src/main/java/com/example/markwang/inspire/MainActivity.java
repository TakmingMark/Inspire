package com.example.markwang.inspire;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);

        Button button = (Button) findViewById(R.id.photoButton);
        MainClickListener mainClickListener = new MainClickListener(this);
        button.setOnClickListener(mainClickListener);

        CircleFoatingMenu circleFoatingMenu= (CircleFoatingMenu) findViewById(R.id.circleFoatingMenu);

        circleFoatingMenu.setMainMenu(Color.parseColor("#258CFF"), R.drawable.main_1, R.drawable.circle_1)
        .addSubMenu(Color.parseColor("#258CFF"),R.drawable.circle_1)
                .addSubMenu(Color.parseColor("#258CFF"),R.drawable.circle_1);

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
