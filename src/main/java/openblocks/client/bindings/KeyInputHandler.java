package openblocks.client.bindings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import openblocks.Config;
import openblocks.common.Vario;
import openblocks.events.PlayerActionEvent;

public class KeyInputHandler {

    private KeyBinding brickBinding;
    private KeyBinding varioSwitchBinding;
    private KeyBinding varioVolUpBinding;
    private KeyBinding varioVolDownBinding;

    private boolean brickKeyPressed;
    private boolean varioSwitchKeyPressed;
    private boolean varioVolUpKeyPressed;
    private boolean varioVolDownKeyPressed;

    public void setup() {
        if (!Config.soSerious) {
            brickBinding = new KeyBinding(
                    "openblocks.keybind.drop_brick",
                    Keyboard.KEY_B,
                    "openblocks.keybind.category");
            ClientRegistry.registerKeyBinding(brickBinding);
        }
        if (Config.hanggliderEnableThermal) {
            varioSwitchBinding = new KeyBinding(
                    "openblocks.keybind.vario_switch",
                    Keyboard.KEY_NONE,
                    "openblocks.keybind.category");
            varioVolUpBinding = new KeyBinding(
                    "openblocks.keybind.vario_vol_up",
                    Keyboard.KEY_NONE,
                    "openblocks.keybind.category");
            varioVolDownBinding = new KeyBinding(
                    "openblocks.keybind.vario_vol_down",
                    Keyboard.KEY_NONE,
                    "openblocks.keybind.category");
            ClientRegistry.registerKeyBinding(varioSwitchBinding);
            ClientRegistry.registerKeyBinding(varioVolUpBinding);
            ClientRegistry.registerKeyBinding(varioVolDownBinding);
        }
        FMLCommonHandler.instance().bus().register(this);
    }

    private static boolean isNastyStuffAllowed() {
        return !Config.soSerious && (Config.fartTypying || Minecraft.getMinecraft().currentScreen == null);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.KeyInputEvent evt) {
        if (brickBinding != null && brickBinding.isPressed()) {
            if (!brickKeyPressed) {
                if (isNastyStuffAllowed()) new PlayerActionEvent(PlayerActionEvent.Type.BOO).sendToServer();
                brickKeyPressed = true;
            }
        } else brickKeyPressed = false;
        if (varioSwitchBinding != null && varioSwitchBinding.isPressed()) {
            if (!varioSwitchKeyPressed) {
                Vario.instance.toggle();
                varioSwitchKeyPressed = true;
            }
        } else varioSwitchKeyPressed = false;
        if (varioVolUpBinding != null && varioVolUpBinding.isPressed()) {
            if (!varioVolUpKeyPressed) {
                Vario.instance.incVolume();
                varioVolUpKeyPressed = true;
            }
        } else varioVolUpKeyPressed = false;
        if (varioVolDownBinding != null && varioVolDownBinding.isPressed()) {
            if (!varioVolDownKeyPressed) {
                Vario.instance.decVolume();
                varioVolDownKeyPressed = true;
            }
        } else varioVolDownKeyPressed = false;
    }

}
