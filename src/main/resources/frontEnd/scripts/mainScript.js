import { VisualGame } from "./VisualGame.js";

let dropOptions;

function textFocus(colIndex) {
	dropOptions[colIndex].classList.add("text-focus");
}
function textRemoveFocus(colIndex) {
	dropOptions[colIndex].classList.remove("text-focus");
}

document.addEventListener('DOMContentLoaded', () => {
	dropOptions = document.getElementsByClassName("drop-option");
	const pulseOptions = document.getElementsByClassName("drop-pulse");
	const tmpDrop = document.getElementById("tmp-drop");
	const cells = document.getElementsByClassName("cell");
	const dropButtons = document.getElementsByClassName("drop-button");
	const evalBox = document.getElementById("position-evaluation");

	const width = dropOptions.length;
	const height = cells.length / width;

	const visualGame = new VisualGame(width, height, dropOptions, pulseOptions, tmpDrop, cells, evalBox);
	
	document.getElementById("back-button").addEventListener("click", () => {visualGame.back();}); 
	document.getElementById("clear-button").addEventListener("click", () => {visualGame.clear();}); 
	for (const [colIndex, dropButton] of Array.from(dropButtons).entries()) {
		dropButton.addEventListener("click", () => { visualGame.playCol(colIndex); });
		dropButton.addEventListener("mouseover", () => { textFocus(colIndex); });
		dropButton.addEventListener("mouseout", () => { textRemoveFocus(colIndex); });
	}
});

