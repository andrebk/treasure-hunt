/*********************************************
 *  Agent.java 
 *  Sample Agent for Text-Based Adventure Game
 *  COMP3411 Artificial Intelligence
 *  UNSW Session 1, 2017
 */

import java.io.*;
import java.net.*;
import java.util.LinkedList;
import java.util.Queue;

public class Agent extends State {

    LinkedList<Character> plan = new LinkedList<>();

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
    public char get_action(char view[][]) {
        char action;
        long startTime = System.nanoTime(), stopTime, duration;

        updateMap(view);

        // If there already exists a plan, return the next step in that plan
        if (!plan.isEmpty()) {
            System.out.println("Preexisting plan, executing next step: " + plan.peekFirst());

            action = plan.removeFirst();
            updateState(action);
            return action;
        }

        printMap();
        printState();

        // If the agent has picked up a treasure, plan a route back to the start to win the game
        if (hasTreasure) {
            LinkedList<Tile> home = new LinkedList<>();
            home.add(getTile(start, start));

            try {
                System.out.println("Have treasure, planning path home...");
                startTime = System.nanoTime();
                plan = Search.AStar(this, home, SearchMode.FREE);

                stopTime = System.nanoTime();
                duration = (stopTime - startTime) / 1000000;
                System.out.println("Found path home in " + duration + " ms, executing: " + plan.peekFirst());

                action = plan.removeFirst();
                updateState(action);
                return action;
            } catch (NoPathFoundException e) {
                stopTime = System.nanoTime();
                duration = (stopTime - startTime) / 1000000;
                System.out.println("Could not find path home [" + duration + " ms]: " + e.getMessage());
            }
        }

        /* Search for places to explore, that don't require actions that can't be undone.
         * The agent will not chop trees, go between land and water, or blow up tiles. Unlocking doors is allowed
         */
        try {
            System.out.println("Planning safe exploration...");
            startTime = System.nanoTime();
            plan = Search.UCS(this, SearchMode.SAFE);

            stopTime = System.nanoTime();
            duration = (stopTime - startTime) / 1000000;
            System.out.println("Found safe exploration path in " + duration + " ms, executing: " + plan.peekFirst());

            action = plan.removeFirst();
            updateState(action);
            return action;
        } catch (NoPathFoundException e) {
            stopTime = System.nanoTime();
            duration = (stopTime - startTime) / 1000000;
            System.out.println("Could not find safe exploration [" + duration + " ms]: " + e.getMessage());
        }

        /* If the agent knows the location of treasure, it tries to plan a path to it */
        if (!knownTreasures.isEmpty()) {

            /* First it tries to plan a path using the hypothetical search mode. This mode allows using items
             * the agent doesn't have, and is used to evaluate if there exists a feasible path to the treasure */
            try {
                System.out.println("Know where treasure is, planning hypothetical path to it...");
                startTime = System.nanoTime();
                plan = Search.AStar(this, knownTreasures, SearchMode.HYPOTHETICAL);

                stopTime = System.nanoTime();
                duration = (stopTime - startTime) / 1000000;
                System.out.println("Found hypothetical path to treasure in " + duration + " ms");
            } catch (NoPathFoundException e) {
                stopTime = System.nanoTime();
                duration = (stopTime - startTime) / 1000000;
                System.out.println("Could not find hypothetical path to treasure [" + duration + " ms]: " + e.getMessage());
            }

            // If the hypothetical search produces a plan that can be executed, the agent will use that plan
            if (hasViablePlan()) {
                System.out.println("Hypothetical plan to treasure is viable, executing " + plan.peekFirst());
                action = plan.removeFirst();
                updateState(action);
                return action;
            }
                /*
                try {
                    System.out.println("Know where treasure is, planning path to it...");
                    startTime = System.nanoTime();
                    plan = Search.AStar(this, knownTreasures, SearchMode.FREE);

                    stopTime = System.nanoTime();
                    duration = (stopTime - startTime) / 1000000;
                    System.out.println("Found path to treasure in " + duration + " ms, executing: " + plan.peekFirst());

                    action = plan.removeFirst();
                    updateState(action);
                    return action;
                } catch (NoPathFoundException e) {
                    stopTime = System.nanoTime();
                    duration = (stopTime - startTime) / 1000000;
                    System.out.println("Could not find path to treasure [" + duration + " ms]: " + e.getMessage());
                }
                */
        }

        /* If the agent knows the location of any items (keys, dynamite or axes), it tries to plan a path to one */
        if (!knownItems.isEmpty()) {

            // First it tries to search using the hypothetical sear mode, to see if the items can be reached
            try {
                System.out.println("Know where item(s) are, planning hypothetical path to one...");
                startTime = System.nanoTime();
                plan = Search.AStar(this, knownItems, SearchMode.HYPOTHETICAL);

                stopTime = System.nanoTime();
                duration = (stopTime - startTime) / 1000000;
                System.out.println("Found hypothetical path to item in " + duration + " ms");
            } catch (NoPathFoundException e) {
                stopTime = System.nanoTime();
                duration = (stopTime - startTime) / 1000000;
                System.out.println("Could not find hypothetical path to item [" + duration + " ms]: " + e.getMessage());
            }

            // If the hypothetical search produces a plan that can be executed, the agent will use that plan
            if (hasViablePlan()) {
                System.out.println("Hypothetical plan to item is viable, executing " + plan.peekFirst());
                action = plan.removeFirst();
                updateState(action);
                return action;
            }

            /*
            try {
                System.out.println("Know where item(s) are, planning path to one...");
                startTime = System.nanoTime();
                plan = Search.AStar(this, knownItems, SearchMode.FREE);

                stopTime = System.nanoTime();
                duration = (stopTime - startTime) / 1000000;
                System.out.println("Found path to item in " + duration + " ms, executing: " + plan.peekFirst());

                action = plan.removeFirst();
                updateState(action);
                return action;
            } catch (NoPathFoundException e) {
                stopTime = System.nanoTime();
                duration = (stopTime - startTime) / 1000000;
                System.out.println("Could not find path to item [" + duration + " ms]: " + e.getMessage());
            }
            */
        }

        /* If none of the previous searches produced viable plans, more exploration is probably necessary.
         * This exploration will allow chopping trees and using the raft, in order to reach new places */
        try {
            System.out.println("Planning moderate exploration...");
            startTime = System.nanoTime();
            plan = Search.UCS(this, SearchMode.MODERATE);

            stopTime = System.nanoTime();
            duration = (stopTime - startTime) / 1000000;
            System.out.println("Found moderate exploration path in " + duration + " ms, executing: " + plan.peekFirst());

            action = plan.removeFirst();
            updateState(action);
            return action;
        } catch (NoPathFoundException e) {
            stopTime = System.nanoTime();
            duration = (stopTime - startTime) / 1000000;
            System.out.println("Could not find moderate exploration [" + duration + " ms]: " + e.getMessage());
        }

        /* If all else fails, the agent is allowed to use all methods in order to explore, including using dynamite */
        try {
            System.out.println("Planning exploration...");
            startTime = System.nanoTime();
            plan = Search.UCS(this, SearchMode.FREE);

            stopTime = System.nanoTime();
            duration = (stopTime - startTime) / 1000000;
            System.out.println("Found exploration path in " + duration + " ms, executing: " + plan.peekFirst());

            action = plan.removeFirst();
            updateState(action);
            return action;
        } catch (NoPathFoundException e) {
            stopTime = System.nanoTime();
            duration = (stopTime - startTime) / 1000000;
            System.out.println("Could not find unmapped area [" + duration + " ms]: " + e.getMessage());
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

    /* Checks if the plan the agent has found can be executed with the items it currently possesses */
    private boolean hasViablePlan() {
        int chops = 0;
        int opens = 0;
        int explosions = 0;
        for (char action : plan) {
            switch (action) {
                case 'u':
                    opens++;
                    break;
                case 'c':
                    chops++;
                    break;
                case 'b':
                    explosions++;
            }
        }
        return (chops > 0 == hasAxe) && (opens > 0 == hasKey) && explosions <= dynamites;
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
        char action = 'F';
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
            }
        }
    }
}
