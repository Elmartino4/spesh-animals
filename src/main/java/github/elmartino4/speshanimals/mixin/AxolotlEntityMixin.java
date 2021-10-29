package github.elmartino4.speshanimals.mixin;

import github.elmartino4.speshanimals.Gene;
import github.elmartino4.speshanimals.SpeshAnimals;
import github.elmartino4.speshanimals.util.AnimalInterface;
import net.minecraft.entity.EntityType;
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
        stack.getOrCreateNbt().putFloat("genetics", ((AnimalInterface) (Object) this).getGenetics());
        stack.getOrCreateNbt().putFloat("stability", ((AnimalInterface) (Object) this).getStability());
    }

    @Inject(method = "mobTick", at = @At("HEAD"))
    private void mobTick(CallbackInfo ci){
        ((AnimalInterface) (Object) this).speshTick();
    }

    @Inject(method = "copyDataFromNbt", at = @At("HEAD"))
    private void onUseBucket(NbtCompound nbt, CallbackInfo ci){
        double genetics = nbt.getFloat("genetics");
        double stability = nbt.getFloat("stability");

        if(!nbt.contains("genetics")) genetics = (random.nextFloat() - 0.5) * Gene.MIN_DIFFERENCE * 3.0F;
        if(!nbt.contains("stability")) stability = 0.001F;

        ((AnimalInterface) (Object) this).setGenetics((float)genetics);
        ((AnimalInterface) (Object) this).setStability((float)stability);

        ((AnimalInterface)(Object) this).loadSize(world);
    }
}
