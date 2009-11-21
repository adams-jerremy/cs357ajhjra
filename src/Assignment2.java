import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Scanner;


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
		printDigraph();
//		Map<Integer, Pair<Integer,Integer>> distances = SSSP(SOURCE);
//		printPath(distances);
//		printDistances(distances);
//		d("size of distances: "+distances.size()+"");
		djikstra();
		
//		changePhi(distances);
//		updateMatching(distances);
		d("Dist:");
		for(int i:dist.keySet())d(i+":"+dist.get(i));
		d("Parents:");
		for(int i:parents.keySet()) d(i+":"+parents.get(i));
		changePhi();
		updateMatching();
		d("done");
		return true;
	}
	
	private static int SOURCE = -1;
	private static int SINK = -2;
	
	private static void printDigraph(){
		d("DIGRAPH:");
		for(int i : digraph.keySet()) d(i+": "+digraph.get(i));
	}
	
	
	private static void buildDigraph(){
		
		digraph = new HashMap<Integer,ArrayList<Link>>();
		digraph.put(SOURCE, new ArrayList<Link>());//source
		digraph.put(SINK, new ArrayList<Link>());//sink
		for(int i = 0;i<matching.length;++i){//keys correspond to v nodes, e.g. if (1, 2) = (u, v) in last M, the supernode for this edge will be called 2
			digraph.put(i, new ArrayList<Link>());
			digraph.get(i).add(new Link(SINK,excess.get(matching[i])));//every supernode has edge to sink with w = w(u) - phi(v) 
		}
		
		int v;
		for(int u : graph.keySet())
			if((v=matched(u))!=-1) 
				for(Link l:graph.get(u))
					if(l.destination != v &&l.weight>=phi[l.destination])
						digraph.get(v).add(new Link(l.destination,excess.get(u)-(l.weight-phi[l.destination])));
		
		for(Link l:graph.get(U))
			if(l.weight>=phi[l.destination]) digraph.get(SOURCE).add(new Link(l.destination,excess.get(U)-(l.weight-phi[l.destination])));
		digraph.get(SOURCE).add(new Link(SINK,excess.get(U)));
	}
	private static HashMap<Integer,Integer> dist = new HashMap<Integer,Integer>();//node to dist
	private static HashMap<Integer,Integer> parents = new HashMap<Integer,Integer>();//node to parent
	
	private static void djikstra(){
		PriorityQueue<Pair<Integer,Integer>> q = new PriorityQueue<Pair<Integer,Integer>>(matching.length+2,new Comparator<Pair<Integer,Integer>>(){
			public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2){return o1.second-o2.second;}});
		dist.clear();
		parents.clear();
		q.add(new Pair<Integer,Integer>(SOURCE,0));
		dist.put(U,0);
		parents.put(U,null);
		Pair<Integer,Integer> cur;//first = node, second = weight
		while(!q.isEmpty()){
			cur = q.poll();
			for(Link l:digraph.get(cur.first)){
				Pair<Integer,Integer> n = new Pair<Integer,Integer>(l.destination,cur.second+l.weight);
				if(!dist.containsKey(n.first)){
					q.add(n);
					dist.put(n.first,n.second);
					parents.put(n.first, cur.first);
				}
				else if(n.second<dist.get(n.first)){
					q.remove(n);
					q.add(n);
					dist.put(n.first,n.second);
					parents.put(n.first, cur.first);
				}
			}
		}
	}
	
	
	private static boolean weightsLTPhi(int n){
		for(Link l : graph.get(n)) {
			if(l.weight >= phi[l.destination])
				return false;	
		}
		return true;
	}
	
	private static void changePhi(){
		d("Fix Phi");
		int d = dist.get(SINK); //this is the distance of shortest path from source to sink
		for (int i = 0;i<phi.length; ++i){
			if(dist.containsKey(i))
				phi[i]+=Math.max(0, (d - dist.get(i)));
		}
	}
	private static void updateMatching(){
		d("Update Matching");
		int child = parents.get(SINK), parent;
		if(child != SOURCE){
			parent = parents.get(child);
			while(parent != SOURCE){
				d(parent+" to "+child);
				matching[child] = matching[parent];
				child = parent;
				parent = parents.get(child);
			}
			matching[child] = U;
		}
		
	}
	
	private static void addEdge(int source, int dest, int weight){ graph.get(source).add(new Link(dest, weight)); }
		
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
			d(i+": "+graph.get(i));
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
		public boolean equals(Object o){ if(o instanceof Link) return destination == ((Link)o).destination; return false;}
	}
	
	static class Pair<T,E>{
		T first;
		E second;
		public Pair(T tt, E ee){first = tt; second = ee;}
	}
	
}
