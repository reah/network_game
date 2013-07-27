/* MachinePlayer.java */

package player;
import list.*;

/**
*  An implementation of an automatic Network player.  Keeps track of moves
*  made by both players.  Can select a move for itself.
*/
public class MachinePlayer extends Player {

	public final static int QUIT = 0;
	public final static int ADD = 1;
	public final static int STEP = 2;

	public final static int EMPTY = 2;
	public final static int WHITE = 1;
	public final static int BLACK = 0;
	
	public final static int SIZE = 2;

	Board gameBoard = new Board();

	private int machinePlayerColor;
	private int oppColor;
	private int searchDepth;

	/** Creates a machine player with the given machinePlayerColor.  Color is 
   *  either 0 (black) or 1 (white).  (White has the first move.)
   * @param the color of "this" player
   **/
	public MachinePlayer(int color) {
		this(color, -1);
	}

	/** Creates a machine player with the given machinePlayerColor and search 
   *  depth.  Color is either 0 (black) or 1 (white).  (White has the first
   *   move.)
   * @param color of "this" player
   * @param searchDepth is the deepest we can look for moves
   **/
	public MachinePlayer(int color, int searchDepth) {
		this.machinePlayerColor = color;
		this.oppColor = (color + 1) % 2;
		this.searchDepth = searchDepth;
	}
	
	/** Tests each Move for a winning network, if a network is found, then
   *  that move is returned to calling Function. 
   * @param m is a move passed in to be tested
   * @return Move or null
   **/
	private Move findWin(Move m) {
		updateGameBoard(m, machinePlayerColor);
		if (gameBoard.isNetwork(oppColor)) {
			return null;
		} else if (gameBoard.isNetwork(machinePlayerColor)) { 
			return m;
		}
		return null;
	}

	/** Returns a new move by "this" player.  Internally records the move 
   *  (updates the internal game board) as a move by "this" player.
   * @returns a move by "this" player
   **/
public Move chooseMove() {
	Move[] moves = gameBoard.validMoves(this.oppColor);
	for(Move m : moves){
		if(m != null){
			updateGameBoard(m, this.oppColor);
			if(this.gameBoard.isNetwork(oppColor)){
				clearCoordinates();
				if(gameBoard.isValid(m, this.machinePlayerColor)){
					return m;
				}
			}
			undoMove(m, this.oppColor);
		}
	}

	Best bestMove;
	
	if (machinePlayerColor == WHITE) {
		if (searchDepth == -1 && gameBoard.whiteChips < 1) {
			bestMove = gameTreeSearch(machinePlayerColor, -50.0, 50.0, 1.0, 2);
		} else if (searchDepth == -1) {
			bestMove = gameTreeSearch(machinePlayerColor, -50.0, 50.0, 1.0, 3);
		} else {
			bestMove = gameTreeSearch(machinePlayerColor, -50.0, 50.0, 1.0, searchDepth);
		}
	} else {
		if (searchDepth == -1 && gameBoard.blackChips < 1) {
			bestMove = gameTreeSearch(machinePlayerColor, -50.0, 50.0, 1.0, 2);
		} else if (searchDepth == -1) {
			bestMove = gameTreeSearch(machinePlayerColor, -50.0, 50.0, 1.0, 3);
		} else {
			bestMove = gameTreeSearch(machinePlayerColor, -50.0, 50.0, 1.0, searchDepth);
		}
	}

	Move m = bestMove.move;
	updateGameBoard(m, machinePlayerColor);
	return m;
}

	/** If the Move m is legal, records the move as a move by the opponent
   * (updates the internal game board) and returns true.  If the move is
   * illegal, returns false without modifying the internal state of "this"
   * player.  This method allows your opponents to inform you of their moves.
   * @param m is the move the opponent is making
   * @return whether the move is valid or not
   **/
	public boolean opponentMove(Move m) {
		if(gameBoard.isValid(m, oppColor)){
			updateGameBoard(m, oppColor);
			return true;
		}
		return false;
	}


	/** If the Move m is legal, records the move as a move by "this" player
   *  (updates the internal game board) and returns true.  If the move is
   *  illegal, returns false without modifying the internal state of "this"
   *  player.  This method is used to help set up "Network problems" for your
   *  player to solve.
   * @param the move from "this" player
   * @return whether the move is valid
   **/
	public boolean forceMove(Move m) {
		if(gameBoard.isValid(m, machinePlayerColor)){
			updateGameBoard(m, machinePlayerColor);
			return true;
		}
		return false;
	}

	/** Updates the board for the player with a given move. Does not update
   * the board if the move is not valid.
   * @param m is the move we are updating with
   * @param player is who we are updating for
   **/
	private void updateGameBoard(Move m, int player) {
		if (player == WHITE) {
			gameBoard.addWhite(m.x1, m.y1);
			if (m.moveKind == STEP) {
				gameBoard.removeChip(m.x2, m.y2, player);
			}
		} else {
			gameBoard.addBlack(m.x1, m.y1);
			if (m.moveKind == STEP) {
				gameBoard.removeChip(m.x2, m.y2, player);
			}
		}
	}
	
	/** Reverses the process of a move done by a player. This is done for 
   * gameTreeSearch to build hypothetical boards to find the right move.
   * @param m is the move we are reversing
   * @param player is who originally did the move
   **/
	public void undoMove(Move m, int player){
		if (gameBoard.myBoard[m.x1 + 1][m.y1 + 1].item == gameBoard.EMPTY) {
			return;
		}
		gameBoard.removeChip(m.x1,m.y1,player);
		if (m.moveKind == STEP){
			if (player == WHITE){
				gameBoard.addWhite(m.x2,m.y2);
			} else {
				gameBoard.addBlack(m.x2,m.y2);
			}
		}
	}


	private void clearCoordinates(){
		for (int i = 0; i < 9; i++){
	 	   for (int j = 0; j < 9; j++){
				if (gameBoard.myBoard[i][j].item != EMPTY){
			    	gameBoard.myBoard[i][j].coordList = new SList();
			    	gameBoard.myBoard[i][j].visited = false;
				}
	 	   }
		}
    }
	
	/** This function uses alpha beta minimax search to find the best move.
   * Search goes down at most maxDepth moves and finds the best move based
   * on ratings of the hypothetical board built. The highest and lowest 
   * scores are given to boards that have a network for a certain player.
   * When a network hasn't been made, this function calls the evaluator 
   * function to get a score.
   * @param currPlayer is whose valid moves we are analyzing
   * @param alpha is the highest score we have found
   * @param beta is the lowest score we have found
   * @param depth is the current depth of searching we are at
   * @param maxDepth is the deepest this function can look
   * @return a Best object holding the best move and the score it has earned
   **/
	private Best gameTreeSearch(int currPlayer, double alpha, double beta, double depth, int maxDepth) {
		Best myBest = new Best();
		Best reply;
		boolean debug = false;
		
		clearCoordinates();

		// Base cases
		// if both have networks, other player wins 	
		if (gameBoard.isNetwork(currPlayer) && gameBoard.isNetwork((currPlayer + 1) % 2)) {		
			if (currPlayer == machinePlayerColor) {
				
				return new Best(null, (-50.0 / (double)depth));
			} else {
				return new Best(null, (50.0 / (double)depth));
			}
		}
	
		// if machine player wins
	  if (gameBoard.isNetwork(machinePlayerColor)) {
			if (depth == 1.0) {
				return new Best(null, 100.0);
			}
			return new Best(null, (50.0 / (double)depth));
		} 
		// if opponent wins
		if (gameBoard.isNetwork(oppColor)) {
			return new Best(null, (-50.0 / (double)depth));
		}
		// if max depth reached
		if (depth > maxDepth) {		
				return new Best(null, (gameBoard.evaluatorFcn(machinePlayerColor) / (double)depth));
		}
		
		// set worstcase scores
		if (currPlayer == machinePlayerColor) { 
			myBest.score = alpha;
		} else {
			myBest.score = beta;
		}
		
		// rebuild possible moves
		for (int k = 0; k < 10 - 1; k++){
			for (int l = 0; l < 10 - 1; l++){
				if (gameBoard.myBoard[k][l].item != EMPTY){
					gameBoard.myBoard[k][l].coordList = new SList();
					gameBoard.myBoard[k][l].visited = false;
				}
			}
		}
		
		// create array of valid moves
		Move[] moves = gameBoard.validMoves(currPlayer);
		
		// search through moves
		for (Move m : moves) {
			if (m != null) {
				updateGameBoard(m, currPlayer);
				reply = gameTreeSearch(((currPlayer + 1) % 2), alpha, beta, depth + 1, maxDepth);
				undoMove(m, currPlayer);	
				if ((currPlayer == machinePlayerColor) && (reply.score > myBest.score)) {
					myBest.move = m;
					myBest.score = reply.score;
					alpha = reply.score;
				} else if (( currPlayer == oppColor) && (reply.score < myBest.score)) {
					myBest.move = m;
					myBest.score = reply.score;
					beta = reply.score;
				}
				
				// alpha beta pruning
				if (alpha >= beta) {
					return myBest;
				} 
			} else {
				break;
			}
		}
		return myBest;
	}
	
	// Second evaluator function for possible evaluatorFcn replacement
	private double evaluatorFcn2(int nothing) {
		double score = 0.0;

		if (this.gameBoard.isNetwork(this.machinePlayerColor)) {
			score = 50.0;
		} else if (this.gameBoard.isNetwork(this.oppColor)) {
			score = -50.0;
		} else {
			int myChips = 0;
			int oppChips = 0;
			int myConnections = 0;
			int oppConnections = 0;

			if (this.machinePlayerColor == BLACK) {
				myChips = this.gameBoard.blackChips;
				oppChips = this.gameBoard.whiteChips;
			} else {
				myChips = this.gameBoard.whiteChips;
				oppChips = this.gameBoard.blackChips;
			}

			myConnections = this.gameBoard.countConnections(this.machinePlayerColor);
			oppConnections = this.gameBoard.countConnections(this.oppColor);

			score += 5 * (myConnections - oppConnections);

			for (int x = 1; x < 9; x++) {
				for (int y = 1; y < 9; y++) {
					if (this.gameBoard.myBoard[x][y].item != EMPTY) {
						for (int i = x - 1; i <= x + 1; i++) {
							for (int j = y - 1; j <= y + 1; j++) {
								if (this.gameBoard.myBoard[x][y].item == this.machinePlayerColor) {
									if (this.gameBoard.myBoard[i][j].item == this.machinePlayerColor) {
										score += 3;
									}
								} else {
									if (this.gameBoard.myBoard[i][j].item == this.oppColor)
										score -= 3;
								}
							}
						}
					}
				}
			}

			if (myChips + oppChips <= 2) {
				if (this.machinePlayerColor == BLACK) {
					for (int i = 2; i < 8; i++) {
						if ((this.gameBoard.myBoard[i][1].item == this.machinePlayerColor)
						|| (this.gameBoard.myBoard[i][8].item == this.machinePlayerColor)) {
							score += 2;
						}
					}
					for (int j = 2; j < 8; j++) {
						if ((this.gameBoard.myBoard[1][j].item == this.oppColor)
						|| (this.gameBoard.myBoard[8][j].item == this.oppColor)) {
							score -= 2;
						}
					}
				} else {
					for (int i = 2; i < 8; i++) {
						if ((this.gameBoard.myBoard[i][1].item == this.oppColor)
						|| (this.gameBoard.myBoard[i][8].item == this.oppColor)) {
							score -= 2;
						}
					}
					for (int j = 2; j < 8; j++) {
						if ((this.gameBoard.myBoard[1][j].item == this.machinePlayerColor)
						|| (this.gameBoard.myBoard[8][j].item == this.machinePlayerColor)) {
							score += 2;
						}
					}
				}
			}

			if (this.machinePlayerColor == BLACK
			&& (this.gameBoard.myBoard[5][1].item == BLACK || this.gameBoard.myBoard[5][8].item == BLACK)) {
				score += 1;
			}
			if (this.machinePlayerColor == WHITE
			&& (this.gameBoard.myBoard[1][5].item == WHITE || this.gameBoard.myBoard[8][5].item == WHITE)) {
				score += 1;
			}
		}
		return score;
	}
	
	
	
	// Test Code -- Reader Can Ignore
	public static void main(String[] args){
//		Move m = new Move(2,0);
//		Move m1 = new Move(3,1);
//		Move m3 = new Move(5,3);
//		Move m4 = new Move(3,5);
//		Move m5 = new Move(2,7);
//		
//		Move m7 = new Move(0,1);
//		Move m8 = new Move(5,1);
//		Move m9 = new Move(4,2);
//		Move m10 = new Move(0,4);
//		Move m11 = new Move(1,5);
//		
		
		Move m = new Move(1,0);
		Move m1 = new Move(3,4);
		Move m3 = new Move(6,4);
		Move m4 = new Move(1,6);
		Move m5 = new Move(6,7);
		
		Move m7 = new Move(0,1);
		Move m8 = new Move(6,1);
		Move m9 = new Move(2,2);
		Move m10 = new Move(2,3);
		Move m11 = new Move(7,4);
		Move m12 = new Move(2,5);

		
		MachinePlayer player = new MachinePlayer(WHITE, 4);	
		player.opponentMove(m);
		player.opponentMove(m1);
		player.opponentMove(m3);
		player.opponentMove(m4);
		player.opponentMove(m5);
		
		player.forceMove(m7);
		player.forceMove(m8);
		player.forceMove(m9);
		player.forceMove(m10);
		player.forceMove(m11);

		System.out.println(player.gameBoard);	

		player.chooseMove();
		
		System.out.println(player.gameBoard);	
	}
}

