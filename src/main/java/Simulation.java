import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

class Simulation {
    private final Network network;
    private final TreeSet<Event> eventsByTime = new TreeSet<>();

    Simulation(Network network) {
        this.network = network;
    }

    // 广播信息，包括进行广播行为的源节点，需广播的信息， time 现在时间？
    void broadcast(Node source, Message message, double time) {
        for (Node destination : network.getNodes()) {
//      latency为广播所需时间
            double latency = network.getLatency(source, destination);
            double arrivalTime = time + latency;
//      记录传播消息事件（依据时间记录，该消息事件中包含 抵达时间，目标节点，消息内容，即针对目标节点的消息事件
//            System.out.printf("time = %.5f, latency = %.5f, arrivalTime = %.5f\n", time, latency, arrivalTime);
            eventsByTime.add(new MessageEvent(arrivalTime, destination, message));
        }
    }

    Network getNetwork() {
        return network;
    }

    Node getLeader(int index) {
        return network.getLeader(index);
    }

    void scheduleEvent(Event event) {
        eventsByTime.add(event);
    }

    /**
     * Run until all events have been processed, including any newly added events which may be added
     * while running.
     *
     * @param timeLimit the maximum amount of time before the simulation halts
     * @return whether the simulation completed within the time limit
     */
    boolean run(double timeLimit) {
//    类似启动每个节点，将其设置为开始状态
        for (Node node : network.getNodes()) {
            node.onStart(this);
        }
//  在传播阶段添加了许多事件，要逐个推出
        while (!eventsByTime.isEmpty()) {
            Event event = eventsByTime.pollFirst();
            // 若该event时间大于限制时间，return false
            if (event.getTime() > timeLimit) {
                //System.out.println("WARNING: Simulation timed out");
                return false;
            }

            Node subject = event.getSubject();
            if (event instanceof TimerEvent) {
                subject.onTimerEvent((TimerEvent) event, this);
            } else if (event instanceof MessageEvent) {
                subject.onMessageEvent((MessageEvent) event, this);
            } else {
                throw new AssertionError("Unexpected event: " + event);
            }
        }

        return true;
    }

}