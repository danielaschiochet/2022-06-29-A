package it.polito.tdp.itunes.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.itunes.db.ItunesDAO;

public class Model {
	
	private List<Album> allAlbum;
	private SimpleDirectedWeightedGraph<Album, DefaultWeightedEdge> graph;
	private ItunesDAO dao;
	private List<Album> bestPath;
	private int bestScore;
	
	public Model() {
		this.allAlbum = new ArrayList<>();
		this.graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);	
		this.dao = new ItunesDAO();
	}
	
	public List<Album> getPath(Album source, Album target, int threshold) {
		List<Album> parziale = new ArrayList<>();
		this.bestPath = new ArrayList<>();
		this.bestScore = 0;
		parziale.add(source);
		
		ricorsione(parziale, target, threshold );
		
		return this.bestPath;
	}
	
	private void ricorsione(List<Album> parziale, Album target, int threshold) {
		Album current = parziale.get(parziale.size()-1);
		
		//condizione di uscita
		if(current.equals(target)) {
			//controllo se questa soluzione è migliore del best 
			if ( getScore(parziale) > this.bestScore ) {
				this.bestScore = getScore(parziale);
				this.bestPath = new ArrayList<>(parziale);
			}
			return;
		}
			
		//continuo ad aggiungere elementi in parziale
		List<Album> successors = Graphs.successorListOf(this.graph, current);
		
		for (Album a : successors) {
			if( this.graph.getEdgeWeight(this.graph.getEdge(current, a)) >= threshold && !parziale.contains(a)) {
				parziale.add(a);
				ricorsione(parziale, target, threshold);
				parziale.remove(a); //backtracking
			}
		}
	}

	private int getScore(List<Album> parziale) {
		int score = 0;
		Album source = parziale.get(0);
		
		for (Album a : parziale.subList(1, parziale.size()-1)) {
			if (getBilancio(a) > getBilancio(source))
				score += 1;
		}
		
		return score;
		
	}

	public List<bilancioAlbum> getAdiacenti (Album root){
		List<Album> successori = Graphs.successorListOf(this.graph, root);
		List<bilancioAlbum> bilancioSuccessori = new ArrayList<>();
		
		for (Album a : successori) {
			bilancioSuccessori.add(new bilancioAlbum(a, getBilancio(a)));
		}
		
		Collections.sort(bilancioSuccessori);
		
		return bilancioSuccessori;
		
	}
	
	
	public void buildGraph(int n) {
		clearGraph();
		loadNodes(n);
		
		Graphs.addAllVertices(this.graph, this.allAlbum);
		
		for (Album a1 : this.allAlbum) {
			for (Album a2 : this.allAlbum) {
				int peso = a1.getNumSongs() - a2.getNumSongs();
				
				if (peso > 0)
					Graphs.addEdgeWithVertices(this.graph, a2, a1, peso);
				
			}
		}
		
		System.out.println(this.graph.vertexSet().size());
		System.out.println(this.graph.edgeSet().size());

		
	}
	
	private int getBilancio(Album a) {
		int bilancio = 0;
		List<DefaultWeightedEdge> edgesIN = new ArrayList<>(this.graph.incomingEdgesOf(a));
		List<DefaultWeightedEdge> edgesOUT = new ArrayList<>(this.graph.outgoingEdgesOf(a));
		
		for (DefaultWeightedEdge edge : edgesIN)
			bilancio += this.graph.getEdgeWeight(edge);
		
		for (DefaultWeightedEdge edge : edgesOUT)
			bilancio -= this.graph.getEdgeWeight(edge);
		
		return bilancio;		
	}
	
	public List<Album> getVertices () {
		List<Album> allVertices = new ArrayList<>(this.graph.vertexSet());
		Collections.sort(allVertices);
		return allVertices;
		}
	
	private void clearGraph() {
		this.allAlbum = new ArrayList<>();
		this.graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);			
	}

	private void loadNodes(int n) {
		if(this.allAlbum.isEmpty())
			this.allAlbum = dao.getFilteredAlbums(n);
	}

	public int getNumVertices() {
		// TODO Auto-generated method stub
		return this.graph.vertexSet().size();
	}

	public int getNumEdges() {
		// TODO Auto-generated method stub
		return this.graph.edgeSet().size();
	}
	
	
	
	
	
	
	
	
	
	
}