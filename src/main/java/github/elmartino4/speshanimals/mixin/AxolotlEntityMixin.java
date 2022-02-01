package github.elmartino4.speshanimals.mixin;

import github.elmartino4.speshanimals.genetics.Genetics;
import github.elmartino4.speshanimals.util.AnimalInterface;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.AxolotlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AxolotlEntity.class)
public abstract class AxolotlEntityMixin extends AnimalEntity {
    protected AxolotlEntityMixin(EntityType<? extends AnimalEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "copyDataToStack", at = @At("HEAD"))
    private void onBucket(ItemStack stack, CallbackInfo ci){
        stack.getOrCreateNbt().putFloat("genetics", ((AnimalInterface) (Object) this).getGenetics().GENETICS);
        stack.getOrCreateNbt().putFloat("stability", ((AnimalInterface) (Object) this).getGenetics().STABILITY);
    }

    @Inject(method = "mobTick", at = @At("HEAD"))
    private void mobTick(CallbackInfo ci){
        ((AnimalInterface) (Object) this).getGenetics().speshTick();
    }

    @Inject(method = "copyDataFromNbt", at = @At("HEAD"))
    private void onUseBucket(NbtCompound nbt, CallbackInfo ci){
        ((AnimalInterface) (Object) this).setGenetics(new Genetics(world, (MobEntity) (Object) this));

        ((AnimalInterface) (Object) this).getGenetics().readNbt(nbt);

        ((AnimalInterface)(Object) this).getGenetics().loadSize();
    }
}
