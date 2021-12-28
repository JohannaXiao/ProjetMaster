import java.util.Random;

public class Job extends Object {
    private double condition = 0;
    private double MAX = 10000;

    Job(double condition) {
        this.condition = condition;
    }

    public boolean Run() {
        Random rand = new Random();
        while (true){
            double d = rand.nextDouble()*this.MAX;
            if(d<=condition){
                return true;
            }
        }
    }
}
