
public class SearchState extends State {
    private int targetX, targetY;

    private char[] prevActions;
    //TODO: Store previous states as well? Check for repetition
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
        setCost(0);
        setHeuristic();
    }

    SearchState(SearchState state) {
        this((State) state);
        this.targetX = state.targetX;
        this.targetY = state.targetY;
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

    private void setCost(int cost) {
        this.cost = cost;
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
/*
    private boolean canMoveForward() {

    }

    private boolean canCutTree() {

    }

    private boolean canUnlock() {

    }

    private boolean canBlowUp() {

    }
    */
}
