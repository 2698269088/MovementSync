package xin.bbtt.utils;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;
import java.util.Optional;

/**
 * 轴对齐包围盒(Axis-Aligned Bounding Box)
 * 移植自原版MC的AABB实现，用于精确的碰撞检测
 */
public class AABB {
    private static final double EPSILON = 1.0E-7;
    public final double minX;
    public final double minY;
    public final double minZ;
    public final double maxX;
    public final double maxY;
    public final double maxZ;

    public AABB(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        this.minX = Math.min(minX, maxX);
        this.minY = Math.min(minY, maxY);
        this.minZ = Math.min(minZ, maxZ);
        this.maxX = Math.max(minX, maxX);
        this.maxY = Math.max(minY, maxY);
        this.maxZ = Math.max(minZ, maxZ);
    }

    public AABB(Vector3dc min, Vector3dc max) {
        this(min.x(), min.y(), min.z(), max.x(), max.y(), max.z());
    }

    /**
     * 创建单位立方体包围盒
     */
    public static AABB unitCubeFromLowerCorner(Vector3dc pos) {
        return new AABB(pos.x(), pos.y(), pos.z(), pos.x() + 1.0, pos.y() + 1.0, pos.z() + 1.0);
    }

    /**
     * 包围完整方块的AABB
     */
    public static AABB ofBlock(double x, double y, double z) {
        return new AABB(x, y, z, x + 1.0, y + 1.0, z + 1.0);
    }

    /**
     * 玩家碰撞箱
     */
    public static AABB playerBoundingBox(Vector3dc position) {
        // 玩家碰撞箱尺寸：0.6宽 x 1.8高 x 0.6深
        double width = 0.3; // 半宽
        double height = 0.9; // 半高
        return new AABB(
            position.x() - width, position.y(), position.z() - width,
            position.x() + width, position.y() + height * 2, position.z() + width
        );
    }

    /**
     * 移动包围盒
     */
    public AABB move(double xa, double ya, double za) {
        return new AABB(this.minX + xa, this.minY + ya, this.minZ + za, 
                       this.maxX + xa, this.maxY + ya, this.maxZ + za);
    }

    public AABB move(Vector3dc delta) {
        return move(delta.x(), delta.y(), delta.z());
    }

    /**
     * 扩展包围盒
     */
    public AABB expandTowards(double xa, double ya, double za) {
        double newMinX = this.minX;
        double newMinY = this.minY;
        double newMinZ = this.minZ;
        double newMaxX = this.maxX;
        double newMaxY = this.maxY;
        double newMaxZ = this.maxZ;
        
        if (xa < 0.0) {
            newMinX += xa;
        } else if (xa > 0.0) {
            newMaxX += xa;
        }

        if (ya < 0.0) {
            newMinY += ya;
        } else if (ya > 0.0) {
            newMaxY += ya;
        }

        if (za < 0.0) {
            newMinZ += za;
        } else if (za > 0.0) {
            newMaxZ += za;
        }

        return new AABB(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }

    /**
     * 膨胀包围盒
     */
    public AABB inflate(double xAdd, double yAdd, double zAdd) {
        return new AABB(
            this.minX - xAdd, this.minY - yAdd, this.minZ - zAdd,
            this.maxX + xAdd, this.maxY + yAdd, this.maxZ + zAdd
        );
    }

    public AABB inflate(double amount) {
        return inflate(amount, amount, amount);
    }

    /**
     * 缩小包围盒
     */
    public AABB deflate(double xSubtract, double ySubtract, double zSubtract) {
        return inflate(-xSubtract, -ySubtract, -zSubtract);
    }

    public AABB deflate(double amount) {
        return inflate(-amount);
    }

    /**
     * 检查是否与其他AABB相交
     */
    public boolean intersects(AABB other) {
        return this.intersects(other.minX, other.minY, other.minZ, 
                              other.maxX, other.maxY, other.maxZ);
    }

    public boolean intersects(double minX, double minY, double minZ, 
                             double maxX, double maxY, double maxZ) {
        return this.minX < maxX && this.maxX > minX &&
               this.minY < maxY && this.maxY > minY &&
               this.minZ < maxZ && this.maxZ > minZ;
    }

    /**
     * 检查是否包含指定点
     */
    public boolean contains(Vector3dc point) {
        return contains(point.x(), point.y(), point.z());
    }

    public boolean contains(double x, double y, double z) {
        return x >= this.minX && x < this.maxX &&
               y >= this.minY && y < this.maxY &&
               z >= this.minZ && z < this.maxZ;
    }

    /**
     * 获取交集
     */
    public AABB intersect(AABB other) {
        double newMinX = Math.max(this.minX, other.minX);
        double newMinY = Math.max(this.minY, other.minY);
        double newMinZ = Math.max(this.minZ, other.minZ);
        double newMaxX = Math.min(this.maxX, other.maxX);
        double newMaxY = Math.min(this.maxY, other.maxY);
        double newMaxZ = Math.min(this.maxZ, other.maxZ);
        return new AABB(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }

    /**
     * 获取并集
     */
    public AABB minmax(AABB other) {
        double newMinX = Math.min(this.minX, other.minX);
        double newMinY = Math.min(this.minY, other.minY);
        double newMinZ = Math.min(this.minZ, other.minZ);
        double newMaxX = Math.max(this.maxX, other.maxX);
        double newMaxY = Math.max(this.maxY, other.maxY);
        double newMaxZ = Math.max(this.maxZ, other.maxZ);
        return new AABB(newMinX, newMinY, newMinZ, newMaxX, newMaxY, newMaxZ);
    }

    /**
     * 检查沿向量移动是否会与任何AABB碰撞
     */
    public boolean collidedAlongVector(Vector3dc vector, List<AABB> aabbs) {
        Vector3d from = getCenter();
        Vector3d to = new Vector3d(from).add(vector);

        for (AABB shapePart : aabbs) {
            // 膨胀目标AABB以考虑当前AABB的大小
            AABB inflated = shapePart.inflate(
                this.getXsize() * 0.5 - EPSILON,
                this.getYsize() * 0.5 - EPSILON,
                this.getZsize() * 0.5 - EPSILON
            );
            
            if (inflated.contains(to) || inflated.contains(from)) {
                return true;
            }

            if (clip(from, to).isPresent()) {
                return true;
            }
        }

        return false;
    }

    /**
     * 射线与AABB的交点计算
     */
    public Optional<Vector3d> clip(Vector3dc from, Vector3dc to) {
        return clip(this.minX, this.minY, this.minZ, this.maxX, this.maxY, this.maxZ, from, to);
    }

    public static Optional<Vector3d> clip(double minX, double minY, double minZ,
                                         double maxX, double maxY, double maxZ,
                                         Vector3dc from, Vector3dc to) {
        double[] scaleReference = new double[]{1.0};
        double dx = to.x() - from.x();
        double dy = to.y() - from.y();
        double dz = to.z() - from.z();

        // X轴检测
        if (dx > EPSILON) {
            double s = (minX - from.x()) / dx;
            if (s >= 0.0 && s <= scaleReference[0]) {
                double py = from.y() + s * dy;
                double pz = from.z() + s * dz;
                if (minY - EPSILON <= py && py <= maxY + EPSILON &&
                    minZ - EPSILON <= pz && pz <= maxZ + EPSILON) {
                    scaleReference[0] = s;
                }
            }
        } else if (dx < -EPSILON) {
            double s = (maxX - from.x()) / dx;
            if (s >= 0.0 && s <= scaleReference[0]) {
                double py = from.y() + s * dy;
                double pz = from.z() + s * dz;
                if (minY - EPSILON <= py && py <= maxY + EPSILON &&
                    minZ - EPSILON <= pz && pz <= maxZ + EPSILON) {
                    scaleReference[0] = s;
                }
            }
        }

        // Y轴检测
        if (dy > EPSILON) {
            double s = (minY - from.y()) / dy;
            if (s >= 0.0 && s <= scaleReference[0]) {
                double px = from.x() + s * dx;
                double pz = from.z() + s * dz;
                if (minX - EPSILON <= px && px <= maxX + EPSILON &&
                    minZ - EPSILON <= pz && pz <= maxZ + EPSILON) {
                    scaleReference[0] = s;
                }
            }
        } else if (dy < -EPSILON) {
            double s = (maxY - from.y()) / dy;
            if (s >= 0.0 && s <= scaleReference[0]) {
                double px = from.x() + s * dx;
                double pz = from.z() + s * dz;
                if (minX - EPSILON <= px && px <= maxX + EPSILON &&
                    minZ - EPSILON <= pz && pz <= maxZ + EPSILON) {
                    scaleReference[0] = s;
                }
            }
        }

        // Z轴检测
        if (dz > EPSILON) {
            double s = (minZ - from.z()) / dz;
            if (s >= 0.0 && s <= scaleReference[0]) {
                double px = from.x() + s * dx;
                double py = from.y() + s * dy;
                if (minX - EPSILON <= px && px <= maxX + EPSILON &&
                    minY - EPSILON <= py && py <= maxY + EPSILON) {
                    scaleReference[0] = s;
                }
            }
        } else if (dz < -EPSILON) {
            double s = (maxZ - from.z()) / dz;
            if (s >= 0.0 && s <= scaleReference[0]) {
                double px = from.x() + s * dx;
                double py = from.y() + s * dy;
                if (minX - EPSILON <= px && px <= maxX + EPSILON &&
                    minY - EPSILON <= py && py <= maxY + EPSILON) {
                    scaleReference[0] = s;
                }
            }
        }

        if (scaleReference[0] < 1.0) {
            double s = scaleReference[0];
            return Optional.of(new Vector3d(
                from.x() + s * dx,
                from.y() + s * dy,
                from.z() + s * dz
            ));
        }

        return Optional.empty();
    }

    /**
     * 获取尺寸
     */
    public double getXsize() {
        return this.maxX - this.minX;
    }

    public double getYsize() {
        return this.maxY - this.minY;
    }

    public double getZsize() {
        return this.maxZ - this.minZ;
    }

    /**
     * 获取中心点
     */
    public Vector3d getCenter() {
        return new Vector3d(
            (this.minX + this.maxX) * 0.5,
            (this.minY + this.maxY) * 0.5,
            (this.minZ + this.maxZ) * 0.5
        );
    }

    /**
     * 获取底部中心点
     */
    public Vector3d getBottomCenter() {
        return new Vector3d(
            (this.minX + this.maxX) * 0.5,
            this.minY,
            (this.minZ + this.maxZ) * 0.5
        );
    }

    /**
     * 获取最小坐标点
     */
    public Vector3d getMinPosition() {
        return new Vector3d(this.minX, this.minY, this.minZ);
    }

    /**
     * 获取最大坐标点
     */
    public Vector3d getMaxPosition() {
        return new Vector3d(this.maxX, this.maxY, this.maxZ);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof AABB)) return false;
        AABB other = (AABB) obj;
        return Double.compare(this.minX, other.minX) == 0 &&
               Double.compare(this.minY, other.minY) == 0 &&
               Double.compare(this.minZ, other.minZ) == 0 &&
               Double.compare(this.maxX, other.maxX) == 0 &&
               Double.compare(this.maxY, other.maxY) == 0 &&
               Double.compare(this.maxZ, other.maxZ) == 0;
    }

    @Override
    public int hashCode() {
        long bits = Double.doubleToLongBits(minX);
        bits = 31 * bits + Double.doubleToLongBits(minY);
        bits = 31 * bits + Double.doubleToLongBits(minZ);
        bits = 31 * bits + Double.doubleToLongBits(maxX);
        bits = 31 * bits + Double.doubleToLongBits(maxY);
        bits = 31 * bits + Double.doubleToLongBits(maxZ);
        return (int) (bits ^ (bits >>> 32));
    }

    @Override
    public String toString() {
        return String.format("AABB[%.3f, %.3f, %.3f] -> [%.3f, %.3f, %.3f]", 
                           minX, minY, minZ, maxX, maxY, maxZ);
    }

    /**
     * 检查是否包含NaN值
     */
    public boolean hasNaN() {
        return Double.isNaN(minX) || Double.isNaN(minY) || Double.isNaN(minZ) ||
               Double.isNaN(maxX) || Double.isNaN(maxY) || Double.isNaN(maxZ);
    }
}