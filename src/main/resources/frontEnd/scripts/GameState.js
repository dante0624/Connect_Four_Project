import { EvalTree } from "./EvalTree.js";

export class GameState {
	constructor(width, height, gameReadyCallback) {
		this.width = width;
		this.height = height;

		this.moveHistory = [];
		this.gameGrid = [];
		for (let colIndex = 0; colIndex < width; colIndex++) {
			this.gameGrid.push([]);
		}
		this.evalTree = new EvalTree(width, gameReadyCallback);
	}

	moveCount() {
		return this.moveHistory.length;
	}
	playerOneTurn() {
		return this.moveCount() % 2 == 0;
	}
	getHeightFromTop(colIndex) {
		return this.height - this.gameGrid[colIndex].length;
	}
	getTopCellIndex(colIndex) {
		return this.getHeightFromTop(colIndex) * this.width + colIndex;
	}
	getCurrentEval() {
		return this.evalTree.getCurrentEval();
	}
	gameIsWon() {
		const evaluation = this.getCurrentEval();
		const winningMoveNumber = evaluation > 0 ? evaluation * 2 - 1 : evaluation * -2;
		return this.moveCount() == winningMoveNumber
	}
	canPlay(colIndex) {
		/* Note the 3 ways a column cannot be played:
			1. The column is full
			2. The game is already a win or a loss
		*/
		return !(this.gameGrid[colIndex].length >= this.height || this.gameIsWon());
	}

	playCol(colIndex) {
		/* Adds a new move to history if and only if it is playable
			and its evaulation has already been determined
		If both conditions are met, return the cell index where it will fall
			otherwise return undefined
		After playing, update where the evalTree points
		*/
		if (!this.canPlay(colIndex) || this.evalTree.getChildEval(colIndex) === undefined) {
			return undefined;
		}

		this.gameGrid[colIndex].push(this.playerOneTurn());
		this.moveHistory.push(colIndex);
		this.evalTree.moveDown(colIndex);

		return this.getTopCellIndex(colIndex);
	}
	handleChildEval(colIndex, callback) {
		/* Tells the evaluation tree to fetch and set a child eval if it does not already exist
		This should be called iteratively on each column, after playCol() is called
		Should also be called right after the game is initialized
		Has no effect if the desired column is not playable */
		if (!this.canPlay(colIndex)) {
			return;
		}

		const currChildEval = this.evalTree.getChildEval(colIndex);
		if (currChildEval !== undefined) {
			callback(currChildEval);
		}
		else {
			this.evalTree.fetchChildEval(this.moveHistory, colIndex, callback);
		}
	}
	
	back() {
		/* Deletes the last played move from history
		Returns the cell index of where that chip was
		If history is empty, returns undefined */
		if (!this.moveCount()) {
			return undefined;
		}
		const colIndex = this.moveHistory.pop();
		const cellIndex = this.getTopCellIndex(colIndex);
		this.gameGrid[colIndex].pop();
		this.evalTree.moveUp();

		return cellIndex; 
	}
	clear() {
		// Deletes all moves from history
		// Returns an array of all cell indicies that had chips deleted
		const cellIndicies = [];
		for (const [colIndex, gameColumn] of this.gameGrid.entries()) {
			while (gameColumn.length > 0) {
				cellIndicies.push(this.getTopCellIndex(colIndex));
				gameColumn.pop();
			}
		}
		this.moveHistory.splice(0, this.moveCount());
		this.evalTree.moveTop();

		return cellIndicies;
	}
}

