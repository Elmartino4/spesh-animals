package github.elmartino4.speshanimals.mixin;

import github.elmartino4.speshanimals.util.AnimalInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.lwjgl.system.CallbackI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Consumer;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @ModifyArg(method = "dropLoot", at = @At(value = "INVOKE", target = "net/minecraft/loot/LootTable.generateLoot(Lnet/minecraft/loot/context/LootContext;Ljava/util/function/Consumer;)V"))
    private Consumer<ItemStack> modifyLoot(Consumer<ItemStack> previous){
        if((Object) this instanceof AnimalEntity)
            return this::dropLootStacks;

        return previous;
    }

    private void dropLootStacks(ItemStack itmStack){
        double count = itmStack.getCount();
        double multiplier = ((AnimalInterface) (Object) this).getGenetics().processSize();
        count *= multiplier * multiplier * multiplier;
        dropStack(new ItemStack(itmStack.getItem(), (int)Math.ceil(count)));
    }
}
