package src.minesweeper;

public class BombCell extends Cell {

    public BombCell() {
        super();
        this.cellType = CellType.Bomb;
    }

    public BombCell(String isCovered, String isMarked) {
        super(isCovered, isMarked);
        this.cellType = CellType.Bomb;
    }

    @Override
    public CellType getCellType() {
        return this.cellType;
    }

    @Override
    public String getImageName() {
        return ImageName.Bomb.toString();
    }
}
