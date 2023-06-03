package src.minesweeper;


public class NeighborOfBombCell extends Cell {

    private int bombCellCount;

    public NeighborOfBombCell() {
        super();
        this.cellType = CellType.BombNeighbor;
    }


    public NeighborOfBombCell(String isCovered, String isMarked, int bombCellCount) {
        super(isCovered, isMarked);
        this.cellType = CellType.BombNeighbor;
        this.bombCellCount = bombCellCount;
    }

    //want to call this function when initializing board, which automatically
    @Override
    public void cellCount() {
        this.bombCellCount ++;
    }

    @Override
    public CellType getCellType() {
        return this.cellType;
    }

    @Override
    public String getImageName(){
        return Integer.toString(bombCellCount);
    }

}
