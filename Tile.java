

public class Tile {
    private char type;
    private char item = '0';
    private int x, y;

    Tile(){
        this(' ', '0', 0, 0);
    }

    Tile (char type, char item, int x, int y) {
        this.setType(type);
        this.setItem(item);
        this.x = x;
        this.y = y;
    }

    public char getType() {
        return type;
    }

    public void setType(char type) throws InvalidTypeException {
        type = Character.toLowerCase(type);
        switch (type){
            case ' ':
            case 'a':
            case 'k':
            case 'd':
            case '$':
                this.type = ' ';
                break;
            case '*':
            case '~':
            case 'T':
            case '-':
            case '.':
                this.type = type;
                break;
            default:
                throw new InvalidTypeException("Character '" + type + "' is not a valid tile type");
        }
    }

    public char getItem() {
        return this.item;
    }

    public void setItem(char item) {
        item = Character.toLowerCase(item);
        switch(item) {
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

    InvalidTypeException (String message) {
        super(message);
    }
}