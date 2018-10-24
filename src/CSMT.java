import javafx.util.Pair;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

public class CSMT {
    private NodeBase root;

    private static int log2(int n) {
        return n == 0 ? -1 : 31 - Integer.numberOfLeadingZeros(n);
    }

    private static int distance(int x, int y) {
        return log2(x ^ y);
    }

    public void insert(int key, String val) throws KeyExistsException {
        //System.out.println("csmt.insert(" + key + ", \"" + val + "\");");
        root = insert(root, key, val);
    }

    private NodeBase insert(NodeBase rootNode, int key, String val) throws KeyExistsException {
        if (rootNode == null) {
            return new LeafNode(key, val);
        }

        if (rootNode instanceof InnerNode) {
            InnerNode root = (InnerNode) rootNode;
            int lDist = distance(key, root.getLeft().getKey());
            int rDist = distance(key, root.getRight().getKey());

            if (lDist == rDist) {
                LeafNode newLeaf = new LeafNode(key, val);

                if (key < Math.min(root.getLeft().getKey(), root.getRight().getKey())) {
                    return new InnerNode(newLeaf, root);
                } else {
                    return new InnerNode(root, newLeaf);
                }
            } else {
                if (lDist < rDist) {
                    root.setLeft(insert(root.getLeft(), key, val));
                } else { // rDist < lDist
                    root.setRight(insert(root.getRight(), key, val));
                }
                return new InnerNode(root.getLeft(), root.getRight());
            }
        }

        // root node is leaf node
        LeafNode newLeaf = new LeafNode(key, val);

        if (key == rootNode.getKey()) {
            throw new KeyExistsException();
        } else if (key > rootNode.getKey()) {
            return new InnerNode(rootNode, newLeaf);
        }
        // key < rootNode.getKey()
        return new InnerNode(newLeaf, rootNode);
    }

    public void delete(int k) {
        root = delete(root, k);
    }

    private NodeBase delete(NodeBase rootNode, int k) {
        if (rootNode instanceof LeafNode) {
            if (rootNode.getKey() == k) {
                return null;
            }
            return rootNode; // Key does not exists
        }
        InnerNode root = (InnerNode) rootNode;
        if (root.getLeft() instanceof LeafNode && root.getLeft().getKey() == k) {
            return root.getRight();
        } else if (root.getRight() instanceof LeafNode && root.getRight().getKey() == k) {
            return root.getLeft();
        }

        int lDist = distance(k, root.getLeft().getKey());
        int rDist = distance(k, root.getRight().getKey());

        if (lDist == rDist) {
            // Key does not exists
            return rootNode;
        }
        if (lDist < rDist) {
            root.setLeft(delete(root.getLeft(), k));
        } else { // rDist < lDist
            root.setRight(delete(root.getRight(), k));
        }
        return new InnerNode(root.getLeft(), root.getRight());
    }

    public ArrayList<Proof> getProof(int k) {
        ArrayList<Proof> ans = new ArrayList<>();
        Pair<Integer, Integer> res = getBoundKeys(null, 'x', root, k);
        ArrayList<ProofItem> res1 = new ArrayList<>();
        if (res.getValue() == null) {
            // non membership edge case 1: when the key is greater than the largest element in the tree
            getProof(res1, null, 'x', root, res.getKey());
            ans.add(new Proof(res.getKey(), res1));
            ans.add(null);
        } else if (res.getKey() == null) {
            // non membership edge case 2: when the key is smaller than the smallest element in the tree
            ans.add(null);
            getProof(res1, null, 'x', root, res.getValue());
            ans.add(new Proof(res.getValue(), res1));
        } else if (res.getKey().equals(res.getValue())) {
            // membership proof
            getProof(res1, null, 'x', root, k);
            ans.add(new Proof(k, res1));
        } else {
            // non membership proof (with two bound keys)
            getProof(res1, null, 'x', root, res.getKey());
            ArrayList<ProofItem> res2 = new ArrayList<>();
            getProof(res2, null, 'x', root, res.getValue());
            ans.add(new Proof(res.getKey(), res1));
            ans.add(new Proof(res.getValue(), res2));
        }
        return ans;
    }

    private void getProof(ArrayList<ProofItem> res, NodeBase sibling, char direction, NodeBase rootNode, int k) {
        if (rootNode instanceof LeafNode) {
            if (rootNode != this.root) {
                res.add(new ProofItem(sibling.getHash(), reverse(direction)));
            }
            return;
        }
        InnerNode root = (InnerNode) rootNode;
        if (distance(k, root.getLeft().getKey()) < distance(k, root.getRight().getKey())) {
            getProof(res, root.getRight(), 'l', root.getLeft(), k);
        } else {
            getProof(res, root.getLeft(), 'r', root.getRight(), k);
        }
        if (rootNode != this.root) {
            res.add(new ProofItem(sibling.getHash(), reverse(direction)));
        }
    }

    private Pair<Integer, Integer> getBoundKeys(NodeBase sibling, char direction, NodeBase rootNode, int k) {
        if (rootNode instanceof LeafNode) {
            if (rootNode.getKey() == k) {
                return new Pair<>(k, k);
            } else {
                // non membership proof
                return getNonMembershipBoundKeys(k, rootNode.getKey(), direction, sibling, rootNode);
            }
        }
        InnerNode root = (InnerNode) rootNode;
        if (root.getLeft().getKey() < k && k < minInSubtree(root.getRight())) {
            return new Pair<>(root.getLeft().getKey(), minInSubtree(root.getRight()));
        }
        Pair<Integer, Integer> res;
        if (root.getLeft().getKey() < k) {
            res = getBoundKeys(root.getLeft(), 'r', root.getRight(), k);
        } else {
            res = getBoundKeys(root.getRight(), 'l', root.getLeft(), k);
        }

        if (res.getValue() == null && direction == 'l') {
            return new Pair<>(res.getKey(), minInSubtree(sibling));
        }
        if (res.getKey() == null && direction == 'r') {
            return new Pair<>(sibling.getKey(), res.getValue());
        }
        return res;
    }

    private Pair<Integer, Integer> getNonMembershipBoundKeys(int k, int key, char direction, NodeBase sibling, NodeBase cur) {
        if (k > key) {
            if (direction == 'l') {
                return new Pair<>(key, minInSubtree(sibling));
            }
            return new Pair<>(key, null);
        }
        if (direction == 'l' || direction == 'x') {
            return new Pair<>(null, key);
        }
        //return new Pair<>(sibling.getKey(), key);
        return new Pair<>(sibling.getKey(), minInSubtree(cur));
    }

    private int minInSubtree(NodeBase root) {
        if (root instanceof LeafNode) {
            return root.getKey();
        }
        return minInSubtree(((InnerNode) root).getLeft());
    }

    private static char reverse(char direction) {
        return direction == 'l' ? 'r' : 'l';
    }

    public void printTree() {
        printTree(root, new StringBuilder());
    }

    private void printTree(NodeBase root, StringBuilder path) {
        if (root instanceof InnerNode) {
            printTree(((InnerNode) root).getLeft(), path.append('l'));
            path.deleteCharAt(path.length() - 1);
            printTree(((InnerNode) root).getRight(), path.append('r'));
            path.deleteCharAt(path.length() - 1);
        } else {
            System.out.println(path + ": " + ((LeafNode) root).getValue());
        }
    }

    public static byte[] hash(byte[] value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value == null ? new byte[0] : value);
        } catch (NoSuchAlgorithmException e) {
            // unreachable
            e.printStackTrace();
        }
        throw new RuntimeException("Unreachable statement");
    }

    public static byte[] hash(String value) {
        return hash(value == null ? null : value.getBytes());
    }

    public static byte[] combinedHash(byte[] x, byte[] y) {
        byte[] a = new byte[x.length + y.length];
        System.arraycopy(x, 0, a, 0, x.length);
        System.arraycopy(y, 0, a, x.length, y.length);
        return hash(a);
    }

    public static boolean verifyMembershipProof(byte[] rootHash, int key, String data, Proof proof) {
        if (key != proof.getKey()) {
            return false;
        }
        byte[] curHash = hash(data);
        for (ProofItem item : proof.getProof()) {
            if (item.getDirection() == 'l') {
                curHash = combinedHash(item.getHash(), curHash);
            } else if (item.getDirection() == 'r') {
                curHash = combinedHash(curHash, item.getHash());
            } else { // unexpected direction character (neither 'l' nor 'r')
                return false;
            }
        }
        return Arrays.equals(curHash, rootHash);
    }

    public static boolean verifyNonMembershipProof(byte[] rootHash, int key, String data1, String data2, ArrayList<Proof> proofs) {
        if (proofs.size() != 2) {
            return false;
        }
        Proof proof1 = proofs.get(0);
        Proof proof2 = proofs.get(1);

        if (proof1 != null) {
            if (!verifyMembershipProof(rootHash, proof1.getKey(), data1, proof1)) {
                return false;
            }
        }
        if (proof2 != null) {
            if (!verifyMembershipProof(rootHash, proof2.getKey(), data2, proof2)) {
                return false;
            }
        }
        if (proof1 == null) {
            if (proof2 == null || key >= proof2.getKey()) {
                return false;
            }
            // the most left element
            for (ProofItem item : proof2.getProof()) {
                if (item.getDirection() != 'r') {
                    return false;
                }
            }
            return true;
        }
        if (proof2 == null) {
            if (key <= proof1.getKey()) {
                return false;
            }
            // the most right element
            for (ProofItem item : proof1.getProof()) {
                if (item.getDirection() != 'l') {
                    return false;
                }
            }
            return true;
        }
        if (key <= proof1.getKey() || key >= proof2.getKey()) {
            return false;
        }
        int posR = 0;
        while (posR < proof1.getProof().size() && proof1.getProof().get(posR).getDirection() != 'r') {
            posR++;
        }
        int posL = 0;
        while (posL < proof2.getProof().size() && proof2.getProof().get(posL).getDirection() != 'l') {
            posL++;
        }
        if (posR == proof1.getProof().size()
                || posL == proof2.getProof().size()
                || proof1.getProof().size() - posR != proof2.getProof().size() - posL) {
            return false;
        }
        for (int i = 1; i < proof1.getProof().size() - posR; i++) {
            if (!Arrays.equals(proof1.getProof().get(posR + i).getHash(), proof2.getProof().get(posL + i).getHash())) {
                return false;
            }
        }
        return true;
    }

    public byte[] getRootHash() {
        byte res[] = new byte[root.getHash().length];
        System.arraycopy(root.getHash(), 0, res, 0, res.length);
        return res;
    }

    public String getData(int k) {
        //System.out.println("csmt.get(" + k + ");");
        return getData(root, k);
    }

    private String getData(NodeBase rootNode, int k) {
        if (rootNode instanceof LeafNode) {
            if (rootNode.getKey() == k) {
                return ((LeafNode) rootNode).getValue();
            }
            return null; // Key does not exists
        }

        InnerNode root = (InnerNode) rootNode;
        int lDist = distance(k, root.getLeft().getKey());
        int rDist = distance(k, root.getRight().getKey());

        if (lDist == rDist) {
            return null; // Key does not exists
            //return root.getLeft().getKey() == k ? getData(root.getLeft(), k) :
            //        root.getRight().getKey() == k ? getData(root.getRight(), k) :
            //                null;
        }
        if (lDist < rDist) {
            return getData(root.getLeft(), k);
        }
        // rDist < lDist
        return getData(root.getRight(), k);
    }
}
