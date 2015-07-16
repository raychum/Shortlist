package com.openrice.android.shortlist;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {

    private int count = 0;
    private Button buttonAdd;
    private Button buttonAdd0;
    private final String[] images = {"http://d.ibtimes.co.uk/en/full/1409516/how-update-nexus-5-official-android-5-0-lollipop-build-lrx21o-via-factory-image.jpg",
            "http://www.menucool.com/slider/jsImgSlider/images/image-slider-1.jpg",
            "http://www.starplugins.com/sites/starplugins/images/jetzoom/large/image2.jpg",
            "https://s.yimg.com/cd/resizer/2.0/FIT_TO_WIDTH-w500/63b0da5cfb829f66e960b6d5e8f4279a26adc641.jpg",
            "http://www.jssor.com/img/photography/005.jpg"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonAdd = (Button) findViewById(R.id.add);
        buttonAdd0 = (Button) findViewById(R.id.add0);
        buttonAdd.setText("Add " + count);
        buttonAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShortlistManager.getInstance().addShortlistItem(MainActivity.this, count, images[count], false, (ImageView) findViewById(R.id.screenview));
                count++;
                if (count > 4) {
                    count = 0;
                }
                buttonAdd.setText("Add " + count);
            }
        });
        buttonAdd0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShortlistManager.getInstance().addShortlistItem(MainActivity.this, 0, images[0], false, (ImageView) findViewById(R.id.screenview));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(MainActivity.this.getApplicationContext(), ShortlistService.class));
    }
}
