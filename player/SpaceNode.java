/* SpaceNode.java */

package player;
import list.*;

/** This class represents one cell on a gameboard. It has fields for what is
*  occupying the cell, whether it has been visited by the network-checking
*  methods and a list of all the other chips of the same color it can see
**/
public class SpaceNode{
	int item = Board.EMPTY;
	SList coordList = new SList();
	boolean visited;

	// Test Code -- Grader Can Ignore 
	public static void main(String[] args){
		SpaceNode a = new SpaceNode();
		System.out.println(a.item);
		System.out.println(a.visited);		
	}
}


