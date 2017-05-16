/*********************************************
 *  Agent.java 
 *  Sample Agent for Text-Based Adventure Game
 *  COMP3411 Artificial Intelligence
 *  UNSW Session 1, 2017
 */

import java.io.*;
import java.net.*;

public class Agent {

    private Tile[][] map = new Tile[164][164];
    private int start = 82;
    private int x = start;
    private int y = start;

    private int dynamites = 0;
    private boolean hasDynamite = false;
    private boolean hasAxe = false;
    private boolean hasKey = false;
    private boolean hasRaft = false;
    private boolean hasTreasure = false;

    private final static int EAST = 0;
    private final static int NORTH = 1;
    private final static int WEST = 2;
    private final static int SOUTH = 3;

    private int direction = NORTH;


    private void updateMap(char view[][]) {
        int row, col;
        Tile newTile;
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {

                // Transform coordinates to rotate view orientation to correct map orientation
                switch (direction) {
                    case NORTH:
                        row = i;
                        col = j;
                        break;
                    case WEST:
                        row = 5 - 1 - j;
                        col = i;
                        break;
                    case EAST:
                        row = j;
                        col = 5 - 1 - i;
                        break;
                    case SOUTH:
                        row = 5 - 1 - i;
                        col = 5 - 1 - j;
                        break;
                    default:
                        throw new RuntimeException("Invalid direction");
                }

                int tileY = y - 2 + row;
                int tileX = x - 2 + col;

                if (i == 2 && j == 2) {
                    // If players position has not been set before, the player must be on the start tile
                    // so set that tile to a "land" tile
                    if (map[tileY][tileX] == null) {
                        newTile = new Tile(' ', ' ', tileX, tileY);
                        map[tileY][tileX] = newTile;
                    }
                } else if (map[tileY][tileX] == null) {
                    newTile = new Tile(view[i][j], view[i][j], tileX, tileY);
                    map[tileY][tileX] = newTile;
                } else {
                    map[tileY][tileX].setType(view[i][j]);
                    map[tileY][tileX].setItem(view[i][j]);
                }
            }
        }
    }

    private void printMap() {
        char ch = ' ';
        char[] line = new char[164];
        System.out.println();
        boolean printLine;
        Tile currentTile;

        for (int y = 0; y < 164; y++) {
            printLine = false;

            for (int x = 0; x < 164; x++) {
                currentTile = map[y][x];

                if (map[y][x] == null) {
                    // This tile has not yet been seen by the agent
                    ch = '?';
                } else if (x == this.x && y == this.y) {
                    // If at player position, print player icon in correct orientation
                    switch (direction) {
                        case NORTH:
                            ch = '^';
                            break;
                        case EAST:
                            ch = '>';
                            break;
                        case WEST:
                            ch = '<';
                            break;
                        case SOUTH:
                            ch = 'v';
                            break;
                    }
                    printLine = true;
                } else if (x == start && y == start) {
                    // Indicate starting position when printing the map
                    ch = 'S';
                    printLine = true;
                } else {
                    // Print the tile type. If there is an item there, print it instead.
                    ch = currentTile.getType();
                    if (currentTile.getItem() != '0') {
                        ch = currentTile.getItem();
                    }
                    printLine = true;
                }

                line[x] = ch;
            }
            if (printLine) {
                // Print this line of the map, if it has explored parts
                System.out.format("y: %3d  ", y);
                System.out.println(line);
            }
        }
        System.out.format("    x:  %-10d%-10d%-10d%-10d%-10d%-10d%-10d%-10d%-10d%-10d%-10d%-10d%-10d%-10d%-10d%-10d%-10d%n%n",
                0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120, 130, 140, 150, 160);
    }

    private void printState() {
        System.out.println("Raft: " + hasRaft + "  Axe: " + hasAxe + "  Key: " + hasKey + "  Dynamite: " + dynamites + "  Treasure: " + hasTreasure);
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

    private void updateState(char ch) {
        int deltaX = 0;
        int deltaY = 0;
        int nextX, nextY;
        Tile nextTile;
        Tile currentTile = map[y][x];

        switch (direction) {
            case EAST:
                deltaX = 1;
                break;
            case WEST:
                deltaX = -1;
                break;
            case NORTH:
                deltaY = -1;
                break;
            case SOUTH:
                deltaY = 1;
                break;
        }

        // Find the tile in front of the player
        nextX = x + deltaX;
        nextY = y + deltaY;
        nextTile = map[nextY][nextX];

        // Update agents world state
        switch (ch) {
            case 'f':
                switch (nextTile.getType()) {
                    case '*':
                    case '-':
                    case 'T':
                        // Obstacle in the way, do not update position
                        break;
                    case ' ':
                        // Lose the raft when moving from water to land
                        if (currentTile.getType() == '~' && hasRaft) {
                            hasRaft = false;
                        }
                        x = nextX;
                        y = nextY;
                        break;
                    default:
                        x = nextX;
                        y = nextY;
                        break;
                }
                switch (nextTile.getItem()) {
                    case 'a':
                        hasAxe = true;
                        break;
                    case 'd':
                        hasDynamite = true;
                        dynamites++;
                        break;
                    case 'k':
                        hasKey = true;
                        break;
                    case '$':
                        hasTreasure = true;
                        break;
                }
                break;
            case 'l':
                direction = (direction + 1) % 4;
                break;
            case 'r':
                direction = (direction + 3) % 4;
                break;
            case 'c':
                if (nextTile.getType() == 't' && hasAxe) {
                    hasRaft = true;
                }
                break;
            case 'u':
                break;
            case 'b':
                if (hasDynamite) {
                    dynamites--;
                    if (dynamites <= 0) {
                        hasDynamite = false;
                    }
                }
                break;
        }
    }

    public char get_action(char view[][]) {

        // REPLACE THIS CODE WITH AI TO CHOOSE ACTION

        updateMap(view);
        printMap();
        printState();

        try {
            char ch = getHumanAction();
            updateState(ch);

            return ch;
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
                agent.print_view(view); // COMMENT THIS OUT BEFORE SUBMISSION
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
