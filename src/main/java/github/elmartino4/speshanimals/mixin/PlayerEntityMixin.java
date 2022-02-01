package github.elmartino4.speshanimals.mixin;

import github.elmartino4.speshanimals.util.AnimalInterface;
import github.elmartino4.speshanimals.util.FishInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.ZoglinEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
    @Shadow public abstract String getEntityName();

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interact", at = @At("TAIL"), cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD)
    private void interactWithBook(Entity entity, Hand hand, CallbackInfoReturnable<ActionResult> cir, ItemStack itemStack) {
        if(world instanceof ServerWorld) {
            if (itemStack.getItem() == Items.WRITABLE_BOOK && (entity instanceof AnimalEntity || entity instanceof ZoglinEntity)) {
                giveBook(entity, hand, ((AnimalInterface) entity).getGenetics().bookData());

                cir.setReturnValue(ActionResult.SUCCESS);
            } else if (itemStack.getItem() == Items.WRITABLE_BOOK && (entity instanceof FishEntity)) {
                giveBook(entity, hand, ((FishInterface) entity).bookData());

                cir.setReturnValue(ActionResult.SUCCESS);
            }
        }
    }

    private void giveBook(Entity entity, Hand hand, NbtList book) {
        ItemStack newBook = new ItemStack(Items.WRITTEN_BOOK);
        newBook.getOrCreateNbt().put("pages", book);
        newBook.getOrCreateNbt().putString("author", "[" + getEntityName() + "]");

        String entityName = (new TranslatableText("" + entity.getType().getTranslationKey())).getString();
        if (entity.getCustomName() != null) entityName = entity.getDisplayName().asString();
        newBook.getOrCreateNbt().putString("title", "Information on " + entityName);

        setStackInHand(hand, newBook);
    }
}
