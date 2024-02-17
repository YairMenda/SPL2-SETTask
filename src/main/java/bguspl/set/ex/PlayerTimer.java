package bguspl.set.ex;

import bguspl.set.Env;

public class PlayerTimer implements Runnable{

    /**
     * The player to put a timer to
     */
    final int playerNumber;
    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * The amount of time to set
     */
    private long time;
    private Player player;

    /**
     *
     * @param playerNumber
     * @param env
     * @param time
     * @param player
     */
    public PlayerTimer(int playerNumber,Env env,long time,Player player)
    {
        this.playerNumber = playerNumber;
        this.time = time;
        this.env = env;
        this.player = player;
    }
    public void run()
    {
        while (time > 0) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
                time -= 1000;
                env.ui.setFreeze(playerNumber, time);
            }
        this.player.releaseFreeze();

    }
}
