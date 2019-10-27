package com.kapiteon.multimod;

import com.kapiteon.Helper;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetHandlerPlayClientWrapper;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerCapabilities;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketInput;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerAbilities;
import net.minecraft.world.GameType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(
        modid = FreerCamForge.MOD_ID,
        version = FreerCamForge.VERSION,
        name = FreerCamForge.NAME,
        acceptedMinecraftVersions = FreerCamForge.MINECRAFT_VERSIONS
)
public class FreerCamForge {
    static final String MOD_ID = "freercam";
    static final String VERSION = "1.1";
    static final String MINECRAFT_VERSIONS = "[1.12.2]";
    public static final String NAME = "FreerCam";

    private static final Logger LOGGER = LogManager.getLogger(NAME);

    private NetHandlerPlayClientWrapper netHandler;

    private static FreerCamForge instance = null;

    @EventHandler
    public void init(FMLInitializationEvent event) {
        instance = this;

        this.keys = new KeyBinding[1];
        this.keys[0] = new KeyBinding(NAME + " (ON/OFF)", 64, NAME); // 64 = F6
        MinecraftForge.EVENT_BUS.register(this);
        for (KeyBinding key : this.keys) {
            ClientRegistry.registerKeyBinding(key);
        }
        this.mc = Minecraft.getMinecraft();
    }


    @EventHandler
    public void init(FMLPreInitializationEvent event) {
    }


    @SideOnly(Side.CLIENT)
    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public void onEvent(InputEvent.KeyInputEvent event) {
        if (this.keys[0].isPressed()) {
            if (this.enabled) {
                Disable();
            } else {
                Enable();
            }
            LOGGER.log(Level.INFO, "FreerCam Toggled " + (this.enabled ? "on" : "off"));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onConnect(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        try {
            NetworkManager networkManager;
            if (mc.currentScreen instanceof net.minecraft.client.multiplayer.GuiConnecting) {
                networkManager = (NetworkManager) Helper.getValueReflection(mc.currentScreen, NetworkManager.class);
            } else {
                networkManager = (NetworkManager) Helper.getValueReflection(mc, NetworkManager.class);
            }

            if (networkManager != null) {
                INetHandler inh = (INetHandler) Helper.getValueReflection(networkManager, INetHandler.class);
                if (inh instanceof NetHandlerPlayClient) {
                    NetHandlerPlayClient nhpc = (NetHandlerPlayClient) inh;

                    //noinspection ConstantConditions
                    this.netHandler = new NetHandlerPlayClientWrapper(mc, mc.currentScreen, networkManager, nhpc.getGameProfile(), nhpc, this);

                    networkManager.setNetHandler(this.netHandler);
                    FMLClientHandler.instance().setPlayClient(this.netHandler);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


//    @SideOnly(Side.CLIENT)
//    @SubscribeEvent(priority = EventPriority.HIGH, receiveCanceled = true)
//    public void onEvent(RenderGameOverlayEvent.Text event) {
//        List<String> list = new ArrayList<String>();
//        list.add(TextFormatting.RESET + NAME + " enabled: " + (this.enabled ? TextFormatting.GREEN : TextFormatting.RED) +
//                String.valueOf(this.enabled));
//
//        Minecraft mc = Minecraft.getMinecraft();
//        for (int i = 0; i < list.size(); i++) {
//            String s = (String) list.get(i);
//
//            if (!Strings.isNullOrEmpty(s)) {
//                int j = mc.fontRenderer.FONT_HEIGHT;
//                int k = mc.fontRenderer.getStringWidth(s);
//                int l = (new ScaledResolution(mc)).getScaledWidth() - 2 - k;
//                int i1 = 2 + j * i;
//                Gui.drawRect(l - 1, i1 - 1, l + k + 1, i1 + j - 1, -1873784752);
//                mc.fontRenderer.drawString(s, l, i1, 14737632);
//            }
//        }
//    }


    private static FreerCamForge getInstance() {
        return instance;
    }

    private KeyBinding[] keys = new KeyBinding[0];

    private boolean enabled = false;
    public Minecraft mc;
    private double posX_fake;
    private double posY_fake;
    private double posZ_fake;
    private float yaw_fake;
    private float pitch_fake;
    private GameType oldGameType;
    private PlayerCapabilities oldCapabilities;

    private void Enable() {
        this.posX_fake = this.mc.player.posX;
        this.posY_fake = this.mc.player.posY;
        this.posZ_fake = this.mc.player.posZ;
        this.yaw_fake = this.mc.player.rotationYaw;
        this.pitch_fake = this.mc.player.rotationPitch;

        NetHandlerPlayClientWrapper.sendProcessors.add(packetIn -> {
            if (packetIn instanceof CPacketPlayerAbilities) {
                // CPacketPlayerAbilities packet = (CPacketPlayerAbilities) packetIn;
                (getInstance()).netHandler.wrapped.sendPacket(new CPacketPlayerAbilities(this.oldCapabilities));
                return Boolean.TRUE;
            }
            if (packetIn instanceof CPacketInput) {
                // CPacketInput packet = (CPacketInput) packetIn;
                return Boolean.TRUE;
            }
            if (packetIn instanceof CPacketEntityAction) {
                // CPacketEntityAction packet = (CPacketEntityAction) packetIn;
                return Boolean.TRUE;
            }
            if (packetIn.getClass() == CPacketPlayer.class) {
                // CPacketPlayer packet = (CPacketPlayer) packetIn;
                (getInstance()).netHandler.wrapped.sendPacket(new CPacketPlayer(true));
                return Boolean.TRUE;
            }
            if (packetIn instanceof CPacketPlayer.Position) {
                // CPacketPlayer.Position packet = (CPacketPlayer.Position) packetIn;
                (getInstance()).netHandler.wrapped.sendPacket(
                        new CPacketPlayer.Position(this.posX_fake, this.posY_fake, this.posZ_fake, true));
                return Boolean.TRUE;
            }
            if (packetIn instanceof CPacketPlayer.PositionRotation) {
                // CPacketPlayer.PositionRotation packet = (CPacketPlayer.PositionRotation) packetIn;
                (getInstance()).netHandler.wrapped.sendPacket(
                        new CPacketPlayer.PositionRotation(this.posX_fake, this.posY_fake, this.posZ_fake, this.yaw_fake, this.pitch_fake, true));
                return Boolean.TRUE;
            }
            return Boolean.FALSE;
        });

        for (Field field : this.mc.player.getClass().getDeclaredFields()) {
            if (field.getType() == NetHandlerPlayClient.class) {
                field.setAccessible(true);
                try {
                    field.set(this.mc.player, (getInstance()).netHandler);
                    break;
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        for (Field field : this.mc.playerController.getClass().getDeclaredFields()) {
            if (field.getType() == NetHandlerPlayClient.class) {
                field.setAccessible(true);
                try {
                    field.set(this.mc.playerController, (getInstance()).netHandler);
                    break;
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
        this.oldGameType = this.mc.playerController.getCurrentGameType();
        this.oldCapabilities = this.mc.player.capabilities;
        this.mc.player.capabilities = new PlayerCapabilities();
        this.mc.player.capabilities.allowFlying = true;
        this.mc.player.capabilities.isFlying = true;
        this.mc.playerController.setGameType(GameType.SPECTATOR);
        this.mc.player.setGameType(GameType.SPECTATOR);
        (getInstance()).netHandler.setGameType(this.mc.player.getGameProfile().getId(), GameType.SPECTATOR);
        this.enabled = true;
    }

    private void Disable() {
        this.mc.player.capabilities = this.oldCapabilities;
        (getInstance()).netHandler.setGameType(this.mc.player.getGameProfile().getId(), this.oldGameType);
        this.mc.playerController.setGameType(this.oldGameType);
        this.mc.player.setGameType(this.oldGameType);
        NetHandlerPlayClientWrapper.sendProcessors.clear();

        this.mc.player.setPosition(this.posX_fake, this.posY_fake, this.posZ_fake);
        this.mc.player.setPositionAndRotation(this.posX_fake, this.posY_fake, this.posZ_fake, this.yaw_fake, this.pitch_fake);
        this.mc.player.motionX = 0.0D;
        this.mc.player.motionY = 0.0D;
        this.mc.player.motionZ = 0.0D;

        for (Field field : this.mc.player.getClass().getDeclaredFields()) {
            if (field.getType() == NetHandlerPlayClient.class) {
                field.setAccessible(true);
                try {
                    field.set(this.mc.player, (getInstance()).netHandler.wrapped);
                    break;
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        for (Field field : this.mc.playerController.getClass().getDeclaredFields()) {
            if (field.getType() == NetHandlerPlayClient.class) {
                field.setAccessible(true);
                try {
                    field.set(this.mc.playerController, (getInstance()).netHandler.wrapped);
                    break;
                } catch (IllegalArgumentException | IllegalAccessException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        this.enabled = false;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

//    public KeyBinding[] getKeys() {
//        return this.keys;
//    }
}