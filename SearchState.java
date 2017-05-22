import java.util.LinkedList;

public class SearchState extends State implements Comparable<SearchState> {
    private int targetX, targetY;

    private LinkedList<SearchState> prevStates;//TODO: Check for repetition
    private char prevAction;
    private int cost, heuristic;

    //TODO: List of known treasure positions?
    //TODO: List of known item positions?


    SearchState(State state) {
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
            //removeRepeatStates(newStates);
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

        //removeRepeatStates(newStates);
        return newStates;
    }

    public static void removeRepeatStates(LinkedList<SearchState> states) {
        // Goes through a List of SearchStates and removes all states that have a state S in their prevStates List
        // that satisfies state.sameState(S)
        for (SearchState state : states) {
            if (state.prevStates != null && state.prevStates.contains(state)) {
                states.remove(state);
            }
        }
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

    // Needed for sorting in priority queue
    public int compareTo(SearchState state) {
        int comparison;

        comparison = Integer.compare(this.getFCost(), state.getFCost());
        if (comparison == 0) {
            comparison = Integer.compare(this.getHeuristic(), state.getHeuristic());
        }
        return comparison;
    }

    // Needed for List.contains in AStarSearch. Might be obsolete if data structures change
    @Override
    public boolean equals(Object o) {
        //return (o instanceof Thing) && (this.x == ((Thing) o).x);
        return (o instanceof SearchState) && sameState((SearchState) o);
    }
}
