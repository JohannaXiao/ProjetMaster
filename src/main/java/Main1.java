import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class Main1 {
    private static final int RANDOM_SEED = 12345;
    private static final double TIME_LIMIT = 4;
    private static final int SAMPLES = 50;

    public static void main(String[] args) {
        // Print the first row which contains column names.
        System.out.println(",  pbft");

//        double pbftBestLatency = Double.MAX_VALUE;

        int NumNodes = 1000;
        double initalTimeout = 0.12;
        double failedNodeRate = 0.2;

        for (int nodeNum = 2; nodeNum <= NumNodes; nodeNum += 10) {
            DoubleSummaryStatistics pbftTimeSamples = new DoubleSummaryStatistics();
//    for (double initalTimeout = 0.01; initalTimeout <= 0.4; initalTimeout += 0.01) {
//        for (int nodenum = 10; nodenum <= 5000; nodenum += 100) {
//            List<Double> pbftTimeSamples = new ArrayList<>();
            for (int i = 0; i < SAMPLES; ++i) {
                int failednode = (int)Math.floor(nodeNum*failedNodeRate);
                double pbftTime = runPbftTimer(initalTimeout, nodeNum-failednode,failednode);
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

    private static Optional<DoubleSummaryStatistics> runPbft(
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
        Collections.shuffle(nodes, random);

        Network network = new FullyConnectedNetwork(nodes, random);
        Simulation simulation = new Simulation(network);
        if (!simulation.run(TIME_LIMIT)) {
            return Optional.empty();
        }
        List<Node> correctNodes = nodes.stream()
                .filter(n -> n instanceof CorrectPbftNode)
                .collect(Collectors.toList());
        if (!correctNodes.stream().allMatch(Node::hasTerminated)) {
            System.out.println("WARNING: Not all Pbft nodes terminated.");
            return Optional.empty();
        }
        //System.out.println("Pbft times: " + correctNodes.stream().mapToDouble(Node::getTerminationTime).sorted().boxed().collect(Collectors.toList()));
        return Optional.of(nodes.stream()
                .mapToDouble(Node::getTerminationTime)
                .summaryStatistics());
//        return Optional.of(nodes.stream().mapToDouble(Node::getOutput).summaryStatistics());
    }

    private static double runPbftTimer(
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
        Collections.shuffle(nodes, random);

        double startTime = System.currentTimeMillis();
        Network network = new FullyConnectedNetwork(nodes, random);
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

