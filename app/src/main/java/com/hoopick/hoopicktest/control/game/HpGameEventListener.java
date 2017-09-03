package com.hoopick.hoopicktest.control.game;

import com.hoopick.hoopicktest.control.game.team.player.HpPlayer;

/**
 * Created by pro on 2016-09-22.
 */
public interface HpGameEventListener {

    // void onTick(final String aRemainingGameTime, final String aRemainingShotTime);
    void onTick(final int aRemainingGameTime, final int aRemainingShotTime);

    void onShotClockResult();

    void onShotClockResult2();

    void onGameClockResult();

    void onWhistleResult();

    void onOutOfBoundResult(HpPlayer aPlayer);

    void onGrabbedTheBallResult(HpPlayer aPlayer);

    void onScoreResult(int aHomeScore, int aAwayScore);

    void onFoulsResult(HpPlayer aPlayer, int aHomeFouls, int aAwayFouls);

    void onFreeThrowResult(HpPlayer aPlayerFreeThrow, String aMadeOrMiss, int aTeamFoul);

    void onViolationResult(HpPlayer aPlayer, String aViolationType);

    void onShotResult(String aMadeOrMiss, int aPointScore, String aPlayerShot, String aPlayerAssist);

    void onReadyToGrabbedTheBall();

    void onDisplayBoard(String aText);

    void onPostAction();
}
