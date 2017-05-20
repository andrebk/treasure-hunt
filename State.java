
public class State {
    protected Tile[][] map = new Tile[164][164];
    protected int start = 82;
    protected int posX, posY;

    protected int dynamites = 0;
    protected boolean hasDynamite = false;
    protected boolean hasAxe = false;
    protected boolean hasKey = false;
    protected boolean hasRaft = false;
    protected boolean hasTreasure = false;

    public final static int EAST = 0;
    public final static int NORTH = 1;
    public final static int WEST = 2;
    public final static int SOUTH = 3;

    protected int direction = NORTH;


    public Tile getTile(int x, int y) {
        return map[y][x];
    }

    protected void setTile(int x, int y, Tile newTile) {
        map[y][x] = newTile;
    }

    protected int numUnseenTiles(int x, int y) {
        int unSeen = 0;
        int i, j;
        for (i = -2; i <= 2; i++) {
            for (j = -2; j <= 2; j++) {
                if (getTile(x + j, y + i) == null) {
                    unSeen++;
                }
            }
        }
        return unSeen;
    }

    protected void updateMap(char view[][]) {
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

                int tileY = posY - 2 + row;
                int tileX = posX - 2 + col;

                if (i == 2 && j == 2) {
                    // If players position has not been set before, the player must be on the start tile
                    // so set that tile to a "land" tile
                    if (getTile(tileX, tileY) == null) {
                        newTile = new Tile(' ', ' ', tileX, tileY);
                        setTile(tileX, tileY, newTile);
                    }
                } else if (getTile(tileX, tileY) == null) {
                    newTile = new Tile(view[i][j], view[i][j], tileX, tileY);
                    setTile(tileX, tileY, newTile);
                } else {
                    getTile(tileX, tileY).setType(view[i][j]);
                    getTile(tileX, tileY).setItem(view[i][j]);
                }
            }
        }
    }

    protected void updateState(char action) {
        int deltaX = 0;
        int deltaY = 0;
        int nextX, nextY;
        Tile nextTile;
        Tile currentTile = getTile(posX, posY);

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
        nextX = posX + deltaX;
        nextY = posY + deltaY;
        nextTile = getTile(nextX, nextY);

        // Update agents world state
        switch (action) {
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
                        posX = nextX;
                        posY = nextY;
                        break;
                    default:
                        posX = nextX;
                        posY = nextY;
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
                //TODO: Only use a dynamite if nextTile can be blown up?
                if (hasDynamite) {
                    dynamites--;
                    if (dynamites <= 0) {
                        hasDynamite = false;
                    }
                }
                break;
        }
    }

    protected void printMap() {
        char ch = ' ';
        char[] line = new char[164];
        System.out.println();
        boolean printLine;
        Tile currentTile;

        for (int y = 0; y < 164; y++) {
            printLine = false;

            for (int x = 0; x < 164; x++) {
                currentTile = getTile(x, y);

                if (currentTile == null) {
                    // This tile has not yet been seen by the agent
                    ch = '?';
                } else if (x == this.posX && y == this.posY) {
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

    protected void printState() {
        System.out.println("Raft: " + hasRaft + "  Axe: " + hasAxe + "  Key: " + hasKey + "  Dynamite: " + dynamites + "  Treasure: " + hasTreasure);
    }
}
