import { EvalTree } from "./EvalTree.js";

const SERVER_PATH = "/alignment/";

// Helper class to track which children evaluations are fully loaded
// Once all are fully loaded, it fires a single, fixed callback on what to do now
class ChildrenState {
	constructor(width, childrenReadyCallback) {
		this.childrenReady = new Array(width).fill(false);
		this.childrenReadyCallback = childrenReadyCallback;
	}
	allChildrenReady() {
		return this.childrenReady.every((x) => x === true);
	}
	setAllPending() {
		this.childrenReady.fill(false);
	}

	// Once the last child is ready, then we issue the general callback for all children
	setChildReady(colIndex) {
		this.childrenReady[colIndex] = true;
		if (this.allChildrenReady()) {
			this.childrenReadyCallback();
		}
	}
}

export class GameState {
	constructor(width, height, initialStateReadyCallback, childrenReadyCallback) {
		this.width = width;
		this.height = height;

		this.moveHistory = [];
		this.gameGrid = [];
		for (let colIndex = 0; colIndex < width; colIndex++) {
			this.gameGrid.push([]);
		}

		this.childrenState = new ChildrenState(width, childrenReadyCallback);
		this.evalTree = new EvalTree(width, () => {
			initialStateReadyCallback();
			this.fetchChildrenEvals();
		});
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
	getChildEval(colIndex) {
		return this.evalTree.getChildEval(colIndex);
	}
	getCurrentEval() {
		return this.evalTree.getCurrentEval();
	}
	fetchChildrenEvals() {
		this.childrenState.setAllPending();
		for (let colIndex = 0; colIndex < this.width; colIndex++) {
			const setReady = () => { this.childrenState.setChildReady(colIndex); }

			if (!this.canLegallyPlay(colIndex) || this.getChildEval(colIndex) !== undefined) {
				// Condition hits if we cannot play the column, if the eval has already been fetched
				// Either way, it is ready because we do not need to fetch it
				setReady();
			}

			else {
				this.evalTree.fetchChildEval(this.moveHistory, colIndex, setReady);
			}
		}
	}
	fetchAlignment(callback) {
		fetch(SERVER_PATH + this.moveHistory.join(''))
			.then(response => response.text())
			.then(text => { callback(text.split(',').map((x) => parseInt(x))); });
	}
	gameIsWon() {
		const evaluation = this.getCurrentEval();
		const winningMoveNumber = evaluation > 0 ? evaluation * 2 - 1 : evaluation * -2;
		return this.moveCount() == winningMoveNumber
	}
	gameIsDrawn() {
		return this.getCurrentEval() == 0 && this.moveCount() == this.width * this.height;
	}
	canLegallyPlay(colIndex) {
		/* Note the 3 ways a column cannot be played:
			1. The column is full
			2. The game is already a win or a loss
		*/
		return !(this.gameGrid[colIndex].length >= this.height || this.gameIsWon());
	}
	readyToPlay(colIndex) {
		// Checks if a column can be legally played, and all children are ready
		// Good idea to call this before playCol()
		return this.canLegallyPlay(colIndex) && this.childrenState.allChildrenReady();
	}

	playCol(colIndex) {
		/* Adds a new move to history and game grid
		Also moves the eval tree down, and begins to load the next children evaluations
		Should call readyToPlay() before calling playCol()
		*/
		this.gameGrid[colIndex].push(this.playerOneTurn());
		this.moveHistory.push(colIndex);
		this.evalTree.moveDown(colIndex);

		this.fetchChildrenEvals();

		return this.getTopCellIndex(colIndex);
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

