package src.minesweeper;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Random;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JButton;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Stack;
import javax.swing.JOptionPane;

public class Board extends JPanel implements ActionListener {

    //DEFINE FIELDS

    //Fields related to board and cells

    private final int CELL_SIZE = 15;
    private final int N_MINES = 10;
    private static int N_ROWS = 16;
    private static int N_COLS = 16;
    private static String CELL_SPLITTER = " - ";
    private static String OBJECT_SPLITTER = "$";
    private final int BOARD_WIDTH = N_ROWS * CELL_SIZE + 1;
    private final int BOARD_HEIGHT = N_COLS * CELL_SIZE + 1;
    private int minesLeft;//keeps track of how many mines are left based on what user has flagged
    //2D array to represent game board
    protected static Cell[][] gameBoard;
    //total number of cells
    private int allCells;
    //Fields related to images used in our game to represent cells and bombs
    private final int NUM_IMAGES = 13;
    //Using map as collection to store images and their names, which can make it more easily retrievable
    private java.util.Map<String, Image> images;

    //Fields related to game status
    private boolean inGame;
    private static JLabel statusbar;
    private static JButton bUndo;
    private static JButton bRule;
    private static JTextArea textArea;
    private static String STATUS_FILE = "Status.txt";

    private Stack gameSteps = new Stack();
    //Constructor
    public Board(JLabel statusbar, JButton bUndo, JButton bRule, JTextArea textArea) throws IOException {

        this.statusbar = statusbar;
        this.bUndo = bUndo;
        this.bUndo.addActionListener(this);
        this.bRule = bRule;
        this.bRule.addActionListener(this);
        this.textArea = textArea;

        initBoard();
    }


    //Action performed when user wants to undo moves or see the rules
    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            if (e.getActionCommand().equals("Rules")) {
                showRules();
            }
            else if (e.getActionCommand().equals("Undo")) {
                this.undo();
            }
        }
        catch (Exception oe) {
            oe.printStackTrace();
        }
    }


    //Allows user to undo moves
    private void undo() {
        if (!gameSteps.empty()) {
            int i = (Integer)gameSteps.pop();//gets most recent game step
            //corresponding cell to the game step
            Cell cell = gameBoard[i / N_COLS][i % N_ROWS];

            //Handle flagged cells situation, which are covered
            if (cell.isCoveredCell()) {
                cell.changeWhetherMarked();
                if (cell.isMarkedCell()) {
                    minesLeft--;
                } else {
                    minesLeft++;
                    if (!inGame) {
                        inGame = true;
                    }
                }
            }

            else if (cell.getCellType() == CellType.Bomb) {
                cell.isCovered = true;
                inGame = true;

            }

            else if (cell.getCellType() == CellType.BombNeighbor) {
                cell.isCovered = true;
            }

            String msg = Integer.toString(minesLeft);
            this.statusbar.setText("Flags Left: " + msg);

            //Takes care of empty cell situation
            if (cell.getCellType() == CellType.Empty) {
                cell.isCovered = true;
                while (!gameSteps.empty()) {
                    int j = (Integer)gameSteps.pop();
                    Cell cellNext = gameBoard[j / N_COLS][j % N_ROWS];
                    if (cellNext.getCellType().equals(CellType.BombNeighbor)) {
                        gameSteps.push(j);
                        break;
                    } else {
                        cellNext.isCovered = true;
                    }
                }

            }

            repaint();
        }
    }


    //Saving game status
    protected static void saveGameStatus2File() throws IOException {
        String userName = "";
        //lets user know that
        if ("".equals(textArea.getText()) || textArea.getText().equals("Input your name here...")) {
            JOptionPane.showMessageDialog(null, "We gave you a default user name, you may input your name next time.");
            userName = "Default user";
        } else {
            userName = textArea.getText();
        }


        if (gameBoard.length == 0) {
            System.exit(0);
        }

        //Writes user name
        //Goes through the entire game board, records the state of each cell, writes in a text file
        FileWriter writer = new FileWriter(STATUS_FILE, false);

        try (PrintWriter printLine = new PrintWriter(writer)) {
            printLine.println(OBJECT_SPLITTER + "User Name" + OBJECT_SPLITTER);
            printLine.println(userName);
            printLine.println(OBJECT_SPLITTER + "Cells" + OBJECT_SPLITTER);
            for (int i = 0; i < N_ROWS; i++) {
                for (int j = 0; j < N_COLS; j++) {
                    if (null != gameBoard[i][j].getCellType()) switch (gameBoard[i][j].getCellType()) {
                        //if cell is empty
                        case Empty:
                            printLine.println(CellType.Empty.toString() + CELL_SPLITTER +
                                    Boolean.toString(gameBoard[i][j].isCoveredCell()) + CELL_SPLITTER +
                                    Boolean.toString(gameBoard[i][j].isMarkedCell()) + CELL_SPLITTER + "0");
                            break;
                        //if cell is a bomb cell
                        case Bomb:
                            printLine.println(CellType.Bomb.toString() + CELL_SPLITTER +
                                    Boolean.toString(gameBoard[i][j].isCoveredCell()) + CELL_SPLITTER +
                                    Boolean.toString(gameBoard[i][j].isMarkedCell()) + CELL_SPLITTER+ "0");
                            break;
                        //if cell is a neighbor of bomb
                        case BombNeighbor:
                            printLine.println(CellType.BombNeighbor.toString() + CELL_SPLITTER +
                                    Boolean.toString(gameBoard[i][j].isCoveredCell()) + CELL_SPLITTER +
                                    Boolean.toString(gameBoard[i][j].isMarkedCell()) + CELL_SPLITTER +
                                    gameBoard[i][j].getImageName());
                            break;
                        default:
                            break;
                    }
                }
            }
        }

        JOptionPane.showMessageDialog(null, "Status is saved!");

    }

    private void showRules() {
        JOptionPane.showMessageDialog(null, "GAME RULES: \n" + "\n"
                + "Goal: sweep all bombs from a 16x16 mine field." + "\n"
                + "Left click to uncover the cells, a cell with a number reveals the number of neighboring cells that contain bombs" +"\n"
                + "If a empty cell and its neighbors are also empty they both will be revealed (the entire region of all empty cells) until a cell with a number appears. Use this information plus guess work to avoid the bombs. "+"\n"
                + "To mark a cell you think is a bomb, right-click on the cell and a flag will appear. You have 40 flags in total, one for each bomb. The user can “unflag” a cell by right clicking the cell again."+"\n"
                + "You will be notified when you have used up all your 40. lags with a count of how many flags you have left in the lower left corner" +"\n"
                + "The user can “unflag” a cell by right clicking the cell again."+"\n"
                + "The game is won when the user has successfully identified all the cells that contain bombs and the game is lost when the player clicks on a cell which contains a bomb. \n" +
                "The user can undo any number of moves for any type of move, which includes clicking on flagged cells, empty cells, and neighbor cells."+"\n"
                + "To start a new game, just clicks anywhere on the board. You can stop the game at any point by exiting the game. The game will automatically be saved." +
                " When re-loaded, the user will have the option of starting a new game or starting from the most recent version of the game when exited.. "+"\n");
    }
