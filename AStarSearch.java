import java.util.LinkedList;
import java.util.ListIterator;
import java.util.PriorityQueue;

public class AStarSearch {


    public static LinkedList<Character> findPath(Agent agent, LinkedList<Tile> targets) {
        SearchState current;
        LinkedList<SearchState> newStates;
        boolean inOpen = false;

        PriorityQueue<SearchState> open = new PriorityQueue<>();
        LinkedList<SearchState> closed = new LinkedList<>(); //TODO: Change to hashmap, or other more optimized structure.

        if (targets == null || targets.isEmpty()) {
            System.out.println("No targets provided");
            return new LinkedList<>();
        }

        SearchState firstState = new SearchState(agent, targets);
        open.add(firstState);

        while (!open.isEmpty()) {
            current = open.poll();
            closed.addLast(current);
            //System.out.println("Current path: " + current.getPathHere().toString());


            for (Tile target : targets) {
                if (target.getX() == current.posX && target.getY() == current.posY) {
                    return current.getPathHere();
                }
            }

            newStates = current.expandState();

            for (SearchState newState : newStates) {
                if (closed.contains(newState)) { // Works because SearchState overrides the equals method.
                    continue;
                }


                //TODO: Possibly rewrite this so it doesn't remove in a foreach loop
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
                if (!inOpen || open.isEmpty()) {
                    open.add(newState);
                }
            }
        }

        //TODO: Do something if no path is found? What should be returned? Throw exception and handle it outside?
        //throw new RuntimeException("Error in AStarSearch.java : findPath(). No possible path was found after exhausting all possibilities");
        System.out.println("Did not find a path");
        return new LinkedList<>();
    }
}
