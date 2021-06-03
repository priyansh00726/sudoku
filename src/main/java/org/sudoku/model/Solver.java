package org.sudoku.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Node;
import javafx.scene.control.Label;

public class Solver {
    private final SolveSudoku solveSudoku;

    //GetSolution : Either "" or "number" read from ui, set grid value
    //New Game : Either "0" or "number" read from string, set ui label value
    private final SimpleStringProperty[] sudokuCellsArray;

    public SimpleStringProperty[] getSudokuCellsArray() {
        return sudokuCellsArray;
    }

    public Solver() {
        solveSudoku = new SolveSudoku();
        sudokuCellsArray = new SimpleStringProperty[81];
        for (int i = 0; i < sudokuCellsArray.length; i++)
            sudokuCellsArray[i] = new SimpleStringProperty("");
    }

    //Dark : -fx-background-color: #4e4f52;-fx-text-fill: white;
    //Error : -fx-background-color: #873b36

    public void modifyInputAsNode(Label node, int index, String style, boolean b) {
        if (b && !sudokuCellsArray[index].get().isEmpty() && !sudokuCellsArray[index].get().equals("0")) {
            char value = sudokuCellsArray[index].get().charAt(0);
            solveSudoku.getTheGrid().setCharAt(index, value);
            if (!solveSudoku.validation(index, value))
                node.setStyle("-fx-background-color: #873b36");
            else node.setStyle("-fx-background-color: #4e4f52;-fx-text-fill: white;");
        }
        else if (!b && !sudokuCellsArray[index].get().isEmpty() && !sudokuCellsArray[index].get().equals("0")) {
            char value = sudokuCellsArray[index].get().charAt(0);
            solveSudoku.getTheGrid().setCharAt(index, value);
            if (!solveSudoku.validation(index, value))
                node.setStyle("-fx-background-color: #873b36");
            else node.setStyle(style);
        }
        else {
            solveSudoku.getTheGrid().setCharAt(index, '0');
            node.setStyle(style);
        }
    }

    public void modifyNodeAsInput(Label node, int index, String style) {
        if (!sudokuCellsArray[index].get().isEmpty() && !sudokuCellsArray[index].get().equals("0")) {
            char value = sudokuCellsArray[index].get().charAt(0);
            node.setText(String.valueOf(value));
            solveSudoku.getTheGrid().setCharAt(index, value);
            if (!solveSudoku.validation(index, value))
                node.setStyle("-fx-background-color: #873b36");
            else node.setStyle("-fx-background-color: #4e4f52;-fx-text-fill: white;");
        }
        else {
            solveSudoku.getTheGrid().setCharAt(index, '0');
            node.setStyle(style);
        }
    }

    public void syncWithSolvedSudoku(String string) {
        int index = 0;
        for (SimpleStringProperty cell : sudokuCellsArray) {
            cell.set(String.valueOf(string.charAt(index++)));
        }
    }

    public Object[] getSolutionFromSolver(Task<Object[]> task) {
        return solveSudoku.getSolutionsAsArray(task);
    }

    public void formatGridForNewGame(ObservableList<Node> cells) {
        for (int i = 0; i < cells.size() - 1; i++) {
            if (!sudokuCellsArray[i].get().equals(String.valueOf('0'))) {
                modifyNodeAsInput((Label) cells.get(i), i, "");
            }
        }
    }
}
