/*
 *  Agent.java 
 *  Sample Agent for Text-Based Adventure Game
 *  COMP3411 Artificial Intelligence
 *  UNSW Session 1, 2017
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
