package uni.fmi.miroslav.carcompanion.tools;

public abstract class Calc {
    public static final double KM_MI_RATIO = 1.609344;
    public static final double LI_GAL_RATIO = 3.785411784;
    public static final double LKM_MPG_RATIO = 235.214583;
    public static double getMPG(double mi, double gal){
        return mi / gal;
    }
    public static double convert(double eff){
        return LKM_MPG_RATIO / eff;
    }
    public static double getLKM(double km, double li){
        return (li / km)*100;
    }
    public static double kmToMi(double km){ return km / KM_MI_RATIO; }
    public static int kmToMi(int km) {return (int)(km / KM_MI_RATIO);}
    public static double miToKm(double mi){
        return mi * KM_MI_RATIO;
    }
    public static int miToKm(int mi) { return (int)(mi * KM_MI_RATIO); }
    public static double liToGal(double li){
        return li / LI_GAL_RATIO;
    }
    public static double galToLi(double gal){ return gal * LI_GAL_RATIO; }
}
