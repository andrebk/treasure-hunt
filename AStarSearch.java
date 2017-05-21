import java.util.LinkedList;
import java.util.PriorityQueue;

public class AStarSearch {


    public static LinkedList<Character> findPath(Agent agent, Tile start, Tile target) {
        SearchState current;
        LinkedList<SearchState> newStates;
        boolean inOpen = false;

        PriorityQueue<SearchState> open = new PriorityQueue<>();
        LinkedList<SearchState> closed = new LinkedList<>(); //TODO: Change to hashmap, or other more optimized structure.

        SearchState firstState = new SearchState(agent, target);
        open.add(firstState);

        while (!open.isEmpty()) {
            current = open.poll();
            closed.addLast(current);

            if (current.samePosition(target)) {
                return current.getPathHere();
            }

            newStates = current.expandState();

            for (SearchState newState : newStates) {
                if (closed.contains(newState)) {
                    //TODO: Change condition, this one probably doesn't work because it can be the same state without being the same object.
                    // Dependent on closed implementation
                    //TODO: Also check that this is not a repeated state.
                    continue;
                }


                for (SearchState state : open) {
                    inOpen = false;
                    if (state.sameState(newState)) {
                        if (newState.getFCost() < state.getFCost()) {
                            open.remove(state);
                            open.add(newState);
                            break;
                        }
                        inOpen = true;
                    }
                }
                if (!inOpen) {
                    open.add(newState);
                }
            }
        }

        throw new RuntimeException("Error in AStarSearch.java : findPath(). I think this means no possible path was found after exhausting all possibilities");
    }
}
