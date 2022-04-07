import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.*;

public class Main3 {
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
                Optional<Double> PoSWbftTime = runDPOS(initalTimeout, NumNodes-failednode,failednode);
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

    private static List<Double> runDPOS(
            double initialTimeout, int correctNodeCount, int failedNodeCout) {
        Random random = new Random();
        List<Node> nodes = new ArrayList<>();
        List<Node> delegates;
        List<Double> DPOSTokenOverallStats;
        List<Node> lastDelegates = new ArrayList<>();
        List<Double> rechooseRateList = new ArrayList<>(),DPOSOverallStatsList = new ArrayList<>(),failRateList = new ArrayList<>();
        int failCount=0;
        for (int i = 0; i < correctNodeCount; ++i) {
            EarthPosition position = EarthPosition.randomPosition(random);
            nodes.add(new CorrectDposNode(position,initialTimeout);
        }
        for (int i = 0; i < failedNodeCout; ++i) {
            EarthPosition position = EarthPosition.randomPosition(random);
            nodes.add(new FailedNode(position));
        }
        Collections.shuffle(nodes, random);

        for(int r = 0;r < SAMPLES;r++){                                            //模拟n轮投票
            HashMap<Node,Double> voteTimeMap = new HashMap<>();

            HashMap<Node, List<Node>> votesMap = getVotes(nodes,"DPoS");    //模拟投票
//      delegates = getDelegatesByVotes(votesMap, delegateNum);      //选出见证人
            delegates = getDelegatesByVotes(votesMap, SELECTEDNUM*2);      //选出共识见证人和备选见证人

            delegates = delegates.subList(0, delegates.size() / 2);  //选出共识见证人节点

            failRateList.add(Double.valueOf(delegates.stream().filter(n->n instanceof FailedNode).count())/delegates.size());
            if(!lastDelegates.isEmpty()){
                List<Node> finalLastDelegates = lastDelegates;
                double lastFailNodes = finalLastDelegates.stream().filter(n->n instanceof FailedNode).count();
                double rechooseRate = lastFailNodes == 0?0.0:delegates.stream().filter(n->n instanceof FailedNode && finalLastDelegates.contains(n)).count()/lastFailNodes;
                rechooseRateList.add(rechooseRate);
                lastDelegates = new ArrayList<>(delegates);
            }else{
                lastDelegates = new ArrayList<>(delegates);
            }

            Collections.shuffle(delegates, random);                                 //随机打乱

            Network network = new FullyConnectedNetwork(delegates, random);
            for (int i = 0; i < SELECTEDNUM; i++) {                                  //这里每个共识见证人出块一次
                network.setCreatorId(i);
                Node creator = network.getCreator();
                if(creator instanceof DPoSNode) {
                    creator.setToken(creator.getToken() + reward);//更新生产节点奖励
                } else{
                    failCount++;
                }
            }
        }
        DPOSTokenOverallStats = nodes.stream().filter(node -> node.getToken()>=0)
                .map(Node::getToken)
                .collect(Collectors.toList());//统计金额
//    DPOSOverallStatsList.add(round>1?rechooseRateList.stream().mapToDouble(n->n).average().getAsDouble():0.);
        DPOSOverallStatsList.add(failRateList.stream().mapToDouble(n->n).average().getAsDouble());
        DPOSOverallStatsList.add(gini(DPOSTokenOverallStats));
//    System.out.println(round*delegateNum-failCount);
//    nodes.stream().filter(node -> node instanceof DPoSNode).forEach(x->System.out.println(x.getCredit()+":"+x.getToken()));
//    nodes.stream().filter(node -> node instanceof FailedNode).forEach(x->System.out.println(x.getCredit()+":"+x.getToken()));
//    rechooseRateList.stream().forEach(x->System.out.println(x));
        return DPOSOverallStatsList;
    }

    private static HashMap<Node,List<Node>> getVotes(List<Node> nodes,String type){
        HashMap<Node,List<Node>> result = new HashMap<>();
        Random random = new Random(1234);
        for(Node node:nodes){
            double max = 0;int randint = random.nextInt(nodes.size()),maxId = randint,num = type.equals("DPoS")? nodes.size() : nodes.size()/2;
            for(int i = randint;i<randint +num;i++){           //DPoSPT随机选出一半投票
                double feature = type.equals("DPoS")?Math.pow(random.nextDouble(),1/nodes.get(i%nodes.size()).getToken()):Math.pow(random.nextDouble(),1/(nodes.get(i%nodes.size()).getCredit()*nodes.get(i%nodes.size()).getToken()));
                if( feature> max){
                    max = feature;
                    maxId = i%nodes.size();
                }
            }
            if(result.containsKey(nodes.get(maxId))){
                result.get(nodes.get(maxId)).add(node);
            }else {
                result.put(nodes.get(maxId),new ArrayList<>(){{add(node);}});
            }
        }
        for(Node node:nodes){
            Set<Node> nodeSet = result.keySet();
            if(!nodeSet.contains(node)){
                result.put(node,new ArrayList<>());
            }
        }
        return result;
    }

    private static List<Node> getDelegatesByVotes(HashMap<Node,List<Node>> votesMap,int delegateNum){
        HashMap<Node,Double> votes = new HashMap<>();
        for(Map.Entry<Node,List<Node>> entry:votesMap.entrySet()){
            votes.put(entry.getKey(),entry.getValue().stream().mapToDouble(node -> node.getCredit()* node.getToken()).sum());
        }
        List<Map.Entry<Node,Double>> list = new ArrayList<Map.Entry<Node,Double>>(votes.entrySet());
        List<Node> result = new ArrayList<>();
        Collections.sort(list, new Comparator<Map.Entry<Node, Double>>() {
            @Override
            public int compare(Map.Entry<Node, Double> o1, Map.Entry<Node, Double> o2) {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        for( Map.Entry<Node,Double> e :list.subList(0,delegateNum)){
            result.add(e.getKey());
        }
        return result;
    }

    private static String statisticsToCompactString(DoubleSummaryStatistics statistics) {
        return String.format("min=%.2f, max=%.2f, average=%.2f",
                statistics.getMin(), statistics.getMax(), statistics.getAverage());
    }
}

