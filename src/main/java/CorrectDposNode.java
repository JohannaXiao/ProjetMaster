import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CorrectDposNode extends Node {
    private int cycle = 0;
    private double timeout;
    private double nextTimer;
    private int participeOrNot = 0;

    CorrectDposNode(EarthPosition position, double initialTimeout) {
        super(position);
        this.timeout = initialTimeout;
    }
    @Override public void onStart(Simulation simulation) {
    }

    @Override public void onTimerEvent(TimerEvent timerEvent, Simulation simulation) {

    }

    @Override public void onMessageEvent(MessageEvent messageEvent, Simulation simulation){

    }

}
