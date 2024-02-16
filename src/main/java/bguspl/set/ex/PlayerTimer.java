package bguspl.set.ex;

import bguspl.set.Env;

public class PlayerTimer implements Runnable{

    /**
     * The player to put a timer to
     */
    final int playerNumber;
    private Table table;
    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * The amount of time to set
     */
    private long time;

    public PlayerTimer(int playerNumber,long time,Env env,Table table)
    {
        this.playerNumber = playerNumber;
        this.time = time;
        this.env = env;
        this.table = table;
    }
    public void run()
    {
            while (time > 0)
            {
                try{Thread.sleep(1000);}catch (InterruptedException ignored){}
                time-=1000;
                env.ui.setFreeze(playerNumber,time);
            }


    }

    public void resetTime()
    {
        this.time = 0;
    }
}
