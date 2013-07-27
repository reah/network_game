/* Board.java  */

package player;
import list.*;

/** A class that represents the game board and all of its pieces. This board is
*   a 10 x 10 array of SpaceNode objects that hold an item for what the cell 
*   contains and an SList that holds coordinates of all the other chips of the 
*   same color. The board is bigger than the 9 x 9 board to make the isValid 
*   function easier.
*/
public class Board {
	static final int EMPTY = 2;
	static final int WHITE = 1;
	static final int BLACK = 0;

	static final int SIZE = 10;
	static final int CHIPS = 10;

	static final int CONTINUE_SEARCH = 0;

	static final int NORTH = 1;
	static final int NORTHEAST = 2;
	static final int EAST = 3;
	static final int SOUTHEAST = 4;
	static final int SOUTH = 5;
	static final int SOUTHWEST = 6;
	static final int WEST = 7;
	static final int NORTHWEST = 8;

	static boolean GAMEOVER = false;

	int blackChips;
	int whiteChips;
	SpaceNode[][] myBoard;

	Board() {
		myBoard = new SpaceNode[SIZE][SIZE];
		for(int j = 0; j < SIZE; j++){
			for(int i = 0; i < SIZE; i++){
				myBoard[i][j] = new SpaceNode();
			}
		}
		blackChips = CHIPS;
		whiteChips = CHIPS;
	}

	Board(Board b) {
		myBoard = b.myBoard;
		blackChips = b.blackChips;
		whiteChips = b.whiteChips;
	}

	/** This function returns all the valid moves possible for a certain player
	*  on this board. The size of the array returned depends on whether ADD or
	*  STEP moves can be performed
	*  @param player is who we want to know which moves are valid
	*  @return an array of all possible valid moves of size that varies
	*/
	Move[] validMoves(int player) {
		// how many chips are left in the players chips
		int playerChips;
		if (player == BLACK) {
			playerChips = blackChips;
		} else {
			playerChips = whiteChips;
		}
		// now if playerChips < 0 then move is STEP MOVE

		// create appropriate size array for either add moves or step moves
		Move[] moveArr;
		if (playerChips > 0) {
			moveArr = new Move[48 - (CHIPS - playerChips)];
		} else {
			moveArr = new Move[38 * CHIPS];
		}

		// fill moveArr
		int counter = 0; 
		Move m;
		for (int i = 0; i < 8; i++) {
			for (int j = 0; j < 8; j++) {
				if ((playerChips < 1) && (myBoard[i + 1][j + 1].item == player)) {
					for (int k = 0; k < 8; k++) {
						for (int l = 0; l < 8; l++) {
							if (myBoard[k + 1][l + 1].item == EMPTY) {
								m = new Move(k, l, i, j);
								if (isValid(m, player)) {
									moveArr[counter] = m;
									counter++;
								}
							}
						}
					}
				} else if (myBoard[i + 1][j + 1].item == EMPTY) {
					m = new Move(i, j);
					if (isValid(m, player)) {
						moveArr[counter] = m;
						counter++;
					}
				}
			}
		}
		return moveArr;
	}

	/** This updates this board to add a black chip in the desired spot
	*  and subtract from the current black chip count.
	*  @param x is the x coordinate of the location we want to add.
	*  @param y is the y coordinate of the location we want to add.
	*/
	 void addBlack(int x, int y) {
		myBoard[x + 1][y + 1].item = BLACK;
		blackChips--;
	}

	/** This updates this board to add a white chip in the desired spot
	*  and subtract from the current white chip count.
	*  @param x is the x coordinate 
	*  @param y is the y coordinate 
	*/
	void addWhite(int x, int y) {
		myBoard[x + 1][y + 1].item = WHITE;
		whiteChips--;
	}

	/** This removes a chip from the board and adds a chip to the chip count.
	*  @param x is the x coordinate 
	*  @param y is the y coordinate
	*  @param player is the color of chip we want to remove.
	**/
	void removeChip(int x, int y, int player) {
		myBoard[x + 1][y + 1].item = EMPTY;
		myBoard[x + 1][y + 1].coordList = new SList();
		if (player == WHITE) {
			whiteChips++;
		} else {
			blackChips++;
		}
	}

	/** Finds out whether a given move is valid for the player. This method
	* takes into account the number of chips left, whether the game is over,
	* whether the move is trying to add a chip in a spot that is not empty,
	* and whether the chip added will create a group of 3.
	* @param m is the move that we want to know is valid or not.
	* @param player is the player that is making this move.
	* @return whether the move is valid.
	**/
	boolean isValid(Move m, int player) { 
		// no longer have valid moves if GAMEOVER
		boolean debug = false;
		if (GAMEOVER) {
			return false;
		}

		// if movekind is quit then return false evermore. 
		if(m.moveKind == Move.QUIT){
			GAMEOVER = true;
			return false;
		}

		// no longer add chips if number of chips is 0
		if (m.moveKind==Move.ADD){
			if (player==WHITE){
				if (whiteChips == 0){
					return false;
				}
			}else{
				if (blackChips == 0){
					return false;
				}
			}
		}

		// if steping from a different color then false
		if(m.moveKind == Move.STEP){
			if(myBoard[m.x2 + 1][m.y2 + 1].item != player){
				return false;
			}
		}

		// reassigning x,y values for our larger array
		int x = m.x1 + 1;
		int y = m.y1 + 1;

		// 3) No chip may be placed in a square that is already occupied
		if (myBoard[x][y].item != EMPTY) {
			return false;
		}

		// 1) no chip may be placed in any of the four corners
		if ((x == 1 && y == 1) || (x == 1 && y == 8) ||
		(y == 1 && x==8) || (x == 8 && y == 8)) {
			return false;
		}

		// 2) No chip may be placed in a goal of the opposite color.
		if (player == BLACK) {
			if (x < 2 || x > 7 || y < 1 || y > 8) {
				return false;
			}
		} else {
			if (x < 1 || x > 8 || y < 2 || y > 7) {
				return false;
			}
		}

		// 4) A player may not have more than two chips in a connected group
		int countMain = 0;
		int countSecondary = 0;

		// if step we need to remove from square to check move to position.
		if (m.moveKind == Move.STEP) {
			removeChip(m.x2, m.y2, player);
		}

		for (int i = (x - 1); i <= (x + 1); i++) {
			for (int j = (y - 1); j <= (y + 1); j++) {
				if (debug) {
					System.out.println("Main: (" + (i - 1) + "," + (j - 1) + ") -- Item: " + myBoard[i][j].item);
				}
				if (myBoard[i][j].item == player) {
					countMain++;
					for (int k = (i - 1); k <= (i + 1); k++) {
						for (int l = (j - 1); l <= (j + 1); l++) {
							if (debug) {
								System.out.println("Secondary: (" + (k - 1) + "," + (l - 1) + ") -- Item: " + myBoard[k][l].item);
							}
							if (myBoard[k][l].item == player) {
								countSecondary++;
							}
						}
					}
					if (countSecondary >= 2) {
						// replacing the chip we removed for the step test
						if (m.moveKind == Move.STEP) {
							if (player == WHITE) {
								addWhite(m.x2, m.y2);
							} else {
								addBlack(m.x2, m.y2);
							}
						}
						return false;
					}
					countSecondary = 0;
				}
			}
		}
		if (countMain >= 2) {
			// replacing the chip we removed for the step test
			if (m.moveKind == Move.STEP) {
				if (player == WHITE) {
					addWhite(m.x2, m.y2);
				} else {
					addBlack(m.x2, m.y2);
				}
			}
			return false;
		}	

		// replacing the chip we removed for the step test
		if (m.moveKind == Move.STEP) {
			if (player == WHITE) {
				addWhite(m.x2, m.y2);
			} else {
				addBlack(m.x2, m.y2);
			}
		}
		return true;
	}

	/** Returns whether this given board has a network for the specified player
	*  @param the player in question
	*  @return whether the player has a network for this board
	**/
	boolean isNetwork(int player) {
		// check if enough chips have been played
		if ((player == BLACK && blackChips > 4)
		|| (player == WHITE && whiteChips > 4)) {
			return false;
		} 

		// check left white goal for chips then right goal for networks
		if (player == WHITE) {
			int whiteGoal = 0;
			for (int j = 2; j < SIZE - 2; j++) {
				if (this.myBoard[1][j].item == WHITE) {
					whiteGoal++;
				}
			}
			if (whiteGoal < 1) {
				return false;
			}
			for (int j = 2; j < SIZE - 2; j++) {
				if (this.myBoard[8][j].item == WHITE) {
					if (findNetwork(8, j, player, 1, SOUTH)) {
						return true;
					}
				}
			}
		}

		// check top black goal for chips then bottom goal for networks
		if (player == BLACK) {
			int blackGoal = 0;
			for (int i = 2; i < SIZE - 2; i++) {
				if (this.myBoard[i][1].item == BLACK) {
					blackGoal++;
				}
			}
			if (blackGoal < 1) {
				return false;
			}
			for (int i = 2; i < SIZE - 2; i++) {
				if (this.myBoard[i][8].item == BLACK) {
					if (findNetwork(i, 8, player, 1, WEST)) { 
						return true;
					}
				}
			}
		}
		return false;
	}

	/** This finds out whether a cell is apart of a working network. The 
	* counter goes from right to left (for white) and from bottom to top
	* (for black) and if the base cases hold true, then a network has been
	* made. Once we go from a chip to another in a certain direction, we
	* cannot follow that same direction. 
	* @param x coordinate of cell
	* @param y coordinate of cell
	* @param player is who we want to find the network for
	* @param counter keeps track of how many chips we have gone through
	* @param lastDirection tells us from what direction we have came from
	* @return whether a network has been found
	**/
	private boolean findNetwork(int x, int y, int player, int counter, int lastDirection) {
		if (counter > 10) {
			return false;
		}

		// base cases for white
		if (player == WHITE) {
			if (counter < 6 && x == 1) {
				return false;
			}
			if (counter > 1 && x > 7) {
				return false;
			} 
			if (counter == 10 && x != 1) {
				return false;
			}
			if (counter >= 6 && x == 1) {
				return true;
			}
		}

		// base case for black
		if (player == BLACK) {
			// don't look into same goal that we started in
			if (counter < 6 && y == 1) {
				return false;
			}
			if (counter > 1 && y > 7) {
				return false;
			}
			if (counter == 10 && y!= 1) {
				return false;
			}
			if (counter >= 6 && y == 1) {
				return true;
			}
		}

		// What chips can this chip see?
		myBoard[x][y].coordList = new SList(); // here
		basicSeerFcn(this, x, y, player, true);

		// return false if coordList is empty
		if (myBoard[x][y].coordList.isEmpty()) {
			return false;
		}

		// set space to visited (need to unset at bottom)
		myBoard[x][y].visited = true;

		// make list of possible moves (shrink SList)
		SList tempList = myBoard[x][y].coordList;
		SListNode tempNode = (SListNode)tempList.front();
		SListNode nextNode;
		try {
			while (tempNode.isValidNode()) {
				nextNode = (SListNode)tempNode.next();
				if (myBoard[tempNode.valX()][tempNode.valY()].visited == true) {
					tempNode.remove();
				} else if (tempNode.item() == lastDirection){
					tempNode.remove();
				}
				tempNode = nextNode;
			}
		} catch (InvalidNodeException ine) {
			System.err.println("InvalidNodeException 1");
		}

		// base case, if tempList empty return false
		if (tempList.length()  == 0) {
			return false;
		}

		tempNode = (SListNode)tempList.front();
		try {
			while (tempNode.isValidNode()) {
				if (findNetwork(tempNode.valX(), tempNode.valY(), player, counter + 1, tempNode.item())) {
					return true;
				}
				tempNode = (SListNode)tempNode.next();
			}
		} catch (InvalidNodeException ine) {
			System.err.println("InvalidNodeException 2");
		}

		// unset space to visited
		myBoard[x][y].visited = false;

		return false;
	}

	/** This function fills up the SList for each SpaceNode that holds the 
	* coordinates of the other chips of the same color it can see. If it 
	* sees a chip of another color or a chip it has already seen before,
	* then this chip is not added to the SList.
	* @param b is the board we are using
	* @param x is the x coordinate of the board
	* @param y is the y coordinate of the board
	* @param player is the color of chips we are trying to see
	* @param firstLevel determines how many levels away a chip can see
	**/
	private void basicSeerFcn(Board b, int x, int y, int player, boolean firstLevel) {
		int opponent = (player + 1) % 2;
		boolean foundOpp = false;
		int j;

		b.myBoard[x][y].coordList = new SList();

		// going W
		for (int i = (x - 1); i > 0; i--) {
			if (b.myBoard[i][y].item == opponent || b.myBoard[i][y].visited == true) {	
				foundOpp = true;
			}
			if (b.myBoard[i][y].item == player && foundOpp == false) {
				b.myBoard[x][y].coordList.insertFront(WEST, i, y);
				break;
			}
		}

		// going E
		foundOpp = false;
		for (int i = (x + 1); i < (SIZE - 1); i++) {
			if (b.myBoard[i][y].item == opponent || b.myBoard[i][y].visited == true) {
				foundOpp = true;	
			}
			if (b.myBoard[i][y].item == player && foundOpp == false) {
				b.myBoard[x][y].coordList.insertFront(EAST, i, y);
				break;
			}
		}

		// going N
		foundOpp = false;
		for (j = (y - 1); j > 0; j--) {
			if (b.myBoard[x][j].item == opponent || b.myBoard[x][j].visited == true) {
				foundOpp = true;
			}
			if (b.myBoard[x][j].item == player && foundOpp == false) {
				b.myBoard[x][y].coordList.insertFront(NORTH, x, j);
				break;
			}
		}

		// going S
		foundOpp = false;
		for (j = (y + 1); j < SIZE - 1; j++) {
			if (b.myBoard[x][j].item == opponent || b.myBoard[x][j].visited == true) {
				foundOpp = true;
			}
			if (b.myBoard[x][j].item == player && foundOpp == false) {
				b.myBoard[x][y].coordList.insertFront(SOUTH, x, j);
				break;
			}
		}	

		// going NW
		j = (y - 1);
		foundOpp = false;
		for (int i = (x - 1); i > 0; i--) {
			if (j < 1) {
				break;
			}
			if (b.myBoard[i][j].item == opponent || b.myBoard[i][j].visited == true) {
				foundOpp = true;
			}
			if (b.myBoard[i][j].item == player && foundOpp == false) {
				b.myBoard[x][y].coordList.insertFront(NORTHWEST, i, j);
				break;
			}
			j--;
		}

		// going NE
		j = (y - 1);
		foundOpp = false;
		for (int i = (x + 1); i < SIZE - 1; i++) {
			if (j < 1) {
				break;
			}
			if (b.myBoard[i][j].item == opponent || b.myBoard[i][j].visited == true) {
				foundOpp = true;
			}
			if (b.myBoard[i][j].item == player && foundOpp == false) {
				b.myBoard[x][y].coordList.insertFront(NORTHEAST, i, j);
				break;
			}
			j--;
		}

		// going SW
		j = (y + 1);
		foundOpp = false;
		for (int i = (x - 1); i > 0; i--) {
			if (j > SIZE - 1) {
				break;
			}
			if (b.myBoard[i][j].item == opponent || b.myBoard[i][j].visited == true) {
				foundOpp = true;
			}
			if (b.myBoard[i][j].item == player && foundOpp == false) {
				b.myBoard[x][y].coordList.insertFront(SOUTHWEST, i, j);
				break;
			}
			j++;
		}

		// going SE
		j = (y + 1);
		foundOpp = false;
		for (int i = (x + 1); i < SIZE - 1; i++) {
			if (j > SIZE - 1) {
				break;
			}
			if (b.myBoard[i][j].item == opponent || b.myBoard[i][j].visited == true) {
				foundOpp = true;
			}
			if (b.myBoard[i][j].item == player && foundOpp == false) {
				b.myBoard[x][y].coordList.insertFront(SOUTHEAST, i, j);
				break;
			}
			j++;
		}
	}

	/** This function fills up the SList for each SpaceNode that holds the 
	* coordinates of the other chips of the same color it can see. If it 
	* sees a chip of another color or a chip it has already seen before,
	* then this chip is not added to the SList.
	* @param b is the board we are using
	* @param x is the x coordinate of the board
	* @param y is the y coordinate of the board
	* @param player is the color of chips we are trying to see
	* @param firstLevel determines how many levels away a chip can see
	**/
	private void seerFcn(Board b, int x, int y, int player, boolean firstLevel) {
		int opponent = (player + 1) % 2;
		boolean foundOpp = false;
		int j;

		b.myBoard[x][y].coordList = new SList();

		// going W
		for (int i = (x - 1); i > 0; i--) {
			if (b.myBoard[i][y].item == opponent || b.myBoard[i][y].visited == true) {	
				foundOpp = true;
			}
			if (b.myBoard[i][y].item == player && foundOpp == false) {
				b.myBoard[x][y].coordList.insertFront(WEST, i, y);
			}
		}

		// going E
		foundOpp = false;
		for (int i = (x + 1); i < (SIZE - 1); i++) {
			if (b.myBoard[i][y].item == opponent || b.myBoard[i][y].visited == true) {
				foundOpp = true;	
			}
			if (b.myBoard[i][y].item == player && foundOpp == false) {
				b.myBoard[x][y].coordList.insertFront(EAST, i, y);
			}
		}

		// going N
		foundOpp = false;
		for (j = (y - 1); j > 0; j--) {
			if (b.myBoard[x][j].item == opponent || b.myBoard[x][j].visited == true) {
				foundOpp = true;
			}
			if (b.myBoard[x][j].item == player && foundOpp == false) {
				b.myBoard[x][y].coordList.insertFront(NORTH, x, j);
			}
		}

		// going S
		foundOpp = false;
		for (j = (y + 1); j < SIZE - 1; j++) {
			if (b.myBoard[x][j].item == opponent || b.myBoard[x][j].visited == true) {
				foundOpp = true;
			}
			if (b.myBoard[x][j].item == player && foundOpp == false) {
				b.myBoard[x][y].coordList.insertFront(SOUTH, x, j);
			}
		}	

		// going NW
		j = (y - 1);
		foundOpp = false;
		for (int i = (x - 1); i > 0; i--) {
			if (j < 1) {
				break;
			}
			if (b.myBoard[i][j].item == opponent || b.myBoard[i][j].visited == true) {
				foundOpp = true;
			}
			if (b.myBoard[i][j].item == player && foundOpp == false) {
				b.myBoard[x][y].coordList.insertFront(NORTHWEST, i, j);
			}
			j--;
		}

		// going NE
		j = (y - 1);
		foundOpp = false;
		for (int i = (x + 1); i < SIZE - 1; i++) {
			if (j < 1) {
				break;
			}
			if (b.myBoard[i][j].item == opponent || b.myBoard[i][j].visited == true) {
				foundOpp = true;
			}
			if (b.myBoard[i][j].item == player && foundOpp == false) {
				b.myBoard[x][y].coordList.insertFront(NORTHEAST, i, j);
			}
			j--;
		}

		// going SW
		j = (y + 1);
		foundOpp = false;
		for (int i = (x - 1); i > 0; i--) {
			if (j > SIZE - 1) {
				break;
			}
			if (b.myBoard[i][j].item == opponent || b.myBoard[i][j].visited == true) {
				foundOpp = true;
			}
			if (b.myBoard[i][j].item == player && foundOpp == false) {
				b.myBoard[x][y].coordList.insertFront(SOUTHWEST, i, j);
			}
			j++;
		}

		// going SE
		j = (y + 1);
		foundOpp = false;
		for (int i = (x + 1); i < SIZE - 1; i++) {
			if (j > SIZE - 1) {
				break;
			}
			if (b.myBoard[i][j].item == opponent || b.myBoard[i][j].visited == true) {
				foundOpp = true;
			}
			if (b.myBoard[i][j].item == player && foundOpp == false) {
				b.myBoard[x][y].coordList.insertFront(SOUTHEAST, i, j);
			}
			j++;
		}
	}

	/** This function evaluates this board and gives it a rating based on how
	* likely the player is at winning. If the player is on its way to losing,
	* a negative number is returned. Otherwise a positive number is returned.
	* @param player is who we want to find out has the upper hand or not
	* @return a rating between -1.0 and 1.0
	**/
	double evaluatorFcn(int player) {		
		int opponent = (player +1 ) % 2;
		double result;
		double enemyCount = 0.0;
		double heroCount = 0.0;
		double friendBorder = borderCount(player);
		double foeBorder = borderCount(opponent);

		for (int i = 2; i < SIZE - 2; i++){
			for (int j = 2; j < SIZE - 2; j++){
				if (this.myBoard[i][j].item == player){
					basicSeerFcn(this,i,j,player,true);
					heroCount += (double)myBoard[i][j].coordList.length();
				} else if (this.myBoard[i][j].item == opponent){
					seerFcn(this, i, j, opponent, true);
					enemyCount += (double)myBoard[i][j].coordList.length();
				}
			}
		}
		heroCount = heroCount * (double)friendBorder;
		enemyCount = enemyCount * (double)foeBorder;
		result = (heroCount-enemyCount)/(heroCount+enemyCount);
		return result * 50; 		
	}
	
	int countConnections(int player){
		int myConnections = 0;
		for (int i = 2; i < SIZE - 2; i++){
			for (int j = 2; j < SIZE - 2; j++){
				if (this.myBoard[i][j].item == player){
					seerFcn(this,i,j,player,true);
					myConnections += (double)myBoard[i][j].coordList.length();
				}
			}
		}
		return myConnections;
	}

	private double borderCount(int player) {
		double count = 0.0;
		if (player==WHITE){
			for (int i=2; i<SIZE-2;i++){
				if (this.myBoard[1][i].item==player){
					count++;
				}
				if (this.myBoard[8][i].item==player){
					count++;
				}
			}
		}else{
			for (int j=2; j<SIZE-2;j++){
				if (this.myBoard[j][1].item==player){
					count++;
				}
				if (this.myBoard[j][8].item==player){
					count++;
				}
			}
		}
		if (count<=2.0){
			return 1.0;
		}else{
			return 2.0/(double)count;
		}
	}

	private String border() {
		System.out.print('\n');
		for (int i = 0; i<SIZE+7;i++){
			System.out.print("- ");
		}
		System.out.print('\n');
		return "";
	}

	public String toString() {
		border();
		for (int j=0;j<SIZE-2;j++){
			System.out.print("| ");
			for (int i = 0; i<SIZE-2; i++){
				if (myBoard[i+1][j+1].item==WHITE){
					System.out.print("W" + " | ");
				}
				else if(myBoard[i+1][j+1].item==BLACK){
					System.out.print("B" + " | ");
				}
				else{
					System.out.print(" " + " | ");
				}
			}
			border();
		}
		return "";
	}

  // Test Code. Reader Can Ignore
	private static void testNetworks() {

		// First Board Test
		Board a = new Board();
		System.out.println("Printing First myBoard:");

		a.addBlack(6, 0);
		a.addBlack(2, 1);
		a.addBlack(4, 1);
		a.addBlack(2, 2);
		a.addBlack(5, 2);
		a.addBlack(2, 4);
		a.addBlack(5, 4);
		a.addBlack(5, 5);
		a.addWhite(0, 1);
		a.addWhite(6, 1);
		a.addWhite(4, 2);
		a.addWhite(6, 3);
		a.addWhite(6, 4);
		a.addWhite(4, 5);	
		a.addWhite(4, 6);
		a.addWhite(6, 6);
		a.addWhite(7, 6);
		System.out.println(a);
		System.out.println("Testing evaluator White:" + a.evaluatorFcn(WHITE));
		System.out.println("Testing evaluator Black:" + a.evaluatorFcn(BLACK));
		System.out.println("BLACK NETWORK: " + a.isNetwork(BLACK) + "    Should be: false");
		System.out.println("WHITE NETWORK: " + a.isNetwork(WHITE) + "    Should be: true");

		// Second Board Test
		a = new Board();
		System.out.println("Printing Second myBoard:");

		a.addBlack(2, 0); 
		a.addBlack(2, 2);
		a.addBlack(1, 3);
		a.addBlack(5, 3);
		a.addBlack(5, 5);
		a.addBlack(3, 6);
		a.addBlack(1, 7);
		a.addBlack(6, 7);
		a.addWhite(0, 1);
		a.addWhite(1, 1);
		a.addWhite(3, 1);	
		a.addWhite(5, 1);
		a.addWhite(6, 1);
		a.addWhite(3, 5);
		a.addWhite(7, 5);
		a.addWhite(6, 6);
		System.out.println(a);
		System.out.println("Testing evaluator White:" + a.evaluatorFcn(WHITE));
		System.out.println("Testing evaluator Black:" + a.evaluatorFcn(BLACK));
		System.out.println("BLACK NETWORK: " + a.isNetwork(BLACK) + "    Should be: true");
		System.out.println("WHITE NETWORK: " + a.isNetwork(WHITE) + "    Should be: false");


		// Third Board Test
		a = new Board();
		System.out.println("Printing Third myBoard:");

		a.addBlack(1, 0); 
		a.addBlack(4, 0);
		a.addBlack(4, 3);
		a.addBlack(1, 4);
		a.addBlack(2, 4);
		a.addBlack(6, 4);
		a.addWhite(0, 1);
		a.addWhite(5, 1);
		a.addWhite(6, 2);	
		a.addWhite(4, 4);
		a.addWhite(5, 4);
		a.addWhite(5, 6);
		a.addWhite(7, 6);
		System.out.println(a);
		System.out.println("Testing evaluator White:" + a.evaluatorFcn(WHITE));
		System.out.println("Testing evaluator Black:" + a.evaluatorFcn(BLACK));
		System.out.println("BLACK NETWORK: " + a.isNetwork(BLACK) + "    Should be: false");
		System.out.println("WHITE NETWORK: " + a.isNetwork(WHITE) + "    Should be: true");

		// Fourth Board Test
		a = new Board();
		System.out.println("Printing Fourth myBoard:");

		a.addBlack(4, 0); 
		a.addBlack(5, 0);
		a.addBlack(1, 1);
		a.addBlack(2, 1);
		a.addBlack(5, 2);
		a.addBlack(6, 2);
		a.addBlack(4, 4);
		a.addBlack(5, 5);
		a.addBlack(3, 7);
		a.addBlack(4, 7);
		a.addWhite(3, 1);
		a.addWhite(5, 1);
		a.addWhite(1, 2);	
		a.addWhite(5, 3);
		a.addWhite(7, 3);
		a.addWhite(3, 4);
		a.addWhite(0, 6);
		a.addWhite(1, 6);
		a.addWhite(3, 6);
		a.addWhite(4, 6);
		System.out.println(a);
		System.out.println("Testing evaluator White:" + a.evaluatorFcn(WHITE));
		System.out.println("Testing evaluator Black:" + a.evaluatorFcn(BLACK));
		System.out.println("BLACK NETWORK: " + a.isNetwork(BLACK) + "    Should be: false");
		System.out.println("WHITE NETWORK: " + a.isNetwork(WHITE) + "    Should be: true");

		// Fifth Board Test
		a = new Board();
		System.out.println("Printing Fifth myBoard:");

		a.addBlack(5, 0); 
		a.addBlack(6, 0);
		a.addBlack(4, 3);
		a.addBlack(2, 6);
		a.addBlack(2, 7);
		a.addWhite(0, 1);
		a.addWhite(6, 1);
		a.addWhite(7, 2);	
		a.addWhite(3, 4);
		a.addWhite(2, 5);
		a.addWhite(6, 5);
		System.out.println(a);
		System.out.println("Testing evaluator White:" + a.evaluatorFcn(WHITE));
		System.out.println("Testing evaluator Black:" + a.evaluatorFcn(BLACK));
		System.out.println("BLACK NETWORK: " + a.isNetwork(BLACK) + "    Should be: false");
		System.out.println("WHITE NETWORK: " + a.isNetwork(WHITE) + "    Should be: true");

		// Sixth Board Test
		a = new Board();
		System.out.println("Printing Sixth myBoard:");

		a.addBlack(2, 0); 
		a.addBlack(3, 0);
		a.addBlack(6, 0);
		a.addBlack(3, 2);
		a.addBlack(6, 3);
		a.addBlack(5, 4);
		a.addBlack(1, 5);
		a.addBlack(3, 5);
		a.addBlack(1, 6);
		a.addBlack(4, 6);
		a.addWhite(0, 1);
		a.addWhite(5, 1);
		a.addWhite(6, 1);	
		a.addWhite(3, 3);
		a.addWhite(1, 4);
		a.addWhite(3, 4);
		a.addWhite(6, 4);
		a.addWhite(3, 6);
		a.addWhite(6, 6);
		a.addWhite(7, 6);
		System.out.println(a);
		System.out.println("Testing evaluator White:" + a.evaluatorFcn(WHITE));
		System.out.println("Testing evaluator Black:" + a.evaluatorFcn(BLACK));
		System.out.println("BLACK NETWORK: " + a.isNetwork(BLACK) + "    Should be: false");
		System.out.println("WHITE NETWORK: " + a.isNetwork(WHITE) + "    Should be: true");

		// Seventh Board Test
		a = new Board();
		System.out.println("Printing Seventh myBoard:");

		a.addBlack(4, 2); 
		a.addBlack(3, 3);
		a.addBlack(1, 5);
		a.addBlack(3, 6);
		a.addBlack(2, 7);
		a.addWhite(0, 1);
		a.addWhite(6, 1);
		a.addWhite(6, 4);	
		a.addWhite(3, 4);
		a.addWhite(5, 6);
		a.addWhite(7, 6);
		System.out.println(a);
		a.basicSeerFcn(a, 5, 3, BLACK, true);
		System.out.println("COORDLIST (4, 2): " + a.myBoard[5][3].coordList);
		a.basicSeerFcn(a, 4, 4, BLACK, true);
		System.out.println("COORDLIST (3, 3): " + a.myBoard[4][4].coordList);
		a.basicSeerFcn(a, 2, 6, BLACK, true);
		System.out.println("COORDLIST (1, 5): " + a.myBoard[2][6].coordList);
		a.basicSeerFcn(a, 4, 7, BLACK, true);
		System.out.println("COORDLIST (3, 6): " + a.myBoard[4][7].coordList);
		a.basicSeerFcn(a, 3, 8, BLACK, true);
		System.out.println("COORDLIST (2, 7): " + a.myBoard[3][8].coordList);
		a.basicSeerFcn(a, 1, 2, WHITE, true);
		System.out.println("COORDLIST (0, 1): " + a.myBoard[1][2].coordList);
		a.basicSeerFcn(a, 7, 2, WHITE, true);
		System.out.println("COORDLIST (6, 1): " + a.myBoard[7][2].coordList);
		a.basicSeerFcn(a, 7, 5, WHITE, true);
		System.out.println("COORDLIST (6, 4): " + a.myBoard[7][5].coordList);
		a.basicSeerFcn(a, 4, 5, WHITE, true);
		System.out.println("COORDLIST (3, 4): " + a.myBoard[4][5].coordList);
		a.basicSeerFcn(a, 6, 7, WHITE, true);
		System.out.println("COORDLIST (5, 6): " + a.myBoard[6][7].coordList);
		a.basicSeerFcn(a, 8, 7, WHITE, true);
		System.out.println("COORDLIST (7, 6): " + a.myBoard[8][7].coordList);

		System.out.println("Testing evaluator White:" + a.evaluatorFcn(WHITE));
		System.out.println("Testing evaluator Black:" + a.evaluatorFcn(BLACK));
		System.out.println("BLACK NETWORK: " + a.isNetwork(BLACK) + "    Should be: false");
		System.out.println("WHITE NETWORK: " + a.isNetwork(WHITE) + "    Should be: true");

		// FIND WINNING MOVE Board Test
		a = new Board();
		System.out.println("Printing FIND WINNING MOVE:");

		a.addBlack(2, 0); 
		a.addBlack(3, 0);
		a.addBlack(6, 0);
		a.addBlack(3, 2);
		a.addBlack(6, 3);
		a.addBlack(5, 4);
		a.addBlack(1, 5);
		a.addBlack(3, 5);
		a.addBlack(1, 6);
		a.addBlack(4, 6);
		a.addWhite(0, 1);
		a.addWhite(5, 1);
		a.addWhite(6, 1);	
		a.addWhite(3, 3);
		a.addWhite(1, 4);
		a.addWhite(3, 4);
		a.addWhite(6, 4);
		a.addWhite(3, 6);
		a.addWhite(6, 6);
		a.addWhite(7, 6);
		System.out.println(a);
		System.out.println("Testing evaluator White:" + a.evaluatorFcn(WHITE));
		System.out.println("Testing evaluator Black:" + a.evaluatorFcn(BLACK));
		System.out.println("BLACK NETWORK: " + a.isNetwork(BLACK) + "    Should be: false");
		System.out.println("WHITE NETWORK: " + a.isNetwork(WHITE) + "    Should be: true");
	}

	// Test Code. Reader Can Ignore
	public static void main(String[] args) {
		testNetworks();
	}
}


