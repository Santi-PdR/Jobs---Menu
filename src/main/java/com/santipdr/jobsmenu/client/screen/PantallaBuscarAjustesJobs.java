package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.client.ui.BotonExpediente;
import com.santipdr.jobsmenu.client.ui.ChromeExpediente;
import com.santipdr.jobsmenu.client.ui.Paleta;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import org.lwjgl.glfw.GLFW;

import java.util.Locale;

/** Buscador transversal de las preferencias propias de Jobs. */
public final class PantallaBuscarAjustesJobs extends Screen {

    private static final int ITEM_H = 32;

    private record Ajuste(int categoria, String clave, String detalle) {
    }

    private static final Ajuste[] AJUSTES = {
            ajuste(0, "jobsmenu.ajustes.escena"),
            ajuste(0, "jobsmenu.ajustes.estado"),
            ajuste(0, "jobsmenu.ajustes.respiracion"),
            ajuste(0, "jobsmenu.ajustes.presencia"),
            ajuste(0, "jobsmenu.ajustes.eventos"),
            ajuste(0, "jobsmenu.ajustes.papel"),
            ajuste(0, "jobsmenu.ajustes.guia"),
            ajuste(0, "jobsmenu.ajustes.interfaz"),
            ajuste(0, "jobsmenu.ajustes.alto"),
            ajuste(0, "jobsmenu.ajustes.grande"),

            ajuste(1, "jobsmenu.ajustes.rotar"),
            ajuste(1, "jobsmenu.ajustes.cuenta"),
            ajuste(1, "jobsmenu.ajustes.nivelfijo"),
            ajuste(1, "jobsmenu.ajustes.estancia"),
            ajuste(1, "jobsmenu.ajustes.rotacioncalma"),
            ajuste(1, "jobsmenu.ajustes.avisos"),
            ajuste(1, "jobsmenu.ajustes.duracion"),
            ajuste(1, "jobsmenu.ajustes.fecha"),

            ajuste(2, "jobsmenu.ajustes.volaviso"),
            ajuste(2, "jobsmenu.ajustes.volmusica"),
            ajuste(2, "jobsmenu.ajustes.volambiente"),
            ajuste(2, "jobsmenu.ajustes.pista"),
            ajuste(2, "jobsmenu.ajustes.musica"),
            ajuste(2, "jobsmenu.ajustes.ambiente"),
            ajuste(2, "jobsmenu.ajustes.botones"),
            ajuste(2, "jobsmenu.ajustes.credito"),

            ajuste(3, "jobsmenu.ajustes.perfil"),
            ajuste(3, "jobsmenu.ajustes.movimiento"),
            ajuste(3, "jobsmenu.ajustes.destellos"),
            ajuste(3, "jobsmenu.ajustes.bajoconsumo"),
            ajuste(3, "jobsmenu.ajustes.alto"),
            ajuste(3, "jobsmenu.ajustes.grande"),
            ajuste(3, "jobsmenu.ajustes.papel"),
            ajuste(3, "jobsmenu.ajustes.guia"),

            ajuste(4, "jobsmenu.ajustes.suspension"),
            ajuste(4, "jobsmenu.ajustes.menu"),
            ajuste(4, "jobsmenu.ajustes.pausa"),
            ajuste(4, "jobsmenu.ajustes.rotacioncalma"),

            new Ajuste(5, "jobsmenu.ajustes.perfil", "jobsmenu.ajustes.perfil.detalle")
    };

    private final PantallaAjustesAviso anterior;
    private EditBox busqueda;
    private ListaResultados lista;
    private int panelX, panelY, panelW, panelH;
    private String filtroConservado = "";
    private boolean focoConservado = true;
    private double scrollConservado;
    private int resultadosVisibles;

    public PantallaBuscarAjustesJobs(PantallaAjustesAviso anterior) {
        super(Component.translatable("jobsmenu.ajustes.titulo"));
        this.anterior = anterior;
    }

    private static Ajuste ajuste(int categoria, String clave) {
        return new Ajuste(categoria, clave, clave + ".detalle");
    }

    @Override
    protected void init() {
        this.panelW = Math.max(190, Math.min(450, this.width - 12));
        this.panelH = Math.max(190, Math.min(310, this.height - 12));
        this.panelW = Math.min(this.panelW, Math.max(1, this.width - 4));
        this.panelH = Math.min(this.panelH, Math.max(1, this.height - 4));
        this.panelX = Math.max(2, (this.width - this.panelW) / 2);
        this.panelY = Math.max(2, (this.height - this.panelH) / 2);

        int margen = this.panelW < 280 ? 10 : 18;
        int x = this.panelX + margen;
        int w = Math.max(90, this.panelW - margen * 2);
        int searchY = this.panelY + 49;

        this.busqueda = new EditBox(this.font, x, searchY, w, 20,
                Component.literal("CTRL+F // SEARCH"));
        this.busqueda.setBordered(false);
        this.busqueda.setTextColor(Paleta.UI_TINTA);
        this.busqueda.setTextColorUneditable(Paleta.UI_TINTA_TENUE);
        this.busqueda.setResponder(s -> {
            this.filtroConservado = s;
            if (this.lista != null) this.lista.recargar(s);
        });
        this.addRenderableWidget(this.busqueda);

        int listY = searchY + 27;
        int footerY = this.panelY + this.panelH - 31;
        int listH = Math.max(62, footerY - listY - 8);
        this.lista = new ListaResultados(this.minecraft, x, listY, w, listH);
        this.addRenderableWidget(this.lista);

        if (!this.filtroConservado.isEmpty()) {
            this.busqueda.setValue(this.filtroConservado);
        } else {
            this.lista.recargar("");
        }
        this.lista.setScrollAmount(this.scrollConservado);

        if (this.focoConservado) {
            this.setFocused(this.busqueda);
            this.busqueda.setFocused(true);
        }

        int volverW = Math.min(150, w);
        this.addRenderableWidget(new BotonExpediente(
                this.width / 2 - volverW / 2, footerY, volverW, 21,
                Component.translatable("jobsmenu.interfaz.volver"),
                BotonExpediente.Tipo.PRINCIPAL, this::onClose));
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        if (this.busqueda != null) {
            this.filtroConservado = this.busqueda.getValue();
            this.focoConservado = this.busqueda.isFocused();
        }
        if (this.lista != null) {
            this.scrollConservado = this.lista.getScrollAmount();
        }
        super.resize(minecraft, width, height);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        ChromeExpediente.fondo(g, this.width, this.height);
        ChromeExpediente.panel(g, this.panelX, this.panelY, this.panelW, this.panelH);
        ChromeExpediente.cabecera(g, this.font, this.title,
                Component.translatable("jobsmenu.interfaz.aviso.subtitulo"),
                this.panelX, this.panelY, this.panelW);
        ChromeExpediente.esquinas(g, this.panelX, this.panelY, this.panelW, this.panelH);

        if (this.busqueda != null) {
            int x = this.busqueda.getX();
            int y = this.busqueda.getY();
            int w = this.busqueda.getWidth();
            int h = this.busqueda.getHeight();
            int borde = Paleta.conAlfa(Paleta.UI_ACENTO,
                    this.busqueda.isFocused() ? 0.62F : 0.26F);
            g.fill(x - 2, y - 2, x + w + 2, y + h + 2,
                    Paleta.conAlfa(Paleta.UI_ACENTO, this.busqueda.isFocused() ? 0.13F : 0.05F));
            g.fill(x - 1, y - 1, x + w + 1, y, borde);
            g.fill(x - 1, y + h, x + w + 1, y + h + 1,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.18F));
        }

        String contador = String.format(Locale.ROOT, "%02d", this.resultadosVisibles);
        int cw = this.font.width(contador);
        g.drawString(this.font, contador,
                this.panelX + this.panelW - cw - 17, this.panelY + 34,
                Paleta.conAlfa(Paleta.UI_TINTA_TENUE, 0.52F), false);

        super.render(g, mouseX, mouseY, partialTick);
        ChromeExpediente.pie(g, this.font, this.panelX, this.panelY,
                this.panelW, this.panelH, "CFG-SEARCH");
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (Screen.hasControlDown() && keyCode == GLFW.GLFW_KEY_F && this.busqueda != null) {
            this.setFocused(this.busqueda);
            this.busqueda.setFocused(true);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE && this.busqueda != null && this.busqueda.isFocused()) {
            if (!this.busqueda.getValue().isEmpty()) {
                this.busqueda.setValue("");
                return true;
            }
            this.busqueda.setFocused(false);
            this.setFocused(null);
            this.focoConservado = false;
            return true;
        }
        if ((keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER)
                && this.lista != null && this.lista.getSelected() != null) {
            abrir(this.lista.getSelected().ajuste);
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) this.minecraft.setScreen(this.anterior);
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        // ChromeExpediente renders the complete background.
    }

    private void abrir(Ajuste ajuste) {
        if (ajuste == null || this.minecraft == null) return;
        MezclaAudio.gesto(SonidosNivel.UI_ELEGIR, 0.34F);
        this.minecraft.setScreen(this.anterior);
        this.anterior.keyPressed(GLFW.GLFW_KEY_1 + ajuste.categoria(), 0, 0);
    }

    private static String claveCategoria(int categoria) {
        return switch (categoria) {
            case 1 -> "jobsmenu.ajustes.categoria.nivel";
            case 2 -> "jobsmenu.ajustes.categoria.audio";
            case 3 -> "jobsmenu.ajustes.categoria.accesibilidad";
            case 4 -> "jobsmenu.ajustes.categoria.sistema";
            case 5 -> "jobsmenu.ajustes.perfil";
            default -> "jobsmenu.ajustes.categoria.visual";
        };
    }

    private final class ListaResultados extends ObjectSelectionList<EntradaResultado> {
        private final int rowW;

        ListaResultados(Minecraft minecraft, int left, int top, int width, int height) {
            super(minecraft, width, height, top, top + height, ITEM_H);
            this.rowW = Math.max(40, width - 10);
            this.setLeftPos(left);
            this.setRenderBackground(false);
            this.setRenderTopAndBottom(false);
            this.setRenderSelection(false);
        }

        void recargar(String filtro) {
            this.clearEntries();
            String aguja = filtro == null ? "" : filtro.trim().toLowerCase(Locale.ROOT);
            EntradaResultado primera = null;
            int cantidad = 0;
            for (Ajuste ajuste : AJUSTES) {
                String titulo = Component.translatable(ajuste.clave()).getString();
                String detalle = Component.translatable(ajuste.detalle()).getString();
                String categoria = Component.translatable(claveCategoria(ajuste.categoria())).getString();
                String bolsa = (titulo + " " + detalle + " " + categoria).toLowerCase(Locale.ROOT);
                if (!aguja.isEmpty() && !bolsa.contains(aguja)) continue;
                EntradaResultado entrada = new EntradaResultado(ajuste);
                this.addEntry(entrada);
                if (primera == null) primera = entrada;
                cantidad++;
            }
            PantallaBuscarAjustesJobs.this.resultadosVisibles = cantidad;
            if (primera != null) {
                this.setSelected(primera);
            } else {
                this.setSelected(null);
                this.setScrollAmount(0.0D);
            }
        }

        @Override
        public int getRowWidth() {
            return this.rowW;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getRowLeft() + this.getRowWidth() + 4;
        }
    }

    private final class EntradaResultado extends ObjectSelectionList.Entry<EntradaResultado> {
        private final Ajuste ajuste;
        private long ultimoClick;

        EntradaResultado(Ajuste ajuste) {
            this.ajuste = ajuste;
        }

        @Override
        public void render(GuiGraphics g, int index, int top, int left, int rowWidth,
                           int rowHeight, int mouseX, int mouseY, boolean hovered, float partialTick) {
            boolean seleccionada = PantallaBuscarAjustesJobs.this.lista.getSelected() == this;
            int x0 = left + 2;
            int x1 = left + rowWidth - 2;
            if (seleccionada) {
                g.fill(x0, top + 1, x1, top + rowHeight - 2,
                        Paleta.conAlfa(Paleta.UI_ACENTO, 0.20F));
                g.fill(x0, top + 1, x0 + 3, top + rowHeight - 2,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, 0.78F));
            } else if (hovered) {
                g.fill(x0, top + 1, x1, top + rowHeight - 2,
                        Paleta.conAlfa(Paleta.UI_ACENTO, 0.08F));
            }

            String categoria = Component.translatable(claveCategoria(this.ajuste.categoria())).getString();
            int catW = PantallaBuscarAjustesJobs.this.font.width(categoria);
            int maxTitulo = Math.max(30, rowWidth - catW - 34);
            String titulo = ChromeExpediente.ajustar(PantallaBuscarAjustesJobs.this.font,
                    Component.translatable(this.ajuste.clave()).getString(), maxTitulo);
            String detalle = ChromeExpediente.ajustar(PantallaBuscarAjustesJobs.this.font,
                    Component.translatable(this.ajuste.detalle()).getString(), Math.max(40, rowWidth - 22));

            int tx = left + 9;
            g.drawString(PantallaBuscarAjustesJobs.this.font, titulo, tx, top + 5,
                    seleccionada ? Paleta.tintaPrincipal() : Paleta.tintaSecundaria(), false);
            g.drawString(PantallaBuscarAjustesJobs.this.font, detalle, tx, top + 17,
                    Paleta.conAlfa(Paleta.UI_TINTA_TENUE, seleccionada ? 0.62F : 0.42F), false);

            if (rowWidth > 130) {
                int badgeX = left + rowWidth - catW - 10;
                g.drawString(PantallaBuscarAjustesJobs.this.font, categoria, badgeX, top + 5,
                        Paleta.conAlfa(Paleta.UI_ACENTO_FUERTE, seleccionada ? 0.72F : 0.42F), false);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0) return false;
            long ahora = System.currentTimeMillis();
            boolean doble = PantallaBuscarAjustesJobs.this.lista.getSelected() == this
                    && ahora - this.ultimoClick < 420L;
            this.ultimoClick = ahora;
            PantallaBuscarAjustesJobs.this.lista.setSelected(this);
            if (doble) PantallaBuscarAjustesJobs.this.abrir(this.ajuste);
            else MezclaAudio.gesto(SonidosNivel.UI_ELEGIR, 0.24F);
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.translatable(this.ajuste.clave());
        }
    }
}
