package bguspl.set.ex;

import bguspl.set.Env;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;

/**
 * This class manages the players' threads and data
 *
 * @inv id >= 0
 * @inv score >= 0
 */
public class Player implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;

    /**
     * The id of the player (starting from 0).
     */
    public final int id;

    /**
     * The thread representing the current player.
     */
    private Thread playerThread;

    /**
     * The thread of the AI (computer) player (an additional thread used to generate key presses).
     */
    private Thread aiThread;

    /**
     * True iff the player is human (not a computer player).
     */
    private final boolean human;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    /**
     * The current score of the player.
     */
    private int score;

    /**
     * The incoming actions of the player
     */
    private Queue<Integer> actions;

    /**
     * Boolean value - If the player is freezed and cannot do anything
     */
    private boolean freeze;
    /**
     * The class constructor.
     *
     * @param env    - the environment object.
     * @param dealer - the dealer object.
     * @param table  - the table object.
     * @param id     - the id of the player.
     * @param human  - true iff the player is a human player (i.e. input is provided manually, via the keyboard).
     */
    public Player(Env env, Dealer dealer, Table table, int id, boolean human) {
        this.env = env;
        this.table = table;
        this.id = id;
        this.human = human;
        this.actions = new PriorityQueue<Integer>();
        this.freeze = false;
    }

    /**
     * The main player thread of each player starts here (main loop for the player thread).
     */
    @Override
    public void run() {
        playerThread = Thread.currentThread();
        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
        if (!human) createArtificialIntelligence();

        else {
            while (!terminate) {
                if (!actions.isEmpty() & !freeze)
                    this.table.actionToToken(id, actions.remove());

            }
        }

        if (!human) try { aiThread.join(); } catch (InterruptedException ignored) {}
        env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * Creates an additional thread for an AI (computer) player. The main loop of this thread repeatedly generates
     * key presses. If the queue of key presses is full, the thread waits until it is not full.
     */
    private void createArtificialIntelligence() {
        // note: this is a very, very smart AI (!)
        aiThread = new Thread(() -> {
            env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
            Random rnd = new Random();
            try{Thread.sleep(3000);}catch (InterruptedException ignored){}

            while (!terminate) {
                // TODO implement player key press simulator
                int randomKeyPressed = rnd.nextInt(12);
                keyPressed(randomKeyPressed);

                if (!actions.isEmpty() & !freeze)
                    this.table.actionToToken(id, actions.remove());
                try {
                    synchronized (this) { wait(2000); }
                } catch (InterruptedException ignored) {}
            }
            env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
        }, "computer-" + id);
        aiThread.start();
    }

    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        terminate = true;
    }

    /**
     * This method is called when a key is pressed.
     *
     * @param slot - the slot corresponding to the key pressed.
     */
    public void keyPressed(int slot) {
        //??not IN Penalty
        if (actions.size() < env.config.SetSize & !freeze)
            actions.add(slot);
    }

    /**
     * Award a point to a player and perform other related actions.
     *
     * @post - the player's score is increased by 1.
     * @post - the player's score is updated in the ui.
     */
    public void point() {
        // TODO implement

        //int ignored = table.countCards(); // this part is just for demonstration in the unit tests

        this.score++;
        env.ui.setScore(id, score);
        Thread timerThread = new Thread(new PlayerTimer(id,env,env.config.pointFreezeMillis,this));
        timerThread.start();

    }

    /**
     * Penalize a player and perform other related actions.
     */
    public void penalty() {
        // TODO implement
        Thread timerThread = new Thread(new PlayerTimer(id,env,env.config.penaltyFreezeMillis,this));
        timerThread.start();
    }

    public int score() {
        return score;
    }
    public void releaseFreeze()
    {
        this.freeze = false;
    }
    public void freezePlayer()
    {
        this.freeze = true;
    }


}
