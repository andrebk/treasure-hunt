import java.util.LinkedList;
import java.util.ListIterator;

/* The SearchState class represents a state of the game that is found whilst searching through the statespace.
 * In addition to the game state, it keeps track of heuristics and costs, possible actions, previous actions.
 * It also has a search mode, that controls some of its behaviour, e.g. what actions it sees as possible */
public class SearchState extends State implements Comparable<SearchState> {
    private LinkedList<Tile> targets;

    private LinkedList<SearchState> prevStates;
    private char prevAction;
    private int cost;
    private int heuristic = Integer.MAX_VALUE;
    private SearchMode mode = SearchMode.SAFE;

    /* Helper constructor, sets the parameters that are shared between Agent and SearchState objects */
    private SearchState(State state) {
        this.map = state.map;
        this.posX = state.posX;
        this.posY = state.posY;
        this.dynamites = state.dynamites;
        this.hasDynamite = state.hasDynamite;
        this.hasAxe = state.hasAxe;
        this.hasKey = state.hasKey;
        this.hasRaft = state.hasRaft;
        this.hasTreasure = state.hasTreasure;
        this.direction = state.direction;
        this.doorsOpened = shallowCopyLL(state.doorsOpened);
        this.treesChopped = shallowCopyLL(state.treesChopped);
        this.tilesBlownUp = shallowCopyLL(state.tilesBlownUp);
        this.knownTrees = deepCopyLL(state.knownTrees);
        this.knownItems = deepCopyLL(state.knownItems);
        this.knownTreasures = deepCopyLL(state.knownTreasures);
    }

    /* Constructor for creating the initial SearchState from the current state of the agent */
    SearchState(Agent agent, LinkedList<Tile> targets, SearchMode mode) {
        this((State) agent);
        this.targets = targets;
        this.mode = mode;
        prevStates = new LinkedList<>();
        prevAction = Character.MIN_VALUE; // null
        setCost(0);
        setHeuristic();
    }

    /* Helper constructor for creating a new SearchState from an existing one */
    private SearchState(SearchState state) {
        this((State) state);
        this.targets = state.targets;
        this.mode = state.mode;
        prevStates = shallowCopyLL(state.prevStates);
        prevStates.addLast(state);
        // Doesn't set cost, heuristic or prevAction
    }

    /* Constructor for creating a SearchState that expands a previous state by doing an action */
    SearchState(SearchState state, char action) {
        this(state);

        // If map is about to change, do a deep copy so as to not mess up for other states.
        switch (action) {
            case 'u':
            case 'c':
            case 'b':
                this.map = deepCopyMap();
                break;
            case 'f':
                Position nextPos = getNextPos();
                if (getTile(nextPos.getX(), nextPos.getY()).getItem() != '0') {
                    this.map = deepCopyMap();
                }
        }

        prevAction = action;
        setCost(state.cost);
        increaseCost(action);
        updateState(action);
        setHeuristic();
    }

    /* Expands this state by creating all the new states that can be reached from it */
    public LinkedList<SearchState> expandState() {
        LinkedList<SearchState> newStates = new LinkedList<>();
        Position nextPos = getNextPos();
        Tile nextTile = getTile(nextPos.getX(), nextPos.getY());

        newStates.add(new SearchState(this, 'r'));
        newStates.add(new SearchState(this, 'l'));

        // Can't plan a path into unexplored territory
        if (nextTile != null) {

            if (canMoveForward(nextTile)) {
                newStates.add(new SearchState(this, 'f'));
            }
            if (canCutTree(nextTile)) {
                newStates.add(new SearchState(this, 'c'));
            }
            if (canUnlock(nextTile)) {
                newStates.add(new SearchState(this, 'u'));
            }
            if (canBlowUp(nextTile)) {
                newStates.add(new SearchState(this, 'b'));
            }
        }

        removeRepeatStates(newStates);
        return newStates;
    }

    /* Goes through a List of SearchStates and removes all states that have a state S in their prevStates list
     * that satisfies state.sameState(S) */
    public static void removeRepeatStates(LinkedList<SearchState> states) {
        SearchState state;
        ListIterator<SearchState> it = states.listIterator();

        while (it.hasNext()) {
            state = it.next();
            if (state.prevStates != null && state.prevStates.contains(state)) {
                it.remove();
            }
        }
    }

    /* Find the actions necessary to reach this state from the start state */
    public LinkedList<Character> getPathHere() {
        LinkedList<Character> path;

        if (prevAction != Character.MIN_VALUE) {
            path = prevStates.peekLast().getPathHere();
            path.addLast(prevAction);
            return path;
        } else {
            return new LinkedList<>();
        }
    }

    /* Get the action that was taken to reach this state from the previous state */
    public char getPrevAction() {
        return prevAction;
    }

    /* Gets the path cost of moving to this state */
    public int getCost() {
        return cost;
    }

    /* Sets the path cost of this state */
    private void setCost(int cost) {
        this.cost = cost;
    }

    /* Sets the cost of going to this state, dependent on the action that was taken to reach it */
    public void increaseCost(char action) {
        //TODO: Improve costs. Bomb cost dependent on number of bombs?
        Tile currentTile = getTileAtPos();
        Tile nextTile = getTile(getNextPos().getX(), getNextPos().getY());

        switch (action) {

            // Cost of turning
            case 'r':
            case 'l':
                this.cost++;
                break;

            // Cost of moving forward
            case 'f':

                // Cost of moving from raft to land
                if (currentTile.getType() == '~' && nextTile.getType() == ' ' && hasRaft) {
                    this.cost += 5;
                }

                // Cost of moving from land to raft. Cheaper if there are many trees on the map
                else if (currentTile.getType() == ' ' && nextTile.getType() == '~' && hasRaft) {
                    if (knownTrees.size() > 0) {
                        this.cost += Math.ceil(5 / knownTrees.size());
                    } else {
                        this.cost += 5;
                    }
                }

                // Cost of moving forward in the same environment
                else {
                    this.cost++;
                }
                break;

            // Cost of chopping down tree.
            case 'c':

                // Discourage chopping trees if agent already has a raft, and there are few trees
                if (hasRaft) {
                    if (knownTrees.size() > 0) {
                        this.cost += Math.ceil(10 / knownTrees.size());
                    } else {
                        this.cost += 10;
                    }
                }

                // If agent doesn't have a raft there is no downside to chopping a tree, so make it cheap
                else {
                    this.cost++;
                }
                break;

            // Cost of unlocking a door, always cheap as there is no downside
            case 'u':
                this.cost++;
                break;

            // Cost of using dynamite. Discourage blowing up tiles that can be removed using other tools
            case 'b':
                switch (nextTile.getType()) {
                    case '*':
                        this.cost += 15;
                        break;
                    case '-':
                    case 't':
                        this.cost += 20;

                }
                break;
        }
    }

    /* Get the heuristic value of this state, that is the approximate cost of reaching the target */
    public int getHeuristic() {
        return heuristic;
    }

    /* Calculate the heuristic for this state. Uses the Manhattan distance to the closes target.
     * If there are no targets, set the heuristic to zero. This makes uniform cost search possible with A* algorithm */
    public void setHeuristic() {
        int newHeuristic;

        if (targets == null || targets.isEmpty()) {
            this.heuristic = 0;
            return;
        }

        for (Tile target : targets) {
            newHeuristic = Math.abs(target.getX() - posX) + Math.abs(target.getY() - posY);
            if (newHeuristic < this.heuristic) {
                this.heuristic = newHeuristic;
            }
        }
    }

    /* Get the estimate total cost of reaching the goal from the start state */
    public int getFCost() {
        return getCost() + getHeuristic();
    }

    /* Check if the agent is on a given tile */
    public boolean samePosition(Tile tile) {
        return this.posX == tile.getX() && this.posY == tile.getY();
    }

    /* Check if the agent can move forward from this state */
    private boolean canMoveForward(Tile nextTile) {
        //TODO: Bring these if statement into the return statements?
        if (nextTile == null) {
            return false;
        }

        Tile currentTile = getTileAtPos();
        switch (mode) {
            case SAFE:
                // Do not go from land to water, or from water to land. To avoid wasting raft
                return currentTile.getType() == nextTile.getType();
            case MODERATE:
            case FREE:
            case HYPOTHETICAL:
                //TODO: Hypothetial search modde can not be set here, as updateState check what items agent has
                //TODO: Should be implemented as giving agent items in constructor, or similar

                // Can always move forward to land, but can only go into water if agent has a raft
                switch (nextTile.getType()) {
                    case ' ':
                        return true;
                    case '~':
                        return hasRaft;
                    default:
                        return false;
                }
            default:
                return true;
        }
    }

    /* Check if agent can perform the chop tree action from this state */
    private boolean canCutTree(Tile nextTile) {
        if (nextTile == null) {
            return false;
        }

        switch (mode) {
            case SAFE:
                // Force agent to not cut trees in safe search mode
                return false;
            case MODERATE:
            case FREE:
                return nextTile.getType() == 't' && hasAxe;
            case HYPOTHETICAL:
                return nextTile.getType() == 't';
            default:
                return false;
        }
    }

    /* Check if agent can perform the unlock door action from this state */
    private boolean canUnlock(Tile nextTile) {
        switch (mode) {
            case SAFE:
            case MODERATE:
            case FREE:
                return nextTile != null && nextTile.getType() == '-' && hasKey;
            case HYPOTHETICAL:
                return nextTile != null && nextTile.getType() == '-';
            default:
                return false;
        }
    }

    /* Check if agent can perform the blow up tile action from this state */
    private boolean canBlowUp(Tile nextTile) {
        if (nextTile == null) {
            return false;
        }

        switch (mode) {
            case SAFE:
            case MODERATE:
                // Never use dynamite when in safe or moderate search mode
                return false;
            case FREE:

                // Can use dynamite if the tile in front of the agent can be blown up, and it has dynamite in inventory
                switch (nextTile.getType()) {
                    case '*':
                    case 't':
                    case '-':
                        return hasDynamite;
                    default:
                        return false;
                }
            case HYPOTHETICAL:
                switch (nextTile.getType()) {
                    case '*':
                    case 't':
                    case '-':
                        return true;
                }
            default:
                return false;
        }
    }

    /* Helper method that does a shallow copy of a linked list */
    private <T> LinkedList<T> shallowCopyLL(LinkedList<T> list) {
        LinkedList<T> newList = new LinkedList<>();
        for (T item : list) {
            newList.addLast(item);
        }
        return newList;
    }

    /* Helper method that does a deep copy of a linked list of tiles */
    private LinkedList<Tile> deepCopyLL(LinkedList<Tile> list) {
        LinkedList<Tile> tiles = new LinkedList<>();
        Tile newTile;

        for (Tile tile : list) {
            newTile = new Tile(tile);
            tiles.add(newTile);
        }
        return tiles;
    }

    /* Overload the compareTo method. Needed for sorting in priority queue.
    * Sorts by fCost, and uses the heuristic as a tie breaker*/
    public int compareTo(SearchState state) {
        int comparison;

        comparison = Integer.compare(this.getFCost(), state.getFCost());
        if (comparison == 0) {
            comparison = Integer.compare(this.getHeuristic(), state.getHeuristic());
        }
        return comparison;
    }

    /* Override the equals method. Needed for contains() method for e.g. linked lists */
    @Override
    public boolean equals(Object o) {
        return (o instanceof SearchState) && sameState((SearchState) o);
    }
}
