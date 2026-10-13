package com.example.b52airstrike;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod(B52Airstrike.MODID)
public class B52Airstrike {
    public static final String MODID = "b52airstrike";
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> RADIO = ITEMS.register("radio",
        () -> new AirstrikeRadio(new Item.Properties().stacksTo(1)));

    public B52Airstrike() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(bus);
        bus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(RADIO);
        }
    }

    public static class AirstrikeRadio extends Item {
        public AirstrikeRadio(Properties properties) { super(properties); }

        @Override
        public InteractionResultHolder<net.minecraft.world.item.ItemStack> use(
                Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            HitResult hit = player.pick(128.0D, 0.0F, false);
            if (!(hit instanceof BlockHitResult blockHit)) {
                if (!level.isClientSide) player.displayClientMessage(
                    Component.literal("Нет цели в пределах 128 блоков."), true);
                return InteractionResultHolder.fail(stack);
            }

            if (!level.isClientSide) {
                double tx = blockHit.getLocation().x;
                double ty = blockHit.getLocation().y;
                double tz = blockHit.getLocation().z;
                player.displayClientMessage(Component.literal("B-52 вызван! Авиаудар через несколько секунд..."), true);

                // Prototype airstrike: a line of TNT bombs arrives in staggered waves.
                // The vanilla TNT entity supplies the falling-bomb visuals and explosion.
                int count = 9;
                for (int i = 0; i < count; i++) {
                    double offset = (i - (count - 1) / 2.0) * 3.0;
                    PrimedTnt bomb = new PrimedTnt(level, tx + offset, ty + 28.0, tz - 8.0, player);
                    bomb.setFuse(55 + i * 7);
                    level.addFreshEntity(bomb);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
    }
}
