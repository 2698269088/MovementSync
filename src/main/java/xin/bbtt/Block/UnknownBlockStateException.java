package xin.bbtt.Block;

public class UnknownBlockStateException extends RuntimeException {
    public UnknownBlockStateException(int stateId) {
        super("corresponding block mapping data of StateID not found: " + stateId);
    }
}
