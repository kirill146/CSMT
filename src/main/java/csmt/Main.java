package csmt;

import java.util.*;

public class Main {

    private static void getProofAndCheck(CSMT csmt, int k) {
        System.out.println("Test " + k);
        String data = csmt.getData(k);
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

    private static void test3() throws KeyExistsException {
        CSMT csmt = new CSMT();
        Map<Integer, String> map = new HashMap<>();
        Random random = new Random(4);
        for (int i = 0; i < 10000; i++) {
            int op = random.nextInt(3);
            if (op == 0 || map.size() == 0) {
                // insert
                int key;
                do {
                    key = random.nextInt(Integer.MAX_VALUE);
                } while (map.containsKey(key));
                String val = Integer.toString(key);
                map.put(key, val);
                csmt.insert(key, val);
            } else if (op == 1) {
                // verify membership proof

                int k = random.nextInt(map.size());
                Iterator it = map.entrySet().iterator();
                for (int j = 0; j < k + 1; j++) {
                    it.hasNext();
                }
                k = (int) ((Map.Entry)it.next()).getKey();
                getProofAndCheck(csmt, k);
            } else if (op == 2) {
                // verify non membership proof
                int key;
                do {
                    key = random.nextInt(Integer.MAX_VALUE);
                } while (map.containsKey(key));
                getProofAndCheck(csmt, key);
            }
        }
    }

    public static void main(String[] args) throws KeyExistsException {
        test3();
    }
}
