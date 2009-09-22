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


public class Assignment1 {
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
				
				System.err.println("!!!!!!!BLOCK!!!!!!!");
				init(n);
				print();
				int line = 0;
				while(br.ready()){
					System.err.print("Reading line "+ ++line);
					if(!handleLine(br.readLine())) break;
					print();
				}
				n = Integer.parseInt(br.readLine());
			}
			
		}catch(IOException e){e.printStackTrace();}
	}
	
	private static boolean handleLine(String s){
//		for(int i : graph.keySet()){
//			System.err.println(i+": "+graph.get(i));
//		}
		Scanner sc = new Scanner(s);
		int source = sc.nextInt(), newLink;
		System.err.println(" source: "+source);
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
		vVisited = new HashSet<Integer>();
		System.err.println("Call DFSTight on source = "+source);
		Pair<Boolean,List<Integer>> p = DFSTight(source, new ArrayList<Integer>(), new boolean[graph.size()][matching.length]);
		System.err.println("Ret path: "+p.second);
		if(p.first){
			p.second.add(source);
			invertPath(p.second);
		}
		else changePhi();
		
		return true;
	}
	
	private static void invertPath(List<Integer> l){ 
		System.err.println("Invert Path: "+l);
		for(ListIterator<Integer> li = l.listIterator(l.size()); li.hasPrevious();){
			int v = li.previous();
			if(!li.hasPrevious()) break;
			int u = li.previous();
			matching[u] = v;
		}
	}
	
	private static void changePhi(){
		//System.err.println("Fix Phi");
		for(int v:vVisited) ++phi[v];
	}
	
	private static Pair<Boolean,List<Integer>> DFSTight(int start, List<Integer> path, boolean[][] visited){
		System.err.println("DFSTighting from u"+start);
		for(Link l : graph.get(start)) 
			if (l.tight && !visited[start][l.destination]){
				vVisited.add(l.destination);
				visited[start][l.destination] = true;
				System.err.println("Explore from u"+start+" to v"+l.destination);
				System.err.println("Explore from v"+l.destination+" to u"+matching[l.destination]);
				if(excess.get(matching[l.destination]) == 0){
					System.err.println("Add u' = "+matching[l.destination]+" and "+l.destination);
					path.add(matching[l.destination]);
					path.add(l.destination);
					return new Pair<Boolean, List<Integer>>(true,path);
				}
				
				Pair<Boolean,List<Integer>> p = DFSTight(matching[l.destination],path, visited);
				if(p.first){
					System.err.println("Add u"+matching[l.destination]+" and "+l.destination+" to path");
					p.second.add(matching[l.destination]);
					p.second.add(l.destination);
					return p;
				}
			}
		System.err.println("DFSTight has no nodes to visit");
		return new Pair<Boolean, List<Integer>>(new Boolean(false),path);
	}
	
	/*
	private static Pair<Boolean,List<Integer>> DFSTight(int start, List<Integer> path, boolean[][] visited){
		//System.err.println("DFSTighting from u"+start);
		for(Link l : graph.get(start)) 
			if (l.tight && !visited[start][l.destination]){
				vVisited.add(l.destination);
				visited[start][l.destination] = true;
				//System.err.println("Tighting from u"+start+" to v"+l.destination);
				Pair<Boolean,List<Integer>> p = DFSMatched(l.destination,path, visited); 
				if(p.first){
					//System.err.println("Found u' = "+p.second.get(p.second.size()-1)+" found = "+p.first);
					path.add(l.destination);
					return p;
				} 
			}
		//System.err.println("DFSTight has no nodes to visit");
		return new Pair<Boolean, List<Integer>>(new Boolean(false),path);
	}
	private static Pair<Boolean,List<Integer>> DFSMatched(int start, List<Integer> path, boolean[][] visited){
		//System.err.println("DFSSMatcheding from v"+start+" to u"+matching[start]);
		if(excess.get(matching[start]) == 0){
			path.add(matching[start]);
			return new Pair<Boolean, List<Integer>>(true,path);
		}
		Pair<Boolean,List<Integer>> p = DFSTight(matching[start],path, visited);
		if(p.first) path.add(start);
		return p;
	}*/
	
	
	private static void ensureExcessSize(){
		while(excess.size()<graph.size()) excess.add(0);
	}
	
	private static void calculateExcess(int n){
		int max = Integer.MIN_VALUE, temp;
		for(Link l : graph.get(n))
			if((temp = l.weight-phi[l.destination]) >max) max = temp; 
		excess.set(n, max);
		for(Link l : graph.get(n)) l.tight = ( max>=0 && ((l.weight-phi[l.destination]) == max));
//		if(n == 0){
//			System.err.println("Excess of 0: "+excess.get(0));
//			System.err.println("Max of 0: "+max);
//			for(Link l : graph.get(n)){
//				System.err.println("0 to "+l.destination);
//				if(l.tight) System.err.println("Tight from 0 to "+l.destination);
//			}
//		}
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
		//sb.append('\n');
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
