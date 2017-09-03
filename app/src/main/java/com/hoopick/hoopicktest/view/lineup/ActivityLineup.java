package com.hoopick.hoopicktest.view.lineup;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.hoopick.hoopicktest.R;
import com.hoopick.hoopicktest.control.action.HpActionLineup;
import com.hoopick.hoopicktest.data.util.HpGenUtil;
import com.hoopick.hoopicktest.view.main.ActivityMain;
import com.hoopick.hoopicktest.view.playball.ActivityPlayBall;
import com.hoopick.hoopicktest.view.playball.ActivityTest;

import static android.R.attr.action;

/**
 * Created by junhyeok on 2017. 8. 14..
 */

public class ActivityLineup extends AppCompatActivity {

    private final int REQUEST_CODE_START_ACTIVITY = 1;
    private AlertDialog mAlertDialog = null;
    private TextView mTextHomeTeam = null;
    private TextView mTextAwayTeam = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lineup);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Lineup");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mTextHomeTeam = (TextView)findViewById(R.id.textHomeTeam);
        mTextAwayTeam = (TextView)findViewById(R.id.textAwayTeam);

        findViewById(R.id.buttonDummy).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(mTextHomeTeam.getText().toString().trim().equalsIgnoreCase("")){
                    mTextHomeTeam.setText("한국");
                }
                if(mTextAwayTeam.getText().toString().trim().equalsIgnoreCase("")){
                    mTextAwayTeam.setText("미국");
                }

            }
        });
    }

    @Override
    public void onBackPressed() {
        mAlertDialog = new AlertDialog.Builder(this)
                .setTitle("Hoopick")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface alertDialog, int arg1) {

                        Intent lIntent = new Intent(ActivityLineup.this, ActivityMain.class);
                        lIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ActivityLineup.this.startActivityForResult(lIntent, REQUEST_CODE_START_ACTIVITY);

                        ActivityLineup.this.finish();

                    }
                }).create();

        mAlertDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_lineup, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                // Toast.makeText(getApplicationContext(),"Back button clicked", Toast.LENGTH_SHORT).show();
                onBackPressed();
                break;

            case R.id.action_jumpball:
                jumpBall();
                break;

            case R.id.action_jumpballtest:
                jumpBallTest();
                break;

            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_START_ACTIVITY) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user came back from the Enable Location Activity
                // Dismiss the Dialog
                if ((null != mAlertDialog) && mAlertDialog.isShowing()) {
                    mAlertDialog.dismiss();
                }
            }
        }

    }

    private boolean validateJumpBall() {

        TextView[] lArrayEdit = {mTextHomeTeam, mTextAwayTeam};

        for (TextView nText : lArrayEdit) {
            if (nText.getText().toString().trim().equalsIgnoreCase("")) {
                nText.requestFocus();
                return false;
            }
        }

        return true;
    }


    public void onClickBackground(View view) {

        EditText editDummy = (EditText)findViewById(R.id.editDummy);

        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(editDummy.getWindowToken(), 0);

        findViewById(R.id.layoutLineupBox).requestFocus();
    }

    private void jumpBall() {

        if (false == validateJumpBall() ) {
            return;
        }

        try {

            String lGameTimeMin = "10";
            String lShotTimeSec = "24";

            new HpActionLineup(ActivityLineup.this
                    , HpGenUtil.genGameId()
                    , "2016-2017"
                    , mTextHomeTeam.getText().toString()
                    , mTextAwayTeam.getText().toString()
                    , lGameTimeMin
                    , lShotTimeSec
            ).execute();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Intent lIntent = new Intent(ActivityLineup.this, ActivityPlayBall.class);
        lIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ActivityLineup.this.startActivityForResult(lIntent, REQUEST_CODE_START_ACTIVITY);

        ActivityLineup.this.finish();

//        Intent tIntent = new Intent(ActivityLineup.this, ActivityTest.class);
//        tIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        ActivityLineup.this.startActivityForResult(lIntent, REQUEST_CODE_START_ACTIVITY);
//
//        ActivityLineup.this.finish();

    }

    private void jumpBallTest() {

        if (false == validateJumpBall() ) {
            return;
        }

        try {

            String lGameTimeMin = "10";
            String lShotTimeSec = "24";

            new HpActionLineup(ActivityLineup.this
                    , HpGenUtil.genGameId()
                    , "2016-2017"
                    , mTextHomeTeam.getText().toString()
                    , mTextAwayTeam.getText().toString()
                    , lGameTimeMin
                    , lShotTimeSec
            ).execute();

        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Intent lIntent = new Intent(ActivityLineup.this, ActivityTest.class);
        lIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        ActivityLineup.this.startActivityForResult(lIntent, REQUEST_CODE_START_ACTIVITY);

        ActivityLineup.this.finish();

//        Intent tIntent = new Intent(ActivityLineup.this, ActivityTest.class);
//        tIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        ActivityLineup.this.startActivityForResult(lIntent, REQUEST_CODE_START_ACTIVITY);
//
//        ActivityLineup.this.finish();

    }

}