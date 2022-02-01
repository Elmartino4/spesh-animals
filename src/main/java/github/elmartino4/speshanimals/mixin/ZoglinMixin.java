package github.elmartino4.speshanimals.mixin;

import github.elmartino4.speshanimals.genetics.Genetics;
import github.elmartino4.speshanimals.util.AnimalInterface;
import net.minecraft.entity.mob.ZoglinEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ZoglinEntity.class)
public class ZoglinMixin implements AnimalInterface {
    @Unique
    private Genetics genetics;

    @Inject(method = "mobTick", at = @At("HEAD"))
    private void mobTick(CallbackInfo ci){
        if (genetics != null)
            genetics.speshTick();
    }

    @Override
    public Genetics getGenetics() {
        return genetics;
    }

    @Override
    public void setGenetics(Genetics genetics) {
        this.genetics = genetics;
    }
}
