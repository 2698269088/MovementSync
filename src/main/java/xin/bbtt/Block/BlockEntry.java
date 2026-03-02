package xin.bbtt.Block;

import lombok.Data;
import java.util.List;

@Data
public class BlockEntry {
    private int id;
    private String name;
    private int minStateId;
    private int maxStateId;
    private int defaultState;
    private List<StateProperty> states;

    @Data
    static
    class StateProperty {
        private String name;
        private String type;
        private int num_values;
        private List<String> values;

        public String getValueAt(int index) {
            if ("bool".equals(type)) {
                return index == 0 ? "true" : "false";
            }
            if (values != null && index < values.size()) {
                return values.get(index);
            }
            return String.valueOf(index);
        }
    }
}
