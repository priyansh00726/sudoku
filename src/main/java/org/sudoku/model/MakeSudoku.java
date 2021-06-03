package org.sudoku.model;

import javafx.concurrent.Task;

import java.util.HashSet;
import java.util.Set;

public class MakeSudoku {
    private SolveSudoku solveSudoku;
    private static Task<Object[]> task;
    private Set<Integer> indices;
    private Set<String> grids;

    public MakeSudoku() {
        this.solveSudoku = new SolveSudoku();
        indices = new HashSet<>();
        grids = new HashSet<>();
    }

    private StringBuilder createRandomUnSolvedSudoku() {
        Set<Integer> set = new HashSet<>();
        int index = 0;
        int value = (int) Math.floor(9 * (Math.random())) + 1 + '0';
        while (index < 9) {
            while(set.contains(value)) value = (int) Math.floor(9 * (Math.random())) + 1 + '0';
            set.add(value);
            solveSudoku.getTheGrid().setCharAt(index, (char) value);
            index++;
        }
        return solveSudoku.getTheGrid();
    }

    public String makeSudoku(Task<Object[]> task, int level) {
        return makeHardSudoku(task, level, createRandomUnSolvedSudoku());
    }

    private String makeHardSudoku(Task<Object[]> task, int level, StringBuilder s) {
        SolveSudoku sudoku = new SolveSudoku(s);
        SolveSudoku ss = new SolveSudoku(s);
        StringBuilder completeSolOfUpSudoku = ss.solveForNumerousTimes(task, true, 20);

//        StringBuilder completeSolOfUpSudoku = new StringBuilder(s);
//        SolveSudoku sudoku = new SolveSudoku(completeSolOfUpSudoku);
        if (level == 0 && generate(sudoku, completeSolOfUpSudoku, task, 0, 46) == 1) return completeSolOfUpSudoku.toString();
        else if (level == 1 && generate(sudoku, completeSolOfUpSudoku, task, 0, 50) == 1) return completeSolOfUpSudoku.toString();
        else if (level == 2 && generate(sudoku, completeSolOfUpSudoku, task, 0, 55) == 1) return completeSolOfUpSudoku.toString();
        return null;
    }

    private int generate(SolveSudoku ssObject, StringBuilder sudoku, Task<Object[]> task, int removed, int hard) {
        if (removed > hard) {
//            SolveSudoku.printArray(SolveSudoku.generateGrid(sudoku));
            return 1;
        }
        int index = (int) Math.floor(81 * Math.random());
        while (sudoku.charAt(index) == '0' && indices.contains(index)) index = (int) Math.floor(81 * Math.random());
        char c = sudoku.charAt(index);
        sudoku.setCharAt(index, '0');
        indices.add(index);
        if (grids.contains(sudoku.toString())) {
            indices.remove(index);
            return generate(ssObject, sudoku, task, removed, hard);
        }
        ssObject.setTheGrid(sudoku);
        int solution = -1;
        int sol = ssObject.solveForSpecificTimes(task, true, 2);
        if (sol == 1)
            solution = generate(ssObject, sudoku, task, removed + 1, hard);
        if (sol > 1 || solution > 1) {
            sudoku.setCharAt(index, c);
            grids.add(sudoku.toString());
            indices.remove(index);
            ssObject.setTheGrid(sudoku);
            solution = generate(ssObject, sudoku, task, removed, hard);
        }
        return solution;
    }

    public static void main(String[] args) {
        MakeSudoku o = new MakeSudoku();
        System.out.println(o.createRandomUnSolvedSudoku().toString());
    }
}
