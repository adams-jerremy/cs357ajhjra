import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;


public class Assignment1Final {
	
	static int[] phi;
	static int[] matching;
	static Map<Integer,ArrayList<Link>> graph;
	static List<Integer> excess;
	static Set<Integer> vVisited;
	
	public static void main(String[] args) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int n;
		try{
			n =  Integer.parseInt(br.readLine());
			while(n>0){
				init(n);
				print();
				while(br.ready()){
					if(!handleLine(br.readLine())) break;
					print();
				}
				n = Integer.parseInt(br.readLine());
			}		
		}catch(IOException e){e.printStackTrace();}
	}
	
	private static boolean handleLine(String s){
		Scanner sc = new Scanner(s);
		int source = sc.nextInt();
		if(source<0) return false;
		handleSource(source); //Add Node/Increment weights
		while(sc.hasNextInt()) addEdge(source,sc.nextInt());//graph.get(source).add(new Link(sc.nextInt(), 0)); // update graph
		if(matched(source)) return true; // case 1 - u matched in M, return same matching, same phi
		ensureExcessSize(); //make sure that our excess list is large enough
		for(int i:graph.keySet()) calculateExcess(i);//find Excess/tightness
	
		vVisited = new HashSet<Integer>(); // reset the v nodes we've visited
		Pair<Boolean,List<Integer>> p = DFS(source); // kick off depth first search
		if(p.first) invertPath(p.second); // if DFS found u' swap tight/matched edges
		else changePhi(); //if no u' found, increment phis of visited v nodes.
		
		return true;
	}
	
	private static void addEdge(int source, int dest){ graph.get(source).add(new Link(dest, 0)); }
	
	private static void invertPath(List<Integer> l){ 
		for(ListIterator<Integer> li = l.listIterator(l.size()); li.hasPrevious();){
			int v = li.previous();
			if(!li.hasPrevious()) break;
			int u = li.previous();
			matching[u] = v;
		}
	}
	
	private static void changePhi(){ for(int v:vVisited) ++phi[v]; }
	
	private static Pair<Boolean, List<Integer>> DFS(int start){
		Pair<Boolean,List<Integer>> p = DFSTight(start, new ArrayList<Integer>(), new boolean[graph.size()][matching.length]); //Do depth first search
		p.second.add(start);
		return p;
	}
	
	private static Pair<Boolean,List<Integer>> DFSTight(int start, List<Integer> path, boolean[][] visited){
		for(Link l : graph.get(start)) 
			if (l.tight && !visited[start][l.destination] && !visited[matching[l.destination]][l.destination] ){
				vVisited.add(l.destination);
				visited[start][l.destination] = visited[matching[l.destination]][l.destination] = true;
				if(excess.get(matching[l.destination]) == 0){
					path.add(matching[l.destination]);
					path.add(l.destination);
					return new Pair<Boolean, List<Integer>>(true,path);
				}
				
				Pair<Boolean,List<Integer>> p = DFSTight(matching[l.destination],path, visited);
				if(p.first){
					p.second.add(matching[l.destination]);
					p.second.add(l.destination);
					return p;
				}
			}
		return new Pair<Boolean, List<Integer>>(new Boolean(false),path);
	}
	
	private static void ensureExcessSize(){ while(excess.size()<graph.size()) excess.add(0);}
	
	private static void calculateExcess(int n){
		int max = Integer.MIN_VALUE, temp;
		for(Link l : graph.get(n)) if((temp = l.weight-phi[l.destination]) >max) max = temp; 
		excess.set(n, max);
		for(Link l : graph.get(n)) l.tight = ( max>=0 && ((l.weight-phi[l.destination]) == max));
	}
	
	private static boolean matched(int n){
		for(int i : matching) if(i == n) return true;
		return false;
	}
	
	private static void handleSource(int n){
		if(graph.containsKey(n))
			for(Link l : graph.get(n)) ++l.weight;
		else graph.put(n, new ArrayList<Link>());
	}
	
	private static void init(int n){
		phi = new int[n];
		matching = new int[n];
		graph = new HashMap<Integer,ArrayList<Link>>();
		excess = new ArrayList<Integer>();
		for(int i = 0;i<matching.length;++i){
			matching[i] = i;
			graph.put(i,new ArrayList<Link>());
			graph.get(i).add(new Link(i,0));
		}
		
	}
	
	private static void print(){
		StringBuilder sb = new StringBuilder();
		for(int i : matching) sb.append(i).append(' ');
		sb.append('\n');
		for(int i : phi) sb.append(i).append(' ');
		System.out.println(sb.toString());
	}
	
	
	private static class Link{
		public int destination, weight;
		public boolean tight;
		public Link(int d, int w){ destination = d; weight = w; tight=false;}
		public String toString(){return ""+destination;	}
	}
	
	static class Pair<T,E>{
		T first;
		E second;
		public Pair(T tt, E ee){first = tt; second = ee;}
	}
	
}
