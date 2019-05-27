public interface BallisticsHandler {
    static Double getAngle(Double range, Bullet bullet, double marksman_angle) {
        try {
            System.out.println(bullet.toString());
            double k = (Enviroment.getAir_density_kg_to_cubeM() * bullet.getDrag() * bullet.getArea_sq_mm()) / (2 * bullet.getMass_kg());
            System.out.println(String.format("got k = %f from \t{" +
                            "\tair_density = %f;" +
                            "\t\tdrag = %f;" +
                            "\t\tarea = %f;" +
                            "\t\tmass = %f\t}",
                    k, Enviroment.getAir_density_kg_to_cubeM(),bullet.getDrag(),bullet.getArea_sq_meters(), bullet.getMass_kg()));
            double eps_kx_minus_1 = Math.pow(Math.E, -1 * k * range);
            System.out.println(k*range);
            System.out.println("got eps_kx_minus_1 = " + eps_kx_minus_1);
            double velocity = bullet.getMuzzleVelocity_m_per_s() * eps_kx_minus_1;
            double time = (range/bullet.getMuzzleVelocity_m_per_s()) * Math.log( (bullet.getMuzzleVelocity_m_per_s()/velocity)/(1 - velocity/bullet.getMuzzleVelocity_m_per_s()));
            double tan = Math.atan(marksman_angle) - ((9.8 * time)/bullet.getMuzzleVelocity_m_per_s())*(0.5*(1 + bullet.getMuzzleVelocity_m_per_s()/velocity));
            double res = Math.atan(tan);
            System.out.println(String.format("got Vx = %f from \t{" +
                            "\ttime = %f;" +
                            "\t\ttan = %f;" +
                            "\t\tres = %f;\t}",
                    velocity, time, tan, res));
            return res;
        } catch (Exception e) {
            System.out.println("Exception while calculating angle: " + e.getMessage());
            return null;
        }
    }

    static Double getAngle(Double range, Bullet bullet) {
        return getAngle(range, bullet, 0);
    }


}
