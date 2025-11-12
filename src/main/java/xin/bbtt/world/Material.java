package xin.bbtt.world;

/**
 * Minecraft材料枚举 - 移植自MCC
 * 包含物理模拟所需的所有材料类型
 */
public enum Material {
    // 空气
    Air(false, false, false, false),

    // 石头类
    Stone(true, false, false, false),
    GrassBlock(true, false, false, false),
    Dirt(true, false, false, false),

    // 液体
    Water(false, true, false, false),
    Lava(false, true, false, true),

    // 可攀爬
    Ladder(false, false, true, false),
    Vine(false, false, true, false),
    Scaffolding(false, false, true, false),
    TwistingVines(false, false, true, false),
    TwistingVinesPlant(false, false, true, false),
    WeepingVines(false, false, true, false),
    WeepingVinesPlant(false, false, true, false),

    // 危险
    Fire(false, false, false, true),
    Cactus(true, false, false, true),

    // 半砖（需特殊处理）
    StoneSlab(true, false, false, false),
    OakSlab(true, false, false, false),

    // 其他
    Unknown(true, false, false, false);

    private final boolean solid;
    private final boolean liquid;
    private final boolean climbable;
    private final boolean harmful;

    Material(boolean solid, boolean liquid, boolean climbable, boolean harmful) {
        this.solid = solid;
        this.liquid = liquid;
        this.climbable = climbable;
        this.harmful = harmful;
    }

    public boolean isSolid() {
        return solid;
    }

    public boolean isLiquid() {
        return liquid;
    }

    public boolean canBeClimbedOn() {
        return climbable;
    }

    public boolean canHarmPlayers() {
        return harmful;
    }
}