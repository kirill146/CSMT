import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {

    public static void getProofAndCheck(CSMT csmt, int k) {
        System.out.println("Test " + k);
        String data = null;
        try {
            data = csmt.getData(k);
        } catch (Exception e) {
            // data == null
        }
        System.out.println("data == " + data);
        ArrayList<Proof> proof = csmt.getProof(k);
        if (proof.size() != 1 && proof.size() != 2) {
            throw new RuntimeException("Unexpected proof format");
        }
        if (CSMT.verifyProof(data, proof, csmt.getRootHash())) {
            System.out.print("Correct ");
        } else {
            System.out.print("Incorrect ");
        }
        if (proof.size() == 1) {
            System.out.println("membership proof");
        } else { // proof.size() == 2
            System.out.println("non membership proof");
        }
        System.out.println("------------------------");
    }

    public static void main(String[] args) throws Exception { // todo: handle exception
        CSMT csmt = new CSMT();
        csmt.insert(7, "C");
        csmt.insert(3, "B");
        csmt.insert(1, "A");
        csmt.printTree();
        for (int k = 0; k < 10; k++) {
            getProofAndCheck(csmt, k);
        }
    }
}
