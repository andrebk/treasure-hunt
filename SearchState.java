import java.util.LinkedList;
import java.util.ListIterator;

public class SearchState extends State implements Comparable<SearchState> {
    private LinkedList<Tile> targets;

    private LinkedList<SearchState> prevStates;
    private char prevAction;
    private int cost;
    private int heuristic = Integer.MAX_VALUE;
    private SearchMode mode = SearchMode.SAFE;


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

    SearchState(Agent agent, LinkedList<Tile> targets, SearchMode mode) {
        this((State) agent);
        this.targets = targets;
        this.mode = mode;
        prevStates = new LinkedList<>();
        prevAction = Character.MIN_VALUE; // null
        setCost(0);
        setHeuristic();
    }

    private SearchState(SearchState state) {
        this((State) state);
        this.targets = state.targets;
        this.mode = state.mode;
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

    public LinkedList<SearchState> expandState() {
        LinkedList<SearchState> newStates = new LinkedList<>();
        Position nextPos = getNextPos();
        Tile nextTile = getTile(nextPos.getX(), nextPos.getY());

        newStates.add(new SearchState(this, 'r'));
        newStates.add(new SearchState(this, 'l'));

        if (nextTile != null) {
            // Can't plan a path into unexplored territory

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

    public static void removeRepeatStates(LinkedList<SearchState> states) {
        // Goes through a List of SearchStates and removes all states that have a state S in their prevStates List
        // that satisfies state.sameState(S)

        SearchState state;
        ListIterator<SearchState> it = states.listIterator();

        while (it.hasNext()) {
            state = it.next();
            if (state.prevStates != null && state.prevStates.contains(state)) {
                it.remove();
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
        //TODO: Improve costs. Bomb cost dependent on number of bombs?
        Tile currentTile = getTileAtPos();
        Tile nextTile = getTile(getNextPos().getX(), getNextPos().getY());

        switch (action) {
            case 'r':
            case 'l':
                this.cost++;
                break;
            case 'f':
                if (currentTile.getType() == '~' && nextTile.getType() == ' ' && hasRaft) {
                    // Cost of moving from raft to land
                    this.cost += 20;
                } else if (currentTile.getType() == ' ' && nextTile.getType() == '~' && hasRaft) {
                    // Cost of moving from land to raft
                    if (knownTrees.size() > 0) {
                        this.cost += Math.ceil(20 / knownTrees.size());
                    } else {
                        this.cost += 20;
                    }
                } else {
                    this.cost++;
                }
                break;
            case 'c':
                if (hasRaft) {
                    // Encourage not chopping down trees if agent already has a raft
                    if (knownTrees.size() > 0) {
                        this.cost += Math.ceil(10 / knownTrees.size());
                    } else {
                        this.cost += 15;
                    }
                } else {
                    this.cost++;
                }
                break;
            case 'u':
                this.cost++;
                break;
            case 'b':
                switch (nextTile.getType()) {
                    case '*':
                        this.cost += 25;
                        break;
                    case '-':
                    case 't':
                        this.cost += 50;

                }
                break;
        }
    }

    public int getHeuristic() {
        return heuristic;
    }

    public void setHeuristic() {
        // Manhattan distance
        int newHeuristic;

        if (targets == null || targets.isEmpty()) {
            //throw new NullPointerException("Targets not set");
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

    public int getFCost() {
        return getCost() + getHeuristic();
    }

    public boolean samePosition(Tile tile) {
        return this.posX == tile.getX() && this.posY == tile.getY();
    }

    private boolean canMoveForward(Tile nextTile) {
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

    private boolean canCutTree(Tile nextTile) {
        if (nextTile == null) {
            return false;
        }

        switch (mode) {
            case SAFE:
                return false;
            case MODERATE:
            case FREE:
                return nextTile.getType() == 't' && hasAxe;
            case HYPOTHETICAL:
                return true;
            default:
                return false;
        }
    }

    private boolean canUnlock(Tile nextTile) {
        switch (mode) {
            case SAFE:
            case MODERATE:
            case FREE:
                return nextTile != null && nextTile.getType() == '-' && hasKey;
            case HYPOTHETICAL:
                return true;
            default:
                return false;
        }
    }

    private boolean canBlowUp(Tile nextTile) {
        if (nextTile == null) {
            return false;
        }

        switch (mode) {
            case SAFE:
            case MODERATE:
                return false;
            case FREE:
                switch (nextTile.getType()) {
                    case '*':
                    case 't':
                    case '-':
                        return hasDynamite;
                    default:
                        return false;
                }
            case HYPOTHETICAL:
                return true;
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

    private LinkedList<Tile> deepCopyLL(LinkedList<Tile> list) {
        LinkedList<Tile> tiles = new LinkedList<>();
        Tile newTile;

        for (Tile tile : list) {
            newTile = new Tile(tile);
            tiles.add(newTile);
        }
        return tiles;
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
        return (o instanceof SearchState) && sameState((SearchState) o);
    }
}
