import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Scanner;


public class Assignment1 {
	static int[] phi;
	static int[] matching;
	static Map<Integer,ArrayList<Link>> graph;
	static List<Integer> excess;
	
	public static void main(String[] args) {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int n = 0;
		try{
			n = Integer.parseInt(br.readLine());
			if(n>0){
				init(n);
				print();
				while(br.ready()){
					if(!handleLine(br.readLine())) break;
					print();
				}
			}
			
		}catch(IOException e){e.printStackTrace();}
	}
	
	private static boolean handleLine(String s){
		Scanner sc = new Scanner(s);
		int source = sc.nextInt(), newLink;
		if(source<0) return false;
		handleSource(source); //Add Node/Increment weights
		while(sc.hasNextInt()){
			newLink = sc.nextInt();
			graph.get(source).add(new Link(newLink, 0)); // update graph
		}
		if(matched(source)) return true; // case 1 - u matched in M
		ensureExcessSize();
		for(int i:graph.keySet()) calculateExcess(i);//Find Excess/tightness
		/* Two cases left:
		 * U matched in an mwmcm, BFS along tight edges
		 */
		List<Integer> list = new ArrayList<Integer>();
		list.add(source);
		Pair<Boolean,List<Integer>> p = DFSTight(source, list);
		if(p.first) invertPath(p.second);
		else changePhi(p.second);
		
		return true;
	}
	
	private static void invertPath(List<Integer> l){ 
		for(ListIterator<Integer> li = l.listIterator(); li.hasNext();){
			int u = li.next();
			int v = li.next();
			matching[v] = u;
		}
	}
	
	private static void changePhi(List<Integer> l){
		for(int i = 1;i<l.size();i+=2) ++phi[l.get(i)];
	}
	
	private static Pair<Boolean,List<Integer>> DFSTight(int start, List<Integer> visited){
		for(Link l : graph.get(start)) 
			if (l.tight && visited.get(visited.size()-1) != l.destination){
				visited.add(l.destination);
				return DFSMatched(l.destination, visited);
			}
		return new Pair<Boolean, List<Integer>>(new Boolean(false),visited);
	}
	private static Pair<Boolean,List<Integer>> DFSMatched(int start, List<Integer> visited){
		visited.add(matching[start]);
		if(excess.get(matching[start]) == 0) return new Pair<Boolean, List<Integer>>(true,visited);
		return DFSTight(matching[start],visited);
	}
	
	
	private static void ensureExcessSize(){
		while(excess.size()<graph.size()) excess.add(0);
	}
	
	private static void calculateExcess(int n){
		int max = Integer.MIN_VALUE, temp;
		for(Link l : graph.get(n))
			if((temp = l.weight-phi[l.destination])>max) max = temp; 
		excess.set(n, max);
		for(Link l : graph.get(n)) l.tight = max>=0 && ((l.weight-phi[l.destination]) == max);
	}
	
	private static boolean matched(int n){
		for(int i : matching)
			if( i == n) return true;
		return false;
	}
	
	private static void handleSource(int n){
		if(graph.containsKey(n))
			for(Link l : graph.get(n))
				++l.weight;
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
		StringBuilder sb = new StringBuilder();
		for(int i : matching) sb.append(i).append(' ');
		sb.append('\n');
		for(int i : phi) sb.append(i).append(' ');
		sb.append('\n');
		System.out.println(sb.toString());
	}
	private static class Link{
		public int destination, weight;
		public boolean tight;
		public Link(int d, int w){ destination = d; weight = w; tight=false;}

	}
	
	static class Pair<T,E>{
		T first;
		E second;
		public Pair(T tt, E ee){first = tt; second = ee;}
	}
	
}
