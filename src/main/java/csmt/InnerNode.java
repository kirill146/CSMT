package csmt;

public class InnerNode extends NodeBase {
    private NodeBase left;
    private NodeBase right;

    InnerNode(NodeBase left, NodeBase right) {
        super(CSMT.combinedHash(left.getHash(), right.getHash()), Math.max(left.getKey(), right.getKey()));
        this.left = left;
        this.right = right;
    }

    public NodeBase getLeft() {
        return left;
    }

    public NodeBase getRight() {
        return right;
    }

    public void setLeft(NodeBase left) {
        this.left = left;
    }

    public void setRight(NodeBase right) {
        this.right = right;
    }
}
