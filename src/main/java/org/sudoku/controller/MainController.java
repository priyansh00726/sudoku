package org.sudoku.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.Duration;
import org.sudoku.model.MakeSudoku;
import org.sudoku.model.SolveSudoku;
import org.sudoku.model.Solver;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public class MainController {

    @FXML private GridPane mainGridPane;
    @FXML private Button newGameButton;
//    @FXML private Button resumeGameButton;
    @FXML private Button getSolutionButton;
    @FXML private ProgressBar progressBar;
    @FXML private GridPane inputKeyBoard;
    @FXML private Label submitButton;
    @FXML private Label resetButton;
    @FXML private Label timer;
    @FXML private Label numSolutions;
    @FXML private Button backToMenuButton;
    @FXML private Button deleteCellButton;
    @FXML private Label score;

    private Solver solverObject;
    private final MakeSudoku makeSudokuObject;
    private ObservableList<Node> mainGridList;
    private final Stack<Node> selectionCellStack;
    private Service<Object[]> service;
    private Service<String> countDownService;
    private String highScore;

    private FileReader fileReader;
    private FileWriter fileWriter;
    private BufferedReader bufferedReader;
    private BufferedWriter bufferedWriter;

    public MainController() {
        highScore = "";
        selectionCellStack = new Stack<>();
        makeSudokuObject = new MakeSudoku();
        File file = Paths.get("files/highestScore.txt").toFile();
        try {
            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void init() throws IOException {
        highScore = bufferedReader.readLine();
    }

    public void stop() throws IOException {
        File file = Paths.get("files/highestScore.txt").toFile();
        fileWriter = new FileWriter(file);
        bufferedWriter = new BufferedWriter(fileWriter);
        if (highScore != null) bufferedWriter.write(highScore);
        bufferedWriter.newLine();
        bufferedWriter.close();
        bufferedReader.close();
        fileReader.close();
        fileWriter.close();
    }

    private void submitAndResetDisable(boolean b) {
        submitButton.setVisible(!b);
        submitButton.setDisable(b);
        resetButton.setVisible(!b);
        resetButton.setDisable(b);
    }

    private void keyBoardAndDeleteDisable(boolean b) {
        inputKeyBoard.setDisable(b);
        inputKeyBoard.setVisible(!b);
        deleteCellButton.setDisable(b);
        deleteCellButton.setVisible(!b);
    }

    public void setDefaults() {
        mainGridList = mainGridPane.getChildren();
        submitButton.setText("Submit");
        newGameButton.setDisable(false);
//        resumeGameButton.setDisable(false);
        getSolutionButton.setDisable(false);
        progressBar.setVisible(false);
        submitAndResetDisable(true);
        keyBoardAndDeleteDisable(true);
        mainGridPane.setMouseTransparent(true);
        solverObject = new Solver();
        backToMenuButton.setDisable(true);
        backToMenuButton.setOnMouseClicked(e -> backToMenuClicked());
    }

    private void backToMenuClicked() {
        Worker.State s = (service == null) ? null : service.getState();
        if (s == Worker.State.SCHEDULED || s == Worker.State.RUNNING) {
            service.cancel();
            System.out.println("Service is cancelled. Now, Relax");
        }
        s = (countDownService == null) ? null : countDownService.getState();
        if (s == Worker.State.SCHEDULED || s == Worker.State.RUNNING) {
            countDownService.cancel();
            System.out.println("CountDown Service is cancelled. Now, Relax");
        }
        timer.setText("");
        resetGridGetSolution();
        resetGridNewGame(true);
        selectionCellStack.clear();
        progressBar.visibleProperty().unbind();
        progressBar.setVisible(false);
        progressBar.setDisable(true);
        newGameButton.setDisable(false);
//        resumeGameButton.setDisable(false);
        getSolutionButton.setDisable(false);
        submitAndResetDisable(true);
        keyBoardAndDeleteDisable(true);
        mainGridPane.setMouseTransparent(true);
        for (int index = 0; index < mainGridList.size() - 1; index++)
            ((Label) mainGridList.get(index)).textProperty().unbindBidirectional(
                    solverObject.getSudokuCellsArray()[index]);
        solverObject = new Solver();
        backToMenuButton.setDisable(true);
    }

    private void formatGrid(boolean b) {
        int i = 0;
        for (Node node : mainGridList) {
            int finalI = i;
            if (i < mainGridList.size() - 1)
                node.setOnMouseClicked(e -> clickedCellHandler((Label) node, finalI, b));
//            node.requestFocus();
//            node.setOnKeyTyped(e -> {
//                System.out.println("Heydays");
//                if (e.getCode() == KeyCode.BACK_SPACE) {
//                    ((Label) node).setText("");
//                    mouseExits((Label) node, finalI);
//                }
//            });
            i++;
        }
    }

    private void clickedCellHandler(Label node, int index, boolean b) {
        if (!selectionCellStack.isEmpty() && ((Label) selectionCellStack.peek()).getText().isEmpty())
            selectionCellStack.pop().setStyle("");
        selectionCellStack.add(node);
        node.setStyle("-fx-background-color: white");
        keyBoardAndDeleteDisable(false);
        int i = 0;
        ObservableList<Node> list = inputKeyBoard.getChildren();
        for (Node inputCell : list) {
            if (i == list.size() - 1) break;
            i++;
            inputCell.setOnMouseClicked(e -> {
                node.setText(((Label) inputCell).getText());
                mouseExits(node, index, b);
            });
        }
        deleteCellButton.setOnAction(e -> {
            node.setText("");
            node.setStyle("");
            mouseExits(node, index, b);
        });
    }

    private void mouseExits(Label node, int index, boolean b) {
        keyBoardAndDeleteDisable(true);
        solverObject.modifyInputAsNode(node, index, "", b);
    }

    private static int[] formatTime(LocalTime start, LocalTime end) {
        int a = 0, b = 0, c = 0;
        long startSec = 3600 * start.getHour() + 60 * start.getMinute() + start.getSecond();
        long endSec = 3600 * end.getHour() + 60 * end.getMinute() + end.getSecond();
        long time = endSec - startSec;
        a = (int) (time / 3600);
        b = (int) ((time - a * 3600) / 60);
        c = (int) ((time - a * 3600 - b * 60));
        return new int[]{a, b, c};
    }

    private Task<String> countDown(LocalTime startTime) {
        Task<String> task = new Task<>() {
            @Override
            protected String call() throws Exception {
                LocalTime time;
                String hour = "";
                String minute = "";
                String seconds = "";
                int[] result;
                String s = null;
                while (!this.isCancelled()) {
                    time = LocalTime.now();
                    result = formatTime(startTime, time);
                    int a = result[0];
                    int b = result[1];
                    int c = result[2];
                    hour = a < 10 ? "0" + a : "" + a;
                    minute = b < 10 ? "0" + b : "" + b;
                    seconds = c < 10 ? "0" + c : "" + c;
                    s = (hour + ":" + minute + ":" + seconds);
                    String finalS = s;
                    Platform.runLater(() -> timer.setText(finalS));
                    Thread.sleep(1000);
                }
                return s;
            };
        };
        return task;
    }

    private int determineHardness() {
        Dialog<ButtonType> dialog = new Dialog<>();
        ButtonType easy = new ButtonType("EASY");
        ButtonType medium = new ButtonType("MEDIUM");
        ButtonType hard = new ButtonType("HARD");
        dialog.setTitle("SELECT HARDNESS");
        dialog.getDialogPane().getButtonTypes().addAll(easy, medium, hard);
        Optional<ButtonType> result = dialog.showAndWait();
        if (result.get().getText().equals("EASY")) return 0;
        if (result.get().getText().equals("MEDIUM")) return 1;
        if (result.get().getText().equals("HARD")) return 2;
        return -1;
    }

    @FXML public void newGameAction(Event e) {
        System.out.println("Hello New Game");
        int level = determineHardness();
        System.out.println(level);

        backToMenuButton.setDisable(false);
//        resumeGameButton.setDisable(true);
        getSolutionButton.setDisable(true);
        submitAndResetDisable(false);
        service = new Service<Object[]>() {
            @Override
            protected Task<Object[]> createTask() {
                return new Task<>() {
                    @Override
                    protected Object[] call() throws Exception {
                        char[] s = makeSudokuObject.makeSudoku(this, level).toCharArray();
                        int j = 0;
                        assert s.length == 81;
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < s.length; i++) {
                            if (s[i] != '0')
                                solverObject.getSudokuCellsArray()[i].set(String.valueOf(s[i]));
                            sb.append(s[i]);
                        }
                        SolveSudoku.printArray(SolveSudoku.generateGrid(new StringBuilder(sb)));
                        return null;
                    }
                };
            }
        };
        service.start();
        countDownService = new Service<>() {
            @Override
            protected Task<String> createTask() {
                return countDown(LocalTime.now());
            }
        };
        progressBar.setDisable(false);
        progressBar.visibleProperty().bind(service.runningProperty());
        progressBar.progressProperty().bind(service.progressProperty());
        service.setOnSucceeded(event -> {
            System.out.println("Sudoku Generated. Now time to fix the UI");
            newGameButton.setDisable(true);
            if (!selectionCellStack.isEmpty() && ((Label) selectionCellStack.peek()).getText().isEmpty())
                selectionCellStack.pop().setStyle("");
            solverObject.formatGridForNewGame(mainGridList);
            mainGridPane.setMouseTransparent(false);
            formatGrid(false);
            AtomicInteger j = new AtomicInteger();
            mainGridList.forEach(node -> {
                if (j.get() < mainGridList.size() - 1 && !((Label) node).getText().isEmpty())
                    node.setMouseTransparent(true);
                else node.setMouseTransparent(false);
                j.getAndIncrement();
            });
            int index = 0;
            for (; index < mainGridList.size() - 1; index++)
                ((Label) mainGridList.get(index)).textProperty().bindBidirectional(
                        solverObject.getSudokuCellsArray()[index]);
            countDownService.start();
            submitButton.setOnMouseClicked(eventClicked -> {
                submitNewGame();
                Timeline timeline = new Timeline();
                timeline.getKeyFrames().addAll(
                        new KeyFrame(Duration.ZERO),
                        new KeyFrame(new Duration(2000), new KeyValue(numSolutions.textProperty(), "")));
                timeline.play();
            });
            resetButton.setOnMouseClicked(eventClicked -> resetGridNewGame(false));
        });
        service.setOnCancelled(event -> System.out.println("New Game not found. Task Cancelled"));
    }

    private static int seconds(String score) {
        // hh:mm:ss
        if (score == null) return Integer.MAX_VALUE;
        int hour = Integer.parseInt(score.substring(0, 2));
        int minute = Integer.parseInt(score.substring(3, 5));
        int seconds = Integer.parseInt(score.substring(6, 8));
        return 3600 * hour + 60 * minute + seconds;
    }

    private void submitNewGame() {
        if (!selectionCellStack.isEmpty() && ((Label) selectionCellStack.peek()).getText().isEmpty())
            selectionCellStack.pop().setStyle("");
        keyBoardAndDeleteDisable(true);
        int j = 0;
        for (Node node : mainGridList) {
            if (node.getStyle().contains("-fx-background-color: #873b36") ||
                    (j < mainGridList.size() - 1 && ((Label) node).getText().isEmpty())) {
                numSolutions.setText("Improper Entries.");
                return;
            }
            j++;
        }
        String score = timer.getText();
        countDownService.cancel();
        mainGridList.forEach(e -> e.setMouseTransparent(true));
        String s = "Score";
        if (seconds(score) < seconds(highScore)) {
            s = "Highest Score";
            highScore = score;
        }
        s = s + "\n" + score;

        FXMLLoader loader = new FXMLLoader();
        Pane rootDialog = null;
        try {
            loader.setLocation(Objects.requireNonNull(getClass().getResource("/view/viewDialog.fxml")));
            loader.setController(this);
            rootDialog = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.score.setText(s);
        Dialog<ButtonType> congratulations = new Dialog<>();
        congratulations.setTitle("CONGRATULATIONS");
        congratulations.getDialogPane().getChildren().add(rootDialog);
        congratulations.getDialogPane().setPrefWidth(600);
        congratulations.getDialogPane().setPrefHeight(400);
        congratulations.getDialogPane().getButtonTypes().add(ButtonType.OK);

        congratulations.showAndWait();
    }

    @FXML public void resumeAction(Event e) {
        backToMenuButton.setDisable(false);
        newGameButton.setDisable(true);
        getSolutionButton.setDisable(true);
        submitAndResetDisable(false);
        System.out.println("Hello Resume Game");
    }

    @FXML public void getSolutionAction(Event e) {
        formatGrid(true);
        backToMenuButton.setDisable(false);
        newGameButton.setDisable(true);
        getSolutionButton.setDisable(true);
//        resumeGameButton.setDisable(true);
        submitAndResetDisable(false);
        mainGridPane.setMouseTransparent(false);
        getSolutionGridBindingSet();
        submitButton.setOnMouseClicked(event -> {
            System.out.println("Submit button Clicked");
            try {
                submitButtonAction();
                Timeline timeline = new Timeline();
                timeline.getKeyFrames().addAll(
                        new KeyFrame(Duration.ZERO),
                        new KeyFrame(new Duration(2000), new KeyValue(numSolutions.textProperty(), "")));
                timeline.play();
            } catch (ExecutionException | InterruptedException executionException) {
                executionException.printStackTrace();
            }
        });
        resetButton.setOnMouseClicked(event -> {
            resetGridGetSolution();
        });
    }

    private void getSolutionGridBindingSet() {
        System.out.println("Hey!! You enter the sudoku");

        Service<Boolean> service = new Service<>() {
            @Override
            protected Task<Boolean> createTask() {
                return new Task<Boolean>() {
                    @Override
                    protected Boolean call() throws Exception {
                        int index = 0;
                        for (; index < mainGridList.size() - 1; index++)
                            ((Label) mainGridList.get(index)).textProperty().bindBidirectional(
                                    solverObject.getSudokuCellsArray()[index]);
                        return true;
                    }
                };
            }
        };
        service.start();
        service.setOnSucceeded(e -> System.out.println("Sudoku properties are uploaded"));
    }

    private void submitButtonAction() throws ExecutionException, InterruptedException {
        System.out.println("Hey Man");
        if (!selectionCellStack.isEmpty() && ((Label) selectionCellStack.peek()).getText().isEmpty())
            selectionCellStack.pop().setStyle("");
        keyBoardAndDeleteDisable(true);

        for (Node node : mainGridList)
            if (node.getStyle().contains("-fx-background-color: #873b36")) {
                numSolutions.setText("Improper Entries.");
                return;
            }

        mainGridList.forEach(e -> {
            e.setMouseTransparent(true);
        });

        Task<Object[]> task = new Task<>() {
            @Override
            protected Object[] call() {
                return solverObject.getSolutionFromSolver(this);
            }
        };

        service = new Service<Object[]>() {
            @Override
            protected Task<Object[]> createTask() {
                return task;
            }
        };
        service.start();

        progressBar.visibleProperty().bind(task.runningProperty());
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(e -> {
            System.out.println("solution found. Task Completed");
            Object[] solutions = new Object[2];
            try {
                solutions = task.get();
            } catch (InterruptedException | ExecutionException interruptedException) {
                interruptedException.printStackTrace();
            }
            int numSolution = (int) solutions[1];
            solverObject.syncWithSolvedSudoku((String) solutions[0]);
            String s = numSolution == 1 ? numSolution + " Solution " : numSolution + " Solutions ";
            s += "found.";
            numSolutions.setText(s);
        });
        task.setOnCancelled(e -> {
            System.out.println("Solution not found. Task Cancelled");
        });
    }

    private void resetGridGetSolution() {
        Worker.State s = (service == null) ? null : service.getState();
        if (s == Worker.State.SCHEDULED || s == Worker.State.RUNNING) {
            service.cancel();
            System.out.println("Service is cancelled. Now, Relax");
        }
        AtomicInteger index = new AtomicInteger();
        mainGridList.forEach(e -> {
            e.setMouseTransparent(false);
            if (index.get() < mainGridList.size() -1) {
                ((Label) e).setText("");
                solverObject.modifyInputAsNode(((Label) e), index.get(), "", false);
                index.getAndIncrement();
            }
        });
        numSolutions.setText("");
        keyBoardAndDeleteDisable(true);
    }

    private void resetGridNewGame(boolean b) {
        Worker.State s = (service == null) ? null : service.getState();
        if (s == Worker.State.SCHEDULED || s == Worker.State.RUNNING) {
            service.cancel();
            System.out.println("Service is cancelled. Now, Relax");
        }
        AtomicInteger index = new AtomicInteger();
        mainGridList.forEach(e -> {
            if (index.get() < mainGridList.size() - 1 && (b || !e.isMouseTransparent())) {
                ((Label) e).setText("");
                solverObject.modifyNodeAsInput(((Label) e), index.get(), "");
            }
            index.getAndIncrement();
        });
        numSolutions.setText("");
        keyBoardAndDeleteDisable(true);
    }
}
