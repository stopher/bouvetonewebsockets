package no.bouvet.bouvetone;
public class Visitor {
	
	public String name;
	public int posx;
	public int posy;
	private int id;	
	public Visitor(int id) {
		this.id = id;
	}
	public int getId() {
		return id;
	}
	
}
