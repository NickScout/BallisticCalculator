import java.util.ArrayList;
import java.util.Vector;
import java.util.function.Function;

public interface Interpolator {
    static Function<Double, Double> build_function(double[] x, double[] y) {
        int len = x.length;
        ArrayList<Function<Double, Double>> l = new ArrayList<>(len);
        for (int i = 0; i < len; i++) {
            int finalI = i;
            l.add((var) -> {
               double res = 1;
                for (int j = 0; j < len; j++) {
                    res *= (var - x[j])/(x[finalI] - x[j]);
                }
                return res;
            });

        }
        Function<Double,Double> f = (X) -> {
            double L = 0;
            for (int i = 0; i < l.size(); i++) {
                L += l.get(i).apply(X) * y[i];
            }
            return L;
        };

        return f;
    }
}
