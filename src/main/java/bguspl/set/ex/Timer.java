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
     * The timer should stop
     */
    private boolean terminate;

    /**
     * Constructor
     * @param env
     * @param table
     */
    public Timer(Env env,Table table)
    {
        this.terminate = false;
        this.time = -1;
        this.env = env;
        this.table = table;
    }
    public void run() {
        while (!terminate){
            if (time != -1) {
                if (time <= env.config.turnTimeoutWarningMillis)
                    env.ui.setCountdown(time, true);
                else
                    env.ui.setCountdown(time, false);
            }

            if (time > 0) {
                try {
                    time -= 1000;
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }

            } else {
                try {
                    this.table.setTimeOut(true); // at the end of the round - time out - > new round
                    synchronized (this) {
                        wait();
                    }
                } catch (InterruptedException ignored) {
                }
            }
        }
        System.out.println("Timer Thread terminated");
    }

    /**
     * The function reset the time value to default (round time)
     */
    public synchronized void resetTime()
    {
        this.time = env.config.turnTimeoutMillis;
        this.table.setTimeOut(false);
        notify();
    }

    public void terminate()
    {
        this.terminate = true;
    }
}
