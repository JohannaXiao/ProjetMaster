import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class Main2_0 {
    private static final int RANDOM_SEED = 12345;
    private static final double TIME_LIMIT = 4;
    private static final int SAMPLES = 50;
    private static final int SELECTEDNUM = 250;

    public static void main(String[] args) {
        // Print the first row which contains column names.
//    System.out.println("initial_timeout, tendermint, algorand, this_work");
        System.out.println("nodeNum, PoSWbft");


        int NumNodes = 1000;
        double initalTimeout = 0.12;
//        double failedNodeRate = 0.1;

//        for (int nodeNum = 2; nodeNum <= NumNodes; nodeNum += 10) {
        for (int failednode = 0; failednode <= NumNodes; failednode += 10) {
            DoubleSummaryStatistics PoSWbftTimeSamples = new DoubleSummaryStatistics();
//    for (double initalTimeout = 0.01; initalTimeout <= 0.4; initalTimeout += 0.01) {
//        for (int nodenum = 10; nodenum <= 5000; nodenum += 100) {
            for (int i = 0; i < SAMPLES; ++i) {
//                int failednode = (int)Math.floor(nodeNum*failedNodeRate);
                Optional<Double> PoSWbftTime = runPoSWbftTimer(initalTimeout, NumNodes-failednode,failednode);
//                PoSWbftTimeSamples.accept(PoSWbftTime);
                PoSWbftTime.ifPresent(PoSWbftTimeSamples::accept);
            }

            System.out.printf("%d, %s,\n",
                    failednode,
//                  initalTimeout,
//                    nodeNum,
                    PoSWbftTimeSamples.getCount() > 0 ? PoSWbftTimeSamples.getAverage() : "");
        }

        System.out.println();

    }

    private static Optional<Double> runPoSWbftTimer(
            double initialTimeout, int correctNodeCount, int failedNodeCount) {
        Random random = new Random();
        List<Node> nodes = new ArrayList<>();
        for (int i = 0; i < correctNodeCount; ++i) {
            EarthPosition position = EarthPosition.randomPosition(random);
            nodes.add(new CorrectPoSWbftNode(position, initialTimeout));
        }
        for (int i = 0; i < failedNodeCount; ++i) {
            EarthPosition position = EarthPosition.randomPosition(random);
            nodes.add(new FailedNode(position));
        }

        double startTime = System.currentTimeMillis();
        Job job = new Job(10);
        for (Node node :nodes){
            if (node instanceof CorrectPoSWbftNode) {
                node.runPosTime(job);
            }
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
            return Optional.empty();
        }
        double endTime =  System.currentTimeMillis();
        List<Node> correctNodes = selectedNodes.stream()
                .filter(n -> n instanceof CorrectPoSWbftNode)
                .collect(Collectors.toList());
        if (!correctNodes.stream().allMatch(Node::hasTerminated)) {
            System.out.println("WARNING: Not all nodes terminated.");
            return Optional.empty();
        }
        return Optional.of(endTime-startTime);
    }
    private static String statisticsToCompactString(DoubleSummaryStatistics statistics) {
        return String.format("min=%.2f, max=%.2f, average=%.2f",
                statistics.getMin(), statistics.getMax(), statistics.getAverage());
    }
}

