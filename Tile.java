
/* The Tile class represents the properties of a particular location in the world map */
public class Tile {
    private char type;
    private char item = '0';
    private int x = -1;
    private int y = -1;

    /* Default constructor. Sets the type to an off map tile */
    Tile() {
        this('.', '0', 0, 0);
    }

    /* Constructor that sets all the object parameters */
    Tile(char type, char item, int x, int y) {
        this.x = x;
        this.y = y;
        this.setType(type);
        this.setItem(item);
    }

    /* Constructor that copies the values of an existing tile */
    Tile(Tile tile) {
        this(tile.getType(), tile.getItem(), tile.getX(), tile.getY());
    }

    /* Return the environment type of this tile */
    char getType() {
        return type;
    }

    /* Set the environment type of this tile */
    void setType(char type) throws InvalidTypeException {
        type = Character.toLowerCase(type);
        switch (type) {
            case ' ':
            case 'a':
            case 'k':
            case 'd':
            case '$':
                this.type = ' ';
                break;
            case '*':
            case '~':
            case 't':
            case '-':
            case '.':
                this.type = type;
                break;
            default:
                throw new InvalidTypeException("Error setting type for tile at x: " + x + " y: " + y + ". Character '" + type + "' is not a valid tile type");
        }
    }

    /* Get the item located on this tile */
    char getItem() {
        return this.item;
    }

    /* Set the item located on this tile. If the item is unvalid, it will set it to no item */
    void setItem(char item) {
        item = Character.toLowerCase(item);
        switch (item) {
            case 'a':
            case 'k':
            case 'd':
            case '$':
                this.item = item;
                break;
            default:
                this.item = '0';
        }
    }

    /* Get the x coordinate of this tile */
    int getX() {
        return this.x;
    }

    /* Get the y coordinate of this tile */
    int getY() {
        return this.y;
    }

    /* Compare this tile to another, and check if they are identical */
    boolean sameTile(Tile tile) {
        return this.type == tile.type &&
                this.item == tile.item &&
                this.x == tile.x &&
                this.y == tile.y;
    }

    /* Override the equals method. Needed for contains() method for e.g. linked lists */
    @Override
    public boolean equals(Object o) {
        return (o instanceof Tile) && sameTile((Tile) o);
    }

    /* Return a string representation of this tile. This is an overload */
    public String toString() {
        return "Tile: x: " + x + "  y: " + y + "  type: '" + type + "'  item: " + item;
    }

}

class InvalidTypeException extends RuntimeException {
    InvalidTypeException() {
        super();
    }

    InvalidTypeException(String message) {
        super(message);
    }
}