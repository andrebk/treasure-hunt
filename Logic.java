import java.util.LinkedList;
import java.util.Queue;

public class Logic {
	
	public void exploreMap(Agent agent){
		Tile start = agent.getTile(82, 82);
		Queue<Tile> queue = new LinkedList<Tile>();
		start.setSeen();
		queue.add(start);
		while(queue != null){
			Tile n = queue.poll();
			int x = n.getX();
			int y = n.getY();
			// Check WEST tile
			if(agent.getTile(x-1, y).getSeen() == false){
				agent.getTile(x-1, y).setSeen();
				if(agent.getTile(x-1, y).getType() == ' '){
					queue.add(agent.getTile(x-1, y));	
				}
			}
			// Check EAST tile
			else if(agent.getTile(x+1, y).getSeen() == false){
				agent.getTile(x+1, y).setSeen();
				if(agent.getTile(x+1, y).getType() == ' '){
					queue.add(agent.getTile(x+1, y));					
				}
			}
			// Check NORTH tile
			else if(agent.getTile(x, y+1).getSeen() == false){
				agent.getTile(x, y+1).setSeen();
				if(agent.getTile(x, y+1).getType() == ' '){
					queue.add(agent.getTile(x, y+1));					
				}
			}
			// Check SOUTH tile
			else if(agent.getTile(x, y-1).getSeen() == false){
				agent.getTile(x, y-1).setSeen();
				if(agent.getTile(x, y-1).getType() == ' '){
					queue.add(agent.getTile(x, y-1));					
				}
			}
		}
		return;
	}

}
