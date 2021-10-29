package github.elmartino4.speshanimals.util;

import net.minecraft.nbt.NbtList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;

public interface AnimalInterface {
    NbtList bookData() throws IOException;
    void loadSize(World world);
    float getGenetics();
    float getStability();
    void setGenetics(float val);
    void setStability(float val);
    void speshTick();
    float processSize();
}
