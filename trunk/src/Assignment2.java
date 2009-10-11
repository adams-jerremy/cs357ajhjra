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


public class Assignment2 {
	private static final boolean DEBUG = false;
	private static void d(String s){ if(DEBUG) System.err.println(s);}
	
	static int[] phi;
	static int[] matching;
	static Map<Integer,ArrayList<Link>> graph;
	static Map<Integer,ArrayList<Link>> digraph;
	static List<Integer> excess;
	//static Set<Integer> vVisited;
	static int U = 0;
	static int[] path;
	
	public static void main(String[] args) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int n;
		int line = 0, block = -1;
		try{
			n =  Integer.parseInt(br.readLine());
			while(n>0){
				U = n;
				line = 0;
				d("________BLOCK "+ ++block+"_________");
				d("Reading line "+line);
				init(n);
				print();
				while(br.ready()){
					d("Reading line "+ ++line);
					if(!handleLine(br.readLine())) break;
					print();
					++U;
				}
				n = Integer.parseInt(br.readLine());
			}		
		}catch(IOException e){e.printStackTrace();}
	}
	
	private static boolean handleLine(String s){
		d("Graph:");
		for(int i : graph.keySet()) d(i+": "+graph.get(i));
		
		Scanner sc = new Scanner(s);
//		int source = sc.nextInt();
//		d(" source: "+source);
//		if(source<0) return false;
		graph.put(U, new ArrayList<Link>());//handleSource(U); //Add Node
		int destination;
		while(sc.hasNextInt()){
			destination = sc.nextInt();
			if(destination<0) return false;
			addEdge(U,destination, sc.nextInt());//graph.get(source).add(new Link(sc.nextInt(), 0)); // update graph
		}

		if(weightsLTPhi(U)) return true; // case 1 - for all edges in E(i + 1) w(e) < phi(e)
		
		ensureExcessSize();
		for(int i:graph.keySet()) calculateExcess(i);//find Excess/tightness
		
		buildDigraph();//building auxiliary directed graph
		Map<Integer, Pair<Integer,Integer>> distances = SSSP(SOURCE);
		
		changePhi(distances);
		
		updateMatching(distances);
		

		return true;
	}
	
	private static int SOURCE = -1;
	private static int SINK = -2;
	
	private static void buildDigraph(){
		
		digraph = new HashMap<Integer,ArrayList<Link>>();
		digraph.put(SOURCE, new ArrayList<Link>());//source
		digraph.put(SINK, new ArrayList<Link>());//sink
		for(int i = 0;i<matching.length;++i){
			digraph.put(i, new ArrayList<Link>());
			digraph.get(i).add(new Link(SINK,excess.get(matching[i])));
		}
		
		int v;
		for(int u : graph.keySet())
			if((v=matched(u))!=-1) 
				for(Link l:graph.get(u))
					if(l.destination != v &&l.weight>=phi[l.destination])
						digraph.get(matching[u]).add(new Link(l.destination,excess.get(u)-(l.weight-phi[l.destination])));
		
		for(Link l:graph.get(U))
			if(l.weight>=phi[l.destination]) digraph.get(SOURCE).add(new Link(l.destination,excess.get(U)-(l.weight-phi[l.destination])));
	}
	
	private static Map<Integer, Pair<Integer,Integer>> SSSP(Integer start){
		
		Map<Integer, Pair<Integer,Integer>> X = new HashMap<Integer,  Pair<Integer,Integer>>();
		Map<Integer, Pair<Integer,Integer>> Y = new HashMap<Integer,  Pair<Integer,Integer>>();
		X.put(start, new Pair<Integer, Integer>(0, Integer.MIN_VALUE));
		
		for( int u : digraph.keySet()){	
			if (u != start){
				Y.put(u,new Pair<Integer, Integer>(Integer.MAX_VALUE, Integer.MIN_VALUE));
			}
		}
		
		while(! Y.isEmpty()){
			int min = Integer.MAX_VALUE;
			int argmin = -1;
			for( int y : Y.keySet()){
				for( int x : X.keySet()){
					for( Link l : digraph.get(x)){
						if( (X.get(x).first + l.weight) <  Y.get(Y).first){
							Y.put(y, new Pair<Integer, Integer>((X.get(x).first + l.weight), x));
						}
					}
				}
			}
			for (int i : Y.keySet()){
				if( Y.get(i).first < min ){
					min = Y.get(i).first;
					argmin = i;
				}
			}

			X.put(argmin, Y.get(argmin) );
			Y.remove(argmin);
			
		}
		
		return X;
	}
	
	
	private static boolean weightsLTPhi(int n){
		for(Link l : graph.get(n)) {
			if(l.weight >= phi[l.destination])
				return false;	
		}
		return true;
	}
	
	private static void changePhi(Map<Integer, Pair<Integer,Integer>> distances){
		d("Fix Phi");
		int d = distances.get(SINK).first; //this is the distance of shortest path from source to sink
		for (int v : phi){
			int dSubI = distances.get(v).first;
			dSubI = Math.max(0, (d - dSubI));
			phi[v] = phi[v] + dSubI;
		}
	}
	
	private static void updateMatching(Map<Integer, Pair<Integer,Integer>> distances){
		
		if(distances.get(SINK).second != SOURCE){
			
		}
		
	}
	
	private static void addEdge(int source, int dest, int weight){ graph.get(source).add(new Link(dest, weight)); }
	
	private static void invertPath(List<Integer> l){ 
		d("Invert Path: "+l);
		for(ListIterator<Integer> li = l.listIterator(l.size()); li.hasPrevious();){
			int v = li.previous();
			if(!li.hasPrevious()) break;
			int u = li.previous();
			matching[u] = v;
			//System.err.print("matching: [");
			//for (int i = 0; i < matching.length; i++){
				//System.err.print(matching[i] + ", ");
			//}
			//System.err.print("]\n");
		}
	}
	

	
/*	private static Pair<Boolean, List<Integer>> DFS(int start){
		Pair<Boolean,List<Integer>> p = DFSTight(start, new ArrayList<Integer>(), new boolean[graph.size()][matching.length]); //Do depth first search
		p.second.add(start);
		return p;
	}
	
	private static Pair<Boolean,List<Integer>> DFSTight(int start, List<Integer> path, boolean[][] visited){
		d("DFSTighting from u"+start);
		for(Link l : graph.get(start)) 
			if (l.tight && !visited[start][l.destination] && !visited[matching[l.destination]][l.destination] ){
				vVisited.add(l.destination);
				visited[start][l.destination] = visited[matching[l.destination]][l.destination] = true;
				d("Explore from u"+start+" to v"+l.destination+" to u"+matching[l.destination]);
				if(excess.get(matching[l.destination]) == 0){
					d("Add u' = "+matching[l.destination]+" and v"+l.destination);
					path.add(matching[l.destination]);
					path.add(l.destination);
					return new Pair<Boolean, List<Integer>>(true,path);
				}
				
				Pair<Boolean,List<Integer>> p = DFSTight(matching[l.destination],path, visited);
				if(p.first){
					d("Add u"+matching[l.destination]+" and v"+l.destination+" to path");
					p.second.add(matching[l.destination]);
					p.second.add(l.destination);
					return p;
				}
			}
		d("DFSTight has no nodes to visit");
		return new Pair<Boolean, List<Integer>>(new Boolean(false),path);
	}*/
	
	private static void ensureExcessSize(){
		while(excess.size()<graph.size()) excess.add(0);
	}
	
	private static void calculateExcess(int n){
		int max = Integer.MIN_VALUE, temp;
		for(Link l : graph.get(n)) if((temp = l.weight-phi[l.destination]) >max) max = temp; 
		excess.set(n, max);
		for(Link l : graph.get(n)) l.tight = ( max>=0 && ((l.weight-phi[l.destination]) == max));
	}
	
	private static int matched(int n){
		for(int i = 0;i<matching.length;++i)
			if(matching[i]==n) return i;
		return -1;
	}
	
	private static void handleSource(int n){
		if(! graph.containsKey(n))
			 graph.put(n, new ArrayList<Link>());
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
		
		for(int i : graph.keySet()){
			System.err.println(i+": "+graph.get(i));
		}
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
		public String toString(){return ""+destination+"("+weight+")";	}
	}
	
	static class Pair<T,E>{
		T first;
		E second;
		public Pair(T tt, E ee){first = tt; second = ee;}
	}
	
}
