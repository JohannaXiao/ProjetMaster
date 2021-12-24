import java.lang.Double;

abstract class Node implements Comparable<Node>{
  private final EarthPosition position;
  private Proposal output;
  private double terminationTime;
//  private boolean participeOrNot = false;
  private double PosStartTime = 0;
  private double PosEndTime = 0;
  private double PosDuration = 0;

  Node(EarthPosition position) {
    this.position = position;
  }

//  public boolean getParticipeOrNot(){
//    return this.participeOrNot;
//  }
//  public void defParticipeOrNot(boolean x){
//    this.participeOrNot = x;
//  }
  public double getPosStartTime(){return this.PosStartTime;}
  public double getPosEndTime(){return this.PosEndTime;}
  public double getPosDuration(){return this.PosDuration;}

  public void runPosTime(Job job){
    double startTime = System.currentTimeMillis();
      if(job.Run()){
        double endTime = System.currentTimeMillis();
        this.PosStartTime = startTime;
        this.PosEndTime = endTime;
        this.PosDuration = endTime-startTime;
    }
  }

  public int compareTo(Node node){
    if (this.PosDuration== node.PosDuration){
      return Double.compare(this.PosEndTime, node.PosEndTime);
    }
    return Double.compare(this.PosDuration, node.PosDuration);
  }

  abstract void onStart(Simulation simulation);

  abstract void onTimerEvent(TimerEvent timerEvent, Simulation simulation);

  abstract void onMessageEvent(MessageEvent messageEvent, Simulation simulation);

//  判断该节点是否完成Proposal，已经完成了即output！=null（有proposal了）则返回true
  boolean hasTerminated() {
    return output != null;
  }

  void terminate(Proposal output, double terminationTime) {
    this.output = output;
    this.terminationTime = terminationTime;
  }

  /** The great-circle distance to another node, in meters. */
  double getDistance(Node that) {
    return this.position.getDistance(that.position);
  }

  double getTerminationTime() {
    return terminationTime;
  }

  Proposal getOutput(){return output;};
}

/** A node which has simply failed, and thus ignores all events. */
class FailedNode extends Node {
  FailedNode(EarthPosition position) {
    super(position);
  }

  @Override public void onStart(Simulation simulation) {
    // No-op.
  }

  @Override public void onTimerEvent(TimerEvent timerEvent, Simulation simulation) {
    // No-op.
  }

  @Override public void onMessageEvent(MessageEvent messageEvent, Simulation simulation) {
    // No-op.
  }
}

