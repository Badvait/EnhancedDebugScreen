package me.badvait.enhanceddebugmenu.mixin;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.UnmodifiableIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.SharedConstants;
import net.minecraft.block.BlockState;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.fluid.FluidState;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Property;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.*;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {

    private final MinecraftClient client = MinecraftClient.getInstance();

    @Shadow protected abstract String propertyToString(Map.Entry<Property<?>, Comparable<?>> propEntry);

    @Shadow @Nullable private ChunkPos pos;

    @Shadow public abstract void resetChunk();

    @Shadow protected abstract World getWorld();

    @Shadow @Nullable protected abstract String method_27871();

    @Shadow protected abstract WorldChunk getClientChunk();

    @Shadow @Nullable protected abstract WorldChunk getChunk();

    @Shadow @Final private static Map<Heightmap.Type, String> HEIGHT_MAP_TYPES;

    @Shadow @Nullable protected abstract ServerWorld getServerWorld();

    @Shadow protected abstract List<String> getLeftText();

    @Inject(at = @At("HEAD"), method = "getLeftText", cancellable = true)
    protected void getLeftText(CallbackInfoReturnable<List<String>> cir) {
        IntegratedServer integratedServer = this.client.getServer();
        ClientConnection clientConnection = this.client.getNetworkHandler().getConnection();
        float f = clientConnection.getAveragePacketsSent();
        float g = clientConnection.getAveragePacketsReceived();
        /*
        String string2;
        if (integratedServer != null) {
            string2 = String.format("Integrated server @ %.0f ms ticks, %.0f tx, %.0f rx", integratedServer.getTickTime(), f, g);
        } else {
            string2 = String.format("\"%s\" server, %.0f tx, %.0f rx", this.client.player.getServerBrand(), f, g);
        }

         */

        BlockPos blockPos = this.client.getCameraEntity().getBlockPos();
        if (this.client.hasReducedDebugInfo()) {
            cir.setReturnValue(Lists.newArrayList("Minecraft " + SharedConstants.getGameVersion().getName() + " (" + this.client.getGameVersion() + "/" + ClientBrandRetriever.getClientModName() + ")", this.client.fpsDebugString, this.client.worldRenderer.getChunksDebugString(), this.client.worldRenderer.getEntitiesDebugString(), "P: " + this.client.particleManager.getDebugString() + ". T: " + this.client.world.getRegularEntityCount(), this.client.world.getDebugString(), "", String.format("Chunk-relative: %d %d %d", blockPos.getX() & 15, blockPos.getY() & 15, blockPos.getZ() & 15)));
        } else {
            Entity entity = this.client.getCameraEntity();
            Direction direction = entity.getHorizontalFacing();
            String string7;
            switch(direction) {
                case NORTH:
                    string7 = "Towards negative Z";
                    break;
                case SOUTH:
                    string7 = "Towards positive Z";
                    break;
                case WEST:
                    string7 = "Towards negative X";
                    break;
                case EAST:
                    string7 = "Towards positive X";
                    break;
                default:
                    string7 = "Invalid";
            }

            ChunkPos chunkPos = new ChunkPos(blockPos);
            if (!Objects.equals(this.pos, chunkPos)) {
                this.pos = chunkPos;
                this.resetChunk();
            }

            World world = this.getWorld();
            LongSet longSet = world instanceof ServerWorld ? ((ServerWorld)world).getForcedChunks() : LongSets.EMPTY_SET;

            List<String> list = Lists.newArrayList
                    ("Minecraft " + SharedConstants.getGameVersion().getName() + " (" + this.client.getGameVersion() + "/" + ClientBrandRetriever.getClientModName()
                            + ("release".equalsIgnoreCase(this.client.getVersionType()) ? "" : "/" + this.client.getVersionType()) + ")"
                            , this.client.fpsDebugString);

            list.add(this.client.worldRenderer.getEntitiesDebugString());
            list.add(this.client.worldRenderer.getChunksDebugString());
            // , this.client.worldRenderer.getChunksDebugString(), this.client.worldRenderer.getEntitiesDebugString(), "P: " + this.client.particleManager.getDebugString() + ". T: " + this.client.world.getRegularEntityCount(), this.client.world.getDebugString()

            /*
            String string8 = this.method_27871();
            if (string8 != null) {
                list.add(string8);
            }

             */


            //list.add(this.client.world.getRegistryKey().getValue() + " FC: " + ((LongSet)longSet).size());
            list.add("");
            list.add(String.format(Locale.ROOT, "XYZ: %.3f / %.5f / %.3f", this.client.getCameraEntity().getX(), this.client.getCameraEntity().getY(), this.client.getCameraEntity().getZ()));
            list.add(String.format("Block: %d %d %d", blockPos.getX(), blockPos.getY(), blockPos.getZ()));
            list.add(String.format("Chunk: %d %d %d in %d %d %d", blockPos.getX() & 15, blockPos.getY() & 15, blockPos.getZ() & 15, blockPos.getX() >> 4, blockPos.getY() >> 4, blockPos.getZ() >> 4));
            list.add(String.format(Locale.ROOT, "Facing: %s (%s) (%.1f / %.1f)", direction, string7, MathHelper.wrapDegrees(entity.yaw), MathHelper.wrapDegrees(entity.pitch)));
            int m;
            if (this.client.world != null) {
                if (this.client.world.isChunkLoaded(blockPos)) {
                    WorldChunk worldChunk = this.getClientChunk();
                    if (worldChunk.isEmpty()) {
                        list.add("Waiting for chunk...");
                    } else {
                        int i = this.client.world.getChunkManager().getLightingProvider().getLight(blockPos, 0);
                        int j = this.client.world.getLightLevel(LightType.SKY, blockPos);
                        m = this.client.world.getLightLevel(LightType.BLOCK, blockPos);
                        list.add("Client Light: " + i + " (" + j + " sky, " + m + " block)");

                        WorldChunk worldChunk2 = this.getChunk();

                        /*
                        if (worldChunk2 != null) {
                            LightingProvider lightingProvider = world.getChunkManager().getLightingProvider();
                            list.add("Server Light: (" + lightingProvider.get(LightType.SKY).getLightLevel(blockPos) + " sky, " + lightingProvider.get(LightType.BLOCK).getLightLevel(blockPos) + " block)");
                        } else {
                            list.add("Server Light: (?? sky, ?? block)");
                        }

                         */

                        StringBuilder stringBuilder = new StringBuilder("CH");
                        Heightmap.Type[] var21 = Heightmap.Type.values();
                        int var22 = var21.length;

                        int var23;
                        Heightmap.Type type2;
                        for(var23 = 0; var23 < var22; ++var23) {
                            type2 = var21[var23];
                            if (type2.shouldSendToClient()) {
                                stringBuilder.append(" ").append((String)HEIGHT_MAP_TYPES.get(type2)).append(": ").append(worldChunk.sampleHeightmap(type2, blockPos.getX(), blockPos.getZ()));
                            }
                        }

                        //list.add(stringBuilder.toString());
                        stringBuilder.setLength(0);
                        stringBuilder.append("SH");
                        var21 = Heightmap.Type.values();
                        var22 = var21.length;

                        for(var23 = 0; var23 < var22; ++var23) {
                            type2 = var21[var23];
                            if (type2.isStoredServerSide()) {
                                stringBuilder.append(" ").append((String)HEIGHT_MAP_TYPES.get(type2)).append(": ");
                                if (worldChunk2 != null) {
                                    stringBuilder.append(worldChunk2.sampleHeightmap(type2, blockPos.getX(), blockPos.getZ()));
                                } else {
                                    stringBuilder.append("??");
                                }
                            }
                        }

                        list.add(stringBuilder.toString());
                        if (blockPos.getY() >= 0 && blockPos.getY() < 256) {
                            list.add("Biome: " + Registry.BIOME.getId(this.client.world.getBiome(blockPos)));
                            long l = 0L;
                            float h = 0.0F;
                            if (worldChunk2 != null) {
                                h = world.getMoonSize();
                                l = worldChunk2.getInhabitedTime();
                            }

                            LocalDifficulty localDifficulty = new LocalDifficulty(world.getDifficulty(), world.getTimeOfDay(), l, h);
                            list.add(String.format(Locale.ROOT, "Local Difficulty: %.2f // %.2f (Day %d)", localDifficulty.getLocalDifficulty(), localDifficulty.getClampedLocalDifficulty(), this.client.world.getTimeOfDay() / 24000L));
                        }
                    }
                } else {
                    list.add("Outside of world...");
                }
            } else {
                list.add("Outside of world...");
            }

            ServerWorld serverWorld = this.getServerWorld();

            if (serverWorld != null) {
                SpawnHelper.Info info = serverWorld.getChunkManager().getSpawnInfo();
                if (info != null) {
                    Object2IntMap<SpawnGroup> object2IntMap = info.getGroupToCount();
                    m = info.getSpawningChunkCount();
                    list.add("SC: " + m + ", " + (String) Stream.of(SpawnGroup.values()).map((spawnGroup) -> {
                        return Character.toUpperCase(spawnGroup.getName().charAt(0)) + ": " + object2IntMap.getInt(spawnGroup);
                    }).collect(Collectors.joining(", ")));
                } else {
                    list.add("SC: N/A");
                }


            }


            /*
            ShaderEffect shaderEffect = this.client.gameRenderer.getShader();
            if (shaderEffect != null) {
                list.add("Shader: " + shaderEffect.getName());
            }

            list.add(this.client.getSoundManager().getDebugString() + String.format(" (Mood %d%%)", Math.round(this.client.player.getMoodPercentage() * 100.0F)));

             */
            cir.setReturnValue(list);
        }
    }

    private final TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

    @Inject(at = @At("HEAD"), method = "renderLeftText", cancellable = true)
    private void renderLeftText(MatrixStack matrices, CallbackInfo ci) {
        List<String> list = getLeftText();
        
        HitResult blockHit = client.getCameraEntity().rayTrace(20.0D, 0.0F, false);
        HitResult fluidHit = client.getCameraEntity().rayTrace(20.0D, 0.0F, true);
        
        BlockPos blockPos2;
        UnmodifiableIterator var12;
        Map.Entry entry2;
        Iterator var16;
        Identifier identifier2;
        if (blockHit.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
            blockPos2 = ((BlockHitResult)blockHit).getBlockPos();
            BlockState blockState = this.client.world.getBlockState(blockPos2);
            list.add("");
            list.add(Formatting.UNDERLINE + "Targeted Block: " + blockPos2.getX() + ", " + blockPos2.getY() + ", " + blockPos2.getZ());
            list.add(String.valueOf(Registry.BLOCK.getId(blockState.getBlock())));
            var12 = blockState.getEntries().entrySet().iterator();

            while(var12.hasNext()) {
                entry2 = (Map.Entry)var12.next();
                list.add(propertyToString(entry2));
            }

            var16 = (this.client.getNetworkHandler()).getTagManager().blocks().getTagsFor(blockState.getBlock()).iterator();

            while(var16.hasNext()) {
                identifier2 = (Identifier)var16.next();
                list.add("#" + identifier2);
            }
        }

        if (fluidHit.getType() == net.minecraft.util.hit.HitResult.Type.BLOCK) {
            blockPos2 = ((BlockHitResult)fluidHit).getBlockPos();
            assert this.client.world != null;
            FluidState fluidState = this.client.world.getFluidState(blockPos2);
            list.add("");
            list.add(Formatting.UNDERLINE + "Targeted Fluid: " + blockPos2.getX() + ", " + blockPos2.getY() + ", " + blockPos2.getZ());
            list.add(String.valueOf(Registry.FLUID.getId(fluidState.getFluid())));
            var12 = fluidState.getEntries().entrySet().iterator();

            while(var12.hasNext()) {
                entry2 = (Map.Entry)var12.next();
                list.add(this.propertyToString(entry2));
            }

            var16 = Objects.requireNonNull(this.client.getNetworkHandler()).getTagManager().fluids().getTagsFor(fluidState.getFluid()).iterator();

            while(var16.hasNext()) {
                identifier2 = (Identifier)var16.next();
                list.add("#" + identifier2);
            }
        }


        for(int i = 0; i < list.size(); ++i) {
            String string = (String)list.get(i);
            if (!Strings.isNullOrEmpty(string)) {
                Objects.requireNonNull(this.textRenderer);
                int j = 9;
                int k = this.textRenderer.getWidth(string);
                int m = 2 + j * i;
                DrawableHelper.fill(matrices, 1, m - 1, 2 + k + 1, m + j - 1, -1873784752);
                this.textRenderer.draw(matrices, string, 2.0F, (float)m, 14737632);
            }
        }

        ci.cancel();



    }

    @Inject(at = @At("HEAD"), method = "getRightText", cancellable = true)
    private void getRightText(CallbackInfoReturnable<List<String>> cir) {
        cir.cancel();
    }

    @Inject(at = @At("HEAD"), method = "renderRightText", cancellable = true)
    private void renderRightText(MatrixStack matrices, CallbackInfo ci) {
        ci.cancel();
    }

}