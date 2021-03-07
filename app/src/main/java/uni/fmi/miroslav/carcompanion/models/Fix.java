package uni.fmi.miroslav.carcompanion.models;

public class Fix{

    private final int partId;
    private final int atKm;
    private final String date;
    private final String info;

    public Fix(int partId, int atKm, String date, String info) {
        this.partId = partId;
        this.atKm = atKm;
        this.date = date;
        this.info = info;
    }

    public int getPartId() {
        return partId;
    }

    public int getAtKm() {
        return atKm;
    }

    public String getDate() {
        return date;
    }

    public String getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return date + atKm + "km ";
    }
}
