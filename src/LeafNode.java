public class LeafNode extends NodeBase {
    private String value;

    LeafNode(int key, String value) {
        super(CSMT.hash(value.getBytes()), key);
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
