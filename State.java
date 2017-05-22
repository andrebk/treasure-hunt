
public class State {
    private final static int mapSize = 164;
    protected final static int start = mapSize / 2;
    protected Tile[][] map = new Tile[mapSize][mapSize];

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

    protected void setTile(char type, char item, int x, int y) {
        if (map[y][x] == null) {
            // Tile doesn't exist, because it hasn't been seen before. Create it
            map[y][x] = new Tile(type, item, x, y);
        } else {
            // Update existing tile
            map[y][x].setItem(item);
            map[y][x].setType(type);
        }
    }

    public Position getNextPos() {
        int deltaX = 0;
        int deltaY = 0;
        int nextX, nextY;

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
        return new Position(nextX, nextY);
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
        int row, col, tileX, tileY;
        char tileView;

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

                tileY = posY - 2 + row;
                tileX = posX - 2 + col;
                tileView = view[i][j];

                if (i == 2 && j == 2) {
                    // If players position has not been set before, the player must be on the start tile
                    // so set that tile to a "land" tile. Otherwise do nothing, as the view does not contain
                    // the players position
                    if (getTile(tileX, tileY) == null) {
                        setTile(' ', ' ', tileX, tileY);
                    }
                } else {
                    setTile(tileView, tileView, tileX, tileY);
                }
            }
        }
    }

    protected void updateState(char action) {
        Tile currentTile = getTile(posX, posY);
        Position nextPos = getNextPos();

        int nextX = nextPos.getX();
        int nextY = nextPos.getY();
        Tile nextTile = getTile(nextX, nextY);

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
                    nextTile.setType(' ');
                }
                break;
            case 'u':
                if (nextTile.getType() == '-' && hasKey) {
                    nextTile.setType(' ');
                }
                break;
            case 'b':
                if (hasDynamite) {
                    switch (nextTile.getType()) {
                        case '*':
                        case '-':
                        case 't':
                            dynamites--;
                            if (dynamites <= 0) {
                                hasDynamite = false;
                            }
                            nextTile.setType(' ');
                            break;
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

    protected boolean sameState(State state) {
        return this.posX == state.posX &&
                this.posY == state.posY &&
                this.dynamites == state.dynamites &&
                this.hasDynamite == state.hasDynamite &&
                this.hasAxe == state.hasAxe &&
                this.hasKey == state.hasKey &&
                this.hasRaft == state.hasRaft &&
                this.hasTreasure == state.hasTreasure &&
                this.direction == state.direction;// &&
                //this.sameMap(state);
    }

    protected Tile[][] deepCopyMap() {
        Tile[][] newMap = new Tile[mapSize][mapSize];
        Tile currentTile;

        for (int i = 0; i < mapSize; i++) {
            for (int j = 0; j < mapSize; j++) {

                currentTile = map[i][j];
                if (currentTile == null) {
                    newMap[i][j] = null;
                } else {
                    newMap[i][j] = new Tile(map[i][j].getType(), map[i][j].getItem(), map[i][j].getX(), map[i][j].getY());
                }

            }
        }
        return newMap;
    }

    protected boolean sameMap(State state) {
        for (int i = 0; i < mapSize; i++) {
            for (int j = 0; j < mapSize; j++) {
                if (this.map[i][j] == null && state.map[i][j] != null || this.map[i][j] != null && state.map[i][j] == null) {
                    return false;
                } else if (this.map[i][j] != null && state.map[i][j] != null && this.map[i][j].equals(state.map[i][j])) {
                    return false;
                }
            }
        }
        return true;
    }
}

class Position {
    private int x, y;

    Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }
}