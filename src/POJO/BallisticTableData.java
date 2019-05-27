package POJO;

import javafx.beans.property.SimpleDoubleProperty;

public class BallisticTableData {
    private SimpleDoubleProperty range;
    private SimpleDoubleProperty angle;
    private SimpleDoubleProperty moa;

    public BallisticTableData(SimpleDoubleProperty range, SimpleDoubleProperty angle, SimpleDoubleProperty moa) {
        this.range = range;
        this.angle = angle;
        this.moa = moa;
    }

    public BallisticTableData() { }

    public double getRange() {
        return range.get();
    }

    public SimpleDoubleProperty rangeProperty() {
        return range;
    }

    public void setRange(double range) {
        this.range.set(range);
    }

    public double getAngle() {
        return angle.get();
    }

    public SimpleDoubleProperty angleProperty() {
        return angle;
    }

    public void setAngle(double angle) {
        this.angle.set(angle);
    }

    public double getMoa() {
        return moa.get();
    }

    public SimpleDoubleProperty moaProperty() {
        return moa;
    }

    public void setMoa(double moa) {
        this.moa.set(moa);
    }
}
