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