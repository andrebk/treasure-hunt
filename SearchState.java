import java.util.PriorityQueue;

public class SearchState extends State {
    private int targetX, targetY;

    private char[] prevActions;
    //TODO: Store previous states as well? Check for repetion
    private int cost = 0;
    private int heuristic = 0;

    //TODO: List of known treasure positions?
    //TODO: List of known item positions?


    SearchState(Tile[][] map, int x, int y, int dynamites, boolean hasAxe, boolean hasKey, boolean hasRaft, boolean hasTreasure, int dir) {
        this.map = map; //TODO: Is this a new map, or a reference to the old one?
        //TODO: Maybe do a deep copy of the map, and then pass the reference to later states?
        //TODO: Might not work, as it could be hard to undo changes when going back in the search tree.
        this.posX = x;
        this.posY = y;
        this.dynamites = dynamites;
        this.hasDynamite = dynamites > 0;
        this.hasAxe = hasAxe;
        this.hasKey = hasKey;
        this.hasRaft = hasRaft;
        this.hasTreasure = hasTreasure;
        this.direction = dir;
        //TODO: Set target coordinates
        //TODO: Set cost, heuristic and prevActions
    }

    SearchState(SearchState state) {
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
        this.targetX = state.targetX;
        this.targetY = state.targetY;
        //TODO: Set cost, heuristic and prevActions
    }

    SearchState(SearchState state, char action) {
        this(state);
        //TODO: Add action to prevActions and update costs
        //TODO: Update state, as in agent code
    }

    public void expandState() {
        //TODO: Expand this state. Create new states for all possible actions, and calculate their cost/heuristic
    }

    public char[] getPrevActions() {
        return prevActions;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(char action) {
        //TODO: Improve costs. Extra cost for moving to land and losing the raft. Bomb cost dependent on number of bombs?
        switch (action) {
            case 'r':
            case 'l':
            case 'f':
                this.cost++;
                break;
            case 'c':
            case 'u':
                this.cost++;
                break;
            case 'b':
                this.cost += 20;
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

    private boolean canMoveForward() {

    }

    private boolean canCutTree() {

    }

    private boolean canUnlock() {

    }

    private boolean canBlowUp() {

    }
}
