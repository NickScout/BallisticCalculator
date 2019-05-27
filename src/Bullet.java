import java.util.function.Function;

public class Bullet {
    private String name;
    private double mass_kg;
    private double caliber_mm;
    private Function<Double, Double> drag_curve = //Drags to m/s
            (V) -> {
                if (V > Enviroment.getSoundSpeed_m_per_s())
                    return 22.5 * Math.pow(V, -0.56);
                else return 0.125;
            };
    private double G7_form_factor = 1.0;
    private double area; //sq m
    private double muzzleVelocity_m_per_s;

    public double getMuzzleVelocity_m_per_s() {
        return muzzleVelocity_m_per_s;
    }

    public double getMuzzleVelocity_ft_per_s() { return muzzleVelocity_m_per_s*3.28084; }

    public void setMuzzleVelocity_m_per_s(double muzzleVelocity_m_per_s) {
        this.muzzleVelocity_m_per_s = muzzleVelocity_m_per_s;
    }

    public void setMuzzleVelocity_ft_per_s(double muzzleVelocity_ft_per_s) {
        this.muzzleVelocity_m_per_s = muzzleVelocity_ft_per_s * 0.3048;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setMass_kg(double mass_kg) {
        this.mass_kg = mass_kg;
    }

    public void setMass_pound(double mass) {
        this.mass_kg = mass*0.453592;
    }

    public void setMass_gran(double mass) {
        this.mass_kg = mass*0.00082;
    }

    public double getMass_kg() { return mass_kg;}

    public double getMass_gram() {
        return mass_kg *1000;
    }

    public double getMass_gran() {
        return mass_kg *1219.5121951219512;
    }

    public double getCaliber_inch() {
        return caliber_mm/25.4;
    }

    public void setCaliber_inch(double caliber_inch) {
        this.setCaliber_mm(caliber_inch * 25.4);
    }

    public void setCaliber_mm(double caliber_mm) {
        this.caliber_mm = caliber_mm;
        area = Math.PI * (caliber_mm/1000) * (caliber_mm/1000)/ 4;
    }

    public double getCaliber_mm() {
        return caliber_mm;
    }

    public Function<Double, Double> getDrag_curve() {
        return drag_curve;
    }

    public void setDrag_curve(Function<Double, Double> drag_curve) {
        this.drag_curve = drag_curve;
    }

    public void setG7_velocity_m_per_s(double x[], double y[]) {
        drag_curve = Interpolator.build_function(x,y);
    }

    public void setG7_velocity_ft_per_s (double x[], double y[]) {
        for (int i = 0; i < x.length; i++) {
            x[i] *= 0.3048;
        }
        drag_curve = Interpolator.build_function(x,y);
    }

    public double getG7_form_factor() {
        return G7_form_factor;
    }

    public void setG7_form_factor(double g7_form_factor) {
        G7_form_factor = g7_form_factor;
    }

    public void setG7_form_factor(double nose_len, double Rt_to_R, double mepl, double tail, double tail_angle) {
        G7_form_factor = 1.470 -
                0.346*getCaliber_inch() -
                0.162*nose_len +
                0.018*Rt_to_R +
                0.072*Rt_to_R*Rt_to_R +
                2.520*mepl -
                3.584*mepl*mepl -
                0.171 * tail -
                0.111 * tail_angle +
                0.0118*tail_angle*tail_angle -
                0.000359*tail_angle*tail_angle*tail_angle;
    }

    public double getArea_sq_mm() {
        return area* 0.000001;
    }

    public double getArea_sq_meters() {
        return area;
    }

    public void setG7ToDefault() {
        drag_curve = (V) -> {
            if (V > Enviroment.getSoundSpeed_m_per_s())
                return 22.5 * Math.pow(V, -0.56);
            else return 0.125;
        };
    }


    public Bullet() {
        setG7_form_factor(1.0);
        setG7ToDefault();
    }

    public Bullet(String name, double mass, double caliber_mm, double g7_form_factor, double muzzleVelocity_m_per_s, Function<Double, Double> dragcurve) {
        this.name = name;
        this.mass_kg = mass;
        this.setCaliber_mm(caliber_mm);
        drag_curve = dragcurve;
        G7_form_factor = g7_form_factor;
        this.muzzleVelocity_m_per_s = muzzleVelocity_m_per_s;
    }



    public double getBallisticCoefficient() {
        return mass_kg /(caliber_mm*caliber_mm + getG7_form_factor());
    }

    public double getDrag() {
        return G7_form_factor;
    }

    @Override
    public String toString() {
        return String.format("{\n\tBullet %s" +
                "\tcal = %f" +
                "\tmass = %f" +
                "\tV0 = %f" +
                "\tsq = %f\n}\n", this.getName(), this.getCaliber_mm(), this.getMass_kg(), this.getMuzzleVelocity_m_per_s(), this.getArea_sq_mm());
    }
}
