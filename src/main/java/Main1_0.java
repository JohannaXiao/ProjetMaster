import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class Main1_0 {
    private static final int RANDOM_SEED = 12345;
    private static final double TIME_LIMIT = 4;
    private static final int SAMPLES = 50;

    public static void main(String[] args) {
        // Print the first row which contains column names.
        System.out.println("failedNode,  pbft");
//        System.out.println("nodeNum,  pbft");

//        double pbftBestLatency = Double.MAX_VALUE;

        int NumNodes = 1000;
        double initalTimeout = 0.12;
//        double failedNodeRate = 0.15;

//        for (int nodeNum = 2; nodeNum <= NumNodes; nodeNum += 10) {
//    for (double initalTimeout = 0.01; initalTimeout <= 0.4; initalTimeout += 0.01) {
        for (int failednode = 0; failednode <= NumNodes; failednode += 10) {
//            List<Double> pbftTimeSamples = new ArrayList<>();
            DoubleSummaryStatistics pbftTimeSamples = new DoubleSummaryStatistics();
            for (int i = 0; i < SAMPLES; ++i) {
//                int failednode = (int)Math.floor(nodeNum*failedNodeRate);
                Optional<Double> pbftTime = runPbftTimer(initalTimeout, NumNodes-failednode,failednode);
//                pbftTimeSamples. accept(pbftTime);
                pbftTime.ifPresent(pbftTimeSamples::accept);
            }

            System.out.printf("%d, %s,\n",
                    failednode,
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
    private static Optional<Double> runPbftTimer(
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
            return Optional.empty();
        }
        double endTime =  System.currentTimeMillis();
        List<Node> correctNodes = nodes.stream()
                .filter(n -> n instanceof CorrectPbftNode)
                .collect(Collectors.toList());
        if (!correctNodes.stream().allMatch(Node::hasTerminated)) {
            System.out.println("WARNING: Not all Pbft nodes terminated.");
            return Optional.empty();

        }
        return Optional.of(endTime-startTime);
    }

    private static String statisticsToCompactString(DoubleSummaryStatistics statistics) {
        return String.format("min=%.2f, max=%.2f, average=%.2f",
                statistics.getMin(), statistics.getMax(), statistics.getAverage());
    }
}

