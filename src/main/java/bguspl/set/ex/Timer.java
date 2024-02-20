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

    /**
     * Constructor
     * @param time
     * @param env
     * @param table
     */
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
                if (time <= env.config.turnTimeoutWarningMillis)
                    env.ui.setCountdown(time,true);
                else
                    env.ui.setCountdown(time,false);
            }

            this.table.setTimeOut(true); // at the end of the round - time out - > new round
        //System.out.println("thread " + Thread.currentThread().getName() + "terminated");
    }

    /**
     * The function reset the time value to default (round time)
     */
    public void resetTime()
    {
        this.time = env.config.turnTimeoutMillis;
        this.table.setTimeOut(false);
    }

    /**
     * The function set the time value to zero - stoping the run function - > stops the thread activation
     */
    public void terminate()
    {
        this.time = 0;
    }
}
