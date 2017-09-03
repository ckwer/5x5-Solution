package com.hoopick.hoopicktest.control.game;


import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hoopick.hoopicktest.control.game.HpGameEventListener;
import com.hoopick.hoopicktest.control.bluetooth.HpBluetoothManager;
import com.hoopick.hoopicktest.control.bluetooth.HpBluetoothSerialService;
import com.hoopick.hoopicktest.control.game.ball.HpBall;
import com.hoopick.hoopicktest.control.game.team.HpTeam;
import com.hoopick.hoopicktest.control.game.team.HpTeamRunnable;
import com.hoopick.hoopicktest.control.game.team.player.HpPlayer;
import com.hoopick.hoopicktest.control.game.team.player.HpPlayerRunnable;
import com.hoopick.hoopicktest.control.game.timer.HpGameTimer;
import com.hoopick.hoopicktest.control.game.timer.HpGameTimerListener;
import com.hoopick.hoopicktest.data.model.HpStateBluetooth;

/**
 * Created by pro on 2016-09-22.
 */
public class HpGameManager implements HpGameTimerListener {

    // -------------------------------------------------------------
    // property

    private Context mContext = null;

    private HpGameEventListener mGameEventListener = null;

    private HpGameTimer mGameTimer = new HpGameTimer();

    private HpTeam mTeamHome = new HpTeam(HpTeam.TEAM_TYPE_HOME);
    private HpTeam mTeamAway = new HpTeam(HpTeam.TEAM_TYPE_AWAY);

    private HpBall mBall = new HpBall();

    private String mSaveFileName = "";


    // -------------------------------------------------------------
    // method

    public void startGame() throws Exception {

        mBall.setGameManager(this);
        mGameTimer.setGameTimerListener(this);
        mGameTimer.startGame();

    }

    public void pause() {
        mGameTimer.pause();
    }

    public void resume() {
        mGameTimer.resume();
    }

    public void stop() {mGameTimer.stop();}

    public boolean isPaused() {
        return mGameTimer.isPaused();
    }

    protected void runOnListener(final Runnable aRunnable) {

        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                aRunnable.run();
            }
        });

    }

    @Override
    public void onTick(final int aRemainGameClockSec, final int aRemainShotClockSec) {

        Log.d("Hoopick", String.format("Time : %d %d", aRemainGameClockSec, aRemainShotClockSec));

        //
        // Bluetooth Write
        if (HpBluetoothManager.get().getSerialService().getState() == HpBluetoothSerialService.STATE_CONNECTED) {

            try {
                HpStateBluetooth lStateBluetooth = new HpStateBluetooth();

                String lGameTime = String.format("%02d:%02d", aRemainGameClockSec/60, aRemainGameClockSec%60);
                lStateBluetooth.setRemainingTime(lGameTime);

                ObjectMapper lMapper = new ObjectMapper();
                final String lJsonStateBluetooth = lMapper.writerWithDefaultPrettyPrinter().writeValueAsString(lStateBluetooth);

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HpBluetoothManager.get().write(lJsonStateBluetooth);
                        }
                        catch (Exception e) {
                            Log.e("Hoopick", e.getMessage());
                        }
                    }
                });


            }
            catch (Exception e) {
                Log.e("Hoopick", e.getMessage());
            }

        }

        runOnListener(new Runnable() {
            @Override
            public void run() {
                mGameEventListener.onTick(aRemainGameClockSec, aRemainShotClockSec);
            }
        });

        // 경기종료인 경우
        if (0 == aRemainGameClockSec) {
            mGameTimer.getStopWatchGame().stop();
            mGameTimer.getStopWatchShot().stop();

            runOnListener(new Runnable() {
                @Override
                public void run() {
                    mGameEventListener.onGameClockResult();
                }
            });

        }

        // 공격시간 종료일 경우
        if (0 == aRemainShotClockSec) {
            mGameTimer.getStopWatchShot().stop();

        }

        // 공격시간 10초 남았을때 사운드
        if (10 == aRemainShotClockSec) {

            runOnListener(new Runnable() {
                @Override
                public void run() {
                    // 공격시간 10초 남았을때 사운드
                    mGameEventListener.onShotClockResult2();
                }
            });

        }

    }

    public HpPlayer findPlayerBySlot(int t, int s) {

        HpTeam lTaretTeam = null;

        if (t == 0) {
            lTaretTeam = mTeamHome;
        }
        else {
            lTaretTeam = mTeamAway;
        }

        if (null == lTaretTeam) {
            return null;
        }

        for (HpPlayer nPlayer : lTaretTeam.getPlayerList()) {
            if (nPlayer.getSlot() == s) {
                return nPlayer;
            }
        }

        return null;
    }

    public HpPlayer findPlayByTeam(final String aPlayTeam) {

        for (HpPlayer nPlayer : mTeamHome.getPlayerList()) {
            if (nPlayer.getName().equalsIgnoreCase(aPlayTeam)) {
                return nPlayer;
            }
        }

        for (HpPlayer nPlayer : mTeamAway.getPlayerList()) {
            if (nPlayer.getName().equalsIgnoreCase(aPlayTeam)) {
                return nPlayer;
            }
        }

        return null;
    }

    public void each(HpTeamRunnable aTeamRunnable) {

        aTeamRunnable.run(mTeamHome);
        aTeamRunnable.run(mTeamAway);

    }

    public void each(HpPlayerRunnable aPlayerRunnable) {

        for (HpPlayer nPlayer : mTeamHome.getPlayerList()) {
            aPlayerRunnable.run(nPlayer);
        }

        for (HpPlayer nPlayer : mTeamAway.getPlayerList()) {
            aPlayerRunnable.run(nPlayer);
        }

    }


    // -------------------------------------------------------------
    // start singleton

    private static HpGameManager gHpGameManager;

    public static HpGameManager get() {
        if (null == gHpGameManager) {
            gHpGameManager = new HpGameManager();
        }
        return gHpGameManager;
    }

    private HpGameManager() {
    }


    // -------------------------------------------------------------
    // setter/getter


    public String getSaveFileName() {
        return mSaveFileName;
    }

    public void setSaveFileName(String aSaveFileName) {
        this.mSaveFileName = aSaveFileName;
    }

    public void setGameEventListener(HpGameEventListener aHpGameEventListener) {
        mGameEventListener = aHpGameEventListener;
        mContext = (Context) aHpGameEventListener;
    }

    public HpGameEventListener getGameEventListener() {
        return mGameEventListener;
    }

    public HpGameTimer getTimer() {
        return mGameTimer;
    }

    public HpTeam getTeamHome() {
        return mTeamHome;
    }

    public void setTeamHome(HpTeam aTeamHome) {
        this.mTeamHome = aTeamHome;
    }

    public HpTeam getTeamAway() {
        return mTeamAway;
    }

    public void setTeamAway(HpTeam aTeamAway) {
        this.mTeamAway = aTeamAway;
    }

    public HpBall getBall() {
        return mBall;
    }

    public void setBall(HpBall aBall) {
        this.mBall = aBall;
    }
}
