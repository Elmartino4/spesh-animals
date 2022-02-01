package github.elmartino4.speshanimals.mixin;

import github.elmartino4.speshanimals.genetics.Genetics;
import github.elmartino4.speshanimals.util.AnimalInterface;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin extends LivingEntity implements AnimalInterface {
    @Unique private Genetics genetics;

    @Inject(method = "mobTick", at = @At("HEAD"))
    private void mobTick(CallbackInfo ci){
        genetics.speshTick();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initSizing(EntityType<? extends AnimalEntity> entityType, World world, CallbackInfo ci){
        genetics = new Genetics(world, (MobEntity) (Object) this);
        genetics.loadSize();
    }

    protected AnimalEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "breed", at = @At(value = "INVOKE", target = "net/minecraft/entity/passive/PassiveEntity.setBaby(Z)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void initBabyData(ServerWorld world, AnimalEntity other, CallbackInfo ci, PassiveEntity passiveEntity /* child */){
        ((AnimalInterface) passiveEntity).setGenetics(new Genetics(world, passiveEntity, genetics, ( (AnimalInterface) other).getGenetics()));
    }

    @Inject(method = "breed", at = @At(value = "INVOKE", target = "net/minecraft/server/world/ServerWorld.spawnEntityAndPassengers(Lnet/minecraft/entity/Entity;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void loadBabyData(ServerWorld world, AnimalEntity other, CallbackInfo ci, PassiveEntity passiveEntity /* child */){
        ((AnimalInterface) passiveEntity).getGenetics().loadSize();
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeNbt(NbtCompound nbt, CallbackInfo ci){
        genetics.writeNbt(nbt);
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readNbt(NbtCompound nbt, CallbackInfo ci){
        genetics.readNbt(nbt);
        genetics.loadSize();
    }

    @Override
    public Genetics getGenetics(){
        return genetics;
    }

    @Override
    public void setGenetics(Genetics genetics){
        this.genetics = genetics;
    }
}


