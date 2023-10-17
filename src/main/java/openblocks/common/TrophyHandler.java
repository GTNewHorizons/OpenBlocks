package openblocks.common;

import java.util.Map;
import java.util.Random;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

import com.google.common.base.Strings;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import openblocks.Config;
import openblocks.OpenBlocks;
import openblocks.common.item.ItemTrophyBlock;
import openblocks.common.tileentity.TileEntityTrophy;
import openblocks.trophy.BlazeBehavior;
import openblocks.trophy.CaveSpiderBehavior;
import openblocks.trophy.CreeperBehavior;
import openblocks.trophy.EndermanBehavior;
import openblocks.trophy.ITrophyBehavior;
import openblocks.trophy.ItemDropBehavior;
import openblocks.trophy.MooshroomBehavior;
import openblocks.trophy.SkeletonBehavior;
import openblocks.trophy.SnowmanBehavior;
import openblocks.trophy.SquidBehavior;
import openblocks.trophy.WitchBehavior;
import openmods.Log;
import openmods.calc.Environment;
import openmods.calc.ExprType;
import openmods.calc.SingleExprEvaluator;
import openmods.calc.SingleExprEvaluator.EnvironmentConfigurator;
import openmods.calc.types.fp.DoubleCalculatorFactory;
import openmods.config.properties.ConfigurationChange;
import openmods.reflection.ReflectionHelper;

public class TrophyHandler {

    private static final Map<Trophy, Entity> ENTITY_CACHE = Maps.newHashMap();

    private final Random fallbackDropChance = new Random();

    private final SingleExprEvaluator<Double, ExprType> dropChangeCalculator = SingleExprEvaluator
            .create(DoubleCalculatorFactory.createDefault());

    {
        updateDropChanceFormula();
    }

    @SubscribeEvent
    public void onConfigChange(ConfigurationChange.Post evt) {
        if (evt.check("trophy", "trophyDropChanceFormula")) updateDropChanceFormula();
    }

    private void updateDropChanceFormula() {
        dropChangeCalculator.setExpr(ExprType.INFIX, Config.trophyDropChanceFormula);
    }

    public static Entity getEntityFromCache(Trophy trophy) {
        Entity entity = ENTITY_CACHE.get(trophy);
        if (entity == null) {
            if (!ENTITY_CACHE.containsKey(trophy)) {
                try {
                    entity = trophy.createEntity();
                } catch (Throwable t) {
                    Log.severe(t, "Failed to create dummy entity for trophy %s", trophy);
                }
            }
            ENTITY_CACHE.put(trophy, entity);
        }
        return entity;
    }

    private static Entity setSlimeSize(Entity entity, int size) {
        try {
            ReflectionHelper
                    .call(entity, new String[] { "func_70799_a", "setSlimeSize" }, ReflectionHelper.primitive(size));
        } catch (Exception e) {
            Log.warn(e, "Can't update slime size");
        }
        return entity;
    }

    public enum Trophy {

        Wolf(),
        Chicken(new ItemDropBehavior(10000, new ItemStack(Items.egg), "mob.chicken.plop")),
        Cow(new ItemDropBehavior(20000, new ItemStack(Items.leather))),
        Creeper(new CreeperBehavior()),
        Skeleton(new SkeletonBehavior()),
        PigZombie(new ItemDropBehavior(20000, new ItemStack(Items.gold_nugget))),
        Bat(1.0, -0.3),
        Zombie(),
        Witch(0.35, new WitchBehavior()),
        Villager(),
        Ozelot() {

            @Override
            protected Entity createEntity() {
                Entity entity = super.createEntity();

                try {
                    ((EntityOcelot) entity).setTamed(true);
                } catch (ClassCastException e) {
                    Log.warn("Invalid cat entity class: %s", entity.getClass());
                }
                return entity;
            }
        },
        Sheep(new ItemDropBehavior(20000, new ItemStack(Blocks.wool))),
        Blaze(new BlazeBehavior()),
        Silverfish(),
        Spider(),
        CaveSpider(new CaveSpiderBehavior()),
        Slime(0.6) {

            @Override
            protected Entity createEntity() {
                return setSlimeSize(super.createEntity(), 1);
            }
        },
        Ghast(0.1, 0.3),
        Enderman(0.3, new EndermanBehavior()),
        LavaSlime(0.6) {

            @Override
            protected Entity createEntity() {
                return setSlimeSize(super.createEntity(), 1);
            }
        },
        Squid(0.3, 0.5, new SquidBehavior()),
        MushroomCow(new MooshroomBehavior()),
        VillagerGolem(0.3),
        SnowMan(new SnowmanBehavior()),
        Pig(new ItemDropBehavior(20000, new ItemStack(Items.porkchop)));

        private double scale = 0.4;
        private double verticalOffset = 0.0;
        private ITrophyBehavior behavior;

        Trophy() {}

        Trophy(ITrophyBehavior behavior) {
            this.behavior = behavior;
        }

        Trophy(double scale) {
            this.scale = scale;
        }

        Trophy(double scale, ITrophyBehavior behavior) {
            this.scale = scale;
            this.behavior = behavior;
        }

        Trophy(double scale, double verticalOffset) {
            this(scale);
            this.verticalOffset = verticalOffset;
        }

        Trophy(double scale, double verticalOffset, ITrophyBehavior behavior) {
            this(scale, verticalOffset);
            this.behavior = behavior;
        }

        public double getVerticalOffset() {
            return verticalOffset;
        }

        public double getScale() {
            return scale;
        }

        public Entity getEntity() {
            return getEntityFromCache(this);
        }

        public ItemStack getItemStack() {
            return ItemTrophyBlock.putMetadata(new ItemStack(OpenBlocks.Blocks.trophy), this);
        }

        public void playSound(World world, double x, double y, double z) {
            if (world == null) return;

            Entity e = getEntity();
            if (e instanceof EntityLiving) {
                e.posX = x;
                e.posY = y;
                e.posZ = z;

                synchronized (e) {
                    e.worldObj = world;
                    ((EntityLiving) e).playLivingSound();
                    e.worldObj = null;
                }
            }
        }

        public int executeActivateBehavior(TileEntityTrophy tile, EntityPlayer player) {
            if (behavior != null) return behavior.executeActivateBehavior(tile, player);
            return 0;
        }

        public void executeTickBehavior(TileEntityTrophy tile) {
            if (behavior != null) behavior.executeTickBehavior(tile);
        }

        protected Entity createEntity() {
            return EntityList.createEntityByName(toString(), null);
        }

        static {

            ImmutableMap.Builder<String, Trophy> builder = ImmutableMap.builder();

            for (Trophy t : values()) builder.put(t.name(), t);

            TYPES = builder.build();
        }

        public final static Map<String, Trophy> TYPES;

        public final static Trophy[] VALUES = values();
    }

    @SubscribeEvent
    public void onLivingDrops(final LivingDropsEvent event) {
        final Entity entity = event.entity;
        if (event.recentlyHit && canDrop(entity)) {
            final Double result = dropChangeCalculator.evaluate(new EnvironmentConfigurator<Double>() {

                @Override
                public void accept(Environment<Double> env) {
                    env.setGlobalSymbol("looting", Double.valueOf(event.lootingLevel));
                    env.setGlobalSymbol("specialDrop", Double.valueOf(event.specialDropValue));
                    env.setGlobalSymbol("chance", Config.trophyDropChance);
                }
            }, new Supplier<Double>() {

                @Override
                public Double get() {
                    final double bias = fallbackDropChance.nextDouble() / 4;
                    final double selection = fallbackDropChance.nextDouble();
                    return (event.lootingLevel + bias) * Config.trophyDropChance - selection;
                }
            });

            if (result > 0) {
                final String entityName = EntityList.getEntityString(entity);
                if (!Strings.isNullOrEmpty(entityName)) {
                    Trophy mobTrophy = Trophy.TYPES.get(entityName);
                    if (mobTrophy != null) {
                        EntityItem drop = new EntityItem(
                                entity.worldObj,
                                entity.posX,
                                entity.posY,
                                entity.posZ,
                                mobTrophy.getItemStack());
                        drop.delayBeforeCanPickup = 10;
                        event.drops.add(drop);
                    }
                }
            }
        }
    }

    private static boolean canDrop(Entity entity) {
        final World world = entity.worldObj;
        return world != null && world.getGameRules().getGameRuleBooleanValue("doMobLoot");
    }

}
