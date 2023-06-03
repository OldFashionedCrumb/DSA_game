package src.minesweeper;

public class EmptyCell extends Cell {

    public EmptyCell() {
        super();
        this.cellType = CellType.Empty;
    }

    public EmptyCell(String isCovered, String isMarked) {
        super(isCovered, isMarked);
        this.cellType = CellType.Empty;
    }


    @Override
    public CellType getCellType() {
        return this.cellType;
    }

    @Override
    public String getImageName() {
        return ImageName.Empty.toString();
    }

}
