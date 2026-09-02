package com.santipdr.jobsmenu.config;

/**
 * Perfiles de experiencia de alto nivel. No agregan estados paralelos: cada
 * perfil escribe las preferencias reales de ConfigTurno para que luego puedan
 * seguir editandose una por una desde la interfaz.
 */
public final class PerfilesJobs {

    public enum Perfil {
        EQUILIBRADO("equilibrado"),
        INMERSIVO("inmersivo"),
        RENDIMIENTO("rendimiento"),
        ACCESIBLE("accesible"),
        MINIMO("minimo");

        private final String id;

        Perfil(String id) {
            this.id = id;
        }

        public String id() {
            return this.id;
        }

        public String claveNombre() {
            return "jobsmenu.perfil." + this.id;
        }

        public String claveDetalle() {
            return claveNombre() + ".detalle";
        }
    }

    private PerfilesJobs() {
    }

    public static void aplicar(Perfil perfil) {
        if (perfil == null) return;

        // Salir primero del perfil compuesto evita que sus valores calculados
        // interfieran mientras se escribe un perfil distinto.
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

    public static Perfil actual() {
        if (ConfigTurno.perfilAccesible()) return Perfil.ACCESIBLE;
        if (ConfigTurno.interfazMinima() && ConfigTurno.bajoConsumo()) return Perfil.MINIMO;
        if (ConfigTurno.bajoConsumo() && ConfigTurno.movimientoReducido()) return Perfil.RENDIMIENTO;
        if (!ConfigTurno.bajoConsumo()
                && ConfigTurno.eventosAmbientales()
                && ConfigTurno.presenciaFondo()
                && ConfigTurno.volumenAmbientePorcentaje() >= 64) {
            return Perfil.INMERSIVO;
        }
        if (!ConfigTurno.bajoConsumo()
                && !ConfigTurno.movimientoReducido()
                && !ConfigTurno.interfazMinima()) {
            return Perfil.EQUILIBRADO;
        }
        return null;
    }
}
