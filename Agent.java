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

    Agent() {
        super();
        posX = start;
        posY = start;
    }

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

    public char get_action(char view[][]) {

        // REPLACE THIS CODE WITH AI TO CHOOSE ACTION

        char action;

        updateMap(view);
        printMap();
        printState();
        System.out.println("Known treasures: " + knownTreasures.toString());
        System.out.println("Known items: " + knownItems.toString());

//        System.out.println("Starting pathfinding...");
//        long startTime = System.nanoTime();
//        plan = AStarSearch.findPath(this, knownTreasures);
//        long stopTime = System.nanoTime();
//        long duration = (stopTime - startTime) / 1000000;
//
//        System.out.println("Found path in " + duration + " milliseconds");
//        System.out.println("Path is: " + plan.toString());

        if (!plan.isEmpty()) {
            System.out.println("Preexisting plan, executing next step");

            action = plan.removeFirst();
            updateState(action);
            return action;
        }

        if (hasTreasure) {
            LinkedList<Tile> home = new LinkedList<>();
            home.add(getTile(start, start));

            try {
                System.out.println("Have treasure, planning path home...");
                plan = Search.AStar(this, home);
                System.out.println("Found path home, executing...");


                action = plan.removeFirst();
                updateState(action);
                return action;
            } catch (NoPathFoundException e) {
                System.out.println("Could not find path home: " + e.getMessage());
            }
        }

        if (!knownTreasures.isEmpty()) {
            try {
                System.out.println("Know where treasure is, planning path to it...");
                plan = Search.AStar(this, knownTreasures);
                System.out.println("Found path to treasure, executing...");

                action = plan.removeFirst();
                updateState(action);
                return action;
            } catch (NoPathFoundException e) {
                System.out.println("Could not find path to treasure: " + e.getMessage());
            }
        }

        if (!knownItems.isEmpty()) {
            try {
                System.out.println("Know where item(s) are, planning path to one...");
                plan = Search.AStar(this, knownItems);
                System.out.println("Found path to item, executing...");

                action = plan.removeFirst();
                updateState(action);
                return action;
            } catch (NoPathFoundException e) {
                System.out.println("Could not find path to item: " + e.getMessage());
            }
        }

        try {
            System.out.println("Planning exploration...");
            plan = Search.UCS(this);
            System.out.println("Found exploration path, executing...");

            action = plan.removeFirst();
            updateState(action);
            return action;
        } catch (NoPathFoundException e) {
            System.out.println("Could not find unmapped area: " + e.getMessage());
        }


        try {
            action = getHumanAction();
            updateState(action);

            return action;
        } catch (IOException e) {
            System.out.println("IO error:" + e);
        }

        return 0;
    }

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
