import java.util.Iterator;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Search {

    public static LinkedList<Character> AStar(Agent agent, LinkedList<Tile> targets, SearchMode mode) throws NoPathFoundException {
        // If no targets where provided, don't try to search, simply return
        if (targets == null || targets.isEmpty()) {
            throw new NoPathFoundException("No targets provided");
        }

        return findPath(agent, targets, "AStar", mode);
    }

    public static LinkedList<Character> UCS(Agent agent, SearchMode mode) throws NoPathFoundException {
        return findPath(agent, new LinkedList<Tile>(), "UCS", mode);
    }


    private static LinkedList<Character> findPath(Agent agent, LinkedList<Tile> targets, String algorithm, SearchMode mode) throws NoPathFoundException {
        SearchState current;
        LinkedList<SearchState> newStates;
        boolean inOpen = false;

        PriorityQueue<SearchState> open = new PriorityQueue<>();
        LinkedList<SearchState> closed = new LinkedList<>(); //TODO: Change to hashmap, or other more optimized structure.


        // Add the starting state to the set of open states
        SearchState firstState = new SearchState(agent, targets, mode);
        open.add(firstState);

        // Search as long as there are open states, i.e. states that haven't been expanded
        while (!open.isEmpty()) {
            current = open.poll();
            closed.addLast(current);
            //System.out.println("Current path: " + current.getPathHere().toString());


            // If we have reached a goal, return the path to it
            if (algorithm.equals("AStar")) {
                for (Tile target : targets) {
                    if (target.getX() == current.posX && target.getY() == current.posY) {
                        return current.getPathHere();
                    }
                }
            } else if (algorithm.equals("UCS")) {
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


                // If state is in open, check if new path to it is better, and if so update it
                Iterator<SearchState> it = open.iterator();
                while (it.hasNext()) {
                    inOpen = false;
                    SearchState state = it.next();
                    if (state.sameState(newState)) {
                        inOpen = true;
                        if (newState.getFCost() < state.getFCost()) {
                            it.remove();
                            open.add(newState);
                            break;
                        }

                    }
                }

                // Add states that have not been seen before to the open set
                if (!inOpen || open.isEmpty()) {
                    open.add(newState);
                }
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
