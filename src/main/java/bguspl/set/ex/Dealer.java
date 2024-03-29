package bguspl.set.ex;

import bguspl.set.Env;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class manages the dealer's threads and data
 */
public class Dealer implements Runnable {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Game entities.
     */
    private final Table table;

    /**
     *Array of players
     */
    private final Player[] players;

    /**
     * The list of card ids that are left in the dealer's deck.
     */
    private final List<Integer> deck;

    /**
     * True iff game should be terminated.
     */
    private volatile boolean terminate;

    /**
     * Timer entitiy
     */

    private Timer timer;
    /**
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime = Long.MAX_VALUE;

    /**
     * Timer Thread
     */
    private Thread maintimer;

    /**
     * Constructor
     * @param env
     * @param table
     * @param players
     */
    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0,env.config.deckSize).boxed().collect(Collectors.toList());
        this.timer = new Timer(this.env,table);
        maintimer = new Thread(this.timer);

    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");

        // Initiates player threads
        for ( int i = 0; i < players.length;i++) {
            Player p = players[i];
            Thread player = new Thread(p);
            player.start();
        }


        //start main countdown timer thread
        maintimer.start();

        //run until the game is over
        while (!shouldFinish()) {
            placeCardsOnTable();

            try{Thread.sleep(500);}catch (InterruptedException ignored){}

            updateTimerDisplay(true);

            //Release players logistic lock
            for (Player p:players) {
                p.logisticFreeze(false);
            }

            timerLoop();
            updateTimerDisplay(false);
            removeAllCardsFromTable();
        }

        announceWinners();
        shutDownThreads();
        env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
        System.out.println("Dealer terminated");
        //System.out.println("thread " + Thread.currentThread().getName() + "terminated");
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did not time out.
     */
    private void timerLoop() {
        while (!terminate && System.currentTimeMillis() < reshuffleTime && !(this.table.timeout) ) {
            sleepUntilWokenOrTimeout();
            updateTimerDisplay(false);
            removeCardsFromTable();
            placeCardsOnTable();
        }
    }

    /**
     * Called when the game should be terminated.
     */
    public void terminate() {
        // TODO implement
        this.terminate = true;
    }

    /**
     * The function shuts down all the threads
     */
    public void shutDownThreads()
    {
        this.timer.terminate(); //terminate the time thread
        this.maintimer.interrupt();
        try {
            this.maintimer.join();
        }catch (InterruptedException ignored){}

        //try{Thread.sleep(300);}catch (InterruptedException ignored){}

        for (Player p: players) {
            p.terminate();
        }

    }
    /**
     * Check if the game should be terminated or the game end conditions are met.
     *
     * @return true iff the game should be finished.
     */
    private boolean shouldFinish() {
        return terminate || env.util.findSets(deck, 1).size() == 0;
    }

    /**
     * Checks cards should be removed from the table and removes them.
     */
    private void removeCardsFromTable() {
        // TODO implement
        /**IF it is a SET - > remove cards
         */
        LinkedList<Integer>[] playerTokens = this.table.playersTokens;
        //convert tokens choices to cards
        while (!table.checkedList.isEmpty()) {

            int playerIndex = table.checkedList.peek();

            if (playerTokens[playerIndex].size()  == env.config.featureSize) {

                LinkedList<Integer> tokens = new LinkedList<Integer>(playerTokens[playerIndex]);
                int[] cards = new int[this.table.SetSize];
                int j = 0;
                for (Integer slot : tokens) {
                    cards[j] = table.convertToCard(slot);
                    j++;
                }

                boolean isASet = env.util.testSet(cards); // check if player choice is a set

                if (isASet) {
                    this.players[playerIndex].point();
                    for (Integer slot : tokens) {
                        this.table.removeCard(slot);
                    }
                    updateTimerDisplay(true);

                } else {
                    this.players[playerIndex].penalty();
                }
            }
            table.checkedList.remove();
        }
    }

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    private void placeCardsOnTable() {
        // TODO implement

        //place the remaining card from the deck to the table

        Collections.shuffle(deck);//reshuffle

            for (int i = 0; i < table.slotToCard.length; i++) {
                if (this.table.slotToCard[i] == null & deck.size() >= 1) {
                    int card = deck.remove(0);
                    this.table.placeCard(card, i);
                }
            }

    }


    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some purpose.
     */
    private void sleepUntilWokenOrTimeout() {
        // TODO implement
           table.dealerWaits();
    }
    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay(boolean reset) {
        // TODO implement

        //reset main countdown timer
        if (reset)
            this.timer.resetTime();
    }

    /**
     * Returns all the cards from the table to the deck.
     */
    private void removeAllCardsFromTable() {
        // TODO implement

        for (Player p:players) {
            p.logisticFreeze(true); // lock players while removing all cards
        }

        List<Integer> cardsToDeck = this.table.removeCardToDeck();
        deck.addAll(cardsToDeck);

    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {
        // TODO implement

        //creates array of winners

        LinkedList<Integer> winners = new LinkedList<Integer>();
        int maxScore = 0;
        for (int i=0; i < players.length; i++){
            if (players[i].score() > maxScore)
                maxScore = players[i].score();
        }
        for (int i=0; i < players.length; i++){
            if (players[i].score() == maxScore)
                winners.add(players[i].id);
        }
        int[] arr = new int[winners.size()];
        for (int i=0; i < arr.length; i++)
            arr[i] = (int) winners.get(i);

        env.ui.announceWinner(arr);
    }
}
