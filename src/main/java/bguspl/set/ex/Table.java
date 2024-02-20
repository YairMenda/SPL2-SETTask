package bguspl.set.ex;

import bguspl.set.Env;

import java.util.*;
import java.util.stream.Collectors;

/**
 * This class contains the data that is visible to the player.
 *
 * @inv slotToCard[x] == y iff cardToSlot[y] == x
 */
public class Table {

    /**
     * The game environment object.
     */
    private final Env env;

    /**
     * Mapping between a slot and the card placed in it (null if none).
     */
    protected final Integer[] slotToCard; // card per slot (if any)

    /**
     * Mapping between a card and the slot it is in (null if none).
     */
    protected final Integer[] cardToSlot; // slot per card (if any)

    /**
     * Amount of cards that makes a set
     */
    public int SetSize;

    /**
     * Array that represents Players's token choices
     */
    protected LinkedList<Integer>[] playersTokens;

    /**
     * Boolean value - If the dealer needs to wake up anc clear table
     */
    public boolean timeout = false;
    /**
     * Queue of players to be checked by the dealer for possible set
     */
    public Queue<Integer> checkedList;
    /**
     * Constructor for testing.
     *
     * @param env        - the game environment objects.
     * @param slotToCard - mapping between a slot and the card placed in it (null if none).
     * @param cardToSlot - mapping between a card and the slot it is in (null if none).
     */
    public Table(Env env, Integer[] slotToCard, Integer[] cardToSlot) {

        this.env = env;
        this.slotToCard = slotToCard;
        this.cardToSlot = cardToSlot;
        this.SetSize = env.config.featureSize;
        this.playersTokens = new LinkedList[env.config.players];
        for (int i = 0; i < playersTokens.length; i++) {
            this.playersTokens[i] = new LinkedList<Integer>();
        }
        this.checkedList = new PriorityQueue<Integer>();
    }

    /**
     * Constructor for actual usage.
     * @param env - the game environment objects.
     */
    public Table(Env env) {

        this(env, new Integer[env.config.tableSize], new Integer[env.config.deckSize]);
    }

    /**
     * This method prints all possible legal sets of cards that are currently on the table.
     */
    public void hints() {
        List<Integer> deck = Arrays.stream(slotToCard).filter(Objects::nonNull).collect(Collectors.toList());
        env.util.findSets(deck, Integer.MAX_VALUE).forEach(set -> {
            StringBuilder sb = new StringBuilder().append("Hint: Set found: ");
            List<Integer> slots = Arrays.stream(set).mapToObj(card -> cardToSlot[card]).sorted().collect(Collectors.toList());
            int[][] features = env.util.cardsToFeatures(set);
            System.out.println(sb.append("slots: ").append(slots).append(" features: ").append(Arrays.deepToString(features)));
        });
    }

    /**
     * Count the number of cards currently on the table.
     *
     * @return - the number of cards on the table.
     */
    public int countCards() {
        int cards = 0;
        for (Integer card : slotToCard)
            if (card != null)
                ++cards;
        return cards;
    }

    /**
     * Places a card on the table in a grid slot.
     * @param card - the card id to place in the slot.
     * @param slot - the slot in which the card should be placed.
     *
     * @post - the card placed is on the table, in the assigned slot.
     */
    public synchronized void placeCard(int card, int slot) {
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {}

        cardToSlot[card] = slot;
        slotToCard[slot] = card;

        // TODO implement
        env.ui.placeCard(card,slot);
    }

    /**
     * Removes a card from a grid slot on the table.
     * @param slot - the slot from which to remove the card.
     */
    public synchronized void removeCard(int slot) {
        try {
            Thread.sleep(env.config.tableDelayMillis);
        } catch (InterruptedException ignored) {}

        // TODO implement
        for (int i = 0; i < playersTokens.length; i++) {
            playersTokens[i].remove((Integer) slot);
        }
        env.ui.removeTokens(slot);

        if (slotToCard[slot]!=null) {
            int card = slotToCard[slot];
            cardToSlot[card] = null;
            slotToCard[slot] = null;

            env.ui.removeCard(slot);
        }
    }

    /**
     * Places a player token on a grid slot.
     * @param player - the player the token belongs to.
     * @param slot   - the slot on which to place the token.
     */
    public synchronized void placeToken(int player, int slot) {
        // TODO implement
        if (playersTokens[player].size() < this.SetSize) {
            playersTokens[player].add(slot);
            env.ui.placeToken(player, slot);

            if (tokenAmountForSet(player)) {
                this.checkedList.add(player); //Adds the player to the queue of the players need to be checked
                notify(); //Notify the dealer to check the chosen cards
            }
        }
    }

    /**
     * Removes a token of a player from a grid slot.
     * @param player - the player the token belongs to.
     * @param slot   - the slot from which to remove the token.
     * @return       - true iff a token was successfully removed.
     */
    public boolean removeToken(int player, int slot) {
        // TODO implement
        playersTokens[player].remove((Integer)slot);
        env.ui.removeToken(player,slot);
        return true;

    }

    /**
     * Convert slot to a card
     * @param slot - the slot we want to convert
     * @return - int, the related card
     *assumes slot is legal
     */
    public int convertToCard(int slot)
    {
        return  slotToCard[slot];
    }

    /**
     * The function returns the token choices of the players
     */
    public LinkedList<Integer>[] GetPlayerTokens()
    {
        return playersTokens;
    }
    public boolean tokenAmountForSet(int player){return playersTokens[player].size() == this.SetSize;}

    /**
     *  The function converts slot pick into token action
     * @param player - player's id
     * @param slot - the chosen slot
     */
    public void actionToToken(int player,int slot)
    {
        if (slotToCard[slot] != null){//if a card is located at this slot
            if (playersTokens[player].contains(slot))
                removeToken(player, slot);
            else
                placeToken(player, slot);
        }
    }

    /**
     * the function reset the countdown and notifys the dealer
     * @param timeout - if the main counddown should be reset
     */
    public synchronized void setTimeOut(boolean timeout)
    {
        this.timeout = timeout;
        notify(); // notifys dealer
    }

    /**
     * Dealer waits between round time or if he needs to check optional cards
     */
    public synchronized void dealerWaits()
    {
        while (this.checkedList.isEmpty() & !(this.timeout))
            try{
                wait();
            }catch (InterruptedException ignored){}

    }
}

