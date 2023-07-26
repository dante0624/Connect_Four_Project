import { GameState } from "./GameState.js";

const MAX_ANIMATION_TIME_MS = 650;
const REFRESH_RATE_MILLISEC = 33;
const PLAYER_CHIPS = ["url(images/yellowChip.svg)",  "url(images/redChip.svg)"];
const PLAYER_PULSES = ["pulse-yellow", "pulse-red"];
const PLAYER_COLORS = ["Yellow", "Red"];
const GAME_IS_DRAW = "The Game is a Draw";
const GAME_WILL_DRAW = "Both Players Can Force a Draw";
function evaluationMessage(playerIndex, movesUntilWin) {
	return movesUntilWin == 1 ?
		`${PLAYER_COLORS[playerIndex]} Can Win in ${movesUntilWin} Move` :
		`${PLAYER_COLORS[playerIndex]} Can Force a Win in ${movesUntilWin} Moves`;
}
function victoryMessage(playerIndex) {
	return `${PLAYER_COLORS[playerIndex]} Has Won`;
}

// Helper class exists to ensure that animation is always in a valid state
// Either both attributes exist, or neither exist
class AnimationState {
	constructor() {
		this.animateID = undefined;
		this.afterAnimation = null;
	}
	startAnimation(animationFunction, afterAnimation) {
		this.animateID = setInterval(animationFunction, REFRESH_RATE_MILLISEC);
		this.afterAnimation = afterAnimation;
	}
	// Ends an animation and jumps to the afterAnimation if it exists
	// Can safely call this, as it does nothing if no animation is active
	endAnimation() {
		// Note that calls to setInterval always return nonzero "truthy" values
		if (this.animateID) {
			clearInterval(this.animateID);
		}
		this.animateID = undefined;

		if (this.afterAnimation) {
			this.afterAnimation();
		}
		this.afterAnimation = null;
	}
}

export class VisualGame {
	constructor(width, height, dropOptions, pulseOptions, tmpDrop, cells, evalBox) {
		this.dropOptions = dropOptions;
		this.pulseOptions = pulseOptions;
		this.tmpDrop = tmpDrop;
		this.cells = cells;
		this.evaluationBox = evalBox;

		this.gameState = new GameState(
			width,
			height,
			() => { this.displayCurrentStateInfo(); },
			() => { this.displayChildrenInfo(); }
		);
			
		this.animationState = new AnimationState();
		this.millisecPerCell = MAX_ANIMATION_TIME_MS / height;

		this.alignmentCellIndicies = [];
	}

	getPlayerIndex() {
		return this.gameState.playerOneTurn() ? 0 : 1;
	}
	getPlayerChip() {
		return PLAYER_CHIPS[this.getPlayerIndex()];
	}
	getPlayerPulse() {
		return PLAYER_PULSES[this.getPlayerIndex()];
	}
	setChildrenVisualsOff() {
		for (const dropOption of this.dropOptions) {
			dropOption.innerHTML = "";
		}
		for (const pulseOption of this.pulseOptions) {
			pulseOption.classList.remove(PLAYER_PULSES[0]);
			pulseOption.classList.remove(PLAYER_PULSES[1]);
		}

	}

	playCol(colIndex) {
		if (!this.gameState.readyToPlay(colIndex)) {
			return;
		}

		// Cancel jump to finishing prior animation if it is still going
		this.animationState.endAnimation();

		// Turn off drop evaluations and pulses
		this.setChildrenVisualsOff();

		// Get this before we update game state, as that effectively flips colors
		const fallingChipImage = this.getPlayerChip();

		// Make tmp chip appear over the correct column
		const startX = this.dropOptions[colIndex].offsetLeft;
		const startY = this.dropOptions[colIndex].offsetTop;
		this.tmpDrop.style.left = `${startX}px`;
		this.tmpDrop.style.top = `${startY}px`;
		this.tmpDrop.style.backgroundImage = fallingChipImage;

		// Update the game state and issue callback for the children evals
		const cellIndex = this.gameState.playCol(colIndex);

		// Flip the colors and display the current state eval
		this.displayCurrentStateInfo();

		// Calculate the fall
		const animationDuration = (this.gameState.getHeightFromTop(colIndex) +  1) * this.millisecPerCell;
		const endSquare = this.cells[cellIndex];
		const endY = endSquare.offsetTop;
		const deltaY = (endY - startY) * REFRESH_RATE_MILLISEC / animationDuration;
		let animateY = this.tmpDrop.offsetTop;

		// Need to be arrow functions so that 'this' is still a VisualHandler instance
		// Traditional function turns 'this' to undefined
		const animationFunction = () => {
			if (animateY >= endY) {
				this.animationState.endAnimation();
			}
			else {
				animateY += deltaY;
				this.tmpDrop.style.top = `${animateY}px`;
			}
		}
		const afterAnimation = () => {
			this.tmpDrop.style.backgroundImage = null;
			endSquare.style.backgroundImage = fallingChipImage;
			this.safeDisplayAlignments();
		}
		this.animationState.startAnimation(animationFunction, afterAnimation);
	}
	back() {
		const cellIndex = this.gameState.back();
		this.animationState.endAnimation();
		this.clearAlignments();
		if (cellIndex !== undefined) {
			this.cells[cellIndex].style.backgroundImage = null;
		}
		this.displayExternalInfo();
	}
	clear() {
		const cellIndicies = this.gameState.clear();
		this.animationState.endAnimation();
		this.clearAlignments();
		for (const cellIndex of cellIndicies) {
			this.cells[cellIndex].style.backgroundImage = null;
		}
		this.displayExternalInfo();
	}

	getRelativeEval(totalMoves, absoluteEval) {
		// Converts from "Yellow / Red Move Number" to "Mate In" notation
		if (absoluteEval > 0) {
			return absoluteEval - Math.ceil(totalMoves / 2);
		}
		if (absoluteEval < 0) {
			return absoluteEval + Math.floor(totalMoves / 2);
		}
		return 0;
	}
	getBoxMessage(totalMoves, absoluteEval) {
		if (absoluteEval == 0) {
			return this.gameState.gameIsDrawn() ? GAME_IS_DRAW : GAME_WILL_DRAW;
		}

		const winningPlayerIndex = absoluteEval > 0 ? 0 : 1;
		const movesUntilWin = Math.abs(this.getRelativeEval(totalMoves, absoluteEval));
		return this.gameState.gameIsWon() ? victoryMessage(winningPlayerIndex) : 
			evaluationMessage(winningPlayerIndex, movesUntilWin);
	}
	displayCurrentStateInfo() {
		const totalMoves = this.gameState.moveCount();
		const evaluation = this.gameState.getCurrentEval();
		this.evaluationBox.innerHTML = this.getBoxMessage(totalMoves, evaluation);
		for (const option of this.dropOptions) {
			option.style.backgroundImage = this.getPlayerChip();
		}
	}
	displayChildrenInfo() {
		// Kills the prior visuals if they exist
		this.setChildrenVisualsOff();

		const totalMoves = this.gameState.moveCount();
		const positionEval = this.gameState.getCurrentEval();
		let hidePulses = true;

		/* Display the pulse if Child's Absolute Eval == Current Position's Absolute Eval
		But, we never want to pulse every child (if all equal, they all likely lose equally)
		First loop through should verify that not all children evals == current position eval
		We also use the first loop to set the drop option evaluations */
		for (const [colIndex, dropOption] of Array.from(this.dropOptions).entries()) {
			const childEval = this.gameState.getChildEval(colIndex);
			if (childEval === undefined) {
				continue;
			}
			if (childEval != positionEval) {
				hidePulses = false;
			}
			dropOption.innerHTML = this.getRelativeEval(totalMoves, childEval);
		}

		if (hidePulses) {
			return;
		}

		const playerPulse = this.getPlayerPulse();
		for (const [colIndex, pulseOption] of Array.from(this.pulseOptions).entries()) {
			if (this.gameState.getChildEval(colIndex) == positionEval) {
				pulseOption.classList.add(playerPulse);
			}
		}
	}
	displayExternalInfo() {
		this.displayCurrentStateInfo();
		this.displayChildrenInfo();
	}
	safeDisplayAlignments() {
		/* First check if that the game is won before doing anything
		Then issue a callback to display X's over all cells, after we fetch those cell's indicies
		Built into the callback is a check that that game is still won
		This is needed, as a race condition could happen where we issue the callback while the game is won,
			But then the back button is pressed so the game is no longer won. But then the callback hits, 
			which could result in display X's over a non-winning position. */
		if (!this.gameState.gameIsWon()) {
			return;
		}

		this.gameState.fetchAlignment((cellIndicies) => {
			if (!this.gameState.gameIsWon()) {
				return;
			}

			this.alignmentCellIndicies = [];
			for (const cellIndex of cellIndicies) {
				this.cells[cellIndex].innerHTML = "X";
				this.alignmentCellIndicies.push(cellIndex);
			}
		});	
	}
	clearAlignments() {
		for (const cellIndex of this.alignmentCellIndicies) {
			this.cells[cellIndex].innerHTML = "";
		}
		this.alignmentCellIndicies = [];
	}
}

