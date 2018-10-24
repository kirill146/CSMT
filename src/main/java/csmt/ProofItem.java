package csmt;

public class ProofItem {
    private byte[] hash;
    private char direction;

    ProofItem(byte[] hash, char direction) {
        this.hash = hash;
        this.direction = direction;
    }

    public byte[] getHash() {
        return hash;
    }

    public char getDirection() {
        return direction;
    }
}
