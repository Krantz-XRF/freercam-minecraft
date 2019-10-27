package net.minecraft.client.network;

import com.kapiteon.multimod.FreerCamForge;
import com.mojang.authlib.GameProfile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.ClientAdvancementManager;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketThreadUtil;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketAdvancementInfo;
import net.minecraft.network.play.server.SPacketAnimation;
import net.minecraft.network.play.server.SPacketBlockAction;
import net.minecraft.network.play.server.SPacketBlockBreakAnim;
import net.minecraft.network.play.server.SPacketBlockChange;
import net.minecraft.network.play.server.SPacketCamera;
import net.minecraft.network.play.server.SPacketChangeGameState;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketChunkData;
import net.minecraft.network.play.server.SPacketCloseWindow;
import net.minecraft.network.play.server.SPacketCollectItem;
import net.minecraft.network.play.server.SPacketCombatEvent;
import net.minecraft.network.play.server.SPacketConfirmTransaction;
import net.minecraft.network.play.server.SPacketCooldown;
import net.minecraft.network.play.server.SPacketCustomPayload;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketDisconnect;
import net.minecraft.network.play.server.SPacketDisplayObjective;
import net.minecraft.network.play.server.SPacketEffect;
import net.minecraft.network.play.server.SPacketEntity;
import net.minecraft.network.play.server.SPacketEntityAttach;
import net.minecraft.network.play.server.SPacketEntityEffect;
import net.minecraft.network.play.server.SPacketEntityEquipment;
import net.minecraft.network.play.server.SPacketEntityHeadLook;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.network.play.server.SPacketEntityProperties;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityTeleport;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketHeldItemChange;
import net.minecraft.network.play.server.SPacketJoinGame;
import net.minecraft.network.play.server.SPacketKeepAlive;
import net.minecraft.network.play.server.SPacketMaps;
import net.minecraft.network.play.server.SPacketMoveVehicle;
import net.minecraft.network.play.server.SPacketMultiBlockChange;
import net.minecraft.network.play.server.SPacketOpenWindow;
import net.minecraft.network.play.server.SPacketParticles;
import net.minecraft.network.play.server.SPacketPlaceGhostRecipe;
import net.minecraft.network.play.server.SPacketPlayerAbilities;
import net.minecraft.network.play.server.SPacketPlayerListHeaderFooter;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.network.play.server.SPacketRecipeBook;
import net.minecraft.network.play.server.SPacketRemoveEntityEffect;
import net.minecraft.network.play.server.SPacketResourcePackSend;
import net.minecraft.network.play.server.SPacketRespawn;
import net.minecraft.network.play.server.SPacketScoreboardObjective;
import net.minecraft.network.play.server.SPacketSelectAdvancementsTab;
import net.minecraft.network.play.server.SPacketServerDifficulty;
import net.minecraft.network.play.server.SPacketSetExperience;
import net.minecraft.network.play.server.SPacketSetPassengers;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.network.play.server.SPacketSignEditorOpen;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnExperienceOrb;
import net.minecraft.network.play.server.SPacketSpawnGlobalEntity;
import net.minecraft.network.play.server.SPacketSpawnMob;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.network.play.server.SPacketSpawnPainting;
import net.minecraft.network.play.server.SPacketSpawnPlayer;
import net.minecraft.network.play.server.SPacketSpawnPosition;
import net.minecraft.network.play.server.SPacketStatistics;
import net.minecraft.network.play.server.SPacketTabComplete;
import net.minecraft.network.play.server.SPacketTeams;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.network.play.server.SPacketTitle;
import net.minecraft.network.play.server.SPacketUnloadChunk;
import net.minecraft.network.play.server.SPacketUpdateBossInfo;
import net.minecraft.network.play.server.SPacketUpdateHealth;
import net.minecraft.network.play.server.SPacketUpdateScore;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.network.play.server.SPacketUseBed;
import net.minecraft.network.play.server.SPacketWindowItems;
import net.minecraft.network.play.server.SPacketWindowProperty;
import net.minecraft.network.play.server.SPacketWorldBorder;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.GameType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class NetHandlerPlayClientWrapper extends NetHandlerPlayClient {
    private Minecraft mc;
    private FreerCamForge fc;
    public NetHandlerPlayClient wrapped;
    public static final ArrayList<Function<Packet<?>, Boolean>> sendProcessors = new ArrayList<>();

    public NetHandlerPlayClientWrapper(Minecraft mcIn, GuiScreen guiScreenIn, NetworkManager networkManagerIn,
                                       GameProfile profileIn, NetHandlerPlayClient wrapped, FreerCamForge fc) {
        super(mcIn, guiScreenIn, networkManagerIn, profileIn);
        this.fc = fc;
        this.mc = fc.mc;
        this.wrapped = wrapped;
    }

//    public NetHandlerPlayClientWrapper(Minecraft mcIn, GuiScreen guiScreenIn, NetworkManager networkManagerIn, GameProfile profileIn) {
//        super(mcIn, guiScreenIn, networkManagerIn, profileIn);
//    }

    public void setGameType(UUID uniqueId, GameType gameType) {
        NetworkPlayerInfo pi = this.wrapped.getPlayerInfo(uniqueId);
        pi.setGameType(gameType);
    }

    //#region custom overrides
    @Override
    public void handlePlayerPosLook(SPacketPlayerPosLook packetIn) {
        PacketThreadUtil.checkThreadAndEnqueue(packetIn, this, this.mc);
        if (this.fc.isEnabled()) {
            this.wrapped.sendPacket(new CPacketConfirmTeleport(packetIn.getTeleportId()));
            this.wrapped.sendPacket(new CPacketPlayer.PositionRotation(packetIn.getX(), packetIn.getY(), packetIn.getZ(), packetIn
                    .getYaw(), packetIn.getPitch(), false));
        } else {
            this.wrapped.handlePlayerPosLook(packetIn);
        }
    }

    @Override
    public void sendPacket(Packet<?> packetIn) {
        for (Function<Packet<?>, Boolean> processor : sendProcessors) {
            if (processor.apply(packetIn)) {
                return;
            }
        }
        this.wrapped.sendPacket(packetIn);
    }

    @Override
    public void handleChangeGameState(SPacketChangeGameState packetIn) {
        if (!this.fc.isEnabled()) {
            this.wrapped.handleChangeGameState(packetIn);
        }
    }

    @Override
    public void handlePlayerAbilities(SPacketPlayerAbilities packetIn) {
        if (!this.fc.isEnabled()) {
            this.wrapped.handlePlayerAbilities(packetIn);
        }
    }
    //#endregion

    //#region pass-through overrides
    @Override
    public void cleanup() {
        this.wrapped.cleanup();
    }

    @Override
    public void handleJoinGame(SPacketJoinGame packetIn) {
        this.wrapped.handleJoinGame(packetIn);
    }

    @Override
    public void handleSpawnObject(SPacketSpawnObject packetIn) {
        this.wrapped.handleSpawnObject(packetIn);
    }

    @Override
    public void handleSpawnExperienceOrb(SPacketSpawnExperienceOrb packetIn) {
        this.wrapped.handleSpawnExperienceOrb(packetIn);
    }

    @Override
    public void handleSpawnGlobalEntity(SPacketSpawnGlobalEntity packetIn) {
        this.wrapped.handleSpawnGlobalEntity(packetIn);
    }

    @Override
    public void handleSpawnPainting(SPacketSpawnPainting packetIn) {
        this.wrapped.handleSpawnPainting(packetIn);
    }

    @Override
    public void handleEntityVelocity(SPacketEntityVelocity packetIn) {
        this.wrapped.handleEntityVelocity(packetIn);
    }

    @Override
    public void handleEntityMetadata(SPacketEntityMetadata packetIn) {
        this.wrapped.handleEntityMetadata(packetIn);
    }

    @Override
    public void handleSpawnPlayer(SPacketSpawnPlayer packetIn) {
        this.wrapped.handleSpawnPlayer(packetIn);
    }

    @Override
    public void handleEntityTeleport(SPacketEntityTeleport packetIn) {
        this.wrapped.handleEntityTeleport(packetIn);
    }

    @Override
    public void handleHeldItemChange(SPacketHeldItemChange packetIn) {
        this.wrapped.handleHeldItemChange(packetIn);
    }

    @Override
    public void handleEntityMovement(SPacketEntity packetIn) {
        this.wrapped.handleEntityMovement(packetIn);
    }

    @Override
    public void handleEntityHeadLook(SPacketEntityHeadLook packetIn) {
        this.wrapped.handleEntityHeadLook(packetIn);
    }

    @Override
    public void handleDestroyEntities(SPacketDestroyEntities packetIn) {
        this.wrapped.handleDestroyEntities(packetIn);
    }

    @Override
    public void handleMultiBlockChange(SPacketMultiBlockChange packetIn) {
        this.wrapped.handleMultiBlockChange(packetIn);
    }

    @Override
    public void handleChunkData(SPacketChunkData packetIn) {
        this.wrapped.handleChunkData(packetIn);
    }

    @Override
    public void processChunkUnload(SPacketUnloadChunk packetIn) {
        this.wrapped.processChunkUnload(packetIn);
    }

    @Override
    public void handleBlockChange(SPacketBlockChange packetIn) {
        this.wrapped.handleBlockChange(packetIn);
    }

    @Override
    public void handleDisconnect(SPacketDisconnect packetIn) {
        this.wrapped.handleDisconnect(packetIn);
    }

    @Override
    public void onDisconnect(ITextComponent reason) {
        this.wrapped.onDisconnect(reason);
    }

    @Override
    public void handleCollectItem(SPacketCollectItem packetIn) {
        this.wrapped.handleCollectItem(packetIn);
    }

    @Override
    public void handleChat(SPacketChat packetIn) {
        this.wrapped.handleChat(packetIn);
    }

    @Override
    public void handleAnimation(SPacketAnimation packetIn) {
        this.wrapped.handleAnimation(packetIn);
    }

    @Override
    public void handleUseBed(SPacketUseBed packetIn) {
        this.wrapped.handleUseBed(packetIn);
    }

    @Override
    public void handleSpawnMob(SPacketSpawnMob packetIn) {
        this.wrapped.handleSpawnMob(packetIn);
    }

    @Override
    public void handleTimeUpdate(SPacketTimeUpdate packetIn) {
        this.wrapped.handleTimeUpdate(packetIn);
    }

    @Override
    public void handleSpawnPosition(SPacketSpawnPosition packetIn) {
        this.wrapped.handleSpawnPosition(packetIn);
    }

    @Override
    public void handleSetPassengers(SPacketSetPassengers packetIn) {
        this.wrapped.handleSetPassengers(packetIn);
    }

    @Override
    public void handleEntityAttach(SPacketEntityAttach packetIn) {
        this.wrapped.handleEntityAttach(packetIn);
    }

    @Override
    public void handleEntityStatus(SPacketEntityStatus packetIn) {
        this.wrapped.handleEntityStatus(packetIn);
    }

    @Override
    public void handleUpdateHealth(SPacketUpdateHealth packetIn) {
        this.wrapped.handleUpdateHealth(packetIn);
    }

    @Override
    public void handleSetExperience(SPacketSetExperience packetIn) {
        this.wrapped.handleSetExperience(packetIn);
    }

    @Override
    public void handleRespawn(SPacketRespawn packetIn) {
        this.wrapped.handleRespawn(packetIn);
    }

    @Override
    public void handleExplosion(SPacketExplosion packetIn) {
        this.wrapped.handleExplosion(packetIn);
    }

    @Override
    public void handleOpenWindow(SPacketOpenWindow packetIn) {
        this.wrapped.handleOpenWindow(packetIn);
    }

    @Override
    public void handleSetSlot(SPacketSetSlot packetIn) {
        this.wrapped.handleSetSlot(packetIn);
    }

    @Override
    public void handleConfirmTransaction(SPacketConfirmTransaction packetIn) {
        this.wrapped.handleConfirmTransaction(packetIn);
    }

    @Override
    public void handleWindowItems(SPacketWindowItems packetIn) {
        this.wrapped.handleWindowItems(packetIn);
    }

    @Override
    public void handleSignEditorOpen(SPacketSignEditorOpen packetIn) {
        this.wrapped.handleSignEditorOpen(packetIn);
    }

    @Override
    public void handleUpdateTileEntity(SPacketUpdateTileEntity packetIn) {
        this.wrapped.handleUpdateTileEntity(packetIn);
    }

    @Override
    public void handleWindowProperty(SPacketWindowProperty packetIn) {
        this.wrapped.handleWindowProperty(packetIn);
    }

    @Override
    public void handleEntityEquipment(SPacketEntityEquipment packetIn) {
        this.wrapped.handleEntityEquipment(packetIn);
    }

    @Override
    public void handleCloseWindow(SPacketCloseWindow packetIn) {
        this.wrapped.handleCloseWindow(packetIn);
    }

    @Override
    public void handleBlockAction(SPacketBlockAction packetIn) {
        this.wrapped.handleBlockAction(packetIn);
    }

    @Override
    public void handleBlockBreakAnim(SPacketBlockBreakAnim packetIn) {
        this.wrapped.handleBlockBreakAnim(packetIn);
    }

    @Override
    public void handleMaps(SPacketMaps packetIn) {
        this.wrapped.handleMaps(packetIn);
    }

    @Override
    public void handleEffect(SPacketEffect packetIn) {
        this.wrapped.handleEffect(packetIn);
    }

    @Override
    public void handleAdvancementInfo(SPacketAdvancementInfo packetIn) {
        this.wrapped.handleAdvancementInfo(packetIn);
    }

    @Override
    public void handleSelectAdvancementsTab(SPacketSelectAdvancementsTab packetIn) {
        this.wrapped.handleSelectAdvancementsTab(packetIn);
    }

    @Override
    public void handleStatistics(SPacketStatistics packetIn) {
        this.wrapped.handleStatistics(packetIn);
    }

    @Override
    public void handleRecipeBook(SPacketRecipeBook packetIn) {
        this.wrapped.handleRecipeBook(packetIn);
    }

    @Override
    public void handleEntityEffect(SPacketEntityEffect packetIn) {
        this.wrapped.handleEntityEffect(packetIn);
    }

    @Override
    public void handleCombatEvent(SPacketCombatEvent packetIn) {
        this.wrapped.handleCombatEvent(packetIn);
    }

    @Override
    public void handleServerDifficulty(SPacketServerDifficulty packetIn) {
        this.wrapped.handleServerDifficulty(packetIn);
    }

    @Override
    public void handleCamera(SPacketCamera packetIn) {
        this.wrapped.handleCamera(packetIn);
    }

    @Override
    public void handleWorldBorder(SPacketWorldBorder packetIn) {
        this.wrapped.handleWorldBorder(packetIn);
    }

    @Override
    public void handleTitle(SPacketTitle packetIn) {
        this.wrapped.handleTitle(packetIn);
    }

    @Override
    public void handlePlayerListHeaderFooter(SPacketPlayerListHeaderFooter packetIn) {
        this.wrapped.handlePlayerListHeaderFooter(packetIn);
    }

    @Override
    public void handleRemoveEntityEffect(SPacketRemoveEntityEffect packetIn) {
        this.wrapped.handleRemoveEntityEffect(packetIn);
    }

    @Override
    public void handlePlayerListItem(SPacketPlayerListItem packetIn) {
        this.wrapped.handlePlayerListItem(packetIn);
    }

    @Override
    public void handleKeepAlive(SPacketKeepAlive packetIn) {
        this.wrapped.handleKeepAlive(packetIn);
    }

    @Override
    public void handleTabComplete(SPacketTabComplete packetIn) {
        this.wrapped.handleTabComplete(packetIn);
    }

    @Override
    public void handleSoundEffect(SPacketSoundEffect packetIn) {
        this.wrapped.handleSoundEffect(packetIn);
    }

    @Override
    public void handleCustomSound(SPacketCustomSound packetIn) {
        this.wrapped.handleCustomSound(packetIn);
    }

    @Override
    public void handleResourcePack(SPacketResourcePackSend packetIn) {
        this.wrapped.handleResourcePack(packetIn);
    }

    @Override
    public void handleUpdateBossInfo(SPacketUpdateBossInfo packetIn) {
        this.wrapped.handleUpdateBossInfo(packetIn);
    }

    @Override
    public void handleCooldown(SPacketCooldown packetIn) {
        this.wrapped.handleCooldown(packetIn);
    }

    @Override
    public void handleMoveVehicle(SPacketMoveVehicle packetIn) {
        this.wrapped.handleMoveVehicle(packetIn);
    }

    @Override
    public void handleCustomPayload(SPacketCustomPayload packetIn) {
        this.wrapped.handleCustomPayload(packetIn);
    }

    @Override
    public void handleScoreboardObjective(SPacketScoreboardObjective packetIn) {
        this.wrapped.handleScoreboardObjective(packetIn);
    }

    @Override
    public void handleUpdateScore(SPacketUpdateScore packetIn) {
        this.wrapped.handleUpdateScore(packetIn);
    }

    @Override
    public void handleDisplayObjective(SPacketDisplayObjective packetIn) {
        this.wrapped.handleDisplayObjective(packetIn);
    }

    @Override
    public void handleTeams(SPacketTeams packetIn) {
        this.wrapped.handleTeams(packetIn);
    }

    @Override
    public void handleParticles(SPacketParticles packetIn) {
        this.wrapped.handleParticles(packetIn);
    }

    @Override
    public void handleEntityProperties(SPacketEntityProperties packetIn) {
        this.wrapped.handleEntityProperties(packetIn);
    }

    @Override
    public void func_194307_a(SPacketPlaceGhostRecipe p_194307_1_) {
        this.wrapped.func_194307_a(p_194307_1_);
    }

    @Override
    public NetworkManager getNetworkManager() {
        return this.wrapped.getNetworkManager();
    }

    @Override
    public Collection<NetworkPlayerInfo> getPlayerInfoMap() {
        return this.wrapped.getPlayerInfoMap();
    }

    @Override
    public NetworkPlayerInfo getPlayerInfo(UUID uniqueId) {
        return this.wrapped.getPlayerInfo(uniqueId);
    }

    @Override
    public NetworkPlayerInfo getPlayerInfo(String name) {
        return this.wrapped.getPlayerInfo(name);
    }

    @Override
    public GameProfile getGameProfile() {
        return this.wrapped.getGameProfile();
    }

    @Override
    public ClientAdvancementManager getAdvancementManager() {
        return this.wrapped.getAdvancementManager();
    }
    //#endregion
}
