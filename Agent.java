/*
 * Briefly describe how your program works, including any algorithms and data structures employed, and explain any design decisions you made along the way.
 *
 * The agent uses A* search and uniform cost search to search through the state space for a sequence of actions that
 * will lead to a better state than it is currently in. A state is described not only by the agents position on the map,
 * but also which items it has, what items it has discovered, what tiles it has changed, etc. When searching for a path
 * the search algorithms will expand each state to find the states that the agent would be in for each of the actions it
 * could perform in its previous state. Thus it is not only searching for a path in a two dimensional world, as it can
 * be in the same position, but not the same state, because of e.g. using an item. The goals the agent will try to
 * find a way to fulfill are, in prioritized order:
 *
 * - Getting back to start, if it has the treasure
 * - Exploring, without performing actions that can't be reversed (like blowing up tiles, or using/losing the raft)
 * - Picking up the treasure, if it knows where it is
 * - Picking up an item, if it knows where one or more are
 * - Exploring, with chopping trees and using/losing the raft allowed
 * - Exploring, with all actions allowed (including using dynamite)
 *
 * Searching for a "path" to a goal (a tile with a treasure or an item) is done using A* search. The heuristic is simply
 * the Manhattan distance from the current position to the target tile. Exploring is done using the same implementation
 * as the A* search, but with no targets. This means the heuristic gets set to 0, making the search the same as
 * uniform cost search. The exploration search terminates when a tile with unseen tiles around it is found, so finds the
 * "closest" unexplored tile. The implementation uses a hashset to keep track of closed states, for quick lookup. The open
 * states are stored in a priority queue, that sorts the states by their fCost (that is, the path cost + heuristic).
 * Because it is very slow to find a particular state in the priority queue, the open states are also listed in a
 * hashmap. That way it is quick to check whether or not a newly expanded state is a cheaper path to a known state,
 * and the costly iteration through the priority queue only needs to be done when it is known to be necessary to update
 * a state.
 *
 * The state of the game is described in the State class. This class is extended by both the Agent and SearchState
 * classes, as they both need the same information about the state of the game. The agent stores the actual state of
 * the game, whilst the SearchState objects store the state that will occur as a result of a specific sequence of
 * actions. The SearchState class also keeps track of parameters important for searching, such as the heuristics and
 * costs, and the search mode. The search mode limits what actions the agent is allowed to do, to avoid making rash
 * or uninformed decisions. The state also stores the world map. This is implemented as an array of arrays of
 * Tile objects, where each object represents one square of the map. The map is 164 by 164, in order to be able to fit
 * a 80x80 map with a random start position. This large size means that most of the array will never be utilized, and as
 * such it is extremely inefficient to compare two maps to see if they are different. So keeping track of changes in the
 * map between states is done by storing which tiles have been changed in linked lists, one each for the three ways to
 * remove tiles.
 *
 * When testing locally, the agent is able to solve all the provided maps (s0 - s9), except s7 and s9. For the s7 map
 * the problem is the size of the map, combined with the many trees and wall locked treasure. When the agent tries to
 * plan a path to the treasure, it will explore all options before giving up. After chopping a tree the agent can go
 * back to a previous position, and still be considered to be in a new state, because a tile has been removed. This
 * means that every time the agent chops a tree, it has to search through the entire map again, before it can be sure it
 * has tried all paths to reach the treasure. On a large map with lots of trees, like s7, this is not possible before
 * running out of memory. The s9 map the agent is able to solve, but it takes around three minutes, compared to all
 * other solved maps being done in less than five seconds. This is due to a combination of 3 things: The same problem
 * as in s7, with removing tiles allowing the agent to search through states it has already been in. And second that the
 * heuristic is not very helpful when the target is the treasure, but the agent has to collect dynamite first. Third the
 * fact that using dynamite has a high cost, which leads the agent to spend a lot of time exploring other options,
 * before allowing itself to use the dynamite.
 */

import java.io.*;
import java.net.*;
import java.util.LinkedList;

public class Agent extends State {

    private boolean logPrint = false;
    private LinkedList<Character> plan = new LinkedList<>();

    /* Default constructor. Initializes position of the agent to the center of the map */
    Agent() {
        super();
        posX = start;
        posY = start;
    }

    /* Gets an action form a human player using the keyboard, and returns it if is is valid */
    private char getHumanAction() throws IOException {
        int ch = 0;
        System.out.print("Enter Action(s): ");

        while (ch != -1) {
            // read character from keyboard
            ch = System.in.read();
            ch = Character.toLowerCase(ch);

            // Return the action if it is valid
            switch (ch) {
                case 'f':
                case 'l':
                case 'r':
                case 'c':
                case 'u':
                case 'b':
                    return ((char) ch);
            }
        }
        throw new RuntimeException("Error occurred when getting player input");
    }

    /* Finds the next action to be performed. The agent will either return the next step in a preexisting plan,
     * or try to plan a route to one of it's (sub)goals.
     */
    char get_action(char view[][]) {
        char action;

        updateMap(view);

        // If there already exists a plan, return the next step in that plan
        if (!plan.isEmpty()) {
            if (logPrint) System.out.println("Preexisting plan, executing next step: " + plan.peekFirst());

            action = plan.removeFirst();
            updateState(action);
            return action;
        }

        if (logPrint) {
            printMap();
            printState();
        }

        // If the agent has picked up a treasure, plan a route back to the start to win the game
        if (hasTreasure) {
            LinkedList<Tile> home = new LinkedList<>();
            home.add(getTile(start, start));

            try {
                if (logPrint) System.out.println("Have treasure, planning path home...");
                plan = Search.AStar(this, home, SearchMode.FREE);
                if (logPrint) System.out.println("Found path home, executing: " + plan.peekFirst());

                action = plan.removeFirst();
                updateState(action);
                return action;
            } catch (NoPathFoundException e) {
                if (logPrint) System.out.println("Could not find path home: " + e.getMessage());
            }
        }

        /* Search for places to explore, that don't require actions that can't be undone.
         * The agent will not chop trees, go between land and water, or blow up tiles. Unlocking doors is allowed
         */
        try {
            if (logPrint) System.out.println("Planning safe exploration...");
            plan = Search.UCS(this, SearchMode.SAFE);
            if (logPrint) System.out.println("Found safe exploration path, executing: " + plan.peekFirst());

            action = plan.removeFirst();
            updateState(action);
            return action;
        } catch (NoPathFoundException e) {
            if (logPrint) System.out.println("Could not find safe exploration: " + e.getMessage());
        }

        /* If the agent knows the location of treasure, it tries to plan a path to it */
        if (!knownTreasures.isEmpty()) {
            try {
                if (logPrint) System.out.println("Know where treasure is, planning path to it...");
                plan = Search.AStar(this, knownTreasures, SearchMode.FREE);
                if (logPrint) System.out.println("Found path to treasure, executing: " + plan.peekFirst());

                action = plan.removeFirst();
                updateState(action);
                return action;
            } catch (NoPathFoundException e) {
                if (logPrint) System.out.println("Could not find path to treasure: " + e.getMessage());
            }

        }

        /* If the agent knows the location of any items (keys, dynamite or axes), it tries to plan a path to one */
        if (!knownItems.isEmpty()) {
            try {
                if (logPrint) System.out.println("Know where item(s) are, planning path to one...");
                plan = Search.AStar(this, knownItems, SearchMode.FREE);
                if (logPrint) System.out.println("Found path to item, executing: " + plan.peekFirst());

                action = plan.removeFirst();
                updateState(action);
                return action;
            } catch (NoPathFoundException e) {
                if (logPrint) System.out.println("Could not find path to item: " + e.getMessage());
            }
        }

        /* If none of the previous searches produced viable plans, more exploration is probably necessary.
         * This exploration will allow chopping trees and using the raft, in order to reach new places */
        try {
            if (logPrint) System.out.println("Planning moderate exploration...");
            plan = Search.UCS(this, SearchMode.MODERATE);
            if (logPrint) System.out.println("Found moderate exploration path, executing: " + plan.peekFirst());

            action = plan.removeFirst();
            updateState(action);
            return action;
        } catch (NoPathFoundException e) {
            if (logPrint) System.out.println("Could not find moderate exploration: " + e.getMessage());
        }

        /* If all else fails, the agent is allowed to use all methods in order to explore, including using dynamite */
        try {
            if (logPrint) System.out.println("Planning exploration...");
            plan = Search.UCS(this, SearchMode.FREE);
            if (logPrint) System.out.println("Found exploration path, executing: " + plan.peekFirst());

            action = plan.removeFirst();
            updateState(action);
            return action;
        } catch (NoPathFoundException e) {
            if (logPrint) System.out.println("Could not find unmapped area: " + e.getMessage());
        }


        /* If the agent can not find any viable action to take, a human player can help it out.
         * Mostly for debugging purposes */
        try {
            action = getHumanAction();
            updateState(action);

            return action;
        } catch (IOException e) {
            System.out.println("IO error:" + e);
        }

        return 0;
    }

    /* Print the agents current view. Part of the provided class */
    private void print_view(char view[][]) {
        int i, j;

        System.out.println("\n+-----+");
        for (i = 0; i < 5; i++) {
            System.out.print("|");
            for (j = 0; j < 5; j++) {
                if ((i == 2) && (j == 2)) {
                    System.out.print('^');
                } else {
                    System.out.print(view[i][j]);
                }
            }
            System.out.println("|");
        }
        System.out.println("+-----+");
    }

    /* Connect to the game host, and communicate with it to play the game. Part of the provided class */
    public static void main(String[] args) {
        InputStream in = null;
        OutputStream out = null;
        Socket socket = null;
        Agent agent = new Agent();
        char view[][] = new char[5][5];
        char action;
        int port;
        int ch;
        int i, j;

        if (args.length < 2) {
            System.out.println("Usage: java Agent -p <port>\n");
            System.exit(-1);
        }

        port = Integer.parseInt(args[1]);

        try { // open socket to Game Engine
            socket = new Socket("localhost", port);
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            System.out.println("Could not bind to port: " + port);
            System.exit(-1);
        }

        try { // scan 5-by-5 window around current location
            while (true) {
                for (i = 0; i < 5; i++) {
                    for (j = 0; j < 5; j++) {
                        if (!((i == 2) && (j == 2))) {
                            ch = in.read();
                            if (ch == -1) {
                                System.exit(-1);
                            }
                            view[i][j] = (char) ch;
                        }
                    }
                }
                //agent.print_view(view); // COMMENT THIS OUT BEFORE SUBMISSION
                action = agent.get_action(view);
                out.write(action);
            }
        } catch (IOException e) {
            System.out.println("Lost connection to port: " + port);
            System.exit(-1);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Failed to close socket");
            }
        }
    }
}
