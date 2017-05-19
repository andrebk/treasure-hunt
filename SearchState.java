import java.util.PriorityQueue;

public class SearchState {
    private Tile[][] map;
    private int posX, posY;

    private int dynamites = 0;
    private boolean hasDynamite = false;
    private boolean hasAxe = false;
    private boolean hasKey = false;
    private boolean hasRaft = false;
    private boolean hasTreasure = false;

    private int direction = Agent.NORTH;

    private char[] prevActions;
    private int cost = 0;
    private int heuristic = 0;

    PriorityQueue actions;

    //TODO: List of known treasure positions?
    //TODO: List of known item positions?


    SearchState(Tile[][] map, int x, int y, int dynamites, boolean hasAxe, boolean hasKey, boolean hasRaft, boolean hasTreasure, int dir) {
        this.map = map; //TODO: Is this a new map, or a reference to the old one?
        this.posX = x;
        this.posY = y;
        this.dynamites = dynamites;
        if (dynamites > 0) {
            hasDynamite = true;
        } else {
            hasDynamite = false;
        }
        this.hasAxe = hasAxe;
        this.hasKey = hasKey;
        this.hasRaft = hasRaft;
        this.hasTreasure = hasTreasure;
        this.direction = dir;
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
        //TODO: Set cost, heuristic and prevActions
    }

    SearchState(SearchState state, char action) {
        this(state);
        //TODO: Add action to prevActions and update costs
    }

    public void expandState(){
        //TODO: Expand this state. Create new states for all possible actions, and calculate their cost/heuristic
    }

    public char[] getPrevActions() {
        return prevActions;
    }

    public int getCost() {
        return cost;
    }

    public void setCost(int cost) {
        //TODO: Set based on previous actions
        this.cost = cost;
    }

    public int getHeuristic() {
        return heuristic;
    }

    public void setHeuristic(int h) {
        //TODO: Set based on the actual goal
        this.heuristic = h;
    }

}
