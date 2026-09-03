package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.ListasExpediente;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

/** Multijugador de Jobs: lista vanilla dentro de un tablero de operaciones propio. */
public final class PantallaMultijugadorJobs extends JoinMultiplayerScreen {

    private static final String SERVIDOR_IP = "JobsDosh.exaroton.me:56477";

    private final Screen anteriorJobs;
    private Button realSelect, realDirect, realAdd, realEdit, realDelete, realRefresh, realCancel;
    private BotonExpediente select, edit, delete, refresh;
    private int panelX, panelY, panelW, panelH;

    public PantallaMultijugadorJobs(Screen anterior) {
        super(anterior);
        this.anteriorJobs = anterior;
    }

    @Override
    protected void init() {
        super.init();
        this.panelX = 12;
        this.panelY = 8;
        this.panelW = Math.max(120, this.width - 24);
        this.panelH = Math.max(100, this.height - 16);

        asegurarServidorOficial();

        this.realSelect = buscar("selectServer.select");
        this.realDirect = buscar("selectServer.direct");
        this.realAdd = buscar("selectServer.add");
        this.realEdit = buscar("selectServer.edit");
        this.realDelete = buscar("selectServer.delete");
        this.realRefresh = buscar("selectServer.refresh");
        this.realCancel = buscar("gui.cancel");

        for (var child : this.children()) {
            if (child instanceof Button b) b.visible = false;
            if (child instanceof ServerSelectionList lista) {
                lista.setRenderBackground(false);
                lista.setRenderTopAndBottom(false);
                int top = Math.max(88, panelY + 78);
                int bottom = Math.max(top + 40, this.height - 80);
                lista.updateSize(this.width, this.height, top, bottom);
            }
        }
        crearBotones();
    }

    private void asegurarServidorOficial() {
        ServerList servidores = this.getServers();
        if (servidores == null) return;

        String nombre = Component.translatable("jobsmenu.servidor.oficial").getString();
        boolean oficialConservado = false;
        for (int i = servidores.size() - 1; i >= 0; i--) {
            ServerData dato = servidores.get(i);
            String nombreGuardado = dato.name == null ? "" : dato.name.toLowerCase(Locale.ROOT);
            boolean oficial = SERVIDOR_IP.equalsIgnoreCase(dato.ip);
            boolean ghoulLegado = nombreGuardado.contains("ghoul outbreak");
            boolean jobsFalso = !oficial
                    && (nombreGuardado.equals("jobs official server")
                    || nombreGuardado.equals("servidor oficial de jobs"));
            if (oficial && !oficialConservado) {
                dato.name = nombre;
                oficialConservado = true;
            } else if (oficial || ghoulLegado || jobsFalso) {
                servidores.remove(dato);
            }
        }

        int indice = -1;
        for (int i = 0; i < servidores.size(); i++) {
            ServerData dato = servidores.get(i);
            if (SERVIDOR_IP.equalsIgnoreCase(dato.ip)) {
                dato.name = nombre;
                indice = i;
                break;
            }
        }
        if (indice < 0) {
            servidores.add(new ServerData(nombre, SERVIDOR_IP, false), false);
            indice = servidores.size() - 1;
        }
        while (indice > 0) {
            servidores.swap(indice, indice - 1);
            indice--;
        }
        servidores.save();
        if (this.serverSelectionList != null) this.serverSelectionList.updateOnlineServers(servidores);
    }

    private Button buscar(String clave) {
        String esperado = Component.translatable(clave).getString();
        for (var child : this.children()) {
            if (child instanceof Button b && b.getMessage().getString().equals(esperado)) return b;
        }
        return null;
    }

    private void crearBotones() {
        int gap = this.panelW < 300 ? 3 : 5;
        int margen = this.panelW < 300 ? 10 : 18;
        int util = Math.max(1, this.panelW - margen * 2);
        int topW = Math.max(1, (util - gap * 2) / 3);
        int bottomW = Math.max(1, (util - gap * 3) / 4);
        int topY = this.height - 68;
        int bottomY = this.height - 42;
        int xTop = this.panelX + margen;
        int xBottom = this.panelX + margen;

        this.select = agregar(xTop, topY, topW, "selectServer.select", "jobsmenu.tooltip.servidor.entrar",
                BotonExpediente.Tipo.PRINCIPAL, this.realSelect);
        agregar(xTop + topW + gap, topY, topW, "selectServer.direct", "jobsmenu.tooltip.servidor.directa",
                BotonExpediente.Tipo.NORMAL, this.realDirect);
        agregar(xTop + (topW + gap) * 2, topY, topW, "selectServer.add", "jobsmenu.tooltip.servidor.agregar",
                BotonExpediente.Tipo.NORMAL, this.realAdd);

        this.edit = agregar(xBottom, bottomY, bottomW, "selectServer.edit", "jobsmenu.tooltip.servidor.editar",
                BotonExpediente.Tipo.NORMAL, this.realEdit);
        this.delete = agregar(xBottom + bottomW + gap, bottomY, bottomW, "selectServer.delete", "jobsmenu.tooltip.servidor.eliminar",
                BotonExpediente.Tipo.TERMINAL, this.realDelete);
        this.refresh = agregar(xBottom + (bottomW + gap) * 2, bottomY, bottomW, "selectServer.refresh", "jobsmenu.tooltip.servidor.actualizar",
                BotonExpediente.Tipo.NORMAL, this.realRefresh);
        agregar(xBottom + (bottomW + gap) * 3, bottomY, bottomW, "gui.cancel", "jobsmenu.tooltip.volver",
                BotonExpediente.Tipo.NORMAL, this.realCancel);
        sincronizarEstados();
    }

    private BotonExpediente agregar(int x, int y, int w, String clave, String ayuda,
                                     BotonExpediente.Tipo tipo, Button real) {
        Runnable accion = "gui.cancel".equals(clave) ? this::volverAlMenu : () -> pulsar(real);
        BotonExpediente b = new BotonExpediente(x, y, w, 21,
                Component.translatable(clave), tipo, accion);
        this.addRenderableWidget(b);
        b.setTooltip(Tooltip.create(Component.translatable(ayuda)));
        return b;
    }

    private static void pulsar(Button boton) {
        if (boton != null && boton.active) boton.onPress();
    }

    private void sincronizarEstados() {
        boolean oficial = servidorOficialSeleccionado();
        if (this.select != null) this.select.active = this.realSelect != null && this.realSelect.active;
        if (this.edit != null) this.edit.active = !oficial && this.realEdit != null && this.realEdit.active;
        if (this.delete != null) this.delete.active = !oficial && this.realDelete != null && this.realDelete.active;
        if (oficial) {
            Component ayuda = Component.translatable("jobsmenu.tooltip.servidor.protegido");
            if (this.edit != null) this.edit.setTooltip(Tooltip.create(ayuda));
            if (this.delete != null) this.delete.setTooltip(Tooltip.create(ayuda));
        } else {
            if (this.edit != null) this.edit.setTooltip(Tooltip.create(Component.translatable("jobsmenu.tooltip.servidor.editar")));
            if (this.delete != null) this.delete.setTooltip(Tooltip.create(Component.translatable("jobsmenu.tooltip.servidor.eliminar")));
        }
    }

    private boolean servidorOficialSeleccionado() {
        if (this.serverSelectionList == null) return false;
        ServerSelectionList.Entry entrada = this.serverSelectionList.getSelected();
        return entrada instanceof ServerSelectionList.OnlineServerEntry online
                && SERVIDOR_IP.equalsIgnoreCase(online.getServerData().ip);
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panelArchivo(g, panelX, panelY, panelW, panelH);

        int listX = panelX + 16;
        int listY = Math.max(86, panelY + 76);
        int listW = Math.max(1, panelW - 32);
        int listBottom = Math.max(listY + 38, this.height - 78);
        g.fill(listX, listY, listX + listW, listBottom,
                Paleta.conAlfa(Paleta.ARCHIVO_SUPERFICIE, 0.48F));
        g.fill(listX, listY, listX + 2, listBottom,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.24F));
        g.fill(listX, listY, listX + listW, listY + 1,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.16F));
        g.fill(listX, listBottom - 1, listX + listW, listBottom,
                Paleta.conAlfa(Paleta.ARCHIVO_TEXTO_TENUE, 0.10F));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        sincronizarEstados();
        RenderSystem.setShaderColor(0.76F, 0.76F, 0.76F, 1.0F);
        super.render(g, mouseX, mouseY, partialTick);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        ListasExpediente.renderarBarras(this, g);
        ChromeExpediente.reemplazarCabeceraArchivo(g, this.font,
                Component.translatable("jobsmenu.interfaz.multijugador.titulo"),
                Component.translatable("jobsmenu.interfaz.multijugador.subtitulo"),
                panelX, panelY, panelW);

        int tarjetaX = panelX + 18;
        int tarjetaY = panelY + 47;
        int tarjetaW = Math.max(1, panelW - 36);
        int tarjetaH = 24;
        boolean seleccionOficial = servidorOficialSeleccionado();
        int fondo = Paleta.mezclar(Paleta.ARCHIVO_SUPERFICIE,
                Paleta.ARCHIVO_SUPERFICIE_FOCO, seleccionOficial ? 0.72F : 0.20F);
        g.fill(tarjetaX, tarjetaY, tarjetaX + tarjetaW, tarjetaY + tarjetaH, fondo);
        g.fill(tarjetaX, tarjetaY, tarjetaX + Math.min(3, tarjetaW), tarjetaY + tarjetaH,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, seleccionOficial ? 0.92F : 0.62F));
        g.fill(tarjetaX + 6, tarjetaY + 5, tarjetaX + 12, tarjetaY + 11,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.68F));
        g.fill(tarjetaX + 8, tarjetaY + 11, tarjetaX + 10, tarjetaY + 17,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.44F));

        String oficialTxt = Component.translatable("jobsmenu.servidor.oficial").getString();
        int reservado = tarjetaW > 340 ? 150 : 30;
        String oficialVisible = ChromeExpediente.ajustar(this.font, oficialTxt,
                Math.max(8, tarjetaW - reservado));
        g.drawString(this.font, oficialVisible, tarjetaX + 18, tarjetaY + 5,
                Paleta.conAlfa(Paleta.ARCHIVO_TEXTO, 0.94F), false);

        if (tarjetaW > 220) {
            String ipVisible = ChromeExpediente.ajustar(this.font, SERVIDOR_IP, Math.max(40, tarjetaW - 70));
            g.drawString(this.font, ipVisible, tarjetaX + 18, tarjetaY + 14,
                    Paleta.conAlfa(Paleta.ARCHIVO_TEXTO_TENUE, 0.62F), false);
        }

        Component fijado = Component.translatable("jobsmenu.servidor.fijado");
        int fw = this.font.width(fijado);
        if (tarjetaW > fw + 170) {
            int fx = tarjetaX + tarjetaW - fw - 10;
            g.fill(fx - 6, tarjetaY + 5, tarjetaX + tarjetaW - 5, tarjetaY + 17,
                    Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, seleccionOficial ? 0.18F : 0.10F));
            g.drawString(this.font, fijado, fx, tarjetaY + 7,
                    Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.82F), false);
        }

        int ruleY = this.height - 74;
        g.fill(panelX + 18, ruleY, panelX + panelW - 18, ruleY + 1,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.16F));
        String ayuda = "F5  //  " + (seleccionOficial ? "JOBS" : "SERVER");
        int aw = this.font.width(ayuda);
        if (panelW > aw + 80) {
            g.drawString(this.font, ayuda, panelX + panelW - aw - 22, ruleY + 4,
                    Paleta.conAlfa(Paleta.ARCHIVO_TEXTO_TENUE, 0.52F), false);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            volverAlMenu();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_F5 && this.realRefresh != null && this.realRefresh.active) {
            this.realRefresh.onPress();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        volverAlMenu();
    }

    private void volverAlMenu() {
        if (this.minecraft == null) return;
        this.minecraft.setScreen(this.anteriorJobs != null ? this.anteriorJobs : new PantallaNivel());
    }
}
