

public class Tile {
    private char type;
    private char item = '0';
    private int x = -1;
    private int y = -1;
    private boolean visited = false;

    Tile() {
        this('.', '0', 0, 0);
    }

    Tile(char type, char item, int x, int y) {
        this.x = x;
        this.y = y;
        this.setType(type);
        this.setItem(item);
    }

    public char getType() {
        return type;
    }

    public void setType(char type) throws InvalidTypeException {
        type = Character.toLowerCase(type);
        //TODO: Do we need an "unknown"/unexplored type as well? To decide if the tile should be explored?
        // If unexplored type is implemented, make that the default in the constructor.
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
    
    public boolean getVisited(){
    	return this.visited;
    }
    
    public void setVisited(){
    	this.visited = true;
    }

    public char getItem() {
        return this.item;
    }

    public void setItem(char item) {
        item = Character.toLowerCase(item);
        switch (item) {
            case 'a':
            case 'k':
            case 'd':
            case '$':
                this.item = item;
                break;
            case '0':
                this.item = '0';
                break;
            default:
                this.item = '0';
                //TODO: Throw exception?
                break;
        }
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
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