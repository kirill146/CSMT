import java.util.ArrayList;

public class Main {

    private static void getProofAndCheck(CSMT csmt, int k) {
        System.out.println("Test " + k);
        String data = null;
        try {
            data = csmt.getData(k);
        } catch (Exception e) {
            // data == null
        }
        System.out.println("data == " + data);
        ArrayList<Proof> proofs = csmt.getProof(k);
        if (proofs.size() != 1 && proofs.size() != 2) {
            throw new RuntimeException("Unexpected proof format");
        }
        if (proofs.size() == 1) {
            if (CSMT.verifyMembershipProof(csmt.getRootHash(), k, data, proofs.get(0))) {
                System.out.println("Correct membership proof");
            } else {
                System.out.println("Incorrect membership proof");
            }
        } else {
            Proof proof1 = proofs.get(0);
            Proof proof2 = proofs.get(1);
            String data1 = null;
            String data2 = null;
            try {
                data1 = (proof1 == null ? null : csmt.getData(proof1.getKey()));
                data2 = (proof2 == null ? null : csmt.getData(proof2.getKey()));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (CSMT.verifyNonMembershipProof(csmt.getRootHash(), k, data1, data2, proofs)) {
                System.out.println("Correct non membership proof");
            } else {
                System.out.println("Incorrect non membership proof");
            }
        }
        System.out.println("------------------------");
    }

    private static void test1() throws KeyExistsException {
        CSMT csmt = new CSMT();
        csmt.insert(7, "C");
        csmt.insert(3, "B");
        csmt.insert(1, "A");
        csmt.printTree();
        for (int k = 0; k < 10; k++) {
            getProofAndCheck(csmt, k);
        }
    }

    public static void test2() throws KeyExistsException {
        CSMT csmt = new CSMT();
        csmt.insert(1, "1");
        csmt.insert(2, "2");
        csmt.insert(3, "3");
        csmt.insert(4, "4");
        csmt.printTree();
    }

    public static void main(String[] args) throws KeyExistsException {
        test1();
    }
}
