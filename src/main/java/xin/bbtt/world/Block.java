package xin.bbtt.world;

/**
 * Minecraft方块类 - 移植自MCC
 * 包装方块ID并提供材料类型
 */
public class Block {
    public static final Block AIR = new Block(0);

    private final int blockId;
    private Material material;

    public Block(int blockId) {
        this.blockId = blockId;
        this.material = resolveMaterial(blockId);
    }

    public int getBlockId() {
        return blockId;
    }

    public Material getType() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }

    /**
     * 根据方块ID解析材料类型
     * TODO: 根据实际服务器版本完善映射表
     */
    private Material resolveMaterial(int id) {
        return switch (id) {
            case 0 -> Material.Air;
            case 1 -> Material.Stone;
            case 2 -> Material.GrassBlock;
            case 3 -> Material.Dirt;
            case 8, 9 -> Material.Water;
            case 10, 11 -> Material.Lava;
            case 65 -> Material.Ladder;
            case 106 -> Material.Vine;
            case 51 -> Material.Fire;
            case 81 -> Material.Cactus;
            default -> Material.Unknown;
        };
    }

    /**
     * 从块状态ID创建Block（1.13+）
     */
    public static Block fromStateId(int stateId) {
        // 简化版 - 实际需要根据调色板映射
        return new Block(stateId);
    }
}