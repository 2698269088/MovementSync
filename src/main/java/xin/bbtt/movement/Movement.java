package xin.bbtt.movement;

public abstract class Movement {
    public abstract void init();
    public abstract void onTick();
    public abstract long getTime();
    public abstract void onStop();
}
