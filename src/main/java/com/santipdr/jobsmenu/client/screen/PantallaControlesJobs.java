package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.ToggleExpediente;

import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

/** Hub de controles: mantiene las opciones de Minecraft, cambia su jerarquia visual. */
public final class PantallaControlesJobs extends Screen {

    private final Screen anterior;
    private final Options opciones;
    private int panelX, panelY, panelW, panelH;
    private boolean cerrando;

    public PantallaControlesJobs(Screen anterior, Options opciones) {
        super(Component.translatable("controls.title"));
        this.anterior = anterior;
        this.opciones = opciones;
    }

    @Override
    protected void init() {
        this.cerrando = false;
        this.panelW = Math.max(1, Math.min(360, this.width - 12));
        this.panelH = Math.max(1, Math.min(238, this.height - 12));
        this.panelX = (this.width - panelW) / 2;
        this.panelY = Math.max(4, (this.height - panelH) / 2);

        int gap = 8;
        int bw = Math.max(100, (panelW - 48 - gap) / 2);
        int x0 = panelX + 20;
        int x1 = x0 + bw + gap;
        int y = panelY + 60;
        int h = 22;

        this.addRenderableWidget(new BotonExpediente(
                x0, y, bw, h, Component.translatable("options.mouse_settings"),
                BotonExpediente.Tipo.PRINCIPAL, this::abrirMouse));
        this.addRenderableWidget(new BotonExpediente(
                x1, y, bw, h, Component.translatable("controls.keybinds"),
                BotonExpediente.Tipo.PRINCIPAL, this::abrirTeclas));

        int y1 = y + 32;
        Function<Boolean, Component> holdToggle = v ->
                Component.translatable(v ? "options.key.toggle" : "options.key.hold");
        this.addRenderableWidget(toggle(x0, y1, bw, h, "key.sneak",
                () -> this.opciones.toggleCrouch().get(),
                v -> this.opciones.toggleCrouch().set(v), holdToggle));
        this.addRenderableWidget(toggle(x1, y1, bw, h, "key.sprint",
                () -> this.opciones.toggleSprint().get(),
                v -> this.opciones.toggleSprint().set(v), holdToggle));

        int y2 = y1 + 30;
        this.addRenderableWidget(toggle(x0, y2, bw, h, "options.autoJump",
                () -> this.opciones.autoJump().get(),
                v -> this.opciones.autoJump().set(v), null));
        this.addRenderableWidget(toggle(x1, y2, bw, h, "options.operatorItemsTab",
                () -> this.opciones.operatorItemsTab().get(),
                v -> this.opciones.operatorItemsTab().set(v), null));

        int volverW = Math.min(150, panelW - 40);
        this.addRenderableWidget(new BotonExpediente(
                this.width / 2 - volverW / 2, panelY + panelH - 31, volverW, 22,
                Component.translatable("jobsmenu.interfaz.volver"),
                BotonExpediente.Tipo.PRINCIPAL, this::onClose));
    }

    private void abrirMouse() {
        if (this.minecraft != null) this.minecraft.setScreen(new PantallaMouseJobs(this, this.opciones));
    }

    private void abrirTeclas() {
        if (this.minecraft != null) this.minecraft.setScreen(new PantallaTeclasJobs(this, this.opciones));
    }

    private ToggleExpediente toggle(int x, int y, int w, int h, String clave,
                                    BooleanSupplier leer, Consumer<Boolean> fijar,
                                    Function<Boolean, Component> formato) {
        Consumer<Boolean> guardar = v -> {
            fijar.accept(v);
            this.opciones.save();
        };
        if (formato == null) {
            return new ToggleExpediente(x, y, w, h, Component.translatable(clave), leer, guardar);
        }
        return new ToggleExpediente(x, y, w, h, Component.translatable(clave), leer, guardar, formato);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panel(g, panelX, panelY, panelW, panelH);
        ChromeExpediente.cabecera(g, this.font, this.title,
                Component.translatable("jobsmenu.interfaz.controles.subtitulo"), panelX, panelY, panelW);
        ChromeExpediente.esquinas(g, panelX, panelY, panelW, panelH);
        ChromeExpediente.pie(g, this.font, panelX, panelY, panelW, panelH, "CTL-012");
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
