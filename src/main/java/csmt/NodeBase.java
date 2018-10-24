package csmt;

public abstract class NodeBase {
    private byte[] hash;
    private int key;

    NodeBase(byte[] hash, int key) {
        this.hash = hash;
        this.key = key;
    }

    public byte[] getHash() {
        return hash;
    }

    public int getKey() {
        return key;
    }
}
