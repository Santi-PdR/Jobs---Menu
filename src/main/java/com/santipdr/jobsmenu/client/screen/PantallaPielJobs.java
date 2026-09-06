package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;

import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;

import java.util.EnumMap;
import java.util.Map;

/** Personalizacion del ocupante presentada como ficha de identificacion. */
public final class PantallaPielJobs extends Screen {

    private final Screen anterior;
    private final Options opciones;
    private final Map<PlayerModelPart, BotonExpediente> botones = new EnumMap<>(PlayerModelPart.class);
    private BotonExpediente brazo;
    private int panelX, panelY, panelW, panelH;
    private boolean cerrando;

    public PantallaPielJobs(Screen anterior, Options opciones) {
        super(Component.translatable("options.skinCustomisation"));
        this.anterior = anterior;
        this.opciones = opciones;
    }

    @Override
    protected void init() {
        this.cerrando = false;
        this.botones.clear();
        this.panelW = Math.min(356, Math.max(250, this.width - 24));
        this.panelH = Math.min(258, Math.max(220, this.height - 20));
        this.panelX = (this.width - panelW) / 2;
        this.panelY = Math.max(6, (this.height - panelH) / 2);

        int gap = 7;
        int bw = Math.max(102, (panelW - 48 - gap) / 2);
        int x0 = panelX + 20;
        int x1 = x0 + bw + gap;
        int y0 = panelY + 60;
        int paso = 27;

        PlayerModelPart[] partes = PlayerModelPart.values();
        for (int i = 0; i < partes.length; i++) {
            PlayerModelPart parte = partes[i];
            int x = (i % 2 == 0) ? x0 : x1;
            int y = y0 + (i / 2) * paso;
            BotonExpediente boton = new BotonExpediente(x, y, bw, 21,
                    rotulo(parte), () -> alternar(parte));
            this.botones.put(parte, boton);
            this.addRenderableWidget(boton);
        }

        int i = partes.length;
        int bx = (i % 2 == 0) ? x0 : x1;
        int by = y0 + (i / 2) * paso;
        this.brazo = new BotonExpediente(bx, by, bw, 21, rotuloBrazo(), this::alternarBrazo);
        this.addRenderableWidget(this.brazo);

        int volverW = Math.min(150, panelW - 40);
        this.addRenderableWidget(new BotonExpediente(
                this.width / 2 - volverW / 2, panelY + panelH - 31, volverW, 22,
                Component.translatable("jobsmenu.interfaz.volver"),
                BotonExpediente.Tipo.PRINCIPAL, this::onClose));
    }

    private Component rotulo(PlayerModelPart parte) {
        return parte.getName().copy().append(": ")
                .append(CommonComponents.optionStatus(this.opciones.isModelPartEnabled(parte)));
    }

    private Component rotuloBrazo() {
        return Component.translatable("options.mainHand").copy().append(": ")
                .append(this.opciones.mainHand().get().getCaption());
    }

    private void alternar(PlayerModelPart parte) {
        this.opciones.toggleModelPart(parte, !this.opciones.isModelPartEnabled(parte));
        this.opciones.save();
        BotonExpediente b = this.botones.get(parte);
        if (b != null) b.setMessage(rotulo(parte));
    }

    private void alternarBrazo() {
        HumanoidArm actual = this.opciones.mainHand().get();
        this.opciones.mainHand().set(actual == HumanoidArm.LEFT ? HumanoidArm.RIGHT : HumanoidArm.LEFT);
        this.opciones.save();
        if (this.brazo != null) this.brazo.setMessage(rotuloBrazo());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panel(g, panelX, panelY, panelW, panelH);
        ChromeExpediente.cabecera(g, this.font, this.title,
                Component.translatable("jobsmenu.interfaz.piel.subtitulo"), panelX, panelY, panelW);
        ChromeExpediente.esquinas(g, panelX, panelY, panelW, panelH);
        ChromeExpediente.pie(g, this.font, panelX, panelY, panelW, panelH, "ID-012");
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (this.cerrando || this.minecraft == null) return;
        this.cerrando = true;
        this.minecraft.setScreen(this.anterior);
    }

    @Override
    public void renderBackground(GuiGraphics g) {
    }
}
