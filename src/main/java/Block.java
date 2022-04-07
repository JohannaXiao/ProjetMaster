import java.util.List;

public class Block {
    private int index;

    /**
     * 前一个区块的hash值
     */
    private String previousHash;

    public void setIndex(int index) {
        this.index = index;
    }



    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    /**
     * 生成区块的时间戳
     */
    private long timestamp;

    public int getIndex() {
        return index;
    }



    public String getPreviousHash() {
        return previousHash;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getNonce() {
        return nonce;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    /**
     * 工作量证明，计算正确hash值的次数
     */
    private int nonce;
    /**
     * 当前区块存储的业务数据集合（例如转账交易信息、票据信息、合同信息等）
     */
    private List<Transaction> transactions;

    @Override
    public String toString() {
        return String.valueOf(index) + "&" + String.valueOf(timestamp)+ "&" + previousHash;
    }
}
