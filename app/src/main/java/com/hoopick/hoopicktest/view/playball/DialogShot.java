package com.hoopick.hoopicktest.view.playball;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.hoopick.hoopicktest.R;

/**
 * Created by junhyeok on 2017. 8. 17..
 */

public class DialogShot extends Dialog implements android.view.View.OnClickListener{

    public static final int SHOT_MADE = 1;
    public static final int SHOT_MISS = 2;
    public static final int SHOT_FOUL = 3;

    public Activity c;
    public Dialog d;

    private OnClickShotListner mOnClickShotListner = null;

    private String mTitle = "";
    private String mDesc = "";

    public DialogShot(Context context, String aTitle, String aDesc) {
        super(context);
        mTitle = aTitle;
        mDesc = aDesc;
    }

    public String getDesc() {
        return ((TextView)findViewById(R.id.text_desc)).getText().toString();
    }
    public void setDesc(String aDesc) {
        ((TextView)findViewById(R.id.text_desc)).setText(aDesc);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_shot);

        findViewById(R.id.button_shot_made).setOnClickListener(this);
        findViewById(R.id.button_shot_miss).setOnClickListener(this);
        findViewById(R.id.button_shot_foul).setOnClickListener(this);
        findViewById(R.id.text_shot_foul).setVisibility(View.GONE);

        ((TextView)findViewById(R.id.text_title)).setText(mTitle);
        ((TextView)findViewById(R.id.text_desc)).setText(mDesc);

    }

    public void setOnClickShotListner(OnClickShotListner aOnClickShotListner) {
        mOnClickShotListner = aOnClickShotListner;
    }

    public static abstract class OnClickShotListner {
        abstract void OnClickShot(int aMenuItem);
    }

    public void toggleShotFoul() {

        if (findViewById(R.id.text_shot_foul).getVisibility() == View.VISIBLE) {
            findViewById(R.id.text_shot_foul).setVisibility(View.GONE);
        }
        else {
            findViewById(R.id.text_shot_foul).setVisibility(View.VISIBLE);
            ((ImageButton)findViewById(R.id.button_shot_foul)).setEnabled(false);
        }

    }

    public boolean isShotFoul() {
        return (findViewById(R.id.text_shot_foul).getVisibility() == View.VISIBLE);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.button_shot_made: {
                mOnClickShotListner.OnClickShot(SHOT_MADE);
                break;
            }

            case R.id.button_shot_miss: {
                mOnClickShotListner.OnClickShot(SHOT_MISS);
                break;
            }

            case R.id.button_shot_foul: {
                // whistle sound
                MediaPlayer lPlayer = MediaPlayer.create(DialogShot.this.getContext(), R.raw.buzzer);
                lPlayer.start();
                mOnClickShotListner.OnClickShot(SHOT_FOUL);
                break;
            }

        }


    }

}
