package com.santipdr.jobsmenu.client.screen;

import com.santipdr.jobsmenu.client.scene.EscenaNivel;
import com.santipdr.jobsmenu.client.SesionMenu;
import com.santipdr.jobsmenu.client.scene.Nivel;
import com.santipdr.jobsmenu.client.scene.RotacionNiveles;
import com.santipdr.jobsmenu.client.sound.GestorAmbiente;
import com.santipdr.jobsmenu.client.sound.GestorMusica;
import com.santipdr.jobsmenu.client.sound.MezclaAudio;
import com.santipdr.jobsmenu.client.sound.SonidosNivel;
import com.santipdr.jobsmenu.client.ui.HudPrincipalJobs;
import com.santipdr.jobsmenu.client.ui.NotaAviso;
import com.santipdr.jobsmenu.client.ui.Paleta;
import com.santipdr.jobsmenu.client.ui.RelojAparicion;
import com.santipdr.jobsmenu.client.ui.RenglonTablon;
import com.santipdr.jobsmenu.client.ui.SecretosJobs;
import com.santipdr.jobsmenu.config.ConfigTurno;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.OptionsScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.client.gui.ModListScreen;

import org.lwjgl.glfw.GLFW;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Pantalla de titulo: el aviso pegado a la pared del nivel. */
public class PantallaNivel extends Screen {

    private static final int ANCHO_HOJA = 214;
    private static final int NOTAS_POR_NIVEL = 3;
    private static final int ALTO_RENGLON = 20;
    private static final int SEPARACION = 3;
    private static final int HUECO_APARTE = 10;
    private static final int MARGEN_HOJA = 12;
    private static final int ALTO_LINEA = 11;
    private static final int ALTO_TITULO = 18;
    private static final int AIRE_TITULO = 4;
    private static final int AIRE_REGLA = 7;
    private static final int AIRE_CABECERA = 14;
    private static final int AIRE_PIE = 16;
    private static final int MARGEN_PANTALLA = 12;
    private static final long ENTRADA_ROTULO_MS = 900L;

    private int hojaX;
    private int hojaY;
    private int hojaAlto;
    private int anchoHoja = ANCHO_HOJA;
    private boolean compacta;
    private float escalaTipografia = 1.0F;
    private int altoCabecera;
    private int altoAviso;
    private List<FormattedCharSequence> lineasSubtitulo = Collections.emptyList();
    private List<List<FormattedCharSequence>> porNumeroNivelActual = Collections.emptyList();
    private List<List<FormattedCharSequence>> porNumeroTarifa = Collections.emptyList();
    private List<FormattedCharSequence> lineasFecha = Collections.emptyList();
    private int nivelVisto;
    private long desdeCambio;
    private RotacionNiveles.Estado estadoFrame;
    private RenglonTablon renglonSalida;
    private final List<RenglonTablon> renglones = new ArrayList<>();
    private NotaAviso notaAviso;
    private long confirmarSalidaHasta;

    public PantallaNivel() {
        super(Component.translatable("jobsmenu.pantalla.nivel"));
        this.estadoFrame = RotacionNiveles.capturar();
        this.nivelVisto = this.estadoFrame.indice();
        this.desdeCambio = this.estadoFrame.ahora();
    }

    @Override
    protected void init() {
        this.renglones.clear();
        this.notaAviso = null;
        GestorAmbiente.abrir();
        GestorMusica.asegurar();

        this.compacta = this.height < 310 || this.width < 270;
        this.escalaTipografia = !this.compacta && ConfigTurno.textoGrande() ? 1.15F : 1.0F;
        int margenPantalla = margenPantalla();
        this.anchoHoja = Math.max(1, Math.min(ANCHO_HOJA, this.width - 2 * margenPantalla));
        this.hojaX = Math.max(margenPantalla,
                Math.min((int) (this.width * 0.07F), this.width - margenPantalla - this.anchoHoja));

        int anchoTexto = Math.max(1, this.anchoHoja - 2 * margenHoja());
        int anchoMedido = Math.max(1, Math.round(anchoTexto / this.escalaTipografia));
        int anchoMax = Nivel.cantidad();
        this.lineasSubtitulo = this.font.split(Component.translatable("jobsmenu.subtitulo"), anchoMedido);
        List<List<FormattedCharSequence>> niveles = new ArrayList<>(anchoMax + 1);
        List<List<FormattedCharSequence>> tarifas = new ArrayList<>(anchoMax + 1);
        for (int i = 0; i <= anchoMax; i++) {
            niveles.add(this.font.split(Component.translatable("jobsmenu.nivel.actual", i), anchoMedido));
            tarifas.add(this.font.split(Component.translatable("jobsmenu.nivel.tarifa", i + 1), anchoMedido));
        }
        this.porNumeroNivelActual = niveles;
        this.porNumeroTarifa = tarifas;
        this.lineasFecha = this.font.split(fechaTurno(), anchoMedido);
        this.altoCabecera = medirCabecera();

        int salto = altoRenglon() + separacion();
        int altoLista = 3 * salto + huecoAparte() + altoRenglon();
        this.altoAviso = ConfigTurno.avisosRotativos() && !this.compacta ? medirAviso() : 0;
        this.hojaAlto = margenHoja() + this.altoCabecera + aireCabecera() + altoLista
                + (this.altoAviso > 0 ? airePie() + this.altoAviso : 0) + margenHoja();

        int disponible = this.height - 2 * margenPantalla;
        this.hojaY = this.hojaAlto > disponible
                ? margenPantalla
                : Math.max(margenPantalla,
                        Math.min((int) (this.height * 0.13F), this.height - margenPantalla - this.hojaAlto));

        int x = this.hojaX + margenHoja();
        int ancho = Math.max(1, this.anchoHoja - 2 * margenHoja());
        int y = this.hojaY + margenHoja() + this.altoCabecera + aireCabecera();
        agregar(x, y, ancho, "01", "jobsmenu.tablon.cuadrilla", this::abrirCuadrilla, false);
        agregar(x, y + salto, ancho, "02", "jobsmenu.tablon.condiciones", this::abrirCondiciones, false);
        agregar(x, y + 2 * salto, ancho, "03", "jobsmenu.tablon.registro", this::abrirRegistro, false);
        this.renglonSalida = agregar(x, y + 3 * salto + huecoAparte(), ancho, "04",
                "jobsmenu.tablon.renunciar", this::renunciar, true);
        if (this.altoAviso > 0) {
            this.notaAviso = new NotaAviso(x, y + altoLista + airePie(), ancho, this.altoAviso);
            this.addRenderableWidget(this.notaAviso);
        }
    }

    private int medirCabecera() {
        int alto = altoTitulo() + aireTitulo();
        alto += this.lineasSubtitulo.size() * altoLinea();
        alto += aireRegla() + 1 + aireRegla();
        int maximo = 1;
        for (List<FormattedCharSequence> variante : this.porNumeroNivelActual) maximo = Math.max(maximo, variante.size());
        for (List<FormattedCharSequence> variante : this.porNumeroTarifa) maximo = Math.max(maximo, variante.size());
        alto += maximo * 2 * altoLinea();
        if (ConfigTurno.mostrarFecha()) alto += this.lineasFecha.size() * altoLinea();
        return alto;
    }

    private int medirAviso() {
        int ancho = Math.max(1, this.anchoHoja - 2 * margenHoja());
        int anchoMedido = Math.max(1, Math.round(ancho / this.escalaTipografia));
        int max = 1;
        for (int i = 0; i < NotaAviso.AVISOS; i++) {
            max = Math.max(max, this.font.split(Component.translatable("jobsmenu.aviso." + i), anchoMedido).size());
        }
        for (String especial : NotaAviso.ESPECIALES) {
            max = Math.max(max, this.font.split(Component.translatable(especial), anchoMedido).size());
        }
        return max * altoLinea();
    }

    private int margenPantalla() { return this.compacta ? 6 : MARGEN_PANTALLA; }
    private int margenHoja() { return this.compacta ? 8 : MARGEN_HOJA; }
    private int altoLinea() { return Math.round(ALTO_LINEA * this.escalaTipografia); }
    private int altoTitulo() { return Math.round((this.compacta ? 10 : ALTO_TITULO) * this.escalaTipografia); }
    private int aireTitulo() { return Math.round((this.compacta ? 2 : AIRE_TITULO) * this.escalaTipografia); }
    private int aireRegla() { return Math.round((this.compacta ? 3 : AIRE_REGLA) * this.escalaTipografia); }
    private int aireCabecera() { return Math.round((this.compacta ? 6 : AIRE_CABECERA) * this.escalaTipografia); }
    private int airePie() { return Math.round((this.compacta ? 8 : AIRE_PIE) * this.escalaTipografia); }
    private int altoRenglon() { return Math.round((this.compacta ? 18 : ALTO_RENGLON) * this.escalaTipografia); }
    private int separacion() { return Math.round(SEPARACION * this.escalaTipografia); }
    private int huecoAparte() { return Math.round((this.compacta ? 5 : HUECO_APARTE) * this.escalaTipografia); }

    private Component fechaTurno() {
        LocalDateTime ahora = LocalDateTime.now();
        return Component.translatable("jobsmenu.pantalla.fecha",
                String.format(Locale.ROOT, "%02d", ahora.getDayOfMonth()),
                String.format(Locale.ROOT, "%02d", ahora.getMonthValue()),
                String.format(Locale.ROOT, "%02d:%02d", ahora.getHour(), ahora.getMinute()));
    }

    private RenglonTablon agregar(int x, int y, int ancho, String orden,
                                  String clave, Runnable accion, boolean terminal) {
        RenglonTablon renglon = new RenglonTablon(
                x, y, ancho, altoRenglon(), orden, Component.translatable(clave), accion, terminal);
        this.renglones.add(renglon);
        this.addRenderableWidget(renglon);
        return renglon;
    }

    private void abrirCuadrilla() { Minecraft.getInstance().setScreen(new JoinMultiplayerScreen(this)); }
    private void abrirRegistro() { Minecraft.getInstance().setScreen(new ModListScreen(this)); }
    private void abrirCondiciones() {
        Minecraft cliente = Minecraft.getInstance();
        cliente.setScreen(new OptionsScreen(this, cliente.options));
    }

    private void renunciar() {
        long ahora = System.currentTimeMillis();
        if (ahora > this.confirmarSalidaHasta) {
            this.confirmarSalidaHasta = ahora + 3_500L;
            if (this.renglonSalida != null) this.renglonSalida.setMessage(Component.translatable("jobsmenu.tablon.confirmar_salida"));
            return;
        }
        GestorAmbiente.cerrar();
        SesionMenu.cerrar();
        Minecraft.getInstance().stop();
    }

    @Override
    public void removed() { super.removed(); }

    @Override
    public void render(GuiGraphics grafico, int ratonX, int ratonY, float parcial) {
        if (this.confirmarSalidaHasta > 0L && System.currentTimeMillis() > this.confirmarSalidaHasta) {
            this.confirmarSalidaHasta = 0L;
            if (this.renglonSalida != null) this.renglonSalida.setMessage(Component.translatable("jobsmenu.tablon.renunciar"));
        }
        this.estadoFrame = RotacionNiveles.capturar();
        for (RenglonTablon renglon : this.renglones) renglon.setLuzFrame(this.estadoFrame.luz());
        if (this.notaAviso != null) this.notaAviso.setLuzFrame(this.estadoFrame.luz());
        GestorAmbiente.atender(this.estadoFrame);
        seguirNivel();

        this.renderBackground(grafico);
        dibujarComposicionPrincipal(grafico);
        if (!ConfigTurno.interfazMinima()) hoja(grafico);
        cabecera(grafico);
        super.render(grafico, ratonX, ratonY, parcial);
        HudPrincipalJobs.dibujar(grafico, this.width, this.height, this.estadoFrame);
        if (ConfigTurno.mostrarCuentaRegresiva()) ronda(grafico);
        if (!ConfigTurno.interfazMinima()) {
            rotuloNivel(grafico);
            estadoInstalacion(grafico);
        }
        credito(grafico);
        easterEggs(grafico);
    }

    private void dibujarComposicionPrincipal(GuiGraphics g) {
        if (this.compacta) return;
        int x = this.hojaX;
        int y = this.hojaY;
        int h = this.hojaAlto;
        float luz = this.estadoFrame.luz();
        int acento = Paleta.conAlfa(Paleta.papelAviso(), 0.12F * luz);
        int tenue = Paleta.conAlfa(Paleta.papelAviso(), 0.055F * luz);
        g.fill(Math.max(2, x - 10), y + 16, Math.max(3, x - 9), y + h - 16, acento);
        g.fill(Math.max(2, x - 14), y + 36, Math.max(3, x - 11), y + 37, tenue);
        g.fill(Math.max(2, x - 18), y + h / 2, Math.max(3, x - 12), y + h / 2 + 1,
                Paleta.conAlfa(Paleta.papelAviso(), 0.08F * luz));
        g.fill(x + this.anchoHoja + 8, y + 12, x + this.anchoHoja + 9, y + h - 12,
                Paleta.conAlfa(Paleta.papelAviso(), 0.045F * luz));

        if (this.width - (x + this.anchoHoja) > 150) {
            int rx = x + this.anchoHoja + 18;
            int rw = Math.min(98, this.width - rx - 14);
            if (rw > 38) {
                g.fill(rx, y + 2, rx + rw, y + 3, tenue);
                g.fill(rx, y + 2, rx + 1, y + 31, tenue);
                g.fill(rx + rw - 22, y + 8, rx + rw, y + 9, Paleta.conAlfa(Paleta.papelAviso(), 0.08F));
                String tag = "JOBS / LEVEL " + this.estadoFrame.indice();
                g.drawString(this.font, tag, rx, y + 8,
                        Paleta.conAlfa(Paleta.papelAviso(), 0.36F * luz), false);
                String file = SecretosJobs.codigoExpediente();
                g.drawString(this.font, file, rx, y + 19,
                        Paleta.conAlfa(Paleta.papelAviso(), 0.17F * luz), false);
            }
        }
    }

    private void estadoInstalacion(GuiGraphics grafico) {
        if (!ConfigTurno.mostrarEstadoInstalacion()) return;
        Component estado;
        if (this.estadoFrame.enSuspension()) estado = Component.translatable("jobsmenu.estado.suspension");
        else if (this.estadoFrame.enTransicion()) estado = Component.translatable("jobsmenu.estado.transicion");
        else estado = Component.translatable("jobsmenu.estado.normal");
        int ancho = this.font.width(estado);
        int x = Math.max(8, this.width - ancho - 12);
        int y = this.height - 13;
        int color = Paleta.conAlfa(Paleta.tintaSecundaria(), this.estadoFrame.enSuspension() ? 0.78F : 0.52F);
        grafico.drawString(this.font, estado, x, y, color, false);
    }

    private void credito(GuiGraphics grafico) {
        float alfa = GestorMusica.creditoAlfa();
        if (alfa <= 0.02F) return;
        Component titulo = Component.translatable("jobsmenu.credito.titulo");
        Component autor = Component.translatable("jobsmenu.credito.autor");
        int margen = 12;
        int y = ConfigTurno.mostrarCuentaRegresiva() ? margen + 34 : margen;
        int anchoTitulo = this.font.width(titulo);
        int anchoAutor = this.font.width(autor);
        int ancho = Math.max(anchoTitulo, anchoAutor);
        int izq = this.width - margen - ancho;
        grafico.fill(izq - 6, y, izq - 5, y + 19, Paleta.conAlfa(Paleta.papelAviso(), 0.45F * alfa));
        grafico.drawString(this.font, titulo, this.width - margen - anchoTitulo, y,
                Paleta.conAlfa(Paleta.papelAviso(), 0.90F * alfa), false);
        grafico.drawString(this.font, autor, this.width - margen - anchoAutor, y + 10,
                Paleta.conAlfa(Paleta.papelAviso(), 0.55F * alfa), false);
    }

    private float tinta() { return 0.10F + 0.90F * this.estadoFrame.luz(); }

    private void seguirNivel() {
        int ahora = this.estadoFrame.indice();
        if (ahora != this.nivelVisto) {
            this.nivelVisto = ahora;
            this.desdeCambio = this.estadoFrame.ahora();
        }
    }

    @Override
    public void renderBackground(GuiGraphics grafico) {
        if (this.estadoFrame == null) this.estadoFrame = RotacionNiveles.capturar();
        EscenaNivel.dibujar(grafico, this.width, this.height, this.estadoFrame);
    }

    private void hoja(GuiGraphics grafico) {
        float luz = this.estadoFrame == null ? RotacionNiveles.luzDisponible() : this.estadoFrame.luz();
        com.santipdr.jobsmenu.client.ui.HojaPapel.dibujar(grafico,
                this.hojaX, this.hojaY, this.hojaX + this.anchoHoja, this.hojaY + this.hojaAlto, true, luz);
    }

    private void cabecera(GuiGraphics grafico) {
        int x = this.hojaX + margenHoja();
        int ancho = Math.max(1, this.anchoHoja - 2 * margenHoja());
        int y = this.hojaY + margenHoja();
        float tinta = tinta();
        grafico.pose().pushPose();
        grafico.pose().translate(x, y, 0.0D);
        float escalaTitulo = (this.compacta ? 1.0F : 2.0F) * this.escalaTipografia;
        grafico.pose().scale(escalaTitulo, escalaTitulo, 1.0F);
        grafico.drawString(this.font, Component.translatable("jobsmenu.titulo"), 0, 0,
                Paleta.conAlfa(Paleta.tintaPrincipal(), tinta), false);
        grafico.pose().popPose();
        y += altoTitulo() + aireTitulo();
        y = parrafo(grafico, this.lineasSubtitulo, x, y, Paleta.conAlfa(Paleta.tintaSecundaria(), tinta));
        y += aireRegla();
        grafico.fill(x, y, x + ancho, y + 1, Paleta.conAlfa(Paleta.tintaSecundaria(), 0.45F * tinta));
        grafico.fill(x, y + 2, x + Math.max(12, ancho / 6), y + 3,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.18F * tinta));
        y += 1 + aireRegla();
        int n = this.estadoFrame == null ? RotacionNiveles.indiceActual() : this.estadoFrame.indice();
        y = parrafo(grafico, this.porNumeroNivelActual.get(n), x, y,
                Paleta.conAlfa(Paleta.tintaSecundaria(), tinta));
        y = parrafo(grafico, this.porNumeroTarifa.get(n), x, y,
                Paleta.conAlfa(Paleta.tintaPrincipal(), tinta));
        if (ConfigTurno.mostrarFecha()) parrafo(grafico, this.lineasFecha, x, y,
                Paleta.conAlfa(Paleta.tintaSecundaria(), 0.70F * tinta));
    }

    private int parrafo(GuiGraphics grafico, List<FormattedCharSequence> lineas, int x, int y, int color) {
        for (FormattedCharSequence linea : lineas) {
            if (this.escalaTipografia == 1.0F) grafico.drawString(this.font, linea, x, y, color, false);
            else {
                grafico.pose().pushPose();
                grafico.pose().translate(x, y, 0.0D);
                grafico.pose().scale(this.escalaTipografia, this.escalaTipografia, 1.0F);
                grafico.drawString(this.font, linea, 0, 0, color, false);
                grafico.pose().popPose();
            }
            y += altoLinea();
        }
        return y;
    }

    private void ronda(GuiGraphics grafico) {
        boolean destellosReducidos = ConfigTurno.destellosReducidos() || !ConfigTurno.escenaViva();
        long restante = RelojAparicion.restanteMs(this.estadoFrame.ahora());
        Component rotulo;
        if (RelojAparicion.enRonda(restante)) rotulo = Component.translatable("jobsmenu.reloj.encurso");
        else if (RelojAparicion.inminente(restante)) rotulo = Component.translatable("jobsmenu.reloj.inminente");
        else rotulo = Component.translatable("jobsmenu.reloj.proxima");
        String tiempo = RelojAparicion.formatoRestante(restante);
        int color = RelojAparicion.color(destellosReducidos, restante, this.estadoFrame.ahora());
        int margen = 12;
        int anchoRotulo = this.font.width(rotulo);
        int anchoTiempo = this.font.width(tiempo);
        int x0 = this.width - margen - Math.max(anchoRotulo, anchoTiempo);
        grafico.fill(x0 - 8, margen - 6, this.width - margen + 6, margen + 26,
                Paleta.conAlfa(Paleta.VANO, 0.45F));
        grafico.fill(x0 - 8, margen + 26, this.width - margen + 6, margen + 27,
                Paleta.conAlfa(Paleta.papelAviso(), 0.10F));
        grafico.drawString(this.font, rotulo, this.width - margen - anchoRotulo, margen,
                Paleta.conAlfa(Paleta.papelAviso(), 0.80F), false);
        grafico.drawString(this.font, tiempo, this.width - margen - anchoTiempo, margen + 13, color, false);
    }

    private void rotuloNivel(GuiGraphics grafico) {
        Nivel nivel = this.estadoFrame.nivel();
        float entrada = (this.estadoFrame.ahora() - this.desdeCambio) / (float) ENTRADA_ROTULO_MS;
        entrada = Math.max(0.0F, Math.min(1.0F, entrada));
        float alfa = entrada * this.estadoFrame.luz();
        if (alfa <= 0.02F) return;
        Component nombre = Component.translatable("jobsmenu." + nivel.clave + ".nombre");
        Component nota;
        if (this.estadoFrame.enSuspension()) nota = Component.translatable("jobsmenu.suspension.nota");
        else {
            int cual = (int) (Math.floorDiv(this.estadoFrame.ahora(), 1000L) / 27L % NOTAS_POR_NIVEL);
            nota = Component.translatable("jobsmenu." + nivel.clave + ".nota" + cual);
        }
        int x = 12;
        int y = this.height - 30;
        int finHoja = this.hojaY + this.hojaAlto;
        if (!ConfigTurno.interfazMinima() && y < finHoja + 4) {
            x = this.hojaX + this.anchoHoja + 14;
            y = Math.max(12, this.height - 30);
            int anchoNecesario = Math.max(this.font.width(nombre), this.font.width(nota));
            if (x + anchoNecesario > this.width - 12) return;
        }
        grafico.fill(x, y - 3, x + Math.max(18, this.font.width(nombre) / 3), y - 2,
                Paleta.conAlfa(Paleta.papelAviso(), 0.12F * alfa));
        grafico.drawString(this.font, nombre, x, y,
                Paleta.conAlfa(Paleta.papelAviso(), 0.85F * alfa), false);
        grafico.drawString(this.font, nota, x, y + 11,
                Paleta.conAlfa(Paleta.papelAviso(), 0.45F * alfa), false);
    }

    private void easterEggs(GuiGraphics g) {
        if (this.compacta || ConfigTurno.interfazMinima()) return;
        if (SecretosJobs.hora333()) {
            Component texto = Component.translatable("jobsmenu.secreto.333");
            int w = this.font.width(texto);
            int x = Math.max(12, this.width - w - 14);
            int y = Math.max(46, this.height / 2);
            g.drawString(this.font, texto, x, y,
                    Paleta.conAlfa(Paleta.papelAviso(), 0.24F * this.estadoFrame.luz()), false);
        } else if (SecretosJobs.expedienteRaro()) {
            String codigo = SecretosJobs.codigoExpediente();
            g.drawString(this.font, codigo, 12, 12,
                    Paleta.conAlfa(Paleta.papelAviso(), 0.18F * this.estadoFrame.luz()), false);
        }
    }

    @Override
    public boolean keyPressed(int codigo, int escaneo, int modificadores) {
        if (codigo == GLFW.GLFW_KEY_M) {
            MezclaAudio.alternarSilencio();
            return true;
        }
        if (codigo == GLFW.GLFW_KEY_F && ConfigTurno.rotarNiveles()) {
            RotacionNiveles.adelantar();
            MezclaAudio.gesto(SonidosNivel.UI_ALTERNAR, 0.60F);
            return true;
        }
        return super.keyPressed(codigo, escaneo, modificadores);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    public boolean shouldCloseOnEsc() { return false; }
}
