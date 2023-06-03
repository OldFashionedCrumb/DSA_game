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
                + "Goal: sweep all bombs from a 16x16 mine field" + "\n"
                + "Left click to uncover the cells, a cell with a number reveals the number of neighboring cells that contain bombs" +"\n"
                + "If a empty cell and its neighbors are also empty they both will be revealed " + "\n"
                +"(the entire region of all empty cells) until a cell with a number appears"+"\n"
                + "To mark a cell you think is a bomb, right-click on the cell and a flag will appear" +"\n"
                +" You have 40 flags in total, one for each bomb. The user can “unflag” a cell by right clicking the cell again."+"\n"
                + "You will be notified when you have used up all your 40 flags with a count in the lower left corner" +"\n"
                + "The user can “unflag” a cell by right clicking the cell again."+"\n"
                + "The game is won when the user has successfully identified all the cells that contain bombs" +"\n"
                +" and the game is lost when the player clicks on a cell which contains a bomb" + "\n" +
                "The user can undo any number of moves for any type of move" +"\n" +
                " which includes clicking on flagged cells, empty cells, and neighbor cells." +"\n"
                + "To start a new game, just clicks anywhere on the board. " +"\n"+
                "You can stop the game at any point by exiting the game. The game will automatically be saved." +
                " When re-loaded, the user will have the option of starting a new game or continue last game.. "+"\n");
    }

    //Initializes the game board
    private void initBoard() throws IOException {

        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        images = new java.util.HashMap<>();


        //Put all relevant images in the map, some images named with integers, others named with descriptors
        for (int i = 1; i < 9; i++) {
            String path = "src/resources/" + i + ".png";
            images.put(Integer.toString(i), (new ImageIcon(path)).getImage());
        }

        images.put("Bomb", (new ImageIcon("src/resources/Bomb.png")).getImage());
        images.put("Covered", (new ImageIcon("src/resources/Covered.png")).getImage());
        images.put("Empty", (new ImageIcon("src/resources/Empty.png")).getImage());
        images.put("Marked", (new ImageIcon("src/resources/Marked.png")).getImage());
        images.put("Wrongmarked", (new ImageIcon("src/resources/Wrongmarked.png")).getImage());

        addMouseListener(new MinesAdapter());

        showRules();

        //Load Game if user saves game status

        File statusFile = new File(STATUS_FILE);
        if (statusFile.exists()) {

            String[] options = { "yes, please!", "no, thanks!" };
            int result = JOptionPane.showOptionDialog(null, "Do you want to restore the previous status?", "",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

            if (result == 1) {
                newGame();
            } else {
                loadStatusFromFile();
                repaint();
            }

        } else {
            newGame();
        }
    }

    //Loads game status from file
    private void loadStatusFromFile() throws IOException {
        try {
            //2D Array of cells in gameboard
            gameBoard = new Cell[N_ROWS][N_COLS];

            try (BufferedReader reader = new BufferedReader(new FileReader(STATUS_FILE))) {
                inGame = true;
                minesLeft = N_MINES;
                // Parse user name
                String line = reader.readLine();
                if (line != null) {
                    if (line.startsWith(OBJECT_SPLITTER) && line.endsWith(OBJECT_SPLITTER) && line.contains("User Name")) {
                        line = reader.readLine();
                        if (line != null) {
                            textArea.setText(line);
                        }
                    }
                }
                // Parse cells of 2D Array
                //Extracts properties of 2D array cells previously written in
                line = reader.readLine();
                if (line != null) {
                    if (line.startsWith(OBJECT_SPLITTER) && line.endsWith(OBJECT_SPLITTER) && line.contains("Cells")) {
                        line = reader.readLine();
                        int i = 0;
                        while (line != null) {
                            String[] lineValue = line.split(CELL_SPLITTER);
                            if (lineValue.length == 4) {
                                if (null != lineValue[0]) switch (lineValue[0]) {
                                    case "Empty":
                                        gameBoard[i / N_COLS][i % N_ROWS] = new EmptyCell(lineValue[1], lineValue[2]);

                                        break;
                                    case "Bomb":
                                        gameBoard[i / N_COLS][i % N_ROWS] = new BombCell(lineValue[1], lineValue[2]);

                                        break;
                                    case "BombNeighbor":
                                        gameBoard[i / N_COLS][i % N_ROWS] = new NeighborOfBombCell(lineValue[1], lineValue[2],
                                                Integer.valueOf(lineValue[3]));
                                        break;
                                    default:
                                        break;
                                }
                            }
                            if (gameBoard[i / N_COLS][i % N_ROWS].isMarkedCell()) {
                                this.minesLeft--;
                            }
                            line = reader.readLine();
                            i++;
                        }
                    }
                }

                String msg = Integer.toString(minesLeft);
                this.statusbar.setText("Flags Left: " + msg);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Sets up value of game board for the first time
    private void newGame() {


        inGame = true;
        minesLeft = N_MINES;

        allCells = N_ROWS * N_COLS;

        //2D Array of cells in gameboard
        gameBoard = new Cell[N_ROWS][N_COLS];

        for (int x = 0; x < N_ROWS; x++) {//initially have everything be empty
            for(int y=0; y < N_COLS; y++) {
                gameBoard[x][y] = new EmptyCell();
            }
        }

        statusbar.setText("Flags Left: " + Integer.toString(minesLeft));

        int i = 0;

        //set up the grid
        while (i < N_MINES) {
            Random random = new Random();
            int positionX = (int) (random.nextInt(15 - 0 + 1) + 0);
            int positionY = (int) (random.nextInt(15 - 0 + 1) + 0);

            //randomly place the bomb cell
            if(gameBoard[positionX][positionY].getCellType() != CellType.Bomb) {
                gameBoard[positionX][positionY] = new BombCell();


                //sets up neighbor cells
                for(int dx = -1; dx <= 1; dx++) {
                    for(int dy = -1; dy <= 1; dy++) {
                        if((dx != 0 || dy != 0) && positionX + dx < N_COLS && positionY + dy < N_ROWS
                                && positionX + dx >= 0 && positionY + dy >=0) {
                            CellType typeOfCell = gameBoard[positionX + dx][positionY + dy].getCellType();
                            if(typeOfCell != CellType.Bomb) {//not already a neighbor cell
                                if (typeOfCell != CellType.BombNeighbor) {
                                    NeighborOfBombCell neighbor = new NeighborOfBombCell();
                                    neighbor.cellCount();
                                    gameBoard[positionX + dx][positionY + dy] = neighbor;
                                }
                                else {//already a neighbor cell, just need to update the neighbor count

                                    gameBoard[positionX + dx][positionY + dy].cellCount();
                                }
                            }

                        }
                    }
                }
                i++;

            }

        }
    }

    //checks this for all neighbors
    public void find_empty_cells(int x, int y) {

        //int current_col = j % N_COLS;
        gameBoard[x][y].flipUp();
        gameSteps.push(x * N_COLS + y);//add steps to gameSteps Stack
        for(int dx = -1; dx <= 1; dx++) {
            for(int dy = -1; dy <= 1; dy++) {//set bounds
                if((dx != 0 || dy != 0) && x + dx < N_COLS && y + dy < N_ROWS
                        && x + dx >= 0 && y + dy >= 0) {

                    CellType typeOfCell = gameBoard[x + dx][y + dy].getCellType();
                    //if(typeOfCell == CellType.BombNeighbor && gameBoard[x + dx][y + dy].isCoveredCell()) {
                    //    gameBoard[x + dx][y + dy].flipUp();
                    //}
                    //else
                    if(typeOfCell == CellType.Empty && gameBoard[x + dx][y + dy].isCoveredCell()) {
                        find_empty_cells(x + dx, y + dy);
                    }
                }
            }
        }



    }


    @Override
    public void paintComponent(Graphics g) {

        int uncover = 0;

        for (int i = 0; i < N_ROWS; i++) {
            for (int j = 0; j < N_COLS; j++) {

                Cell cell = gameBoard[i][j];
                String imageName = cell.getImageName();

                //game over when user clicks on mine
                if (inGame && cell.getCellType() == CellType.Bomb && !cell.isCoveredCell()) {
                    inGame = false;
                }
                if (!inGame) {//when game is over
                    if (cell.getCellType() == CellType.Bomb && !cell.isMarkedCell()) {
                        cell.flipUp();
                        imageName = ImageName.Bomb.toString(); // draw mine
                    } else if (cell.isCoveredCell() && cell.getCellType() == CellType.Bomb && cell.isMarkedCell()) {
                        imageName = ImageName.Marked.toString(); //draw mark
                    } else if (cell.isCoveredCell() && cell.getCellType() != CellType.Bomb && cell.isMarkedCell()) {//wrongly marked cells
                        imageName = ImageName.Wrongmarked.toString(); //draw wrong mark
                    } else if (cell.isCoveredCell()) {//board cells that are still covered remain covered
                        imageName = ImageName.Covered.toString(); //draw cover
                    }

                } else {//when game is still going
                    if (cell.isMarkedCell()) {//draw a mark if user clicks on covered cell
                        imageName = ImageName.Marked.toString();
                    } else if (cell.isCoveredCell()) {//draw cover when user clicks on a flagged/marked cell, cover is revealed reducing cell value
                        imageName = ImageName.Covered.toString();
                        uncover++;
                    }
                }

                g.drawImage(images.get(imageName), (j * CELL_SIZE),
                        (i * CELL_SIZE), this);
            }
        }

        if (uncover == 0 && inGame) {//when there's nothing left to uncover
            inGame = false;
            statusbar.setText("Game won");
        } else if (!inGame) {
            //Clear all user steps stored in gameSteps so user cannot "undo" moves when
            //bomb is clicked on
            gameSteps.clear();
            statusbar.setText("Game lost");
        }

        //no more "Undo" option once user undoes all the steps
        if (gameSteps.empty()) {
            this.bUndo.setEnabled(false);
        } else {
            this.bUndo.setEnabled(true);
        }
    }


    //Makes changes based on user action
    private class MinesAdapter extends MouseAdapter {


        @Override
        public void mousePressed(MouseEvent e) {

            //x and y coordinates
            int x = e.getX();
            int y = e.getY();

            //corresponding column and row in board
            int cCol = x / CELL_SIZE;
            int cRow = y / CELL_SIZE;

            boolean doRepaint = false;

            if (!inGame) {

                newGame();
                repaint();
                gameSteps.clear();
            }

            //check if we are located in mine field area
            if ((x < N_COLS * CELL_SIZE) && (y < N_ROWS * CELL_SIZE)) {

                //RIGHT MOUSE CLICK
                if (e.getButton() == MouseEvent.BUTTON3) {

                    if (gameBoard[cRow][cCol].isCoveredCell()) {
                        doRepaint = true;//implies that we do nothing if user right clicks on a number cell

                        //right click on an unmarked cell

                        if (!gameBoard[cRow][cCol].isMarkedCell() && minesLeft > 0) {
                            Cell cell = gameBoard[cRow][cCol];
                            cell.changeWhetherMarked();//changed to marked cell
                            minesLeft--;
                            if (minesLeft > 0) {
                                String msg = Integer.toString(minesLeft);
                                statusbar.setText("Flags Left: " + msg);
                            }  else {
                                statusbar.setText("No marks left");
                            }
                            //add steps to gameSteps stack
                            gameSteps.push(cRow * N_COLS + cCol);
                        }
                        else if (gameBoard[cRow][cCol].isMarkedCell()) {//right click on already marked cell, removes marks and increase number of cells to be marked
                            gameBoard[cRow][cCol].changeWhetherMarked();//changes it to not marked
                            minesLeft++;
                            String msg = Integer.toString(minesLeft);
                            statusbar.setText("Flags Left: " + msg);
                        }


                    }


                    //LEFT MOUSE CLICK
                } else {

                    //nothing happens if user left clicks on covered and marked cell
                    if (gameBoard[cRow][cCol].isMarkedCell()) {
                        return;
                    }

                    //user left clicks to remove a cover from cell
                    if (gameBoard[cRow][cCol].isCoveredCell()
                        //&& (gameBoard[cRow][cCol].getCellType() == CellType.Bomb )
                    ) {

                        gameBoard[cRow][cCol].flipUp();
                        doRepaint = true;
                        gameSteps.push(cRow * N_COLS + cCol);

                        //if user clicks on mine, game is over
                        if (gameBoard[cRow][cCol].getCellType() == CellType.Bomb
                                && !gameBoard[cRow][cCol].isCoveredCell()) {
                            inGame = false;
                        }

                        //if user clicks on empty cell, call empty cell function which will handle the situation
                        if (gameBoard[cRow][cCol].getCellType() == CellType.Empty) {
                            find_empty_cells(cRow, cCol);
                        }
                    }
                }

                if (doRepaint) {
                    repaint();
                }




            }
        }
    }
}

