import java.util.LinkedList;

public class SearchState extends State implements Comparable<SearchState> {
    private int targetX, targetY;

    private LinkedList<SearchState> prevStates;//TODO: Check for repetition
    private char prevAction;
    private int cost, heuristic;

    //TODO: List of known treasure positions?
    //TODO: List of known item positions?


    SearchState(State state) {
        this.map = state.map; //TODO: Is this a new map, or a reference to the old one?
        //TODO: Maybe do a deep copy of the map, and then pass the reference to later states?
        //TODO: Might not work, as it could be hard to undo changes when going back in the search tree.
        //TODO: Maybe do deep copy only when actions that change the map are performed.
        this.posX = state.posX;
        this.posY = state.posY;
        this.dynamites = state.dynamites;
        this.hasDynamite = state.hasDynamite;
        this.hasAxe = state.hasAxe;
        this.hasKey = state.hasKey;
        this.hasRaft = state.hasRaft;
        this.hasTreasure = state.hasTreasure;
        this.direction = state.direction;

        //TODO: This constructor is called by other constructors. Those need to set SearchState specific parameters like:
        //TODO: Set target coordinates, cost, heuristic and prevActions
    }

    SearchState(Agent agent, Tile target) {
        this((State) agent);
        this.targetX = target.getX();
        this.targetY = target.getY();
        prevStates = new LinkedList<>();
        prevAction = Character.MIN_VALUE; // null
        setCost(0);
        setHeuristic();
    }

    private SearchState(SearchState state) {
        this((State) state);
        this.targetX = state.targetX;
        this.targetY = state.targetY;
        prevStates = shallowCopyLL(state.prevStates);
        prevStates.addLast(state);
        // Doesn't set cost, heuristic or prevAction
    }

    SearchState(SearchState state, char action) {
        this(state);

        // If map is about to change, do a deep copy so as to not mess up for other states.
        switch (action) {
            case 'u':
            case 'c':
            case 'b':
                this.map = deepCopyMap();
        }

        prevAction = action;
        setCost(state.cost);
        increaseCost(action);
        updateState(action);
        setHeuristic();
    }

    public LinkedList<SearchState> expandState() {
        //TODO: Check for repeats here?
        LinkedList<SearchState> newStates = new LinkedList<>();
        Position nextPos = getNextPos();
        Tile nextTile = getTile(nextPos.getX(), nextPos.getY());

        newStates.add(new SearchState(this, 'r'));
        newStates.add(new SearchState(this, 'l'));

        if (nextTile == null) {
            // Can't plan a path into unexplored territory
            return newStates;
        }
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
        return newStates;
    }

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

    public char getPrevAction() {
        return prevAction;
    }

    public int getCost() {
        return cost;
    }

    private void setCost(int cost) {
        this.cost = cost;
    }

    public void increaseCost(char action) {
        //TODO: Improve costs. Extra cost for moving to land and losing the raft. Bomb cost dependent on number of bombs?
        switch (action) {
            case 'r':
            case 'l':
            case 'f':
                this.cost++;
                break;
            case 'c':
                if (hasRaft) {
                    // Encourage not chopping down trees if agent already has a raft
                    this.cost += 15;
                } else {
                    this.cost++;
                }
                break;
            case 'u':
                this.cost++;
                break;
            case 'b':
                this.cost += 25;
                break;
        }
    }

    public int getHeuristic() {
        return heuristic;
    }

    public void setHeuristic() {
        // Manhattan distance
        this.heuristic = Math.abs(targetX - posX) + Math.abs(targetY - posY);
    }

    public int getFCost() {
        return getCost() + getHeuristic();
    }

    public int compareTo(SearchState state) {
        int comparison;

        comparison = Integer.compare(this.getFCost(), state.getFCost());
        if (comparison == 0) {
            comparison = Integer.compare(this.getHeuristic(), state.getHeuristic());
        }
        return comparison;
    }

    public boolean samePosition(Tile tile) {
        return this.posX == tile.getX() && this.posY == tile.getY();
    }

    private boolean canMoveForward(Tile nextTile) {
        switch (nextTile.getType()) {
            case ' ':
                return true;
            case '~':
                return hasRaft;
            default:
                return false;
        }

    }

    private boolean canCutTree(Tile nextTile) {
        return nextTile.getType() == 't' && hasAxe;
    }

    private boolean canUnlock(Tile nextTile) {
        return nextTile.getType() == '-' && hasKey;
    }

    private boolean canBlowUp(Tile nextTile) {
        switch (nextTile.getType()) {
            case '*':
            case 't':
            case '-':
                return hasDynamite;
            default:
                return false;
        }
    }

    private <T> LinkedList<T> shallowCopyLL(LinkedList<T> list) {
        LinkedList<T> newList = new LinkedList<>();
        for (T item : list) {
            newList.addLast(item);
        }
        return newList;
    }
}
