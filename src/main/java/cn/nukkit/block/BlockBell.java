package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.api.DeprecationDetails;
import cn.nukkit.api.PowerNukkitOnly;
import cn.nukkit.api.Since;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityBell;
import cn.nukkit.blockproperty.ArrayBlockProperty;
import cn.nukkit.blockproperty.BlockProperties;
import cn.nukkit.blockproperty.BlockProperty;
import cn.nukkit.blockproperty.value.BellAttachmentType;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.entity.projectile.EntityArrow;
import cn.nukkit.event.block.BellRingEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.ItemTool;
import cn.nukkit.level.Level;
import cn.nukkit.level.Position;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.SimpleAxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.BlockColor;
import cn.nukkit.utils.Faceable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static cn.nukkit.blockproperty.CommonBlockProperties.DIRECTION;
import static cn.nukkit.blockproperty.CommonBlockProperties.TOGGLE;

@PowerNukkitOnly
public class BlockBell extends BlockTransparentMeta implements Faceable, BlockEntityHolder<BlockEntityBell> {
    @PowerNukkitOnly
    @Since("1.4.0.0-PN")
    public static final BlockProperty<BellAttachmentType> ATTACHMENT_TYPE = new ArrayBlockProperty<>("attachment", false, BellAttachmentType.class);

    @PowerNukkitOnly
    @Since("1.4.0.0-PN")
    public static final BlockProperties PROPERTIES = new BlockProperties(DIRECTION, ATTACHMENT_TYPE, TOGGLE);

    @PowerNukkitOnly
    @Deprecated
    @DeprecationDetails(since = "1.4.0.0-PN", by = "PowerNukkit",
        reason = "Magic values", replaceWith = "BellAttachmentType.STANDING")
    public static final int TYPE_ATTACHMENT_STANDING = 0;
    
    @PowerNukkitOnly
    @Deprecated
    @DeprecationDetails(since = "1.4.0.0-PN", by = "PowerNukkit",
            reason = "Magic values", replaceWith = "BellAttachmentType.HANGING")
    public static final int TYPE_ATTACHMENT_HANGING = 1;
    
    @PowerNukkitOnly
    @Deprecated
    @DeprecationDetails(since = "1.4.0.0-PN", by = "PowerNukkit",
            reason = "Magic values", replaceWith = "BellAttachmentType.SIDE")
    public static final int TYPE_ATTACHMENT_SIDE = 2;
    
    @PowerNukkitOnly
    @Deprecated
    @DeprecationDetails(since = "1.4.0.0-PN", by = "PowerNukkit",
            reason = "Magic values", replaceWith = "BellAttachmentType.MULTIPLE")
    public static final int TYPE_ATTACHMENT_MULTIPLE = 3;

    public BlockBell() {
        this(0);
    }

    public BlockBell(int meta) {
        super(meta);
    }

    @Override
    public String getName() {
        return "Bell";
    }

    @Override
    public int getId() {
        return BELL;
    }

    @Since("1.4.0.0-PN")
    @PowerNukkitOnly
    @Nonnull
    @Override
    public BlockProperties getProperties() {
        return PROPERTIES;
    }

    @Since("1.4.0.0-PN")
    @PowerNukkitOnly
    @Nonnull
    @Override
    public Class<? extends BlockEntityBell> getBlockEntityClass() {
        return BlockEntityBell.class;
    }

    @PowerNukkitOnly
    @Since("1.4.0.0-PN")
    @Nonnull
    @Override
    public String getBlockEntityType() {
        return BlockEntity.BELL;
    }

    private boolean isConnectedTo(BlockFace connectedFace, BellAttachmentType attachmentType, BlockFace blockFace) {
        BlockFace.Axis faceAxis = connectedFace.getAxis();
        switch (attachmentType) {
            case STANDING:
                if (faceAxis == BlockFace.Axis.Y) {
                    return connectedFace == BlockFace.DOWN;
                } else {
                    return blockFace.getAxis() != faceAxis;
                }
            case HANGING:
                return connectedFace == BlockFace.UP;
            case SIDE:
                return connectedFace == blockFace.getOpposite();
            case MULTIPLE:
                return connectedFace == blockFace || connectedFace == blockFace.getOpposite();
            default:
        }
        return false;
    }

    @Override
    protected AxisAlignedBB recalculateBoundingBox() {
        BellAttachmentType attachmentType = getBellAttachmentType();
        BlockFace blockFace = getBlockFace();
        boolean north = this.isConnectedTo(BlockFace.NORTH, attachmentType, blockFace);
        boolean south = this.isConnectedTo(BlockFace.SOUTH, attachmentType, blockFace);
        boolean west = this.isConnectedTo(BlockFace.WEST, attachmentType, blockFace);
        boolean east = this.isConnectedTo(BlockFace.EAST, attachmentType, blockFace);
        boolean up = this.isConnectedTo(BlockFace.UP, attachmentType, blockFace);
        boolean down = this.isConnectedTo(BlockFace.DOWN, attachmentType, blockFace);

        double n = north ? 0 : 0.25;
        double s = south ? 1 : 0.75;
        double w = west ? 0 : 0.25;
        double e = east ? 1 : 0.75;
        double d = down ? 0 : 0.25;
        double u = up ? 1 : 0.75;

        return new SimpleAxisAlignedBB(
                this.x + w,
                this.y + d,
                this.z + n,
                this.x + e,
                this.y + u,
                this.z + s
        );
    }

    @Override
    public void onEntityCollide(Entity entity) {
        if (entity instanceof EntityItem && entity.positionChanged) {
            AxisAlignedBB boundingBox = entity.getBoundingBox();
            AxisAlignedBB blockBoundingBox = this.getCollisionBoundingBox();
            if (boundingBox.intersectsWith(blockBoundingBox)) {
                Vector3 entityCenter = new Vector3(
                        (boundingBox.getMaxX() - boundingBox.getMinX()) / 2,
                        (boundingBox.getMaxY() - boundingBox.getMinY()) / 2,
                        (boundingBox.getMaxZ() - boundingBox.getMinZ()) / 2
                );

                Vector3 blockCenter = new Vector3(
                        (blockBoundingBox.getMaxX() - blockBoundingBox.getMinX()) / 2,
                        (blockBoundingBox.getMaxY() - blockBoundingBox.getMinY()) / 2,
                        (blockBoundingBox.getMaxZ() - blockBoundingBox.getMinZ()) / 2
                );
                Vector3 entityPos = entity.add(entityCenter);
                Vector3 blockPos = this.add(
                        blockBoundingBox.getMinX() - x + blockCenter.x,
                        blockBoundingBox.getMinY() - y + blockCenter.y,
                        blockBoundingBox.getMinZ() - z + blockCenter.z
                );

                Vector3 entityVector = entityPos.subtract(blockPos);
                entityVector = entityVector.normalize().multiply(0.4);
                entityVector.y = Math.max(0.15, entityVector.y);
                if(ring(entity, BellRingEvent.RingCause.DROPPED_ITEM)) {
                    entity.setMotion(entityVector);
                }
            }
        }
    }

    @Override
    public boolean hasEntityCollision() {
        return true;
    }

    @Override
    protected AxisAlignedBB recalculateCollisionBoundingBox() {
        return recalculateBoundingBox().expand(0.000001, 0.000001, 0.000001);
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public boolean onActivate(@Nonnull Item item, Player player) {
        return ring(player, player != null? BellRingEvent.RingCause.HUMAN_INTERACTION : BellRingEvent.RingCause.UNKNOWN);
    }

    public boolean ring(Entity causeEntity, BellRingEvent.RingCause cause) {
        return ring(causeEntity, cause, null);
    }

    public boolean ring(Entity causeEntity, BellRingEvent.RingCause cause, BlockFace hitFace) {
        BlockEntityBell bell = getOrCreateBlockEntity();
        boolean addException = true;
        BlockFace blockFace = getBlockFace();
        if (hitFace == null) {
            if (causeEntity != null) {
                if (causeEntity instanceof EntityItem) {
                    Position blockMid = add(0.5, 0.5, 0.5);
                    Vector3 vector = causeEntity.subtract(blockMid).normalize();
                    int x = vector.x < 0? -1 : vector.x > 0? 1 : 0;
                    int z = vector.z < 0? -1 : vector.z > 0? 1 : 0;
                    if (x != 0 && z != 0) {
                        if (Math.abs(vector.x) < Math.abs(vector.z)) {
                            x = 0;
                        } else {
                            z = 0;
                        }
                    }
                    hitFace = blockFace;
                    for (BlockFace face : BlockFace.values()) {
                        if (face.getXOffset() == x && face.getZOffset() == z) {
                            hitFace = face;
                            break;
                        }
                    }
                } else {
                    hitFace = causeEntity.getDirection();
                }
            } else {
                hitFace = blockFace;
            }
        }
        switch (getBellAttachmentType()) {
            case STANDING:
                if (hitFace.getAxis() != blockFace.getAxis()) {
                    return false;
                }
                break;
            case MULTIPLE:
                if (hitFace.getAxis() == blockFace.getAxis()) {
                    return false;
                }
                break;
            case SIDE:
                if (hitFace.getAxis() == blockFace.getAxis()) {
                    addException = false;
                }
                break;
            default:
        }

        BellRingEvent event = new BellRingEvent(this, cause, causeEntity);
        this.level.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        bell.setDirection(hitFace.getOpposite().getHorizontalIndex());
        bell.setTicks(0);
        bell.setRinging(true);
        if (addException && causeEntity instanceof Player) {
            bell.spawnExceptions.add((Player) causeEntity);
        }
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean checkSupport() {
        switch (getBellAttachmentType()) {
            case STANDING:
                if (checkSupport(down(), BlockFace.UP)) {
                    return true;
                }
                break;
            case HANGING:
                if (checkSupport(up(), BlockFace.DOWN)) {
                    return true;
                }
                break;
            case MULTIPLE:
                BlockFace blockFace = getBlockFace();
                if (checkSupport(getSide(blockFace), blockFace.getOpposite()) &&
                        checkSupport(getSide(blockFace.getOpposite()), blockFace)) {
                    return true;
                }
                break;
            case SIDE:
                blockFace = getBlockFace();
                if (checkSupport(getSide(blockFace.getOpposite()), blockFace)) {
                    return true;
                }
                break;
            default:
        }
        return false;
    }

    private boolean checkSupport(Block support, BlockFace attachmentFace) {
        if (BlockLever.isSupportValid(support, attachmentFace)) {
            return true;
        }
        
        if (attachmentFace == BlockFace.DOWN) {
            switch (support.getId()) {
                case CHAIN_BLOCK:
                case HOPPER_BLOCK:
                case IRON_BARS:
                    return true;
                default:
                    return support instanceof BlockFence || support instanceof BlockWallBase;
            }
        }
        
        if (support instanceof BlockCauldron) {
            return attachmentFace == BlockFace.UP;
        }

        return false;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (!checkSupport()) {
                this.level.useBreakOn(this);
            }
            return type;
        } else if (type == Level.BLOCK_UPDATE_REDSTONE) {
            if (level.isBlockPowered(this)) {
                if (!isToggled()) {
                    setToggled(true);
                    this.level.setBlock(this, this, true, true);
                    ring(null, BellRingEvent.RingCause.REDSTONE);
                }
            } else if (isToggled()) {
                setToggled(false);
                this.level.setBlock(this, this, true, true);
            }
            return type;
        }
        return 0;
    }

    @Override
    public boolean place(@Nonnull Item item, @Nonnull Block block, @Nonnull Block target, @Nonnull BlockFace face, double fx, double fy, double fz, @Nullable Player player) {
        if (block.canBeReplaced() && block.getId() != AIR && block.getId() != BUBBLE_COLUMN && !(block instanceof BlockLiquid)) {
            face = BlockFace.UP;
        }
        BlockFace playerDirection = player != null? player.getDirection() : BlockFace.EAST;
        switch (face) {
            case UP:
                setBellAttachmentType(BellAttachmentType.STANDING);
                setBlockFace(playerDirection.getOpposite());
                break;
            case DOWN:
                setBellAttachmentType(BellAttachmentType.HANGING);
                setBlockFace(playerDirection.getOpposite());
                break;
            default:
                setBlockFace(face);
                if (checkSupport(block.getSide(face), face.getOpposite())) {
                    setBellAttachmentType(BellAttachmentType.MULTIPLE);
                } else {
                    setBellAttachmentType(BellAttachmentType.SIDE);
                }
        }
        if (!checkSupport()) {
            return false;
        }
        return BlockEntityHolder.setBlockAndCreateEntity(this) != null;
    }

    @Since("1.4.0.0-PN")
    @PowerNukkitOnly
    @Override
    public boolean onProjectileHit(@Nonnull Entity projectile, @Nonnull Position position, @Nonnull Vector3 motion) {
        ring(projectile, BellRingEvent.RingCause.PROJECTILE);
        if (projectile.isOnFire() && projectile instanceof EntityArrow && level.getBlock(projectile).getId() == BlockID.AIR) {
            level.setBlock(projectile, Block.get(BlockID.FIRE), true);
        }
        return true;
    }

    @Override
    public BlockFace getBlockFace() {
        return getPropertyValue(DIRECTION);
    }

    @PowerNukkitOnly
    @Since("1.3.0.0-PN")
    @Override
    public void setBlockFace(BlockFace face) {
        setPropertyValue(DIRECTION, face);
    }

    @PowerNukkitOnly
    @Since("1.4.0.0-PN")
    public BellAttachmentType getBellAttachmentType() {
        return getPropertyValue(ATTACHMENT_TYPE);
    }
    
    @PowerNukkitOnly
    @Since("1.4.0.0-PN")
    public void setBellAttachmentType(BellAttachmentType bellAttachmentType) {
        setPropertyValue(ATTACHMENT_TYPE, bellAttachmentType);
    }
    
    @Deprecated
    @DeprecationDetails(since = "1.4.0.0-PN", reason = "Magic values.", replaceWith = "getBellAttachmentType()")
    public int getAttachmentType() {
        return getBellAttachmentType().ordinal();
    }

    @Deprecated
    @DeprecationDetails(since = "1.4.0.0-PN", reason = "Magic values.", replaceWith = "setBellAttachmentType(BellAttachmentType)")
    public void setAttachmentType(int attachmentType) {
        setBellAttachmentType(BellAttachmentType.values()[attachmentType]);
    }

    @PowerNukkitOnly
    public boolean isToggled() {
        return getBooleanValue(TOGGLE);
    }

    @PowerNukkitOnly
    public void setToggled(boolean toggled) {
        setBooleanValue(TOGGLE, toggled);
    }

    @Override
    public Item toItem() {
        return new ItemBlock(new BlockBell());
    }

    @PowerNukkitOnly
    @Override
    public int getWaterloggingLevel() {
        return 1;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_PICKAXE;
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }

    @Override
    public double getHardness() {
        return 1;
    }

    @Override
    public double getResistance() {
        return 25;
    }

    @Override
    public int getToolTier() {
        return ItemTool.TIER_WOODEN;
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.GOLD_BLOCK_COLOR;
    }
}
