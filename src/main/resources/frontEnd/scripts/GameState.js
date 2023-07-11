export class GameState {
	constructor(width, height) {
		this.width = width;
		this.height = height;

		this.moveHistory = [];
		this.gameGrid = [];
		for (let colIndex = 0; colIndex < width; colIndex++) {
			this.gameGrid.push([]);
		}
	}

	playerOneTurn() {
		return this.moveHistory.length % 2 == 0;
	}
	getHeightFromTop(colIndex) {
		return this.height - this.gameGrid[colIndex].length;
	}
	getTopCellIndex(colIndex) {
		return this.getHeightFromTop(colIndex) * this.width + colIndex;
	}

	playCol(colIndex) {
		// Adds a new move to history if and only if it is playable
		// If playable, return the cell index where it will fall
		// Otherwise return undefined
		if (this.gameGrid[colIndex].length >= this.height) {
			return undefined;
		}
		this.gameGrid[colIndex].push(this.playerOneTurn());
		this.moveHistory.push(colIndex);
		return this.getTopCellIndex(colIndex);
	}
	back() {
		// Deletes the last played move from history
		// Returns the cell index of where that chip was
		// If history is empty, returns undefined
		if (!this.moveHistory.length) {
			return undefined;
		}
		const colIndex = this.moveHistory.pop();
		const cellIndex = this.getTopCellIndex(colIndex);
		this.gameGrid[colIndex].pop();
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
		this.moveHistory.splice(0, this.moveHistory.length);
		return cellIndicies;
	}
}

