/* Best.java */

package player;

  /** This class is used in the gameTreeSearch function in MachinePlayer to find 
	*  the best move. In order to link the best move with its respective score, 
	*  we give this class fields for both the move and the score it receives.
	**/
public class Best {
	public Move move;
	public double score;
	
	/** This initializes the move as an empty move and the score as 0
	**/
	public Best() {
		this.move = null;
		this.score = 0;
	}
	
	/** This initializes the move as an empty move and the score to the one 
	*  given.
	* @param score is what we want initialized in the score field
	**/
	public Best(double score) {
		this.move = null;
		this.score = score;
	}

	/** This initializes both the move and the score as the ones given.
	* @param move is what we want initialized
	* @param score as well
	**/
	public Best(Move move, double score) {
		this.move = move;
		this.score = score;
	}
}