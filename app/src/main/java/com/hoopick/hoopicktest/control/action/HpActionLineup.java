package com.hoopick.hoopicktest.control.action;

import android.content.Context;

import com.hoopick.hoopicktest.control.action.HpActionBase;
import com.hoopick.hoopicktest.control.game.HpGameManager;
import com.hoopick.hoopicktest.control.game.team.player.HpPlayer;
import com.hoopick.hoopicktest.data.HpDataManager;
import com.hoopick.hoopicktest.data.model.HpState;
import com.hoopick.hoopicktest.data.util.HpGenUtil;

/**
 * Created by pro on 2016-09-22.
 */
public class HpActionLineup extends HpActionBase {

    private String mGameId = "";
    private String mSeason = "";
    private String mTeamHome = "";
    private String mTeamAway = "";
    private String mRemainingTime = "";
    private String mShotTime = "";
    public HpActionLineup(Context aContext
            , String aGameId
            , String aSeason
            , String aTeamHome
            , String aTeamAway
            , String aRemainingTime
            , String aShotTime
    ) {
        super(aContext);

        mGameId = aGameId;
        mSeason = aSeason;
        mTeamHome = aTeamHome;
        mTeamAway = aTeamAway;
        mRemainingTime = aRemainingTime;
        mShotTime = aShotTime;
    }

    @Override
    protected boolean preTask() throws Exception {

        // init gameData
        HpDataManager.get().getRepository().clear();
        HpGameManager.get().getTimer().getStopWatchGame().stop();
        HpGameManager.get().getTimer().getStopWatchShot().stop();
        HpGameManager.get().getTeamHome().setName("");
        HpGameManager.get().getTeamHome().setmScore(0);
        HpGameManager.get().getTeamHome().clearPlayer();
        HpGameManager.get().getTeamAway().clearPlayer();
        HpGameManager.get().getTeamAway().setName("");
        HpGameManager.get().getTeamAway().setmScore(0);

        //

        HpGameManager.get().setSaveFileName(HpGenUtil.genFileName(mTeamHome, mTeamAway));

        HpGameManager.get().getTimer().setGameClockMinutes(Integer.parseInt(mRemainingTime), Integer.parseInt(mShotTime));

        HpGameManager.get().getTeamHome().setName(mTeamHome);
        HpGameManager.get().getTeamAway().setName(mTeamAway);

        HpGameManager.get().getTeamHome().addPlayer(new HpPlayer(0, mTeamHome, mTeamHome));


        HpGameManager.get().getTeamAway().addPlayer(new HpPlayer(0, mTeamAway, mTeamAway));


        return true;
    }

    @Override
    protected void postTask() throws Exception {

    }

    @Override
    protected void applyState(HpState aState) {

        aState.setGameId(mGameId);
        aState.setSeason(mSeason);
//        aState.setGameDate(mGameDate);
        aState.setTeamHome(mTeamHome);
        aState.setTeamAway(mTeamAway);

//        aState.setPeriod("1");
        aState.setScoreAway("");
        aState.setScoreHome("");
//        aState.setRemainingTime(String.format("%02d:%02d", 0, 0));
//        aState.setElapsedTime(String.format("%02d:%02d", 0, 0));
//        aState.setElapsedTimeSec(""));
//        aState.setPlayLength(String.format("%02d:%02d", 0, 0));
//        aState.setPlayLengthSec("0");
//        aState.setPlayId(String.format("%d", Integer.parseInt(aState.getPlayId().trim()) + 1));
//        aState.setTeamNameExecutedEvent(mTeamExecuteEvent);
        aState.setEventType(HpState.EVENT_TYPE_LINEUP);;
        aState.setJumpBallAway("");
        aState.setJumpBallHome("");
        aState.setteamBlockedAShot("");
        aState.setteamCheckIn("");
        aState.setteamCheckOut("");
        aState.setFreeThrowsOrder("");
        aState.setteamFoul("");
        aState.setteamWhistleGrabbed("");
        aState.setFreeThrowsCount("");
        aState.setteamExecutedEvent("");
        aState.setPointsScoredWithinEvent("");
        aState.setPlayerGrabbedTheBallAfterEvent("");
        aState.setMoreDetailOfEvent("");
        aState.setShotMadeOrMissed("");
        aState.setteamSteal("");
        aState.setEventDetail("");
        aState.setShotDistance("");
        aState.setShotAxisX("");
        aState.setShotAxisY("");
        aState.setEventDesc("");

    }


    public String getmGameId() {
        return mGameId;
    }

    public void setmGameId(String mGameId) {
        this.mGameId = mGameId;
    }

    public String getmSeason() {
        return mSeason;
    }

    public void setmSeason(String mSeason) {
        this.mSeason = mSeason;
    }

    public String getmTeamHome() {
        return mTeamHome;
    }

    public void setmTeamHome(String mTeamHome) {
        this.mTeamHome = mTeamHome;
    }

    public String getmTeamAway() {
        return mTeamAway;
    }

    public void setmTeamAway(String mTeamAway) {
        this.mTeamAway = mTeamAway;
    }


    public String getmRemainingTime() {
        return mRemainingTime;
    }

    public void setmRemainingTime(String mRemainingTime) {
        this.mRemainingTime = mRemainingTime;
    }
}
