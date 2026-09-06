package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.ListasExpediente;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.multiplayer.ServerSelectionList;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.multiplayer.ServerList;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

/** Multijugador de Jobs: lista vanilla dentro de un tablero de operaciones propio. */
public final class PantallaMultijugadorJobs extends JoinMultiplayerScreen {

    private static final String SERVIDOR_IP = "JobsDosh.exaroton.me:56477";
    private static final Component TITULO = Component.translatable("jobsmenu.interfaz.multijugador.titulo");
    private static final Component SUBTITULO = Component.translatable("jobsmenu.interfaz.multijugador.subtitulo");
    private static final Component SERVIDOR_OFICIAL = Component.translatable("jobsmenu.servidor.oficial");
    private static final Component FIJADO = Component.translatable("jobsmenu.servidor.fijado");
    private static final Tooltip TOOLTIP_PROTEGIDO =
            Tooltip.create(Component.translatable("jobsmenu.tooltip.servidor.protegido"));
    private static final Tooltip TOOLTIP_EDITAR =
            Tooltip.create(Component.translatable("jobsmenu.tooltip.servidor.editar"));
    private static final Tooltip TOOLTIP_ELIMINAR =
            Tooltip.create(Component.translatable("jobsmenu.tooltip.servidor.eliminar"));

    private final Screen pantallaPadre;
    private String servidorPreferido;
    private double scrollPreferido;
    private Button realSelect, realDirect, realAdd, realEdit, realDelete;
    private BotonExpediente select, edit, delete, refresh;
    private int panelX, panelY, panelW, panelH;
    private boolean cerrando;
    private boolean seleccionOficial;
    private boolean tooltipInicializado;
    private boolean tooltipOficial;
    private String oficialVisible = "";
    private String ipVisible = SERVIDOR_IP;
    private String ayudaF5 = "F5";
    private int anchoFijado;

    public PantallaMultijugadorJobs(Screen anterior) {
        this(anterior, null, -1.0D);
    }

    private PantallaMultijugadorJobs(Screen anterior, String servidorPreferido,
                                      double scrollPreferido) {
        super(anterior);
        this.pantallaPadre = anterior;
        this.servidorPreferido = servidorPreferido;
        this.scrollPreferido = scrollPreferido;
    }

    @Override
    protected void init() {
        this.cerrando = false;
        this.tooltipInicializado = false;
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
        restaurarSeleccionPreferida();
        restaurarScrollPreferido();
        prepararRotulos();
        crearBotones();
    }

    /**
     * Screen.resize vuelve a ejecutar init(). Guardamos antes la posicion de la
     * lista y el servidor seleccionado para que cambiar de escala GUI, maximizar
     * o redimensionar la ventana no mande al usuario otra vez al principio.
     */
    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        String seleccion = ipSeleccionada();
        if (seleccion != null && !seleccion.isBlank()) {
            this.servidorPreferido = seleccion;
        }
        double scroll = scrollActual();
        if (scroll >= 0.0D) {
            this.scrollPreferido = scroll;
        }
        super.resize(minecraft, width, height);
    }

    private void prepararRotulos() {
        int tarjetaW = Math.max(1, this.panelW - 36);
        int reservado = tarjetaW > 340 ? 150 : 30;
        this.oficialVisible = ChromeExpediente.ajustar(this.font, SERVIDOR_OFICIAL.getString(),
                Math.max(8, tarjetaW - reservado));
        this.ipVisible = ChromeExpediente.ajustar(this.font, SERVIDOR_IP, Math.max(40, tarjetaW - 70));
        this.ayudaF5 = "F5  //  " + Component.translatable("selectServer.refresh")
                .getString().toUpperCase(Locale.ROOT);
        this.anchoFijado = this.font.width(FIJADO);
    }

    private void asegurarServidorOficial() {
        ServerList servidores = this.getServers();
        if (servidores == null) return;

        String nombre = SERVIDOR_OFICIAL.getString();
        boolean oficialConservado = false;
        boolean cambiado = false;
        for (int i = servidores.size() - 1; i >= 0; i--) {
            ServerData dato = servidores.get(i);
            String nombreGuardado = dato.name == null ? "" : dato.name.toLowerCase(Locale.ROOT);
            boolean oficial = SERVIDOR_IP.equalsIgnoreCase(dato.ip);
            boolean ghoulLegado = nombreGuardado.contains("ghoul outbreak");
            boolean jobsFalso = !oficial
                    && (nombreGuardado.equals("jobs official server")
                    || nombreGuardado.equals("servidor oficial de jobs"));
            if (oficial && !oficialConservado) {
                if (!nombre.equals(dato.name)) {
                    dato.name = nombre;
                    cambiado = true;
                }
                oficialConservado = true;
            } else if (oficial || ghoulLegado || jobsFalso) {
                servidores.remove(dato);
                cambiado = true;
            }
        }

        int indice = -1;
        for (int i = 0; i < servidores.size(); i++) {
            ServerData dato = servidores.get(i);
            if (SERVIDOR_IP.equalsIgnoreCase(dato.ip)) {
                if (!nombre.equals(dato.name)) {
                    dato.name = nombre;
                    cambiado = true;
                }
                indice = i;
                break;
            }
        }
        if (indice < 0) {
            servidores.add(new ServerData(nombre, SERVIDOR_IP, false), false);
            indice = servidores.size() - 1;
            cambiado = true;
        }
        while (indice > 0) {
            servidores.swap(indice, indice - 1);
            indice--;
            cambiado = true;
        }
        if (cambiado) {
            servidores.save();
            if (this.serverSelectionList != null) {
                this.serverSelectionList.updateOnlineServers(servidores);
            }
        }
    }

    /** Restaura el servidor seleccionado despues de una recarga F5 sin guardar una Entry obsoleta. */
    private void restaurarSeleccionPreferida() {
        if (this.serverSelectionList == null || this.servidorPreferido == null
                || this.servidorPreferido.isBlank()) return;
        for (ServerSelectionList.Entry entrada : this.serverSelectionList.children()) {
            if (entrada instanceof ServerSelectionList.OnlineServerEntry online
                    && this.servidorPreferido.equalsIgnoreCase(online.getServerData().ip)) {
                this.serverSelectionList.setSelected(entrada);
                this.onSelectedChange();
                return;
            }
        }
    }

    /** Mantiene la zona que el usuario estaba inspeccionando al recargar. */
    private void restaurarScrollPreferido() {
        if (this.serverSelectionList == null || this.scrollPreferido < 0.0D) return;
        this.serverSelectionList.setScrollAmount(this.scrollPreferido);
    }

    private String ipSeleccionada() {
        if (this.serverSelectionList == null) return null;
        ServerSelectionList.Entry entrada = this.serverSelectionList.getSelected();
        if (entrada instanceof ServerSelectionList.OnlineServerEntry online) {
            return online.getServerData().ip;
        }
        return null;
    }

    private double scrollActual() {
        return this.serverSelectionList == null ? -1.0D : this.serverSelectionList.getScrollAmount();
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
                BotonExpediente.Tipo.NORMAL, null);
        agregar(xBottom + (bottomW + gap) * 3, bottomY, bottomW, "gui.cancel", "jobsmenu.tooltip.volver",
                BotonExpediente.Tipo.NORMAL, null);
        sincronizarEstados();
    }

    private BotonExpediente agregar(int x, int y, int w, String clave, String ayuda,
                                     BotonExpediente.Tipo tipo, Button real) {
        Runnable accion;
        if ("gui.cancel".equals(clave)) accion = this::cerrarAlPadre;
        else if ("selectServer.select".equals(clave)) accion = this::conectarSeleccionado;
        else if ("selectServer.refresh".equals(clave)) accion = this::refrescarLista;
        else accion = () -> pulsar(real);
        BotonExpediente b = new BotonExpediente(x, y, w, 21,
                Component.translatable(clave), tipo, accion);
        this.addRenderableWidget(b);
        b.setTooltip(Tooltip.create(Component.translatable(ayuda)));
        return b;
    }

    /**
     * Inicia la conexion con esta pantalla como padre. Asi tanto Cancelar como
     * DisconnectedScreen regresan a la lista Jobs y no al menu principal.
     */
    private void conectarSeleccionado() {
        if (this.minecraft == null || this.serverSelectionList == null) return;
        ServerSelectionList.Entry entrada = this.serverSelectionList.getSelected();
        if (entrada instanceof ServerSelectionList.OnlineServerEntry online) {
            ServerData servidor = online.getServerData();
            ConnectScreen.startConnecting(this, this.minecraft,
                    ServerAddress.parseString(servidor.ip), servidor, false);
            return;
        }
        pulsar(this.realSelect);
    }

    private static void pulsar(Button boton) {
        if (boton != null && boton.active) boton.onPress();
    }

    private void sincronizarEstados() {
        boolean oficial = servidorOficialSeleccionado();
        this.seleccionOficial = oficial;
        if (this.select != null) this.select.active = this.realSelect != null && this.realSelect.active;
        if (this.edit != null) this.edit.active = !oficial && this.realEdit != null && this.realEdit.active;
        if (this.delete != null) this.delete.active = !oficial && this.realDelete != null && this.realDelete.active;
        if (this.refresh != null) this.refresh.active = !this.cerrando;

        if (!this.tooltipInicializado || this.tooltipOficial != oficial) {
            this.tooltipInicializado = true;
            this.tooltipOficial = oficial;
            if (this.edit != null) this.edit.setTooltip(oficial ? TOOLTIP_PROTEGIDO : TOOLTIP_EDITAR);
            if (this.delete != null) this.delete.setTooltip(oficial ? TOOLTIP_PROTEGIDO : TOOLTIP_ELIMINAR);
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
                TITULO, SUBTITULO, panelX, panelY, panelW);

        int tarjetaX = panelX + 18;
        int tarjetaY = panelY + 47;
        int tarjetaW = Math.max(1, panelW - 36);
        int tarjetaH = 24;
        int fondo = Paleta.mezclar(Paleta.ARCHIVO_SUPERFICIE,
                Paleta.ARCHIVO_SUPERFICIE_FOCO, this.seleccionOficial ? 0.72F : 0.20F);
        g.fill(tarjetaX, tarjetaY, tarjetaX + tarjetaW, tarjetaY + tarjetaH, fondo);
        g.fill(tarjetaX, tarjetaY, tarjetaX + Math.min(3, tarjetaW), tarjetaY + tarjetaH,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, this.seleccionOficial ? 0.92F : 0.62F));
        g.fill(tarjetaX + 6, tarjetaY + 5, tarjetaX + 12, tarjetaY + 11,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.68F));
        g.fill(tarjetaX + 8, tarjetaY + 11, tarjetaX + 10, tarjetaY + 17,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.44F));

        g.drawString(this.font, this.oficialVisible, tarjetaX + 18, tarjetaY + 5,
                Paleta.conAlfa(Paleta.ARCHIVO_TEXTO, 0.94F), false);

        if (tarjetaW > 220) {
            g.drawString(this.font, this.ipVisible, tarjetaX + 18, tarjetaY + 14,
                    Paleta.conAlfa(Paleta.ARCHIVO_TEXTO_TENUE, 0.62F), false);
        }

        if (tarjetaW > this.anchoFijado + 170) {
            int fx = tarjetaX + tarjetaW - this.anchoFijado - 10;
            g.fill(fx - 6, tarjetaY + 5, tarjetaX + tarjetaW - 5, tarjetaY + 17,
                    Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, this.seleccionOficial ? 0.18F : 0.10F));
            g.drawString(this.font, FIJADO, fx, tarjetaY + 7,
                    Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.82F), false);
        }

        int ruleY = this.height - 74;
        g.fill(panelX + 18, ruleY, panelX + panelW - 18, ruleY + 1,
                Paleta.conAlfa(Paleta.ARCHIVO_ACENTO, 0.16F));
        int aw = this.font.width(this.ayudaF5);
        if (panelW > aw + 80) {
            g.drawString(this.font, this.ayudaF5, panelX + panelW - aw - 22, ruleY + 4,
                    Paleta.conAlfa(Paleta.ARCHIVO_TEXTO_TENUE, 0.52F), false);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_F5) {
            if (this.minecraft != null && !this.cerrando) {
                MezclaAudio.gesto(SonidosNivel.UI_ALTERNAR, 0.34F);
            }
            refrescarLista();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    /** Recarga servidores sin crear pantalla vanilla ni perder contexto de lista. */
    private void refrescarLista() {
        if (this.minecraft == null || this.cerrando) return;
        String seleccion = ipSeleccionada();
        double scroll = scrollActual();
        this.cerrando = true;
        this.minecraft.setScreen(new PantallaMultijugadorJobs(
                padreDestino(), seleccion, scroll));
    }

    /**
     * Escape y Cancelar terminan exactamente en el mismo padre Jobs. No delegar
     * en super.onClose(): en Forge 1.20.1 esa ruta usa popGuiLayer() y no la
     * navegacion del boton Cancelar vanilla de JoinMultiplayerScreen.
     */
    @Override
    public void onClose() {
        cerrarAlPadre();
    }

    private void cerrarAlPadre() {
        if (this.cerrando) return;
        this.cerrando = true;
        if (this.minecraft != null) this.minecraft.setScreen(padreDestino());
    }

    private Screen padreDestino() {
        return this.pantallaPadre != null ? this.pantallaPadre : new PantallaNivel();
    }
}
