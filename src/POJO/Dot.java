package POJO;
import javafx.beans.property.SimpleDoubleProperty;

//a POJO class used to store dotes to interpolate bullet's drag coefficient on them
public class Dot {
    //Drag coefficient
    private SimpleDoubleProperty D;
    //Velocity
    private SimpleDoubleProperty V;

    public double getD() {
        return D.get();
    }

    public SimpleDoubleProperty dProperty() {
        return D;
    }

    @Override
    public boolean equals(Object obj) {
        Dot other;
        try {
            other = (Dot) obj;
        } catch (Exception ex) {
            return false;
        }
        return D.equals(other.dProperty()) && V.equals(other.vProperty());
    }

    public void setD(double d) {
        this.D.set(d);
    }

    public double getV() {
        return V.get();
    }

    public SimpleDoubleProperty vProperty() {
        return V;
    }

    public void setV(double v) {
        this.V.set(v);
    }

    public Dot(SimpleDoubleProperty d, SimpleDoubleProperty v) {

        D = d;
        V = v;
    }

    public Dot() { }


}
