package me.badvait.enhanceddebugmenu.mixin;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.options.AttackIndicator;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.GameMode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class InGameHudMixin extends DrawableHelper {

    @Shadow protected abstract boolean shouldRenderSpectatorCrosshair(HitResult hitResult);

    @Shadow private int scaledWidth;
    @Shadow private int scaledHeight;
    private final MinecraftClient client = MinecraftClient.getInstance();

    @Inject(method = "renderCrosshair", at = @At("HEAD"), cancellable = true)
    private void renderCrosshair(MatrixStack matrices, CallbackInfo ci) {
        if (client.options.perspective == 0) {
            if (this.client.interactionManager.getCurrentGameMode() != GameMode.SPECTATOR || this.shouldRenderSpectatorCrosshair(this.client.crosshairTarget)) {
                RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.ONE_MINUS_DST_COLOR, GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO);
                this.drawTexture(matrices, (this.scaledWidth - 15) / 2, (this.scaledHeight - 15) / 2, 0, 0, 15, 15);
                if (this.client.options.attackIndicator == AttackIndicator.CROSSHAIR.CROSSHAIR) {
                    float f = this.client.player.getAttackCooldownProgress(0.0F);
                    boolean bl = false;
                    if (this.client.targetedEntity != null && this.client.targetedEntity instanceof LivingEntity && f >= 1.0F) {
                        bl = this.client.player.getAttackCooldownProgressPerTick() > 5.0F;
                        bl &= this.client.targetedEntity.isAlive();
                    }

                    int j = this.scaledHeight / 2 - 7 + 16;
                    int k = this.scaledWidth / 2 - 8;
                    if (bl) {
                        this.drawTexture(matrices, k, j, 68, 94, 16, 16);
                    } else if (f < 1.0F) {
                        int l = (int) (f * 17.0F);
                        this.drawTexture(matrices, k, j, 36, 94, 16, 4);
                        this.drawTexture(matrices, k, j, 52, 94, l, 4);
                    }
                }
            }

        }
        ci.cancel();
    }

}