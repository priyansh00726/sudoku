module org.sudoku {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.jetbrains.annotations;
    exports org.sudoku;

    opens org.sudoku.controller;
}