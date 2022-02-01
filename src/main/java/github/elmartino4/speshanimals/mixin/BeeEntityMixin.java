package github.elmartino4.speshanimals.mixin;

import github.elmartino4.speshanimals.util.AnimalInterface;
import net.minecraft.entity.passive.BeeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeeEntity.class)
public class BeeEntityMixin {
    @Inject(method = "mobTick", at = @At("HEAD"))
    private void mobTick(CallbackInfo ci){
        ((AnimalInterface) (Object) this).getGenetics().speshTick();
    }
}
