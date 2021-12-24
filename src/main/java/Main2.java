import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class Main2 {
    private static final int RANDOM_SEED = 12345;
    private static final double TIME_LIMIT = 4;
    private static final int SAMPLES = 50;
    private static final int SELECTEDNUM = 100;

    public static void main(String[] args) {
        // Print the first row which contains column names.
//    System.out.println("initial_timeout, tendermint, algorand, this_work");
        System.out.println(",  pbft");


        int NumNodes = 1000;
        double initalTimeout = 0.12;
        double failedNodeRate = 0.1;
//        List<DoubleSummaryStatistics> pbftTimes = new ArrayList<>();

        for (int nodeNum = 100; nodeNum <= NumNodes; nodeNum += 10) {
            DoubleSummaryStatistics pbftTimeSamples = new DoubleSummaryStatistics();
//    for (double initalTimeout = 0.01; initalTimeout <= 0.4; initalTimeout += 0.01) {
//        for (int nodenum = 10; nodenum <= 5000; nodenum += 100) {
//            List<Double> pbftTimeSamples = new ArrayList<>();
            for (int i = 0; i < SAMPLES; ++i) {
                int failednode = (int)Math.floor(nodeNum*failedNodeRate);
                double pbftTime = runPoSWbftTimer(initalTimeout, nodeNum-failednode,failednode);
                pbftTimeSamples.accept(pbftTime);
            }

            System.out.printf("%d, %s,\n",
                    nodeNum,
//                  initalTimeout,
//                    nodenum,
                    pbftTimeSamples.getCount() > 0 ? pbftTimeSamples.getAverage() : "");
        }

        System.out.println();

    }

    private static double runPoSWbftTimer(
            double initialTimeout, int correctNodeCount, int failedNodeCount) {
        Random random = new Random();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < correctNodeCount; ++i) {
            EarthPosition position = EarthPosition.randomPosition(random);
            nodes.add(new CorrectPbftNode(position, initialTimeout));
        }
        for (int i = 0; i < failedNodeCount; ++i) {
            EarthPosition position = EarthPosition.randomPosition(random);
            nodes.add(new FailedNode(position));
        }

        double startTime = System.currentTimeMillis();
        Job job = new Job(10);
        for (Node node :nodes){
            node.runPosTime(job);
        }
        List<Node> selectedNodes;
        if (nodes.size()>SELECTEDNUM){
            selectedNodes = nodes.stream().sorted().limit(SELECTEDNUM).collect(Collectors.toList());
        }else {
            selectedNodes = nodes;
        }
        Collections.shuffle(selectedNodes, random);
        Network network = new FullyConnectedNetwork(selectedNodes, random);
        Simulation simulation = new Simulation(network);
        if (!simulation.run(TIME_LIMIT)) {
            return Double.NaN;
        }
        double endTime =  System.currentTimeMillis();
        List<Node> correctNodes = nodes.stream()
                .filter(n -> n instanceof CorrectPbftNode)
                .collect(Collectors.toList());
        if (!correctNodes.stream().allMatch(Node::hasTerminated)) {
            System.out.println("WARNING: Not all Pbft nodes terminated.");
            return Double.NaN;
        }
        return endTime-startTime;
    }
    private static String statisticsToCompactString(DoubleSummaryStatistics statistics) {
        return String.format("min=%.2f, max=%.2f, average=%.2f",
                statistics.getMin(), statistics.getMax(), statistics.getAverage());
    }
}

