import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

public class Main {
    private static final int RANDOM_SEED = 12345;
    private static final double TIME_LIMIT = 4;
    private static final int SAMPLES = 50;

    public static void main(String[] args) {
        // Print the first row which contains column names.
//    System.out.println("initial_timeout, tendermint, algorand, this_work");
        System.out.println("initial_timeout, tendermint, algorand, mir, pbft");

        double pbftBestLatency = Double.MAX_VALUE, pbftBestTimeout = 0;

        int NumNodes = 1000;
        double initalTimeout = 0.12;

        for (int faliednode = 110; faliednode <= 140; faliednode += 1) {
//    for (double initalTimeout = 0.01; initalTimeout <= 0.4; initalTimeout += 0.01) {
//        for (int nodenum = 10; nodenum <= 5000; nodenum += 100) {
            DoubleSummaryStatistics tendermintOverallStats = new DoubleSummaryStatistics(),
                    pbftOverallStats = new DoubleSummaryStatistics();
            for (int i = 0; i < SAMPLES; ++i) {

                Optional<DoubleSummaryStatistics> pbftStats =
                        runPbft(initalTimeout, NumNodes - faliednode, faliednode);

                pbftStats.ifPresent(pbftOverallStats::combine);
            }

            if (pbftOverallStats.getCount() > 0 &&
                    pbftOverallStats.getAverage() < pbftBestLatency) {
                pbftBestLatency = pbftOverallStats.getAverage();
                pbftBestTimeout = initalTimeout;
            }

            System.out.printf("%d, %s,\n",
                    faliednode,
//                  initalTimeout,
//                    nodenum,
                    pbftOverallStats.getCount() > 0 ? pbftOverallStats.getAverage() : "");
        }

        System.out.println();

//    System.out.printf("pbft best with timeout %.2f: %.4f\n",
//        pbftBestTimeout, pbftBestLatency);
//    System.out.printf("Mir best with timeout %.2f: %.4f\n",
//            mirBestTimeout, mirBestLatency);
//    double secondBestLatency = Math.min(tendermintBestLatency, algorandBestLatency);
//    System.out.printf("Mir speedup: %.4f\n",
//        (secondBestLatency - mirBestLatency) / secondBestLatency);
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
    }

    private static String statisticsToCompactString(DoubleSummaryStatistics statistics) {
        return String.format("min=%.2f, max=%.2f, average=%.2f",
                statistics.getMin(), statistics.getMax(), statistics.getAverage());
    }
}
