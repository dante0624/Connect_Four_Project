const RED = 1;
const YELLOW = -1;
const STARTING_PLAYER = RED;
const WIDTH = 7;
const HEIGHT = 6;
const MAX_ANIMATION_TIME_MS = 650;
const MILLISEC_PER_CELL = MAX_ANIMATION_TIME_MS / HEIGHT;
const REFRESH_RATE_MILLISEC = 33;

const moveHistory = [];
const gameGrid = []
for (let i = 0; i < WIDTH; i++) {
	gameGrid.push([]);
}
let currPlayer;

function changePlayer() {
	currPlayer *= -1;
}
function getChip(color) {
	if (color == RED) {
		return "url(redChip.svg)";
	}
	return "url(yellowChip.svg)";
}
function getTopCellIndex(col) {
	const rowFromTop = HEIGHT - gameGrid[col].length;
	return rowFromTop * WIDTH + col;
}


let dropOptions;
let tmpDrop;
let cells;
let animateID;

function setDropOptionColors() {
	for (const option of dropOptions) {
		option["style"]["backgroundImage"] = getChip(currPlayer);
	}
}

function playCol(col) {
	if (animateID) {
		clearInterval(animateID);

		// We have cancelled the prior animation, jump to doing its afterAnimation
		const priorCol = moveHistory[moveHistory.length - 1];
		const cellIndex = getTopCellIndex(priorCol);
		afterAnimation(cellIndex, currPlayer * -1);
	}

	moveHistory.push(col);
	gameGrid[col].push(currPlayer);
	changePlayer()

	prepareAnimate(col, currPlayer * -1);
}

// Color is the color of the temporary chip, and the chip at the bottom
function prepareAnimate(col, color) {
	// Turn off drop evaluations
	for (const option of dropOptions) {
		option.innerHTML = "";
	}

	// Make tmp chip appear over the correct column
	const startX = dropOptions[col]["offsetLeft"];
	const startY = dropOptions[col]["offsetTop"];
	tmpDrop.style.left = startX + 'px';
	tmpDrop.style.top = startY + 'px';
	tmpDrop.style["backgroundImage"] = getChip(color);

	// Change drop options colors and load next evaluations
	nextDropOptions();

	// Calculate the fall
	const animationDuration = (HEIGHT - gameGrid[col].length + 1) * MILLISEC_PER_CELL;
	const cellIndex = getTopCellIndex(col);
	const endSquare = cells[cellIndex];
	const endY = endSquare.offsetTop;
	const deltaY = (endY - startY) * REFRESH_RATE_MILLISEC / animationDuration;
	let animateY = tmpDrop.offsetTop;

	clearInterval(animateID);
  	animateID = setInterval(dropAnimate, REFRESH_RATE_MILLISEC);

	function dropAnimate() {
		if (animateY >= endY) {
			clearInterval(animateID);
			animateID = null;
			afterAnimation(cellIndex, color);
		}
		else {
			animateY += deltaY;
			tmpDrop.style.top = animateY + 'px';
		}
	}
}

function nextDropOptions() {
	setDropOptionColors();

	// TODO: Load new evaluations
	for (const option of dropOptions) {
		option.innerHTML = "0";
	}
}

function afterAnimation(cellIndex, color) {
	tmpDrop.style.backgroundImage = null;

	// Make real chip appear there
	const endSquare = cells[cellIndex];
	endSquare.style.backgroundImage = getChip(color);
}

function back() {
	if (moveHistory.length == 0) {
		return
	}

	const priorCol = moveHistory.pop();
	const cellIndex = getTopCellIndex(priorCol);
	const clearSquare = cells[cellIndex];
	clearSquare.style.backgroundImage = null;
	gameGrid[priorCol].pop();

	changePlayer();
	setDropOptionColors();
}

function clear() {
	moveHistory.splice(0, moveHistory.length);

	for (const [colIndex, gameColumn] of gameGrid.entries()) {
		while (gameColumn.length > 0) {
			const cellIndex = getTopCellIndex(colIndex);
			const clearSquare = cells[cellIndex];
			clearSquare.style.backgroundImage = null;
			gameColumn.pop();
		}	
	}

	currPlayer = STARTING_PLAYER;
	setDropOptionColors();
}

document.addEventListener('DOMContentLoaded', () => {
	dropOptions = document.getElementsByClassName("drop-option")
 	tmpDrop = document.getElementById("tmp-drop");
	cells = document.getElementsByClassName("cell");
	animateID = null;

	const dropButtons = document.getElementsByClassName("drop-button");
	document.getElementById("back-button").onclick = back;
	document.getElementById("clear-button").onclick = clear;
	for (let i = 0; i < WIDTH; i++) {
		dropButtons[i].setAttribute("onclick", "playCol(" + i + ");")
	}

	for (const dropOption of dropOptions) {
		dropOption.style.backgroundImage = getChip(STARTING_PLAYER);
	}
	currPlayer = STARTING_PLAYER;


});

