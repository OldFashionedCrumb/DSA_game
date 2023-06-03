package src.minesweeper;

public class Cell {

    protected boolean isCovered = true;//default
    protected CellType cellType = CellType.Empty;
    protected boolean isMarked = false;//default


    public Cell() {
    }

    public void cellCount() {
        //this.bombCellCount ++;
    }

    public Cell(String isCovered, String isMarked) {
        if(isCovered.equals("true")) {
            this.isCovered = true;
        }else {
            this.isCovered = false;
        }

        if(isMarked.equals("true")) {
            this.isMarked = true;
        }else {
            this.isMarked = false;
        }

    }

    public void flipUp() {
        this.isCovered = false;
    }

    public String getImageName() {
        return ImageName.Covered.toString();
    }

    public CellType getCellType() {
        return this.cellType;
    }

    public boolean isCoveredCell() {
        return this.isCovered;
    }

    public boolean isMarkedCell() {
        return this.isMarked;
    }

    public void changeWhetherMarked() {
        this.isMarked = !isMarked;
    }

}


