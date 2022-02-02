package github.elmartino4.speshanimals.genetics;

import github.elmartino4.speshanimals.SpeshAnimals;
import net.minecraft.entity.AreaEffectCloudEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class Genetics {
    public float GENETICS;
    public float STABILITY;
    protected final MobEntity entity;
    protected final World world;

    protected static final Gene size = new Gene(1, 0);
    protected static final Gene motion = new Gene(1, 1);
    protected static final Gene health = new Gene(1, 2);
    protected static final Gene power = new Gene(1, 3);
    protected static final Gene explosivity = new Gene(2, 5);  //probability, size
    protected static final Gene disease = new Gene(4, 7);   //probability, effect, duration, amplifier

    protected int speshTimer = 0;

    public Genetics(World world, MobEntity entity) { //new entity
        GENETICS = (float) ((world.random.nextFloat() - 0.5) * Gene.MIN_DIFFERENCE * 3.0F);
        STABILITY = 0.01F;
        this.world = world;
        this.entity = entity;
    }

    public Genetics(World world, MobEntity entity, Genetics parent0, Genetics parent1) { //child entity
        float stability = parent0.STABILITY + parent1.STABILITY;
        stability *= 0.55;

        for (StatusEffectInstance effect : parent0.entity.getStatusEffects()) {
            stability *= effect.getAmplifier() + 1.1;
        }

        for (StatusEffectInstance effect : parent1.entity.getStatusEffects()) {
            stability *= effect.getAmplifier() + 1.1;
        }

        float genetics = parent0.GENETICS + parent1.GENETICS;
        float diff = parent1.GENETICS - parent0.GENETICS;
        diff *= 0.05;
        genetics *= 0.5;
        genetics += diff;

        GENETICS = genetics;
        STABILITY = stability;
        this.world = world;
        this.entity = entity;
    }

    public void readNbt(NbtCompound nbt){
        if(nbt.contains("genetics")) GENETICS = nbt.getFloat("genetics");
        if(nbt.contains("stability")) STABILITY = nbt.getFloat("stability");
    }

    public void writeNbt(NbtCompound nbt){
        nbt.putFloat("genetics", GENETICS);
        nbt.putFloat("stability", STABILITY);
    }

    public void loadSize() {
        if (world instanceof ServerWorld) {
            ScaleData widthData = ScaleTypes.WIDTH.getScaleData(entity);
            widthData.resetScale();
            widthData.setScale(processSize());
            widthData.onUpdate();

            ScaleData heightData = ScaleTypes.HEIGHT.getScaleData(entity);
            heightData.resetScale();
            heightData.setScale(processSize());
            heightData.onUpdate();

            ScaleData motionData = ScaleTypes.MOTION.getScaleData(entity);
            motionData.resetScale();
            motionData.setScale(processSpeed());
            motionData.onUpdate();

            ScaleData healthData = ScaleTypes.HEALTH.getScaleData(entity);
            healthData.resetScale();
            healthData.setScale(processHealth());
            healthData.onUpdate();

            ScaleData attackData = ScaleTypes.ATTACK.getScaleData(entity);
            attackData.resetScale();
            attackData.setScale(processPower());
            attackData.onUpdate();

            ScaleData defenseData = ScaleTypes.DEFENSE.getScaleData(entity);
            defenseData.resetScale();
            defenseData.setScale(processPower());
            defenseData.onUpdate();
        }
    }

    public NbtList bookData() {
        System.out.println(GENETICS);
        System.out.println(STABILITY);

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
                    {"text":"Instability - %.3f\\n","bold":false},
                    {"text":"Gene - %.2f\\n","bold":false},
                    {"text":"------------\\n","bold":false},
                    {"text":"%s Sick\\n","bold":false},
                    {"text":"%s Explosive","bold":false}
                ]
                """;

        //,"hoverEvent":{"action":"show_entity","contents":{"id":"%s"}}

        String entityName = new TranslatableText(entity.getType().getTranslationKey()).getString();
        if (entity.getCustomName() != null) entityName = entity.getCustomName().asString();

        out.add(NbtString.of(String.format(page1Str,
                entityName,
                processSize(),
                processSpeed(),
                entity.getMaxHealth(),
                processPower(),
                STABILITY,
                GENETICS,
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

        double[] diseaseValues = disease.getValues((ServerWorld) world, GENETICS);


        int duration = (int) (-Math.log(diseaseValues[2] * 0.5 + 0.5) * 160.0D);
        int amplifier = (int) (-Math.log(Math.abs(diseaseValues[3])) * 3.0D + 1.0D);

        out.add(NbtString.of(String.format(page2Str,
                1.5 * (explosivity.getValues((ServerWorld) world, GENETICS)[1] + 1),
                (new TranslatableText(getEffect().getTranslationKey())).getString(),
                duration / 20.0,
                amplifier,
                (1 - diseaseValues[0]) * 3.0F
        )));

        return out;
    }

    public void speshTick() {
        if (speshTimer++ > 16) {
            speshTimer = 0;
            if (world instanceof ServerWorld) {
                double[] explosivityValues = explosivity.getValues((ServerWorld) world, GENETICS);

                if (isExplosive() && !entity.inPowderSnow) {
                    if ((explosivityValues[0] + 1) / 8D > world.random.nextFloat() || (entity).getFireTicks() > 0) {
                        explosivityValues[1] += 1;
                        explosivityValues[1] *= 1.5;

                        explosivityValues[0] *= 0.25;

                        world.createExplosion(entity, entity.getX(), entity.getY(), entity.getZ(), (float) explosivityValues[1], (world.random.nextFloat() < explosivityValues[0]), Explosion.DestructionType.DESTROY);
                    }
                }

                double[] diseaseValues = disease.getValues((ServerWorld) world, GENETICS);

                if (isSick()) {
                    if ((diseaseValues[0] + 1) / 4.0 > world.random.nextFloat()) {
                        AreaEffectCloudEntity effectCloud = new AreaEffectCloudEntity(world, entity.getX(), entity.getY(), entity.getZ());
                        effectCloud.setRadius((float) (1 - diseaseValues[0]) * 3.0F);
                        int duration = (int) (-Math.log(diseaseValues[2] * 0.5 + 0.5) * 160.0D);
                        int amplifier = (int) (-Math.log(Math.abs(diseaseValues[3])) * 3.0D);
                        effectCloud.addEffect(new StatusEffectInstance(getEffect(), duration, amplifier));

                        effectCloud.setDuration(24);

                        world.spawnEntity(effectCloud);
                    }
                }
            }
        }
    }

    protected boolean isExplosive() {
        return (explosivity.getValue((ServerWorld) world, GENETICS) + 1) * 2.0 < STABILITY;
    }

    protected boolean isSick() {
        return (disease.getValue((ServerWorld) world, GENETICS) + 1) * 2.0 < STABILITY;
    }

    public float processSize() {
        double sizeOut = size.getValue((ServerWorld) world, GENETICS) + 1;
        double size;

        if (sizeOut > 1) {
            size = STABILITY * Math.log(sizeOut) + 1;
        } else {
            size = Math.pow(Math.E, STABILITY * (sizeOut - 1));
        }

        return Math.max((float) size, 0.25F);
    }

    protected float processSpeed() {
        double sizeOut = motion.getValue((ServerWorld) world, GENETICS) + 1;
        double size;

        if (sizeOut > 1) {
            size = STABILITY * Math.log(sizeOut) + 1;
        } else {
            size = Math.pow(Math.E, STABILITY * (sizeOut - 1));
        }

        return (float) size;
    }

    protected float processHealth() {
        double sizeOut = health.getValue((ServerWorld) world, GENETICS) + 1;
        double size;

        if (sizeOut > 1) {
            size = STABILITY * Math.log(sizeOut) + 1;
        } else {
            size = Math.pow(Math.E, STABILITY * (sizeOut - 1));
        }

        return (float) size;
    }

    protected float processPower() {
        double sizeOut = power.getValue((ServerWorld) world, GENETICS);

        double out = -Math.log(sizeOut / 2.0 + 0.5) / 0.693;

        return (float) Math.pow(Math.min(out, 12D), STABILITY);
    }

    protected StatusEffect getEffect() {
        double noisey = disease.getValues((ServerWorld) world, GENETICS)[1];

        int hashable = (int) Math.floor(
                noisey * SpeshAnimals.allEffects.size() * 1.6D
        );

        int effectIndex = Math.abs(Integer.hashCode(
                hashable
        )) % SpeshAnimals.allEffects.size();

        return SpeshAnimals.allEffects.get(effectIndex);
    }
}
