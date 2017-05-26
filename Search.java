import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

/* The Search class implements the A* and Uniform Cost Search search algorithms to search through the state space
 * of the game.  */
public class Search {

    /* Search from the agents state to one of the provided targets, using the A* algorithm */
    public static LinkedList<Character> AStar(Agent agent, LinkedList<Tile> targets, SearchMode mode) throws NoPathFoundException {

        // If no targets where provided, don't try to search, simply return
        if (targets == null || targets.isEmpty()) {
            throw new NoPathFoundException("No targets provided");
        }

        return findPath(agent, targets, "AStar", mode);
    }

    /* Perform uniform cost search from the agents state. Considers any tile with unseen tiles around it a target */
    public static LinkedList<Character> UCS(Agent agent, SearchMode mode) throws NoPathFoundException {

        /* Pass in an empty linked list as target. This makes SearchState set the heuristic to zero, which makes
         * A* search the same as UCS */
        return findPath(agent, new LinkedList<Tile>(), "UCS", mode);
    }

    /* Perform A* or UCS search, depending on the inputs, and return the path to the target */
    private static LinkedList<Character> findPath(Agent agent, LinkedList<Tile> targets, String algorithm, SearchMode mode) throws NoPathFoundException {
        SearchState current;
        LinkedList<SearchState> newStates;
        boolean inOpen = false;

        PriorityQueue<SearchState> open = new PriorityQueue<>();
        HashMap<Integer,SearchState> openH = new HashMap<Integer,SearchState>();
        HashSet<SearchState> closed = new HashSet<>(); //TODO: Change to HashSet

        // Add the starting state to the set of open states
        SearchState firstState = new SearchState(agent, targets, mode);
        open.add(firstState);
        openH.put(firstState.hashCode(), firstState);

        // Search as long as there are open states, i.e. states that haven't been expanded
        while (!open.isEmpty()) {

            // Get the open state with lowest fCost
            current = open.poll();
            openH.remove(current.hashCode());
            closed.add(current);
            //System.out.println("Current path: " + current.getPathHere().toString());

            /* If we have reached a goal, return the path to it.
             * For A* the we have reached the goal if the current position is the same as the positon of a target */
            if (algorithm.equals("AStar")) {
                for (Tile target : targets) {
                    if (target.getX() == current.posX && target.getY() == current.posY) {
                        return current.getPathHere();
                    }
                }
            }

            // For UCS we have reached a goal if the current position has unseen tiles around it
            else if (algorithm.equals("UCS")) {
                if (current.numUnseenTiles() > 0) {
                    return current.getPathHere();
                }
            } else {
                throw new RuntimeException("Unknown search algorithm type");
            }

            // Expand the current state, and add / update the new states to open
            newStates = current.expandState();
            for (SearchState newState : newStates) {

                // The state already been searched, and is no longer of interest
                if (closed.contains(newState)) { // Works because SearchState overrides the equals method.
                    continue;
                }
                int hashCode = newState.hashCode();
                
                if(openH.containsKey(hashCode)){
                	if(newState.getFCost() < openH.get(hashCode).getFCost()){
                		openH.remove(hashCode);
                		open.add(newState);
                		openH.put(hashCode, newState);
                	}
                }
                else if(!openH.containsKey(hashCode)){
                	open.add(newState);
                }

//                // If state is in open, check if new path to it is better, and if so update it
//                Iterator<SearchState> it = open.iterator();
//                while (it.hasNext()) {
//                    inOpen = false;
//                    SearchState state = it.next();
//                    if (state.sameState(newState)) {
//                        inOpen = true;
//                        if (newState.getFCost() < state.getFCost()) {
//                            it.remove();
//                            open.add(newState);
//                            break;
//                        }
//
//                    }
//                }
//
//                // Add states that have not been seen before to the open set
//                if (!inOpen || open.isEmpty()) {
//                    open.add(newState);
//                }
            }
        }

        throw new NoPathFoundException("Exhausted all possibilities");
    }
}


class NoPathFoundException extends RuntimeException {
    NoPathFoundException() {
        super();
    }

    NoPathFoundException(String message) {
        super(message);
    }
}
