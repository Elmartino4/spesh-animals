package github.elmartino4.speshanimals.mixin;

import github.elmartino4.speshanimals.Gene;
import github.elmartino4.speshanimals.SpeshAnimals;
import github.elmartino4.speshanimals.util.AnimalInterface;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtString;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleType;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin extends MobEntityMixin implements AnimalInterface {
    private static final TrackedData<Float> GENETICS = DataTracker.registerData(AnimalEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> STABILITY = DataTracker.registerData(AnimalEntity.class, TrackedDataHandlerRegistry.FLOAT);

    private static final Gene size = new Gene(1, 0);
    private static final Gene motion = new Gene(1, 1);
    private static final Gene health = new Gene(1, 2);
    private static final Gene power = new Gene(1, 3);
    private static final Gene explosivity = new Gene(2, 5);  //probability, size
    private static final Gene disease = new Gene(4, 7);   //probability, effect, duration, amplifier
    @Unique int speshTimer = 0;

    @Override
    public void speshTick(){
        if(speshTimer++ > 16){
            speshTimer = 0;
            if(world instanceof ServerWorld){
                double[] explosivityValues = explosivity.getValues((ServerWorld) world, getDataTracker().get(GENETICS));

                if (isExplosive() && !((Entity) (Object) this).inPowderSnow){
                    if((explosivityValues[0] + 1) / 8D > world.random.nextFloat() || ((Entity)(Object) this).getFireTicks() > 0) {
                        explosivityValues[1] += 1;
                        explosivityValues[1] *= 1.5;

                        explosivityValues[0] *= 0.25;

                        world.createExplosion((Entity) (Object) this, getX(), getY(), getZ(), (float) explosivityValues[1], (world.random.nextFloat() < explosivityValues[0]), Explosion.DestructionType.DESTROY);
                    }
                }

                double[] diseaseValues = disease.getValues((ServerWorld) world, getDataTracker().get(GENETICS));

                if(isSick()){
                    if((diseaseValues[0] + 1) / 4.0 > world.random.nextFloat()){
                        AreaEffectCloudEntity effectCloud = new AreaEffectCloudEntity(world, getX(), getY(), getZ());
                        effectCloud.setRadius((float)(1 - diseaseValues[0]) * 3.0F);
                        int duration = (int)(-Math.log(diseaseValues[2] * 0.5 + 0.5) * 80.0D);
                        int amplifier = (int)(-Math.log(Math.abs(diseaseValues[3])) * 3.0D);
                        effectCloud.addEffect(new StatusEffectInstance(getEffect(), duration, amplifier));

                        effectCloud.setDuration(24);

                        world.spawnEntity(effectCloud);
                    }
                }
            }
        }
    }

    @Inject(method = "mobTick", at = @At("HEAD"))
    private void mobTick(CallbackInfo ci){
        speshTick();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initSizing(EntityType<? extends AnimalEntity> entityType, World world, CallbackInfo ci){
        loadSize(world);
    }

    public void loadSize(World world){
        if(world instanceof ServerWorld){
            ScaleData widthData = ScaleType.WIDTH.getScaleData((Entity) (Object) this);
            widthData.resetScale();
            widthData.setScale(processSize());
            widthData.onUpdate();

            ScaleData heightData = ScaleType.HEIGHT.getScaleData((Entity) (Object) this);
            heightData.resetScale();
            heightData.setScale(processSize());
            heightData.onUpdate();

            ScaleData motionData = ScaleType.MOTION.getScaleData((Entity) (Object) this);
            motionData.resetScale();
            motionData.setScale(processSpeed());
            motionData.onUpdate();

            ScaleData healthData = ScaleType.HEALTH.getScaleData((Entity) (Object) this);
            healthData.resetScale();
            healthData.setScale(processHealth());
            healthData.onUpdate();

            ScaleData attackData = ScaleType.ATTACK.getScaleData((Entity) (Object) this);
            attackData.resetScale();
            attackData.setScale(processPower());
            attackData.onUpdate();

            ScaleData defenseData = ScaleType.DEFENSE.getScaleData((Entity) (Object) this);
            defenseData.resetScale();
            defenseData.setScale(processPower());
            defenseData.onUpdate();
        }
    }

    private boolean isExplosive(){
        return (explosivity.getValue((ServerWorld) world, getDataTracker().get(GENETICS)) + 1) * 2.0 < getDataTracker().get(STABILITY);
    }

    private boolean isSick(){
        return (disease.getValue((ServerWorld) world, getDataTracker().get(GENETICS)) + 1) * 2.0 < getDataTracker().get(STABILITY);
    }

    public float processSize(){
        double sizeOut = size.getValue((ServerWorld)world, getDataTracker().get(GENETICS));

        double out = -Math.log(sizeOut / 2.0 + 0.5)/0.693;

        out = Math.min(out, 3.5D) * getDataTracker().get(STABILITY);

        return (float)out;
    }

    private float processSpeed(){
        double sizeOut = motion.getValue((ServerWorld)world, getDataTracker().get(GENETICS));

        double out = -Math.log(sizeOut / 2.0 + 0.5)/0.693;

        return (float)Math.min(out, 3.5D) * getDataTracker().get(STABILITY);
    }

    private float processHealth(){
        double sizeOut = health.getValue((ServerWorld)world, getDataTracker().get(GENETICS));

        double out = -Math.log(sizeOut / 2.0 + 0.5)/0.693;

        return (float)Math.min(out, 4D) * getDataTracker().get(STABILITY);
    }

    private float processPower(){
        double sizeOut = power.getValue((ServerWorld)world, getDataTracker().get(GENETICS));

        double out = -Math.log(sizeOut / 2.0 + 0.5)/0.693;

        return (float)Math.min(out, 12D) * getDataTracker().get(STABILITY);
    }

    private StatusEffect getEffect(){
        double noisey = disease.getValues((ServerWorld) world, getDataTracker().get(GENETICS))[1];

        int hashable = (int)Math.floor(
            noisey * SpeshAnimals.allEffects.size() * 1.6D
        );

        int effectIndex = Math.abs(Integer.hashCode(
                hashable
        )) % SpeshAnimals.allEffects.size();

        return SpeshAnimals.allEffects.get(effectIndex);
    }

    protected AnimalEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected void initDataTracker() {
        getDataTracker().startTracking(GENETICS, (float)((random.nextFloat() - 0.5) * Gene.MIN_DIFFERENCE * 3.0F));
        getDataTracker().startTracking(STABILITY, 0.01F);
        super.initDataTracker();
    }

    @Inject(method = "breed", at = @At(value = "INVOKE", target = "net/minecraft/entity/passive/PassiveEntity.setBaby(Z)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void initBabyData(ServerWorld world, AnimalEntity other, CallbackInfo ci, PassiveEntity passiveEntity /* child */){
        float stability = other.getDataTracker().get(STABILITY) + getDataTracker().get(STABILITY);
        stability *= 0.55;

        for (StatusEffectInstance effect: other.getStatusEffects()){
            stability *= effect.getAmplifier() + 1.1;
        }

        for (StatusEffectInstance effect: getStatusEffects()){
            stability *= effect.getAmplifier() + 1.1;
        }

        float genetics = other.getDataTracker().get(GENETICS) + getDataTracker().get(GENETICS);
        float diff = other.getDataTracker().get(GENETICS) - getDataTracker().get(GENETICS);
        diff *= 0.05;
        genetics *= 0.5;
        genetics += diff;
        passiveEntity.getDataTracker().set(STABILITY, stability);
        passiveEntity.getDataTracker().set(GENETICS, genetics);
    }

    @Inject(method = "breed", at = @At(value = "INVOKE", target = "net/minecraft/server/world/ServerWorld.spawnEntityAndPassengers(Lnet/minecraft/entity/Entity;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
    private void loadBabyData(ServerWorld world, AnimalEntity other, CallbackInfo ci, PassiveEntity passiveEntity /* child */){
        ((AnimalInterface) passiveEntity).loadSize(world);
    }

    @Inject(method = "writeCustomDataToNbt", at = @At("TAIL"))
    private void writeNbt(NbtCompound nbt, CallbackInfo ci){
        nbt.putFloat("genetics", getDataTracker().get(GENETICS));
        nbt.putFloat("stability", getDataTracker().get(STABILITY));
    }

    @Inject(method = "readCustomDataFromNbt", at = @At("TAIL"))
    private void readNbt(NbtCompound nbt, CallbackInfo ci){
        if(nbt.contains("genetics")) getDataTracker().set(GENETICS, nbt.getFloat("genetics"));
        if(nbt.contains("stability")) getDataTracker().set(STABILITY, nbt.getFloat("stability"));
    }

    @Override
    public NbtList bookData() {
        NbtList out = new NbtList();
        String page1Str = """
                [
                    {"text":"Info on ","bold":true},
                    {"text":"%s","bold":true},
                    {"text":"\\nSize - %.2f\\n","bold":false},
                    {"text":"Speed - %.2f\\n","bold":false},
                    {"text":"Health - %.1f\\n","bold":false},
                    {"text":"Power - %.1f\\n","bold":false},
                    {"text":"------------\\n","bold":false},
                    {"text":"Stability - %.3f\\n","bold":false},
                    {"text":"Gene - %.2f\\n","bold":false},
                    {"text":"------------\\n","bold":false},
                    {"text":"%s Sick\\n","bold":false},
                    {"text":"%s Explosive","bold":false}
                ]
                """;

        //,"hoverEvent":{"action":"show_entity","contents":{"id":"%s"}}

        String entityName = new TranslatableText(getType().getTranslationKey()).getString();
        if(getCustomName() != null) entityName = getCustomName().asString();

        out.add(NbtString.of(String.format(page1Str,
                entityName,
                processSize(),
                processSpeed(),
                getMaxHealth(),
                processPower(),
                getDataTracker().get(STABILITY),
                getDataTracker().get(GENETICS),
                isSick() ? "Is" : "Isn't",
                isExplosive() ? "Is" : "Isn't"
        )));

        String page2Str = """
                [
                    {"text":"Side effect stats\\n","bold":true},
                    {"text":"These may not always apply..\\n\\n","bold":false},
                    {"text":"Explosion:\\n","bold":true,"color":"#444444"},
                    {"text":"Size - %.1f\\n","bold":false},
                    {"text":"------------\\n","bold":false},
                    {"text":"Disease:\\n","bold":true,"color":"#444444"},
                    {"text":"Effect - %s\\n","bold":false},
                    {"text":"Duration - %.1f secs\\n","bold":false},
                    {"text":"Amplifier - %d\\n","bold":false},
                    {"text":"Radius - %.1f","bold":false}
                ]
                """;

        double[] diseaseValues = disease.getValues((ServerWorld) world, getDataTracker().get(GENETICS));


        int duration = (int)(-Math.log(diseaseValues[2] * 0.5 + 0.5) * 80.0D);
        int amplifier = (int)(-Math.log(Math.abs(diseaseValues[3])) * 3.0D + 1.0D);

        out.add(NbtString.of(String.format(page2Str,
                1.5 * (explosivity.getValues((ServerWorld) world, getDataTracker().get(GENETICS))[1] + 1),
                (new TranslatableText(getEffect().getTranslationKey())).getString(),
                duration / 20.0,
                amplifier,
                (1 - diseaseValues[0]) * 3.0F
        )));

        return out;
    }

    @Override
    public float getGenetics(){
        return getDataTracker().get(GENETICS);
    }

    @Override
    public float getStability(){
        return getDataTracker().get(STABILITY);
    }

    @Override
    public void setGenetics(float val){
        getDataTracker().set(GENETICS, val);
    }

    @Override
    public void setStability(float val){
        getDataTracker().set(STABILITY, val);
    }
}


