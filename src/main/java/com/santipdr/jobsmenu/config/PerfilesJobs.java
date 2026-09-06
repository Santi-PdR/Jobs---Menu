package com.santipdr.jobsmenu.config;

/**
 * Perfiles de experiencia de alto nivel. No agregan estados paralelos: cada
 * perfil escribe las preferencias reales de ConfigTurno para que luego puedan
 * seguir editandose una por una desde la interfaz.
 */
public final class PerfilesJobs {

    public enum Perfil {
        EQUILIBRADO,
        INMERSIVO,
        RENDIMIENTO,
        ACCESIBLE,
        MINIMO;

        public String claveNombre() {
            return switch (this) {
                case EQUILIBRADO -> "jobsmenu.ajustes.categoria.visual";
                case INMERSIVO -> "jobsmenu.ajustes.escena";
                case RENDIMIENTO -> "jobsmenu.ajustes.bajoconsumo";
                case ACCESIBLE -> "jobsmenu.ajustes.perfil";
                case MINIMO -> "jobsmenu.ajustes.interfaz";
            };
        }

        public String claveDetalle() {
            return switch (this) {
                case EQUILIBRADO -> "jobsmenu.ajustes.escena.detalle";
                case INMERSIVO -> "jobsmenu.ajustes.eventos.detalle";
                case RENDIMIENTO -> "jobsmenu.ajustes.bajoconsumo.detalle";
                case ACCESIBLE -> "jobsmenu.ajustes.perfil.detalle";
                case MINIMO -> "jobsmenu.ajustes.interfaz.detalle";
            };
        }
    }

    private PerfilesJobs() {
    }

    public static void aplicar(Perfil perfil) {
        if (perfil == null) return;
        ConfigTurno.fijarPerfilAccesible(false);

        switch (perfil) {
            case EQUILIBRADO -> aplicarEquilibrado();
            case INMERSIVO -> aplicarInmersivo();
            case RENDIMIENTO -> aplicarRendimiento();
            case ACCESIBLE -> aplicarAccesible();
            case MINIMO -> aplicarMinimo();
        }
        ConfigTurno.guardarPendiente();
    }

    private static void baseComun() {
        ConfigTurno.fijarMenuPropio(true);
        ConfigTurno.fijarPausaPropia(true);
        ConfigTurno.fijarEscenaViva(true);
        ConfigTurno.fijarRotarNiveles(true);
        ConfigTurno.fijarMostrarCuentaRegresiva(true);
        ConfigTurno.fijarMostrarFecha(true);
        ConfigTurno.fijarMostrarEstadoInstalacion(true);
        ConfigTurno.fijarSonidoBotones(true);
        ConfigTurno.fijarSonidoAmbiente(true);
        ConfigTurno.fijarMusicaMenu(true);
        ConfigTurno.fijarCreditoMusica(true);
    }

    private static void aplicarEquilibrado() {
        baseComun();
        ConfigTurno.fijarMovimientoReducido(false);
        ConfigTurno.fijarDestellosReducidos(false);
        ConfigTurno.fijarAltoContraste(false);
        ConfigTurno.fijarTextoGrande(false);
        ConfigTurno.fijarPapelLimpio(false);
        ConfigTurno.fijarInterfazMinima(false);
        ConfigTurno.fijarGuiaLectura(true);
        ConfigTurno.fijarAvisosRotativos(true);
        ConfigTurno.fijarEventosAmbientales(true);
        ConfigTurno.fijarPresenciaFondo(true);
        ConfigTurno.fijarRespiracionCamara(true);
        ConfigTurno.fijarSuspensionRara(true);
        ConfigTurno.fijarRotacionCalma(false);
        ConfigTurno.fijarBajoConsumo(false);
        ConfigTurno.fijarDuracionEstancia(24);
        ConfigTurno.fijarDuracionAvisos(7);
        ConfigTurno.fijarVolumenAviso(100);
        ConfigTurno.fijarVolumenMusica(70);
        ConfigTurno.fijarVolumenAmbiente(55);
    }

    private static void aplicarInmersivo() {
        baseComun();
        ConfigTurno.fijarMovimientoReducido(false);
        ConfigTurno.fijarDestellosReducidos(false);
        ConfigTurno.fijarAltoContraste(false);
        ConfigTurno.fijarTextoGrande(false);
        ConfigTurno.fijarPapelLimpio(false);
        ConfigTurno.fijarInterfazMinima(false);
        ConfigTurno.fijarGuiaLectura(true);
        ConfigTurno.fijarAvisosRotativos(true);
        ConfigTurno.fijarEventosAmbientales(true);
        ConfigTurno.fijarPresenciaFondo(true);
        ConfigTurno.fijarRespiracionCamara(true);
        ConfigTurno.fijarSuspensionRara(true);
        ConfigTurno.fijarRotacionCalma(false);
        ConfigTurno.fijarBajoConsumo(false);
        ConfigTurno.fijarDuracionEstancia(20);
        ConfigTurno.fijarDuracionAvisos(6);
        ConfigTurno.fijarVolumenAviso(100);
        ConfigTurno.fijarVolumenMusica(76);
        ConfigTurno.fijarVolumenAmbiente(68);
    }

    private static void aplicarRendimiento() {
        baseComun();
        ConfigTurno.fijarMovimientoReducido(true);
        ConfigTurno.fijarDestellosReducidos(true);
        ConfigTurno.fijarAltoContraste(false);
        ConfigTurno.fijarTextoGrande(false);
        ConfigTurno.fijarPapelLimpio(true);
        ConfigTurno.fijarInterfazMinima(false);
        ConfigTurno.fijarGuiaLectura(true);
        ConfigTurno.fijarAvisosRotativos(true);
        ConfigTurno.fijarEventosAmbientales(false);
        ConfigTurno.fijarPresenciaFondo(false);
        ConfigTurno.fijarRespiracionCamara(false);
        ConfigTurno.fijarSuspensionRara(false);
        ConfigTurno.fijarRotacionCalma(true);
        ConfigTurno.fijarBajoConsumo(true);
        ConfigTurno.fijarDuracionEstancia(36);
        ConfigTurno.fijarDuracionAvisos(9);
        ConfigTurno.fijarVolumenAviso(100);
        ConfigTurno.fijarVolumenMusica(62);
        ConfigTurno.fijarVolumenAmbiente(48);
    }

    private static void aplicarAccesible() {
        baseComun();
        ConfigTurno.fijarPapelLimpio(true);
        ConfigTurno.fijarInterfazMinima(false);
        ConfigTurno.fijarGuiaLectura(true);
        ConfigTurno.fijarAvisosRotativos(true);
        ConfigTurno.fijarEventosAmbientales(false);
        ConfigTurno.fijarPresenciaFondo(false);
        ConfigTurno.fijarRespiracionCamara(false);
        ConfigTurno.fijarSuspensionRara(false);
        ConfigTurno.fijarRotacionCalma(true);
        ConfigTurno.fijarBajoConsumo(false);
        ConfigTurno.fijarDuracionEstancia(38);
        ConfigTurno.fijarDuracionAvisos(10);
        ConfigTurno.fijarVolumenAviso(90);
        ConfigTurno.fijarVolumenMusica(52);
        ConfigTurno.fijarVolumenAmbiente(42);
        ConfigTurno.fijarPerfilAccesible(true);
    }

    private static void aplicarMinimo() {
        baseComun();
        ConfigTurno.fijarMovimientoReducido(true);
        ConfigTurno.fijarDestellosReducidos(true);
        ConfigTurno.fijarAltoContraste(false);
        ConfigTurno.fijarTextoGrande(false);
        ConfigTurno.fijarPapelLimpio(true);
        ConfigTurno.fijarInterfazMinima(true);
        ConfigTurno.fijarGuiaLectura(true);
        ConfigTurno.fijarAvisosRotativos(false);
        ConfigTurno.fijarEventosAmbientales(false);
        ConfigTurno.fijarPresenciaFondo(false);
        ConfigTurno.fijarRespiracionCamara(false);
        ConfigTurno.fijarSuspensionRara(false);
        ConfigTurno.fijarRotacionCalma(true);
        ConfigTurno.fijarBajoConsumo(true);
        ConfigTurno.fijarDuracionEstancia(45);
        ConfigTurno.fijarDuracionAvisos(10);
        ConfigTurno.fijarVolumenAviso(85);
        ConfigTurno.fijarVolumenMusica(50);
        ConfigTurno.fijarVolumenAmbiente(40);
    }

    /**
     * Devuelve un perfil solo cuando el estado actual coincide con todos los
     * valores que ese preset controla. Antes bastaban dos o tres senales y una
     * configuracion personalizada podia seguir apareciendo como EQUILIBRADO o
     * INMERSIVO aunque ya no correspondiera. Los campos que el preset no toca,
     * como pista musical o nivel fijo, se ignoran a proposito.
     */
    public static Perfil actual() {
        if (coincideAccesible()) return Perfil.ACCESIBLE;
        if (coincideMinimo()) return Perfil.MINIMO;
        if (coincideRendimiento()) return Perfil.RENDIMIENTO;
        if (coincideInmersivo()) return Perfil.INMERSIVO;
        if (coincideEquilibrado()) return Perfil.EQUILIBRADO;
        return null;
    }

    private static boolean baseComunActual(boolean exigirEstadoVisible) {
        return ConfigTurno.menuPropio()
                && ConfigTurno.pausaPropia()
                && ConfigTurno.escenaViva()
                && ConfigTurno.rotarNivelesBruto()
                && ConfigTurno.mostrarCuentaRegresivaBruto()
                && ConfigTurno.mostrarFechaBruto()
                && (!exigirEstadoVisible || ConfigTurno.mostrarEstadoInstalacion())
                && ConfigTurno.sonidoBotones()
                && ConfigTurno.sonidoAmbiente()
                && ConfigTurno.musicaMenu()
                && ConfigTurno.creditoMusica();
    }

    private static boolean coincideEquilibrado() {
        return baseComunActual(true)
                && !ConfigTurno.movimientoReducido()
                && !ConfigTurno.destellosReducidos()
                && !ConfigTurno.altoContraste()
                && !ConfigTurno.textoGrande()
                && !ConfigTurno.papelLimpio()
                && !ConfigTurno.interfazMinima()
                && ConfigTurno.guiaLectura()
                && ConfigTurno.avisosRotativosBruto()
                && ConfigTurno.eventosAmbientales()
                && ConfigTurno.presenciaFondo()
                && ConfigTurno.respiracionCamara()
                && ConfigTurno.suspensionRara()
                && !ConfigTurno.rotacionCalma()
                && !ConfigTurno.bajoConsumo()
                && ConfigTurno.duracionEstancia() == 24
                && ConfigTurno.duracionAvisos() == 7
                && ConfigTurno.volumenAvisoPorcentaje() == 100
                && ConfigTurno.volumenMusicaPorcentaje() == 70
                && ConfigTurno.volumenAmbientePorcentaje() == 55;
    }

    private static boolean coincideInmersivo() {
        return baseComunActual(true)
                && !ConfigTurno.movimientoReducido()
                && !ConfigTurno.destellosReducidos()
                && !ConfigTurno.altoContraste()
                && !ConfigTurno.textoGrande()
                && !ConfigTurno.papelLimpio()
                && !ConfigTurno.interfazMinima()
                && ConfigTurno.guiaLectura()
                && ConfigTurno.avisosRotativosBruto()
                && ConfigTurno.eventosAmbientales()
                && ConfigTurno.presenciaFondo()
                && ConfigTurno.respiracionCamara()
                && ConfigTurno.suspensionRara()
                && !ConfigTurno.rotacionCalma()
                && !ConfigTurno.bajoConsumo()
                && ConfigTurno.duracionEstancia() == 20
                && ConfigTurno.duracionAvisos() == 6
                && ConfigTurno.volumenAvisoPorcentaje() == 100
                && ConfigTurno.volumenMusicaPorcentaje() == 76
                && ConfigTurno.volumenAmbientePorcentaje() == 68;
    }

    private static boolean coincideRendimiento() {
        return baseComunActual(true)
                && ConfigTurno.movimientoReducido()
                && ConfigTurno.destellosReducidos()
                && !ConfigTurno.altoContraste()
                && !ConfigTurno.textoGrande()
                && ConfigTurno.papelLimpio()
                && !ConfigTurno.interfazMinima()
                && ConfigTurno.guiaLectura()
                && ConfigTurno.avisosRotativosBruto()
                && !ConfigTurno.eventosAmbientales()
                && !ConfigTurno.presenciaFondo()
                && !ConfigTurno.respiracionCamara()
                && !ConfigTurno.suspensionRara()
                && ConfigTurno.rotacionCalma()
                && ConfigTurno.bajoConsumo()
                && ConfigTurno.duracionEstancia() == 36
                && ConfigTurno.duracionAvisos() == 9
                && ConfigTurno.volumenAvisoPorcentaje() == 100
                && ConfigTurno.volumenMusicaPorcentaje() == 62
                && ConfigTurno.volumenAmbientePorcentaje() == 48;
    }

    private static boolean coincideAccesible() {
        return baseComunActual(true)
                && ConfigTurno.perfilAccesible()
                && ConfigTurno.papelLimpio()
                && !ConfigTurno.interfazMinima()
                && ConfigTurno.guiaLectura()
                && ConfigTurno.avisosRotativosBruto()
                && !ConfigTurno.eventosAmbientales()
                && !ConfigTurno.presenciaFondo()
                && !ConfigTurno.respiracionCamara()
                && !ConfigTurno.suspensionRara()
                && ConfigTurno.rotacionCalma()
                && !ConfigTurno.bajoConsumo()
                && ConfigTurno.duracionEstancia() == 38
                && ConfigTurno.duracionAvisos() == 10
                && ConfigTurno.volumenAvisoPorcentaje() == 90
                && ConfigTurno.volumenMusicaPorcentaje() == 52
                && ConfigTurno.volumenAmbientePorcentaje() == 42;
    }

    private static boolean coincideMinimo() {
        return baseComunActual(false)
                && ConfigTurno.movimientoReducido()
                && ConfigTurno.destellosReducidos()
                && !ConfigTurno.altoContraste()
                && !ConfigTurno.textoGrande()
                && ConfigTurno.papelLimpio()
                && ConfigTurno.interfazMinima()
                && ConfigTurno.guiaLectura()
                && !ConfigTurno.avisosRotativosBruto()
                && !ConfigTurno.eventosAmbientales()
                && !ConfigTurno.presenciaFondo()
                && !ConfigTurno.respiracionCamara()
                && !ConfigTurno.suspensionRara()
                && ConfigTurno.rotacionCalma()
                && ConfigTurno.bajoConsumo()
                && ConfigTurno.duracionEstancia() == 45
                && ConfigTurno.duracionAvisos() == 10
                && ConfigTurno.volumenAvisoPorcentaje() == 85
                && ConfigTurno.volumenMusicaPorcentaje() == 50
                && ConfigTurno.volumenAmbientePorcentaje() == 40;
    }
}
