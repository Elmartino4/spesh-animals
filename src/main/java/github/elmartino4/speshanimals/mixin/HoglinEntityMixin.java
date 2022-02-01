package github.elmartino4.speshanimals.mixin;

import github.elmartino4.speshanimals.util.AnimalInterface;
import net.minecraft.entity.mob.HoglinEntity;
import net.minecraft.entity.mob.ZoglinEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(HoglinEntity.class)
public class HoglinEntityMixin {
    @Inject(method = "mobTick", at = @At("HEAD"))
    private void mobTick(CallbackInfo ci) {
        ((AnimalInterface) (Object) this).getGenetics().speshTick();
    }

    @Inject(method = "zombify", at = @At("TAIL"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void zombify(ServerWorld world, CallbackInfo ci, ZoglinEntity zoglinEntity) {
        ((AnimalInterface) zoglinEntity).setGenetics(((AnimalInterface) (Object) this).getGenetics());
    }
}
