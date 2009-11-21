import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;


public class Assignment4 {
	private static final boolean DEBUG = false;
	private static void d(String s){ if(DEBUG) System.err.println(s);}
	
	static int[] phi;
	static int[] matching;
	static Map<Integer,ArrayList<Link>> graph;
	static Map<Integer,ArrayList<Link>> digraph;
	//static List<Integer> excess;
	static Map<Integer,Integer> excess;
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
				}
				n = Integer.parseInt(br.readLine());
			}		
		}catch(IOException e){e.printStackTrace();}
	}
	
	private enum Type{one,two}
	
	private static boolean remove(int u){
		if(u<0) return false;
		d("Remove Called");

		if(matched(u)!=-1){
			d("Node to be removed is matched!");
			Map<Integer, Pair<Integer,List<Link>>> auxilary = new HashMap<Integer, Pair<Integer,List<Link>>>(graph.size());
			for(int i:graph.keySet()){
				Pair<Integer,List<Link>> p = new Pair<Integer,List<Link>>(matched(i),new ArrayList<Link>());
				auxilary.put(i,p);
			}
			for(int i:auxilary.keySet()){
				switch(auxilary.get(i).first){
				case -1: // type one
					for(Link l:graph.get(i))
						if(l.weight == phi[l.destination])
							auxilary.get(i).second.add(new Link(matching[l.destination],0));
					break;
				default: // type two
					int u0v0=0;
					for(Link l:graph.get(i))
						if(l.destination==auxilary.get(i).first){
							u0v0 = l.weight;
							break;
						}
					for(Link l:graph.get(i))
						if(l.destination!=auxilary.get(i).first && u0v0-phi[auxilary.get(i).first] == l.weight - phi[l.destination])
								auxilary.get(i).second.add(new Link(matching[l.destination],0));
					break;
				}
			}//aux graph built, do update
			List<Integer> path = BFSToType2(auxilary,u);
			//Amelia changed from path.size()-1
			ListIterator<Integer> li = path.listIterator(path.size());
			int curr = li.previous();
			while(li.hasPrevious()){
				int next = li.previous();
				matching[matched(next)]=curr;
				curr = next;
			}
		}
		d("Past Matching");
		graph.remove(u);
		// update phi :(
		//build auxilary digraph
		Map<Integer, List<Link>> phigraph = new HashMap<Integer, List<Link>>();
		//adding supernodes, supernode with label s is the source supernode, all other supernodes labeled u,v
		phigraph.put(SOURCE, new ArrayList<Link>());
		
		for(int i = 0;i<matching.length;++i)
			phigraph.put(i, new ArrayList<Link>());
		
		Map<Integer,Integer> maxWeights = new HashMap<Integer,Integer>();//v0,weight
		
		for(int i:graph.keySet()){
			if(matched(i)==-1){
				for(Link l:graph.get(i)){
					if(maxWeights.get(l.destination) == null || maxWeights.get(l.destination) < l.weight )
						maxWeights.put(l.destination, l.weight);
				}
			}
		}
		//from Source Supernode 
		for(int v:maxWeights.keySet()){
			phigraph.get(SOURCE).add(new Link(v,phi[v]-maxWeights.get(v)));
		}
		int matched = 0;
		//Between v supernodes
		for(int i : graph.keySet()){
			matched = matched(i);
			if( matched != -1){
				int u0v0 = 0;
				//if this u node is matched u0v0 will get the weight of it's matched edge
				for(Link l: graph.get(i)){
					if(l.destination==matched){
						u0v0 = l.weight;
					}
				}
				//find the other v's that this matched u connects to
				for(Link l : graph.get(i)){
					if( l.destination != matched){
						//those v's must be matched so we add edges from supernode u0v0 to supernode corresponding 
						// to those v's
						phigraph.get(matched).add(new Link(l.destination, (u0v0 - phi[matched]) - (l.weight - phi[l.destination]) ));
					}
				}
			}
		}
		
		//Print out phigraph for debugging
//		for(int i : phigraph.keySet()){
//			d("Supernode in phigraph: " + i);
//			for(Link l : phigraph.get(i)){
//				d("		Connects to supernode: " + l.destination + " with weight " + l.weight);
//			}
//		}
		
		//int[] phiDistance = new int[phi.length]; 
		Map<Integer,Integer> phiDist = new HashMap<Integer,Integer>();
		
		//System.arraycopy(phi, 0, phiDistance, 0, phi.length);
		d("Past Phi construction");
		//do djikstra on auxilary, set phi[v] = max(O, phi[v]-distanceTo(v))
		
		PriorityQueue<Pair<Integer,Integer>> q = new PriorityQueue<Pair<Integer,Integer>>(matching.length+2,new Comparator<Pair<Integer,Integer>>(){
			public int compare(Pair<Integer, Integer> o1, Pair<Integer, Integer> o2){return o1.second-o2.second;}});
		q.add(new Pair<Integer,Integer>(SOURCE,0));
		
		Pair<Integer,Integer> cur;//first = node, second = weight
		while(!q.isEmpty()){
			d("While");
			cur = q.poll();
			d("Node being examined: " + cur.first+" with current distance "+phiDist.get(cur.first)+" and new "+cur.second);
			for(Link l:phigraph.get(cur.first)){
				Pair<Integer,Integer> n = new Pair<Integer,Integer>(l.destination,cur.second+l.weight);
				if(!phiDist.containsKey(n.first) || n.second<phiDist.get(n.first)) {
					q.remove(n);
					q.add(n);
					//phiDistance[n.first] = n.second;
					phiDist.put(n.first, n.second);
				} 
			}
		}
		
		for(int i =0;i<phi.length;++i)
			phi[i] = phiDist.containsKey(i)?Math.max(0,phi[i]-phiDist.get(i)):0;
		
		return true;
	}
	
	
	
	private static List<Integer> BFSToType2(Map<Integer, Pair<Integer,List<Link>>> g,int u){
		Queue<Integer> q = new LinkedList<Integer>();
		List<Integer> p = new LinkedList<Integer>();
		boolean[] seen = new boolean[U+1];
		Map<Integer,Integer> parent = new HashMap<Integer,Integer>();
		for(int i:g.keySet()){
			if(g.get(i).first==-1){//type one
				q.add(i);
				parent.put(i, -1);
				while(!q.isEmpty()){
					int front = q.poll();
					seen[front] = true;
					if(front == u){
						int nextParent = front;
						while(nextParent!=-1){
							p.add(nextParent);
							nextParent = parent.get(nextParent);
						}
						return p;
					}
					for(Link l:g.get(front).second){
						if(g.get(l.destination).first!=-1 && !seen[l.destination]){//only look at type 2
							parent.put(l.destination, front);
							q.add(l.destination);
						}
					}
				}
			}
		}
		return null;
	}
	
	private static boolean handleLine(String s){
		d("Graph:");
		for(int i : graph.keySet()) d(i+": "+graph.get(i));
		
		Scanner sc = new Scanner(s);
//		int source = sc.nextInt();
//		d(" source: "+source);
//		if(source<0) return false;
		graph.put(U, new ArrayList<Link>());//handleSource(U); //Add Node
		int destination = sc.nextInt();
		if(sc.hasNext())
			addEdge(U,destination, sc.nextInt());
		else
			return remove(destination);
		
		while(sc.hasNextInt()){
			destination = sc.nextInt();
			//if(destination<0) return false;
			addEdge(U,destination, sc.nextInt());//graph.get(source).add(new Link(sc.nextInt(), 0)); // update graph
		}

		if(weightsLTPhi(U)){
			++U;
			return true; // case 1 - for all edges in E(i + 1) w(e) < phi(e)
		}
		
		//ensureExcessSize();
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
		++U;
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
		
//	private static void ensureExcessSize(){
//		while(excess.size()<graph.size()) excess.add(0);
//	}
	
	private static void calculateExcess(int n){
		int max = Integer.MIN_VALUE, temp;
		for(Link l : graph.get(n)) if((temp = l.weight-phi[l.destination]) >max) max = temp; 
		excess.put(n, max);
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
		excess = new HashMap<Integer, Integer>();
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
		public Pair(){first = null;second = null;}
		public Pair(T tt, E ee){first = tt; second = ee;}
		
	}
	
}
