package com.extremelyd1.game.winCondition;

import com.extremelyd1.bingo.BingoCard;
import com.extremelyd1.bingo.item.BingoItem;
import com.extremelyd1.config.Config;
import com.extremelyd1.game.team.PlayerTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.extremelyd1.game.Game.PREFIX;

/**
 * A class that stores and handles win conditions
 * Such as needing a full card to win the game
 */
public class WinConditionChecker {

    /**
     * How many lines need to be completed in order to win
     */
    private int numLinesToComplete;
    /**
     * Whether teams need to complete the full card in order to win
     */
    private boolean fullCard;
    /**
     * The number of completions for an item to lock it for the remaining teams
     * A value of zero will indicate that no locking will occur
     */
    private int completionsToLock;

    /**
     * A variant of the "line" mode
     * Any team completing given lines ends the game, but the winner is the team who collect the most items
     */
    private boolean quidditchMode;

    /**
     * quidditch mode only
     * How many extra score can a team receive, if the team gets the first "bingo" e.g. required lines collected
     */
    private final int quidditchGoldenSnitchExtraScore;

    public WinConditionChecker(Config config) {
        this.numLinesToComplete = config.getDefaultNumLinesComplete();
        this.fullCard = false;
        this.completionsToLock = 0;
        this.quidditchMode = config.isDefaultWinConditionIsQuidditch();
        this.quidditchGoldenSnitchExtraScore = config.getQuidditchGoldenSnitchExtraScore();
    }

    /**
     * Checks whether the game is finished and returns a possibly empty list of the current winners
     * @param card The bingo card to check
     * @param team The team to check for
     * @param allTeams All the teams participating
     * @return An empty list if there are no winners and otherwise a list containing the teams that won
     */
    public List<PlayerTeam> getCurrentWinners(BingoCard card, PlayerTeam team, Iterable<PlayerTeam> allTeams) {
        if (completionsToLock > 0) {
            return getLockoutWinner(card, allTeams);
        }

        if (hasBingo(card, team)) {
            // fallen's fork: add "quidditch" mode
            if (isQuidditchMode()) {  // Quidditch Line
                Optional<PlayerTeam> opt = this.decideQuidditchWinner(allTeams);
                if (opt.isPresent()) {
                    return Collections.singletonList(opt.get());
                }
            } else {  // Line
                return Collections.singletonList(team);
            }
        }

        return Collections.emptyList();
    }

    // fallen's fork: add for "quidditch" mode
    public boolean IsInSuddenDeath(BingoCard card, Iterable<PlayerTeam> allTeams) {
        for (PlayerTeam team : allTeams) {
            if (hasBingo(card, team)) {
                return this.decideQuidditchWinner(allTeams).isEmpty();
            }
        }
        return false;
    }

    // fallen's fork: add for "quidditch" mode
    public void onCollection(BingoCard card, PlayerTeam collectorTeam, Iterable<PlayerTeam> allTeams, Config config) {
        // update the first bingo team for "quidditch" mode
        if (hasBingo(card, collectorTeam)) {
            boolean firstBingo = true;
            for (PlayerTeam team : allTeams) {
                if (hasBingo(card, team) && team != collectorTeam) {
                    firstBingo = false;
                    break;
                }
            }
            if (firstBingo) {
                if (config.notifyOtherTeamCompletions()) {
                    Bukkit.broadcastMessage(
                            PREFIX +
                                    collectorTeam.getColor() + collectorTeam.getName()
                                    + ChatColor.WHITE + " team gets the Golden Snitch and receives "
                                    + ChatColor.AQUA + quidditchGoldenSnitchExtraScore + ChatColor.WHITE + " extra score"
                    );
                }
                collectorTeam.setFirstBingo(true);
            }
        }
    }

    /**
     * {@link #decideWinner}, but for quidditch mode
     */
    public Optional<PlayerTeam> decideQuidditchWinner(Iterable<PlayerTeam> teams) {
        List<PlayerTeam> potentialWinners = new ArrayList<>();
        int maxScore = 0;
        for (PlayerTeam team : teams) {
            int score = team.getNumCollected();
            if (team.isFirstBingo()) {
                score += quidditchGoldenSnitchExtraScore;
            }

            if (score > maxScore) {
                potentialWinners.clear();
                maxScore = score;
            }

            if (score >= maxScore) {
                potentialWinners.add(team);
            }
        }

        if (potentialWinners.size() == 1) {
            return Optional.of(potentialWinners.get(0));
        } else {
            return Optional.empty();
        }
    }

    /**
     * Checks whether there is a winner in the 'lockout' game type
     * @param card The bingo card to check
     * @param allTeams All the teams participating
     * @return An Optional of PlayerTeam that possibly contains the winning team
     */
    private List<PlayerTeam> getLockoutWinner(BingoCard card, Iterable<PlayerTeam> allTeams) {
        // If this method is called, the iterable of PlayerTeam instances is never empty
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        int maxNumCollected = StreamSupport
                .stream(allTeams.spliterator(), false)
                .mapToInt(PlayerTeam::getNumCollected)
                .max().getAsInt();

        List<PlayerTeam> maxCollectionsTeams = StreamSupport
                .stream(allTeams.spliterator(), false)
                .filter(team -> team.getNumCollected() == maxNumCollected)
                .collect(Collectors.toList());

        for (PlayerTeam maxCollectionTeam : maxCollectionsTeams) {
            for (PlayerTeam otherTeam : allTeams) {
                if (otherTeam.equals(maxCollectionTeam)) {
                    continue;
                }

                // If this other team can collect at least the same number of items as the leading team
                // currently has, then the leading team has not achieved victory yet
                if (getPossibleNumCollections(card, otherTeam) > maxNumCollected) {
                    return new ArrayList<>();
                }
            }
        }

        return maxCollectionsTeams;
    }

    /**
     * Checks whether the given team has achieved bingo in 'full card' or 'lines' game types
     * @param card The bingo card to check
     * @param team The team to check for
     * @return True if the given team has achieved bingo, false otherwise
     */
    private boolean hasBingo(BingoCard card, PlayerTeam team) {
        if (fullCard) {
            return card.isCardComplete(team);
        } else {
            return card.getNumLinesComplete(team) >= numLinesToComplete;
        }
    }

    /**
     * Decide the winner when the timer ends
     * @param teams The list of teams to choose from
     * @return The team that won
     */
    public WinReason decideWinner(Iterable<PlayerTeam> teams) {
        List<PlayerTeam> potentialWinners = new ArrayList<>();
        int maxScore = 0;
        for (PlayerTeam team : teams) {
            int score = team.getNumCollected();

            if (score > maxScore) {
                potentialWinners.clear();
                maxScore = score;
            }

            if (score >= maxScore) {
                potentialWinners.add(team);
            }
        }

        WinReason winReason;

        if (potentialWinners.size() > 1) {
            winReason = new WinReason(
                    potentialWinners.get(new Random().nextInt(potentialWinners.size())),
                    WinReason.Reason.RANDOM_TIE
            );
        } else {
            winReason = new WinReason(
                    potentialWinners.get(0),
                    WinReason.Reason.COMPLETE
            );
        }

        return winReason;
    }

    /**
     * The number of items that the given team can potentially collect on the given bingo card
     * @param card The bingo card with items
     * @param team The team to calculate this for
     * @return An integer representing the number of items that can be collected
     */
    private int getPossibleNumCollections(BingoCard card, PlayerTeam team) {
        // Keep track of how many items can be collected in total by this team
        int possibleNumCollected = 0;

        for (BingoItem[] bingoItems : card.getBingoItems()) {
            for (BingoItem bingoItem : bingoItems) {
                if (bingoItem.hasCollected(team)) {
                    // If this item is locked, but has already been collected by the team, it still counts
                    possibleNumCollected++;
                } else if (!card.isItemLocked(bingoItem)) {
                    // If the item is not yet locked, it is possible for this team to still collect it
                    possibleNumCollected++;
                }
            }
        }

        return possibleNumCollected;
    }

    public void setNumLinesToComplete(int numLinesToComplete, boolean quidditchMode) {
        if (numLinesToComplete < 1 || numLinesToComplete > 10) {
            throw new IllegalArgumentException("Cannot set number of lines completed to less than 1 or more than 10");
        }

        this.fullCard = false;
        this.completionsToLock = 0;
        this.numLinesToComplete = numLinesToComplete;
        this.quidditchMode = quidditchMode;
    }

    /**
     * Sets the number of lines to complete in order to win
     * @param numLinesToComplete The number of lines to complete; must be between 1 (inclusive) and 10 (inclusive)
     */
    public void setNumLinesToComplete(int numLinesToComplete) {
        this.setNumLinesToComplete(numLinesToComplete, false);
    }

    public int getNumLinesToComplete() {
        return numLinesToComplete;
    }

    public boolean isQuidditchMode() {
        return quidditchMode;
    }

    public void setFullCard() {
        this.completionsToLock = 0;
        this.numLinesToComplete = 0;
        this.fullCard = true;
        this.quidditchMode = false;
    }

    public boolean isFullCard() {
        return fullCard;
    }

    public int getCompletionsToLock() {
        return completionsToLock;
    }

    public void setCompletionsToLock(int completionsToLock) {
        this.fullCard = false;
        this.numLinesToComplete = 0;
        this.completionsToLock = completionsToLock;
        this.quidditchMode = false;
    }
}
