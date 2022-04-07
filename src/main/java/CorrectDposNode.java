import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class CorrectDposNode extends Node {
    private int cycle = 0;
    private double timeout;
    private double nextTimer;
    private CopyOnWriteArrayList<Block> blockChain = new CopyOnWriteArrayList<>();


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
    //添加新区块
    @Override public boolean addBlock(Block blcok){
        return true;
    }

}
