package github.elmartino4.speshanimals.mixin;

import github.elmartino4.speshanimals.Gene;
import github.elmartino4.speshanimals.util.AnimalInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;
import org.lwjgl.system.CallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleType;

import java.io.IOException;

@Mixin(FishEntity.class)
public abstract class FishEntityMixin extends WaterCreatureEntity implements AnimalInterface {
    private static final TrackedData<Float> GENETICS = DataTracker.registerData(FishEntity.class, TrackedDataHandlerRegistry.FLOAT);

    protected FishEntityMixin(EntityType<? extends WaterCreatureEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void inject(EntityType<? extends FishEntity> entityType, World world, CallbackInfo ci){
        scale();
    }

    private void scale(){
        ScaleData baseData = ScaleType.BASE.getScaleData((Entity) (Object) this);
        baseData.resetScale();
        baseData.setScale(getDataTracker().get(GENETICS));
        baseData.onUpdate();
    }

    @Inject(method = "initDataTracker", at = @At("TAIL"))
    private void initDataTracker(CallbackInfo ci) {
        double size = 0;
        if(world.random.nextFloat() < 0.2){
            size = world.random.nextFloat();
            size = size * 2 - 1;
            size = size * size * size;
        }
        dataTracker.startTracking(GENETICS, (float)size + 1F);
    }

    @Inject(method = "copyDataToStack", at = @At("HEAD"))
    private void onBucket(ItemStack stack, CallbackInfo ci){
        stack.getOrCreateNbt().putFloat("genetics", getDataTracker().get(GENETICS));
    }

    @Inject(method = "copyDataFromNbt", at = @At("HEAD"))
    private void onUseBucket(NbtCompound nbt, CallbackInfo ci){
        double size = nbt.getFloat("genetics");
        if(size == 0){
            if(world.random.nextFloat() < 0.2){
                size = world.random.nextFloat() * 2 - 1;
                size = size * size * size;
            }
            size += 1;
        }


        getDataTracker().set(GENETICS, (float)size);
        scale();
    }

    @Override
    public NbtList bookData() {
        NbtList out = new NbtList();
        String page1Str = """
                [
                    {"text":"Info on ","bold":true},
                    {"text":"%s","bold":true},
                    {"text":"\\nSize - %.2f\\n","bold":false}
                ]
                """;

        //,"hoverEvent":{"action":"show_entity","contents":{"id":"%s"}}

        String entityName = new TranslatableText(getType().getTranslationKey()).getString();
        if(getCustomName() != null) entityName = getCustomName().asString();

        out.add(NbtString.of(String.format(page1Str,
                entityName,
                getDataTracker().get(GENETICS)
        )));

        return out;
    }
}
