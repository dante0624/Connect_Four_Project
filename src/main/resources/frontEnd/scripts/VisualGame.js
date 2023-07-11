import { GameState } from "./GameState.js";

const MAX_ANIMATION_TIME_MS = 650;
const REFRESH_RATE_MILLISEC = 33;
const PLAYER_CHIPS = ["url(redChip.svg)",  "url(yellowChip.svg)"];

// Helper class exists to ensure that animation is always in a valid state
class AnimationState {
	constructor() {
		this.animateID = undefined;
		this.afterAnimation = null;
	}
	isAnimating() {
		return !!this.animateID;
	}
	startAnimation(animationFunction, afterAnimation) {
		this.animateID = setInterval(animationFunction, REFRESH_RATE_MILLISEC);
		this.afterAnimation = afterAnimation;
	}
	endAnimation() {
		clearInterval(this.animateID);
		this.animateID = undefined;
		this.afterAnimation();
		this.afterAnimation = null;
	}
}

export class VisualGame {
	constructor(width, height, dropOptions, tmpDrop, cells) {
		this.dropOptions = dropOptions;
		this.tmpDrop = tmpDrop;
		this.cells = cells;

		this.gameState = new GameState(width, height);
		this.animationState = new AnimationState();
		this.millisecPerCell = MAX_ANIMATION_TIME_MS / height;
	}

	getPlayerChip() {
		return PLAYER_CHIPS[(this.gameState.playerOneTurn() ? 0 : 1)];
	}
	setDropOptionColors() {
		for (const option of this.dropOptions) {
			option.style.backgroundImage = this.getPlayerChip();
		}
	}

	playCol(colIndex) {
		// Get this before we update game state, as that effectively flips colors
		const fallingChipImage = this.getPlayerChip();

		// Update the game state and check move validity
		const cellIndex = this.gameState.playCol(colIndex);
		if (!cellIndex) {
			return;
		}

		// Cancel jump to finishing prior animation if it is still going
		if (this.animationState.isAnimating()) {
			this.animationState.endAnimation();
		}

		// Turn off drop evaluations
		for (const option of this.dropOptions) {
			option.innerHTML = "";
		}

		// Make tmp chip appear over the correct column
		const startX = this.dropOptions[colIndex].offsetLeft;
		const startY = this.dropOptions[colIndex].offsetTop;
		this.tmpDrop.style.left = `${startX}px`;
		this.tmpDrop.style.top = `${startY}px`;
		this.tmpDrop.style.backgroundImage = fallingChipImage;

		// Change drop options colors and load new evaluations
		this.setDropOptionColors();
		this.setEvaluations();

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
		}
		this.animationState.startAnimation(animationFunction, afterAnimation);
	}
	back() {
		const cellIndex = this.gameState.back();
		if (cellIndex) {
			this.cells[cellIndex].style.backgroundImage = null;
			this.setDropOptionColors();
		}
	}
	clear() {
		const cellIndicies = this.gameState.clear();
		for (const cellIndex of cellIndicies) {
			this.cells[cellIndex].style.backgroundImage = null;
		}
		this.setDropOptionColors();
	}

	// TODO: Load new evaluations
	// Move this method to separate file
	setEvaluations() {
		for (const option of this.dropOptions) {
			option.innerHTML = "0";
		}
	}
}

