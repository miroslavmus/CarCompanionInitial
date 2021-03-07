package uni.fmi.miroslav.carcompanion.models;

public class Part {

    private final int id;
    private final int picId;
    private final String name;
    private final int km;
    private final int time;

    public Part(int id, int picId, String name, int km, int time) {
        this.id = id;
        this.picId = picId;
        this.name = name;
        this.km = km;
        this.time = time;
    }

    public int getPicId() {
        return picId;
    }

    public String getName() {
        return name;
    }

    public int getKm() {
        return km;
    }

    public int getTime() {
        return time;
    }

}
