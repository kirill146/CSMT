import java.util.ArrayList;

public class Proof {
    private int key;
    private ArrayList<ProofItem> proof;

    Proof(int key, ArrayList<ProofItem> proof) {
        this.key = key;
        this.proof = proof;
    }

    public int getKey() {
        return key;
    }

    public ArrayList<ProofItem> getProof() {
        return proof;
    }
}
