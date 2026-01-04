public class Ship
{
    private int size;          // original size
    private int health;        // remaining parts
    private boolean direction; // true = horizontal, false = vertical
    private int row;
    private int column;

    public Ship(int size, boolean direction, int row, int column)
    {
        this.size = size;
        this.health = size;
        this.direction = direction;
        this.row = row;
        this.column = column;
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder("Ship positions: ");

        for (int i = 0; i < size; i++)
        {
            int currentRow = row;
            int currentColumn = column;

            if (direction) // horizontal
            {
                currentColumn += i;
            }
            else // vertical
            {
                currentRow += i;
            }

            result.append("(")
                  .append(currentRow)
                  .append(",")
                  .append(currentColumn)
                  .append(") ");
        }

        return result.toString();
    }

    // called when ship is hit
    public void hit()
    {
        if (health > 0)
        {
            health--;
        }
    }
    
    public isOnShip(int row, int column)
    {
        for(int i = 0; i < size; i++)
        {
            int rr;
            int cc;
            
            if(direction)
            {
                rr = row;
                cc = column = i;
            }
            else
            {
                rr = row = i;
                cc = column;
            }
            
            if(rr == r && cc == c)
            {
                return true;
            }
        }
        return false;
    }

    // check if ship is sunk
    public boolean isSunk()
    {
        return health == 0;
    }

    public int getHealth()
    {
        return health;
    }
    
    public int getSize()
    {
        return size;
    }
    
    public int getrow(){
        return row;
    }
    
    public int get column()
    {
        return column;
    }
    
    public boolean isHorizontal()
    {
        return direction;
    }
}
