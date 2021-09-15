package me.badvait.enhanceddebugmenu.mixin;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class) // test
public abstract class DebugHudMixin {

    @Shadow
    protected abstract List<String> getLeftText();

    @Final
    private TextRenderer textRenderer;

    @Inject(at = @At("HEAD"), method = "getRightText", cancellable = true)
    private void getRightText(CallbackInfoReturnable<List<String>> cir) {
        cir.cancel();
    }

    @Inject(at = @At("HEAD"), method = "renderRightText", cancellable = true)
    private void renderRightText(MatrixStack matrices, CallbackInfo ci) {
        ci.cancel();
    }

}