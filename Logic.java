import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class Logic {
	
	
		public boolean validExploreMove(Agent agent,Tile step){
			if(agent.getPos().getType() == ' '){
				if(step.getType() == ' '){
					return true;
					}
				if(step.getType() == '~'){
					if(agent.hasRaft()){
						return true;
					}
				}
			}
			
			else if(agent.getPos().getType() == '~'){
				if(step.getType() == ' '){
					return true;
				}
				if(step.getType() == '~'){
					return true;
				}
			}
			return false;
		}
		
		public ArrayList<Tile> exploreMap(Agent agent){
			
		Queue<Tile> queue = new LinkedList<Tile>();
		Map<Tile,Tile> set = new HashMap<Tile,Tile>();
		Tile start = agent.getPos();
		
		queue.add(start);
		set.put(start, null);
		
		
		while(queue != null){
			Tile current = queue.poll();
			int x = current.getX();
			int y = current.getY();
			
			// Explore NOTH
			if(set.containsKey(agent.getTile(x, y+1)) == false && validExploreMove(agent, agent.getTile(x, y+1))){
				set.put(agent.getTile(x, y+1), current);
				queue.add(agent.getTile(x, y+1));
				for(int i=0; i<5; i++){
					if(agent.getTile(x-2+i, y+3) == null){
						Tile step = agent.getTile(x, y+1);
						ArrayList<Tile> path = new ArrayList<Tile>();
						path.add(step);
						while(step != agent.getPos()){
							step = set.get(agent.getTile(x, y+1));
							path.add(step);
						}
						Collections.reverse(path);
						return path;
					}
				}				
			}
			
			// Explore EAST
			if(set.containsKey(agent.getTile(x+1, y)) == false && validExploreMove(agent, agent.getTile(x+1, y))){
				set.put(agent.getTile(x+1, y), current);
				queue.add(agent.getTile(x+1, y));
				for(int i=0; i<5; i++){
					if(agent.getTile(x+3, y-2+i) == null){
						Tile step = agent.getTile(x+1, y);
						ArrayList<Tile> path = new ArrayList<Tile>();
						path.add(step);
						while(step != agent.getPos()){
							step = set.get(agent.getTile(x+1, y));
							path.add(step);
						}
						Collections.reverse(path);
						return path;
					}
				}				
			}
			// Explore SOUTH
			if(set.containsKey(agent.getTile(x, y-1)) == false && validExploreMove(agent, agent.getTile(x, y-1))){
				set.put(agent.getTile(x, y-1), current);
				queue.add(agent.getTile(x, y-1));
				for(int i=0; i<5; i++){
					if(agent.getTile(x-2+i, y-3) == null){
						Tile step = agent.getTile(x, y-1);
						ArrayList<Tile> path = new ArrayList<Tile>();
						path.add(step);
						while(step != agent.getPos()){
							step = set.get(agent.getTile(x, y-1));
							path.add(step);
						}
						Collections.reverse(path);
						return path;
					}
				}				
			}
			
			// Explore WEST
			if(set.containsKey(agent.getTile(x-1, y)) == false && validExploreMove(agent, agent.getTile(x-1, y))){
				set.put(agent.getTile(x-1, y), current);
				queue.add(agent.getTile(x-1, y));
				for(int i=0; i<5; i++){
					if(agent.getTile(x-1, y-2+i) == null){
						Tile step = agent.getTile(x, y+1);
						ArrayList<Tile> path = new ArrayList<Tile>();
						path.add(step);
						while(step != agent.getPos()){
							step = set.get(agent.getTile(x-1, y));
							path.add(step);
						}
						Collections.reverse(path);
						return path;
					}
				}				
			}
		}
		return null;
		}

}
