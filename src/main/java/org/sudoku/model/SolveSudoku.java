package org.sudoku.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Scanner;

public class SolveSudoku {

    private StringBuilder theGrid;
    private final ObservableList<String> solutions;

    private final StringBuilder veryEasy;
    private final StringBuilder easySudoku;
    private final StringBuilder normalSudoku;
    public final StringBuilder hardSudoku;
    private int numSolRequired;

    public SolveSudoku() {
        this(new StringBuilder(
                "000000000000000000000000000000000000000000000000000000000000000000000000000000000"));
    }

    public SolveSudoku(StringBuilder grid) {
        this.theGrid = new StringBuilder(grid.toString());

        solutions = FXCollections.observableArrayList();

        veryEasy = new StringBuilder(
                "000260701680070090100004500820100040004602000050003028009300004040050000003018000");
        easySudoku = new StringBuilder(
                "000260701680070090190004500820100040004602900050003028009300074040050036703018000");
        normalSudoku = new StringBuilder(
                "100489006730000040000001295007120600500703008006095700914600000020000037800512004");
        hardSudoku = new StringBuilder(
                "005300000800000020070010500400005300010070006003200080060500009004000030000009700");
    }

    public StringBuilder getTheGrid() {
        return this.theGrid;
    }

    public void setTheGrid(StringBuilder theGrid) {
        this.theGrid = theGrid;
    }

    public void printHardSudokuDemo() {
        printArray(generateGrid(this.hardSudoku));
    }

    public static void printArray(Integer @NotNull [][] array) {
        System.out.println("***************************");
        for (Integer[] ints : array) {
            for (int j = 0; j < array[0].length; j++) {
                System.out.print(ints[j] + " ");
            }
            System.out.println();
        }
        System.out.println("***************************");
    }

    public static @NotNull Integer[][] generateGrid(StringBuilder grid) {
        Integer[][] result = new Integer[9][9];
        for (int i = 0; i < 9; i++)
            for (int j = 0; j < 9; j++)
                result[i][j] = grid.charAt(9 * i + j) - '0';
        return result;
    }

    public ObservableList<String> getSolutions() {
        return solutions;
    }

    public Object[] getSolutionsAsArray(Task<Object[]> task) {
        mainSolverMethod(0, task, false);
        int num = solutions.size();
        return new Object[]{solutions.get(num - 1), num};
    }

    public int solveForSpecificTimes(Task<Object[]> task, boolean b, int value) {
        numSolRequired = value;
        int a = mainSolverMethod(0, task, b);
        if (b) solutions.clear();
        return a;
    }

    public StringBuilder solveForNumerousTimes(Task<Object[]> task, boolean b, int value) {
        numSolRequired = value;
        int a = mainSolverMethod(0, task, b);
        return new StringBuilder(solutions.get((int) Math.floor(a * Math.random())));
    }

    private static int getIndex(int r, int c) {
        return 9 * r + c;
    }

    public boolean validation(int index, int value) {
        int row = index / 9;
        int column = index % 9;
        value = value - '0';

        return rowCheck(row, column, value, this.theGrid.toString()) &&
                columnCheck(row, column, value, this.theGrid.toString())
                && boxCheck(row, column, value, this.theGrid.toString());
    }

    private boolean rowCheck(int row, int column, int value, String grid) {
        assert value != 0;
        int c = getIndex(row, 0);
        for (int i = c; i < c + 9; i++) {
            if (grid.charAt(i) == '0') continue;
            if (i != (c + column) && (grid.charAt(i) - '0') == value) return false;
        }
        return true;
    }

    private boolean columnCheck(int row, int column, int value, String grid) {
        assert value != 0;
        int c = getIndex(0, column);
        for (int i = 0; i < 9; i++) {
            char c1 = grid.charAt(c + 9 * i);
            if (c1 == '0') continue;
            if (row != i && (c1 - '0') == value) return false;
        }
        return true;
    }

    private boolean boxCheck(int row, int column, int value, String grid) {
        assert value != 0;
        for (int i = 3 * (row / 3); i < 3 * (row / 3) + 3; i++)
            for (int j = 3 * (column / 3); j < 3 * (column / 3) + 3; j++) {
                char c1 = grid.charAt(9 * i + j);
                if (c1 == '0') continue;
                if ((i != row || j != column) && (c1 - '0') == value)
                    return false;
            }
        return true;
    }

    private int mainSolverMethod(int index, Task<Object[]> task, boolean unique) {
        if (task != null && task.isCancelled()) {
            solutions.clear();
            return -1;
        }
        assert theGrid.length() == 81;
        if (index == theGrid.length()) {
            if (unique && solutions.size() > numSolRequired) {
                return 0;
            }
            solutions.add(theGrid.toString());
//            System.out.println("Solution found.");
//            printArray(generateGrid(this.theGrid));
            return 1;
        }
        if (theGrid.charAt(index) != '0') return mainSolverMethod(index + 1, task, unique);
        int result = 0;
        for (char v = '1'; v <= '9'; v++)
            if (validation(index, v)) {
                theGrid.setCharAt(index, v);
                result += mainSolverMethod(index + 1, task, unique);
                if (result > numSolRequired && unique) break;
            }
        theGrid.setCharAt(index, '0');
        return result;
    }

    public static void main(String[] args) throws IOException {
        StringBuilder a = new StringBuilder();
        Scanner sc = new Scanner(System.in);
        a = new StringBuilder(sc.next());
        SolveSudoku sudoku = new SolveSudoku(a);
//        sudoku.theGrid = sudoku.hardSudoku;
//        printArray(sudoku.generateGrid(sudoku.theGrid));
        System.out.println(sudoku.mainSolverMethod(0, null, false));
//        int i = 0;
//        while (i < 9) {
//            a = new StringBuilder("000000000000000000000000000000000000000000000000000000000000000000000000000000000");
//            a.setCharAt((int) Math.floor(81 * Math.random()), (char) ((char) (i) + '0'));
//            sudoku.setTheGrid(a);
//            System.out.println(sudoku.solveForSpecificTimes(new Task<Object[]>() {
//                @Override
//                protected Object[] call() throws Exception {
//                    return new Object[0];
//                }
//            }, true, 20));
//            i++;
//        }

    }
}
