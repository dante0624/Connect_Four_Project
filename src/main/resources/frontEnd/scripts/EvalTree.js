const SERVER_PATH = "/solve/";

export class EvalTree {
	constructor(width, gameReadyCallback) {
		this.width = width;

		fetch(SERVER_PATH)
			.then(response => response.text())
			.then(text => {
				const evaluation = parseInt(text);
				this.root = this.createNode(evaluation);
				this.current = this.root;
				gameReadyCallback();
			});
	}

	createNode(evaluation, parent=null) {
		return {
			eval: evaluation,
			parent: parent,
			children: new Array(this.width).fill(undefined),
		};
	}

	moveDown(column) {
		this.current = this.current.children[column];
	}
	moveUp() {
		this.current = this.current.parent;
	}
	moveTop() {
		this.current = this.root;
	}

	getCurrentEval() {
		return this.current.eval;
	}
	getChildEval(colIndex) {
		// Do not wait for the child eval to be ready
		// Return it if it exists right now, otherwise return undefined
		if (this.current.children[colIndex] === undefined) {
			return undefined;
		}
		return this.current.children[colIndex].eval;
	}
	
	fetchChildEval(moveHistory, colIndex, callback) {
		/* Sets a child node equal to a new node, whose evaulation is fetched from the server
		moveHistory is an int[] of prior moves, and should be provided by the caller
		colIndex specify which child is to be fetched and set
		callback is called with the new evaluation as a parameter after fetch resolves */
		fetch(SERVER_PATH + moveHistory.join('') + colIndex)
			.then(response => response.text())
			.then(text => {
				const evaluation = parseInt(text);
				this.current.children[colIndex] = this.createNode(evaluation, this.current);
				callback(evaluation);
			});
	}
}
