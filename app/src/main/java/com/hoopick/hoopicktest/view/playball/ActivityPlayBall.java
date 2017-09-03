package com.hoopick.hoopicktest.view.playball;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnTouchListener;

import com.hoopick.hoopicktest.R;
import com.hoopick.hoopicktest.control.action.HpActionBase;
import com.hoopick.hoopicktest.control.action.HpActionFoul;
import com.hoopick.hoopicktest.control.action.HpActionFreeThrow;
import com.hoopick.hoopicktest.control.action.HpActionGrabbedTheBall;
import com.hoopick.hoopicktest.control.action.HpActionOutOfBound;
import com.hoopick.hoopicktest.control.action.HpActionShot;
import com.hoopick.hoopicktest.control.action.HpActionViolation;
import com.hoopick.hoopicktest.control.action.HpActionWhistle;
import com.hoopick.hoopicktest.control.bluetooth.HpBluetoothListener;
import com.hoopick.hoopicktest.control.bluetooth.HpBluetoothManager;
import com.hoopick.hoopicktest.control.bluetooth.HpBluetoothSerialService;
import com.hoopick.hoopicktest.control.game.HpGameEventListener;
import com.hoopick.hoopicktest.control.game.HpGameManager;
import com.hoopick.hoopicktest.control.game.team.HpTeam;
import com.hoopick.hoopicktest.control.game.team.player.HpPlayer;
import com.hoopick.hoopicktest.control.game.team.player.HpPlayerRunnable;
import com.hoopick.hoopicktest.control.game.timer.HpGameTimer;
import com.hoopick.hoopicktest.control.game.timer.HpGameTimerListener;
import com.hoopick.hoopicktest.control.util.CoordUtil;
import com.hoopick.hoopicktest.control.util.HpTimeUtil;
import com.hoopick.hoopicktest.data.HpDataManager;
import com.hoopick.hoopicktest.data.model.HpState;
import com.hoopick.hoopicktest.view.lineup.ActivityLineup;
import com.hoopick.hoopicktest.view.main.ActivityMain;

import android.app.Fragment;

import org.w3c.dom.Text;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by junhyeok on 2017. 8. 16..
 */

public class ActivityPlayBall extends AppCompatActivity implements HpGameEventListener,HpBluetoothListener{

    public static final String TAG = "ActivityPlayBall";
    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    public static final int MAX_TEAM = 2;
    public static final int MAX_PLAYER = 1;

    private AlertDialog mAlertDialog = null;
    private final int REQUEST_CODE_START_ACTIVITY = 1;

    private HorizontalScrollView mScrollCourt;
    private ImageView mImageCourt;
    private ImageView mImageEvent;
    private TextView mTextDebugLeft;
    private TextView mTextDebugRight;
    private TextView mTextEndQuater;

    private View[][] mViewPlayer = new View[MAX_TEAM][MAX_PLAYER];


    private Context getContext() {
        return ActivityPlayBall.this;
    }
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playball);

        getSupportActionBar().hide();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        bindLayout();

        initLayout();
    }



    @Override
    public void onBackPressed() {
        mAlertDialog = new AlertDialog.Builder(this)
                .setTitle("Hoopick")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface alertDialog, int arg1) {

                        Intent lIntent = new Intent(ActivityPlayBall.this, ActivityLineup.class);
                        lIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        ActivityPlayBall.this.startActivityForResult(lIntent, REQUEST_CODE_START_ACTIVITY);
                        ActivityPlayBall.this.finish();

                    }
                }).create();

        mAlertDialog.show();


    }

    private void bindLayout(){
        mTextEndQuater = (TextView)findViewById(R.id.quarterText);
        mImageCourt = (ImageView) findViewById(R.id.image_court);
        mImageEvent = (ImageView) findViewById(R.id.image_event);
        mScrollCourt = (HorizontalScrollView) findViewById(R.id.scroll_Court);

        mTextDebugLeft = (TextView) findViewById(R.id.textDebugLeft);
        mTextDebugRight = (TextView) findViewById(R.id.textDebugRight);

        mImageCourt.setOnTouchListener(mOnTouchCourt);
        mImageCourt.setOnLongClickListener(mOnLongClickCourt);

        // HomePlayer1
        mViewPlayer[0][0] = findViewById(R.id.layout_home_player1);
        mViewPlayer[0][0].setOnClickListener(mOnClickPlayer);
        mViewPlayer[0][0].setTag(HpGameManager.get().findPlayerBySlot(0, 0));



        // AwayPlayer1
        mViewPlayer[1][0] = findViewById(R.id.layout_away_player1);
        mViewPlayer[1][0].setOnClickListener(mOnClickPlayer);
        mViewPlayer[1][0].setTag(HpGameManager.get().findPlayerBySlot(1, 0));


        // whistle
        findViewById(R.id.button_whistle).setOnClickListener(mOnClickWhistle);

        // shot
        findViewById(R.id.button_undo).setOnClickListener(mOnClickUndo);

        HpGameManager.get().setGameEventListener(this);

        HpBluetoothManager.get().setListener(this);



    }

    private  void initLayout(){
        TextView lTextGameTime = (TextView) findViewById(R.id.text_game_time);
        lTextGameTime.setText(String.format("%d:00", HpGameManager.get().getTimer().getmMaxGameClockSec()/60));

        TextView lTextShotTime = (TextView) findViewById(R.id.text_shot_time);
        lTextShotTime.setText(String.format("%d", HpGameManager.get().getTimer().getmMaxShotClockSec()));

        // Score
        ((TextView)findViewById(R.id.text_home_score)).setText("0");
        ((TextView)findViewById(R.id.text_away_score)).setText("0");

        // Foul
        ((TextView)findViewById(R.id.text_home_foul)).setText("0");
        ((TextView)findViewById(R.id.text_away_foul)).setText("0");

        // set team name
        ((TextView) findViewById(R.id.text_home_team_name)).setText(HpGameManager.get().getTeamHome().getName());
        ((TextView) findViewById(R.id.text_away_team_name)).setText(HpGameManager.get().getTeamAway().getName());

        // set player name, number
        initPlayer();


    }


    @Override
    public void onBluetoothStateChange(int aState) {

        switch (aState) {

            case HpBluetoothSerialService.STATE_NONE:
                Snackbar.make(mImageCourt, "Bluetooth NONE", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                break;

            case HpBluetoothSerialService.STATE_LISTEN:
                Snackbar.make(mImageCourt, "Bluetooth LISTEN", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                break;

            case HpBluetoothSerialService.STATE_CONNECTING:
                Snackbar.make(mImageCourt, "Bluetooth CONNECTING...", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                break;

            case HpBluetoothSerialService.STATE_CONNECTED:
                Snackbar.make(mImageCourt, "Bluetooth CONNECTED", Snackbar.LENGTH_SHORT).setAction("Action", null).show();
                break;

        }

    }

    private interface PlayerView {
        void run(HpPlayer aPlayer, View aViewPlayer,TextView aTextName,ImageView aImageBall);

    }

    private void each(PlayerView aPlayerView) {

        for (int n=0; n<MAX_TEAM; n++) {
            for (int k = 0; k<MAX_PLAYER; k++) {

                View lViewPlayer = mViewPlayer[n][k];
                HpPlayer lPlayer = (HpPlayer) lViewPlayer.getTag();
                TextView lTextName = (TextView) lViewPlayer.findViewById(R.id.text_player_name);
                ImageView lImageBall = (ImageView) lViewPlayer.findViewById(R.id.image_ball);

                aPlayerView.run(lPlayer, lViewPlayer,lTextName, lImageBall);
            }
        }

    }
    private void initPlayer() {

        each(new PlayerView() {
            @Override
            public void run(HpPlayer aPlayer, View aViewPlayer,TextView aTextName, ImageView aImageBall) {
                aImageBall.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        initPlayer();
        // bluetooth
        HpBluetoothManager.get().resume();

    }

    private void doShot(final int aPointScore, final int aPosX, final int aPosY, final int aDistance) {

        final HpPlayer lPlayerShot = HpGameManager.get().getBall().getGrabbedPlayer();

        String lDesc = String.format("%s : %d point", lPlayerShot.getName(), aPointScore);

        final DialogShot lShotDialog = new DialogShot(getContext(), "Shot", lDesc);

        lShotDialog.setOnClickShotListner(new DialogShot.OnClickShotListner() {

            private String mPlayerShotFoul = "";

            @Override
            void OnClickShot(int aMenuItem) {

                switch (aMenuItem) {

                    case DialogShot.SHOT_MADE:

                        lShotDialog.dismiss();

                        try {
                            new HpActionShot(getContext(), HpActionShot.SHOT_MADE, aPointScore, aPosX, aPosY, aDistance).execute();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            Log.e("", e.toString());
                        }

                        // 추가 자유투
                        if (true == lShotDialog.isShotFoul()) {

                            final Queue<Runnable> lFreeThrowAction = new ArrayDeque<Runnable>();

                            for (int n=0; n<1; n++) {

                                final int count = n + 1;

                                lFreeThrowAction.add(new Runnable() {
                                    @Override
                                    public void run() {

                                        String lDescFreethrow = String.format("%s : Additional Freethrow Shot %d / %d", lPlayerShot.getName(), count, aPointScore);
                                        final DialogFreeThrow lFreethrowDialog = new DialogFreeThrow(getContext(), "Freethrow", lDescFreethrow);

                                        lFreethrowDialog.setOnClickFreethrowListner(new DialogFreeThrow.OnClickFreethrowListner() {
                                            @Override
                                            void OnClickShot(int aMenuItem) {

                                                String lMadeOrMiss = "";
                                                String lPlayerFreeThrow = lPlayerShot.getName();
                                                String lPlayerShotFoul = mPlayerShotFoul;

                                                switch (aMenuItem) {
                                                    case DialogFreeThrow.FREE_THROW_MADE:
                                                        lFreethrowDialog.dismiss();
                                                        lMadeOrMiss = HpActionFreeThrow.FREE_THROW_MADE;
                                                        break;

                                                    case DialogFreeThrow.FREE_THROW_MISS:
                                                        lFreethrowDialog.dismiss();
                                                        lMadeOrMiss = HpActionFreeThrow.FREE_THROW_MISS;
                                                        break;
                                                }

                                                try {
                                                    new HpActionFreeThrow(getContext(), HpActionFreeThrow.FREE_THROW_TYPE_SHOT_FOUL, lMadeOrMiss, lPlayerFreeThrow, lPlayerShotFoul, 0).execute();
                                                }
                                                catch (Exception e) {
                                                    e.printStackTrace();
                                                    Log.e("", e.toString());
                                                }


                                                new Handler().post(new Runnable() {
                                                    @Override
                                                    public void run() {

                                                        if (lFreeThrowAction.size() > 0) {
                                                            Runnable lRunFreeThrow = lFreeThrowAction.poll();
                                                            lRunFreeThrow.run();
                                                        }

                                                    }
                                                });
                                            }
                                        });

                                        lFreethrowDialog.show();
                                    }
                                });

                            }


                            new Handler().post(new Runnable() {
                                @Override
                                public void run() {

                                    if (lFreeThrowAction.size() > 0) {
                                        Runnable lRunFreeThrow = lFreeThrowAction.poll();
                                        lRunFreeThrow.run();
                                    }

                                }
                            });


                        }


                        break;

                    case DialogShot.SHOT_MISS:

                        lShotDialog.dismiss();

                        // Miss & Shot Foul 이면 ActionShot 무시
                        if (true == lShotDialog.isShotFoul()) {
                            // lPlayerShot's Free Throw
                            lShotDialog.dismiss();

                            // 추가 자유투
                            if (true == lShotDialog.isShotFoul()) {

                                final Queue<Runnable> lFreeThrowAction = new ArrayDeque<Runnable>();

                                for (int n=0; n<aPointScore; n++) {

                                    final int count = n + 1;

                                    lFreeThrowAction.add(new Runnable() {
                                        @Override
                                        public void run() {

                                            String lDescFreethrow = String.format("%s : Additional Freethrow Shot %d / %d", lPlayerShot.getName(), count, aPointScore);
                                            final DialogFreeThrow lFreethrowDialog = new DialogFreeThrow(getContext(), "Freethrow", lDescFreethrow);

                                            lFreethrowDialog.setOnClickFreethrowListner(new DialogFreeThrow.OnClickFreethrowListner() {
                                                @Override
                                                void OnClickShot(int aMenuItem) {

                                                    String lMadeOrMiss = "";
                                                    String lPlayerFreeThrow = lPlayerShot.getName();
                                                    String lPlayerShotFoul = mPlayerShotFoul;

                                                    switch (aMenuItem) {
                                                        case DialogFreeThrow.FREE_THROW_MADE:
                                                            lFreethrowDialog.dismiss();
                                                            lMadeOrMiss = HpActionFreeThrow.FREE_THROW_MADE;
                                                            break;

                                                        case DialogFreeThrow.FREE_THROW_MISS:
                                                            lFreethrowDialog.dismiss();
                                                            lMadeOrMiss = HpActionFreeThrow.FREE_THROW_MISS;
                                                            break;
                                                    }

                                                    try {
                                                        new HpActionFreeThrow(getContext(), HpActionFreeThrow.FREE_THROW_TYPE_SHOT_FOUL, lMadeOrMiss, lPlayerFreeThrow, lPlayerShotFoul, 0).execute();
                                                    }
                                                    catch (Exception e) {
                                                        e.printStackTrace();
                                                        Log.e("", e.toString());
                                                    }


                                                    new Handler().post(new Runnable() {
                                                        @Override
                                                        public void run() {

                                                            if (lFreeThrowAction.size() > 0) {
                                                                Runnable lRunFreeThrow = lFreeThrowAction.poll();
                                                                lRunFreeThrow.run();
                                                            }

                                                        }
                                                    });
                                                }
                                            });

                                            lFreethrowDialog.show();
                                        }
                                    });

                                }


                                new Handler().post(new Runnable() {
                                    @Override
                                    public void run() {

                                        if (lFreeThrowAction.size() > 0) {
                                            Runnable lRunFreeThrow = lFreeThrowAction.poll();
                                            lRunFreeThrow.run();
                                        }

                                    }
                                });


                            }

                        }
                        else {

                            try {
                                new HpActionShot(getContext(), HpActionShot.SHOT_MISSED, aPointScore, aPosX, aPosY, aDistance).execute();
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                                Log.e("", e.toString());
                            }


                        }

                        break;

                    case DialogShot.SHOT_FOUL:

                        // GameTime Pause
                        HpGameManager.get().pause();

                        // execute action shot foul
                        lShotDialog.toggleShotFoul();

                        final DialogSelectPlayer lDialogSelectPlayer = new DialogSelectPlayer(getContext(), "Select a player", "Who Shot foul?");
                        lDialogSelectPlayer.hideTeam(lPlayerShot.getParentTeam().getTeamType());
                        lDialogSelectPlayer.setOnClickPlayerListner(new DialogSelectPlayer.OnClickPlayerListner() {
                            @Override
                            void OnClickPlayer(int team, int player) {

                                HpPlayer lPlayerShotFoul = HpGameManager.get().findPlayerBySlot(team, player);

                                // action foul
                                mPlayerShotFoul = lPlayerShotFoul.getName();

                                lShotDialog.setDesc(lShotDialog.getDesc() + ", ShotFoul : " + mPlayerShotFoul);

                            }
                        });

                        lDialogSelectPlayer.show();

                        break;

                }

            }
        });

        lShotDialog.show();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.d(TAG, "onActivityResult " + resultCode);

        switch (requestCode) {

            case REQUEST_CONNECT_DEVICE:

                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras().getString(ActivityBluetoothSearch.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = HpBluetoothManager.get().getBluetoothAdapter().getRemoteDevice(address);
                    // Attempt to connect to the device
                    HpBluetoothManager.get().getSerialService().connect(device);
                }

                break;

        }

    }


    public void addShotPos(int aPosX, int aPosY) {
        float lPosX = mImageCourt.getWidth() * aPosX / 10000;
        float lPosY = mImageCourt.getHeight() * aPosY / 10000;

        LayoutInflater inflater = (LayoutInflater) ActivityPlayBall.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView lImageEvent = (ImageView) inflater.inflate(R.layout.shot, null);

        lImageEvent.setX(lPosX);
        lImageEvent.setY(lPosY);

        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(30, 30);
        lImageEvent.setLayoutParams(parms);

        RelativeLayout lLayoutCourt = (RelativeLayout) findViewById(R.id.layout_court);
        lLayoutCourt.addView(lImageEvent);

    }

    private View.OnClickListener mOnClickUndo = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            // 김상훈 득점
            /*
            mImageEvent.setVisibility(View.INVISIBLE);

            addShotPos(6905, 1202);
            addShotPos(3520, 1738);
            addShotPos(2634, 1238);
            addShotPos(5952, 1416);
            addShotPos(3811, 1940);
            addShotPos(5325, 3547);
            addShotPos(9523, 1738);
            addShotPos(1356, 3690);
            addShotPos(9192, 3678);
            addShotPos(7421, 5059);
            addShotPos(1547, 4273);
            addShotPos(6121, 5750);
            addShotPos(4899, 6047);
            */

            //
            if (HpBluetoothManager.get().getSerialService().getState() == HpBluetoothSerialService.STATE_NONE) {
                // Launch the DeviceListActivity to see devices and do scan
                Intent serverIntent = new Intent(getContext(), ActivityBluetoothSearch.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
            }
            else {
                if (HpBluetoothManager.get().getSerialService().getState() == HpBluetoothSerialService.STATE_CONNECTED) {
                    HpBluetoothManager.get().getSerialService().stop();
                    HpBluetoothManager.get().getSerialService().start();
                }
            }


            if (mTextDebugLeft.getVisibility() == View.VISIBLE) {
                mTextDebugLeft.setVisibility(View.INVISIBLE);
                mTextDebugRight.setVisibility(View.INVISIBLE);
            }
            else {
                mTextDebugLeft.setVisibility(View.VISIBLE);
                mTextDebugRight.setVisibility(View.VISIBLE);
            }

        }
    };



    private View.OnClickListener mOnClickWhistle = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            try {
                new HpActionWhistle(getContext()).execute();
            }
            catch (Exception e) {
                Log.e("Hoopick", e.toString());
            }

        }

    };



    @Override
    public void onWhistleResult() {

        final DialogWhistle lDialogWhistle = new DialogWhistle(getContext());
        lDialogWhistle.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mScrollCourt.scrollTo(185,0);
        lDialogWhistle.setOnClickWhistleListner(new DialogWhistle.OnClickWhistleListner() {
            @Override
            void OnClickWhistle(int aMenuItem) {

                switch (aMenuItem) {

                    case DialogWhistle.MENU_MAIN_FOUL:

                        DialogSelectPlayer lDialog = new DialogSelectPlayer(getContext(), "Select a player", "Who foul?");
                        lDialog.setOnClickPlayerListner(new DialogSelectPlayer.OnClickPlayerListner() {
                            @Override
                            void OnClickPlayer(int team, int player) {

                                HpPlayer lPlayer = HpGameManager.get().findPlayerBySlot(team, player);

                                // action foul
                                try {
                                    new HpActionFoul(getContext(), lPlayer.getName()).execute();
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e("", e.toString());
                                }

                            }
                        });

                        lDialog.show();

                        break;



                    case DialogWhistle.MENU_SUB_VIOLATION_SHOT_CLOCK:
                        try {
                            new HpActionViolation(getContext(), HpActionViolation.VIOLATION_SHOT_CLOCK).execute();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            Log.e("", e.toString());
                        }
                        break;

                    case DialogWhistle.MENU_SUB_VIOLATION_TRAVELING:
                        try {
                            new HpActionViolation(getContext(), HpActionViolation.VIOLATION_TRAVELING).execute();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            Log.e("", e.toString());
                        }
                        break;

                    case DialogWhistle.MENU_SUB_VIOLATION_DOUBLE_DRIBBLE:
                        try {
                            new HpActionViolation(getContext(), HpActionViolation.VIOLATION_DOUBLE_DRIBBLE).execute();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            Log.e("", e.toString());
                        }
                        break;

                    case DialogWhistle.MENU_SUB_VIOLATION_HELD_BALL:
                        try {
                            new HpActionViolation(getContext(), HpActionViolation.VIOLATION_HELD_BALL).execute();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            Log.e("", e.toString());
                        }
                        break;

                    case DialogWhistle.MENU_SUB_VIOLATION_5S_POST_UP:
                        try {
                            new HpActionViolation(getContext(), HpActionViolation.VIOLATION_5S_POST_UP).execute();
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            Log.e("", e.toString());
                        }
                        break;

                    case DialogWhistle.MENU_MAIN_OUT_OF_BOUND:

                        DialogSelectPlayer lDialogSelect = new DialogSelectPlayer(getContext(), "Select a player", "Last Touch Player?");
                        lDialogSelect.setOnClickPlayerListner(new DialogSelectPlayer.OnClickPlayerListner() {
                            @Override
                            void OnClickPlayer(int team, int player) {

                                HpPlayer lPlayerTouch = HpGameManager.get().findPlayerBySlot(team, player);

                                // action OutOfBound
                                try {
                                    new HpActionOutOfBound(getContext(), lPlayerTouch.getName()).execute();
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                    Log.e("", e.toString());
                                }

                            }
                        });

                        lDialogSelect.show();

                        break;

                    case DialogWhistle.MENU_MAIN_CHECK:
                        lDialogWhistle.dismiss();
                        break;

                    case DialogWhistle.MENU_MAIN_END_QUATER:

                        if(mTextEndQuater.getText().equals("1")){
                            mTextEndQuater.setText("2");
                            return;
                        }
                        if(mTextEndQuater.getText()=="2"){
                            mTextEndQuater.setText("3");
                            return;
                        }
                        if(mTextEndQuater.getText()=="3"){
                            mTextEndQuater.setText("4");
                            return;
                        }
                        if(mTextEndQuater.getText()=="4"){
                            ActivityPlayBall.this.finish();
                            return;
                        }

                        break;

                }
            }
        });

        lDialogWhistle.show();

    }

    @Override
    public void onOutOfBoundResult(HpPlayer aPlayer) {

        // 공 맞고 나간 상대팀만 공 잡을 수 있게.
        setVisibleAllPlayer(View.VISIBLE);
        aPlayer.getParentTeam().each(new HpPlayerRunnable() {
            @Override
            public void run(HpPlayer aPlayer) {

                try {
                    mViewPlayer[aPlayer.getParentTeam().getTeamType()][aPlayer.getSlot()].setVisibility(View.INVISIBLE);
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    // continue;
                }

            }
        });

    }

    private OnTouchListener  mOnTouchCourt = new View.OnTouchListener() {


        @Override
        public boolean onTouch(View view, MotionEvent event) {
            final HpPlayer lPlayerSelect = HpGameManager.get().getBall().getGrabbedPlayer();

              if(lPlayerSelect == null) {
                Toast.makeText(getApplicationContext(), "select a shot player.", Toast.LENGTH_SHORT).show();
                return true;

            }

            if (event.getAction() == MotionEvent.ACTION_DOWN) {


            } else if (event.getAction() == MotionEvent.ACTION_UP) {

                String HomeTeamSelect = HpGameManager.get().getTeamHome().getName();
                if(lPlayerSelect.getName() == HomeTeamSelect ) {

                    mImageEvent.setX(event.getX() - 390);
                    mImageEvent.setY(event.getY() - 10);

                    int lPosX = (int) (event.getX() * 15000 / view.getWidth());
                    int lPosY = (int) (event.getY() * 10000 / view.getHeight());

                    int lGoalX = 13500;
                    int lGoalY = 5000;

                    int lDistance = CoordUtil.getDistance(lGoalX, lGoalY, lPosX, lPosY);
                    int lPointScore = CoordUtil.pointScoreInHalf(lGoalX, lGoalY, 10000, 5300, lPosX, lPosY);

                    doShot(lPointScore, lPosX, lPosY, lDistance);
                }
                else{

                    mImageEvent.setX(event.getX() + 30);
                    mImageEvent.setY(event.getY() - 20);

                    int lPosX = (int) (event.getX() * 15000 / view.getWidth());
                    int lPosY = (int) (event.getY() * 10000 / view.getHeight());

                    int lGoalX = 1500;
                    int lGoalY = 5000;

                    int lDistance = CoordUtil.getDistance(lGoalX, lGoalY, lPosX, lPosY);
                    int lPointScore = CoordUtil.pointScoreInHalf(lGoalX, lGoalY, 5000, 5300, lPosX, lPosY);

                    doShot(lPointScore, lPosX, lPosY, lDistance);
                }

                }


             else if (event.getAction() == MotionEvent.ACTION_MOVE) {

                /*
                float lDelX = event.getX() - mMoveX;
                float lDelY = event.getY() - mMoveY;

                mMoveX = event.getX();
                mMoveY = event.getY();

                mImageEvent.setX(mImageEvent.getX() + lDelX);
                mImageEvent.setY(mImageEvent.getY() + lDelY);

                int lPosX = (int) (event.getX() * 10000 / view.getWidth());
                int lPosY = (int) (event.getY() * 10000 / view.getHeight());

                int lPosCenterX = 5400;
                int lPosCenterY = 1000;

                // 골대 지점
                int lDistanceShot = CoordUtil.getDistance(lPosCenterX, lPosCenterY, lPosX, lPosY);
                int lDistance3Pt = CoordUtil.getDistance(lPosCenterX, lPosCenterY, lPosCenterX, 5300);


//                Log.i("Hoopick", String.format("onTouch (%04d, %04d) %d Ponit, Distance %04d, 3Pt : %04d"
//                        , lPosX, lPosY, CoordUtil.pointScoreInHalf(lPosX, lPosY), lDistanceShot, lDistance3Pt));
                */
            }

            return true;
        }
    };



    public View.OnLongClickListener mOnLongClickCourt = new View.OnLongClickListener() {

        @Override
        public boolean onLongClick(View view) {

//            mImageEvent.setX(mTouchX);
//            mImageEvent.setY(mTouchY);

            return false;
        }
    };

    public View.OnClickListener mOnClickPlayer = new View.OnClickListener() {

        @Override
        public void onClick(final View aView) {

            each(new PlayerView() {
                @Override
                public void run(HpPlayer aPlayer, View aViewPlayer,TextView aTextName, ImageView aImageBall) {

                    if (aView.getId() == aViewPlayer.getId()) {

                        try {

                            new HpActionGrabbedTheBall(getContext(), aPlayer.getName()).execute();
                            String HomeTeamSelect = HpGameManager.get().getTeamHome().getName();
                            String AwayTeamSelect = HpGameManager.get().getTeamAway().getName();
                            final HpPlayer lPlayerSelect = HpGameManager.get().getBall().getGrabbedPlayer();
                            if(lPlayerSelect.getName() == HomeTeamSelect ) {
                                mScrollCourt.smoothScrollTo(500, 0);
                                mScrollCourt.setOnTouchListener(new OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent event) {

                                        return true;
                                    }
                                });

                            }
                            else if(lPlayerSelect.getName() == AwayTeamSelect ){
                                mScrollCourt.smoothScrollTo(0, 0);
                                mScrollCourt.setOnTouchListener(new OnTouchListener() {
                                    @Override
                                    public boolean onTouch(View v, MotionEvent event) {

                                        return true;
                                    }
                                });
                            }

                        }
                        catch (Exception e) {
                            e.printStackTrace();
                            Log.e("", e.toString());
                        }

                    }

                }
            });


        }

    };

    private void refreshBall() {

        each(new PlayerView() {
            @Override
            public void run(HpPlayer aPlayer, View aViewPlayer,TextView aTextName, ImageView aImageBall) {

                if (aPlayer == HpGameManager.get().getBall().getGrabbedPlayer()) {
                    aImageBall.setVisibility(View.VISIBLE);
                }
                else {
                    aImageBall.setVisibility(View.INVISIBLE);
                }

            }
        });

    }

    @Override
    public void onTick(final int aRemainGameClockSec, final int aRemainShotClockSec) {

        String lGameTime = String.format("%02d:%02d", aRemainGameClockSec/60, aRemainGameClockSec%60);
        String lShotTime = String.format("%02d", aRemainShotClockSec);

        // Log.d("Hoopick", String.format("Time : %s %s", lGameTime, lShotTime));

        // game time
        TextView lTextGameTime = (TextView) findViewById(R.id.text_game_time);
        lTextGameTime.setText(lGameTime);

        // shot time
        TextView lTextShotTime = (TextView) findViewById(R.id.text_shot_time);
        lTextShotTime.setText(lShotTime);

    }

    @Override
    public void onShotClockResult2() {
        Log.d("Hoopick", "onShotClockResult");

            MediaPlayer lMediaPlayer = MediaPlayer.create(getContext(), R.raw.nbnsound);
            lMediaPlayer.start();

    }

    @Override
    public void onShotClockResult() {
        Log.d("Hoopick", "onShotClockResult");

        MediaPlayer lMediaPlayer = MediaPlayer.create(getContext(), R.raw.whistle);
        lMediaPlayer.start();

    }

    @Override
    public void onGameClockResult() {
        Log.d("Hoopick", "onGameClockResult");

        MediaPlayer lMediaPlayer = MediaPlayer.create(getContext(), R.raw.buzzer);
        lMediaPlayer.start();
    }

    @Override
    public void onGrabbedTheBallResult(HpPlayer aPlayer) {

        refreshBall();

    }

    private static class RunnableFreeThrowTeamFoul {

        public void run(final HpPlayer aPlayerFreeThrow, final int aTeamFoul) {

        }
    }

    @Override
    public void onFoulsResult(final HpPlayer aPlayerFoul, final int aHomeFouls, final int aAwayFouls) {

        ((TextView)findViewById(R.id.text_home_foul)).setText("" + aHomeFouls);
        ((TextView)findViewById(R.id.text_away_foul)).setText("" + aAwayFouls);

        final int lTeamFoul = aPlayerFoul.getParentTeam().getFouls();

        if ( 7 <= lTeamFoul) {
            // 파울 7~9개 자유투 2구 부여
            // 파울 10+ 자유투 2구 & 공격권 부여

            final Queue<RunnableFreeThrowTeamFoul> lFreeThrowAction = new ArrayDeque<>();

            for (int n=0; n<2; n++) {

                final int count = n + 1;

                lFreeThrowAction.add(new RunnableFreeThrowTeamFoul() {
                    @Override
                    public void run(final HpPlayer aPlayerFreeThrow, final int aTeamFoul) {

                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {

                                String lDescFreethrow = String.format("%s : TeamFoul Freethrow Shot %d / %d", aPlayerFreeThrow.getName(), count, 2);
                                final DialogFreeThrow lFreethrowDialog = new DialogFreeThrow(getContext(), "Freethrow", lDescFreethrow);

                                lFreethrowDialog.setOnClickFreethrowListner(new DialogFreeThrow.OnClickFreethrowListner() {
                                    @Override
                                    void OnClickShot(int aMenuItem) {

                                        String lMadeOrMiss = "";
                                        String lPlayerNameFreeThrow = aPlayerFreeThrow.getName();

                                        switch (aMenuItem) {
                                            case DialogFreeThrow.FREE_THROW_MADE:
                                                lFreethrowDialog.dismiss();
                                                lMadeOrMiss = HpActionFreeThrow.FREE_THROW_MADE;
                                                break;

                                            case DialogFreeThrow.FREE_THROW_MISS:
                                                lFreethrowDialog.dismiss();
                                                lMadeOrMiss = HpActionFreeThrow.FREE_THROW_MISS;
                                                break;
                                        }

                                        try {
                                            new HpActionFreeThrow(getContext(), HpActionFreeThrow.FREE_THROW_TYPE_TEAM_FOUL, lMadeOrMiss, lPlayerNameFreeThrow, "", lTeamFoul).execute();
                                        }
                                        catch (Exception e) {
                                            e.printStackTrace();
                                            Log.e("", e.toString());
                                        }

                                        new Handler().post(new Runnable() {
                                            @Override
                                            public void run() {

                                                if (lFreeThrowAction.size() > 0) {
                                                    RunnableFreeThrowTeamFoul lRunFreeThrow = lFreeThrowAction.poll();
                                                    lRunFreeThrow.run(aPlayerFreeThrow, aTeamFoul);
                                                }

                                            }
                                        });

                                    }
                                });

                                lFreethrowDialog.show();

                            }
                        });

                    }
                });

            }


            final DialogSelectPlayer lDialogSelectPlayer = new DialogSelectPlayer(getContext(), "Select a player", "Who FreeThrow?");
            lDialogSelectPlayer.hideTeam(aPlayerFoul.getParentTeam().getTeamType());
            lDialogSelectPlayer.setOnClickPlayerListner(new DialogSelectPlayer.OnClickPlayerListner() {
                @Override
                void OnClickPlayer(int team, int player) {

                    lDialogSelectPlayer.dismiss();
                    //

                    final HpPlayer lPlayerFreeThrow = HpGameManager.get().findPlayerBySlot(team, player);


                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {

                            if (lFreeThrowAction.size() > 0) {
                                RunnableFreeThrowTeamFoul lRunFreeThrow = lFreeThrowAction.poll();
                                lRunFreeThrow.run(lPlayerFreeThrow, aPlayerFoul.getParentTeam().getFouls());
                            }

                        }
                    });

                }
            });

            lDialogSelectPlayer.show();


        }
        else {
            // nothing

            // Foul 한 상대편만 공을 잡을 수 있도록 한다.
            setVisibleAllPlayer(View.VISIBLE);
            aPlayerFoul.getParentTeam().each(new HpPlayerRunnable() {
                @Override
                public void run(HpPlayer aPlayer) {

                    try {
                        mViewPlayer[aPlayer.getParentTeam().getTeamType()][aPlayer.getSlot()].setVisibility(View.INVISIBLE);
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        // continue;
                    }

                }
            });

        }

        //

    }

    private void setVisibleAllPlayer(final int aVisiblity) {
        each(new PlayerView() {
            @Override
            public void run(HpPlayer aPlayer, View aViewPlayer,TextView aTextName,ImageView aImageBall) {
                aViewPlayer.setVisibility(aVisiblity);
            }
        });
    }

    @Override
    public void onViolationResult(final HpPlayer aPlayer, String aViolationType) {

        // violation 하면 수비팀만 공 잡을 수 있도록 한다.
        // violation은 공격에서만 이루어지므로
        setVisibleAllPlayer(View.VISIBLE);
        aPlayer.getParentTeam().each(new HpPlayerRunnable() {
            @Override
            public void run(HpPlayer aPlayer) {

                try {
                    mViewPlayer[aPlayer.getParentTeam().getTeamType()][aPlayer.getSlot()].setVisibility(View.INVISIBLE);
                }
                catch (ArrayIndexOutOfBoundsException e) {
                    // continue;
                }

            }
        });

    }

    @Override
    public void onScoreResult(int aHomeScore, int aAwayScore) {
        ((TextView)findViewById(R.id.text_home_score)).setText("" + aHomeScore);
        ((TextView)findViewById(R.id.text_away_score)).setText("" + aAwayScore);
    }

    @Override
    public void onShotResult(String aMadeOrMiss, int aPointScore, String aPlayerShot, String aPlayerAssist) {

        if ( (true == aMadeOrMiss.equalsIgnoreCase(HpActionShot.SHOT_MADE) )) {


            HpPlayer lPlayerShot = HpGameManager.get().findPlayByTeam(aPlayerShot);
            if (null == lPlayerShot) {
                Log.e("Hoopick", "can not find player by name : " + aPlayerShot);
                return;
            }

            // 슛이 성공하면, 수비팀만 공을 잡을 수 있도록 한다.
            setVisibleAllPlayer(View.VISIBLE);
            lPlayerShot.getParentTeam().each(new HpPlayerRunnable() {
                @Override
                public void run(HpPlayer aPlayer) {

                    try {
                        mViewPlayer[aPlayer.getParentTeam().getTeamType()][aPlayer.getSlot()].setVisibility(View.INVISIBLE);
                    }
                    catch (ArrayIndexOutOfBoundsException e) {
                        // continue;
                    }
                }
            });

        }
        else {

            each(new PlayerView() {
                @Override
                public void run(HpPlayer aPlayer, View aViewPlayer,TextView aTextName, ImageView aImageBall) {
                    aViewPlayer.setVisibility(View.VISIBLE);
                }
            });

        }



    }

    @Override
    public void onFreeThrowResult(HpPlayer aPlayerFreeThrow, String aMadeOrMiss, int aTeamFoul) {

        if ( (true == aMadeOrMiss.equalsIgnoreCase(HpActionFreeThrow.FREE_THROW_MADE) )) {

            if (aTeamFoul >= 10) {

                // 팀파울 10개 넘어서 자유투 성공하면 , 공격만 공을 잡을 수 있도록 한다.
                setVisibleAllPlayer(View.VISIBLE);
                aPlayerFreeThrow.getParentTeam().getEnemyTeam().each(new HpPlayerRunnable() {
                    @Override
                    public void run(HpPlayer aPlayer) {

                        try {
                            mViewPlayer[aPlayer.getParentTeam().getTeamType()][aPlayer.getSlot()].setVisibility(View.INVISIBLE);
                        }
                        catch (ArrayIndexOutOfBoundsException e) {
                            // continue;
                        }


                    }
                });

            }
            else {

                // 슛이 성공하면, 수비팀만 공을 잡을 수 있도록 한다.
                setVisibleAllPlayer(View.VISIBLE);
                aPlayerFreeThrow.getParentTeam().each(new HpPlayerRunnable() {
                    @Override
                    public void run(HpPlayer aPlayer) {

                        try {
                            mViewPlayer[aPlayer.getParentTeam().getTeamType()][aPlayer.getSlot()].setVisibility(View.INVISIBLE);
                        }
                        catch (ArrayIndexOutOfBoundsException e) {
                            // continue;
                        }


                    }
                });
            }

        }
        else {

            each(new PlayerView() {
                @Override
                public void run(HpPlayer aPlayer, View aViewPlayer,TextView aTextName, ImageView aImageBall) {
                    aViewPlayer.setVisibility(View.VISIBLE);
                }
            });


        }

    }

    @Override
    public void onReadyToGrabbedTheBall() {

        each(new PlayerView() {
            @Override
            public void run(HpPlayer aPlayer, View aViewPlayer,TextView aTextName, ImageView aImageBall) {
                aViewPlayer.setVisibility(View.VISIBLE);
            }
        });

    }

    @Override
    public void onDisplayBoard(String aText) {

        // View lViewCourt = findViewById(R.id.layout_court);

        // Snackbar.make(lViewCourt, aText, Snackbar.LENGTH_SHORT).setAction("Action", null).show();

        Toast.makeText(getApplicationContext(), aText, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onPostAction() {

        String lMsgLeft = "";

        if (null == HpGameManager.get().getBall().getGrabbedPlayer()) {
            lMsgLeft += "GrabbedBall : None\r\n";
        }
        else {
            lMsgLeft += "GrabbedBall : " + HpGameManager.get().getBall().getGrabbedPlayer().getName() + "\r\n";
        }

        if (null == HpGameManager.get().getBall().getLastGrabbedPlayer()) {
            lMsgLeft += "LastGrabbedBall : None\r\n";
        }
        else {
            lMsgLeft += "LastGrabbedBall : " + HpGameManager.get().getBall().getLastGrabbedPlayer().getName() + "\r\n";
        }


        mTextDebugLeft.setText(lMsgLeft);


        String lMsgRight = "";
        int lCount = 0;

        for (int n = HpDataManager.get().getRepository().getStateList().size(); n>0; n--) {
            HpState nState = HpDataManager.get().getRepository().getStateList().get(n-1);
            lMsgRight += nState.getEventType() + "\r\n";

            if (lCount++ > 6) {
                break;
            }
        }

        mTextDebugRight.setText(lMsgRight);

    }


}


