package bguspl.set.ex;

import bguspl.set.Env;

public class Timer implements Runnable{

     private Table table;
    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * The amount of time to set
     */
    private long time;

    public Timer(long time,Env env,Table table)
    {
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
                if (time <= 5000)
                    env.ui.setCountdown(time,true);
                else
                    env.ui.setCountdown(time,false);
            }

            this.table.setTimeOut(true);
    }

    public void resetTime()
    {
        this.time = env.config.turnTimeoutMillis;
        this.table.setTimeOut(false);
    }
}
