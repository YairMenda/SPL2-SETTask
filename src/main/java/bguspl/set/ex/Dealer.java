package bguspl.set.ex;

import bguspl.set.Env;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
     * The time when the dealer needs to reshuffle the deck due to turn timeout.
     */
    private long reshuffleTime = Long.MAX_VALUE;

    public Dealer(Env env, Table table, Player[] players) {
        this.env = env;
        this.table = table;
        this.players = players;
        deck = IntStream.range(0, env.config.deckSize).boxed().collect(Collectors.toList());
    }

    /**
     * The dealer thread starts here (main loop for the dealer thread).
     */
    @Override
    public void run() {
        env.logger.info("thread " + Thread.currentThread().getName() + " starting.");
        while (!shouldFinish()) {
            placeCardsOnTable();
            timerLoop();
            updateTimerDisplay(false);
            removeAllCardsFromTable();
        }
        announceWinners();
        env.logger.info("thread " + Thread.currentThread().getName() + " terminated.");
    }

    /**
     * The inner loop of the dealer thread that runs as long as the countdown did not time out.
     */
    private void timerLoop() {
        while (!terminate && System.currentTimeMillis() < reshuffleTime) {
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
        for (int i = 0; i <playerTokens.length;i++)
            if (playerTokens[i].size() == 3) {
                LinkedList<Integer> slots = playerTokens[i];
                int playerWith3Tokens = i;

                int[] cards = {table.convertToCard(slots.get(0)), table.convertToCard(slots.get(1))
                        ,table.convertToCard(slots.get(2))};

                boolean isASet = env.util.testSet(cards);
                if (isASet) {
                    this.players[playerWith3Tokens].point();
                    this.table.removeCard(slots.get(0));
                    this.table.removeCard(slots.get(1));
                    this.table.removeCard(slots.get(2));
                }

                else {
                    this.players[playerWith3Tokens].penalty();
                    this.table.clearAllTokensPenalty(playerWith3Tokens);
                }


            }
    }

    /**
     * Check if any cards can be removed from the deck and placed on the table.
     */
    private void placeCardsOnTable() {
        // TODO implement

        //Unites table cards with deck
        List<Integer> allCardsRemain = new LinkedList<Integer>(deck);
        for (int i = 0; i < table.slotToCard.length; i++) {
            if (this.table.slotToCard[i]!= null)
                allCardsRemain.add(this.table.slotToCard[i]);
        }

        //checks if any set left in the game
        if (env.util.findSets(allCardsRemain, 1).size() == 0)
            terminate();

        else {
            //place the remaining card from the deck to the table
            Collections.shuffle(deck);
            for (int i = 0; i < table.slotToCard.length; i++) {
                if (this.table.slotToCard[i] == null) {
                    int card = deck.remove(0);
                    this.table.placeCard(card, i);
                }
            }
        }
    }

    /**
     * Sleep for a fixed amount of time or until the thread is awakened for some purpose.
     */
    private void sleepUntilWokenOrTimeout() {
        // TODO implement
    }

    /**
     * Reset and/or update the countdown and the countdown display.
     */
    private void updateTimerDisplay(boolean reset) {
        // TODO implement

    }

    /**
     * Returns all the cards from the table to the deck.
     */
    private void removeAllCardsFromTable() {
        // TODO implement
        for (int i = 0; i < this.table.slotToCard.length; i++)
            this.table.removeCard(i);
    }

    /**
     * Check who is/are the winner/s and displays them.
     */
    private void announceWinners() {
        // TODO implement
        int[] winners={0};
         if (players[0].score() < players[1].score()) {
            winners[0]=1;
        }
        else if (players[0].score() == players[1].score()){
            int[] array = {0,1};
            winners = array;
        }
        env.ui.announceWinner(winners);
    }
}
