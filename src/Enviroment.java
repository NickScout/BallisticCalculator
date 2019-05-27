public class Enviroment {
    private static double air_density = 1.2041; //kg/m^3
    private static double tempretaure = 20; //C
    private static double range; //m

    public static double getRange() {
        return range;
    }

    public static double getRange_m() {
        return range;
    }

    public static void setRange_m(double range) {
        Enviroment.range = range;
    }

    public static double getRange_ft() {
        return range * 3.28084;
    }

    public static void setRange_ft(double range) {
        Enviroment.range = range * 0.3048;
    }


    public static double getTempretaure_C() {
        return tempretaure;
    }

    public static double getTempretaure_F() {
        return tempretaure*1.8 + 32;
    }

    public static void setTempretaure_C(double tempretaure) {
        Enviroment.tempretaure = tempretaure;
    }

    public static void setTempretaure_F(double tempretaure_f) { Enviroment.tempretaure =  (68 - tempretaure_f)/1.8; }

    public static double getAir_density_kg_to_cubeM() {
        return air_density;
    }

    public static void setAir_density_kg_to_cubeM(double air_density) {
        Enviroment.air_density = air_density;
    }

    public static void setAir_density_lb_to_cubeFt(double air_density) {
        Enviroment.air_density = air_density*16.018463;
    }

    public static double getSoundSpeed_m_per_s() {
        return 0.331 * 0.606*tempretaure;
    }
}
