package com.oe.rehooked.item.hook;

import com.oe.rehooked.client.KeyBindings;
import com.oe.rehooked.data.HookRegistry;
import com.oe.rehooked.handlers.hook.def.IClientPlayerHookHandler;
import com.oe.rehooked.handlers.hook.def.ICommonPlayerHookHandler;
import com.oe.rehooked.utils.CurioUtils;
import com.oe.rehooked.utils.HandlerHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.List;
import java.util.Optional;

public class HookItem extends Item implements ICurioItem {
    private final String hookType;

    public HookItem(String hookType) {
        this(new Item.Properties().defaultDurability(0).stacksTo(1), hookType);
    }

    public HookItem(Item.Properties properties, String hookType) {
        super(properties);
        this.hookType = hookType;
    }

    public String getHookType() {
        return hookType;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (!(stack.getItem() instanceof HookItem)) return;
        tooltipComponents.add(Component.translatable("tooltip.rehooked:" + hookType + "_hook.info").withStyle(ChatFormatting.GRAY));
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("tooltip.rehooked.press_fire",
                    KeyBindings.getKeyBindComponent(KeyBindings.FIRE_HOOK_KEY)).withStyle(ChatFormatting.GRAY));
            tooltipComponents.add(Component.translatable("tooltip.rehooked.press_retract",
                    KeyBindings.getKeyBindComponent(KeyBindings.RETRACT_HOOK_KEY)).withStyle(ChatFormatting.GRAY));
            HookRegistry.getHookData(hookType).ifPresent(hookData -> {
                if (!hookData.isCreative()) {
                    tooltipComponents.add(Component.translatable("tooltip.rehooked.press_retract_all",
                            KeyBindings.getKeyBindComponent(KeyBindings.REMOVE_ALL_HOOKS_KEY)).withStyle(ChatFormatting.GRAY));
                }
            });
        } else {
            tooltipComponents.add(Component.translatable("tooltip.rehooked.press_shift_more_info")
                    .withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public void onUnequip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player owner)) return;
        HandlerHelper.getHookHandler(owner).ifPresent(ICommonPlayerHookHandler::onUnequip);
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack prevStack, ItemStack stack) {
        if (!(slotContext.entity() instanceof Player owner)) return;
        HandlerHelper.getHookHandler(owner).ifPresent(ICommonPlayerHookHandler::onEquip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            if (!player.getCooldowns().isOnCooldown(this)) {
                player.getCooldowns().addCooldown(this, 5);
                ItemStack hookStack = player.getItemInHand(hand);

                if (hookStack.is(this)) {
                    Entity camera = Minecraft.getInstance().getCameraEntity();
                    if (camera != null) {
                        Optional<IClientPlayerHookHandler> optHandler = IClientPlayerHookHandler.FromPlayer(player).resolve();
                        IClientPlayerHookHandler handler = optHandler.get();
//                        handler.addHook(this.id);
                        handler.shootFromRotation(camera.getXRot(), camera.getYRot());

                    }
                }
            }
        }

        return super.use(level, player, hand);
    }
}
