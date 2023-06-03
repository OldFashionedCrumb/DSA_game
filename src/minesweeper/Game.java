package src.minesweeper;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.WindowEvent;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class Game extends JFrame {

    private JLabel statusbar;
    private JButton bUndo;
    private JButton bRule;
    private JPanel buttonPanel;
    private JTextArea textArea;
    private JPanel statusPanel;
    protected Board board;


    public Game() throws IOException {


        initUI();

    }

    private void initUI() throws IOException {

        bUndo = new JButton("Undo");
        bRule = new JButton("Rules");
        buttonPanel = new JPanel();


        add(buttonPanel, BorderLayout.NORTH);
        buttonPanel.add(bRule, BorderLayout.NORTH);
        buttonPanel.add(bUndo, BorderLayout.SOUTH);

        statusPanel = new JPanel();
        statusbar = new JLabel("Flags Left");
        textArea = new JTextArea();
        textArea.setText("Input your name here...");
        statusPanel.add(statusbar, BorderLayout.NORTH);
        statusPanel.add(textArea, BorderLayout.SOUTH);
        add(statusPanel, BorderLayout.SOUTH);

        board = new Board(statusbar, bUndo, bRule, textArea);
        add(board);

        setResizable(false);
        pack();

        setTitle("Minesweeper");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


    }

    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {
            Game ex;
            try {
                ex = new Game();
                //Automatically saves file when user exits out of game
                ex.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(WindowEvent winEvt) {
                        try {
                            Board.saveGameStatus2File();
                        } catch (IOException ex1) {

                        }

                    }
                });
                ex.setVisible(true);

            } catch (IOException e) {
                e.printStackTrace();
            }

        });
    }
}
